package com.emul8r.bizap.ui.invoice.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.domain.invoice.model.PaymentAnalyticsSummary
import com.emul8r.bizap.domain.invoice.model.OutstandingByAging
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
 */
@HiltViewModel
class PaymentAnalyticsViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow<PaymentAnalyticsUiState>(
        PaymentAnalyticsUiState.Loading
    )
    val state: StateFlow<PaymentAnalyticsUiState> = _state.asStateFlow()

    init {
        loadPaymentAnalytics()
    }

    private fun loadPaymentAnalytics() {
        viewModelScope.launch {
            try {
                Timber.d("PaymentAnalyticsViewModel: Loading analytics")
                _state.value = PaymentAnalyticsUiState.Loading

                // Mock analytics data for demo purposes
                val mockAnalytics = PaymentAnalyticsSummary(
                    businessProfileId = 1L,
                    totalInvoices = 42,
                    paidInvoices = 38,
                    unpaidInvoices = 4,
                    overdueInvoices = 2,
                    totalInvoiceAmount = 145230.50,
                    totalPaidAmount = 132780.00,
                    totalOutstandingAmount = 12450.75,
                    collectionRate = 90.5,
                    averagePaymentTime = 15.0,
                    outstandingByAging = OutstandingByAging(
                        current = 5000.0,
                        past30 = 4200.0,
                        past60 = 2100.0,
                        past90 = 1150.75,
                        totalOutstanding = 12450.75
                    ),
                    cashFlowForecast = emptyList(),
                    riskInvoices = emptyList()
                )

                Timber.d("PaymentAnalyticsViewModel: Loaded analytics - Total: ${mockAnalytics.totalInvoices}")
                _state.value = PaymentAnalyticsUiState.Success(mockAnalytics)
            } catch (e: Exception) {
                Timber.e(e, "PaymentAnalyticsViewModel: Unexpected error")
                _state.value = PaymentAnalyticsUiState.Error(
                    "An unexpected error occurred: ${e.message}"
                )
            }
        }
    }

    fun retryLoadAnalytics() {
        loadPaymentAnalytics()
    }
}

sealed class PaymentAnalyticsUiState {
    object Loading : PaymentAnalyticsUiState()
    data class Success(val analytics: PaymentAnalyticsSummary) : PaymentAnalyticsUiState()
    data class Error(val message: String) : PaymentAnalyticsUiState()
}







