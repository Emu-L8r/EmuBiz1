package com.emul8r.bizap.data.logging

import com.emul8r.bizap.domain.error.BizapException
import com.emul8r.bizap.domain.error.ErrorSeverity
import com.emul8r.bizap.domain.error.severity
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ErrorLogger — central error reporting interface.
 *
 * All error reporting in the app should go through this interface
 * rather than calling Crashlytics or Timber directly. This keeps
 * logging consistent and makes production code testable.
 */
interface ErrorLogger {
    /**
     * Record an exception with optional key/value context.
     *
     * @param exception The throwable to record
     * @param context   Optional map of diagnostic key/value pairs (e.g. invoice_id → "42")
     */
    fun logError(exception: Throwable, context: Map<String, String> = emptyMap())

    /**
     * Log a named error message without an exception.
     *
     * @param tag     A short category tag (e.g. "InvoiceRepository")
     * @param message Human-readable description
     * @param context Optional diagnostic key/value pairs
     */
    fun logError(tag: String, message: String, context: Map<String, String> = emptyMap())

    /**
     * Add a breadcrumb for debugging session flow.
     *
     * @param action  Short label for the action (e.g. "invoice_saved")
     * @param details Optional key/value details about the action
     */
    fun addBreadcrumb(action: String, details: Map<String, String> = emptyMap())

    /**
     * Attach user identity to subsequent crash reports.
     *
     * @param userId Numeric user/business ID
     * @param email  User email (PII — only set after consent)
     */
    fun setUserContext(userId: Long, email: String)
}

/**
 * Production implementation backed by Firebase Analytics and Crashlytics.
 *
 * Both dependencies are nullable so the app continues to work when
 * Firebase is unavailable (e.g. local unit-test runs without google-services.json).
 */
@Singleton
class ErrorLoggerImpl @Inject constructor(
    private val analytics: FirebaseAnalytics?,
    private val crashlytics: FirebaseCrashlytics?
) : ErrorLogger {

    override fun logError(exception: Throwable, context: Map<String, String>) {
        // Always log locally so developers see it in Logcat
        val severity = (exception as? BizapException)?.severity()
        when (severity) {
            ErrorSeverity.CRITICAL, ErrorSeverity.HIGH ->
                Timber.e(exception, "ErrorLogger: ${exception.message}")
            else ->
                Timber.w(exception, "ErrorLogger: ${exception.message}")
        }

        // Record non-fatal in Crashlytics
        try {
            context.forEach { (key, value) -> crashlytics?.setCustomKey(key, value) }
            crashlytics?.recordException(exception)
        } catch (e: Exception) {
            Timber.w(e, "Crashlytics.recordException failed")
        }

        // Log event to Analytics so it appears in the Firebase console
        try {
            analytics?.logEvent("app_error") {
                param("error_type", exception::class.simpleName ?: "Unknown")
                param("error_message", (exception.message ?: "No message").take(100))
                param("error_severity", severity?.name ?: "UNKNOWN")
            }
        } catch (e: Exception) {
            Timber.w(e, "FirebaseAnalytics.logEvent(app_error) failed")
        }
    }

    override fun logError(tag: String, message: String, context: Map<String, String>) {
        Timber.e("[$tag] $message context=$context")
        try {
            crashlytics?.log("ERROR [$tag]: $message")
            context.forEach { (key, value) -> crashlytics?.setCustomKey(key, value) }
        } catch (e: Exception) {
            Timber.w(e, "Crashlytics.log failed")
        }
        try {
            analytics?.logEvent("app_error") {
                param("error_type", tag)
                param("error_message", message.take(100))
            }
        } catch (e: Exception) {
            Timber.w(e, "FirebaseAnalytics.logEvent(app_error/tag) failed")
        }
    }

    override fun addBreadcrumb(action: String, details: Map<String, String>) {
        val detailStr = details.entries.joinToString(", ") { "${it.key}=${it.value}" }
        Timber.d("🔹 $action: $detailStr")
        try {
            crashlytics?.log("🔹 $action: $detailStr")
        } catch (e: Exception) {
            Timber.w(e, "Crashlytics.log(breadcrumb) failed")
        }
    }

    override fun setUserContext(userId: Long, email: String) {
        try {
            crashlytics?.setUserId(userId.toString())
            crashlytics?.setCustomKey("user_email", email)
        } catch (e: Exception) {
            Timber.w(e, "Crashlytics.setUserId failed")
        }
        try {
            analytics?.setUserId(userId.toString())
        } catch (e: Exception) {
            Timber.w(e, "FirebaseAnalytics.setUserId failed")
        }
    }
}
