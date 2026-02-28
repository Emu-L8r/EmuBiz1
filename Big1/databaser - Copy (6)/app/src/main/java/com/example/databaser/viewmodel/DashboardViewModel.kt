package com.example.databaser.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.databaser.MyApplication
import com.example.databaser.data.InvoiceRepository
import com.example.databaser.data.InvoiceWithCustomerAndLineItems
import com.github.mikephil.charting.data.BarEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import java.util.Calendar

data class DashboardUiState(
    val unsentInvoicesTotal: Double = 0.0,
    val sentInvoicesTotal: Double = 0.0,
    val paidInvoicesTotal: Double = 0.0,
    val unsentQuotesTotal: Double = 0.0,
    val sentQuotesTotal: Double = 0.0,
    val unsentInvoices: List<InvoiceWithCustomerAndLineItems> = emptyList(),
    val sentInvoices: List<InvoiceWithCustomerAndLineItems> = emptyList(),
    val paidInvoices: List<InvoiceWithCustomerAndLineItems> = emptyList(),
    val invoiceStatusEntries: Map<String, List<BarEntry>> = emptyMap(),
    val unsentQuotes: List<InvoiceWithCustomerAndLineItems> = emptyList(),
    val sentQuotes: List<InvoiceWithCustomerAndLineItems> = emptyList(),
    val isLoading: Boolean = false
)

class DashboardViewModel(invoiceRepository: InvoiceRepository) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = invoiceRepository.getAllInvoicesStream().map { documents ->
        withContext(Dispatchers.Default) {
            val unsentInvoices = documents
                .filter { !it.invoice.isQuote && !it.invoice.isSent }
                .sortedByDescending { it.invoice.date }
            val sentInvoices = documents
                .filter { !it.invoice.isQuote && it.invoice.isSent && !it.invoice.isPaid }
                .sortedByDescending { it.invoice.date }
            val paidInvoices = documents
                .filter { !it.invoice.isQuote && it.invoice.isPaid }
                .sortedByDescending { it.invoice.date }
            val unsentQuotes = documents
                .filter { it.invoice.isQuote && !it.invoice.isSent }
                .sortedByDescending { it.invoice.date }
            val sentQuotes = documents
                .filter { it.invoice.isQuote && it.invoice.isSent }
                .sortedByDescending { it.invoice.date }

            val unsentInvoicesTotal = unsentInvoices.sumOf { it.lineItems.sumOf { item -> item.quantity * item.unitPrice } }
            val sentInvoicesTotal = sentInvoices.sumOf { it.lineItems.sumOf { item -> item.quantity * item.unitPrice } }
            val paidInvoicesTotal = paidInvoices.sumOf { it.lineItems.sumOf { item -> item.quantity * item.unitPrice } }
            val unsentQuotesTotal = unsentQuotes.sumOf { it.lineItems.sumOf { item -> item.quantity * item.unitPrice } }
            val sentQuotesTotal = sentQuotes.sumOf { it.lineItems.sumOf { item -> item.quantity * item.unitPrice } }

            val allInvoices = unsentInvoices + sentInvoices + paidInvoices

            val groupedInvoices = allInvoices.groupBy {
                val cal = Calendar.getInstance()
                cal.timeInMillis = it.invoice.date
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }

            val dates = groupedInvoices.keys.sorted()

            val paidEntries = dates.mapIndexed { index, date ->
                val count = groupedInvoices[date]?.count { it.invoice.isPaid } ?: 0
                BarEntry(index.toFloat(), count.toFloat())
            }
            val sentEntries = dates.mapIndexed { index, date ->
                val count = groupedInvoices[date]?.count { it.invoice.isSent && !it.invoice.isPaid } ?: 0
                BarEntry(index.toFloat(), count.toFloat())
            }
            val unsentEntries = dates.mapIndexed { index, date ->
                val count = groupedInvoices[date]?.count { !it.invoice.isSent } ?: 0
                BarEntry(index.toFloat(), count.toFloat())
            }

            val invoiceStatusEntries = mapOf(
                "Paid" to paidEntries,
                "Sent" to sentEntries,
                "Unsent" to unsentEntries,
                "dates" to dates.mapIndexed { index, _ -> BarEntry(index.toFloat(), 0f) }
            )

            DashboardUiState(
                unsentInvoicesTotal = unsentInvoicesTotal,
                sentInvoicesTotal = sentInvoicesTotal,
                paidInvoicesTotal = paidInvoicesTotal,
                unsentQuotesTotal = unsentQuotesTotal,
                sentQuotesTotal = sentQuotesTotal,
                unsentInvoices = unsentInvoices,
                sentInvoices = sentInvoices,
                paidInvoices = paidInvoices,
                invoiceStatusEntries = invoiceStatusEntries,
                unsentQuotes = unsentQuotes,
                sentQuotes = sentQuotes,
                isLoading = false
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState(isLoading = true))

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MyApplication)
                val invoiceRepository = application.container.invoiceRepository
                DashboardViewModel(invoiceRepository = invoiceRepository)
            }
        }
    }
}
