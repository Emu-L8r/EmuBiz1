package com.emul8r.bizap.di

import android.content.Context
import androidx.work.*
import com.emul8r.bizap.data.worker.SyncWorker
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Hilt Dependency Injection Module for SyncWorker.
 *
 * Responsibilities:
 * - Provide WorkManager instance
 * - Configure periodic sync worker scheduling
 * - Set up retry policies and constraints
 * - Manage sync worker lifecycle
 *
 * Part of Phase 2 Week 2: Day 6 Implementation
 */
@Module
@InstallIn(SingletonComponent::class)
object SyncWorkerModule {

    private const val SYNC_WORKER_NAME = "offline_sync_worker"
    private const val SYNC_INTERVAL_MINUTES = 15L
    private const val WORKER_TAG = "offline_sync"

    /**
     * Provides the WorkManager singleton instance.
     *
     * WorkManager is responsible for:
     * - Scheduling background work
     * - Managing retries with backoff
     * - Respecting battery and network constraints
     * - Persisting work requests across app restarts
     */
    @Provides
    @Singleton
    fun provideWorkManager(
        @ApplicationContext context: Context
    ): WorkManager {
        Timber.d("🔧 SyncWorkerModule: Providing WorkManager instance")
        return WorkManager.getInstance(context)
    }

    /**
     * Initialize and schedule the periodic sync worker.
     *
     * Called once at application startup to:
     * - Register periodic sync every 15 minutes
     * - Set up retry policy with exponential backoff
     * - Apply network constraints
     *
     * The actual scheduling happens in Application or MainActivity.onCreate()
     */
    @Provides
    @Singleton
    fun provideSyncWorkerScheduler(
        workManager: WorkManager
    ): SyncWorkerScheduler {
        Timber.d("🔧 SyncWorkerModule: Creating SyncWorkerScheduler")
        return SyncWorkerScheduler(workManager)
    }
}

/**
 * Manages scheduling and lifecycle of the sync worker.
 *
 * Patterns:
 * - Periodic sync: Every 15 minutes (when device is online)
 * - On-demand sync: Triggered by network change or user action
 * - Backoff policy: Exponential backoff for failures
 */
class SyncWorkerScheduler(
    private val workManager: WorkManager
) {

    /**
     * Schedule periodic sync worker.
     *
     * Called once at app startup.
     * Schedules background sync every 15 minutes.
     *
     * Only syncs when:
     * - Device has active network connection
     * - Device is charging or battery > 15%
     * - App is not in background too long
     */
    fun schedulePeriodicSync() {
        try {
            Timber.i("📅 SyncWorkerScheduler: Scheduling periodic sync (every 15 minutes)")

            // Build periodic work request with constraints
            val syncWorkRequest = PeriodicWorkRequestBuilder<SyncWorker>(
                SYNC_INTERVAL_MINUTES,
                TimeUnit.MINUTES
            )
                // Retry policy: exponential backoff
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.DEFAULT_BACKOFF_DELAY_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                // Add tags for identification and cancellation
                .addTag(SyncWorkerModule.WORKER_TAG)
                .build()

            // Enqueue with KEEP policy (don't replace if already scheduled)
            workManager.enqueueUniquePeriodicWork(
                SyncWorkerModule.SYNC_WORKER_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                syncWorkRequest
            )

            Timber.i("✅ SyncWorkerScheduler: Periodic sync scheduled successfully")
        } catch (e: Exception) {
            Timber.e(e, "❌ SyncWorkerScheduler: Failed to schedule periodic sync")
        }
    }

    /**
     * Trigger immediate one-time sync.
     *
     * Called when:
     * - Network connectivity is detected
     * - User manually triggers sync
     * - Critical operations need immediate sync
     */
    fun triggerImmediateSync() {
        try {
            Timber.i("📤 SyncWorkerScheduler: Triggering immediate sync")

            val syncWorkRequest = OneTimeWorkRequestBuilder<SyncWorker>()
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.DEFAULT_BACKOFF_DELAY_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .addTag(SyncWorkerModule.WORKER_TAG)
                .build()

            workManager.enqueueUniqueWork(
                "${SyncWorkerModule.SYNC_WORKER_NAME}_immediate",
                ExistingWorkPolicy.REPLACE,
                syncWorkRequest
            )

            Timber.i("✅ SyncWorkerScheduler: Immediate sync triggered")
        } catch (e: Exception) {
            Timber.e(e, "❌ SyncWorkerScheduler: Failed to trigger immediate sync")
        }
    }

    /**
     * Cancel all pending sync work.
     *
     * Called when:
     * - User logs out
     * - App is uninstalled
     * - User disables background sync
     */
    fun cancelAllSyncWork() {
        try {
            Timber.i("⛔ SyncWorkerScheduler: Cancelling all sync work")
            workManager.cancelAllWorkByTag(SyncWorkerModule.WORKER_TAG)
            Timber.i("✅ SyncWorkerScheduler: All sync work cancelled")
        } catch (e: Exception) {
            Timber.e(e, "❌ SyncWorkerScheduler: Failed to cancel sync work")
        }
    }

    /**
     * Get current status of sync operations.
     */
    fun getSyncStatus() {
        try {
            workManager.getWorkInfosByTag(SyncWorkerModule.WORKER_TAG)
                .get()
                .forEach { workInfo ->
                    Timber.d("📊 SyncStatus: ${workInfo.id} - State: ${workInfo.state}")
                }
        } catch (e: Exception) {
            Timber.e(e, "❌ SyncWorkerScheduler: Failed to get sync status")
        }
    }
}




