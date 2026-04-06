package com.emul8r.bizap.data.repository

import com.emul8r.bizap.data.local.dao.InvoiceDaoV2
import com.emul8r.bizap.domain.model.InvoicePeriodData
import com.emul8r.bizap.domain.repository.InvoiceAnalyticsRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [InvoiceAnalyticsRepository] backed by [InvoiceDaoV2].
 *
 * Maps DAO-specific [com.emul8r.bizap.data.local.dao.InvoicePeriodStat] results
 * to the clean domain model [InvoicePeriodData], keeping the DAO type out of the
 * UI layer.
 */
@Singleton
class InvoiceAnalyticsRepositoryImpl @Inject constructor(
    private val invoiceDaoV2: InvoiceDaoV2
) : InvoiceAnalyticsRepository {

    override suspend fun getWeeklyInvoiceTrend(
        businessId: Long,
        months: Int
    ): List<InvoicePeriodData> =
        invoiceDaoV2.getWeeklyInvoiceTrend(businessId, months).map { stat ->
            InvoicePeriodData(
                periodLabel = stat.periodLabel,
                totalCount = stat.totalCount,
                paidCount = stat.paidCount,
                sentCount = stat.sentCount
            )
        }

    override suspend fun getMonthlyInvoiceTrend(
        businessId: Long,
        months: Int
    ): List<InvoicePeriodData> =
        invoiceDaoV2.getMonthlyInvoiceTrend(businessId, months).map { stat ->
            InvoicePeriodData(
                periodLabel = stat.periodLabel,
                totalCount = stat.totalCount,
                paidCount = stat.paidCount,
                sentCount = stat.sentCount
            )
        }
}
