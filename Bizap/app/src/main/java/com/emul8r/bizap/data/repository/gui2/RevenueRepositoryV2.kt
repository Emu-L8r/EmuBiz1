package com.emul8r.bizap.data.repository.gui2

import com.emul8r.bizap.data.local.dao.InvoiceDaoV2
import com.emul8r.bizap.data.repository.analytics.AnalyticsCalculator
import com.emul8r.bizap.data.repository.analytics.AnalyticsValidator
import com.emul8r.bizap.domain.model.gui2.RevenueMetricsV2
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * GUI2 revenue repository.
 * Combines 5 revenue flows from InvoiceDaoV2 into a single unified reactive stream.
 * All data comes directly from the invoices table (Option C — no snapshot dependency).
 *
 * Calculation logic is delegated to [AnalyticsCalculator] so that formulas are
 * defined in exactly one place. [AnalyticsValidator] guards against data corruption
 * before metrics reach the UI.
 *
 * Date windows (MTD, YTD, weekly) are calculated in app code using [Calendar.getInstance()]
 * so they honour the device's local timezone instead of relying on SQLite's UTC date().
 */
@Singleton
class RevenueRepositoryV2 @Inject constructor(
    private val invoiceDaoV2: InvoiceDaoV2,
    private val calculator: AnalyticsCalculator,
    private val validator: AnalyticsValidator
) {
    /**
     * Observe unified revenue metrics for the given business.
     * Automatically reacts to any invoice changes.
     *
     * Date window boundaries are recomputed on each collection to avoid stale
     * calendar state across month/year boundaries (Issues #1, #13).
     */
    fun observeRevenueMetrics(businessId: Long): Flow<RevenueMetricsV2> {
        val now = System.currentTimeMillis()
        val monthStartMs = startOfCurrentMonth(now)
        val yearStartMs = startOfCurrentYear(now)
        val weekStartMs = now - 7L * 24 * 60 * 60 * 1000

        return combine(
            invoiceDaoV2.observeMTDRevenue(businessId, monthStartMs, now),
            invoiceDaoV2.observeYTDRevenue(businessId, yearStartMs, now),
            invoiceDaoV2.observeWeeklyRevenue(businessId, weekStartMs, now),
            invoiceDaoV2.observeTotalPaidRevenue(businessId),
            invoiceDaoV2.observeLast30DaysRevenueTrend(businessId)
        ) { mtd, ytd, weekly, totalPaid, trend ->
            Timber.d("RevenueRepositoryV2: metrics update — mtd=$mtd ytd=$ytd weekly=$weekly totalPaid=$totalPaid")

            val validation = validator.validateRevenueMetrics(mtd, ytd, weekly)
            if (!validation.isValid) {
                Timber.w("RevenueRepositoryV2: validation failed — ${validation.error}")
            }

            calculator.combineRevenueMetrics(
                businessId = businessId,
                mtd = mtd,
                ytd = ytd,
                weekly = weekly,
                totalPaid = totalPaid,
                trend = trend
            )
        }
    }

    /** Returns epoch-millis for midnight on the 1st of the current local month. */
    private fun startOfCurrentMonth(nowMs: Long): Long = Calendar.getInstance().apply {
        timeInMillis = nowMs
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    /** Returns epoch-millis for midnight on January 1st of the current local year. */
    private fun startOfCurrentYear(nowMs: Long): Long = Calendar.getInstance().apply {
        timeInMillis = nowMs
        set(Calendar.MONTH, Calendar.JANUARY)
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}
