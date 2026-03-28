package com.emul8r.bizap.ui.gui2.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.data.repository.gui2.BusinessContextRepositoryV2
import com.emul8r.bizap.data.repository.gui2.CustomerAnalyticsRepositoryV2
import com.emul8r.bizap.domain.model.gui2.CustomerMetricsV2
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class CustomerAnalyticsViewModelV2 @Inject constructor(
    businessContextRepository: BusinessContextRepositoryV2,
    customerRepository: CustomerAnalyticsRepositoryV2
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<CustomerAnalyticsUiStateV2> =
        businessContextRepository.observeActiveBusinessId()
            .flatMapLatest { businessId ->
                Timber.d("CustomerAnalyticsViewModelV2: observing businessId=$businessId")
                customerRepository.observeCustomerMetrics(businessId)
                    .map { result ->
                        result.fold(
                            onSuccess = { metrics ->
                                Timber.d("CustomerAnalyticsViewModelV2: metrics updated for businessId=$businessId - total=${metrics.totalCustomers}")
                                CustomerAnalyticsUiStateV2.Success(metrics)
                            },
                            onFailure = { error ->
                                Timber.e(error, "CustomerAnalyticsViewModelV2: error")
                                CustomerAnalyticsUiStateV2.Error(error.message ?: "Unknown error")
                            }
                        )
                    }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = CustomerAnalyticsUiStateV2.Loading
            )
}

sealed class CustomerAnalyticsUiStateV2 {
    object Loading : CustomerAnalyticsUiStateV2()
    data class Success(val metrics: CustomerMetricsV2) : CustomerAnalyticsUiStateV2()
    data class Error(val message: String) : CustomerAnalyticsUiStateV2()
}

