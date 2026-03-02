package com.emul8r.bizap.di

import com.emul8r.bizap.data.repository.*
import com.emul8r.bizap.domain.customer.repository.CustomerAnalyticsRepository
import com.emul8r.bizap.domain.invoice.repository.PaymentAnalyticsRepository
import com.emul8r.bizap.domain.repository.*
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
        impl: com.emul8r.bizap.data.repository.BusinessProfileRepositoryImpl
    ): com.emul8r.bizap.domain.repository.BusinessProfileRepository

    @Binds
    @Singleton
    abstract fun bindInvoiceRepository(
        impl: com.emul8r.bizap.data.repository.InvoiceRepositoryImpl
    ): com.emul8r.bizap.domain.repository.InvoiceRepository

    @Binds
    @Singleton
    abstract fun bindCustomerRepository(
        impl: com.emul8r.bizap.data.repository.CustomerRepositoryImpl
    ): com.emul8r.bizap.domain.repository.CustomerRepository

    @Binds
    @Singleton
    abstract fun bindThemeRepository(
        impl: com.emul8r.bizap.data.repository.ThemeRepositoryImpl
    ): com.emul8r.bizap.domain.repository.ThemeRepository

    @Binds
    @Singleton
    abstract fun bindDocumentRepository(
        impl: com.emul8r.bizap.data.repository.DocumentRepositoryImpl
    ): com.emul8r.bizap.domain.repository.DocumentRepository

    @Binds
    @Singleton
    abstract fun bindPrefilledItemRepository(
        impl: com.emul8r.bizap.data.repository.PrefilledItemRepositoryImpl
    ): com.emul8r.bizap.domain.repository.PrefilledItemRepository

    @Binds
    @Singleton
    abstract fun bindCurrencyRepository(
        impl: com.emul8r.bizap.data.repository.CurrencyRepositoryImpl
    ): com.emul8r.bizap.domain.repository.CurrencyRepository

    @Binds
    @Singleton
    abstract fun bindRevenueRepository(
        impl: com.emul8r.bizap.data.repository.RevenueRepositoryImpl
    ): com.emul8r.bizap.domain.revenue.repository.RevenueRepository

    @Binds
    @Singleton
    abstract fun bindPaymentAnalyticsRepository(
        impl: com.emul8r.bizap.data.repository.PaymentAnalyticsRepositoryImpl
    ): com.emul8r.bizap.domain.invoice.repository.PaymentAnalyticsRepository

    @Binds
    @Singleton
    abstract fun bindCustomerAnalyticsRepository(
        impl: com.emul8r.bizap.data.repository.CustomerAnalyticsRepositoryImpl
    ): com.emul8r.bizap.domain.customer.repository.CustomerAnalyticsRepository

    @Binds
    @Singleton
    abstract fun bindDashboardSettingsRepository(
        impl: com.emul8r.bizap.data.repository.DashboardSettingsRepositoryImpl
    ): com.emul8r.bizap.domain.repository.DashboardSettingsRepository

    @Binds
    @Singleton
    abstract fun bindNoteRepository(
        impl: com.emul8r.bizap.data.repository.NoteRepositoryImpl
    ): com.emul8r.bizap.domain.repository.NoteRepository
}
