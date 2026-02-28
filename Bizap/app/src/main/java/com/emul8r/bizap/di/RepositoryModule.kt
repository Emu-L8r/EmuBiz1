package com.emul8r.bizap.di

import com.emul8r.bizap.data.repository.BusinessProfileRepositoryImpl
import com.emul8r.bizap.data.repository.CustomerRepositoryImpl
import com.emul8r.bizap.data.repository.CurrencyRepositoryImpl
import com.emul8r.bizap.data.repository.DocumentRepositoryImpl
import com.emul8r.bizap.data.repository.InvoiceRepositoryImpl
import com.emul8r.bizap.data.repository.PrefilledItemRepositoryImpl
import com.emul8r.bizap.data.repository.RevenueRepositoryImpl
import com.emul8r.bizap.data.repository.ThemeRepositoryImpl
import com.emul8r.bizap.domain.repository.BusinessProfileRepository
import com.emul8r.bizap.domain.repository.CustomerRepository
import com.emul8r.bizap.domain.repository.CurrencyRepository
import com.emul8r.bizap.domain.repository.DocumentRepository
import com.emul8r.bizap.domain.repository.InvoiceRepository
import com.emul8r.bizap.domain.repository.PrefilledItemRepository
import com.emul8r.bizap.domain.repository.ThemeRepository
import com.emul8r.bizap.domain.revenue.repository.RevenueRepository
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
    abstract fun bindBusinessProfileRepository(
        impl: BusinessProfileRepositoryImpl
    ): BusinessProfileRepository

    @Binds
    @Singleton
    abstract fun bindInvoiceRepository(
        impl: InvoiceRepositoryImpl
    ): InvoiceRepository

    @Binds
    @Singleton
    abstract fun bindCustomerRepository(
        impl: CustomerRepositoryImpl
    ): CustomerRepository

    @Binds
    @Singleton
    abstract fun bindThemeRepository(
        impl: ThemeRepositoryImpl
    ): ThemeRepository

    @Binds
    @Singleton
    abstract fun bindDocumentRepository(
        impl: DocumentRepositoryImpl
    ): DocumentRepository

    @Binds
    @Singleton
    abstract fun bindPrefilledItemRepository(
        impl: PrefilledItemRepositoryImpl
    ): PrefilledItemRepository

    @Binds
    @Singleton
    abstract fun bindCurrencyRepository(
        impl: CurrencyRepositoryImpl
    ): CurrencyRepository

    @Binds
    @Singleton
    abstract fun bindRevenueRepository(
        impl: RevenueRepositoryImpl
    ): RevenueRepository
}
