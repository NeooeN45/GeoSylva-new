package com.forestry.counter.domain.calculation.pricing

/**
 * Les 13 régions administratives françaises métropolitaines.
 *
 * Remplace GRECO (zones écologiques infra-départementales) comme découpage
 * régional du moteur de prix, pour deux raisons concrètes :
 *  1. Les sources de prix du bois en accès libre (France Bois Forêt, ONF,
 *     observatoires Fibois régionaux) publient PAR RÉGION ADMINISTRATIVE, pas par GRECO.
 *  2. La détection GPS → commune INSEE → département → région est DÉTERMINISTE,
 *     alors que département → GRECO était ambigu (départements à cheval sur plusieurs GRECO).
 *
 * ⚠️ Toutes les régions n'ont pas de source de prix publique : `hasPriceSource = false`
 * signifie coefficient national (×1.0) assumé, à afficher honnêtement à l'utilisateur.
 *
 * Les coefficients sont INDICATIFS (ordres de grandeur FBF/Fibois) et doivent être
 * validés/ajustés ; ils sont surchargeable par l'utilisateur dans les Settings.
 */
enum class FrenchRegion(
    val code: String,
    val labelFr: String,
    /** Codes département (INSEE, 2 caractères ; Corse = "2A"/"2B"). */
    val departments: List<String>,
    /** Coefficient prix moyen vs national (1.0 = moyenne nationale). */
    val priceCoefficient: Double,
    /** true si une source de prix publique existe pour cette région. */
    val hasPriceSource: Boolean,
    val source: String
) {
    ARA(
        "ARA", "Auvergne-Rhône-Alpes",
        listOf("01", "03", "07", "15", "26", "38", "42", "43", "63", "69", "73", "74"),
        priceCoefficient = 0.95, hasPriceSource = true,
        source = "Fibois AuRA - Observatoire des prix"
    ),
    BFC(
        "BFC", "Bourgogne-Franche-Comté",
        listOf("21", "25", "39", "58", "70", "71", "89", "90"),
        priceCoefficient = 1.15, hasPriceSource = true,
        source = "Fibois BFC - Chêne premium / Douglas Jura"
    ),
    BRE(
        "BRE", "Bretagne",
        listOf("22", "29", "35", "56"),
        priceCoefficient = 0.95, hasPriceSource = true,
        source = "Abibois - Observatoire régional"
    ),
    CVL(
        "CVL", "Centre-Val de Loire",
        listOf("18", "28", "36", "37", "41", "45"),
        priceCoefficient = 1.0, hasPriceSource = false,
        source = "Pas de source publique — coefficient national"
    ),
    COR(
        "COR", "Corse",
        listOf("2A", "2B"),
        priceCoefficient = 1.0, hasPriceSource = false,
        source = "Pas de source publique — coefficient national"
    ),
    GES(
        "GES", "Grand Est",
        listOf("08", "10", "51", "52", "54", "55", "57", "67", "68", "88"),
        priceCoefficient = 1.10, hasPriceSource = true,
        source = "Fibois Grand Est - Résineux / Douglas Vosges"
    ),
    HDF(
        "HDF", "Hauts-de-France",
        listOf("02", "59", "60", "62", "80"),
        priceCoefficient = 1.0, hasPriceSource = false,
        source = "Pas de source publique — coefficient national"
    ),
    IDF(
        "IDF", "Île-de-France",
        listOf("75", "77", "78", "91", "92", "93", "94", "95"),
        priceCoefficient = 1.0, hasPriceSource = false,
        source = "Pas de source publique — coefficient national"
    ),
    NOR(
        "NOR", "Normandie",
        listOf("14", "27", "50", "61", "76"),
        priceCoefficient = 1.0, hasPriceSource = false,
        source = "Pas de source publique — coefficient national"
    ),
    NAQ(
        "NAQ", "Nouvelle-Aquitaine",
        listOf("16", "17", "19", "23", "24", "33", "40", "47", "64", "79", "86", "87"),
        priceCoefficient = 1.05, hasPriceSource = true,
        source = "Fibois Nouvelle-Aquitaine - Pin maritime Landes"
    ),
    OCC(
        "OCC", "Occitanie",
        listOf("09", "11", "12", "30", "31", "32", "34", "46", "48", "65", "66", "81", "82"),
        priceCoefficient = 0.90, hasPriceSource = true,
        source = "Fibois Occitanie - Douglas / résineux"
    ),
    PDL(
        "PDL", "Pays de la Loire",
        listOf("44", "49", "53", "72", "85"),
        priceCoefficient = 1.0, hasPriceSource = false,
        source = "Pas de source publique — coefficient national"
    ),
    PAC(
        "PAC", "Provence-Alpes-Côte d'Azur",
        listOf("04", "05", "06", "13", "83", "84"),
        priceCoefficient = 1.0, hasPriceSource = false,
        source = "Pas de source publique — coefficient national"
    );

    companion object {
        /**
         * Région à partir d'un code département (ex: "25" → BFC). Déterministe.
         * Gère la Corse ("2A"/"2B") et normalise le padding ("1" → "01").
         */
        fun fromDepartment(deptCode: String): FrenchRegion? {
            val raw = deptCode.trim().uppercase()
            val normalized = when {
                raw == "2A" || raw == "2B" -> raw
                raw.length == 1 -> "0$raw"
                else -> raw.take(2)
            }
            return entries.firstOrNull { normalized in it.departments }
        }

        /**
         * Région à partir d'un code commune INSEE (5 caractères).
         * Les 2 premiers caractères = département (Corse : "2A"/"2B").
         */
        fun fromCodeCommune(codeCommune: String): FrenchRegion? {
            val c = codeCommune.trim()
            if (c.length < 2) return null
            val dept = if (c.startsWith("2A") || c.startsWith("2B")) c.substring(0, 2) else c.substring(0, 2)
            return fromDepartment(dept)
        }

        /** Région à partir de son code (ex: "GES"). */
        fun fromCode(code: String): FrenchRegion? {
            val upper = code.trim().uppercase()
            return entries.firstOrNull { it.code == upper }
        }

        /** Toutes les régions, pour l'affichage/sélecteur UI. */
        val ALL: List<FrenchRegion> = entries.toList()
    }
}
