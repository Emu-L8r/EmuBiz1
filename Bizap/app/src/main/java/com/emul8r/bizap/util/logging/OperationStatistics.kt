package com.emul8r.bizap.util.logging

/**
 * Aggregated statistics for operations matching a specific (tag, operationName) pair.
 *
 * **Purpose:**
 * Represents computed metrics across multiple executions of the same operation.
 * Used by PerformanceMetricsCalculator to expose dashboard-ready statistics.
 *
 * **Metrics Provided:**
 * - avg, p50, p95, p99, min, max: Percentile distributions of operation duration
 * - count: Total number of executions
 * - successRate: % of operations that succeeded (0.0–1.0)
 * - errorRate: % of operations that failed
 * - degradation: % change from baseline (0.0 = no change, 0.2 = 20% slower)
 * - lastRun: Timestamp of most recent execution
 *
 * **Usage:**
 * ```kotlin
 * val stats = PerformanceMetricsCalculator().calculateMetrics(entries)
 * println("Invoice creation: avg=${stats.avgMs}ms, p95=${stats.p95Ms}ms, error rate=${stats.errorRate}%")
 * ```
 */
data class OperationStatistics(
    val tag: String,
    val operationName: String,
    val count: Int = 0,                    // Number of executions
    val avgMs: Double = 0.0,               // Average duration
    val p50Ms: Double = 0.0,               // Median (50th percentile)
    val p95Ms: Double = 0.0,               // 95th percentile
    val p99Ms: Double = 0.0,               // 99th percentile
    val minMs: Long = 0,                   // Minimum observed
    val maxMs: Long = 0,                   // Maximum observed
    val successRate: Double = 1.0,         // 0.0–1.0 (1.0 = 100% success)
    val errorRate: Double = 0.0,           // 0.0–1.0 (0.0 = no errors)
    val degradation: Double = 0.0,         // % change from baseline (0.2 = 20% slower)
    val lastRunMs: Long = 0,               // Timestamp of most recent execution
    val alert: String? = null              // Alert message if anomaly detected (null = normal)
) {
    /**
     * Check if operation is performing normally.
     */
    fun isHealthy(): Boolean {
        return degradation < 0.2 &&  // Less than 20% slower than baseline
               errorRate < 0.05 &&   // Less than 5% error rate
               p99Ms < 10_000 &&     // p99 under 10 seconds
               alert == null
    }

    /**
     * Get a human-readable summary.
     */
    fun getSummary(): String {
        return """
            [$tag] $operationName:
              Executions: $count
              Avg: ${"%.1f".format(avgMs)}ms
              p95: ${"%.1f".format(p95Ms)}ms
              p99: ${"%.1f".format(p99Ms)}ms
              Range: ${minMs}–${maxMs}ms
              Success Rate: ${"%.1f".format(successRate * 100)}%
              Degradation: ${"%.1f".format(degradation * 100)}%
              Status: ${if (isHealthy()) "✓ HEALTHY" else "⚠ ALERT"}
              ${alert?.let { "Alert: $it" } ?: ""}
        """.trimIndent()
    }

    companion object {
        /**
         * Create an empty statistics object (for operations with no data).
         */
        fun empty(tag: String, operationName: String) = OperationStatistics(
            tag = tag,
            operationName = operationName,
            count = 0
        )
    }
}

