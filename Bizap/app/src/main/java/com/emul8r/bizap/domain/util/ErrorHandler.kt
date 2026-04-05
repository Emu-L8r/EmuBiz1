package com.emul8r.bizap.domain.util

import android.content.Context
import com.emul8r.bizap.R
import timber.log.Timber
import java.io.IOException
import java.sql.SQLException

/**
 * Centralized error handling utility for consistent error messages and logging.
 *
 * Converts exceptions to user-friendly messages and logs for debugging.
 * Supports PDF generation, database, network, and general errors.
 */
object ErrorHandler {

    /**
     * Get user-friendly error message from exception.
     *
     * @param exception The exception that occurred
     * @param context Android context for string resources
     * @return User-friendly error message
     */
    fun getUserMessage(exception: Exception, context: Context): String {
        return when (exception) {
            // PDF Generation Errors
            is IOException -> "Failed to save PDF. Check storage permissions."
            is IllegalStateException -> {
                when {
                    exception.message?.contains("Settings") == true ->
                        "Invoice settings missing. Please configure PDF settings."
                    exception.message?.contains("Snapshot") == true ->
                        "Invoice data is incomplete. Please verify all fields."
                    else -> "An error occurred. Please try again."
                }
            }

            // Database Errors
            is SQLException ->
                "Database error. Please restart the app and try again."
            is RuntimeException -> {
                when {
                    exception.message?.contains("database") == true ->
                        "Database sync error. Please restart the app."
                    else -> exception.message ?: "An unexpected error occurred."
                }
            }

            // Network Errors
            is java.net.SocketTimeoutException ->
                "Network timeout. Check your connection and try again."
            is java.net.UnknownHostException ->
                "Network error. Check your internet connection."

            // Null Pointer & Type Errors
            is NullPointerException ->
                "Data error. Please restart and try again."
            is ClassCastException ->
                "Data format error. Please restart the app."

            // Default
            else -> exception.message ?: "An unexpected error occurred. Please try again."
        }
    }

    /**
     * Log error with context for debugging.
     *
     * @param tag Error tag/category
     * @param message Error message
     * @param exception Optional exception for stacktrace
     */
    fun logError(tag: String, message: String, exception: Exception? = null) {
        val fullMessage = "[$tag] $message"
        if (exception != null) {
            Timber.e(exception, fullMessage)
        } else {
            Timber.e(fullMessage)
        }
    }

    /**
     * Safely execute operation with error handling.
     *
     * @param operation Lambda to execute
     * @param onError Lambda called with error message if exception occurs
     */
    suspend inline fun <T> safeExecute(
        operation: suspend () -> T,
        crossinline onError: (String) -> Unit
    ): T? {
        return try {
            operation()
        } catch (e: Exception) {
            onError(e.message ?: "Unknown error occurred")
            logError("SafeExecute", "Operation failed", e)
            null
        }
    }

    /**
     * Validate PDF generation prerequisites.
     *
     * @param hasSettings Whether settings are available
     * @param hasSnapshot Whether invoice snapshot is valid
     * @return Error message if validation fails, null if OK
     */
    fun validatePdfGeneration(hasSettings: Boolean, hasSnapshot: Boolean): String? {
        return when {
            !hasSettings -> "PDF settings not configured. Please configure in Settings."
            !hasSnapshot -> "Invoice data is incomplete. Please verify all required fields."
            else -> null
        }
    }

    /**
     * Check if error is recoverable (user can retry).
     *
     * @param exception The exception to check
     * @return true if user should be offered retry option
     */
    fun isRecoverable(exception: Exception): Boolean {
        return exception is IOException ||
                exception is java.net.SocketTimeoutException ||
                exception is java.net.UnknownHostException ||
                exception is SQLException
    }

    /**
     * Check if error requires app restart.
     *
     * @param exception The exception to check
     * @return true if user should restart app
     */
    fun requiresRestart(exception: Exception): Boolean {
        return exception is NullPointerException ||
                exception is ClassCastException ||
                (exception is RuntimeException &&
                 exception.message?.contains("database") == true)
    }
}





