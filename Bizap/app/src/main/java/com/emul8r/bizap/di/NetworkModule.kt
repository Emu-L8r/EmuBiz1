package com.emul8r.bizap.di

import com.emul8r.bizap.data.network.ConnectivityNetworkMonitor
import com.emul8r.bizap.data.network.ErrorInterceptor
import com.emul8r.bizap.data.network.NetworkMonitor
import com.emul8r.bizap.data.remote.ExchangeRateService
import com.emul8r.bizap.data.remote.api.CustomerApi
import com.emul8r.bizap.data.remote.api.InvoiceApi
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface NetworkModule {

    @Binds
    @Singleton
    fun bindNetworkMonitor(networkMonitor: ConnectivityNetworkMonitor): NetworkMonitor

    companion object {
        @Provides
        @Singleton
        fun provideJson(): Json {
            return Json { ignoreUnknownKeys = true }
        }

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
                .baseUrl("https://api.bizap.emul8r.com/v1/")
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
        fun provideInvoiceApi(retrofit: Retrofit): InvoiceApi {
            return retrofit.create(InvoiceApi::class.java)
        }

        @Provides
        @Singleton
        fun provideCustomerApi(retrofit: Retrofit): CustomerApi {
            return retrofit.create(CustomerApi::class.java)
        }
    }
}
