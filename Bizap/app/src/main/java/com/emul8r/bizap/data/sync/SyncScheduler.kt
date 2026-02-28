package com.emul8r.bizap.data.sync

import android.content.Context
import androidx.work.*
import timber.log.Timber

/**
 * Schedules sync work with WorkManager.
 */
class SyncScheduler(private val context: Context) {
    
    private val workManager = WorkManager.getInstance(context)
    
    /**
     * Schedules a one-time sync that only runs when the device is online.
     */
    fun scheduleSyncOnConnectivity() {
        Timber.d("📡 SyncScheduler: Scheduling sync for next online event...")
        
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
            
        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .build()
            
        workManager.enqueueUniqueWork(
            "offline_sync",
            ExistingWorkPolicy.KEEP,
            syncRequest
        )
    }
}
