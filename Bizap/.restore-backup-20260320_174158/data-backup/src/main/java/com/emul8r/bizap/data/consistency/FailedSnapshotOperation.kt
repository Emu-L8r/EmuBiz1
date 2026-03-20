package com.emul8r.bizap.data.consistency

/**
 * Represents a snapshot sync operation that failed and is queued for retry.
 *
 * Stored in-memory and serialized to SharedPreferences for persistence between
 * app sessions. Not a Room entity to avoid schema changes.
 */
data class FailedSnapshotOperation(
    /** ID of the invoice whose snapshot failed to sync. */
    val invoiceId: Long,

    /** Owning business profile. */
    val businessId: Long,

    /** Type of snapshot that failed (ANALYTICS, DAILY_REVENUE, PAYMENT). */
    val snapshotType: SnapshotType,

    /** Human-readable error message for logging/debugging. */
    val errorMessage: String,

    /** Unix timestamp (ms) when the failure occurred. */
    val failedAtMs: Long = System.currentTimeMillis(),

    /** Number of retry attempts so far. */
    val retryCount: Int = 0
) {
    enum class SnapshotType {
        ANALYTICS,
        DAILY_REVENUE,
        PAYMENT,
        ALL
    }

    companion object {
        /** Maximum number of retries before giving up on a failed operation. */
        const val MAX_RETRY_COUNT = 5
    }

    /** Returns true if this operation should still be retried. */
    val isRetryable: Boolean get() = retryCount < MAX_RETRY_COUNT

    /** Returns a copy with incremented retry count. */
    fun withIncrementedRetry(): FailedSnapshotOperation = copy(retryCount = retryCount + 1)
}
