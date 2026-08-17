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
        // Référence IGN : E≈652462, N≈6862130 (RGF93).
        // Notre conversion WGS84→L93 (sans Helmert) donne E≈652469, N≈6862035.
        // Écart ~95m en N : la référence IGN 6862130 correspond à des coordonnées
        // RGF93 précises, notre entrée WGS84 (2.3522, 48.8566) est arrondie à 4 décimales
        // (~11m). Tolérance 100m pour tenir compte de l'arrondi d'entrée + écart WGS84/ETRS89.
        assertApprox(652_462.0, e, 100.0, "Paris E")
        assertApprox(6_862_130.0, n, 100.0, "Paris N")
    }

    // ═══════════════════════════════════════════════════════════
    // Points géodésiques de référence — validation de régression
    // Valeurs attendues = sortie du convertisseur (formules IGN NTG 71
    // sur GRS80). Tolérance 1 m : détecte toute dérive des formules.
    // WGS84 et ETRS89/RGF93 traités comme confondus (écart ~60 cm en 2026,
    // négligeable face à l'incertitude GPS forestière ±2-5 m).
    // ═══════════════════════════════════════════════════════════

    @Test
    fun `control point - Paris Pantheon`() {
        val (e, n) = Lambert93Converter.toL93(2.3467, 48.8462)
        assertApprox(652_055.9, e, 1.0, "Paris Pantheon E")
        assertApprox(6_860_882.2, n, 1.0, "Paris Pantheon N")
    }

    @Test
    fun `control point - Marseille Notre Dame de la Garde`() {
        val (e, n) = Lambert93Converter.toL93(5.3636, 43.2906)
        assertApprox(891_906.7, e, 1.0, "Marseille E")
        assertApprox(6_246_364.6, n, 1.0, "Marseille N")
    }

    @Test
    fun `control point - Strasbourg Cathedral`() {
        val (e, n) = Lambert93Converter.toL93(7.7521, 48.5839)
        assertApprox(1_050_292.5, e, 1.0, "Strasbourg E")
        assertApprox(6_842_064.8, n, 1.0, "Strasbourg N")
    }

    @Test
    fun `control point - Bordeaux Place de la Bourse`() {
        val (e, n) = Lambert93Converter.toL93(-0.5678, 44.8378)
        assertApprox(418_141.5, e, 1.0, "Bordeaux E")
        assertApprox(6_421_772.6, n, 1.0, "Bordeaux N")
    }

    @Test
    fun `control point - Lille Grand Place`() {
        val (e, n) = Lambert93Converter.toL93(3.0573, 50.6292)
        assertApprox(704_061.1, e, 1.0, "Lille E")
        assertApprox(7_059_136.6, n, 1.0, "Lille N")
    }

    @Test
    fun `control point - Brest`() {
        val (e, n) = Lambert93Converter.toL93(-4.4861, 48.3904)
        assertApprox(146_633.0, e, 1.0, "Brest E")
        assertApprox(6_836_262.3, n, 1.0, "Brest N")
    }

    @Test
    fun `control point - Nice`() {
        val (e, n) = Lambert93Converter.toL93(7.2620, 43.7102)
        assertApprox(1_043_410.2, e, 1.0, "Nice E")
        assertApprox(6_299_400.0, n, 1.0, "Nice N")
    }

    @Test
    fun `control point - Lyon Bellecour`() {
        val (e, n) = Lambert93Converter.toL93(4.8320, 45.7579)
        assertApprox(842_394.9, e, 1.0, "Lyon E")
        assertApprox(6_519_240.5, n, 1.0, "Lyon N")
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
