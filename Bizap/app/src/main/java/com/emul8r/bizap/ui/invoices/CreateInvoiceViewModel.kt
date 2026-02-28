package com.emul8r.bizap.ui.invoices

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.BuildConfig
import com.emul8r.bizap.data.repository.BusinessProfileRepository
import com.emul8r.bizap.domain.model.Currency
import com.emul8r.bizap.domain.model.Customer
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.InvoiceStatus
import com.emul8r.bizap.domain.repository.CurrencyRepository
import com.emul8r.bizap.domain.repository.CustomerRepository
import com.emul8r.bizap.domain.repository.InvoiceRepository
import com.emul8r.bizap.domain.usecase.GenerateAndSaveInvoiceUseCase
import com.emul8r.bizap.domain.test.TestDataProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateInvoiceUiState(
    val customers: List<Customer> = emptyList(),
    val selectedCustomer: Customer? = null,
    val items: List<LineItemForm> = listOf(LineItemForm()),
    val header: String = "",
    val subheader: String = "",
    val notes: String = "",
    val footer: String = "",
    val photoUris: List<String> = emptyList(),
    val currencies: List<Currency> = emptyList(),
    val selectedCurrencyCode: String = "AUD",
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CreateInvoiceViewModel @Inject constructor(
    private val invoiceRepository: InvoiceRepository,
    private val customerRepository: CustomerRepository,
    private val businessProfileRepository: BusinessProfileRepository,
    private val currencyRepository: CurrencyRepository,
    private val generateAndSaveInvoiceUseCase: GenerateAndSaveInvoiceUseCase
) : ViewModel() {

    private val TAG = "CreateInvoiceViewModel"
    private val _uiState = MutableStateFlow(CreateInvoiceUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            // Observe customers
            customerRepository.getAllCustomers().onEach { customers ->
                _uiState.update { it.copy(customers = customers) }
            }.launchIn(this)

            // Observe currencies
            currencyRepository.getEnabledCurrencies().onEach { currencies ->
                _uiState.update { it.copy(currencies = currencies) }
            }.launchIn(this)
        }
    }

    fun onCurrencySelected(code: String) {
        _uiState.update { it.copy(selectedCurrencyCode = code) }
    }

    /**
     * PRAGMATIC DEBUG FIX: Explicitly load test data on demand.
     */
    fun loadDebugTestData() {
        if (!BuildConfig.DEBUG) return

        Log.d(TAG, "🐛 DEBUG BUTTON CLICKED: Loading test data...")
        
        viewModelScope.launch {
            try {
                val customers = customerRepository.getAllCustomers().first()
                val targetCustomer = customers.firstOrNull() ?: throw Exception("No customers in DB. Seed first.")

                _uiState.update { state ->
                    state.copy(
                        selectedCustomer = targetCustomer,
                        header = TestDataProvider.getDebugInitialHeader(),
                        subheader = TestDataProvider.getDebugInitialSubheader(),
                        notes = TestDataProvider.getDebugInitialNotes(),
                        footer = TestDataProvider.getDebugInitialFooter(),
                        items = TestDataProvider.getDebugLineItems()
                    )
                }
                Log.d(TAG, "✅ DEBUG DATA LOADED for ${targetCustomer.name}")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Debug data load failed: ${e.message}")
                _uiState.update { it.copy(error = "Debug Load Failed: ${e.message}") }
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

    fun selectCustomer(customer: Customer) {
        _uiState.update { it.copy(selectedCustomer = customer) }
    }

    fun addPhoto(uri: String) {
        _uiState.update { it.copy(photoUris = it.photoUris + uri) }
    }

    fun onSaveClicked() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                val state = _uiState.value
                val customer = state.selectedCustomer ?: throw Exception("Please select a customer")

                val businessProfile = businessProfileRepository.profile.first()
                val lineItems = state.items.map { it.toDomain() }
                val subtotal = lineItems.sumOf { it.quantity * it.unitPrice }
                
                val taxRate = 0.1 
                val taxAmount = subtotal * taxRate
                val createdAt = System.currentTimeMillis()
                val dueDate = createdAt + (30L * 24 * 60 * 60 * 1000)

                val invoice = Invoice(
                    customerId = customer.id,
                    customerName = customer.name,
                    customerAddress = customer.address ?: "",
                    customerEmail = customer.email,
                    date = createdAt,
                    dueDate = dueDate,
                    totalAmount = subtotal + taxAmount,
                    items = lineItems,
                    isQuote = false,
                    status = InvoiceStatus.DRAFT,
                    header = state.header.ifBlank { null },
                    subheader = state.subheader.ifBlank { null },
                    notes = state.notes.ifBlank { null },
                    footer = state.footer.ifBlank { null },
                    photoUris = state.photoUris,
                    taxRate = taxRate,
                    taxAmount = taxAmount,
                    companyLogoPath = businessProfile.logoBase64,
                    updatedAt = createdAt,
                    currencyCode = state.selectedCurrencyCode
                )

                val invoiceId = invoiceRepository.saveInvoice(invoice)
                val invoiceWithId = invoice.copy(id = invoiceId)

                val result = generateAndSaveInvoiceUseCase(
                    invoice = invoiceWithId,
                    snapshot = com.emul8r.bizap.domain.model.InvoiceSnapshot(
                        invoiceId = invoiceWithId.id,
                        invoiceNumber = invoiceWithId.getFormattedInvoiceNumber(),
                        customerName = invoiceWithId.customerName,
                        customerAddress = invoiceWithId.customerAddress,
                        customerEmail = invoiceWithId.customerEmail,
                        date = invoiceWithId.date,
                        dueDate = invoiceWithId.dueDate,
                        items = invoiceWithId.items.map { com.emul8r.bizap.domain.model.LineItemSnapshot(it.description, it.quantity, it.unitPrice, it.quantity * it.unitPrice) },
                        subtotal = subtotal,
                        taxRate = taxRate,
                        taxAmount = taxAmount,
                        totalAmount = invoiceWithId.totalAmount,
                        businessName = businessProfile.businessName,
                        businessAbn = businessProfile.abn,
                        businessEmail = businessProfile.email,
                        businessPhone = businessProfile.phone,
                        businessAddress = businessProfile.address,
                        logoBase64 = businessProfile.logoBase64,
                        currencyCode = state.selectedCurrencyCode
                    ),
                    isQuote = false,
                    overwriteExisting = true
                )

                if (result.isSuccess) {
                    _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
                } else {
                    throw result.exceptionOrNull() ?: Exception("Failed to generate PDF")
                }

            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isSaving = false) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
