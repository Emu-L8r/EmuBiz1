package com.emul8r.bizap.util.logging

/**
 * A single operation log entry for aggregation and analysis.
 *
 * **Purpose:**
 * Represents one operation execution in the dashboard observability system.
 * Collected by LogAggregator for metrics calculation and alerting.
 *
 * **Usage:**
 * ```kotlin
 * LogAggregator().append(OperationEntry(
 *     tag = "INVOICE",
 *     operationName = "Creating invoice",
 *     durationMs = 234,
 *     timestamp = System.currentTimeMillis(),
 *     status = "success"
 * ))
 * ```
 *
 * @param tag Semantic tag (e.g., "INVOICE", "ANALYTICS")
 * @param operationName Human-readable operation name
 * @param durationMs Duration of operation in milliseconds
 * @param timestamp Timestamp when operation completed (epoch milliseconds)
 * @param status Outcome: "success", "failure", "timeout", or "cancelled"
 * @param metadata Optional key-value pairs for additional context (e.g., "invoice_count" → "42")
 */
data class OperationEntry(
    val tag: String,
    val operationName: String,
    val durationMs: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "success",  // "success", "failure", "timeout", "cancelled"
    val metadata: Map<String, String> = emptyMap()
) {
    /**
     * Create a copy with additional metadata.
     */
    fun withMetadata(vararg pairs: Pair<String, String>): OperationEntry {
        return this.copy(metadata = metadata + pairs.toMap())
    }

    /**
     * Check if this entry represents a slow operation (relative to typical).
     */
    fun isSlow(thresholdMs: Long = 500): Boolean = durationMs > thresholdMs

    /**
     * Check if this entry represents a failure.
     */
    fun isFailed(): Boolean = status != "success"

    /**
     * Get a human-readable summary.
     */
    override fun toString(): String {
        return "[$tag] $operationName: ${durationMs}ms ($status)"
    }

    companion object {
        /**
         * Create a successful operation entry.
         */
        fun success(
            tag: String,
            operationName: String,
            durationMs: Long,
            metadata: Map<String, String> = emptyMap()
        ) = OperationEntry(
            tag = tag,
            operationName = operationName,
            durationMs = durationMs,
            timestamp = System.currentTimeMillis(),
            status = "success",
            metadata = metadata
        )

        /**
         * Create a failed operation entry.
         */
        fun failure(
            tag: String,
            operationName: String,
            durationMs: Long,
            errorMessage: String = "",
            metadata: Map<String, String> = emptyMap()
        ) = OperationEntry(
            tag = tag,
            operationName = operationName,
            durationMs = durationMs,
            timestamp = System.currentTimeMillis(),
            status = "failure",
            metadata = metadata + ("error" to errorMessage)
        )

        /**
         * Create a timeout entry.
         */
        fun timeout(
            tag: String,
            operationName: String,
            durationMs: Long,
            metadata: Map<String, String> = emptyMap()
        ) = OperationEntry(
            tag = tag,
            operationName = operationName,
            durationMs = durationMs,
            timestamp = System.currentTimeMillis(),
            status = "timeout",
            metadata = metadata
        )
    }
}

