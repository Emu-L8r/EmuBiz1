package com.emul8r.bizap.ui.gui2.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.domain.model.InvoicePeriodData
import com.emul8r.bizap.domain.repository.InvoiceAnalyticsRepository
import com.emul8r.bizap.data.repository.gui2.BusinessContextRepositoryV2
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import timber.log.Timber
import javax.inject.Inject

/** Granularity toggle for the invoice analytics chart. */
enum class AnalyticsGranularity { WEEKLY, MONTHLY }

/** Available date range presets. */
enum class AnalyticsDateRange(val months: Int, val label: String) {
    THREE_MONTHS(3, "3 Months"),
    SIX_MONTHS(6, "6 Months"),
    TWELVE_MONTHS(12, "12 Months")
}

data class InvoiceAnalyticsState(
    val granularity: AnalyticsGranularity = AnalyticsGranularity.MONTHLY,
    val dateRange: AnalyticsDateRange = AnalyticsDateRange.SIX_MONTHS,
    val data: List<InvoicePeriodData> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class InvoiceAnalyticsViewModelV2 @Inject constructor(
    private val invoiceAnalyticsRepository: InvoiceAnalyticsRepository,
    private val businessContextRepository: BusinessContextRepositoryV2
) : ViewModel() {

    private val _granularity = MutableStateFlow(AnalyticsGranularity.MONTHLY)
    private val _dateRange = MutableStateFlow(AnalyticsDateRange.SIX_MONTHS)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<InvoiceAnalyticsState> = combine(
        _granularity,
        _dateRange,
        businessContextRepository.observeActiveBusinessId()
    ) { gran, range, bid -> Triple(gran, range, bid) }
        .mapLatest { (gran, range, bid) ->
            try {
                val data = when (gran) {
                    AnalyticsGranularity.WEEKLY ->
                        invoiceAnalyticsRepository.getWeeklyInvoiceTrend(bid, range.months)
                    AnalyticsGranularity.MONTHLY ->
                        invoiceAnalyticsRepository.getMonthlyInvoiceTrend(bid, range.months)
                }
                Timber.d("InvoiceAnalyticsViewModelV2: loaded ${data.size} periods for businessId=$bid")
                InvoiceAnalyticsState(granularity = gran, dateRange = range, data = data)
            } catch (e: Exception) {
                Timber.e(e, "InvoiceAnalyticsViewModelV2: error loading analytics")
                InvoiceAnalyticsState(granularity = gran, dateRange = range, error = e.message)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = InvoiceAnalyticsState(isLoading = true)
        )

    fun setGranularity(granularity: AnalyticsGranularity) {
        _granularity.value = granularity
    }

    fun setDateRange(range: AnalyticsDateRange) {
        _dateRange.value = range
    }
}
