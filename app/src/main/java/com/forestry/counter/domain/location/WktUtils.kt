package com.forestry.counter.domain.location

/**
 * Utilitaires de parsing WKT (Well-Known Text) pour les coordonnées GPS.
 */
object WktUtils {

    /**
     * Parse un WKT POINT ou POINT Z en (longitude, latitude, altitude?).
     * Exemples supportés :
     *   POINT (2.3488 48.8534)
     *   POINT Z (2.3488 48.8534 35.0)
     */
    fun parsePointZ(wkt: String?): Triple<Double?, Double?, Double?> {
        if (wkt.isNullOrBlank()) return Triple(null, null, null)
        val cleaned = wkt.trim().replace(Regex("\\s+"), " ")
        val regex = Regex("POINT( Z)? ?\\( ?([-0-9.]+) ([-0-9.]+)( [-0-9.]+)? ?\\)")
        val m = regex.find(cleaned) ?: return Triple(null, null, null)
        val lon = m.groupValues.getOrNull(2)?.toDoubleOrNull()
        val lat = m.groupValues.getOrNull(3)?.toDoubleOrNull()
        val alt = m.groupValues.getOrNull(4)?.trim()?.toDoubleOrNull()
        return Triple(lon, lat, alt)
    }
}
