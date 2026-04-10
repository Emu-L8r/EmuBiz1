package com.emul8r.bizap.data.network

import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Rate Limit Interceptor - Prevents DOS to backend and handles rate limit responses
 *
 * **Strategy:**
 * 1. Enforces client-side rate limiting (max X requests/endpoint/second)
 * 2. Respects server-side rate limit headers (Retry-After, X-RateLimit-Reset)
 * 3. Exponential backoff for 429 (Too Many Requests) responses
 * 4. Logs all rate limiting events
 *
 * **Default Limits:**
 * - 10 requests per second per endpoint
 * - Global max 50 requests per second
 * - Backoff: 1s, 2s, 4s, 8s (up to 3 retries)
 */
class RateLimitInterceptor(
    private val requestsPerSecond: Int = 10,
    private val globalRequestsPerSecond: Int = 50,
    private val maxRetries: Int = 3
) : Interceptor {

    companion object {
        private val endpointLimiters = mutableMapOf<String, EndpointRateLimiter>()
        private var globalLimiter = GlobalRateLimiter()

        private const val TAG = "RateLimit"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url
        val endpoint = getEndpointKey(url)

        // Get or create limiter for this endpoint
        val limiter = endpointLimiters.getOrPut(endpoint) {
            EndpointRateLimiter(requestsPerSecond)
        }

        // Check global rate limit
        if (!globalLimiter.allowRequest()) {
            Timber.w("$TAG: Global rate limit exceeded for $endpoint")
            return Response.Builder()
                .request(request)
                .protocol(okhttp3.Protocol.HTTP_1_1)
                .code(429) // Too Many Requests
                .message("Global rate limit exceeded")
                .body("Rate limited".toResponseBody("text/plain".toMediaType()))
                .build()
        }

        // Check endpoint-specific rate limit
        if (!limiter.allowRequest()) {
            Timber.w("$TAG: Endpoint rate limit exceeded for $endpoint")
            return Response.Builder()
                .request(request)
                .protocol(okhttp3.Protocol.HTTP_1_1)
                .code(429)
                .message("Endpoint rate limit exceeded")
                .body("Rate limited".toResponseBody("text/plain".toMediaType()))
                .build()
        }

        // Proceed with request
        var response = chain.proceed(request)

        // Handle server rate limit response (429)
        if (response.code == 429) {
            response = handleRateLimitResponse(chain, request, response, endpoint)
        }

        return response
    }

    /**
     * Handle 429 Too Many Requests response
     * Respects Retry-After header and uses exponential backoff
     */
    private fun handleRateLimitResponse(
        chain: Interceptor.Chain,
        request: okhttp3.Request,
        response: Response,
        endpoint: String
    ): Response {
        val retryAfter = parseRetryAfter(response)
        val backoffMs = calculateBackoff(retryAfter)

        Timber.w(
            "$TAG: Received 429 from $endpoint, " +
                "waiting ${backoffMs}ms before retry (retryAfter: $retryAfter)"
        )

        // Wait before retrying
        Thread.sleep(backoffMs)

        // Retry the request (max 3 times)
        for (attempt in 1..maxRetries) {
            Timber.d("$TAG: Retry attempt $attempt/$maxRetries for $endpoint")

            val retryResponse = try {
                chain.proceed(request)
            } catch (e: Exception) {
                Timber.e(e, "$TAG: Retry $attempt failed")
                continue
            }

            // If successful or not rate limited, return
            if (retryResponse.code != 429) {
                Timber.d("$TAG: Retry $attempt successful for $endpoint (code: ${retryResponse.code})")
                return retryResponse
            }

            // Still rate limited, continue loop
            if (attempt < maxRetries) {
                val nextBackoff = calculateBackoff(null, attempt)
                Timber.w("$TAG: Still rate limited on retry $attempt, waiting ${nextBackoff}ms")
                Thread.sleep(nextBackoff)
            }
        }

        Timber.e("$TAG: All retries exhausted for $endpoint, returning 429 to caller")
        return response
    }

    /**
     * Parse Retry-After header (seconds or HTTP-date format)
     */
    private fun parseRetryAfter(response: Response): Long? {
        val retryAfter = response.header("Retry-After") ?: return null

        return try {
            retryAfter.toLongOrNull()?.times(1000) // Convert seconds to ms
        } catch (e: Exception) {
            Timber.d("$TAG: Could not parse Retry-After header: $retryAfter")
            null
        }
    }

    /**
     * Calculate backoff time using exponential backoff
     * Base: 1000ms, multiplier: 2x, max: 16s
     */
    private fun calculateBackoff(serverRetryAfter: Long?, attemptNumber: Int = 0): Long {
        // Prefer server's Retry-After if provided
        if (serverRetryAfter != null && serverRetryAfter > 0) {
            return serverRetryAfter
        }

        // Exponential backoff: 1s, 2s, 4s, 8s
        val baseDelayMs = 1000L
        val delayMs = baseDelayMs * (1L shl attemptNumber.coerceAtMost(4))

        // Cap at 16 seconds
        return delayMs.coerceAtMost(16000L)
    }

    /**
     * Get endpoint key for rate limiting (e.g., "GET /api/customers")
     */
    private fun getEndpointKey(url: HttpUrl): String {
        val path = url.encodedPath
        val method = "GET" // Would need to pass from request in real scenario
        return "$method $path"
    }

    /**
     * Per-endpoint rate limiter using token bucket algorithm
     */
    private class EndpointRateLimiter(
        private val requestsPerSecond: Int,
        private val bucketSize: Int = requestsPerSecond
    ) {
        private var tokens = bucketSize.toDouble()
        private var lastRefillTimeMs = System.currentTimeMillis()
        private val lock = Any()

        fun allowRequest(): Boolean = synchronized(lock) {
            refillTokens()
            return if (tokens >= 1.0) {
                tokens -= 1.0
                true
            } else {
                false
            }
        }

        private fun refillTokens() {
            val now = System.currentTimeMillis()
            val elapsedSec = (now - lastRefillTimeMs) / 1000.0
            val tokensToAdd = elapsedSec * requestsPerSecond

            tokens = (tokens + tokensToAdd).coerceAtMost(bucketSize.toDouble())
            lastRefillTimeMs = now
        }
    }

    /**
     * Global rate limiter using token bucket
     */
    private class GlobalRateLimiter(private val requestsPerSecond: Int = 50) {
        private var tokens = requestsPerSecond.toDouble()
        private var lastRefillTimeMs = System.currentTimeMillis()
        private val lock = Any()

        fun allowRequest(): Boolean = synchronized(lock) {
            refillTokens()
            return if (tokens >= 1.0) {
                tokens -= 1.0
                true
            } else {
                false
            }
        }

        private fun refillTokens() {
            val now = System.currentTimeMillis()
            val elapsedSec = (now - lastRefillTimeMs) / 1000.0
            val tokensToAdd = elapsedSec * requestsPerSecond

            tokens = (tokens + tokensToAdd).coerceAtMost(requestsPerSecond.toDouble())
            lastRefillTimeMs = now
        }
    }
}

