package com.emul8r.bizap.di

import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that exposes [FirebaseCrashlytics] for injection.
 *
 * Any ViewModel or service that needs to record non-fatal exceptions can
 * declare `FirebaseCrashlytics` as a constructor parameter instead of
 * calling `FirebaseCrashlytics.getInstance()` directly, keeping code
 * testable and consistent with the project's Hilt-everywhere convention.
 *
 * **Example usage in a ViewModel:**
 * ```kotlin
 * @HiltViewModel
 * class CreateInvoiceViewModel @Inject constructor(
 *     private val invoiceRepository: InvoiceRepository,
 *     private val crashlytics: FirebaseCrashlytics
 * ) : ViewModel() {
 *
 *     fun save(invoice: Invoice) {
 *         viewModelScope.launch {
 *             try {
 *                 invoiceRepository.save(invoice)
 *             } catch (e: Exception) {
 *                 crashlytics.recordException(e)
 *                 Timber.e(e, "Failed to save invoice")
 *             }
 *         }
 *     }
 * }
 * ```
 */
@Module
@InstallIn(SingletonComponent::class)
object CrashlyticsModule {

    @Provides
    @Singleton
    fun provideFirebaseCrashlytics(): FirebaseCrashlytics =
        FirebaseCrashlytics.getInstance()
}
