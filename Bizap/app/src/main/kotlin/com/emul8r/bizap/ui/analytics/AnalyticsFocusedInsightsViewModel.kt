package com.emul8r.bizap.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.domain.analytics.AnalyticsDateRange
import com.emul8r.bizap.domain.repository.BusinessProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import timber.log.Timber
import javax.inject.Inject

/**
 * UI state for AnalyticsFocusedInsightsScreen.
 *
 * Represents the shared state across all tabs (selected tab, date range, refresh status).
 */
data class AnalyticsFocusedInsightsUiState(
    val selectedTabIndex: Int = 0,
    val selectedDateRange: AnalyticsDateRange = AnalyticsDateRange.THIRTY_DAYS,
    val isRefreshing: Boolean = false,
    val error: String? = null
)

/**
 * Parent ViewModel for the tabbed analytics dashboard.
 *
 * Manages:
 * - Tab selection (Revenue, Payment, Customer, CashFlow)
 * - Shared date range filtering across all tabs
 * - Refresh state
 *
 * Each tab has its own ViewModel that receives dateRange updates from this parent.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AnalyticsFocusedInsightsViewModel @Inject constructor(
    private val businessProfileRepository: BusinessProfileRepository
) : ViewModel() {

    private val _tabIndex = MutableStateFlow(0)
    private val _dateRange = MutableStateFlow(AnalyticsDateRange.THIRTY_DAYS)
    private val _isRefreshing = MutableStateFlow(false)

    val uiState: StateFlow<AnalyticsFocusedInsightsUiState> = combine(
        _tabIndex,
        _dateRange,
        _isRefreshing
    ) { tabIndex, dateRange, isRefreshing ->
        AnalyticsFocusedInsightsUiState(
            selectedTabIndex = tabIndex,
            selectedDateRange = dateRange,
            isRefreshing = isRefreshing
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AnalyticsFocusedInsightsUiState()
    )

    val activeBusinessId: StateFlow<Long> = businessProfileRepository.activeProfile
        .map { it.id }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 1L  // default
        )

    /**
     * Switch to a different tab by index.
     *
     * @param index 0 = Revenue, 1 = Payment, 2 = Customer, 3 = CashFlow
     */
    fun setTabIndex(index: Int) {
        _tabIndex.value = index
        Timber.d("Analytics: Switched to tab $index")
    }

    /**
     * Change the date range filter applied to all tabs.
     *
     * @param range One of SEVEN_DAYS, THIRTY_DAYS, NINETY_DAYS, or CUSTOM
     */
    fun setDateRange(range: AnalyticsDateRange) {
        _dateRange.value = range
        Timber.d("Analytics: Changed date range to ${range.label}")
    }

    /**
     * Set refresh loading state.
     */
    fun setIsRefreshing(refreshing: Boolean) {
        _isRefreshing.value = refreshing
    }
}

