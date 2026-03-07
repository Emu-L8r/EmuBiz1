package com.emul8r.bizap.domain.model.gui2

/**
 * Aggregated revenue metrics for GUI2 dashboard and analytics screens.
 * All monetary values stored in cents (Long).
 */
data class RevenueMetricsV2(
    val businessProfileId: Long,
    val mtdRevenue: Long,           // Month-to-date paid revenue (cents)
    val ytdRevenue: Long,           // Year-to-date paid revenue (cents)
    val weeklyRevenue: Long,        // Last 7 days paid revenue (cents)
    val totalPaidRevenue: Long,     // All-time paid revenue (cents)
    val outstandingAmount: Long,    // Unpaid outstanding amount (cents)
    val collectedAmount: Long,      // Total amount collected (amountPaid sum, cents)
    val dailyTrend: List<DailyTrendPointV2> = emptyList()
)

/**
 * Single data point in a daily revenue trend series.
 */
data class DailyTrendPointV2(
    val date: String,           // ISO date "YYYY-MM-DD"
    val revenueCents: Long,     // Revenue for this day in cents
    val invoiceCount: Int
)
