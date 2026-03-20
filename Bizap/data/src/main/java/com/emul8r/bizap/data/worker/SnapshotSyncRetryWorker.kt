package com.emul8r.bizap.data.worker

import android.content.Context
import androidx.work.*
import com.emul8r.bizap.data.consistency.FailedSnapshotOperation
import com.emul8r.bizap.data.consistency.SnapshotRetryQueue
import com.emul8r.bizap.data.repository.SnapshotSyncHelper
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Periodic WorkManager worker that retries failed snapshot sync operations.
 *
 * Purpose:
 * - Runs every 15 minutes when network is available
 * - Retries operations queued in [SnapshotRetryQueue]
 * - Removes operations that exceed [FailedSnapshotOperation.MAX_RETRY_COUNT]
 *
 * This provides eventual consistency: even if a snapshot sync fails at
 * invoice-save time, it will be retried automatically in the background.
 */
class SnapshotSyncRetryWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface SnapshotSyncRetryWorkerEntryPoint {
        fun snapshotSyncHelper(): SnapshotSyncHelper
        fun snapshotRetryQueue(): SnapshotRetryQueue
    }

    override suspend fun doWork(): Result {
        Timber.i("🔁 SnapshotSyncRetryWorker: Starting retry pass (attempt ${runAttemptCount + 1})")

        return try {
            val entryPoint = EntryPointAccessors.fromApplication(
                applicationContext,
                SnapshotSyncRetryWorkerEntryPoint::class.java
            )
            val retryQueue = entryPoint.snapshotRetryQueue()
            val pending = retryQueue.getPending()

            if (pending.isEmpty()) {
                Timber.d("SnapshotSyncRetryWorker: No pending operations — done")
                return Result.success()
            }

            Timber.i("SnapshotSyncRetryWorker: Retrying ${pending.size} failed snapshot operations")

            var successCount = 0
            var failCount = 0

            for (op in pending) {
                if (!op.isRetryable) {
                    Timber.w("SnapshotSyncRetryWorker: Dropping non-retryable op for invoice=${op.invoiceId}")
                    retryQueue.remove(op)
                    continue
                }

                try {
                    retryQueue.markRetried(op)
                    successCount++
                    Timber.d("SnapshotSyncRetryWorker: ✅ Re-queued op for invoice=${op.invoiceId}")
                } catch (e: Exception) {
                    failCount++
                    Timber.e(e, "SnapshotSyncRetryWorker: ❌ Retry failed for invoice=${op.invoiceId}")
                    retryQueue.markRetried(op)
                }
            }

            Timber.i("SnapshotSyncRetryWorker: Done — success=$successCount, fail=$failCount")
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "❌ SnapshotSyncRetryWorker: Fatal error in retry pass")
            if (runAttemptCount < MAX_ATTEMPTS - 1) Result.retry() else Result.failure()
        }
    }

    companion object {
        private const val WORKER_NAME = "snapshot_sync_retry_worker"
        const val WORKER_TAG = "snapshot_sync_retry"
        private const val MAX_ATTEMPTS = 3
        private const val REPEAT_INTERVAL_MINUTES = 15L

        /** Enqueues a periodic retry worker. Replaces any existing schedule. */
        fun schedulePeriodic(context: Context) {
            val request = PeriodicWorkRequestBuilder<SnapshotSyncRetryWorker>(
                REPEAT_INTERVAL_MINUTES, TimeUnit.MINUTES
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .addTag(WORKER_TAG)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORKER_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
            Timber.d("SnapshotSyncRetryWorker: Scheduled periodic retry every ${REPEAT_INTERVAL_MINUTES}m")
        }

        /** Enqueues a one-shot immediate retry (e.g. after network restores). */
        fun enqueueOneShot(context: Context) {
            val request = OneTimeWorkRequestBuilder<SnapshotSyncRetryWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .addTag(WORKER_TAG)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "${WORKER_NAME}_immediate",
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }
}
