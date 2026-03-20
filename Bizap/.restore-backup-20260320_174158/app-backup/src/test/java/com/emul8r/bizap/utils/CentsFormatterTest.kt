package com.emul8r.bizap.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CentsFormatterTest {

    @Test
    fun testFormatCents_AUD() {
        val formatted = CentsFormatter.formatCents(14999, "AUD")
        // AUD symbol is "A$" in US locale (CI environment) and "$" in Australian locale
        val validFormats = listOf("A$149.99", "$149.99")
        assertTrue("Expected AUD-formatted value, got: $formatted", formatted in validFormats)
    }

    @Test
    fun testFormatCents_Zero() {
        val formatted = CentsFormatter.formatCents(0, "AUD")
        // AUD symbol is "A$" in US locale (CI environment) and "$" in Australian locale
        val validFormats = listOf("A$0.00", "$0.00")
        assertTrue("Expected AUD zero-formatted value, got: $formatted", formatted in validFormats)
    }

    @Test
    fun testFormatCents_USD() {
        val formatted = CentsFormatter.formatCents(100, "USD")
        // USD symbol can vary by locale ($ in most locales)
        val validFormats = listOf("$1.00", "USD1.00")
        assertTrue("Expected USD-formatted value, got: $formatted", formatted in validFormats)
    }

    @Test
    fun testFormatCents_Large() {
        // DecimalFormat depends on locale, but assuming standard US/AU grouping
        val formatted = CentsFormatter.formatCents(999999, "AUD")
        val validFormats = listOf("A$9,999.99", "$9,999.99", "$9 999.99", "A$9 999.99")
        assertTrue("Formatted value was $formatted", formatted in validFormats)
    }

    @Test
    fun testFormatCentsWithSymbol_Euro() {
        assertEquals("€149.99", CentsFormatter.formatCentsWithSymbol(14999, "€"))
    }

    @Test
    fun testDollarsToCents() {
        assertEquals(14999L, CentsFormatter.dollarsToCents(149.99))
    }

    @Test
    fun testDollarsToCents_Zero() {
        assertEquals(0L, CentsFormatter.dollarsToCents(0.0))
    }

    @Test
    fun testCentsToDollars() {
        assertEquals(149.99, CentsFormatter.centsToDollars(14999), 0.001)
    }

    @Test
    fun testParseToCents() {
        assertEquals(14999L, CentsFormatter.parseToCents("$149.99"))
    }

    @Test
    fun testParseToCents_Empty() {
        assertEquals(0L, CentsFormatter.parseToCents(""))
    }

    @Test
    fun testParseToCents_Invalid() {
        assertEquals(0L, CentsFormatter.parseToCents("abc"))
    }

    @Test
    fun testFormatCents_Negative() {
        val result = CentsFormatter.formatCents(-500, "AUD")
        assertTrue("Should handle negative values gracefully: $result", result.contains("5.00"))
        assertTrue("Should contain a negative sign or parentheses: $result", result.contains("-") || result.contains("("))
    }
}
