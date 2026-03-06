package com.emul8r.bizap.ui.invoice.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.domain.invoice.model.PaymentAnalyticsSummary
import com.emul8r.bizap.domain.invoice.usecase.GetPaymentAnalyticsUseCase
import com.emul8r.bizap.domain.repository.BusinessProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for Payment Analytics Dashboard.
 * Manages payment metrics, aging analysis, cash flow forecasts, and dunning notices.
 * ✅ FIXED: Now supports refresh when invoice data changes
 */
@HiltViewModel
class PaymentAnalyticsViewModel @Inject constructor(
    private val getPaymentAnalyticsUseCase: GetPaymentAnalyticsUseCase,
    private val businessProfileRepository: BusinessProfileRepository
) : ViewModel() {

    private val _state = MutableStateFlow<PaymentAnalyticsUiState>(
        PaymentAnalyticsUiState.Loading
    )
    val state: StateFlow<PaymentAnalyticsUiState> = _state.asStateFlow()

    init {
        loadPaymentAnalytics()
    }

    /**
     * Load payment analytics from the database.
     * ✅ IMPROVED: Made public so it can be called to refresh data
     */
    fun loadPaymentAnalytics() {
        viewModelScope.launch {
            try {
                Timber.d("📊 PaymentAnalyticsViewModel: Loading analytics")
                _state.value = PaymentAnalyticsUiState.Loading

                val businessId = businessProfileRepository.getActiveBusinessId()
                val analytics = getPaymentAnalyticsUseCase(businessId)

                Timber.d("✅ PaymentAnalyticsViewModel: Loaded analytics - Total invoices: ${analytics.totalInvoices}, Outstanding: ${analytics.totalOutstandingAmount}")
                _state.value = PaymentAnalyticsUiState.Success(analytics)
            } catch (e: Exception) {
                Timber.e(e, "❌ PaymentAnalyticsViewModel: Error loading analytics")
                _state.value = PaymentAnalyticsUiState.Error(
                    "Failed to load analytics: ${e.message}"
                )
            }
        }
    }

    /**
     * ✅ RENAMED: Made public and renamed for clarity
     * Call this to refresh analytics (e.g., after creating/editing invoices)
     */
    fun refreshAnalytics() {
        Timber.d("🔄 PaymentAnalyticsViewModel: Refreshing analytics after invoice operation")
        loadPaymentAnalytics()
    }
}

sealed class PaymentAnalyticsUiState {
    object Loading : PaymentAnalyticsUiState()
    data class Success(val analytics: PaymentAnalyticsSummary) : PaymentAnalyticsUiState()
    data class Error(val message: String) : PaymentAnalyticsUiState()
}







