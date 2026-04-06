package com.emul8r.bizap.di

import com.emul8r.bizap.data.repository.BusinessProfileRepositoryImpl
import com.emul8r.bizap.data.repository.CustomFieldRepositoryImpl
import com.emul8r.bizap.data.repository.CustomerRepositoryImpl
import com.emul8r.bizap.data.repository.CurrencyRepositoryImpl
import com.emul8r.bizap.data.repository.CustomerAnalyticsRepositoryImpl
import com.emul8r.bizap.data.repository.DocumentRepositoryImpl
import com.emul8r.bizap.data.repository.ExportRepositoryImpl
import com.emul8r.bizap.data.repository.InvoiceRepositoryImpl
import com.emul8r.bizap.data.repository.NoteRepositoryImpl
import com.emul8r.bizap.data.repository.OfflineQueueRepositoryImpl
import com.emul8r.bizap.data.repository.PaymentAnalyticsRepositoryImpl
import com.emul8r.bizap.data.repository.PaymentRecordRepositoryImpl
import com.emul8r.bizap.data.repository.PDFRepositoryImpl
import com.emul8r.bizap.data.repository.PrefilledItemRepositoryImpl
import com.emul8r.bizap.data.repository.RevenueRepositoryImpl
import com.emul8r.bizap.data.repository.SearchRepositoryImpl
import com.emul8r.bizap.data.repository.TaxRepositoryImpl
import com.emul8r.bizap.data.repository.ThemeRepositoryImpl
import com.emul8r.bizap.data.service.InvoicePdfService
import com.emul8r.bizap.data.settings.UIPreferencesImpl
import com.emul8r.bizap.domain.repository.BusinessProfileRepository
import com.emul8r.bizap.domain.settings.UIPreferences
import com.emul8r.bizap.domain.repository.CustomFieldRepository
import com.emul8r.bizap.domain.repository.CustomerRepository
import com.emul8r.bizap.domain.repository.CurrencyRepository
import com.emul8r.bizap.domain.repository.DocumentRepository
import com.emul8r.bizap.domain.repository.ExportRepository
import com.emul8r.bizap.domain.repository.InvoiceRepository
import com.emul8r.bizap.domain.repository.NoteRepository
import com.emul8r.bizap.domain.repository.OfflineQueueRepository
import com.emul8r.bizap.domain.repository.PDFRepository
import com.emul8r.bizap.domain.repository.PrefilledItemRepository
import com.emul8r.bizap.domain.repository.SearchRepository
import com.emul8r.bizap.domain.repository.TaxRepository
import com.emul8r.bizap.domain.repository.ThemeRepository
import com.emul8r.bizap.domain.revenue.repository.RevenueRepository
import com.emul8r.bizap.domain.customer.repository.CustomerAnalyticsRepository
import com.emul8r.bizap.domain.invoice.repository.PaymentAnalyticsRepository
import com.emul8r.bizap.domain.service.PdfGenerationService
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
    abstract fun bindCustomFieldRepository(
        impl: CustomFieldRepositoryImpl
    ): CustomFieldRepository

    @Binds
    @Singleton
    abstract fun bindTaxRepository(
        impl: TaxRepositoryImpl
    ): TaxRepository

    @Binds
    @Singleton
    abstract fun bindPDFRepository(
        impl: PDFRepositoryImpl
    ): PDFRepository

    @Binds
    @Singleton
    abstract fun bindOfflineQueueRepository(
        impl: OfflineQueueRepositoryImpl
    ): OfflineQueueRepository

    @Binds
    @Singleton
    abstract fun bindNoteRepository(
        impl: NoteRepositoryImpl
    ): NoteRepository

    @Binds
    @Singleton
    abstract fun bindSearchRepository(
        impl: SearchRepositoryImpl
    ): SearchRepository

    @Binds
    @Singleton
    abstract fun bindExportRepository(
        impl: ExportRepositoryImpl
    ): ExportRepository

    // Payment Record Repository binding
    // @Binds
    // @Singleton
    // abstract fun bindPaymentRecordRepository(
    //     impl: PaymentRecordRepositoryImpl
    // ): PaymentRecordRepository

    // Invoice Template Repository binding
    // @Binds
    // @Singleton
    // abstract fun bindInvoiceTemplateRepository(
    //     impl: InvoiceTemplateRepositoryImpl
    // ): InvoiceTemplateRepository

    /**
     * Binds the data-layer InvoicePdfService to the domain-level PdfGenerationService interface.
     * This allows domain use cases to depend on the domain interface rather than the data layer implementation,
     * maintaining clean architecture principles.
     */
    @Binds
    @Singleton
    abstract fun bindPdfGenerationService(
        impl: InvoicePdfService
    ): PdfGenerationService

    @Binds
    @Singleton
    abstract fun bindUIPreferences(impl: UIPreferencesImpl): UIPreferences
}
