package com.emul8r.bizap.data.repository

import com.emul8r.bizap.data.local.dao.AnalyticsDao
import com.emul8r.bizap.data.local.entities.DailyRevenueSnapshot
import com.emul8r.bizap.data.util.DataAccessException
import com.emul8r.bizap.domain.revenue.model.DailyRevenuePoint
import com.emul8r.bizap.domain.revenue.model.RevenueByCurrency
import com.emul8r.bizap.domain.revenue.model.RevenueMetrics
import com.emul8r.bizap.domain.revenue.repository.RevenueRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import timber.log.Timber
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

class RevenueRepositoryImpl @Inject constructor(
    private val analyticsDao: AnalyticsDao
) : RevenueRepository {

    override suspend fun getRevenueMetrics(businessProfileId: Long): RevenueMetrics {
        return withContext(Dispatchers.IO) {
            try {
                Timber.d("RevenueRepository: Fetching analytics for business $businessProfileId")

                val dailySnapshots = analyticsDao.getLast30DaysRevenue(businessProfileId)

                if (dailySnapshots.isEmpty()) {
                    Timber.w("RevenueRepository: No data available")
                    return@withContext RevenueMetrics(
                        mtdRevenue = 0.0,
                        ytdRevenue = 0.0,
                        weeklyRevenue = 0.0,
                        dailyTrend = emptyList(),
                        topPerformers = emptyList()
                    )
                }

                Timber.d("RevenueRepository: Retrieved ${dailySnapshots.size} snapshots")

                val mtd = calculateMTD(dailySnapshots)
                val ytd = calculateYTD(dailySnapshots)
                val weekly = calculateWeekly(dailySnapshots)
                val dailyTrend = transformToDailyData(dailySnapshots)
                val currencyBreakdown = calculateByCurrency(dailySnapshots)

                RevenueMetrics(
                    mtdRevenue = mtd,
                    ytdRevenue = ytd,
                    weeklyRevenue = weekly,
                    dailyTrend = dailyTrend,
                    topPerformers = currencyBreakdown
                )
            } catch (e: Exception) {
                Timber.e(e, "RevenueRepository: Error fetching metrics")
                throw DataAccessException("Failed to get revenue metrics", e)
            }
        }
    }

    private fun calculateMTD(snapshots: List<DailyRevenueSnapshot>): Double {
        val currentMonth = YearMonth.now()
        val mtd = snapshots
            .filter { snapshot ->
                try {
                    val snapshotDate = LocalDate.parse(snapshot.dateString)
                    YearMonth.from(snapshotDate) == currentMonth
                } catch (e: Exception) {
                    Timber.w(e, "Invalid date format: ${snapshot.dateString}")
                    false
                }
            }
            .sumOf { it.totalRevenue }

        Timber.d("RevenueRepository: MTD = $mtd")
        return mtd
    }

    private fun calculateYTD(snapshots: List<DailyRevenueSnapshot>): Double {
        val currentYear = LocalDate.now().year
        val ytd = snapshots
            .filter { snapshot ->
                try {
                    val snapshotDate = LocalDate.parse(snapshot.dateString)
                    snapshotDate.year == currentYear
                } catch (e: Exception) {
                    Timber.w(e, "Invalid date format: ${snapshot.dateString}")
                    false
                }
            }
            .sumOf { it.totalRevenue }

        Timber.d("RevenueRepository: YTD = $ytd")
        return ytd
    }

    private fun calculateWeekly(snapshots: List<DailyRevenueSnapshot>): Double {
        val sevenDaysAgo = LocalDate.now().minusDays(7)
        val weekly = snapshots
            .filter { snapshot ->
                try {
                    val snapshotDate = LocalDate.parse(snapshot.dateString)
                    snapshotDate >= sevenDaysAgo
                } catch (e: Exception) {
                    Timber.w(e, "Invalid date format: ${snapshot.dateString}")
                    false
                }
            }
            .sumOf { it.totalRevenue }

        Timber.d("RevenueRepository: Weekly = $weekly")
        return weekly
    }

    private fun transformToDailyData(
        snapshots: List<DailyRevenueSnapshot>
    ): List<DailyRevenuePoint> {
        return snapshots
            .sortedBy { it.dateString }
            .mapNotNull { snapshot ->
                try {
                    DailyRevenuePoint(
                        date = LocalDate.parse(snapshot.dateString),
                        amount = snapshot.totalRevenue,
                        invoiceCount = snapshot.invoiceCount
                    )
                } catch (e: Exception) {
                    Timber.w(e, "Failed to parse snapshot: ${snapshot.dateString}")
                    null
                }
            }
    }

    private fun calculateByCurrency(
        snapshots: List<DailyRevenueSnapshot>
    ): List<RevenueByCurrency> {
        try {
            val currencyTotals = mutableMapOf<String, Double>()
            var grandTotal = 0.0

            snapshots.forEach { snapshot ->
                try {
                    val jsonObject = JSONObject(snapshot.currencyBreakdown)
                    jsonObject.keys().forEach { currency ->
                        val amount = jsonObject.getDouble(currency)
                        currencyTotals[currency] = currencyTotals.getOrDefault(currency, 0.0) + amount
                        grandTotal += amount
                    }
                } catch (e: Exception) {
                    Timber.w(e, "Failed to parse currency breakdown: ${snapshot.currencyBreakdown}")
                }
            }

            return currencyTotals.map { (currency, amount) ->
                RevenueByCurrency(
                    currencyCode = currency,
                    totalAmount = amount,
                    percentageOfTotal = if (grandTotal > 0) (amount / grandTotal) * 100 else 0.0
                )
            }.sortedByDescending { it.totalAmount }
        } catch (e: Exception) {
            Timber.e(e, "Error calculating currency breakdown")
            return emptyList()
        }
    }
}
