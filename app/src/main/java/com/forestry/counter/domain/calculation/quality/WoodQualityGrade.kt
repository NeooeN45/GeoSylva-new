package com.forestry.counter.domain.calculation.quality

import kotlinx.serialization.Serializable

/**
 * Système de classification qualité du bois — norme professionnelle française.
 *
 * La qualité d'un arbre sur pied détermine :
 * 1. Le type de produit réalisable (mérain, tranchage, sciage, industrie…)
 * 2. Le prix au m³ applicable
 *
 * Sources :
 * - Classement APECF / ONF pour les bois sur pied
 * - Norme NF EN 1316 (classement d'aspect des grumes feuillues)
 * - Pratiques professionnelles des ventes de bois en France
 */
enum class WoodQualityGrade(
    val code: String,
    val label: String,
    val shortLabel: String,
    val description: String,
    val multiplier: Double   // multiplicateur par rapport au prix de base (qualité C = 1.0)
) {
    A(
        code = "A",
        label = "Excellente qualité",
        shortLabel = "A",
        description = "Fût droit, sans défaut, sans nœud apparent. Apte tranchage / mérain / ébénisterie.",
        multiplier = 2.5
    ),
    B(
        code = "B",
        label = "Bonne qualité",
        shortLabel = "B",
        description = "Fût droit avec défauts mineurs. Apte sciage qualité / charpente premium.",
        multiplier = 1.5
    ),
    C(
        code = "C",
        label = "Qualité moyenne",
        shortLabel = "C",
        description = "Défauts modérés (nœuds, légère courbure). Sciage courant / charpente standard.",
        multiplier = 1.0
    ),
    D(
        code = "D",
        label = "Qualité médiocre",
        shortLabel = "D",
        description = "Défauts importants (fourche, pourriture, gros nœuds). Bois industrie / chauffage.",
        multiplier = 0.4
    );

    companion object {
        fun fromCode(code: String): WoodQualityGrade? =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) }
    }
}

/**
 * Produits forestiers réalisables par essence, qualité et diamètre.
 *
 * Exemples concrets :
 * - Chêne A + D≥60cm → Mérain (500–1500 €/m³)
 * - Chêne A + D≥50cm → Tranchage (300–600 €/m³)
 * - Douglas A + D≥40cm → Grume longue ≥12m (120–180 €/m³)
 * - Hêtre B + D≥35cm → Sciage ameublement (90–140 €/m³)
 * - Tout D → Chauffage / industrie (15–40 €/m³)
 */
enum class ForestProduct(
    val code: String,
    val label: String,
    val shortLabel: String,
    val minQuality: WoodQualityGrade,
    val typicalMinDiamCm: Int,
    val isFeuillus: Boolean,
    val isResineux: Boolean
) {
    // ── Produits feuillus premium ──
    MERAIN(
        code = "MERAIN", label = "Mérain (tonnellerie)", shortLabel = "Mérain",
        minQuality = WoodQualityGrade.A, typicalMinDiamCm = 55,
        isFeuillus = true, isResineux = false
    ),
    TRANCHAGE(
        code = "TRANCHAGE", label = "Tranchage / placage", shortLabel = "Tranch.",
        minQuality = WoodQualityGrade.A, typicalMinDiamCm = 45,
        isFeuillus = true, isResineux = false
    ),
    DEROULAGE(
        code = "DEROULAGE", label = "Déroulage (contreplaqué)", shortLabel = "Dérou.",
        minQuality = WoodQualityGrade.A, typicalMinDiamCm = 40,
        isFeuillus = true, isResineux = false
    ),
    SCIAGE_QUAL(
        code = "SCIAGE_Q", label = "Sciage qualité / ébénisterie", shortLabel = "Sci.Q",
        minQuality = WoodQualityGrade.B, typicalMinDiamCm = 35,
        isFeuillus = true, isResineux = true
    ),
    // ── Produits résineux premium ──
    GRUME_LONGUE(
        code = "GRUME_L", label = "Grume longue (≥12m)", shortLabel = "Gru.L",
        minQuality = WoodQualityGrade.A, typicalMinDiamCm = 35,
        isFeuillus = false, isResineux = true
    ),
    POTEAU_LIGNE(
        code = "POTEAU", label = "Poteau de ligne", shortLabel = "Poteau",
        minQuality = WoodQualityGrade.A, typicalMinDiamCm = 20,
        isFeuillus = false, isResineux = true
    ),
    CHARPENTE(
        code = "CHARPENTE", label = "Charpente / lamellé-collé", shortLabel = "Charp.",
        minQuality = WoodQualityGrade.B, typicalMinDiamCm = 25,
        isFeuillus = false, isResineux = true
    ),
    BARDAGE(
        code = "BARDAGE", label = "Bardage / lambris", shortLabel = "Bard.",
        minQuality = WoodQualityGrade.B, typicalMinDiamCm = 20,
        isFeuillus = true, isResineux = true
    ),
    // ── Produits standards ──
    SCIAGE_STD(
        code = "SCIAGE_S", label = "Sciage standard / charpente", shortLabel = "Sci.S",
        minQuality = WoodQualityGrade.C, typicalMinDiamCm = 25,
        isFeuillus = true, isResineux = true
    ),
    PIQUET_CLOTURE(
        code = "PIQUET", label = "Piquet / clôture", shortLabel = "Piquet",
        minQuality = WoodQualityGrade.C, typicalMinDiamCm = 10,
        isFeuillus = true, isResineux = true
    ),
    TRAVERSE(
        code = "TRAVERSE", label = "Traverse de chemin de fer", shortLabel = "Trav.",
        minQuality = WoodQualityGrade.B, typicalMinDiamCm = 30,
        isFeuillus = true, isResineux = true
    ),
    PALETTE(
        code = "PALETTE", label = "Palette / emballage", shortLabel = "Pal.",
        minQuality = WoodQualityGrade.C, typicalMinDiamCm = 20,
        isFeuillus = true, isResineux = true
    ),
    // ── Produits bas de gamme ──
    BOIS_INDUSTRIE(
        code = "BI", label = "Bois d'industrie / trituration", shortLabel = "BI",
        minQuality = WoodQualityGrade.D, typicalMinDiamCm = 10,
        isFeuillus = true, isResineux = true
    ),
    PATE_PAPIER(
        code = "PATE", label = "Pâte à papier", shortLabel = "Pâte",
        minQuality = WoodQualityGrade.D, typicalMinDiamCm = 7,
        isFeuillus = true, isResineux = true
    ),
    BOIS_CHAUFFAGE(
        code = "BCh", label = "Bois de chauffage", shortLabel = "BCh",
        minQuality = WoodQualityGrade.D, typicalMinDiamCm = 7,
        isFeuillus = true, isResineux = false
    ),
    BOIS_ENERGIE(
        code = "BE", label = "Bois énergie / plaquettes", shortLabel = "BE",
        minQuality = WoodQualityGrade.D, typicalMinDiamCm = 7,
        isFeuillus = true, isResineux = true
    );

    companion object {
        fun fromCode(code: String): ForestProduct? =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) }

        /** Produits applicables selon la catégorie d'essence */
        fun forCategorie(categorie: String?): List<ForestProduct> {
            val isFeu = categorie?.contains("feuill", ignoreCase = true) == true
            val isRes = categorie?.contains("résineux", ignoreCase = true) == true ||
                    categorie?.contains("resineux", ignoreCase = true) == true
            if (!isFeu && !isRes) return entries.toList()
            return entries.filter { if (isFeu) it.isFeuillus else it.isResineux }
        }
    }
}

/**
 * Questions d'évaluation rapide de la qualité d'un arbre sur pied.
 * Chaque réponse contribue à un score qui détermine la qualité (A/B/C/D).
 */
@Serializable
data class QualityAssessment(
    val tigeId: String,
    val rectitude: Int = 2,     // 0=fourchu/tordu, 1=courbe, 2=droit, 3=très droit
    val branchage: Int = 2,     // 0=très branchu, 1=branchu, 2=élagué, 3=sans branche
    val etatSanitaire: Int = 2, // 0=pourri/creux, 1=gui/champignons, 2=sain, 3=parfait
    val defautsFut: Int = 2     // 0=gros nœuds/fentes, 1=nœuds moyens, 2=petits nœuds, 3=aucun
) {
    /** Score total sur 12 */
    val score: Int get() = rectitude + branchage + etatSanitaire + defautsFut

    /** Grade qualité dérivé du score */
    val grade: WoodQualityGrade get() = when {
        score >= 10 -> WoodQualityGrade.A
        score >= 7  -> WoodQualityGrade.B
        score >= 4  -> WoodQualityGrade.C
        else        -> WoodQualityGrade.D
    }
}

/**
 * Détermine le meilleur produit réalisable pour un arbre donné.
 */
object ProductClassifier {

    /**
     * Classifie le produit principal réalisable.
     *
     * @param essenceCode    Code de l'essence
     * @param categorie      "Feuillu" ou "Résineux"
     * @param diamCm         Diamètre à 1m30 en cm
     * @param quality        Grade qualité (A/B/C/D)
     * @param hauteurM       Hauteur totale (optionnel)
     * @return Produit principal + produit secondaire éventuel
     */
    fun classify(
        essenceCode: String,
        categorie: String?,
        diamCm: Double,
        quality: WoodQualityGrade,
        hauteurM: Double? = null
    ): ClassificationResult {
        val isFeu = categorie?.contains("feuill", ignoreCase = true) == true
        val isRes = !isFeu
        val d = diamCm.toInt()
        val code = essenceCode.trim().uppercase()

        // ── Produits premium feuillus ──
        if (isFeu && quality == WoodQualityGrade.A) {
            // Mérain : chênes de très gros diamètre, exceptionnels
            if (code.startsWith("CH_") && d >= 55) {
                return ClassificationResult(
                    primary = ForestProduct.MERAIN,
                    secondary = ForestProduct.SCIAGE_QUAL,
                    qualityNote = "Chêne apte mérain — valorisation exceptionnelle"
                )
            }
            // Tranchage : gros feuillus nobles parfaits
            if (d >= 45 && code in ESSENCES_TRANCHAGE) {
                return ClassificationResult(
                    primary = ForestProduct.TRANCHAGE,
                    secondary = ForestProduct.SCIAGE_QUAL,
                    qualityNote = "Apte tranchage / placage — haute valorisation"
                )
            }
            // Déroulage : peuplier, hêtre, bouleau — gros diamètres bien conformés
            if (d >= 40 && code in ESSENCES_DEROULAGE) {
                return ClassificationResult(
                    primary = ForestProduct.DEROULAGE,
                    secondary = ForestProduct.SCIAGE_QUAL,
                    qualityNote = "Apte déroulage (contreplaqué) — bonne valorisation"
                )
            }
            // Sciage qualité
            if (d >= 35) {
                return ClassificationResult(
                    primary = ForestProduct.SCIAGE_QUAL,
                    secondary = ForestProduct.SCIAGE_STD,
                    qualityNote = "Bois d'œuvre qualité A — valorisation premium"
                )
            }
        }

        // ── Produits premium résineux ──
        if (isRes && quality == WoodQualityGrade.A) {
            // Grume longue : Douglas, Sapin, Épicéa avec grande hauteur
            if (d >= 35 && (hauteurM == null || hauteurM >= 20.0)) {
                return ClassificationResult(
                    primary = ForestProduct.GRUME_LONGUE,
                    secondary = ForestProduct.SCIAGE_QUAL,
                    qualityNote = "Grume longue qualité A — valorisation forte"
                )
            }
            // Poteau de ligne : résineux droits petits diamètres
            if (d in 18..35) {
                return ClassificationResult(
                    primary = ForestProduct.POTEAU_LIGNE,
                    secondary = ForestProduct.CHARPENTE,
                    qualityNote = "Apte poteau de ligne — bonne valorisation"
                )
            }
        }

        // ── Qualité B ──
        if (quality == WoodQualityGrade.B) {
            if (d >= 35) {
                return ClassificationResult(
                    primary = ForestProduct.SCIAGE_QUAL,
                    secondary = if (isFeu && code in ESSENCES_TRAVERSE) ForestProduct.TRAVERSE else ForestProduct.SCIAGE_STD,
                    qualityNote = "Sciage qualité — bonne valorisation"
                )
            }
            // Traverse : chêne, hêtre diamètres moyens qualité B (32–34 cm)
            if (isFeu && d >= 32 && code in ESSENCES_TRAVERSE) {
                return ClassificationResult(
                    primary = ForestProduct.TRAVERSE,
                    secondary = ForestProduct.SCIAGE_STD,
                    qualityNote = "Apte traverse — valorisation intermédiaire"
                )
            }
            // Charpente / lamellé-collé : résineux B moyens diamètres
            if (isRes && d >= 25) {
                return ClassificationResult(
                    primary = ForestProduct.CHARPENTE,
                    secondary = ForestProduct.BARDAGE,
                    qualityNote = "Charpente / lamellé-collé — valorisation correcte"
                )
            }
            if (d >= 25) {
                return ClassificationResult(
                    primary = ForestProduct.SCIAGE_STD,
                    secondary = ForestProduct.PALETTE,
                    qualityNote = "Sciage standard / charpente"
                )
            }
            // Bardage / lambris : résineux B petits diamètres
            if (isRes && d >= 20) {
                return ClassificationResult(
                    primary = ForestProduct.BARDAGE,
                    secondary = ForestProduct.PALETTE,
                    qualityNote = "Bardage / lambris — valorisation correcte"
                )
            }
        }

        // ── Qualité C ──
        if (quality == WoodQualityGrade.C) {
            // Piquet / clôture : robinier et châtaignier (naturellement durables)
            if (code in ESSENCES_PIQUET && d in 10..25) {
                return ClassificationResult(
                    primary = ForestProduct.PIQUET_CLOTURE,
                    secondary = ForestProduct.BOIS_CHAUFFAGE,
                    qualityNote = "Piquet / clôture — bois durable naturellement"
                )
            }
            if (d >= 25) {
                return ClassificationResult(
                    primary = ForestProduct.SCIAGE_STD,
                    secondary = if (isFeu) ForestProduct.BOIS_CHAUFFAGE else ForestProduct.BOIS_ENERGIE,
                    qualityNote = "Sciage courant — valorisation standard"
                )
            }
            if (d >= 20) {
                return ClassificationResult(
                    primary = ForestProduct.PALETTE,
                    secondary = ForestProduct.BOIS_INDUSTRIE,
                    qualityNote = "Palette / emballage"
                )
            }
        }

        // ── Qualité D ou petits diamètres ──
        if (d >= 10) {
            // Pâte à papier : résineux petits diamètres
            val secondary = if (isFeu) ForestProduct.BOIS_CHAUFFAGE else ForestProduct.PATE_PAPIER
            return ClassificationResult(
                primary = ForestProduct.BOIS_INDUSTRIE,
                secondary = secondary,
                qualityNote = "Bois industrie / trituration"
            )
        }

        return ClassificationResult(
            primary = if (isFeu) ForestProduct.BOIS_CHAUFFAGE else ForestProduct.BOIS_ENERGIE,
            secondary = null,
            qualityNote = "Bois énergie / chauffage"
        )
    }

    /** Essences pouvant prétendre au tranchage / placage */
    private val ESSENCES_TRANCHAGE = setOf(
        "CH_SESSILE", "CH_PEDONCULE", "HETRE_COMMUN", "NOYER_COMMUN", "NOYER_NOIR",
        "CERISIER_MERIS", "ERABLE_SYC", "ERABLE_PLANE", "FRENE_ELEVE",
        "ORME_LISSE", "ORME_MONT", "ALISIER_TORM", "CORMIER", "POIRIER_SAUV"
    )

    /** Essences pouvant prétendre au déroulage */
    private val ESSENCES_DEROULAGE = setOf(
        "PEUPLIER", "HETRE_COMMUN", "BOUL_VERRUQ", "BOUL_PUBESC",
        "AULNE_GLUT", "TILLEUL_GPF", "TILLEUL_PTF"
    )

    /** Essences aptes à la fabrication de traverses */
    private val ESSENCES_TRAVERSE = setOf(
        "CH_SESSILE", "CH_PEDONCULE", "CH_PUBESCENT", "CH_ROUGE",
        "HETRE_COMMUN", "CHATAIGNIER"
    )

    /** Essences naturellement durables pour piquets et clôtures */
    private val ESSENCES_PIQUET = setOf(
        "ROBINIER", "CHATAIGNIER", "CH_SESSILE", "CH_PEDONCULE",
        "MEL_EUROPE", "MEL_HYBRIDE"
    )
}

data class ClassificationResult(
    val primary: ForestProduct,
    val secondary: ForestProduct?,
    val qualityNote: String
)

/**
 * Prix par défaut par produit forestier (€/m³ bord de route, France 2023-2024).
 * Sources : mercuriales ONF, FBF, DRAAF régionales, observatoire du bois.
 * L'utilisateur peut les personnaliser via l'éditeur de prix.
 */
object DefaultProductPrices {

    /** Prix moyen national par type de produit (€/m³ bord de route) */
    val defaults: Map<String, Double> = mapOf(
        "MERAIN"    to 850.0,   // 500–1800 selon qualité et essence
        "TRANCHAGE" to 380.0,   // 200–650
        "DEROULAGE" to 120.0,   // 80–180
        "SCIAGE_Q"  to 135.0,   // 80–220
        "GRUME_L"   to 110.0,   // 70–170
        "POTEAU"    to 60.0,    // 45–80
        "CHARPENTE" to 85.0,    // 55–130
        "BARDAGE"   to 75.0,    // 50–110
        "SCIAGE_S"  to 70.0,    // 45–110
        "PIQUET"    to 55.0,    // 35–80
        "TRAVERSE"  to 90.0,    // 60–130
        "PALETTE"   to 40.0,    // 25–55
        "BI"        to 25.0,    // 18–38
        "PATE"      to 20.0,    // 12–30
        "BCh"       to 32.0,    // 20–50
        "BE"        to 16.0     // 8–25
    )

    /**
     * Prix spécifique par combinaison essence × produit (€/m³ bord de route).
     * Basé sur les mercuriales et adjudications ONF 2023-2024.
     * Clé : "ESSENCE_CODE:PRODUCT_CODE"
     */
    private val essenceProductPrices: Map<String, Double> = mapOf(
        // ── Chêne sessile (premium feuillus) ──
        "CH_SESSILE:MERAIN"    to 1200.0,
        "CH_SESSILE:TRANCHAGE" to 500.0,
        "CH_SESSILE:SCIAGE_Q"  to 185.0,
        "CH_SESSILE:TRAVERSE"  to 110.0,
        "CH_SESSILE:SCIAGE_S"  to 95.0,
        "CH_SESSILE:BCh"       to 38.0,
        "CH_SESSILE:BI"        to 30.0,
        // ── Chêne pédonculé ──
        "CH_PEDONCULE:MERAIN"    to 1000.0,
        "CH_PEDONCULE:TRANCHAGE" to 420.0,
        "CH_PEDONCULE:SCIAGE_Q"  to 165.0,
        "CH_PEDONCULE:TRAVERSE"  to 100.0,
        "CH_PEDONCULE:SCIAGE_S"  to 85.0,
        "CH_PEDONCULE:BCh"       to 36.0,
        "CH_PEDONCULE:BI"        to 28.0,
        // ── Hêtre ──
        "HETRE_COMMUN:TRANCHAGE" to 250.0,
        "HETRE_COMMUN:DEROULAGE" to 130.0,
        "HETRE_COMMUN:SCIAGE_Q"  to 95.0,
        "HETRE_COMMUN:TRAVERSE"  to 85.0,
        "HETRE_COMMUN:SCIAGE_S"  to 55.0,
        "HETRE_COMMUN:PALETTE"   to 35.0,
        "HETRE_COMMUN:BCh"       to 30.0,
        "HETRE_COMMUN:BI"        to 22.0,
        // ── Frêne ──
        "FRENE_ELEVE:TRANCHAGE" to 300.0,
        "FRENE_ELEVE:SCIAGE_Q"  to 130.0,
        "FRENE_ELEVE:SCIAGE_S"  to 65.0,
        "FRENE_ELEVE:BCh"       to 32.0,
        // ── Noyer ──
        "NOYER_COMMUN:TRANCHAGE" to 650.0,
        "NOYER_COMMUN:SCIAGE_Q"  to 350.0,
        "NOYER_COMMUN:SCIAGE_S"  to 180.0,
        "NOYER_NOIR:TRANCHAGE"   to 700.0,
        "NOYER_NOIR:SCIAGE_Q"    to 380.0,
        // ── Merisier ──
        "CERISIER_MERIS:TRANCHAGE" to 400.0,
        "CERISIER_MERIS:SCIAGE_Q"  to 180.0,
        "CERISIER_MERIS:SCIAGE_S"  to 90.0,
        // ── Érables ──
        "ERABLE_SYC:TRANCHAGE" to 320.0,
        "ERABLE_SYC:SCIAGE_Q"  to 140.0,
        "ERABLE_SYC:SCIAGE_S"  to 70.0,
        "ERABLE_PLANE:SCIAGE_Q" to 120.0,
        "ERABLE_PLANE:SCIAGE_S" to 65.0,
        // ── Châtaignier ──
        "CHATAIGNIER:SCIAGE_Q" to 110.0,
        "CHATAIGNIER:SCIAGE_S" to 60.0,
        "CHATAIGNIER:PIQUET"   to 65.0,
        "CHATAIGNIER:PALETTE"  to 35.0,
        "CHATAIGNIER:BCh"      to 30.0,
        // ── Robinier (forte demande piquet/clôture) ──
        "ROBINIER:SCIAGE_Q"    to 150.0,
        "ROBINIER:SCIAGE_S"    to 80.0,
        "ROBINIER:PIQUET"      to 75.0,
        "ROBINIER:POTEAU"      to 90.0,
        // ── Peuplier (champion du déroulage) ──
        "PEUPLIER:DEROULAGE"   to 100.0,
        "PEUPLIER:SCIAGE_S"    to 40.0,
        "PEUPLIER:PALETTE"     to 28.0,
        "PEUPLIER:PATE"        to 18.0,
        // ── Essences feuillues bas de gamme ──
        "CHARME:BCh"           to 30.0,
        "CHARME:BI"            to 20.0,
        "BOUL_VERRUQ:DEROULAGE" to 85.0,
        "BOUL_VERRUQ:SCIAGE_S" to 45.0,
        "BOUL_VERRUQ:BI"      to 22.0,
        "BOUL_PUBESC:BI"       to 18.0,
        "AULNE_GLUT:SCIAGE_S"  to 40.0,
        "AULNE_GLUT:BI"        to 20.0,
        // ── Douglas (roi des résineux français) ──
        "DOUGLAS_VERT:GRUME_L"   to 145.0,
        "DOUGLAS_VERT:SCIAGE_Q"  to 120.0,
        "DOUGLAS_VERT:CHARPENTE" to 95.0,
        "DOUGLAS_VERT:BARDAGE"   to 85.0,
        "DOUGLAS_VERT:SCIAGE_S"  to 80.0,
        "DOUGLAS_VERT:POTEAU"    to 70.0,
        "DOUGLAS_VERT:PALETTE"   to 42.0,
        "DOUGLAS_VERT:BI"        to 28.0,
        "DOUGLAS_VERT:BE"        to 18.0,
        // ── Sapin pectiné ──
        "SAPIN_PECTINE:GRUME_L"   to 110.0,
        "SAPIN_PECTINE:SCIAGE_Q"  to 100.0,
        "SAPIN_PECTINE:CHARPENTE" to 80.0,
        "SAPIN_PECTINE:SCIAGE_S"  to 65.0,
        "SAPIN_PECTINE:PALETTE"   to 38.0,
        "SAPIN_PECTINE:PATE"      to 22.0,
        "SAPIN_PECTINE:BI"        to 24.0,
        // ── Épicéa ──
        "EPICEA_COMMUN:GRUME_L"   to 105.0,
        "EPICEA_COMMUN:SCIAGE_Q"  to 95.0,
        "EPICEA_COMMUN:CHARPENTE" to 75.0,
        "EPICEA_COMMUN:BARDAGE"   to 65.0,
        "EPICEA_COMMUN:SCIAGE_S"  to 60.0,
        "EPICEA_COMMUN:PALETTE"   to 35.0,
        "EPICEA_COMMUN:PATE"      to 20.0,
        "EPICEA_COMMUN:BI"        to 22.0,
        // ── Mélèze ──
        "MEL_EUROPE:GRUME_L"   to 130.0,
        "MEL_EUROPE:SCIAGE_Q"  to 115.0,
        "MEL_EUROPE:CHARPENTE" to 90.0,
        "MEL_EUROPE:BARDAGE"   to 85.0,
        "MEL_EUROPE:SCIAGE_S"  to 75.0,
        "MEL_EUROPE:PIQUET"    to 60.0,
        "MEL_EUROPE:POTEAU"    to 65.0,
        "MEL_HYBRIDE:GRUME_L"   to 125.0,
        "MEL_HYBRIDE:SCIAGE_Q"  to 110.0,
        "MEL_HYBRIDE:CHARPENTE" to 85.0,
        // ── Pins ──
        "PIN_SYLVESTRE:CHARPENTE" to 60.0,
        "PIN_SYLVESTRE:SCIAGE_S"  to 55.0,
        "PIN_SYLVESTRE:PALETTE"   to 32.0,
        "PIN_SYLVESTRE:PATE"      to 18.0,
        "PIN_SYLVESTRE:BI"        to 20.0,
        "PIN_MARITIME:SCIAGE_S"   to 50.0,
        "PIN_MARITIME:CHARPENTE"  to 55.0,
        "PIN_MARITIME:PALETTE"    to 30.0,
        "PIN_MARITIME:PATE"       to 16.0,
        "PIN_MARITIME:BI"         to 18.0,
        "PIN_LARICIO:SCIAGE_Q"    to 90.0,
        "PIN_LARICIO:CHARPENTE"   to 70.0,
        "PIN_LARICIO:SCIAGE_S"    to 60.0,
        "PIN_LARICIO:POTEAU"      to 55.0,
        "PIN_NOIR_AUTR:SCIAGE_S"  to 55.0,
        "PIN_NOIR_AUTR:CHARPENTE" to 60.0,
        "PIN_NOIR_AUTR:PALETTE"   to 30.0
    )

    /** Multiplicateur global par essence (fallback quand pas de prix spécifique) */
    private val essenceMultipliers: Map<String, Double> = mapOf(
        // Feuillus premium
        "CH_SESSILE"     to 1.40,
        "CH_PEDONCULE"   to 1.25,
        "NOYER_COMMUN"   to 2.50,
        "NOYER_NOIR"     to 2.70,
        "CERISIER_MERIS" to 1.55,
        "CORMIER"        to 2.20,
        "ALISIER_TORM"   to 2.00,
        "POIRIER_SAUV"   to 1.35,
        // Feuillus courants
        "HETRE_COMMUN"   to 0.80,
        "FRENE_ELEVE"    to 1.00,
        "ERABLE_SYC"     to 1.10,
        "ERABLE_PLANE"   to 0.95,
        "CHATAIGNIER"    to 0.90,
        "ROBINIER"       to 1.15,
        "TILLEUL_GPF"    to 0.55,
        "TILLEUL_PTF"    to 0.50,
        "PEUPLIER"       to 0.60,
        // Feuillus bas de gamme
        "CHARME"         to 0.50,
        "BOUL_VERRUQ"    to 0.55,
        "BOUL_PUBESC"    to 0.50,
        "AULNE_GLUT"     to 0.60,
        "SAULE_BLANC"    to 0.35,
        "SAULE_MARSAULT" to 0.30,
        "NOISETIER"      to 0.30,
        "TREMBLE"        to 0.45,
        // Résineux premium
        "DOUGLAS_VERT"   to 1.30,
        "MEL_EUROPE"     to 1.15,
        "MEL_HYBRIDE"    to 1.10,
        "PIN_LARICIO"    to 1.00,
        "CEDRE_ATLAS"    to 0.95,
        // Résineux courants
        "SAPIN_PECTINE"  to 0.95,
        "EPICEA_COMMUN"  to 0.90,
        "PIN_SYLVESTRE"  to 0.75,
        "PIN_MARITIME"   to 0.70,
        "PIN_NOIR_AUTR"  to 0.75,
        "PIN_WEYMOUTH"   to 0.65
    )

    /**
     * Retourne le prix €/m³ pour un produit donné, une essence et une qualité.
     * Cherche d'abord un prix spécifique essence×produit, sinon utilise le prix
     * par défaut du produit ajusté par le multiplicateur essence et la qualité.
     */
    fun priceFor(
        productCode: String,
        essenceCode: String,
        qualityGrade: WoodQualityGrade = WoodQualityGrade.C
    ): Double {
        val code = essenceCode.trim().uppercase()
        // Chercher prix spécifique essence:produit
        val specificPrice = essenceProductPrices["$code:$productCode"]
        if (specificPrice != null) {
            // Ajuster par la qualité par rapport à C (qualité de référence)
            return specificPrice * qualityGrade.multiplier / WoodQualityGrade.C.multiplier
        }
        // Fallback : prix générique × multiplicateur essence × qualité
        val basePrice = defaults[productCode] ?: 50.0
        val essenceMultiplier = essenceMultipliers[code] ?: 1.0
        return basePrice * essenceMultiplier * qualityGrade.multiplier / WoodQualityGrade.C.multiplier
    }
}
