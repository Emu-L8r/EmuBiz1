package com.emul8r.bizap.utils

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import timber.log.Timber

/**
 * Firebase Analytics Event Tracking Utility
 *
 * Central location for all Firebase event logging with consistent patterns.
 * Use this to track user behavior, identify usage patterns, and collect metrics.
 *
 * @param analytics FirebaseAnalytics instance
 */
class FirebaseEventTracker(private val analytics: FirebaseAnalytics?) {

    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // INVOICE LIFECYCLE EVENTS
    // ═══════════════════════════════════════════════════════════════════════════════════════════

    /**
     * Track when a new invoice is created.
     *
     * @param invoiceId Unique invoice ID
     * @param customerId Customer associated with invoice
     * @param amount Total amount in cents
     * @param currencyCode Currency code (e.g., "AUD", "USD")
     * @param lineItemCount Number of line items
     *
     * Example:
     * ```
     * tracker.trackInvoiceCreated(
     *     invoiceId = 42,
     *     customerId = 1,
     *     amount = 10000,  // $100.00
     *     currencyCode = "AUD",
     *     lineItemCount = 3
     * )
     * ```
     */
    fun trackInvoiceCreated(
        invoiceId: Long,
        customerId: Long,
        amount: Long,
        currencyCode: String,
        lineItemCount: Int
    ) {
        val bundle = Bundle().apply {
            putLong("invoice_id", invoiceId)
            putLong("customer_id", customerId)
            putLong("amount_cents", amount)
            putString("currency_code", currencyCode)
            putInt("line_item_count", lineItemCount)
        }
        logEvent("event_invoice_created", bundle)
    }

    /**
     * Track when user views an invoice.
     *
     * @param invoiceId Invoice being viewed
     * @param viewDurationMs Duration spent viewing (optional, call when leaving screen)
     *
     * Usage:
     * ```
     * // When entering detail screen
     * tracker.trackInvoiceViewed(invoiceId = 42)
     *
     * // When leaving detail screen
     * val duration = System.currentTimeMillis() - startTime
     * tracker.trackInvoiceViewed(invoiceId = 42, viewDurationMs = duration)
     * ```
     */
    fun trackInvoiceViewed(invoiceId: Long, viewDurationMs: Long? = null) {
        val bundle = Bundle().apply {
            putLong("invoice_id", invoiceId)
            if (viewDurationMs != null) {
                putLong("view_duration_ms", viewDurationMs)
            }
        }
        logEvent("event_invoice_viewed", bundle)
    }

    /**
     * Track when invoice is edited.
     *
     * @param invoiceId Invoice being edited
     * @param fieldsModified List of field names that were changed (e.g., "amount", "customer", "items")
     */
    fun trackInvoiceEdited(invoiceId: Long, fieldsModified: List<String>) {
        val bundle = Bundle().apply {
            putLong("invoice_id", invoiceId)
            putString("fields_modified", fieldsModified.joinToString(","))
        }
        logEvent("event_invoice_edited", bundle)
    }

    /**
     * Track when invoice is deleted.
     *
     * @param invoiceId Invoice being deleted
     * @param reason Reason for deletion (e.g., "user_action", "duplicate", "correction")
     */
    fun trackInvoiceDeleted(invoiceId: Long, reason: String = "user_action") {
        val bundle = Bundle().apply {
            putLong("invoice_id", invoiceId)
            putString("reason", reason)
        }
        logEvent("event_invoice_deleted", bundle)
    }

    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // PAYMENT EVENTS
    // ═══════════════════════════════════════════════════════════════════════════════════════════

    /**
     * Track when payment is recorded against an invoice.
     *
     * @param invoiceId Invoice receiving payment
     * @param paymentAmount Payment amount in cents
     * @param paymentDate Payment date (epoch ms)
     * @param invoiceTotal Total invoice amount (for calculating completion %)
     *
     * Example:
     * ```
     * tracker.trackPaymentRecorded(
     *     invoiceId = 42,
     *     paymentAmount = 5000,  // $50.00
     *     paymentDate = System.currentTimeMillis(),
     *     invoiceTotal = 10000   // $100.00 total
     * )
     * ```
     */
    fun trackPaymentRecorded(
        invoiceId: Long,
        paymentAmount: Long,
        paymentDate: Long,
        invoiceTotal: Long
    ) {
        val completionPercent = if (invoiceTotal > 0) {
            ((paymentAmount * 100) / invoiceTotal).toInt()
        } else {
            0
        }

        val bundle = Bundle().apply {
            putLong("invoice_id", invoiceId)
            putLong("payment_amount_cents", paymentAmount)
            putLong("payment_date_ms", paymentDate)
            putInt("completion_percent", completionPercent)
        }
        logEvent("event_payment_recorded", bundle)
    }

    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // BUSINESS CONTEXT EVENTS
    // ═══════════════════════════════════════════════════════════════════════════════════════════

    /**
     * Track when user switches between businesses.
     *
     * @param fromBusinessId Current/old business
     * @param toBusinessId New business being switched to
     */
    fun trackBusinessSwitched(fromBusinessId: Long, toBusinessId: Long) {
        val bundle = Bundle().apply {
            putLong("from_business_id", fromBusinessId)
            putLong("to_business_id", toBusinessId)
        }
        logEvent("event_business_switched", bundle)
    }

    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // REVENUE METRICS EVENTS
    // ═══════════════════════════════════════════════════════════════════════════════════════════

    /**
     * Track revenue metrics calculated for dashboard.
     *
     * @param businessId Business for this metric
     * @param mtdRevenue Month-to-date revenue in cents
     * @param outstandingAmount Outstanding invoices in cents
     * @param overdueAmount Overdue invoices in cents
     * @param paymentCompletionPercent % of invoices that are paid
     *
     * Example:
     * ```
     * tracker.trackRevenueMetrics(
     *     businessId = 1,
     *     mtdRevenue = 150000,      // $1,500.00
     *     outstandingAmount = 30000, // $300.00
     *     overdueAmount = 10000,     // $100.00
     *     paymentCompletionPercent = 87
     * )
     * ```
     */
    fun trackRevenueMetrics(
        businessId: Long,
        mtdRevenue: Long,
        outstandingAmount: Long,
        overdueAmount: Long,
        paymentCompletionPercent: Int
    ) {
        val bundle = Bundle().apply {
            putLong("business_id", businessId)
            putLong("mtd_revenue_cents", mtdRevenue)
            putLong("outstanding_cents", outstandingAmount)
            putLong("overdue_cents", overdueAmount)
            putInt("payment_completion_percent", paymentCompletionPercent)
        }
        logEvent("event_revenue_metrics", bundle)
    }

    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // FEATURE USAGE EVENTS
    // ═══════════════════════════════════════════════════════════════════════════════════════════

    /**
     * Track custom feature usage.
     *
     * @param featureName Name of feature (e.g., "export_pdf", "print_invoice", "email_invoice")
     * @param metadata Optional additional context
     */
    fun trackFeatureUsed(featureName: String, metadata: Map<String, String>? = null) {
        val bundle = Bundle().apply {
            putString("feature_name", featureName)
            if (metadata != null) {
                for ((key, value) in metadata) {
                    putString(key, value)
                }
            }
        }
        logEvent("event_feature_used", bundle)
    }

    /**
     * Track UI/Screen view with context.
     *
     * @param screenName Name of screen
     * @param screenClass Full class name for tracking
     */
    fun trackScreenView(screenName: String, screenClass: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass)
        }
        logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    /**
     * Track errors/exceptions with context.
     *
     * @param errorType Type of error (e.g., "validation_error", "network_error")
     * @param errorMessage Human-readable error message
     * @param stackTrace Stack trace (optional, for debugging)
     */
    fun trackError(errorType: String, errorMessage: String, stackTrace: String? = null) {
        val bundle = Bundle().apply {
            putString("error_type", errorType)
            putString("error_message", errorMessage)
            if (stackTrace != null) {
                // Only first 500 chars to avoid quota limits
                putString("stack_trace", stackTrace.take(500))
            }
        }
        logEvent("event_error", bundle)
    }

    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // INTERNAL
    // ═══════════════════════════════════════════════════════════════════════════════════════════

    private fun logEvent(eventName: String, params: Bundle) {
        try {
            if (analytics != null) {
                analytics.logEvent(eventName, params)
                Timber.d("📊 Firebase event logged: $eventName")
            } else {
                // Firebase not available - log to Timber only
                Timber.d("📊 Firebase event QUEUED (Firebase not available): $eventName")
            }
        } catch (e: Exception) {
            Timber.w(e, "Failed to log Firebase event: $eventName")
        }
    }
}

