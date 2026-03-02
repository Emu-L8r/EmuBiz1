package com.emul8r.bizap.data.sync

import android.content.Context
import androidx.work.*
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Schedules sync work with WorkManager.
 */
@Singleton
class SyncScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val workManager = WorkManager.getInstance(context)
    
    /**
     * Schedules a one-time sync that only runs when the device is online.
     */
    fun scheduleSyncOnConnectivity() {
        Timber.d("📡 SyncScheduler: Scheduling one-time sync for connectivity event...")
        
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
            
        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .build()
            
        workManager.enqueueUniqueWork(
            "offline_sync_one_time",
            ExistingWorkPolicy.REPLACE,
            syncRequest
        )
    }

    /**
     * Schedules a periodic background sync to ensure data consistency.
     */
    fun schedulePeriodicSync() {
        Timber.d("🔄 SyncScheduler: Scheduling periodic background sync (every 4h)...")
        
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()
            
        val periodicRequest = PeriodicWorkRequestBuilder<SyncWorker>(4, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()
            
        workManager.enqueueUniquePeriodicWork(
            "periodic_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            periodicRequest
        )
    }
}
