package com.forestry.counter.domain.model

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertThrows
import org.junit.Test

/**
 * Non-regression du bareme officiel IBP FR v3.2 du CNPF (02/02/2026).
 */
class IbpCriterionDataTest {

    @Test
    fun `official score set includes the one point intermediate level`() {
        assertEquals(setOf(0, 1, 2, 5), IbpAnswers.VALID_SCORES)
        assertEquals(IbpAnswers.SCHEMA_PREVIOUS_METHOD, IbpAnswers().schemaVersion)
        assertEquals(IbpAnswers.CURRENT_SCHEMA_VERSION, IbpAnswers.new().schemaVersion)
    }


    @Test
    fun `serialized history without schema version stays on previous method`() {
        val decoded = Json.decodeFromString(IbpAnswers.serializer(), "{}")

        assertEquals(IbpAnswers.SCHEMA_PREVIOUS_METHOD, decoded.schemaVersion)
        assertFalse(decoded.isCurrentMethod)
    }
    @Test
    fun `criterion A applies official lowland thresholds`() {
        val scores = (0..5).map { count ->
            IbpCriterionData.scoreA(List(count) { "genre-$it" }, IbpGrowthConditions.LOWLAND)
        }

        assertEquals(listOf(0, 0, 1, 2, 2, 5), scores)
    }

    @Test
    fun `criterion A applies official subalpine thresholds`() {
        val scores = (0..3).map { count ->
            IbpCriterionData.scoreA(List(count) { "genre-$it" }, IbpGrowthConditions.SUBALPINE)
        }

        assertEquals(listOf(0, 1, 2, 5), scores)
    }

    @Test
    fun `criterion B applies official strata thresholds`() {
        val scores = (1..5).map { count ->
            IbpCriterionData.scoreB(List(count) { "strate-$it" })
        }

        assertEquals(listOf(0, 1, 2, 2, 5), scores)
    }

    @Test
    fun `deadwood criteria distinguish medium and large wood`() {
        assertEquals(0, IbpCriterionData.scoreCFromCounts(0f, 0f))
        assertEquals(1, IbpCriterionData.scoreCFromCounts(0f, 1f))
        assertEquals(1, IbpCriterionData.scoreCFromCounts(0.5f, 0.5f))
        assertEquals(2, IbpCriterionData.scoreCFromCounts(1f, 0f))
        assertEquals(5, IbpCriterionData.scoreCFromCounts(3f, 0f))

        assertEquals(1, IbpCriterionData.scoreDFromCounts(0f, 1f))
    }

    @Test
    fun `large living trees distinguish GB and TGB`() {
        assertEquals(
            1,
            IbpCriterionData.scoreEFromCounts(0f, 1f, IbpGrowthConditions.LOWLAND)
        )
        assertEquals(
            2,
            IbpCriterionData.scoreEFromCounts(1f, 0f, IbpGrowthConditions.LOWLAND)
        )
        assertEquals(
            5,
            IbpCriterionData.scoreEFromCounts(5f, 0f, IbpGrowthConditions.LOWLAND)
        )
    }

    @Test
    fun `microhabitat criterion applies 2 3 and 8 per hectare thresholds`() {
        assertEquals(listOf(0, 1, 2, 5), listOf(1.9f, 2f, 3f, 8f).map(IbpCriterionData::scoreFFromCounts))
    }

    @Test
    fun `microhabitat total caps every official group at two trees per hectare`() {
        val counts = mapOf(
            IbpCriterionData.dmhGroupKey(0) to 8f,
            IbpCriterionData.dmhGroupKey(1) to 1.5f,
            IbpCriterionData.dmhGroupKey(2) to -4f
        )

        assertEquals(3.5f, IbpCriterionData.dmhCappedTotal(counts), 0.001f)
        assertEquals(2, IbpCriterionData.scoreFFromCounts(IbpCriterionData.dmhCappedTotal(counts)))
    }

    @Test
    fun `historical v2 answers cannot receive a v3 point value`() {
        val history = IbpAnswers(schemaVersion = IbpAnswers.SCHEMA_PREVIOUS_METHOD)

        assertFalse(history.isCurrentMethod)
        assertThrows(IllegalArgumentException::class.java) {
            history.set(IbpCriterionId.E1, 1)
        }
        assertEquals(2, history.set(IbpCriterionId.E1, 2).e1)
    }

    @Test
    fun `legacy migration stops at v2 instead of silently applying v32`() {
        val migrated = IbpAnswers(schemaVersion = 1, e1 = 1).migrateToV2()

        assertEquals(IbpAnswers.SCHEMA_PREVIOUS_METHOD, migrated.schemaVersion)
        assertEquals(2, migrated.e1)
        assertFalse(migrated.isCurrentMethod)
    }

    @Test
    fun `open habitat threshold depends on growth case`() {
        assertEquals(2, IbpCriterionData.scoreGFromPct(6f, IbpGrowthConditions.LOWLAND))
        assertEquals(5, IbpCriterionData.scoreGFromPct(6f, IbpGrowthConditions.SUBALPINE))
        assertTrue(IbpAnswers.new().set(IbpCriterionId.E1, 1).get(IbpCriterionId.E1) == 1)
    }
}
