package com.emul8r.bizap.data.network

import com.emul8r.bizap.domain.error.BizapException
import kotlinx.coroutines.delay
import timber.log.Timber
import kotlin.math.pow

/**
 * NetworkRetryPolicy - Implements exponential backoff retry logic for business-layer operations
 *
 * **Architecture Layers:**
 * - HTTP Layer: RetryInterceptor (automatic retry on connection/timeout errors)
 * - Business Layer: NetworkRetryPolicy (manual retry on business logic failures)
 *
 * **When to use NetworkRetryPolicy:**
 * Use this in repositories when you need to retry due to business logic failures:
 * - Rate limiting (HTTP 429)
 * - Server validation errors (HTTP 422)
 * - Stale data detection (custom error codes)
 *
 * **When NOT to use:**
 * - Connection failures: RetryInterceptor handles these automatically
 * - HTTP 5xx errors: RetryInterceptor handles these automatically
 * - Timeout errors: RetryInterceptor handles these automatically
 *
 * **Example in Repository:**
 * ```kotlin
 * private val retryPolicy = NetworkRetryPolicy(maxRetries = 3)
 *
 * override suspend fun fetchInvoice(id: Long): Invoice {
 *     return retryPolicy.execute("fetchInvoice") {
 *         invoiceApi.getInvoice(id)  // Will retry on rate limit or stale data
 *     }
 * }
 * ```
 *
 * **Exponential Backoff Explanation:**
 * Problem: Simple retry (immediate, same speed) floods the server
 * ✅ Exponential backoff: Wait longer between retries
 *
 * EXAMPLE:
 * Attempt 1: Fails immediately
 * Wait 1 second → Attempt 2: Fails
 * Wait 2 seconds → Attempt 3: Fails
 * Wait 4 seconds → Attempt 4: Succeeds!
 *
 * This gives temporary server issues time to recover without overwhelming it.
 *
 * FORMULA:
 * --------
 * waitTime = baseDelay * (multiplier ^ attemptNumber)
 * With jitter to prevent thundering herd:
 * jitteredWait = waitTime * (0.5 + random(0, 1.0))
 */
class NetworkRetryPolicy(
    /** Initial delay in milliseconds (default 1 second) */
    private val baseDelayMs: Long = 1000,

    /** Multiply wait time by this for each retry (default 2x = exponential) */
    private val delayMultiplier: Double = 2.0,

    /** Maximum wait time to prevent waiting forever (default 30 seconds) */
    private val maxDelayMs: Long = 30000,

    /** How many times to retry before giving up (default 3) */
    private val maxRetries: Int = 3
) {

    /**
     * Execute a network operation with automatic retry on failure
     *
     * EXAMPLE USAGE:
     * =============
     * val exchangeRates = retryPolicy.execute(
     *     operationName = "Fetch Exchange Rates",
     *     operation = { exchangeRateService.getLatestRates() }
     * )
     *
     * WHAT HAPPENS:
     * 1. Attempts operation
     * 2. If fails with temporary error (timeout, 503, etc):
     *    - Waits 1 second
     *    - Tries again (up to 3 times)
     * 3. If fails with permanent error (400, 404, validation):
     *    - Throws immediately (no retry)
     * 4. If succeeds:
     *    - Returns result immediately
     *
     * @param operationName Name for logging (e.g., "Fetch Exchange Rates")
     * @param operation The network call to execute
     * @return T The result if successful
     * @throws BizapException If all retries failed
     */
    suspend fun <T> execute(
        operationName: String,
        operation: suspend () -> T
    ): T {
        var lastException: Exception? = null

        // Try up to (maxRetries + 1) times (first attempt + maxRetries retries)
        for (attemptNumber in 0..maxRetries) {
            try {
                Timber.d("$operationName: Attempt ${attemptNumber + 1}/${maxRetries + 1}")

                // Try the operation
                val result = operation()

                // Success!
                if (attemptNumber > 0) {
                    Timber.i("$operationName: Succeeded after ${attemptNumber} retries")
                }
                return result

            } catch (e: Exception) {
                lastException = e

                // Check if we should retry
                val shouldRetry = shouldRetry(e)
                val attemptsLeft = maxRetries - attemptNumber

                if (shouldRetry && attemptsLeft > 0) {
                    // Calculate wait time with exponential backoff
                    val waitMs = calculateBackoffDelay(attemptNumber)

                    Timber.w(
                        e,
                        "$operationName: Failed, retrying in ${waitMs}ms (${attemptsLeft} attempts left)"
                    )

                    // Wait before retrying
                    delay(waitMs)
                } else {
                    // Don't retry - either not retryable or out of retries
                    if (!shouldRetry) {
                        Timber.w(
                            e,
                            "$operationName: Failed with non-retryable error: ${e.message}"
                        )
                    } else {
                        Timber.e(
                            e,
                            "$operationName: Failed after $maxRetries retries: ${e.message}"
                        )
                    }
                    break
                }
            }
        }

        // All retries exhausted
        throw lastException ?: UnknownError("$operationName failed: Unknown error")
    }

    /**
     * Check if an error is retryable (should we try again?)
     *
     * RETRYABLE ERRORS:
     * ✅ NetworkError with retryable flag
     * ✅ TimeoutError
     * ✅ ConnectivityError
     * ✅ HTTP 5xx (server errors)
     * ✅ HTTP 429 (rate limited)
     *
     * NON-RETRYABLE ERRORS:
     * ❌ ValidationError (user input wrong)
     * ❌ HTTP 400 (bad request)
     * ❌ HTTP 401/403 (authentication)
     * ❌ HTTP 404 (not found)
     * ❌ DatabaseError (data corruption)
     */
    private fun shouldRetry(exception: Exception): Boolean {
        return when (exception) {
            // BizapExceptions with specific retry rules
            is BizapException.NetworkError -> exception.isRetryable
            is BizapException.TimeoutError -> true
            is BizapException.ConnectivityError -> true

            // These are definitely not retryable
            is BizapException.ValidationError -> false
            is BizapException.InvalidInvoiceError -> false
            is BizapException.DatabaseError -> false
            is BizapException.FileError -> false
            is BizapException.NotFoundError -> false

            // Other BizapExceptions default to false
            is BizapException -> false

            // Unknown exceptions: try to retry (conservative approach)
            else -> true
        }
    }

    /**
     * Calculate exponential backoff delay
     *
     * FORMULA:
     * --------
     * baseDelay = baseDelayMs
     * exponentialDelay = baseDelay * (multiplier ^ attemptNumber)
     * cappedDelay = min(exponentialDelay, maxDelayMs)
     * jitteredDelay = cappedDelay * (0.5 + random)
     *
     * EXAMPLE:
     * Attempt 0 (first retry):
     *   exponential = 1000 * (2 ^ 0) = 1000ms = 1 second
     *   jittered = 1000 * (0.5 to 1.5) = 500-1500ms
     *
     * Attempt 1 (second retry):
     *   exponential = 1000 * (2 ^ 1) = 2000ms = 2 seconds
     *   jittered = 2000 * (0.5 to 1.5) = 1000-3000ms
     *
     * Attempt 2 (third retry):
     *   exponential = 1000 * (2 ^ 2) = 4000ms = 4 seconds
     *   jittered = 4000 * (0.5 to 1.5) = 2000-6000ms
     *
     * PURPOSE OF JITTER:
     * If 1000 clients all retry at exactly the same time,
     * server gets hammered again. Jitter spreads out retries.
     */
    private fun calculateBackoffDelay(attemptNumber: Int): Long {
        // Calculate exponential delay: baseDelay * (multiplier ^ attemptNumber)
        val exponentialDelay = (baseDelayMs * delayMultiplier.pow(attemptNumber.toDouble())).toLong()

        // Cap at maximum to prevent waiting forever
        val cappedDelay = minOf(exponentialDelay, maxDelayMs)

        // Add random jitter (0.5x to 1.5x the capped delay)
        val jitterFactor = 0.5 + Math.random()  // 0.5 to 1.5
        val jitteredDelay = (cappedDelay * jitterFactor).toLong()

        return jitteredDelay
    }
}

/**
 * Helper: Build a retry policy configured for your needs
 *
 * EXAMPLE:
 * --------
 * // Conservative: Few retries, short waits (for quick operations)
 * val quickRetry = retryPolicyFor(maxRetries = 1, baseDelayMs = 500)
 *
 * // Aggressive: Many retries, long waits (for important operations)
 * val persistentRetry = retryPolicyFor(maxRetries = 5, baseDelayMs = 2000)
 *
 * // Default: Balanced
 * val defaultRetry = retryPolicyFor()
 */
fun retryPolicyFor(
    maxRetries: Int = 3,
    baseDelayMs: Long = 1000,
    delayMultiplier: Double = 2.0,
    maxDelayMs: Long = 30000
): NetworkRetryPolicy {
    return NetworkRetryPolicy(
        baseDelayMs = baseDelayMs,
        delayMultiplier = delayMultiplier,
        maxDelayMs = maxDelayMs,
        maxRetries = maxRetries
    )
}

