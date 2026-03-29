package com.emul8r.bizap.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.emul8r.bizap.data.repository.gui2.InvoiceMetricsRepositoryV2
import com.emul8r.bizap.domain.model.reporting.*
import com.emul8r.bizap.ui.gui2.navigation.ScreenV2
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import timber.log.Timber
import java.util.*
import javax.inject.Inject

/**
 * ViewModel for advanced reporting and month-over-month comparisons.
 */
@HiltViewModel
class AdvancedReportingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val metricsRepository: InvoiceMetricsRepositoryV2
) : ViewModel() {

    private val route: ScreenV2.AdvancedReporting = savedStateHandle.toRoute()
    val businessId: Long = route.businessId

    // Current period selection
    private val _selectedPeriod = MutableStateFlow<PeriodSelection>(PeriodSelection.CurrentMonth)
    val selectedPeriod: StateFlow<PeriodSelection> = _selectedPeriod.asStateFlow()

    // Month-over-month comparison state
    private val _monthComparisonState = MutableStateFlow<MonthComparisonState>(
        MonthComparisonState.Loading
    )
    val monthComparisonState: StateFlow<MonthComparisonState> = _monthComparisonState.asStateFlow()

    // Year-over-year comparison state
    private val _yearComparisonState = MutableStateFlow<YearComparisonState>(
        YearComparisonState.Loading
    )
    val yearComparisonState: StateFlow<YearComparisonState> = _yearComparisonState.asStateFlow()

    init {
        loadComparisons()
    }

    private fun loadComparisons() {
        try {
            _monthComparisonState.value = MonthComparisonState.Loading
            _yearComparisonState.value = YearComparisonState.Loading

            // Load month-over-month comparison
            val monthComparison = calculateMonthOverMonthComparison()
            _monthComparisonState.value = MonthComparisonState.Success(monthComparison)

            // Load year-over-year comparison
            val yearComparison = calculateYearOverYearComparison()
            _yearComparisonState.value = YearComparisonState.Success(yearComparison)

            Timber.d("Advanced reporting data loaded successfully for businessId=$businessId")
        } catch (e: Exception) {
            Timber.e(e, "Failed to load advanced reporting data")
            _monthComparisonState.value = MonthComparisonState.Error(e.message ?: "Unknown error")
            _yearComparisonState.value = YearComparisonState.Error(e.message ?: "Unknown error")
        }
    }

    private fun calculateMonthOverMonthComparison(): MonthComparison {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance().apply { timeInMillis = now }

        // Current month
        val currentMonthStart = calendar.apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }.timeInMillis

        val currentMonthEnd = now

        // Previous month
        calendar.add(Calendar.MONTH, -1)
        val previousMonthStart = calendar.apply {
            set(Calendar.DAY_OF_MONTH, 1)
        }.timeInMillis
        val previousMonthEnd = calendar.apply {
            set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }.timeInMillis

        val currentMetrics = getMonthMetrics(currentMonthStart, currentMonthEnd)
        val previousMetrics = getMonthMetrics(previousMonthStart, previousMonthEnd)

        val comparisonMetrics = buildMonthComparisonMetrics(currentMetrics, previousMetrics)

        return MonthComparison(
            currentMonth = currentMetrics,
            previousMonth = previousMetrics,
            comparison = ComparisonReport(
                period = Period(
                    startDate = currentMonthStart,
                    endDate = currentMonthEnd,
                    label = "Current Month"
                ),
                comparisonPeriod = Period(
                    startDate = previousMonthStart,
                    endDate = previousMonthEnd,
                    label = "Previous Month"
                ),
                metrics = comparisonMetrics
            )
        )
    }

    private fun getMonthMetrics(startMs: Long, endMs: Long): MonthMetrics {
        val calendar = Calendar.getInstance().apply { timeInMillis = startMs }
        val month = calendar.get(Calendar.MONTH) + 1
        val year = calendar.get(Calendar.YEAR)

        // In a real implementation, query from database
        // For now, return placeholder
        return MonthMetrics(
            month = month,
            year = year,
            revenue = 0.0,
            invoiceCount = 0,
            paidCount = 0,
            unpaidCount = 0,
            overDueCount = 0,
            averageDaysToPayment = 0.0
        )
    }

    private fun buildMonthComparisonMetrics(
        current: MonthMetrics,
        previous: MonthMetrics
    ): List<ComparisonMetrics> {
        val metrics = mutableListOf<ComparisonMetrics>()

        // Revenue comparison
        val revenueChange = current.revenue - previous.revenue
        val revenueChangePercent = if (previous.revenue > 0) {
            (revenueChange / previous.revenue) * 100
        } else {
            0.0
        }
        metrics.add(
            ComparisonMetrics(
                metric = "Revenue",
                currentValue = current.revenue,
                previousValue = previous.revenue,
                changeAmount = revenueChange,
                changePercent = revenueChangePercent,
                trend = when {
                    revenueChange > 0 -> TrendDirection.UP
                    revenueChange < 0 -> TrendDirection.DOWN
                    else -> TrendDirection.FLAT
                }
            )
        )

        // Invoice count comparison
        val invoiceChange = (current.invoiceCount - previous.invoiceCount).toDouble()
        val invoiceChangePercent = if (previous.invoiceCount > 0) {
            (invoiceChange / previous.invoiceCount) * 100
        } else {
            0.0
        }
        metrics.add(
            ComparisonMetrics(
                metric = "Invoice Count",
                currentValue = current.invoiceCount.toDouble(),
                previousValue = previous.invoiceCount.toDouble(),
                changeAmount = invoiceChange,
                changePercent = invoiceChangePercent,
                trend = when {
                    invoiceChange > 0 -> TrendDirection.UP
                    invoiceChange < 0 -> TrendDirection.DOWN
                    else -> TrendDirection.FLAT
                }
            )
        )

        // Payment rate comparison
        val currentPaymentRate = if (current.invoiceCount > 0) {
            (current.paidCount.toDouble() / current.invoiceCount) * 100
        } else {
            0.0
        }
        val previousPaymentRate = if (previous.invoiceCount > 0) {
            (previous.paidCount.toDouble() / previous.invoiceCount) * 100
        } else {
            0.0
        }
        metrics.add(
            ComparisonMetrics(
                metric = "Payment Rate",
                currentValue = currentPaymentRate,
                previousValue = previousPaymentRate,
                changeAmount = currentPaymentRate - previousPaymentRate,
                changePercent = currentPaymentRate - previousPaymentRate,
                trend = when {
                    currentPaymentRate > previousPaymentRate -> TrendDirection.UP
                    currentPaymentRate < previousPaymentRate -> TrendDirection.DOWN
                    else -> TrendDirection.FLAT
                }
            )
        )

        return metrics
    }

    private fun calculateYearOverYearComparison(): YearComparison {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance().apply { timeInMillis = now }

        val currentYear = calendar.get(Calendar.YEAR)
        val previousYear = currentYear - 1

        val yearStartCurrentMs = calendar.apply {
            set(Calendar.YEAR, currentYear)
            set(Calendar.MONTH, 0)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }.timeInMillis

        val yearEndCurrentMs = now

        val yearStartPreviousMs = calendar.apply {
            set(Calendar.YEAR, previousYear)
        }.timeInMillis

        val yearEndPreviousMs = calendar.apply {
            set(Calendar.MONTH, 11)
            set(Calendar.DAY_OF_MONTH, 31)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }.timeInMillis

        val currentYearMetrics = getYearMetrics(currentYear, yearStartCurrentMs, yearEndCurrentMs)
        val previousYearMetrics = getYearMetrics(previousYear, yearStartPreviousMs, yearEndPreviousMs)

        val comparisonMetrics = buildYearComparisonMetrics(currentYearMetrics, previousYearMetrics)

        return YearComparison(
            currentYear = currentYearMetrics,
            previousYear = previousYearMetrics,
            comparison = ComparisonReport(
                period = Period(
                    startDate = yearStartCurrentMs,
                    endDate = yearEndCurrentMs,
                    label = "Current Year"
                ),
                comparisonPeriod = Period(
                    startDate = yearStartPreviousMs,
                    endDate = yearEndPreviousMs,
                    label = "Previous Year"
                ),
                metrics = comparisonMetrics
            )
        )
    }

    private fun getYearMetrics(year: Int, startMs: Long, endMs: Long): YearMetrics {
        return YearMetrics(
            year = year,
            revenue = 0.0,
            invoiceCount = 0,
            paidCount = 0,
            unpaidCount = 0,
            monthlyBreakdown = emptyList()
        )
    }

    private fun buildYearComparisonMetrics(
        current: YearMetrics,
        previous: YearMetrics
    ): List<ComparisonMetrics> {
        val metrics = mutableListOf<ComparisonMetrics>()

        // Revenue comparison
        val revenueChange = current.revenue - previous.revenue
        val revenueChangePercent = if (previous.revenue > 0) {
            (revenueChange / previous.revenue) * 100
        } else {
            0.0
        }
        metrics.add(
            ComparisonMetrics(
                metric = "Revenue",
                currentValue = current.revenue,
                previousValue = previous.revenue,
                changeAmount = revenueChange,
                changePercent = revenueChangePercent,
                trend = when {
                    revenueChange > 0 -> TrendDirection.UP
                    revenueChange < 0 -> TrendDirection.DOWN
                    else -> TrendDirection.FLAT
                }
            )
        )

        return metrics
    }

    fun selectPeriod(period: PeriodSelection) {
        _selectedPeriod.value = period
        loadComparisons()
    }

    fun refreshComparisons() {
        loadComparisons()
    }
}

enum class PeriodSelection {
    CurrentMonth,
    LastQuarter,
    LastYear,
    Custom
}

sealed class MonthComparisonState {
    object Loading : MonthComparisonState()
    data class Success(val comparison: MonthComparison) : MonthComparisonState()
    data class Error(val message: String) : MonthComparisonState()
}

sealed class YearComparisonState {
    object Loading : YearComparisonState()
    data class Success(val comparison: YearComparison) : YearComparisonState()
    data class Error(val message: String) : YearComparisonState()
}








