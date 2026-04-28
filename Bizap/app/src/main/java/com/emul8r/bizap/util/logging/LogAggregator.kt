package com.emul8r.bizap.util.logging

import timber.log.Timber
import java.util.concurrent.ConcurrentLinkedQueue
import javax.inject.Inject
import javax.inject.Singleton

/**
 * In-memory circular buffer for operation log entries.
 *
 * **Purpose:**
 * Collects operation entries from ContextBlockLogger for aggregation and metrics calculation.
 * Maintains a rolling window of recent operations (default: 1000 entries, ~50KB memory).
 *
 * **Design:**
 * - Circular buffer: When max size reached, oldest entries are dropped
 * - Thread-safe: Uses ConcurrentLinkedQueue
 * - Fast: O(1) append, O(n) queries (n = buffer size)
 *
 * **Memory Footprint:**
 * Each entry ~50 bytes. 1000 entries = ~50KB. Configurable via maxSize.
 *
 * **Usage:**
 * ```kotlin
 * aggregator.append(OperationEntry.success("INVOICE", "Creating", 234))
 * val invoiceOps = aggregator.getByTag("INVOICE")
 * val avgTime = invoiceOps.map { it.durationMs }.average()
 * ```
 */
@Singleton
class LogAggregator @Inject constructor(
    private val maxSize: Int = 1000
) {

    /**
     * Circular buffer of operation entries.
     * Uses ConcurrentLinkedQueue for thread-safe operations.
     */
    private val entries = ConcurrentLinkedQueue<OperationEntry>()

    /**
     * Append an operation entry to the buffer.
     * If buffer is full, remove the oldest entry.
     *
     * @param entry Operation entry to append
     */
    fun append(entry: OperationEntry) {
        // Remove oldest if at capacity
        if (entries.size >= maxSize) {
            entries.poll()
        }
        entries.offer(entry)
    }

    /**
     * Get all entries in the buffer.
     *
     * @return List of all current entries (snapshot)
     */
    fun getAllEntries(): List<OperationEntry> {
        return entries.toList()
    }

    /**
     * Get entries filtered by tag.
     *
     * @param tag Semantic tag to filter by
     * @return List of entries with matching tag
     */
    fun getByTag(tag: String): List<OperationEntry> {
        return entries.filter { it.tag == tag }
    }

    /**
     * Get entries filtered by tag and operation name.
     *
     * @param tag Semantic tag
     * @param operationName Operation name
     * @return List of matching entries
     */
    fun getByTagAndOperation(tag: String, operationName: String): List<OperationEntry> {
        return entries.filter { it.tag == tag && it.operationName == operationName }
    }

    /**
     * Get successful entries (status = "success").
     *
     * @return List of successful operations
     */
    fun getSuccessfulEntries(): List<OperationEntry> {
        return entries.filter { it.status == "success" }
    }

    /**
     * Get failed entries (status != "success").
     *
     * @return List of failed operations
     */
    fun getFailedEntries(): List<OperationEntry> {
        return entries.filter { it.status != "success" }
    }

    /**
     * Get slow entries (duration > threshold).
     *
     * @param thresholdMs Slowness threshold in milliseconds
     * @return List of slow operations
     */
    fun getSlowEntries(thresholdMs: Long = 500): List<OperationEntry> {
        return entries.filter { it.durationMs > thresholdMs }
    }

    /**
     * Get entries from the last N milliseconds.
     *
     * @param windowMs Time window in milliseconds
     * @return Entries within the time window
     */
    fun getEntriesInWindow(windowMs: Long): List<OperationEntry> {
        val cutoffTime = System.currentTimeMillis() - windowMs
        return entries.filter { it.timestamp >= cutoffTime }
    }

    /**
     * Get unique tags currently in the buffer.
     *
     * @return List of unique tags
     */
    fun getUniqueTags(): List<String> {
        return entries.map { it.tag }.distinct()
    }

    /**
     * Get unique operations for a tag.
     *
     * @param tag Semantic tag
     * @return List of unique operation names for that tag
     */
    fun getUniqueOperationsForTag(tag: String): List<String> {
        return entries
            .filter { it.tag == tag }
            .map { it.operationName }
            .distinct()
    }

    /**
     * Get the count of entries.
     *
     * @return Number of entries currently in buffer
     */
    fun size(): Int = entries.size

    /**
     * Clear all entries from the buffer.
     */
    fun clear() {
        entries.clear()
        Timber.d("LogAggregator cleared (all entries removed)")
    }

    /**
     * Get a summary of buffer statistics.
     */
    fun getSummary(): String {
        val allEntries = getAllEntries()
        val tags = getUniqueTags()
        val successCount = getSuccessfulEntries().size
        val failureCount = getFailedEntries().size
        val slowCount = getSlowEntries().size
        val avgDuration = if (allEntries.isNotEmpty()) {
            allEntries.map { it.durationMs }.average()
        } else {
            0.0
        }

        return """
            LogAggregator Summary:
              Size: ${size()} / $maxSize
              Tags: ${tags.size} (${tags.joinToString(", ")})
              Success: $successCount
              Failed: $failureCount
              Slow (>500ms): $slowCount
              Avg Duration: ${"%.1f".format(avgDuration)}ms
        """.trimIndent()
    }
}

