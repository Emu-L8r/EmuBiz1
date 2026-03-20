package com.emul8r.bizap.data.error

import com.emul8r.bizap.domain.error.BizapException
import com.emul8r.bizap.domain.error.ErrorSeverity
import com.emul8r.bizap.domain.error.severity
import timber.log.Timber

/**
 * ErrorHandler - Maps exceptions to user-friendly messages
 *
 * Centralized error handling that:
 * 1. Converts technical errors to user-friendly messages
 * 2. Logs errors appropriately (with context)
 * 3. Determines severity (critical, warning, info)
 * 4. Provides recovery suggestions
 *
 * USAGE IN VIEWMODEL:
 * ===================
 * try {
 *     invoiceRepository.saveInvoice(invoice)
 * } catch (e: Exception) {
 *     val errorInfo = ErrorHandler.handle(e)
 *     _snackbarMessage.emit(errorInfo.userMessage)
 *     _errorState.emit(errorInfo)
 * }
 */
object ErrorHandler {

    /**
     * ErrorInfo - All information about an error for UI display
     *
     * @param userMessage What to show user (friendly, actionable)
     * @param title Title for error dialog (optional)
     * @param severity How critical is this error
     * @param shouldRetry Whether we should/can retry
     * @param recoveryAction What user can do (e.g., "Check your internet connection")
     * @param technicalDetails For logging (actual exception + stack trace)
     */
    data class ErrorInfo(
        val userMessage: String,
        val title: String? = null,
        val severity: ErrorSeverity = ErrorSeverity.MEDIUM,
        val shouldRetry: Boolean = false,
        val recoveryAction: String? = null,
        val technicalDetails: String? = null
    )

    /**
     * Handle any exception and return user-friendly error information
     *
     * EXAMPLE:
     *   try {
     *     repository.fetch()
     *   } catch (e: Exception) {
     *     val info = ErrorHandler.handle(e, context = "InvoiceListViewModel.load()")
     *     showSnackbar(info.userMessage)
     *     if (info.severity == ErrorSeverity.CRITICAL) {
     *       logToAnalytics(info)
     *     }
     *   }
     *
     * @param exception The exception to handle
     * @param context Where the error occurred (for logging)
     * @return ErrorInfo with user message and metadata
     */
    fun handle(exception: Exception, context: String? = null): ErrorInfo {
        // Log the error with context
        logError(exception, context)

        // Map to user-friendly message
        return when (exception) {
            // =============================================
            // VALIDATION ERRORS - User input problems
            // =============================================

            is BizapException.ValidationError -> ErrorInfo(
                userMessage = "Invalid ${exception.field}: ${exception.message}",
                title = "Validation Error",
                severity = ErrorSeverity.HIGH,
                shouldRetry = false,
                recoveryAction = "Please correct the ${exception.field} and try again",
                technicalDetails = "Field: ${exception.field}, Rule: ${exception.validationRule}, Value: ${exception.actualValue}"
            )

            is BizapException.InvalidInvoiceError -> ErrorInfo(
                userMessage = exception.requiredFix ?: exception.reason,
                title = "Invoice Error",
                severity = ErrorSeverity.HIGH,
                shouldRetry = false,
                recoveryAction = "Fix the invoice: ${exception.reason}",
                technicalDetails = "Invoice ID: ${exception.invoiceId}, Reason: ${exception.reason}"
            )

            // =============================================
            // DATABASE ERRORS - Data persistence
            // =============================================

            is BizapException.DatabaseError -> ErrorInfo(
                userMessage = "Failed to save your data. Your changes may not be saved.",
                title = "Save Error",
                severity = ErrorSeverity.CRITICAL,
                shouldRetry = true,
                recoveryAction = "Try again. If problem persists, restart the app.",
                technicalDetails = "Operation: ${exception.operation}, Table: ${exception.table}, Details: ${exception.message}"
            )

            is BizapException.MigrationError -> ErrorInfo(
                userMessage = "App data structure error. Please restart the app.",
                title = "Database Error",
                severity = ErrorSeverity.CRITICAL,
                shouldRetry = false,
                recoveryAction = "Restart the app. If this persists, contact support.",
                technicalDetails = "Migration ${exception.fromVersion}→${exception.toVersion}: ${exception.reason}"
            )

            // =============================================
            // NETWORK ERRORS - API communication
            // =============================================

            is BizapException.NetworkError -> {
                val (userMsg, retry) = when (exception.statusCode) {
                    400, 401, 403 -> {
                        // Client errors - don't retry
                        "Request failed. Please check your connection and try again." to false
                    }
                    404 -> {
                        // Not found
                        "The requested data was not found." to false
                    }
                    429 -> {
                        // Too many requests - wait before retrying
                        "Too many requests. Waiting before trying again..." to true
                    }
                    500, 502, 503, 504 -> {
                        // Server errors - can retry
                        "Server is experiencing problems. We'll try again automatically." to true
                    }
                    null -> {
                        // No response (network issue)
                        "Network connection problem. Retrying..." to true
                    }
                    else -> {
                        // Other HTTP errors
                        "Network error (${exception.statusCode}). Please try again." to exception.isRetryable
                    }
                }

                ErrorInfo(
                    userMessage = userMsg,
                    title = "Connection Problem",
                    severity = if (exception.retryCount > 0) ErrorSeverity.MEDIUM else ErrorSeverity.LOW,
                    shouldRetry = retry,
                    recoveryAction = "Check your internet connection",
                    technicalDetails = "Endpoint: ${exception.endpoint}, Status: ${exception.statusCode}, Retries: ${exception.retryCount}"
                )
            }

            is BizapException.TimeoutError -> ErrorInfo(
                userMessage = "Request took too long. Retrying with connection...",
                title = "Timeout",
                severity = ErrorSeverity.MEDIUM,
                shouldRetry = true,
                recoveryAction = "Check your internet connection strength",
                technicalDetails = "Endpoint: ${exception.endpoint}, Timeout: ${exception.timeoutMs}ms"
            )

            is BizapException.ConnectivityError -> ErrorInfo(
                userMessage = "No internet connection. Using cached data when available.",
                title = "Offline",
                severity = ErrorSeverity.MEDIUM,
                shouldRetry = true,
                recoveryAction = "Enable WiFi or cellular connection",
                technicalDetails = exception.message
            )

            // =============================================
            // FILE ERRORS - Document operations
            // =============================================

            is BizapException.FileError -> ErrorInfo(
                userMessage = when (exception.operation) {
                    "PDF_GENERATION" -> "Failed to create PDF. Check storage space."
                    "LOGO_READ" -> "Failed to load logo image."
                    else -> "File operation failed."
                },
                title = "File Error",
                severity = ErrorSeverity.CRITICAL,
                shouldRetry = false,
                recoveryAction = "Check app permissions or storage space",
                technicalDetails = "Operation: ${exception.operation}, File: ${exception.filePath}, Reason: ${exception.reason}"
            )

            is BizapException.StorageError -> ErrorInfo(
                userMessage = "Device storage is full. Free up space and try again.",
                title = "Storage Full",
                severity = ErrorSeverity.HIGH,
                shouldRetry = false,
                recoveryAction = "Free up storage space on your device",
                technicalDetails = exception.message
            )

            // =============================================
            // BUSINESS LOGIC ERRORS - Rule violations
            // =============================================

            is BizapException.BusinessLogicError -> ErrorInfo(
                userMessage = exception.reason,
                title = "Not Allowed",
                severity = ErrorSeverity.HIGH,
                shouldRetry = false,
                recoveryAction = "Follow the requirement: ${exception.rule}",
                technicalDetails = "Rule: ${exception.rule}, Action: ${exception.action}"
            )

            is BizapException.DuplicateError -> ErrorInfo(
                userMessage = "${exception.entityType} '${exception.identifier}' already exists",
                title = "Duplicate",
                severity = ErrorSeverity.MEDIUM,
                shouldRetry = false,
                recoveryAction = "Use a different ${exception.entityType.lowercase()} or edit the existing one",
                technicalDetails = "Entity: ${exception.entityType}, ID: ${exception.existingId}"
            )

            is BizapException.NotFoundError -> ErrorInfo(
                userMessage = "${exception.entityType} not found. It may have been deleted.",
                title = "Not Found",
                severity = ErrorSeverity.HIGH,
                shouldRetry = false,
                recoveryAction = "Check that the ${exception.entityType.lowercase()} still exists",
                technicalDetails = "Entity: ${exception.entityType}, Identifier: ${exception.identifier}"
            )

            // =============================================
            // UNKNOWN ERRORS - Unexpected
            // =============================================

            is BizapException.UnknownError -> ErrorInfo(
                userMessage = "An unexpected error occurred. The app might work after restart.",
                title = "Error",
                severity = ErrorSeverity.CRITICAL,
                shouldRetry = false,
                recoveryAction = "Restart the app. If problem persists, contact support.",
                technicalDetails = "Context: ${exception.context}, Message: ${exception.message}, Original: ${exception.originalException?.message}"
            )

            // =============================================
            // GENERIC EXCEPTIONS - Third-party or system
            // =============================================

            is IllegalArgumentException -> ErrorInfo(
                userMessage = "Invalid input: ${exception.message}",
                title = "Invalid Input",
                severity = ErrorSeverity.HIGH,
                shouldRetry = false,
                recoveryAction = "Check your input and try again",
                technicalDetails = exception.message
            )

            is IllegalStateException -> ErrorInfo(
                userMessage = "App is in an unexpected state. Try restarting.",
                title = "State Error",
                severity = ErrorSeverity.CRITICAL,
                shouldRetry = false,
                recoveryAction = "Restart the app",
                technicalDetails = exception.message
            )

            is NullPointerException -> ErrorInfo(
                userMessage = "A necessary piece of data is missing. Try restarting the app.",
                title = "Data Error",
                severity = ErrorSeverity.CRITICAL,
                shouldRetry = false,
                recoveryAction = "Restart the app. If issue persists, contact support.",
                technicalDetails = "Null pointer at ${exception.stackTrace.firstOrNull()}"
            )

            is OutOfMemoryError -> ErrorInfo(
                userMessage = "App ran out of memory. Please restart.",
                title = "Memory Error",
                severity = ErrorSeverity.CRITICAL,
                shouldRetry = false,
                recoveryAction = "Restart the app and close other apps",
                technicalDetails = "Out of memory error"
            )

            is RuntimeException -> ErrorInfo(
                userMessage = "An error occurred. Try again or restart the app.",
                title = "Runtime Error",
                severity = ErrorSeverity.CRITICAL,
                shouldRetry = false,
                recoveryAction = "Restart the app",
                technicalDetails = exception.message
            )

            // Catch-all for any other exception
            else -> ErrorInfo(
                userMessage = "Something went wrong. Try again or restart the app.",
                title = "Error",
                severity = ErrorSeverity.MEDIUM,
                shouldRetry = true,
                recoveryAction = "Try again. If problem continues, restart the app.",
                technicalDetails = "${exception.javaClass.simpleName}: ${exception.message}"
            )
        }
    }

    /**
     * Log error with appropriate level and context
     *
     * LOGGING RULES:
     * - ValidationErrors: Warn (user's mistake, expected)
     * - NetworkErrors: Info (temporary, expected)
     * - DatabaseErrors: Error (data at risk)
     * - UnknownErrors: Error (unexpected)
     * - Critical severity: Send to Firebase
     *
     * @param exception The exception to log
     * @param context Where it occurred (ViewModel, Repository, etc.)
     */
    private fun logError(exception: Exception, context: String?) {
        val contextPrefix = if (context != null) "[$context] " else ""
        val errorType = exception.javaClass.simpleName

        when {
            // User input errors - log as warning (expected)
            exception is BizapException.ValidationError ||
            exception is BizapException.InvalidInvoiceError -> {
                Timber.w(exception, "${contextPrefix}Validation error: ${exception.message}")
            }

            // Temporary network issues - log as info (expected)
            exception is BizapException.NetworkError ||
            exception is BizapException.TimeoutError ||
            exception is BizapException.ConnectivityError -> {
                Timber.i(exception, "${contextPrefix}Network issue: ${exception.message}")
            }

            // Data problems - log as error (unexpected, needs investigation)
            exception is BizapException.DatabaseError ||
            exception is BizapException.FileError ||
            exception is BizapException.MigrationError -> {
                Timber.e(exception, "${contextPrefix}Data error: ${exception.message}")
            }

            // Other BizapExceptions
            exception is BizapException -> {
                Timber.w(exception, "${contextPrefix}App error: ${exception.message}")
            }

            // Generic exceptions
            else -> {
                Timber.e(exception, "${contextPrefix}Unexpected error: ${exception.message}")
            }
        }

        // For critical errors, log extra context for debugging
        if (exception is BizapException && exception.severity() == ErrorSeverity.CRITICAL) {
            Timber.e("CRITICAL ERROR - Context: $context, Exception: $errorType, Message: ${exception.message}")
        }
    }
}

/**
 * Extension function: Safely handle any exception in a coroutine
 *
 * USAGE:
 *   viewModelScope.launch {
 *       try {
 *           loadData()
 *       } catch (e: Exception) {
 *           _errorState.emit(e.toErrorInfo("Loading data"))
 *       }
 *   }
 */
fun Exception.toErrorInfo(context: String? = null): ErrorHandler.ErrorInfo {
    return ErrorHandler.handle(this, context)
}

