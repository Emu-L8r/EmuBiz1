@file:Suppress("UNCHECKED_CAST")
package com.emul8r.bizap.gui2

import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.data.local.dao.InvoiceDaoV2
import com.emul8r.bizap.data.local.entities.DailyRevenueTrendV2
import com.emul8r.bizap.data.local.entities.InvoiceStatusCountV2
import com.emul8r.bizap.data.repository.analytics.AnalyticsCalculator
import com.emul8r.bizap.data.repository.analytics.AnalyticsValidator
import com.emul8r.bizap.data.repository.gui2.PaymentAnalyticsRepositoryV2
import com.emul8r.bizap.data.repository.gui2.RevenueRepositoryV2
import com.emul8r.bizap.data.repository.gui2.RiskAnalyticsRepositoryV2
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

/**
 * Unit tests for the GUI2 repository layer.
 * All DAO calls are mocked; tests verify repository logic and flow composition.
 */
class RevenueRepositoryV2Test : BaseUnitTest() {

    private val daoV2: InvoiceDaoV2 = mockk()
    private lateinit var revenueRepository: RevenueRepositoryV2
    private lateinit var paymentRepository: PaymentAnalyticsRepositoryV2
    private lateinit var riskRepository: RiskAnalyticsRepositoryV2

    private val businessId = 1L

    @Before
    fun setup() {
        val calculator = AnalyticsCalculator()
        val validator = AnalyticsValidator()
        revenueRepository = RevenueRepositoryV2(daoV2, calculator, validator)
        paymentRepository = PaymentAnalyticsRepositoryV2(daoV2, calculator, validator)
        riskRepository = RiskAnalyticsRepositoryV2(daoV2, calculator)
    }

    // ── RevenueRepositoryV2 ───────────────────────────────────────────────────

    @Test
    fun `observeRevenueMetrics combines 5 flows correctly`() = runTest {
        val trend = listOf(
            DailyRevenueTrendV2("2026-03-01", 50000L, 2, 1),
            DailyRevenueTrendV2("2026-03-02", 75000L, 3, 2)
        )
        every { daoV2.observeMTDRevenue(businessId) } returns flowOf(125000L)
        every { daoV2.observeYTDRevenue(businessId) } returns flowOf(500000L)
        every { daoV2.observeWeeklyRevenue(businessId) } returns flowOf(100000L)
        every { daoV2.observeTotalPaidRevenue(businessId) } returns flowOf(500000L)
        every { daoV2.observeLast30DaysRevenueTrend(businessId) } returns flowOf(trend)

        val metrics = revenueRepository.observeRevenueMetrics(businessId).first()

        assertEquals(125000L, metrics.mtdRevenue)
        assertEquals(500000L, metrics.ytdRevenue)
        assertEquals(100000L, metrics.weeklyRevenue)
        assertEquals(500000L, metrics.totalPaidRevenue)
        assertEquals(2, metrics.dailyTrend.size)
        assertEquals("2026-03-01", metrics.dailyTrend[0].date)
        assertEquals(50000L, metrics.dailyTrend[0].revenueCents)
    }

    @Test
    fun `observeRevenueMetrics returns zero values when no data`() = runTest {
        every { daoV2.observeMTDRevenue(businessId) } returns flowOf(0L)
        every { daoV2.observeYTDRevenue(businessId) } returns flowOf(0L)
        every { daoV2.observeWeeklyRevenue(businessId) } returns flowOf(0L)
        every { daoV2.observeTotalPaidRevenue(businessId) } returns flowOf(0L)
        every { daoV2.observeLast30DaysRevenueTrend(businessId) } returns flowOf(emptyList())

        val metrics = revenueRepository.observeRevenueMetrics(businessId).first()

        assertEquals(0L, metrics.mtdRevenue)
        assertEquals(0L, metrics.ytdRevenue)
        assertEquals(emptyList(), metrics.dailyTrend)
    }

    // ── PaymentAnalyticsRepositoryV2 ─────────────────────────────────────────

    @Test
    fun `observePaymentMetrics maps status counts correctly`() = runTest {
        val statusCounts = listOf(
            InvoiceStatusCountV2("PAID", 10),
            InvoiceStatusCountV2("SENT", 3),
            InvoiceStatusCountV2("OVERDUE", 2),
            InvoiceStatusCountV2("DRAFT", 1)
        )
        every { daoV2.observeOutstandingAmount(businessId) } returns flowOf(300000L)
        every { daoV2.observeCollectedAmount(businessId) } returns flowOf(1000000L)
        every { daoV2.observeInvoiceCountByStatus(businessId) } returns flowOf(statusCounts)
        every { daoV2.observeOverdueCount(businessId) } returns flowOf(2)
        every { daoV2.observeAverageDaysToPayment(businessId) } returns flowOf(25.5)

        val metrics = paymentRepository.observePaymentMetrics(businessId).first()

        assertEquals(16, metrics.totalInvoices)
        assertEquals(10, metrics.paidCount)
        assertEquals(3, metrics.sentCount)
        assertEquals(2, metrics.overdueCount)
        assertEquals(1, metrics.draftCount)
        assertEquals(300000L, metrics.outstandingAmount)
        assertEquals(1000000L, metrics.collectedAmount)
        assertEquals(25.5, metrics.averageDaysToPayment)
    }

    // ── RiskAnalyticsRepositoryV2 ─────────────────────────────────────────────

    @Test
    fun `observeRiskMetrics maps risk tiers correctly`() = runTest {
        every { daoV2.observeHighRiskInvoiceCount(businessId) } returns flowOf(3)
        every { daoV2.observeAtRiskInvoiceCount(businessId) } returns flowOf(5)
        every { daoV2.observeHealthyInvoiceCount(businessId) } returns flowOf(42)
        every { daoV2.observeOverdueCount(businessId) } returns flowOf(8)
        every { daoV2.observeOutstandingAmount(businessId) } returns flowOf(500000L)

        val metrics = riskRepository.observeRiskMetrics(businessId).first()

        assertEquals(3, metrics.highRiskCount)
        assertEquals(5, metrics.atRiskCount)
        assertEquals(42, metrics.healthyCount)
        assertEquals(8, metrics.overdueCount)
        assertEquals(500000L, metrics.totalOutstandingCents)
    }

    @Test
    fun `observeRiskMetrics returns zeros when no invoices`() = runTest {
        every { daoV2.observeHighRiskInvoiceCount(businessId) } returns flowOf(0)
        every { daoV2.observeAtRiskInvoiceCount(businessId) } returns flowOf(0)
        every { daoV2.observeHealthyInvoiceCount(businessId) } returns flowOf(0)
        every { daoV2.observeOverdueCount(businessId) } returns flowOf(0)
        every { daoV2.observeOutstandingAmount(businessId) } returns flowOf(0L)

        val metrics = riskRepository.observeRiskMetrics(businessId).first()

        assertEquals(0, metrics.highRiskCount)
        assertEquals(0, metrics.atRiskCount)
        assertEquals(0, metrics.healthyCount)
        assertEquals(0L, metrics.totalOutstandingCents)
    }
}
