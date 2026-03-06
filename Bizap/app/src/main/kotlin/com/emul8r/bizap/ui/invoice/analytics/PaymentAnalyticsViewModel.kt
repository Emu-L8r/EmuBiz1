package com.emul8r.bizap.ui.invoice.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.domain.invoice.model.PaymentAnalyticsSummary
import com.emul8r.bizap.domain.invoice.usecase.GetPaymentAnalyticsUseCase
import com.emul8r.bizap.domain.repository.BusinessProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for Payment Analytics Dashboard.
 * Uses reactive StateFlow to auto-update when payment data changes.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PaymentAnalyticsViewModel @Inject constructor(
    private val getPaymentAnalyticsUseCase: GetPaymentAnalyticsUseCase,
    private val businessProfileRepository: BusinessProfileRepository
) : ViewModel() {

    val state: StateFlow<PaymentAnalyticsUiState> = businessProfileRepository.activeProfile
        .flatMapLatest { businessProfile ->
            getPaymentAnalyticsUseCase(businessProfile.id)
                .map { analytics ->
                    Timber.d("✅ PaymentAnalyticsViewModel: Analytics updated reactively")
                    PaymentAnalyticsUiState.Success(analytics) as PaymentAnalyticsUiState
                }
                .catch { error ->
                    Timber.e(error, "❌ PaymentAnalyticsViewModel: Error loading analytics")
                    emit(PaymentAnalyticsUiState.Error("Failed to load analytics: ${error.message}"))
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PaymentAnalyticsUiState.Loading
        )
}

sealed class PaymentAnalyticsUiState {
    object Loading : PaymentAnalyticsUiState()
    data class Success(val analytics: PaymentAnalyticsSummary) : PaymentAnalyticsUiState()
    data class Error(val message: String) : PaymentAnalyticsUiState()
}







