package com.emul8r.bizap.ui.invoices

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.data.local.DocumentDao
import com.emul8r.bizap.data.local.entities.GeneratedDocumentEntity
import com.emul8r.bizap.data.local.entities.InvoiceWithItems
import com.emul8r.bizap.data.repository.BusinessProfileRepository
import com.emul8r.bizap.data.service.DocumentExportService
import com.emul8r.bizap.data.service.InvoicePdfService
import com.emul8r.bizap.data.service.PrintService
import com.emul8r.bizap.domain.repository.InvoiceRepository
import com.emul8r.bizap.utils.DocumentNamingUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

sealed interface InvoiceDetailUiState {
    object Loading : InvoiceDetailUiState
    data class Success(val data: InvoiceWithItems) : InvoiceDetailUiState
    data class Error(val message: String) : InvoiceDetailUiState
}

sealed interface UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent
}

@HiltViewModel
class InvoiceDetailViewModel @Inject constructor(
    private val invoiceRepo: InvoiceRepository,
    private val pdfService: InvoicePdfService,
    private val businessProfileRepository: BusinessProfileRepository,
    private val exportService: DocumentExportService,
    private val documentDao: DocumentDao,
    private val printService: PrintService
) : ViewModel() {

    private val _uiState = MutableStateFlow<InvoiceDetailUiState>(InvoiceDetailUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _exportEvent = MutableSharedFlow<File>()
    val exportEvent = _exportEvent.asSharedFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

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
            invoiceRepo.updateInvoiceStatus(invoiceId, newStatus)
        }
    }

    fun shareInternalPdf() {
        generateAndExportPdf(share = true)
    }

    fun exportToDownloads() {
        generateAndExportPdf(share = false)
    }

    private fun generateAndExportPdf(share: Boolean) {
        val invoiceData = (uiState.value as? InvoiceDetailUiState.Success)?.data ?: return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val businessProfile = businessProfileRepository.profile.first()
                val isQuote = invoiceData.invoice.isQuote

                val pdfFile = pdfService.generateInvoice(invoiceData, businessProfile, isQuote)
                invoiceRepo.updatePdfPath(invoiceData.invoice.invoiceId, pdfFile.absolutePath)

                // --- Unified Archive Logic ---
                val document = GeneratedDocumentEntity(
                    relatedInvoiceId = invoiceData.invoice.invoiceId,
                    fileName = pdfFile.name,
                    absolutePath = pdfFile.absolutePath,
                    fileType = if(isQuote) "Quote" else "Invoice",
                    status = com.emul8r.bizap.data.local.entities.DocumentStatus.Archived
                )
                documentDao.insertDocument(document)
                // ---------------------------

                if (share) {
                    _exportEvent.emit(pdfFile)
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        exportToDownloadsQ(pdfFile)
                    } else {
                        _uiEvent.emit(UiEvent.ShowSnackbar("Feature requires Android 10 or higher"))
                    }
                }
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.ShowSnackbar("Error processing PDF: ${e.message}"))
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private suspend fun exportToDownloadsQ(internalFile: File) {
        val invoiceData = (uiState.value as? InvoiceDetailUiState.Success)?.data ?: return
        val fileName = DocumentNamingUtils.generateFileName(
            customerName = invoiceData.invoice.customerName,
            date = invoiceData.invoice.date,
            counter = invoiceData.invoice.invoiceId.toInt(),
            type = if (invoiceData.invoice.isQuote) "Quote" else "Invoice"
        )
        val resultUri = exportService.exportToPublicDownloads(internalFile, fileName)

        _uiEvent.emit(if (resultUri != null) {
            UiEvent.ShowSnackbar("Saved to Downloads/EmuBiz")
        } else {
            UiEvent.ShowSnackbar("Failed to export file")
        })
    }

    fun launchSystemPrint() {
        val invoiceData = (uiState.value as? InvoiceDetailUiState.Success)?.data ?: return
        val pdfUri = invoiceData.invoice.pdfUri ?: return
        val file = File(pdfUri)

        if (file.exists()) {
            printService.printPdf(file)
        } else {
            viewModelScope.launch { 
                _uiEvent.emit(UiEvent.ShowSnackbar("PDF not found. Please generate it first.")) 
            }
        }
    }
}
