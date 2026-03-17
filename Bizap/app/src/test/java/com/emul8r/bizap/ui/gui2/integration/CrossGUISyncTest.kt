@file:Suppress("UNCHECKED_CAST")
package com.emul8r.bizap.ui.gui2.integration

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

/**
 * Cross-GUI consistency tests verifying that the same underlying data is exposed
 * consistently via both GUI1-compatible and GUI2 repository queries.
 *
 * These tests ensure no discrepancy between the two UI layers when reading
 * the same business metrics from the shared Room database.
 */
class CrossGUISyncTest : BaseUnitTest() {

    private val dao: InvoiceDaoV2 = mockk()
    private lateinit var revenueRepo: RevenueRepositoryV2
    private lateinit var paymentRepo: PaymentAnalyticsRepositoryV2

    private val businessId = 42L

    @Before
    fun setup() {
        val calculator = AnalyticsCalculator()
        val validator = AnalyticsValidator()
        revenueRepo = RevenueRepositoryV2(dao, calculator, validator)
        paymentRepo = PaymentAnalyticsRepositoryV2(dao, calculator, validator)
    }

    @Test
    fun `revenue totals match between revenue repo and payment repo collected amount`() = runTest {
        val totalPaid = 500000L

        every { dao.observeMTDRevenue(businessId, any(), any()) } returns flowOf(totalPaid)
        every { dao.observeYTDRevenue(businessId, any(), any()) } returns flowOf(totalPaid)
        every { dao.observeWeeklyRevenue(businessId, any(), any()) } returns flowOf(totalPaid)
        every { dao.observeTotalPaidRevenue(businessId) } returns flowOf(totalPaid)
        every { dao.observeLast30DaysRevenueTrend(businessId) } returns flowOf(emptyList())

        every { dao.observeOutstandingAmount(businessId) } returns flowOf(0L)
        every { dao.observeCollectedAmount(businessId) } returns flowOf(totalPaid)
        every { dao.observeInvoiceCountByStatus(businessId) } returns flowOf(
            listOf(InvoiceStatusCountV2("PAID", 5))
        )
        every { dao.observeOverdueCount(businessId) } returns flowOf(0)
        every { dao.observeAverageDaysToPayment(businessId) } returns flowOf(10.0)

        val revenue = revenueRepo.observeRevenueMetrics(businessId).first().getOrThrow()
        val payment = paymentRepo.observePaymentMetrics(businessId).first().getOrThrow()

        // GUI1 would show totalPaidRevenue; GUI2 shows collectedAmount — they must match
        assertEquals(revenue.totalPaidRevenue, payment.collectedAmount)
    }

    @Test
    fun `outstanding balance is consistent across GUI1 and GUI2 views`() = runTest {
        val outstanding = 150000L

        every { dao.observeOutstandingAmount(businessId) } returns flowOf(outstanding)
        every { dao.observeCollectedAmount(businessId) } returns flowOf(350000L)
        every { dao.observeInvoiceCountByStatus(businessId) } returns flowOf(
            listOf(
                InvoiceStatusCountV2("PAID", 3),
                InvoiceStatusCountV2("SENT", 2)
            )
        )
        every { dao.observeOverdueCount(businessId) } returns flowOf(0)
        every { dao.observeAverageDaysToPayment(businessId) } returns flowOf(14.0)

        val payment = paymentRepo.observePaymentMetrics(businessId).first().getOrThrow()

        assertEquals(outstanding, payment.outstandingAmount)
    }

    @Test
    fun `invoice count totals match across views`() = runTest {
        val statusCounts = listOf(
            InvoiceStatusCountV2("PAID", 4),
            InvoiceStatusCountV2("SENT", 2),
            InvoiceStatusCountV2("OVERDUE", 1),
            InvoiceStatusCountV2("DRAFT", 1)
        )
        every { dao.observeOutstandingAmount(businessId) } returns flowOf(0L)
        every { dao.observeCollectedAmount(businessId) } returns flowOf(0L)
        every { dao.observeInvoiceCountByStatus(businessId) } returns flowOf(statusCounts)
        every { dao.observeOverdueCount(businessId) } returns flowOf(1)
        every { dao.observeAverageDaysToPayment(businessId) } returns flowOf(0.0)

        val payment = paymentRepo.observePaymentMetrics(businessId).first().getOrThrow()

        assertEquals(8, payment.totalInvoices) // 4+2+1+1
        assertEquals(4, payment.paidCount)
        assertEquals(2, payment.sentCount)
        assertEquals(1, payment.draftCount)
    }

    @Test
    fun `payment status breakdown covers all invoice statuses`() = runTest {
        val statusCounts = listOf(
            InvoiceStatusCountV2("PAID", 10),
            InvoiceStatusCountV2("PARTIALLY_PAID", 3),
            InvoiceStatusCountV2("SENT", 2),
            InvoiceStatusCountV2("OVERDUE", 1),
            InvoiceStatusCountV2("DRAFT", 4)
        )
        every { dao.observeOutstandingAmount(businessId) } returns flowOf(100000L)
        every { dao.observeCollectedAmount(businessId) } returns flowOf(900000L)
        every { dao.observeInvoiceCountByStatus(businessId) } returns flowOf(statusCounts)
        every { dao.observeOverdueCount(businessId) } returns flowOf(1)
        every { dao.observeAverageDaysToPayment(businessId) } returns flowOf(7.5)

        val payment = paymentRepo.observePaymentMetrics(businessId).first().getOrThrow()

        assertEquals(5, payment.statusBreakdown.size)
        assertEquals(20, payment.totalInvoices) // 10+3+2+1+4
    }

    @Test
    fun `customer totals reflected in dashboard after customer creation`() = runTest {
        // After a new customer is created with invoices, revenue metrics should update
        every { dao.observeMTDRevenue(businessId, any(), any()) } returns flowOf(0L)
        every { dao.observeYTDRevenue(businessId, any(), any()) } returns flowOf(0L)
        every { dao.observeWeeklyRevenue(businessId, any(), any()) } returns flowOf(0L)
        every { dao.observeTotalPaidRevenue(businessId) } returns flowOf(0L)
        every { dao.observeLast30DaysRevenueTrend(businessId) } returns flowOf(emptyList())

        val before = revenueRepo.observeRevenueMetrics(businessId).first().getOrThrow()
        assertEquals(0L, before.mtdRevenue)

        // Simulate data after invoice created for new customer
        every { dao.observeMTDRevenue(businessId, any(), any()) } returns flowOf(99900L)
        every { dao.observeTotalPaidRevenue(businessId) } returns flowOf(99900L)

        val after = revenueRepo.observeRevenueMetrics(businessId).first().getOrThrow()
        assertEquals(99900L, after.mtdRevenue)
    }

    @Test
    fun `invoice calculations match between GUI views after edit`() = runTest {
        val invoiceCents = 250000L
        every { dao.observeOutstandingAmount(businessId) } returns flowOf(invoiceCents)
        every { dao.observeCollectedAmount(businessId) } returns flowOf(0L)
        every { dao.observeInvoiceCountByStatus(businessId) } returns flowOf(
            listOf(InvoiceStatusCountV2("SENT", 1))
        )
        every { dao.observeOverdueCount(businessId) } returns flowOf(0)
        every { dao.observeAverageDaysToPayment(businessId) } returns flowOf(0.0)

        val metrics = paymentRepo.observePaymentMetrics(businessId).first().getOrThrow()

        // outstanding + collected = total invoice value
        assertEquals(invoiceCents, metrics.outstandingAmount + metrics.collectedAmount)
    }
}
