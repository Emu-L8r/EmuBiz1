package com.emul8r.bizap.utils

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for CentsFormatter utility.
 * Tests critical money formatting functions.
 */
class CentsFormatterTest {

    @Test
    fun formatCents_withAUD_shouldReturnDollarWithSymbol() {
        val result = CentsFormatter.formatCents(14999, "AUD")
        assertEquals("$149.99", result)
    }

    @Test
    fun formatCents_withZero_shouldReturnZeroWithSymbol() {
        val result = CentsFormatter.formatCents(0, "AUD")
        assertEquals("$0.00", result)
    }

    @Test
    fun formatCents_withUSD_shouldReturnFormattedAmount() {
        val result = CentsFormatter.formatCents(100, "USD")
        assertEquals("$1.00", result)
    }

    @Test
    fun formatCents_withLargeAmount_shouldIncludeThousandsSeparator() {
        val result = CentsFormatter.formatCents(999999, "AUD")
        assertEquals("$9,999.99", result)
    }

    @Test
    fun formatCentsWithSymbol_withEuroSymbol_shouldFormatCorrectly() {
        val result = CentsFormatter.formatCentsWithSymbol(14999, "€")
        assertEquals("€149.99", result)
    }

    @Test
    fun formatCentsWithSymbol_withCustomSymbol_shouldFormatCorrectly() {
        val result = CentsFormatter.formatCentsWithSymbol(50000, "£")
        assertEquals("£500.00", result)
    }

    @Test
    fun dollarsToCents_shouldConvertCorrectly() {
        val result = CentsFormatter.dollarsToCents(149.99)
        assertEquals(14999L, result)
    }

    @Test
    fun dollarsToCents_withZero_shouldReturnZero() {
        val result = CentsFormatter.dollarsToCents(0.0)
        assertEquals(0L, result)
    }

    @Test
    fun dollarsToCents_withSmallAmount_shouldRoundCorrectly() {
        val result = CentsFormatter.dollarsToCents(0.01)
        assertEquals(1L, result)
    }

    @Test
    fun centsToDollars_shouldConvertCorrectly() {
        val result = CentsFormatter.centsToDollars(14999)
        assertEquals(149.99, result, 0.01)
    }

    @Test
    fun centsToDollars_withZero_shouldReturnZero() {
        val result = CentsFormatter.centsToDollars(0)
        assertEquals(0.0, result, 0.01)
    }

    @Test
    fun centsToDollars_withSmallAmount_shouldReturnFraction() {
        val result = CentsFormatter.centsToDollars(1)
        assertEquals(0.01, result, 0.01)
    }

    @Test
    fun parseToCents_withValidFormattedString_shouldParseCorrectly() {
        val result = CentsFormatter.parseToCents("$149.99")
        assertEquals(14999L, result)
    }

    @Test
    fun parseToCents_withEmptyString_shouldReturnZero() {
        val result = CentsFormatter.parseToCents("")
        assertEquals(0L, result)
    }

    @Test
    fun parseToCents_withInvalidString_shouldReturnZero() {
        val result = CentsFormatter.parseToCents("abc")
        assertEquals(0L, result)
    }

    @Test
    fun parseToCents_withThousandsSeparator_shouldParseCorrectly() {
        val result = CentsFormatter.parseToCents("$1,499.99")
        assertEquals(149999L, result)
    }

    @Test
    fun parseToCents_withOnlyNumbers_shouldParseCorrectly() {
        val result = CentsFormatter.parseToCents("14999")
        assertEquals(1499900L, result) // Parsed as dollars then converted to cents
    }

    @Test
    fun formatCents_withNegativeAmount_shouldHandleGracefully() {
        // Negative amounts should be handled without crash
        // Result format may vary, but should not throw exception
        val result = CentsFormatter.formatCents(-500, "AUD")
        assert(result.contains("5.00") || result.contains("-"))
    }

    @Test
    fun dollarsToCents_withNegativeAmount_shouldReturnNegativeCents() {
        val result = CentsFormatter.dollarsToCents(-100.0)
        assertEquals(-10000L, result)
    }

    @Test
    fun formatCents_roundTripConversion_shouldPreserveValue() {
        val originalCents = 14999L
        val dollars = CentsFormatter.centsToDollars(originalCents)
        val centAgain = CentsFormatter.dollarsToCents(dollars)
        assertEquals(originalCents, centAgain)
    }

    @Test
    fun formatCents_withEUR_shouldUseEuroSymbol() {
        val result = CentsFormatter.formatCents(14999, "EUR")
        // Result should contain "149.99" and possibly € or other EUR formatting
        assert(result.contains("149.99") || result.contains("149,99"))
    }
}

