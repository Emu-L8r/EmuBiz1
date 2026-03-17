package com.emul8r.bizap.ui.gui2.analytics

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.emul8r.bizap.data.repository.gui2.RiskAnalyticsRepositoryV2
import com.emul8r.bizap.ui.gui2.navigation.ScreenV2
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class RiskAnalyticsViewModelV2 @Inject constructor(
    savedStateHandle: SavedStateHandle,
    riskRepository: RiskAnalyticsRepositoryV2
) : ViewModel() {

    private val route: ScreenV2.RiskAnalytics = savedStateHandle.toRoute()
    val businessId: Long = route.businessId

    val uiState: StateFlow<RiskAnalyticsUiStateV2> =
        riskRepository.observeRiskMetrics(businessId)
            .map { result ->
                result.fold(
                    onSuccess = { metrics ->
                        Timber.d("RiskAnalyticsViewModelV2: metrics updated for businessId=$businessId")
                        RiskAnalyticsUiStateV2.Success(metrics)
                    },
                    onFailure = { error ->
                        Timber.e(error, "RiskAnalyticsViewModelV2: error")
                        RiskAnalyticsUiStateV2.Error(error.message ?: "Unknown error")
                    }
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = RiskAnalyticsUiStateV2.Loading
            )
}

sealed class RiskAnalyticsUiStateV2 {
    object Loading : RiskAnalyticsUiStateV2()
    data class Success(val metrics: RiskMetricsV2) : RiskAnalyticsUiStateV2()
    data class Error(val message: String) : RiskAnalyticsUiStateV2()
}
