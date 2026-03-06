package com.emul8r.bizap.data.monitoring

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/**
 * Lightweight in-process performance metrics tracker.
 *
 * Tracks success/failure counts and cumulative duration for named operations
 * so callers can compute average latency and failure rate without an external
 * monitoring framework.
 *
 * Thread-safe via [AtomicInteger] / [AtomicLong].
 *
 * Usage:
 * ```kotlin
 * val start = System.currentTimeMillis()
 * try {
 *     doWork()
 *     PerformanceMetrics.recordSuccess("updateInvoiceStatus", System.currentTimeMillis() - start)
 * } catch (e: Exception) {
 *     PerformanceMetrics.recordFailure("updateInvoiceStatus", System.currentTimeMillis() - start, e)
 * }
 * ```
 */
object PerformanceMetrics {

    private data class OperationStats(
        val successCount: AtomicInteger = AtomicInteger(0),
        val failureCount: AtomicInteger = AtomicInteger(0),
        val totalDurationMs: AtomicLong = AtomicLong(0),
        val errorTypeCount: java.util.concurrent.ConcurrentHashMap<String, Int> =
            java.util.concurrent.ConcurrentHashMap()
    )

    private val stats = java.util.concurrent.ConcurrentHashMap<String, OperationStats>()

    // ── Recording ────────────────────────────────────────────────────────────────

    fun recordSuccess(operation: String, durationMs: Long) {
        val s = stats.getOrPut(operation) { OperationStats() }
        s.successCount.incrementAndGet()
        s.totalDurationMs.addAndGet(durationMs)
    }

    fun recordFailure(operation: String, durationMs: Long, error: Throwable? = null) {
        val s = stats.getOrPut(operation) { OperationStats() }
        s.failureCount.incrementAndGet()
        s.totalDurationMs.addAndGet(durationMs)
        if (error != null) {
            // Count errors by type for future breakdown reporting
            s.errorTypeCount.merge(error.javaClass.simpleName, 1, Int::plus)
        }
    }

    // ── Querying ─────────────────────────────────────────────────────────────────

    /** Average duration in milliseconds across all recorded calls (success + failure). */
    fun getAverageLatencyMs(operation: String): Double {
        val s = stats[operation] ?: return 0.0
        val total = s.successCount.get() + s.failureCount.get()
        return if (total > 0) s.totalDurationMs.get().toDouble() / total else 0.0
    }

    /** Fraction of calls that resulted in a failure (0.0 – 1.0). */
    fun getFailureRate(operation: String): Double {
        val s = stats[operation] ?: return 0.0
        val total = s.successCount.get() + s.failureCount.get()
        return if (total > 0) s.failureCount.get().toDouble() / total else 0.0
    }

    fun getSuccessCount(operation: String): Int = stats[operation]?.successCount?.get() ?: 0
    fun getFailureCount(operation: String): Int = stats[operation]?.failureCount?.get() ?: 0

    /** Resets all counters – intended for use in tests only. */
    fun resetAll() = stats.clear()
}
