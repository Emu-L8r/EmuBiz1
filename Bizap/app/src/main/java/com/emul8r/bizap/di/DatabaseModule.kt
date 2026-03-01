package com.emul8r.bizap.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.emul8r.bizap.data.local.*
import com.emul8r.bizap.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "bizap-db"
        )
        .addMigrations(
            MIGRATION_2_3, 
            MIGRATION_3_4, 
            MIGRATION_4_5, 
            MIGRATION_5_6, 
            MIGRATION_6_7,
            MIGRATION_7_8,
            MIGRATION_8_9,
            MIGRATION_9_10,
            MIGRATION_10_11,
            MIGRATION_11_12,
            MIGRATION_12_13,
            MIGRATION_13_14,
            MIGRATION_14_15,
            MIGRATION_15_16,
            MIGRATION_16_17,
            MIGRATION_17_18,
            MIGRATION_18_19,
            MIGRATION_19_20
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides fun provideCustomerDao(db: AppDatabase): CustomerDao = db.customerDao()
    @Provides fun provideInvoiceDao(db: AppDatabase): InvoiceDao = db.invoiceDao()
    @Provides fun provideDocumentDao(db: AppDatabase): DocumentDao = db.documentDao()
    @Provides fun providePrefilledItemDao(db: AppDatabase): PrefilledItemDao = db.prefilledItemDao()
    @Provides fun provideBusinessProfileDao(db: AppDatabase): BusinessProfileDao = db.businessProfileDao()
    @Provides fun provideCurrencyDao(db: AppDatabase): CurrencyDao = db.currencyDao()
    @Provides fun provideExchangeRateDao(db: AppDatabase): ExchangeRateDao = db.exchangeRateDao()
    @Provides fun providePendingOperationDao(db: AppDatabase): PendingOperationDao = db.pendingOperationDao()
    @Provides fun provideAnalyticsDao(db: AppDatabase): AnalyticsDao = db.analyticsDao()
    @Provides fun provideCustomerAnalyticsDao(db: AppDatabase): CustomerAnalyticsDao = db.customerAnalyticsDao()
    @Provides fun provideInvoicePaymentDao(db: AppDatabase): InvoicePaymentDao = db.invoicePaymentDao()
    @Provides fun provideInvoiceTemplateDao(db: AppDatabase): InvoiceTemplateDao = db.invoiceTemplateDao()
    @Provides fun provideInvoiceCustomFieldDao(db: AppDatabase): InvoiceCustomFieldDao = db.invoiceCustomFieldDao()

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }
}
