package com.emul8r.bizap.ui.invoices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.BuildConfig
import com.emul8r.bizap.domain.model.Currency
import com.emul8r.bizap.domain.model.Customer
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.InvoiceStatus
import com.emul8r.bizap.domain.repository.BusinessProfileRepository
import com.emul8r.bizap.domain.repository.CurrencyRepository
import com.emul8r.bizap.domain.repository.CustomerRepository
import com.emul8r.bizap.domain.repository.InvoiceRepository
import com.emul8r.bizap.domain.usecase.GenerateAndSaveInvoiceUseCase
import com.emul8r.bizap.domain.test.TestDataProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
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

    fun loadDebugTestData() {
        if (!BuildConfig.DEBUG) return
        viewModelScope.launch {
            try {
                val customers = customerRepository.getAllCustomers().first()
                val targetCustomer = customers.firstOrNull() ?: throw Exception("No customers in DB")

                _uiState.update { state ->
                    state.copy(
                        selectedCustomer = targetCustomer,
                        header = TestDataProvider.getDebugInitialHeader(),
                        items = TestDataProvider.getDebugLineItems()
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Debug Load Failed")
            }
        }
    }

    fun onHeaderChange(header: String) = _uiState.update { it.copy(header = header) }
    fun onSubheaderChange(subheader: String) = _uiState.update { it.copy(subheader = subheader) }
    fun onNotesChange(notes: String) = _uiState.update { it.copy(notes = notes) }
    fun onFooterChange(footer: String) = _uiState.update { it.copy(footer = footer) }

    fun addLineItem() = _uiState.update { it.copy(items = it.items + LineItemForm()) }
    fun removeLineItem(id: Long?) = _uiState.update { state -> state.copy(items = state.items.filter { it.id != id }) }
    
    fun updateLineItem(id: Long?, description: String, quantity: Double, unitPrice: Long) {
        _uiState.update { state ->
            state.copy(items = state.items.map {
                if (it.id == id) it.copy(description = description, quantity = quantity, unitPrice = unitPrice) else it
            })
        }
    }

    fun selectCustomer(customer: Customer) = _uiState.update { it.copy(selectedCustomer = customer) }

    fun addPhoto(uri: String) {
        _uiState.update { it.copy(photoUris = it.photoUris + uri) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun onSaveClicked() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                val state = _uiState.value
                val customer = state.selectedCustomer ?: throw Exception("Please select a customer")
                val businessProfile = businessProfileRepository.activeProfile.first()
                val lineItems = state.items.map { it.toDomain() }
                val subtotal = lineItems.sumOf { (it.unitPrice * it.quantity).toLong() }
                val taxRate = if (businessProfile.isTaxRegistered) businessProfile.defaultTaxRate.toDouble() else 0.0
                val taxAmount = (subtotal * taxRate).toLong()

                val invoice = Invoice(
                    customerId = customer.id,
                    customerName = customer.name,
                    customerAddress = customer.address ?: "",
                    date = System.currentTimeMillis(),
                    totalAmount = subtotal + taxAmount,
                    items = lineItems,
                    isQuote = false,
                    status = InvoiceStatus.DRAFT,
                    taxRate = taxRate,
                    taxAmount = taxAmount,
                    currencyCode = state.selectedCurrencyCode
                )

                val invoiceId = invoiceRepository.saveInvoice(invoice)
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isSaving = false) }
            }
        }
    }
}
