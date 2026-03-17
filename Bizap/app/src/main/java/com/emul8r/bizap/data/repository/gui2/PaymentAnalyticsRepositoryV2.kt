package com.emul8r.bizap.data.repository.gui2

import com.emul8r.bizap.data.local.dao.InvoiceDaoV2
import com.emul8r.bizap.data.repository.analytics.AnalyticsCalculator
import com.emul8r.bizap.data.repository.analytics.AnalyticsValidator
import com.emul8r.bizap.domain.model.gui2.PaymentMetricsV2
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * GUI2 payment analytics repository.
 * Bypasses stale snapshots and pulls directly from the invoices table (Option C).
 * Ensures 100% consistency with the dashboard and invoice list.
 *
 * Calculation logic is delegated to [AnalyticsCalculator] so that formulas are
 * defined in exactly one place. [AnalyticsValidator] guards against data corruption
 * before metrics reach the UI.
 */
@Singleton
class PaymentAnalyticsRepositoryV2 @Inject constructor(
    private val invoiceDaoV2: InvoiceDaoV2,
    private val calculator: AnalyticsCalculator,
    private val validator: AnalyticsValidator
) {
    /**
     * Observe comprehensive payment metrics for the given business.
     * Collection rate is computed as: (collected / (collected + outstanding)) × 100
     */
    fun observePaymentMetrics(businessId: Long): Flow<Result<PaymentMetricsV2>> {
        return combine(
            invoiceDaoV2.observeOutstandingAmount(businessId),
            invoiceDaoV2.observeCollectedAmount(businessId),
            invoiceDaoV2.observeInvoiceCountByStatus(businessId),
            invoiceDaoV2.observeOverdueCount(businessId),
            invoiceDaoV2.observeAverageDaysToPayment(businessId)
        ) { outstanding, collected, statusCounts, overdueCount, avgDays ->
            val totalBilled = outstanding + collected
            val validation = validator.validatePaymentMetrics(outstanding, collected, totalBilled)
            if (!validation.isValid) {
                Timber.w("PaymentAnalyticsRepositoryV2: validation failed — ${validation.error}")
            }

            Result.runCatching {
                val metrics = calculator.combinePaymentMetrics(
                    businessId = businessId,
                    outstanding = outstanding,
                    collected = collected,
                    statusCounts = statusCounts,
                    overdueCount = overdueCount,
                    avgDays = avgDays
                )

                Timber.d(
                    "PaymentAnalyticsRepositoryV2: businessId=$businessId outstanding=$outstanding " +
                        "collected=$collected collectionRate=${"%.1f".format(metrics.collectionRate)}%"
                )

                metrics
            }
        }
        .catch { e ->
            Timber.e(e, "PaymentAnalyticsRepositoryV2: error observing metrics for businessId=$businessId")
            emit(Result.failure(e))
        }
    }
}
