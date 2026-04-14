package com.emul8r.bizap.di

import com.emul8r.bizap.data.repository.EffectPreferencesRepositoryImpl
import com.emul8r.bizap.domain.repository.EffectPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that binds [EffectPreferencesRepositoryImpl] to the [EffectPreferencesRepository] interface.
 *
 * The underlying [androidx.datastore.core.DataStore<Preferences>] is already provided as a singleton
 * by [DatabaseModule], so no additional @Provides function is needed here.
 *
 * This module is automatically installed into the [SingletonComponent], making
 * [EffectPreferencesRepository] injectable into any screen ViewModel or composable.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class EffectPreferencesModule {

    @Binds
    @Singleton
    abstract fun bindEffectPreferencesRepository(
        impl: EffectPreferencesRepositoryImpl
    ): EffectPreferencesRepository
}

