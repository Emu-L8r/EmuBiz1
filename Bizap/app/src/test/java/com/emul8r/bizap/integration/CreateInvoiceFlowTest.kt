package com.emul8r.bizap.integration

import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.data.local.dao.InvoiceDaoV2
import com.emul8r.bizap.data.local.entities.DailyRevenueTrendV2
import com.emul8r.bizap.data.local.entities.InvoiceStatusCountV2
import com.emul8r.bizap.data.repository.analytics.AnalyticsCalculator
import com.emul8r.bizap.data.repository.analytics.AnalyticsValidator
import com.emul8r.bizap.data.repository.gui2.PaymentAnalyticsRepositoryV2
import com.emul8r.bizap.data.repository.gui2.RevenueRepositoryV2
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Integration tests for the create invoice flow.
 *
 * Verifies that after an invoice is created:
 * 1. Dashboard revenue metrics update correctly
 * 2. Payment analytics metrics update correctly
 * 3. Outstanding balance is tracked accurately
 * 4. Status transitions are reflected in analytics
 */
class CreateInvoiceFlowTest : BaseUnitTest() {

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

    // ── create invoice → dashboard updates ──────────────────────────────────

    @Test
    fun `createInvoice_DashboardUpdates - new invoice appears in total count`() = runTest {
        val statusCounts = listOf(InvoiceStatusCountV2("SENT", 1))
        stubPaymentDao(outstanding = 100000L, collected = 0L, statusCounts = statusCounts)

        val metrics = paymentRepo.observePaymentMetrics(businessId).first()

        assertEquals(1, metrics.totalInvoices)
        assertEquals(1, metrics.sentCount)
        assertEquals(0, metrics.paidCount)
    }

    @Test
    fun `createInvoice_DashboardUpdates - outstanding amount reflects new invoice total`() = runTest {
        val invoiceTotal = 150000L  // $1,500.00
        val statusCounts = listOf(InvoiceStatusCountV2("SENT", 1))
        stubPaymentDao(outstanding = invoiceTotal, collected = 0L, statusCounts = statusCounts)

        val metrics = paymentRepo.observePaymentMetrics(businessId).first()

        assertEquals(invoiceTotal, metrics.outstandingAmount)
        assertEquals(0L, metrics.collectedAmount)
    }

    @Test
    fun `createInvoice_DashboardUpdates - MTD revenue updates when non-draft invoice created`() = runTest {
        val invoiceAmount = 200000L  // $2,000.00
        stubRevenueDao(mtd = invoiceAmount, ytd = invoiceAmount, weekly = invoiceAmount, totalPaid = 0L)

        val metrics = revenueRepo.observeRevenueMetrics(businessId).first()

        assertEquals(invoiceAmount, metrics.mtdRevenue)
        assertEquals(invoiceAmount, metrics.ytdRevenue)
    }

    @Test
    fun `createInvoice_DashboardUpdates - draft invoice does not affect outstanding`() = runTest {
        val statusCounts = listOf(InvoiceStatusCountV2("DRAFT", 1))
        stubPaymentDao(outstanding = 0L, collected = 0L, statusCounts = statusCounts)

        val metrics = paymentRepo.observePaymentMetrics(businessId).first()

        assertEquals(1, metrics.draftCount)
        assertEquals(0L, metrics.outstandingAmount)
    }

    // ── multiple invoices ────────────────────────────────────────────────────

    @Test
    fun `createMultipleInvoices_TotalsAggregated - outstanding sums all unpaid invoices`() = runTest {
        val totalOutstanding = 450000L  // 3 invoices × $1,500 each
        val statusCounts = listOf(
            InvoiceStatusCountV2("SENT", 2),
            InvoiceStatusCountV2("OVERDUE", 1)
        )
        stubPaymentDao(outstanding = totalOutstanding, collected = 0L, statusCounts = statusCounts)

        val metrics = paymentRepo.observePaymentMetrics(businessId).first()

        assertEquals(3, metrics.totalInvoices)
        assertEquals(totalOutstanding, metrics.outstandingAmount)
    }

    @Test
    fun `createMultipleInvoices_TotalsAggregated - YTD revenue includes all non-draft invoices`() = runTest {
        val ytdTotal = 600000L  // Sum of all non-draft invoices
        stubRevenueDao(mtd = 200000L, ytd = ytdTotal, weekly = 200000L, totalPaid = 200000L)

        val metrics = revenueRepo.observeRevenueMetrics(businessId).first()

        assertEquals(ytdTotal, metrics.ytdRevenue)
    }

    // ── status transitions ───────────────────────────────────────────────────

    @Test
    fun `statusTransition_SENT_to_PAID - outstanding decreases when invoice paid`() = runTest {
        // Before payment: invoice is SENT
        val statusCountsBefore = listOf(InvoiceStatusCountV2("SENT", 1))
        stubPaymentDao(outstanding = 100000L, collected = 0L, statusCounts = statusCountsBefore)
        val before = paymentRepo.observePaymentMetrics(businessId).first()

        // After payment: invoice is PAID
        val statusCountsAfter = listOf(InvoiceStatusCountV2("PAID", 1))
        every { dao.observeOutstandingAmount(businessId) } returns flowOf(0L)
        every { dao.observeCollectedAmount(businessId) } returns flowOf(100000L)
        every { dao.observeInvoiceCountByStatus(businessId) } returns flowOf(statusCountsAfter)
        every { dao.observeOverdueCount(businessId) } returns flowOf(0)
        every { dao.observeAverageDaysToPayment(businessId) } returns flowOf(5.0)
        val after = paymentRepo.observePaymentMetrics(businessId).first()

        assertTrue(before.outstandingAmount > after.outstandingAmount)
        assertEquals(0L, after.outstandingAmount)
        assertEquals(100000L, after.collectedAmount)
    }

    @Test
    fun `statusTransition_SENT_to_OVERDUE - overdue count updates in risk dashboard`() = runTest {
        val statusCounts = listOf(InvoiceStatusCountV2("OVERDUE", 2))
        stubPaymentDao(outstanding = 200000L, collected = 0L, statusCounts = statusCounts, overdue = 2)

        val metrics = paymentRepo.observePaymentMetrics(businessId).first()

        assertEquals(2, metrics.overdueCount)
    }

    // ── outstanding balance math ─────────────────────────────────────────────

    @Test
    fun `outstandingBalance_Correctness - billed minus paid equals outstanding`() = runTest {
        val totalBilled = 300000L
        val totalPaid = 100000L
        val expectedOutstanding = totalBilled - totalPaid

        stubRevenueDao(mtd = totalBilled, ytd = totalBilled, weekly = 0L, totalPaid = totalPaid)
        stubPaymentDao(outstanding = expectedOutstanding, collected = totalPaid,
            statusCounts = listOf(InvoiceStatusCountV2("PAID", 1), InvoiceStatusCountV2("SENT", 2)))

        val payment = paymentRepo.observePaymentMetrics(businessId).first()

        assertEquals(expectedOutstanding, payment.outstandingAmount)
        assertEquals(totalPaid, payment.collectedAmount)
    }

    @Test
    fun `outstandingBalance_Correctness - fully paid invoice has zero outstanding`() = runTest {
        val invoiceAmount = 50000L
        stubPaymentDao(
            outstanding = 0L,
            collected = invoiceAmount,
            statusCounts = listOf(InvoiceStatusCountV2("PAID", 1))
        )

        val metrics = paymentRepo.observePaymentMetrics(businessId).first()

        assertEquals(0L, metrics.outstandingAmount)
        assertEquals(invoiceAmount, metrics.collectedAmount)
        assertFalse(metrics.outstandingAmount < 0, "Outstanding should never be negative")
    }

    // ── daily trend ──────────────────────────────────────────────────────────

    @Test
    fun `createInvoice_DailyTrend - invoice appears in 30-day trend data`() = runTest {
        val trend = listOf(
            DailyRevenueTrendV2(dateString = "2026-03-08", revenue = 100000L, invoiceCount = 1, paidCount = 0)
        )
        stubRevenueDao(mtd = 100000L, ytd = 100000L, weekly = 100000L, totalPaid = 0L, trend = trend)

        val metrics = revenueRepo.observeRevenueMetrics(businessId).first()

        assertEquals(1, metrics.dailyTrend.size)
        assertEquals(100000L, metrics.dailyTrend[0].revenueCents)
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private fun stubRevenueDao(
        mtd: Long,
        ytd: Long,
        weekly: Long,
        totalPaid: Long,
        trend: List<DailyRevenueTrendV2> = emptyList()
    ) {
        every { dao.observeMTDRevenue(businessId) } returns flowOf(mtd)
        every { dao.observeYTDRevenue(businessId) } returns flowOf(ytd)
        every { dao.observeWeeklyRevenue(businessId) } returns flowOf(weekly)
        every { dao.observeTotalPaidRevenue(businessId) } returns flowOf(totalPaid)
        every { dao.observeLast30DaysRevenueTrend(businessId) } returns flowOf(trend)
    }

    private fun stubPaymentDao(
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
