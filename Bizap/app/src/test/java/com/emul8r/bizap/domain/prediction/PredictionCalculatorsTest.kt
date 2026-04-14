package com.emul8r.bizap.domain.prediction

import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.InvoiceStatus
import com.emul8r.bizap.domain.model.insights.RiskLevel
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Unit Tests for Prediction Calculators.
 *
 * **Coverage:**
 * - CashFlowForecastCalculator (8 tests)
 * - RiskScoreCalculator (10 tests)
 */
class PredictionCalculatorsTest : BaseUnitTest() {

    private lateinit var cashFlowCalculator: CashFlowForecastCalculator
    private lateinit var riskCalculator: RiskScoreCalculator

    @Before
    fun setUp() {
        cashFlowCalculator = CashFlowForecastCalculator()
        riskCalculator = RiskScoreCalculator()
    }

    // ========== CASH FLOW FORECAST TESTS ==========

    @Test
    fun `cashFlow_emptyInvoices - returns zero balance`() = runTest {
        val forecast = cashFlowCalculator.forecast(emptyList(), days = 30)

        assertEquals(0L, forecast.predictedBalance)
        assertEquals(0L, forecast.projectedInflows)
        assertEquals(0L, forecast.projectedOutflows)
        assertEquals(RiskLevel.HEALTHY, forecast.riskLevel)
    }

    @Test
    fun `cashFlow_paidInvoices - ignores paid invoices`() = runTest {
        val paidInvoices = listOf(
            createInvoice(id = 1, totalAmount = 10000L, amountPaid = 10000L, status = InvoiceStatus.PAID),
            createInvoice(id = 2, totalAmount = 5000L, amountPaid = 5000L, status = InvoiceStatus.PAID)
        )

        val forecast = cashFlowCalculator.forecast(paidInvoices, days = 30)

        assertEquals(0L, forecast.projectedInflows)
        assertEquals(RiskLevel.HEALTHY, forecast.riskLevel)
    }

    @Test
    fun `cashFlow_unpaidInvoices - calculates projected inflows`() = runTest {
        val invoices = listOf(
            createInvoice(id = 1, totalAmount = 10000L, amountPaid = 0L, status = InvoiceStatus.SENT),
            createInvoice(id = 2, totalAmount = 5000L, amountPaid = 2000L, status = InvoiceStatus.PARTIALLY_PAID)
        )

        val forecast = cashFlowCalculator.forecast(invoices, days = 30)

        val expectedInflows = 10000L + (5000L - 2000L)  // 13000L
        assertEquals(expectedInflows, forecast.projectedInflows)
        assertEquals(expectedInflows, forecast.predictedBalance)
    }

    @Test
    fun `cashFlow_negativeBalance - risk level RISK`() = runTest {
        val invoices = listOf(
            createInvoice(id = 1, totalAmount = 100000L, amountPaid = 150000L, status = InvoiceStatus.SENT)
        )

        val forecast = cashFlowCalculator.forecast(invoices, days = 30)

        assertTrue(forecast.predictedBalance < 0)
        assertEquals(RiskLevel.RISK, forecast.riskLevel)
    }

    @Test
    fun `cashFlow_lowBalance - risk level CAUTION`() = runTest {
        val invoices = listOf(
            createInvoice(id = 1, totalAmount = 30000L, amountPaid = 0L, status = InvoiceStatus.SENT)
        )

        val forecast = cashFlowCalculator.forecast(invoices, days = 30)

        assertTrue(forecast.predictedBalance in 1L..49999L)
        assertEquals(RiskLevel.CAUTION, forecast.riskLevel)
    }

    @Test
    fun `cashFlow_healthyBalance - risk level HEALTHY`() = runTest {
        val invoices = listOf(
            createInvoice(id = 1, totalAmount = 100000L, amountPaid = 0L, status = InvoiceStatus.SENT)
        )

        val forecast = cashFlowCalculator.forecast(invoices, days = 30)

        assertTrue(forecast.predictedBalance >= 50000L)
        assertEquals(RiskLevel.HEALTHY, forecast.riskLevel)
    }

    @Test
    fun `cashFlow_confidenceScore - ranges from 50 to 95`() = runTest {
        val invoices = (1..10).map { id ->
            createInvoice(
                id = id.toLong(),
                totalAmount = (1000L * id),
                amountPaid = 0L,
                status = InvoiceStatus.SENT
            )
        }

        val forecast = cashFlowCalculator.forecast(invoices, days = 30)

        assertTrue(forecast.confidence in 50.0..95.0)
    }

    @Test
    fun `cashFlow_recommendations - provides actionable advice`() = runTest {
        val invoices = listOf(
            createInvoice(id = 1, totalAmount = 10000L, amountPaid = 0L, status = InvoiceStatus.SENT)
        )

        val forecast = cashFlowCalculator.forecast(invoices, days = 30)

        assertTrue(forecast.recommendations.isNotEmpty())
        assertTrue(forecast.recommendations.any { it.contains("Expected") || it.contains("cash") })
    }

    // ========== RISK SCORE TESTS ==========

    @Test
    fun `risk_paidInvoice - score is zero`() = runTest {
        val invoice = createInvoice(id = 1, totalAmount = 10000L, amountPaid = 10000L, status = InvoiceStatus.PAID)

        val risk = riskCalculator.calculateInvoiceRisk(invoice)

        assertEquals(0.0, risk.score)
        assertEquals(RiskLevel.HEALTHY, risk.level)
    }

    @Test
    fun `risk_overdueInvoice - score gte 40`() = runTest {
        val daysAgo = LocalDate.now().minusDays(10).atStartOfDay()
        val overdueDate = Instant.from(daysAgo.atZone(java.time.ZoneId.systemDefault())).toString()

        val invoice = createInvoice(
            id = 1,
            totalAmount = 10000L,
            amountPaid = 0L,
            status = InvoiceStatus.OVERDUE,
            dueDate = overdueDate
        )

        val risk = riskCalculator.calculateInvoiceRisk(invoice)

        assertTrue(risk.score >= 40.0)
        assertEquals(RiskLevel.CAUTION, risk.level)
    }

    @Test
    fun `risk_severelyOverdue - score gte 70`() = runTest {
        val daysAgo = LocalDate.now().minusDays(30).atStartOfDay()
        val overdueDate = Instant.from(daysAgo.atZone(java.time.ZoneId.systemDefault())).toString()

        val invoice = createInvoice(
            id = 1,
            totalAmount = 10000L,
            amountPaid = 0L,
            status = InvoiceStatus.OVERDUE,
            dueDate = overdueDate
        )

        val risk = riskCalculator.calculateInvoiceRisk(invoice)

        assertTrue(risk.score >= 70.0)
        assertEquals(RiskLevel.RISK, risk.level)
    }

    @Test
    fun `risk_largeInvoice - adds points for size`() = runTest {
        val largeInvoice = createInvoice(
            id = 1,
            totalAmount = 60000L,  // > 50% of expected revenue
            amountPaid = 0L,
            status = InvoiceStatus.SENT
        )

        val risk = riskCalculator.calculateInvoiceRisk(
            largeInvoice,
            totalBusinessRevenue = 100000L
        )

        assertTrue(risk.score > 0.0)
        assertTrue(risk.factors.any { it.contains("size") || it.contains("Size") })
    }

    @Test
    fun `risk_badPaymentHistory - adds points for late patterns`() = runTest {
        val currentInvoice = createInvoice(id = 1, totalAmount = 10000L, amountPaid = 0L, status = InvoiceStatus.SENT)

        val lateInvoices = listOf(
            createInvoice(id = 2, totalAmount = 5000L, amountPaid = 5000L, status = InvoiceStatus.PAID),
            createInvoice(id = 3, totalAmount = 8000L, amountPaid = 8000L, status = InvoiceStatus.PAID)
        )

        val risk = riskCalculator.calculateInvoiceRisk(
            currentInvoice,
            customerPaymentHistory = lateInvoices
        )

        assertTrue(risk.score > 0.0)
        assertTrue(risk.factors.any { it.contains("history") || it.contains("History") })
    }

    @Test
    fun `risk_score_clamped - range 0 to 100`() = runTest {
        val invoice = createInvoice(id = 1, totalAmount = 10000L, amountPaid = 0L, status = InvoiceStatus.SENT)

        val risk = riskCalculator.calculateInvoiceRisk(invoice)

        assertTrue(risk.score in 0.0..100.0)
    }

    @Test
    fun `risk_factors_detailed - includes clear reasons`() = runTest {
        val invoice = createInvoice(
            id = 1,
            totalAmount = 10000L,
            amountPaid = 0L,
            status = InvoiceStatus.OVERDUE
        )

        val risk = riskCalculator.calculateInvoiceRisk(invoice)

        assertTrue(risk.factors.isNotEmpty())
        assertTrue(risk.factors.all { it.contains(":") })  // Format: "Factor: +N points"
    }

    @Test
    fun `risk_allFactors_combined - accounts for all risk sources`() = runTest {
        val daysAgo = LocalDate.now().minusDays(15).atStartOfDay()
        val overdueDate = Instant.from(daysAgo.atZone(java.time.ZoneId.systemDefault())).toString()

        val largeOverdueInvoice = createInvoice(
            id = 1,
            totalAmount = 70000L,  // Large
            amountPaid = 0L,
            status = InvoiceStatus.OVERDUE,
            dueDate = overdueDate  // Overdue
        )

        val risk = riskCalculator.calculateInvoiceRisk(
            largeOverdueInvoice,
            totalBusinessRevenue = 100000L
        )

        // Should have multiple factors
        assertTrue(risk.factors.size >= 2)
        assertTrue(risk.score > 50.0)
    }

    // ========== HELPERS ==========

    private fun createInvoice(
        id: Long = 1L,
        totalAmount: Long = 10000L,
        amountPaid: Long = 0L,
        status: InvoiceStatus = InvoiceStatus.SENT,
        dueDate: String? = null
    ): Invoice {
        // Use provided dueDate or default to today + 30 days
        val actualDueDate = dueDate ?: Instant.now().plusSeconds(86_400L * 30).toString()

        return Invoice(
            id = id,
            customerId = 1L,
            customerName = "Test Customer",
            totalAmount = totalAmount,
            amountPaid = amountPaid,
            status = status,
            dueDate = actualDueDate,
            dateCreated = Instant.now().toString(),
            isQuote = false,
            items = emptyList()
        )
    }
}

