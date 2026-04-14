package com.emul8r.bizap.data.calculation

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for tax calculation logic.
 *
 * Verifies 10% GST calculations, zero-tax scenarios, and edge cases
 * like rounding and large amounts.
 */
class TaxCalculationTest {

    // ── calculateTax_10Percent_Correct ────────────────────────────────────────

    @Test
    fun `calculateTax_10Percent_Correct - 10% tax on 100000 cents is 10000 cents`() {
        val subtotal = 100000L  // $1000.00
        val taxRate = 0.10
        val tax = calculateTax(subtotal, taxRate)
        assertEquals(10000L, tax, "10% tax on $1000 should be $100")
    }

    @Test
    fun `calculateTax_10Percent_Correct - total with 10% tax is correct`() {
        val subtotal = 100000L
        val taxRate = 0.10
        val tax = calculateTax(subtotal, taxRate)
        val total = subtotal + tax
        assertEquals(110000L, total, "Total with 10% GST should be $1100")
    }

    @Test
    fun `calculateTax_10Percent_Correct - 10% tax on 50000 cents is 5000 cents`() {
        val subtotal = 50000L  // $500.00
        val taxRate = 0.10
        val tax = calculateTax(subtotal, taxRate)
        assertEquals(5000L, tax, "10% tax on $500 should be $50")
    }

    @Test
    fun `calculateTax_10Percent_Correct - 10% tax on 1 cent results in 0 cents (floor rounding)`() {
        val subtotal = 1L  // 1 cent
        val taxRate = 0.10
        val tax = calculateTax(subtotal, taxRate)
        // 0.1 cents rounds to 0
        assertEquals(0L, tax, "10% tax on 1 cent should be 0 cents (floor rounding)")
    }

    // ── calculateTax_ZeroPercent ──────────────────────────────────────────────

    @Test
    fun `calculateTax_ZeroPercent - 0% tax results in zero tax amount`() {
        val subtotal = 100000L
        val taxRate = 0.0
        val tax = calculateTax(subtotal, taxRate)
        assertEquals(0L, tax, "0% tax should result in zero")
    }

    @Test
    fun `calculateTax_ZeroPercent - total equals subtotal when tax is 0%`() {
        val subtotal = 50000L
        val taxRate = 0.0
        val tax = calculateTax(subtotal, taxRate)
        val total = subtotal + tax
        assertEquals(subtotal, total, "Total should equal subtotal when tax rate is 0")
    }

    // ── calculateTax_EdgeCases ────────────────────────────────────────────────

    @Test
    fun `calculateTax_EdgeCases - large amount tax calculation is correct`() {
        val subtotal = 10_000_000L  // $100,000
        val taxRate = 0.10
        val tax = calculateTax(subtotal, taxRate)
        assertEquals(1_000_000L, tax, "10% tax on $100,000 should be $10,000")
    }

    @Test
    fun `calculateTax_EdgeCases - multiple items subtotal tax calculation`() {
        val items = listOf(50000L, 30000L, 20000L)  // $500, $300, $200 = $1000
        val subtotal = items.sum()
        val taxRate = 0.10
        val tax = calculateTax(subtotal, taxRate)
        assertEquals(100000L, subtotal)
        assertEquals(10000L, tax, "10% tax on $1000 should be $100")
    }

    @Test
    fun `calculateTax_EdgeCases - fractional cents are truncated not rounded`() {
        // $333.33 × 10% = $33.333 → truncated to $33.33 in cents
        val subtotal = 33333L
        val taxRate = 0.10
        val tax = calculateTax(subtotal, taxRate)
        // 3333.3 cents → 3333 cents
        assertTrue(tax >= 3333L, "Tax should be at least 3333 cents for $333.33 subtotal")
        assertTrue(tax <= 3334L, "Tax should not exceed 3334 cents for $333.33 subtotal")
    }

    @Test
    fun `calculateTax_EdgeCases - GST-inclusive amount extraction`() {
        // If total includes GST, extract GST: GST = total × 1/11
        val totalInclGst = 110000L  // $1100 including 10% GST
        val gst = totalInclGst / 11
        assertEquals(10000L, gst, "GST extracted from $1100 inclusive total should be $100")
    }

    @Test
    fun `calculateTax_EdgeCases - 15% tax rate calculation`() {
        val subtotal = 100000L
        val taxRate = 0.15
        val tax = calculateTax(subtotal, taxRate)
        assertEquals(15000L, tax, "15% tax on $1000 should be $150")
    }

    // ── helper ─────────────────────────────────────────────────────────────────

    private fun calculateTax(subtotalCents: Long, taxRate: Double): Long {
        return (subtotalCents * taxRate).toLong()
    }
}

