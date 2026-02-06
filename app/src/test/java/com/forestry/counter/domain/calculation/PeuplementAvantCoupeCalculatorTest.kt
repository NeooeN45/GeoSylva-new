package com.forestry.counter.domain.calculation

import com.forestry.counter.domain.model.Tige
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class PeuplementAvantCoupeCalculatorTest {

    private lateinit var calculator: PeuplementAvantCoupeCalculator

    private fun assertApprox(expected: Double, actual: Double, delta: Double = 1e-4, msg: String = "") {
        val diff = abs(expected - actual)
        assertTrue("$msg — expected ≈$expected but was $actual (diff=$diff > delta=$delta)", diff <= delta)
    }

    private fun makeTige(
        essenceCode: String = "DOUGLAS_VERT",
        diamCm: Double,
        hauteurM: Double? = null,
        parcelleId: String = "p1",
        placetteId: String? = "pl1"
    ) = Tige(
        id = "t-${System.nanoTime()}-${(Math.random() * 10000).toInt()}",
        parcelleId = parcelleId,
        placetteId = placetteId,
        essenceCode = essenceCode,
        diamCm = diamCm,
        hauteurM = hauteurM,
        gpsWkt = null,
        precisionM = null,
        altitudeM = null,
        note = null,
        produit = null,
        fCoef = null,
        valueEur = null
    )

    @Before
    fun setUp() {
        calculator = PeuplementAvantCoupeCalculator()
    }

    // ═══════════════════════════════════════════════════════════
    // CAS VIDE
    // ═══════════════════════════════════════════════════════════

    @Test
    fun `empty tiges list returns zeroed result`() {
        val result = calculator.compute(
            tiges = emptyList(),
            surfacePlacetteM2 = 1000.0,
            hoM = 25.0,
            hauteurMoyenneParClasse = emptyMap()
        )
        assertEquals(0, result.totals.nTotal)
        assertApprox(0.0, result.totals.vTotal, msg = "vTotal empty")
        assertApprox(0.0, result.totals.gParcelleTotal, msg = "gTotal empty")
        assertTrue(result.classes.isNotEmpty()) // classes skeleton still present
        assertTrue(result.classes.all { it.nTotal == 0 })
    }

    @Test
    fun `zero surface returns zeroed result`() {
        val tiges = listOf(makeTige(diamCm = 30.0))
        val result = calculator.compute(
            tiges = tiges,
            surfacePlacetteM2 = 0.0,
            hoM = 25.0,
            hauteurMoyenneParClasse = emptyMap()
        )
        assertEquals(0, result.totals.nTotal)
    }

    @Test
    fun `null surface returns zeroed result`() {
        val tiges = listOf(makeTige(diamCm = 30.0))
        val result = calculator.compute(
            tiges = tiges,
            surfacePlacetteM2 = null,
            hoM = 25.0,
            hauteurMoyenneParClasse = emptyMap()
        )
        assertEquals(0, result.totals.nTotal)
    }

    // ═══════════════════════════════════════════════════════════
    // SINGLE TREE — VALIDATION MANUELLE
    // ═══════════════════════════════════════════════════════════

    @Test
    fun `single Douglas D=35 Ho=25 on 1000m2 — manual verification`() {
        val tiges = listOf(makeTige(essenceCode = "DOUGLAS_VERT", diamCm = 35.0))
        val surfM2 = 1000.0
        val ho = 25.0

        val result = calculator.compute(
            tiges = tiges,
            surfacePlacetteM2 = surfM2,
            hoM = ho,
            hauteurMoyenneParClasse = emptyMap()
        )

        // N total = 1
        assertEquals(1, result.totals.nTotal)

        // N/ha = 1 / 0.1 = 10
        val surfHa = surfM2 / 10000.0
        assertApprox(1.0 / surfHa, result.totals.nHaTotal, delta = 0.01, msg = "N/ha")

        // G unitaire pour D=35cm : π/4 × (0.35)² = 0.09621 m²
        val gUnit = PI / 4.0 * (35.0 / 100.0).pow(2.0)
        val row35 = result.classes.first { it.diamClassCm == 35 }
        assertApprox(gUnit, row35.gUnit, delta = 0.0001, msg = "gUnit D=35")

        // G parcelle = gUnit × 1
        assertApprox(gUnit, row35.gParcelle, delta = 0.0001, msg = "gParcelle D=35")

        // G/ha
        assertApprox(gUnit / surfHa, row35.gHa, delta = 0.001, msg = "gHa D=35")

        // Volume total classe : formule = (0.24868 × d² × h + 0.03179 × (d×h - 0.02473)) × N
        val dM = 35.0 / 100.0
        val expectedV = (0.24868 * dM.pow(2.0) * ho) + 0.03179 * (dM * ho - 0.02473)
        assertApprox(expectedV, row35.vTotal, delta = 0.001, msg = "vTotal D=35")

        // Vérification V > 0
        assertTrue("Volume should be positive", result.totals.vTotal > 0.0)
    }

    // ═══════════════════════════════════════════════════════════
    // MULTIPLE TREES — AGGREGATION
    // ═══════════════════════════════════════════════════════════

    @Test
    fun `multiple trees in same class aggregate correctly`() {
        val tiges = listOf(
            makeTige(diamCm = 34.0), // → class 35
            makeTige(diamCm = 36.0), // → class 35
            makeTige(diamCm = 33.0)  // → class 35
        )

        val result = calculator.compute(
            tiges = tiges,
            surfacePlacetteM2 = 1000.0,
            hoM = 25.0,
            hauteurMoyenneParClasse = emptyMap()
        )

        val row35 = result.classes.first { it.diamClassCm == 35 }
        assertEquals(3, row35.nTotal)
        assertEquals(3, result.totals.nTotal)

        // G parcelle = gUnit(35) × 3
        val gUnit35 = PI / 4.0 * (35.0 / 100.0).pow(2.0)
        assertApprox(gUnit35 * 3, row35.gParcelle, delta = 0.001, msg = "gParcelle 3×D35")
    }

    @Test
    fun `trees in different classes produce correct totals`() {
        val tiges = listOf(
            makeTige(diamCm = 25.0),
            makeTige(diamCm = 35.0),
            makeTige(diamCm = 45.0),
            makeTige(diamCm = 55.0)
        )

        val result = calculator.compute(
            tiges = tiges,
            surfacePlacetteM2 = 2000.0,
            hoM = 28.0,
            hauteurMoyenneParClasse = emptyMap()
        )

        assertEquals(4, result.totals.nTotal)

        // Each class has 1 tree
        for (d in listOf(25, 35, 45, 55)) {
            val row = result.classes.first { it.diamClassCm == d }
            assertEquals("Class $d should have 1 tree", 1, row.nTotal)
        }

        // Total G = sum of individual G
        val expectedGTotal = listOf(25, 35, 45, 55).sumOf { d ->
            PI / 4.0 * (d / 100.0).pow(2.0)
        }
        assertApprox(expectedGTotal, result.totals.gParcelleTotal, delta = 0.001, msg = "gTotal multi-class")
    }

    // ═══════════════════════════════════════════════════════════
    // HAUTEURS PAR CLASSE
    // ═══════════════════════════════════════════════════════════

    @Test
    fun `custom heights per class override Ho`() {
        val tiges = listOf(
            makeTige(diamCm = 30.0),
            makeTige(diamCm = 45.0)
        )
        val customHeights = mapOf(30 to 20.0, 45 to 30.0)

        val result = calculator.compute(
            tiges = tiges,
            surfacePlacetteM2 = 1000.0,
            hoM = 25.0,
            hauteurMoyenneParClasse = customHeights
        )

        val row30 = result.classes.first { it.diamClassCm == 30 }
        val row45 = result.classes.first { it.diamClassCm == 45 }

        assertApprox(20.0, row30.hMoyenne, delta = 0.01, msg = "H class 30")
        assertApprox(30.0, row45.hMoyenne, delta = 0.01, msg = "H class 45")
    }

    @Test
    fun `missing height falls back to Ho`() {
        val tiges = listOf(makeTige(diamCm = 40.0))
        val ho = 22.0

        val result = calculator.compute(
            tiges = tiges,
            surfacePlacetteM2 = 1000.0,
            hoM = ho,
            hauteurMoyenneParClasse = emptyMap()
        )

        val row40 = result.classes.first { it.diamClassCm == 40 }
        assertApprox(ho, row40.hMoyenne, delta = 0.01, msg = "H fallback to Ho")
    }

    // ═══════════════════════════════════════════════════════════
    // VENTILATION BOIS TRITURATION / BOIS D'ŒUVRE
    // ═══════════════════════════════════════════════════════════

    @Test
    fun `pctBoisNonTritu default values are applied`() {
        val tiges = listOf(makeTige(diamCm = 25.0))

        val result = calculator.compute(
            tiges = tiges,
            surfacePlacetteM2 = 1000.0,
            hoM = 25.0,
            hauteurMoyenneParClasse = emptyMap()
        )

        val row25 = result.classes.first { it.diamClassCm == 25 }
        // Default for D=25 is 90.24%
        assertApprox(90.24, row25.pctBoisNonTritu, delta = 0.01, msg = "pctNonTritu D=25")

        // vTritu = vPerTree × (100 - 90.24)/100
        val vTrituExpected = row25.vPerTree * (100.0 - 90.24) / 100.0
        assertApprox(vTrituExpected, row25.vTrituPerTree, delta = 0.001, msg = "vTritu D=25")

        // vBO = vTotal - vTrituClass
        assertApprox(row25.vTotal - row25.vTrituClass, row25.vBoisOeuvreClass, delta = 0.001, msg = "vBO D=25")
    }

    @Test
    fun `custom pctBoisNonTritu table is used`() {
        val tiges = listOf(makeTige(diamCm = 30.0))
        val customPct = mapOf(30 to 50.0)

        val result = calculator.compute(
            tiges = tiges,
            surfacePlacetteM2 = 1000.0,
            hoM = 25.0,
            hauteurMoyenneParClasse = emptyMap(),
            pctBoisNonTrituTable = customPct
        )

        val row30 = result.classes.first { it.diamClassCm == 30 }
        assertApprox(50.0, row30.pctBoisNonTritu, delta = 0.01, msg = "custom pctNonTritu")
    }

    // ═══════════════════════════════════════════════════════════
    // MULTI-ESSENCES
    // ═══════════════════════════════════════════════════════════

    @Test
    fun `multi-essence trees are tracked in nByEssence`() {
        val tiges = listOf(
            makeTige(essenceCode = "DOUGLAS_VERT", diamCm = 35.0),
            makeTige(essenceCode = "DOUGLAS_VERT", diamCm = 35.0),
            makeTige(essenceCode = "EPICEA_COMMUN", diamCm = 35.0)
        )

        val result = calculator.compute(
            tiges = tiges,
            surfacePlacetteM2 = 1000.0,
            hoM = 25.0,
            hauteurMoyenneParClasse = emptyMap()
        )

        val row35 = result.classes.first { it.diamClassCm == 35 }
        assertEquals(2, row35.nByEssence["DOUGLAS_VERT"])
        assertEquals(1, row35.nByEssence["EPICEA_COMMUN"])
        assertEquals(3, row35.nTotal)

        // Percentages by essence
        val pctDouglas = result.totals.pctEssenceByTiges["DOUGLAS_VERT"]!!
        val pctEpicea = result.totals.pctEssenceByTiges["EPICEA_COMMUN"]!!
        assertApprox(2.0 / 3.0, pctDouglas, delta = 0.001, msg = "pctDouglas")
        assertApprox(1.0 / 3.0, pctEpicea, delta = 0.001, msg = "pctEpicea")
    }

    // ═══════════════════════════════════════════════════════════
    // CATÉGORIES DE GROSSEUR (PB, BM, GB)
    // ═══════════════════════════════════════════════════════════

    @Test
    fun `category percentages are computed on correct diameter starts`() {
        val tiges = listOf(
            makeTige(diamCm = 20.0), // Cat 1 (PB)
            makeTige(diamCm = 25.0), // Cat 1 (PB)
            makeTige(diamCm = 30.0), // Cat 2 (BM)
            makeTige(diamCm = 40.0), // Cat 2 (BM)
            makeTige(diamCm = 50.0), // Cat 3 (GB)
            makeTige(diamCm = 60.0)  // Cat 3 (GB)
        )

        val result = calculator.compute(
            tiges = tiges,
            surfacePlacetteM2 = 1000.0,
            hoM = 25.0,
            hauteurMoyenneParClasse = emptyMap()
        )

        // pctNCategory only set on the first class of each category
        val row20 = result.classes.first { it.diamClassCm == 20 }
        val row25 = result.classes.first { it.diamClassCm == 25 }
        val row30 = result.classes.first { it.diamClassCm == 30 }
        val row50 = result.classes.first { it.diamClassCm == 50 }

        assertNotNull("pctN Cat1 on D=20", row20.pctNCategory)
        assertNull("pctN should be null on D=25", row25.pctNCategory)
        assertNotNull("pctN Cat2 on D=30", row30.pctNCategory)
        assertNotNull("pctN Cat3 on D=50", row50.pctNCategory)

        // Cat1: 2/6, Cat2: 2/6, Cat3: 2/6
        assertApprox(2.0 / 6.0, row20.pctNCategory!!, delta = 0.001, msg = "pctN Cat1")
        assertApprox(2.0 / 6.0, row30.pctNCategory!!, delta = 0.001, msg = "pctN Cat2")
        assertApprox(2.0 / 6.0, row50.pctNCategory!!, delta = 0.001, msg = "pctN Cat3")
    }

    // ═══════════════════════════════════════════════════════════
    // S% (ESPACEMENT RELATIF)
    // ═══════════════════════════════════════════════════════════

    @Test
    fun `S percent formula correctness`() {
        // S% = 10746 / (Ho × √(N/ha))
        val tiges = (1..20).map { makeTige(diamCm = 35.0) }
        val surfM2 = 1000.0
        val ho = 25.0

        val result = calculator.compute(
            tiges = tiges,
            surfacePlacetteM2 = surfM2,
            hoM = ho,
            hauteurMoyenneParClasse = emptyMap()
        )

        val nHa = 20.0 / (surfM2 / 10000.0)
        val expectedSPct = 10746.0 / (ho * sqrt(nHa))
        assertApprox(expectedSPct, result.totals.sPct, delta = 0.01, msg = "S% formula")
    }

    // ═══════════════════════════════════════════════════════════
    // COHERENCE : volumes croissants avec le diamètre
    // ═══════════════════════════════════════════════════════════

    @Test
    fun `volume per tree increases with diameter class`() {
        val allClasses = listOf(20, 25, 30, 35, 40, 45, 50, 55)
        val tiges = allClasses.map { makeTige(diamCm = it.toDouble()) }

        val result = calculator.compute(
            tiges = tiges,
            surfacePlacetteM2 = 1000.0,
            hoM = 25.0,
            hauteurMoyenneParClasse = emptyMap()
        )

        var prevV = 0.0
        for (d in allClasses) {
            val row = result.classes.first { it.diamClassCm == d }
            if (row.nTotal > 0) {
                assertTrue(
                    "V/tree at D=$d (${row.vPerTree}) should be > prev ($prevV)",
                    row.vPerTree > prevV
                )
                prevV = row.vPerTree
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // G UNITAIRE
    // ═══════════════════════════════════════════════════════════

    @Test
    fun `gUnit matches pi over 4 times diameter squared`() {
        val result = calculator.compute(
            tiges = listOf(makeTige(diamCm = 40.0)),
            surfacePlacetteM2 = 1000.0,
            hoM = 25.0,
            hauteurMoyenneParClasse = emptyMap()
        )

        val row40 = result.classes.first { it.diamClassCm == 40 }
        val expected = PI / 4.0 * (40.0 / 100.0).pow(2.0)
        assertApprox(expected, row40.gUnit, delta = 1e-6, msg = "gUnit D=40")
    }

    // ═══════════════════════════════════════════════════════════
    // DM GLOBAL
    // ═══════════════════════════════════════════════════════════

    @Test
    fun `dmGlobalM is weighted mean of class diameters`() {
        val tiges = listOf(
            makeTige(diamCm = 30.0),
            makeTige(diamCm = 30.0),
            makeTige(diamCm = 50.0)
        )

        val result = calculator.compute(
            tiges = tiges,
            surfacePlacetteM2 = 1000.0,
            hoM = 25.0,
            hauteurMoyenneParClasse = emptyMap()
        )

        // dmGlobal = (0.30 × 2 + 0.50 × 1) / 3 = 1.10 / 3 ≈ 0.3667 m
        val expected = (0.30 * 2 + 0.50 * 1) / 3.0
        assertApprox(expected, result.totals.dmGlobalM, delta = 0.001, msg = "dmGlobal weighted mean")
    }

    // ═══════════════════════════════════════════════════════════
    // PER-HECTARE COHERENCE
    // ═══════════════════════════════════════════════════════════

    @Test
    fun `per-hectare values scale correctly with surface`() {
        val tiges = (1..10).map { makeTige(diamCm = 35.0) }

        val result500 = calculator.compute(
            tiges = tiges,
            surfacePlacetteM2 = 500.0, // 0.05 ha
            hoM = 25.0,
            hauteurMoyenneParClasse = emptyMap()
        )

        val result2000 = calculator.compute(
            tiges = tiges,
            surfacePlacetteM2 = 2000.0, // 0.2 ha
            hoM = 25.0,
            hauteurMoyenneParClasse = emptyMap()
        )

        // N/ha on 500m² should be 4× N/ha on 2000m²
        assertApprox(
            result500.totals.nHaTotal,
            result2000.totals.nHaTotal * 4.0,
            delta = 0.1,
            msg = "N/ha scaling"
        )

        // V/ha should also scale
        assertApprox(
            result500.totals.vTotalHa,
            result2000.totals.vTotalHa * 4.0,
            delta = 0.1,
            msg = "V/ha scaling"
        )
    }
}
