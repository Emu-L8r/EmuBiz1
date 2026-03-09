package com.emul8r.bizap.data.repository

import com.emul8r.bizap.data.local.dao.InvoiceDaoV2
import com.emul8r.bizap.data.repository.analytics.AnalyticsCalculator
import com.emul8r.bizap.data.repository.analytics.AnalyticsValidator
import com.emul8r.bizap.domain.model.gui2.PaymentMetricsV2
import com.emul8r.bizap.domain.model.gui2.RevenueMetricsV2
import com.emul8r.bizap.domain.model.gui2.RiskMetricsV2
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Unified Accounting Repository — single source of truth for all financial metrics.
 *
 * This repository replaces all snapshot-based analytics queries. Every financial
 * figure is computed on-the-fly from the `invoices` table via [InvoiceDaoV2],
 * ensuring GUI1 and GUI2 always display identical numbers.
 *
 * ## Math Rules (Accrual Basis)
 * ```
 * Outstanding = SUM(totalAmount - amountPaid)
 *               WHERE status IN [SENT, PARTIALLY_PAID, OVERDUE]
 *
 * Collected   = SUM(amountPaid)
 *               WHERE status IN [PAID, PARTIALLY_PAID]
 *
 * CollectionRate = (Collected / (Collected + Outstanding)) × 100
 *
 * MTD Revenue = SUM(amountPaid)
 *               WHERE invoice.date >= start_of_month
 *                 AND status IN [PAID, PARTIALLY_PAID]
 * ```
 *
 * All monetary values are in **cents** (Long). UI layer is responsible for
 * converting to dollars via [com.emul8r.bizap.utils.CentsFormatter].
 *
 * @see com.emul8r.bizap.data.repository.SnapshotCachePolicy
 * @see com.emul8r.bizap.data.repository.AnalyticsRepositoryBridge
 */
@Singleton
class AccountingRepository @Inject constructor(
    private val invoiceDaoV2: InvoiceDaoV2,
    private val calculator: AnalyticsCalculator,
    private val validator: AnalyticsValidator
) {

    // ── Revenue ──────────────────────────────────────────────────────────────

    /**
     * Observe unified revenue metrics for [businessId].
     * Reacts to any change in the invoices table automatically.
     */
    fun observeRevenueMetrics(businessId: Long): Flow<RevenueMetricsV2> =
        combine(
            invoiceDaoV2.observeMTDRevenue(businessId),
            invoiceDaoV2.observeYTDRevenue(businessId),
            invoiceDaoV2.observeWeeklyRevenue(businessId),
            invoiceDaoV2.observeTotalPaidRevenue(businessId),
            invoiceDaoV2.observeLast30DaysRevenueTrend(businessId)
        ) { mtd, ytd, weekly, totalPaid, trend ->
            val validation = validator.validateRevenueMetrics(mtd, ytd, weekly)
            if (!validation.isValid) {
                Timber.w("AccountingRepository.revenue: validation failed — ${validation.error}")
            }
            Timber.d("AccountingRepository: revenue mtd=$mtd ytd=$ytd weekly=$weekly total=$totalPaid")
            calculator.combineRevenueMetrics(
                businessId = businessId,
                mtd = mtd,
                ytd = ytd,
                weekly = weekly,
                totalPaid = totalPaid,
                trend = trend
            )
        }

    // ── Payment / Outstanding ─────────────────────────────────────────────────

    /**
     * Observe payment analytics (outstanding, collected, collection rate, status counts).
     * Formula: collectionRate = collected / (collected + outstanding) × 100
     */
    fun observePaymentMetrics(businessId: Long): Flow<PaymentMetricsV2> =
        combine(
            invoiceDaoV2.observeOutstandingAmount(businessId),
            invoiceDaoV2.observeCollectedAmount(businessId),
            invoiceDaoV2.observeInvoiceCountByStatus(businessId),
            invoiceDaoV2.observeOverdueCount(businessId),
            invoiceDaoV2.observeAverageDaysToPayment(businessId)
        ) { outstanding, collected, statusCounts, overdueCount, avgDays ->
            val totalBilled = outstanding + collected
            val validation = validator.validatePaymentMetrics(outstanding, collected, totalBilled)
            if (!validation.isValid) {
                Timber.w("AccountingRepository.payment: validation failed — ${validation.error}")
            }
            val metrics = calculator.combinePaymentMetrics(
                businessId = businessId,
                outstanding = outstanding,
                collected = collected,
                statusCounts = statusCounts,
                overdueCount = overdueCount,
                avgDays = avgDays
            )
            Timber.d(
                "AccountingRepository: payment outstanding=$outstanding " +
                    "collected=$collected rate=${"%.1f".format(metrics.collectionRate)}%"
            )
            metrics
        }

    // ── Risk ─────────────────────────────────────────────────────────────────

    /**
     * Observe risk classification metrics (high-risk / at-risk / healthy invoice counts).
     * Risk tiers are based on how many days past due each overdue invoice is.
     */
    fun observeRiskMetrics(businessId: Long): Flow<RiskMetricsV2> =
        combine(
            invoiceDaoV2.observeHighRiskInvoiceCount(businessId),
            invoiceDaoV2.observeAtRiskInvoiceCount(businessId),
            invoiceDaoV2.observeHealthyInvoiceCount(businessId),
            invoiceDaoV2.observeOverdueCount(businessId),
            invoiceDaoV2.observeOutstandingAmount(businessId)
        ) { highRisk, atRisk, healthy, overdue, outstanding ->
            Timber.d(
                "AccountingRepository: risk highRisk=$highRisk atRisk=$atRisk " +
                    "healthy=$healthy overdue=$overdue outstanding=$outstanding"
            )
            calculator.combineRiskMetrics(
                businessId = businessId,
                highRisk = highRisk,
                atRisk = atRisk,
                healthy = healthy,
                overdue = overdue,
                outstanding = outstanding
            )
        }

    // ── Convenience ──────────────────────────────────────────────────────────

    /**
     * Observe the outstanding balance (totalAmount - amountPaid) for invoices
     * with status SENT, PARTIALLY_PAID, or OVERDUE.
     */
    fun observeOutstandingAmount(businessId: Long): Flow<Long> =
        invoiceDaoV2.observeOutstandingAmount(businessId)

    /**
     * Observe total collected amount (amountPaid) for invoices with status
     * PAID or PARTIALLY_PAID.
     */
    fun observeCollectedAmount(businessId: Long): Flow<Long> =
        invoiceDaoV2.observeCollectedAmount(businessId)

    /**
     * Observe month-to-date revenue (amountPaid for PAID/PARTIALLY_PAID invoices
     * dated within the current calendar month).
     */
    fun observeMTDRevenue(businessId: Long): Flow<Long> =
        invoiceDaoV2.observeMTDRevenue(businessId)
}
