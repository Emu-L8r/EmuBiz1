package com.emul8r.bizap.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * Daily revenue snapshot for trend analysis.
 * Denormalized for query efficiency.
 */
@Entity(tableName = "daily_revenue_snapshots")
data class DailyRevenue(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val businessId: Long,
    val date: LocalDate,
    val invoicedCents: Long,      // Total amount invoiced this day
    val paidCents: Long,          // Total amount paid this day
    val invoiceCount: Int,        // Number of invoices
    val paidCount: Int,           // Number of paid invoices
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Customer revenue aggregation for concentration analysis.
 */
@Entity(tableName = "customer_revenue")
data class CustomerRevenue(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val businessId: Long,
    val customerId: Long,
    val customerName: String,
    val totalRevenueCents: Long,  // All-time paid revenue from customer
    val invoiceCount: Int,
    val lastPaymentDate: LocalDate? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Invoice velocity metric for workflow efficiency.
 */
@Entity(tableName = "invoice_velocity_metrics")
data class InvoiceVelocity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val businessId: Long,
    val date: LocalDate,
    val avgDaysFromCreationToSent: Double,
    val invoicesCreatedCount: Int,
    val invoicesSentCount: Int,
    val invoicesInDraftCount: Int,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Payment metrics for DSO and collection analysis.
 */
data class PaymentMetrics(
    val averageDaysToPayment: Double,
    val totalOutstandingCents: Long,
    val totalCollectedCents: Long,
    val overdueInvoiceCount: Int,
    val overdueAmountCents: Long,
    val invoiceCountByStatus: Map<String, Int>
)

/**
 * Cash flow data for 30-day trend visualization.
 */
data class CashFlowTrendPoint(
    val date: LocalDate,
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
    val date: LocalDate,
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

