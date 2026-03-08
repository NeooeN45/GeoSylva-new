package com.forestry.counter.domain.model

/**
 * Zone bioclimatique française.
 * Détection automatique par coordonnées GPS + altitude.
 */
enum class ClimateZone(val label: String, val labelFr: String) {
    ATLANTIQUE("Atlantic", "Atlantique"),
    SEMI_OCEANIQUE("Semi-oceanic", "Semi-océanique"),
    CONTINENTALE("Continental", "Continentale"),
    MONTAGNARDE("Montane", "Montagnarde"),
    MEDITERRANEENNE("Mediterranean", "Méditerranéenne"),
    UNKNOWN("Unknown", "Inconnue");

    companion object {
        /**
         * Détecte la zone bioclimatique à partir des coordonnées GPS et de l'altitude.
         * Basé sur les grands domaines bioclimatiques français (CNPF/ONF).
         *
         * @param lat  Latitude WGS84 (ex: 45.75)
         * @param lon  Longitude WGS84 (ex: 4.85)
         * @param altM Altitude en mètres (nullable)
         */
        fun detect(lat: Double, lon: Double, altM: Double? = null): ClimateZone {
            val alt = altM ?: 0.0

            // Montagnard : priorité si altitude suffisante
            // > 600m en zone alpine/pyrénéenne, > 800m en zone centrale
            if (alt >= 600.0 && (lat < 46.0 || lon > 5.5)) return MONTAGNARDE
            if (alt >= 800.0) return MONTAGNARDE

            // Méditerranéen : sud de la France, est du Rhône
            // Basse Provence, Languedoc, Roussillon, Corse
            if (lat < 44.5 && lon > 3.0) return MEDITERRANEENNE
            if (lat < 43.5) return MEDITERRANEENNE

            // Continental : est de la France (Alsace, Lorraine, Bourgogne, est)
            if (lon > 5.5 && lat > 46.5) return CONTINENTALE
            if (lon > 6.5) return CONTINENTALE

            // Atlantique pur : façade ouest
            if (lon < 1.0) return ATLANTIQUE
            if (lon < 2.5 && lat > 44.0) return ATLANTIQUE

            // Semi-océanique : transition
            return SEMI_OCEANIQUE
        }

        /**
         * Extrait lat/lon depuis un WKT POINT "POINT(lon lat)"
         */
        fun detectFromWkt(wkt: String?, altM: Double? = null): ClimateZone {
            if (wkt == null) return UNKNOWN
            return try {
                val coords = wkt.removePrefix("POINT(").removeSuffix(")").split(" ")
                val lon = coords[0].toDouble()
                val lat = coords[1].toDouble()
                detect(lat, lon, altM)
            } catch (_: Exception) {
                UNKNOWN
            }
        }
    }
}
