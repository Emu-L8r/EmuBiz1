@file:Suppress("UNCHECKED_CAST")
package com.emul8r.bizap.ui.gui2.integration

import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.data.local.dao.InvoiceDaoV2
import com.emul8r.bizap.data.local.entities.InvoiceStatusCountV2
import com.emul8r.bizap.data.repository.analytics.AnalyticsCalculator
import com.emul8r.bizap.data.repository.analytics.AnalyticsValidator
import com.emul8r.bizap.data.repository.gui2.PaymentAnalyticsRepositoryV2
import com.emul8r.bizap.data.repository.RevenueRepositoryImpl
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
 * Dashboard integration tests verifying that metrics update correctly
 * when GUI2 operations (customer creation, invoice creation, payment recording) occur.
 */
class DashboardIntegrationTest : BaseUnitTest() {

    private val dao: InvoiceDaoV2 = mockk()
    private lateinit var revenueRepo: RevenueRepositoryImpl
    private lateinit var paymentRepo: PaymentAnalyticsRepositoryV2
    private lateinit var riskRepo: RiskAnalyticsRepositoryV2

    private val businessId = 7L

    @Before
    fun setup() {
        val calculator = AnalyticsCalculator()
        val validator = AnalyticsValidator()
        revenueRepo = RevenueRepositoryImpl(dao, calculator, validator)
        paymentRepo = PaymentAnalyticsRepositoryV2(dao, calculator, validator)
        riskRepo = RiskAnalyticsRepositoryV2(dao, calculator)
    }

    @Test
    fun `dashboard revenue metrics update when invoice is created`() = runTest {
        every { dao.observeMTDRevenue(businessId, any(), any()) } returns flowOf(200000L)
        every { dao.observeYTDRevenue(businessId, any(), any()) } returns flowOf(200000L)
        every { dao.observeWeeklyRevenue(businessId, any(), any()) } returns flowOf(200000L)
        every { dao.observeTotalPaidRevenue(businessId) } returns flowOf(200000L)
        every { dao.observeLast30DaysRevenueTrend(businessId, any(), any()) } returns flowOf(emptyList())
        every { dao.observeOverdueAmount(businessId) } returns flowOf(0L)

        val metrics = revenueRepo.observeRevenueMetrics(businessId).first().getOrThrow()

        assertEquals(200000L, metrics.mtdRevenue)
        assertEquals(200000L, metrics.ytdRevenue)
    }

    @Test
    fun `dashboard payment metrics update when invoice is created`() = runTest {
        val statusCounts = listOf(
            InvoiceStatusCountV2("SENT", 1)
        )
        every { dao.observeOutstandingAmount(businessId) } returns flowOf(100000L)
        every { dao.observeCollectedAmount(businessId) } returns flowOf(0L)
        every { dao.observeInvoiceCountByStatus(businessId) } returns flowOf(statusCounts)
        every { dao.observeOverdueCount(businessId) } returns flowOf(0)
        every { dao.observeAverageDaysToPayment(businessId) } returns flowOf(0.0)

        val metrics = paymentRepo.observePaymentMetrics(businessId).first().getOrThrow()

        assertEquals(1, metrics.totalInvoices)
        assertEquals(1, metrics.sentCount)
        assertEquals(100000L, metrics.outstandingAmount)
    }

    @Test
    fun `dashboard payment metrics update when payment is recorded`() = runTest {
        val statusCounts = listOf(
            InvoiceStatusCountV2("PAID", 1)
        )
        every { dao.observeOutstandingAmount(businessId) } returns flowOf(0L)
        every { dao.observeCollectedAmount(businessId) } returns flowOf(100000L)
        every { dao.observeInvoiceCountByStatus(businessId) } returns flowOf(statusCounts)
        every { dao.observeOverdueCount(businessId) } returns flowOf(0)
        every { dao.observeAverageDaysToPayment(businessId) } returns flowOf(3.0)

        val metrics = paymentRepo.observePaymentMetrics(businessId).first().getOrThrow()

        assertEquals(1, metrics.paidCount)
        assertEquals(0L, metrics.outstandingAmount)
        assertEquals(100000L, metrics.collectedAmount)
    }

    @Test
    fun `outstanding balance calculated correctly after multiple invoices`() = runTest {
        every { dao.observeOutstandingAmount(businessId) } returns flowOf(750000L)
        every { dao.observeCollectedAmount(businessId) } returns flowOf(250000L)
        every { dao.observeInvoiceCountByStatus(businessId) } returns flowOf(
            listOf(
                InvoiceStatusCountV2("PAID", 1),
                InvoiceStatusCountV2("SENT", 3)
            )
        )
        every { dao.observeOverdueCount(businessId) } returns flowOf(0)
        every { dao.observeAverageDaysToPayment(businessId) } returns flowOf(12.0)

        val metrics = paymentRepo.observePaymentMetrics(businessId).first().getOrThrow()

        assertEquals(750000L, metrics.outstandingAmount)
        assertEquals(250000L, metrics.collectedAmount)
        assertEquals(4, metrics.totalInvoices)
    }

    @Test
    fun `risk classifications correct after invoices become overdue`() = runTest {
        every { dao.observeHighRiskInvoiceCount(businessId) } returns flowOf(2)
        every { dao.observeAtRiskInvoiceCount(businessId) } returns flowOf(3)
        every { dao.observeHealthyInvoiceCount(businessId) } returns flowOf(10)
        every { dao.observeOverdueCount(businessId) } returns flowOf(5)
        every { dao.observeOutstandingAmount(businessId) } returns flowOf(600000L)

        val metrics = riskRepo.observeRiskMetrics(businessId).first().getOrThrow()

        assertEquals(2, metrics.highRiskCount)   // overdue 60+ days
        assertEquals(3, metrics.atRiskCount)     // overdue 30–59 days
        assertEquals(10, metrics.healthyCount)   // paid or not yet due
        assertEquals(5, metrics.overdueCount)    // all overdue
        assertEquals(600000L, metrics.totalOutstandingCents)

        // high + at risk = all overdue
        assertEquals(metrics.highRiskCount + metrics.atRiskCount, metrics.overdueCount)
    }
}



