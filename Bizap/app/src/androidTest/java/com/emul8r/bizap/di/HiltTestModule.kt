package com.emul8r.bizap.di

import android.content.Context
import androidx.room.Room
import com.emul8r.bizap.data.local.AppDatabase
import com.emul8r.bizap.data.repository.InvoiceRepositoryImpl
import com.emul8r.bizap.data.repository.CustomerRepositoryImpl
import com.emul8r.bizap.data.repository.PaymentRepositoryImpl
import com.emul8r.bizap.domain.repository.InvoiceRepository
import com.emul8r.bizap.domain.repository.CustomerRepository
import com.emul8r.bizap.domain.repository.PaymentRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HiltTestModule {

    @Provides
    @Singleton
    fun provideInMemoryDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideInvoiceRepository(database: AppDatabase): InvoiceRepository {
        return InvoiceRepositoryImpl(database)
    }

    @Provides
    @Singleton
    fun provideCustomerRepository(database: AppDatabase): CustomerRepository {
        return CustomerRepositoryImpl(database)
    }

    @Provides
    @Singleton
    fun providePaymentRepository(database: AppDatabase): PaymentRepository {
        return PaymentRepositoryImpl(database)
    }
}
