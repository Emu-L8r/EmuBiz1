package com.emul8r.bizap.ui.invoices

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.data.repository.BusinessProfileRepository
import com.emul8r.bizap.data.service.InvoicePdfService
import com.emul8r.bizap.domain.model.Customer
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.repository.CustomerRepository
import com.emul8r.bizap.domain.repository.InvoiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

sealed class NavigationEvent {
    object BackToInvoiceDetail : NavigationEvent()
    data class ShowError(val message: String) : NavigationEvent()
}

sealed interface EditInvoiceUiState {
    data object Loading : EditInvoiceUiState
    data class Success(
        val invoice: Invoice,
        val customers: List<Customer>
    ) : EditInvoiceUiState
    data class Error(val message: String) : EditInvoiceUiState
}

@HiltViewModel
class EditInvoiceViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val invoiceRepository: InvoiceRepository,
    private val customerRepository: CustomerRepository,
    private val invoicePdfService: InvoicePdfService,
    private val businessProfileRepository: BusinessProfileRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val invoiceId: Long = checkNotNull(savedStateHandle["invoiceId"])

    private val _editState = MutableStateFlow<Invoice?>(null)
    private val _isSaving = MutableStateFlow(false)
    val isSaving = _isSaving.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    val uiState: StateFlow<EditInvoiceUiState> = combine(
        invoiceRepository.getInvoiceWithItemsById(invoiceId),
        customerRepository.getAllCustomers(),
        _editState
    ) { params ->
        val invoice = params[0] as? Invoice
        val customers = params[1] as List<Customer>
        val editingInvoice = params[2] as? Invoice

        if (invoice == null && editingInvoice == null) {
            EditInvoiceUiState.Error("Invoice not found")
        } else {
            EditInvoiceUiState.Success(
                invoice = editingInvoice ?: invoice!!,
                customers = customers
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = EditInvoiceUiState.Loading
    )

    fun addLineItem() {
        val currentState = uiState.value
        if (currentState is EditInvoiceUiState.Success) {
            val currentInvoice = currentState.invoice
            val updatedInvoice = currentInvoice.copy(
                items = currentInvoice.items + com.emul8r.bizap.domain.model.LineItem(
                    id = 0,
                    description = "",
                    quantity = 1.0,
                    unitPrice = 0L
                )
            )
            _editState.update { updatedInvoice }
        }
    }

    fun removeLineItem(itemId: Long?) {
        val currentState = uiState.value
        if (currentState is EditInvoiceUiState.Success) {
            val currentInvoice = currentState.invoice
            val updatedInvoice = currentInvoice.copy(
                items = currentInvoice.items.filter { it.id != itemId }
            )
            _editState.update { updatedInvoice }
        }
    }

    fun updateLineItem(id: Long?, description: String, quantity: Double, unitPrice: Long) {
        val currentState = uiState.value
        if (currentState is EditInvoiceUiState.Success) {
            val currentInvoice = currentState.invoice
            val updatedInvoice = currentInvoice.copy(
                items = currentInvoice.items.map { item ->
                    if (item.id == id) {
                        item.copy(description = description, quantity = quantity, unitPrice = unitPrice)
                    } else {
                        item
                    }
                }
            )
            _editState.update { updatedInvoice }
        }
    }

    fun onHeaderChange(header: String) = _editState.update { it?.copy(header = header) }
    fun onSubheaderChange(subheader: String) = _editState.update { it?.copy(subheader = subheader) }
    fun onNotesChange(notes: String) = _editState.update { it?.copy(notes = notes) }
    fun onFooterChange(footer: String) = _editState.update { it?.copy(footer = footer) }

    fun saveInvoice() {
        viewModelScope.launch {
            val state = uiState.value
            if (state is EditInvoiceUiState.Success) {
                _isSaving.value = true
                try {
                    Timber.d("Persisting invoice ${state.invoice.id}...")
                    invoiceRepository.saveInvoice(state.invoice)
                    Timber.d("Persist successful.")
                    _navigationEvent.emit(NavigationEvent.BackToInvoiceDetail)
                } catch (e: Exception) {
                    Timber.e(e, "Save failed: ${e.message}")
                    _navigationEvent.emit(NavigationEvent.ShowError(e.message ?: "Unknown save error"))
                } finally {
                    _isSaving.value = false
                }
            }
        }
    }

    fun shareInvoice() {
        viewModelScope.launch {
            val state = uiState.first()
            if (state is EditInvoiceUiState.Success) {
                try {
                    val businessProfile = businessProfileRepository.profile.first()
                    val invoice = state.invoice

                    // Build snapshot for PDF generation
                    val snapshot = com.emul8r.bizap.domain.model.InvoiceSnapshot(
                        invoiceId = invoice.id,
                        invoiceNumber = invoice.getFormattedInvoiceNumber(),
                        customerName = invoice.customerName,
                        customerAddress = invoice.customerAddress,
                        customerEmail = invoice.customerEmail,
                        date = invoice.date,
                        dueDate = invoice.dueDate,
                        items = invoice.items.map {
                            val itemTotal = (it.unitPrice * it.quantity).toLong()
                            com.emul8r.bizap.domain.model.LineItemSnapshot(
                                it.description,
                                it.quantity,
                                it.unitPrice,
                                itemTotal
                            )
                        },
                        subtotal = invoice.totalAmount - invoice.taxAmount,
                        taxRate = invoice.taxRate,
                        taxAmount = invoice.taxAmount,
                        totalAmount = invoice.totalAmount,
                        businessName = businessProfile.businessName,
                        businessAbn = businessProfile.abn,
                        businessEmail = businessProfile.email,
                        businessPhone = businessProfile.phone,    // FIXED
                        businessAddress = businessProfile.address, // FIXED
                        logoBase64 = businessProfile.logoBase64
                    )

                    val pdfFile = invoicePdfService.generateInvoice(snapshot, isQuote = false)
                    val uri = FileProvider.getUriForFile(context, "com.emul8r.bizap.fileprovider", pdfFile)

                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/pdf"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }

                    val chooser = Intent.createChooser(intent, "Share Invoice")
                    chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(chooser)
                } catch (e: Exception) {
                    Timber.e(e, "Share failed: ${e.message}")
                    _navigationEvent.emit(NavigationEvent.ShowError("Share failed: ${e.message}"))
                }
            }
        }
    }
}

