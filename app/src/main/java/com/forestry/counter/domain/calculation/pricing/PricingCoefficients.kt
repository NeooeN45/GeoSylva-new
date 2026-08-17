package com.forestry.counter.domain.calculation.pricing

/**
 * Coefficients régionaux par RÉGION ADMINISTRATIVE — écarts vs moyenne nationale.
 *
 * Source : France Bois Forêt - Cartes écarts régionaux, observatoires Fibois régionaux.
 * Le coefficient moyen par région vient de [FrenchRegion.priceCoefficient] ;
 * des surcharges essence×région couvrent les écarts documentés les plus forts.
 *
 * ⚠️ Régions sans source de prix publique (FrenchRegion.hasPriceSource = false) :
 * coefficient national (×1.0) assumé. Valeurs indicatives, ajustables dans les Settings.
 */
object RegionalCoefficients {

    /**
     * Surcharges spécifiques par essence × région (prioritaires sur le coefficient moyen).
     * Source : Étude Douglas FBF (écart >50% Est vs Occitanie), observatoires Fibois.
     */
    val essenceRegionCoefficients: Map<Pair<String, FrenchRegion>, Double> = mapOf(
        // Douglas : écart massif Est vs Occitanie/Massif Central
        ("DOUGLAS_VERT" to FrenchRegion.GES) to 1.30,   // Grand Est : demande forte, prix record Vosges
        ("DOUGLAS_VERT" to FrenchRegion.BFC) to 1.25,   // Jura/Bourgogne : bon marché
        ("DOUGLAS_VERT" to FrenchRegion.ARA) to 0.75,   // Massif Central (Auvergne) : prix bas
        ("DOUGLAS_VERT" to FrenchRegion.OCC) to 0.70,   // Occitanie : très bas
        ("DOUGLAS_VERT" to FrenchRegion.NAQ) to 0.80,   // Limousin/Sud-Ouest : bas

        // Chêne : prime Bourgogne-Franche-Comté et Grand Est
        ("CH_SESSILE" to FrenchRegion.BFC) to 1.25,     // grain fin, réputation
        ("CH_SESSILE" to FrenchRegion.GES) to 1.10,
        ("CH_SESSILE" to FrenchRegion.CVL) to 1.15,     // Centre : demande industrielle
        ("CH_PEDONCULE" to FrenchRegion.BFC) to 1.20,
        ("CH_PEDONCULE" to FrenchRegion.CVL) to 1.10,

        // Hêtre : Grand Est et BFC dominent
        ("HETRE_COMMUN" to FrenchRegion.GES) to 1.15,
        ("HETRE_COMMUN" to FrenchRegion.BFC) to 1.20,

        // Pin maritime : filière intégrée Landes (Nouvelle-Aquitaine)
        ("PIN_MARITIME" to FrenchRegion.NAQ) to 1.15
    )

    /**
     * Index normalisé (code essence en MAJUSCULES) — évite les ratés silencieux
     * quand l'appelant passe un alias minuscule ou avec espaces.
     */
    private val normalizedEssenceRegion: Map<Pair<String, FrenchRegion>, Double> =
        essenceRegionCoefficients.entries.associate { (key, value) ->
            (key.first.trim().uppercase() to key.second) to value
        }

    /**
     * Coefficient régional pour une essence dans une région.
     * Priorité : surcharge spécifique essence×région > coefficient moyen de la région.
     * Insensible à la casse / aux espaces sur le code essence.
     */
    fun coefficient(essenceCode: String, region: FrenchRegion): Double {
        val key = essenceCode.trim().uppercase()
        val specific = normalizedEssenceRegion[key to region]
        if (specific != null) return specific
        return region.priceCoefficient
    }
}

/**
 * Coefficient de taille de lot — économie d'échelle.
 * Source : CNPF - Estimer et vendre ses bois, FBF - Méthodologie indicateur.
 *
 * - <50 m³ : pénalité (coûts d'exploitation proportionnellement plus élevés)
 * - 50-200 m³ : prix de référence
 * - >200 m³ : prime (économie d'échelle, attractivité pour acheteurs)
 */
object LotSizeCoefficients {

    fun coefficient(lotVolumeM3: Double?): Double {
        if (lotVolumeM3 == null || lotVolumeM3 <= 0.0) return 1.0
        return when {
            lotVolumeM3 < 50.0 -> 0.85   // -10 à -20%
            lotVolumeM3 < 100.0 -> 0.95  // -5%
            lotVolumeM3 <= 200.0 -> 1.0  // référence
            lotVolumeM3 <= 500.0 -> 1.05 // +5%
            else -> 1.10                 // +10%
        }
    }

    val source: String = "CNPF - Estimer et vendre ses bois"
}
