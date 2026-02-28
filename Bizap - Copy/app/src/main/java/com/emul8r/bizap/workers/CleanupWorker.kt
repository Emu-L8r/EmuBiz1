package com.emul8r.bizap.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

@HiltWorker
class CleanupWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        return@withContext try {
            val cacheDir = applicationContext.cacheDir
            val cutoff = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1)

            cacheDir.listFiles()?.forEach { file ->
                if (file.isFile && file.name.endsWith(".pdf") && file.lastModified() < cutoff) {
                    file.delete()
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
