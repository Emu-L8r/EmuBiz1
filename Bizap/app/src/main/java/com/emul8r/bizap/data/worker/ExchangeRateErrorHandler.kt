package com.emul8r.bizap.data.worker

import timber.log.Timber
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Centralized error handler for Exchange Rate API operations.
 * Provides graceful degradation and detailed error categorization.
 */
object ExchangeRateErrorHandler {

    /**
     * Error classification for exchange rate operations
     */
    sealed class ApiError(val message: String, val isRetryable: Boolean) {
        /** No API key configured - cannot proceed */
        data object MissingApiKey : ApiError(
            message = "No API key configured. Exchange rate features disabled.",
            isRetryable = false
        )

        /** Network connectivity issues - should retry */
        data class NetworkError(val cause: Throwable?) : ApiError(
            message = "Network error: ${cause?.message ?: "Connection failed"}",
            isRetryable = true
        )

        /** API returned error response - may retry depending on status code */
        data class ApiFailure(val statusCode: Int?, val cause: Throwable?) : ApiError(
            message = "API failure (${statusCode ?: "unknown"}): ${cause?.message ?: "Unknown error"}",
            isRetryable = statusCode in listOf(429, 500, 502, 503, 504) // Rate limit or server errors
        )

        /** Timeout during API call - should retry */
        data class Timeout(val cause: Throwable?) : ApiError(
            message = "Request timeout: ${cause?.message ?: "API did not respond in time"}",
            isRetryable = true
        )

        /** Unexpected error - log and skip */
        data class Unknown(val cause: Throwable?) : ApiError(
            message = "Unexpected error: ${cause?.message ?: "Unknown"}",
            isRetryable = false
        )
    }

    /**
     * Classify exception into appropriate error type
     */
    fun classifyError(exception: Exception): ApiError {
        return when (exception) {
            is UnknownHostException -> ApiError.NetworkError(exception)
            is SocketTimeoutException -> ApiError.Timeout(exception)
            is IOException -> ApiError.NetworkError(exception)
            else -> {
                // Try to extract HTTP status code if available
                val statusCode = extractStatusCode(exception)
                if (statusCode != null) {
                    ApiError.ApiFailure(statusCode, exception)
                } else {
                    ApiError.Unknown(exception)
                }
            }
        }
    }

    /**
     * Log error with appropriate severity level
     */
    fun logError(error: ApiError) {
        when (error) {
            is ApiError.MissingApiKey -> {
                Timber.w("⚠️ Exchange Rate API: ${error.message}")
                Timber.w("   To enable, configure EXCHANGE_RATE_API_KEY in gradle.properties")
                Timber.w("   Get a free key at: https://www.exchangerate-api.com/")
            }
            is ApiError.NetworkError -> {
                Timber.w("🌐 Exchange Rate API: ${error.message}")
                Timber.w("   App will use cached rates until network is restored")
            }
            is ApiError.Timeout -> {
                Timber.w("⏱️ Exchange Rate API: ${error.message}")
                Timber.w("   App will retry on next scheduled sync")
            }
            is ApiError.ApiFailure -> {
                if (error.isRetryable) {
                    Timber.w("🔄 Exchange Rate API: ${error.message}")
                    Timber.w("   Will retry on next scheduled sync")
                } else {
                    Timber.e(error.cause, "❌ Exchange Rate API: ${error.message}")
                    Timber.e("   Check API key validity and quota at dashboard")
                }
            }
            is ApiError.Unknown -> {
                Timber.e(error.cause, "❌ Exchange Rate API: ${error.message}")
                Timber.e("   Unexpected error - please report if persistent")
            }
        }
    }

    /**
     * Determine if cached rates are acceptable fallback
     * @param lastUpdateMillis When rates were last successfully fetched
     * @return true if cached rates are recent enough to use
     */
    fun areCachedRatesAcceptable(lastUpdateMillis: Long?): Boolean {
        if (lastUpdateMillis == null) return false

        val now = System.currentTimeMillis()
        val ageInDays = (now - lastUpdateMillis) / (24 * 60 * 60 * 1000)

        // Cached rates acceptable if less than 7 days old
        return ageInDays < 7
    }

    /**
     * Get user-friendly message for error
     */
    fun getUserMessage(error: ApiError): String {
        return when (error) {
            is ApiError.MissingApiKey -> 
                "Exchange rates not configured. Using default rates."
            is ApiError.NetworkError -> 
                "Unable to update rates. Using cached rates."
            is ApiError.Timeout -> 
                "Rate update taking too long. Using cached rates."
            is ApiError.ApiFailure -> 
                if (error.isRetryable) 
                    "Rate service temporarily unavailable. Using cached rates."
                else 
                    "Rate service error. Using cached rates."
            is ApiError.Unknown -> 
                "Unable to update rates. Using cached rates."
        }
    }

    /**
     * Extract HTTP status code from exception if available
     * (Retrofit or other HTTP libraries may wrap status codes)
     */
    private fun extractStatusCode(exception: Exception): Int? {
        // Check if exception message contains HTTP status code
        val statusPattern = Regex("HTTP (\\d{3})")
        val match = statusPattern.find(exception.message ?: "")
        return match?.groupValues?.get(1)?.toIntOrNull()
    }
}
