package com.emul8r.bizap.ui.revenue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.domain.repository.BusinessProfileRepository
import com.emul8r.bizap.domain.revenue.model.RevenueMetrics
import com.emul8r.bizap.domain.revenue.usecase.GetRevenueMetricsUseCase
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
    private val businessProfileRepository: BusinessProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<RevenueDashboardUiState>(RevenueDashboardUiState.Loading)
    val uiState: StateFlow<RevenueDashboardUiState> = _uiState.asStateFlow()

    init {
        loadMetrics()
    }

    /**
     * Load revenue metrics from the database.
     * ✅ FIX: Public method so it can be called to refresh data
     */
    fun loadMetrics() {
        viewModelScope.launch {
            try {
                _uiState.value = RevenueDashboardUiState.Loading
                val businessId = businessProfileRepository.getActiveBusinessId()
                val metrics = getRevenueMetricsUseCase(businessId)
                _uiState.value = RevenueDashboardUiState.Success(metrics)
                Timber.d("✅ RevenueDashboardViewModel: Metrics loaded successfully")
            } catch (e: Exception) {
                Timber.e(e, "❌ RevenueDashboardViewModel: Failed to load metrics")
                _uiState.value = RevenueDashboardUiState.Error(e.message ?: "Unknown Error")
            }
        }
    }

    /**
     * ✅ NEW: Refresh metrics (called after invoice operations)
     * This allows the dashboard to update when data changes
     */
    fun refreshMetrics() {
        Timber.d("🔄 RevenueDashboardViewModel: Refreshing metrics after invoice operation")
        loadMetrics()
    }
}

sealed class RevenueDashboardUiState {
    object Loading : RevenueDashboardUiState()
    data class Success(val metrics: RevenueMetrics) : RevenueDashboardUiState()
    data class Error(val message: String) : RevenueDashboardUiState()
}
