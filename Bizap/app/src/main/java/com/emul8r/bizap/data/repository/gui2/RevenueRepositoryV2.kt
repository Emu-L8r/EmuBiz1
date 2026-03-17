package com.emul8r.bizap.data.repository.gui2

import com.emul8r.bizap.data.local.dao.InvoiceDaoV2
import com.emul8r.bizap.data.local.entities.DailyRevenueTrendV2
import com.emul8r.bizap.data.repository.analytics.AnalyticsCalculator
import com.emul8r.bizap.data.repository.analytics.AnalyticsValidator
import com.emul8r.bizap.data.repository.analytics.CalendarUtils
import com.emul8r.bizap.domain.model.gui2.RevenueMetricsV2
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
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
 * Date windows (MTD, YTD, weekly) are recalculated every minute via [tickerFlow]
 * so the repository stays accurate across day/month/year boundaries without
 * requiring a user action (Issue #1, #13 — stale calendar state).
 * The device's local timezone is used throughout (Issue #2).
 */
@Singleton
class RevenueRepositoryV2 @Inject constructor(
    private val invoiceDaoV2: InvoiceDaoV2,
    private val calculator: AnalyticsCalculator,
    private val validator: AnalyticsValidator
) {
    /**
     * Observe unified revenue metrics for the given business.
     * Automatically reacts to:
     *  - Invoice table changes (Room reactive query re-emission)
     *  - Clock ticks every [REFRESH_INTERVAL_MS] to refresh time-window boundaries
     */
    fun observeRevenueMetrics(businessId: Long): Flow<Result<RevenueMetricsV2>> =
        tickerFlow.flatMapLatest { now ->
            val monthStartMs = CalendarUtils.startOfCurrentMonth(now)
            val yearStartMs = CalendarUtils.startOfCurrentYear(now)
            val weekStartMs = now - CalendarUtils.SEVEN_DAYS_MS

            combine(
                invoiceDaoV2.observeMTDRevenue(businessId, monthStartMs, now),
                invoiceDaoV2.observeYTDRevenue(businessId, yearStartMs, now),
                invoiceDaoV2.observeWeeklyRevenue(businessId, weekStartMs, now),
                invoiceDaoV2.observeTotalPaidRevenue(businessId),
                invoiceDaoV2.observeLast30DaysRevenueTrend(businessId),
                invoiceDaoV2.observeOverdueAmount(businessId)
            ) { values ->
                @Suppress("UNCHECKED_CAST")
                val mtd = values[0] as Long
                val ytd = values[1] as Long
                val weekly = values[2] as Long
                val totalPaid = values[3] as Long
                val trend = values[4] as List<DailyRevenueTrendV2>
                val overdueAmt = values[5] as Long

                Timber.d("RevenueRepositoryV2: metrics update — mtd=$mtd ytd=$ytd weekly=$weekly totalPaid=$totalPaid overdueAmt=$overdueAmt")

                val validation = validator.validateRevenueMetrics(mtd, ytd, weekly)
                if (!validation.isValid) {
                    Timber.w("RevenueRepositoryV2: validation failed — ${validation.error}")
                }

                Result.runCatching {
                    calculator.combineRevenueMetrics(
                        businessId = businessId,
                        mtd = mtd,
                        ytd = ytd,
                        weekly = weekly,
                        totalPaid = totalPaid,
                        trend = trend,
                        overdueAmount = overdueAmt
                    )
                }
            }
        }
        .catch { e ->
            Timber.e(e, "RevenueRepositoryV2: error observing metrics for businessId=$businessId")
            emit(Result.failure(e))
        }

    companion object {
        /** How often the ticker refreshes time-window boundaries. */
        const val REFRESH_INTERVAL_MS = 60_000L

        private const val MILLIS_IN_WEEK = 7L * 24 * 60 * 60 * 1000

        /**
         * Flow that emits [System.currentTimeMillis] immediately, then every
         * [REFRESH_INTERVAL_MS], keeping date-window boundaries fresh across
         * midnight/month/year crossings.
         */
        val tickerFlow: Flow<Long> = flow {
            while (true) {
                emit(System.currentTimeMillis())
                delay(REFRESH_INTERVAL_MS)
            }
        }
    }
}
