package com.forestry.counter.domain.calculation

import com.forestry.counter.domain.model.Tige
import org.junit.Assert.*
import org.junit.Test

class SanityCheckerTest {

    private fun tige(
        diamCm: Double = 30.0,
        hauteurM: Double? = 20.0,
        fCoef: Double? = null,
        id: String = "t1",
        essenceCode: String = "HETRE",
        placetteId: String = "p1",
        timestamp: Long = System.currentTimeMillis()
    ) = Tige(
        id = id,
        parcelleId = "par1",
        placetteId = placetteId,
        essenceCode = essenceCode,
        diamCm = diamCm,
        hauteurM = hauteurM,
        gpsWkt = null,
        precisionM = null,
        altitudeM = null,
        timestamp = timestamp,
        note = null,
        produit = null,
        fCoef = fCoef,
        valueEur = null
    )

    // ── Input: diameter ──

    @Test
    fun `diameter zero triggers ERROR`() {
        val w = SanityChecker.checkTige(tige(diamCm = 0.0))
        assertTrue(w.any { it.code == "diam_zero" && it.severity == SanitySeverity.ERROR })
    }

    @Test
    fun `diameter negative triggers ERROR`() {
        val w = SanityChecker.checkTige(tige(diamCm = -5.0))
        assertTrue(w.any { it.code == "diam_zero" && it.severity == SanitySeverity.ERROR })
    }

    @Test
    fun `diameter 0_3 cm triggers too_small ERROR`() {
        val w = SanityChecker.checkTige(tige(diamCm = 0.3))
        assertTrue(w.any { it.code == "diam_too_small" })
    }

    @Test
    fun `diameter 350 cm triggers too_large ERROR`() {
        val w = SanityChecker.checkTige(tige(diamCm = 350.0))
        assertTrue(w.any { it.code == "diam_too_large" && it.severity == SanitySeverity.ERROR })
    }

    @Test
    fun `diameter 160 cm triggers very_large WARNING`() {
        val w = SanityChecker.checkTige(tige(diamCm = 160.0))
        assertTrue(w.any { it.code == "diam_very_large" && it.severity == SanitySeverity.WARNING })
    }

    @Test
    fun `diameter 30 cm is clean`() {
        val w = SanityChecker.checkTige(tige(diamCm = 30.0))
        assertTrue(w.none { it.domain == SanityDomain.INPUT && it.code.startsWith("diam") })
    }

    // ── Input: height ──

    @Test
    fun `height zero triggers ERROR`() {
        val w = SanityChecker.checkTige(tige(hauteurM = 0.0))
        assertTrue(w.any { it.code == "height_zero" })
    }

    @Test
    fun `height 70 m triggers too_large ERROR`() {
        val w = SanityChecker.checkTige(tige(hauteurM = 70.0))
        assertTrue(w.any { it.code == "height_too_large" && it.severity == SanitySeverity.ERROR })
    }

    @Test
    fun `height 55 m triggers very_large WARNING`() {
        val w = SanityChecker.checkTige(tige(hauteurM = 55.0))
        assertTrue(w.any { it.code == "height_very_large" && it.severity == SanitySeverity.WARNING })
    }

    @Test
    fun `height null produces no height warnings`() {
        val w = SanityChecker.checkTige(tige(hauteurM = null))
        assertTrue(w.none { it.code.startsWith("height") })
    }

    @Test
    fun `height 22 m is clean`() {
        val w = SanityChecker.checkTige(tige(hauteurM = 22.0))
        assertTrue(w.none { it.code.startsWith("height") })
    }

    // ── Input: H/D ratio ──

    @Test
    fun `extreme HD ratio triggers ERROR`() {
        // 40m height / 5cm diam = H/D = 40/5 = 8.0 → extreme (threshold 2.0)
        val w = SanityChecker.checkTige(tige(diamCm = 5.0, hauteurM = 40.0))
        assertTrue(w.any { it.code == "hd_ratio_extreme" })
    }

    @Test
    fun `high HD ratio triggers WARNING`() {
        // 30m / 20cm = H/D = 1.5 → high (threshold 1.2)
        val w = SanityChecker.checkTige(tige(diamCm = 20.0, hauteurM = 30.0))
        assertTrue(w.any { it.code == "hd_ratio_high" && it.severity == SanitySeverity.WARNING })
    }

    @Test
    fun `normal HD ratio is clean`() {
        // 20m / 30cm = H/D = 0.67 → fine (typical French forest)
        val w = SanityChecker.checkTige(tige(diamCm = 30.0, hauteurM = 20.0))
        assertTrue(w.none { it.code.startsWith("hd_ratio") })
    }

    // ── Input: coef forme ──

    @Test
    fun `coef forme 0_10 triggers WARNING`() {
        val w = SanityChecker.checkTige(tige(fCoef = 0.10))
        assertTrue(w.any { it.code == "coef_forme_out_of_range" })
    }

    @Test
    fun `coef forme 0_45 is clean`() {
        val w = SanityChecker.checkTige(tige(fCoef = 0.45))
        assertTrue(w.none { it.code == "coef_forme_out_of_range" })
    }

    // ── Volume checks ──

    @Test
    fun `negative volume triggers ERROR`() {
        val w = SanityChecker.checkTreeVolume("t1", 30.0, -0.5)
        assertTrue(w.any { it.code == "volume_negative" && it.severity == SanitySeverity.ERROR })
    }

    @Test
    fun `volume 35 m3 triggers tree_extreme ERROR`() {
        val w = SanityChecker.checkTreeVolume("t1", 80.0, 35.0)
        assertTrue(w.any { it.code == "volume_tree_extreme" })
    }

    @Test
    fun `volume 18 m3 triggers tree_very_large WARNING`() {
        val w = SanityChecker.checkTreeVolume("t1", 100.0, 18.0)
        assertTrue(w.any { it.code == "volume_tree_very_large" && it.severity == SanitySeverity.WARNING })
    }

    @Test
    fun `normal volume is clean`() {
        val w = SanityChecker.checkTreeVolume("t1", 30.0, 0.8)
        assertTrue(w.isEmpty())
    }

    // ── Revenue checks ──

    @Test
    fun `negative revenue triggers ERROR`() {
        val w = SanityChecker.checkTreeRevenue("t1", -100.0)
        assertTrue(w.any { it.code == "revenue_negative" })
    }

    @Test
    fun `extreme revenue triggers WARNING`() {
        val w = SanityChecker.checkTreeRevenue("t1", 60_000.0)
        assertTrue(w.any { it.code == "revenue_tree_extreme" })
    }

    // ── Aggregate checks ──

    @Test
    fun `surface zero triggers ERROR`() {
        val w = SanityChecker.checkAggregates(
            nPerHa = 500.0, gPerHa = 25.0, vPerHa = 300.0,
            revenuePerHa = null, surfaceHa = 0.0, ratioVG = null
        )
        assertTrue(w.any { it.code == "surface_zero" })
    }

    @Test
    fun `very high N per ha triggers WARNING`() {
        val w = SanityChecker.checkAggregates(
            nPerHa = 8000.0, gPerHa = 30.0, vPerHa = 400.0,
            revenuePerHa = null, surfaceHa = 0.5, ratioVG = null
        )
        assertTrue(w.any { it.code == "n_ha_very_high" })
    }

    @Test
    fun `extreme N per ha triggers ERROR`() {
        val w = SanityChecker.checkAggregates(
            nPerHa = 25000.0, gPerHa = 30.0, vPerHa = 400.0,
            revenuePerHa = null, surfaceHa = 0.01, ratioVG = null
        )
        assertTrue(w.any { it.code == "n_ha_extreme" && it.severity == SanitySeverity.ERROR })
    }

    @Test
    fun `extreme G per ha triggers ERROR`() {
        val w = SanityChecker.checkAggregates(
            nPerHa = 500.0, gPerHa = 200.0, vPerHa = 3000.0,
            revenuePerHa = null, surfaceHa = 0.5, ratioVG = null
        )
        assertTrue(w.any { it.code == "g_ha_extreme" })
    }

    @Test
    fun `normal aggregates are clean`() {
        val w = SanityChecker.checkAggregates(
            nPerHa = 450.0, gPerHa = 28.0, vPerHa = 350.0,
            revenuePerHa = 12000.0, surfaceHa = 1.0, ratioVG = 12.5
        )
        assertTrue(w.isEmpty())
    }

    @Test
    fun `low VG ratio triggers WARNING`() {
        val w = SanityChecker.checkAggregates(
            nPerHa = 500.0, gPerHa = 30.0, vPerHa = 60.0,
            revenuePerHa = null, surfaceHa = 1.0, ratioVG = 2.0
        )
        assertTrue(w.any { it.code == "vg_ratio_low" })
    }

    @Test
    fun `high VG ratio triggers WARNING`() {
        val w = SanityChecker.checkAggregates(
            nPerHa = 500.0, gPerHa = 10.0, vPerHa = 400.0,
            revenuePerHa = null, surfaceHa = 1.0, ratioVG = 40.0
        )
        assertTrue(w.any { it.code == "vg_ratio_high" })
    }

    // ── Batch checks ──

    @Test
    fun `checkAllTiges detects duplicates`() {
        val ts = System.currentTimeMillis()
        val tiges = listOf(
            tige(id = "t1", essenceCode = "HETRE", diamCm = 30.0, placetteId = "p1", timestamp = ts),
            tige(id = "t2", essenceCode = "HETRE", diamCm = 30.0, placetteId = "p1", timestamp = ts + 5000)
        )
        val w = SanityChecker.checkAllTiges(tiges)
        assertTrue(w.any { it.code == "potential_duplicates" })
    }

    @Test
    fun `checkAllTiges does not flag distinct tiges`() {
        val ts = System.currentTimeMillis()
        val tiges = listOf(
            tige(id = "t1", essenceCode = "HETRE", diamCm = 30.0, placetteId = "p1", timestamp = ts),
            tige(id = "t2", essenceCode = "HETRE", diamCm = 35.0, placetteId = "p1", timestamp = ts + 5000)
        )
        val w = SanityChecker.checkAllTiges(tiges)
        assertTrue(w.none { it.code == "potential_duplicates" })
    }

    @Test
    fun `checkAllTiges limits detailed alerts`() {
        // 20 tiges with diameter 0 → should cap detailed output
        val tiges = (1..20).map { tige(id = "t$it", diamCm = 0.0) }
        val w = SanityChecker.checkAllTiges(tiges)
        assertTrue(w.any { it.code == "many_input_errors" })
        // Should not have 20 individual diam_zero warnings
        val diamZeroCount = w.count { it.code == "diam_zero" }
        assertTrue(diamZeroCount <= 10)
    }
}
