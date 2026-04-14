package com.emul8r.bizap.data.repository.analytics

import com.emul8r.bizap.domain.model.InvoiceStatus
import com.emul8r.bizap.domain.model.analytics.AnalyticsPeriod
import com.emul8r.bizap.domain.model.analytics.ForthnightlyMetrics
import com.emul8r.bizap.domain.model.analytics.PeriodAnalytics
import com.emul8r.bizap.domain.model.analytics.QuarterlyMetrics
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Aggregates analytics data by period (daily, fortnightly, quarterly, yearly).
 *
 * Builds on existing analytics infrastructure (DailyRevenueTrendV2, etc.).
 * Calculates metrics for any time window based on fresh invoice data.
 *
 * **Design:**
 * - Period-agnostic (works with any date range)
 * - Efficient (minimal calculations, reuses existing queries)
 * - Fresh-data ready (no seasonal/YoY logic)
 * - Testable (pure functions where possible)
 *
 * **Usage:**
 * ```kotlin
 * // Get fortnightly metrics for last 14 days
 * val fortnightly = aggregator.calculateForthnightly(
 *     invoices = allInvoices,
 *     endDate = LocalDate.now()
 * )
 *
 * // Get quarterly metrics for last 90 days
 * val quarterly = aggregator.calculateQuarterly(
 *     invoices = allInvoices,
 *     endDate = LocalDate.now()
 * )
 * ```
 */
@Singleton
class PeriodAggregator @Inject constructor() {

    /**
     * Calculate fortnightly metrics (last 14 days).
     *
     * @param invoices All invoices for the business
     * @param endDate Last day to include (default: today)
     * @return ForthnightlyMetrics for the 14-day period
     */
    fun calculateForthnightly(
        invoices: List<InvoiceDataPoint>,
        endDate: LocalDate = LocalDate.now()
    ): ForthnightlyMetrics {
        val startDate = endDate.minusDays(14)

        val periodInvoices = invoices.filter { invoice ->
            invoice.dateCreated.isAfter(startDate.atStartOfDay()) &&
            invoice.dateCreated.isBefore(endDate.atStartOfDay().plusDays(1))
        }

        Timber.d("📊 Fortnightly: ${periodInvoices.size} invoices from $startDate to $endDate")

        return aggregateToForthnightly(periodInvoices)
    }

    /**
     * Calculate quarterly metrics (last 90 days).
     *
     * @param invoices All invoices for the business
     * @param customerName Map of customerId to customerName
     * @param endDate Last day to include (default: today)
     * @return QuarterlyMetrics for the 90-day period
     */
    fun calculateQuarterly(
        invoices: List<InvoiceDataPoint>,
        customerName: Map<Long, String>,
        endDate: LocalDate = LocalDate.now()
    ): QuarterlyMetrics {
        val startDate = endDate.minusDays(90)

        val periodInvoices = invoices.filter { invoice ->
            invoice.dateCreated.isAfter(startDate.atStartOfDay()) &&
            invoice.dateCreated.isBefore(endDate.atStartOfDay().plusDays(1))
        }

        Timber.d("📊 Quarterly: ${periodInvoices.size} invoices from $startDate to $endDate")

        return aggregateToQuarterly(periodInvoices, customerName)
    }

    /**
     * Calculate period analytics for any date range.
     *
     * @param invoices All invoices for the business
     * @param period The period type (FORTNIGHTLY, QUARTERLY, etc.)
     * @param endDate Last day to include (default: today)
     * @return PeriodAnalytics with full metrics
     */
    fun calculatePeriod(
        invoices: List<InvoiceDataPoint>,
        period: AnalyticsPeriod,
        endDate: LocalDate = LocalDate.now()
    ): PeriodAnalytics {
        val startDate = when (period) {
            AnalyticsPeriod.DAILY -> endDate
            AnalyticsPeriod.WEEKLY -> endDate.minusDays(7)
            AnalyticsPeriod.FORTNIGHTLY -> endDate.minusDays(14)
            AnalyticsPeriod.MONTHLY -> endDate.minusDays(30)
            AnalyticsPeriod.QUARTERLY -> endDate.minusDays(90)
            AnalyticsPeriod.YEARLY -> endDate.minusDays(365)
        }

        val periodInvoices = invoices.filter { invoice ->
            invoice.dateCreated.isAfter(startDate.atStartOfDay()) &&
            invoice.dateCreated.isBefore(endDate.atStartOfDay().plusDays(1))
        }

        Timber.d("📊 $period: ${periodInvoices.size} invoices from $startDate to $endDate")

        return aggregateToPeriodAnalytics(
            invoices = periodInvoices,
            period = period,
            startDate = startDate,
            endDate = endDate
        )
    }

    /**
     * Aggregate invoices to fortnightly metrics.
     */
    private fun aggregateToForthnightly(invoices: List<InvoiceDataPoint>): ForthnightlyMetrics {
        val totalRevenue = invoices.sumOf { it.amountCents }
        val paidInvoices = invoices.filter { it.status == InvoiceStatus.PAID }
        val paidCount = paidInvoices.size
        val paidPercentage = if (invoices.isNotEmpty()) (paidCount * 100f) / invoices.size else 0f
        val averageInvoiceSize = if (invoices.isNotEmpty()) totalRevenue / invoices.size else 0L

        return ForthnightlyMetrics(
            totalRevenue = totalRevenue,
            invoiceCount = invoices.size,
            paidCount = paidCount,
            averageInvoiceSize = averageInvoiceSize,
            paidPercentage = paidPercentage
        )
    }

    /**
     * Aggregate invoices to quarterly metrics.
     */
    private fun aggregateToQuarterly(
        invoices: List<InvoiceDataPoint>,
        customerName: Map<Long, String>
    ): QuarterlyMetrics {
        val totalRevenue = invoices.sumOf { it.amountCents }
        val paidAmount = invoices.filter { it.status == InvoiceStatus.PAID }
            .sumOf { it.amountCents }
        val outstandingAmount = invoices.filter { it.status !in listOf(InvoiceStatus.PAID, InvoiceStatus.CANCELLED) }
            .sumOf { it.amountCents }

        val paidInvoices = invoices.filter { it.status == InvoiceStatus.PAID }
        val averageDaysToPayment = if (paidInvoices.isNotEmpty()) {
            paidInvoices.map { it.daysToPay ?: 0 }.average().toInt()
        } else 0

        // Top customer
        val topCustomer = invoices
            .groupBy { it.customerId }
            .maxByOrNull { (_, invoiceList) -> invoiceList.sumOf { it.amountCents } }
            ?.let { (customerId, invoiceList) ->
                customerName[customerId] to invoiceList.sumOf { it.amountCents }
            }

        return QuarterlyMetrics(
            totalRevenue = totalRevenue,
            paidAmount = paidAmount,
            outstandingAmount = outstandingAmount,
            invoiceCount = invoices.size,
            paidCount = paidInvoices.size,
            averageDaysToPayment = averageDaysToPayment,
            topCustomer = topCustomer?.first ?: "N/A",
            topCustomerRevenue = topCustomer?.second ?: 0L
        )
    }

    /**
     * Aggregate invoices to full PeriodAnalytics.
     */
    private fun aggregateToPeriodAnalytics(
        invoices: List<InvoiceDataPoint>,
        period: AnalyticsPeriod,
        startDate: LocalDate,
        endDate: LocalDate
    ): PeriodAnalytics {
        val totalRevenue = invoices.sumOf { it.amountCents }
        val paidInvoices = invoices.filter { it.status == InvoiceStatus.PAID }
        val paidCount = paidInvoices.size
        val paidAmount = paidInvoices.sumOf { it.amountCents }
        val outstandingAmount = invoices.filter { it.status !in listOf(InvoiceStatus.PAID, InvoiceStatus.CANCELLED) }
            .sumOf { it.amountCents }
        val paidPercentage = if (invoices.isNotEmpty()) (paidCount * 100f) / invoices.size else 0f
        val onTimePaymentRate = if (paidInvoices.isNotEmpty()) {
            paidInvoices.filter { (it.daysToPay ?: 0) <= 7 }.size * 100f / paidInvoices.size
        } else 0f
        val averageInvoiceSize = if (invoices.isNotEmpty()) totalRevenue / invoices.size else 0L
        val daysSinceLastInvoice = if (invoices.isNotEmpty()) {
            LocalDate.now().until(invoices.maxByOrNull { it.dateCreated }?.dateCreated?.toLocalDate() ?: LocalDate.now()).days.coerceAtLeast(0)
        } else 999

        return PeriodAnalytics(
            period = period,
            startDate = startDate,
            endDate = endDate,
            totalRevenue = totalRevenue,
            paidAmount = paidAmount,
            outstandingAmount = outstandingAmount,
            invoiceCount = invoices.size,
            paidCount = paidCount,
            averageInvoiceSize = averageInvoiceSize,
            paidPercentage = paidPercentage,
            onTimePaymentRate = onTimePaymentRate,
            daysSinceLastInvoice = daysSinceLastInvoice
        )
    }
}

/**
 * Data class representing a single invoice for aggregation.
 * Minimal representation needed for period calculations.
 *
 * @param invoiceId Unique invoice identifier
 * @param customerId Customer who was invoiced
 * @param amountCents Invoice total in cents
 * @param status Invoice status (DRAFT, SENT, PAID, OVERDUE, etc.)
 * @param dateCreated When invoice was created
 * @param daysToPay Days from creation to payment (null if not paid)
 */
data class InvoiceDataPoint(
    val invoiceId: Long,
    val customerId: Long,
    val amountCents: Long,
    val status: InvoiceStatus,
    val dateCreated: java.time.LocalDateTime,
    val daysToPay: Int? = null
)

