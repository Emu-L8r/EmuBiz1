package com.emul8r.bizap.domain.revenue.model

import java.time.LocalDate

/**
 * HIGH-LEVEL REVENUE BUSINESS INTELLIGENCE
 */
data class RevenueMetrics(
    val mtdRevenue: Double,
    val ytdRevenue: Double,
    val weeklyRevenue: Double,
    val dailyTrend: List<DailyRevenuePoint>,
    val topPerformers: List<RevenueByCurrency>
)

data class DailyRevenuePoint(
    val date: LocalDate,
    val amount: Double,
    val invoiceCount: Int
)

data class RevenueByCurrency(
    val currencyCode: String,
    val totalAmount: Double,
    val percentageOfTotal: Double
)
