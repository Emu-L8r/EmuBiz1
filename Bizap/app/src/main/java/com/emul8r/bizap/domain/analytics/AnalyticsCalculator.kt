package com.emul8r.bizap.domain.analytics

import com.emul8r.bizap.data.local.entities.InvoiceAnalyticsSnapshot
import timber.log.Timber
import kotlin.math.round

/**
 * Calculates analytics metrics from raw data
 */
object AnalyticsCalculator {

    private const val TAG = "AnalyticsCalculator"

    /**
     * Calculate customer lifetime value
     */
    fun calculateCustomerLifetimeValue(
        invoices: List<InvoiceAnalyticsSnapshot>,
        paidOnly: Boolean = true
    ): Double {
        return if (paidOnly) {
            invoices.filter { it.isPaid }.sumOf { it.totalAmount }
        } else {
            invoices.sumOf { it.totalAmount }
        }
    }

    /**
     * Calculate average days to payment
     */
    fun calculateAverageDaysToPayment(
        invoices: List<InvoiceAnalyticsSnapshot>
    ): Int {
        val paidInvoices = invoices.filter { it.isPaid && it.paidAtMs != null }

        if (paidInvoices.isEmpty()) return 0

        val totalDays = paidInvoices.sumOf { invoice ->
            ((invoice.paidAtMs!! - invoice.invoiceDateMs) / (1000 * 60 * 60 * 24)).toInt()
        }

        return totalDays / paidInvoices.size
    }

    /**
     * Calculate payment rate
     */
    fun calculatePaymentRate(invoices: List<InvoiceAnalyticsSnapshot>): Double {
        if (invoices.isEmpty()) return 0.0

        val paidCount = invoices.count { it.isPaid }
        return round((paidCount.toDouble() / invoices.size) * 100) / 100
    }

    /**
     * Calculate business health score
     */
    fun calculateHealthScore(
        totalRevenue: Double,
        paidRate: Double,
        overduePercentage: Double,
        monthOverMonthGrowth: Double,
        activeCustomerCount: Int
    ): Int {
        var score = 50 // Start at 50

        // Payment health (40 points max)
        score += (paidRate * 40).toInt()

        // Overdue penalty (20 points)
        score -= (overduePercentage * 20).toInt()

        // Growth bonus (10 points)
        if (monthOverMonthGrowth > 0.05) score += 10

        // Customer diversity (10 points)
        if (activeCustomerCount > 10) score += 10

        // Cap at 0-100
        return score.coerceIn(0, 100)
    }

    /**
     * Determine health status
     */
    fun determineHealthStatus(score: Int): String {
        return when {
            score < 30 -> "CRITICAL"
            score < 60 -> "CAUTION"
            score < 80 -> "NORMAL"
            else -> "EXCELLENT"
        }
    }

    /**
     * Calculate month over month growth
     */
    fun calculateMonthOverMonthGrowth(
        thisMonthRevenue: Double,
        lastMonthRevenue: Double
    ): Double {
        if (lastMonthRevenue == 0.0) return 0.0

        return round(((thisMonthRevenue - lastMonthRevenue) / lastMonthRevenue) * 100) / 100
    }

    /**
     * Calculate overdue percentage
     */
    fun calculateOverduePercentage(invoices: List<InvoiceAnalyticsSnapshot>): Double {
        if (invoices.isEmpty()) return 0.0

        val overdueCount = invoices.count { it.isOverdue }
        return round((overdueCount.toDouble() / invoices.size) * 100) / 100
    }
}

