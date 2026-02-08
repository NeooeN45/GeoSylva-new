package com.forestry.counter.domain.geo

import kotlin.math.*

/**
 * Convertisseur Lambert 93 (EPSG:2154) → WGS84 (EPSG:4326).
 *
 * Formules : Snyder, "Map Projections — A Working Manual" (USGS PP 1395),
 * Lambert Conformal Conic 2SP, adaptées aux paramètres officiels RGF93.
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
    private const val FLAT = 1.0 / 298.257222101 // aplatissement
    private val E = sqrt(2 * FLAT - FLAT * FLAT) // première excentricité

    // ── Paramètres Lambert 93 ──
    private val PHI_1 = Math.toRadians(44.0)
    private val PHI_2 = Math.toRadians(49.0)
    private val PHI_0 = Math.toRadians(46.5)
    private val LAMBDA_0 = Math.toRadians(3.0)
    private const val FE = 700_000.0             // false easting
    private const val FN = 6_600_000.0           // false northing

    // ── Constantes précalculées (Snyder) ──
    private val N: Double     // cone constant
    private val FF: Double    // Snyder F
    private val RHO_0: Double // ρ at latitude of origin

    init {
        val m1 = mFunc(PHI_1)
        val m2 = mFunc(PHI_2)
        val t0 = tFunc(PHI_0)
        val t1 = tFunc(PHI_1)
        val t2 = tFunc(PHI_2)

        N = (ln(m1) - ln(m2)) / (ln(t1) - ln(t2))
        FF = m1 / (N * t1.pow(N))
        RHO_0 = A * FF * t0.pow(N)
    }

    /**
     * Snyder t(φ) = tan(π/4 − φ/2) / ((1 − e·sinφ)/(1 + e·sinφ))^(e/2)
     */
    private fun tFunc(phi: Double): Double {
        val eSinPhi = E * sin(phi)
        return tan(PI / 4 - phi / 2) / ((1 - eSinPhi) / (1 + eSinPhi)).pow(E / 2)
    }

    /**
     * m(φ) = cos(φ) / √(1 − e²·sin²(φ))
     */
    private fun mFunc(phi: Double): Double {
        val sinPhi = sin(phi)
        return cos(phi) / sqrt(1 - E * E * sinPhi * sinPhi)
    }

    /**
     * Convertit des coordonnées Lambert 93 (X, Y) en WGS84 (longitude, latitude) en degrés.
     *
     * @param x Easting Lambert 93 en mètres
     * @param y Northing Lambert 93 en mètres
     * @return Pair(longitude, latitude) en degrés décimaux
     */
    fun toWgs84(x: Double, y: Double): Pair<Double, Double> {
        val dx = x - FE
        val dy = RHO_0 - (y - FN)
        val rho = sign(N) * sqrt(dx * dx + dy * dy)
        val theta = atan2(dx, dy)

        val lambda = theta / N + LAMBDA_0
        val t = (rho / (A * FF)).pow(1.0 / N)

        // Itération pour retrouver φ depuis t (Snyder eq. 7-9)
        var phi = PI / 2 - 2 * atan(t)
        repeat(15) {
            val eSinPhi = E * sin(phi)
            val phiNew = PI / 2 - 2 * atan(
                t * ((1 - eSinPhi) / (1 + eSinPhi)).pow(E / 2)
            )
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
