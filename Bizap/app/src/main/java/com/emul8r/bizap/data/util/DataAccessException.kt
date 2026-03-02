package com.emul8r.bizap.data.util

/**
 * Custom exception for data layer failures.
 */
class DataAccessException(message: String, cause: Throwable? = null) : Exception(message, cause)

