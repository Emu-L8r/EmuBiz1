package com.emul8r.bizap.data.logging

import com.emul8r.bizap.domain.error.BizapException
import com.emul8r.bizap.domain.error.ErrorSeverity
import com.emul8r.bizap.domain.error.severity
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * CrashlyticsBridge — centralised error categorisation and severity routing.
 *
 * Wraps [ErrorLogger] and provides convenience helpers that automatically
 * derive severity, apply retry suggestions, and enrich breadcrumbs without
 * requiring each call site to understand the full error taxonomy.
 *
 * Typical usage in a repository:
 * ```kotlin
 * crashlyticsBridge.record(e, operation = "saveInvoice", entityId = invoice.id.toString())
 * ```
 */
@Singleton
class CrashlyticsBridge @Inject constructor(
    private val errorLogger: ErrorLogger
) {
    /**
     * Record a throwable with standard repository context.
     *
     * Automatically:
     * - Derives severity from [BizapException] subtype
     * - Logs retry suggestion for retryable errors
     * - Adds breadcrumb for traceability
     *
     * @param exception   The throwable to record
     * @param operation   What the caller was doing (e.g. "saveInvoice")
     * @param entityId    Optional primary-key / identifier for the entity involved
     * @param extraContext Additional diagnostic key/value pairs
     */
    fun record(
        exception: Throwable,
        operation: String,
        entityId: String? = null,
        extraContext: Map<String, String> = emptyMap()
    ) {
        val context = buildMap {
            put("operation", operation)
            if (entityId != null) put("entity_id", entityId)
            putAll(extraContext)
            put("error_class", exception::class.simpleName ?: "Unknown")
        }

        errorLogger.logError(exception, context)

        val severity = (exception as? BizapException)?.severity()
        if (severity == ErrorSeverity.CRITICAL || severity == ErrorSeverity.HIGH) {
            Timber.e("⚠️ [$severity] $operation failed — ${exception.message}")
        }
    }

    /**
     * Log a named warning without an exception.
     */
    fun warn(tag: String, message: String, context: Map<String, String> = emptyMap()) {
        errorLogger.logError(tag, message, context)
    }

    /**
     * Add a navigation / action breadcrumb so crash reports show what the user
     * was doing immediately before a crash.
     */
    fun breadcrumb(action: String, details: Map<String, String> = emptyMap()) {
        errorLogger.addBreadcrumb(action, details)
    }

    /**
     * Map a [BizapException] to a human-readable retry suggestion.
     *
     * Callers can surface this message in a Snackbar or error state.
     */
    fun retrySuggestion(exception: BizapException): String = when (exception) {
        is BizapException.NetworkError,
        is BizapException.TimeoutError,
        is BizapException.ConnectivityError -> "Check your internet connection and try again."
        is BizapException.DatabaseError -> "A local save error occurred. Please try again."
        is BizapException.ValidationError -> "Please fix the \"${exception.field}\" field."
        is BizapException.StorageError -> "Your device storage may be full. Free up space and retry."
        else -> "An error occurred. Please try again."
    }
}
