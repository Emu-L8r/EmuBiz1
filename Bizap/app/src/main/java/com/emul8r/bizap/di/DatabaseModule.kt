package com.emul8r.bizap.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.emul8r.bizap.data.local.AppDatabase
import com.emul8r.bizap.data.local.CustomerDao
import com.emul8r.bizap.data.local.DocumentDao
import com.emul8r.bizap.data.local.InvoiceDao
import com.emul8r.bizap.data.local.MIGRATION_2_3
import com.emul8r.bizap.data.local.MIGRATION_3_4
import com.emul8r.bizap.data.local.MIGRATION_4_5
import com.emul8r.bizap.data.local.MIGRATION_5_6
import com.emul8r.bizap.data.local.MIGRATION_6_7
import com.emul8r.bizap.data.local.MIGRATION_7_8
import com.emul8r.bizap.data.local.MIGRATION_8_9
import com.emul8r.bizap.data.local.MIGRATION_9_10
import com.emul8r.bizap.data.local.PrefilledItemDao
import com.emul8r.bizap.data.local.BusinessProfileDao
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
            MIGRATION_9_10
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides fun provideCustomerDao(db: AppDatabase): CustomerDao = db.customerDao()
    @Provides fun provideInvoiceDao(db: AppDatabase): InvoiceDao = db.invoiceDao()
    @Provides fun provideDocumentDao(db: AppDatabase): DocumentDao = db.documentDao()
    @Provides fun providePrefilledItemDao(db: AppDatabase): PrefilledItemDao = db.prefilledItemDao()
    @Provides fun provideBusinessProfileDao(db: AppDatabase): BusinessProfileDao = db.businessProfileDao()

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }
}
