package com.emul8r.bizap.ui.revenue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.domain.repository.BusinessProfileRepository
import com.emul8r.bizap.domain.revenue.model.RevenueMetrics
import com.emul8r.bizap.domain.revenue.usecase.GetRevenueMetricsUseCase
import com.emul8r.bizap.domain.revenue.usecase.RefreshAnalyticsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class RevenueDashboardViewModel @Inject constructor(
    private val getRevenueMetricsUseCase: GetRevenueMetricsUseCase,
    private val refreshAnalyticsUseCase: RefreshAnalyticsUseCase,
    private val businessProfileRepository: BusinessProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<RevenueDashboardUiState>(RevenueDashboardUiState.Loading)
    val uiState: StateFlow<RevenueDashboardUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private var currentBusinessId: Long = 0

    init {
        observeActiveBusiness()
    }

    private fun observeActiveBusiness() {
        businessProfileRepository.activeProfile
            .onEach { business ->
                if (business.id != currentBusinessId) {
                    currentBusinessId = business.id
                    loadMetrics(autoRefresh = true)
                }
            }
            .launchIn(viewModelScope)
    }

    fun loadMetrics(autoRefresh: Boolean = false) {
        if (currentBusinessId == 0L) return
        
        viewModelScope.launch {
            try {
                _uiState.value = RevenueDashboardUiState.Loading
                val metrics = getRevenueMetricsUseCase(currentBusinessId)
                
                // Auto-refresh if no data exists and it's the initial load for this business
                if (autoRefresh && metrics.dailyTrend.isEmpty()) {
                    Timber.d("No metrics found for business $currentBusinessId, triggering auto-refresh...")
                    refreshAnalytics()
                } else {
                    _uiState.value = RevenueDashboardUiState.Success(metrics)
                }
            } catch (e: Exception) {
                Timber.e(e, "RevenueDashboardViewModel: Failed to load metrics")
                _uiState.value = RevenueDashboardUiState.Error(e.message ?: "Unknown Error")
            }
        }
    }

    fun refreshAnalytics() {
        if (currentBusinessId == 0L) return

        viewModelScope.launch {
            try {
                _isRefreshing.value = true
                refreshAnalyticsUseCase(currentBusinessId)
                val metrics = getRevenueMetricsUseCase(currentBusinessId)
                _uiState.value = RevenueDashboardUiState.Success(metrics)
                Timber.d("RevenueDashboardViewModel: Analytics refreshed successfully for business $currentBusinessId")
            } catch (e: Exception) {
                Timber.e(e, "RevenueDashboardViewModel: Failed to refresh analytics")
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}

sealed class RevenueDashboardUiState {
    object Loading : RevenueDashboardUiState()
    data class Success(val metrics: RevenueMetrics) : RevenueDashboardUiState()
    data class Error(val message: String) : RevenueDashboardUiState()
}
