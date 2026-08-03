package com.forestry.counter.domain.calculation

import com.forestry.counter.domain.model.Tige
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.Method
import kotlin.math.ln

/**
 * Tests unitaires des indices de biodiversité (Shannon, Piélou, IBP simplifié).
 *
 * Les formules de Shannon, Piélou et IBP ne sont pas exposées comme des
 * fonctions publiques distinctes : elles sont calculées en ligne dans
 * `computeBiodiversityIndex` (MartelageModels.kt, lignes 508-569), qui est
 * une fonction *privée au niveau du fichier*. Aucun fichier source n'étant
 * modifiable, on y accède par réflexion JVM afin de tester la logique avec
 * des entrées contrôlées (perEssence et specialTrees synthétiques).
 *
 * Formules (issues du code) :
 * - Shannon : H' = -Σ(pi * ln(pi)) avec pi = n_i / nTotal
 * - Piélou  : J = H' / ln(S), J = 0 si S = 1, J = null si S = 0
 * - IBP     : score simplifié 0-10 (diversité, TGB, arbres bio, mort, dépérissant, équitabilité)
 */
class BiodiversityIndicesTest {

    // region --- Reflection vers computeBiodiversityIndex (privée) ---

    private val biodiversityMethod: Method = Class.forName(
        "com.forestry.counter.domain.calculation.MartelageModelsKt"
    ).getDeclaredMethod(
        "computeBiodiversityIndex",
        List::class.java,
        List::class.java,
        Int::class.javaPrimitiveType,
        List::class.java
    ).apply { isAccessible = true }

    /**
     * Invoque `computeBiodiversityIndex` avec des agrégats synthétiques.
     * @param perEssence répartition des tiges par essence (seul `n` compte pour Shannon)
     * @param nTotal nombre total de tiges (doit être > 0, sinon renvoie null)
     * @param tiges tiges du peuplement (sert uniquement au comptage des TGB ≥ 70 cm)
     * @param specialTrees arbres spéciaux (ARBRE_BIO, MORT, DEPERISSANT)
     */
    @Suppress("UNCHECKED_CAST")
    private fun biodiversity(
        perEssence: List<PerEssenceStats>,
        nTotal: Int,
        tiges: List<Tige> = emptyList(),
        specialTrees: List<SpecialTreeEntry> = emptyList()
    ): BiodiversityIndex? {
        return biodiversityMethod.invoke(null, tiges, specialTrees, nTotal, perEssence)
            as BiodiversityIndex?
    }

    private fun perEssence(code: String, n: Int): PerEssenceStats = PerEssenceStats(
        essenceCode = code,
        essenceName = code,
        n = n,
        nPct = 0.0,
        vTotal = 0.0,
        vPct = 0.0,
        vPerHa = 0.0,
        gTotal = 0.0,
        gPct = 0.0,
        gPerHa = 0.0,
        dm = null,
        dg = null,
        meanPricePerM3 = null,
        revenueTotal = null,
        revenuePerHa = null
    )

    private fun tige(diamCm: Double, id: String = "t$diamCm-${System.nanoTime()}"): Tige = Tige(
        id = id,
        parcelleId = "P",
        placetteId = null,
        essenceCode = "QUPE",
        diamCm = diamCm,
        hauteurM = null,
        gpsWkt = null,
        precisionM = null,
        altitudeM = null,
        note = null,
        produit = null,
        fCoef = null,
        valueEur = null
    )

    private fun specialTree(categorie: String, count: Int): SpecialTreeEntry =
        SpecialTreeEntry(categorie = categorie, count = count)

    // endregion

    // =====================================================================
    // 1. Shannon avec 1 essence — H' = 0 (monospécifique)
    // =====================================================================

    @Test
    fun `should_return_zero_shannon_when_single_species`() {
        val result = biodiversity(perEssence = listOf(perEssence("A", 10)), nTotal = 10)

        assertNotNull(result)
        assertEquals(0.0, result!!.shannonH, 1e-9)
    }

    // =====================================================================
    // 2. Shannon avec 2 essences équiréparties — H' = ln(2)
    // =====================================================================

    @Test
    fun `should_return_ln2_shannon_when_two_even_species`() {
        val result = biodiversity(
            perEssence = listOf(perEssence("A", 5), perEssence("B", 5)),
            nTotal = 10
        )

        assertNotNull(result)
        assertEquals(ln(2.0), result!!.shannonH, 1e-9)
    }

    // =====================================================================
    // 3. Shannon avec 3 essences équiréparties — H' = ln(3)
    // =====================================================================

    @Test
    fun `should_return_ln3_shannon_when_three_even_species`() {
        val result = biodiversity(
            perEssence = listOf(perEssence("A", 10), perEssence("B", 10), perEssence("C", 10)),
            nTotal = 30
        )

        assertNotNull(result)
        assertEquals(ln(3.0), result!!.shannonH, 1e-9)
    }

    // =====================================================================
    // 4. Piélou avec 1 essence — J = 0 (convention du code : S=1 → 0.0)
    // =====================================================================

    @Test
    fun `should_return_zero_pielou_when_single_species`() {
        val result = biodiversity(perEssence = listOf(perEssence("A", 10)), nTotal = 10)

        assertNotNull(result)
        assertEquals(0.0, result!!.pielou!!, 1e-9)
    }

    // =====================================================================
    // 5. Piélou avec 2 essences équiréparties — J = 1 (parfaite équirépartition)
    // =====================================================================

    @Test
    fun `should_return_one_pielou_when_two_even_species`() {
        val result = biodiversity(
            perEssence = listOf(perEssence("A", 5), perEssence("B", 5)),
            nTotal = 10
        )

        assertNotNull(result)
        assertEquals(1.0, result!!.pielou!!, 1e-9)
    }

    // =====================================================================
    // 6. Piélou avec 2 essences déséquilibrées — J < 1
    // =====================================================================

    @Test
    fun `should_return_pielou_below_one_when_two_unbalanced_species`() {
        val result = biodiversity(
            perEssence = listOf(perEssence("A", 9), perEssence("B", 1)),
            nTotal = 10
        )

        assertNotNull(result)
        val pielou = result!!.pielou!!
        assertTrue("Piélou doit être < 1 : $pielou", pielou < 1.0)
        assertTrue("Piélou doit être > 0 : $pielou", pielou > 0.0)
    }

    // =====================================================================
    // 7. IBP simplifié avec valeurs connues — vérifier le score
    // =====================================================================

    @Test
    fun `should_compute_expected_ibp_score_when_known_inputs`() {
        // 3 essences équiréparties → diversite_3+ (+1), Piélou = 1.0 → equitabilite (+1).
        // 1 TGB (≥ 70 cm) → tgb_1+ (+1).
        // 1 arbre bio, 1 mort, 1 dépérissant → +1 +1 +1.
        // Total attendu : 1 + 1 + 1 + 1 + 1 + 1 = 6.
        val result = biodiversity(
            perEssence = listOf(
                perEssence("A", 10),
                perEssence("B", 10),
                perEssence("C", 10)
            ),
            nTotal = 30,
            tiges = listOf(tige(75.0)),
            specialTrees = listOf(
                specialTree("ARBRE_BIO", 1),
                specialTree("MORT", 1),
                specialTree("DEPERISSANT", 1)
            )
        )

        assertNotNull(result)
        val ibp = result!!
        assertEquals(6, ibp.ibpScore)
        assertEquals(10, ibp.ibpMax)
        assertEquals(3, ibp.speciesCount)
        assertEquals(1, ibp.tgbCount)
        assertEquals(1, ibp.bioTreeCount)
        assertEquals(1, ibp.deadTreeCount)
        assertEquals(1, ibp.dyingTreeCount)
        assertTrue(
            "Détails IBP doivent contenir diversite_3+ : ${ibp.ibpDetails}",
            ibp.ibpDetails.contains("diversite_3+")
        )
        assertTrue(
            "Détails IBP doivent contenir equitabilite : ${ibp.ibpDetails}",
            ibp.ibpDetails.contains("equitabilite")
        )
    }

    // =====================================================================
    // 8. Shannon avec liste vide — comportement (null, nTotal = 0)
    // =====================================================================

    @Test
    fun `should_return_null_when_empty_stand`() {
        // nTotal == 0 → computeBiodiversityIndex renvoie null (aucune exception).
        val result = biodiversity(perEssence = emptyList(), nTotal = 0)

        assertNull("Aucun arbre → biodiversité null (pas d'exception)", result)
    }

    // =====================================================================
    // 9. Shannon avec proportions ne sommant pas à 1 — comportement
    // =====================================================================

    @Test
    fun `should_compute_without_exception_when_proportions_do_not_sum_to_one`() {
        // La fonction ne valide pas que Σ(n_i) == nTotal : elle calcule
        // pi = n_i / nTotal tel quel. Ici n_i somment à 10 pour nTotal = 20,
        // donc pi = 0.25 chacun → H' = ln(2) sans normalisation ni exception.
        val result = biodiversity(
            perEssence = listOf(perEssence("A", 5), perEssence("B", 5)),
            nTotal = 20
        )

        assertNotNull(result)
        val shannon = result!!.shannonH
        assertTrue("Shannon doit être fini : $shannon", !shannon.isNaN() && !shannon.isInfinite())
        // Sans normalisation : H' = -(0.25*ln0.25 + 0.25*ln0.25) = ln(2).
        assertEquals(ln(2.0), shannon, 1e-9)
    }
}
