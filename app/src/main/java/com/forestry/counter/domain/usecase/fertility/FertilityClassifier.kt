package com.forestry.counter.domain.usecase.fertility

import com.forestry.counter.domain.model.ClimateZone
import com.forestry.counter.domain.model.Tige
import kotlin.math.PI

/**
 * Niveau de confiance de l'estimation de fertilité.
 */
enum class ConfidenceLevel(val label: String) {
    HIGH("Fiable"),
    MEDIUM("Indicatif"),
    LOW("Approximatif"),
    INSUFFICIENT("Données insuffisantes")
}

/**
 * Résultat de la classification de fertilité pour une essence dans une placette.
 */
data class FertilityResult(
    val essenceCode: String,
    val essenceName: String,
    val fertilityClass: FertilityClass,
    val confidence: ConfidenceLevel,
    val dominantHeightM: Double?,       // Hauteur dominante estimée
    val loreyHeightM: Double?,          // Hauteur de Lorrey (pondérée G)
    val treeCount: Int,
    val avgDiamCm: Double,
    val climateZone: ClimateZone,
    val zoneCompatibility: ZoneCompatibility,
    val notes: List<String>
)

enum class ZoneCompatibility(val label: String, val icon: String) {
    OPTIMAL("Zone optimale", "✓"),
    ACCEPTABLE("Zone acceptable", "~"),
    SUBOPTIMAL("Zone marginale", "!")
}

/**
 * Moteur de classification automatique des classes de fertilité.
 *
 * Méthode : estimation de la hauteur dominante (H0) à partir des
 * mesures dendrométriques disponibles, comparée aux seuils de référence
 * par essence (FertilityReference).
 *
 * Sans connaissance de l'âge des arbres, on utilise :
 *   1. La hauteur des arbres co-dominants (top 20% par diamètre) si disponible
 *   2. La hauteur de Lorey (pondérée par surface terrière)
 *   3. Un proxy par rapport au diamètre d'exploitation typique
 *
 * Sources : ONF guides de sylviculture, CNPF, IFN France
 */
object FertilityClassifier {

    /**
     * Classifie la fertilité pour chaque essence présente dans la liste de tiges.
     *
     * @param tiges       Toutes les tiges de la placette ou parcelle
     * @param climateZone Zone bioclimatique détectée ou saisie
     * @param essenceNames Map code → nom commun (pour affichage)
     */
    fun classify(
        tiges: List<Tige>,
        climateZone: ClimateZone,
        essenceNames: Map<String, String> = emptyMap()
    ): List<FertilityResult> {
        if (tiges.isEmpty()) return emptyList()

        return tiges
            .groupBy { it.essenceCode.uppercase() }
            .mapNotNull { (code, tigesEssence) ->
                val ref = FertilityReference.forCode(code) ?: return@mapNotNull null
                classifyEssence(code, tigesEssence, ref, climateZone, essenceNames[code])
            }
            .sortedByDescending { it.treeCount }
    }

    private fun classifyEssence(
        code: String,
        tiges: List<Tige>,
        ref: SpeciesFertilityRef,
        climateZone: ClimateZone,
        nameOverride: String?
    ): FertilityResult {
        val essenceName = nameOverride ?: ref.commonName
        val notes = mutableListOf<String>()

        // ── 1. Tiges avec mesures de hauteur ──
        val tigesWithH = tiges.filter { (it.hauteurM ?: 0.0) > 1.0 }
        val avgDiam = tiges.map { it.diamCm }.average()

        // ── 2. Hauteur dominante : moyenne des 20% plus grands diamètres avec hauteur ──
        val dominantH: Double? = if (tigesWithH.size >= 2) {
            val cutoff = (tigesWithH.size * 0.8).toInt().coerceAtLeast(1)
            tigesWithH.sortedByDescending { it.diamCm }.take(
                (tigesWithH.size - cutoff).coerceAtLeast(1)
            ).mapNotNull { it.hauteurM }.average().takeIf { !it.isNaN() }
        } else if (tigesWithH.size == 1) {
            tigesWithH[0].hauteurM
        } else null

        // ── 3. Hauteur de Lorey (pondérée par surface terrière) ──
        val loreyH: Double? = if (tigesWithH.size >= 2) {
            val sumG = tigesWithH.sumOf { PI / 4.0 * (it.diamCm / 100.0).let { d -> d * d } }
            if (sumG > 0) {
                val sumGH = tigesWithH.sumOf {
                    val g = PI / 4.0 * (it.diamCm / 100.0).let { d -> d * d }
                    g * (it.hauteurM ?: 0.0)
                }
                (sumGH / sumG).takeIf { !it.isNaN() }
            } else null
        } else null

        // ── 4. Estimation de fertilité ──
        val (fertClass, confidence) = when {
            dominantH != null && tigesWithH.size >= 5 -> {
                // Haute confiance : hauteur dominante sur ≥ 5 tiges
                val cls = classFromHeight(dominantH, ref)
                notes.add("Hauteur dominante estimée : ${String.format("%.1f", dominantH)} m")
                cls to ConfidenceLevel.HIGH
            }
            dominantH != null && tigesWithH.size >= 3 -> {
                val cls = classFromHeight(dominantH, ref)
                notes.add("Basé sur ${tigesWithH.size} mesures de hauteur")
                cls to ConfidenceLevel.MEDIUM
            }
            loreyH != null -> {
                // Utilise la hauteur de Lorey comme proxy — moins précis
                val cls = classFromHeight(loreyH * 1.05, ref) // légère correction Lorey→dominant
                notes.add("Hauteur de Lorey utilisée (proxy)")
                cls to ConfidenceLevel.MEDIUM
            }
            tiges.size >= 3 && avgDiam >= ref.typicalHarvestDiamCm * 0.5 -> {
                // Proxy par diamètre : estimation approximative
                val ratio = avgDiam / ref.typicalHarvestDiamCm
                val cls = estimateFromDiamRatio(ratio, ref)
                notes.add("Estimé d'après le diamètre moyen (${String.format("%.0f", avgDiam)} cm)")
                cls to ConfidenceLevel.LOW
            }
            else -> {
                notes.add("Moins de 3 tiges mesurées")
                FertilityClass.UNKNOWN to ConfidenceLevel.INSUFFICIENT
            }
        }

        // ── 5. Compatibilité de la zone bioclimatique ──
        val zoneCompat = when {
            climateZone == ClimateZone.UNKNOWN -> ZoneCompatibility.ACCEPTABLE
            ref.preferredZones.contains(climateZone) -> ZoneCompatibility.OPTIMAL
            adjacentZones(climateZone).any { ref.preferredZones.contains(it) } -> ZoneCompatibility.ACCEPTABLE
            else -> ZoneCompatibility.SUBOPTIMAL
        }
        if (zoneCompat == ZoneCompatibility.SUBOPTIMAL) {
            notes.add("Zone ${climateZone.labelFr} marginale pour cette essence")
        }

        return FertilityResult(
            essenceCode = code,
            essenceName = essenceName,
            fertilityClass = fertClass,
            confidence = confidence,
            dominantHeightM = dominantH,
            loreyHeightM = loreyH,
            treeCount = tiges.size,
            avgDiamCm = avgDiam,
            climateZone = climateZone,
            zoneCompatibility = zoneCompat,
            notes = notes
        )
    }

    /** Convertit une hauteur en classe selon les seuils du référentiel. */
    private fun classFromHeight(h: Double, ref: SpeciesFertilityRef): FertilityClass {
        return ref.thresholds.firstOrNull { h >= it.first }?.second ?: FertilityClass.IV
    }

    /**
     * Estimation approximative basée sur le rapport diamètre actuel / diamètre d'exploitation.
     * Si les arbres sont proches du diamètre d'exploitation, on peut comparer la hauteur attendue.
     */
    private fun estimateFromDiamRatio(ratio: Double, ref: SpeciesFertilityRef): FertilityClass {
        // Sans hauteur, on estime prudemment depuis la classe médiane
        return when {
            ratio >= 0.9 -> FertilityClass.II  // arbres matures → probable classe II+
            ratio >= 0.6 -> FertilityClass.III
            else         -> FertilityClass.UNKNOWN
        }
    }

    /** Zones bioclimatiques proches (transition) */
    private fun adjacentZones(zone: ClimateZone): List<ClimateZone> = when (zone) {
        ClimateZone.ATLANTIQUE       -> listOf(ClimateZone.SEMI_OCEANIQUE)
        ClimateZone.SEMI_OCEANIQUE   -> listOf(ClimateZone.ATLANTIQUE, ClimateZone.CONTINENTALE, ClimateZone.MONTAGNARDE)
        ClimateZone.CONTINENTALE     -> listOf(ClimateZone.SEMI_OCEANIQUE, ClimateZone.MONTAGNARDE)
        ClimateZone.MONTAGNARDE      -> listOf(ClimateZone.SEMI_OCEANIQUE, ClimateZone.CONTINENTALE)
        ClimateZone.MEDITERRANEENNE  -> listOf(ClimateZone.SEMI_OCEANIQUE)
        else                         -> emptyList()
    }
}
