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
import com.emul8r.bizap.domain.repository.BusinessProfileRepository
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
import timber.log.Timber
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
                Timber.d("📄 Starting PDF preview preparation for invoice: $invoiceId")

                val invoice = invoiceRepo.getInvoiceWithItemsById(invoiceId).first()
                    ?: throw IllegalStateException("Invoice not found: $invoiceId")
                val profile = businessProfileRepo.activeProfile.first()
                    ?: throw IllegalStateException("No active business profile found")

                // Build snapshot for PDF generation
                val snapshot = com.emul8r.bizap.domain.model.InvoiceSnapshot(
                    invoiceId = invoice.id,
                    invoiceNumber = invoice.getFormattedInvoiceNumber(),
                    customerName = invoice.customerName,
                    customerAddress = invoice.customerAddress,
                    customerEmail = invoice.customerEmail,
                    date = invoice.date,
                    dueDate = invoice.dueDate,
                    items = invoice.items.map {
                        val itemTotal = (it.unitPrice * it.quantity).toLong()
                        com.emul8r.bizap.domain.model.LineItemSnapshot(
                            it.description,
                            it.quantity,
                            it.unitPrice,
                            itemTotal
                        )
                    },
                    subtotal = invoice.totalAmount - invoice.taxAmount,
                    taxRate = invoice.taxRate,
                    taxAmount = invoice.taxAmount,
                    totalAmount = invoice.totalAmount,
                    businessName = profile.businessName,
                    businessAbn = profile.abn,
                    businessEmail = profile.email,
                    businessPhone = profile.phone,
                    businessAddress = profile.address,
                    logoBase64 = profile.logoBase64,
                    headerText = invoice.header ?: "",
                    subheaderText = invoice.subheader ?: "",
                    footerText = invoice.footer ?: "",
                    notes = invoice.notes ?: "",
                    bankAccountName = profile.accountName ?: "",
                    bankAccountNumber = profile.accountNumber ?: "",
                    bankBsb = profile.bsbNumber ?: "",
                    bankName = profile.bankName ?: ""
                )

                Timber.d("📝 Generated invoice snapshot for ${invoice.customerName}")

                // Generate PDF to a temporary file first
                val tempPdfFile = pdfService.generateInvoice(snapshot, isQuote)
                Timber.d("🔄 Temporary PDF generated: ${tempPdfFile.absolutePath}")

                // 1. Move from temporary cache to permanent internal documents folder
                val permanentFile = documentManager.archiveToInternalStorage(tempPdfFile, invoice.id)
                Timber.d("📁 PDF archived to internal storage: ${permanentFile.absolutePath}")

                // 2. Update Room immediately so the Vault sees it
                invoiceRepo.updatePdfPath(invoice.id, permanentFile.absolutePath).getOrThrow()
                Timber.d("💾 PDF path updated in database")

                // Now, generate a bitmap for the UI preview from the permanent file
                val bitmap = generateBitmapFromFile(permanentFile)
                Timber.d("🖼️ PDF preview bitmap created successfully")

                _uiState.value = PdfPreviewUiState.Ready(bitmap, permanentFile)
                Timber.i("✅ PDF preview ready for invoice: $invoiceId")

            } catch (e: Exception) {
                Timber.e(e, "❌ Error preparing PDF preview for invoice: $invoiceId")
                _uiState.value = PdfPreviewUiState.Error(e.message ?: "An unexpected error occurred during PDF generation")
            }
        }
    }

    private fun generateBitmapFromFile(file: File): Bitmap {
        return try {
            // Use FileProvider to get safe URI for internal storage files
            val fileUri = FileProvider.getUriForFile(context, "com.emul8r.bizap.fileprovider", file)

            // Open file descriptor safely with error handling
            val fd = context.contentResolver.openFileDescriptor(fileUri, "r")
                ?: throw IllegalStateException("Could not open PDF file descriptor: ${file.absolutePath}")

            val renderer = PdfRenderer(fd)
            val page = renderer.openPage(0)
            val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()
            renderer.close()

            Timber.d("✅ PDF bitmap generated successfully: ${file.name}")
            bitmap
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to generate PDF bitmap from: ${file.absolutePath}")
            throw IllegalStateException("Failed to generate PDF preview: ${e.message}", e)
        }
    }

    fun shareInternalFile() {
        val state = _uiState.value
        if (state is PdfPreviewUiState.Ready) {
            try {
                Timber.d("📤 Sharing PDF file: ${state.pdfFile.name}")
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
                Timber.i("✅ Share intent launched successfully")
            } catch (e: Exception) {
                Timber.e(e, "❌ Failed to share PDF file")
            }
        } else {
            Timber.w("⚠️ Cannot share: PDF not ready (state=${state::class.simpleName})")
        }
    }

    fun exportToPublicDownloads() {
        val state = _uiState.value
        if (state is PdfPreviewUiState.Ready) {
            try {
                Timber.d("💾 Exporting PDF to Downloads: ${state.pdfFile.name}")
                viewModelScope.launch(Dispatchers.IO) {
                    try {
                        val result = documentManager.saveToDownloads(state.pdfFile, state.pdfFile.name)
                        if (result != null) {
                            Timber.i("✅ PDF exported to Downloads: $result")
                        } else {
                            Timber.e("❌ Failed to export PDF to Downloads (returned null)")
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "❌ Error exporting PDF to Downloads")
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ Failed to initiate Downloads export")
            }
        } else {
            Timber.w("⚠️ Cannot export: PDF not ready (state=${state::class.simpleName})")
        }
    }

    fun launchSystemPrint() {
        val state = _uiState.value
        if (state is PdfPreviewUiState.Ready) {
            try {
                Timber.d("🖨️ Launching system print dialog for: ${state.pdfFile.name}")
                // Note: System Print is a complex operation that needs a PrintDocumentAdapter.
                // This is a placeholder for the full implementation.
                Timber.w("⚠️ System print not yet fully implemented")
            } catch (e: Exception) {
                Timber.e(e, "❌ Failed to launch system print")
            }
        } else {
            Timber.w("⚠️ Cannot print: PDF not ready (state=${state::class.simpleName})")
        }
    }
}
