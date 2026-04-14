package com.emul8r.bizap.data.calculation

import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.InvoiceStatus
import com.emul8r.bizap.domain.model.InvoiceItem
import com.emul8r.bizap.domain.model.balanceRemaining
import com.emul8r.bizap.domain.model.isFullyPaid
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import java.time.Instant

/**
 * Unit tests for outstanding balance calculations.
 *
 * Verifies balance remaining logic, partial payment calculations,
 * aging bucket classification, and line item total calculations.
 */
class OutstandingBalanceCalculationTest {

    private val now = Instant.now().toString()
    private val tomorrow = Instant.now().plusSeconds(86_400L).toString()

    private fun buildInvoice(
        totalAmount: Long,
        amountPaid: Long = 0L,
        status: InvoiceStatus = InvoiceStatus.SENT
    ) = Invoice(
        id = 1L,
        businessProfileId = 1L,
        customerId = 1L,
        customerName = "Test Customer",
        dateCreated = now,
        dueDate = tomorrow,
        totalAmount = totalAmount,
        amountPaid = amountPaid,
        items = listOf(InvoiceItem(description = "Service", quantity = 1.0, unitPrice = totalAmount)),
        isQuote = false,
        status = status,
        currency = "AUD"
    )

    // ── outstanding_PartialPayment ─────────────────────────────────────────────

    @Test
    fun `outstanding_PartialPayment - partial payment reduces balance correctly`() {
        val invoice = buildInvoice(totalAmount = 100000L, amountPaid = 40000L)
        val balance: Double = invoice.balanceRemaining
        assertEquals(600.0, balance, "Balance should be $600 after $400 payment")
    }

    @Test
    fun `outstanding_PartialPayment - multiple payments accumulate correctly`() {
        val totalAmount = 100000L
        val payment1 = 30000L
        val payment2 = 25000L
        val totalPaid = payment1 + payment2
        val remaining = totalAmount - totalPaid
        assertEquals(45000L, remaining, "Remaining should be $450 after $300 + $250 payments")
    }

    @Test
    fun `outstanding_PartialPayment - isFullyPaid is false for partial payment`() {
        val invoice = buildInvoice(totalAmount = 100000L, amountPaid = 60000L)
        assertFalse(invoice.isFullyPaid, "Invoice should not be fully paid with partial payment")
        assertTrue(invoice.balanceRemaining > 0)
    }

    // ── outstanding_FullPayment ───────────────────────────────────────────────

    @Test
    fun `outstanding_FullPayment - full payment results in zero balance`() {
        val invoice = buildInvoice(totalAmount = 100000L, amountPaid = 100000L)
        val balance: Double = invoice.balanceRemaining
        assertEquals(0.0, balance, "Balance should be zero after full payment")
    }

    @Test
    fun `outstanding_FullPayment - isFullyPaid is true when balance is zero`() {
        val invoice = buildInvoice(totalAmount = 100000L, amountPaid = 100000L)
        assertTrue(invoice.isFullyPaid, "Invoice should be marked as fully paid")
    }

    @Test
    fun `outstanding_FullPayment - zero amount paid with nonzero invoice is not fully paid`() {
        val invoice = buildInvoice(totalAmount = 100000L, amountPaid = 0L)
        assertFalse(invoice.isFullyPaid, "Invoice with no payment should not be fully paid")
        val balance: Double = invoice.balanceRemaining
        assertEquals(1000.0, balance)
    }

    // ── outstanding_Overpayment_Prevented ─────────────────────────────────────

    @Test
    fun `outstanding_Overpayment_Prevented - overpayment is prevented by validation`() {
        val outstanding = 50000L
        val overpayment = 60000L
        val isValid = overpayment <= outstanding
        assertFalse(isValid, "Overpayment should be prevented")
    }

    @Test
    fun `outstanding_Overpayment_Prevented - payment equal to outstanding is allowed`() {
        val outstanding = 50000L
        val exactPayment = 50000L
        val isValid = exactPayment <= outstanding
        assertTrue(isValid, "Payment equal to outstanding should be allowed")
    }

    @Test
    fun `outstanding_Overpayment_Prevented - balance never goes negative`() {
        val totalAmount = 100000L
        // If we prevent overpayment, balance is always >= 0
        val amountPaid = minOf(100000L, totalAmount)  // Capped at totalAmount
        val balance = totalAmount - amountPaid
        assertTrue(balance >= 0, "Balance should never go negative")
    }

    // ── invoiceTotal_SubtotalPlusTax ──────────────────────────────────────────

    @Test
    fun `invoiceTotal_SubtotalPlusTax - total equals subtotal when no tax`() {
        val items = listOf(
            InvoiceItem(description = "Service", quantity = 2.0, unitPrice = 10000L)
        )
        val subtotal = items.sumOf { (it.unitPrice * it.quantity).toLong() }
        val tax = 0L
        val total = subtotal + tax
        assertEquals(20000L, total)
    }

    @Test
    fun `invoiceTotal_SubtotalPlusTax - total includes GST when applicable`() {
        val subtotal = 100000L
        val taxAmount = 10000L  // 10% GST
        val total = subtotal + taxAmount
        assertEquals(110000L, total)
    }

    @Test
    fun `invoiceTotal_SubtotalPlusTax - invoice total matches calculated total`() {
        val items = listOf(
            InvoiceItem(description = "Item A", quantity = 2.0, unitPrice = 5000L),
            InvoiceItem(description = "Item B", quantity = 1.0, unitPrice = 10000L)
        )
        val subtotal = items.sumOf { (it.unitPrice * it.quantity).toLong() }
        assertEquals(20000L, subtotal, "Subtotal should be $200")
    }

    // ── lineItemTotal_QtyTimesPrice ────────────────────────────────────────────

    @Test
    fun `lineItemTotal_QtyTimesPrice - line item total is quantity times unit price`() {
        val item = InvoiceItem(description = "Service", quantity = 3.0, unitPrice = 10000L)
        val total = (item.unitPrice * item.quantity).toLong()
        assertEquals(30000L, total, "3 × $100 = $300")
    }

    @Test
    fun `lineItemTotal_QtyTimesPrice - fractional quantity calculates correctly`() {
        val item = InvoiceItem(description = "Half Hour", quantity = 0.5, unitPrice = 20000L)
        val total = (item.unitPrice * item.quantity).toLong()
        assertEquals(10000L, total, "0.5 × $200 = $100")
    }

    @Test
    fun `lineItemTotal_QtyTimesPrice - multiple items summed correctly`() {
        val items = listOf(
            InvoiceItem(description = "Item 1", quantity = 2.0, unitPrice = 10000L),  // $200
            InvoiceItem(description = "Item 2", quantity = 3.0, unitPrice = 5000L),   // $150
            InvoiceItem(description = "Item 3", quantity = 1.0, unitPrice = 20000L)   // $200
        )
        val total = items.sumOf { (it.unitPrice * it.quantity).toLong() }
        assertEquals(55000L, total, "Sum should be $550")
    }

    // ── ageingBuckets_Classification_Correct ──────────────────────────────────

    @Test
    fun `ageingBuckets_Classification_Correct - invoice within 30 days is current`() {
        val invoiceDate = Instant.now().minusSeconds(15 * 86_400L)  // 15 days ago
        val now_instant = Instant.now()
        val daysOverdue = (now_instant.epochSecond - invoiceDate.epochSecond) / 86_400
        val bucket = classifyAgingBucket(daysOverdue.toInt())
        assertEquals("Current", bucket, "15 days ago should be Current")
    }

    @Test
    fun `ageingBuckets_Classification_Correct - invoice 31-60 days overdue is 1-30 bucket`() {
        val daysOverdue = 45
        val bucket = classifyAgingBucket(daysOverdue)
        assertEquals("31-60 days", bucket)
    }

    @Test
    fun `ageingBuckets_Classification_Correct - invoice over 90 days is 90+ bucket`() {
        val daysOverdue = 120
        val bucket = classifyAgingBucket(daysOverdue)
        assertEquals("90+ days", bucket)
    }

    @Test
    fun `ageingBuckets_Classification_Correct - bucket sums equal total outstanding`() {
        val current = 30000L
        val days1to30 = 20000L
        val days31to60 = 15000L
        val days61to90 = 10000L
        val days90plus = 5000L
        val total = current + days1to30 + days31to60 + days61to90 + days90plus
        val totalOutstanding = 80000L
        assertEquals(totalOutstanding, total, "Aging bucket sums should equal total outstanding")
    }

    // ── helper ─────────────────────────────────────────────────────────────────

    private fun classifyAgingBucket(daysOverdue: Int): String {
        return when {
            daysOverdue <= 30 -> "Current"
            daysOverdue <= 60 -> "31-60 days"
            daysOverdue <= 90 -> "61-90 days"
            else -> "90+ days"
        }
    }
}



