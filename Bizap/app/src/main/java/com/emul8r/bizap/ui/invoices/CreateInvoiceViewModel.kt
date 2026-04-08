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
import com.emul8r.bizap.domain.model.PrefilledItem
import com.emul8r.bizap.domain.repository.CurrencyRepository
import com.emul8r.bizap.domain.repository.CustomerRepository
import com.emul8r.bizap.domain.repository.InvoiceRepository
import com.emul8r.bizap.domain.usecase.CalculateInvoiceMetricsUseCase
import com.emul8r.bizap.domain.usecase.GenerateAndSaveInvoiceUseCase
import com.emul8r.bizap.domain.test.TestDataProvider
import com.emul8r.bizap.domain.validation.ValidationRules
import com.emul8r.bizap.utils.FirebaseEventTracker
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

/**
 * Manages invoice creation workflow and form state for GUI1.
 *
 * **Purpose:**
 * Central state manager for the "Create Invoice" screen. Handles user input from invoice forms,
 * manages real-time calculations, coordinates with repositories for data persistence, and
 * triggers PDF generation upon save.
 *
 * **Responsibilities:**
 * - Manage invoice form state (customers, line items, tax settings, photos)
 * - Load customers, currencies, and business profile asynchronously
 * - Calculate invoice metrics (subtotal, tax, total) in real-time
 * - Validate invoice data before saving
 * - Persist invoice to database and generate PDF
 * - Handle form interactions (add/remove line items, select customer, etc.)
 *
 * **Key Operations:**
 * - selectCustomer(): Update selected customer
 * - addLineItem(): Add empty line item
 * - updateLineItem(): Modify existing line item amounts
 * - onSaveClicked(): Save invoice with full validation + PDF generation
 * - onCurrencySelected(): Change currency
 * - addPhoto()/removePhoto(): Manage attachments
 *
 * **Data Sources:**
 * - [InvoiceRepository] - Save invoices
 * - [CustomerRepository] - Get available customers
 * - [CurrencyRepository] - Supported currencies
 * - [BusinessProfileRepository] - Tax settings & branding
 * - [GenerateAndSaveInvoiceUseCase] - PDF generation
 * - [CalculateInvoiceMetricsUseCase] - Monetary calculations
 *
 * @param invoiceRepository Invoice persistence
 * @param customerRepository Customer data
 * @param businessProfileRepository Business settings
 * @param currencyRepository Currency options
 * @param generateAndSaveInvoiceUseCase PDF generation
 * @param calculateMetricsUseCase Calculation engine
 */
@HiltViewModel
class CreateInvoiceViewModel @Inject constructor(
    private val invoiceRepository: InvoiceRepository,
    private val customerRepository: CustomerRepository,
    private val businessProfileRepository: BusinessProfileRepository,
    private val currencyRepository: CurrencyRepository,
    private val generateAndSaveInvoiceUseCase: GenerateAndSaveInvoiceUseCase,
    private val calculateMetricsUseCase: CalculateInvoiceMetricsUseCase,
    private val eventTracker: FirebaseEventTracker,
    private val invoiceSettingsRepository: com.emul8r.bizap.data.repository.InvoiceSettingsRepository
) : ViewModel() {

    private val TAG = "CreateInvoiceViewModel"
    private val _uiState = MutableStateFlow(CreateInvoiceUiState())
    val uiState = _uiState.asStateFlow()

    // 🔥 CRITICAL FIX: Use same hardcoded userId as InvoiceSettingsViewModel
    // This ensures settings saved in InvoiceSettingsViewModel can be loaded here
    private val currentUserId = "current_user"

    // 🔥 CRITICAL: Store the business ID from navigation route
    // This is used instead of activeProfile.id to ensure invoices are saved to the correct business
    private var _businessId: Long? = null
    fun setBusinessId(businessId: Long) {
        Timber.d("🎯 CreateInvoiceViewModel.setBusinessId($businessId) called - will use this when saving invoice")
        _businessId = businessId
    }

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

            // Load invoice settings to auto-populate defaults
            invoiceSettingsRepository.getSettingsFlow(currentUserId).onEach { settings ->
                settings?.let { s ->
                    _uiState.update { state ->
                        state.copy(
                            footer = s.footerMessage
                            // NOTE: companyName now comes from BusinessProfile
                        )
                    }
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

    /**
     * ✅ NEW: Load pre-filled items from settings/templates and add to invoice
     *
     * This allows users to quickly add pre-configured line items that were saved
     * in the invoice settings, speeding up invoice creation.
     */
    fun loadPrefilledItems(prefilledItems: List<com.emul8r.bizap.domain.model.LineItem>) {
        if (prefilledItems.isEmpty()) {
            Timber.d("No pre-filled items to load")
            return
        }

        Timber.d("📋 Loading ${prefilledItems.size} pre-filled items")

        val newItems = prefilledItems.map { item ->
            LineItemForm(
                transientId = java.util.UUID.randomUUID(),
                description = item.description,
                quantity = item.quantity,
                unitPrice = item.unitPrice
            )
        }

        _uiState.update { state ->
            state.copy(items = state.items + newItems)
        }

        Timber.d("✅ Pre-filled items loaded. Total items: ${_uiState.value.items.size}")
    }

    /**
     * ✅ Add a single pre-filled item to the invoice
     *
     * This is called when the user selects a pre-filled item from the dialog
     * in the Create Invoice screen.
     *
     * @param prefilledItem The pre-filled item to add
     */
    fun addLineItemFromPrefilledItem(prefilledItem: PrefilledItem) {
        Timber.d("➕ Adding pre-filled item: ${prefilledItem.description} ($${prefilledItem.unitPrice / 100.0})")

        val newItem = LineItemForm(
            transientId = java.util.UUID.randomUUID(),
            description = prefilledItem.description,
            quantity = 1.0,  // Default quantity to 1
            unitPrice = prefilledItem.unitPrice
        )

        _uiState.update { state ->
            state.copy(items = state.items + newItem)
        }

        Timber.d("✅ Pre-filled item added. Total items: ${_uiState.value.items.size}")
    }

    fun removeLineItem(transientId: java.util.UUID) {
        // ✅ NULL SAFETY: Validate UUID not null
        require(transientId.toString().isNotEmpty()) { "Line item ID cannot be empty" }
        _uiState.update { state -> state.copy(items = state.items.filter { it.transientId != transientId }) }
    }

    /**
     * ✅ FIX FOR ISSUE #2: Batch update line items with proper handling for additions, deletions, and modifications.
     * The editor returns items in the same order they were passed in, plus any new items added.
     *
     * @param updatedItems List of updated LineItem objects from the editor (in same order)
     * @param currentItems Current list of LineItemForm objects in ViewModel state
     */
    fun updateLineItemsFromEditor(
        updatedItems: List<com.emul8r.bizap.domain.model.LineItem>,
        currentItems: List<LineItemForm>
    ) {
        Timber.d("🔄 updateLineItemsFromEditor called:")
        Timber.d("   - Updated items count: ${updatedItems.size}")
        Timber.d("   - Current items count: ${currentItems.size}")
        Timber.d("   - State items count: ${_uiState.value.items.size}")

        _uiState.update { state ->
            // Handle additions, deletions, and modifications
            val newItems = mutableListOf<LineItemForm>()

            // Process each updated item (including new additions)
            for (index in updatedItems.indices) {
                val updatedItem = updatedItems[index]

                val itemForm = if (index < currentItems.size) {
                    // Update existing item
                    val currentItem = currentItems[index]
                    Timber.d("   Item[$index] UPDATED: '${currentItem.description}' → '${updatedItem.description}' | qty: ${currentItem.quantity} → ${updatedItem.quantity}")

                    currentItem.copy(
                        description = updatedItem.description,
                        quantity = updatedItem.quantity,
                        unitPrice = updatedItem.unitPrice
                    )
                } else {
                    // NEW ITEM ADDED - create new LineItemForm
                    Timber.d("   Item[$index] ADDED: '${updatedItem.description}' | qty: ${updatedItem.quantity} | price: ${updatedItem.unitPrice}")

                    LineItemForm(
                        transientId = java.util.UUID.randomUUID(),
                        description = updatedItem.description,
                        quantity = updatedItem.quantity,
                        unitPrice = updatedItem.unitPrice
                    )
                }

                newItems.add(itemForm)
            }

            // Log deletions
            if (updatedItems.size < currentItems.size) {
                Timber.d("   ${currentItems.size - updatedItems.size} items REMOVED")
            }

            state.copy(items = newItems)
        }

        Timber.d("✅ updateLineItemsFromEditor complete: ${_uiState.value.items.size} items in state")
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
            Timber.d("═══════════════════════════════════════════════════════════════════════════")
            Timber.d("🎬 CreateInvoiceViewModel.onSaveClicked() - STARTING INVOICE SAVE FLOW")
            Timber.d("═══════════════════════════════════════════════════════════════════════════")
            _uiState.update { it.copy(isSaving = true) }
            try {
                Timber.d("🔵 STEP 1: INVOICE SAVE STARTED")
                val state = _uiState.value
                val customer = state.selectedCustomer ?: throw Exception("Please select a customer")
                Timber.d("✅ STEP 2: Customer selected: ${customer.name} (ID=${customer.id})")

                val businessProfile = businessProfileRepository.activeProfile.first()
                Timber.d("✅ STEP 3: Active business profile loaded:")
                Timber.d("   - Business ID: ${businessProfile.id}")
                Timber.d("   - Business Name: ${businessProfile.businessName}")
                Timber.d("   - Tax Registered: ${businessProfile.isTaxRegistered}")
                Timber.d("   - Tax Rate: ${businessProfile.defaultTaxRate}")

                val lineItems = state.items.map { it.toDomain() }
                Timber.d("✅ STEP 4: Line items mapped: ${lineItems.size} items")
                lineItems.forEachIndexed { idx, item ->
                    Timber.d("   [$idx] ${item.description} x${item.quantity} @ ${item.unitPrice} cents")
                }

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
                Timber.d("✅ STEP 5: Metrics calculated:")
                Timber.d("   - Subtotal: ${metrics.subtotal} cents")
                Timber.d("   - Tax (${(taxRate * 100).toInt()}%): ${metrics.taxAmount} cents")
                Timber.d("   - Total: ${metrics.totalAmount} cents")

                val createdAt = System.currentTimeMillis()
                val dueDate = createdAt + (30L * 24 * 60 * 60 * 1000)

                // 🔥 CRITICAL FIX: Use the businessId from navigation route, NOT the active profile ID
                // This ensures the invoice is saved to the business being viewed, not always to the default
                val businessIdToUse = _businessId ?: businessProfile.id
                Timber.d("🔥 CRITICAL: Using businessId=$businessIdToUse for invoice (_businessId=$_businessId, activeProfile=${businessProfile.id})")

                val invoice = Invoice(
                    businessProfileId = businessIdToUse,  // 🔥 CRITICAL: Use navigation businessId, not active profile
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

                Timber.d("✅ STEP 6: Invoice object created:")
                Timber.d("   - Invoice ID (before save): NOT YET ASSIGNED")
                Timber.d("   - Business Profile ID: ${invoice.businessProfileId} 🔥 THIS IS CRITICAL FOR FILTERING")
                Timber.d("   - Customer: ${invoice.customerName} (ID=${invoice.customerId})")
                Timber.d("   - Total: ${invoice.totalAmount} cents")
                Timber.d("   - Items: ${invoice.items.size}")

                // 🔒 VALIDATION: Verify invoice meets all business rules before saving
                val validationResult = ValidationRules.validateInvoice(invoice)
                if (validationResult.isFailure()) {
                    val errorMessage = validationResult.getErrorOrNull() ?: "Unknown validation error"
                    Timber.w("⚠️ STEP 7: VALIDATION FAILED: $errorMessage")
                    _uiState.update { it.copy(error = errorMessage, isSaving = false) }
                    return@launch
                }
                Timber.d("✅ STEP 7: Invoice passed all validation rules")

                val invoiceId = invoiceRepository.saveInvoice(invoice).getOrThrow()
                Timber.d("✅ STEP 8: Invoice SAVED to database:")
                Timber.d("   - Invoice ID (from DB): $invoiceId")
                Timber.d("   - Business Profile ID: ${invoice.businessProfileId}")
                Timber.d("   - Customer: ${invoice.customerName} (ID=${invoice.customerId})")
                Timber.d("   - Amount: ${invoice.totalAmount} cents")
                Timber.d("   - Items: ${invoice.items.size}")
                Timber.d("   🔥 CRITICAL: When invoice list loads, it will filter by businessProfileId=${invoice.businessProfileId}")
                Timber.d("   🔥 If the list uses a different businessProfileId, the invoice WON'T APPEAR!")

                val invoiceWithId = invoice.copy(id = invoiceId)

                // 📊 Track invoice creation event
                eventTracker.trackInvoiceCreated(
                    invoiceId = invoiceId,
                    customerId = invoice.customerId ?: 0L,
                    amount = invoice.totalAmount,
                    currencyCode = invoice.currencyCode,
                    lineItemCount = invoice.items.size
                )

                Timber.d("✅ STEP 9: Firebase event tracked")
                Timber.d("🔵 STEP 10: Loading invoice settings to get selected theme...")

                // Load invoice settings to get the selected theme (FIX: Cause #2)
                val invoiceSettings = try {
                    invoiceSettingsRepository.getSettings(currentUserId)
                } catch (e: Exception) {
                    Timber.w(e, "Failed to load invoice settings, using default theme")
                    null
                }
                val selectedTheme = invoiceSettings?.selectedTheme
                Timber.d("✅ STEP 10a: Selected theme: ${selectedTheme?.name ?: "DEFAULT (CANVAS)"}")
                Timber.d("   invoiceSettings is null: ${invoiceSettings == null}")
                Timber.d("   selectedTheme is null: ${selectedTheme == null}")
                if (invoiceSettings != null) {
                    Timber.d("   Full settings: $invoiceSettings")
                }

                Timber.d("🔵 STEP 10b: Starting PDF generation with theme...")
                Timber.d("   About to call generateAndSaveInvoiceUseCase with theme=${selectedTheme?.name ?: "NULL"}")
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
                    overwriteExisting = true,
                    theme = selectedTheme  // FIX: Cause #1 - Pass theme parameter!
                )

                if (result.isSuccess) {
                    Timber.d("✅ STEP 11: PDF generation successful")
                    Timber.d("🎯 STEP 12: SETTING saveSuccess = true to trigger LaunchedEffect")
                    _uiState.update { state ->
                        state.copy(isSaving = false, saveSuccess = true).also {
                            Timber.d("✅ STEP 13: State updated: isSaving=false, saveSuccess=true")
                            Timber.d("   Current state will trigger LaunchedEffect in CreateInvoiceScreenV2")
                            Timber.d("   Which will call onCreate() → navController.popBackStack()")
                        }
                    }
                    Timber.d("═══════════════════════════════════════════════════════════════════════════")
                    Timber.d("✅ INVOICE SAVE COMPLETE - SUCCESS ✅")
                    Timber.d("═══════════════════════════════════════════════════════════════════════════")
                } else {
                    val error = result.exceptionOrNull() ?: Exception("Failed to generate PDF")
                    Timber.e(error, "❌ PDF generation failed")
                    throw error
                }

            } catch (e: Exception) {
                Timber.e("═══════════════════════════════════════════════════════════════════════════")
                Timber.e("❌ INVOICE SAVE FAILED ❌")
                Timber.e("═══════════════════════════════════════════════════════════════════════════")
                Timber.e(e, "Exception: ${e.message}")
                Timber.e("Stack trace:")
                e.stackTraceToString().split("\n").forEach { line ->
                    Timber.e("  $line")
                }
                Timber.e("═══════════════════════════════════════════════════════════════════════════")
                _uiState.update { it.copy(error = e.message, isSaving = false) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Reset form after successful save - allows user to create another invoice.
     * Called by navigation after onCreate() completes.
     */
    fun resetFormState() {
        Timber.d("🔄 resetFormState: Clearing form for next invoice")
        _uiState.update {
            it.copy(
                selectedCustomer = null,
                items = listOf(LineItemForm()),
                header = "",
                subheader = "",
                notes = "",
                footer = "",
                photoUris = emptyList(),
                isSaving = false,
                saveSuccess = false,
                error = null
            )
        }
    }

    // Phase 2: Customization functions
    fun updateCompanyName(name: String) {
        _uiState.update { it.copy(companyName = name) }
    }

    fun updateTemplateType(template: String) {
        _uiState.update { it.copy(templateType = template) }
    }
}
