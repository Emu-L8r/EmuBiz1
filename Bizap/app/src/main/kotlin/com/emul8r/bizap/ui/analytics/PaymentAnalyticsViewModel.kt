package com.emul8r.bizap.ui.analytics

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.domain.model.InvoiceStatus
import com.emul8r.bizap.domain.repository.InvoiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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
                val invoices = invoiceRepository.getAllInvoicesWithItems().first()
                val statusBreakdown = invoices.groupingBy { it.status }.eachCount()

                val totalInvoices = invoices.size
                val paidCount = statusBreakdown[InvoiceStatus.PAID] ?: 0
                val overdueCount = statusBreakdown[InvoiceStatus.OVERDUE] ?: 0
                val dueSoonCount = statusBreakdown[InvoiceStatus.SENT] ?: 0
                val draftCount = statusBreakdown[InvoiceStatus.DRAFT] ?: 0

                val collectionRate = if (totalInvoices > 0) {
                    (paidCount.toFloat() / totalInvoices.toFloat()) * 100f
                } else {
                    0f
                }

                val daysOutstanding = if (totalInvoices > 0) {
                    ((overdueCount.toFloat() / totalInvoices.toFloat()) * 30).toInt()
                } else {
                    0
                }

                val paymentStatusBreakdown = mapOf(
                    "Paid" to paidCount,
                    "Due Soon" to dueSoonCount,
                    "Overdue" to overdueCount,
                    "Draft" to draftCount
                )

                val metrics = PaymentMetrics(
                    totalInvoices = totalInvoices,
                    paidInvoices = paidCount,
                    dueSoonCount = dueSoonCount,
                    overdueCount = overdueCount,
                    draftCount = draftCount,
                    collectionRate = collectionRate,
                    daysOutstanding = daysOutstanding,
                    paymentStatusBreakdown = paymentStatusBreakdown,
                    averagePaymentDays = 25
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
