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
        return combine(
            invoiceDao.observeMTDRevenue(businessProfileId),
            invoiceDao.observeYTDRevenue(businessProfileId),
            invoiceDao.observeWeeklyRevenue(businessProfileId),
            invoiceDao.observeTotalPaidRevenue(businessProfileId),
            invoiceDao.observeLast30DaysRevenueTrend(businessProfileId)
        ) { mtd, ytd, weekly, totalPaid, trend ->
            Timber.d("🔍 RevenueRepository: Revenue metrics received:")
            Timber.d("   MTD: $mtd cents (${mtd/100}.${mtd%100} ${if(mtd==0L) "⚠️ ZERO!" else "✅"})")
            Timber.d("   YTD: $ytd cents")
            Timber.d("   Weekly: $weekly cents")
            Timber.d("   Total Paid: $totalPaid cents")
            Timber.d("   Trend points: ${trend.size} days")

            if (mtd == 0L) {
                Timber.w("⚠️⚠️⚠️ RevenueRepository: MTD is ZERO - check if PAID invoices exist!")
            }

            RevenueMetrics(
                mtdRevenue = mtd,
                ytdRevenue = ytd,
                weeklyRevenue = weekly,
                totalPaidRevenue = totalPaid,
                dailyTrend = transformToDailyData(trend),
                topPerformers = calculateByCurrency(trend)
            )
        }
    }

    override suspend fun getRevenueMetrics(businessProfileId: Long): RevenueMetrics {
        Timber.d("RevenueRepository: Fetching metrics for business $businessProfileId")
        val mtd = invoiceDao.observeMTDRevenue(businessProfileId).first()
        val ytd = invoiceDao.observeYTDRevenue(businessProfileId).first()
        val weekly = invoiceDao.observeWeeklyRevenue(businessProfileId).first()
        val totalPaid = invoiceDao.observeTotalPaidRevenue(businessProfileId).first()
        val trend = invoiceDao.observeLast30DaysRevenueTrend(businessProfileId).first()
        return RevenueMetrics(
            mtdRevenue = mtd,
            ytdRevenue = ytd,
            weeklyRevenue = weekly,
            totalPaidRevenue = totalPaid,
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
