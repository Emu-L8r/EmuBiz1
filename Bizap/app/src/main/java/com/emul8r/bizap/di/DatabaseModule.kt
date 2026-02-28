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
import com.emul8r.bizap.data.local.PrefilledItemDao
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
            MIGRATION_6_7
        )
        .fallbackToDestructiveMigration() // Safety net for early development
        .build()
    }

    @Provides fun provideCustomerDao(db: AppDatabase): CustomerDao = db.customerDao()
    @Provides fun provideInvoiceDao(db: AppDatabase): InvoiceDao = db.invoiceDao()
    @Provides fun provideDocumentDao(db: AppDatabase): DocumentDao = db.documentDao()
    @Provides fun providePrefilledItemDao(db: AppDatabase): PrefilledItemDao = db.prefilledItemDao()

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }
}
