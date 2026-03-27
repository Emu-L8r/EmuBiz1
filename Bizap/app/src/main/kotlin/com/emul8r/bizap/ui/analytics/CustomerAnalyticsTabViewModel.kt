package com.emul8r.bizap.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.domain.analytics.AnalyticsDateRange
import com.emul8r.bizap.domain.customer.usecase.GetCustomerAnalyticsUseCase
import com.emul8r.bizap.domain.repository.BusinessProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import timber.log.Timber
import javax.inject.Inject

/**
 * UI state for Customer Analytics Tab.
 *
 * Displays customer metrics including total count, VIP/Regular/At-Risk segments,
 * average lifetime value, and detailed breakdowns.
 */
data class CustomerAnalyticsTabUiState(
    val totalCustomers: Int = 0,
    val vipCount: Int = 0,
    val regularCount: Int = 0,
    val atRiskCount: Int = 0,
    val dormantCount: Int = 0,
    val averageLTV: Double = 0.0,
    val churnRate: Double = 0.0,
    val isLoading: Boolean = true,
    val error: String? = null
)

/**
 * ViewModel for Customer Analytics Tab.
 *
 * Loads customer analytics summary and segments from GetCustomerAnalyticsUseCase.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CustomerAnalyticsTabViewModel @Inject constructor(
    private val getCustomerAnalyticsUseCase: GetCustomerAnalyticsUseCase,
    private val businessProfileRepository: BusinessProfileRepository
) : ViewModel() {

    private val _dateRange = MutableStateFlow(AnalyticsDateRange.THIRTY_DAYS)

    val state: StateFlow<CustomerAnalyticsTabUiState> = businessProfileRepository.activeProfile
        .flatMapLatest { profile ->
            Timber.d("CustomerTab: Loading analytics for business ${profile.id}")
            flow {
                try {
                    val analytics = getCustomerAnalyticsUseCase.execute(profile.id)
                    emit(
                        CustomerAnalyticsTabUiState(
                            totalCustomers = analytics.totalCustomers,
                            vipCount = analytics.vipCount,
                            regularCount = analytics.regularCount,
                            atRiskCount = analytics.atRiskCount,
                            dormantCount = analytics.dormantCount,
                            averageLTV = analytics.averageLTV,
                            churnRate = analytics.churnRate,
                            isLoading = false
                        )
                    )
                } catch (error: Exception) {
                    Timber.e(error, "CustomerTab: Error loading analytics")
                    emit(CustomerAnalyticsTabUiState(error = error.message ?: "Unknown error"))
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CustomerAnalyticsTabUiState()
        )

    /**
     * Update date range filter.
     *
     * TODO: Implement filtering logic once repository supports date ranges.
     */
    fun setDateRange(range: AnalyticsDateRange) {
        _dateRange.value = range
        Timber.d("CustomerTab: Date range changed to ${range.label}")
    }
}

