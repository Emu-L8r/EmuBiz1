package com.emul8r.bizap.di

import com.emul8r.bizap.data.repository.CustomerRepositoryImpl
import com.emul8r.bizap.data.repository.InvoiceRepositoryImpl
import com.emul8r.bizap.data.repository.ThemeRepositoryImpl
import com.emul8r.bizap.data.repository.DocumentRepositoryImpl
import com.emul8r.bizap.data.repository.PrefilledItemRepositoryImpl
import com.emul8r.bizap.domain.repository.CustomerRepository
import com.emul8r.bizap.domain.repository.InvoiceRepository
import com.emul8r.bizap.domain.repository.ThemeRepository
import com.emul8r.bizap.domain.repository.DocumentRepository
import com.emul8r.bizap.domain.repository.PrefilledItemRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindCustomerRepository(impl: CustomerRepositoryImpl): CustomerRepository

    @Binds
    abstract fun bindInvoiceRepository(impl: InvoiceRepositoryImpl): InvoiceRepository

    @Binds
    abstract fun bindThemeRepository(impl: ThemeRepositoryImpl): ThemeRepository

    @Binds
    abstract fun bindDocumentRepository(impl: DocumentRepositoryImpl): DocumentRepository

    @Binds
    abstract fun bindPrefilledItemRepository(impl: PrefilledItemRepositoryImpl): PrefilledItemRepository
}
