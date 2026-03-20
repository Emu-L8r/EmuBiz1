package com.emul8r.bizap.di

import com.emul8r.bizap.domain.usecase.DateChangeTickerManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TickerModule {

    @Provides
    @Singleton
    fun provideDateChangeTickerManager(): DateChangeTickerManager {
        return DateChangeTickerManager(
            scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
        )
    }
}
