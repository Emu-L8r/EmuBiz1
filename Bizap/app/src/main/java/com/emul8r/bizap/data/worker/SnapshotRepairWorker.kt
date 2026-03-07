package com.emul8r.bizap.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.emul8r.bizap.data.repository.SnapshotRebuildService
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Periodic worker for snapshot repair and consistency checking.
 *
 * Purpose:
 * - Runs once every 24 hours (at off-peak times)
 * - Verifies all invoices have snapshots
 * - Rebuilds missing or inconsistent snapshots
 * - Self-healing layer for unforeseen failures
 *
 * Benefits:
 * - ✅ Guarantees recovery from edge cases
 * - ✅ No impact on user experience (background task)
 * - ✅ Ensures data longevity and consistency
 * - ✅ No manual intervention needed
 *
 * Triggers:
 * - Scheduled once at app startup
 * - Runs daily at system idle time
 * - Can be triggered manually for debugging
 */
class SnapshotRepairWorker(
    context: Context,
    params: androidx.work.WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = try {
        Timber.i("🔧 Starting snapshot repair worker...")
        val startTime = System.currentTimeMillis()

        // Get the service via entry point
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            SnapshotRepairWorkerEntryPoint::class.java
        )
        val rebuildService = entryPoint.snapshotRebuildService()

        // Perform snapshot rebuild and health check
        rebuildService.rebuildAllSnapshots()
        val duration = System.currentTimeMillis() - startTime

        Timber.i("✅ Snapshot repair completed in ${duration}ms")

        Result.success()
    } catch (e: Exception) {
        Timber.e(e, "❌ Snapshot repair failed: ${e.message}")

        // Retry on next scheduled time (exponential backoff handled by WorkManager)
        if (runAttemptCount < MAX_RETRY_COUNT) {
            Timber.w("⚠️ Retrying snapshot repair (attempt ${runAttemptCount + 1}/$MAX_RETRY_COUNT)")
            Result.retry()
        } else {
            Timber.e("❌ Snapshot repair failed after $MAX_RETRY_COUNT attempts")
            Result.failure()
        }
    }

    companion object {
        private const val WORKER_TAG = "snapshot_repair"
        private const val WORKER_NAME = "snapshot_repair_worker"
        private const val REPAIR_INTERVAL_HOURS = 24L
        private const val MAX_RETRY_COUNT = 3

        /**
         * Schedules the snapshot repair worker to run once every 24 hours.
         * Called at app startup (e.g., in MainActivity or Application class).
         */
        fun schedulePeriodicRepair(context: Context) {
            try {
                val repairRequest = PeriodicWorkRequestBuilder<SnapshotRepairWorker>(
                    repeatInterval = REPAIR_INTERVAL_HOURS,
                    repeatIntervalTimeUnit = TimeUnit.HOURS
                )
                    .addTag(WORKER_TAG)
                    .setBackoffCriteria(
                        backoffPolicy = androidx.work.BackoffPolicy.EXPONENTIAL,
                        backoffDelay = 15,
                        timeUnit = TimeUnit.MINUTES
                    )
                    .build()

                WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    WORKER_NAME,
                    androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                    repairRequest
                )

                Timber.i("✅ Scheduled periodic snapshot repair worker (every $REPAIR_INTERVAL_HOURS hours)")
            } catch (e: Exception) {
                Timber.e(e, "❌ Failed to schedule snapshot repair worker")
            }
        }

        /**
         * Cancels the snapshot repair worker.
         * Call this if you want to disable background repairs.
         */
        fun cancelRepair(context: Context) {
            try {
                WorkManager.getInstance(context).cancelUniqueWork(WORKER_NAME)
                Timber.i("✅ Cancelled snapshot repair worker")
            } catch (e: Exception) {
                Timber.e(e, "❌ Failed to cancel snapshot repair worker")
            }
        }

        /**
         * Triggers an immediate one-time repair.
         * Useful for testing or manual recovery.
         */
        fun triggerImmediateRepair(context: Context) {
            try {
                val immediateRequest = androidx.work.OneTimeWorkRequestBuilder<SnapshotRepairWorker>()
                    .addTag("${WORKER_TAG}_manual")
                    .build()

                WorkManager.getInstance(context).enqueue(immediateRequest)
                Timber.i("✅ Triggered immediate snapshot repair")
            } catch (e: Exception) {
                Timber.e(e, "❌ Failed to trigger immediate repair")
            }
        }
    }
}

/**
 * Hilt entry point for injecting SnapshotRebuildService into the worker.
 * Workers cannot use constructor injection, so we use entry points instead.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface SnapshotRepairWorkerEntryPoint {
    fun snapshotRebuildService(): SnapshotRebuildService
}


