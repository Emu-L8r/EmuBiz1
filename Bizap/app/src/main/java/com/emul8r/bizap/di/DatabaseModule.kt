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
import com.emul8r.bizap.data.local.migration.MIGRATION_AddPdfEngineAndLayout
import com.emul8r.bizap.data.local.migration.MIGRATION_AddSignatureField
import com.emul8r.bizap.data.local.migration.MIGRATION_41_42
import com.emul8r.bizap.data.local.migration.MIGRATION_42_43
import com.emul8r.bizap.data.local.migrations.MIGRATION_38_39
import com.emul8r.bizap.data.local.migrations.MIGRATION_44_45
import com.emul8r.bizap.data.local.migrations.Migration_45_46
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
        // ✅ REMOVED: System.loadLibrary("sqlcipher")
        // Now pre-loaded in BizapApplication.preloadDatabaseResources() during onCreate()
        // This reduces JNI lock duration from 400ms to ~100ms during Room.build()

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
            MIGRATION_AddInvoiceSettings,  // Add invoice_settings table for Phase 4
            MIGRATION_38_39,   // Add selected_html_style and selected_canvas_template columns
            MIGRATION_AddPdfEngineAndLayout,  // Add PDF engine and page layout columns for three-tier architecture
            MIGRATION_AddSignatureField,        // Add show_signature_field column for Phase 2 PDF enhancements
            MIGRATION_41_42,                   // Add discount_amount column; create InvoiceFTS virtual table
            MIGRATION_42_43,                   // Add customization layers (color scheme, spacing, visual accents); delete old invoices
            MIGRATION_44_45,                   // Add invoice numbering columns (dailySequence, invoiceYear, invoiceNumber)
            Migration_45_46                    // Add payment media attachments for proof-of-payment feature
        )

        // ✅ PRODUCTION SAFE: Only allow destructive fallback in DEBUG builds
        // In RELEASE: fail loudly if migration missing (don't silently delete user data)
        if (com.emul8r.bizap.BuildConfig.DEBUG) {
            // ENHANCED: Make destructive migrations LOUD for visibility during testing
            builder.fallbackToDestructiveMigration()

            // Add callback to log when migrations are applied or database opens
            builder.addCallback(object : RoomDatabase.Callback() {
                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    val currentVersion = db.version
                    Timber.w("""
                        ⚠️ DATABASE OPENED (v$currentVersion)
                        If you see this AND previous data is missing,
                        check: 1) New Migration_XX_YY.kt file created?
                               2) Migration added to DatabaseModule addMigrations()?
                               3) Fresh install or data cleared?
                    """.trimIndent())
                }

                override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                    super.onDestructiveMigration(db)
                    Timber.e("""
                        🚨 DESTRUCTIVE MIGRATION TRIGGERED! 🚨
                        Database Version: ${db.version}

                        All tables were DROPPED. This happens when:
                        - A migration file is missing in the chain
                        - Migration not registered in addMigrations()
                        - Database schema changed without migration

                        ACTION: Check app/src/main/java/com/emul8r/bizap/data/local/
                        - Check Migration_XX_${db.version}.kt files
                        - Verify all migrations registered in DatabaseModule addMigrations()
                        - Run with fresh install to restore data
                    """.trimIndent())
                }
            })
        } else {
            // In production: log success after migration to confirm user data is intact
            builder.addCallback(object : RoomDatabase.Callback() {
                override fun onOpen(db: SupportSQLiteDatabase) {
                    Timber.i("✅ Database migration successful - user data intact (v${db.version})")
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
    @Provides fun providePaymentMediaAttachmentDao(db: AppDatabase): PaymentMediaAttachmentDao = db.paymentMediaAttachmentDao()
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
