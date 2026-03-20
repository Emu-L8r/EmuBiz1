package com.emul8r.bizap.di

import com.emul8r.bizap.data.repository.SettingsRepositoryImpl
import com.emul8r.bizap.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that binds [SettingsRepositoryImpl] to the [SettingsRepository] interface.
 *
 * The underlying [androidx.datastore.core.DataStore] is already provided as a singleton
 * by [DatabaseModule], so no additional @Provides function is needed here.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class SettingsModule {

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        impl: SettingsRepositoryImpl
    ): SettingsRepository
}
