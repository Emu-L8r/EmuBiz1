package com.emul8r.bizap

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.*
import com.emul8r.bizap.data.worker.ExchangeRateWorker
import dagger.hilt.android.HiltAndroidApp
import java.time.Duration
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
        scheduleExchangeRateUpdates()
    }

    private fun scheduleExchangeRateUpdates() {
        val exchangeRateWork = PeriodicWorkRequestBuilder<ExchangeRateWorker>(
            24, TimeUnit.HOURS
        ).build()
        
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "exchange_rate_update",
            ExistingPeriodicWorkPolicy.KEEP,
            exchangeRateWork
        )
    }
}
