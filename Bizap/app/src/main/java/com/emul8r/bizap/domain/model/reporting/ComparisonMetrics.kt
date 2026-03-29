package com.emul8r.bizap.domain.model.reporting

import kotlinx.serialization.Serializable

/**
 * Metrics for month-over-month or year-over-year comparison.
 */
@Serializable
data class ComparisonMetrics(
    val metric: String,
    val currentValue: Double,
    val previousValue: Double,
    val changeAmount: Double,
    val changePercent: Double,
    val trend: TrendDirection
) {
    val isPositive: Boolean get() = changeAmount >= 0
}

enum class TrendDirection {
    UP, DOWN, FLAT
}

/**
 * Complete comparison report for a specific period.
 */
@Serializable
data class ComparisonReport(
    val period: Period,
    val comparisonPeriod: Period,
    val metrics: List<ComparisonMetrics>
) {
    val netChangePercent: Double by lazy {
        metrics.filter { it.metric == "Revenue" }
            .firstOrNull()?.changePercent ?: 0.0
    }
}

@Serializable
data class Period(
    val startDate: Long,  // ms
    val endDate: Long,    // ms
    val label: String
)

/**
 * Month-over-month comparison container.
 */
@Serializable
data class MonthComparison(
    val currentMonth: MonthMetrics,
    val previousMonth: MonthMetrics,
    val comparison: ComparisonReport
)

@Serializable
data class MonthMetrics(
    val month: Int,
    val year: Int,
    val revenue: Double,
    val invoiceCount: Int,
    val paidCount: Int,
    val unpaidCount: Int,
    val overDueCount: Int,
    val averageDaysToPayment: Double
) {
    val label: String get() = "${monthName(month)} $year"

    private fun monthName(month: Int): String {
        return listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun",
                     "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")[month - 1]
    }
}

/**
 * Year-over-year comparison container.
 */
@Serializable
data class YearComparison(
    val currentYear: YearMetrics,
    val previousYear: YearMetrics,
    val comparison: ComparisonReport
)

@Serializable
data class YearMetrics(
    val year: Int,
    val revenue: Double,
    val invoiceCount: Int,
    val paidCount: Int,
    val unpaidCount: Int,
    val monthlyBreakdown: List<MonthMetrics>
)

