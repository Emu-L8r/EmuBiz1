package com.emul8r.bizap.ui.revenue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.domain.revenue.model.RevenueMetrics
import com.emul8r.bizap.domain.revenue.usecase.GetRevenueMetricsUseCase
import com.emul8r.bizap.domain.revenue.usecase.RefreshAnalyticsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class RevenueDashboardViewModel @Inject constructor(
    private val getRevenueMetricsUseCase: GetRevenueMetricsUseCase,
    private val refreshAnalyticsUseCase: RefreshAnalyticsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<RevenueDashboardUiState>(RevenueDashboardUiState.Loading)
    val uiState: StateFlow<RevenueDashboardUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // Scoped to first business for now
    private val businessId = 1L

    init {
        loadMetrics(autoRefresh = true)
    }

    fun loadMetrics(autoRefresh: Boolean = false) {
        viewModelScope.launch {
            try {
                _uiState.value = RevenueDashboardUiState.Loading
                var metrics = getRevenueMetricsUseCase(businessId)
                
                // Auto-refresh if no data exists and it's the initial load
                if (autoRefresh && metrics.dailyTrend.isEmpty()) {
                    Timber.d("No metrics found, triggering auto-refresh...")
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
        viewModelScope.launch {
            try {
                _isRefreshing.value = true
                refreshAnalyticsUseCase(businessId)
                val metrics = getRevenueMetricsUseCase(businessId)
                _uiState.value = RevenueDashboardUiState.Success(metrics)
                Timber.d("RevenueDashboardViewModel: Analytics refreshed successfully")
            } catch (e: Exception) {
                Timber.e(e, "RevenueDashboardViewModel: Failed to refresh analytics")
                // Keep current state but maybe show a snackbar (handled in UI via shared flow if needed)
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
