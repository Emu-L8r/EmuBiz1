package com.emul8r.bizap.data.repository

import com.emul8r.bizap.data.local.InvoiceDao
import com.emul8r.bizap.data.local.InvoiceDao.DailyRevenueTrend
import com.emul8r.bizap.domain.revenue.model.DailyRevenuePoint
import com.emul8r.bizap.domain.revenue.model.RevenueByCurrency
import com.emul8r.bizap.domain.revenue.model.RevenueMetrics
import com.emul8r.bizap.domain.revenue.repository.RevenueRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject

/**
 * Revenue repository that queries directly from the invoices table.
 * This eliminates the dependency on snapshot tables (DailyRevenueSnapshot,
 * InvoiceAnalyticsSnapshot) and ensures data is always current.
 */
class RevenueRepositoryImpl @Inject constructor(
    private val invoiceDao: InvoiceDao
) : RevenueRepository {

    override fun observeRevenueMetrics(businessProfileId: Long): Flow<RevenueMetrics> {
        val amountsFlow = combine(
            invoiceDao.observeMTDRevenue(businessProfileId),
            invoiceDao.observeYTDRevenue(businessProfileId),
            invoiceDao.observeWeeklyRevenue(businessProfileId),
            invoiceDao.observeTotalPaidRevenue(businessProfileId),
            invoiceDao.observeOutstandingAmount(businessProfileId)
        ) { mtd, ytd, weekly, totalPaid, outstanding ->
            RevenueAmounts(mtd, ytd, weekly, totalPaid, outstanding)
        }
        return combine(
            amountsFlow,
            invoiceDao.observeLast30DaysRevenueTrend(businessProfileId)
        ) { amounts, trend ->
            Timber.d("🔍 RevenueRepository: Revenue metrics received:")
            Timber.d("   MTD: ${amounts.mtd} cents ${if (amounts.mtd == 0L) "⚠️ ZERO!" else "✅"}")
            Timber.d("   YTD: ${amounts.ytd} cents")
            Timber.d("   Weekly: ${amounts.weekly} cents")
            Timber.d("   Total Paid: ${amounts.totalPaid} cents")
            Timber.d("   Outstanding: ${amounts.outstanding} cents")
            Timber.d("   Trend points: ${trend.size} days")

            if (amounts.mtd == 0L) {
                Timber.w("⚠️⚠️⚠️ RevenueRepository: MTD is ZERO - check if PAID invoices exist!")
            }

            RevenueMetrics(
                mtdRevenue = amounts.mtd,
                ytdRevenue = amounts.ytd,
                weeklyRevenue = amounts.weekly,
                totalPaidRevenue = amounts.totalPaid,
                outstandingAmount = amounts.outstanding,
                dailyTrend = transformToDailyData(trend),
                topPerformers = calculateByCurrency(trend)
            )
        }
    }

    private data class RevenueAmounts(
        val mtd: Long,
        val ytd: Long,
        val weekly: Long,
        val totalPaid: Long,
        val outstanding: Long
    )

    override suspend fun getRevenueMetrics(businessProfileId: Long): RevenueMetrics {
        Timber.d("RevenueRepository: Fetching metrics for business $businessProfileId")
        val mtd = invoiceDao.observeMTDRevenue(businessProfileId).first()
        val ytd = invoiceDao.observeYTDRevenue(businessProfileId).first()
        val weekly = invoiceDao.observeWeeklyRevenue(businessProfileId).first()
        val totalPaid = invoiceDao.observeTotalPaidRevenue(businessProfileId).first()
        val outstanding = invoiceDao.observeOutstandingAmount(businessProfileId).first()
        val trend = invoiceDao.observeLast30DaysRevenueTrend(businessProfileId).first()
        return RevenueMetrics(
            mtdRevenue = mtd,
            ytdRevenue = ytd,
            weeklyRevenue = weekly,
            totalPaidRevenue = totalPaid,
            outstandingAmount = outstanding,
            dailyTrend = transformToDailyData(trend),
            topPerformers = calculateByCurrency(trend)
        )
    }

    private fun transformToDailyData(trend: List<DailyRevenueTrend>): List<DailyRevenuePoint> {
        return trend
            .groupBy { it.dateString }
            .entries
            .sortedBy { it.key }
            .mapNotNull { (dateString, rows) ->
                try {
                    DailyRevenuePoint(
                        date = LocalDate.parse(dateString),
                        amount = rows.sumOf { it.revenue },
                        invoiceCount = rows.sumOf { it.invoiceCount }
                    )
                } catch (e: Exception) {
                    Timber.w(e, "RevenueRepository: Failed to parse date: $dateString")
                    null
                }
            }
    }

    private fun calculateByCurrency(trend: List<DailyRevenueTrend>): List<RevenueByCurrency> {
        val currencyTotals = trend
            .groupBy { it.currencyCode }
            .mapValues { (_, rows) -> rows.sumOf { it.revenue } }
        val grandTotal = currencyTotals.values.sum()
        return currencyTotals
            .map { (currency, amount) ->
                RevenueByCurrency(
                    currencyCode = currency,
                    totalAmount = amount,
                    percentageOfTotal = if (grandTotal > 0) (amount.toDouble() / grandTotal.toDouble()) * 100 else 0.0
                )
            }
            .sortedByDescending { it.totalAmount }
    }
}
