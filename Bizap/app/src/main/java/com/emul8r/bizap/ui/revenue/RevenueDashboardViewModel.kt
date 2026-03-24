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

/**
 * Manages revenue metrics dashboard state for GUI1 and GUI2.
 *
 * **Purpose:**
 * Displays real-time revenue metrics: MTD (month-to-date) revenue, outstanding, overdue amounts.
 * Works across multiple businesses with reactive updates when context switches.
 *
 * **Responsibilities:**
 * - Load revenue metrics using GetRevenueMetricsUseCase
 * - Auto-update when business context changes
 * - Support business override (for testing/debugging)
 * - Track loading/error states
 *
 * **Reactive Updates:**
 * When user switches business, metrics automatically recalculate via flatMapLatest.
 * UI observes stateFlow and recomposes with new data.
 *
 * @param getRevenueMetricsUseCase Use case for metric calculations
 * @param businessProfileRepository Business context
 */
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
    /**
     * Override the business context for metrics calculation.
     *
     * **Behavior:**
     * - When set, uses this business ID instead of active profile
     * - Triggers re-calculation of all metrics
     * - Useful for debugging or UI state management
     *
     * **Example:**
     * ```kotlin
     * viewModel.setBusinessId(42)  // Show metrics for business 42
     * ```
     *
     * @param businessId Business ID to use, or null to revert to active profile
     */
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
