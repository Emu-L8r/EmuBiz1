package com.emul8r.bizap.domain.model

/**
 * Sealed class representing the result of an API call.
 */
sealed class ApiResult<out T> {
    data class Success<out T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val throwable: Throwable? = null) : ApiResult<Nothing>()
}
