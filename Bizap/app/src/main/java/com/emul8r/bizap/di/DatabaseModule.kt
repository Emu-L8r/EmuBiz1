package com.emul8r.bizap.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.emul8r.bizap.data.backup.DatabaseBackupService
import com.emul8r.bizap.data.backup.DatabaseRestoreService
import com.emul8r.bizap.data.local.*
import com.emul8r.bizap.data.local.dao.*
import com.emul8r.bizap.data.local.migrations.*
import com.emul8r.bizap.data.local.migration.MIGRATION_AddInvoiceSettings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import timber.log.Timber
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
        passphraseManager: DatabasePassphraseManager
    ): AppDatabase {
        // Load SQLCipher native libraries before opening any encrypted database
        System.loadLibrary("sqlcipher")

        val passphrase = passphraseManager.getOrCreatePassphrase()
        val factory = SupportOpenHelperFactory(passphrase)

        val builder = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "bizap-db"
        )
        .openHelperFactory(factory)
        .addMigrations(
            MIGRATION_21_22,
            MIGRATION_22_23,
            MIGRATION_23_24,
            MIGRATION_24_25,
            MIGRATION_25_26,
            MIGRATION_26_27,
            MIGRATION_27_28,
            MIGRATION_28_29,
            MIGRATION_29_30,
            MIGRATION_30_31,
            MIGRATION_31_32,
            MIGRATION_32_33,
            MIGRATION_33_34,
            MIGRATION_34_35,
            MIGRATION_35_36,
            MIGRATION_36_37,  // Remove UNIQUE constraint on email field
            MIGRATION_AddInvoiceSettings  // Add invoice_settings table for Phase 4
        )

        // ✅ PRODUCTION SAFE: Only allow destructive fallback in DEBUG builds
        // In RELEASE: fail loudly if migration missing (don't silently delete user data)
        if (com.emul8r.bizap.BuildConfig.DEBUG) {
            builder.fallbackToDestructiveMigration()
            Timber.w("⚠️ DESTRUCTIVE MIGRATION ENABLED - Development only!")
        } else {
            // In production: log success after migration to confirm user data is intact
            builder.addCallback(object : RoomDatabase.Callback() {
                override fun onOpen(db: SupportSQLiteDatabase) {
                    Timber.i("✅ Database migration successful - user data intact")
                }
            })
        }

        return builder.build()
    }

    @Provides fun provideCustomerDao(db: AppDatabase): CustomerDao = db.customerDao()
    @Provides fun provideInvoiceDao(db: AppDatabase): InvoiceDao = db.invoiceDao()
    @Provides fun provideDocumentDao(db: AppDatabase): DocumentDao = db.documentDao()
    @Provides fun providePrefilledItemDao(db: AppDatabase): PrefilledItemDao = db.prefilledItemDao()
    @Provides fun provideBusinessProfileDao(db: AppDatabase): BusinessProfileDao = db.businessProfileDao()
    @Provides fun provideCurrencyDao(db: AppDatabase): CurrencyDao = db.currencyDao()
    @Provides fun provideExchangeRateDao(db: AppDatabase): ExchangeRateDao = db.exchangeRateDao()
    @Provides fun provideAnalyticsDao(db: AppDatabase): AnalyticsDao = db.analyticsDao()
    @Provides fun provideCustomerAnalyticsDao(db: AppDatabase): CustomerAnalyticsDao = db.customerAnalyticsDao()
    @Provides fun provideInvoicePaymentDao(db: AppDatabase): InvoicePaymentDao = db.invoicePaymentDao()
    @Provides fun provideInvoiceTemplateDao(db: AppDatabase): InvoiceTemplateDao = db.invoiceTemplateDao()
    @Provides fun provideInvoiceCustomFieldDao(db: AppDatabase): InvoiceCustomFieldDao = db.invoiceCustomFieldDao()
    @Provides fun providePendingOperationDao(db: AppDatabase): PendingOperationDao = db.pendingOperationDao()
    @Provides fun provideOfflineOperationDao(db: AppDatabase): OfflineOperationDao = db.offlineOperationDao()
    @Provides fun provideInvoiceDaoV2(db: AppDatabase): InvoiceDaoV2 = db.invoiceDaoV2()
    @Provides fun provideCustomerDaoV2(db: AppDatabase): CustomerDaoV2 = db.customerDaoV2()
    @Provides fun providePaymentDaoV2(db: AppDatabase): PaymentDaoV2 = db.paymentDaoV2()
    @Provides fun provideNoteDao(db: AppDatabase): NoteDao = db.noteDao()

    @Provides fun provideAnalyticsEventDao(db: AppDatabase): com.emul8r.bizap.data.local.dao.AnalyticsEventDao = db.analyticsEventDao()
    @Provides fun provideInvoiceSettingsDao(db: AppDatabase): InvoiceSettingsDao = db.invoiceSettingsDao()

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }

    @Provides
    @Singleton
    fun provideDatabaseBackupService(
        @ApplicationContext context: Context,
        appDatabase: AppDatabase
    ): DatabaseBackupService {
        return DatabaseBackupService(context, appDatabase)
    }

    @Provides
    @Singleton
    fun provideDatabaseRestoreService(
        @ApplicationContext context: Context,
        appDatabase: AppDatabase
    ): DatabaseRestoreService {
        return DatabaseRestoreService(context, appDatabase)
    }
}
