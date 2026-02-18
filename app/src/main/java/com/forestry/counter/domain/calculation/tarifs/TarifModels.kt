package com.forestry.counter.domain.calculation.tarifs

import kotlinx.serialization.Serializable

/**
 * Méthodes officielles de cubage utilisées en foresterie française.
 *
 * Références :
 * - Schaeffer (1 entrée) : Schaeffer, 1949 — Table de production, Annales ENEF, Nancy
 * - Schaeffer (2 entrées) : Schaeffer, 1949 — V = f(C130, H)
 * - Algan : Algan, 1958 — Tarifs de cubage pour Douglas, Épicéa, etc.
 * - Tarifs rapides IFN : Inventaire Forestier National — 36 tarifs à 1 entrée (D130)
 * - Tarifs lents IFN : Inventaire Forestier National — Tables à 2 entrées (D130 + H)
 * - FGH : V = F × G × H  (variante explicite de la méthode du coefficient de forme)
 * - Coefficient de forme : V = G × H × f  (méthode classique)
 */
enum class TarifMethod(val code: String, val label: String, val description: String, val entrees: Int) {
    SCHAEFFER_1E(
        code = "SCHAEFFER_1E",
        label = "Schaeffer 1 entrée",
        description = "V = a + b × C130²  — Tarif à 1 entrée (circonférence). Schaeffer, 1949.",
        entrees = 1
    ),
    SCHAEFFER_2E(
        code = "SCHAEFFER_2E",
        label = "Schaeffer 2 entrées",
        description = "V = a + b × C130² × H  — Tarif à 2 entrées (circonférence + hauteur). Schaeffer, 1949.",
        entrees = 2
    ),
    ALGAN(
        code = "ALGAN",
        label = "Algan",
        description = "V = a × D^b × H^c  — Coefficients par essence (Algan 1958, Pardé & Bouchon 1988). Adapté résineux et feuillus.",
        entrees = 2
    ),
    IFN_RAPIDE(
        code = "IFN_RAPIDE",
        label = "Tarif rapide IFN",
        description = "Tarifs IFN à 1 entrée (n° 1–36). V = f(D130). Inventaire Forestier National.",
        entrees = 1
    ),
    IFN_LENT(
        code = "IFN_LENT",
        label = "Tarif lent IFN",
        description = "Tables IFN à 2 entrées (D130, H). Inventaire Forestier National.",
        entrees = 2
    ),
    FGH(
        code = "FGH",
        label = "FGH",
        description = "V = F × G × H  — Variante explicite de la méthode du coefficient de forme (F = facteur de forme).",
        entrees = 2
    ),
    COEF_FORME(
        code = "COEF_FORME",
        label = "Coefficient de forme",
        description = "V = G × H × f  — Méthode classique avec coefficient de forme/décroissance.",
        entrees = 2
    );

    companion object {
        fun fromCode(code: String): TarifMethod? = entries.firstOrNull { it.code.equals(code, ignoreCase = true) }
    }
}

// ─────────────────────────────────────────────────────
// Coefficients Schaeffer 1 entrée : V = a + b × C²
// C en mètres, V en m³
// Source : Schaeffer 1949, tables publiées par l'ONF
// ─────────────────────────────────────────────────────
@Serializable
data class SchaefferOneEntryCoefs(
    val essence: String,
    val numero: Int,           // numéro du tarif Schaeffer (1–16)
    val a: Double,             // constante (m³)
    val b: Double              // coefficient sur C² (m³/m²)
) {
    fun volume(circonfCm: Double): Double {
        val cM = circonfCm / 100.0
        return (a + b * cM * cM).coerceAtLeast(0.0)
    }

    fun volumeFromDiam(diamCm: Double): Double {
        val cCm = diamCm * Math.PI
        return volume(cCm)
    }
}

// ─────────────────────────────────────────────────────
// Coefficients Schaeffer 2 entrées : V = a + b × C² × H
// C en mètres, H en mètres, V en m³
// ─────────────────────────────────────────────────────
@Serializable
data class SchaefferTwoEntryCoefs(
    val essence: String,
    val numero: Int,
    val a: Double,
    val b: Double
) {
    fun volume(circonfCm: Double, hauteurM: Double): Double {
        val cM = circonfCm / 100.0
        return (a + b * cM * cM * hauteurM).coerceAtLeast(0.0)
    }

    fun volumeFromDiam(diamCm: Double, hauteurM: Double): Double {
        val cCm = diamCm * Math.PI
        return volume(cCm, hauteurM)
    }
}

// ─────────────────────────────────────────────────────
// Coefficients Algan : V = a × D^b × H^c
// D en cm, H en mètres, V en m³
// Source : Algan 1958, peuplements réguliers
// ─────────────────────────────────────────────────────
@Serializable
data class AlganCoefs(
    val essence: String,
    val a: Double,
    val b: Double,
    val c: Double
) {
    fun volume(diamCm: Double, hauteurM: Double): Double {
        if (diamCm <= 0.0 || hauteurM <= 0.0) return 0.0
        return (a * Math.pow(diamCm, b) * Math.pow(hauteurM, c)).coerceAtLeast(0.0)
    }
}

// ─────────────────────────────────────────────────────
// Tarif rapide IFN : 36 tarifs numérotés
// V = a₀ + a₁×D + a₂×D²  (D en cm, V en dm³ → converti en m³)
// Source : IFN, documentation technique
// ─────────────────────────────────────────────────────
@Serializable
data class IfnRapideCoefs(
    val numero: Int,           // 1–36
    val a0: Double,            // dm³
    val a1: Double,            // dm³/cm
    val a2: Double             // dm³/cm²
) {
    fun volumeDm3(diamCm: Double): Double {
        return (a0 + a1 * diamCm + a2 * diamCm * diamCm).coerceAtLeast(0.0)
    }

    fun volumeM3(diamCm: Double): Double = volumeDm3(diamCm) / 1000.0
}

// ─────────────────────────────────────────────────────
// Tarif lent IFN : Tables à 2 entrées (D, H)
// V = a₀ + a₁×D² + a₂×D²×H  (D en cm, H en m, V en dm³ → m³)
// Source : IFN
// ─────────────────────────────────────────────────────
@Serializable
data class IfnLentCoefs(
    val numero: Int,
    val a0: Double,
    val a1: Double,
    val a2: Double
) {
    fun volumeDm3(diamCm: Double, hauteurM: Double): Double {
        val d2 = diamCm * diamCm
        return (a0 + a1 * d2 + a2 * d2 * hauteurM).coerceAtLeast(0.0)
    }

    fun volumeM3(diamCm: Double, hauteurM: Double): Double = volumeDm3(diamCm, hauteurM) / 1000.0
}

// ─────────────────────────────────────────────────────
// Coefficient de forme classique : V = G × H × f
// G = π/4 × (D/100)², H en m, f = coefficient de forme
// ─────────────────────────────────────────────────────
@Serializable
data class CoefFormeEntry(
    val essence: String,
    val minDiam: Int = 0,
    val maxDiam: Int = 999,
    val f: Double              // coefficient de forme (0.35 – 0.55 typiquement)
) {
    fun volume(diamCm: Double, hauteurM: Double): Double {
        if (diamCm <= 0.0 || hauteurM <= 0.0) return 0.0
        val g = Math.PI / 4.0 * Math.pow(diamCm / 100.0, 2.0)
        return g * hauteurM * f
    }
}

// ─────────────────────────────────────────────────────
// Configuration de tarif sélectionné par l'utilisateur
// pour une parcelle ou globalement
// ─────────────────────────────────────────────────────
@Serializable
data class TarifSelection(
    val method: String,                       // code de TarifMethod
    val schaefferNumero: Int? = null,         // numéro Schaeffer si applicable
    val ifnNumero: Int? = null,               // numéro IFN si applicable
    val essenceOverrides: Map<String, String>? = null  // essence → method code pour override par essence
)

// ─────────────────────────────────────────────────────
// Système de découpe par produits — configurable par essence
// ─────────────────────────────────────────────────────

/** Types de produits forestiers standards */
enum class ProduitBois(val code: String, val label: String, val shortLabel: String) {
    BOIS_OEUVRE("BO", "Bois d'œuvre", "BO"),
    BOIS_INDUSTRIE("BI", "Bois d'industrie", "BI"),
    BOIS_CHAUFFAGE("BCh", "Bois de chauffage", "BCh"),
    BOIS_ENERGIE("BE", "Bois énergie", "BE"),
    PATE("PATE", "Bois de trituration / pâte", "Pâte"),
    PIQUET("PIQ", "Piquets", "Piq"),
    POTEAU("POT", "Poteaux", "Pot");

    companion object {
        fun fromCode(code: String): ProduitBois? = entries.firstOrNull { it.code.equals(code, ignoreCase = true) }
    }
}

/**
 * Règle de découpe par produit, configurable par essence.
 * Permet de définir pour chaque essence/type d'arbre quelle proportion
 * du volume va dans quel produit selon la classe de diamètre.
 */
@Serializable
data class DecoupeRule(
    val essence: String,         // code essence ou "*" pour wildcard
    val categorie: String?,      // "Feuillu", "Résineux", "Conifère" ou null
    val minDiam: Int,
    val maxDiam: Int,
    val produit: String,         // code ProduitBois
    val pctVolume: Double = 100.0 // % du volume allant vers ce produit (permet ventilation)
)

/**
 * Prix par produit, essence et classe de diamètre.
 * Version enrichie avec distinction par qualité si besoin.
 */
@Serializable
data class PrixProduit(
    val essence: String,         // code ou "*"
    val categorie: String?,      // "Feuillu" / "Résineux" ou null
    val produit: String,         // code ProduitBois
    val minDiam: Int = 0,
    val maxDiam: Int = 999,
    val qualite: String? = null, // "A", "B", "C", "D" ou null
    val eurPerM3: Double
)
