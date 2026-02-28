package com.example.databaser.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.databaser.MyApplication
import com.example.databaser.data.Invoice
import com.example.databaser.data.InvoiceRepository
import com.example.databaser.data.InvoiceWithCustomerAndLineItems
import com.example.databaser.data.LineItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class InvoiceUiState(
    val invoices: List<InvoiceWithCustomerAndLineItems> = emptyList(),
    val quotes: List<InvoiceWithCustomerAndLineItems> = emptyList(),
    val isLoading: Boolean = false
)

class InvoiceViewModel(private val invoiceRepository: InvoiceRepository) : ViewModel() {

    val uiState: StateFlow<InvoiceUiState> = invoiceRepository.getAllInvoicesStream()
        .map { allDocs ->
            val sortedDocs = allDocs.sortedByDescending { it.invoice.id }
            InvoiceUiState(
                invoices = sortedDocs.filter { !it.invoice.isQuote },
                quotes = sortedDocs.filter { it.invoice.isQuote },
                isLoading = false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = InvoiceUiState(isLoading = true)
        )

    fun getInvoicesForCustomer(customerId: Int): Flow<List<InvoiceWithCustomerAndLineItems>> {
        return invoiceRepository.getInvoicesForCustomerStream(customerId).map { it.sortedByDescending { invoice -> invoice.invoice.id } }
    }

    fun getInvoiceById(invoiceId: Int): Flow<InvoiceWithCustomerAndLineItems?> {
        return invoiceRepository.getInvoiceStream(invoiceId)
    }

    suspend fun addInvoice(invoice: Invoice): Long {
        return withContext(Dispatchers.IO) {
            invoiceRepository.insertInvoice(invoice)
        }
    }

    suspend fun updateInvoice(invoice: Invoice) {
        invoiceRepository.updateInvoice(invoice)
    }

    fun deleteInvoice(invoice: Invoice) {
        viewModelScope.launch(Dispatchers.IO) {
            invoiceRepository.deleteInvoice(invoice)
        }
    }

    suspend fun createQuoteFromInvoice(invoice: Invoice, lineItems: List<LineItem>): Long {
        return withContext(Dispatchers.IO) {
            val quoteNumber = generateQuoteNumber()
            val quote = invoice.copy(id = 0, invoiceNumber = quoteNumber, isQuote = true, isSent = false, isPaid = false)
            val quoteId = invoiceRepository.insertInvoice(quote)
            lineItems.forEach { 
                invoiceRepository.insertLineItem(it.copy(id = 0, invoiceId = quoteId.toInt()))
            }
            quoteId
        }
    }
    
    fun markInvoiceAsSent(invoiceId: Int) {
        viewModelScope.launch {
            val invoiceWithItems = getInvoiceById(invoiceId).first()
            if (invoiceWithItems != null && !invoiceWithItems.invoice.isSent) {
                updateInvoice(invoiceWithItems.invoice.copy(isSent = true))
            }
        }
    }

    fun updateInvoiceStatus(invoiceId: Int, isPaid: Boolean, isSent: Boolean, isHidden: Boolean) {
        viewModelScope.launch {
            val invoiceWithItems = getInvoiceById(invoiceId).first()
            if (invoiceWithItems != null) {
                updateInvoice(invoiceWithItems.invoice.copy(isPaid = isPaid, isSent = isSent, isHidden = isHidden))
            }
        }
    }

    fun convertToInvoice(invoice: Invoice) {
        viewModelScope.launch {
            updateInvoice(invoice.copy(isQuote = false))
        }
    }

    private suspend fun generateQuoteNumber(): String {
        val count = invoiceRepository.getQuoteCount()
        val dateFormat = SimpleDateFormat("ddMMyyyy", Locale.getDefault())
        return "Q-${dateFormat.format(Date())}-${count + 1}"
    }

    suspend fun generateInvoiceNumber(): String {
        val startOfDayCalendar = Calendar.getInstance()
        startOfDayCalendar.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = startOfDayCalendar.timeInMillis

        val endOfDayCalendar = Calendar.getInstance()
        endOfDayCalendar.apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        val endOfDay = endOfDayCalendar.timeInMillis

        val count = invoiceRepository.getInvoiceCountForDate(startOfDay, endOfDay)
        val dateFormat = SimpleDateFormat("ddMMyyyy", Locale.getDefault())
        return "${dateFormat.format(Date())}-${count + 1}"
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MyApplication)
                val invoiceRepository = application.container.invoiceRepository
                InvoiceViewModel(invoiceRepository = invoiceRepository)
            }
        }
    }
}
