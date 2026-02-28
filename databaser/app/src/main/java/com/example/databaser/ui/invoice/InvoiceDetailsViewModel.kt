package com.example.databaser.ui.invoice

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.databaser.R
import com.example.databaser.data.model.Invoice
import com.example.databaser.data.model.InvoiceLineItem
import com.example.databaser.data.repository.InvoiceRepository
import com.example.databaser.util.PdfGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class InvoiceDetailsUiState(
    val date: String = "",
    val header: String = "",
    val subHeader: String = "",
    val footer: String = "",
    val lineItems: List<InvoiceLineItem> = emptyList(),
    val total: Double = 0.0,
    val invoice: Invoice? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class InvoiceDetailsViewModel @Inject constructor(
    private val application: Application,
    private val invoiceRepository: InvoiceRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    sealed class Event {
        data class ShowToast(val message: String) : Event()
        object InvoiceSaved : Event()
    }

    private val _uiState = MutableStateFlow(InvoiceDetailsUiState())
    val uiState = _uiState.asStateFlow()

    private val invoiceId: Long? = savedStateHandle.get<Long>("invoiceId")
    private var tempIdCounter = -1L

    private val _event = MutableSharedFlow<Event>()
    val event = _event.asSharedFlow()

    private val _pdfFile = MutableSharedFlow<File>()
    val pdfFile = _pdfFile.asSharedFlow()

    private val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    init {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                if (invoiceId != null && invoiceId > 0) {
                    val invoice = withContext(Dispatchers.IO) {
                        invoiceRepository.getInvoiceStream(invoiceId).firstOrNull()
                    }

                    if (invoice != null) {
                        _uiState.update {
                            it.copy(
                                invoice = invoice,
                                date = Instant.ofEpochMilli(invoice.date).atZone(ZoneId.systemDefault()).toLocalDate().format(formatter),
                                header = invoice.header,
                                subHeader = invoice.subHeader,
                                footer = invoice.footer
                            )
                        }
                        invoiceRepository.getLineItemsStream(invoiceId)
                            .onEach { items -> updateLineItems(items) }
                            .launchIn(viewModelScope)
                    } else {
                        _uiState.update { it.copy(errorMessage = "Invoice not found.") }
                    }
                } else {
                    _uiState.update { it.copy(date = LocalDate.now().format(formatter)) }
                }
            } catch (e: Exception) {
                // It's a good practice to log the exception here to help with debugging
                _uiState.update { it.copy(errorMessage = "Failed to load invoice details.") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun updateLineItems(items: List<InvoiceLineItem>) {
        val total = items.sumOf { it.quantity * it.price }
        _uiState.update {
            it.copy(
                lineItems = items,
                total = total
            )
        }
    }

    fun onDateChange(newDate: String) {
        _uiState.update { it.copy(date = newDate) }
    }

    fun onHeaderChange(newHeader: String) {
        _uiState.update { it.copy(header = newHeader) }
    }

    fun onSubHeaderChange(newSubHeader: String) {
        _uiState.update { it.copy(subHeader = newSubHeader) }
    }

    fun onFooterChange(newFooter: String) {
        _uiState.update { it.copy(footer = newFooter) }
    }

    fun onLineItemDescriptionChange(itemId: Long, newDescription: String) {
        val updatedItems = _uiState.value.lineItems.map {
            if (it.id == itemId) it.copy(description = newDescription) else it
        }
        updateLineItems(updatedItems)
    }

    fun onLineItemQuantityChange(itemId: Long, newQuantity: String) {
        val updatedItems = _uiState.value.lineItems.map {
            if (it.id == itemId) it.copy(quantity = newQuantity.toIntOrNull() ?: 0) else it
        }
        updateLineItems(updatedItems)
    }

    fun onLineItemPriceChange(itemId: Long, newPrice: String) {
        val updatedItems = _uiState.value.lineItems.map {
            if (it.id == itemId) it.copy(price = newPrice.toDoubleOrNull() ?: 0.0) else it
        }
        updateLineItems(updatedItems)
    }

    fun saveInvoice(customerId: Long) {
        viewModelScope.launch {
            if (_uiState.value.date.isEmpty()) {
                _event.emit(Event.ShowToast(application.getString(R.string.please_fill_all_fields)))
                return@launch
            }
            val dateInMillis = LocalDate.parse(_uiState.value.date, formatter).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val invoiceToSave = _uiState.value.invoice?.copy(
                customerId = customerId,
                date = dateInMillis,
                header = _uiState.value.header,
                subHeader = _uiState.value.subHeader,
                footer = _uiState.value.footer
            ) ?: Invoice(customerId = customerId, date = dateInMillis, header = _uiState.value.header, subHeader = _uiState.value.subHeader, footer = _uiState.value.footer)

            withContext(Dispatchers.IO) {
                invoiceRepository.saveInvoice(invoiceToSave, _uiState.value.lineItems)
            }
            _event.emit(Event.InvoiceSaved)
        }
    }

    fun addLineItem() {
        val newItem = InvoiceLineItem(id = tempIdCounter--, invoiceId = invoiceId ?: 0, description = "", quantity = 1, price = 0.0)
        val updatedItems = _uiState.value.lineItems + newItem
        updateLineItems(updatedItems)
    }

    fun removeLineItem(lineItem: InvoiceLineItem) {
        viewModelScope.launch {
            if (lineItem.id > 0L) {
                withContext(Dispatchers.IO) {
                    invoiceRepository.deleteLineItem(lineItem.id)
                }
            } else {
                val updatedItems = _uiState.value.lineItems.filterNot { it.id == lineItem.id }
                updateLineItems(updatedItems)
            }
        }
    }

    fun generatePdf() {
        viewModelScope.launch {
            val headerString = application.getString(R.string.header)
            val subHeaderString = application.getString(R.string.sub_header)
            val footerString = application.getString(R.string.footer)

            val text = StringBuilder()
            text.append("$headerString: ${_uiState.value.header}\n")
            text.append("$subHeaderString: ${_uiState.value.subHeader}\n\n")
            text.append(_uiState.value.lineItems.joinToString("\n") { "${it.description} - Qty: ${it.quantity} - Price: ${it.price}" })
            text.append("\n\n$footerString: ${_uiState.value.footer}")

            val file = withContext(Dispatchers.IO) {
                PdfGenerator.generatePdf(application, text.toString())
            }
            file?.let {
                _pdfFile.emit(it)
            }
        }
    }
}
