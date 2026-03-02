package com.emul8r.bizap

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.*
import com.emul8r.bizap.data.worker.ExchangeRateWorker
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import com.emul8r.bizap.utils.CrashlyticsTree
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
        
        // 🪵 TASK 1: INITIALIZE LOGGING
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            Timber.d("🚀 Bizap initialized in DEBUG mode. Timber logging enabled.")
        } else {
            // Plant a release tree that forwards errors to Crashlytics
            Timber.plant(CrashlyticsTree())
        }

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
