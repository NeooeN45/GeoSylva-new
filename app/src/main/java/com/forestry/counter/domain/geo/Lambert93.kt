package com.forestry.counter.domain.geo

import kotlin.math.*

/**
 * Convertisseur Lambert 93 (EPSG:2154) → WGS84 (EPSG:4326).
 *
 * Paramètres officiels RGF93 / Lambert 93 :
 * - Ellipsoïde GRS 1980 : a = 6 378 137 m, 1/f = 298.257222101
 * - Parallèles sécants : φ1 = 44°N, φ2 = 49°N
 * - Latitude d'origine : φ0 = 46.5°N
 * - Méridien central : λ0 = 3°E
 * - False Easting : 700 000 m
 * - False Northing : 6 600 000 m
 */
object Lambert93 {

    // ── Ellipsoïde GRS 1980 ──
    private const val A = 6_378_137.0            // demi-grand axe (m)
    private const val F = 1.0 / 298.257222101    // aplatissement
    private val E = sqrt(2 * F - F * F)          // première excentricité

    // ── Paramètres Lambert 93 ──
    private val PHI_1 = Math.toRadians(44.0)
    private val PHI_2 = Math.toRadians(49.0)
    private val PHI_0 = Math.toRadians(46.5)
    private val LAMBDA_0 = Math.toRadians(3.0)
    private const val X_0 = 700_000.0            // false easting
    private const val Y_0 = 6_600_000.0          // false northing

    // ── Constantes précalculées ──
    private val N: Double
    private val C: Double
    private val XS: Double
    private val YS: Double

    init {
        val m1 = cos(PHI_1) / sqrt(1 - E * E * sin(PHI_1) * sin(PHI_1))
        val m2 = cos(PHI_2) / sqrt(1 - E * E * sin(PHI_2) * sin(PHI_2))
        val t0 = latIso(PHI_0)
        val t1 = latIso(PHI_1)
        val t2 = latIso(PHI_2)

        N = (ln(m1) - ln(m2)) / (t1 - t2)
        C = m1 * exp(N * t1) / N
        XS = X_0
        YS = Y_0 + C * exp(-N * t0)
    }

    /**
     * Latitude isométrique (conforme) pour une latitude φ donnée.
     */
    private fun latIso(phi: Double): Double {
        val eSinPhi = E * sin(phi)
        return ln(tan(PI / 4 + phi / 2) * ((1 - eSinPhi) / (1 + eSinPhi)).pow(E / 2))
    }

    /**
     * Convertit des coordonnées Lambert 93 (X, Y) en WGS84 (longitude, latitude) en degrés.
     *
     * @param x Easting Lambert 93 en mètres
     * @param y Northing Lambert 93 en mètres
     * @return Pair(longitude, latitude) en degrés décimaux
     */
    fun toWgs84(x: Double, y: Double): Pair<Double, Double> {
        val dx = x - XS
        val dy = YS - y
        val r = sqrt(dx * dx + dy * dy)
        val gamma = atan2(dx, dy)

        val lambda = LAMBDA_0 + gamma / N
        val latIsoInv = -ln(abs(r / C)) / N

        // Itération de Newton pour retrouver φ depuis la latitude isométrique
        var phi = 2 * atan(exp(latIsoInv)) - PI / 2
        repeat(12) {
            val eSinPhi = E * sin(phi)
            val phiNew = 2 * atan(
                ((1 + eSinPhi) / (1 - eSinPhi)).pow(E / 2) * exp(latIsoInv)
            ) - PI / 2
            if (abs(phiNew - phi) < 1e-12) {
                phi = phiNew
                return@repeat
            }
            phi = phiNew
        }

        return Pair(Math.toDegrees(lambda), Math.toDegrees(phi))
    }

    /**
     * Convertit un tableau de coordonnées Lambert 93 en WGS84.
     *
     * @param coords Liste de paires (X, Y) Lambert 93
     * @return Liste de paires (longitude, latitude) WGS84
     */
    fun batchToWgs84(coords: List<Pair<Double, Double>>): List<Pair<Double, Double>> =
        coords.map { (x, y) -> toWgs84(x, y) }
}
