package com.forestry.counter.domain.location

import kotlin.math.*

/**
 * Convertisseur WGS84 (EPSG:4326) ↔ Lambert 93 (EPSG:2154).
 *
 * Lambert 93 est le système de projection officiel en France métropolitaine
 * depuis le décret n° 2000-1276 du 26 décembre 2000.
 *
 * Paramètres de projection :
 * - Ellipsoïde : GRS80 (IAG)
 * - Méridien central : 3°E (Greenwich)
 * - Parallèles standard : 44°N et 49°N
 * - Latitude d'origine : 46°30'N
 * - Fausse abscisse : 700 000 m
 * - Fausse ordonnée : 6 600 000 m
 *
 * Sources :
 * - IGN : « Algorithmes de conversion de coordonnées »
 *   (NT/G 71, Service de Géodésie et Nivellement)
 * - Registre EPSG : https://epsg.io/2154
 */
object Lambert93Converter {

    // ── Paramètres de l'ellipsoïde GRS80 ──
    private const val A = 6378137.0           // demi-grand axe (m)
    private const val F_INV = 298.257222101   // aplatissement inverse
    private val F = 1.0 / F_INV              // aplatissement
    private val B = A * (1.0 - F)            // demi-petit axe
    private val E2 = 2 * F - F * F           // première excentricité²
    private val E = sqrt(E2)                  // première excentricité

    // ── Paramètres de la projection conique conforme de Lambert ──
    private val PHI_1 = Math.toRadians(44.0)     // 1er parallèle standard
    private val PHI_2 = Math.toRadians(49.0)     // 2e parallèle standard
    private val PHI_0 = Math.toRadians(46.5)     // latitude d'origine
    private val LAMBDA_0 = Math.toRadians(3.0)   // méridien central
    private const val X_0 = 700000.0             // fausse abscisse (E)
    private const val Y_0 = 6600000.0            // fausse ordonnée (N)

    // ── Constantes pré-calculées ──
    private val N: Double
    private val C: Double
    private val XS: Double
    private val YS: Double

    init {
        val n1 = latIso(PHI_1)
        val n2 = latIso(PHI_2)
        val w1 = grandNormal(PHI_1)
        val w2 = grandNormal(PHI_2)

        N = (ln(w2 * cos(PHI_1)) - ln(w1 * cos(PHI_2))) / (n1 - n2)
        C = (w1 * cos(PHI_1) / N) * exp(N * n1)

        val r0 = C * exp(-N * latIso(PHI_0))
        XS = X_0
        YS = Y_0 + r0
    }

    /**
     * Convertit des coordonnées WGS84 (lon/lat en degrés) en Lambert 93 (E, N en mètres).
     *
     * @param lonDeg Longitude WGS84 en degrés décimaux
     * @param latDeg Latitude WGS84 en degrés décimaux
     * @return Pair(easting, northing) en mètres Lambert 93
     */
    fun toL93(lonDeg: Double, latDeg: Double): Pair<Double, Double> {
        val phi = Math.toRadians(latDeg)
        val lambda = Math.toRadians(lonDeg)

        val l = latIso(phi)
        val r = C * exp(-N * l)
        val gamma = N * (lambda - LAMBDA_0)

        val x = XS + r * sin(gamma)
        val y = YS - r * cos(gamma)
        return Pair(x, y)
    }

    /**
     * Convertit des coordonnées Lambert 93 (E, N en mètres) en WGS84 (lon, lat en degrés).
     *
     * @param easting  Abscisse Lambert 93 en mètres
     * @param northing Ordonnée Lambert 93 en mètres
     * @return Pair(longitude, latitude) en degrés WGS84
     */
    fun toWGS84(easting: Double, northing: Double): Pair<Double, Double> {
        val dx = easting - XS
        val dy = YS - northing
        val r = sqrt(dx * dx + dy * dy)
        val gamma = atan2(dx, dy)

        val lambda = LAMBDA_0 + gamma / N
        val l = -ln(abs(r / C)) / N

        // Itération pour retrouver la latitude à partir de la latitude isométrique
        var phi = 2.0 * atan(exp(l)) - PI / 2.0
        for (i in 0 until 30) {
            val eSinPhi = E * sin(phi)
            val phiNew = 2.0 * atan(
                ((1.0 + eSinPhi) / (1.0 - eSinPhi)).pow(E / 2.0) * exp(l)
            ) - PI / 2.0
            if (abs(phiNew - phi) < 1e-12) break
            phi = phiNew
        }

        return Pair(Math.toDegrees(lambda), Math.toDegrees(phi))
    }

    /**
     * Formate des coordonnées Lambert 93 pour affichage.
     * @return "E: 700 000 m — N: 6 600 000 m"
     */
    fun formatL93(easting: Double, northing: Double): String {
        return "E: %.0f m — N: %.0f m".format(easting, northing)
    }

    /**
     * Vérifie si des coordonnées WGS84 sont dans l'emprise Lambert 93
     * (France métropolitaine + marge).
     */
    fun isInFranceMetro(lonDeg: Double, latDeg: Double): Boolean {
        return lonDeg in -6.0..10.0 && latDeg in 41.0..52.0
    }

    // ── Fonctions auxiliaires ──

    /** Grande normale (rayon de courbure dans le premier vertical) */
    private fun grandNormal(phi: Double): Double {
        val sinPhi = sin(phi)
        return A / sqrt(1.0 - E2 * sinPhi * sinPhi)
    }

    /** Latitude isométrique sur l'ellipsoïde GRS80 */
    private fun latIso(phi: Double): Double {
        val eSinPhi = E * sin(phi)
        return ln(tan(PI / 4.0 + phi / 2.0) * ((1.0 - eSinPhi) / (1.0 + eSinPhi)).pow(E / 2.0))
    }
}
