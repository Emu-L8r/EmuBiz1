package com.emul8r.bizap.domain.revenue.model

import java.time.LocalDate

/**
 * HIGH-LEVEL REVENUE BUSINESS INTELLIGENCE
 * All amounts are stored as Long (cents).
 */
data class RevenueMetrics(
    val mtdRevenue: Long,               // Month-to-date, in cents (PAID invoices)
    val ytdRevenue: Long,               // Year-to-date, in cents (PAID invoices)
    val weeklyRevenue: Long,            // Weekly, in cents (PAID invoices)
    val totalPaidRevenue: Long,         // All-time total of all PAID invoices, in cents
    val outstandingAmount: Long = 0L,   // Outstanding amount from SENT/OVERDUE invoices, in cents
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
