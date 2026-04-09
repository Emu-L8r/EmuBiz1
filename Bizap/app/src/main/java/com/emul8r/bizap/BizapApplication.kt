package com.emul8r.bizap

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.*
import com.emul8r.bizap.data.local.InvoiceDao
import com.emul8r.bizap.data.repository.SnapshotSyncHelper
import com.emul8r.bizap.data.worker.ExchangeRateWorker
import com.emul8r.bizap.data.worker.SyncWorker
import com.emul8r.bizap.domain.repository.CurrencyRepository
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import com.emul8r.bizap.utils.CrashlyticsTree
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class BizapApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var currencyRepository: CurrencyRepository

    @Inject
    lateinit var invoiceDao: InvoiceDao

    @Inject
    lateinit var snapshotSyncHelper: SnapshotSyncHelper

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        // 🪵 INITIALIZE LOGGING FRAMEWORK (Fast, synchronous)
        initializeLogging()

        // 📊 INITIALIZE FIREBASE ANALYTICS (Fast, handles own threading)
        initializeAnalytics()

        // 🚀 ASYNC INITIALIZATION (Off-main-thread to prevent skipped frames)
        applicationScope.launch {
            performAsyncInitialization()
        }

        // ⏰ SCHEDULE BACKGROUND JOBS (Fast, WorkManager handles threading)
        scheduleExchangeRateUpdates()
        scheduleSyncWorker()
    }

    /**
     * CONSOLIDATED ASYNC INITIALIZATION
     * ================================
     * Moves all heavy data-layer initialization to Dispatchers.IO.
     * This prevents the "Skipped Frames" warning on emulator/tablet startup.
     */
    private suspend fun performAsyncInitialization() = withContext(Dispatchers.IO) {
        try {
            Timber.d("🚀 Starting Async Initialization...")

            // 💱 SEED CURRENCIES
            Timber.d("💱 Seeding currencies...")
            currencyRepository.seedDefaultCurrencies()

            // 📸 BACKFILL SNAPSHOTS
            val prefs = getSharedPreferences("bizap_prefs", MODE_PRIVATE)
            val alreadyBackfilled = prefs.getBoolean("snapshots_backfilled", false)

            if (!alreadyBackfilled) {
                Timber.i("📸 Starting snapshot backfill for existing invoices...")
                val invoices = invoiceDao.getAllInvoiceEntities()
                invoices.forEach { invoice ->
                    snapshotSyncHelper.syncAllSnapshots(invoice, invoice.businessProfileId)
                }
                prefs.edit().putBoolean("snapshots_backfilled", true).apply()
                Timber.d("✅ Snapshot backfill complete")
            } else {
                Timber.d("✅ Snapshots already backfilled, skipping...")
            }

            Timber.d("✅ Async Initialization Successful")
        } catch (e: Exception) {
            Timber.e(e, "❌ Async Initialization Failed")
        }
    }

    private fun initializeLogging() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            Timber.plant(com.emul8r.bizap.utils.logging.FileLoggingTree(this))
            Timber.plant(CrashlyticsTree())
            Timber.d("🚀 Bizap initialized in DEBUG mode.")
        } else {
            Timber.plant(com.emul8r.bizap.utils.logging.FileLoggingTree(this))
            Timber.plant(CrashlyticsTree())
        }
    }

    private fun initializeAnalytics() {
        try {
            FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(true)
            initializeCrashlytics()
        } catch (e: Exception) {
            Timber.w(e, "⚠️ Firebase Analytics initialization failed")
        }
    }

    private fun initializeCrashlytics() {
        try {
            val crashlytics = com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance()
            crashlytics.isCrashlyticsCollectionEnabled = true
            crashlytics.setCustomKey("app_version", BuildConfig.VERSION_NAME)
            crashlytics.setCustomKey("debug_mode", BuildConfig.DEBUG)
        } catch (e: Exception) {
            Timber.w(e, "⚠️ Firebase Crashlytics initialization failed")
        }
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

    private fun scheduleSyncWorker() {
        SyncWorker.enqueueOneShot(this)
    }
}
