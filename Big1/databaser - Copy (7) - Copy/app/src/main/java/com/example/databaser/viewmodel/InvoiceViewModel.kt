package com.example.databaser.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.databaser.data.BusinessInfo
import com.example.databaser.data.Invoice
import com.example.databaser.data.InvoiceRepository
import com.example.databaser.data.InvoiceWithCustomerAndLineItems
import com.example.databaser.data.LineItem
import com.example.databaser.utils.PdfGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class InvoiceUiState(
    val invoices: List<InvoiceWithCustomerAndLineItems> = emptyList(),
    val quotes: List<InvoiceWithCustomerAndLineItems> = emptyList(),
    val isLoading: Boolean = false,
)

@HiltViewModel
class InvoiceViewModel @Inject constructor(private val invoiceRepository: InvoiceRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _photoUris = MutableStateFlow<List<Uri>>(emptyList())
    val photoUris: StateFlow<List<Uri>> = _photoUris.asStateFlow()
    
    private val _header = MutableStateFlow("")
    val header: StateFlow<String> = _header.asStateFlow()

    private val _subHeader = MutableStateFlow("")
    val subHeader: StateFlow<String> = _subHeader.asStateFlow()
    
    private val _footer = MutableStateFlow("")
    val footer: StateFlow<String> = _footer.asStateFlow()
    
    private val _pdfGenerated = Channel<Uri?>()
    val pdfGenerated = _pdfGenerated.receiveAsFlow()
    
    private val _conversionResult = MutableSharedFlow<Boolean>()
    val conversionResult: SharedFlow<Boolean> = _conversionResult.asSharedFlow()

    val uiState: StateFlow<InvoiceUiState> = 
        invoiceRepository.getAllInvoicesStream().combine(_searchQuery) { allDocs, query ->
            val sortedDocs = allDocs.sortedByDescending { it.invoice.id }
            val filteredDocs = if (query.isBlank()) {
                sortedDocs
            } else {
                sortedDocs.filter { 
                    it.customer.name.contains(query, ignoreCase = true) || 
                    it.invoice.invoiceNumber.contains(query, ignoreCase = true) 
                }
            }
            InvoiceUiState(
                invoices = filteredDocs.filter { !it.invoice.isQuote },
                quotes = filteredDocs.filter { it.invoice.isQuote },
                isLoading = false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = InvoiceUiState(isLoading = true)
        )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun getInvoicesForCustomer(customerId: Int): Flow<List<InvoiceWithCustomerAndLineItems>> {
        return invoiceRepository.getInvoicesForCustomerStream(customerId).map { invoices ->
            invoices.sortedByDescending { invoice -> invoice.invoice.id }
        }
    }

    fun getInvoiceById(invoiceId: Int): Flow<InvoiceWithCustomerAndLineItems?> {
        return invoiceRepository.getInvoiceStream(invoiceId)
    }

    fun setPhotoUris(uris: List<Uri>) {
        _photoUris.value = uris
    }

    fun addPhotoUri(uri: Uri) {
        _photoUris.value += uri
    }

    fun removePhotoUri(uri: Uri) {
        _photoUris.value -= uri
    }
    
    fun setHeader(header: String){
        _header.value = header
    }
    
    fun setSubHeader(subHeader: String){
        _subHeader.value = subHeader
    }
    
    fun setFooter(footer: String){
        _footer.value = footer
    }

    fun refreshInvoice(invoiceId: Int) {
        viewModelScope.launch {
            invoiceRepository.getInvoiceStream(invoiceId).first()
        }
    }

    suspend fun addInvoice(invoice: Invoice): Long {
        return withContext(Dispatchers.IO) {
            invoiceRepository.insertInvoice(invoice)
        }
    }

    suspend fun updateInvoice(invoice: Invoice) {
        invoiceRepository.updateInvoice(invoice)
    }
    
    suspend fun addInvoiceWithLineItems(invoice: Invoice, lineItems: List<LineItem>): Long {
        val newInvoiceId = addInvoice(invoice)
        lineItems.forEach {
            invoiceRepository.insertLineItem(it.copy(invoiceId = newInvoiceId.toInt()))
        }
        return newInvoiceId
    }

    suspend fun updateInvoiceWithLineItems(invoice: Invoice, lineItems: List<LineItem>) {
        updateInvoice(invoice)
        invoiceRepository.deleteLineItemsByInvoiceId(invoice.id)
        lineItems.forEach {
            invoiceRepository.insertLineItem(it.copy(invoiceId = invoice.id))
        }
    }

    fun deleteInvoice(invoice: Invoice) {
        viewModelScope.launch(Dispatchers.IO) {
            invoiceRepository.deleteInvoice(invoice)
        }
    }

    suspend fun createQuoteFromInvoice(invoice: Invoice, lineItems: List<LineItem>, customerName: String): Long {
        return withContext(Dispatchers.IO) {
            val quoteNumber = generateQuoteNumber(customerName)
            val quote = invoice.copy(id = 0, invoiceNumber = quoteNumber, isQuote = true, isSent = false, isPaid = false)
            val quoteId = invoiceRepository.insertInvoice(quote)
            lineItems.forEach {
                invoiceRepository.insertLineItem(it.copy(id = 0, invoiceId = quoteId.toInt()))
            }
            quoteId
        }
    }

    fun createQuoteFromInvoice(invoiceId: Int) {
        viewModelScope.launch {
            val invoiceWithItems = getInvoiceById(invoiceId).first()
            if (invoiceWithItems != null) {
                createQuoteFromInvoice(invoiceWithItems.invoice, invoiceWithItems.lineItems, invoiceWithItems.customer.name)
            }
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
            try {
                updateInvoice(invoice.copy(isQuote = false))
                _conversionResult.emit(true)
            } catch (_: Exception) {
                _conversionResult.emit(false)
            }
        }
    }
    
    private fun getSafeCustomerName(customerName: String): String {
        return customerName.trim().replace(Regex("[^a-zA-Z0-9]"), "").take(4).uppercase()
    }

    suspend fun generateQuoteNumber(customerName: String): String {
        val count = invoiceRepository.getQuoteCount()
        val dateFormat = SimpleDateFormat("ddMMyyyy", Locale.getDefault())
        val safeCustomerName = getSafeCustomerName(customerName)
        return "Q-$safeCustomerName-${dateFormat.format(Date())}-${count + 1}"
    }

    suspend fun generateInvoiceNumber(customerName: String): String {
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
        val safeCustomerName = getSafeCustomerName(customerName)
        return "$safeCustomerName-${dateFormat.format(Date())}-${count + 1}"
    }

    fun generatePdf(context: Context, invoice: InvoiceWithCustomerAndLineItems, customerName: String, businessInfo: BusinessInfo, isQuote: Boolean, header: String, subHeader: String, footer: String, photoUris: List<Uri>) {
        viewModelScope.launch(Dispatchers.IO) {
            val uri = PdfGenerator.createInvoicePdf(context, invoice, customerName, businessInfo, isQuote, this@InvoiceViewModel, header, subHeader, footer, photoUris)
            _pdfGenerated.send(uri)
        }
    }
}
