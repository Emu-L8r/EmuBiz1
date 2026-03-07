package com.emul8r.bizap.ui.revenue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.domain.repository.BusinessProfileRepository
import com.emul8r.bizap.domain.revenue.model.RevenueMetrics
import com.emul8r.bizap.domain.revenue.usecase.GetRevenueMetricsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import timber.log.Timber
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class RevenueDashboardViewModel @Inject constructor(
    private val getRevenueMetricsUseCase: GetRevenueMetricsUseCase,
    private val businessProfileRepository: BusinessProfileRepository
) : ViewModel() {

    val uiState: StateFlow<RevenueDashboardUiState> =
        businessProfileRepository.activeProfile
            .flatMapLatest { businessProfile ->
                getRevenueMetricsUseCase(businessProfile.id)
                    .map { metrics ->
                        Timber.d("✅ RevenueDashboardViewModel: Metrics updated reactively")
                        RevenueDashboardUiState.Success(metrics) as RevenueDashboardUiState
                    }
                    .catch { error ->
                        Timber.e(error, "❌ RevenueDashboardViewModel: Failed to load metrics")
                        emit(RevenueDashboardUiState.Error(error.message ?: "Unknown Error"))
                    }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = RevenueDashboardUiState.Loading
            )
}

sealed class RevenueDashboardUiState {
    object Loading : RevenueDashboardUiState()
    data class Success(val metrics: RevenueMetrics) : RevenueDashboardUiState()
    data class Error(val message: String) : RevenueDashboardUiState()
}
