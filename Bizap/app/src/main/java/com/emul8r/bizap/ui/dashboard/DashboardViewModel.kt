package com.emul8r.bizap.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.data.repository.gui2.BusinessContextRepositoryV2
import com.emul8r.bizap.data.repository.gui2.RevenueRepositoryV2
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import timber.log.Timber
import javax.inject.Inject

sealed class DashboardRevenueState {
    object Loading : DashboardRevenueState()
    data class Success(val metrics: RevenueMetricsV2) : DashboardRevenueState()
    data class Error(val message: String) : DashboardRevenueState()
}

/**
 * ViewModel for the GUI1 Dashboard screen.
 * Uses the same V2 repositories as GUI2 to ensure data consistency
 * between both GUIs.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val revenueRepository: RevenueRepositoryV2,
    private val businessContextRepository: BusinessContextRepositoryV2
) : ViewModel() {

    val revenueState: StateFlow<DashboardRevenueState> =
        businessContextRepository.observeActiveBusinessId()
            .flatMapLatest { businessId ->
                revenueRepository.observeRevenueMetrics(businessId)
                    .map { result ->
                        result.fold(
                            onSuccess = { metrics ->
                                Timber.d("DashboardViewModel: Revenue metrics updated for business $businessId")
                                DashboardRevenueState.Success(metrics)
                            },
                            onFailure = { error ->
                                Timber.e(error, "DashboardViewModel: Failed to load revenue metrics")
                                DashboardRevenueState.Error(error.message ?: "Unknown error")
                            }
                        )
                    }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = DashboardRevenueState.Loading
            )
}
