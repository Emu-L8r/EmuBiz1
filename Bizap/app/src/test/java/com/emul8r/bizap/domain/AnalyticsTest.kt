package com.emul8r.bizap.domain

import com.emul8r.bizap.data.local.entities.InvoiceAnalyticsSnapshot
import com.emul8r.bizap.domain.analytics.AnalyticsCalculator
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for analytics calculations
 */
class AnalyticsTest {

    // ==================== CUSTOMER VALUE TESTS ====================

    @Test
    fun `test customer lifetime value calculation`() {
        // Arrange
        val invoices = listOf(
            InvoiceAnalyticsSnapshot(
                invoiceId = 1,
                businessProfileId = 1,
                customerId = 1,
                customerName = "Test Customer",
                invoiceNumber = "INV-001",
                currencyCode = "AUD",
                subtotal = 1000.0,
                taxAmount = 100.0,
                totalAmount = 1100.0,
                status = "PAID",
                isPaid = true,
                isOverdue = false,
                invoiceDateMs = System.currentTimeMillis(),
                createdAtMs = System.currentTimeMillis()
            ),
            InvoiceAnalyticsSnapshot(
                invoiceId = 2,
                businessProfileId = 1,
                customerId = 1,
                customerName = "Test Customer",
                invoiceNumber = "INV-002",
                currencyCode = "AUD",
                subtotal = 500.0,
                taxAmount = 50.0,
                totalAmount = 550.0,
                status = "PAID",
                isPaid = true,
                isOverdue = false,
                invoiceDateMs = System.currentTimeMillis(),
                createdAtMs = System.currentTimeMillis()
            )
        )

        // Act
        val ltv = AnalyticsCalculator.calculateCustomerLifetimeValue(invoices)

        // Assert
        assertEquals(1650.0, ltv)
    }

    @Test
    fun `test payment rate calculation`() {
        // Arrange
        val invoices = listOf(
            InvoiceAnalyticsSnapshot(
                invoiceId = 1,
                businessProfileId = 1,
                customerId = 1,
                customerName = "Test",
                invoiceNumber = "INV-001",
                currencyCode = "AUD",
                subtotal = 1000.0,
                taxAmount = 100.0,
                totalAmount = 1100.0,
                status = "PAID",
                isPaid = true,
                isOverdue = false,
                invoiceDateMs = System.currentTimeMillis(),
                createdAtMs = System.currentTimeMillis()
            ),
            InvoiceAnalyticsSnapshot(
                invoiceId = 2,
                businessProfileId = 1,
                customerId = 1,
                customerName = "Test",
                invoiceNumber = "INV-002",
                currencyCode = "AUD",
                subtotal = 500.0,
                taxAmount = 50.0,
                totalAmount = 550.0,
                status = "DRAFT",
                isPaid = false,
                isOverdue = false,
                invoiceDateMs = System.currentTimeMillis(),
                createdAtMs = System.currentTimeMillis()
            )
        )

        // Act
        val rate = AnalyticsCalculator.calculatePaymentRate(invoices)

        // Assert
        assertEquals(0.5, rate) // 50% paid
    }

    @Test
    fun `test overdue percentage calculation`() {
        // Arrange
        val invoices = listOf(
            InvoiceAnalyticsSnapshot(
                invoiceId = 1,
                businessProfileId = 1,
                customerId = 1,
                customerName = "Test",
                invoiceNumber = "INV-001",
                currencyCode = "AUD",
                subtotal = 1000.0,
                taxAmount = 100.0,
                totalAmount = 1100.0,
                status = "OVERDUE",
                isPaid = false,
                isOverdue = true,
                invoiceDateMs = System.currentTimeMillis(),
                createdAtMs = System.currentTimeMillis()
            ),
            InvoiceAnalyticsSnapshot(
                invoiceId = 2,
                businessProfileId = 1,
                customerId = 1,
                customerName = "Test",
                invoiceNumber = "INV-002",
                currencyCode = "AUD",
                subtotal = 500.0,
                taxAmount = 50.0,
                totalAmount = 550.0,
                status = "SENT",
                isPaid = false,
                isOverdue = false,
                invoiceDateMs = System.currentTimeMillis(),
                createdAtMs = System.currentTimeMillis()
            ),
            InvoiceAnalyticsSnapshot(
                invoiceId = 3,
                businessProfileId = 1,
                customerId = 1,
                customerName = "Test",
                invoiceNumber = "INV-003",
                currencyCode = "AUD",
                subtotal = 1000.0,
                taxAmount = 100.0,
                totalAmount = 1100.0,
                status = "PAID",
                isPaid = true,
                isOverdue = false,
                invoiceDateMs = System.currentTimeMillis(),
                createdAtMs = System.currentTimeMillis()
            )
        )

        // Act
        val overduePercentage = AnalyticsCalculator.calculateOverduePercentage(invoices)

        // Assert
        assertEquals(0.33, overduePercentage) // 33% overdue
    }

    // ==================== HEALTH SCORE TESTS ====================

    @Test
    fun `test health score calculation excellent`() {
        // Act
        val score = AnalyticsCalculator.calculateHealthScore(
            totalRevenue = 100000.0,
            paidRate = 0.95, // 95% paid
            overduePercentage = 0.02, // 2% overdue
            monthOverMonthGrowth = 0.10, // 10% growth
            activeCustomerCount = 50
        )

        // Assert
        assertTrue(score > 80)
        assertEquals("EXCELLENT", AnalyticsCalculator.determineHealthStatus(score))
    }

    @Test
    fun `test health score calculation critical`() {
        // Act
        val score = AnalyticsCalculator.calculateHealthScore(
            totalRevenue = 10000.0,
            paidRate = 0.05, // 5% paid (critically bad)
            overduePercentage = 0.95, // 95% overdue (critically bad)
            monthOverMonthGrowth = -0.50, // -50% growth
            activeCustomerCount = 1
        )

        // Assert
        // Score math: 50 + (0.05 * 40) - (0.95 * 20) = 50 + 2 - 19 = 33
        // Status: 33 is in CAUTION range (30-60)
        assertTrue(score <= 50, "Score should be low, was $score")
        assertEquals("CAUTION", AnalyticsCalculator.determineHealthStatus(score))
    }

    // ==================== GROWTH CALCULATION TESTS ====================

    @Test
    fun `test month over month growth calculation`() {
        // Arrange
        val thisMonth = 10000.0
        val lastMonth = 8000.0

        // Act
        val growth = AnalyticsCalculator.calculateMonthOverMonthGrowth(thisMonth, lastMonth)

        // Assert
        assertEquals(0.25, growth) // 25% growth
    }

    @Test
    fun `test month over month growth with zero last month`() {
        // Arrange
        val thisMonth = 10000.0
        val lastMonth = 0.0

        // Act
        val growth = AnalyticsCalculator.calculateMonthOverMonthGrowth(thisMonth, lastMonth)

        // Assert
        assertEquals(0.0, growth)
    }

    @Test
    fun `test payment rate with empty invoices`() {
        // Act
        val rate = AnalyticsCalculator.calculatePaymentRate(emptyList())

        // Assert
        assertEquals(0.0, rate)
    }

    @Test
    fun `test average days to payment calculation`() {
        // Arrange - 2 invoices paid 30 days after creation
        val now = System.currentTimeMillis()
        val thirtyDaysAgoMs = 30 * 24 * 60 * 60 * 1000L
        val thirtyDaysAgo = now - thirtyDaysAgoMs

        val invoices = listOf(
            InvoiceAnalyticsSnapshot(
                invoiceId = 1,
                businessProfileId = 1,
                customerId = 1,
                customerName = "Test",
                invoiceNumber = "INV-001",
                currencyCode = "AUD",
                subtotal = 1000.0,
                taxAmount = 100.0,
                totalAmount = 1100.0,
                status = "PAID",
                isPaid = true,
                isOverdue = false,
                invoiceDateMs = thirtyDaysAgo,
                createdAtMs = thirtyDaysAgo,
                paidAtMs = now // Paid 30 days later
            )
        )

        // Act
        val avgDays = AnalyticsCalculator.calculateAverageDaysToPayment(invoices)

        // Assert
        assertEquals(30, avgDays)
    }
}

