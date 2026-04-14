package com.emul8r.bizap.domain.model.analytics

import java.time.LocalDate

/**
 * Analytics period types for different time windows.
 */
enum class AnalyticsPeriod {
    DAILY,       // Single day
    WEEKLY,      // Last 7 days
    FORTNIGHTLY, // Last 14 days
    MONTHLY,     // Last 30 days
    QUARTERLY,   // Last 90 days
    YEARLY       // Last 365 days
}

/**
 * Metrics for a specific analytics period.
 *
 * Aggregated revenue, invoice, and payment data for a given period.
 * Works with fresh data (no historical requirements).
 *
 * @param period The analytics period (DAILY, FORTNIGHTLY, QUARTERLY, etc.)
 * @param startDate First day of the period
 * @param endDate Last day of the period
 * @param totalRevenue Total revenue in cents for the period
 * @param paidAmount Amount paid/collected in cents
 * @param outstandingAmount Amount still unpaid in cents
 * @param invoiceCount Total number of invoices created
 * @param paidCount Number of paid invoices
 * @param averageInvoiceSize Average invoice amount in cents
 * @param paidPercentage Percentage of invoices that were paid (0-100)
 * @param onTimePaymentRate Percentage of invoices paid on time (0-100)
 * @param daysSinceLastInvoice Days since last invoice was created
 */
data class PeriodAnalytics(
    val period: AnalyticsPeriod,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val totalRevenue: Long,           // cents
    val paidAmount: Long,              // cents
    val outstandingAmount: Long,       // cents
    val invoiceCount: Int,
    val paidCount: Int,
    val averageInvoiceSize: Long,      // cents
    val paidPercentage: Float,         // 0-100
    val onTimePaymentRate: Float,      // 0-100
    val daysSinceLastInvoice: Int
) {
    /**
     * Returns formatted total revenue as dollars.
     */
    fun formattedTotalRevenue(): String = "$${totalRevenue / 100.0}"

    /**
     * Returns formatted paid amount as dollars.
     */
    fun formattedPaidAmount(): String = "$${paidAmount / 100.0}"

    /**
     * Returns formatted outstanding as dollars.
     */
    fun formattedOutstandingAmount(): String = "$${outstandingAmount / 100.0}"

    /**
     * Returns formatted average invoice size as dollars.
     */
    fun formattedAverageInvoiceSize(): String = "$${averageInvoiceSize / 100.0}"

    /**
     * Comparison vs previous period (for trend display).
     */
    fun getTrendIndicator(previousPeriod: PeriodAnalytics?): String {
        if (previousPeriod == null) return "→"
        return when {
            totalRevenue > previousPeriod.totalRevenue -> "↑"
            totalRevenue < previousPeriod.totalRevenue -> "↓"
            else -> "→"
        }
    }
}

/**
 * Simplified fortnightly metrics (14-day rolling window).
 * Optimized for dashboard display.
 */
data class ForthnightlyMetrics(
    val totalRevenue: Long,           // cents
    val invoiceCount: Int,
    val paidCount: Int,
    val averageInvoiceSize: Long,     // cents
    val paidPercentage: Float,        // 0-100
    val trend: String = "→"           // ↑ ↓ →
) {
    fun formattedTotalRevenue(): String = "$${totalRevenue / 100.0}"
    fun formattedAverageInvoiceSize(): String = "$${averageInvoiceSize / 100.0}"
}

/**
 * Quarterly analytics (90-day rolling window).
 * For trend analysis and forecasting.
 */
data class QuarterlyMetrics(
    val totalRevenue: Long,           // cents
    val paidAmount: Long,             // cents
    val outstandingAmount: Long,      // cents
    val invoiceCount: Int,
    val paidCount: Int,
    val averageDaysToPayment: Int,    // days
    val topCustomer: String,          // customer name or "N/A"
    val topCustomerRevenue: Long      // cents
) {
    fun formattedTotalRevenue(): String = "$${totalRevenue / 100.0}"
    fun formattedTopCustomerRevenue(): String = "$${topCustomerRevenue / 100.0}"
}

/**
 * Analytics comparison between two periods.
 * Used for showing growth, trends, etc.
 */
data class PeriodComparison(
    val current: PeriodAnalytics,
    val previous: PeriodAnalytics,
    val revenueGrowth: Float,         // percentage (-100 to +100)
    val invoiceGrowth: Float,         // percentage
    val paymentRateChange: Float      // percentage points
)

