package com.emul8r.bizap.ui.analytics

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.domain.repository.InvoiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for Payment Analytics.
 *
 * Tracks payment metrics and status:
 * - Collection rate
 * - Days Sales Outstanding (DSO)
 * - Payment status breakdown
 * - Invoice aging
 */
@HiltViewModel
class PaymentAnalyticsViewModel @Inject constructor(
    private val invoiceRepository: InvoiceRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val businessId = savedStateHandle.get<Long>("businessId") ?: 0L

    private val _paymentMetrics = MutableStateFlow<PaymentMetricsState>(PaymentMetricsState.Loading)
    val paymentMetrics: StateFlow<PaymentMetricsState> = _paymentMetrics.asStateFlow()

    init {
        loadPaymentMetrics()
    }

    private fun loadPaymentMetrics() {
        viewModelScope.launch {
            try {
                // For MVP: Return mock payment data
                // TODO: Wire to actual invoice queries in future
                val mockPaymentStatus = mapOf(
                    "Paid" to 45,
                    "Due Soon" to 12,
                    "Overdue" to 5,
                    "Draft" to 8
                )

                val totalInvoices = mockPaymentStatus.values.sum()
                val paidCount = mockPaymentStatus["Paid"] ?: 0

                val metrics = PaymentMetrics(
                    totalInvoices = totalInvoices,
                    paidInvoices = paidCount,
                    dueSoonCount = mockPaymentStatus["Due Soon"] ?: 0,
                    overdueCount = mockPaymentStatus["Overdue"] ?: 0,
                    draftCount = mockPaymentStatus["Draft"] ?: 0,
                    collectionRate = if (totalInvoices > 0) {
                        (paidCount.toFloat() / totalInvoices.toFloat()) * 100f
                    } else {
                        0f
                    },
                    daysOutstanding = 18,  // Mock DSO
                    paymentStatusBreakdown = mockPaymentStatus,
                    averagePaymentDays = 25
                )

                _paymentMetrics.value = PaymentMetricsState.Success(metrics)
                Timber.d("✅ Payment metrics loaded: collection=${metrics.collectionRate}%, DSO=${metrics.daysOutstanding}")
            } catch (e: Exception) {
                Timber.e(e, "Failed to load payment metrics")
                _paymentMetrics.value = PaymentMetricsState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

sealed class PaymentMetricsState {
    object Loading : PaymentMetricsState()
    data class Success(val metrics: PaymentMetrics) : PaymentMetricsState()
    data class Error(val message: String) : PaymentMetricsState()
}

data class PaymentMetrics(
    val totalInvoices: Int = 0,
    val paidInvoices: Int = 0,
    val dueSoonCount: Int = 0,
    val overdueCount: Int = 0,
    val draftCount: Int = 0,
    val collectionRate: Float = 0f,  // Percentage
    val daysOutstanding: Int = 0,    // DSO
    val paymentStatusBreakdown: Map<String, Int> = emptyMap(),
    val averagePaymentDays: Int = 0
)

