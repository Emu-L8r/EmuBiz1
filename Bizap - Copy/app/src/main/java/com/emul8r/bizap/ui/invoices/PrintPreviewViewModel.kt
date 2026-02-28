package com.emul8r.bizap.ui.invoices

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.data.DocumentManager
import com.emul8r.bizap.data.local.entities.InvoiceWithItems
import com.emul8r.bizap.data.repository.BusinessProfileRepository
import com.emul8r.bizap.data.service.InvoicePdfService
import com.emul8r.bizap.domain.model.BusinessProfile
import com.emul8r.bizap.domain.repository.InvoiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

sealed interface PdfPreviewUiState {
    object Loading : PdfPreviewUiState
    data class Ready(val previewBitmap: Bitmap, val pdfFile: File) : PdfPreviewUiState
    data class Error(val message: String) : PdfPreviewUiState
}

@HiltViewModel
class InvoicePdfViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val invoiceRepo: InvoiceRepository,
    private val pdfService: InvoicePdfService,
    private val businessProfileRepo: BusinessProfileRepository,
    private val documentManager: DocumentManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<PdfPreviewUiState>(PdfPreviewUiState.Loading)
    val uiState = _uiState.asStateFlow()

    fun preparePreview(invoiceId: Long, isQuote: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val invoice = invoiceRepo.getInvoiceWithItemsById(invoiceId).first() ?: throw IllegalStateException("Invoice not found")
                val profile = businessProfileRepo.profile.first()

                // Generate PDF to a temporary file first
                val tempPdfFile = pdfService.generateInvoice(invoice, profile, isQuote)

                // 1. Move from temporary cache to permanent internal documents folder
                val permanentFile = documentManager.archiveToInternalStorage(tempPdfFile, invoice.invoice.invoiceId)

                // 2. Update Room immediately so the Vault sees it
                invoiceRepo.updatePdfPath(invoice.invoice.invoiceId, permanentFile.absolutePath)

                // Now, generate a bitmap for the UI preview from the permanent file
                val bitmap = generateBitmapFromFile(permanentFile)

                _uiState.value = PdfPreviewUiState.Ready(bitmap, permanentFile)

            } catch (e: Exception) {
                _uiState.value = PdfPreviewUiState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }

    private fun generateBitmapFromFile(file: File): Bitmap {
        val renderer = PdfRenderer(context.contentResolver.openFileDescriptor(Uri.fromFile(file), "r")!!)
        val page = renderer.openPage(0)
        val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        page.close()
        renderer.close()
        return bitmap
    }

    fun shareInternalFile() {
        val state = _uiState.value
        if (state is PdfPreviewUiState.Ready) {
            val contentUri = FileProvider.getUriForFile(context, "com.emul8r.bizap.fileprovider", state.pdfFile)
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, contentUri)
                type = "application/pdf"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(shareIntent, "Share Invoice via...")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        }
    }

    fun exportToPublicDownloads() {
        val state = _uiState.value
        if (state is PdfPreviewUiState.Ready) {
            viewModelScope.launch(Dispatchers.IO) {
                documentManager.saveToDownloads(state.pdfFile, state.pdfFile.name)
            }
        }
    }

    fun launchSystemPrint() {
        // Note: System Print is a complex operation that needs a PrintDocumentAdapter.
        // This is a placeholder for the full implementation.
    }
}
