package com.emul8r.bizap.ui.invoices

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.data.local.entities.CustomerEntity
import com.emul8r.bizap.domain.repository.CustomerRepository
import com.emul8r.bizap.domain.repository.InvoiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateInvoiceUiState(
    val customers: List<CustomerEntity> = emptyList(),
    val selectedCustomer: CustomerEntity? = null,
    val items: List<LineItemForm> = listOf(LineItemForm()),
    val header: String = "",
    val subheader: String = "",
    val notes: String = "",
    val footer: String = "",
    val photoUris: List<String> = emptyList(),
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null,
    val isQuote: Boolean = false
)

@HiltViewModel
class CreateInvoiceViewModel @Inject constructor(
    private val invoiceRepository: InvoiceRepository,
    private val customerRepository: CustomerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateInvoiceUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            customerRepository.getAllCustomers().collect { customers ->
                _uiState.update { it.copy(customers = customers) }
            }
        }
    }

    fun onHeaderChange(header: String) {
        _uiState.update { it.copy(header = header) }
    }

    fun onSubheaderChange(subheader: String) {
        _uiState.update { it.copy(subheader = subheader) }
    }

    fun onNotesChange(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    fun onFooterChange(footer: String) {
        _uiState.update { it.copy(footer = footer) }
    }

    fun addLineItem() {
        _uiState.update { 
            it.copy(items = it.items + LineItemForm())
        }
    }

    fun removeLineItem(id: Long?) = _uiState.update { state -> state.copy(items = state.items.filter { it.id != id }) }
    fun updateLineItem(id: Long?, description: String, quantity: Double, unitPrice: Double) {
        _uiState.update { state ->
            state.copy(items = state.items.map {
                if (it.id == id) it.copy(description = description, quantity = quantity, unitPrice = unitPrice) else it
            })
        }
    }

    fun selectCustomer(customer: CustomerEntity) {
        _uiState.update { it.copy(selectedCustomer = customer) }
    }

    fun addPhoto(uri: String) {
        _uiState.update { it.copy(photoUris = it.photoUris + uri) }
    }

    fun onIsQuoteChange(isQuote: Boolean) {
        _uiState.update { it.copy(isQuote = isQuote) }
    }

    fun onSaveClicked() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                val state = _uiState.value
                if (state.selectedCustomer == null) {
                    _uiState.update { it.copy(error = "Please select a customer", isSaving = false) }
                    return@launch
                }
                val invoiceId = invoiceRepository.saveInvoice(
                    customer = state.selectedCustomer,
                    items = state.items.map { it.toDomain() },
                    header = state.header,
                    subheader = state.subheader,
                    notes = state.notes,
                    footer = state.footer,
                    photoUris = state.photoUris,
                    isQuote = state.isQuote
                )
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isSaving = false) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
