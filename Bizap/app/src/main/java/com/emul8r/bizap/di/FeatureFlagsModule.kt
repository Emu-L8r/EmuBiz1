package com.emul8r.bizap.di

import android.content.Context
import android.content.SharedPreferences
import com.emul8r.bizap.util.feature.FeatureFlags
import com.emul8r.bizap.util.feature.FeatureFlagsImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dependency injection module for feature flags.
 * Provides shared preferences and feature flag implementation.
 */
@Module
@InstallIn(SingletonComponent::class)
object FeatureFlagsModule {

    @Provides
    @Singleton
    fun provideSharedPreferences(
        @ApplicationContext context: Context
    ): SharedPreferences {
        return context.getSharedPreferences("matrix_feature_flags", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideFeatureFlags(
        sharedPreferences: SharedPreferences
    ): FeatureFlags = FeatureFlagsImpl(sharedPreferences)
}

