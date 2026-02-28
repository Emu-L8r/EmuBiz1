package com.emul8r.emubizzz.di

import com.emul8r.emubizzz.data.repository.CustomerRepositoryImpl
import com.emul8r.emubizzz.data.repository.InvoiceRepositoryImpl
import com.emul8r.emubizzz.domain.repository.CustomerRepository
import com.emul8r.emubizzz.domain.repository.InvoiceRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCustomerRepository(
        customerRepositoryImpl: CustomerRepositoryImpl
    ): CustomerRepository

    @Binds
    @Singleton
    abstract fun bindInvoiceRepository(
        invoiceRepositoryImpl: InvoiceRepositoryImpl
    ): InvoiceRepository
}