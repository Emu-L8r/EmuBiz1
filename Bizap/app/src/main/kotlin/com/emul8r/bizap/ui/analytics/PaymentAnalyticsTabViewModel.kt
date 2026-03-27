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
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PaymentAnalyticsTabViewModel @Inject constructor(
    private val getPaymentAnalyticsUseCase: GetPaymentAnalyticsUseCase,
    private val businessProfileRepository: BusinessProfileRepository
) : ViewModel() {

    private val _dateRange = MutableStateFlow(AnalyticsDateRange.THIRTY_DAYS)

    /**
     * Reuse existing payment analytics logic from GetPaymentAnalyticsUseCase.
     *
     * TODO: Enhance this to respect date range filtering once repository supports it.
     */
    val state: StateFlow<PaymentAnalyticsSummary?> = businessProfileRepository.activeProfile
        .flatMapLatest { profile ->
            Timber.d("PaymentTab: Loading analytics for business ${profile.id}")
            getPaymentAnalyticsUseCase(profile.id)
                .catch { error ->
                    Timber.e(error, "PaymentTab: Error loading analytics")
                    // Emit empty/zero state on error instead of null
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
     * TODO: Implement filtering logic once repository supports date ranges.
     */
    fun setDateRange(range: AnalyticsDateRange) {
        _dateRange.value = range
        Timber.d("PaymentTab: Date range changed to ${range.label}")
    }
}


