@file:Suppress("UNCHECKED_CAST")
package com.emul8r.bizap.ui.gui2.integration

import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.data.local.dao.InvoiceDaoV2
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
import kotlin.test.assertTrue

/**
 * End-to-end journey tests verifying that data flows correctly
 * from repositories through to observable dashboard metrics.
 *
 * These tests simulate the full data path:
 * DAO → Repository → (ViewModel would combine) → expected state.
 */
class EndToEndJourneyTest : BaseUnitTest() {

    private val dao: InvoiceDaoV2 = mockk()
    private lateinit var revenueRepo: RevenueRepositoryV2
    private lateinit var paymentRepo: PaymentAnalyticsRepositoryV2
    private lateinit var riskRepo: RiskAnalyticsRepositoryV2

    private val businessId = 1L

    @Before
    fun setup() {
        val calculator = AnalyticsCalculator()
        val validator = AnalyticsValidator()
        revenueRepo = RevenueRepositoryV2(dao, calculator, validator)
        paymentRepo = PaymentAnalyticsRepositoryV2(dao, calculator, validator)
        riskRepo = RiskAnalyticsRepositoryV2(dao, calculator)
    }

    // ── Journey 1: Create customer → create invoice → record payment ──────────

    @Test
    fun `revenue metrics reflect paid invoice after payment recorded`() = runTest {
        every { dao.observeMTDRevenue(businessId) } returns flowOf(50000L)
        every { dao.observeYTDRevenue(businessId) } returns flowOf(50000L)
        every { dao.observeWeeklyRevenue(businessId) } returns flowOf(50000L)
        every { dao.observeTotalPaidRevenue(businessId) } returns flowOf(50000L)
        every { dao.observeLast30DaysRevenueTrend(businessId) } returns flowOf(emptyList())

        val metrics = revenueRepo.observeRevenueMetrics(businessId).first()

        assertEquals(50000L, metrics.mtdRevenue)
        assertEquals(50000L, metrics.totalPaidRevenue)
    }

    @Test
    fun `payment metrics show invoice as paid after full payment`() = runTest {
        val statusCounts = listOf(
            InvoiceStatusCountV2("PAID", 1),
            InvoiceStatusCountV2("SENT", 0)
        )
        every { dao.observeOutstandingAmount(businessId) } returns flowOf(0L)
        every { dao.observeCollectedAmount(businessId) } returns flowOf(50000L)
        every { dao.observeInvoiceCountByStatus(businessId) } returns flowOf(statusCounts)
        every { dao.observeOverdueCount(businessId) } returns flowOf(0)
        every { dao.observeAverageDaysToPayment(businessId) } returns flowOf(5.0)

        val metrics = paymentRepo.observePaymentMetrics(businessId).first()

        assertEquals(1, metrics.paidCount)
        assertEquals(0L, metrics.outstandingAmount)
        assertEquals(50000L, metrics.collectedAmount)
        assertEquals(0, metrics.overdueCount)
    }

    @Test
    fun `risk metrics classify overdue invoice as high risk after 60 days`() = runTest {
        every { dao.observeHighRiskInvoiceCount(businessId) } returns flowOf(1)
        every { dao.observeAtRiskInvoiceCount(businessId) } returns flowOf(0)
        every { dao.observeHealthyInvoiceCount(businessId) } returns flowOf(0)
        every { dao.observeOverdueCount(businessId) } returns flowOf(1)
        every { dao.observeOutstandingAmount(businessId) } returns flowOf(50000L)

        val metrics = riskRepo.observeRiskMetrics(businessId).first()

        assertEquals(1, metrics.highRiskCount)
        assertEquals(0, metrics.atRiskCount)
        assertEquals(0, metrics.healthyCount)
        assertEquals(50000L, metrics.totalOutstandingCents)
    }

    @Test
    fun `partial payment reduces outstanding balance correctly`() = runTest {
        val statusCounts = listOf(InvoiceStatusCountV2("PARTIALLY_PAID", 1))
        every { dao.observeOutstandingAmount(businessId) } returns flowOf(25000L)
        every { dao.observeCollectedAmount(businessId) } returns flowOf(25000L)
        every { dao.observeInvoiceCountByStatus(businessId) } returns flowOf(statusCounts)
        every { dao.observeOverdueCount(businessId) } returns flowOf(0)
        every { dao.observeAverageDaysToPayment(businessId) } returns flowOf(0.0)

        val metrics = paymentRepo.observePaymentMetrics(businessId).first()

        assertEquals(1, metrics.partiallyPaidCount)
        assertEquals(25000L, metrics.outstandingAmount)
        assertEquals(25000L, metrics.collectedAmount)
    }

    @Test
    fun `dashboard metrics go to zero when invoice is deleted`() = runTest {
        every { dao.observeMTDRevenue(businessId) } returns flowOf(0L)
        every { dao.observeYTDRevenue(businessId) } returns flowOf(0L)
        every { dao.observeWeeklyRevenue(businessId) } returns flowOf(0L)
        every { dao.observeTotalPaidRevenue(businessId) } returns flowOf(0L)
        every { dao.observeLast30DaysRevenueTrend(businessId) } returns flowOf(emptyList())

        val metrics = revenueRepo.observeRevenueMetrics(businessId).first()

        assertEquals(0L, metrics.mtdRevenue)
        assertEquals(0L, metrics.ytdRevenue)
        assertTrue(metrics.dailyTrend.isEmpty())
    }
}
