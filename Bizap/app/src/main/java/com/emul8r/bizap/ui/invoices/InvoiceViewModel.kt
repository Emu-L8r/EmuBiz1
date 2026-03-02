package com.emul8r.bizap.ui.invoices

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.domain.model.Customer
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.InvoiceStatus
import com.emul8r.bizap.domain.model.LineItem
import com.emul8r.bizap.domain.repository.CustomerRepository
import com.emul8r.bizap.domain.usecase.SaveInvoiceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class InvoiceUiState(
    val customers: List<Customer> = emptyList(),
    val selectedCustomer: Customer? = null,
    val items: List<LineItemForm> = listOf(LineItemForm()),
    val header: String = "",
    val subheader: String = "",
    val notes: String = "",
    val footer: String = "",
    val photoUris: List<Uri> = emptyList(),
    val isSaving: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false,
)

@HiltViewModel
class InvoiceViewModel @Inject constructor(
    private val customerRepository: CustomerRepository,
    private val saveInvoiceUseCase: SaveInvoiceUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(InvoiceUiState())
    val uiState: StateFlow<InvoiceUiState> = combine(
        _uiState,
        customerRepository.getAllCustomers()
    ) { state, customers ->
        state.copy(customers = customers)
    }.stateIn(
        scope = viewModelScope,
        started = WhileSubscribed(5000),
        initialValue = InvoiceUiState()
    )

    fun onSaveClicked() {
        Timber.d("Save clicked. Customer: ${uiState.value.selectedCustomer?.name}")
        saveInvoice()
    }

    fun selectCustomer(customer: Customer) {
        _uiState.update { it.copy(selectedCustomer = customer) }
    }

    fun addLineItem() {
        _uiState.update { it.copy(items = it.items + LineItemForm()) }
    }

    fun updateLineItem(id: Long?, description: String, quantity: Double, unitPrice: Double) {
        _uiState.update { state ->
            state.copy(items = state.items.map {
                if (it.id == id) it.copy(description = description, quantity = quantity, unitPrice = unitPrice) else it
            })
        }
    }

    fun removeLineItem(id: Long?) {
        _uiState.update { state ->
            state.copy(items = state.items.filter { it.id != id })
        }
    }

    fun onHeaderChange(text: String) = _uiState.update { it.copy(header = text) }
    fun onSubheaderChange(text: String) = _uiState.update { it.copy(subheader = text) }
    fun onNotesChange(text: String) = _uiState.update { it.copy(notes = text) }
    fun onFooterChange(text: String) = _uiState.update { it.copy(footer = text) }
    fun addPhoto(uri: Uri) = _uiState.update { it.copy(photoUris = it.photoUris + uri) }

    private fun saveInvoice() {
        viewModelScope.launch {
            try {
                Timber.d("Attempting to save invoice...")
                val currentState = _uiState.value
                val customer = currentState.selectedCustomer ?: run {
                    Timber.e("Save failed: Customer not selected.")
                    return@launch
                }

                val domainInvoice = Invoice(
                    customerId = customer.id,
                    customerName = customer.name,
                    date = System.currentTimeMillis(),
                    totalAmount = 0.0, // Calculated in Use Case
                    items = currentState.items.map { it.toDomain() },
                    isQuote = false,
                    status = InvoiceStatus.DRAFT,
                    header = currentState.header,
                    subheader = currentState.subheader,
                    notes = currentState.notes,
                    footer = currentState.footer,
                    photoUris = currentState.photoUris.map { it.toString() }
                )

                saveInvoiceUseCase(domainInvoice).onSuccess {
                    Timber.d("Save Successful!")
                    _uiState.update { 
                        it.copy(
                            isSaving = false, 
                            saveSuccess = true, 
                            items = listOf(LineItemForm()), 
                            selectedCustomer = null,
                            photoUris = emptyList()
                        ) 
                    }
                }.onFailure { e ->
                    Timber.e(e, "SAVE FAILED: ${e.message}")
                    _uiState.update { it.copy(isSaving = false, error = e.message) }
                }
            } catch (e: Exception) {
                Timber.e(e, "SAVE FAILED: ${e.message}")
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
