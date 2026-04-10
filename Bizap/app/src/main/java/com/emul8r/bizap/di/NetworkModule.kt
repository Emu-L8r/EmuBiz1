package com.emul8r.bizap.di

import android.content.Context
import com.emul8r.bizap.BuildConfig
import com.emul8r.bizap.data.network.ConnectivityNetworkMonitor
import com.emul8r.bizap.data.network.ErrorInterceptor
import com.emul8r.bizap.data.network.HttpClientConfiguration
import com.emul8r.bizap.data.network.NetworkMonitor
import com.emul8r.bizap.data.network.RateLimitInterceptor
import com.emul8r.bizap.data.network.RetryInterceptor
import com.emul8r.bizap.data.remote.ExchangeRateService
import com.emul8r.bizap.data.remote.api.CustomerApi
import com.emul8r.bizap.data.remote.api.InvoiceApi
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Interceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Network Dependency Injection Module
 *
 * **Responsibilities:**
 * 1. Configure OkHttpClient with timeouts, retry logic, and caching
 * 2. Provide Retrofit instances for API services
 * 3. Wire NetworkMonitor for observable network state
 *
 * **Architecture:**
 * - Uses HttpClientConfiguration (interface) for timeout strategy
 * - Integrates RetryInterceptor for automatic exponential backoff
 * - Uses OkHttp's built-in cache (not hardcoded string lookup)
 * - Provides HttpLoggingInterceptor for debug logging
 *
 * **Rationale for Configuration:**
 * - Connect timeout: 30s (conservative for slow networks, acceptable for fast)
 * - Read/Write timeouts: 30s (same as connect)
 * - Call timeout: 30s (overall deadline)
 * - Cache: 10MB with 24-hour max-age (server controls actual cache duration via headers)
 * - Retry: 3 attempts with exponential backoff (1s, 2s, 4s)
 *
 * **Future enhancements:**
 * - Phase 2: Adaptive timeouts based on NetworkState quality
 * - Phase 2: Per-endpoint cache durations via Cache-Control headers
 */
@Module
@InstallIn(SingletonComponent::class)
interface NetworkModule {

    @Binds
    @Singleton
    fun bindNetworkMonitor(networkMonitor: ConnectivityNetworkMonitor): NetworkMonitor

    companion object {
        // ────────────────────────────────────────────────────────────────────────────────
        // CONFIGURATION CONSTANTS
        //
        // These replace the hardcoded magic numbers that were scattered throughout the app.
        // Made explicit so they can be tested, documented, and easily adjusted.
        // ────────────────────────────────────────────────────────────────────────────────

        /** Connect timeout in milliseconds (TCP handshake). Default: 30s. */
        private const val CONNECT_TIMEOUT_MS = 30_000L

        /** Read timeout in milliseconds (receiving response data). Default: 30s. */
        private const val READ_TIMEOUT_MS = 30_000L

        /** Write timeout in milliseconds (sending request data). Default: 30s. */
        private const val WRITE_TIMEOUT_MS = 30_000L

        /** Call timeout in milliseconds (overall request deadline). Default: 30s. */
        private const val CALL_TIMEOUT_MS = 30_000L

        /** HTTP cache size in bytes. 10 MB = 10 * 1024 * 1024 */
        private const val HTTP_CACHE_SIZE_BYTES = 10 * 1024 * 1024L

        /** Max age for cached responses in seconds (24 hours). Actual cache duration
         *  per-endpoint is controlled by server via Cache-Control headers. */
        private const val HTTP_CACHE_MAX_AGE_SECONDS = 24 * 60 * 60

        /** Max number of retry attempts for transient errors. */
        private const val RETRY_MAX_ATTEMPTS = 3

        /** Base delay for exponential backoff (1 second). */
        private const val RETRY_BASE_DELAY_MS = 1000L

        @Provides
        @Singleton
        fun provideJson(): Json {
            return Json { ignoreUnknownKeys = true }
        }

        /**
         * Provides the default HTTP client configuration strategy.
         *
         * Currently uses fixed timeouts (30s for all operations).
         * Phase 2: Can be switched to adaptive() based on NetworkState for dynamic tuning.
         */
        @Provides
        @Singleton
        fun provideHttpClientConfiguration(): HttpClientConfiguration {
            return HttpClientConfiguration.fixed(
                connectTimeoutMs = CONNECT_TIMEOUT_MS,
                readTimeoutMs = READ_TIMEOUT_MS,
                writeTimeoutMs = WRITE_TIMEOUT_MS,
                callTimeoutMs = CALL_TIMEOUT_MS
            )
        }

        /**
         * Provides the main OkHttpClient for all API calls.
         *
         * **Configuration includes:**
         * - HTTP/2 support (multiplexing, header compression, connection reuse)
         * - Request/response logging (DEBUG level in development, NONE in production)
         * - Automatic retry on transient errors (RetryInterceptor)
         * - Error handling (ErrorInterceptor)
         * - HTTP caching (OkHttp's built-in Cache class)
         *
         * **Timeout Strategy:** All timeouts are 30s (fixed configuration).
         * This is conservative for 3G but acceptable for WiFi/fiber.
         * For Bizap's use case (frequent small API calls), this is appropriate.
         * In Phase 2, can adopt adaptive timeouts based on NetworkState.
         *
         * **Caching Strategy:** Uses OkHttp's native cache with Cache-Control headers
         * from the server. No hardcoded endpoint-specific cache durations.
         *
         * **Certificate Pinning:** Removed until OpenExchangeRates provides official
         * certificate hashes. Pinning with incorrect hashes breaks production.
         *
         * **Why not the old NetworkOptimization.kt?**
         * - Cargo-cult marketing metrics ("20-30% faster")
         * - Hardcoded connection pool config (redundant with OkHttp defaults)
         * - RequestBatching utility that doesn't actually batch (sequential execution)
         * - CachingStrategy string map (breaks if API versioning changes)
         * - NetworkStatus.isNetworkAvailable() (boolean is stale immediately)
         * - Static singletons (impossible to mock for testing)
         */
        @Provides
        @Singleton
        fun provideOkHttpClient(
            @ApplicationContext context: Context,
            config: HttpClientConfiguration
        ): OkHttpClient {
            // Setup HTTP caching
            val cacheDir = File(context.cacheDir, "http-cache")
            val cache = Cache(cacheDir, HTTP_CACHE_SIZE_BYTES)

            val builder = OkHttpClient.Builder()
                // Timeouts (from HttpClientConfiguration)
                .connectTimeout(config.connectTimeoutMs, TimeUnit.MILLISECONDS)
                .readTimeout(config.readTimeoutMs, TimeUnit.MILLISECONDS)
                .writeTimeout(config.writeTimeoutMs, TimeUnit.MILLISECONDS)
                .callTimeout(config.callTimeoutMs, TimeUnit.MILLISECONDS)

            // 🔐 SECURITY: HTTP logging only in debug builds
            // Production builds do NOT log HTTP traffic to prevent sensitive data exposure
            // (API keys, tokens, user data) - complies with GDPR/Privacy Shield
            if (BuildConfig.DEBUG) {
                try {
                    // Dynamically load HttpLoggingInterceptor only when available (debug builds)
                    @Suppress("UNCHECKED_CAST")
                    val loggingInterceptorClass = Class.forName("okhttp3.logging.HttpLoggingInterceptor")
                    val loggerClass = Class.forName("okhttp3.logging.HttpLoggingInterceptor\$Logger")

                    // Create logger instance
                    val logger = java.lang.reflect.Proxy.newProxyInstance(
                        loggerClass.classLoader,
                        arrayOf(loggerClass)
                    ) { proxy, method, args ->
                        if (method.name == "log") {
                            Timber.tag("OkHttp").d(args[0].toString())
                        }
                        null
                    }

                    // Create interceptor
                    val interceptor = loggingInterceptorClass
                        .getConstructor(loggerClass)
                        .newInstance(logger) as Interceptor

                    // Set level to BASIC
                    val levelClass = Class.forName("okhttp3.logging.HttpLoggingInterceptor\$Level")
                    val basicLevel = levelClass.getDeclaredField("BASIC").get(null)
                    loggingInterceptorClass.getDeclaredField("level").apply {
                        isAccessible = true
                        set(interceptor, basicLevel)
                    }

                    builder.addNetworkInterceptor(interceptor)
                } catch (e: Exception) {
                    // Logging not available, skip it
                    Timber.w("HttpLoggingInterceptor not available: ${e.message}")
                }
            }

            return builder
                // 2. Rate limiting (prevents DOS to backend)
                .addInterceptor(RateLimitInterceptor(
                    requestsPerSecond = 10,
                    globalRequestsPerSecond = 50,
                    maxRetries = 3
                ))

                // 3. Error handling (converts HTTP errors to BizapExceptions)
                .addInterceptor(ErrorInterceptor())

                // 4. Retry (handles transient failures)
                .addInterceptor(RetryInterceptor(
                    maxRetries = RETRY_MAX_ATTEMPTS,
                    baseDelayMs = RETRY_BASE_DELAY_MS
                ))

                // HTTP caching (stores responses based on Cache-Control headers)
                .cache(cache)

                // Protocol selection (prefers HTTP/2 for multiplexing)
                .protocols(listOf(okhttp3.Protocol.HTTP_2, okhttp3.Protocol.HTTP_1_1))

                .apply {
                    Timber.d(
                        "✅ OkHttpClient configured: " +
                            "timeouts=${config.connectTimeoutMs}ms, " +
                            "cache=${HTTP_CACHE_SIZE_BYTES / (1024 * 1024)}MB, " +
                            "retries=$RETRY_MAX_ATTEMPTS"
                    )
                }
                .build()
        }

        /**
         * Provides the Retrofit client for API service definitions.
         *
         * Uses the OpenExchangeRates API as the base URL for exchange rate fetching.
         * Other API endpoints (invoices, customers) use their own services with
         * different base URLs if needed.
         */
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
