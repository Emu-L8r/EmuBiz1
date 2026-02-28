package com.emul8r.bizap.ui.invoices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.data.DocumentManager
import com.emul8r.bizap.data.repository.BusinessProfileRepository
import com.emul8r.bizap.data.service.InvoicePdfService
import com.emul8r.bizap.data.service.PrintService
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.InvoiceSnapshot
import com.emul8r.bizap.domain.model.LineItemSnapshot
import com.emul8r.bizap.domain.model.InvoiceStatus
import com.emul8r.bizap.domain.repository.InvoiceRepository
import com.emul8r.bizap.domain.usecase.GenerateAndSaveInvoiceUseCase
import com.emul8r.bizap.utils.DocumentNamingUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

sealed interface InvoiceDetailUiState {
    object Loading : InvoiceDetailUiState
    data class Success(val data: Invoice) : InvoiceDetailUiState
    data class Error(val message: String) : InvoiceDetailUiState
}

sealed interface UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent
}

sealed interface InvoiceDetailEvent {
    object InvoiceDeleted : InvoiceDetailEvent
}

@HiltViewModel
class InvoiceDetailViewModel @Inject constructor(
    private val invoiceRepo: InvoiceRepository,
    private val pdfService: InvoicePdfService,
    private val businessProfileRepository: BusinessProfileRepository,
    private val printService: PrintService,
    private val documentManager: DocumentManager,
    private val generateAndSaveInvoiceUseCase: GenerateAndSaveInvoiceUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<InvoiceDetailUiState>(InvoiceDetailUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _exportEvent = MutableSharedFlow<File>()
    val exportEvent = _exportEvent.asSharedFlow()

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
        invoiceRepo.getInvoiceWithItemsById(id)
            .onEach { data ->
                _uiState.value = if (data != null) {
                    InvoiceDetailUiState.Success(data)
                } else {
                    InvoiceDetailUiState.Error("Invoice not found")
                }
            }
            .launchIn(viewModelScope)
    }

    fun updateStatus(invoiceId: Long, newStatus: String) {
        viewModelScope.launch {
            val status = InvoiceStatus.valueOf(newStatus)
            invoiceRepo.updateInvoiceStatus(invoiceId, status)
        }
    }

    fun shareInternalPdf() {
        checkAndProceedWithPdfGeneration(share = true)
    }

    fun exportToDownloads() {
        checkAndProceedWithPdfGeneration(share = false)
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
                val businessProfile = businessProfileRepository.profile.first()
                val snapshot = buildSnapshot(invoiceData, businessProfile)

                val quoteResult = generateAndSaveInvoiceUseCase(
                    invoice = invoiceData,
                    snapshot = snapshot,
                    isQuote = true,
                    overwriteExisting = overwriteExisting
                )

                val invoiceResult = generateAndSaveInvoiceUseCase(
                    invoice = invoiceData,
                    snapshot = snapshot,
                    isQuote = false,
                    overwriteExisting = overwriteExisting
                )

                if (quoteResult.isSuccess && invoiceResult.isSuccess) {
                    val quotePdf = quoteResult.getOrThrow()
                    val invoicePdf = invoiceResult.getOrThrow()

                    invoiceRepo.updatePdfPath(invoiceData.id, invoicePdf.absolutePath)

                    if (share) {
                        _exportEvent.emit(invoicePdf)
                    } else {
                        val quoteFileName = DocumentNamingUtils.generateFileName(
                            customerName = invoiceData.customerName,
                            date = invoiceData.date,
                            counter = invoiceData.id.toInt(),
                            type = "Quote"
                        )

                        val invoiceFileName = DocumentNamingUtils.generateFileName(
                            customerName = invoiceData.customerName,
                            date = invoiceData.date,
                            counter = invoiceData.id.toInt(),
                            type = "Invoice"
                        )

                        val quotePublicUri = documentManager.saveToDownloads(quotePdf, quoteFileName)
                        val invoicePublicUri = documentManager.saveToDownloads(invoicePdf, invoiceFileName)

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

    private fun buildSnapshot(invoice: Invoice, business: com.emul8r.bizap.domain.model.BusinessProfile): InvoiceSnapshot {
        return InvoiceSnapshot(
            invoiceId = invoice.id,
            invoiceNumber = invoice.getFormattedInvoiceNumber(),
            customerName = invoice.customerName,
            customerAddress = invoice.customerAddress,
            customerEmail = invoice.customerEmail,
            date = invoice.date,
            dueDate = invoice.dueDate,
            items = invoice.items.map { LineItemSnapshot(it.description, it.quantity, it.unitPrice, it.quantity * it.unitPrice) },
            subtotal = invoice.totalAmount - invoice.taxAmount,
            taxRate = invoice.taxRate,
            taxAmount = invoice.taxAmount,
            totalAmount = invoice.totalAmount,
            businessName = business.businessName,
            businessAbn = business.abn,
            businessEmail = business.email,
            businessPhone = business.phone,    // FIXED
            businessAddress = business.address, // FIXED
            logoBase64 = business.logoBase64
        )
    }

    fun launchSystemPrint() {
        val invoiceData = (uiState.value as? InvoiceDetailUiState.Success)?.data ?: return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val businessProfile = businessProfileRepository.profile.first()
                val snapshot = buildSnapshot(invoiceData, businessProfile)
                
                val result = generateAndSaveInvoiceUseCase(
                    invoice = invoiceData,
                    snapshot = snapshot,
                    isQuote = invoiceData.isQuote,
                    overwriteExisting = true
                )

                result.onSuccess { file ->
                    printService.printPdf(file)
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
            try {
                invoiceRepo.deleteInvoice(id)
                _event.emit(InvoiceDetailEvent.InvoiceDeleted)
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.ShowSnackbar("Failed to delete invoice: ${e.message}"))
            }
        }
    }
}
