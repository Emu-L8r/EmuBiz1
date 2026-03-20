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
     * Calculate customer lifetime value (in cents)
     */
    fun calculateCustomerLifetimeValue(
        invoices: List<InvoiceAnalyticsSnapshot>,
        paidOnly: Boolean = true
    ): Long {
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
            val daysDiff = ((invoice.paidAtMs!! - invoice.invoiceDateMs) / (1000 * 60 * 60 * 24)).toInt()
            daysDiff.coerceIn(-10000, 10000)
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
        if (paidRate.isNaN() || paidRate.isInfinite() ||
            overduePercentage.isNaN() || overduePercentage.isInfinite() ||
            monthOverMonthGrowth.isNaN() || monthOverMonthGrowth.isInfinite()) {
            Timber.w("AnalyticsCalculator: Invalid input detected, returning default score")
            return 50
        }

        var score = 50 // Start at 50

        // Payment health (40 points max) - ensure paidRate is between 0-1
        score += (paidRate.coerceIn(0.0, 1.0) * 40).toInt()

        // Overdue penalty (20 points) - ensure overduePercentage is between 0-1
        score -= (overduePercentage.coerceIn(0.0, 1.0) * 20).toInt()

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

