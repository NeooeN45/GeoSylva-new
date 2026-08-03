package com.forestry.counter.domain.calculation

import com.forestry.counter.domain.model.ParameterItem
import com.forestry.counter.domain.repository.ParameterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.pow

/**
 * Tests unitaires du modèle de croissance de Richards (1959) :
 * D(t) = A / (1 + exp(-k*(t - t0)))^b
 *
 * `richardsGrowthModel` est publique et testée directement.
 * `getRichardsParameters` est privée : on y accède par réflexion JVM pour
 * vérifier les paramètres par essence (tests 7 et 8). Aucun fichier source
 * n'est modifié.
 */
class RichardsGrowthModelTest {

    // region --- Test infrastructure ---

    private class FakeParameterRepository : ParameterRepository {
        override fun getAllParameters(): Flow<List<ParameterItem>> = flow { emit(emptyList()) }
        override fun getParameter(key: String): Flow<ParameterItem?> = flow { emit(null) }
        override suspend fun setParameter(item: ParameterItem) {}
        override suspend fun setParameters(items: List<ParameterItem>) {}
        override suspend fun deleteParameter(key: String) {}
    }

    private fun buildCalculator(): ExpertForestryCalculator {
        val fakeRepo = FakeParameterRepository()
        val base = ForestryCalculator(fakeRepo)
        return ExpertForestryCalculator(base, fakeRepo)
    }

    /**
     * Invoque `getRichardsParameters` (privée) par réflexion.
     * @return les paramètres (A, k, t0, b) pour l'essence et la classe de station.
     */
    private fun richardsParameters(
        calc: ExpertForestryCalculator,
        essenceCode: String,
        classeStation: Int = 3
    ): RichardsParameters {
        val method = ExpertForestryCalculator::class.java
            .getDeclaredMethod("getRichardsParameters", String::class.java, Int::class.java)
        method.isAccessible = true
        return method.invoke(calc, essenceCode, classeStation) as RichardsParameters
    }

    // endregion

    // =====================================================================
    // 1. Richards QUPE (Chêne pédonculé) — monotonie croissante
    // =====================================================================

    @Test
    fun `should_be_monotonically_increasing_when_qupe_growth`() {
        val calc = buildCalculator()

        var previous = -1.0
        for (age in 10..200 step 10) {
            val diameter = calc.richardsGrowthModel("QUPE", age, classeStation = 3)
            assertTrue(
                "D($age)=$diameter doit excéder D précédent=$previous",
                diameter > previous
            )
            previous = diameter
        }
    }

    // =====================================================================
    // 2. Richards FASY (Hêtre) — monotonie croissante
    // =====================================================================

    @Test
    fun `should_be_monotonically_increasing_when_fasy_growth`() {
        val calc = buildCalculator()

        var previous = -1.0
        for (age in 10..200 step 10) {
            val diameter = calc.richardsGrowthModel("FASY", age, classeStation = 3)
            assertTrue(
                "D($age)=$diameter doit excéder D précédent=$previous",
                diameter > previous
            )
            previous = diameter
        }
    }

    // =====================================================================
    // 3. Richards ABAL (Sapin pectiné) — monotonie croissante
    // =====================================================================

    @Test
    fun `should_be_monotonically_increasing_when_abal_growth`() {
        val calc = buildCalculator()

        var previous = -1.0
        for (age in 10..200 step 10) {
            val diameter = calc.richardsGrowthModel("ABAL", age, classeStation = 3)
            assertTrue(
                "D($age)=$diameter doit excéder D précédent=$previous",
                diameter > previous
            )
            previous = diameter
        }
    }

    // =====================================================================
    // 4. Richards avec repli générique — ne crash pas
    // =====================================================================

    @Test
    fun `should_not_crash_when_unknown_essence_fallback`() {
        val calc = buildCalculator()

        val diameter = calc.richardsGrowthModel("UNKNOWN_SPECIES", 80, classeStation = 3)

        assertTrue("D doit être fini et positif (repli générique)", diameter > 0.0)
        assertTrue("D doit être fini", !diameter.isNaN() && !diameter.isInfinite())
    }

    // =====================================================================
    // 5. Richards à t=0 — valeur initiale
    // =====================================================================

    @Test
    fun `should_return_small_positive_initial_value_when_t_zero`() {
        val calc = buildCalculator()
        // D(0) = A / (1 + exp(k*t0))^b : le modèle de Richards ne vaut pas
        // exactement 0 à t=0 (asymptote inférieure implicite), mais une petite
        // valeur positive strictement inférieure à l'asymptote A.
        val d0 = calc.richardsGrowthModel("QUPE", 0, classeStation = 3)

        assertTrue("D(0) doit être positif : $d0", d0 > 0.0)
        assertTrue("D(0) doit être fini", !d0.isNaN() && !d0.isInfinite())
        // Asymptote A pour QUPE station 3 = 80 (facteur station 1.0).
        assertTrue("D(0) doit être inférieur à A", d0 < 80.0)
        // La valeur initiale doit rester petite (< 10 % de l'asymptote).
        assertTrue("D(0) doit être petit (< 8 cm) : $d0", d0 < 8.0)
    }

    // =====================================================================
    // 6. Richards à t très grand — convergence vers A (asymptote)
    // =====================================================================

    @Test
    fun `should_converge_to_asymptote_A_when_t_very_large`() {
        val calc = buildCalculator()
        // Asymptote A pour QUPE station 3 = 80 (facteur station 1.0).
        val expectedA = 80.0

        val dLarge = calc.richardsGrowthModel("QUPE", 10_000, classeStation = 3)

        assertEquals(
            "D(t→∞) doit converger vers A=$expectedA, obtenu=$dLarge",
            expectedA,
            dLarge,
            1e-6
        )
    }

    // =====================================================================
    // 7. getRichardsParameters pour chaque essence supportée — paramètres non nuls
    // =====================================================================

    @Test
    fun `should_return_non_zero_parameters_when_qupe`() {
        val calc = buildCalculator()
        val params = richardsParameters(calc, "QUPE", classeStation = 3)

        assertValidRichardsParameters(params)
        // QUPE : A=80, k=0.045, t0=25, b=2.2 (avant ajustement station).
        assertEquals(80.0, params.A, 1e-9)
        assertEquals(0.045, params.k, 1e-9)
        assertEquals(25.0, params.t0, 1e-9)
        assertEquals(2.2, params.b, 1e-9)
    }

    @Test
    fun `should_return_non_zero_parameters_when_fasy`() {
        val calc = buildCalculator()
        val params = richardsParameters(calc, "FASY", classeStation = 3)

        assertValidRichardsParameters(params)
        // FASY : A=60, k=0.055, t0=20, b=2.5.
        assertEquals(60.0, params.A, 1e-9)
        assertEquals(0.055, params.k, 1e-9)
        assertEquals(20.0, params.t0, 1e-9)
        assertEquals(2.5, params.b, 1e-9)
    }

    @Test
    fun `should_return_non_zero_parameters_when_abal`() {
        val calc = buildCalculator()
        val params = richardsParameters(calc, "ABAL", classeStation = 3)

        assertValidRichardsParameters(params)
        // ABAL : A=90, k=0.040, t0=30, b=2.0.
        assertEquals(90.0, params.A, 1e-9)
        assertEquals(0.040, params.k, 1e-9)
        assertEquals(30.0, params.t0, 1e-9)
        assertEquals(2.0, params.b, 1e-9)
    }

    // =====================================================================
    // 8. getRichardsParameters pour essence non supportée — repli générique
    // =====================================================================

    @Test
    fun `should_return_generic_fallback_parameters_when_unsupported_essence`() {
        val calc = buildCalculator()
        val params = richardsParameters(calc, "UNKNOWN_SPECIES", classeStation = 3)

        assertValidRichardsParameters(params)
        // Repli générique : A=70, k=0.050, t0=22, b=2.3.
        assertEquals(70.0, params.A, 1e-9)
        assertEquals(0.050, params.k, 1e-9)
        assertEquals(22.0, params.t0, 1e-9)
        assertEquals(2.3, params.b, 1e-9)
    }

    // =====================================================================
    // Cohérence : station meilleure → asymptote et taux plus élevés
    // =====================================================================

    @Test
    fun `should_scale_A_and_k_when_better_station_class`() {
        val calc = buildCalculator()
        val poor = richardsParameters(calc, "QUPE", classeStation = 1)
        val good = richardsParameters(calc, "QUPE", classeStation = 5)

        // Facteur station 1 → 0.6, station 5 → 1.4.
        assertEquals(80.0 * 0.6, poor.A, 1e-9)
        assertEquals(0.045 * 0.6, poor.k, 1e-9)
        assertEquals(80.0 * 1.4, good.A, 1e-9)
        assertEquals(0.045 * 1.4, good.k, 1e-9)
        // t0 et b ne dépendent pas de la station.
        assertEquals(poor.t0, good.t0, 1e-9)
        assertEquals(poor.b, good.b, 1e-9)
    }

    // region --- Helpers ---

    private fun assertValidRichardsParameters(params: RichardsParameters) {
        assertTrue("A doit être positif : ${params.A}", params.A > 0.0)
        assertTrue("k doit être positif : ${params.k}", params.k > 0.0)
        assertTrue("t0 doit être positif : ${params.t0}", params.t0 > 0.0)
        assertTrue("b doit être positif : ${params.b}", params.b > 0.0)
    }

    // endregion
}
