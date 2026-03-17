package com.emul8r.bizap.di

import com.emul8r.bizap.domain.config.BizapConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ConfigModule {

    @Provides
    @Singleton
    fun provideBizapConfig(): BizapConfig {
        return BizapConfig.production()
    }
}
