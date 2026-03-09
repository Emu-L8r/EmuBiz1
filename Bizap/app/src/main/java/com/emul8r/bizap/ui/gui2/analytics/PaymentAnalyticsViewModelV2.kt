package com.emul8r.bizap.ui.gui2.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.data.repository.gui2.BusinessContextRepositoryV2
import com.emul8r.bizap.data.repository.gui2.PaymentAnalyticsRepositoryV2
import com.emul8r.bizap.domain.model.gui2.PaymentMetricsV2
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PaymentAnalyticsViewModelV2 @Inject constructor(
    businessContextRepository: BusinessContextRepositoryV2,
    paymentRepository: PaymentAnalyticsRepositoryV2
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<PaymentAnalyticsUiStateV2> =
        businessContextRepository.observeActiveBusinessId()
            .flatMapLatest { businessId ->
                Timber.d("PaymentAnalyticsViewModelV2: observing businessId=$businessId")
                paymentRepository.observePaymentMetrics(businessId)
                    .map<PaymentMetricsV2, PaymentAnalyticsUiStateV2> { metrics ->
                        Timber.d("PaymentAnalyticsViewModelV2: metrics updated for businessId=$businessId")
                        PaymentAnalyticsUiStateV2.Success(metrics)
                    }
                    .catch { error ->
                        Timber.e(error, "PaymentAnalyticsViewModelV2: error")
                        emit(PaymentAnalyticsUiStateV2.Error(error.message ?: "Unknown error"))
                    }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = PaymentAnalyticsUiStateV2.Loading
            )
}

sealed class PaymentAnalyticsUiStateV2 {
    object Loading : PaymentAnalyticsUiStateV2()
    data class Success(val metrics: PaymentMetricsV2) : PaymentAnalyticsUiStateV2()
    data class Error(val message: String) : PaymentAnalyticsUiStateV2()
}
