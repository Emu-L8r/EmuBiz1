package com.emul8r.bizap.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.emul8r.bizap.domain.usecase.SyncPendingOperationsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * WorkManager worker that processes the offline operation queue.
 *
 * Triggered by:
 * - [OfflineQueueRepositoryImpl.triggerSync] (one-shot, on-demand)
 * - Network connectivity change (via [NetworkConstraint])
 *
 * Implements exponential back-off so transient failures are retried
 * without hammering the server.
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val syncPendingOperationsUseCase: SyncPendingOperationsUseCase
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        Timber.d("🔄 SyncWorker: Processing offline queue (attempt ${runAttemptCount + 1}/$MAX_ATTEMPTS)…")

        return try {
            syncPendingOperationsUseCase()
            Timber.d("✅ SyncWorker: Queue processed successfully")
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "❌ SyncWorker: Failed to process queue")
            if (runAttemptCount < MAX_ATTEMPTS - 1) {
                Result.retry()
            } else {
                Timber.e("❌ SyncWorker: Max attempts ($MAX_ATTEMPTS) reached, giving up")
                Result.failure()
            }
        }
    }

    companion object {
        const val WORKER_TAG = "offline_sync"
        private const val WORKER_NAME = "offline_sync_worker"
        // Total number of allowed attempts (1 initial run + MAX_ATTEMPTS-1 retries).
        // runAttemptCount is 0-indexed, so the final attempt is runAttemptCount == MAX_ATTEMPTS-1.
        private const val MAX_ATTEMPTS = 5

        /** Network-constrained one-shot sync request (triggered on demand). */
        fun buildOneShotRequest(): OneTimeWorkRequest =
            OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .addTag(WORKER_TAG)
                .build()

        /**
         * Enqueues a one-shot sync. Replaces any existing queued request so
         * multiple rapid calls are coalesced into a single run.
         */
        fun enqueueOneShot(context: Context) {
            WorkManager.getInstance(context).enqueueUniqueWork(
                WORKER_NAME,
                ExistingWorkPolicy.REPLACE,
                buildOneShotRequest()
            )
        }
    }
}
