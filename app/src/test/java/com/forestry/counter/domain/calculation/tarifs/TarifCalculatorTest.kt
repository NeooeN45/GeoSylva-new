package com.forestry.counter.domain.calculation.tarifs

import org.junit.Assert.*
import org.junit.Test
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.pow

class TarifCalculatorTest {

    private fun assertApprox(expected: Double, actual: Double?, delta: Double = 1e-6, msg: String = "") {
        assertNotNull("$msg — expected $expected but got null", actual)
        val diff = abs(expected - actual!!)
        assertTrue("$msg — expected ≈$expected but was $actual (diff=$diff > delta=$delta)", diff <= delta)
    }

    // ═══════════════════════════════════════════════════════════
    // SCHAEFFER 1 ENTRÉE
    // V = a + b × C²   (C = π × D/100, en m)
    // ═══════════════════════════════════════════════════════════

    @Test
    fun `Schaeffer 1E tarif 8 — Hetre D=35`() {
        val v = TarifCalculator.computeVolume(
            method = TarifMethod.SCHAEFFER_1E,
            essenceCode = "HETRE_COMMUN",
            diamCm = 35.0,
            hauteurM = null,
            tarifNumero = 8
        )
        // Manual: C = π × 0.35 = 1.0996 m, C² = 1.2091
        // V = -0.0802 + 1.5381 × 1.2091 = -0.0802 + 1.8596 = 1.7794
        val cM = PI * 35.0 / 100.0
        val expected = -0.0802 + 1.5381 * cM * cM
        assertApprox(expected, v, delta = 0.001, msg = "Schaeffer 1E n°8 D=35")
    }

    @Test
    fun `Schaeffer 1E — all 16 tariffs produce increasing volumes for same diameter`() {
        val results = (1..16).map { n ->
            TarifCalculator.computeVolume(
                method = TarifMethod.SCHAEFFER_1E,
                essenceCode = "HETRE_COMMUN",
                diamCm = 40.0,
                hauteurM = null,
                tarifNumero = n
            )!!
        }
        // Chaque tarif doit donner un volume > au précédent (D fixé)
        for (i in 1 until results.size) {
            assertTrue(
                "Tarif ${i + 1} (${results[i]}) should be > tarif $i (${results[i - 1]})",
                results[i] > results[i - 1]
            )
        }
    }

    @Test
    fun `Schaeffer 1E — does not require height`() {
        assertFalse(TarifCalculator.requiresHeight(TarifMethod.SCHAEFFER_1E))
    }

    // ═══════════════════════════════════════════════════════════
    // SCHAEFFER 2 ENTRÉES
    // V = a + b × C² × H
    // ═══════════════════════════════════════════════════════════

    @Test
    fun `Schaeffer 2E tarif 4 — Chene sessile D=40 H=22`() {
        val v = TarifCalculator.computeVolume(
            method = TarifMethod.SCHAEFFER_2E,
            essenceCode = "CH_SESSILE",
            diamCm = 40.0,
            hauteurM = 22.0,
            tarifNumero = 4
        )
        val cM = PI * 40.0 / 100.0
        val expected = -0.0082 + 0.03498 * cM * cM * 22.0
        assertApprox(expected, v, delta = 0.001, msg = "Schaeffer 2E n°4 D=40 H=22")
    }

    @Test
    fun `Schaeffer 2E — returns null without height`() {
        val v = TarifCalculator.computeVolume(
            method = TarifMethod.SCHAEFFER_2E,
            essenceCode = "CH_SESSILE",
            diamCm = 40.0,
            hauteurM = null,
            tarifNumero = 4
        )
        assertNull(v)
    }

    @Test
    fun `Schaeffer 2E — requires height`() {
        assertTrue(TarifCalculator.requiresHeight(TarifMethod.SCHAEFFER_2E))
    }

    // ═══════════════════════════════════════════════════════════
    // ALGAN
    // V = a × D^b × H^c   (D cm, H m, V m³)
    // ═══════════════════════════════════════════════════════════

    @Test
    fun `Algan — Hetre commun D=35 H=22`() {
        val v = TarifCalculator.computeVolume(
            method = TarifMethod.ALGAN,
            essenceCode = "HETRE_COMMUN",
            diamCm = 35.0,
            hauteurM = 22.0
        )
        // Coefficients Algan Hêtre : a=0.0000362, b=2.158, c=0.860
        val expected = 0.0000362 * 35.0.pow(2.158) * 22.0.pow(0.860)
        assertApprox(expected, v, delta = 0.001, msg = "Algan Hêtre D=35 H=22")
    }

    @Test
    fun `Algan — Douglas vert D=45 H=28`() {
        val v = TarifCalculator.computeVolume(
            method = TarifMethod.ALGAN,
            essenceCode = "DOUGLAS_VERT",
            diamCm = 45.0,
            hauteurM = 28.0
        )
        val expected = 0.0000298 * 45.0.pow(2.262) * 28.0.pow(0.795)
        assertApprox(expected, v, delta = 0.001, msg = "Algan Douglas D=45 H=28")
    }

    @Test
    fun `Algan — Pin sylvestre D=30 H=18`() {
        val v = TarifCalculator.computeVolume(
            method = TarifMethod.ALGAN,
            essenceCode = "PIN_SYLVESTRE",
            diamCm = 30.0,
            hauteurM = 18.0
        )
        val expected = 0.0000318 * 30.0.pow(2.218) * 18.0.pow(0.815)
        assertApprox(expected, v, delta = 0.001, msg = "Algan Pin sylvestre D=30 H=18")
    }

    @Test
    fun `Algan — alias HETRE resolves to HETRE_COMMUN`() {
        val v1 = TarifCalculator.computeVolume(TarifMethod.ALGAN, "HETRE", 35.0, 22.0)
        val v2 = TarifCalculator.computeVolume(TarifMethod.ALGAN, "HETRE_COMMUN", 35.0, 22.0)
        assertNotNull(v1)
        assertNotNull(v2)
        assertApprox(v1!!, v2, delta = 1e-12, msg = "HETRE alias")
    }

    @Test
    fun `Algan — alias DOUGLAS resolves to DOUGLAS_VERT`() {
        val v1 = TarifCalculator.computeVolume(TarifMethod.ALGAN, "DOUGLAS", 40.0, 25.0)
        val v2 = TarifCalculator.computeVolume(TarifMethod.ALGAN, "DOUGLAS_VERT", 40.0, 25.0)
        assertNotNull(v1)
        assertNotNull(v2)
        assertApprox(v1!!, v2, delta = 1e-12, msg = "DOUGLAS alias")
    }

    @Test
    fun `Algan — returns null without height`() {
        val v = TarifCalculator.computeVolume(TarifMethod.ALGAN, "HETRE_COMMUN", 35.0, null)
        assertNull(v)
    }

    @Test
    fun `Algan — returns null for zero diameter`() {
        val v = TarifCalculator.computeVolume(TarifMethod.ALGAN, "HETRE_COMMUN", 0.0, 22.0)
        assertNull(v)
    }

    @Test
    fun `Algan — unknown essence falls back to Hetre coefficients`() {
        val v = TarifCalculator.computeVolume(TarifMethod.ALGAN, "ESSENCE_INCONNUE", 35.0, 22.0)
        val expected = 0.0000362 * 35.0.pow(2.158) * 22.0.pow(0.860) // Hêtre fallback
        assertApprox(expected, v, delta = 0.001, msg = "Algan unknown essence fallback")
    }

    @Test
    fun `Algan — all canonical essences have coefficients`() {
        val essences = listOf(
            "CH_SESSILE", "CH_PEDONCULE", "HETRE_COMMUN", "CHARME", "CHATAIGNIER",
            "FRENE_ELEVE", "ERABLE_SYC", "ERABLE_PLANE", "ERABLE_CHAMP",
            "BOUL_VERRUQ", "BOUL_PUBESC", "AULNE_GLUT", "AULNE_BLANC",
            "TIL_PET_FEUIL", "TIL_GR_FEUIL", "ORME_CHAMP", "ORME_LISSE", "ORME_MONT",
            "ROBINIER", "NOYER_COMMUN", "NOYER_NOIR", "CERISIER_MERIS", "CORMIER",
            "PIN_SYLVESTRE", "PIN_MARITIME", "PIN_NOIR_AUTR", "PIN_LARICIO", "PIN_WEYMOUTH",
            "EPICEA_COMMUN", "SAPIN_PECTINE", "DOUGLAS_VERT", "MEL_EUROPE", "MEL_HYBRIDE"
        )
        for (ess in essences) {
            val coefs = TarifCalculator.alganCoefsFor(ess)
            assertNotNull("Missing Algan coefficients for $ess", coefs)
            val v = TarifCalculator.computeVolume(TarifMethod.ALGAN, ess, 35.0, 22.0)
            assertNotNull("Volume null for $ess", v)
            assertTrue("Volume should be positive for $ess (got $v)", v!! > 0.0)
        }
    }

    // ═══════════════════════════════════════════════════════════
    // IFN RAPIDE (1 entrée)
    // V = a₀ + a₁×D + a₂×D²  (V dm³ → m³)
    // ═══════════════════════════════════════════════════════════

    @Test
    fun `IFN Rapide — tarif 12 D=35`() {
        val v = TarifCalculator.computeVolume(
            method = TarifMethod.IFN_RAPIDE,
            essenceCode = "HETRE_COMMUN",
            diamCm = 35.0,
            hauteurM = null,
            tarifNumero = 12
        )
        // V(dm³) = -27.05 + 2.518 × 35 + 0.3424 × 35²
        val vDm3 = -27.05 + 2.518 * 35.0 + 0.3424 * 35.0 * 35.0
        assertApprox(vDm3 / 1000.0, v, delta = 0.001, msg = "IFN rapide n°12 D=35")
    }

    @Test
    fun `IFN Rapide — auto-selects tarif for Douglas (n=20)`() {
        val v = TarifCalculator.computeVolume(
            method = TarifMethod.IFN_RAPIDE,
            essenceCode = "DOUGLAS_VERT",
            diamCm = 40.0,
            hauteurM = null,
            tarifNumero = null // should auto-select n°20
        )
        assertNotNull(v)
        assertTrue("Douglas IFN rapide volume should be positive", v!! > 0.0)

        // Verify it's tarif 20
        val vExplicit = TarifCalculator.computeVolume(
            method = TarifMethod.IFN_RAPIDE,
            essenceCode = "DOUGLAS_VERT",
            diamCm = 40.0,
            hauteurM = null,
            tarifNumero = 20
        )
        assertApprox(v, vExplicit, delta = 1e-12, msg = "Douglas auto n°20")
    }

    @Test
    fun `IFN Rapide — 36 tariffs produce increasing volumes`() {
        val results = (1..36).map { n ->
            TarifCalculator.computeVolume(
                method = TarifMethod.IFN_RAPIDE,
                essenceCode = "HETRE_COMMUN",
                diamCm = 40.0,
                hauteurM = null,
                tarifNumero = n
            )!!
        }
        for (i in 1 until results.size) {
            assertTrue("IFN tarif ${i + 1} should > tarif $i", results[i] > results[i - 1])
        }
    }

    // ═══════════════════════════════════════════════════════════
    // IFN LENT (2 entrées)
    // V = a₀ + a₁×D² + a₂×D²×H  (V dm³ → m³)
    // ═══════════════════════════════════════════════════════════

    @Test
    fun `IFN Lent — tarif 5 D=35 H=22`() {
        val v = TarifCalculator.computeVolume(
            method = TarifMethod.IFN_LENT,
            essenceCode = "HETRE_COMMUN",
            diamCm = 35.0,
            hauteurM = 22.0,
            tarifNumero = 5
        )
        val d2 = 35.0 * 35.0
        val vDm3 = -10.10 + 0.0360 * d2 + 0.05520 * d2 * 22.0
        assertApprox(vDm3 / 1000.0, v, delta = 0.001, msg = "IFN lent n°5 D=35 H=22")
    }

    @Test
    fun `IFN Lent — returns null without height`() {
        val v = TarifCalculator.computeVolume(
            method = TarifMethod.IFN_LENT,
            essenceCode = "HETRE_COMMUN",
            diamCm = 35.0,
            hauteurM = null,
            tarifNumero = 5
        )
        assertNull(v)
    }

    // ═══════════════════════════════════════════════════════════
    // COEFFICIENT DE FORME
    // V = G × H × f = π/4 × (D/100)² × H × f
    // ═══════════════════════════════════════════════════════════

    @Test
    fun `Coef forme — Hetre D=35 H=22 default f`() {
        val v = TarifCalculator.computeVolume(
            method = TarifMethod.COEF_FORME,
            essenceCode = "HETRE_COMMUN",
            diamCm = 35.0,
            hauteurM = 22.0
        )
        val f = TarifCalculator.defaultCoefForme("HETRE_COMMUN") // 0.45
        val g = PI / 4.0 * (35.0 / 100.0).pow(2.0)
        val expected = g * 22.0 * f
        assertApprox(expected, v, delta = 0.001, msg = "Coef forme Hêtre default")
    }

    @Test
    fun `Coef forme — custom override`() {
        val v = TarifCalculator.computeVolume(
            method = TarifMethod.COEF_FORME,
            essenceCode = "HETRE_COMMUN",
            diamCm = 35.0,
            hauteurM = 22.0,
            coefFormOverride = 0.50
        )
        val g = PI / 4.0 * (35.0 / 100.0).pow(2.0)
        val expected = g * 22.0 * 0.50
        assertApprox(expected, v, delta = 0.001, msg = "Coef forme custom 0.50")
    }

    @Test
    fun `FGH — equals coef forme for same data`() {
        val d = 35.0
        val h = 22.0
        val ess = "HETRE_COMMUN"

        val vFgh = TarifCalculator.computeVolume(
            method = TarifMethod.FGH,
            essenceCode = ess,
            diamCm = d,
            hauteurM = h
        )
        val vCoef = TarifCalculator.computeVolume(
            method = TarifMethod.COEF_FORME,
            essenceCode = ess,
            diamCm = d,
            hauteurM = h
        )
        assertApprox(vCoef ?: 0.0, vFgh, delta = 1e-12, msg = "FGH should match coef forme")
    }

    @Test
    fun `FGH — returns null without height`() {
        val v = TarifCalculator.computeVolume(
            method = TarifMethod.FGH,
            essenceCode = "HETRE_COMMUN",
            diamCm = 35.0,
            hauteurM = null
        )
        assertNull(v)
    }

    @Test
    fun `defaultCoefForme returns sensible values`() {
        val fHetre = TarifCalculator.defaultCoefForme("HETRE_COMMUN")
        val fDouglas = TarifCalculator.defaultCoefForme("DOUGLAS_VERT")
        val fChene = TarifCalculator.defaultCoefForme("CH_SESSILE")
        val fWildcard = TarifCalculator.defaultCoefForme("UNKNOWN_SPECIES")

        assertTrue("Hêtre f should be 0.40–0.50", fHetre in 0.40..0.50)
        assertTrue("Douglas f should be 0.35–0.45", fDouglas in 0.35..0.45)
        assertTrue("Chêne f should be 0.40–0.50", fChene in 0.40..0.50)
        assertTrue("Wildcard f should be 0.40–0.50", fWildcard in 0.40..0.50)
    }

    // ═══════════════════════════════════════════════════════════
    // CROSS-METHOD : Vérification de cohérence entre méthodes
    // Les volumes doivent être du même ordre de grandeur.
    // ═══════════════════════════════════════════════════════════

    @Test
    fun `Cross-method — Hetre D=35 H=22 all methods within 50 pct of each other`() {
        val d = 35.0
        val h = 22.0
        val ess = "HETRE_COMMUN"

        val vAlgan = TarifCalculator.computeVolume(TarifMethod.ALGAN, ess, d, h)!!
        val vSchaeffer2E = TarifCalculator.computeVolume(TarifMethod.SCHAEFFER_2E, ess, d, h, tarifNumero = 5)!!
        val vIfnLent = TarifCalculator.computeVolume(TarifMethod.IFN_LENT, ess, d, h, tarifNumero = 5)!!
        val vCoefForme = TarifCalculator.computeVolume(TarifMethod.COEF_FORME, ess, d, h)!!

        val all = listOf(vAlgan, vSchaeffer2E, vIfnLent, vCoefForme)
        val mean = all.average()

        for (v in all) {
            val pctDiff = abs(v - mean) / mean * 100.0
            assertTrue(
                "Volume $v deviates ${pctDiff.toInt()}% from mean $mean (should be < 50%)",
                pctDiff < 50.0
            )
        }
    }

    @Test
    fun `Cross-method — volumes increase with diameter (D=20,30,40,50)`() {
        val h = 22.0
        val methods = listOf(TarifMethod.ALGAN, TarifMethod.COEF_FORME)
        val diameters = listOf(20.0, 30.0, 40.0, 50.0)

        for (m in methods) {
            var prev = 0.0
            for (d in diameters) {
                val v = TarifCalculator.computeVolume(m, "HETRE_COMMUN", d, h)!!
                assertTrue("${m.code} D=$d: $v should be > $prev", v > prev)
                prev = v
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // DECOUPE / VENTILATION PAR PRODUIT
    // ═══════════════════════════════════════════════════════════

    @Test
    fun `DecoupeCalculator — gros feuillu ventile en BO+BI+BCh`() {
        val result = DecoupeCalculator.ventilerParProduit(
            volumeTotal = 1.0,
            essenceCode = "CH_SESSILE",
            categorie = "Feuillu",
            diamCm = 45.0
        )
        assertTrue("Should contain BO", result.containsKey("BO"))
        assertTrue("Should contain BI", result.containsKey("BI"))
        assertTrue("Should contain BCh", result.containsKey("BCh"))
        assertApprox(1.0, result.values.sum(), delta = 0.001, msg = "Total should sum to 1.0")
        assertTrue("BO should be majority", result["BO"]!! > result["BI"]!!)
    }

    @Test
    fun `DecoupeCalculator — petit résineux ventile en BI+PATE+BE`() {
        val result = DecoupeCalculator.ventilerParProduit(
            volumeTotal = 0.5,
            essenceCode = "EPICEA_COMMUN",
            categorie = "Résineux",
            diamCm = 15.0
        )
        assertTrue("Should contain BI", result.containsKey("BI"))
        assertApprox(0.5, result.values.sum(), delta = 0.001, msg = "Total should sum to 0.5")
    }

    @Test
    fun `DecoupeCalculator — Douglas special rules apply`() {
        val result = DecoupeCalculator.ventilerParProduit(
            volumeTotal = 2.0,
            essenceCode = "DOUGLAS_VERT",
            categorie = "Résineux",
            diamCm = 30.0
        )
        assertTrue("Douglas D=30 should have BO (special rule)", result.containsKey("BO"))
        assertTrue("BO should be >= 70% for Douglas", result["BO"]!! >= 2.0 * 0.70)
    }

    @Test
    fun `DecoupeCalculator — zero volume returns empty`() {
        val result = DecoupeCalculator.ventilerParProduit(0.0, "CH_SESSILE", "Feuillu", 40.0)
        assertTrue(result.isEmpty())
    }

    // ═══════════════════════════════════════════════════════════
    // UTILITAIRES
    // ═══════════════════════════════════════════════════════════

    @Test
    fun `availableTarifNumbers returns correct ranges`() {
        assertEquals(1..16, TarifCalculator.availableTarifNumbers(TarifMethod.SCHAEFFER_1E))
        assertEquals(1..8, TarifCalculator.availableTarifNumbers(TarifMethod.SCHAEFFER_2E))
        assertEquals(1..36, TarifCalculator.availableTarifNumbers(TarifMethod.IFN_RAPIDE))
        assertEquals(1..8, TarifCalculator.availableTarifNumbers(TarifMethod.IFN_LENT))
        assertNull(TarifCalculator.availableTarifNumbers(TarifMethod.ALGAN))
        assertNull(TarifCalculator.availableTarifNumbers(TarifMethod.FGH))
        assertNull(TarifCalculator.availableTarifNumbers(TarifMethod.COEF_FORME))
    }

    @Test
    fun `TarifMethod fromCode round-trips`() {
        for (m in TarifMethod.entries) {
            assertEquals(m, TarifMethod.fromCode(m.code))
        }
        assertNull(TarifMethod.fromCode("INEXISTANT"))
    }

    @Test
    fun `recommendedTarifNumero is not null for major essences`() {
        val essences = listOf("HETRE_COMMUN", "CH_SESSILE", "DOUGLAS_VERT", "EPICEA_COMMUN", "PIN_SYLVESTRE")
        for (ess in essences) {
            val n = TarifCalculator.recommendedTarifNumero(TarifMethod.IFN_RAPIDE, ess)
            assertNotNull("IFN rapide recommended n° for $ess should not be null", n)
        }
    }

    // ═══════════════════════════════════════════════════════════
    // ENRICHED ESSENCES — New canonical species coverage
    // ═══════════════════════════════════════════════════════════

    @Test
    fun `Algan — all enriched feuillus have coefficients and positive volume`() {
        val essences = listOf(
            "CH_PUBESCENT", "CH_ROUGE", "CH_VERT", "CH_LIEGE", "CH_TAUZIN", "CH_KERMES",
            "FRENE_OXYPHYLLE", "FRENE_FLEURS", "ERABLE_MONTPELLIER", "ERABLE_OBIER",
            "AULNE_CORSE", "SAULE_FRAGILE", "PEUPLIER_TREMB", "PLATANE", "MICOCOULIER",
            "MARRONNIER", "TULIPIER", "OLIVIER", "ARBRE_JUDEE", "FIGUIER", "MURIER_BLANC",
            "SORBIER_DOMESTIQUE", "CORNOUILLER_MALE", "CORNOUILLER_SANG", "SUREAU_NOIR",
            "AUBEPINE_MONOGYNE", "PRUNELLIER", "BUIS", "TROENE", "VIORNE_LANTANE",
            "VIORNE_OBIER", "GENETS_SCORPION", "EUCALYPTUS_GUNNII", "EUCALYPTUS_GLOBULUS"
        )
        for (ess in essences) {
            val coefs = TarifCalculator.alganCoefsFor(ess)
            assertNotNull("Missing Algan coefficients for $ess", coefs)
            val v = TarifCalculator.computeVolume(TarifMethod.ALGAN, ess, 30.0, 18.0)
            assertNotNull("Volume null for $ess", v)
            assertTrue("Volume should be positive for $ess (got $v)", v!! > 0.0)
        }
    }

    @Test
    fun `Algan — all enriched résineux have coefficients and positive volume`() {
        val essences = listOf(
            "PIN_ALEP", "PIN_PIGNON", "PIN_CEMBRO", "PIN_MUGO", "PIN_SALZMANN", "PIN_MONTEREY",
            "EPICEA_SITKA", "EPICEA_OMORIKA", "SAPIN_NORDMANN", "SAPIN_GRANDIS",
            "SAPIN_CEPHALONIE", "SAPIN_ESPAGNE", "MEL_JAPON", "CEDRE_ATLAS", "CEDRE_LIBAN",
            "THUYA_GEANT", "CYPRES_PROVENCE", "SEQUOIA_TOUJOURS_VERT", "CRYPTOMERE",
            "CYPRES_CHAUVE", "TSUGA_HETEROPHYLLE", "GENEVRIER_CADE", "GENEVRIER_PHENICIE"
        )
        for (ess in essences) {
            val coefs = TarifCalculator.alganCoefsFor(ess)
            assertNotNull("Missing Algan coefficients for $ess", coefs)
            val v = TarifCalculator.computeVolume(TarifMethod.ALGAN, ess, 35.0, 22.0)
            assertNotNull("Volume null for $ess", v)
            assertTrue("Volume should be positive for $ess (got $v)", v!! > 0.0)
        }
    }

    @Test
    fun `CoefForme — all enriched essences have form coefficients`() {
        val essences = listOf(
            "CH_PUBESCENT", "CH_ROUGE", "PLATANE", "TULIPIER", "EUCALYPTUS_GUNNII",
            "PIN_ALEP", "SAPIN_GRANDIS", "CEDRE_ATLAS", "CYPRES_PROVENCE", "SEQUOIA_TOUJOURS_VERT",
            "CRYPTOMERE", "TSUGA_HETEROPHYLLE", "GENEVRIER_CADE"
        )
        for (ess in essences) {
            val f = TarifCalculator.defaultCoefForme(ess)
            assertTrue("CoefForme for $ess ($f) should be in 0.35..0.55", f in 0.35..0.55)
        }
    }

    @Test
    fun `IFN Rapide — enriched essences have auto-selected tarif`() {
        val essences = listOf(
            "SAPIN_GRANDIS", "CEDRE_ATLAS", "SEQUOIA_TOUJOURS_VERT",
            "EUCALYPTUS_GLOBULUS", "PLATANE", "TULIPIER", "PIN_ALEP"
        )
        for (ess in essences) {
            val v = TarifCalculator.computeVolume(TarifMethod.IFN_RAPIDE, ess, 40.0, null, tarifNumero = null)
            assertNotNull("IFN rapide auto-select should work for $ess", v)
            assertTrue("Volume should be positive for $ess", v!! > 0.0)
        }
    }

    @Test
    fun `Alias — FRENE resolves to FRENE_ELEVE`() {
        val v1 = TarifCalculator.computeVolume(TarifMethod.ALGAN, "FRENE", 35.0, 22.0)
        val v2 = TarifCalculator.computeVolume(TarifMethod.ALGAN, "FRENE_ELEVE", 35.0, 22.0)
        assertNotNull(v1)
        assertNotNull(v2)
        assertApprox(v1!!, v2, delta = 1e-12, msg = "FRENE alias -> FRENE_ELEVE")
    }

    @Test
    fun `Alias — SAPIN resolves to SAPIN_PECTINE`() {
        val v1 = TarifCalculator.computeVolume(TarifMethod.ALGAN, "SAPIN", 35.0, 22.0)
        val v2 = TarifCalculator.computeVolume(TarifMethod.ALGAN, "SAPIN_PECTINE", 35.0, 22.0)
        assertNotNull(v1)
        assertNotNull(v2)
        assertApprox(v1!!, v2, delta = 1e-12, msg = "SAPIN alias -> SAPIN_PECTINE")
    }

    @Test
    fun `Alias — CEDRE resolves to CEDRE_ATLAS`() {
        val v1 = TarifCalculator.computeVolume(TarifMethod.ALGAN, "CEDRE", 35.0, 22.0)
        val v2 = TarifCalculator.computeVolume(TarifMethod.ALGAN, "CEDRE_ATLAS", 35.0, 22.0)
        assertNotNull(v1)
        assertNotNull(v2)
        assertApprox(v1!!, v2, delta = 1e-12, msg = "CEDRE alias -> CEDRE_ATLAS")
    }

    @Test
    fun `Alias — EUCALYPTUS resolves`() {
        val v = TarifCalculator.computeVolume(TarifMethod.ALGAN, "EUCALYPTUS", 35.0, 22.0)
        assertNotNull("EUCALYPTUS alias should resolve", v)
        assertTrue(v!! > 0.0)
    }
}
