package com.emul8r.bizap.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.domain.analytics.AnalyticsDateRange
import com.emul8r.bizap.domain.analytics.ChartDataPoint
import com.emul8r.bizap.domain.analytics.TrendMetric
import com.emul8r.bizap.domain.invoice.usecase.GetRevenueAnalyticsTrendUseCase
import com.emul8r.bizap.domain.repository.BusinessProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import timber.log.Timber
import javax.inject.Inject

/**
 * UI state for Revenue Analytics Tab.
 *
 * Displays month-to-date (MTD) and year-to-date (YTD) revenue with trends,
 * daily trend chart, revenue breakdown by invoice status, and top invoices.
 */
data class RevenueAnalyticsTabUiState(
    val mtdRevenue: TrendMetric? = null,
    val ytdRevenue: TrendMetric? = null,
    val dailyTrendData: List<ChartDataPoint> = emptyList(),
    val revenueByStatus: Map<String, Double> = emptyMap(),
    val topInvoices: List<Pair<String, Double>> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

/**
 * ViewModel for Revenue Analytics Tab.
 *
 * Manages revenue metrics queries and filters based on date range selection.
 * Uses GetRevenueAnalyticsTrendUseCase to fetch comprehensive revenue data.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class RevenueAnalyticsTabViewModel @Inject constructor(
    private val businessProfileRepository: BusinessProfileRepository,
    private val getRevenueAnalyticsTrendUseCase: GetRevenueAnalyticsTrendUseCase
) : ViewModel() {

    private val _dateRange = MutableStateFlow(AnalyticsDateRange.THIRTY_DAYS)

    val state: StateFlow<RevenueAnalyticsTabUiState> = _dateRange
        .flatMapLatest { range ->
            businessProfileRepository.activeProfile
                .flatMapLatest { profile ->
                    Timber.d("RevenueTab: Loading analytics for business ${profile.id}, range=${range.label}")

                    getRevenueAnalyticsTrendUseCase(profile.id)
                        .map { trend ->
                            RevenueAnalyticsTabUiState(
                                mtdRevenue = trend.mtdRevenue,
                                ytdRevenue = trend.ytdRevenue,
                                dailyTrendData = trend.dailyTrend,
                                revenueByStatus = trend.revenueByStatus,
                                topInvoices = trend.topInvoices,
                                isLoading = false
                            )
                        }
                        .catch { error ->
                            Timber.e(error, "RevenueTab: Error loading analytics")
                            emit(RevenueAnalyticsTabUiState(error = error.message ?: "Unknown error"))
                        }
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = RevenueAnalyticsTabUiState()
        )

    /**
     * Update date range filter.
     * Automatically triggers reload of revenue data via reactive Flow.
     */
    fun setDateRange(range: AnalyticsDateRange) {
        _dateRange.value = range
        Timber.d("RevenueTab: Date range changed to ${range.label}")
    }
}


