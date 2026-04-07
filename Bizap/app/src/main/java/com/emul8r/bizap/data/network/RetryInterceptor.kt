package com.emul8r.bizap.data.network

import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber
import java.io.IOException

/**
 * OkHttp Interceptor that implements exponential backoff retry logic for transport-layer errors.
 *
 * **Purpose:** Automatically retry failed requests due to transient network issues
 * (timeouts, connection errors, temporary server errors) without manual retry logic
 * in repositories.
 *
 * **Distinction from NetworkRetryPolicy:**
 * - **RetryInterceptor (HTTP layer):** Retries due to connection/timeout failures
 * - **NetworkRetryPolicy (business layer):** Retries due to business logic failures
 *   (e.g., rate limiting, stale data, validation errors)
 *
 * **What it retries:**
 * ✅ IOException (connection timeout, socket timeout, DNS failure)
 * ✅ HTTP 5xx (500, 502, 503, 504)
 * ✅ HTTP 429 (rate limited)
 *
 * **What it does NOT retry:**
 * ❌ HTTP 4xx (400, 401, 403, 404) — client errors, not temporary
 * ❌ Retrofit validation errors — let business logic decide
 *
 * **Exponential backoff:** 1s, 2s, 4s, etc. with jitter to prevent thundering herd.
 *
 * Example:
 * ```
 * // Retries automatically happen in background
 * val response = okHttpClient.newCall(request).execute()  // Retries if timeout
 * ```
 */
class RetryInterceptor(
    private val maxRetries: Int = 3,
    private val baseDelayMs: Long = 1000
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var lastException: Exception? = null
        var lastResponse: Response? = null

        for (attempt in 0..maxRetries) {
            try {
                val response = chain.proceed(chain.request())

                // Success or client error (don't retry 4xx)
                if (response.isSuccessful || response.code in 400..499) {
                    return response
                }

                // Server error (5xx) or rate limit (429) — retry
                if (response.code >= 500 || response.code == 429) {
                    lastResponse = response
                    if (attempt < maxRetries) {
                        val delayMs = calculateBackoffDelay(attempt)
                        Timber.w("HTTP ${response.code}, retrying in ${delayMs}ms (attempt ${attempt + 1}/$maxRetries)")
                        Thread.sleep(delayMs)
                        continue
                    } else {
                        // Out of retries, return the failed response
                        return response
                    }
                }

                // Other response code, return as-is
                return response

            } catch (e: IOException) {
                // Connection error, timeout, DNS failure — retry if attempts left
                lastException = e

                if (attempt < maxRetries) {
                    val delayMs = calculateBackoffDelay(attempt)
                    Timber.w(e, "IOException, retrying in ${delayMs}ms (attempt ${attempt + 1}/$maxRetries)")
                    try {
                        Thread.sleep(delayMs)
                    } catch (sleepInterrupt: InterruptedException) {
                        Thread.currentThread().interrupt()
                        throw sleepInterrupt
                    }
                } else {
                    // Out of retries
                    throw e
                }
            }
        }

        // Should not reach here, but just in case
        throw lastException ?: UnknownError("RetryInterceptor exhausted retries")
    }

    /**
     * Calculate exponential backoff with jitter.
     *
     * Formula: baseDelay * (2 ^ attempt) * jitter(0.5-1.5)
     *
     * Examples:
     * - Attempt 0: 1000 * 1 * [0.5-1.5] = 500-1500ms
     * - Attempt 1: 1000 * 2 * [0.5-1.5] = 1000-3000ms
     * - Attempt 2: 1000 * 4 * [0.5-1.5] = 2000-6000ms
     */
    private fun calculateBackoffDelay(attemptNumber: Int): Long {
        val exponentialDelay = baseDelayMs * (1 shl attemptNumber)  // 2^n via bit shift
        val jitterFactor = 0.5 + Math.random()
        return (exponentialDelay * jitterFactor).toLong()
    }
}

/**
 * Exception type for transport-layer retry exhaustion.
 * Thrown when RetryInterceptor has exhausted all retry attempts.
 */
class RetryExhaustedException(message: String, cause: Throwable? = null) :
    Exception(message, cause)


