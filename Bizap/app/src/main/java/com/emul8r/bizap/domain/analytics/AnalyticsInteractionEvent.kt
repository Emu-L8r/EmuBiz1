package com.emul8r.bizap.domain.analytics

/**
 * Analytics-specific user interactions for ViewModel communication.
 *
 * These events are dispatched from UI composables to ViewModels to trigger
 * analytics data loads, drills, and comparisons.
 */
sealed class AnalyticsInteractionEvent {
    data class DrillMetric(val metricName: String, val tabIndex: Int) : AnalyticsInteractionEvent()
    data class SelectDateRange(val range: AnalyticsDateRange) : AnalyticsInteractionEvent()
    data class CustomDateRange(val startDays: Int, val endDays: Int) : AnalyticsInteractionEvent()
    data class CompareMetric(val metric1: String, val metric2: String) : AnalyticsInteractionEvent()
    object ExportReport : AnalyticsInteractionEvent()
}

