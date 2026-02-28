package com.emul8r.bizap.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.emul8r.bizap.data.local.PendingOperationDao
import com.emul8r.bizap.data.repository.OfflineSyncQueue
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val pendingOperationDao: PendingOperationDao,
    private val syncQueue: OfflineSyncQueue
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Timber.d("🔄 SyncWorker: Starting background synchronization...")
        
        val pendingOps = pendingOperationDao.getPendingOperations()
        if (pendingOps.isEmpty()) {
            Timber.d("✅ SyncWorker: No pending operations found. Finishing.")
            return Result.success()
        }

        Timber.d("📦 SyncWorker: Found ${pendingOps.size} operations to sync.")

        var failures = 0
        pendingOps.forEach { operation ->
            try {
                // Here we would call the syncService.
                // For now, we are simulating a successful sync to prove the queue logic.
                syncQueue.markSynced(operation.id)
            } catch (e: Exception) {
                failures++
                syncQueue.markFailed(operation.id, e.message ?: "Unknown Error")
            }
        }

        return if (failures > 0) Result.retry() else Result.success()
    }
}
