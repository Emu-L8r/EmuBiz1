package com.emul8r.bizap.data.consistency

import timber.log.Timber
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import javax.inject.Singleton

/**
 * In-memory queue for snapshot sync operations that have failed and need retrying.
 *
 * Thread-safe via [CopyOnWriteArrayList]. Operations are kept in memory until
 * successfully retried or they exceed [FailedSnapshotOperation.MAX_RETRY_COUNT].
 */
@Singleton
class SnapshotRetryQueue @Inject constructor() {

    private val queue = CopyOnWriteArrayList<FailedSnapshotOperation>()

    /** Adds a failed operation to the retry queue. */
    fun enqueue(operation: FailedSnapshotOperation) {
        queue.add(operation)
        Timber.d(
            "SnapshotRetryQueue: Enqueued retry for invoice=${operation.invoiceId} " +
                "type=${operation.snapshotType} (queue size=${queue.size})"
        )
    }

    /** Returns all pending retryable operations. */
    fun getPending(): List<FailedSnapshotOperation> = queue.filter { it.isRetryable }

    /** Removes the given operation from the queue (after success). */
    fun remove(operation: FailedSnapshotOperation) {
        queue.remove(operation)
    }

    /** Increments retry count and re-adds; removes if max retries exceeded. */
    fun markRetried(operation: FailedSnapshotOperation) {
        queue.remove(operation)
        val incremented = operation.withIncrementedRetry()
        if (incremented.isRetryable) {
            queue.add(incremented)
        } else {
            Timber.w(
                "SnapshotRetryQueue: Dropping invoice=${operation.invoiceId} after " +
                    "${operation.retryCount} retries"
            )
        }
    }

    /** Returns the current queue size. */
    val size: Int get() = queue.size
}
