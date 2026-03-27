package com.emul8r.bizap.domain.analytics

/**
 * Metric with comparison to previous period for trend visualization.
 *
 * Used to display metrics with percentage deltas and visual trend indicators.
 * Example: MTD Revenue = $5,000 (↑8% vs last month)
 */
data class TrendMetric(
    val label: String,
    val currentValue: Double,
    val previousValue: Double,
    val unit: String = ""
) {
    val deltaAbsolute: Double get() = currentValue - previousValue

    val deltaPercent: Double get() = if (previousValue != 0.0) {
        ((currentValue - previousValue) / previousValue) * 100.0
    } else {
        0.0
    }

    val trendDirection: TrendDirection get() = when {
        deltaPercent > 5.0 -> TrendDirection.UP
        deltaPercent < -5.0 -> TrendDirection.DOWN
        else -> TrendDirection.NEUTRAL
    }
}

enum class TrendDirection {
    UP, DOWN, NEUTRAL
}

/**
 * Generic chart data point for time-series visualizations.
 *
 * Represents a single data point in a chart (daily revenue, weekly invoices, etc.)
 */
data class ChartDataPoint(
    val label: String,     // "Mar 1", "Week 1", etc.
    val value: Float,
    val timestamp: Long    // Unix millis for sorting
)

/**
 * Date range enum for analytics filtering across all dashboards.
 *
 * Used by AnalyticsFocusedInsightsViewModel to filter metrics by time period.
 */
enum class AnalyticsDateRange(val label: String, val days: Int) {
    SEVEN_DAYS("Last 7 Days", 7),
    THIRTY_DAYS("Last 30 Days", 30),
    NINETY_DAYS("Last 90 Days", 90),
    CUSTOM("Custom Range", -1)
}

