package com.emul8r.bizap

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.emul8r.bizap.workers.CleanupWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class BizapApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        // TODO: Re-enable setupRecurringWork() once WorkManager HiltWorker configuration is fixed
        // Currently disabled because CleanupWorker fails to instantiate:
        // "java.lang.NoSuchMethodException: CleanupWorker.<init>[Context, WorkerParameters]"
        // setupRecurringWork()
    }

    private fun setupRecurringWork() {
        // DISABLED: WorkManager HiltWorker integration broken
        // val repeatingRequest = PeriodicWorkRequestBuilder<CleanupWorker>(
        //     1, TimeUnit.DAYS
        // ).build()
        //
        // WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
        //     "cache-cleanup",
        //     ExistingPeriodicWorkPolicy.KEEP,
        //     repeatingRequest
        // )
    }
}
