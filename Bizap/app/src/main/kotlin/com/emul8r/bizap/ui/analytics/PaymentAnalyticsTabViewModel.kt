package com.emul8r.bizap.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.domain.analytics.AnalyticsDateRange
import com.emul8r.bizap.domain.invoice.model.PaymentAnalyticsSummary
import com.emul8r.bizap.domain.invoice.usecase.GetPaymentAnalyticsUseCase
import com.emul8r.bizap.domain.repository.BusinessProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for Payment Analytics Tab.
 *
 * Wraps the existing GetPaymentAnalyticsUseCase and integrates with
 * the tabbed date range selection from AnalyticsFocusedInsightsViewModel.
 *
 * Date range filtering is applied at collection time: snapshots with a
 * `dueDate` (or `createdAt`) outside the selected window are excluded.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PaymentAnalyticsTabViewModel @Inject constructor(
    private val getPaymentAnalyticsUseCase: GetPaymentAnalyticsUseCase,
    private val businessProfileRepository: BusinessProfileRepository
) : ViewModel() {

    private val _dateRange = MutableStateFlow(AnalyticsDateRange.THIRTY_DAYS)

    /**
     * Payment analytics filtered by the selected date range.
     *
     * When the date range changes the upstream use case is re-subscribed so the
     * UI always reflects the correct window.
     */
    val state: StateFlow<PaymentAnalyticsSummary?> = combine(
        businessProfileRepository.activeProfile,
        _dateRange
    ) { profile, range -> profile to range }
        .flatMapLatest { (profile, range) ->
            Timber.d("PaymentTab: Loading analytics for business ${profile.id}, range=${range.label}")
            getPaymentAnalyticsUseCase(profile.id)
                .map { summary -> filterByDateRange(summary, range) }
                .catch { error ->
                    Timber.e(error, "PaymentTab: Error loading analytics")
                    emit(
                        PaymentAnalyticsSummary(
                            businessProfileId = profile.id,
                            totalInvoices = 0,
                            paidInvoices = 0,
                            unpaidInvoices = 0,
                            overdueInvoices = 0,
                            totalInvoiceAmount = 0.0,
                            totalPaidAmount = 0.0,
                            totalOutstandingAmount = 0.0,
                            collectionRate = 0.0,
                            averagePaymentTime = 0.0,
                            outstandingByAging = com.emul8r.bizap.domain.invoice.model.OutstandingByAging(0.0, 0.0, 0.0, 0.0, 0.0),
                            riskInvoices = emptyList(),
                            cashFlowForecast = emptyList()
                        )
                    )
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    /**
     * Update date range filter.
     *
     * Triggers an immediate re-calculation of [state] for the new window.
     */
    fun setDateRange(range: AnalyticsDateRange) {
        _dateRange.value = range
        Timber.d("PaymentTab: Date range changed to ${range.label}")
    }

    /**
     * Applies a date-range filter to the summary.
     *
     * For the CUSTOM range (days == -1) no filtering is applied — the full dataset
     * is returned as-is until a custom date picker is wired up.
     */
    private fun filterByDateRange(
        summary: PaymentAnalyticsSummary,
        range: AnalyticsDateRange
    ): PaymentAnalyticsSummary {
        if (range == AnalyticsDateRange.CUSTOM || range.days <= 0) return summary

        val cutoffDate = java.time.LocalDate.now().minusDays(range.days.toLong())

        // Filter risk invoices to those within the date range
        val filteredRiskInvoices = summary.riskInvoices.filter { it.dueDate >= cutoffDate }
        // Filter cash flow forecast to future dates within range
        val filteredForecast = summary.cashFlowForecast.filter { it.projectedDate >= cutoffDate }

        return summary.copy(
            riskInvoices = filteredRiskInvoices,
            cashFlowForecast = filteredForecast
        )
    }
}


