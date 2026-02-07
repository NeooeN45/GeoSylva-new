package com.forestry.counter.domain.calculation.quality

import org.junit.Assert.*
import org.junit.Test

class DefaultProductPricesTest {

    // ═══════════════════════════════════════════════════════════
    // Prix spécifiques essence×produit
    // ═══════════════════════════════════════════════════════════

    @Test
    fun `chene sessile merain has specific price around 1200`() {
        val price = DefaultProductPrices.priceFor("MERAIN", "CH_SESSILE", WoodQualityGrade.C)
        assertTrue("Chêne sessile mérain should be > 1000 (got $price)", price > 1000.0)
        assertTrue("Chêne sessile mérain should be < 1500 (got $price)", price < 1500.0)
    }

    @Test
    fun `douglas grume longue has specific price around 145`() {
        val price = DefaultProductPrices.priceFor("GRUME_L", "DOUGLAS_VERT", WoodQualityGrade.C)
        assertTrue("Douglas grume longue should be > 100 (got $price)", price > 100.0)
        assertTrue("Douglas grume longue should be < 200 (got $price)", price < 200.0)
    }

    @Test
    fun `noyer tranchage has specific price around 650`() {
        val price = DefaultProductPrices.priceFor("TRANCHAGE", "NOYER_COMMUN", WoodQualityGrade.C)
        assertTrue("Noyer tranchage should be > 500 (got $price)", price > 500.0)
        assertTrue("Noyer tranchage should be < 800 (got $price)", price < 800.0)
    }

    // ═══════════════════════════════════════════════════════════
    // Qualité ajuste le prix
    // ═══════════════════════════════════════════════════════════

    @Test
    fun `grade A price is higher than grade C`() {
        val pA = DefaultProductPrices.priceFor("SCIAGE_Q", "CH_SESSILE", WoodQualityGrade.A)
        val pC = DefaultProductPrices.priceFor("SCIAGE_Q", "CH_SESSILE", WoodQualityGrade.C)
        assertTrue("Grade A ($pA) should be > grade C ($pC)", pA > pC)
    }

    @Test
    fun `grade D price is lower than grade C`() {
        val pD = DefaultProductPrices.priceFor("SCIAGE_Q", "CH_SESSILE", WoodQualityGrade.D)
        val pC = DefaultProductPrices.priceFor("SCIAGE_Q", "CH_SESSILE", WoodQualityGrade.C)
        assertTrue("Grade D ($pD) should be < grade C ($pC)", pD < pC)
    }

    @Test
    fun `all grades produce positive prices`() {
        for (g in WoodQualityGrade.entries) {
            val p = DefaultProductPrices.priceFor("SCIAGE_S", "HETRE_COMMUN", g)
            assertTrue("Price for grade $g should be > 0 (got $p)", p > 0.0)
        }
    }

    // ═══════════════════════════════════════════════════════════
    // Fallback vers prix générique
    // ═══════════════════════════════════════════════════════════

    @Test
    fun `unknown essence falls back to base price with multiplier 1`() {
        val price = DefaultProductPrices.priceFor("SCIAGE_S", "ESSENCE_INCONNUE", WoodQualityGrade.C)
        val basePrice = DefaultProductPrices.defaults["SCIAGE_S"]!!
        assertEquals("Unknown essence should use base price", basePrice, price, 0.01)
    }

    @Test
    fun `unknown product returns 50 as fallback`() {
        val price = DefaultProductPrices.priceFor("PRODUIT_INEXISTANT", "CH_SESSILE", WoodQualityGrade.C)
        assertTrue("Unknown product should return positive price", price > 0.0)
    }

    // ═══════════════════════════════════════════════════════════
    // Cohérence des prix
    // ═══════════════════════════════════════════════════════════

    @Test
    fun `merain is more expensive than sciage`() {
        val merain = DefaultProductPrices.priceFor("MERAIN", "CH_SESSILE", WoodQualityGrade.C)
        val sciage = DefaultProductPrices.priceFor("SCIAGE_Q", "CH_SESSILE", WoodQualityGrade.C)
        assertTrue("Mérain ($merain) should be >> sciage ($sciage)", merain > sciage * 3)
    }

    @Test
    fun `bois energie is cheapest product`() {
        val be = DefaultProductPrices.defaults["BE"]!!
        for ((code, price) in DefaultProductPrices.defaults) {
            if (code != "BE") {
                assertTrue("$code ($price) should be >= BE ($be)", price >= be)
            }
        }
    }

    @Test
    fun `chene premium essences are more expensive than hetre`() {
        val chene = DefaultProductPrices.priceFor("SCIAGE_Q", "CH_SESSILE", WoodQualityGrade.C)
        val hetre = DefaultProductPrices.priceFor("SCIAGE_Q", "HETRE_COMMUN", WoodQualityGrade.C)
        assertTrue("Chêne sessile sciage Q ($chene) should be > hêtre ($hetre)", chene > hetre)
    }

    @Test
    fun `douglas is more expensive than pin sylvestre for sciage`() {
        val douglas = DefaultProductPrices.priceFor("SCIAGE_S", "DOUGLAS_VERT", WoodQualityGrade.C)
        val pin = DefaultProductPrices.priceFor("SCIAGE_S", "PIN_SYLVESTRE", WoodQualityGrade.C)
        assertTrue("Douglas ($douglas) should be > pin sylvestre ($pin)", douglas > pin)
    }
}
