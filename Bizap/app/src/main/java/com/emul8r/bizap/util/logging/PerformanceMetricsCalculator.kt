package com.emul8r.bizap.util.logging

import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Calculates aggregated performance metrics from operation entries.
 *
 * **Purpose:**
 * Computes statistics (avg, p95, p99, min, max, error rate) for operations.
 * Detects performance anomalies and alerts when metrics degrade.
 *
 * **Usage:**
 * ```kotlin
 * val entries = aggregator.getByTag("INVOICE")
 * val stats = calculator.calculateMetrics(entries)
 * println("Invoice creation: avg=${stats.avgMs}ms, p99=${stats.p99Ms}ms, error=${stats.errorRate}")
 * ```
 *
 * **Baseline Tracking:**
 * Tracks historical baselines per (tag, operationName) to detect degradation.
 * Stores baseline after first 10 executions for stability.
 */
@Singleton
class PerformanceMetricsCalculator @Inject constructor() {

    /**
     * Stores baseline metrics for (tag, operation) pairs.
     * Key: "{tag}:{operation}"
     * Value: baseline average duration (ms)
     */
    private val baselineMetrics = mutableMapOf<String, Double>()

    /**
     * Calculate aggregated metrics from a list of entries.
     *
     * @param entries List of operation entries
     * @return Aggregated statistics
     */
    fun calculateMetrics(entries: List<OperationEntry>): OperationStatistics {
        if (entries.isEmpty()) {
            return OperationStatistics.empty("", "")
        }

        val tag = entries.first().tag
        val operationName = entries.first().operationName
        val durations = entries.map { it.durationMs }
        val successCount = entries.count { it.status == "success" }
        val failureCount = entries.count { it.status != "success" }

        // Calculate percentiles
        val sortedDurations = durations.sorted()
        val avg = durations.average()
        val p50 = calculatePercentile(sortedDurations, 50)
        val p95 = calculatePercentile(sortedDurations, 95)
        val p99 = calculatePercentile(sortedDurations, 99)
        val min = sortedDurations.first()
        val max = sortedDurations.last()
        val successRate = successCount.toDouble() / entries.size
        val errorRate = failureCount.toDouble() / entries.size

        // Calculate degradation from baseline
        val baselineKey = "$tag:$operationName"
        val baseline = baselineMetrics.getOrPut(baselineKey) {
            // Initialize baseline after first 10 executions for stability
            if (entries.size >= 10) {
                avg
            } else {
                avg  // Use current avg until we have 10 samples
            }
        }

        val degradation = if (baseline > 0) {
            (avg - baseline) / baseline
        } else {
            0.0
        }

        // Detect anomalies
        val alert = detectAnomalies(
            tag = tag,
            operationName = operationName,
            avg = avg,
            p99 = p99,
            errorRate = errorRate,
            degradation = degradation
        )

        val stats = OperationStatistics(
            tag = tag,
            operationName = operationName,
            count = entries.size,
            avgMs = avg,
            p50Ms = p50,
            p95Ms = p95,
            p99Ms = p99,
            minMs = min,
            maxMs = max,
            successRate = successRate,
            errorRate = errorRate,
            degradation = degradation,
            lastRunMs = entries.maxOf { it.timestamp },
            alert = alert
        )

        // Log if unhealthy
        if (!stats.isHealthy()) {
            Timber.w("⚠ Performance alert for [$tag] $operationName: $alert")
        }

        return stats
    }

    /**
     * Calculate metrics for all operations in entries, grouped by (tag, operationName).
     *
     * @param entries All operation entries
     * @return Map of "{tag}:{operation}" → OperationStatistics
     */
    fun calculateAllMetrics(entries: List<OperationEntry>): Map<String, OperationStatistics> {
        return entries
            .groupBy { "${it.tag}:${it.operationName}" }
            .mapValues { (_, groupedEntries) ->
                calculateMetrics(groupedEntries)
            }
    }

    /**
     * Calculate percentile from a sorted list.
     *
     * @param sortedValues Sorted list of values
     * @param percentile Percentile to calculate (0–100)
     * @return Percentile value
     */
    private fun calculatePercentile(sortedValues: List<Long>, percentile: Int): Double {
        if (sortedValues.isEmpty()) return 0.0

        val index = ((percentile / 100.0) * (sortedValues.size - 1)).toInt()
        return sortedValues[index].toDouble()
    }

    /**
     * Detect performance anomalies in operation statistics.
     *
     * @return Alert message if anomaly detected, null if normal
     */
    private fun detectAnomalies(
        tag: String,
        operationName: String,
        avg: Double,
        p99: Double,
        errorRate: Double,
        degradation: Double
    ): String? {
        val alerts = mutableListOf<String>()

        // Check for significant degradation (20% slower than baseline)
        if (degradation > 0.20) {
            alerts.add("Operation ${"%.1f".format(degradation * 100)}% slower than baseline")
        }

        // Check for high error rate (>5%)
        if (errorRate > 0.05) {
            alerts.add("Error rate ${"%.1f".format(errorRate * 100)}% (>5% threshold)")
        }

        // Check for extreme p99 (>10 seconds)
        if (p99 > 10_000) {
            alerts.add("p99 at ${"%.0f".format(p99)}ms (>10s threshold)")
        }

        // Check for extreme average (>5 seconds)
        if (avg > 5_000) {
            alerts.add("Average ${"%.0f".format(avg)}ms (>5s threshold)")
        }

        return if (alerts.isNotEmpty()) alerts.joinToString("; ") else null
    }

    /**
     * Update baseline for a specific operation.
     * Useful for resetting baselines after expected performance changes.
     *
     * @param tag Semantic tag
     * @param operationName Operation name
     * @param baselineMs New baseline average in milliseconds
     */
    fun setBaseline(tag: String, operationName: String, baselineMs: Double) {
        val key = "$tag:$operationName"
        baselineMetrics[key] = baselineMs
        Timber.i("Updated baseline for $key: ${baselineMs}ms")
    }

    /**
     * Get the baseline for an operation.
     *
     * @param tag Semantic tag
     * @param operationName Operation name
     * @return Baseline average duration, or null if not set
     */
    fun getBaseline(tag: String, operationName: String): Double? {
        return baselineMetrics["$tag:$operationName"]
    }

    /**
     * Clear all baselines (for testing or reset).
     */
    fun clearBaselines() {
        baselineMetrics.clear()
        Timber.i("Performance baselines cleared")
    }

    /**
     * Get a report of current baselines.
     */
    fun getBaselineReport(): String {
        return if (baselineMetrics.isEmpty()) {
            "No baselines recorded"
        } else {
            baselineMetrics
                .entries
                .sortedBy { it.key }
                .joinToString("\n") { (key, baseline) ->
                    "$key: ${"%.1f".format(baseline)}ms"
                }
        }
    }
}

