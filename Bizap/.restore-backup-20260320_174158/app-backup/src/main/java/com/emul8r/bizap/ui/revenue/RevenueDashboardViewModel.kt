package com.emul8r.bizap.ui.revenue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.domain.model.gui2.RevenueMetricsV2
import com.emul8r.bizap.domain.repository.BusinessProfileRepository
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

    private val _overrideBusinessId = MutableStateFlow<Long?>(null)

    val uiState: StateFlow<RevenueDashboardUiState> = combine(
        businessProfileRepository.activeProfile,
        _overrideBusinessId
    ) { profile, override ->
        override ?: profile.id
    }
        .flatMapLatest { businessId ->
            getRevenueMetricsUseCase(businessId)
                .map { result ->
                    result.fold(
                        onSuccess = { metrics ->
                            Timber.d("✅ RevenueDashboardViewModel: Metrics updated reactively")
                            RevenueDashboardUiState.Success(metrics) as RevenueDashboardUiState
                        },
                        onFailure = { error ->
                            Timber.e(error, "❌ RevenueDashboardViewModel: Failed to load metrics")
                            RevenueDashboardUiState.Error(error.message ?: "Unknown Error")
                        }
                    )
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

    // ✅ FIX 3: Accept businessId from navigation
    fun setBusinessId(businessId: Long?) {
        if (businessId != null) {
            _overrideBusinessId.value = businessId
            Timber.d("📍 RevenueDashboardViewModel: Using business context $businessId from navigation")
        }
    }
}

sealed class RevenueDashboardUiState {
    object Loading : RevenueDashboardUiState()
    data class Success(val metrics: RevenueMetricsV2) : RevenueDashboardUiState()
    data class Error(val message: String) : RevenueDashboardUiState()
}
