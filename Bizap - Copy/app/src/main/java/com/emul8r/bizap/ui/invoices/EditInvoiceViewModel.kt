package com.emul8r.bizap.ui.invoices

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.emul8r.bizap.data.local.entities.CustomerEntity
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.InvoiceStatus
import com.emul8r.bizap.domain.repository.CustomerRepository
import com.emul8r.bizap.domain.repository.InvoiceRepository
import com.emul8r.bizap.domain.usecase.SaveInvoiceUseCase
import com.emul8r.bizap.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditInvoiceUiState(
    val customers: List<CustomerEntity> = emptyList(),
    val selectedCustomer: CustomerEntity? = null,
    val items: List<LineItemForm> = emptyList(),
    val header: String = "",
    val subheader: String = "",
    val notes: String = "",
    val footer: String = "",
    val photoUris: List<Uri> = emptyList(),
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null,
    val isQuote: Boolean = false
)

@HiltViewModel
class EditInvoiceViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val invoiceRepo: InvoiceRepository,
    private val customerRepo: CustomerRepository,
    private val saveInvoiceUseCase: SaveInvoiceUseCase,
    private val lineItemMapper: LineItemMapper
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditInvoiceUiState())
    val uiState = _uiState.asStateFlow()

    private val invoiceId: Long = savedStateHandle.toRoute<Screen.EditInvoice>().invoiceId

    init {
        loadInvoice(invoiceId)
    }

    private fun loadInvoice(id: Long) {
        invoiceRepo.getInvoiceWithItemsById(id)
            .onEach { data ->
                val allCustomers = customerRepo.getAllCustomers().first()
                if (data != null) {
                    val invoice = data.invoice
                    _uiState.update {
                        it.copy(
                            customers = allCustomers,
                            selectedCustomer = allCustomers.find { c -> c.id == invoice.customerId },
                            header = invoice.header ?: "",
                            subheader = invoice.subheader ?: "",
                            notes = invoice.notes ?: "",
                            footer = invoice.footer ?: "",
                            items = data.items.map { item -> lineItemMapper.toForm(item) },
                            photoUris = invoice.photoUris.split(",").mapNotNull { uriString -> 
                                try { Uri.parse(uriString) } catch (e: Exception) { null } 
                            },
                            isQuote = invoice.isQuote
                        )
                    }
                } else {
                    _uiState.update { it.copy(error = "Invoice not found.") }
                }
            }
            .launchIn(viewModelScope)
    }
    
    fun onHeaderChange(text: String) = _uiState.update { it.copy(header = text) }
    fun onSubheaderChange(text: String) = _uiState.update { it.copy(subheader = text) }
    fun onNotesChange(text: String) = _uiState.update { it.copy(notes = text) }
    fun onFooterChange(text: String) = _uiState.update { it.copy(footer = text) }
    fun addPhoto(uri: Uri) = _uiState.update { it.copy(photoUris = it.photoUris + uri) }
    fun addLineItem() = _uiState.update { it.copy(items = it.items + LineItemForm()) }
    fun removeLineItem(id: Long?) = _uiState.update { state -> state.copy(items = state.items.filter { it.id != id }) }
    fun updateLineItem(id: Long?, description: String, quantity: Double, unitPrice: Double) {
        _uiState.update { state ->
            state.copy(items = state.items.map {
                if (it.id == id) it.copy(description = description, quantity = quantity, unitPrice = unitPrice) else it
            })
        }
    }

    fun onIsQuoteChange(isQuote: Boolean) {
        _uiState.update { it.copy(isQuote = isQuote) }
    }

    fun updateInvoice() {
        val currentState = _uiState.value
        val customer = currentState.selectedCustomer ?: return

        _uiState.update { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            val domainInvoice = Invoice(
                id = invoiceId,
                customerId = customer.id,
                customerName = customer.name,
                date = System.currentTimeMillis(),
                totalAmount = 0.0, // Recalculated in UseCase
                items = currentState.items.map { it.toDomain() },
                isQuote = currentState.isQuote,
                status = InvoiceStatus.DRAFT, // Saving an edit returns it to DRAFT
                header = currentState.header,
                subheader = currentState.subheader,
                notes = currentState.notes,
                footer = currentState.footer,
                photoUris = currentState.photoUris.map { it.toString() }
            )

            saveInvoiceUseCase(domainInvoice).onSuccess {
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
                loadInvoice(invoiceId) // Refresh the UI with the new IDs
            }.onFailure { e ->
                _uiState.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }
}
