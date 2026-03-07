package com.emul8r.bizap.data.repository.gui2

import com.emul8r.bizap.data.local.dao.InvoiceDaoV2
import com.emul8r.bizap.domain.model.gui2.PaymentMetricsV2
import com.emul8r.bizap.domain.model.gui2.StatusBreakdownV2
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * GUI2 payment analytics repository.
 * Combines outstanding/collected/status queries from InvoiceDaoV2.
 */
@Singleton
class PaymentAnalyticsRepositoryV2 @Inject constructor(
    private val invoiceDaoV2: InvoiceDaoV2
) {
    /**
     * Observe comprehensive payment metrics for the given business.
     */
    fun observePaymentMetrics(businessId: Long): Flow<PaymentMetricsV2> {
        return combine(
            invoiceDaoV2.observeOutstandingAmount(businessId),
            invoiceDaoV2.observeCollectedAmount(businessId),
            invoiceDaoV2.observeInvoiceCountByStatus(businessId),
            invoiceDaoV2.observeOverdueCount(businessId),
            invoiceDaoV2.observeAverageDaysToPayment(businessId)
        ) { outstanding, collected, statusCounts, overdueCount, avgDays ->
            val countMap = statusCounts.associate { it.status to it.count }
            Timber.d("PaymentAnalyticsRepositoryV2: outstanding=$outstanding collected=$collected overdueCount=$overdueCount")
            PaymentMetricsV2(
                businessProfileId = businessId,
                totalInvoices = statusCounts.sumOf { it.count },
                paidCount = countMap["PAID"] ?: 0,
                sentCount = countMap["SENT"] ?: 0,
                overdueCount = overdueCount,
                partiallyPaidCount = countMap["PARTIALLY_PAID"] ?: 0,
                draftCount = countMap["DRAFT"] ?: 0,
                outstandingAmount = outstanding,
                collectedAmount = collected,
                averageDaysToPayment = avgDays,
                statusBreakdown = statusCounts.map { StatusBreakdownV2(it.status, it.count) }
            )
        }
    }
}
