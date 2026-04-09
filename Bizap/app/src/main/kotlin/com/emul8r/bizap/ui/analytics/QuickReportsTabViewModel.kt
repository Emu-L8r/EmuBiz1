package com.emul8r.bizap.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.domain.analytics.AnalyticsDateRange
import com.emul8r.bizap.domain.invoice.usecase.GetPaymentAnalyticsUseCase
import com.emul8r.bizap.domain.repository.BusinessProfileRepository
import com.emul8r.bizap.domain.revenue.repository.RevenueRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import timber.log.Timber
import javax.inject.Inject

/**
 * UI state for Quick Reports Tab - Executive Dashboard.
 *
 * Consolidates Revenue, Payment, and Risk analytics into one comprehensive view.
 * Shows 9 critical metrics for C-level executive overview.
 */
data class QuickReportsUiState(
    // Revenue & Invoice Metrics (Row 1)
    val totalRevenue: Double = 0.0,
    val revenueGrowth: Double = 0.0,
    val invoiceCount: Int = 0,

    // Payment Health Metrics (Row 2)
    val outstandingAmount: Double = 0.0,
    val collectionRate: Double = 0.0,
    val averageDaysToPayment: Double = 0.0,

    // Risk Metrics (Row 3)
    val atRiskCount: Int = 0,
    val overdueTotalAmount: Double = 0.0,
    val riskScore: Double = 0.0,

    val isLoading: Boolean = true,
    val error: String? = null
)

/**
 * ViewModel for Quick Reports Tab.
 *
 * Aggregates data from Revenue, Payment, and Risk sources.
 * Uses REAL data from GetPaymentAnalyticsUseCase and RevenueRepository.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class QuickReportsTabViewModel @Inject constructor(
    private val businessProfileRepository: BusinessProfileRepository,
    private val getPaymentAnalyticsUseCase: GetPaymentAnalyticsUseCase,
    private val revenueRepository: RevenueRepository
) : ViewModel() {

    private val _dateRange = MutableStateFlow(AnalyticsDateRange.THIRTY_DAYS)

    val state: StateFlow<QuickReportsUiState> = businessProfileRepository.activeProfile
        .flatMapLatest { profile ->
            Timber.d("QuickReports: Loading analytics for business ${profile.id}")
            combine(
                getPaymentAnalyticsUseCase(profile.id),
                revenueRepository.observeRevenueMetrics(profile.id)
            ) { paymentAnalytics, revenueResult ->
                val revenue = revenueResult.getOrNull()

                QuickReportsUiState(
                    // Revenue metrics (REAL DATA ✅)
                    totalRevenue = (revenue?.ytdRevenue ?: 0L) / 100.0,
                    revenueGrowth = computeRevenueGrowth(
                        mtd = revenue?.mtdRevenue ?: 0L,
                        ytd = revenue?.ytdRevenue ?: 0L
                    ),
                    invoiceCount = paymentAnalytics.totalInvoices,

                    // Payment metrics (REAL DATA ✅)
                    outstandingAmount = paymentAnalytics.totalOutstandingAmount,
                    collectionRate = paymentAnalytics.collectionRate * 100,
                    averageDaysToPayment = paymentAnalytics.averagePaymentTime,

                    // Risk metrics (REAL DATA ✅)
                    atRiskCount = paymentAnalytics.riskInvoices.size,
                    overdueTotalAmount = (revenue?.overdueAmount ?: 0L) / 100.0,
                    riskScore = (paymentAnalytics.overdueInvoices.toDouble() / maxOf(paymentAnalytics.totalInvoices, 1)) * 100,

                    isLoading = false
                )
            }.catch { error ->
                Timber.e(error, "QuickReports: Error loading analytics")
                emit(QuickReportsUiState(error = error.message ?: "Unknown error"))
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = QuickReportsUiState()
        )

    fun setDateRange(range: AnalyticsDateRange) {
        _dateRange.value = range
        Timber.d("QuickReports: Date range changed to ${range.label}")
    }

    /**
     * Estimate month-over-month revenue growth from YTD and MTD figures.
     *
     * Uses a simple heuristic: compare MTD revenue to the average monthly
     * revenue for the year so far. Returns 0 when there is insufficient data.
     */
    private fun computeRevenueGrowth(mtd: Long, ytd: Long): Double {
        if (ytd <= 0L) return 0.0
        val currentMonth = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1
        if (currentMonth <= 1) return 0.0
        val avgMonthlyRevenue = ytd.toDouble() / currentMonth
        if (avgMonthlyRevenue <= 0.0) return 0.0
        return ((mtd - avgMonthlyRevenue) / avgMonthlyRevenue) * 100.0
    }
}
