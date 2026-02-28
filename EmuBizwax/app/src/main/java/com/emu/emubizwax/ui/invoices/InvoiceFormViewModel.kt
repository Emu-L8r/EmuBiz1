package com.emu.emubizwax.ui.invoices

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emu.emubizwax.domain.model.Customer
import com.emu.emubizwax.domain.model.Invoice
import com.emu.emubizwax.domain.repository.CustomerRepository
import com.emu.emubizwax.domain.usecase.CacheImagesUseCase
import com.emu.emubizwax.domain.usecase.SaveInvoiceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class InvoiceFormViewModel @Inject constructor(
    private val customerRepository: CustomerRepository,
    private val saveInvoiceUseCase: SaveInvoiceUseCase,
    private val cacheImagesUseCase: CacheImagesUseCase
) : ViewModel() {

    private val _formState = MutableStateFlow(InvoiceFormState())
    val formState: StateFlow<InvoiceFormState> = _formState.asStateFlow()

    val customers: StateFlow<List<Customer>> = customerRepository.getCustomers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateCustomer(customer: Customer) {
        _formState.update { it.copy(selectedCustomer = customer) }
    }

    fun updateItem(id: String, updatedItem: LineItem) {
        _formState.update { currentState ->
            val newItems = currentState.items.map {
                if (it.id == id) updatedItem else it
            }
            currentState.copy(items = newItems)
        }
    }

    fun addItem() {
        _formState.update { it.copy(items = it.items + LineItem()) }
    }

    fun removeItem(id: String) {
        _formState.update { currentState ->
            val newItems = currentState.items.filterNot { it.id == id }
            if (newItems.isEmpty()) {
                currentState.copy(items = listOf(LineItem()))
            } else {
                currentState.copy(items = newItems)
            }
        }
    }

    fun onPhotosSelected(uris: List<Uri>) {
        viewModelScope.launch {
            val cachedUris = cacheImagesUseCase(uris)
            _formState.update { it.copy(photoUris = it.photoUris + cachedUris) }
        }
    }

    fun onRemovePhoto(uri: Uri) {
        _formState.update { currentState ->
            val newUris = currentState.photoUris.filterNot { it == uri }
            currentState.copy(photoUris = newUris)
        }
    }

    fun saveInvoice() {
        viewModelScope.launch {
            val state = formState.value
            val invoice = Invoice(
                customerId = state.selectedCustomer?.id ?: 0L,
                date = state.date,
                status = if (state.isQuote) com.emu.emubizwax.domain.model.InvoiceStatus.DRAFT else com.emu.emubizwax.domain.model.InvoiceStatus.SENT,
                isQuote = state.isQuote,
                taxRate = state.taxRate,
                photoUris = state.photoUris.map { it.toString() },
                totalAmount = BigDecimal(state.grandTotal)
            )
            val lineItems = state.items.map {
                com.emu.emubizwax.domain.model.LineItem(
                    description = it.description,
                    quantity = BigDecimal(it.quantity),
                    unitPrice = BigDecimal(it.price),
                    invoiceId = 0 // The repository will handle this
                )
            }
            saveInvoiceUseCase(invoice, lineItems)
        }
    }
}
