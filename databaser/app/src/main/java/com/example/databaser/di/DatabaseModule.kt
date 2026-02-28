package com.example.databaser.di

import android.content.Context
import androidx.room.Room
import com.example.databaser.data.AppDatabase
import com.example.databaser.data.dao.CustomerDao
import com.example.databaser.data.dao.InvoiceDao
import com.example.databaser.data.dao.InvoiceLineItemDao
import com.example.databaser.data.dao.NoteDao
import com.example.databaser.data.dao.QuoteDao
import com.example.databaser.data.dao.QuoteLineItemDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideCustomerDao(appDatabase: AppDatabase): CustomerDao = appDatabase.customerDao()

    @Provides
    @Singleton
    fun provideInvoiceDao(appDatabase: AppDatabase): InvoiceDao = appDatabase.invoiceDao()

    @Provides
    @Singleton
    fun provideInvoiceLineItemDao(appDatabase: AppDatabase): InvoiceLineItemDao = appDatabase.invoiceLineItemDao()

    @Provides
    @Singleton
    fun provideNoteDao(appDatabase: AppDatabase): NoteDao = appDatabase.noteDao()

    @Provides
    @Singleton
    fun provideQuoteDao(appDatabase: AppDatabase): QuoteDao = appDatabase.quoteDao()

    @Provides
    @Singleton
    fun provideQuoteLineItemDao(appDatabase: AppDatabase): QuoteLineItemDao = appDatabase.quoteLineItemDao()
}
