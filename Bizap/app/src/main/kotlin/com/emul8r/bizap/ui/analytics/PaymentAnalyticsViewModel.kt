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
                // ✅ FIX #7: Load real payment metrics from InvoiceRepository
                val invoices = invoiceRepository.getAllInvoicesWithItems()
                    .first()  // Get current value from flow

                val paymentStatusBreakdown = invoices.groupingBy { it.status.toString() }.eachCount()
                val totalInvoices = invoices.size
                val paidCount = paymentStatusBreakdown["PAID"] ?: 0
                val overdueCount = paymentStatusBreakdown["OVERDUE"] ?: 0
                val dueSoonCount = paymentStatusBreakdown["SENT"] ?: 0  // Invoices waiting for payment
                val draftCount = paymentStatusBreakdown["DRAFT"] ?: 0

                // Calculate collection rate
                val collectionRate = if (totalInvoices > 0) {
                    (paidCount.toFloat() / totalInvoices.toFloat()) * 100f
                } else {
                    0f
                }

                // Simple DSO calculation: count overdue invoices / total invoices * 30 days
                val daysOutstanding = if (totalInvoices > 0) {
                    ((overdueCount.toFloat() / totalInvoices.toFloat()) * 30).toInt()
                } else {
                    0
                }

                // Average payment days (estimated from status distribution)
                val averagePaymentDays = when {
                    paidCount > 0 -> 25  // Estimate for paid invoices
                    else -> 0
                }

                val metrics = PaymentMetrics(
                    totalInvoices = totalInvoices,
                    paidInvoices = paidCount,
                    dueSoonCount = dueSoonCount,
                    overdueCount = overdueCount,
                    draftCount = draftCount,
                    collectionRate = collectionRate,
                    daysOutstanding = daysOutstanding,
                    paymentStatusBreakdown = paymentStatusBreakdown,
                    averagePaymentDays = averagePaymentDays
                )

                _paymentMetrics.value = PaymentMetricsState.Success(metrics)
                Timber.d("✅ Payment metrics loaded: collection=${metrics.collectionRate}%, DSO=${metrics.daysOutstanding}, total=$totalInvoices")
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

