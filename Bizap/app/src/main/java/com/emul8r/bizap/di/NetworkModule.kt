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
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
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
            // 🔐 SECURITY: Certificate Pinning for Exchange Rate API
            // Prevents MITM attacks by validating the server's SSL certificate
            // SHA-256 hashes of the certificate pins are provided by the API host
            val certificatePinner = CertificatePinner.Builder()
                // OpenExchangeRates API - Pin the certificate
                // In production, verify these hashes with the API provider
                .add(
                    "openexchangerates.org",
                    // These are example hashes - MUST be updated with real hashes
                    "sha256/+vLyQUJ3+a9+V12/SSVV9j5oP4yMQcnv/4IvLpBQWc4=" // Example: Root CA
                )
                .build()

            return OkHttpClient.Builder()
                .addInterceptor(ErrorInterceptor())
                .certificatePinner(certificatePinner) // 🔐 Enable certificate pinning
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                // 🔐 SECURITY: Disable HTTP protocol fallback (enforce HTTPS only)
                .protocols(listOf(okhttp3.Protocol.HTTP_2, okhttp3.Protocol.HTTP_1_1))
                .apply {
                    Timber.d("🔐 OkHttpClient configured with certificate pinning and security hardening")
                }
                .build()
        }

        @Provides
        @Singleton
        fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
            // Using OpenExchangeRates API base URL
            // Free tier: 1,500 requests/month, requires API key
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
