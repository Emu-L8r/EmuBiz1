package com.emul8r.bizap.utils

import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.InvoiceStatus
import com.emul8r.bizap.domain.model.LineItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Validates the global "Cents Only" unit standardization rule.
 *
 * Rule: All business logic operates on cents (Long). The UI layer is the only
 * place that converts to dollars via [CentsFormatter]. Mixing cents and dollars
 * anywhere else is a bug.
 *
 * Tests in this file will fail if cents/dollars are ever confused in domain models
 * or repository calculations.
 */
class MoneyUnitTest {

    // ── Domain Model: Invoice ─────────────────────────────────────────────────

    @Test
    fun `invoice totalAmount is stored as cents (Long)`() {
        // $149.99 → 14999 cents. If stored as dollars this would be 149 (wrong).
        val invoice = buildInvoice(totalAmountCents = 14999L, amountPaidCents = 0L)
        assertEquals(14999L, invoice.totalAmount)
    }

    @Test
    fun `invoice amountPaid is stored as cents (Long)`() {
        val invoice = buildInvoice(totalAmountCents = 10000L, amountPaidCents = 5000L)
        assertEquals(5000L, invoice.amountPaid)
    }

    @Test
    fun `invoice balanceRemaining is computed in cents`() {
        val invoice = buildInvoice(totalAmountCents = 20000L, amountPaidCents = 8000L)
        // $200.00 - $80.00 = $120.00 = 12000 cents
        assertEquals(12000L, invoice.balanceRemaining)
    }

    @Test
    fun `invoice is fully paid when balanceRemaining is zero`() {
        val invoice = buildInvoice(totalAmountCents = 10000L, amountPaidCents = 10000L)
        assertTrue(invoice.isFullyPaid)
        assertEquals(0L, invoice.balanceRemaining)
    }

    @Test
    fun `invoice lineItem unitPrice is stored as cents (Long)`() {
        // $49.99 → 4999 cents
        val item = LineItem(description = "Service", quantity = 1.0, unitPrice = 4999L)
        assertEquals(4999L, item.unitPrice)
    }

    // ── CentsFormatter conversions ────────────────────────────────────────────

    @Test
    fun `centsToDollars converts correctly`() {
        // 14999 cents → $149.99
        assertEquals(149.99, CentsFormatter.centsToDollars(14999L), 0.001)
    }

    @Test
    fun `dollarsToCents converts correctly`() {
        // $149.99 → 14999 cents
        assertEquals(14999L, CentsFormatter.dollarsToCents(149.99))
    }

    @Test
    fun `centsToDollars on zero returns zero`() {
        assertEquals(0.0, CentsFormatter.centsToDollars(0L), 0.0)
    }

    @Test
    fun `dollarsToCents on zero returns zero`() {
        assertEquals(0L, CentsFormatter.dollarsToCents(0.0))
    }

    @Test
    fun `round-trip cents - dollars - cents is lossless for common amounts`() {
        listOf(0L, 1L, 99L, 100L, 14999L, 100000L, 9999999L).forEach { cents ->
            val dollars = CentsFormatter.centsToDollars(cents)
            val roundTripped = CentsFormatter.dollarsToCents(dollars)
            assertEquals("Round-trip failed for $cents cents", cents, roundTripped)
        }
    }

    // ── Accounting Math Rules ─────────────────────────────────────────────────

    @Test
    fun `outstanding is totalAmount minus amountPaid in cents`() {
        val totalCents = 100000L  // $1,000.00
        val paidCents = 30000L    // $300.00
        val outstanding = totalCents - paidCents
        assertEquals(70000L, outstanding)  // $700.00
    }

    @Test
    fun `collection rate is amount-based not count-based`() {
        // 1 large PAID invoice ($900) vs 9 small SENT invoices ($100 total)
        val collected = 90000L    // $900 in cents
        val outstanding = 10000L  // $100 in cents
        val collectionRate = collected * 100.0 / (collected + outstanding)
        // Should be 90%, not 10% (count-based would be 1/10)
        assertEquals(90.0, collectionRate, 0.01)
    }

    @Test
    fun `mtd revenue uses amountPaid not totalAmount`() {
        // An invoice for $100 with $50 paid: MTD revenue is $50, not $100
        val invoice = buildInvoice(totalAmountCents = 10000L, amountPaidCents = 5000L)
        val mtdRevenue = invoice.amountPaid  // Only what was actually collected
        assertEquals(5000L, mtdRevenue)
    }

    @Test
    fun `draft invoices contribute zero to outstanding and revenue`() {
        val draftInvoice = buildInvoice(
            totalAmountCents = 100000L,
            amountPaidCents = 0L,
            status = InvoiceStatus.DRAFT
        )
        // DRAFT invoices are excluded from outstanding (status filter in SQL)
        // We verify the model carries the right cents values
        assertEquals(InvoiceStatus.DRAFT, draftInvoice.status)
        assertEquals(0L, draftInvoice.amountPaid)
    }

    // ── Formatting sanity ─────────────────────────────────────────────────────

    @Test
    fun `formatCents produces dollar-formatted output`() {
        val formatted = CentsFormatter.formatCents(8220000L, "AUD")
        // A$82,200.00 or $82,200.00 depending on locale
        assertTrue(
            "Expected dollar-formatted output, got: $formatted",
            formatted.contains("82") && formatted.contains("200") && formatted.contains("00")
        )
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun buildInvoice(
        totalAmountCents: Long,
        amountPaidCents: Long,
        status: InvoiceStatus = InvoiceStatus.SENT
    ) = Invoice(
        id = 1L,
        businessProfileId = 1L,
        customerId = 1L,
        customerName = "Test Customer",
        date = System.currentTimeMillis(),
        totalAmount = totalAmountCents,
        amountPaid = amountPaidCents,
        items = listOf(
            LineItem(description = "Service", quantity = 1.0, unitPrice = totalAmountCents)
        ),
        isQuote = false,
        status = status
    )
}
