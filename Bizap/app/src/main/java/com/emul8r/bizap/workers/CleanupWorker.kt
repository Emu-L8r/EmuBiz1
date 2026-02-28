package com.emul8r.bizap.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.emul8r.bizap.domain.repository.InvoiceRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

@HiltWorker
class CleanupWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val invoiceRepository: InvoiceRepository 
) : CoroutineWorker(appContext, workerParams) { // <--- MUST extend CoroutineWorker

    override suspend fun doWork(): Result {
        return try {
            // Your cleanup logic
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
