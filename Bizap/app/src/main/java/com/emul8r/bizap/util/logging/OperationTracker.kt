package com.emul8r.bizap.util.logging

import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tracks active operations by tag and operation name.
 *
 * **Purpose:**
 * Provides observability into currently-executing operations:
 * - What operations are running?
 * - How long have they been running?
 * - How many of each type are in-flight?
 *
 * **Thread Safety:**
 * All state is thread-safe (uses ConcurrentHashMap).
 *
 * **Usage:**
 * ```kotlin
 * val startMs = tracker.recordStart("INVOICE", "Creating invoice")
 * try {
 *     // Do work...
 *     tracker.recordComplete("INVOICE", elapsedMs, success = true)
 * } catch (e: Exception) {
 *     tracker.recordComplete("INVOICE", elapsedMs, success = false, error = e)
 * }
 * ```
 *
 * **Monitoring:**
 * ```kotlin
 * val activeOps = tracker.getActiveOperations()
 * // activeOps = listOf(
 * //   ActiveOperation("INVOICE", "Creating invoice", 234ms),
 * //   ActiveOperation("ANALYTICS", "Computing metrics", 1250ms)
 * // )
 * ```
 */
@Singleton
class OperationTracker @Inject constructor() {

    /**
     * Active operations currently in-flight.
     * Key: operation ID (unique per start call)
     * Value: metadata about the operation
     */
    private val activeOperations = ConcurrentHashMap<String, OperationMetadata>()

    /**
     * Counter for generating unique operation IDs.
     */
    private var operationIdCounter = 0L

    /**
     * Record the start of an operation.
     *
     * @param tag Semantic tag (e.g., "INVOICE", "ANALYTICS")
     * @param operationName Human-readable operation name
     * @return Operation ID (used to track completion)
     */
    fun recordStart(tag: String, operationName: String): String {
        val operationId = "op_${++operationIdCounter}"
        val metadata = OperationMetadata(
            operationId = operationId,
            tag = tag,
            operationName = operationName,
            startTimeMs = System.currentTimeMillis(),
            status = "running"
        )
        activeOperations[operationId] = metadata
        return operationId
    }

    /**
     * Record completion of an operation.
     *
     * @param operationId Operation ID returned from recordStart()
     * @param durationMs Duration of operation in milliseconds
     * @param success Whether operation succeeded (true) or failed (false)
     * @param error Optional exception if operation failed
     */
    fun recordComplete(
        operationId: String,
        durationMs: Long,
        success: Boolean = true,
        error: Throwable? = null
    ) {
        val metadata = activeOperations.remove(operationId) ?: return

        // Log completion
        if (success) {
            Timber.d("✓ [${metadata.tag}] ${metadata.operationName} completed in ${durationMs}ms")
        } else {
            Timber.w("✗ [${metadata.tag}] ${metadata.operationName} failed after ${durationMs}ms: ${error?.message}")
        }
    }

    /**
     * Get all currently active operations.
     *
     * @return List of operations currently in-flight
     */
    fun getActiveOperations(): List<OperationMetadata> {
        return activeOperations.values.toList()
    }

    /**
     * Get active operations filtered by tag.
     *
     * @param tag Semantic tag to filter by
     * @return Operations with matching tag
     */
    fun getActiveOperationsByTag(tag: String): List<OperationMetadata> {
        return activeOperations.values.filter { it.tag == tag }
    }

    /**
     * Get active operation count by tag.
     *
     * @return Map of tag → operation count
     */
    fun getActiveOperationCountByTag(): Map<String, Int> {
        return activeOperations.values
            .groupingBy { it.tag }
            .eachCount()
    }

    /**
     * Check if a tag has any active operations.
     *
     * @param tag Semantic tag
     * @return true if any operations are running with this tag
     */
    fun hasActiveOperations(tag: String): Boolean {
        return activeOperations.values.any { it.tag == tag }
    }

    /**
     * Get the longest-running operation (for timeout detection).
     *
     * @return Oldest active operation, or null if none
     */
    fun getLongestRunningOperation(): OperationMetadata? {
        return activeOperations.values.minByOrNull { it.startTimeMs }
    }

    /**
     * Get the longest-running operation for a specific tag.
     *
     * @param tag Semantic tag
     * @return Oldest active operation with matching tag, or null
     */
    fun getLongestRunningOperation(tag: String): OperationMetadata? {
        return activeOperations.values
            .filter { it.tag == tag }
            .minByOrNull { it.startTimeMs }
    }

    /**
     * Clear all active operations (for testing).
     */
    fun clear() {
        activeOperations.clear()
    }

    /**
     * Metadata about an active operation.
     */
    data class OperationMetadata(
        val operationId: String,
        val tag: String,
        val operationName: String,
        val startTimeMs: Long,
        val status: String  // "running", "slow", "timeout"
    ) {
        /**
         * Get the elapsed time for this operation so far.
         */
        fun getElapsedMs(): Long = System.currentTimeMillis() - startTimeMs
    }
}

