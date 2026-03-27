package com.emul8r.bizap.domain.analytics

/**
 * Domain events for invoice analytics.
 *
 * Tracks key business events for reporting and insights.
 * Events are logged and persisted for later analysis and reporting.
 *
 * **Event Types:**
 * - InvoiceCreated: When a new invoice is created
 * - InvoiceViewed: When an invoice is viewed by user
 * - StatusChanged: When invoice status changes (DRAFT → SENT, etc)
 * - PaymentRecorded: When a payment is recorded
 *
 * Events enable:
 * - Historical trend analysis
 * - User behavior insights
 * - Predictive analytics
 * - Custom report generation
 * - Audit trails
 */
sealed class InvoiceAnalyticsEvent {
    /**
     * Unique business identifier for multi-tenant isolation.
     */
    abstract val businessId: Long

    /**
     * Event timestamp in milliseconds.
     * Defaults to current time when event is created.
     */
    abstract val timestamp: Long

    /**
     * Fired when a new invoice is created.
     *
     * @param businessId Business that created the invoice
     * @param invoiceId ID of the new invoice
     * @param amount Invoice amount in cents
     * @param timestamp When the invoice was created
     */
    data class InvoiceCreated(
        override val businessId: Long,
        val invoiceId: Long,
        val amount: Long,
        override val timestamp: Long = System.currentTimeMillis()
    ) : InvoiceAnalyticsEvent()

    /**
     * Fired when an invoice is viewed by a user.
     *
     * Used to track customer engagement and popular invoices.
     *
     * @param businessId Business that owns the invoice
     * @param invoiceId ID of the invoice viewed
     * @param timestamp When the invoice was viewed
     */
    data class InvoiceViewed(
        override val businessId: Long,
        val invoiceId: Long,
        override val timestamp: Long = System.currentTimeMillis()
    ) : InvoiceAnalyticsEvent()

    /**
     * Fired when an invoice status changes.
     *
     * Examples: DRAFT → SENT, SENT → PAID, any status transition
     *
     * @param businessId Business that owns the invoice
     * @param invoiceId ID of the invoice
     * @param oldStatus Previous status (optional)
     * @param newStatus New status after change
     * @param timestamp When the status changed
     */
    data class StatusChanged(
        override val businessId: Long,
        val invoiceId: Long,
        val oldStatus: String? = null,
        val newStatus: String,
        override val timestamp: Long = System.currentTimeMillis()
    ) : InvoiceAnalyticsEvent()

    /**
     * Fired when a payment is recorded against an invoice.
     *
     * Tracks payment activity for collection metrics and trends.
     *
     * @param businessId Business that received the payment
     * @param invoiceId ID of the invoice being paid
     * @param amount Payment amount in cents
     * @param timestamp When the payment was recorded
     */
    data class PaymentRecorded(
        override val businessId: Long,
        val invoiceId: Long,
        val amount: Long,
        override val timestamp: Long = System.currentTimeMillis()
    ) : InvoiceAnalyticsEvent()
}

