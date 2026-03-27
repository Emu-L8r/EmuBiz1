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
 */
@HiltViewModel
class InvoiceDetailViewModelV2 @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val invoiceDao: InvoiceDao,
    private val paymentRepositoryV2: PaymentRepositoryV2,
    private val pdfGenerationService: PdfGenerationService,
    private val businessProfileRepository: BusinessProfileRepository,
    private val documentRepository: DocumentRepository
) : ViewModel() {

    private val route: ScreenV2.InvoiceDetail = savedStateHandle.toRoute()
    val businessId: Long = route.businessId
    val invoiceId: Long = route.invoiceId

    val uiState: StateFlow<InvoiceDetailUiStateV2> =
        invoiceDao.getInvoiceWithItemsById(invoiceId)
            .map<InvoiceWithItems?, InvoiceDetailUiStateV2> { invoice ->
                if (invoice == null) {
                    Timber.w("InvoiceDetailViewModelV2: invoice $invoiceId not found")
                    InvoiceDetailUiStateV2.NotFound
                } else {
                    Timber.d("InvoiceDetailViewModelV2: invoice $invoiceId loaded")
                    InvoiceDetailUiStateV2.Success(invoice)
                }
            }
            .catch { error ->
                Timber.e(error, "InvoiceDetailViewModelV2: error loading invoice $invoiceId")
                emit(InvoiceDetailUiStateV2.Error(error.message ?: "Unknown error"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = InvoiceDetailUiStateV2.Loading
            )

    private val _paymentEvent = MutableSharedFlow<String>()
    val paymentEvent: SharedFlow<String> = _paymentEvent.asSharedFlow()

    // ===== PDF EXPORT STATE =====
    private val _pdfExportState = MutableStateFlow<PdfExportState>(PdfExportState.Idle)
    val pdfExportState: StateFlow<PdfExportState> = _pdfExportState.asStateFlow()

    private val _pdfFile = MutableSharedFlow<File>()
    val pdfFile: SharedFlow<File> = _pdfFile.asSharedFlow()

    // ...existing code...

    fun recordPayment(amount: Long) {
        viewModelScope.launch {
            try {
                val invoice = invoiceDao.getInvoiceById(invoiceId) ?: return@launch
                val remaining = invoice.totalAmount - invoice.amountPaid
                if (amount <= 0) {
                    _paymentEvent.emit("Payment amount must be greater than zero.")
                    return@launch
                }
                if (amount > remaining) {
                    _paymentEvent.emit(
                        "Payment exceeds the outstanding balance of ${CentsFormatter.formatCents(remaining)}."
                    )
                    return@launch
                }
                val newAmountPaid = invoice.amountPaid + amount
                Timber.d("InvoiceDetailViewModelV2: Recording payment of $amount cents")
                invoiceDao.updateAmountPaid(invoiceId, newAmountPaid)

                // Update status based on payment
                val newStatus = if (newAmountPaid >= invoice.totalAmount) {
                    com.emul8r.bizap.domain.model.InvoiceStatus.PAID
                } else {
                    com.emul8r.bizap.domain.model.InvoiceStatus.PARTIALLY_PAID
                }
                invoiceDao.updateStatus(invoiceId, newStatus)
                Timber.d("InvoiceDetailViewModelV2: Status updated to $newStatus")

                Timber.d("InvoiceDetailViewModelV2: Payment recorded successfully")
                _paymentEvent.emit("Payment recorded successfully.")
            } catch (e: Exception) {
                Timber.e(e, "InvoiceDetailViewModelV2: Failed to record payment")
                _paymentEvent.emit("Failed to record payment: ${e.message}")
            }
        }
    }

    fun updateInvoiceStatus(newStatus: com.emul8r.bizap.domain.model.InvoiceStatus) {
        viewModelScope.launch {
            try {
                Timber.d("InvoiceDetailViewModelV2: Updating status to $newStatus")
                if (newStatus == InvoiceStatus.PAID) {
                    paymentRepositoryV2.markInvoiceAsPaid(invoiceId, businessId)
                        .onSuccess {
                            Timber.d("InvoiceDetailViewModelV2: Invoice marked as paid and payment auto-recorded")
                            _paymentEvent.emit("Invoice marked as paid and payment auto-recorded.")
                        }
                        .onFailure { e ->
                            Timber.e(e, "InvoiceDetailViewModelV2: Failed to mark invoice as paid")
                            _paymentEvent.emit("Failed to mark invoice as paid: ${e.message}")
                        }
                } else {
                    invoiceDao.updateStatus(invoiceId, newStatus)
                    Timber.d("InvoiceDetailViewModelV2: Status updated successfully")
                    _paymentEvent.emit("Status updated to ${newStatus.name}.")
                }
            } catch (e: Exception) {
                Timber.e(e, "InvoiceDetailViewModelV2: Failed to update status")
            }
        }
    }

    // ===== PDF EXPORT =====
    fun exportToPdf(invoice: InvoiceWithItems) {
        viewModelScope.launch {
            try {
                _pdfExportState.value = PdfExportState.Loading
                Timber.d("InvoiceDetailViewModelV2: Starting PDF export for invoice $invoiceId")

                // Fetch business profile from repository
                val businessProfile = businessProfileRepository.activeProfile.firstOrNull()
                    ?: run {
                        Timber.e("InvoiceDetailViewModelV2: Business profile not available")
                        _pdfExportState.value = PdfExportState.Error("Business profile not found")
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
                    // ✅ NOW FILLED WITH REAL DATA (WAS EMPTY BEFORE)
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

                // ✅ GENERATE BOTH QUOTE AND INVOICE (LIKE GUI1)
                val quoteResult = runCatching {
                    pdfGenerationService.generatePdf(
                        snapshot = snapshot,
                        isQuote = true,
                        overwriteExisting = true
                    )
                }

                val invoiceResult = runCatching {
                    pdfGenerationService.generatePdf(
                        snapshot = snapshot,
                        isQuote = false,
                        overwriteExisting = true
                    )
                }

                if (quoteResult.isFailure || invoiceResult.isFailure) {
                    val error = quoteResult.exceptionOrNull() ?: invoiceResult.exceptionOrNull()
                    Timber.e(error, "InvoiceDetailViewModelV2: Failed to generate PDF")
                    val errorMsg = error?.message ?: "Failed to generate PDF"
                    _pdfExportState.value = PdfExportState.Error(errorMsg)
                    return@launch
                }

                val quotePdf = quoteResult.getOrThrow()
                val invoicePdf = invoiceResult.getOrThrow()

                Timber.d("InvoiceDetailViewModelV2: Both PDFs generated successfully")
                Timber.d("  Quote PDF: ${quotePdf.absolutePath}")
                Timber.d("  Invoice PDF: ${invoicePdf.absolutePath}")

                // ✅ INSERT BOTH INTO VAULT
                try {
                    // Insert Quote
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

                    // Insert Invoice
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
                    // Don't fail - the PDF files are still valid
                }

                _pdfExportState.value = PdfExportState.Success(invoicePdf)
                _pdfFile.emit(invoicePdf)

            } catch (e: Exception) {
                Timber.e(e, "InvoiceDetailViewModelV2: Failed to export PDF - ${e.message}")
                val errorMsg = e.message ?: "Unknown error occurred during PDF export"
                _pdfExportState.value = PdfExportState.Error(errorMsg)
                _paymentEvent.emit("PDF Export Failed: $errorMsg")
            }
        }
    }
}

sealed class PdfExportState {
    object Idle : PdfExportState()
    object Loading : PdfExportState()
    data class Success(val file: File) : PdfExportState()
    data class Error(val message: String) : PdfExportState()
}

sealed class InvoiceDetailUiStateV2 {
    object Loading : InvoiceDetailUiStateV2()
    object NotFound : InvoiceDetailUiStateV2()
    data class Success(val invoice: InvoiceWithItems) : InvoiceDetailUiStateV2()
    data class Error(val message: String) : InvoiceDetailUiStateV2()
}
