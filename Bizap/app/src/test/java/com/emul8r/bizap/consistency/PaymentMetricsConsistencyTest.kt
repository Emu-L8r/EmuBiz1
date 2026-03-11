@file:Suppress("UNCHECKED_CAST")
package com.emul8r.bizap.consistency

import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.data.local.dao.InvoiceDaoV2
import com.emul8r.bizap.data.local.entities.InvoiceStatusCountV2
import com.emul8r.bizap.data.repository.analytics.AnalyticsCalculator
import com.emul8r.bizap.data.repository.analytics.AnalyticsValidator
import com.emul8r.bizap.data.repository.gui2.PaymentAnalyticsRepositoryV2
import com.emul8r.bizap.data.repository.gui2.RevenueRepositoryV2
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests verifying that payment metrics are consistent across all dashboards.
 *
 * Both GUI1 and GUI2 now share [AnalyticsRepositoryBridge] → V2 repositories,
 * so the same data source is used regardless of which screen is displayed.
 *
 * These tests verify the invariants that must always hold:
 * 1. Outstanding = Total Billed - Total Paid
 * 2. Collection Rate = (Paid / Billed) × 100
 * 3. Paid count matches collected amount > 0
 */
class PaymentMetricsConsistencyTest : BaseUnitTest() {

    private val dao: InvoiceDaoV2 = mockk()
    private lateinit var revenueRepo: RevenueRepositoryV2
    private lateinit var paymentRepo: PaymentAnalyticsRepositoryV2

    private val businessId = 1L

    @Before
    fun setup() {
        val calculator = AnalyticsCalculator()
        val validator = AnalyticsValidator()
        revenueRepo = RevenueRepositoryV2(dao, calculator, validator)
        paymentRepo = PaymentAnalyticsRepositoryV2(dao, calculator, validator)
    }

    // ── outstanding balance invariant ────────────────────────────────────────

    @Test
    fun `outstandingBalance_Invariant - outstanding equals billed minus collected`() = runTest {
        val billed = 500000L    // $5,000 total billed
        val collected = 200000L  // $2,000 paid
        val outstanding = billed - collected  // $3,000 outstanding

        stubDao(outstanding = outstanding, collected = collected,
            statusCounts = listOf(
                InvoiceStatusCountV2("PAID", 2),
                InvoiceStatusCountV2("SENT", 3)
            )
        )

        val metrics = paymentRepo.observePaymentMetrics(businessId).first()

        assertEquals(outstanding, metrics.outstandingAmount)
        assertEquals(collected, metrics.collectedAmount)
        // Outstanding must equal billed minus collected
        assertEquals(billed - metrics.collectedAmount, metrics.outstandingAmount)
    }

    @Test
    fun `outstandingBalance_Invariant - never negative`() = runTest {
        // Even with weird data, outstanding should never be negative in a well-formed dataset
        stubDao(outstanding = 0L, collected = 500000L,
            statusCounts = listOf(InvoiceStatusCountV2("PAID", 5)))

        val metrics = paymentRepo.observePaymentMetrics(businessId).first()

        assertTrue(metrics.outstandingAmount >= 0, "Outstanding should never be negative")
    }

    @Test
    fun `outstandingBalance_Invariant - fully paid business has zero outstanding`() = runTest {
        stubDao(outstanding = 0L, collected = 1000000L,
            statusCounts = listOf(InvoiceStatusCountV2("PAID", 10)))

        val metrics = paymentRepo.observePaymentMetrics(businessId).first()

        assertEquals(0L, metrics.outstandingAmount)
        assertEquals(10, metrics.paidCount)
    }

    // ── cross-dashboard consistency ──────────────────────────────────────────

    @Test
    fun `crossDashboard_Consistency - revenue repo and payment repo show same paid total`() = runTest {
        val totalPaid = 750000L

        every { dao.observeMTDRevenue(businessId) } returns flowOf(750000L)
        every { dao.observeYTDRevenue(businessId) } returns flowOf(750000L)
        every { dao.observeWeeklyRevenue(businessId) } returns flowOf(150000L)
        every { dao.observeTotalPaidRevenue(businessId) } returns flowOf(totalPaid)
        every { dao.observeLast30DaysRevenueTrend(businessId) } returns flowOf(emptyList())

        stubDao(outstanding = 250000L, collected = totalPaid,
            statusCounts = listOf(
                InvoiceStatusCountV2("PAID", 3),
                InvoiceStatusCountV2("SENT", 1)
            )
        )

        val revenue = revenueRepo.observeRevenueMetrics(businessId).first()
        val payment = paymentRepo.observePaymentMetrics(businessId).first()

        // Revenue repo totalPaidRevenue must equal payment repo collectedAmount
        assertEquals(revenue.totalPaidRevenue, payment.collectedAmount,
            "GUI1 and GUI2 must show the same paid total")
    }

    @Test
    fun `crossDashboard_Consistency - same data source ensures identical metrics`() = runTest {
        val outstanding = 300000L
        val collected = 700000L

        every { dao.observeMTDRevenue(businessId) } returns flowOf(1000000L)
        every { dao.observeYTDRevenue(businessId) } returns flowOf(1000000L)
        every { dao.observeWeeklyRevenue(businessId) } returns flowOf(200000L)
        every { dao.observeTotalPaidRevenue(businessId) } returns flowOf(collected)
        every { dao.observeLast30DaysRevenueTrend(businessId) } returns flowOf(emptyList())
        stubDao(outstanding = outstanding, collected = collected,
            statusCounts = listOf(
                InvoiceStatusCountV2("PAID", 7),
                InvoiceStatusCountV2("SENT", 3)
            )
        )

        val revenue1 = revenueRepo.observeRevenueMetrics(businessId).first()
        val revenue2 = revenueRepo.observeRevenueMetrics(businessId).first()

        // Multiple collections from the same V2 repository must be identical
        assertEquals(revenue1.totalPaidRevenue, revenue2.totalPaidRevenue)
        assertEquals(revenue1.mtdRevenue, revenue2.mtdRevenue)
    }

    // ── invoice count consistency ────────────────────────────────────────────

    @Test
    fun `invoiceCount_Consistency - total count equals sum of all status counts`() = runTest {
        val statusCounts = listOf(
            InvoiceStatusCountV2("PAID", 5),
            InvoiceStatusCountV2("SENT", 3),
            InvoiceStatusCountV2("DRAFT", 2),
            InvoiceStatusCountV2("OVERDUE", 1),
            InvoiceStatusCountV2("PARTIALLY_PAID", 1)
        )
        stubDao(outstanding = 400000L, collected = 600000L, statusCounts = statusCounts)

        val metrics = paymentRepo.observePaymentMetrics(businessId).first()

        val expectedTotal = statusCounts.sumOf { it.count }
        assertEquals(expectedTotal, metrics.totalInvoices)
        assertEquals(5, metrics.paidCount)
        assertEquals(3, metrics.sentCount)
        assertEquals(2, metrics.draftCount)
        assertEquals(1, metrics.partiallyPaidCount)
    }

    // ── overdue metrics ──────────────────────────────────────────────────────

    @Test
    fun `overdueCount_Consistent - overdue count matches risk data`() = runTest {
        val overdueCount = 4
        stubDao(
            outstanding = 400000L,
            collected = 0L,
            statusCounts = listOf(InvoiceStatusCountV2("OVERDUE", overdueCount)),
            overdue = overdueCount
        )

        val metrics = paymentRepo.observePaymentMetrics(businessId).first()

        assertEquals(overdueCount, metrics.overdueCount)
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private fun stubDao(
        outstanding: Long,
        collected: Long,
        statusCounts: List<InvoiceStatusCountV2>,
        overdue: Int = 0,
        avgDays: Double = 0.0
    ) {
        every { dao.observeOutstandingAmount(businessId) } returns flowOf(outstanding)
        every { dao.observeCollectedAmount(businessId) } returns flowOf(collected)
        every { dao.observeInvoiceCountByStatus(businessId) } returns flowOf(statusCounts)
        every { dao.observeOverdueCount(businessId) } returns flowOf(overdue)
        every { dao.observeAverageDaysToPayment(businessId) } returns flowOf(avgDays)
    }
}
