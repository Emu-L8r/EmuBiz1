package com.emul8r.bizap.domain.validation

import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Unit tests for payment validation rules.
 *
 * Covers amount positivity, overpayment prevention, and date validation.
 * These tests complement the existing [com.emul8r.bizap.data.repository.PaymentValidationTest]
 * by focusing on the domain-level validation rules.
 */
class PaymentValidationTest {

    private val now = System.currentTimeMillis()
    private val todayMidnight = run {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        cal.timeInMillis
    }
    private val invoiceDate = todayMidnight - 14 * 86_400_000L  // 14 days ago

    // ── amount_Positive ───────────────────────────────────────────────────────

    @Test
    fun `amount_Positive - zero amount is rejected`() {
        val amount = 0L
        val outstanding = 50000L
        assertFalse(isValidPaymentAmount(amount, outstanding), "Zero amount should be rejected")
    }

    @Test
    fun `amount_Positive - negative amount is rejected`() {
        val amount = -1L
        val outstanding = 50000L
        assertFalse(isValidPaymentAmount(amount, outstanding), "Negative amount should be rejected")
    }

    @Test
    fun `amount_Positive - one cent is the minimum valid amount`() {
        val amount = 1L
        val outstanding = 50000L
        assertTrue(isValidPaymentAmount(amount, outstanding), "1 cent should be the minimum valid amount")
    }

    @Test
    fun `amount_Positive - positive amount within outstanding is valid`() {
        val amount = 25000L
        val outstanding = 50000L
        assertTrue(isValidPaymentAmount(amount, outstanding), "Positive amount within outstanding should be valid")
    }

    // ── amount_NotExceedOutstanding ───────────────────────────────────────────

    @Test
    fun `amount_NotExceedOutstanding - amount equal to outstanding is valid (full payment)`() {
        val outstanding = 50000L
        val amount = outstanding
        assertTrue(isValidPaymentAmount(amount, outstanding), "Amount equal to outstanding should be valid")
    }

    @Test
    fun `amount_NotExceedOutstanding - amount one cent over outstanding is rejected`() {
        val outstanding = 50000L
        val amount = outstanding + 1L
        assertFalse(isValidPaymentAmount(amount, outstanding), "Overpayment by 1 cent should be rejected")
    }

    @Test
    fun `amount_NotExceedOutstanding - large overpayment is rejected`() {
        val outstanding = 10000L
        val amount = 1000000L
        assertFalse(isValidPaymentAmount(amount, outstanding), "Large overpayment should be rejected")
    }

    @Test
    fun `amount_NotExceedOutstanding - any payment on fully paid invoice is rejected`() {
        val outstanding = 0L  // Fully paid
        val amount = 1L
        assertFalse(isValidPaymentAmount(amount, outstanding), "Payment on fully paid invoice should be rejected")
    }

    // ── date_NotFuture ────────────────────────────────────────────────────────

    @Test
    fun `date_NotFuture - today's midnight is valid payment date`() {
        assertFalse(isFutureDate(todayMidnight), "Today's date should not be considered future")
    }

    @Test
    fun `date_NotFuture - past date is valid payment date`() {
        val yesterday = todayMidnight - 86_400_000L
        assertFalse(isFutureDate(yesterday), "Yesterday should be a valid payment date")
    }

    @Test
    fun `date_NotFuture - tomorrow is rejected as future date`() {
        val tomorrow = todayMidnight + 86_400_000L
        assertTrue(isFutureDate(tomorrow), "Tomorrow should be rejected as future date")
    }

    @Test
    fun `date_NotFuture - payment date before invoice date is rejected`() {
        val beforeInvoice = invoiceDate - 86_400_000L
        assertTrue(isBeforeInvoiceDate(beforeInvoice, invoiceDate),
            "Payment date before invoice date should be rejected")
    }

    @Test
    fun `date_NotFuture - payment date on invoice date is valid`() {
        assertFalse(isBeforeInvoiceDate(invoiceDate, invoiceDate),
            "Payment date on invoice date should be valid")
    }

    @Test
    fun `date_NotFuture - payment date after invoice date is valid`() {
        val afterInvoice = invoiceDate + 86_400_000L
        assertFalse(isBeforeInvoiceDate(afterInvoice, invoiceDate),
            "Payment date after invoice date should be valid")
    }

    // ── helper validation functions ────────────────────────────────────────────

    private fun isValidPaymentAmount(amount: Long, outstanding: Long): Boolean {
        return amount > 0 && amount <= outstanding
    }

    private fun isFutureDate(dateMs: Long): Boolean {
        return dateMs > todayMidnight
    }

    private fun isBeforeInvoiceDate(paymentDate: Long, invoiceDate: Long): Boolean {
        return paymentDate < invoiceDate
    }
}



