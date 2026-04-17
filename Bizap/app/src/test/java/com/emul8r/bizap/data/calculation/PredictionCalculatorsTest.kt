package com.emul8r.bizap.data.calculation

import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.InvoiceItem
import com.emul8r.bizap.domain.model.InvoiceStatus
import com.emul8r.bizap.domain.model.insights.RiskLevel
import com.emul8r.bizap.domain.prediction.CashFlowForecastCalculator
import com.emul8r.bizap.domain.prediction.RiskScoreCalculator
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Unit tests for [CashFlowForecastCalculator] and [RiskScoreCalculator].
 */
class PredictionCalculatorsTest : BaseUnitTest() {

    private lateinit var cashFlowCalculator: CashFlowForecastCalculator
    private lateinit var riskCalculator: RiskScoreCalculator

    // ── helpers ────────────────────────────────────────────────────────────

    private fun invoice(
        id: Long = 1L,
        status: InvoiceStatus = InvoiceStatus.SENT,
        totalAmount: Long = 10_000L,
        amountPaid: Long = 0L,
        dueDate: String = futureDateString(30),
        customerId: Long = 1L
    ) = Invoice(
        id = id,
        customerId = customerId,
        customerName = "Test Client",
        totalAmount = totalAmount,
        amountPaid = amountPaid,
        status = status,
        dueDate = dueDate,
        dateCreated = pastDateString(10),
        currency = "AUD",
        items = listOf(InvoiceItem(description = "Service", quantity = 1.0, unitPrice = totalAmount))
    )

    private fun futureDateString(daysAhead: Int): String {
        val date = java.time.LocalDate.now().plusDays(daysAhead.toLong())
        return date.toString() + "T00:00:00Z"
    }

    private fun pastDateString(daysAgo: Int): String {
        val date = java.time.LocalDate.now().minusDays(daysAgo.toLong())
        return date.toString() + "T00:00:00Z"
    }

    @Before
    fun setUp() {
        cashFlowCalculator = CashFlowForecastCalculator()
        riskCalculator = RiskScoreCalculator()
    }

    // ── CashFlowForecastCalculator ─────────────────────────────────────────

    @Test
    fun `forecast returns non-null result`() {
        val result = cashFlowCalculator.forecast(emptyList(), days = 30)
        assertNotNull(result)
    }

    @Test
    fun `forecast returns correct forecast days`() {
        val result = cashFlowCalculator.forecast(emptyList(), days = 30)
        assertEquals(30, result.forecastDays)
    }

    @Test
    fun `forecast with no invoices has zero projected inflows`() {
        val result = cashFlowCalculator.forecast(emptyList(), days = 30)
        assertEquals(0L, result.projectedInflows)
    }

    @Test
    fun `forecast with no invoices has zero projected outflows`() {
        val result = cashFlowCalculator.forecast(emptyList(), days = 30)
        assertEquals(0L, result.projectedOutflows)
    }

    @Test
    fun `forecast with unpaid sent invoices due within window has positive inflows`() {
        val inv = invoice(status = InvoiceStatus.SENT, totalAmount = 50_000L, dueDate = futureDateString(15))
        val result = cashFlowCalculator.forecast(listOf(inv), days = 30)
        assertTrue(result.projectedInflows > 0L)
    }

    @Test
    fun `forecast with paid invoice has no projected inflows from that invoice`() {
        val paid = invoice(status = InvoiceStatus.PAID, totalAmount = 50_000L, amountPaid = 50_000L)
        val result = cashFlowCalculator.forecast(listOf(paid), days = 30)
        // Paid invoices should not contribute to projected inflows
        assertEquals(0L, result.projectedInflows)
    }

    @Test
    fun `forecast confidence is between 0 and 100`() {
        val result = cashFlowCalculator.forecast(listOf(invoice()), days = 30)
        assertTrue(result.confidence in 0.0..100.0)
    }

    @Test
    fun `forecast has a risk level`() {
        val result = cashFlowCalculator.forecast(emptyList(), days = 30)
        assertNotNull(result.riskLevel)
    }

    @Test
    fun `forecast for 60 days returns forecastDays=60`() {
        val result = cashFlowCalculator.forecast(emptyList(), days = 60)
        assertEquals(60, result.forecastDays)
    }

    @Test
    fun `forecast for 90 days returns forecastDays=90`() {
        val result = cashFlowCalculator.forecast(emptyList(), days = 90)
        assertEquals(90, result.forecastDays)
    }

    @Test
    fun `forecast with invoice due beyond window is excluded from inflows`() {
        val farFuture = invoice(status = InvoiceStatus.SENT, totalAmount = 50_000L, dueDate = futureDateString(90))
        val result = cashFlowCalculator.forecast(listOf(farFuture), days = 30)
        // Invoice due in 90 days should not contribute to 30-day forecast
        assertEquals(0L, result.projectedInflows)
    }

    // ── RiskScoreCalculator ────────────────────────────────────────────────

    @Test
    fun `calculateInvoiceRisk returns non-null result`() {
        val inv = invoice()
        val result = riskCalculator.calculateInvoiceRisk(inv)
        assertNotNull(result)
    }

    @Test
    fun `paid invoice has HEALTHY risk level`() {
        val paid = invoice(status = InvoiceStatus.PAID, amountPaid = 10_000L)
        val result = riskCalculator.calculateInvoiceRisk(paid)
        assertEquals(RiskLevel.HEALTHY, result.level)
    }

    @Test
    fun `overdue invoice has elevated risk`() {
        val overdue = invoice(
            status = InvoiceStatus.OVERDUE,
            dueDate = pastDateString(30)
        )
        val result = riskCalculator.calculateInvoiceRisk(overdue)
        assertTrue(result.score > 0.0)
    }

    @Test
    fun `overdue invoice has at least CAUTION risk level`() {
        val overdue = invoice(
            status = InvoiceStatus.OVERDUE,
            dueDate = pastDateString(60)
        )
        val result = riskCalculator.calculateInvoiceRisk(overdue)
        assertTrue(result.level == RiskLevel.CAUTION || result.level == RiskLevel.RISK)
    }

    @Test
    fun `draft invoice has low risk score`() {
        val draft = invoice(status = InvoiceStatus.DRAFT)
        val result = riskCalculator.calculateInvoiceRisk(draft)
        assertEquals(RiskLevel.HEALTHY, result.level)
    }

    @Test
    fun `risk score is within 0-100 bounds`() {
        val overdue = invoice(status = InvoiceStatus.OVERDUE, dueDate = pastDateString(120))
        val result = riskCalculator.calculateInvoiceRisk(overdue)
        assertTrue(result.score in 0.0..100.0)
    }

    @Test
    fun `invoice with bad payment history has higher risk score`() {
        val current = invoice(status = InvoiceStatus.SENT)
        val history = List(5) {
            invoice(id = it.toLong() + 10, status = InvoiceStatus.OVERDUE, dueDate = pastDateString(30 + it * 10))
        }
        val withHistory = riskCalculator.calculateInvoiceRisk(current, customerPaymentHistory = history)
        val withoutHistory = riskCalculator.calculateInvoiceRisk(current)
        assertTrue(withHistory.score >= withoutHistory.score)
    }

    @Test
    fun `very large invoice relative to business revenue increases risk`() {
        val largeInvoice = invoice(totalAmount = 999_999_000L)
        val smallRevenue = 100_000L
        val result = riskCalculator.calculateInvoiceRisk(largeInvoice, totalBusinessRevenue = smallRevenue)
        assertTrue(result.score > 0.0)
    }
}

