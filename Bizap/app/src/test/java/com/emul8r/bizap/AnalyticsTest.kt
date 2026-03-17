package com.emul8r.bizap

import com.emul8r.bizap.data.model.*
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for analytics data models and ViewModel logic.
 */
class AnalyticsTest {

    // ═════════════════════════════════════════════════════════════════
    // TEST 1: Daily Revenue Model
    // ═════════════════════════════════════════════════════════════════

    @Test
    fun testDailyRevenueCreation() {
        val dateMillis = 1710604800000L  // 2026-03-16 00:00:00 UTC
        val dailyRevenue = DailyRevenue(
            businessId = 1L,
            date = dateMillis,
            invoicedCents = 50000,  // $500
            paidCents = 30000,      // $300
            invoiceCount = 5,
            paidCount = 3
        )

        assertEquals(1L, dailyRevenue.businessId)
        assertEquals(dateMillis, dailyRevenue.date)
        assertEquals(50000, dailyRevenue.invoicedCents)
        assertEquals(30000, dailyRevenue.paidCents)
        assertEquals(5, dailyRevenue.invoiceCount)
        assertEquals(3, dailyRevenue.paidCount)
    }

    @Test
    fun testDailyRevenueWithDefaults() {
        val currentTimeMillis = System.currentTimeMillis()
        val dailyRevenue = DailyRevenue(
            businessId = 1L,
            date = currentTimeMillis,
            invoicedCents = 100000,
            paidCents = 100000,
            invoiceCount = 10,
            paidCount = 10
        )

        assertEquals(1L, dailyRevenue.businessId)
        assertEquals(currentTimeMillis, dailyRevenue.date)
        assertEquals(100000, dailyRevenue.invoicedCents)
        assertEquals(100000, dailyRevenue.paidCents)
    }

    // ═════════════════════════════════════════════════════════════════
    // TEST 2: Customer Revenue Model
    // ═════════════════════════════════════════════════════════════════

    @Test
    fun testCustomerRevenueCreation() {
        val dateMillis = 1710513600000L  // 2026-03-15 00:00:00 UTC
        val customerRevenue = CustomerRevenue(
            customerId = 100L,
            customerName = "Acme Corp",
            totalRevenueCents = 500000,  // $5000
            invoiceCount = 10,
            lastPaymentDate = dateMillis
        )

        assertEquals(100L, customerRevenue.customerId)
        assertEquals("Acme Corp", customerRevenue.customerName)
        assertEquals(500000, customerRevenue.totalRevenueCents)
        assertEquals(10, customerRevenue.invoiceCount)
        assertEquals(dateMillis, customerRevenue.lastPaymentDate)
    }

    @Test
    fun testCustomerRevenueWithoutPaymentDate() {
        val customerRevenue = CustomerRevenue(
            customerId = 200L,
            customerName = "New Customer",
            totalRevenueCents = 100000,
            invoiceCount = 2,
            lastPaymentDate = null
        )

        assertNull(customerRevenue.lastPaymentDate)
    }

    // ═════════════════════════════════════════════════════════════════
    // TEST 3: Invoice Velocity Model
    // ═════════════════════════════════════════════════════════════════

    @Test
    fun testInvoiceVelocityMetrics() {
        val velocity = InvoiceVelocity(
            businessId = 1L,
            date = System.currentTimeMillis(),
            avgDaysFromCreationToSent = 2.5,
            invoicesCreatedCount = 10,
            invoicesSentCount = 8,
            invoicesInDraftCount = 2
        )

        assertEquals(1L, velocity.businessId)
        assertEquals(2.5, velocity.avgDaysFromCreationToSent, 0.1)
        assertEquals(10, velocity.invoicesCreatedCount)
        assertEquals(8, velocity.invoicesSentCount)
        assertEquals(2, velocity.invoicesInDraftCount)
    }

    @Test
    fun testInvoiceVelocityWithZeroVelocity() {
        val velocity = InvoiceVelocity(
            businessId = 1L,
            date = System.currentTimeMillis(),
            avgDaysFromCreationToSent = 0.0,  // Instant
            invoicesCreatedCount = 5,
            invoicesSentCount = 5,
            invoicesInDraftCount = 0
        )

        assertEquals(0.0, velocity.avgDaysFromCreationToSent, 0.1)
    }

    // ═════════════════════════════════════════════════════════════════
    // TEST 4: Payment Metrics & Cash Flow
    // ═════════════════════════════════════════════════════════════════

    @Test
    fun testPaymentMetrics() {
        val metrics = PaymentMetrics(
            averageDaysToPayment = 14.5,
            totalOutstandingCents = 200000,
            totalCollectedCents = 1000000,
            overdueInvoiceCount = 3,
            overdueAmountCents = 50000
        )

        assertEquals(14.5, metrics.averageDaysToPayment, 0.1)
        assertEquals(200000, metrics.totalOutstandingCents)
        assertEquals(1000000, metrics.totalCollectedCents)
        assertEquals(3, metrics.overdueInvoiceCount)
        assertEquals(50000, metrics.overdueAmountCents)
    }

    @Test
    fun testCashFlowTrendPoint() {
        val dateMillis = 1710604800000L  // 2026-03-16 00:00:00 UTC
        val trendPoint = CashFlowTrendPoint(
            date = dateMillis,
            invoicedCents = 100000,
            paidCents = 80000
        )

        assertEquals(dateMillis, trendPoint.date)
        assertEquals(100000, trendPoint.invoicedCents)
        assertEquals(80000, trendPoint.paidCents)
        assertEquals(-20000, trendPoint.netCents)  // More invoiced than paid
    }

    @Test
    fun testCashFlowPositiveNet() {
        val trendPoint = CashFlowTrendPoint(
            date = System.currentTimeMillis(),
            invoicedCents = 50000,
            paidCents = 100000  // More paid than invoiced
        )

        assertEquals(50000, trendPoint.netCents)  // Positive net cash
    }

    // ═════════════════════════════════════════════════════════════════
    // TEST 5: Top Customer Metrics & Formatting
    // ═════════════════════════════════════════════════════════════════

    @Test
    fun testTopCustomerMetric() {
        val customer = TopCustomerMetric(
            customerId = 1L,
            customerName = "Best Client",
            revenueCents = 500000,  // $5000
            percentageOfTotal = 45.5,
            invoiceCount = 20
        )

        assertEquals(1L, customer.customerId)
        assertEquals("Best Client", customer.customerName)
        assertEquals(500000, customer.revenueCents)
        assertEquals(45.5, customer.percentageOfTotal, 0.1)
        assertEquals(20, customer.invoiceCount)
    }

    @Test
    fun testTopCustomerMetricFormatting() {
        val customer = TopCustomerMetric(
            customerId = 1L,
            customerName = "Client A",
            revenueCents = 123456,
            percentageOfTotal = 32.7,
            invoiceCount = 15
        )

        assertEquals("\$1234.56", customer.revenueFormatted)
        assertEquals("32.7%", customer.percentageFormatted)
    }

    @Test
    fun testTopCustomerMetricWith100Percent() {
        val customer = TopCustomerMetric(
            customerId = 1L,
            customerName = "Only Client",
            revenueCents = 1000000,
            percentageOfTotal = 100.0,
            invoiceCount = 50
        )

        assertEquals("100.0%", customer.percentageFormatted)
    }

    // ═════════════════════════════════════════════════════════════════
    // TEST 6: Days to Pay Metric
    // ═════════════════════════════════════════════════════════════════

    @Test
    fun testDaysToPayMetric() {
        val dateMillis = 1709251200000L  // 2026-02-28 00:00:00 UTC
        val metric = DaysToPayMetric(
            date = dateMillis,
            averageDaysToPayment = 14.5
        )

        assertEquals(dateMillis, metric.date)
        assertEquals(14.5, metric.averageDaysToPayment, 0.1)
    }

    @Test
    fun testDaysToPayMetricFormatting() {
        val metric = DaysToPayMetric(
            date = System.currentTimeMillis(),
            averageDaysToPayment = 12.7
        )

        assertEquals("12.7 days", metric.averageFormatted)
    }

    @Test
    fun testDaysToPayMetricWithWholeNumber() {
        val metric = DaysToPayMetric(
            date = System.currentTimeMillis(),
            averageDaysToPayment = 10.0
        )

        assertEquals("10.0 days", metric.averageFormatted)
    }

    // ═════════════════════════════════════════════════════════════════
    // TEST 7: Combined Analytics Data
    // ═════════════════════════════════════════════════════════════════

    @Test
    fun testAnalyticsDataAggregation() {
        val date1 = 1710345600000L  // 2026-03-14
        val date2 = 1710432000000L  // 2026-03-15
        val date3 = 1710518400000L  // 2026-03-16

        val trendPoints = listOf(
            CashFlowTrendPoint(date1, 50000, 40000),
            CashFlowTrendPoint(date2, 60000, 50000),
            CashFlowTrendPoint(date3, 70000, 60000)
        )

        val topCustomers = listOf(
            TopCustomerMetric(1L, "Client A", 500000, 50.0, 20),
            TopCustomerMetric(2L, "Client B", 300000, 30.0, 15),
            TopCustomerMetric(3L, "Client C", 200000, 20.0, 10)
        )

        val paymentMetrics = PaymentMetrics(
            averageDaysToPayment = 14.0,
            totalOutstandingCents = 100000,
            totalCollectedCents = 1000000,
            overdueInvoiceCount = 2,
            overdueAmountCents = 50000
        )

        val analyticsData = AnalyticsData(
            cashFlowTrend = trendPoints,
            averageDaysToPayTrend = emptyList(),
            topCustomerMetrics = topCustomers,
            currentAverageDaysToPayment = 14.0,
            totalRevenue = 1000000,
            paymentMetrics = paymentMetrics
        )

        assertEquals(3, analyticsData.cashFlowTrend.size)
        assertEquals(3, analyticsData.topCustomerMetrics.size)
        assertEquals(14.0, analyticsData.currentAverageDaysToPayment, 0.1)
        assertEquals(1000000, analyticsData.totalRevenue)
    }
}

