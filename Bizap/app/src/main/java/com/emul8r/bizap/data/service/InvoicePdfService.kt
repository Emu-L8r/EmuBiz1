package com.emul8r.bizap.data.service

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.annotation.RequiresApi
import com.emul8r.bizap.domain.model.InvoiceSnapshot
import com.emul8r.bizap.domain.repository.DocumentRepository
import com.emul8r.bizap.domain.pdf.PdfTableRenderer
import com.emul8r.bizap.domain.pdf.PdfBrandingRenderer
import com.emul8r.bizap.domain.pdf.PdfPageManager
import com.emul8r.bizap.domain.pdf.PdfWatermarkRenderer
import com.emul8r.bizap.domain.service.PdfGenerationService
import com.emul8r.bizap.ui.templates.TemplateSnapshotManager
import com.emul8r.bizap.utils.DocumentNamingUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data-layer implementation of PDF generation service.
 *
 * **Architecture:**
 * Implements the domain-level [PdfGenerationService] interface, allowing
 * domain use cases to depend only on domain abstractions while this service
 * handles the Android-specific details of PDF generation.
 *
 * **Responsibility:**
 * - Generates PDF files from invoice snapshots using Android's PdfDocument API
 * - Manages internal storage of generated PDFs
 * - Handles file naming, versioning, and overwrite logic
 */
@Singleton
class InvoicePdfService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val documentRepository: DocumentRepository
) : PdfGenerationService {
    companion object {
        private const val TAG = "InvoicePdfService"
    }

    private val pdfStyler = PdfStyler()
    private val snapshotManager = TemplateSnapshotManager()

    /**
     * Domain-level API: Generate a PDF from an invoice snapshot.
     * Delegates to the internal generateInvoice method.
     */
    override suspend fun generatePdf(
        snapshot: InvoiceSnapshot,
        isQuote: Boolean,
        overwriteExisting: Boolean
    ): File = generateInvoice(snapshot, isQuote, overwriteExisting)

    suspend fun checkIfPdfExists(invoiceId: Long, fileType: String): Pair<Boolean, String?> {
        val existingDoc = documentRepository.getDocumentByInvoiceAndType(invoiceId, fileType)
        return if (existingDoc != null) Pair(true, existingDoc.fileName) else Pair(false, null)
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    suspend fun generateInvoice(
        snapshot: InvoiceSnapshot,
        isQuote: Boolean,
        overwriteExisting: Boolean = true,
        templateSnapshotJson: String? = null,
        customFieldValuesJson: String? = null
    ): File {
        val fileType = if (isQuote) "Quote" else "Invoice"
        val baseFileName = DocumentNamingUtils.generateFileName(
            snapshot.customerName, snapshot.date, snapshot.invoiceId.toInt(), fileType
        )

        val existingDoc = documentRepository.getDocumentByInvoiceAndType(snapshot.invoiceId, fileType)
        val fileName = if (!overwriteExisting && existingDoc != null) {
            generateVersionedFileName(baseFileName)
        } else {
            if (existingDoc != null) File(existingDoc.absolutePath).let { if (it.exists()) it.delete() }
            baseFileName
        }

        val file = File(context.filesDir, "documents/$fileName")
        file.parentFile?.mkdirs()

        val templateSnapshot = snapshotManager.restoreSnapshot(templateSnapshotJson)
        val customFieldValues = snapshotManager.restoreCustomFieldValues(customFieldValuesJson)

        val colors = pdfStyler.extractColors(templateSnapshot)
        val hideLineItems = pdfStyler.shouldHideLineItems(templateSnapshot)
        val hidePaymentTerms = pdfStyler.shouldHidePaymentTerms(templateSnapshot)

        val pdfDocument = PdfDocument()
        val pageManager = PdfPageManager(pdfDocument, 595, 842)
        var canvas = pageManager.startNewPage()

        val boldTypeface = pdfStyler.getTypeface(templateSnapshot?.fontFamily, context, isBold = true)
        val regularTypeface = pdfStyler.getTypeface(templateSnapshot?.fontFamily, context, isBold = false)
        val italicTypeface = Typeface.create(regularTypeface, Typeface.ITALIC)

        val symbol = getCurrencySymbol(snapshot.currencyCode)

        val headerPaint = Paint().apply { typeface = boldTypeface; textSize = 10f; color = Color.BLACK; isAntiAlias = true }
        val brandPaint = Paint().apply { typeface = boldTypeface; textSize = 18f; color = colors.primary; isAntiAlias = true }
        val bodyPaint = Paint().apply { typeface = regularTypeface; textSize = 10f; color = colors.textLight; isAntiAlias = true }
        val labelPaint = Paint().apply { typeface = boldTypeface; textSize = 9f; color = colors.primary; isAntiAlias = true }

        // ===== BRANDING & HEADER SECTION (Page 1 Only) =====
        // Add light gray background to entire header section
        val headerBackgroundPaint = Paint().apply {
            color = Color.parseColor("#F5F5F5")
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 15f, 595f, 110f, headerBackgroundPaint)

        // Draw logo if available (on the left side)
        val brandingRenderer = PdfBrandingRenderer(canvas, 595f)
        brandingRenderer.drawLogo(snapshot.logoBase64)

        // Professional side-by-side layout: Logo on left, company info on right
        bodyPaint.textAlign = Paint.Align.LEFT
        val logoAreaWidth = 100f  // Space for logo on the left
        val infoStartX = logoAreaWidth + 20f  // Offset from logo

        // Company name (larger, bold, primary color)
        val companyNamePaint = Paint().apply {
            typeface = boldTypeface
            textSize = 14f
            color = colors.primary
            isAntiAlias = true
        }
        canvas.drawText(snapshot.businessName.uppercase(), infoStartX, 35f, companyNamePaint)

        // ABN
        bodyPaint.textSize = 9f
        canvas.drawText("ABN: ${snapshot.businessAbn}", infoStartX, 50f, bodyPaint)

        // Phone
        canvas.drawText("Phone: ${snapshot.businessPhone}", infoStartX, 62f, bodyPaint)

        // Email
        canvas.drawText("Email: ${snapshot.businessEmail}", infoStartX, 74f, bodyPaint)

        // Address
        canvas.drawText(snapshot.businessAddress, infoStartX, 86f, bodyPaint)

        // Reset paint and draw enhanced divider line
        bodyPaint.textSize = 10f
        canvas.drawLine(40f, 110f, 555f, 110f, Paint().apply { color = colors.primary; strokeWidth = 2f })

        // ===== HEADER SECTION: TWO-COLUMN LAYOUT (Bill To | Invoice Details) =====
        // Visual section container styling
        val sectionBoxPaint = Paint().apply { color = Color.parseColor("#F5F5F5"); style = Paint.Style.FILL }
        val billToBoxBorderPaint = Paint().apply { color = colors.primary; strokeWidth = 1.5f; style = Paint.Style.STROKE }

        // Left box: Bill To (40-280)
        canvas.drawRect(40f, 120f, 280f, 195f, sectionBoxPaint)
        canvas.drawRect(40f, 120f, 280f, 195f, billToBoxBorderPaint)

        // Box labels with primary color
        val boxLabelPaint = Paint().apply { typeface = boldTypeface; textSize = 9f; color = colors.primary; isAntiAlias = true }
        bodyPaint.textAlign = Paint.Align.LEFT
        canvas.drawText("BILL TO", 50f, 135f, boxLabelPaint)
        canvas.drawText(snapshot.customerName, 50f, 155f, headerPaint)
        canvas.drawText(snapshot.customerAddress, 50f, 170f, bodyPaint)
        snapshot.customerEmail?.let { canvas.drawText(it, 50f, 185f, bodyPaint) }

        // Right box: Invoice Details (315-555)
        val invoiceBoxBorderPaint = Paint().apply { color = colors.primary; strokeWidth = 1.5f; style = Paint.Style.STROKE }
        canvas.drawRect(315f, 120f, 555f, 195f, sectionBoxPaint)
        canvas.drawRect(315f, 120f, 555f, 195f, invoiceBoxBorderPaint)

        canvas.drawText("INVOICE", 325f, 135f, boxLabelPaint)
        canvas.drawText(snapshot.displayName.ifBlank { snapshot.invoiceNumber }, 325f, 155f, headerPaint)
        canvas.drawText("Date: ${formatDate(snapshot.date)}", 325f, 170f, bodyPaint)
        canvas.drawText("Due: ${formatDate(snapshot.dueDate)}", 325f, 185f, bodyPaint)

        // ===== WATERMARK (appears on first page) =====
        val watermarkRenderer = PdfWatermarkRenderer(canvas, 595f, 842f)
        watermarkRenderer.drawWatermark(snapshot.invoiceStatus)

        var currentY = 210f
        pageManager.setY(currentY)


        val separatorPaint = Paint().apply { color = colors.secondary; strokeWidth = 0.5f; style = Paint.Style.STROKE }
        val sectionHeaderPaint = Paint().apply { typeface = boldTypeface; textSize = 11f; color = colors.primary; isAntiAlias = true }
        val subheaderBodyPaint = Paint().apply { typeface = italicTypeface; textSize = 10f; color = colors.textLight; isAntiAlias = true }
        val footerBodyPaint = Paint().apply { typeface = italicTypeface; textSize = 9f; color = Color.GRAY; isAntiAlias = true }


        // ===== HEADER AND SUBHEADER TEXT (Optional, appears before line items) =====
        if (snapshot.headerText.isNotBlank() || snapshot.subheaderText.isNotBlank()) {
            canvas = pageManager.ensureSpace(50f)

            if (snapshot.headerText.isNotBlank()) {
                canvas.drawText(snapshot.headerText, 40f, pageManager.currentY, headerPaint)
                pageManager.advanceY(16f)
            }

            if (snapshot.subheaderText.isNotBlank()) {
                canvas.drawText(snapshot.subheaderText, 40f, pageManager.currentY, bodyPaint)
                pageManager.advanceY(12f)
            }

            pageManager.advanceY(8f)  // Extra spacing after header/subheader
        }

        if (!hideLineItems) {
            // ===== LINE ITEMS TABLE WITH PAGINATION =====
            // Draw table header with professional styling
            val headerTextPaint = Paint(headerPaint).apply {
                color = Color.WHITE
                textSize = 11f
                typeface = boldTypeface
            }

            // Ensure space for header row (approximately 40 points)
            canvas = pageManager.ensureSpace(40f)

            // Draw table border top
            val tableBorderPaint = Paint().apply {
                color = colors.primary
                strokeWidth = 2f
                style = Paint.Style.STROKE
            }
            canvas.drawLine(40f, pageManager.currentY, 555f, pageManager.currentY, tableBorderPaint)

            val tableRenderer = PdfTableRenderer(
                canvas = canvas,
                startX = 40f,
                currentY = pageManager.currentY,
                pageWidth = 595f,
                columnWeights = listOf(0.5f, 0.1f, 0.15f, 0.25f),
                headerBackgroundColor = colors.primary,
                alternateRowColor = Color.parseColor("#F9F9F9")
            )

            tableRenderer.drawRow(listOf("Description", "Qty", "Price", "Total"), headerTextPaint, isHeader = true, headerTextColor = Color.WHITE)
            pageManager.setY(tableRenderer.getPosition())

            // Draw vertical column separators for clarity
            val columnSeparatorPaint = Paint().apply {
                color = colors.secondary
                strokeWidth = 0.5f
                style = Paint.Style.STROKE
            }
            tableRenderer.drawColumnSeparators(canvas, 20f, tableRenderer.getPosition() - 20f, columnSeparatorPaint)

            // Draw table rows with automatic pagination
            snapshot.items.forEach { item ->
                val rowHeight = 35f  // Estimated row height with wrapping

                // Check if we need to start a new page
                canvas = pageManager.ensureSpace(rowHeight)

                // Render row on current canvas
                tableRenderer.drawRow(
                    listOf(
                        item.description,
                        item.quantity.toInt().toString(),
                        String.format(Locale.getDefault(), "%s%.2f", symbol, item.unitPrice / 100.0),
                        String.format(Locale.getDefault(), "%s%.2f", symbol, item.total / 100.0)
                    ),
                    bodyPaint
                )
                pageManager.setY(tableRenderer.getPosition())
            }

            // Draw table border bottom
            canvas.drawLine(40f, pageManager.currentY, 555f, pageManager.currentY, tableBorderPaint)

            currentY = pageManager.currentY + 20f
            pageManager.setY(currentY)
        }

        val rightX = 545f
        bodyPaint.textAlign = Paint.Align.RIGHT
        headerPaint.textAlign = Paint.Align.RIGHT

        // Ensure space for totals section (approximately 80 points)
        canvas = pageManager.ensureSpace(80f)

        // Draw totals box with colored header bar
        val totalsHeaderPaint = Paint().apply { color = colors.primary; style = Paint.Style.FILL }
        val totalsBodyPaint = Paint().apply { color = Color.parseColor("#F9F9F9"); style = Paint.Style.FILL }
        val totalBoxBorderPaint = Paint().apply { color = colors.primary; strokeWidth = 2f; style = Paint.Style.STROKE }

        // Draw header section (primary color)
        canvas.drawRect(320f, pageManager.currentY - 5f, 555f, pageManager.currentY + 15f, totalsHeaderPaint)
        // Draw body section (light gray)
        canvas.drawRect(320f, pageManager.currentY + 15f, 555f, pageManager.currentY + 65f, totalsBodyPaint)
        // Draw border
        canvas.drawRect(320f, pageManager.currentY - 5f, 555f, pageManager.currentY + 65f, totalBoxBorderPaint)

        // Header label
        val totalsHeaderLabelPaint = Paint().apply {
            typeface = boldTypeface
            textSize = 10f
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("TOTAL SUMMARY", 437f, pageManager.currentY + 10f, totalsHeaderLabelPaint)

        // Adjust Y for content
        pageManager.advanceY(20f)

        // Subtotal
        bodyPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Subtotal:", 450f, pageManager.currentY, bodyPaint)
        canvas.drawText(String.format(Locale.getDefault(), "%s%.2f", symbol, snapshot.subtotal / 100.0), rightX, pageManager.currentY, bodyPaint)

        pageManager.advanceY(16f)
        if (snapshot.taxAmount > 0) {
            canvas.drawText("Tax (${(snapshot.taxRate * 100).toInt()}%):", 450f, pageManager.currentY, bodyPaint)
            canvas.drawText(String.format(Locale.getDefault(), "%s%.2f", symbol, snapshot.taxAmount / 100.0), rightX, pageManager.currentY, bodyPaint)
            pageManager.advanceY(18f)
        }

        // Total Amount Due - Highly emphasized
        val totalDuePaint = Paint().apply {
            typeface = boldTypeface
            textSize = 14f
            color = colors.primary
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }
        canvas.drawText("TOTAL DUE:", 450f, pageManager.currentY, totalDuePaint)
        val formattedAmount = String.format(Locale.getDefault(), "%s%.2f", symbol, snapshot.totalAmount / 100.0)
        canvas.drawText(formattedAmount, rightX, pageManager.currentY, totalDuePaint)

        // ===== PAYMENT DETAILS SECTION =====
        // Ensure space for payment details header (approximately 100 points)
        canvas = pageManager.ensureSpace(100f)

        pageManager.advanceY(20f)

        // Payment section box
        val paymentBoxPaint = Paint().apply { color = Color.parseColor("#FAFAFA"); style = Paint.Style.FILL }
        val paymentBoxBorderPaint = Paint().apply { color = colors.secondary; strokeWidth = 1f; style = Paint.Style.STROKE }

        val paymentBoxTop = pageManager.currentY
        canvas.drawRect(40f, paymentBoxTop, 555f, paymentBoxTop + 85f, paymentBoxPaint)
        canvas.drawRect(40f, paymentBoxTop, 555f, paymentBoxTop + 85f, paymentBoxBorderPaint)

        canvas.drawText("PAYMENT DETAILS", 50f, pageManager.currentY + 12f, labelPaint)
        pageManager.advanceY(18f)

        bodyPaint.textAlign = Paint.Align.LEFT
        canvas.drawText("Payment Terms: Due within 30 days of invoice date", 50f, pageManager.currentY, bodyPaint)
        pageManager.advanceY(12f)
        canvas.drawText("Reference: ${snapshot.invoiceNumber}", 50f, pageManager.currentY, bodyPaint)
        pageManager.advanceY(12f)

        if (snapshot.businessPhone.isNotBlank()) {
            canvas.drawText("Contact: ${snapshot.businessPhone}", 50f, pageManager.currentY, bodyPaint)
            pageManager.advanceY(12f)
        }

        if (snapshot.businessEmail.isNotBlank()) {
            canvas.drawText(snapshot.businessEmail, 50f, pageManager.currentY, bodyPaint)
            pageManager.advanceY(12f)
        }

        // Bank / EFT payment details — only shown when the business profile has them set
        val hasBankDetails = snapshot.bankAccountNumber.isNotBlank() || snapshot.bankBsb.isNotBlank()
        if (hasBankDetails) {
            pageManager.advanceY(8f)
            canvas.drawText("EFT / Bank Transfer:", 50f, pageManager.currentY, labelPaint)
            pageManager.advanceY(14f)
            if (snapshot.bankName.isNotBlank()) {
                canvas.drawText("Bank: ${snapshot.bankName}", 50f, pageManager.currentY, bodyPaint)
                pageManager.advanceY(12f)
            }
            if (snapshot.bankAccountName.isNotBlank()) {
                canvas.drawText("Account Name: ${snapshot.bankAccountName}", 50f, pageManager.currentY, bodyPaint)
                pageManager.advanceY(12f)
            }
            if (snapshot.bankBsb.isNotBlank()) {
                canvas.drawText("BSB: ${snapshot.bankBsb}", 50f, pageManager.currentY, bodyPaint)
                pageManager.advanceY(12f)
            }
            if (snapshot.bankAccountNumber.isNotBlank()) {
                canvas.drawText("Account No: ${snapshot.bankAccountNumber}", 50f, pageManager.currentY, bodyPaint)
                pageManager.advanceY(12f)
            }
        }

        // Render notes and footer below totals
        bodyPaint.textAlign = Paint.Align.LEFT
        if (snapshot.notes.isNotBlank()) {
            canvas = pageManager.ensureSpace(70f)
            pageManager.advanceY(20f)

            val notesBoxPaint = Paint().apply { color = Color.parseColor("#FAFAFA"); style = Paint.Style.FILL }
            val notesBoxBorderPaint = Paint().apply { color = colors.secondary; strokeWidth = 1f; style = Paint.Style.STROKE }

            val notesBoxTop = pageManager.currentY
            canvas.drawRect(40f, notesBoxTop, 555f, notesBoxTop + 55f, notesBoxPaint)
            canvas.drawRect(40f, notesBoxTop, 555f, notesBoxTop + 55f, notesBoxBorderPaint)

            canvas.drawText("NOTES", 50f, pageManager.currentY + 12f, labelPaint)
            pageManager.advanceY(18f)
            pageManager.setY(drawWrappedText(canvas, snapshot.notes, 50f, pageManager.currentY, 490f, bodyPaint))
        }

        if (snapshot.footerText.isNotBlank()) {
            canvas = pageManager.ensureSpace(70f)
            pageManager.advanceY(20f)

            val footerBoxPaint = Paint().apply { color = Color.parseColor("#F5F5F5"); style = Paint.Style.FILL }
            val footerBoxBorderPaint = Paint().apply { color = colors.secondary; strokeWidth = 1f; style = Paint.Style.STROKE }

            val footerBoxTop = pageManager.currentY
            canvas.drawRect(40f, footerBoxTop, 555f, footerBoxTop + 55f, footerBoxPaint)
            canvas.drawRect(40f, footerBoxTop, 555f, footerBoxTop + 55f, footerBoxBorderPaint)

            canvas.drawText("FOOTER", 50f, pageManager.currentY + 12f, labelPaint)
            pageManager.advanceY(18f)
            pageManager.setY(drawWrappedText(canvas, snapshot.footerText, 50f, pageManager.currentY, 490f, footerBodyPaint))
        }

        // ===== BRANDED FOOTER SECTION (Professional) =====
        canvas = pageManager.ensureSpace(50f)
        pageManager.advanceY(30f)

        // Footer background bar (primary color)
        val footerBackgroundPaint = Paint().apply {
            color = colors.primary
            style = Paint.Style.FILL
        }
        val footerBarHeight = 45f
        canvas.drawRect(0f, pageManager.currentY, 595f, pageManager.currentY + footerBarHeight, footerBackgroundPaint)

        // Footer text styling
        val footerTextPaint = Paint().apply {
            typeface = boldTypeface
            textSize = 10f
            color = Color.WHITE
            isAntiAlias = true
        }

        val footerSmallPaint = Paint().apply {
            typeface = regularTypeface
            textSize = 8f
            color = Color.WHITE
            isAntiAlias = true
        }

        // Line 1: Company name | Website
        bodyPaint.textAlign = Paint.Align.LEFT
        canvas.drawText(snapshot.businessName, 50f, pageManager.currentY + 15f, footerTextPaint)

        // Extract website from email domain
        val footerWebsite = snapshot.businessEmail.substringAfter("@").lowercase()
        canvas.drawText("| ${footerWebsite}", 250f, pageManager.currentY + 15f, footerSmallPaint)

        // Line 2: ABN | Contact email
        canvas.drawText("ABN: ${snapshot.businessAbn}  |  ${snapshot.businessEmail}", 50f, pageManager.currentY + 28f, footerSmallPaint)

        // Line 3: Address
        canvas.drawText(snapshot.businessAddress, 50f, pageManager.currentY + 38f, footerSmallPaint)

        pageManager.advanceY(footerBarHeight + 5f)

        // Finalize all pages and close document
        pageManager.finalize()
        file.outputStream().use { pdfDocument.writeTo(it) }
        pdfDocument.close()

        // 🔍 VALIDATION: Ensure file was actually written and is not empty
        if (!file.exists()) {
            throw IllegalStateException("PDF file was not created at: ${file.absolutePath}")
        }

        val fileSize = file.length()
        if (fileSize == 0L) {
            file.delete() // Cleanup empty file
            throw IllegalStateException("PDF file created but is empty (0 bytes) at: ${file.absolutePath}")
        }

        // 📝 LOG: PDF generation success with file details
        timber.log.Timber.d(
            "✅ PDF generated successfully:\n" +
            "  File: ${file.name}\n" +
            "  Path: ${file.absolutePath}\n" +
            "  Size: $fileSize bytes\n" +
            "  Type: $fileType"
        )

        // Log with ErrorExportLogger for structured search
        com.emul8r.bizap.utils.logging.ErrorExportLogger.logPdfSuccess(
            invoiceId = snapshot.invoiceId,
            filePath = file.absolutePath,
            sizeBytes = fileSize,
            type = fileType
        )

        return file
    }

    /**
     * Draws wrapped text using StaticLayout and returns the Y position after the drawn text.
     */
    private fun drawWrappedText(
        canvas: Canvas,
        text: String,
        x: Float,
        y: Float,
        maxWidth: Float,
        paint: Paint
    ): Float {
        val textPaint = TextPaint(paint)
        val layout = StaticLayout.Builder
            .obtain(text, 0, text.length, textPaint, maxWidth.toInt())
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(0f, 1.2f)
            .build()
        canvas.save()
        canvas.translate(x, y)
        layout.draw(canvas)
        canvas.restore()
        return y + layout.height
    }

    private fun getCurrencySymbol(code: String): String {
        return when (code) {
            "USD" -> "$"
            "EUR" -> "€"
            "GBP" -> "£"
            "JPY" -> "¥"
            "AUD" -> "$"
            else -> "$"
        }
    }

    private fun formatDate(timestamp: Long): String = 
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(timestamp))

    private fun generateVersionedFileName(baseFileName: String): String {
        val nameWithoutExt = baseFileName.substringBeforeLast(".pdf")
        var version = 2
        while (true) {
            val name = "${nameWithoutExt}_v${version}.pdf"
            if (!File(context.filesDir, "documents/$name").exists()) return name
            version++
        }
    }
}
