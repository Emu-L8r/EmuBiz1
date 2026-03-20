package com.emul8r.bizap.domain.invoice.model

import java.time.LocalDate

/**
 * Invoice payment status classification for analytics.
 */
enum class PaymentStatus {
    PAID, PARTIALLY_PAID, UNPAID, OVERDUE, CANCELLED
}

/**
 * Supported payment methods.
 */
enum class PaymentMethod {
    CASH, CHECK, CREDIT_CARD, BANK_TRANSFER, OTHER
}

/**
 * Ageing buckets for outstanding invoices.
 */
enum class AgeingBucket {
    CURRENT, PAST_30, PAST_60, PAST_90
}

/**
 * Outstanding amount broken down by aging brackets.
 */
data class OutstandingByAging(
    val current: Double,
    val past30: Double,
    val past60: Double,
    val past90: Double,
    val totalOutstanding: Double
)

/**
 * Summary of payment analytics for a business.
 */
data class PaymentAnalyticsSummary(
    val businessProfileId: Long,
    val totalInvoices: Int,
    val paidInvoices: Int,
    val unpaidInvoices: Int,
    val overdueInvoices: Int,
    val totalInvoiceAmount: Double,
    val totalPaidAmount: Double,
    val totalOutstandingAmount: Double,
    val collectionRate: Double,
    val averagePaymentTime: Double,
    val outstandingByAging: OutstandingByAging,
    val riskInvoices: List<InvoicePaymentStatus>,
    val cashFlowForecast: List<CashFlowForecast>
)

/**
 * Detailed payment status for a specific invoice.
 */
data class InvoicePaymentStatus(
    val invoiceId: Long,
    val invoiceNumber: String,
    val customerId: Long,
    val customerName: String,
    val invoiceDate: LocalDate,
    val dueDate: LocalDate,
    val totalAmount: Double,
    val paidAmount: Double,
    val outstandingAmount: Double,
    val paymentStatus: PaymentStatus,
    val ageingBucket: AgeingBucket,
    val daysOverdue: Int,
    val lastPaymentDate: LocalDate? = null,
    val paymentHistory: List<PaymentRecord> = emptyList()
)

/**
 * Record of an individual payment.
 */
data class PaymentRecord(
    val id: Long,
    val amount: Double,
    val date: LocalDate,
    val method: PaymentMethod,
    val reference: String,
    val notes: String?
)

/**
 * Notice regarding overdue payment.
 */
data class DunningNotice(
    val noticeId: Long,
    val customerId: Long,
    val customerName: String,
    val overdueInvoices: List<InvoicePaymentStatus>,
    val totalAmountDue: Double,
    val daysSinceDue: Int,
    val noticeLevel: Int, // 1, 2, 3
    val sentDate: LocalDate,
    val nextActionDate: LocalDate
)

/**
 * Projected cash flow based on payment patterns.
 */
data class CashFlowForecast(
    val projectedDate: LocalDate,
    val expectedRevenue: Double,
    val expectedPayments: Double,
    val netCashFlow: Double,
    val cumulativeCashFlow: Double,
    val confidence: Double // 0.0 to 1.0
)
