package com.emul8r.bizap.data.repository.gui2

import com.emul8r.bizap.data.local.dao.InvoiceDaoV2
import com.emul8r.bizap.data.repository.analytics.AnalyticsCalculator
import com.emul8r.bizap.domain.model.gui2.RiskMetricsV2
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * GUI2 risk analytics repository.
 * Classifies invoices into risk tiers (high-risk, at-risk, healthy).
 *
 * Calculation logic is delegated to [AnalyticsCalculator] so that formulas are
 * defined in exactly one place.
 */
@Singleton
class RiskAnalyticsRepositoryV2 @Inject constructor(
    private val invoiceDaoV2: InvoiceDaoV2,
    private val calculator: AnalyticsCalculator
) {
    /**
     * Observe risk metrics for the given business.
     */
    fun observeRiskMetrics(businessId: Long): Flow<RiskMetricsV2> {
        return combine(
            invoiceDaoV2.observeHighRiskInvoiceCount(businessId),
            invoiceDaoV2.observeAtRiskInvoiceCount(businessId),
            invoiceDaoV2.observeHealthyInvoiceCount(businessId),
            invoiceDaoV2.observeOverdueCount(businessId),
            invoiceDaoV2.observeOutstandingAmount(businessId)
        ) { highRisk, atRisk, healthy, overdue, outstanding ->
            Timber.d("RiskAnalyticsRepositoryV2: highRisk=$highRisk atRisk=$atRisk healthy=$healthy overdue=$overdue")
            calculator.combineRiskMetrics(
                businessId = businessId,
                highRisk = highRisk,
                atRisk = atRisk,
                healthy = healthy,
                overdue = overdue,
                outstanding = outstanding
            )
        }
    }
}
