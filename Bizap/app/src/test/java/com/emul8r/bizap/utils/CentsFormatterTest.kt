package com.emul8r.bizap.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CentsFormatterTest {

    @Test
    fun testFormatCents_AUD() {
        assertEquals("$149.99", CentsFormatter.formatCents(14999, "AUD"))
    }

    @Test
    fun testFormatCents_Zero() {
        assertEquals("$0.00", CentsFormatter.formatCents(0, "AUD"))
    }

    @Test
    fun testFormatCents_USD() {
        // Assuming USD also uses $ as symbol in default locale
        assertEquals("$1.00", CentsFormatter.formatCents(100, "USD"))
    }

    @Test
    fun testFormatCents_Large() {
        // DecimalFormat depends on locale, but assuming standard US/AU grouping
        val formatted = CentsFormatter.formatCents(999999, "AUD")
        assertTrue("Formatted value was $formatted", formatted == "$9,999.99" || formatted == "$9 999.99")
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
