package com.emul8r.bizap.domain.revenue.model

import java.time.LocalDate

/**
 * HIGH-LEVEL REVENUE BUSINESS INTELLIGENCE
 * All amounts are stored as Long (cents).
 */
data class RevenueMetrics(
    val mtdRevenue: Long,               // Month-to-date, in cents
    val ytdRevenue: Long,               // Year-to-date, in cents
    val weeklyRevenue: Long,            // Weekly, in cents
    val dailyTrend: List<DailyRevenuePoint>,
    val topPerformers: List<RevenueByCurrency>
)

data class DailyRevenuePoint(
    val date: LocalDate,
    val amount: Long,                   // In cents
    val invoiceCount: Int
)

data class RevenueByCurrency(
    val currencyCode: String,
    val totalAmount: Long,              // In cents
    val percentageOfTotal: Double
)

