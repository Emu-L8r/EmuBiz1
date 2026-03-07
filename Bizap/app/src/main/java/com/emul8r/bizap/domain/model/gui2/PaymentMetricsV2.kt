package com.emul8r.bizap.domain.model.gui2

/**
 * Payment analytics metrics for GUI2.
 * Derived directly from the invoices table (Option C).
 */
data class PaymentMetricsV2(
    val businessProfileId: Long,
    val totalInvoices: Int,
    val paidCount: Int,
    val sentCount: Int,
    val overdueCount: Int,
    val partiallyPaidCount: Int,
    val draftCount: Int,
    val outstandingAmount: Long,        // cents
    val collectedAmount: Long,          // cents
    val averageDaysToPayment: Double,   // average days from issue to payment
    val statusBreakdown: List<StatusBreakdownV2>
)

/**
 * Count of invoices per status.
 */
data class StatusBreakdownV2(
    val status: String,
    val count: Int
)
