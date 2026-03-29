package com.emul8r.bizap.ui.gui2.invoices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.domain.model.Customer
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.repository.CustomerRepository
import com.emul8r.bizap.domain.repository.InvoiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class CreateInvoiceViewModelV2 @Inject constructor(
    private val invoiceRepository: InvoiceRepository,
    private val customerRepository: CustomerRepository
) : ViewModel() {

    // Track loading state for UI
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Customer data management
    private val _customers = MutableStateFlow<List<Customer>>(emptyList())
    val customers: StateFlow<List<Customer>> = _customers.asStateFlow()

    private val _selectedCustomer = MutableStateFlow<Customer?>(null)
    val selectedCustomer: StateFlow<Customer?> = _selectedCustomer.asStateFlow()

    init {
        loadCustomers()
    }

    /**
     * Load all customers for the current business
     * Called automatically on ViewModel initialization
     */
    private fun loadCustomers() {
        viewModelScope.launch {
            try {
                Timber.d("CreateInvoiceViewModelV2: Loading customers")
                customerRepository.getAllCustomers()
                    .collect { customerList ->
                        _customers.value = customerList
                        Timber.d("CreateInvoiceViewModelV2: Loaded ${customerList.size} customers")
                    }
            } catch (e: Exception) {
                Timber.e(e, "CreateInvoiceViewModelV2: Failed to load customers")
                _customers.value = emptyList()
            }
        }
    }

    /**
     * Select a customer for the invoice
     * @param customer The customer to select, or null to deselect
     */
    fun selectCustomer(customer: Customer?) {
        _selectedCustomer.value = customer
        Timber.d("CreateInvoiceViewModelV2: Selected customer ${customer?.name ?: "None"}")
    }

    fun createInvoice(
        invoice: Invoice,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                Timber.d("CreateInvoiceViewModelV2: Creating invoice for ${invoice.customerName}")
                val result = invoiceRepository.saveInvoice(invoice)

                result.onSuccess { invoiceId ->
                    Timber.d("✅ CreateInvoiceViewModelV2: Invoice created successfully with ID=$invoiceId")
                    onSuccess()
                }

                result.onFailure { exception ->
                    Timber.e(exception, "❌ CreateInvoiceViewModelV2: Failed to create invoice")
                    onError(exception.message ?: "Unknown error during invoice creation")
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ CreateInvoiceViewModelV2: Unexpected error during invoice creation")
                onError(e.message ?: "Unexpected error")
            } finally {
                _isLoading.value = false
            }
        }
    }
}

