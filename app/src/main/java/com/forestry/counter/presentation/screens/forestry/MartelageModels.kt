package com.forestry.counter.presentation.screens.forestry

import com.forestry.counter.domain.calculation.ClassSynthesis
import com.forestry.counter.domain.calculation.ForestryCalculator
import com.forestry.counter.domain.calculation.ForestrySynthesisParams
import com.forestry.counter.domain.calculation.SynthesisTotals
import com.forestry.counter.domain.calculation.quality.WoodQualityGrade
import com.forestry.counter.domain.model.Essence
import com.forestry.counter.domain.model.Tige
import java.util.Locale
import kotlin.math.PI
import kotlin.math.sqrt

// Vue locale pour la synthèse (indépendante du scope de navigation)
enum class MartelageViewScope { PLACETTE, PARCELLE, GLOBAL }

/**
 * Calcule les agrégats de martelage pour un jeu de tiges donné.
 * Logique métier pure, extraite de l'écran Compose pour testabilité.
 */
suspend fun computeMartelageStats(
    tigesInScope: List<Tige>,
    surfaceM2: Double,
    selectedEssenceCodes: Set<String>,
    martelageHeights: Map<String, Map<Int, Double>>,
    synthesisParams: ForestrySynthesisParams?,
    diameterClasses: List<Int>,
    essences: List<Essence>,
    forestryCalculator: ForestryCalculator
): MartelageStats? {
    if (tigesInScope.isEmpty() || surfaceM2 <= 0.0) return null

    val surfaceHa = surfaceM2 / 10_000.0
    if (surfaceHa <= 0.0) return null

    fun normalizeEssenceCode(code: String): String = code.trim().uppercase(Locale.getDefault())

    val classes = if (diameterClasses.isNotEmpty()) diameterClasses else (5..120 step 5).toList()

    val byEssence = tigesInScope.groupBy { normalizeEssenceCode(it.essenceCode) }
    val perEssence = mutableListOf<PerEssenceStats>()

    val heightModes = synthesisParams?.heightModes.orEmpty()
    val missingHeightsByEssence = mutableMapOf<String, List<Int>>()
    byEssence.forEach { (code, tigesEss) ->
        if (selectedEssenceCodes.isNotEmpty() && code !in selectedEssenceCodes) return@forEach
        val byClass = tigesEss.groupBy { forestryCalculator.diameterClassFor(it.diamCm, classes) }
        val manual = martelageHeights[normalizeEssenceCode(code)].orEmpty()
        val missing = byClass.entries
            .filter { (diamClass, list) ->
                val hasMissingMeasured = list.any { it.hauteurM == null }
                if (!hasMissingMeasured) {
                    false
                } else {
                    val manualH = manual[diamClass]
                    val mode = heightModes.firstOrNull { it.essence.equals(code, true) && it.diamClass == diamClass }
                    val fixedH = mode?.mode?.equals("FIXED", ignoreCase = true) == true && (mode.fixed ?: 0.0) > 0.0
                    val sampleH = mode?.mode?.equals("SAMPLES", ignoreCase = true) == true && list.any { it.hauteurM != null }
                    val canResolve = manualH != null || fixedH || sampleH
                    !canResolve
                }
            }
            .map { it.key }
            .sorted()
        if (missing.isNotEmpty()) {
            missingHeightsByEssence[code] = missing
        }
    }
    val volumeAvailable = missingHeightsByEssence.isEmpty()
    val missingHeightEssenceCodes = missingHeightsByEssence.keys.sorted()
    val missingHeightEssenceNames = missingHeightEssenceCodes.map { code ->
        essences.firstOrNull { normalizeEssenceCode(it.code) == code }?.name ?: code
    }

    var nTotal = 0
    var gTotal = 0.0
    var vTotal = 0.0
    var revenueTotal = 0.0
    var unpricedVolumeTotal = 0.0
    val unpricedEssenceNames = mutableListOf<String>()
    var dmSum = 0.0
    var dmWeight = 0
    var hSum = 0.0
    var hWeight = 0
    var loreyGhSum = 0.0
    var loreyGSum = 0.0
    val allDiams = mutableListOf<Double>()

    // Distribution par classe (agrégée toutes essences confondues)
    val classDistMap = mutableMapOf<Int, Triple<Int, Double, Double>>() // class -> (n, g, v)

    // Distribution par qualité bois
    val qualityCounts = mutableMapOf<WoodQualityGrade, Int>()
    val qualityVolumes = mutableMapOf<WoodQualityGrade, Double>()
    var qualityAssessedCount = 0

    byEssence.forEach { (code, tigesEss) ->
        if (selectedEssenceCodes.isNotEmpty() && code !in selectedEssenceCodes) return@forEach

        val manualHeightsForEss = martelageHeights[normalizeEssenceCode(code)]
        val (rows, totals) = try {
            forestryCalculator.synthesisForEssence(
                essenceCode = code,
                classes = classes,
                tiges = tigesEss,
                manualHeights = manualHeightsForEss,
                method = null,
                params = synthesisParams,
                requireHeights = true
            )
        } catch (_: Throwable) {
            emptyList<ClassSynthesis>() to SynthesisTotals(0, null, null, null)
        }

        val vEss = if (volumeAvailable) (totals.vTotal ?: 0.0) else 0.0
        val gEss = tigesEss.sumOf { forestryCalculator.computeG(it.diamCm) }
        val revEss = if (volumeAvailable) rows.sumOf { it.valueSumEur ?: 0.0 } else 0.0
        val nEss = tigesEss.size

        nTotal += nEss
        gTotal += gEss
        vTotal += vEss
        revenueTotal += revEss
        tigesEss.forEach { allDiams += it.diamCm }
        totals.dmWeighted?.let { dm ->
            dmSum += dm * nEss
            dmWeight += nEss
        }
        totals.hMean?.let { hm ->
            hSum += hm * nEss
            hWeight += nEss
        }

        if (volumeAvailable) {
            val manual = manualHeightsForEss.orEmpty()
            tigesEss.forEach { t ->
                val diamClass = forestryCalculator.diameterClassFor(t.diamCm, classes)
                val h = t.hauteurM ?: manual[diamClass]
                if (h != null) {
                    val g = forestryCalculator.computeG(t.diamCm)
                    loreyGhSum += g * h
                    loreyGSum += g
                }
            }
        }

        // Distribution par classe
        rows.forEach { r ->
            val prev = classDistMap[r.diamClass] ?: Triple(0, 0.0, 0.0)
            val gClass = tigesEss.filter { forestryCalculator.diameterClassFor(it.diamCm, classes) == r.diamClass }
                .sumOf { forestryCalculator.computeG(it.diamCm) }
            classDistMap[r.diamClass] = Triple(
                prev.first + r.count,
                prev.second + gClass,
                prev.third + (if (volumeAvailable) (r.vSum ?: 0.0) else 0.0)
            )
        }

        // Qualité bois par tige
        tigesEss.forEach { t ->
            val grade = t.qualite?.let { q -> WoodQualityGrade.entries.getOrNull(q) }
            if (grade != null) {
                qualityCounts[grade] = (qualityCounts[grade] ?: 0) + 1
                qualityAssessedCount++
            }
        }

        val essenceName = essences.firstOrNull { normalizeEssenceCode(it.code) == code }?.name ?: code
        val dmEss = if (tigesEss.isNotEmpty()) tigesEss.sumOf { it.diamCm } / tigesEss.size else null
        val dgEss = if (nEss > 0 && gEss > 0.0) sqrt((4.0 * gEss) / (PI * nEss.toDouble())) * 100.0 else null
        val qualityEss = tigesEss.mapNotNull { t -> t.qualite?.let { WoodQualityGrade.entries.getOrNull(it) } }
        val dominantQuality = qualityEss.groupBy { it }.maxByOrNull { it.value.size }?.key

        if (volumeAvailable) {
            val unpricedVEss = rows.asSequence()
                .filter { r -> r.count > 0 && (r.vSum ?: 0.0) > 0.0 && r.valueSumEur == null }
                .sumOf { it.vSum ?: 0.0 }
            if (unpricedVEss > 0.0) {
                unpricedVolumeTotal += unpricedVEss
                unpricedEssenceNames += essenceName
            }
        }

        perEssence += PerEssenceStats(
            essenceCode = code,
            essenceName = essenceName,
            n = nEss,
            nPct = 0.0,
            vTotal = vEss,
            vPct = 0.0,
            vPerHa = if (surfaceHa > 0.0) vEss / surfaceHa else 0.0,
            gTotal = gEss,
            gPct = 0.0,
            gPerHa = if (surfaceHa > 0.0) gEss / surfaceHa else 0.0,
            dm = dmEss,
            dg = dgEss,
            meanPricePerM3 = if (volumeAvailable && vEss > 0.0 && revEss > 0.0) revEss / vEss else null,
            revenueTotal = if (volumeAvailable && revEss > 0.0) revEss else null,
            revenuePerHa = if (volumeAvailable && revEss > 0.0 && surfaceHa > 0.0) revEss / surfaceHa else null,
            dominantQuality = dominantQuality,
            qualityAssessedPct = if (tigesEss.isNotEmpty()) qualityEss.size.toDouble() / tigesEss.size * 100.0 else 0.0
        )
    }

    if (nTotal == 0 && vTotal == 0.0 && gTotal == 0.0) return null

    // Recalculer les pourcentages par essence
    val perEssenceWithPct = perEssence.map { row ->
        row.copy(
            nPct = if (nTotal > 0) row.n.toDouble() / nTotal * 100.0 else 0.0,
            gPct = if (gTotal > 0.0) row.gTotal / gTotal * 100.0 else 0.0,
            vPct = if (vTotal > 0.0) row.vTotal / vTotal * 100.0 else 0.0
        )
    }

    val nPerHa = if (surfaceHa > 0.0) nTotal / surfaceHa else 0.0
    val gPerHa = if (surfaceHa > 0.0) gTotal / surfaceHa else 0.0
    val vPerHa = if (surfaceHa > 0.0) vTotal / surfaceHa else 0.0
    val unpricedVolumePerHa = if (surfaceHa > 0.0) unpricedVolumeTotal / surfaceHa else 0.0
    val revenuePerHa = if (surfaceHa > 0.0 && revenueTotal > 0.0) revenueTotal / surfaceHa else null

    val dm = if (dmWeight > 0) dmSum / dmWeight else null
    val hMean = if (hWeight > 0) hSum / hWeight else null
    val dg = if (nTotal > 0 && gTotal > 0.0) sqrt((4.0 * gTotal) / (PI * nTotal.toDouble())) * 100.0 else null
    val hLorey = if (volumeAvailable && loreyGSum > 0.0) loreyGhSum / loreyGSum else null

    // Statistiques avancées sur les diamètres
    val dMin = allDiams.minOrNull()
    val dMax = allDiams.maxOrNull()
    val cvDiam = if (allDiams.size > 1 && dm != null && dm > 0.0) {
        val variance = allDiams.sumOf { (it - dm) * (it - dm) } / (allDiams.size - 1)
        (sqrt(variance) / dm) * 100.0
    } else null
    val ratioVG = if (volumeAvailable && gTotal > 0.0 && vTotal > 0.0) vTotal / gTotal else null

    // Distribution par classes triée
    val classDistribution = classDistMap.entries
        .sortedBy { it.key }
        .map { (cls, triple) -> ClassDistEntry(cls, triple.first, triple.second, if (volumeAvailable) triple.third else null) }

    // Distribution qualité agrégée
    val qualityDistribution = WoodQualityGrade.entries.map { grade ->
        QualityDistEntry(
            grade = grade,
            count = qualityCounts[grade] ?: 0,
            pct = if (qualityAssessedCount > 0) (qualityCounts[grade] ?: 0).toDouble() / qualityAssessedCount * 100.0 else 0.0
        )
    }.filter { it.count > 0 }

    return MartelageStats(
        nTotal = nTotal,
        nPerHa = nPerHa,
        gTotal = gTotal,
        gPerHa = gPerHa,
        vTotal = vTotal,
        vPerHa = vPerHa,
        unpricedVolumeTotal = unpricedVolumeTotal,
        unpricedVolumePerHa = unpricedVolumePerHa,
        unpricedEssenceNames = unpricedEssenceNames,
        revenueTotal = if (volumeAvailable && revenueTotal > 0.0) revenueTotal else null,
        revenuePerHa = if (volumeAvailable) revenuePerHa else null,
        dm = dm,
        meanH = if (volumeAvailable) hMean else null,
        dg = dg,
        hLorey = hLorey,
        dMin = dMin,
        dMax = dMax,
        cvDiam = cvDiam,
        ratioVG = ratioVG,
        surfaceHa = surfaceHa,
        classDistribution = classDistribution,
        qualityDistribution = qualityDistribution,
        qualityAssessedCount = qualityAssessedCount,
        qualityTotalCount = nTotal,
        perEssence = perEssenceWithPct.sortedBy { it.essenceName },
        volumeAvailable = volumeAvailable,
        missingHeightEssenceCodes = missingHeightEssenceCodes,
        missingHeightEssenceNames = missingHeightEssenceNames
    )
}

// Agrégats globaux martelage pour une vue donnée
data class MartelageStats(
    val nTotal: Int,
    val nPerHa: Double,
    val gTotal: Double,
    val gPerHa: Double,
    val vTotal: Double,
    val vPerHa: Double,
    val unpricedVolumeTotal: Double,
    val unpricedVolumePerHa: Double,
    val unpricedEssenceNames: List<String>,
    val revenueTotal: Double?,
    val revenuePerHa: Double?,
    val dm: Double?,
    val meanH: Double?,
    val dg: Double?,
    val hLorey: Double?,
    val dMin: Double?,
    val dMax: Double?,
    val cvDiam: Double?,
    val ratioVG: Double?,
    val surfaceHa: Double,
    val classDistribution: List<ClassDistEntry>,
    val qualityDistribution: List<QualityDistEntry>,
    val qualityAssessedCount: Int,
    val qualityTotalCount: Int,
    val perEssence: List<PerEssenceStats>,
    val volumeAvailable: Boolean,
    val missingHeightEssenceCodes: List<String>,
    val missingHeightEssenceNames: List<String>
)

data class ClassDistEntry(
    val diamClass: Int,
    val n: Int,
    val g: Double,
    val v: Double?
)

data class QualityDistEntry(
    val grade: WoodQualityGrade,
    val count: Int,
    val pct: Double
)

// Agrégats par essence pour le tableau
data class PerEssenceStats(
    val essenceCode: String,
    val essenceName: String,
    val n: Int,
    val nPct: Double,
    val vTotal: Double,
    val vPct: Double,
    val vPerHa: Double,
    val gTotal: Double,
    val gPct: Double,
    val gPerHa: Double,
    val dm: Double?,
    val dg: Double?,
    val meanPricePerM3: Double?,
    val revenueTotal: Double?,
    val revenuePerHa: Double?,
    val dominantQuality: WoodQualityGrade? = null,
    val qualityAssessedPct: Double = 0.0
)
