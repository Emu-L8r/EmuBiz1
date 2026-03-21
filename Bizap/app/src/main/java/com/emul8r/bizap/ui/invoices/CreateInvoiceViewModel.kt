package com.emul8r.bizap.ui.invoices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.BuildConfig
import com.emul8r.bizap.domain.repository.BusinessProfileRepository
import com.emul8r.bizap.domain.model.Currency
import com.emul8r.bizap.domain.model.Customer
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.InvoiceMetrics
import com.emul8r.bizap.domain.model.InvoiceStatus
import com.emul8r.bizap.domain.repository.CurrencyRepository
import com.emul8r.bizap.domain.repository.CustomerRepository
import com.emul8r.bizap.domain.repository.InvoiceRepository
import com.emul8r.bizap.domain.usecase.CalculateInvoiceMetricsUseCase
import com.emul8r.bizap.domain.usecase.GenerateAndSaveInvoiceUseCase
import com.emul8r.bizap.domain.test.TestDataProvider
import com.emul8r.bizap.domain.validation.ValidationRules
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
    val taxRate: Double = 0.0,
    val isTaxRegistered: Boolean = false,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null,
    // Phase 2: Customization fields
    val companyName: String = "",
    val templateType: String = "standard"
)

@HiltViewModel
class CreateInvoiceViewModel @Inject constructor(
    private val invoiceRepository: InvoiceRepository,
    private val customerRepository: CustomerRepository,
    private val businessProfileRepository: BusinessProfileRepository,
    private val currencyRepository: CurrencyRepository,
    private val generateAndSaveInvoiceUseCase: GenerateAndSaveInvoiceUseCase,
    private val calculateMetricsUseCase: CalculateInvoiceMetricsUseCase
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

            // Observe business profile to keep tax settings in sync
            businessProfileRepository.activeProfile.onEach { profile ->
                _uiState.update {
                    it.copy(
                        isTaxRegistered = profile.isTaxRegistered,
                        taxRate = if (profile.isTaxRegistered) profile.defaultTaxRate.toDouble() else 0.0
                    )
                }
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

        Timber.d("🐛 DEBUG BUTTON CLICKED: Loading test data...")
        
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
                Timber.d("✅ DEBUG DATA LOADED for ${targetCustomer.name}")
            } catch (e: Exception) {
                Timber.e(e, "❌ Debug data load failed: ${e.message}")
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

    fun removeLineItem(transientId: java.util.UUID) {
        // ✅ NULL SAFETY: Validate UUID not null
        require(transientId.toString().isNotEmpty()) { "Line item ID cannot be empty" }
        _uiState.update { state -> state.copy(items = state.items.filter { it.transientId != transientId }) }
    }

    fun updateLineItem(transientId: java.util.UUID, description: String, quantity: Double, unitPrice: Long) {
        // ✅ NULL SAFETY: Validate input before update
        require(transientId.toString().isNotEmpty()) { "Line item ID cannot be empty" }
        require(description.isNotBlank()) { "Line item description cannot be blank" }
        require(quantity > 0) { "Line item quantity must be positive, got $quantity" }
        require(unitPrice >= 0) { "Line item unit price cannot be negative, got $unitPrice" }

        _uiState.update { state ->
            state.copy(items = state.items.map {
                if (it.transientId == transientId) it.copy(description = description, quantity = quantity, unitPrice = unitPrice) else it
            })
        }
    }

    fun selectCustomer(customer: Customer) {
        // ✅ NULL SAFETY: Validate customer before selection
        require(customer.id > 0) { "Customer ID must be positive" }
        require(customer.name.isNotBlank()) { "Customer name cannot be blank" }
        // Email is optional - customers can be created without email

        _uiState.update { it.copy(selectedCustomer = customer) }
    }

    fun addPhoto(uri: String) {
        // ✅ NULL SAFETY: Validate URI before adding
        require(uri.isNotBlank()) { "Photo URI cannot be blank" }
        require(!uri.contains("..")) { "Invalid photo URI path" }  // Prevent directory traversal

        _uiState.update { it.copy(photoUris = it.photoUris + uri) }
    }

    fun removePhoto(uri: String) {
        // ✅ NULL SAFETY: Validate URI before removing
        require(uri.isNotBlank()) { "Photo URI cannot be blank" }

        _uiState.update { state -> state.copy(photoUris = state.photoUris.filter { it != uri }) }
    }

    /**
     * Returns calculated invoice metrics (subtotal, tax, total) for the current UI state.
     * Uses [CalculateInvoiceMetricsUseCase] as single source of truth for all calculations.
     *
     * ✅ NULL SAFETY: Returns safe defaults if state is incomplete
     */
    fun getInvoiceMetrics(): InvoiceMetrics {
        val state = _uiState.value

        // ✅ NULL SAFETY: Validate required fields exist
        val customerId = state.selectedCustomer?.id
            ?: run {
                Timber.w("⚠️ getInvoiceMetrics called without customer selection")
                return InvoiceMetrics(subtotal = 0, taxAmount = 0, totalAmount = 0)  // Safe default
            }

        require(customerId > 0) { "Customer ID must be positive" }

        val customerName = state.selectedCustomer?.name ?: ""
        require(state.items.isNotEmpty()) { "Invoice must have at least one line item" }

        val invoiceForCalculation = Invoice(
            customerId = customerId,
            customerName = customerName,
            date = System.currentTimeMillis(),
            totalAmount = 0L,  // Placeholder — overridden by metrics
            items = state.items.map { it.toDomain() },
            isQuote = false,
            status = InvoiceStatus.DRAFT,
            taxRate = state.taxRate
        )
        return calculateMetricsUseCase(invoiceForCalculation)
    }

    fun onSaveClicked() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                Timber.d("🔵 INVOICE SAVE STARTED")
                val state = _uiState.value
                val customer = state.selectedCustomer ?: throw Exception("Please select a customer")
                Timber.d("✅ Customer selected: ${customer.name}")

                val businessProfile = businessProfileRepository.activeProfile.first()
                val lineItems = state.items.map { it.toDomain() }
                Timber.d("✅ Line items mapped: ${lineItems.size} items")

                // Use CalculateInvoiceMetricsUseCase as single source of truth for all calculations
                val taxRate: Double = if (businessProfile.isTaxRegistered) businessProfile.defaultTaxRate.toDouble() else 0.0
                val tempInvoice = Invoice(
                    customerId = customer.id,
                    customerName = customer.name,
                    date = System.currentTimeMillis(),  // Placeholder for metrics calculation only
                    totalAmount = 0L,  // Placeholder — metrics will provide the real value
                    items = lineItems,
                    isQuote = false,
                    status = InvoiceStatus.DRAFT,
                    taxRate = taxRate
                )
                val metrics = calculateMetricsUseCase(tempInvoice)
                Timber.d("✅ Metrics calculated: subtotal=${metrics.subtotal}, tax=${metrics.taxAmount}, total=${metrics.totalAmount} cents")
                val createdAt = System.currentTimeMillis()
                val dueDate = createdAt + (30L * 24 * 60 * 60 * 1000)

                val invoice = Invoice(
                    customerId = customer.id,
                    customerName = customer.name,
                    customerAddress = customer.address ?: "",
                    customerEmail = customer.email,
                    date = createdAt,
                    dueDate = dueDate,
                    totalAmount = metrics.totalAmount,
                    items = lineItems,
                    isQuote = false,
                    status = InvoiceStatus.DRAFT,
                    header = state.header.ifBlank { null },
                    subheader = state.subheader.ifBlank { null },
                    notes = state.notes.ifBlank { null },
                    footer = state.footer.ifBlank { null },
                    photoUris = state.photoUris,
                    taxRate = taxRate,
                    taxAmount = metrics.taxAmount,
                    companyLogoPath = businessProfile.logoBase64,
                    updatedAt = createdAt,
                    currencyCode = state.selectedCurrencyCode
                )

                // 🔒 VALIDATION: Verify invoice meets all business rules before saving
                // This is CRITICAL - prevents invalid data from entering the database
                // Uses Result<Unit> pattern - doesn't throw, returns error message
                val validationResult = ValidationRules.validateInvoice(invoice)
                if (validationResult.isFailure()) {
                    val errorMessage = validationResult.getErrorOrNull() ?: "Unknown validation error"
                    Timber.w("⚠️ VALIDATION FAILED: $errorMessage")
                    _uiState.update { it.copy(error = errorMessage, isSaving = false) }
                    return@launch
                }
                Timber.d("✅ Invoice passed all validation rules")

                val invoiceId = invoiceRepository.saveInvoice(invoice).getOrThrow()
                Timber.d("✅ Invoice saved to database: ID=$invoiceId")
                val invoiceWithId = invoice.copy(id = invoiceId)

                Timber.d("🔵 Starting PDF generation...")
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
                        items = invoiceWithId.items.map {
                            val itemTotal = (it.unitPrice * it.quantity).toLong()  // Cents
                            com.emul8r.bizap.domain.model.LineItemSnapshot(
                                description = it.description,
                                quantity = it.quantity,
                                unitPrice = it.unitPrice,
                                total = itemTotal
                            )
                        },
                        subtotal = metrics.subtotal,
                        taxRate = taxRate,
                        taxAmount = metrics.taxAmount,
                        totalAmount = invoiceWithId.totalAmount,
                        businessName = businessProfile.businessName,
                        businessAbn = businessProfile.abn,
                        businessEmail = businessProfile.email,
                        businessPhone = businessProfile.phone,
                        businessAddress = businessProfile.address,
                        logoBase64 = businessProfile.logoBase64,
                        currencyCode = state.selectedCurrencyCode,
                        headerText = state.header,
                        subheaderText = state.subheader,
                        footerText = state.footer,
                        notes = state.notes,
                        bankAccountName = businessProfile.accountName ?: "",
                        bankAccountNumber = businessProfile.accountNumber ?: "",
                        bankBsb = businessProfile.bsbNumber ?: "",
                        bankName = businessProfile.bankName ?: ""
                    ),
                    isQuote = false,
                    overwriteExisting = true
                )

                if (result.isSuccess) {
                    Timber.d("✅ PDF generation successful")
                    _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
                    Timber.d("✅ INVOICE SAVE COMPLETE - SUCCESS")
                } else {
                    val error = result.exceptionOrNull() ?: Exception("Failed to generate PDF")
                    Timber.e(error, "❌ PDF generation failed")
                    throw error
                }

            } catch (e: Exception) {
                Timber.e(e, "❌ INVOICE SAVE FAILED: ${e.message}")
                _uiState.update { it.copy(error = e.message, isSaving = false) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    // Phase 2: Customization functions
    fun updateCompanyName(name: String) {
        _uiState.update { it.copy(companyName = name) }
    }

    fun updateTemplateType(template: String) {
        _uiState.update { it.copy(templateType = template) }
    }
}
