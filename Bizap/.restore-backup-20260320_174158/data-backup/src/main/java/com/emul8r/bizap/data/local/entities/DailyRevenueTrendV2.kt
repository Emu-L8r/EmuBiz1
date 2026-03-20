package com.emul8r.bizap.data.local.entities

/**
 * Query result model for daily revenue trend data.
 * Returned by InvoiceDaoV2.observeLast30DaysRevenueTrend().
 * Contains one row per date with aggregated revenue.
 */
data class DailyRevenueTrendV2(
    val dateString: String,     // ISO date: "YYYY-MM-DD"
    val revenue: Long,          // Sum of paid invoice amounts in cents
    val invoiceCount: Int,      // Total invoices on this date
    val paidCount: Int          // Paid invoices on this date
)
