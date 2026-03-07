package com.emul8r.bizap.data.repository.gui2

import com.emul8r.bizap.data.local.dao.InvoiceDaoV2
import com.emul8r.bizap.domain.model.gui2.DailyTrendPointV2
import com.emul8r.bizap.domain.model.gui2.RevenueMetricsV2
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * GUI2 revenue repository.
 * Combines 5 revenue flows from InvoiceDaoV2 into a single unified reactive stream.
 * All data comes directly from the invoices table (Option C — no snapshot dependency).
 */
@Singleton
class RevenueRepositoryV2 @Inject constructor(
    private val invoiceDaoV2: InvoiceDaoV2
) {
    /**
     * Observe unified revenue metrics for the given business.
     * Automatically reacts to any invoice changes.
     */
    fun observeRevenueMetrics(businessId: Long): Flow<RevenueMetricsV2> {
        return combine(
            invoiceDaoV2.observeMTDRevenue(businessId),
            invoiceDaoV2.observeYTDRevenue(businessId),
            invoiceDaoV2.observeWeeklyRevenue(businessId),
            invoiceDaoV2.observeTotalPaidRevenue(businessId),
            invoiceDaoV2.observeLast30DaysRevenueTrend(businessId)
        ) { mtd, ytd, weekly, totalPaid, trend ->
            Timber.d("RevenueRepositoryV2: metrics update — mtd=$mtd ytd=$ytd weekly=$weekly totalPaid=$totalPaid")
            RevenueMetricsV2(
                businessProfileId = businessId,
                mtdRevenue = mtd,
                ytdRevenue = ytd,
                weeklyRevenue = weekly,
                totalPaidRevenue = totalPaid,
                outstandingAmount = 0L,   // populated by PaymentAnalyticsRepositoryV2
                collectedAmount = 0L,     // populated by PaymentAnalyticsRepositoryV2
                dailyTrend = trend.map { point ->
                    DailyTrendPointV2(
                        date = point.dateString,
                        revenueCents = point.revenue,
                        invoiceCount = point.invoiceCount
                    )
                }
            )
        }
    }
}
