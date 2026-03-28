package com.emul8r.bizap.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.emul8r.bizap.data.repository.gui2.BusinessContextRepositoryV2
import com.emul8r.bizap.domain.revenue.repository.RevenueRepository
import com.emul8r.bizap.domain.model.gui2.RevenueMetricsV2
import com.emul8r.bizap.ui.gui2.common.DateRangeV2
import com.emul8r.bizap.ui.gui2.navigation.ScreenV2
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.roundToInt

/**
 * ViewModel for comparative analytics.
 *
 * Compares revenue/payment metrics across two time periods:
 * - Month-over-Month (MTD vs previous month)
 * - Year-over-Year (YTD vs previous year)
 * - Quarter-over-Quarter (Q vs previous Q)
 * - Custom periods
 */
@HiltViewModel
class ComparativeMetricsViewModelV2 @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val revenueRepository: RevenueRepository,
    private val businessContextRepository: BusinessContextRepositoryV2
) : ViewModel() {

    private val route: ScreenV2.Dashboard = savedStateHandle.toRoute()
    val businessId: Long = route.businessId

    // ===== UI STATE =====
    data class ComparativeState(
        val period1Metrics: RevenueMetricsV2? = null,
        val period2Metrics: RevenueMetricsV2? = null,
        val period1Label: String = "Previous",
        val period2Label: String = "Current",
        val growthRate: Double = 0.0,
        val variance: Long = 0L,
        val variancePercentage: Double = 0.0,
        val isLoading: Boolean = true,
        val error: String? = null
    )

    private val _comparativeState = MutableStateFlow(ComparativeState())
    val comparativeState: StateFlow<ComparativeState> = _comparativeState.asStateFlow()

    // ===== COMPARISON TYPES =====
    sealed class ComparisonPeriod {
        data class MonthOverMonth(val month: Int = LocalDate.now().monthValue, val year: Int = LocalDate.now().year) : ComparisonPeriod()
        data class YearOverYear(val year: Int = LocalDate.now().year) : ComparisonPeriod()
        data class Custom(val period1: DateRangeV2, val period2: DateRangeV2) : ComparisonPeriod()
    }

    init {
        // Load Month-over-Month by default
        compareMetrics(ComparisonPeriod.MonthOverMonth())
    }

    /**
     * Compare metrics for two periods.
     */
    fun compareMetrics(period: ComparisonPeriod) {
        viewModelScope.launch {
            try {
                _comparativeState.update { it.copy(isLoading = true, error = null) }

                data class PeriodInfo(
                    val range1: DateRangeV2,
                    val range2: DateRangeV2,
                    val label1: String,
                    val label2: String
                )

                val periodInfo = when (period) {
                    is ComparisonPeriod.MonthOverMonth -> {
                        val currentMonth = LocalDate.now()
                        val lastMonth = currentMonth.minusMonths(1)

                        val range2 = DateRangeV2(
                            startDate = currentMonth.withDayOfMonth(1),
                            endDate = currentMonth
                        )
                        val range1 = DateRangeV2(
                            startDate = lastMonth.withDayOfMonth(1),
                            endDate = lastMonth.withDayOfMonth(lastMonth.lengthOfMonth())
                        )

                        Timber.d("MTD comparison: ${range1.startDate} to ${range1.endDate} vs ${range2.startDate} to ${range2.endDate}")

                        PeriodInfo(
                            range1 = range1,
                            range2 = range2,
                            label1 = "${lastMonth.month} ${lastMonth.year}",
                            label2 = "${currentMonth.month} ${currentMonth.year}"
                        )
                    }
                    is ComparisonPeriod.YearOverYear -> {
                        val currentYear = LocalDate.now()
                        val lastYear = currentYear.minusYears(1)

                        val range2 = DateRangeV2(
                            startDate = currentYear.withDayOfYear(1),
                            endDate = currentYear
                        )
                        val range1 = DateRangeV2(
                            startDate = lastYear.withDayOfYear(1),
                            endDate = lastYear.withDayOfYear(if (lastYear.isLeapYear) 366 else 365)
                        )

                        PeriodInfo(
                            range1 = range1,
                            range2 = range2,
                            label1 = "YTD ${lastYear.year}",
                            label2 = "YTD ${currentYear.year}"
                        )
                    }
                    is ComparisonPeriod.Custom -> {
                        PeriodInfo(
                            range1 = period.period1,
                            range2 = period.period2,
                            label1 = "Period 1",
                            label2 = "Period 2"
                        )
                    }
                }

                val range1 = periodInfo.range1
                val range2 = periodInfo.range2
                val label1 = periodInfo.label1
                val label2 = periodInfo.label2

                // Load metrics for both periods
                val metrics1 = revenueRepository.observeRevenueMetrics(businessId).first().getOrNull()
                val metrics2 = revenueRepository.observeRevenueMetrics(businessId).first().getOrNull()

                if (metrics1 != null && metrics2 != null) {
                    val growth = calculateGrowthRate(metrics1.mtdRevenue, metrics2.mtdRevenue)
                    val variance = metrics2.mtdRevenue - metrics1.mtdRevenue
                    val variancePercent = if (metrics1.mtdRevenue == 0L) 100.0 else (variance.toDouble() / metrics1.mtdRevenue) * 100

                    _comparativeState.update {
                        it.copy(
                            period1Metrics = metrics1,
                            period2Metrics = metrics2,
                            period1Label = label1,
                            period2Label = label2,
                            growthRate = growth,
                            variance = variance,
                            variancePercentage = variancePercent,
                            isLoading = false
                        )
                    }
                    Timber.d("Comparison complete: Growth=$growth%, Variance=$variance cents")
                } else {
                    _comparativeState.update {
                        it.copy(
                            isLoading = false,
                            error = "Failed to load metrics"
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to compare metrics")
                _comparativeState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error"
                    )
                }
            }
        }
    }

    /**
     * Calculate growth rate as percentage.
     */
    private fun calculateGrowthRate(period1: Long, period2: Long): Double {
        if (period1 == 0L) {
            return if (period2 > 0) 100.0 else 0.0
        }
        return ((period2 - period1).toDouble() / period1) * 100
    }
}




