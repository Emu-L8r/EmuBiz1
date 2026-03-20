package com.emul8r.bizap.di

import com.emul8r.bizap.domain.manager.BusinessContextManager
import com.emul8r.bizap.domain.repository.BusinessProfileRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that provides the [BusinessContextManager] singleton.
 *
 * Keeps business-context concerns isolated from [GuiV2Module] and
 * [RepositoryModule] so the dependency graph stays easy to read.
 */
@Module
@InstallIn(SingletonComponent::class)
object BusinessContextModule {

    @Provides
    @Singleton
    fun provideBusinessContextManager(
        businessProfileRepository: BusinessProfileRepository
    ): BusinessContextManager = BusinessContextManager(businessProfileRepository)
}
