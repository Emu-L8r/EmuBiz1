package com.emul8r.bizap.ui.analytics

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import com.emul8r.bizap.util.ContextBlockLogger
import com.emul8r.bizap.domain.model.InvoiceStatus
import com.emul8r.bizap.domain.repository.InvoiceRepository
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
 * Calculates and tracks payment-related metrics:
 * - Payment collection rate
 * - Days Sales Outstanding (DSO)
 * - Payment status breakdown
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
        val startMs = ContextBlockLogger.logStart("ANALYTICS", "Computing payment metrics")
        viewModelScope.launch {
            try {
                val invoices = invoiceRepository.getAllInvoicesWithItems().first()
                val totalInvoices = invoices.size
                val paidInvoices = invoices.filter { it.status == InvoiceStatus.PAID }
                val sentInvoices = invoices.filter { it.status == InvoiceStatus.SENT }
                val overdueInvoices = invoices.filter { it.status == InvoiceStatus.OVERDUE }
                val draftInvoices = invoices.filter { it.status == InvoiceStatus.DRAFT }

                val collectionRate = if (totalInvoices > 0) {
                    (paidInvoices.size.toFloat() / totalInvoices.toFloat() * 100f)
                } else {
                    0f
                }

                // Calculate DSO (Days Sales Outstanding)
                val now = System.currentTimeMillis()
                val outstandingInvoices = invoices.filter { it.status != InvoiceStatus.PAID }
                val daysOutstanding = if (outstandingInvoices.isNotEmpty()) {
                    val avgDaysOld = outstandingInvoices.map { invoice ->
                        try {
                            val invoiceDate = java.time.Instant.parse(invoice.dateCreated).toEpochMilli()
                            (now - invoiceDate) / (1000 * 60 * 60 * 24)  // Days
                        } catch (e: Exception) {
                            0L
                        }
                    }.average().toInt()
                    avgDaysOld
                } else {
                    0
                }

                val paymentStatusBreakdown = mapOf(
                    "Paid" to paidInvoices.size,
                    "Pending" to sentInvoices.size,
                    "Overdue" to overdueInvoices.size,
                    "Draft" to draftInvoices.size
                )

                val metrics = PaymentMetrics(
                    totalInvoices = totalInvoices,
                    paidInvoices = paidInvoices.size,
                    dueSoonCount = sentInvoices.size,
                    overdueCount = overdueInvoices.size,
                    draftCount = draftInvoices.size,
                    collectionRate = collectionRate,
                    daysOutstanding = daysOutstanding,
                    paymentStatusBreakdown = paymentStatusBreakdown,
                    averagePaymentDays = daysOutstanding
                )

                _paymentMetrics.value = PaymentMetricsState.Success(metrics)
                Timber.d("✅ Payment metrics loaded: collection=${metrics.collectionRate}%, DSO=${metrics.daysOutstanding}, total=$totalInvoices")
            } catch (e: Exception) {
                ContextBlockLogger.logFailure("ANALYTICS", startMs, "Payment metrics loading", e)
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
