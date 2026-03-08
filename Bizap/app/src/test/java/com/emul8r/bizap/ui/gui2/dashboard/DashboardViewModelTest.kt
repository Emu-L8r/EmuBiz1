package com.emul8r.bizap.ui.gui2.dashboard

import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.data.local.dao.InvoiceDaoV2
import com.emul8r.bizap.data.local.entities.InvoiceStatusCountV2
import com.emul8r.bizap.data.repository.gui2.BusinessContextRepositoryV2
import com.emul8r.bizap.data.repository.gui2.PaymentAnalyticsRepositoryV2
import com.emul8r.bizap.data.repository.gui2.RevenueRepositoryV2
import com.emul8r.bizap.data.repository.gui2.RiskAnalyticsRepositoryV2
import com.emul8r.bizap.domain.model.gui2.RevenueMetricsV2
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Unit tests for the Dashboard layer.
 *
 * These tests exercise the repository layer that the DashboardViewModelV2
 * depends on, verifying that metrics are combined correctly.
 */
class DashboardViewModelTest : BaseUnitTest() {

    private val dao: InvoiceDaoV2 = mockk()
    private lateinit var revenueRepository: RevenueRepositoryV2
    private lateinit var paymentRepository: PaymentAnalyticsRepositoryV2
    private lateinit var riskRepository: RiskAnalyticsRepositoryV2

    private val businessId = 1L

    @Before
    fun setUp() {
        revenueRepository = RevenueRepositoryV2(dao)
        paymentRepository = PaymentAnalyticsRepositoryV2(dao)
        riskRepository = RiskAnalyticsRepositoryV2(dao)
    }

    // ── loadMetrics_Success ───────────────────────────────────────────────────

    @Test
    fun `loadMetrics_Success - revenue metrics load with correct values`() = runTest {
        every { dao.observeMTDRevenue(businessId) } returns flowOf(150000L)
        every { dao.observeYTDRevenue(businessId) } returns flowOf(500000L)
        every { dao.observeWeeklyRevenue(businessId) } returns flowOf(50000L)
        every { dao.observeTotalPaidRevenue(businessId) } returns flowOf(450000L)
        every { dao.observeLast30DaysRevenueTrend(businessId) } returns flowOf(emptyList())

        val metrics = revenueRepository.observeRevenueMetrics(businessId).first()

        assertEquals(150000L, metrics.mtdRevenue)
        assertEquals(500000L, metrics.ytdRevenue)
        assertEquals(50000L, metrics.weeklyRevenue)
        assertEquals(450000L, metrics.totalPaidRevenue)
    }

    @Test
    fun `loadMetrics_Success - zero revenue when no invoices exist`() = runTest {
        every { dao.observeMTDRevenue(businessId) } returns flowOf(0L)
        every { dao.observeYTDRevenue(businessId) } returns flowOf(0L)
        every { dao.observeWeeklyRevenue(businessId) } returns flowOf(0L)
        every { dao.observeTotalPaidRevenue(businessId) } returns flowOf(0L)
        every { dao.observeLast30DaysRevenueTrend(businessId) } returns flowOf(emptyList())

        val metrics = revenueRepository.observeRevenueMetrics(businessId).first()

        assertEquals(0L, metrics.mtdRevenue)
        assertEquals(0L, metrics.ytdRevenue)
        assertTrue(metrics.dailyTrend.isEmpty())
    }

    // ── metricsUpdate_OnDataChange ────────────────────────────────────────────

    @Test
    fun `metricsUpdate_OnDataChange - payment metrics reflect outstanding invoices`() = runTest {
        val statusCounts = listOf(
            InvoiceStatusCountV2(status = "SENT", count = 3, totalAmount = 30000L),
            InvoiceStatusCountV2(status = "PAID", count = 5, totalAmount = 50000L)
        )
        every { dao.observeOutstandingAmount(businessId) } returns flowOf(30000L)
        every { dao.observeCollectedAmount(businessId) } returns flowOf(50000L)
        every { dao.observeInvoiceCountByStatus(businessId) } returns flowOf(statusCounts)
        every { dao.observeOverdueCount(businessId) } returns flowOf(0)
        every { dao.observeAverageDaysToPayment(businessId) } returns flowOf(15.0)

        val metrics = paymentRepository.observePaymentMetrics(businessId).first()

        assertEquals(30000L, metrics.outstandingAmount)
        assertEquals(50000L, metrics.collectedAmount)
    }

    @Test
    fun `metricsUpdate_OnDataChange - risk metrics reflect overdue invoices`() = runTest {
        every { dao.observeHighRiskInvoiceCount(businessId) } returns flowOf(1)
        every { dao.observeAtRiskInvoiceCount(businessId) } returns flowOf(1)
        every { dao.observeHealthyInvoiceCount(businessId) } returns flowOf(3)
        every { dao.observeOverdueCount(businessId) } returns flowOf(2)
        every { dao.observeOutstandingAmount(businessId) } returns flowOf(25000L)

        val metrics = riskRepository.observeRiskMetrics(businessId).first()

        assertEquals(2, metrics.overdueCount)
        assertEquals(25000L, metrics.totalOutstandingCents)
    }

    @Test
    fun `metricsUpdate_OnDataChange - revenue metrics not null after loading`() = runTest {
        every { dao.observeMTDRevenue(businessId) } returns flowOf(100000L)
        every { dao.observeYTDRevenue(businessId) } returns flowOf(800000L)
        every { dao.observeWeeklyRevenue(businessId) } returns flowOf(25000L)
        every { dao.observeTotalPaidRevenue(businessId) } returns flowOf(700000L)
        every { dao.observeLast30DaysRevenueTrend(businessId) } returns flowOf(emptyList())

        val metrics = revenueRepository.observeRevenueMetrics(businessId).first()

        assertNotNull(metrics)
    }
}
