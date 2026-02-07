package com.forestry.counter.domain.location

import org.junit.Assert.*
import org.junit.Test
import kotlin.math.abs

class Lambert93ConverterTest {

    private fun assertApprox(expected: Double, actual: Double, delta: Double, msg: String = "") {
        assertTrue("$msg — expected ≈$expected but was $actual (diff=${abs(expected - actual)} > delta=$delta)",
            abs(expected - actual) <= delta)
    }

    // ═══════════════════════════════════════════════════════════
    // WGS84 → Lambert 93 — cohérence de la projection
    // ═══════════════════════════════════════════════════════════

    @Test
    fun `Paris is in valid L93 range`() {
        val (e, n) = Lambert93Converter.toL93(2.3522, 48.8566)
        assertTrue("Paris E ($e) should be in [100_000, 1_300_000]", e in 100_000.0..1_300_000.0)
        assertTrue("Paris N ($n) should be in [6_000_000, 7_200_000]", n in 6_000_000.0..7_200_000.0)
    }

    @Test
    fun `Strasbourg is east of Paris`() {
        val (eParis, _) = Lambert93Converter.toL93(2.3522, 48.8566)
        val (eStrasbourg, _) = Lambert93Converter.toL93(7.7521, 48.5734)
        assertTrue("Strasbourg ($eStrasbourg) should be east of Paris ($eParis)", eStrasbourg > eParis)
    }

    @Test
    fun `Bordeaux is south of Paris`() {
        val (_, nParis) = Lambert93Converter.toL93(2.3522, 48.8566)
        val (_, nBordeaux) = Lambert93Converter.toL93(-0.5792, 44.8378)
        assertTrue("Bordeaux ($nBordeaux) should be south (lower N) than Paris ($nParis)", nBordeaux < nParis)
    }

    // ═══════════════════════════════════════════════════════════
    // Emprise géographique France métropolitaine
    // isInFranceMetro(lonDeg, latDeg)
    // ═══════════════════════════════════════════════════════════

    @Test
    fun `Paris is in France`() {
        assertTrue(Lambert93Converter.isInFranceMetro(2.3522, 48.8566))
    }

    @Test
    fun `Rome is not in France`() {
        // Rome : lon=12.496, lat=41.902 — hors emprise car lon > 10
        assertFalse(Lambert93Converter.isInFranceMetro(12.496, 41.902))
    }

    @Test
    fun `Nice is in France`() {
        assertTrue(Lambert93Converter.isInFranceMetro(7.2620, 43.7102))
    }

    @Test
    fun `Brest is in France`() {
        assertTrue(Lambert93Converter.isInFranceMetro(-4.4861, 48.3904))
    }

    @Test
    fun `Reunion is not in France metropolitaine`() {
        assertFalse(Lambert93Converter.isInFranceMetro(55.5364, -21.1151))
    }

    // ═══════════════════════════════════════════════════════════
    // Formatage — formatL93(easting, northing) → String
    // ═══════════════════════════════════════════════════════════

    @Test
    fun `formatL93 returns non-empty string with E and N`() {
        val formatted = Lambert93Converter.formatL93(652_462.0, 6_862_130.0)
        assertTrue("Formatted should contain E", formatted.contains("E"))
        assertTrue("Formatted should contain N", formatted.contains("N"))
    }

    // ═══════════════════════════════════════════════════════════
    // Aller-retour WGS84 → L93 → WGS84
    // ═══════════════════════════════════════════════════════════

    @Test
    fun `toL93 Paris matches IGN reference`() {
        val (e, n) = Lambert93Converter.toL93(2.3522, 48.8566)
        // Référence IGN : E≈652462, N≈6862130 — tolérance 50m
        assertApprox(652_462.0, e, 50.0, "Paris E")
        assertApprox(6_862_130.0, n, 150.0, "Paris N")
    }

    @Test
    fun `round trip Paris`() {
        val lon = 2.3522; val lat = 48.8566
        val (e, n) = Lambert93Converter.toL93(lon, lat)
        val (lonBack, latBack) = Lambert93Converter.toWGS84(e, n)
        assertApprox(lon, lonBack, 0.0001, "Round-trip lon")
        assertApprox(lat, latBack, 0.0001, "Round-trip lat")
    }

    @Test
    fun `round trip Bordeaux`() {
        val lon = -0.5792; val lat = 44.8378
        val (e, n) = Lambert93Converter.toL93(lon, lat)
        val (lonBack, latBack) = Lambert93Converter.toWGS84(e, n)
        assertApprox(lon, lonBack, 0.0001, "Round-trip lon")
        assertApprox(lat, latBack, 0.0001, "Round-trip lat")
    }
}
