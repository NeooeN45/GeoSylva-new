package com.forestry.counter.domain.calculation

/**
 * Calculateur de prix intégrant la qualité du bois et les produits.
 *
 * Sources fiables pour les coefficients de qualité :
 * - France Bois Forêt / FCBA : Observatoire économique du bois
 * - ONF : Barèmes des ventes publiques de bois
 * - CNPF : Indices régionaux de prix
 *
 * La qualité a un impact majeur sur le prix :
 * - Douglas qualité A  → coefficient ×1.50 (charpente / structure premium)
 * - Douglas qualité D  → coefficient ×0.50 (bois d'industrie ou énergie)
 *
 * Exemple concret : Douglas BO base 72 €/m³
 *   A → 108 €/m³ | B → 86 €/m³ | C → 58 €/m³ | D → 36 €/m³
 */
object PriceCalculator {

    // ═══════════════════════════════════════════════════════════
    // Coefficients de qualité par essence
    //
    // Sources :
    //  • ONF « Résultats des ventes de bois » 2022-2024
    //  • France Bois Forêt / FCBA « Prix de marché du bois »
    //  • CNPF / IDF « Guide des prix du bois sur pied »
    //
    // Format : essence → (A, B, C, D) multiplicateurs
    // ═══════════════════════════════════════════════════════════
    private val qualityCoefficients: Map<String, Map<String, Double>> = mapOf(
        // Chênes — très sensibles à la qualité (tranchage vs chauffage)
        "CH_SESSILE"    to mapOf("A" to 1.40, "B" to 1.15, "C" to 0.85, "D" to 0.55),
        "CH_PEDONCULE"  to mapOf("A" to 1.35, "B" to 1.12, "C" to 0.88, "D" to 0.60),
        "CH_PUBESCENT"  to mapOf("A" to 1.25, "B" to 1.10, "C" to 0.90, "D" to 0.65),
        "CH_ROUGE"      to mapOf("A" to 1.30, "B" to 1.12, "C" to 0.88, "D" to 0.62),
        // Hêtre — écart très fort (déroulage A vs palette D)
        "HETRE_COMMUN"  to mapOf("A" to 1.50, "B" to 1.20, "C" to 0.80, "D" to 0.45),
        // Douglas — essence phare, qualité cruciale (structure vs coffrage)
        "DOUGLAS_VERT"  to mapOf("A" to 1.50, "B" to 1.20, "C" to 0.80, "D" to 0.50),
        // Sapins / épicéas
        "SAPIN_PECTINE" to mapOf("A" to 1.35, "B" to 1.15, "C" to 0.85, "D" to 0.60),
        "EPICEA_COMMUN" to mapOf("A" to 1.30, "B" to 1.12, "C" to 0.88, "D" to 0.65),
        "SAPIN_GRANDIS" to mapOf("A" to 1.35, "B" to 1.15, "C" to 0.85, "D" to 0.58),
        // Pins
        "PIN_SYLVESTRE" to mapOf("A" to 1.25, "B" to 1.10, "C" to 0.90, "D" to 0.70),
        "PIN_MARITIME"  to mapOf("A" to 1.20, "B" to 1.08, "C" to 0.92, "D" to 0.72),
        "PIN_NOIR_AUTR" to mapOf("A" to 1.25, "B" to 1.10, "C" to 0.90, "D" to 0.68),
        "PIN_LARICIO"   to mapOf("A" to 1.30, "B" to 1.12, "C" to 0.88, "D" to 0.65),
        // Mélèzes
        "MEL_EUROPE"    to mapOf("A" to 1.35, "B" to 1.15, "C" to 0.85, "D" to 0.62),
        "MEL_JAPON"     to mapOf("A" to 1.30, "B" to 1.12, "C" to 0.88, "D" to 0.60),
        // Feuillus précieux — très forte sensibilité qualité
        "FRENE_ELEVE"     to mapOf("A" to 1.45, "B" to 1.20, "C" to 0.80, "D" to 0.50),
        "ERABLE_SYC"      to mapOf("A" to 1.50, "B" to 1.25, "C" to 0.75, "D" to 0.45),
        "NOYER_COMMUN"    to mapOf("A" to 1.60, "B" to 1.30, "C" to 0.70, "D" to 0.40),
        "CERISIER_MERIS"  to mapOf("A" to 1.55, "B" to 1.25, "C" to 0.72, "D" to 0.42),
        "ALISIER_TORMINAL" to mapOf("A" to 1.55, "B" to 1.25, "C" to 0.75, "D" to 0.45),
        "CORMIER"         to mapOf("A" to 1.60, "B" to 1.30, "C" to 0.70, "D" to 0.40),
        // Autres feuillus
        "CHARME"       to mapOf("A" to 1.15, "B" to 1.05, "C" to 0.92, "D" to 0.75),
        "CHATAIGNIER"  to mapOf("A" to 1.30, "B" to 1.15, "C" to 0.85, "D" to 0.65),
        "ROBINIER"     to mapOf("A" to 1.40, "B" to 1.20, "C" to 0.80, "D" to 0.55),
        "PEUPLIER_HYBR" to mapOf("A" to 1.30, "B" to 1.12, "C" to 0.88, "D" to 0.65),
        // Résineux spéciaux
        "CEDRE_ATLAS"            to mapOf("A" to 1.35, "B" to 1.15, "C" to 0.85, "D" to 0.60),
        "SEQUOIA_TOUJOURS_VERT" to mapOf("A" to 1.30, "B" to 1.12, "C" to 0.88, "D" to 0.65),
        // Wildcard — toute essence non listée
        "*" to mapOf("A" to 1.30, "B" to 1.10, "C" to 0.90, "D" to 0.65)
    )

    /**
     * Retourne le coefficient de qualité pour une essence et une qualité.
     * Retourne 1.0 si quality == null.
     */
    fun getQualityCoefficient(essenceCode: String, quality: String?): Double {
        if (quality.isNullOrBlank()) return 1.0
        val q = quality.uppercase().firstOrNull()?.toString() ?: return 1.0
        val coefs = qualityCoefficients[essenceCode] ?: qualityCoefficients["*"] ?: return 1.0
        return coefs[q] ?: 1.0
    }

    /**
     * Trouve le meilleur prix de base dans la table pour (essence, produit, diamètre).
     * Résolution : essenceCode exact → wildcard "*" → null.
     */
    fun findBasePrice(
        prices: List<PriceEntry>,
        essenceCode: String,
        product: String,
        diamCm: Int
    ): Double? {
        // Exact match : même essence, même produit, diamètre dans [min..max]
        prices.firstOrNull {
            it.essence.equals(essenceCode, ignoreCase = true) &&
                it.product.equals(product, ignoreCase = true) &&
                diamCm >= it.min && diamCm <= it.max
        }?.let { return it.eurPerM3 }

        // Wildcard essence "*"
        prices.firstOrNull {
            it.essence == "*" &&
                it.product.equals(product, ignoreCase = true) &&
                diamCm >= it.min && diamCm <= it.max
        }?.let { return it.eurPerM3 }

        return null
    }

    /**
     * Prix ajusté = prix_base × coeff_qualité.
     */
    fun adjustedPrice(
        prices: List<PriceEntry>,
        essenceCode: String,
        product: String,
        diamCm: Int,
        quality: String? = null
    ): Double? {
        val base = findBasePrice(prices, essenceCode, product, diamCm) ?: return null
        return base * getQualityCoefficient(essenceCode, quality)
    }

    /**
     * Calcule la ventilation par produit avec prix ajustés à la qualité.
     *
     * @param prices  liste de PriceEntry depuis les paramètres
     * @param essenceCode code essence
     * @param volumeByProduct map produit → volume (ex: {"BO" -> 1.2, "BI" -> 0.3})
     * @param diamCm diamètre moyen pour le lookup prix
     * @param quality qualité globale (A/B/C/D ou null)
     */
    fun buildBreakdown(
        prices: List<PriceEntry>,
        essenceCode: String,
        volumeByProduct: Map<String, Double>,
        diamCm: Int,
        quality: String? = null
    ): List<ProductBreakdownRow> {
        return volumeByProduct.map { (product, volume) ->
            val pricePerM3 = adjustedPrice(prices, essenceCode, product, diamCm, quality) ?: 0.0
            ProductBreakdownRow(
                product = product,
                volumeM3 = volume,
                pricePerM3 = pricePerM3,
                totalEur = pricePerM3 * volume
            )
        }
    }
}

data class ProductBreakdownRow(
    val product: String,
    val volumeM3: Double,
    val pricePerM3: Double,
    val totalEur: Double
)
