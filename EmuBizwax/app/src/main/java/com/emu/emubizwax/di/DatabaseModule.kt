package com.emu.emubizwax.di

import android.content.Context
import androidx.room.Room
import com.emu.emubizwax.data.local.AppDatabase
import com.emu.emubizwax.data.local.Converters
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
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "invoicing_database"
        )
        .addTypeConverters(Converters())
        .fallbackToDestructiveMigration() // Use migrations for production!
        .build()
    }

    @Provides
    fun provideCustomerDao(db: AppDatabase) = db.customerDao()

    @Provides
    fun provideInvoiceDao(db: AppDatabase) = db.invoiceDao()
}
