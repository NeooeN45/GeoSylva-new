package com.forestry.counter.data.work

import com.forestry.counter.domain.calculation.PriceEntry
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class PriceSyncWorkerValidationTest {
    private val valid = PriceEntry(
        essence = "CH_SESSILE",
        product = "BO",
        min = 25,
        max = 40,
        eurPerM3 = 95.0,
        quality = "A",
        source = "Mercuriale contrôlée",
        region = "NATIONAL",
        year = 2026
    )

    @Test
    fun `valid finite price grid is accepted`() {
        assertTrue(validatePriceEntries(listOf(valid)))
    }

    @Test
    fun `empty and duplicate grids are rejected`() {
        assertFalse(validatePriceEntries(emptyList()))
        assertFalse(validatePriceEntries(listOf(valid, valid.copy(essence = " ch_sessile "))))
    }

    @Test
    fun `invalid business values are rejected before persistence`() {
        listOf(
            valid.copy(essence = ""),
            valid.copy(product = ""),
            valid.copy(min = -1),
            valid.copy(max = 24),
            valid.copy(max = 10_001),
            valid.copy(eurPerM3 = -0.01),
            valid.copy(eurPerM3 = Double.NaN),
            valid.copy(eurPerM3 = Double.POSITIVE_INFINITY),
            valid.copy(quality = "Z"),
            valid.copy(source = ""),
            valid.copy(region = ""),
            valid.copy(year = 1800)
        ).forEach { invalid ->
            assertFalse("Entrée invalide acceptée : $invalid", validatePriceEntries(listOf(invalid)))
        }
    }

    @Test
    fun `response body limit accepts the boundary and rejects oversized payloads`() {
        val exact = "1234".toResponseBody("application/json".toMediaType())
        assertEquals("1234", exact.readLimitedUtf8(maxBytes = 4))

        val tooLarge = "12345".toResponseBody("application/json".toMediaType())
        try {
            tooLarge.readLimitedUtf8(maxBytes = 4)
            fail("Un flux tarifaire dépassant la limite doit être refusé")
        } catch (_: IOException) {
            // Limite appliquée avant allocation et décodage.
        }
    }
}
