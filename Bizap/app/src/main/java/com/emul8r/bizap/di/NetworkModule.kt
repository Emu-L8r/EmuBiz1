package com.emul8r.bizap.di

import com.emul8r.bizap.data.network.ErrorInterceptor
import com.emul8r.bizap.data.remote.ExchangeRateService
import com.emul8r.bizap.data.sync.SyncService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(ErrorInterceptor())
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://openexchangerates.org/api/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    @Provides
    @Singleton
    fun provideExchangeRateService(retrofit: Retrofit): ExchangeRateService {
        return retrofit.create(ExchangeRateService::class.java)
    }

    @Provides
    @Singleton
    fun provideSyncService(retrofit: Retrofit): SyncService {
        return retrofit.create(SyncService::class.java)
    }
}
