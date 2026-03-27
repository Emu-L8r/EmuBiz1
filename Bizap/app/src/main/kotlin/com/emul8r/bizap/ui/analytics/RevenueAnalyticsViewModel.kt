package com.emul8r.bizap.ui.analytics

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.domain.repository.InvoiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

                // Get today's date
                val today = calendar.apply {
                    timeInMillis = now
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }.timeInMillis

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

                // For MVP: Return mock revenue data
                // TODO: Wire to actual invoice queries in future
                val mockDailyRevenue = listOf(
                    DailyRevenue(today - (6 * 86400000), 0),
                    DailyRevenue(today - (5 * 86400000), 25000),
                    DailyRevenue(today - (4 * 86400000), 15000),
                    DailyRevenue(today - (3 * 86400000), 45000),
                    DailyRevenue(today - (2 * 86400000), 30000),
                    DailyRevenue(today - 86400000, 55000),
                    DailyRevenue(today, 62500)
                )

                val totalRevenue = mockDailyRevenue.sumOf { it.amount }
                val averageDaily = if (mockDailyRevenue.isNotEmpty()) {
                    totalRevenue / mockDailyRevenue.size
                } else {
                    0L
                }

                // Calculate trend (last 3 days vs previous 3 days)
                val last3Days = mockDailyRevenue.takeLast(3).sumOf { it.amount }
                val previous3Days = mockDailyRevenue.dropLast(3).takeLast(3).sumOf { it.amount }
                val trend = if (previous3Days > 0) {
                    ((last3Days - previous3Days).toFloat() / previous3Days.toFloat()) * 100f
                } else {
                    0f
                }

                val metrics = RevenueMetrics(
                    dailyRevenue = mockDailyRevenue,
                    totalRevenue = totalRevenue,
                    averageDaily = averageDaily,
                    thisMonthRevenue = (totalRevenue * 0.4).toLong(),
                    thisYearRevenue = (totalRevenue * 0.8).toLong(),
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

