package com.emul8r.bizap.di

import android.content.Context
import androidx.room.Room
import com.emul8r.bizap.data.local.AppDatabase
import com.emul8r.bizap.data.local.CustomerDao
import com.emul8r.bizap.data.local.DocumentDao
import com.emul8r.bizap.data.local.InvoiceDao
import com.emul8r.bizap.data.local.PrefilledItemDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "emu_bizzz_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideCustomerDao(database: AppDatabase): CustomerDao {
        return database.customerDao()
    }

    @Provides
    fun provideInvoiceDao(database: AppDatabase): InvoiceDao {
        return database.invoiceDao()
    }

    @Provides
    fun providePrefilledItemDao(database: AppDatabase): PrefilledItemDao {
        return database.prefilledItemDao()
    }

    @Provides
    fun provideDocumentDao(database: AppDatabase): DocumentDao {
        return database.documentDao()
    }
}
