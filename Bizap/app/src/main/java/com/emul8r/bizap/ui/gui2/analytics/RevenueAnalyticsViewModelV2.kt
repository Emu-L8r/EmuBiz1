package com.emul8r.bizap.ui.gui2.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.data.repository.gui2.BusinessContextRepositoryV2
import com.emul8r.bizap.data.repository.gui2.RevenueRepositoryV2
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class RevenueAnalyticsViewModelV2 @Inject constructor(
    businessContextRepository: BusinessContextRepositoryV2,
    revenueRepository: RevenueRepositoryV2
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<RevenueAnalyticsUiStateV2> =
        businessContextRepository.observeActiveBusinessId()
            .flatMapLatest { businessId ->
                Timber.d("RevenueAnalyticsViewModelV2: observing businessId=$businessId")
                revenueRepository.observeRevenueMetrics(businessId)
                    .map { result ->
                        result.fold(
                            onSuccess = { metrics ->
                                Timber.d("RevenueAnalyticsViewModelV2: metrics updated for businessId=$businessId")
                                RevenueAnalyticsUiStateV2.Success(metrics)
                            },
                            onFailure = { error ->
                                Timber.e(error, "RevenueAnalyticsViewModelV2: error")
                                RevenueAnalyticsUiStateV2.Error(error.message ?: "Unknown error")
                            }
                        )
                    }
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
