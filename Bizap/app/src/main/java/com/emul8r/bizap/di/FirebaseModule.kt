package com.emul8r.bizap.di

import android.content.Context
import com.emul8r.bizap.analytics.AppMonitoring
import com.emul8r.bizap.utils.FirebaseEventTracker
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import timber.log.Timber
import javax.inject.Singleton

/**
 * Firebase Module
 *
 * Provides Firebase services as singletons available throughout the app.
 * This enables dependency injection of Firebase Analytics and Firebase Auth everywhere.
 */
@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    /**
     * Provides FirebaseAnalytics instance.
     *
     * Firebase automatically initializes from google-services.json.
     * This is a safe singleton that can be injected anywhere.
     *
     * NOTE: This method is wrapped in error handling to gracefully handle:
     * - Missing google-services.json (development)
     * - Firebase initialization errors
     * - Device with Play Services not installed
     *
     * @return FirebaseAnalytics instance (null if initialization fails)
     */
    @Provides
    @Singleton
    fun provideFirebaseAnalytics(@ApplicationContext context: Context): FirebaseAnalytics? {
        return try {
            val instance = FirebaseAnalytics.getInstance(context)
            Timber.d("✅ FirebaseAnalytics initialized successfully")
            instance
        } catch (e: Exception) {
            Timber.w(e, "⚠️ Failed to initialize FirebaseAnalytics - app will continue without crash reporting")
            null  // Allow app to continue without Firebase
        }
    }

    /**
     * Provides FirebaseAuth instance.
     *
     * Used by [UserIdProvider] to obtain the real Firebase UID.
     * Gracefully handles missing google-services.json in development.
     *
     * @return FirebaseAuth instance
     */
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    /**
     * Provides FirebaseEventTracker utility.
     *
     * Use this to track events consistently throughout the app.
     *
     * The tracker gracefully handles when FirebaseAnalytics fails to initialize:
     * - Still logs to Timber (visible in Logcat)
     * - Silently skips Firebase logging
     * - App continues to function normally
     *
     * Example:
     * ```
     * @Inject
     * lateinit var eventTracker: FirebaseEventTracker
     *
     * fun onInvoiceCreated(invoice: Invoice) {
     *     eventTracker.trackInvoiceCreated(
     *         invoiceId = invoice.id,
     *         customerId = invoice.customerId,
     *         amount = invoice.totalAmount,
     *         currencyCode = invoice.currencyCode,
     *         lineItemCount = invoice.items.size
     *     )
     * }
     * ```
     *
     * @param analytics FirebaseAnalytics instance (nullable if initialization failed)
     * @return FirebaseEventTracker configured with analytics
     */
    @Provides
    @Singleton
    fun provideFirebaseEventTracker(analytics: FirebaseAnalytics?): FirebaseEventTracker {
        if (analytics == null) {
            Timber.w("⚠️ FirebaseEventTracker initialized with null analytics - events will not be sent to Firebase")
        }
        return FirebaseEventTracker(analytics)
    }

    /**
     * Provides FirebaseRemoteConfig configured with a 1-hour minimum fetch interval.
     * In debug builds, the interval is reduced to 0 seconds so changes take effect
     * immediately during development.
     */
    @Provides
    @Singleton
    fun provideFirebaseRemoteConfig(): FirebaseRemoteConfig {
        val config = FirebaseRemoteConfig.getInstance()
        val minFetchIntervalSeconds = if (com.emul8r.bizap.BuildConfig.DEBUG) 0L else 3600L
        val settings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(minFetchIntervalSeconds)
            .build()
        config.setConfigSettingsAsync(settings)
        return config
    }

    /**
     * Provides [AppMonitoring] — the unified production observability layer.
     *
     * Records navigation errors, feature flag changes, performance metrics,
     * security events, and app lifecycle signals to Firebase Analytics +
     * Crashlytics. Both dependencies are nullable — app functions without Firebase.
     *
     * See: [AppMonitoring] and docs/RUNBOOK.md for alert thresholds.
     */
    @Provides
    @Singleton
    fun provideAppMonitoring(
        analytics: FirebaseAnalytics?,
        crashlytics: FirebaseCrashlytics?
    ): AppMonitoring = AppMonitoring(analytics, crashlytics)
}

