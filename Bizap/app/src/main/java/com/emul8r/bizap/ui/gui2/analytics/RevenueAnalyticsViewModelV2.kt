package com.emul8r.bizap.ui.gui2.analytics

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.emul8r.bizap.data.repository.gui2.RevenueRepositoryV2
import com.emul8r.bizap.domain.model.gui2.RevenueMetricsV2
import com.emul8r.bizap.ui.gui2.navigation.ScreenV2
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class RevenueAnalyticsViewModelV2 @Inject constructor(
    savedStateHandle: SavedStateHandle,
    revenueRepository: RevenueRepositoryV2
) : ViewModel() {

    private val route: ScreenV2.RevenueAnalytics = savedStateHandle.toRoute()
    val businessId: Long = route.businessId

    val uiState: StateFlow<RevenueAnalyticsUiStateV2> =
        revenueRepository.observeRevenueMetrics(businessId)
            .map<RevenueMetricsV2, RevenueAnalyticsUiStateV2> { metrics ->
                Timber.d("RevenueAnalyticsViewModelV2: metrics updated for businessId=$businessId")
                RevenueAnalyticsUiStateV2.Success(metrics)
            }
            .catch { error ->
                Timber.e(error, "RevenueAnalyticsViewModelV2: error")
                emit(RevenueAnalyticsUiStateV2.Error(error.message ?: "Unknown error"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = RevenueAnalyticsUiStateV2.Loading
            )
}

sealed class RevenueAnalyticsUiStateV2 {
    object Loading : RevenueAnalyticsUiStateV2()
    data class Success(val metrics: RevenueMetricsV2) : RevenueAnalyticsUiStateV2()
    data class Error(val message: String) : RevenueAnalyticsUiStateV2()
}
