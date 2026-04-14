package com.emul8r.bizap.ui.analytics

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.domain.model.InvoiceStatus
import com.emul8r.bizap.domain.repository.InvoiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import javax.inject.Inject

/**
 * ViewModel for Revenue Analytics.
 *
 * Calculates and tracks revenue metrics over time:
 * - Daily, weekly, and monthly revenue
 * - Revenue trends and growth percentages
 * - Year-to-date totals
 */
@HiltViewModel
class RevenueAnalyticsViewModel @Inject constructor(
    private val invoiceRepository: InvoiceRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val businessId = savedStateHandle.get<Long>("businessId") ?: 0L

    private val _revenueMetrics = MutableStateFlow<RevenueMetricsState>(RevenueMetricsState.Loading)
    val revenueMetrics: StateFlow<RevenueMetricsState> = _revenueMetrics.asStateFlow()

    init {
        loadRevenueMetrics()
    }

    private fun loadRevenueMetrics() {
        viewModelScope.launch {
            try {
                val now = System.currentTimeMillis()
                val calendar = Calendar.getInstance()

                // Get year start
                val yearStart = calendar.apply {
                    set(Calendar.DAY_OF_YEAR, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }.timeInMillis

                // Get month start
                val monthStart = calendar.apply {
                    timeInMillis = now
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }.timeInMillis

                val invoices = invoiceRepository.getAllInvoicesWithItems().first()
                val paidInvoices = invoices.filter { it.status == InvoiceStatus.PAID }

                // Group by date
                val dailyRevenueMap = mutableMapOf<Long, Long>()
                paidInvoices.forEach { invoice ->
                    val invoiceDateMs = try {
                        java.time.Instant.parse(invoice.dateCreated).toEpochMilli() - (java.time.Instant.parse(invoice.dateCreated).toEpochMilli() % 86400000)
                    } catch (e: Exception) {
                        System.currentTimeMillis() - (System.currentTimeMillis() % 86400000)
                    }
                    val currentAmount = dailyRevenueMap[invoiceDateMs] ?: 0L
                    dailyRevenueMap[invoiceDateMs] = currentAmount + invoice.totalAmount
                }

                val dailyRevenue = dailyRevenueMap
                    .map { (dateMs, amount) -> DailyRevenue(dateMs, amount) }
                    .sortedBy { it.dateMs }
                    .takeLast(30)

                val totalRevenue = dailyRevenue.sumOf { it.amount }

                // Calculate trend
                val last7Days = dailyRevenue.takeLast(7).sumOf { it.amount }
                val previous7Days = dailyRevenue.dropLast(7).takeLast(7).sumOf { it.amount }
                val trend = if (previous7Days > 0) {
                    ((last7Days - previous7Days).toFloat() / previous7Days.toFloat()) * 100f
                } else {
                    0f
                }

                // Month/year revenue
                val thisMonthRevenue = paidInvoices
                    .filter {
                        try {
                            val invoiceDate = java.time.Instant.parse(it.dateCreated).toEpochMilli()
                            invoiceDate >= monthStart
                        } catch (e: Exception) {
                            false
                        }
                    }
                    .sumOf { it.totalAmount }

                val thisYearRevenue = paidInvoices
                    .filter {
                        try {
                            val invoiceDate = java.time.Instant.parse(it.dateCreated).toEpochMilli()
                            invoiceDate >= yearStart
                        } catch (e: Exception) {
                            false
                        }
                    }
                    .sumOf { it.totalAmount }

                val averageDaily = if (dailyRevenue.isNotEmpty()) {
                    totalRevenue / dailyRevenue.size
                } else {
                    0L
                }

                val metrics = RevenueMetrics(
                    dailyRevenue = dailyRevenue,
                    totalRevenue = totalRevenue,
                    averageDaily = averageDaily,
                    thisMonthRevenue = thisMonthRevenue,
                    thisYearRevenue = thisYearRevenue,
                    trend = trend
                )

                _revenueMetrics.value = RevenueMetricsState.Success(metrics)
                Timber.d("✅ Revenue metrics loaded: total=$totalRevenue, trend=$trend%")
            } catch (e: Exception) {
                Timber.e(e, "Failed to load revenue metrics")
                _revenueMetrics.value = RevenueMetricsState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

sealed class RevenueMetricsState {
    object Loading : RevenueMetricsState()
    data class Success(val metrics: RevenueMetrics) : RevenueMetricsState()
    data class Error(val message: String) : RevenueMetricsState()
}

data class RevenueMetrics(
    val dailyRevenue: List<DailyRevenue> = emptyList(),
    val totalRevenue: Long = 0,
    val averageDaily: Long = 0,
    val thisMonthRevenue: Long = 0,
    val thisYearRevenue: Long = 0,
    val trend: Float = 0f  // Percentage change
)

data class DailyRevenue(
    val dateMs: Long,
    val amount: Long
)
