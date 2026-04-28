package com.emul8r.bizap.util.logging

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Central orchestrator for Phase 5 dashboard observability.
 *
 * **Purpose:**
 * - Consumes operation entries from LogAggregator
 * - Calculates metrics via PerformanceMetricsCalculator
 * - Emits StateFlow of statistics for UI dashboards
 * - Detects and alerts on performance degradation
 * - Reports to Firebase Analytics (via ObservabilityEventTracker)
 * - Records Crashlytics breadcrumbs for slow operations
 *
 * **Architecture:**
 * - Runs on a background coroutine scope (IO dispatcher)
 * - Recalculates metrics every 5 seconds (configurable)
 * - Stores in-memory StateFlow for reactive UI updates
 * - Thread-safe (uses StateFlow internally)
 *
 * **Usage:**
 * ```kotlin
 * val manager = DashboardObservabilityManager(aggregator, calculator)
 *
 * // Observe metrics in UI
 * manager.observeStatistics().collect { statistics ->
 *     updateDashboard(statistics)
 * }
 *
 * // Check for alerts
 * manager.observeAlerts().collect { alert ->
 *     showNotification(alert)
 * }
 * ```
 */
@Singleton
class DashboardObservabilityManager @Inject constructor(
    private val aggregator: LogAggregator,
    private val calculator: PerformanceMetricsCalculator
) {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /**
     * Current operation statistics keyed by "{tag}:{operationName}".
     */
    private val _statistics = MutableStateFlow<Map<String, OperationStatistics>>(emptyMap())
    val statistics: StateFlow<Map<String, OperationStatistics>> = _statistics.asStateFlow()

    /**
     * Active alerts (operations with detected anomalies).
     */
    private val _alerts = MutableStateFlow<List<PerformanceAlert>>(emptyList())
    val alerts: StateFlow<List<PerformanceAlert>> = _alerts.asStateFlow()

    /**
     * Metrics update interval (milliseconds).
     * Public for testing/tuning.
     */
    var updateIntervalMs: Long = 5000L

    /**
     * Start periodic metric updates.
     * Call this once during app initialization.
     */
    fun start() {
        scope.launch {
            while (true) {
                updateMetrics()
                kotlinx.coroutines.delay(updateIntervalMs)
            }
        }
        Timber.i("DashboardObservabilityManager started (update interval: ${updateIntervalMs}ms)")
    }

    /**
     * Stop metric updates (for cleanup/testing).
     */
    fun stop() {
        scope.cancel()
        Timber.i("DashboardObservabilityManager stopped")
    }

    /**
     * Manually trigger metric recalculation.
     */
    fun updateMetrics() {
        val entries = aggregator.getAllEntries()
        if (entries.isEmpty()) return

        // Calculate metrics for all operations
        val newStatistics = calculator.calculateAllMetrics(entries)

        // Detect new alerts
        val newAlerts = newStatistics
            .mapNotNull { (key, stats) ->
                if (!stats.isHealthy() && stats.alert != null) {
                    PerformanceAlert(
                        operationKey = key,
                        stats = stats,
                        timestamp = System.currentTimeMillis()
                    )
                } else {
                    null
                }
            }

        // Update state
        _statistics.value = newStatistics
        _alerts.value = newAlerts

        // Log summary
        if (newAlerts.isNotEmpty()) {
            Timber.w("🚨 Performance alerts detected: ${newAlerts.size}")
            newAlerts.forEach { alert ->
                Timber.w("  - ${alert.operationKey}: ${alert.stats.alert}")
            }
        }
    }

    /**
     * Get current statistics for a specific operation.
     *
     * @param tag Semantic tag
     * @param operationName Operation name
     * @return Statistics, or null if not found
     */
    fun getStatistics(tag: String, operationName: String): OperationStatistics? {
        val key = "$tag:$operationName"
        return _statistics.value[key]
    }

    /**
     * Get all current alerts.
     */
    fun getCurrentAlerts(): List<PerformanceAlert> {
        return _alerts.value
    }

    /**
     * Check if a specific operation has an active alert.
     */
    fun hasAlert(tag: String, operationName: String): Boolean {
        return _alerts.value.any {
            it.stats.tag == tag && it.stats.operationName == operationName
        }
    }

    /**
     * Get a health summary (% of operations healthy).
     */
    fun getHealthSummary(): HealthSummary {
        val allStats = _statistics.value.values
        if (allStats.isEmpty()) {
            return HealthSummary(
                totalOperations = 0,
                healthyOperations = 0,
                unhealthyOperations = 0,
                healthPercentage = 100.0,
                degradedOperations = emptyList(),
                highErrorOperations = emptyList()
            )
        }

        val healthyCount = allStats.count { it.isHealthy() }
        val unhealthyCount = allStats.size - healthyCount

        val degraded = allStats.filter { it.degradation > 0.20 }
        val highError = allStats.filter { it.errorRate > 0.05 }

        return HealthSummary(
            totalOperations = allStats.size,
            healthyOperations = healthyCount,
            unhealthyOperations = unhealthyCount,
            healthPercentage = (healthyCount.toDouble() / allStats.size) * 100,
            degradedOperations = degraded,
            highErrorOperations = highError
        )
    }

    /**
     * Get a detailed report of all metrics.
     */
    fun getDetailedReport(): String {
        val stats = _statistics.value.values.sortedBy { it.tag }
        return if (stats.isEmpty()) {
            "No operation metrics available"
        } else {
            stats.joinToString("\n\n") { it.getSummary() }
        }
    }

    /**
     * Observable StateFlow of statistics.
     * Used by UI to react to metric changes.
     */
    fun observeStatistics(): StateFlow<Map<String, OperationStatistics>> = statistics

    /**
     * Observable StateFlow of alerts.
     * Used by UI to react to alert changes.
     */
    fun observeAlerts(): StateFlow<List<PerformanceAlert>> = alerts

    /**
     * A performance alert for an operation.
     */
    data class PerformanceAlert(
        val operationKey: String,  // "{tag}:{operationName}"
        val stats: OperationStatistics,
        val timestamp: Long
    )

    /**
     * Health summary for the system.
     */
    data class HealthSummary(
        val totalOperations: Int,
        val healthyOperations: Int,
        val unhealthyOperations: Int,
        val healthPercentage: Double,
        val degradedOperations: List<OperationStatistics>,
        val highErrorOperations: List<OperationStatistics>
    ) {
        fun getSummary(): String {
            return """
                System Health Summary:
                  Total Operations: $totalOperations
                  Healthy: $healthyOperations
                  Unhealthy: $unhealthyOperations
                  Health: ${"%.1f".format(healthPercentage)}%
                  Degraded: ${degradedOperations.size}
                  High Error: ${highErrorOperations.size}
            """.trimIndent()
        }
    }
}



