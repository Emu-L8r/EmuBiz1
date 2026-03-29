package com.emul8r.bizap.ui.gui2.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.data.repository.gui2.BusinessContextRepositoryV2
import com.emul8r.bizap.data.repository.gui2.PaymentAnalyticsRepositoryV2
import com.emul8r.bizap.domain.model.InvoiceStatus
import com.emul8r.bizap.domain.model.gui2.PaymentMetricsV2
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import timber.log.Timber
import javax.inject.Inject

/**
 * Filter state for payment analytics
 */
data class PaymentAnalyticsFilterState(
    val startDate: Long? = null,
    val endDate: Long? = null,
    val statuses: Set<InvoiceStatus> = setOf(InvoiceStatus.SENT, InvoiceStatus.OVERDUE)
)

@HiltViewModel
class PaymentAnalyticsViewModelV2 @Inject constructor(
    businessContextRepository: BusinessContextRepositoryV2,
    private val paymentRepository: PaymentAnalyticsRepositoryV2
) : ViewModel() {

    // Filter state
    private val _filterState = MutableStateFlow(PaymentAnalyticsFilterState())
    val filterState: StateFlow<PaymentAnalyticsFilterState> = _filterState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<PaymentAnalyticsUiStateV2> =
        businessContextRepository.observeActiveBusinessId()
            .flatMapLatest { businessId ->
                Timber.d("PaymentAnalyticsViewModelV2: observing businessId=$businessId")
                combine(
                    paymentRepository.observePaymentMetrics(businessId),
                    _filterState
                ) { result, filter ->
                    result.fold(
                        onSuccess = { metrics ->
                            Timber.d("PaymentAnalyticsViewModelV2: metrics updated for businessId=$businessId")
                            PaymentAnalyticsUiStateV2.Success(
                                metrics = metrics,
                                filterState = filter
                            )
                        },
                        onFailure = { error ->
                            Timber.e(error, "PaymentAnalyticsViewModelV2: error")
                            PaymentAnalyticsUiStateV2.Error(error.message ?: "Unknown error")
                        }
                    )
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = PaymentAnalyticsUiStateV2.Loading
            )

    /**
     * Set date range filter
     */
    fun setDateRange(startDate: Long?, endDate: Long?) {
        _filterState.value = _filterState.value.copy(
            startDate = startDate,
            endDate = endDate
        )
        Timber.d("Date filter set: $startDate - $endDate")
    }

    /**
     * Set status filter
     */
    fun setStatusFilter(statuses: Set<InvoiceStatus>) {
        _filterState.value = _filterState.value.copy(statuses = statuses)
        Timber.d("Status filter set: $statuses")
    }

    /**
     * Clear all filters
     */
    fun clearFilters() {
        _filterState.value = PaymentAnalyticsFilterState()
        Timber.d("Filters cleared")
    }
}

sealed class PaymentAnalyticsUiStateV2 {
    object Loading : PaymentAnalyticsUiStateV2()
    data class Success(
        val metrics: PaymentMetricsV2,
        val filterState: PaymentAnalyticsFilterState = PaymentAnalyticsFilterState()
    ) : PaymentAnalyticsUiStateV2()
    data class Error(val message: String) : PaymentAnalyticsUiStateV2()
}
