package com.forestry.counter

import com.forestry.counter.domain.calculator.FormulaParser
import com.forestry.counter.domain.model.Counter
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class FormulaParserTest {

    private lateinit var parser: FormulaParser
    private lateinit var testCounters: List<Counter>

    @Before
    fun setup() {
        parser = FormulaParser()
        
        testCounters = listOf(
            Counter(
                id = "1",
                groupId = "g1",
                name = "Hêtre 15-20",
                value = 10.0,
                tags = listOf("Feuillus", "Hêtre")
            ),
            Counter(
                id = "2",
                groupId = "g1",
                name = "Hêtre 20-25",
                value = 15.0,
                tags = listOf("Feuillus", "Hêtre")
            ),
            Counter(
                id = "3",
                groupId = "g1",
                name = "Chêne 15-20",
                value = 8.0,
                tags = listOf("Feuillus", "Chêne")
            ),
            Counter(
                id = "4",
                groupId = "g1",
                name = "Sapin 15-20",
                value = 12.0,
                tags = listOf("Résineux", "Sapin")
            )
        )
    }

    @Test
    fun `test basic arithmetic`() {
        val result = parser.evaluate("2 + 3 * 4", emptyList())
        assertTrue(result is FormulaParser.ParseResult.Success)
        assertEquals(14.0, (result as FormulaParser.ParseResult.Success).value, 0.001)
    }

    @Test
    fun `test sum all counters`() {
        val result = parser.evaluate("sum(*)", testCounters)
        assertTrue(result is FormulaParser.ParseResult.Success)
        assertEquals(45.0, (result as FormulaParser.ParseResult.Success).value, 0.001)
    }

    @Test
    fun `test sum with startsWith filter`() {
        val result = parser.evaluate("sum(name:startsWith('Hêtre'))", testCounters)
        assertTrue(result is FormulaParser.ParseResult.Success)
        assertEquals(25.0, (result as FormulaParser.ParseResult.Success).value, 0.001)
    }

    @Test
    fun `test sum with contains filter`() {
        val result = parser.evaluate("sum(name:contains('15-20'))", testCounters)
        assertTrue(result is FormulaParser.ParseResult.Success)
        assertEquals(30.0, (result as FormulaParser.ParseResult.Success).value, 0.001)
    }

    @Test
    fun `test sum with tag filter`() {
        val result = parser.evaluate("sum(tag:'Feuillus')", testCounters)
        assertTrue(result is FormulaParser.ParseResult.Success)
        assertEquals(33.0, (result as FormulaParser.ParseResult.Success).value, 0.001)
    }

    @Test
    fun `test avg function`() {
        val result = parser.evaluate("avg(*)", testCounters)
        assertTrue(result is FormulaParser.ParseResult.Success)
        assertEquals(11.25, (result as FormulaParser.ParseResult.Success).value, 0.001)
    }

    @Test
    fun `test count function`() {
        val result = parser.evaluate("count(name:startsWith('Hêtre'))", testCounters)
        assertTrue(result is FormulaParser.ParseResult.Success)
        assertEquals(2.0, (result as FormulaParser.ParseResult.Success).value, 0.001)
    }

    @Test
    fun `test min function`() {
        val result = parser.evaluate("min(*)", testCounters)
        assertTrue(result is FormulaParser.ParseResult.Success)
        assertEquals(8.0, (result as FormulaParser.ParseResult.Success).value, 0.001)
    }

    @Test
    fun `test max function`() {
        val result = parser.evaluate("max(*)", testCounters)
        assertTrue(result is FormulaParser.ParseResult.Success)
        assertEquals(15.0, (result as FormulaParser.ParseResult.Success).value, 0.001)
    }

    @Test
    fun `test counter reference by name`() {
        val result = parser.evaluate("[Hêtre 15-20] + [Chêne 15-20]", testCounters)
        assertTrue(result is FormulaParser.ParseResult.Success)
        assertEquals(18.0, (result as FormulaParser.ParseResult.Success).value, 0.001)
    }

    @Test
    fun `test density calculation with variable`() {
        val variables = mapOf("PLOT_AREA" to 2000.0)
        val result = parser.evaluate("sum(*) * (10000 / PLOT_AREA)", testCounters, variables)
        assertTrue(result is FormulaParser.ParseResult.Success)
        assertEquals(225.0, (result as FormulaParser.ParseResult.Success).value, 0.001)
    }

    @Test
    fun `test if condition greater than`() {
        val result = parser.evaluate("if(10 > 5, 100, 50)", emptyList())
        assertTrue(result is FormulaParser.ParseResult.Success)
        assertEquals(100.0, (result as FormulaParser.ParseResult.Success).value, 0.001)
    }

    @Test
    fun `test if condition less than`() {
        val result = parser.evaluate("if(3 < 5, 100, 50)", emptyList())
        assertTrue(result is FormulaParser.ParseResult.Success)
        assertEquals(100.0, (result as FormulaParser.ParseResult.Success).value, 0.001)
    }

    @Test
    fun `test complex formula`() {
        val variables = mapOf("PLOT_AREA" to 2000.0)
        val expression = "(sum(tag:'Feuillus') / sum(*)) * 100"
        val result = parser.evaluate(expression, testCounters, variables)
        assertTrue(result is FormulaParser.ParseResult.Success)
        val percentage = (result as FormulaParser.ParseResult.Success).value
        assertEquals(73.33, percentage, 0.01)
    }

    @Test
    fun `test validation with balanced parentheses`() {
        val result = parser.validate("(2 + 3) * 4")
        assertTrue(result is FormulaParser.ValidationResult.Valid)
    }

    @Test
    fun `test validation with unbalanced parentheses`() {
        val result = parser.validate("(2 + 3 * 4")
        assertTrue(result is FormulaParser.ValidationResult.Error)
    }

    @Test
    fun `test validation with empty expression`() {
        val result = parser.validate("")
        assertTrue(result is FormulaParser.ValidationResult.Error)
    }
}
