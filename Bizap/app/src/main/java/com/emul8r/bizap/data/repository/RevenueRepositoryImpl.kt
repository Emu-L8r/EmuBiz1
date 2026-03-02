package com.emul8r.bizap.data.repository

import com.emul8r.bizap.data.local.InvoiceDao
import com.emul8r.bizap.data.local.dao.AnalyticsDao
import com.emul8r.bizap.data.local.entities.DailyRevenueSnapshot
import com.emul8r.bizap.data.mapper.toDomain
import com.emul8r.bizap.data.util.DataAccessException
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.InvoiceStatus
import com.emul8r.bizap.domain.revenue.model.DailyRevenuePoint
import com.emul8r.bizap.domain.revenue.model.RevenueByCurrency
import com.emul8r.bizap.domain.revenue.model.RevenueMetrics
import com.emul8r.bizap.domain.revenue.repository.RevenueRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import org.json.JSONObject
import timber.log.Timber
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class RevenueRepositoryImpl @Inject constructor(
    private val analyticsDao: AnalyticsDao,
    private val invoiceDao: InvoiceDao
) : RevenueRepository {

    override fun getRevenueMetrics(businessProfileId: Long): Flow<RevenueMetrics> {
        // HYBRID REACTIVE ENGINE:
        // Combines live invoice data (for accurate totals) with cached snapshots (for trend history)
        return combine(
            invoiceDao.getInvoicesByBusinessId(businessProfileId),
            flow { emit(analyticsDao.getLast30DaysRevenue(businessProfileId)) }
        ) { invoiceWithItems, snapshots ->
            val invoices = invoiceWithItems.map { it.toDomain() }
            
            val mtd = calculateMTDLive(invoices)
            val ytd = calculateYTDLive(invoices)
            val pending = calculatePendingLive(invoices)
            
            // Trend still comes from snapshots for historical performance
            val dailyTrend = transformToDailyData(snapshots)
            val currencyBreakdown = calculateByCurrencyLive(invoices)

            RevenueMetrics(
                mtdRevenue = mtd,
                ytdRevenue = ytd,
                weeklyRevenue = 0L, // Optional: could add live calc
                pendingRevenue = pending,
                dailyTrend = dailyTrend,
                topPerformers = currencyBreakdown
            )
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun getRevenueMetricsSnapshot(businessProfileId: Long): RevenueMetrics = withContext(Dispatchers.IO) {
        // One-shot version used for background processing
        val dailySnapshots = analyticsDao.getLast30DaysRevenue(businessProfileId)
        if (dailySnapshots.isEmpty()) {
            return@withContext RevenueMetrics(0L, 0L, 0L, 0L, emptyList(), emptyList())
        }
        
        // Use the snapshot-based calculation for the one-shot fetch
        RevenueMetrics(
            mtdRevenue = calculateMTD(dailySnapshots),
            ytdRevenue = calculateYTD(dailySnapshots),
            weeklyRevenue = calculateWeekly(dailySnapshots),
            pendingRevenue = dailySnapshots.sumOf { it.pendingRevenue },
            dailyTrend = transformToDailyData(dailySnapshots),
            topPerformers = calculateByCurrency(dailySnapshots)
        )
    }

    // --- LIVE CALCULATIONS (From Invoices Table) ---

    private fun calculateMTDLive(invoices: List<Invoice>): Long {
        val currentMonth = YearMonth.now()
        return invoices.filter {
            val date = Instant.ofEpochMilli(it.date).atZone(ZoneId.systemDefault()).toLocalDate()
            YearMonth.from(date) == currentMonth
        }.sumOf { it.amountPaid }
    }

    private fun calculateYTDLive(invoices: List<Invoice>): Long {
        val currentYear = LocalDate.now().year
        return invoices.filter {
            val date = Instant.ofEpochMilli(it.date).atZone(ZoneId.systemDefault()).toLocalDate()
            date.year == currentYear
        }.sumOf { it.amountPaid }
    }

    private fun calculatePendingLive(invoices: List<Invoice>): Long {
        return invoices
            .filter { it.status != InvoiceStatus.DRAFT }
            .sumOf { it.totalAmount - it.amountPaid }
    }

    private fun calculateByCurrencyLive(invoices: List<Invoice>): List<RevenueByCurrency> {
        val totals = invoices.groupBy { it.currencyCode }
            .mapValues { (_, invs) -> invs.sumOf { it.amountPaid } }
        
        val grandTotal = totals.values.sum().toDouble()
        
        return totals.map { (code, amount) ->
            RevenueByCurrency(
                currencyCode = code,
                totalAmount = amount,
                percentageOfTotal = if (grandTotal > 0) amount / grandTotal else 0.0
            )
        }.sortedByDescending { it.totalAmount }
    }

    // --- SNAPSHOT CALCULATIONS (Legacy/Trend) ---

    private fun calculateMTD(snapshots: List<DailyRevenueSnapshot>): Long {
        val currentMonth = YearMonth.now()
        return snapshots.filter {
            try {
                val date = LocalDate.parse(it.dateString)
                YearMonth.from(date) == currentMonth
            } catch (e: Exception) { false }
        }.sumOf { it.totalRevenue }
    }

    private fun calculateYTD(snapshots: List<DailyRevenueSnapshot>): Long {
        val currentYear = LocalDate.now().year
        return snapshots.filter {
            try {
                val date = LocalDate.parse(it.dateString)
                date.year == currentYear
            } catch (e: Exception) { false }
        }.sumOf { it.totalRevenue }
    }

    private fun calculateWeekly(snapshots: List<DailyRevenueSnapshot>): Long {
        val sevenDaysAgo = LocalDate.now().minusDays(7)
        return snapshots.filter {
            try {
                val date = LocalDate.parse(it.dateString)
                date >= sevenDaysAgo
            } catch (e: Exception) { false }
        }.sumOf { it.totalRevenue }
    }

    private fun transformToDailyData(snapshots: List<DailyRevenueSnapshot>): List<DailyRevenuePoint> {
        return snapshots
            .sortedBy { it.dateString }
            .mapNotNull { snapshot ->
                try {
                    DailyRevenuePoint(
                        date = LocalDate.parse(snapshot.dateString),
                        amount = snapshot.totalRevenue,
                        invoiceCount = snapshot.invoiceCount
                    )
                } catch (e: Exception) { null }
            }
    }

    private fun calculateByCurrency(snapshots: List<DailyRevenueSnapshot>): List<RevenueByCurrency> {
        val currencyTotals = mutableMapOf<String, Long>()
        var grandTotal = 0L

        snapshots.forEach { snapshot ->
            try {
                val breakdown = snapshot.currencyBreakdown
                if (breakdown.isNullOrBlank() || breakdown == "{}") return@forEach
                
                val jsonObject = JSONObject(breakdown)
                jsonObject.keys().forEach { currency ->
                    val amount = try {
                        jsonObject.getLong(currency)
                    } catch (e: Exception) {
                        (jsonObject.optDouble(currency, 0.0) * 100).toLong()
                    }
                    currencyTotals[currency] = currencyTotals.getOrDefault(currency, 0L) + amount
                    grandTotal += amount
                }
            } catch (e: Exception) {
                Timber.w(e, "Failed to parse currency breakdown")
            }
        }

        return currencyTotals.map { (currency, amount) ->
            RevenueByCurrency(
                currencyCode = currency,
                totalAmount = amount,
                percentageOfTotal = if (grandTotal > 0) (amount.toDouble() / grandTotal.toDouble()) else 0.0
            )
        }.sortedByDescending { it.totalAmount }
    }
}
