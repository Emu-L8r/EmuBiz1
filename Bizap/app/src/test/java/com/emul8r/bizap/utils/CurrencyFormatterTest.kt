package com.emul8r.bizap.utils

import org.junit.Test
import java.util.Locale
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Regression tests for CurrencyFormatter to ensure money formatting logic is robust.
 */
class CurrencyFormatterTest {

    @Test
    fun `test standard AUD formatting`() {
        val formatted = CurrencyFormatter.formatCents(123456L, "AUD")
        // Note: Locale.getDefault() might vary in different environments, 
        // but it should contain the symbol and correct digits.
        assertTrue(formatted.contains("1,234.56"))
    }

    @Test
    fun `test large values format safely without crash`() {
        // One trillion dollars in cents
        val largeValue = 100_000_000_000_000L 
        val formatted = CurrencyFormatter.formatCents(largeValue, "USD")
        assertTrue(formatted.contains("1,000,000,000,000.00"))
    }

    @Test
    fun `test zero cents formatting`() {
        val formatted = CurrencyFormatter.formatCents(0L, "USD")
        assertTrue(formatted.contains("0.00"))
    }

    @Test
    fun `test negative values formatted with minus sign`() {
        val formatted = CurrencyFormatter.formatCents(-5000L, "USD")
        // Some locales put minus before symbol, some after.
        assertTrue(formatted.contains("50.00"))
        assertTrue(formatted.contains("-") || formatted.contains("("))
    }

    @Test
    fun `test rounding precision is preserved`() {
        // 199 cents should be 1.99
        assertEquals(true, CurrencyFormatter.formatCents(199L, "USD").contains("1.99"))
        // 200 cents should be 2.00
        assertEquals(true, CurrencyFormatter.formatCents(200L, "USD").contains("2.00"))
    }

    @Test
    fun `test unsupported currency code falls back gracefully`() {
        // "XYZ" is not a valid ISO code
        val formatted = CurrencyFormatter.formatCents(100L, "XYZ")
        assertTrue(formatted.contains("1.00"))
        assertTrue(formatted.contains("$")) // Fallback uses $
    }

    @Test
    fun `test different currency symbols`() {
        val eur = CurrencyFormatter.formatCents(100L, "EUR")
        assertTrue(eur.contains("1.00"))
        
        val jpy = CurrencyFormatter.formatCents(100L, "JPY")
        // JPY usually doesn't have decimals in standard format, 
        // but our formatter currently forces / 100.0 and %.2f in fallback or standard NumberFormat.
        // If Currency.getInstance("JPY").getDefaultFractionDigits() is 0, NumberFormat might behave differently.
        // But our fallback uses %.2f.
        assertTrue(jpy.contains("1.00"))
    }
}
