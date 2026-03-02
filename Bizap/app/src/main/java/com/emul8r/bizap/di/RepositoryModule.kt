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

    @Binds
    @Singleton
    abstract fun bindPaymentAnalyticsRepository(
        impl: PaymentAnalyticsRepositoryImpl
    ): PaymentAnalyticsRepository

    @Binds
    @Singleton
    abstract fun bindCustomerAnalyticsRepository(
        impl: CustomerAnalyticsRepositoryImpl
    ): CustomerAnalyticsRepository

    @Binds
    @Singleton
    abstract fun bindDashboardSettingsRepository(
        impl: DashboardSettingsRepositoryImpl
    ): DashboardSettingsRepository

    @Binds
    @Singleton
    abstract fun bindNoteRepository(
        impl: NoteRepositoryImpl
    ): NoteRepository
}
