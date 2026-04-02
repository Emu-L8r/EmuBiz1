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
import com.emul8r.bizap.data.repository.InvoiceSettingsRepository
import com.emul8r.bizap.domain.pdf.PdfTableRenderer
import com.emul8r.bizap.domain.pdf.PdfBrandingRenderer
import com.emul8r.bizap.domain.pdf.PdfPageManager
import com.emul8r.bizap.domain.pdf.PdfWatermarkRenderer
import com.emul8r.bizap.domain.service.PdfGenerationService
import com.emul8r.bizap.ui.templates.TemplateSnapshotManager
import com.emul8r.bizap.utils.DocumentNamingUtils
import timber.log.Timber
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
 * - Routes HTML PDF generation with proper style selection
 */
@Singleton
class InvoicePdfService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val documentRepository: DocumentRepository,
    private val invoiceSettingsRepository: InvoiceSettingsRepository
) : PdfGenerationService {
    companion object {
        private const val TAG = "InvoicePdfService"
    }

    private val pdfStyler = PdfStyler()
    private val snapshotManager = TemplateSnapshotManager()

    /**
     * Domain-level API: Generate a PDF from an invoice snapshot.
     * Routes to the appropriate theme implementation based on the theme parameter.
     *
     * @param theme Optional theme selection (CANVAS or HTML_PDF). If null, defaults to CANVAS.
     */
    override suspend fun generatePdf(
        snapshot: InvoiceSnapshot,
        isQuote: Boolean,
        overwriteExisting: Boolean,
        theme: com.emul8r.bizap.domain.model.InvoiceTheme?
    ): File {
        Timber.d("═══════════════════════════════════════════════════════════════════════════")
        Timber.d("📄 InvoicePdfService.generatePdf() called with theme: ${theme?.name ?: "NULL"}")

        // FIX: Cause #3 - Use theme parameter to route to correct service
        return when (theme) {
            com.emul8r.bizap.domain.model.InvoiceTheme.HTML_PDF -> {
                Timber.d("✅ THEME MATCHED: HTML_PDF")
                Timber.d("🎨 Routing to HtmlPdfInvoiceService for PDF generation")
                try {
                    // Load current user's invoice settings to get the selected HTML style
                    // For now, we'll use the default user ID - in production this should come from auth context
                    val currentUserId = "current_user"  // TODO: Get from authentication context
                    val settings = try {
                        invoiceSettingsRepository.getSettings(currentUserId)
                    } catch (e: Exception) {
                        Timber.w(e, "Failed to load invoice settings, using default")
                        null
                    }

                    // Use HTML-to-PDF service for modern design, passing settings for style selection
                    Timber.d("🔄 Creating HtmlPdfInvoiceService instance with settings...")
                    val htmlPdfService = HtmlPdfInvoiceService(context, settings)
                    Timber.d("🔄 Calling htmlPdfService.generatePdf()...")
                    val result = htmlPdfService.generatePdf(snapshot, isQuote, overwriteExisting, theme)
                    Timber.d("✅ HtmlPdfInvoiceService.generatePdf() completed successfully")
                    Timber.d("✅ PDF file: ${result.name} (${result.length()} bytes)")
                    result
                } catch (e: Exception) {
                    Timber.e(e, "❌ HTML-to-PDF generation FAILED, falling back to Canvas")
                    // Fallback to Canvas if HTML service fails
                    Timber.d("⚠️ Falling back to generateInvoice() (Canvas)...")
                    generateInvoice(snapshot, isQuote, overwriteExisting)
                }
            }
            else -> {
                Timber.d("✅ THEME: ${theme?.name ?: "NULL/DEFAULT"}")
                Timber.d("🎨 Using Canvas theme for PDF generation (default)")
                // Default: use Canvas theme
                generateInvoice(snapshot, isQuote, overwriteExisting)
            }
        }
    }

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

        // ===== PHASE 9A: ARTISTIC/LAYERED HEADER WITH OVERLAPPING SHAPES =====
        val artisticHeaderHeight = 100f

        // LAYER 1: Primary color background (base)
        val headerBackgroundPaint = Paint().apply {
            color = colors.primary
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, 595f, artisticHeaderHeight, headerBackgroundPaint)

        // LAYER 2: Diagonal accent overlay (right side) - creates visual interest
        val diagonalAccentPaint = Paint().apply {
            color = android.graphics.Color.argb(25, 255, 255, 255)  // Subtle white
            style = Paint.Style.FILL
        }
        val diagonalPath = Path().apply {
            moveTo(420f, 0f)
            lineTo(595f, 0f)
            lineTo(595f, 90f)
            lineTo(470f, artisticHeaderHeight)
            close()
        }
        canvas.drawPath(diagonalPath, diagonalAccentPaint)

        // LAYER 3: Wave/curve bottom accent (elegant transition)
        val waveBottomPaint = Paint().apply {
            color = android.graphics.Color.argb(20, 0, 0, 0)
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, artisticHeaderHeight - 4f, 595f, artisticHeaderHeight, waveBottomPaint)

        // Draw logo if available (left side, fully visible)
        val brandingRenderer = PdfBrandingRenderer(canvas, 595f)
        brandingRenderer.drawLogo(snapshot.logoBase64)

        // Premium white text styling
        val artisticHeaderPaint = Paint().apply {
            typeface = boldTypeface
            textSize = 18f
            color = Color.WHITE
            isAntiAlias = true
        }

        val artisticSubheaderPaint = Paint().apply {
            typeface = regularTypeface
            textSize = 8f
            color = Color.parseColor("#E8E8E8")
            isAntiAlias = true
        }

        // Company name (left side, prominent)
        canvas.drawText(snapshot.businessName.uppercase(), 120f, 35f, artisticHeaderPaint)

        // INVOICE LABEL (right side, elegant positioning - not a stamp)
        val invoiceLabelPaint = Paint().apply {
            typeface = boldTypeface
            textSize = 11f
            color = Color.parseColor("#C0C0C0")
            isAntiAlias = true
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("INVOICE", 555f, 28f, invoiceLabelPaint)
        canvas.drawText(snapshot.invoiceNumber, 555f, 40f, invoiceLabelPaint)

        // Business info (right-aligned, clean)
        artisticSubheaderPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("ABN: ${snapshot.businessAbn}", 555f, 55f, artisticSubheaderPaint)
        canvas.drawText(snapshot.businessPhone, 555f, 65f, artisticSubheaderPaint)
        canvas.drawText(snapshot.businessEmail, 555f, 75f, artisticSubheaderPaint)

        // ===== HEADER SECTION: TWO-COLUMN LAYOUT (Bill To | Invoice Details) =====

        // ===== PHASE 9B: PREMIUM FLOATING CARDS WITH SHADOWS & ROUNDED CORNERS =====
        // Card background (white with subtle accent tint)
        val cardBackgroundPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        val cardBorderPaint = Paint().apply {
            color = Color.parseColor("#D8D8D8")  // Slightly darker border
            strokeWidth = 0.8f
            style = Paint.Style.STROKE
        }

        // Shadow paint for depth effect
        val shadowPaint = Paint().apply {
            color = android.graphics.Color.argb(15, 0, 0, 0)
            style = Paint.Style.FILL
        }

        // Bill To Card (left) - Enhanced with shadow & rounded corners
        val billToLeft = 38f
        val billToTop = 125f
        val billToRight = 282f
        val billToBottom = 228f

        // Draw shadow first (darker layer below)
        canvas.drawRoundRect(billToLeft + 2f, billToTop + 2f, billToRight + 2f, billToBottom + 2f, 8f, 8f, shadowPaint)
        // Draw card background with rounded corners
        canvas.drawRoundRect(billToLeft, billToTop, billToRight, billToBottom, 8f, 8f, cardBackgroundPaint)
        // Draw border
        canvas.drawRoundRect(billToLeft, billToTop, billToRight, billToBottom, 8f, 8f, cardBorderPaint)

        val cardLabelPaint = Paint().apply {
            typeface = boldTypeface
            textSize = 8.5f
            color = colors.primary
            isAntiAlias = true
        }
        canvas.drawText("BILL TO", 50f, 142f, cardLabelPaint)

        val cardNamePaint = Paint().apply {
            typeface = boldTypeface
            textSize = 11f
            color = Color.BLACK
            isAntiAlias = true
        }
        canvas.drawText(snapshot.customerName, 50f, 160f, cardNamePaint)

        val cardDetailPaint = Paint().apply {
            typeface = regularTypeface
            textSize = 8.5f
            color = Color.parseColor("#666666")
            isAntiAlias = true
        }
        canvas.drawText(snapshot.customerAddress, 50f, 173f, cardDetailPaint)
        snapshot.customerEmail?.let {
            canvas.drawText(it, 50f, 186f, cardDetailPaint)
        }
        canvas.drawText("Mob: ${snapshot.businessPhone}", 50f, 199f, cardDetailPaint)

        // Invoice Details Card (right) - Enhanced with shadow & rounded corners
        val invoiceLeft = 313f
        val invoiceTop = 125f
        val invoiceRight = 557f
        val invoiceBottom = 228f

        // Draw shadow first
        canvas.drawRoundRect(invoiceLeft + 2f, invoiceTop + 2f, invoiceRight + 2f, invoiceBottom + 2f, 8f, 8f, shadowPaint)
        // Draw card with rounded corners
        canvas.drawRoundRect(invoiceLeft, invoiceTop, invoiceRight, invoiceBottom, 8f, 8f, cardBackgroundPaint)
        // Draw border
        canvas.drawRoundRect(invoiceLeft, invoiceTop, invoiceRight, invoiceBottom, 8f, 8f, cardBorderPaint)

        canvas.drawText("INVOICE", 325f, 142f, cardLabelPaint)

        val invoiceNumberPaint = Paint().apply {
            typeface = boldTypeface
            textSize = 12.5f
            color = colors.primary
            isAntiAlias = true
        }
        canvas.drawText(snapshot.displayName.ifBlank { snapshot.invoiceNumber }, 325f, 160f, invoiceNumberPaint)

        val invoiceDatePaint = Paint().apply {
            typeface = regularTypeface
            textSize = 8.5f
            color = Color.parseColor("#555555")
            isAntiAlias = true
        }
        canvas.drawText("Date: ${formatDate(snapshot.date)}", 325f, 175f, invoiceDatePaint)
        canvas.drawText("Due: ${formatDate(snapshot.dueDate)}", 325f, 188f, invoiceDatePaint)
        canvas.drawText("Status: ${snapshot.invoiceStatus}", 325f, 201f, invoiceDatePaint)

        // ===== WATERMARK (appears on first page) =====
        val watermarkRenderer = PdfWatermarkRenderer(canvas, 595f, 842f)
        watermarkRenderer.drawWatermark(snapshot.invoiceStatus)

        // Update Y position after enhanced cards (now 228px bottom vs old 210px)
        var currentY = 235f  // Increased from 210f to account for 100px header and larger cards
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

        // Ensure space for totals section
        canvas = pageManager.ensureSpace(100f)

        // ===== PHASE 9D: PREMIUM CAPSULE/BADGE TOTALS DESIGN =====
        val totalsCapsuleLeft = 320f
        val totalsCapsuleTop = pageManager.currentY - 10f
        val totalsCapsuleRight = 560f
        val totalsCapsuleHeight = 90f

        // Capsule background (accent color with soft appearance)
        val capsuleBackgroundPaint = Paint().apply {
            color = Color.parseColor("#F5F5F5")
            style = Paint.Style.FILL
        }
        // Draw rounded capsule container
        canvas.drawRoundRect(totalsCapsuleLeft, totalsCapsuleTop, totalsCapsuleRight, totalsCapsuleTop + totalsCapsuleHeight, 10f, 10f, capsuleBackgroundPaint)

        // Accent border on capsule
        val capsuleBorderPaint = Paint().apply {
            color = colors.primary
            strokeWidth = 2f
            style = Paint.Style.STROKE
        }
        canvas.drawRoundRect(totalsCapsuleLeft, totalsCapsuleTop, totalsCapsuleRight, totalsCapsuleTop + totalsCapsuleHeight, 10f, 10f, capsuleBorderPaint)

        // Totals header label
        val totalsHeaderPaint = Paint().apply {
            typeface = boldTypeface
            textSize = 9f
            color = colors.primary
            isAntiAlias = true
        }
        canvas.drawText("TOTALS", 335f, totalsCapsuleTop + 16f, totalsHeaderPaint)

        // Subtotal with luxury spacing
        val subtotalLabelPaint = Paint().apply {
            typeface = regularTypeface
            textSize = 9.5f
            color = Color.parseColor("#666666")
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }
        canvas.drawText("Subtotal:", 480f, totalsCapsuleTop + 32f, subtotalLabelPaint)
        canvas.drawText(String.format(Locale.getDefault(), "%s%.2f", symbol, snapshot.subtotal / 100.0), 545f, totalsCapsuleTop + 32f, subtotalLabelPaint)

        // Tax with generous spacing (if present)
        if (snapshot.taxAmount > 0) {
            canvas.drawText("Tax (${(snapshot.taxRate * 100).toInt()}%):", 480f, totalsCapsuleTop + 50f, subtotalLabelPaint)
            canvas.drawText(String.format(Locale.getDefault(), "%s%.2f", symbol, snapshot.taxAmount / 100.0), 545f, totalsCapsuleTop + 50f, subtotalLabelPaint)
        }

        // Total Amount Due - LARGE, BOLD, PROMINENT
        val totalDuePaint = Paint().apply {
            typeface = boldTypeface
            textSize = 18f  // Increased from 16pt for prominence
            color = colors.primary
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }
        val totalLabel = "TOTAL DUE"
        canvas.drawText(totalLabel, 480f, totalsCapsuleTop + 70f, totalDuePaint)
        val formattedAmount = String.format(Locale.getDefault(), "%s%.2f", symbol, snapshot.totalAmount / 100.0)
        canvas.drawText(formattedAmount, 545f, totalsCapsuleTop + 70f, totalDuePaint)

        pageManager.advanceY(totalsCapsuleHeight + 15f)

        // ===== PHASE 9E: CARD-BASED PAYMENT SECTIONS WITH ROUNDED CORNERS =====
        canvas = pageManager.ensureSpace(130f)

        pageManager.advanceY(15f)

        // Payment section as rounded card
        val paymentBoxPaint = Paint().apply { color = Color.parseColor("#FAFAFA"); style = Paint.Style.FILL }
        val paymentBoxBorderPaint = Paint().apply { color = Color.parseColor("#D8D8D8"); strokeWidth = 1f; style = Paint.Style.STROKE }

        val paymentBoxTop = pageManager.currentY
        val paymentBoxHeight = 110f
        // Draw rounded card background
        canvas.drawRoundRect(40f, paymentBoxTop, 555f, paymentBoxTop + paymentBoxHeight, 8f, 8f, paymentBoxPaint)
        // Draw rounded card border
        canvas.drawRoundRect(40f, paymentBoxTop, 555f, paymentBoxTop + paymentBoxHeight, 8f, 8f, paymentBoxBorderPaint)

        // Payment section header (bold, primary color)
        val paymentSectionHeaderPaint = Paint().apply {
            typeface = boldTypeface
            textSize = 11f
            color = colors.primary
            isAntiAlias = true
        }
        canvas.drawText("PAYMENT DETAILS", 50f, pageManager.currentY + 14f, paymentSectionHeaderPaint)
        pageManager.advanceY(20f)

        // Payment Terms subsection
        bodyPaint.textAlign = Paint.Align.LEFT
        val paymentTermsLabelPaint = Paint().apply {
            typeface = boldTypeface
            textSize = 9f
            color = Color.parseColor("#333333")
            isAntiAlias = true
        }
        canvas.drawText("Payment Terms:", 50f, pageManager.currentY, paymentTermsLabelPaint)
        pageManager.advanceY(11f)
        canvas.drawText("Due within 30 days of invoice date", 65f, pageManager.currentY, bodyPaint)
        pageManager.advanceY(18f)

        // Reference subsection
        canvas.drawText("Reference:", 50f, pageManager.currentY, paymentTermsLabelPaint)
        pageManager.advanceY(11f)
        canvas.drawText(snapshot.invoiceNumber, 65f, pageManager.currentY, bodyPaint)

        pageManager.advanceY(paymentBoxHeight)

        // ===== EFT / BANK TRANSFER DETAILS (Separate card-based section) =====
        val hasBankDetails = snapshot.bankAccountNumber.isNotBlank() || snapshot.bankBsb.isNotBlank()
        if (hasBankDetails) {
            pageManager.advanceY(15f)  // Space between sections

            canvas = pageManager.ensureSpace(100f)

            // EFT section as rounded card
            val eftBoxPaint = Paint().apply { color = Color.parseColor("#FAFAFA"); style = Paint.Style.FILL }
            val eftBoxBorderPaint = Paint().apply { color = Color.parseColor("#D8D8D8"); strokeWidth = 1f; style = Paint.Style.STROKE }

            val eftBoxTop = pageManager.currentY
            val eftBoxHeight = 85f
            // Draw rounded card
            canvas.drawRoundRect(40f, eftBoxTop, 555f, eftBoxTop + eftBoxHeight, 8f, 8f, eftBoxPaint)
            // Draw rounded border
            canvas.drawRoundRect(40f, eftBoxTop, 555f, eftBoxTop + eftBoxHeight, 8f, 8f, eftBoxBorderPaint)

            canvas.drawText("EFT / BANK TRANSFER", 50f, pageManager.currentY + 14f, paymentSectionHeaderPaint)
            pageManager.advanceY(22f)

            if (snapshot.bankName.isNotBlank()) {
                canvas.drawText("Bank:", 50f, pageManager.currentY, paymentTermsLabelPaint)
                canvas.drawText(snapshot.bankName, 65f, pageManager.currentY, bodyPaint)
                pageManager.advanceY(11f)
            }
            if (snapshot.bankAccountName.isNotBlank()) {
                canvas.drawText("Account Name:", 50f, pageManager.currentY, paymentTermsLabelPaint)
                canvas.drawText(snapshot.bankAccountName, 65f, pageManager.currentY, bodyPaint)
                pageManager.advanceY(11f)
            }
            if (snapshot.bankBsb.isNotBlank()) {
                canvas.drawText("BSB:", 50f, pageManager.currentY, paymentTermsLabelPaint)
                canvas.drawText(snapshot.bankBsb, 65f, pageManager.currentY, bodyPaint)
                pageManager.advanceY(11f)
            }
            if (snapshot.bankAccountNumber.isNotBlank()) {
                canvas.drawText("Account Number:", 50f, pageManager.currentY, paymentTermsLabelPaint)
                canvas.drawText(snapshot.bankAccountNumber, 65f, pageManager.currentY, bodyPaint)
            }

            pageManager.advanceY(eftBoxHeight)
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

        // ===== PHASE 9F: ELEGANT MINIMAL FOOTER REDESIGN =====
        canvas = pageManager.ensureSpace(50f)
        pageManager.advanceY(20f)  // Slim spacing

        // Footer background (primary color)
        val artFooterBackgroundPaint = Paint().apply {
            color = colors.primary
            style = Paint.Style.FILL
        }

        val artFooterBarHeight = 35f  // Slim, not heavy
        canvas.drawRect(0f, pageManager.currentY, 595f, pageManager.currentY + artFooterBarHeight, artFooterBackgroundPaint)

        // Subtle accent top line (elegance)
        val footerAccentPaint = Paint().apply {
            color = android.graphics.Color.argb(30, 255, 255, 255)
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, pageManager.currentY, 595f, pageManager.currentY + 2f, footerAccentPaint)

        // Footer styling - elegant and premium
        val artFooterMainPaint = Paint().apply {
            typeface = boldTypeface
            textSize = 11f  // Larger "thank you" message
            color = Color.WHITE
            isAntiAlias = true
        }

        val artFooterSmallPaint = Paint().apply {
            typeface = regularTypeface
            textSize = 7.5f  // Subtle contact info
            color = Color.parseColor("#E8E8E8")
            isAntiAlias = true
        }

        // Center "Thank you" message - primary focus
        artFooterMainPaint.textAlign = Paint.Align.CENTER
        canvas.drawText("Thank you for your business.", 297f, pageManager.currentY + 13f, artFooterMainPaint)

        // Contact info - subtle and compact (centered)
        artFooterSmallPaint.textAlign = Paint.Align.CENTER
        val footerWebsiteDomain = snapshot.businessEmail.substringAfter("@").lowercase()
        val footerContactInfo = "${snapshot.businessEmail} | ${snapshot.businessPhone} | www.$footerWebsiteDomain"
        canvas.drawText(footerContactInfo, 297f, pageManager.currentY + 25f, artFooterSmallPaint)

        pageManager.advanceY(artFooterBarHeight + 3f)

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
