package com.emul8r.bizap.data.repository

import com.emul8r.bizap.data.local.dao.InvoiceDaoV2
import com.emul8r.bizap.data.local.entities.DailyRevenueTrendV2
import com.emul8r.bizap.data.repository.analytics.AnalyticsCalculator
import com.emul8r.bizap.data.repository.analytics.AnalyticsValidator
import com.emul8r.bizap.data.repository.analytics.CalendarUtils
import com.emul8r.bizap.domain.model.gui2.RevenueMetricsV2
import com.emul8r.bizap.domain.revenue.repository.RevenueRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Unified revenue repository — single source of truth for both GUI1 and GUI2.
 *
 * Replaces the former split-brain architecture where [RevenueRepositoryImpl] (legacy, using
 * [com.emul8r.bizap.data.local.InvoiceDao]) and [com.emul8r.bizap.data.repository.gui2.RevenueRepositoryV2]
 * (modern) implemented different calculation logic against
 * the same database. All revenue metrics now flow through one implementation backed
 * by [InvoiceDaoV2], [AnalyticsCalculator], and [AnalyticsValidator].
 *
 * Date windows (MTD, YTD, weekly) are recalculated every minute via [tickerFlow]
 * so the repository stays accurate across day/month/year boundaries without
 * requiring a user action.
 * The device's local timezone is used throughout.
 */
@Singleton
class RevenueRepositoryImpl @Inject constructor(
    private val invoiceDaoV2: InvoiceDaoV2,
    private val calculator: AnalyticsCalculator,
    private val validator: AnalyticsValidator
) : RevenueRepository {

    /**
     * Observe unified revenue metrics for the given business.
     * Automatically reacts to:
     *  - Invoice table changes (Room reactive query re-emission)
     *  - Clock ticks every [REFRESH_INTERVAL_MS] to refresh time-window boundaries
     */
    override fun observeRevenueMetrics(businessId: Long): Flow<Result<RevenueMetricsV2>> =
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

                Timber.d("RevenueRepositoryImpl: metrics update — mtd=$mtd ytd=$ytd weekly=$weekly totalPaid=$totalPaid overdueAmt=$overdueAmt")

                val validation = validator.validateRevenueMetrics(mtd, ytd, weekly)
                if (!validation.isValid) {
                    Timber.w("RevenueRepositoryImpl: validation failed — ${validation.error}")
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
            Timber.e(e, "RevenueRepositoryImpl: error observing metrics for businessId=$businessId")
            emit(Result.failure(e))
        }

    companion object {
        /** How often the ticker refreshes time-window boundaries. */
        const val REFRESH_INTERVAL_MS = 60_000L

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
