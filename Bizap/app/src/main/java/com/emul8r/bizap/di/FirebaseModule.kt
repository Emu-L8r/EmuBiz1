package com.emul8r.bizap.di

import android.content.Context
import com.emul8r.bizap.utils.FirebaseEventTracker
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Firebase Module
 *
 * Provides Firebase services as singletons available throughout the app.
 * This enables dependency injection of Firebase Analytics everywhere.
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
     * @return FirebaseAnalytics instance
     */
    @Provides
    @Singleton
    fun provideFirebaseAnalytics(@ApplicationContext context: Context): FirebaseAnalytics {
        return FirebaseAnalytics.getInstance(context)
    }

    /**
     * Provides FirebaseEventTracker utility.
     *
     * Use this to track events consistently throughout the app.
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
     * @param analytics FirebaseAnalytics instance
     * @return FirebaseEventTracker configured with analytics
     */
    @Provides
    @Singleton
    fun provideFirebaseEventTracker(analytics: FirebaseAnalytics): FirebaseEventTracker {
        return FirebaseEventTracker(analytics)
    }
}



