package com.emul8r.bizap.domain.exception

/**
 * Thrown when invoice settings are required but not found or not initialized.
 * This indicates the user needs to complete the PDF Settings screen first.
 */
class SettingsNotInitializedException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

/**
 * Thrown when a required field in InvoiceSettings is NULL or invalid.
 */
class InvalidSettingsException(
    message: String,
    val fieldName: String? = null,
    cause: Throwable? = null
) : Exception(message, cause)
