package com.emul8r.bizap.ui.invoices

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.data.DocumentManager
import com.emul8r.bizap.domain.model.DocumentStatus
import com.emul8r.bizap.domain.model.GeneratedDocument
import com.emul8r.bizap.domain.analytics.AnalyticsRepository
import com.emul8r.bizap.domain.analytics.InvoiceAnalyticsEvent
import com.emul8r.bizap.domain.repository.BusinessProfileRepository
import com.emul8r.bizap.data.repository.InvoiceSettingsRepository
import com.emul8r.bizap.data.service.CsvExportService
import com.emul8r.bizap.data.service.InvoicePdfService
import com.emul8r.bizap.data.service.PrintService
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.InvoiceStatus
import com.emul8r.bizap.domain.repository.InvoiceRepository
import com.emul8r.bizap.domain.usecase.GenerateAndSaveInvoiceUseCase
import com.emul8r.bizap.domain.util.createDefaultSnapshot
import com.emul8r.bizap.domain.util.toSnapshot
import com.emul8r.bizap.utils.CentsFormatter
import com.emul8r.bizap.utils.DocumentNamingUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import javax.inject.Inject

sealed interface InvoiceDetailUiState {
    object Loading : InvoiceDetailUiState
    data class Success(
        val data: Invoice,
        val versions: List<Invoice> = emptyList() // PHASE 3A: Sibling versions for picker
    ) : InvoiceDetailUiState
    data class Error(val message: String) : InvoiceDetailUiState
}

sealed interface UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent
    data class NavigateToInvoice(val invoiceId: Long) : UiEvent
}

sealed interface InvoiceDetailEvent {
    object InvoiceDeleted : InvoiceDetailEvent
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class InvoiceDetailViewModel @Inject constructor(
    private val invoiceRepo: InvoiceRepository,
    private val analyticsRepository: AnalyticsRepository,
    private val documentRepository: com.emul8r.bizap.domain.repository.DocumentRepository,
    private val invoiceSettingsRepository: InvoiceSettingsRepository,
    private val pdfService: InvoicePdfService,
    private val csvExportService: CsvExportService,
    private val businessProfileRepository: BusinessProfileRepository,
    private val printService: PrintService,
    private val documentManager: DocumentManager,
    private val generateAndSaveInvoiceUseCase: GenerateAndSaveInvoiceUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _currentInvoiceId = MutableStateFlow<Long>(checkNotNull(savedStateHandle["invoiceId"]))

    val uiState: StateFlow<InvoiceDetailUiState> = _currentInvoiceId
        .flatMapLatest { id ->
            invoiceRepo.getInvoiceWithItemsById(id)
                .map { invoice ->
                    if (invoice == null) {
                        InvoiceDetailUiState.Error("Invoice not found")
                    } else {
                        // TODO: Re-enable version grouping after getInvoiceGroupWithVersions is implemented
                        InvoiceDetailUiState.Success(invoice, emptyList())
                    }
                }
        }
        .catch { e ->
            Timber.e(e, "Failed to load invoice")
            emit(InvoiceDetailUiState.Error(e.message ?: "Unknown error"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = InvoiceDetailUiState.Loading
        )

    private val _exportEvent = MutableSharedFlow<File>()
    val exportEvent = _exportEvent.asSharedFlow()

    private val _csvExportEvent = MutableSharedFlow<File>()
    val csvExportEvent = _csvExportEvent.asSharedFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val _event = MutableSharedFlow<InvoiceDetailEvent>()
    val event = _event.asSharedFlow()

    private val _showOverwriteDialog = MutableStateFlow<PdfOverwriteState?>(null)
    val showOverwriteDialog = _showOverwriteDialog.asStateFlow()

    data class PdfOverwriteState(
        val fileName: String,
        val isQuote: Boolean,
        val share: Boolean
    )

    fun loadInvoice(id: Long) {
        _currentInvoiceId.value = id
    }

    /**
     * PHASE 3A: RECORD PAYMENT
     * Validates that the payment does not exceed the remaining balance before recording.
     */
    fun recordPayment(amount: Long) {
        val currentState = uiState.value as? InvoiceDetailUiState.Success ?: return
        val invoice = currentState.data

        val remaining = invoice.totalAmount - invoice.amountPaid
        if (amount <= 0) {
            viewModelScope.launch {
                _uiEvent.emit(UiEvent.ShowSnackbar("Payment amount must be greater than zero."))
            }
            return
        }
        if (amount > remaining) {
            viewModelScope.launch {
                _uiEvent.emit(
                    UiEvent.ShowSnackbar(
                        "Payment of ${CentsFormatter.formatCents(amount)} exceeds the outstanding balance of ${CentsFormatter.formatCents(remaining)}."
                    )
                )
            }
            return
        }

        viewModelScope.launch {
            try {
                val newAmountPaid = invoice.amountPaid + amount
                val newStatus = if (newAmountPaid >= invoice.totalAmount) InvoiceStatus.PAID else InvoiceStatus.PARTIALLY_PAID

                invoiceRepo.updateAmountPaid(invoice.id, newAmountPaid).getOrThrow()
                invoiceRepo.updateInvoiceStatus(invoice.id, newStatus).getOrThrow()

                _uiEvent.emit(UiEvent.ShowSnackbar("Payment of ${CentsFormatter.formatCents(amount)} recorded."))
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.ShowSnackbar("Failed to record payment: ${e.message}"))
            }
        }
    }

    /**
     * PHASE 3A: CREATE CORRECTION (Cloning to v+1)
     */
    fun createCorrection() {
        val currentState = uiState.value as? InvoiceDetailUiState.Success ?: return
        val original = currentState.data

        viewModelScope.launch {
            invoiceRepo.createCorrection(original.id)
                .onSuccess { newId ->
                    _uiEvent.emit(UiEvent.NavigateToInvoice(newId))
                    _uiEvent.emit(UiEvent.ShowSnackbar("New version created. Editing v${original.version + 1}."))
                }
                .onFailure { e ->
                    _uiEvent.emit(UiEvent.ShowSnackbar("Correction failed: ${e.message}"))
                }
        }
    }

    fun updateStatus(invoiceId: Long, newStatus: String) {
        viewModelScope.launch {
            try {
                val status = InvoiceStatus.valueOf(newStatus)
                invoiceRepo.updateInvoiceStatus(invoiceId, status)
                    .onSuccess {
                        Timber.d("✅ Invoice status updated to $newStatus")

                        // Log event to analytics
                        val businessId = (uiState.value as? InvoiceDetailUiState.Success)
                            ?.data?.businessProfileId ?: 0L
                        viewModelScope.launch {
                            analyticsRepository.logEvent(
                                InvoiceAnalyticsEvent.StatusChanged(
                                    businessId = businessId,
                                    invoiceId = invoiceId,
                                    newStatus = newStatus,
                                    timestamp = System.currentTimeMillis()
                                )
                            )
                        }

                        val message = if (status == InvoiceStatus.PAID) {
                            "Invoice marked as paid and payment auto-recorded."
                        } else {
                            "Status updated to $newStatus"
                        }
                        _uiEvent.emit(UiEvent.ShowSnackbar(message))
                    }
                    .onFailure { e ->
                        Timber.e(e, "❌ Failed to update status")
                        _uiEvent.emit(UiEvent.ShowSnackbar("Failed to update status: ${e.message}"))
                    }
            } catch (e: IllegalArgumentException) {
                Timber.e(e, "❌ Invalid status value: $newStatus")
                _uiEvent.emit(UiEvent.ShowSnackbar("Invalid status: $newStatus"))
            }
        }
    }

    fun shareInternalPdf() {
        com.emul8r.bizap.utils.logging.ErrorExportLogger.logPdfAttempt(
            invoiceId = (uiState.value as? InvoiceDetailUiState.Success)?.data?.id ?: 0,
            type = "Invoice"
        )
        checkAndProceedWithPdfGeneration(share = true)
    }

    fun exportToDownloads() {
        com.emul8r.bizap.utils.logging.ErrorExportLogger.logPdfAttempt(
            invoiceId = (uiState.value as? InvoiceDetailUiState.Success)?.data?.id ?: 0,
            type = "Invoice"
        )
        checkAndProceedWithPdfGeneration(share = false)
    }

    fun exportAsCsv() {
        val invoice = (uiState.value as? InvoiceDetailUiState.Success)?.data ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val csvFile = csvExportService.exportSingleInvoice(invoice)
                _csvExportEvent.emit(csvFile)
            } catch (e: Exception) {
                Timber.e(e, "CSV export failed")
                _uiEvent.emit(UiEvent.ShowSnackbar("CSV export failed: ${e.message}"))
            }
        }
    }

    private fun checkAndProceedWithPdfGeneration(share: Boolean) {
        val invoiceData = (uiState.value as? InvoiceDetailUiState.Success)?.data ?: return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = pdfService.checkIfPdfExists(invoiceData.id, "Invoice")
                val invoiceExists = result.first
                val invoiceFileName = result.second

                if (invoiceExists && invoiceFileName != null) {
                    _showOverwriteDialog.value = PdfOverwriteState(
                        fileName = invoiceFileName,
                        isQuote = false,
                        share = share
                    )
                } else {
                    generateAndExportPdf(share = share, overwriteExisting = true)
                }
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.ShowSnackbar("Error checking PDF: ${e.message}"))
            }
        }
    }

    fun onOverwriteExisting() {
        val dialogState = _showOverwriteDialog.value ?: return
        _showOverwriteDialog.value = null
        generateAndExportPdf(share = dialogState.share, overwriteExisting = true)
    }

    fun onKeepBothVersions() {
        val dialogState = _showOverwriteDialog.value ?: return
        _showOverwriteDialog.value = null
        generateAndExportPdf(share = dialogState.share, overwriteExisting = false)
    }

    fun onDismissOverwriteDialog() {
        _showOverwriteDialog.value = null
    }

    private fun generateAndExportPdf(share: Boolean, overwriteExisting: Boolean = true) {
        val invoiceData = (uiState.value as? InvoiceDetailUiState.Success)?.data ?: return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val businessProfile = businessProfileRepository.activeProfile.first()

                // ✅ PHASE 2: Load invoice settings to pass all customization options to PDF generation
                val invoiceSettings = try {
                    invoiceSettingsRepository.getSettings("current_user")
                } catch (e: Exception) {
                    Timber.w(e, "Failed to load invoice settings, using defaults")
                    null
                }
                val selectedTheme = invoiceSettings?.selectedTheme
                Timber.d("📄 Loaded theme for PDF generation: ${selectedTheme?.name ?: "NULL (will use default)"}")
                Timber.d("✅ PHASE 2: Loaded customization settings - color: ${invoiceSettings?.selectedColorScheme?.name}, spacing: ${invoiceSettings?.selectedSpacingProfile?.name}")

                val snapshot = invoiceSettings?.toSnapshot(invoiceData, businessProfile)
                    ?: createDefaultSnapshot(invoiceData, businessProfile)

                val quoteResult = generateAndSaveInvoiceUseCase(
                    invoice = invoiceData,
                    snapshot = snapshot,
                    isQuote = true,
                    overwriteExisting = overwriteExisting,
                    theme = selectedTheme
                )

                val invoiceResult = generateAndSaveInvoiceUseCase(
                    invoice = invoiceData,
                    snapshot = snapshot,
                    isQuote = false,
                    overwriteExisting = overwriteExisting,
                    theme = selectedTheme
                )

                if (quoteResult.isSuccess && invoiceResult.isSuccess) {
                    val quotePdf = quoteResult.getOrThrow()
                    val invoicePdf = invoiceResult.getOrThrow()

                    // Pre-flight validation: Ensure files are shareable before emitting
                    if (!com.emul8r.bizap.utils.FileUriProvider.isFileSharable(quotePdf)) {
                        _uiEvent.emit(UiEvent.ShowSnackbar("Generated Quote PDF is invalid or inaccessible"))
                        return@launch
                    }
                    if (!com.emul8r.bizap.utils.FileUriProvider.isFileSharable(invoicePdf)) {
                        _uiEvent.emit(UiEvent.ShowSnackbar("Generated Invoice PDF is invalid or inaccessible"))
                        return@launch
                    }

                    invoiceRepo.updatePdfPath(invoiceData.id, invoicePdf.absolutePath).getOrThrow()

                    // 📝 INSERT DOCUMENTS INTO VAULT
                    // Create document records so they appear in the vault
                    try {
                        val quoteDoc = GeneratedDocument(
                            id = 0, // Auto-generated
                            relatedInvoiceId = invoiceData.id,
                            fileName = quotePdf.name,
                            absolutePath = quotePdf.absolutePath,
                            fileType = "Quote",
                            createdAt = System.currentTimeMillis(),
                            status = DocumentStatus.ARCHIVED
                        )
                        documentRepository.insertDocument(quoteDoc).getOrThrow()
                        Timber.d("✅ Vault: Inserted Quote PDF for invoice #${invoiceData.id}")

                        val invoiceDoc = GeneratedDocument(
                            id = 0, // Auto-generated
                            relatedInvoiceId = invoiceData.id,
                            fileName = invoicePdf.name,
                            absolutePath = invoicePdf.absolutePath,
                            fileType = "Invoice",
                            createdAt = System.currentTimeMillis(),
                            status = DocumentStatus.ARCHIVED
                        )
                        documentRepository.insertDocument(invoiceDoc).getOrThrow()
                        Timber.d("✅ Vault: Inserted Invoice PDF for invoice #${invoiceData.id}")
                        com.emul8r.bizap.utils.logging.ErrorExportLogger.logPdfSuccess(
                            invoiceId = invoiceData.id,
                            filePath = invoicePdf.absolutePath,
                            sizeBytes = invoicePdf.length(),
                            type = "Invoice"
                        )
                    } catch (e: Exception) {
                        Timber.e(e, "❌ Failed to insert documents into vault")
                        com.emul8r.bizap.utils.logging.ErrorExportLogger.logPdfFailure(
                            invoiceId = invoiceData.id,
                            error = e,
                            type = "Vault Insert"
                        )
                    }

                    if (share) {
                        _exportEvent.emit(invoicePdf)
                    } else {
                        val quotePublicUri = documentManager.saveToDownloads(quotePdf, quotePdf.name)
                        val invoicePublicUri = documentManager.saveToDownloads(invoicePdf, invoicePdf.name)

                        if (quotePublicUri != null && invoicePublicUri != null) {
                            _uiEvent.emit(UiEvent.ShowSnackbar("Success: Documents exported to Downloads/Bizap"))
                        } else {
                            _uiEvent.emit(UiEvent.ShowSnackbar("Partial Success: Check Downloads/Bizap folder"))
                        }
                    }
                } else {
                    val errorMsg = quoteResult.exceptionOrNull()?.message ?: invoiceResult.exceptionOrNull()?.message ?: "Unknown error"
                    _uiEvent.emit(UiEvent.ShowSnackbar("Atomic Save Failed: $errorMsg"))
                }
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.ShowSnackbar("Error processing PDF: ${e.message}"))
            }
        }
    }

    fun launchSystemPrint() {
        val invoiceData = (uiState.value as? InvoiceDetailUiState.Success)?.data ?: return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val businessProfile = businessProfileRepository.activeProfile.first()

                // ✅ PHASE 2: Load settings for print as well
                val invoiceSettings = try {
                    invoiceSettingsRepository.getSettings("current_user")
                } catch (e: Exception) {
                    Timber.w(e, "Failed to load invoice settings for print, using defaults")
                    null
                }

                val snapshot = invoiceSettings?.toSnapshot(invoiceData, businessProfile)
                    ?: createDefaultSnapshot(invoiceData, businessProfile)

                val result = generateAndSaveInvoiceUseCase(
                    invoice = invoiceData,
                    snapshot = snapshot,
                    isQuote = invoiceData.isQuote,
                    overwriteExisting = true
                )

                result.onSuccess { file ->
                    // Validate file before sending to print service
                    if (com.emul8r.bizap.utils.FileUriProvider.isFileSharable(file)) {
                        printService.printPdf(file)
                    } else {
                        _uiEvent.emit(UiEvent.ShowSnackbar("Generated PDF is invalid or inaccessible for printing"))
                    }
                }.onFailure { e ->
                    _uiEvent.emit(UiEvent.ShowSnackbar("Print Failed: ${e.message}"))
                }
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.ShowSnackbar("Error preparing print: ${e.message}"))
            }
        }
    }

    fun deleteInvoice(id: Long) {
        viewModelScope.launch {
            invoiceRepo.deleteInvoice(id)
                .onSuccess { _event.emit(InvoiceDetailEvent.InvoiceDeleted) }
                .onFailure { e ->
                    _uiEvent.emit(UiEvent.ShowSnackbar("Failed to delete invoice: ${e.message}"))
                }
        }
    }

    fun exportToCsv() {
        val currentState = uiState.value as? InvoiceDetailUiState.Success ?: return
        val invoice = currentState.data

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val csvFile = csvExportService.exportSingleInvoice(invoice)
                _csvExportEvent.emit(csvFile)
                _uiEvent.emit(UiEvent.ShowSnackbar("CSV exported successfully"))
                Timber.d("✅ CSV exported: ${csvFile.name}")
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.ShowSnackbar("Failed to export CSV: ${e.message}"))
                Timber.e(e, "❌ CSV export failed")
            }
        }
    }
}
