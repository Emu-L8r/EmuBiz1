package com.emu.emubizwax.di

import com.emu.emubizwax.data.repository.CustomerRepositoryImpl
import com.emu.emubizwax.data.repository.FileRepositoryImpl
import com.emu.emubizwax.data.repository.InvoiceRepositoryImpl
import com.emu.emubizwax.data.repository.ThemeRepositoryImpl
import com.emu.emubizwax.data.repository.UserRepositoryImpl
import com.emu.emubizwax.domain.repository.CustomerRepository
import com.emu.emubizwax.domain.repository.FileRepository
import com.emu.emubizwax.domain.repository.InvoiceRepository
import com.emu.emubizwax.domain.repository.ThemeRepository
import com.emu.emubizwax.domain.repository.UserRepository
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
    abstract fun bindThemeRepository(impl: ThemeRepositoryImpl): ThemeRepository

    @Binds
    @Singleton
    abstract fun bindCustomerRepository(impl: CustomerRepositoryImpl): CustomerRepository

    @Binds
    @Singleton
    abstract fun bindInvoiceRepository(impl: InvoiceRepositoryImpl): InvoiceRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    abstract fun bindFileRepository(impl: FileRepositoryImpl): FileRepository
}
