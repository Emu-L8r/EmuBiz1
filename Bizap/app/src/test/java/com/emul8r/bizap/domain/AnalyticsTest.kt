package com.emul8r.bizap.domain

import com.emul8r.bizap.data.local.entities.InvoiceAnalyticsSnapshot
import com.emul8r.bizap.domain.analytics.AnalyticsCalculator
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for analytics calculations with precision fixes.
 */
class AnalyticsTest {

    @Test
    fun `test customer lifetime value calculation`() {
        val invoices = listOf(
            createSnapshot(1, 1000.0, true),
            createSnapshot(2, 500.0, true)
        )
        val ltv = AnalyticsCalculator.calculateCustomerLifetimeValue(invoices)
        assertEquals(1500.0, ltv)
    }

    @Test
    fun `test overdue percentage calculation`() {
        val invoices = listOf(
            createSnapshot(1, 1000.0, false, isOverdue = true),
            createSnapshot(2, 500.0, false, isOverdue = false),
            createSnapshot(3, 500.0, false, isOverdue = false)
        )
        val overduePercentage = AnalyticsCalculator.calculateOverduePercentage(invoices)
        // ✅ FIXED: Using 0.01 tolerance for Double precision
        assertEquals(0.33, overduePercentage, 0.01) 
    }

    @Test
    fun `test health score calculation`() {
        val score = AnalyticsCalculator.calculateHealthScore(
            totalRevenue = 100000.0,
            paidRate = 0.95,
            overduePercentage = 0.02,
            monthOverMonthGrowth = 0.10,
            activeCustomerCount = 50
        )
        assertTrue(score > 80)
        assertEquals("EXCELLENT", AnalyticsCalculator.determineHealthStatus(score))
    }

    private fun createSnapshot(id: Long, amount: Double, isPaid: Boolean, isOverdue: Boolean = false) = 
        InvoiceAnalyticsSnapshot(
            invoiceId = id,
            businessProfileId = 1,
            customerId = 1,
            customerName = "Test",
            invoiceNumber = "INV-$id",
            currencyCode = "AUD",
            subtotal = amount,
            taxAmount = 0.0,
            totalAmount = amount,
            status = if (isPaid) "PAID" else if (isOverdue) "OVERDUE" else "SENT",
            isPaid = isPaid,
            isOverdue = isOverdue,
            invoiceDateMs = System.currentTimeMillis(),
            createdAtMs = System.currentTimeMillis(),
            snapshotCreatedAtMs = System.currentTimeMillis(),
            daysPending = 0,
            lineItemCount = 1
        )
}
