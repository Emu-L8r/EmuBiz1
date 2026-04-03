package com.emul8r.bizap.ui.gui2.invoice

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.emul8r.bizap.data.local.InvoiceDao
import com.emul8r.bizap.data.local.entities.InvoiceWithItems
import com.emul8r.bizap.data.local.entities.GeneratedDocumentEntity
import com.emul8r.bizap.data.local.entities.DocumentStatus
import com.emul8r.bizap.data.repository.gui2.PaymentRepositoryV2
import com.emul8r.bizap.data.repository.InvoiceSettingsRepository
import com.emul8r.bizap.di.UserIdProvider
import com.emul8r.bizap.domain.model.InvoiceSnapshot
import com.emul8r.bizap.domain.model.InvoiceStatus
import com.emul8r.bizap.domain.repository.BusinessProfileRepository
import com.emul8r.bizap.domain.repository.DocumentRepository
import com.emul8r.bizap.domain.service.PdfGenerationService
import com.emul8r.bizap.ui.gui2.navigation.ScreenV2
import com.emul8r.bizap.utils.CentsFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import javax.inject.Inject

/**
 * ViewModel for the GUI2 invoice detail screen.
 * businessId and invoiceId are guaranteed non-null from the navigation route.
 *
 * Features:
 * - Load and display invoice details
 * - Record payments
 * - Update invoice status
 * - Generate PDF exports
 *
 * STATE MANAGEMENT: Uses unified dialog state in InvoiceDetailUiStateV2.Success
 * - All dialog states (which dialog is open, loading flags, error states) live in uiState
 * - Single source of truth for all operations
 */
@HiltViewModel
class InvoiceDetailViewModelV2 @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val invoiceDao: InvoiceDao,
    private val paymentRepositoryV2: PaymentRepositoryV2,
    private val pdfGenerationService: PdfGenerationService,
    private val businessProfileRepository: BusinessProfileRepository,
    private val documentRepository: DocumentRepository,
    private val invoiceSettingsRepository: InvoiceSettingsRepository,
    private val userIdProvider: UserIdProvider
) : ViewModel() {

    private val route: ScreenV2.InvoiceDetail = savedStateHandle.toRoute()
    val businessId: Long = route.businessId
    val invoiceId: Long = route.invoiceId

    private val _uiState = MutableStateFlow<InvoiceDetailUiStateV2>(InvoiceDetailUiStateV2.Loading)
    val uiState: StateFlow<InvoiceDetailUiStateV2> = _uiState.asStateFlow()

    init {
        loadInvoice()
    }

    private fun loadInvoice() {
        viewModelScope.launch {
            invoiceDao.getInvoiceWithItemsById(invoiceId)
                .collect { invoice ->
                    _uiState.value = if (invoice == null) {
                        Timber.w("InvoiceDetailViewModelV2: invoice $invoiceId not found")
                        InvoiceDetailUiStateV2.NotFound
                    } else {
                        Timber.d("InvoiceDetailViewModelV2: invoice $invoiceId loaded")
                        InvoiceDetailUiStateV2.Success(invoice)
                    }
                }
        }
    }

    // ===== DIALOG CONTROL =====
    fun openPaymentDialog() {
        val currentState = _uiState.value
        if (currentState is InvoiceDetailUiStateV2.Success) {
            _uiState.value = currentState.copy(
                dialogState = DialogState.PaymentDialog,
                paymentError = null
            )
        }
    }

    fun openStatusMenu() {
        val currentState = _uiState.value
        if (currentState is InvoiceDetailUiStateV2.Success) {
            _uiState.value = currentState.copy(
                dialogState = DialogState.StatusMenu,
                statusUpdateError = null
            )
        }
    }

    fun openPdfExport() {
        val currentState = _uiState.value
        if (currentState is InvoiceDetailUiStateV2.Success) {
            _uiState.value = currentState.copy(
                dialogState = DialogState.PdfExport.Loading
            )
            // Trigger export immediately
            exportToPdf(currentState.invoice)
        }
    }

    fun closeDialog() {
        val currentState = _uiState.value
        if (currentState is InvoiceDetailUiStateV2.Success) {
            _uiState.value = currentState.copy(
                dialogState = DialogState.None,
                paymentError = null,
                statusUpdateError = null
            )
        }
    }

    // ===== PAYMENT =====
    fun recordPayment(amount: Long) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState !is InvoiceDetailUiStateV2.Success) return@launch

            // Set loading state
            _uiState.value = currentState.copy(
                paymentLoading = true,
                paymentError = null
            )

            try {
                val invoice = invoiceDao.getInvoiceById(invoiceId) ?: run {
                    _uiState.value = currentState.copy(
                        paymentLoading = false,
                        paymentError = "Invoice not found"
                    )
                    return@launch
                }

                val remaining = invoice.totalAmount - invoice.amountPaid
                if (amount <= 0) {
                    _uiState.value = currentState.copy(
                        paymentLoading = false,
                        paymentError = "Payment amount must be greater than zero."
                    )
                    return@launch
                }
                if (amount > remaining) {
                    _uiState.value = currentState.copy(
                        paymentLoading = false,
                        paymentError = "Payment exceeds the outstanding balance of ${CentsFormatter.formatCents(remaining)}."
                    )
                    return@launch
                }

                val newAmountPaid = invoice.amountPaid + amount
                Timber.d("InvoiceDetailViewModelV2: Recording payment of $amount cents")
                invoiceDao.updateAmountPaid(invoiceId, newAmountPaid)

                // Update status based on payment
                val newStatus = if (newAmountPaid >= invoice.totalAmount) {
                    InvoiceStatus.PAID
                } else {
                    InvoiceStatus.PARTIALLY_PAID
                }
                invoiceDao.updateStatus(invoiceId, newStatus)
                Timber.d("InvoiceDetailViewModelV2: Status updated to $newStatus")
                Timber.d("InvoiceDetailViewModelV2: Payment recorded successfully")

                // Close dialog on success
                _uiState.value = currentState.copy(
                    dialogState = DialogState.None,
                    paymentLoading = false,
                    paymentError = null
                )
            } catch (e: Exception) {
                Timber.e(e, "InvoiceDetailViewModelV2: Failed to record payment")
                _uiState.value = currentState.copy(
                    paymentLoading = false,
                    paymentError = "Failed to record payment: ${e.message}"
                )
            }
        }
    }

    // ===== STATUS UPDATE =====
    fun updateInvoiceStatus(newStatus: InvoiceStatus) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState !is InvoiceDetailUiStateV2.Success) return@launch

            try {
                Timber.d("InvoiceDetailViewModelV2: Updating status to $newStatus")
                if (newStatus == InvoiceStatus.PAID) {
                    paymentRepositoryV2.markInvoiceAsPaid(invoiceId, businessId)
                        .onSuccess {
                            Timber.d("InvoiceDetailViewModelV2: Invoice marked as paid and payment auto-recorded")
                            _uiState.value = currentState.copy(
                                dialogState = DialogState.None,
                                statusUpdateError = null
                            )
                        }
                        .onFailure { e ->
                            Timber.e(e, "InvoiceDetailViewModelV2: Failed to mark invoice as paid")
                            _uiState.value = currentState.copy(
                                statusUpdateError = "Failed to mark invoice as paid: ${e.message}"
                            )
                        }
                } else {
                    invoiceDao.updateStatus(invoiceId, newStatus)
                    Timber.d("InvoiceDetailViewModelV2: Status updated successfully")
                    _uiState.value = currentState.copy(
                        dialogState = DialogState.None,
                        statusUpdateError = null
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "InvoiceDetailViewModelV2: Failed to update status")
                _uiState.value = currentState.copy(
                    statusUpdateError = "Failed to update status: ${e.message}"
                )
            }
        }
    }

    // ===== PDF EXPORT =====
    fun exportToPdf(invoice: InvoiceWithItems) {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                if (currentState !is InvoiceDetailUiStateV2.Success) return@launch

                // Update to Loading state
                _uiState.value = currentState.copy(
                    dialogState = DialogState.PdfExport.Loading
                )

                Timber.d("InvoiceDetailViewModelV2: Starting PDF export for invoice $invoiceId")

                // Fetch business profile from repository
                val businessProfile = businessProfileRepository.activeProfile.firstOrNull()
                    ?: run {
                        Timber.e("InvoiceDetailViewModelV2: Business profile not available")
                        _uiState.value = currentState.copy(
                            dialogState = DialogState.PdfExport.Error("Business profile not found")
                        )
                        return@launch
                    }

                Timber.d("InvoiceDetailViewModelV2: Business profile loaded: ${businessProfile.businessName}")

                // Calculate subtotal from items
                val subtotal = invoice.items.sumOf { (it.unitPrice * it.quantity).toLong() }

                // Create invoice snapshot with REAL business data
                val snapshot = InvoiceSnapshot(
                    invoiceId = invoice.invoice.id,
                    invoiceNumber = invoice.invoice.invoiceNumber,
                    displayName = invoice.invoice.displayName,
                    customerName = invoice.invoice.customerName,
                    customerAddress = invoice.invoice.customerAddress,
                    customerEmail = invoice.invoice.customerEmail,
                    date = invoice.invoice.date,
                    dueDate = invoice.invoice.dueDate,
                    items = invoice.items.map { item ->
                        com.emul8r.bizap.domain.model.LineItemSnapshot(
                            description = item.description,
                            quantity = item.quantity,
                            unitPrice = item.unitPrice.toLong(),
                            total = (item.unitPrice * item.quantity).toLong()
                        )
                    },
                    subtotal = subtotal,
                    taxRate = invoice.invoice.taxRate,
                    taxAmount = invoice.invoice.taxAmount,
                    totalAmount = invoice.invoice.totalAmount,
                    businessName = businessProfile.businessName,
                    businessAbn = businessProfile.abn ?: "",
                    businessEmail = businessProfile.email ?: "",
                    businessPhone = businessProfile.phone ?: "",
                    businessAddress = businessProfile.address ?: "",
                    logoBase64 = businessProfile.logoBase64,
                    currencyCode = "AUD",
                    headerText = invoice.invoice.header ?: "",
                    subheaderText = invoice.invoice.subheader ?: "",
                    footerText = invoice.invoice.footer ?: "",
                    notes = invoice.invoice.notes ?: "",
                    bankAccountName = businessProfile.accountName ?: "",
                    bankAccountNumber = businessProfile.accountNumber ?: "",
                    bankBsb = businessProfile.bsbNumber ?: "",
                    bankName = businessProfile.bankName ?: "",
                    invoiceStatus = invoice.invoice.status
                )

                Timber.d("InvoiceDetailViewModelV2: Invoice snapshot created successfully")

                // Load invoice settings
                val userId = userIdProvider.getCurrentUserId()
                Timber.d("InvoiceDetailViewModelV2: Loading settings for userId: $userId")

                val invoiceSettings = try {
                    val loadedSettings = invoiceSettingsRepository.getSettings(userId)
                    Timber.d("InvoiceDetailViewModelV2: ═════════════════════════════════════════════")
                    Timber.d("InvoiceDetailViewModelV2: SETTINGS LOADED FOR PDF GENERATION")
                    Timber.d("InvoiceDetailViewModelV2: ═════════════════════════════════════════════")
                    if (loadedSettings != null) {
                        Timber.d("✅ Settings loaded successfully")
                        Timber.d("   - userId: $userId")
                        Timber.d("   - selectedTheme: ${loadedSettings.selectedTheme.name}")
                        Timber.d("   - selectedHtmlStyle: ${loadedSettings.selectedHtmlStyle.displayName}")
                        Timber.d("   - CSS file: ${loadedSettings.selectedHtmlStyle.styleFile}")
                        Timber.d("InvoiceDetailViewModelV2: ═════════════════════════════════════════════")
                    } else {
                        Timber.w("⚠️  Settings returned NULL - using defaults")
                    }
                    loadedSettings
                } catch (e: Exception) {
                    Timber.w(e, "InvoiceDetailViewModelV2: Failed to load settings, using defaults")
                    null
                }
                val selectedTheme = invoiceSettings?.selectedTheme
                Timber.d("InvoiceDetailViewModelV2: Theme for PDF: ${selectedTheme?.name ?: "NULL (will use default)"}")

                // Generate both quote and invoice
                val quoteResult = runCatching {
                    pdfGenerationService.generatePdf(
                        snapshot = snapshot,
                        isQuote = true,
                        overwriteExisting = true,
                        theme = selectedTheme
                    )
                }

                val invoiceResult = runCatching {
                    pdfGenerationService.generatePdf(
                        snapshot = snapshot,
                        isQuote = false,
                        overwriteExisting = true,
                        theme = selectedTheme
                    )
                }

                if (quoteResult.isFailure || invoiceResult.isFailure) {
                    val error = quoteResult.exceptionOrNull() ?: invoiceResult.exceptionOrNull()
                    Timber.e(error, "InvoiceDetailViewModelV2: Failed to generate PDF")
                    val errorMsg = error?.message ?: "Failed to generate PDF"
                    _uiState.value = currentState.copy(
                        dialogState = DialogState.PdfExport.Error(errorMsg)
                    )
                    return@launch
                }

                val quotePdf = quoteResult.getOrThrow()
                val invoicePdf = invoiceResult.getOrThrow()

                Timber.d("InvoiceDetailViewModelV2: Both PDFs generated successfully")
                Timber.d("  Quote PDF: ${quotePdf.absolutePath}")
                Timber.d("  Invoice PDF: ${invoicePdf.absolutePath}")

                // Insert both into vault
                try {
                    val quoteDoc = GeneratedDocumentEntity(
                        id = 0,
                        relatedInvoiceId = invoiceId,
                        fileName = quotePdf.name,
                        absolutePath = quotePdf.absolutePath,
                        fileType = "Quote",
                        createdAt = System.currentTimeMillis(),
                        status = DocumentStatus.ARCHIVED
                    )
                    documentRepository.insertDocument(quoteDoc).getOrThrow()
                    Timber.d("InvoiceDetailViewModelV2: ✅ Quote PDF saved to vault - Invoice #$invoiceId")

                    val invoiceDoc = GeneratedDocumentEntity(
                        id = 0,
                        relatedInvoiceId = invoiceId,
                        fileName = invoicePdf.name,
                        absolutePath = invoicePdf.absolutePath,
                        fileType = "Invoice",
                        createdAt = System.currentTimeMillis(),
                        status = DocumentStatus.ARCHIVED
                    )
                    documentRepository.insertDocument(invoiceDoc).getOrThrow()
                    Timber.d("InvoiceDetailViewModelV2: ✅ Invoice PDF saved to vault - Invoice #$invoiceId")
                } catch (e: Exception) {
                    Timber.e(e, "InvoiceDetailViewModelV2: Failed to save PDFs to vault, but files exist")
                }

                // Update to Success state
                _uiState.value = currentState.copy(
                    dialogState = DialogState.PdfExport.Success(invoicePdf)
                )

            } catch (e: Exception) {
                Timber.e(e, "InvoiceDetailViewModelV2: Failed to export PDF - ${e.message}")
                val errorMsg = e.message ?: "Unknown error occurred during PDF export"
                val currentState = _uiState.value
                if (currentState is InvoiceDetailUiStateV2.Success) {
                    _uiState.value = currentState.copy(
                        dialogState = DialogState.PdfExport.Error(errorMsg)
                    )
                }
            }
        }
    }
}

// ===== DIALOG STATE =====
sealed class DialogState {
    object None : DialogState()
    object PaymentDialog : DialogState()
    object StatusMenu : DialogState()

    sealed class PdfExport : DialogState() {
        object Loading : PdfExport()
        data class Success(val file: File) : PdfExport()
        data class Error(val message: String) : PdfExport()
    }
}

// ===== UI STATE =====
sealed class InvoiceDetailUiStateV2 {
    object Loading : InvoiceDetailUiStateV2()
    object NotFound : InvoiceDetailUiStateV2()
    data class Success(
        val invoice: InvoiceWithItems,
        val dialogState: DialogState = DialogState.None,
        val paymentLoading: Boolean = false,
        val paymentError: String? = null,
        val statusUpdateError: String? = null
    ) : InvoiceDetailUiStateV2()
    data class Error(val message: String) : InvoiceDetailUiStateV2()
}

