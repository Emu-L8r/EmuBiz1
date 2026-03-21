package com.emul8r.bizap.data.model

/**
 * Daily revenue snapshot for trend analysis.
 * Read-only data class for query results (NOT a Room entity).
 */
data class DailyRevenue(
    val businessId: Long,
    val date: Long,               // Stored as epoch milliseconds
    val invoicedCents: Long,      // Total amount invoiced this day
    val paidCents: Long,          // Total amount paid this day
    val invoiceCount: Int,        // Number of invoices
    val paidCount: Int            // Number of paid invoices
)

/**
 * Customer revenue aggregation for concentration analysis.
 * Read-only data class for query results.
 */
data class CustomerRevenue(
    val customerId: Long,
    val customerName: String,
    val totalRevenueCents: Long,  // All-time paid revenue from customer
    val invoiceCount: Int,
    val lastPaymentDate: Long? = null  // Stored as epoch milliseconds
)

/**
 * Invoice velocity metric for workflow efficiency.
 * Read-only data class for query results.
 */
data class InvoiceVelocity(
    val businessId: Long,
    val date: Long,               // Stored as epoch milliseconds
    val avgDaysFromCreationToSent: Double,
    val invoicesCreatedCount: Int,
    val invoicesSentCount: Int,   // Count of SENT invoices only
    val invoicesPaidCount: Int,   // NEW: Count of PAID invoices
    val invoicesInDraftCount: Int
)

/**
 * Payment metrics for DSO and collection analysis.
 * Aggregated read-only data class.
 */
data class PaymentMetrics(
    val averageDaysToPayment: Double,
    val totalOutstandingCents: Long,
    val totalCollectedCents: Long,
    val overdueInvoiceCount: Int,
    val overdueAmountCents: Long
)

/**
 * Cash flow data for 30-day trend visualization.
 */
data class CashFlowTrendPoint(
    val date: Long,               // Stored as epoch milliseconds
    val invoicedCents: Long,
    val paidCents: Long,
    val netCents: Long = paidCents - invoicedCents
)

/**
 * Top customer with revenue percentage.
 */
data class TopCustomerMetric(
    val customerId: Long,
    val customerName: String,
    val revenueCents: Long,
    val percentageOfTotal: Double,
    val invoiceCount: Int
) {
    val revenueFormatted: String
        get() = "\$${revenueCents / 100}.${String.format("%02d", revenueCents % 100)}"

    val percentageFormatted: String
        get() = String.format("%.1f%%", percentageOfTotal)
}

/**
 * Days to pay trend data point.
 */
data class DaysToPayMetric(
    val date: Long,               // Stored as epoch milliseconds
    val averageDaysToPayment: Double
) {
    val averageFormatted: String
        get() = String.format("%.1f days", averageDaysToPayment)
}

/**
 * Combined analytics data for dashboard.
 */
data class AnalyticsData(
    val cashFlowTrend: List<CashFlowTrendPoint>,
    val averageDaysToPayTrend: List<DaysToPayMetric>,
    val topCustomerMetrics: List<TopCustomerMetric>,
    val currentAverageDaysToPayment: Double,
    val totalRevenue: Long,
    val paymentMetrics: PaymentMetrics
)

