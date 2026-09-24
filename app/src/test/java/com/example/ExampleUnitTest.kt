package com.example

import com.example.calculator.MathEvaluator
import com.example.converter.ConverterCategory
import com.example.converter.UnitConverter
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun addition_isCorrect() {
        val result = MathEvaluator.evaluate("2 + 2", isDegree = true)
        assertTrue(result is MathEvaluator.EvalResult.Success)
        assertEquals("4", (result as MathEvaluator.EvalResult.Success).formatted)
    }

    @Test
    fun operatorPrecedence_isCorrect() {
        val result = MathEvaluator.evaluate("2 + 3 * 4", isDegree = true)
        assertTrue(result is MathEvaluator.EvalResult.Success)
        assertEquals("14", (result as MathEvaluator.EvalResult.Success).formatted)
    }

    @Test
    fun parentheses_isCorrect() {
        val result = MathEvaluator.evaluate("(2 + 3) * 4", isDegree = true)
        assertTrue(result is MathEvaluator.EvalResult.Success)
        assertEquals("20", (result as MathEvaluator.EvalResult.Success).formatted)
    }

    @Test
    fun divisionByZero_returnsError() {
        val result = MathEvaluator.evaluate("5 / 0", isDegree = true)
        assertTrue(result is MathEvaluator.EvalResult.Error)
        assertEquals("Cannot divide by 0", (result as MathEvaluator.EvalResult.Error).message)
    }

    @Test
    fun trigonometricFunctions_isCorrect() {
        val sin90Deg = MathEvaluator.evaluate("sin(90)", isDegree = true)
        assertTrue(sin90Deg is MathEvaluator.EvalResult.Success)
        assertEquals("1", (sin90Deg as MathEvaluator.EvalResult.Success).formatted)

        val cos0Deg = MathEvaluator.evaluate("cos(0)", isDegree = true)
        assertTrue(cos0Deg is MathEvaluator.EvalResult.Success)
        assertEquals("1", (cos0Deg as MathEvaluator.EvalResult.Success).formatted)
    }

    @Test
    fun factorial_isCorrect() {
        val fact5 = MathEvaluator.evaluate("5!", isDegree = true)
        assertTrue(fact5 is MathEvaluator.EvalResult.Success)
        assertEquals("120", (fact5 as MathEvaluator.EvalResult.Success).formatted)
    }

    @Test
    fun unitConverter_length_isCorrect() {
        val units = UnitConverter.getUnitsForCategory(ConverterCategory.LENGTH)
        val m = units.first { it.symbol == "m" }
        val km = units.first { it.symbol == "km" }
        val converted = UnitConverter.convert(ConverterCategory.LENGTH, km, m, 2.5)
        assertEquals(2500.0, converted, 0.001)
    }
}
