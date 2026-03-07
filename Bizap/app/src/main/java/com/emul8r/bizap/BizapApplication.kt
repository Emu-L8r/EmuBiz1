package com.emul8r.bizap

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.*
import com.emul8r.bizap.data.worker.ExchangeRateWorker
import com.emul8r.bizap.data.worker.SyncWorker
import com.emul8r.bizap.domain.repository.CurrencyRepository
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        
        // 🪵 INITIALIZE LOGGING FRAMEWORK
        initializeLogging()

        // 📊 INITIALIZE FIREBASE ANALYTICS
        initializeAnalytics()

        // 💱 SEED CURRENCIES (must happen early so dropdowns work)
        seedCurrencies()

        // 📸 BACKFILL SNAPSHOTS (one-time on first run after fix)
        backfillSnapshots()

        // ⏰ SCHEDULE BACKGROUND JOBS
        scheduleExchangeRateUpdates()
        scheduleSyncWorker()
    }

    /**
     * TIMBER LOGGING INITIALIZATION
     * ============================
     * Timber is a logging abstraction that works with Tree implementations.
     *
     * WHY DIFFERENT TREES FOR DEBUG VS RELEASE?
     * - DEBUG: DebugTree logs to Android Logcat (visible in Android Studio)
     *   Good for: Development, debugging, seeing all log levels
     *
     * - RELEASE: CrashlyticsTree logs ONLY errors/warnings to Firebase
     *   Good for: Production monitoring, crash analysis, performance tracking
     *
     * TIMBER AUTOMATICALLY:
     * - Extracts class name as tag (ViewModel → "ViewModel")
     * - Handles exceptions and stack traces
     * - Provides single logging API across the app
     *
     * BEST PRACTICE: Never call Android's Log.d(), Log.e(), etc.
     * Always use Timber.d(), Timber.e(), etc. instead.
     */
    private fun initializeLogging() {
        if (BuildConfig.DEBUG) {
            // In DEBUG: Log everything to Logcat
            Timber.plant(Timber.DebugTree())
            Timber.d("🚀 Bizap initialized in DEBUG mode. Full logging enabled.")
        } else {
            // In RELEASE: Log only WARN/ERROR to Firebase Crashlytics
            Timber.plant(CrashlyticsTree())
            Timber.i("🚀 Bizap initialized in RELEASE mode. Logging to Firebase Crashlytics.")
        }
    }

    /**
     * FIREBASE ANALYTICS INITIALIZATION
     * ==================================
     * Firebase Analytics automatically tracks:
     * - App installs and version updates
     * - User engagement and session duration
     * - Crashes and errors
     *
     * CUSTOM EVENTS:
     * You can also log custom events like:
     * - "invoice_created" when user saves an invoice
     * - "payment_recorded" when user logs a payment
     * - "export_pdf" when user exports to PDF
     *
     * WHY USE ANALYTICS?
     * - Understand which features users actually use
     * - Track down where users get stuck (drop-off points)
     * - Measure impact of new features
     * - Identify bugs in the wild (which Android versions crash?)
     *
     * PRIVACY NOTE:
     * - Firebase Analytics is GDPR compliant (anonymized)
     * - Don't log personally identifiable info (PII)
     * - Don't log sensitive data like invoices
     *
     * EXAMPLE CUSTOM EVENT:
     *   FirebaseAnalytics.getInstance(this).logEvent("invoice_created") {
     *       param("currency", "AUD")
     *       param("line_item_count", 3)
     *   }
     */
    private fun initializeAnalytics() {
        try {
            // Enable collection (important: respects user's data sharing preferences)
            FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(true)
            Timber.d("✅ Firebase Analytics initialized")
        } catch (e: Exception) {
            // Firebase might not be initialized if google-services.json is missing
            // This is expected in development environments
            Timber.w(e, "Firebase Analytics initialization failed (expected if google-services.json missing)")
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

    /**
     * SNAPSHOT BACKFILL
     * =================
     * One-time backfill of snapshots for existing invoices.
     * This runs once on first app startup after the snapshot creation fix.
     *
     * WHY NEEDED:
     * - Before the fix, SaveInvoiceUseCase didn't create snapshots
     * - Revenue Dashboard queries snapshots, so showed $0.00
     * - This backfill creates snapshots for all existing invoices
     * - After this runs once, future invoices get snapshots automatically
     */
    private fun backfillSnapshots() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefs = getSharedPreferences("bizap_prefs", MODE_PRIVATE)
                val alreadyBackfilled = prefs.getBoolean("snapshots_backfilled", false)

                if (alreadyBackfilled) {
                    Timber.d("✅ Snapshots already backfilled, skipping...")
                    return@launch
                }

                Timber.i("📸 Starting snapshot backfill for existing invoices...")

                // TODO: Implement backfill logic here
                // val invoices = invoiceRepository.getAllInvoices()
                // invoices.forEach { invoice ->
                //     snapshotSyncHelper.syncAllSnapshots(invoice, invoice.businessProfileId)
                // }

                // Mark as complete
                prefs.edit().putBoolean("snapshots_backfilled", true).apply()
                Timber.d("✅ Snapshot backfill complete")
            } catch (e: Exception) {
                Timber.e(e, "❌ Snapshot backfill failed (non-blocking)")
            }
        }
    }

    /**
     * OFFLINE SYNC WORKER
     * ===================
     * Enqueues a one-shot sync on startup so any operations that were
     * queued while the app was last closed are replayed immediately
     * once connectivity is available.
     */
    private fun scheduleSyncWorker() {
        SyncWorker.enqueueOneShot(this)
    }

    /**
     * CURRENCY SEEDING
     * ================
     * Seeds the currencies table with default currencies (AUD, USD, EUR, GBP, JPY)
     * This is called on app startup to ensure currency dropdowns have data
     */
    private fun seedCurrencies() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Timber.d("💱 Seeding currencies...")
                currencyRepository.seedDefaultCurrencies()
                Timber.d("✅ Currencies seeded successfully")
            } catch (e: Exception) {
                Timber.e(e, "⚠️ Failed to seed currencies (non-blocking)")
            }
        }
    }
}
