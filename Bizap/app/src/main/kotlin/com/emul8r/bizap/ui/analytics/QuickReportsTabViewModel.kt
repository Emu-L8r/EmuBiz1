package com.emul8r.bizap.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.domain.analytics.AnalyticsDateRange
import com.emul8r.bizap.domain.invoice.usecase.GetPaymentAnalyticsUseCase
import com.emul8r.bizap.domain.repository.BusinessProfileRepository
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
    val totalRevenue: Double = 45000.0,
    val revenueGrowth: Double = 12.5,
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
 * Uses REAL data from GetPaymentAnalyticsUseCase.
 * Uses mock data for Revenue and Risk (ready to wire real data).
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class QuickReportsTabViewModel @Inject constructor(
    private val businessProfileRepository: BusinessProfileRepository,
    private val getPaymentAnalyticsUseCase: GetPaymentAnalyticsUseCase
) : ViewModel() {

    private val _dateRange = MutableStateFlow(AnalyticsDateRange.THIRTY_DAYS)

    val state: StateFlow<QuickReportsUiState> = businessProfileRepository.activeProfile
        .flatMapLatest { profile ->
            Timber.d("QuickReports: Loading analytics for business ${profile.id}")
            flow {
                try {
                    val paymentAnalytics = getPaymentAnalyticsUseCase(profile.id).first()

                    emit(
                        QuickReportsUiState(
                            // Revenue metrics (mock - ready for RevenueRepository)
                            totalRevenue = 45000.0,
                            revenueGrowth = 12.5,
                            invoiceCount = paymentAnalytics.totalInvoices,

                            // Payment metrics (REAL DATA ✅)
                            outstandingAmount = paymentAnalytics.totalOutstandingAmount,
                            collectionRate = paymentAnalytics.collectionRate * 100,
                            averageDaysToPayment = paymentAnalytics.averagePaymentTime,

                            // Risk metrics (mock - ready for RiskRepository)
                            atRiskCount = paymentAnalytics.riskInvoices.size,
                            overdueTotalAmount = 8500.0,
                            riskScore = (paymentAnalytics.overdueInvoices.toDouble() / maxOf(paymentAnalytics.totalInvoices, 1)) * 100,

                            isLoading = false
                        )
                    )
                } catch (error: Exception) {
                    Timber.e(error, "QuickReports: Error loading analytics")
                    emit(QuickReportsUiState(error = error.message ?: "Unknown error"))
                }
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
}
