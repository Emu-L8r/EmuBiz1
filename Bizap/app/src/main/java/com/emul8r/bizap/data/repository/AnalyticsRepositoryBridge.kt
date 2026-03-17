package com.emul8r.bizap.data.repository

import com.emul8r.bizap.data.repository.gui2.PaymentAnalyticsRepositoryV2
import com.emul8r.bizap.data.repository.gui2.RiskAnalyticsRepositoryV2
import com.emul8r.bizap.data.repository.gui2.RevenueRepositoryV2
import com.emul8r.bizap.domain.model.gui2.PaymentMetricsV2
import com.emul8r.bizap.domain.model.gui2.RiskMetricsV2
import com.emul8r.bizap.domain.model.gui2.RevenueMetricsV2
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Bridge that unifies GUI1 and GUI2 analytics under a single data source.
 *
 * Both GUIs share the same underlying V2 repositories, which query directly
 * from the `invoices` table (never from stale snapshots). This guarantees
 * identical numbers regardless of which screen the user is viewing.
 *
 * Architecture summary:
 * ```
 * GUI1 screens ─┐
 *               ├─▶ AnalyticsRepositoryBridge ─▶ V2 Repositories ─▶ InvoiceDaoV2 ─▶ invoices table
 * GUI2 screens ─┘
 * ```
 *
 * Snapshots are only written as a write-through cache (see [SnapshotCachePolicy]).
 * They are never read for financial calculations.
 */
@Singleton
class AnalyticsRepositoryBridge @Inject constructor(
    private val revenueRepositoryV2: RevenueRepositoryV2,
    private val paymentAnalyticsRepositoryV2: PaymentAnalyticsRepositoryV2,
    private val riskAnalyticsRepositoryV2: RiskAnalyticsRepositoryV2
) {
    /**
     * Observe revenue metrics from the single source of truth (invoices table).
     * Replaces any snapshot-based revenue query for both GUI1 and GUI2.
     */
    fun observeRevenueMetrics(businessId: Long): Flow<Result<RevenueMetricsV2>> {
        Timber.d("AnalyticsRepositoryBridge: observeRevenueMetrics for businessId=$businessId")
        return revenueRepositoryV2.observeRevenueMetrics(businessId)
    }

    /**
     * Observe payment analytics metrics from the single source of truth.
     * Replaces both GUI1 PaymentAnalyticsRepositoryImpl (snapshot-based) and
     * GUI2 PaymentAnalyticsRepositoryV2 with the same V2 implementation.
     */
    fun observePaymentMetrics(businessId: Long): Flow<Result<PaymentMetricsV2>> {
        Timber.d("AnalyticsRepositoryBridge: observePaymentMetrics for businessId=$businessId")
        return paymentAnalyticsRepositoryV2.observePaymentMetrics(businessId)
    }

    /**
     * Observe risk analytics metrics from the single source of truth.
     * Replaces GUI1 IdentifyRiskInvoicesUseCase (snapshot-based) with V2 queries.
     */
    fun observeRiskMetrics(businessId: Long): Flow<Result<RiskMetricsV2>> {
        Timber.d("AnalyticsRepositoryBridge: observeRiskMetrics for businessId=$businessId")
        return riskAnalyticsRepositoryV2.observeRiskMetrics(businessId)
    }
}
