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
import com.emul8r.bizap.data.service.pdf.PdfTableRenderer
import com.emul8r.bizap.data.service.pdf.PdfBrandingRenderer
import com.emul8r.bizap.data.service.pdf.PdfPageManager
import com.emul8r.bizap.data.service.pdf.PdfWatermarkRenderer
import com.emul8r.bizap.domain.pdf.GridLayoutManager
import com.emul8r.bizap.domain.pdf.InvoiceSpacingConfig
import com.emul8r.bizap.domain.service.PdfGenerationService
import com.emul8r.bizap.data.service.templates.TemplateSnapshotManager
import com.emul8r.bizap.utils.DocumentNamingUtils
import com.emul8r.bizap.data.repository.InvoiceSettingsRepository
import com.emul8r.bizap.di.UserIdProvider
import com.emul8r.bizap.domain.model.CanvasInvoiceTemplate
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
 */
@Singleton
class InvoicePdfService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val documentRepository: DocumentRepository,
    private val invoiceSettingsRepository: InvoiceSettingsRepository,
    private val userIdProvider: UserIdProvider
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
        // FIX: Cause #3 - Use theme parameter to route to correct service
        return when (theme) {
            com.emul8r.bizap.domain.model.InvoiceTheme.HTML_PDF -> {
                Timber.d("═══════════════════════════════════════════════════════════════════════════")
                Timber.d("✅ THEME MATCHED: HTML_PDF")
                Timber.d("🎨 Routing to HtmlPdfInvoiceService for PDF generation")
                Timber.d("═══════════════════════════════════════════════════════════════════════════")
                try {
                    // FIX #1: Load current user's invoice settings - MANDATORY (not optional)
                    val currentUserId = userIdProvider.getCurrentUserId()
                    Timber.d("🔍 Step 1: Get current user ID")
                    Timber.d("   User ID: $currentUserId")

                    val settings = try {
                        Timber.d("🔍 Step 2: Load settings from repository")
                        val loadedSettings = invoiceSettingsRepository.getSettings(currentUserId)

                        // FIX #1: Validate settings are not NULL
                        if (loadedSettings == null) {
                            throw IllegalStateException(
                                "Invoice settings not found for user $currentUserId. " +
                                "Settings must be initialized before generating PDF with HTML theme."
                            )
                        }

                        Timber.d("   ✅ Settings loaded successfully")
                        Timber.d("   Selected Theme: ${loadedSettings.selectedTheme.name}")
                        Timber.d("   Selected HTML Style: ${loadedSettings.selectedHtmlStyle.displayName}")
                        Timber.d("   Style enum: ${loadedSettings.selectedHtmlStyle.name}")
                        loadedSettings
                    } catch (e: Exception) {
                        Timber.e(e, "❌ Step 2 FAILED: Could not load settings")
                        Timber.e("   Exception type: ${e.javaClass.simpleName}")
                        Timber.e("   Message: ${e.message}")
                        Timber.e("   This means the selected HTML style CANNOT be applied")
                        throw e  // FIX #1: Don't silently fail - propagate error
                    }

                    // FIX #1: Additional validation
                    Timber.d("🔍 Step 3: Validate settings object")
                    @Suppress("ConstantConditionIf")  // Redundant check: selectedHtmlStyle is guaranteed non-null from Step 2
                    if (settings.selectedHtmlStyle == null) {
                        Timber.e("❌ VALIDATION FAILED: selectedHtmlStyle is NULL")
                        throw IllegalStateException(
                            "Settings loaded but selectedHtmlStyle is NULL. " +
                            "This indicates a data model error."
                        )
                    }
                    Timber.d("   ✅ Validation passed - selectedHtmlStyle is NOT NULL")

                    // Create service with validated settings
                    Timber.d("🔄 Step 4: Create HtmlPdfInvoiceService instance")
                    Timber.d("   Passing settings with HTML style: ${settings.selectedHtmlStyle.displayName}")
                    val htmlPdfService = HtmlPdfInvoiceService(context, settings)

                    Timber.d("🔄 Step 5: Call htmlPdfService.generatePdf()")
                    val result = htmlPdfService.generatePdf(snapshot, isQuote, overwriteExisting, theme)

                    Timber.d("✅ PDF generation complete")
                    Timber.d("   File: ${result.name}")
                    Timber.d("   Size: ${result.length()} bytes")
                    Timber.d("   HTML Style Applied: ${settings.selectedHtmlStyle.displayName}")
                    Timber.d("═══════════════════════════════════════════════════════════════════════════")
                    result
                } catch (e: Exception) {
                    Timber.e(e, "❌ HTML PDF generation failed")
                    throw e
                }
            }
            else -> {
                Timber.d("🎨 Using Canvas theme for PDF generation (default)")
                val currentUserId = userIdProvider.getCurrentUserId()
                val settings = try {
                    invoiceSettingsRepository.getSettings(currentUserId)
                } catch (e: Exception) {
                    Timber.w(e, "Could not load settings for Canvas template selection; using MODERN")
                    null
                }
                val template = settings?.selectedCanvasTemplate ?: CanvasInvoiceTemplate.MODERN
                Timber.d("🎨 Canvas template: ${template.displayName}")
                val overrideColors = PdfColors(
                    primary = android.graphics.Color.parseColor(template.primaryHex),
                    secondary = android.graphics.Color.parseColor(template.accentHex),
                    text = android.graphics.Color.BLACK,
                    textLight = android.graphics.Color.DKGRAY
                )
                generateInvoice(snapshot, isQuote, overwriteExisting, overrideColors = overrideColors)
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
        customFieldValuesJson: String? = null,
        overrideColors: PdfColors? = null
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

        val colors = overrideColors ?: pdfStyler.extractColors(templateSnapshot)
        val hideLineItems = pdfStyler.shouldHideLineItems(templateSnapshot)
        val hidePaymentTerms = pdfStyler.shouldHidePaymentTerms(templateSnapshot)

        val pdfDocument = PdfDocument()
        val pageManager = PdfPageManager(pdfDocument, 595, 842)
        var canvas = pageManager.startNewPage()

        // PHASE 2: Initialize grid layout manager for systematic positioning
        val layoutManager = GridLayoutManager()

        val boldTypeface = pdfStyler.getTypeface(templateSnapshot?.fontFamily, context, isBold = true)
        val regularTypeface = pdfStyler.getTypeface(templateSnapshot?.fontFamily, context, isBold = false)
        val italicTypeface = Typeface.create(regularTypeface, Typeface.ITALIC)

        val symbol = getCurrencySymbol(snapshot.currencyCode)

        val headerPaint = Paint().apply { typeface = boldTypeface; textSize = 10f; color = Color.BLACK; isAntiAlias = true }
        val brandPaint = Paint().apply { typeface = boldTypeface; textSize = 18f; color = colors.primary; isAntiAlias = true }
        val bodyPaint = Paint().apply { typeface = regularTypeface; textSize = 10f; color = colors.textLight; isAntiAlias = true }
        val labelPaint = Paint().apply { typeface = boldTypeface; textSize = 9f; color = colors.primary; isAntiAlias = true }

        // ===== PHASE 2: GRID-BASED HEADER (Compressed: 100px → 60px) =====
        // Using InvoiceSpacingConfig.HEADER_HEIGHT (60px) from design spec
        val headerY = layoutManager.getHeaderY()
        val headerHeight = InvoiceSpacingConfig.HEADER_HEIGHT
        val headerBottom = headerY + headerHeight

        // LAYER 1: Primary color background (base)
        val headerBackgroundPaint = Paint().apply {
            color = colors.primary
            style = Paint.Style.FILL
        }
        canvas.drawRect(
            layoutManager.getContentLeft(),
            headerY,
            layoutManager.getContentRight(),
            headerBottom,
            headerBackgroundPaint
        )

        // LAYER 2: Diagonal accent overlay (right side) - creates visual interest
        val diagonalAccentPaint = Paint().apply {
            color = android.graphics.Color.argb(25, 255, 255, 255)  // Subtle white
            style = Paint.Style.FILL
        }
        val diagonalPath = Path().apply {
            moveTo(420f, headerY)
            lineTo(595f, headerY)
            lineTo(595f, headerY + 50f)
            lineTo(470f, headerBottom)
            close()
        }
        canvas.drawPath(diagonalPath, diagonalAccentPaint)

        // LAYER 3: Wave/curve bottom accent (elegant transition)
        val waveBottomPaint = Paint().apply {
            color = android.graphics.Color.argb(20, 0, 0, 0)
            style = Paint.Style.FILL
        }
        canvas.drawRect(layoutManager.getContentLeft(), headerBottom - 4f, layoutManager.getContentRight(), headerBottom, waveBottomPaint)

        // Draw logo if available (left side, fully visible)
        val brandingRenderer = PdfBrandingRenderer(canvas, 595f)
        brandingRenderer.drawLogo(snapshot.logoBase64)

        // Premium white text styling
        val artisticHeaderPaint = Paint().apply {
            typeface = boldTypeface
            textSize = InvoiceSpacingConfig.TEXT_SIZE_HEADER
            color = Color.WHITE
            isAntiAlias = true
        }

        val artisticSubheaderPaint = Paint().apply {
            typeface = regularTypeface
            textSize = InvoiceSpacingConfig.TEXT_SIZE_SMALL
            color = Color.parseColor("#E8E8E8")
            isAntiAlias = true
        }

        // Company name (left side, prominent) - Grid-based positioning
        canvas.drawText(
            snapshot.businessName.uppercase(),
            layoutManager.getX(14),  // 14 grid units from left margin
            headerY + 18f,           // Top-aligned text
            artisticHeaderPaint
        )

        // INVOICE LABEL (right side, elegant positioning - not a stamp)
        val invoiceLabelPaint = Paint().apply {
            typeface = boldTypeface
            textSize = 11f
            color = Color.parseColor("#C0C0C0")
            isAntiAlias = true
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("INVOICE", layoutManager.getContentRight() - 10f, headerY + 12f, invoiceLabelPaint)
        canvas.drawText(snapshot.invoiceNumber, layoutManager.getContentRight() - 10f, headerY + 24f, invoiceLabelPaint)

        // Business info (right-aligned, clean) - Grid-based
        // ✅ FIX: Moved to compact 2-line format to fit within header bounds (60px height)
        val compactBusinessPaint = Paint().apply {
            typeface = regularTypeface
            textSize = 8f  // Smaller font to fit
            color = Color.parseColor("#E8E8E8")
            isAntiAlias = true
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("ABN: ${snapshot.businessAbn} | ${snapshot.businessPhone}", layoutManager.getContentRight() - 10f, headerY + 36f, compactBusinessPaint)
        canvas.drawText(snapshot.businessEmail, layoutManager.getContentRight() - 10f, headerY + 46f, compactBusinessPaint)

        // ===== PHASE 2: TWO-COLUMN LAYOUT (Bill To | Invoice Details - Side-by-Side) =====
        // Using grid manager for systematic positioning
        // Each column: 80px height, equal width, 12px gap between

        // Card background (white with subtle accent tint)
        val cardBackgroundPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        val cardBorderPaint = Paint().apply {
            color = Color.parseColor("#D8D8D8")
            strokeWidth = InvoiceSpacingConfig.BORDER_WIDTH
            style = Paint.Style.STROKE
        }

        // Shadow paint for depth effect
        val shadowPaint = Paint().apply {
            color = android.graphics.Color.argb(15, 0, 0, 0)
            style = Paint.Style.FILL
        }

        // ===== BILL TO CARD (Left) =====
        val billToY = layoutManager.getBillToY()
        val billToHeight = InvoiceSpacingConfig.BILL_TO_HEIGHT
        val billToLeft = layoutManager.getBillToLeft()
        val billToRight = layoutManager.getBillToRight()
        val billToBottom = billToY + billToHeight

        // Draw shadow first (darker layer below)
        canvas.drawRoundRect(
            billToLeft + InvoiceSpacingConfig.SHADOW_OFFSET,
            billToY + InvoiceSpacingConfig.SHADOW_OFFSET,
            billToRight + InvoiceSpacingConfig.SHADOW_OFFSET,
            billToBottom + InvoiceSpacingConfig.SHADOW_OFFSET,
            InvoiceSpacingConfig.CORNER_RADIUS,
            InvoiceSpacingConfig.CORNER_RADIUS,
            shadowPaint
        )
        // Draw card background with rounded corners
        canvas.drawRoundRect(billToLeft, billToY, billToRight, billToBottom, InvoiceSpacingConfig.CORNER_RADIUS, InvoiceSpacingConfig.CORNER_RADIUS, cardBackgroundPaint)
        // Draw border
        canvas.drawRoundRect(billToLeft, billToY, billToRight, billToBottom, InvoiceSpacingConfig.CORNER_RADIUS, InvoiceSpacingConfig.CORNER_RADIUS, cardBorderPaint)

        // Add accent color left-side bar (modern design element)
        val accentBarPaint = Paint().apply {
            color = colors.secondary
            style = Paint.Style.FILL
        }
        canvas.drawRect(billToLeft, billToY, billToLeft + InvoiceSpacingConfig.ACCENT_BAR_WIDTH, billToBottom, accentBarPaint)

        val cardLabelPaint = Paint().apply {
            typeface = boldTypeface
            textSize = InvoiceSpacingConfig.TEXT_SIZE_SECTION_HEADER
            color = colors.primary
            isAntiAlias = true
        }
        canvas.drawText("BILL TO", billToLeft + InvoiceSpacingConfig.PADDING_H, billToY + 17f, cardLabelPaint)

        val cardNamePaint = Paint().apply {
            typeface = boldTypeface
            textSize = 11f
            color = Color.BLACK
            isAntiAlias = true
        }
        canvas.drawText(snapshot.customerName, billToLeft + InvoiceSpacingConfig.PADDING_H, billToY + 35f, cardNamePaint)

        val cardDetailPaint = Paint().apply {
            typeface = regularTypeface
            textSize = InvoiceSpacingConfig.TEXT_SIZE_SMALL
            color = Color.parseColor("#666666")
            isAntiAlias = true
        }
        canvas.drawText(snapshot.customerAddress, billToLeft + InvoiceSpacingConfig.PADDING_H, billToY + 48f, cardDetailPaint)
        snapshot.customerEmail?.let {
            canvas.drawText(it, billToLeft + InvoiceSpacingConfig.PADDING_H, billToY + 61f, cardDetailPaint)
        }
        canvas.drawText("Mob: ${snapshot.businessPhone}", billToLeft + InvoiceSpacingConfig.PADDING_H, billToY + 74f, cardDetailPaint)

        // ===== INVOICE DETAILS CARD (Right - Side-by-Side with Bill To) =====
        val invoiceDetailsY = layoutManager.getInvoiceDetailsY()
        val invoiceDetailsHeight = InvoiceSpacingConfig.INVOICE_DETAILS_HEIGHT
        val invoiceDetailsLeft = layoutManager.getInvoiceDetailsLeft()
        val invoiceDetailsRight = layoutManager.getInvoiceDetailsRight()
        val invoiceDetailsBottom = invoiceDetailsY + invoiceDetailsHeight

        // Draw shadow first
        canvas.drawRoundRect(
            invoiceDetailsLeft + InvoiceSpacingConfig.SHADOW_OFFSET,
            invoiceDetailsY + InvoiceSpacingConfig.SHADOW_OFFSET,
            invoiceDetailsRight + InvoiceSpacingConfig.SHADOW_OFFSET,
            invoiceDetailsBottom + InvoiceSpacingConfig.SHADOW_OFFSET,
            InvoiceSpacingConfig.CORNER_RADIUS,
            InvoiceSpacingConfig.CORNER_RADIUS,
            shadowPaint
        )
        // Draw card with rounded corners
        canvas.drawRoundRect(invoiceDetailsLeft, invoiceDetailsY, invoiceDetailsRight, invoiceDetailsBottom, InvoiceSpacingConfig.CORNER_RADIUS, InvoiceSpacingConfig.CORNER_RADIUS, cardBackgroundPaint)
        // Draw border
        canvas.drawRoundRect(invoiceDetailsLeft, invoiceDetailsY, invoiceDetailsRight, invoiceDetailsBottom, InvoiceSpacingConfig.CORNER_RADIUS, InvoiceSpacingConfig.CORNER_RADIUS, cardBorderPaint)

        // Add accent color left-side bar (modern design element)
        canvas.drawRect(invoiceDetailsLeft, invoiceDetailsY, invoiceDetailsLeft + InvoiceSpacingConfig.ACCENT_BAR_WIDTH, invoiceDetailsBottom, accentBarPaint)

        canvas.drawText("INVOICE", invoiceDetailsLeft + InvoiceSpacingConfig.PADDING_H, invoiceDetailsY + 17f, cardLabelPaint)

        val invoiceNumberPaint = Paint().apply {
            typeface = boldTypeface
            textSize = 12.5f
            color = colors.primary
            isAntiAlias = true
        }
        canvas.drawText(snapshot.displayName.ifBlank { snapshot.invoiceNumber }, invoiceDetailsLeft + InvoiceSpacingConfig.PADDING_H, invoiceDetailsY + 35f, invoiceNumberPaint)

        val invoiceDatePaint = Paint().apply {
            typeface = regularTypeface
            textSize = InvoiceSpacingConfig.TEXT_SIZE_SMALL
            color = Color.parseColor("#555555")
            isAntiAlias = true
        }
        canvas.drawText("Date: ${formatDate(snapshot.date)}", invoiceDetailsLeft + InvoiceSpacingConfig.PADDING_H, invoiceDetailsY + 50f, invoiceDatePaint)
        canvas.drawText("Due: ${formatDate(snapshot.dueDate)}", invoiceDetailsLeft + InvoiceSpacingConfig.PADDING_H, invoiceDetailsY + 63f, invoiceDatePaint)
        canvas.drawText("Status: ${snapshot.invoiceStatus}", invoiceDetailsLeft + InvoiceSpacingConfig.PADDING_H, invoiceDetailsY + 76f, invoiceDatePaint)

        // ===== WATERMARK (appears on first page) =====
        val watermarkRenderer = PdfWatermarkRenderer(canvas, 595f, 842f)
        watermarkRenderer.drawWatermark(snapshot.invoiceStatus)

        // ===== PHASE 2: Update Y position using grid-based calculation =====
        // Header block bottom: header + gap + bill to = complete top section
        var currentY = layoutManager.getInvoiceHeaderBlockBottom() + InvoiceSpacingConfig.SECTION_GAP
        pageManager.setY(currentY)


        val separatorPaint = Paint().apply { color = colors.secondary; strokeWidth = 0.5f; style = Paint.Style.STROKE }
        val sectionHeaderPaint = Paint().apply { typeface = boldTypeface; textSize = 11f; color = colors.primary; isAntiAlias = true }
        val subheaderBodyPaint = Paint().apply { typeface = italicTypeface; textSize = 10f; color = colors.textLight; isAntiAlias = true }
        val footerBodyPaint = Paint().apply { typeface = italicTypeface; textSize = 9f; color = Color.GRAY; isAntiAlias = true }


        // ===== HEADER AND SUBHEADER TEXT (Optional, appears before line items) =====
        if (snapshot.headerText.isNotBlank() || snapshot.subheaderText.isNotBlank()) {
            canvas = pageManager.ensureSpace(50f)

            // ✅ FIX: Header/Subheader rendering with proper spacing to prevent overlap
            if (snapshot.headerText.isNotBlank()) {
                val headerTextPaint = Paint().apply {
                    typeface = boldTypeface
                    textSize = 14f  // Prominent header
                    color = colors.primary
                    isAntiAlias = true
                }
                canvas.drawText(snapshot.headerText, 40f, pageManager.currentY + 12f, headerTextPaint)
                pageManager.advanceY(20f)  // Increased spacing for large header
            }

            if (snapshot.subheaderText.isNotBlank()) {
                val subheaderPaint = Paint().apply {
                    typeface = regularTypeface
                    textSize = 11f  // Slightly smaller
                    color = colors.textLight
                    isAntiAlias = true
                }
                canvas.drawText(snapshot.subheaderText, 40f, pageManager.currentY + 10f, subheaderPaint)
                pageManager.advanceY(16f)  // Good spacing after subheader
            }

            pageManager.advanceY(12f)  // Extra spacing before items table
        }

        if (!hideLineItems) {
            // ===== PHASE 2: ITEMS TABLE WITH GRID-BASED POSITIONING =====
            // Draw table header with professional styling
            val headerTextPaint = Paint(headerPaint).apply {
                color = Color.WHITE
                textSize = 11f
                typeface = boldTypeface
            }

            // Get table position from grid manager
            val itemsTableY = layoutManager.getItemsTableY()
            val tableHeaderHeight = InvoiceSpacingConfig.TABLE_HEADER_HEIGHT

            // Ensure space for header row
            canvas = pageManager.ensureSpace(tableHeaderHeight + (snapshot.items.size * InvoiceSpacingConfig.TABLE_ROW_HEIGHT))

            // Set page manager to table position
            pageManager.setY(itemsTableY)

            // Draw table border top
            val tableBorderPaint = Paint().apply {
                color = colors.primary
                strokeWidth = 2f
                style = Paint.Style.STROKE
            }
            canvas.drawLine(layoutManager.getItemsTableLeft(), itemsTableY, layoutManager.getItemsTableRight(), itemsTableY, tableBorderPaint)

            val tableRenderer = PdfTableRenderer(
                canvas = canvas,
                startX = layoutManager.getItemsTableLeft(),
                currentY = itemsTableY,
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

            // Draw table rows using grid-based row height
            snapshot.items.forEachIndexed { index, item ->
                // Use consistent row height from InvoiceSpacingConfig
                val rowHeight = InvoiceSpacingConfig.TABLE_ROW_HEIGHT

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
            canvas.drawLine(layoutManager.getItemsTableLeft(), pageManager.currentY, layoutManager.getItemsTableRight(), pageManager.currentY, tableBorderPaint)

            // Update current position after items table
            currentY = pageManager.currentY + InvoiceSpacingConfig.SECTION_GAP
            pageManager.setY(currentY)
        }

        bodyPaint.textAlign = Paint.Align.RIGHT
        headerPaint.textAlign = Paint.Align.RIGHT

        // ===== PHASE 2: INTEGRATED TOTALS SECTION (Typography-Driven) =====
        // Get totals position from grid manager (depends on items count)
        val itemCount = snapshot.items.size
        val totalsY = layoutManager.getTotalsY(itemCount)
        val totalsHeight = InvoiceSpacingConfig.TOTALS_HEIGHT
        val totalsLeft = layoutManager.getTotalsLeft()
        val totalsRight = layoutManager.getTotalsRight()

        // Ensure space for totals section
        canvas = pageManager.ensureSpace(totalsHeight + InvoiceSpacingConfig.SECTION_GAP)
        pageManager.setY(totalsY)

        // ===== TYPOGRAPHY-DRIVEN HIERARCHY (No Floating Box) =====

        // Subtotal line
        val subtotalLabelPaint = Paint().apply {
            typeface = regularTypeface
            textSize = InvoiceSpacingConfig.TEXT_SIZE_BODY
            color = Color.parseColor("#333333")
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }
        val subtotalY = totalsY
        canvas.drawText("Subtotal:", totalsRight - 10f, subtotalY + 12f, subtotalLabelPaint)
        canvas.drawText(String.format(Locale.getDefault(), "%s%.2f", symbol, snapshot.subtotal / 100.0), totalsRight - 10f, subtotalY + 12f, subtotalLabelPaint)

        // Tax line (if present)
        if (snapshot.taxAmount > 0) {
            val taxLabelPaint = Paint().apply {
                typeface = regularTypeface
                textSize = InvoiceSpacingConfig.TEXT_SIZE_BODY
                color = Color.parseColor("#333333")
                textAlign = Paint.Align.RIGHT
                isAntiAlias = true
            }
            val taxY = subtotalY + 16f
            canvas.drawText("Tax (${(snapshot.taxRate * 100).toInt()}%):", totalsRight - 10f, taxY + 12f, taxLabelPaint)
            canvas.drawText(String.format(Locale.getDefault(), "%s%.2f", symbol, snapshot.taxAmount / 100.0), totalsRight - 10f, taxY + 12f, taxLabelPaint)
        }

        // Divider line (visual separation)
        val dividerPaint = Paint().apply {
            color = colors.secondary
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }
        val dividerY = totalsY + 30f
        canvas.drawLine(totalsLeft + 10f, dividerY, totalsRight - 10f, dividerY, dividerPaint)

        // TOTAL DUE - EMPHASIZED (Large, bold, primary color)
        val totalDueLabelPaint = Paint().apply {
            typeface = boldTypeface
            textSize = InvoiceSpacingConfig.TEXT_SIZE_TOTAL_LABEL
            color = Color.parseColor("#333333")
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }
        val totalDueAmountPaint = Paint().apply {
            typeface = boldTypeface
            textSize = InvoiceSpacingConfig.TEXT_SIZE_TOTAL_AMOUNT
            color = colors.primary  // Primary color for prominence
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }
        val totalDueY = dividerY + 8f
        canvas.drawText("TOTAL DUE", totalsRight - 10f, totalDueY + 12f, totalDueLabelPaint)
        val formattedAmount = String.format(Locale.getDefault(), "%s%.2f", symbol, snapshot.totalAmount / 100.0)
        canvas.drawText(formattedAmount, totalsRight - 10f, totalDueY + 32f, totalDueAmountPaint)

        // Accent underline under TOTAL DUE (visual emphasis)
        val accentUnderlinePaint = Paint().apply {
            color = colors.primary
            strokeWidth = 2f
            style = Paint.Style.STROKE
        }
        canvas.drawLine(totalsLeft + 10f, totalDueY + 38f, totalsRight - 10f, totalDueY + 38f, accentUnderlinePaint)

        pageManager.advanceY(totalsHeight + InvoiceSpacingConfig.SECTION_GAP)

        // ===== SPACING CONSTANTS FOR PROPER LAYOUT =====
        val SECTION_MARGIN_TOP = 24f      // Gap before section
        val SECTION_HEADER_HEIGHT = 28f   // Height of colored header bar
        val SECTION_PADDING_TOP = 16f     // Padding inside section (top)
        val SECTION_PADDING_HORIZ = 16f   // Horizontal padding inside section
        val SECTION_PADDING_BOTTOM = 16f  // Padding inside section (bottom)
        val LINE_HEIGHT = 18f             // Height for each text line
        val LABEL_VALUE_GAP = 6f          // Gap between label and value
        val ROW_SPACING = 12f             // Gap between rows
        val CONTENT_BG_COLOR = Color.parseColor("#F8F9FA")  // Light gray

        // Paints for sections
        val layoutFixSectionHeaderPaint = Paint().apply {
            typeface = boldTypeface
            textSize = 13f
            color = Color.WHITE
            isAntiAlias = true
        }
        val sectionHeaderBgPaint = Paint().apply {
            color = colors.primary
            style = Paint.Style.FILL
        }
        val leftAccentBarPaint = Paint().apply {
            color = colors.secondary
            style = Paint.Style.FILL
        }
        val contentBgPaint = Paint().apply {
            color = CONTENT_BG_COLOR
            style = Paint.Style.FILL
        }
        val fieldLabelPaint = Paint().apply {
            typeface = boldTypeface
            textSize = 11f
            color = Color.parseColor("#333333")
            isAntiAlias = true
        }
        val fieldValuePaint = Paint().apply {
            typeface = regularTypeface
            textSize = 11f
            color = Color.parseColor("#555555")
            isAntiAlias = true
        }

        // ===== PAYMENT DETAILS SECTION - PROPER LAYOUT =====
        canvas = pageManager.ensureSpace(200f)
        var currentSectionY = pageManager.currentY + SECTION_MARGIN_TOP

        // Calculate section height based on content
        val paymentContentLines = 4  // "Payment Terms:" (label) + "Due..." (value) + spacing + "Reference:" + spacing
        val paymentSectionHeight = SECTION_HEADER_HEIGHT + SECTION_PADDING_TOP + (paymentContentLines * LINE_HEIGHT) + (2 * ROW_SPACING) + SECTION_PADDING_BOTTOM

        // Draw background
        canvas.drawRect(40f, currentSectionY, 555f, currentSectionY + paymentSectionHeight, contentBgPaint)

        // Draw header bar
        canvas.drawRect(40f, currentSectionY, 555f, currentSectionY + SECTION_HEADER_HEIGHT, sectionHeaderBgPaint)

        // Draw left accent bar
        canvas.drawRect(40f, currentSectionY, 45f, currentSectionY + paymentSectionHeight, leftAccentBarPaint)

        // Draw header text
        canvas.drawText("PAYMENT DETAILS", 55f, currentSectionY + SECTION_HEADER_HEIGHT - 8f, layoutFixSectionHeaderPaint)

        // Start content below header
        var contentY = currentSectionY + SECTION_HEADER_HEIGHT + SECTION_PADDING_TOP

        // Payment Terms label
        canvas.drawText("Payment Terms:", 55f, contentY, fieldLabelPaint)
        contentY += LINE_HEIGHT + LABEL_VALUE_GAP

        // Payment Terms value
        canvas.drawText("Due within 30 days of invoice date", 71f, contentY, fieldValuePaint)
        contentY += LINE_HEIGHT + ROW_SPACING

        // Reference label
        canvas.drawText("Reference:", 55f, contentY, fieldLabelPaint)
        contentY += LINE_HEIGHT + LABEL_VALUE_GAP

        // Reference value
        val refValue = if (snapshot.invoiceNumber.isNotBlank()) snapshot.invoiceNumber else "Not provided"
        canvas.drawText(refValue, 71f, contentY, fieldValuePaint)

        pageManager.setY(currentSectionY + paymentSectionHeight + SECTION_MARGIN_TOP)

        // ===== EFT / BANK TRANSFER SECTION - PROPER LAYOUT =====
        val hasBankDetails = snapshot.bankAccountNumber.isNotBlank() || snapshot.bankBsb.isNotBlank()
            || snapshot.bankName.isNotBlank() || snapshot.bankAccountName.isNotBlank()

        if (hasBankDetails) {
            canvas = pageManager.ensureSpace(250f)
            currentSectionY = pageManager.currentY + SECTION_MARGIN_TOP

            // Calculate section height - 4 fields × (label height + value height + spacing)
            val bankContentLines = 8  // 4 fields × 2 lines each
            val bankSectionHeight = SECTION_HEADER_HEIGHT + SECTION_PADDING_TOP + (bankContentLines * LINE_HEIGHT) + (3 * ROW_SPACING) + SECTION_PADDING_BOTTOM

            // Draw background
            canvas.drawRect(40f, currentSectionY, 555f, currentSectionY + bankSectionHeight, contentBgPaint)

            // Draw header bar
            canvas.drawRect(40f, currentSectionY, 555f, currentSectionY + SECTION_HEADER_HEIGHT, sectionHeaderBgPaint)

            // Draw left accent bar
            canvas.drawRect(40f, currentSectionY, 45f, currentSectionY + bankSectionHeight, leftAccentBarPaint)

            // Draw header text
            canvas.drawText("EFT / BANK TRANSFER", 55f, currentSectionY + SECTION_HEADER_HEIGHT - 8f, layoutFixSectionHeaderPaint)

            // Start content below header
            contentY = currentSectionY + SECTION_HEADER_HEIGHT + SECTION_PADDING_TOP

            // Bank Name
            if (snapshot.bankName.isNotBlank()) {
                canvas.drawText("Bank Name:", 55f, contentY, fieldLabelPaint)
                contentY += LINE_HEIGHT + LABEL_VALUE_GAP
                canvas.drawText(snapshot.bankName, 71f, contentY, fieldValuePaint)
                contentY += LINE_HEIGHT + ROW_SPACING
            }

            // Account Name
            if (snapshot.bankAccountName.isNotBlank()) {
                canvas.drawText("Account Name:", 55f, contentY, fieldLabelPaint)
                contentY += LINE_HEIGHT + LABEL_VALUE_GAP
                canvas.drawText(snapshot.bankAccountName, 71f, contentY, fieldValuePaint)
                contentY += LINE_HEIGHT + ROW_SPACING
            }

            // BSB
            if (snapshot.bankBsb.isNotBlank()) {
                canvas.drawText("BSB:", 55f, contentY, fieldLabelPaint)
                contentY += LINE_HEIGHT + LABEL_VALUE_GAP
                canvas.drawText(snapshot.bankBsb, 71f, contentY, fieldValuePaint)
                contentY += LINE_HEIGHT + ROW_SPACING
            }

            // Account Number
            if (snapshot.bankAccountNumber.isNotBlank()) {
                canvas.drawText("Account Number:", 55f, contentY, fieldLabelPaint)
                contentY += LINE_HEIGHT + LABEL_VALUE_GAP
                canvas.drawText(snapshot.bankAccountNumber, 71f, contentY, fieldValuePaint)
            }

            pageManager.setY(currentSectionY + bankSectionHeight + SECTION_MARGIN_TOP)
        }

        pageManager.advanceY(8f)  // Small spacing after sections

        // Render notes and footer below totals
        bodyPaint.textAlign = Paint.Align.LEFT
        if (snapshot.notes.isNotBlank()) {
            canvas = pageManager.ensureSpace(70f)
            pageManager.advanceY(20f)

            val notesBoxPaint = Paint().apply { color = Color.parseColor("#FAFAFA"); style = Paint.Style.FILL }
            val notesBoxBorderPaint = Paint().apply { color = colors.secondary; strokeWidth = 1f; style = Paint.Style.STROKE }

            val notesBoxTop = pageManager.currentY
            canvas.drawRect(layoutManager.getContentLeft(), notesBoxTop, layoutManager.getContentRight(), notesBoxTop + 55f, notesBoxPaint)
            canvas.drawRect(layoutManager.getContentLeft(), notesBoxTop, layoutManager.getContentRight(), notesBoxTop + 55f, notesBoxBorderPaint)

            canvas.drawText("NOTES", layoutManager.getContentLeft() + InvoiceSpacingConfig.PADDING_H, pageManager.currentY + 12f, labelPaint)
            pageManager.advanceY(18f)
            pageManager.setY(drawWrappedText(canvas, snapshot.notes, layoutManager.getContentLeft() + InvoiceSpacingConfig.PADDING_H, pageManager.currentY, layoutManager.getContentWidth() - InvoiceSpacingConfig.PADDING_H * 2, bodyPaint))
        }

        if (snapshot.footerText.isNotBlank()) {
            canvas = pageManager.ensureSpace(70f)
            pageManager.advanceY(20f)

            val footerBoxPaint = Paint().apply { color = Color.parseColor("#F5F5F5"); style = Paint.Style.FILL }
            val footerBoxBorderPaint = Paint().apply { color = colors.secondary; strokeWidth = 1f; style = Paint.Style.STROKE }

            val footerBoxTop = pageManager.currentY
            canvas.drawRect(layoutManager.getContentLeft(), footerBoxTop, layoutManager.getContentRight(), footerBoxTop + 55f, footerBoxPaint)
            canvas.drawRect(layoutManager.getContentLeft(), footerBoxTop, layoutManager.getContentRight(), footerBoxTop + 55f, footerBoxBorderPaint)

            canvas.drawText("FOOTER", layoutManager.getContentLeft() + InvoiceSpacingConfig.PADDING_H, pageManager.currentY + 12f, labelPaint)
            pageManager.advanceY(18f)
            pageManager.setY(drawWrappedText(canvas, snapshot.footerText, layoutManager.getContentLeft() + InvoiceSpacingConfig.PADDING_H, pageManager.currentY, layoutManager.getContentWidth() - InvoiceSpacingConfig.PADDING_H * 2, bodyPaint))
        }

        // ===== PHASE 2: ELEGANT MINIMAL FOOTER (Grid-Based) =====
        // Using GridLayoutManager for footer positioning
        canvas = pageManager.ensureSpace(InvoiceSpacingConfig.FOOTER_HEIGHT + 10f)
        pageManager.advanceY(InvoiceSpacingConfig.SECTION_GAP)

        val footerY = pageManager.currentY
        val footerHeight = InvoiceSpacingConfig.FOOTER_HEIGHT

        // Footer background (primary color)
        val artFooterBackgroundPaint = Paint().apply {
            color = colors.primary
            style = Paint.Style.FILL
        }

        canvas.drawRect(0f, footerY, 595f, footerY + footerHeight, artFooterBackgroundPaint)

        // Subtle accent top line (elegance)
        val footerAccentPaint = Paint().apply {
            color = android.graphics.Color.argb(30, 255, 255, 255)
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, footerY, 595f, footerY + 2f, footerAccentPaint)

        // Footer styling - elegant and premium
        val artFooterMainPaint = Paint().apply {
            typeface = boldTypeface
            textSize = InvoiceSpacingConfig.TEXT_SIZE_SECTION_HEADER
            color = Color.WHITE
            isAntiAlias = true
        }

        val artFooterSmallPaint = Paint().apply {
            typeface = regularTypeface
            textSize = InvoiceSpacingConfig.TEXT_SIZE_SMALL
            color = Color.parseColor("#E8E8E8")
            isAntiAlias = true
        }

        // Center "Thank you" message - primary focus
        artFooterMainPaint.textAlign = Paint.Align.CENTER
        canvas.drawText("Thank you for your business.", 297f, footerY + 13f, artFooterMainPaint)

        // Contact info - subtle and compact (centered)
        artFooterSmallPaint.textAlign = Paint.Align.CENTER
        val footerWebsiteDomain = snapshot.businessEmail.substringAfter("@").lowercase()
        val footerContactInfo = "${snapshot.businessEmail} | ${snapshot.businessPhone} | www.$footerWebsiteDomain"
        canvas.drawText(footerContactInfo, 297f, footerY + 25f, artFooterSmallPaint)

        pageManager.advanceY(footerHeight + 3f)

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
