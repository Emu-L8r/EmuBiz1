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
import com.emul8r.bizap.domain.model.TotalBoxStyle
import com.emul8r.bizap.domain.model.CurrencyPosition
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
                    // Note: selectedHtmlStyle is guaranteed non-null from Step 2, redundant check removed
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

        // Ensure snapshot reflects the isQuote flag so rendering uses correct labels/watermark
        val snapshot = if (isQuote != snapshot.isQuote) snapshot.copy(isQuote = isQuote) else snapshot

        val templateSnapshot = snapshotManager.restoreSnapshot(templateSnapshotJson)
        val customFieldValues = snapshotManager.restoreCustomFieldValues(customFieldValuesJson)

        // ✅ PHASE 2 P2.5: Build theme colors from snapshot (not just template)
        // ✅ PHASE 2 FEATURE #3: COLOR SCHEME APPLICATION
        // Apply color scheme based on selectedColorScheme setting
        val schemeColors = when (snapshot.selectedColorScheme) {
            com.emul8r.bizap.domain.model.ColorScheme.PROFESSIONAL -> Triple("#003366", "#FFC107", "#2c3e50")  // Navy + Gold
            com.emul8r.bizap.domain.model.ColorScheme.VIBRANT -> Triple("#6B4C9A", "#FF9F43", "#333333")  // Purple + Orange
            com.emul8r.bizap.domain.model.ColorScheme.MINIMAL -> Triple("#1a1a1a", "#666666", "#000000")  // Grayscale
            com.emul8r.bizap.domain.model.ColorScheme.WARM -> Triple("#D97706", "#78350F", "#92400E")  // Amber + Brown
            com.emul8r.bizap.domain.model.ColorScheme.TECH -> Triple("#0F172A", "#06B6D4", "#1e293b")  // Deep blue + Cyan
            com.emul8r.bizap.domain.model.ColorScheme.NATURE -> Triple("#15803D", "#92400E", "#166534")  // Green + Earth
        }

        val colors = overrideColors ?: run {
            val templateColors = pdfStyler.extractColors(templateSnapshot)
            // Override with snapshot theme colors (scheme takes precedence)
            templateColors.copy(
                primary = try { Color.parseColor(schemeColors.first) } catch (e: Exception) { templateColors.primary },
                secondary = try { Color.parseColor(schemeColors.second) } catch (e: Exception) { templateColors.secondary }
            )
        }
        val hideLineItems = pdfStyler.shouldHideLineItems(templateSnapshot)
        val hidePaymentTerms = pdfStyler.shouldHidePaymentTerms(templateSnapshot)

        val pdfDocument = PdfDocument()
        val pageManager = PdfPageManager(pdfDocument, 595, 842)
        var canvas = pageManager.startNewPage()

        // PHASE 2: Initialize grid layout manager for systematic positioning
        val layoutManager = GridLayoutManager()

        // ✅ BACKGROUND LAYER 1: Brand watermark — drawn first, below all content
        // Gated by snapshot.enableBrandWatermark (user toggle in Customization Settings)
        if (snapshot.enableBrandWatermark) {
            drawBrandWatermark(canvas, context, colors.primary)
        }

        // ✅ PHASE 3 COMPLEX: BACKGROUND PATTERNS — Draw before any content (layer 2, above watermark)
        if (snapshot.enableBackgroundPattern) {
            drawBackgroundPattern(canvas, snapshot.backgroundPatternType, snapshot.patternOpacity)
        }

        // ✅ PHASE 2 FEATURE #2: TYPOGRAPHY SELECTION
        // Get font family based on selectedTypography setting
        val typographyFontFamily = when (snapshot.selectedTypography) {
            com.emul8r.bizap.domain.model.Typography.MODERN -> null  // System sans-serif (default)
            com.emul8r.bizap.domain.model.Typography.CLASSIC -> "serif"  // Serif fonts (Times New Roman, Georgia)
            com.emul8r.bizap.domain.model.Typography.ROUNDED -> "monospace"  // Rounded mono fonts
        }

        val boldTypeface = pdfStyler.getTypeface(typographyFontFamily ?: templateSnapshot?.fontFamily, context, isBold = true)
        val regularTypeface = pdfStyler.getTypeface(typographyFontFamily ?: templateSnapshot?.fontFamily, context, isBold = false)
        val italicTypeface = Typeface.create(regularTypeface, Typeface.ITALIC)

        val symbol = getCurrencySymbol(snapshot.currencyCode)

        // ✅ PHASE 2 FEATURE #1: LOCALE SUPPORT
        // Get locale-specific formatting from snapshot.selectedLocale
        val localeConfig = getLocaleConfig(snapshot.selectedLocale)
        val currencyFormatter = { amount: Long ->
            val value = amount / 100.0
            val formattedValue = String.format(
                localeConfig.locale,
                "%.${localeConfig.decimalPlaces}f",
                value
            ).replace('.', localeConfig.decimalSeparator).replace(',', localeConfig.thousandsSeparator)

            // Position currency symbol based on locale
            when (localeConfig.currencyPosition) {
                CurrencyPosition.BEFORE -> "${localeConfig.currencySymbol}${formattedValue}"
                CurrencyPosition.AFTER -> "${formattedValue} ${localeConfig.currencySymbol}"
            }
        }

        val dateFormatter = { timestamp: Long ->
            SimpleDateFormat(localeConfig.dateFormat, localeConfig.locale).format(Date(timestamp))
        }

        // ✅ PHASE 2 FEATURE #4: SPACING PROFILES — Initialize spacing multiplier
        val spacingMultiplier = InvoiceSpacingConfig.getSpacingMultiplier(snapshot.selectedSpacingProfile)
        val adjustedSectionGap = InvoiceSpacingConfig.SECTION_GAP * spacingMultiplier
        val adjustedPaddingV = InvoiceSpacingConfig.PADDING_V * spacingMultiplier

        // ✅ PHASE 2 FEATURE #5: VISUAL ACCENTS — Parse visual accent settings
        val visualAccents = parseVisualAccents(snapshot.visualAccentsJson)
        Timber.d("Visual Accents: showBorders=${visualAccents.showBorders}, showShadows=${visualAccents.showShadows}, showDividers=${visualAccents.showDividers}, highlightTotals=${visualAccents.highlightTotals}, useGradients=${visualAccents.useGradients}")

        val headerPaint = Paint().apply { typeface = boldTypeface; textSize = 10f; color = Color.BLACK; isAntiAlias = true }
        val brandPaint = Paint().apply { typeface = boldTypeface; textSize = 18f; color = colors.primary; isAntiAlias = true }
        val bodyPaint = Paint().apply { typeface = regularTypeface; textSize = 10f; color = colors.textLight; isAntiAlias = true }
        val labelPaint = Paint().apply { typeface = boldTypeface; textSize = 9f; color = colors.primary; isAntiAlias = true }

        // ===== PHASE 2: GRID-BASED HEADER (Compressed: 100px → 60px) =====
        // Using InvoiceSpacingConfig.HEADER_HEIGHT (60px) from design spec
        val headerY = layoutManager.getHeaderY()
        val headerHeight = InvoiceSpacingConfig.HEADER_HEIGHT
        val headerBottom = headerY + headerHeight

        // LAYER 1: Header background — gradient or solid based on settings
        val headerBackgroundPaint = Paint().apply {
            style = Paint.Style.FILL
            if (snapshot.enableGradientHeader) {
                // ✅ PHASE 3: Gradient header — sweep from primary → headerGradientEndColor
                shader = android.graphics.LinearGradient(
                    layoutManager.getContentLeft(), headerY,
                    layoutManager.getContentRight(), headerBottom,
                    colors.primary,
                    try { Color.parseColor(snapshot.headerGradientEndColor) } catch (e: Exception) { colors.secondary },
                    android.graphics.Shader.TileMode.CLAMP
                )
            } else {
                color = colors.primary
            }
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
            lineTo(595f, headerBottom)   // was headerY + 50f — now uses dynamic height
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
        // ✅ PHASE 3 COMPLEX: Respect enableLogo setting (existing brandingRenderer uses logoBase64 from BusinessProfile)
        if (snapshot.enableLogo) {
            val brandingRenderer = PdfBrandingRenderer(canvas, 595f)
            brandingRenderer.drawLogo(snapshot.logoBase64)
        }

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
        // ✅ QUICK WIN #4: Show "QUOTE" or "INVOICE" based on snapshot.isQuote
        val invoiceOrQuoteLabel = if (snapshot.isQuote) "QUOTE" else "INVOICE"
        val invoiceLabelPaint = Paint().apply {
            typeface = boldTypeface
            textSize = 11f
            color = Color.WHITE  // White on coloured banner — full contrast
            isAntiAlias = true
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText(invoiceOrQuoteLabel, layoutManager.getContentRight() - 10f, headerY + 12f, invoiceLabelPaint)
        canvas.drawText(snapshot.invoiceNumber, layoutManager.getContentRight() - 10f, headerY + 24f, invoiceLabelPaint)

        // ✅ PHASE 3: Status Badge — drawn in header top-right corner when enabled
        // DRAFT is the internal default state — never show it to clients on the PDF
        if (snapshot.enableStatusBadges
            && snapshot.invoiceStatus.isNotBlank()
            && snapshot.invoiceStatus.uppercase() != "DRAFT") {
            val badgeText = snapshot.invoiceStatus.uppercase()
            val badgeColor = when (snapshot.invoiceStatus.uppercase()) {
                "PAID" -> Color.parseColor("#27AE60")
                "OVERDUE" -> Color.parseColor("#E74C3C")
                "SENT" -> Color.parseColor("#2E86DE")
                "DRAFT" -> Color.parseColor("#95A5A6")
                "PARTIALLY_PAID" -> Color.parseColor("#F39C12")
                else -> colors.secondary
            }
            val badgePaint = Paint().apply {
                color = badgeColor
                style = when (snapshot.badgeStyle) {
                    com.emul8r.bizap.domain.model.BadgeStyle.ROUNDED_OUTLINE -> Paint.Style.STROKE
                    else -> Paint.Style.FILL
                }
                isAntiAlias = true
            }
            val badgeTextPaint = Paint().apply {
                typeface = boldTypeface
                textSize = 7f
                color = when (snapshot.badgeStyle) {
                    com.emul8r.bizap.domain.model.BadgeStyle.ROUNDED_OUTLINE -> badgeColor
                    else -> Color.WHITE
                }
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
            }
            val bx = layoutManager.getContentRight() - 48f
            val by = headerY + 38f
            val bw = 40f
            val bh = 13f
            val br = if (snapshot.badgeStyle == com.emul8r.bizap.domain.model.BadgeStyle.CIRCULAR) bh / 2 else 4f
            canvas.drawRoundRect(bx, by, bx + bw, by + bh, br, br, badgePaint)
            canvas.drawText(badgeText, bx + bw / 2, by + bh - 3f, badgeTextPaint)
        }

        // Business info (right-aligned, clean) - Grid-based
        // ✅ FIX: Moved to compact 2-line format to fit within header bounds (60px height)
        val compactBusinessPaint = Paint().apply {
            typeface = regularTypeface
            textSize = 8f  // Smaller font to fit
            color = Color.parseColor("#E8E8E8")
            isAntiAlias = true
            textAlign = Paint.Align.RIGHT
        }
        // ✅ POLISH P2: Only show ABN/phone if they have data (avoid "ABN:  | " empty pipe)
        val abnLine = buildString {
            if (snapshot.businessAbn.isNotBlank()) append("ABN: ${snapshot.businessAbn}")
            if (snapshot.businessAbn.isNotBlank() && snapshot.businessPhone.isNotBlank()) append(" | ")
            if (snapshot.businessPhone.isNotBlank()) append(snapshot.businessPhone)
        }
        if (abnLine.isNotBlank()) {
            canvas.drawText(abnLine, layoutManager.getContentRight() - 10f, headerY + 36f, compactBusinessPaint)
        }
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
        // ✅ PHASE 2 P2.7: Use dynamic shadowIntensity from settings
        val shadowAlpha = (snapshot.shadowIntensity * 255).toInt().coerceIn(0, 255)
        val shadowPaint = Paint().apply {
            color = android.graphics.Color.argb(shadowAlpha, 0, 0, 0)
            style = Paint.Style.FILL
        }

        // ===== BILL TO CARD (Left) =====
        val billToY = layoutManager.getBillToY()
        val billToHeight = InvoiceSpacingConfig.BILL_TO_HEIGHT
        val billToLeft = layoutManager.getBillToLeft()
        val billToRight = layoutManager.getBillToRight()
        val billToBottom = billToY + billToHeight

        // ✅ PHASE 2 P2.6: Use dynamic cornerRadiusDp from settings
        val cornerRadius = snapshot.cornerRadiusDp

        // Draw shadow first (darker layer below)
        canvas.drawRoundRect(
            billToLeft + InvoiceSpacingConfig.SHADOW_OFFSET,
            billToY + InvoiceSpacingConfig.SHADOW_OFFSET,
            billToRight + InvoiceSpacingConfig.SHADOW_OFFSET,
            billToBottom + InvoiceSpacingConfig.SHADOW_OFFSET,
            cornerRadius,
            cornerRadius,
            shadowPaint
        )
        // Draw card background with rounded corners
        canvas.drawRoundRect(billToLeft, billToY, billToRight, billToBottom, cornerRadius, cornerRadius, cardBackgroundPaint)
        // Draw border
        canvas.drawRoundRect(billToLeft, billToY, billToRight, billToBottom, cornerRadius, cornerRadius, cardBorderPaint)

        // Add accent color left-side bar (modern design element) — uses primary for consistency
        val accentBarPaint = Paint().apply {
            color = Color.argb(
                180,
                Color.red(colors.primary),
                Color.green(colors.primary),
                Color.blue(colors.primary)
            )
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
        // ✅ POLISH P1: Show customer email (more useful than business phone for Bill To)
        // Only show if customer has a mobile field; fallback to blank if not available
        // Note: InvoiceSnapshot doesn't have customerPhone field, so we skip the "Mob:" line
        // This keeps the Bill To card clean and customer-focused

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
            cornerRadius,
            cornerRadius,
            shadowPaint
        )
        // Draw card with rounded corners
        canvas.drawRoundRect(invoiceDetailsLeft, invoiceDetailsY, invoiceDetailsRight, invoiceDetailsBottom, cornerRadius, cornerRadius, cardBackgroundPaint)
        // Draw border
        canvas.drawRoundRect(invoiceDetailsLeft, invoiceDetailsY, invoiceDetailsRight, invoiceDetailsBottom, cornerRadius, cornerRadius, cardBorderPaint)

        // Add accent color left-side bar (modern design element)
        canvas.drawRect(invoiceDetailsLeft, invoiceDetailsY, invoiceDetailsLeft + InvoiceSpacingConfig.ACCENT_BAR_WIDTH, invoiceDetailsBottom, accentBarPaint)

        // ✅ QUICK WIN #4: Show "QUOTE" or "INVOICE" label based on isQuote
        canvas.drawText(invoiceOrQuoteLabel, invoiceDetailsLeft + InvoiceSpacingConfig.PADDING_H, invoiceDetailsY + 17f, cardLabelPaint)

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
        // ✅ QUICK WIN #4: Show "Valid until:" for quotes, "Due:" for invoices
        val dueOrValidLabel = if (snapshot.isQuote) "Valid until:" else "Due:"
        canvas.drawText("$dueOrValidLabel ${formatDate(snapshot.dueDate)}", invoiceDetailsLeft + InvoiceSpacingConfig.PADDING_H, invoiceDetailsY + 63f, invoiceDatePaint)

        // ===== WATERMARK (appears on first page) =====
        // ✅ PHASE 3 FEATURE #1: Quote Differentiation - Special watermark for quotes
        val watermarkRenderer = PdfWatermarkRenderer(canvas, 595f, 842f)
        if (snapshot.isQuote) {
            // For quotes, show "QUOTE" watermark in light blue
            watermarkRenderer.drawWatermark("QUOTE", Color.parseColor("#4A90E2"))
        } else {
            // For invoices, show status-based watermark (PAID/OVERDUE/etc)
            watermarkRenderer.drawWatermark(snapshot.invoiceStatus)
        }

        // ===== PHASE 2: Update Y position using grid-based calculation =====
        // Header block bottom: header + gap + bill to = complete top section
        var currentY = layoutManager.getInvoiceHeaderBlockBottom() + InvoiceSpacingConfig.SECTION_GAP
        pageManager.setY(currentY)


        val separatorPaint = Paint().apply { color = colors.secondary; strokeWidth = 0.5f; style = Paint.Style.STROKE }
        val sectionHeaderPaint = Paint().apply { typeface = boldTypeface; textSize = 11f; color = colors.primary; isAntiAlias = true }
        val subheaderBodyPaint = Paint().apply { typeface = italicTypeface; textSize = 10f; color = colors.textLight; isAntiAlias = true }
        val footerBodyPaint = Paint().apply { typeface = italicTypeface; textSize = 9f; color = Color.GRAY; isAntiAlias = true }


        // ===== HEADER AND SUBHEADER TEXT (Optional, appears before line items) =====
        if (snapshot.header.isNotBlank() || snapshot.subheader.isNotBlank()) {
            canvas = pageManager.ensureSpace(50f)

            // ✅ FIX: Header/Subheader rendering with proper spacing to prevent overlap
            if (snapshot.header.isNotBlank()) {
                val headerPaint = Paint().apply {
                    typeface = boldTypeface
                    textSize = 14f  // Prominent header
                    color = colors.primary
                    isAntiAlias = true
                }
                canvas.drawText(snapshot.header, 40f, pageManager.currentY + 12f, headerPaint)
                pageManager.advanceY(20f)  // Increased spacing for large header
            }

            if (snapshot.subheader.isNotBlank()) {
                val subheaderPaint = Paint().apply {
                    typeface = regularTypeface
                    textSize = 11f  // Slightly smaller
                    color = colors.textLight
                    isAntiAlias = true
                }
                canvas.drawText(snapshot.subheader, 40f, pageManager.currentY + 10f, subheaderPaint)
                pageManager.advanceY(16f)  // Good spacing after subheader
            }

            pageManager.advanceY(12f)  // Extra spacing before items table
        }

        if (!hideLineItems) {
            // ===== PHASE 2: ITEMS TABLE WITH GRID-BASED POSITIONING =====
            // Draw table header with professional styling
            val headerPaint = Paint(headerPaint).apply {
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

            // Draw table border top — brand-tinted accent line above the header
            val tableBorderPaint = Paint().apply {
                // Always show a subtle brand-tinted top rule — it's part of the design, not a "grid"
                color = android.graphics.Color.argb(
                    if (visualAccents.showBorders) 180 else 90,
                    Color.red(colors.primary), Color.green(colors.primary), Color.blue(colors.primary))
                strokeWidth = 1.5f
                style = Paint.Style.STROKE
            }
            canvas.drawLine(layoutManager.getItemsTableLeft(), itemsTableY, layoutManager.getItemsTableRight(), itemsTableY, tableBorderPaint)

            val tableRenderer = PdfTableRenderer(
                canvas = canvas,
                startX = layoutManager.getItemsTableLeft(),
                currentY = itemsTableY,
                pageWidth = 595f,
                // ✅ PHASE 3 FEATURE #4: Item Numbering - Added # column (0.05f) at start
                columnWeights = listOf(0.05f, 0.45f, 0.1f, 0.15f, 0.25f),
                headerBackgroundColor = colors.primary,
                // ✅ QUICK WIN #1: Use alternateRowColor from settings (not hardcoded)
                alternateRowColor = if (snapshot.enableAlternatingRowColors) {
                    Color.parseColor(snapshot.alternateRowColor)
                } else {
                    Color.WHITE  // Disable zebra striping if setting is false
                },
                showRowLines = snapshot.enableDividers  // Row lines follow the same toggle as column dividers
            )

            // ✅ PHASE 3 FEATURE #4: Item Numbering - Add # column header
            tableRenderer.drawRow(listOf("#", "Description", "Qty", "Price", "Total"), headerPaint, isHeader = true)
            pageManager.setY(tableRenderer.getPosition())

            // Draw vertical column separators for clarity
            val columnSeparatorPaint = Paint().apply {
                // ✅ QUICK WIN #2: Use dividerColor and dividerThicknessPx from settings
                color = if (snapshot.enableDividers) {
                    Color.parseColor(snapshot.dividerColor)
                } else {
                    Color.TRANSPARENT  // Hide dividers if disabled
                }
                strokeWidth = snapshot.dividerThicknessPx
                style = Paint.Style.STROKE
                // ✅ PHASE 3: Divider styles — DASHED and DOTTED patterns
                when (snapshot.dividerStyle) {
                    com.emul8r.bizap.domain.model.DividerStyle.DASHED ->
                        pathEffect = android.graphics.DashPathEffect(floatArrayOf(8f, 4f), 0f)
                    com.emul8r.bizap.domain.model.DividerStyle.DOTTED ->
                        pathEffect = android.graphics.DashPathEffect(floatArrayOf(2f, 4f), 0f)
                    com.emul8r.bizap.domain.model.DividerStyle.SOLID -> { /* no path effect */ }
                }
            }
            // ✅ FIX: Correct argument order — height = drawn table extent, startY = table top
            if (snapshot.enableDividers) {
                val tableDrawnHeight = tableRenderer.getPosition() - itemsTableY
                tableRenderer.drawColumnSeparators(canvas, tableDrawnHeight, itemsTableY, columnSeparatorPaint)
            }

            // Draw table rows using grid-based row height
            snapshot.items.forEachIndexed { index, item ->
                // Use consistent row height from InvoiceSpacingConfig
                val rowHeight = InvoiceSpacingConfig.TABLE_ROW_HEIGHT

                // Check if we need to start a new page
                canvas = pageManager.ensureSpace(rowHeight)

                // ✅ PHASE 3 FEATURE #3: Discount Rows - Apply different styling for discounts
                val itemPaint = if (item.isDiscount) {
                    Paint().apply {
                        typeface = regularTypeface
                        textSize = 10f
                        color = Color.RED  // Show discounts in red
                        isAntiAlias = true
                    }
                } else {
                    bodyPaint
                }

                // Render row on current canvas
                // For discounts: show negative values and different styling
                val displayQty = if (item.isDiscount) "-" else item.quantity.toInt().toString()
                val displayUnitPrice = if (item.isDiscount) "-" else String.format(Locale.getDefault(), "%s%.2f", symbol, item.unitPrice / 100.0)
                val displayTotal = if (item.isDiscount) {
                    "-${String.format(Locale.getDefault(), "%s%.2f", symbol, item.total / 100.0)}"
                } else {
                    String.format(Locale.getDefault(), "%s%.2f", symbol, item.total / 100.0)
                }

                tableRenderer.drawRow(
                    listOf(
                        (index + 1).toString(),  // ✅ PHASE 3 FEATURE #4: Item number (1-indexed)
                        item.description,
                        displayQty,
                        displayUnitPrice,
                        displayTotal
                    ),
                    itemPaint
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
        // Right-align totals block to right 50% — professional invoice standard
        val totalsLeft = layoutManager.getContentLeft() + layoutManager.getContentWidth() * 0.5f
        val totalsRight = layoutManager.getTotalsRight()

        // Ensure space for totals section
        canvas = pageManager.ensureSpace(totalsHeight + InvoiceSpacingConfig.SECTION_GAP)
        pageManager.setY(totalsY)

        // ===== TYPOGRAPHY-DRIVEN HIERARCHY (No Floating Box) =====

        // ✅ BUG FIX P3: Add thin separator line above subtotal row for visual transition
        val preTotalsSeparatorPaint = Paint().apply {
            // Brand-tinted separator instead of plain grey — feels intentional, not accidental
            color = android.graphics.Color.argb(60,
                Color.red(colors.primary), Color.green(colors.primary), Color.blue(colors.primary))
            strokeWidth = 0.5f
            style = Paint.Style.STROKE
        }
        // ✅ FIX #4: Narrow separator — only spans the totals/amount columns (right-half), not full page width
        // ✅ FIX #5: Increased top padding (was +2f) to +8f for better visual separation from last table row
        canvas.drawLine(totalsLeft + 10f, totalsY + 8f, totalsRight - 10f, totalsY + 8f, preTotalsSeparatorPaint)

        // ✅ BUG FIX #1: Split subtotal label and value into separate paints with correct alignment
        // Label: LEFT-aligned at totalsLeft + 10f
        // Value: RIGHT-aligned at totalsRight - 10f
        val subtotalLabelPaint = Paint().apply {
            typeface = regularTypeface
            textSize = InvoiceSpacingConfig.TEXT_SIZE_BODY
            color = Color.parseColor("#555555")
            textAlign = Paint.Align.LEFT  // ← LEFT-aligned for label
            isAntiAlias = true
        }
        val subtotalValuePaint = Paint().apply {
            typeface = regularTypeface
            textSize = InvoiceSpacingConfig.TEXT_SIZE_BODY
            color = Color.parseColor("#333333")
            textAlign = Paint.Align.RIGHT  // ← RIGHT-aligned for value
            isAntiAlias = true
        }
        val subtotalY = totalsY
        // ✅ ROUND 3: Add tax handling indicator per Australian compliance
        // ✅ PHASE 3 FEATURE #3: Discounts are already subtracted from subtotal (applied before tax)
        val subtotalLabel = if (snapshot.taxHandling == com.emul8r.bizap.domain.model.TaxHandling.EXCLUSIVE) {
            "Subtotal (excl. ${snapshot.taxName}):"
        } else {
            "Subtotal (incl. ${snapshot.taxName}):"
        }
        // ✅ FIX #5: Increased text offset from +12f to +18f to provide breathing room from separator line
        canvas.drawText(subtotalLabel, totalsLeft + 10f, subtotalY + 18f, subtotalLabelPaint)
        canvas.drawText(String.format(Locale.getDefault(), "%s%.2f", symbol, snapshot.subtotal / 100.0), totalsRight - 10f, subtotalY + 18f, subtotalValuePaint)

        // Tax line (if present) — also split label and value
        if (snapshot.taxAmount > 0) {
            val taxLabelPaint = Paint().apply {
                typeface = regularTypeface
                textSize = InvoiceSpacingConfig.TEXT_SIZE_BODY
                color = Color.parseColor("#555555")
                textAlign = Paint.Align.LEFT  // ← LEFT-aligned for label
                isAntiAlias = true
            }
            val taxValuePaint = Paint().apply {
                typeface = regularTypeface
                textSize = InvoiceSpacingConfig.TEXT_SIZE_BODY
                color = Color.parseColor("#333333")
                textAlign = Paint.Align.RIGHT  // ← RIGHT-aligned for value
                isAntiAlias = true
            }
            val taxY = subtotalY + 16f
            // ✅ ROUND 3: Use taxName from snapshot instead of hardcoded "Tax"
            // ✅ FIX #5: Updated text offset from +12f to +16f to match new subtotal spacing
            canvas.drawText(
                "${snapshot.taxName} (${(snapshot.taxRate * 100).toInt()}%):",
                totalsLeft + 10f, taxY + 16f, taxLabelPaint
            )
            canvas.drawText(
                String.format(Locale.getDefault(), "%s%.2f", symbol, snapshot.taxAmount / 100.0),
                totalsRight - 10f, taxY + 16f, taxValuePaint
            )
        }

        // Divider line (visual separation) — subtle gray, respects showDividers toggle
        val dividerPaint = Paint().apply {
            color = if (visualAccents.showDividers) Color.parseColor("#E0E0E0") else Color.TRANSPARENT
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }
        // ✅ FIX #5: Updated divider position from +30f to +36f to account for new subtotal spacing
        val dividerY = totalsY + 36f
        canvas.drawLine(totalsLeft + 10f, dividerY, totalsRight - 10f, dividerY, dividerPaint)

        // ✅ QUICK WIN #1: HIGHLIGHT TOTALS BOX (Phase 2 Implementation)
        // Render background box behind TOTAL DUE if highlightTotals is enabled
        val totalDueY = dividerY + 8f
        if (snapshot.highlightTotals) {
            val highlightBoxPaint = when (snapshot.totalBoxStyle) {
                TotalBoxStyle.SUBTLE_BACKGROUND -> Paint().apply {
                    // Brand-tinted highlight — matches primary, not hardcoded blue
                    color = Color.argb(
                        18,
                        Color.red(colors.primary),
                        Color.green(colors.primary),
                        Color.blue(colors.primary)
                    )
                    style = Paint.Style.FILL
                }
                TotalBoxStyle.PROMINENT_BORDER -> Paint().apply {
                    color = Color.TRANSPARENT
                    style = Paint.Style.STROKE
                    strokeWidth = 3f
                }
                TotalBoxStyle.ACCENT_BORDER -> Paint().apply {
                    color = Color.TRANSPARENT
                    style = Paint.Style.STROKE
                    strokeWidth = 2f
                }
                TotalBoxStyle.BOLD_HIGHLIGHT -> Paint().apply {
                    color = Color.parseColor("#FFE8CC")  // Light orange background (bold)
                    style = Paint.Style.FILL
                }
                TotalBoxStyle.GRADIENT_BACKGROUND -> Paint().apply {
                    color = Color.parseColor("#E8F5E9")  // Light green (simulated gradient)
                    style = Paint.Style.FILL
                }
            }

            // Draw rounded highlight box (was plain rect — now matches card style)
            canvas.drawRoundRect(
                totalsLeft + 5f, totalDueY, totalsRight - 5f, totalDueY + 45f,
                snapshot.cornerRadiusDp, snapshot.cornerRadiusDp,
                highlightBoxPaint
            )

            // Draw accent border if ACCENT_BORDER or PROMINENT_BORDER style
            if (snapshot.totalBoxStyle == TotalBoxStyle.ACCENT_BORDER || snapshot.totalBoxStyle == TotalBoxStyle.PROMINENT_BORDER) {
                val borderPaint = Paint().apply {
                    color = colors.primary
                    strokeWidth = 2f
                    style = Paint.Style.STROKE
                }
                canvas.drawRect(
                    totalsLeft + 5f, totalDueY, totalsRight - 5f, totalDueY + 45f,
                    borderPaint
                )
            }
        }

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

        // ✅ BUG FIX #2: Use the ACTUAL drawn extent (totalDueY + 38f underline + gap) instead of TOTALS_HEIGHT
        // This ensures Payment Details doesn't overlap the totals section
        val actualTotalsBottom = totalDueY + 42f  // 38px underline + 4px buffer
        pageManager.setY(actualTotalsBottom + InvoiceSpacingConfig.SECTION_GAP)

        // ===== SPACING CONSTANTS FOR PROPER LAYOUT =====
        val SECTION_MARGIN_TOP = 10f      // was 24f — saves 14px per section
        val SECTION_HEADER_HEIGHT = 20f   // was 28f — saves 8px per section
        val SECTION_PADDING_TOP = 8f      // was 16f — saves 8px per section
        val SECTION_PADDING_HORIZ = 12f   // was 16f
        val SECTION_PADDING_BOTTOM = 8f   // was 16f — saves 8px per section
        val LINE_HEIGHT = 13f             // was 18f — saves 5px per label-value pair
        val LABEL_VALUE_GAP = 3f          // was 6f
        val ROW_SPACING = 6f              // was 12f — saves 6px between bank detail rows
        // ✅ AESTHETIC UPDATE: Slightly lighter background (#FAFBFC) for Payment Information card
        val CONTENT_BG_COLOR = Color.parseColor("#FAFBFC")  // Slightly lighter than before (#F8F9FA)
        val COLUMN_BOX_BG_COLOR = Color.parseColor("#FFFFFF")  // Pure white for nested column boxes
        val COLUMN_BOX_BORDER_COLOR = Color.parseColor("#E8ECF0")  // Subtle cool-toned border

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
            color = colors.primary
            style = Paint.Style.FILL
        }
        val contentBgPaint = Paint().apply {
            color = CONTENT_BG_COLOR
            style = Paint.Style.FILL
        }
        // ✅ AESTHETIC UPDATE: White nested column box paints (like BILL TO / INVOICE boxes)
        val colBoxBgPaint = Paint().apply {
            color = COLUMN_BOX_BG_COLOR
            style = Paint.Style.FILL
        }
        val colBoxBorderPaint = Paint().apply {
            color = COLUMN_BOX_BORDER_COLOR
            strokeWidth = 0.8f
            style = Paint.Style.STROKE
        }
        // ✅ AESTHETIC UPDATE: Subtle column sub-label paint (uppercase, smaller, muted)
        val colSubLabelPaint = Paint().apply {
            typeface = boldTypeface
            textSize = 9f  // Small uppercase sub-heading inside column box
            color = colors.primary  // ✅ Brand blue (was muted grey #8A96A3)
            isAntiAlias = true
            letterSpacing = 0.08f  // Slight tracking for uppercase
        }
        val fieldLabelPaint = Paint().apply {
            typeface = boldTypeface
            textSize = 11f
            color = Color.parseColor("#333333")
            isAntiAlias = true
        }
        val fieldValuePaint = Paint().apply {
            typeface = regularTypeface
            textSize = 11f  // ✅ Upgraded from 10f → 11f to match body text weight
            color = Color.parseColor("#555555")
            isAntiAlias = true
        }

        // ===== UNIFIED PAYMENT INFORMATION SECTION (Option A: Nested boxes like BILL TO / INVOICE) =====
        val hasPaymentTerms = !snapshot.isQuote && snapshot.paymentTermsDays > 0
        val hasBankDetails = snapshot.bankAccountNumber.isNotBlank() || snapshot.bankBsb.isNotBlank()
            || snapshot.bankName.isNotBlank() || snapshot.bankAccountName.isNotBlank()
        val hasPaymentInfo = hasPaymentTerms || (hasBankDetails && !snapshot.isQuote)

        if (hasPaymentInfo) {
            // Calculate bank detail lines (compact: BSB + Acct on same line)
            val bankLineCount = if (hasBankDetails) {
                var count = 0
                if (snapshot.bankName.isNotBlank()) count++
                if (snapshot.bankAccountName.isNotBlank()) count++
                if (snapshot.bankBsb.isNotBlank() || snapshot.bankAccountNumber.isNotBlank()) count++ // combined line
                count
            } else 0

            // Column box internal constants
            val COL_BOX_PADDING_H = 10f    // Horizontal padding inside each box
            val COL_BOX_PADDING_V = 8f     // Vertical padding inside each box
            val COL_SUB_LABEL_H = 11f      // Sub-label height (e.g. "TERMS", "BANK DETAILS")
            val COL_FIELD_SIZE = 11f        // Field text size

            // Calculate height of each column's content
            val termsContentH = if (hasPaymentTerms) COL_SUB_LABEL_H + 4f + COL_FIELD_SIZE else 0f
            val bankContentH = if (hasBankDetails) COL_SUB_LABEL_H + 4f + (bankLineCount * (COL_FIELD_SIZE + 4f)) else 0f
            val colBoxContentH = maxOf(termsContentH, bankContentH)
            val colBoxHeight = COL_BOX_PADDING_V + colBoxContentH + COL_BOX_PADDING_V

            // Total outer section height: header + padding + column boxes + padding
            val outerPaddingV = 10f   // Outer padding top/bottom within card
            val sectionHeight = SECTION_HEADER_HEIGHT + outerPaddingV + colBoxHeight + outerPaddingV

            canvas = pageManager.ensureSpace(sectionHeight + SECTION_MARGIN_TOP)
            val sectionY = pageManager.currentY + SECTION_MARGIN_TOP

            // ── Outer card background (slightly lighter #FAFBFC) ──
            canvas.drawRoundRect(40f, sectionY, 555f, sectionY + sectionHeight, cornerRadius, cornerRadius, contentBgPaint)

            // ── Navy header bar (rounded top only) ──
            val headerPath = android.graphics.Path().apply {
                addRoundRect(android.graphics.RectF(40f, sectionY, 555f, sectionY + SECTION_HEADER_HEIGHT),
                    floatArrayOf(cornerRadius, cornerRadius, cornerRadius, cornerRadius, 0f, 0f, 0f, 0f),
                    android.graphics.Path.Direction.CW)
            }
            canvas.drawPath(headerPath, sectionHeaderBgPaint)

            // ── Left accent bar (full height) ──
            canvas.drawRect(40f, sectionY, 45f, sectionY + sectionHeight, leftAccentBarPaint)

            // ── Header text ──
            canvas.drawText("PAYMENT INFORMATION", 55f, sectionY + SECTION_HEADER_HEIGHT - 8f, layoutFixSectionHeaderPaint)

            // ── Column box geometry ──
            val colBoxTop = sectionY + SECTION_HEADER_HEIGHT + outerPaddingV
            val colBoxBottom = colBoxTop + colBoxHeight
            val cardLeft = 50f   // Left edge (offset from accent bar)
            val cardRight = 548f // Right edge (inset from outer card)
            val midGap = 12f     // Gap between the two column boxes
            val colMidX = cardLeft + (cardRight - cardLeft) / 2f
            val leftBoxRight = colMidX - midGap / 2f
            val rightBoxLeft = colMidX + midGap / 2f

            // ── LEFT COLUMN BOX: Payment Terms ──
            if (hasPaymentTerms) {
                // Draw white box
                canvas.drawRoundRect(cardLeft, colBoxTop, leftBoxRight, colBoxBottom, cornerRadius, cornerRadius, colBoxBgPaint)
                canvas.drawRoundRect(cardLeft, colBoxTop, leftBoxRight, colBoxBottom, cornerRadius, cornerRadius, colBoxBorderPaint)

                // Sub-label "TERMS" (muted uppercase)
                var boxY = colBoxTop + COL_BOX_PADDING_V + COL_SUB_LABEL_H
                canvas.drawText("TERMS", cardLeft + COL_BOX_PADDING_H, boxY, colSubLabelPaint)

                // Value: "Due in X days"
                boxY += 4f + COL_FIELD_SIZE
                canvas.drawText("Due in ${snapshot.paymentTermsDays} days", cardLeft + COL_BOX_PADDING_H, boxY, fieldValuePaint)
            }

            // ── RIGHT COLUMN BOX: Bank Transfer ──
            if (hasBankDetails) {
                // Draw white box
                canvas.drawRoundRect(rightBoxLeft, colBoxTop, cardRight, colBoxBottom, cornerRadius, cornerRadius, colBoxBgPaint)
                canvas.drawRoundRect(rightBoxLeft, colBoxTop, cardRight, colBoxBottom, cornerRadius, cornerRadius, colBoxBorderPaint)

                val bankFieldPaint = Paint().apply {
                    typeface = regularTypeface
                    textSize = 11f
                    color = Color.parseColor("#555555")
                    isAntiAlias = true
                }

                // Sub-label "BANK DETAILS" (brand blue uppercase)
                var boxY = colBoxTop + COL_BOX_PADDING_V + COL_SUB_LABEL_H
                canvas.drawText("BANK DETAILS", rightBoxLeft + COL_BOX_PADDING_H, boxY, colSubLabelPaint)

                // Alternating row shading paint (matches table zebra style)
                val rowShadePaint = Paint().apply {
                    color = Color.parseColor("#F5F7FA")  // Same subtle tint as table alternate rows
                    style = Paint.Style.FILL
                }

                // Bank fields (inline format) with alternating row shading
                boxY += 4f + COL_FIELD_SIZE
                var rowIndex = 0

                if (snapshot.bankName.isNotBlank()) {
                    // Shaded row (odd)
                    if (rowIndex % 2 == 0) canvas.drawRect(rightBoxLeft + 1f, boxY - COL_FIELD_SIZE, cardRight - 1f, boxY + 4f, rowShadePaint)
                    canvas.drawText("Bank: ${snapshot.bankName}", rightBoxLeft + COL_BOX_PADDING_H, boxY, bankFieldPaint)
                    boxY += COL_FIELD_SIZE + 4f
                    rowIndex++
                }

                if (snapshot.bankAccountName.isNotBlank()) {
                    if (rowIndex % 2 == 0) canvas.drawRect(rightBoxLeft + 1f, boxY - COL_FIELD_SIZE, cardRight - 1f, boxY + 4f, rowShadePaint)
                    canvas.drawText("Account: ${snapshot.bankAccountName}", rightBoxLeft + COL_BOX_PADDING_H, boxY, bankFieldPaint)
                    boxY += COL_FIELD_SIZE + 4f
                    rowIndex++
                }

                // BSB + Account Number on same line (compact)
                if (snapshot.bankBsb.isNotBlank() || snapshot.bankAccountNumber.isNotBlank()) {
                    if (rowIndex % 2 == 0) canvas.drawRect(rightBoxLeft + 1f, boxY - COL_FIELD_SIZE, cardRight - 1f, boxY + 4f, rowShadePaint)
                    val bsbText = if (snapshot.bankBsb.isNotBlank()) "BSB: ${snapshot.bankBsb}" else ""
                    val acctText = if (snapshot.bankAccountNumber.isNotBlank()) "Acct: ${snapshot.bankAccountNumber}" else ""
                    val combined = if (bsbText.isNotEmpty() && acctText.isNotEmpty())
                        "$bsbText  |  $acctText"
                    else
                        bsbText.ifEmpty { acctText }
                    canvas.drawText(combined, rightBoxLeft + COL_BOX_PADDING_H, boxY, bankFieldPaint)
                }
            }

            // ── Single-column fallback: if only one section is present, span full width ──
            // (Handled by box geometry already — one box renders at half width, still fine)

            pageManager.setY(sectionY + sectionHeight)
        }

        pageManager.advanceY(8f)  // Small spacing after sections

        // Render notes and footer below totals
        bodyPaint.textAlign = Paint.Align.LEFT
        if (snapshot.notes.isNotBlank()) {
            canvas = pageManager.ensureSpace(70f)
            pageManager.advanceY(20f)

            val notesBoxPaint = Paint().apply { color = Color.parseColor("#FAFAFA"); style = Paint.Style.FILL }
            val notesBoxBorderPaint = Paint().apply {
                color = if (visualAccents.showBorders) Color.parseColor("#E0E0E0") else Color.TRANSPARENT
                strokeWidth = 1f
                style = Paint.Style.STROKE
            }

            val notesBoxTop = pageManager.currentY
            canvas.drawRoundRect(layoutManager.getContentLeft(), notesBoxTop, layoutManager.getContentRight(), notesBoxTop + 55f, cornerRadius, cornerRadius, notesBoxPaint)
            canvas.drawRoundRect(layoutManager.getContentLeft(), notesBoxTop, layoutManager.getContentRight(), notesBoxTop + 55f, cornerRadius, cornerRadius, notesBoxBorderPaint)

            canvas.drawText("NOTES", layoutManager.getContentLeft() + InvoiceSpacingConfig.PADDING_H, pageManager.currentY + 12f, labelPaint)
            pageManager.advanceY(18f)
            pageManager.setY(drawWrappedText(canvas, snapshot.notes, layoutManager.getContentLeft() + InvoiceSpacingConfig.PADDING_H, pageManager.currentY, layoutManager.getContentWidth() - InvoiceSpacingConfig.PADDING_H * 2, bodyPaint))
        }

        if (snapshot.footerText.isNotBlank()) {
            canvas = pageManager.ensureSpace(70f)
            pageManager.advanceY(20f)

            val footerBoxPaint = Paint().apply { color = Color.parseColor("#F5F5F5"); style = Paint.Style.FILL }
            val footerBoxBorderPaint = Paint().apply {
                color = if (visualAccents.showBorders) Color.parseColor("#E0E0E0") else Color.TRANSPARENT
                strokeWidth = 1f
                style = Paint.Style.STROKE
            }

            val footerBoxTop = pageManager.currentY
            canvas.drawRoundRect(layoutManager.getContentLeft(), footerBoxTop, layoutManager.getContentRight(), footerBoxTop + 55f, cornerRadius, cornerRadius, footerBoxPaint)
            canvas.drawRoundRect(layoutManager.getContentLeft(), footerBoxTop, layoutManager.getContentRight(), footerBoxTop + 55f, cornerRadius, cornerRadius, footerBoxBorderPaint)

            canvas.drawText("FOOTER", layoutManager.getContentLeft() + InvoiceSpacingConfig.PADDING_H, pageManager.currentY + 12f, labelPaint)
            pageManager.advanceY(18f)
            pageManager.setY(drawWrappedText(canvas, snapshot.footerText, layoutManager.getContentLeft() + InvoiceSpacingConfig.PADDING_H, pageManager.currentY, layoutManager.getContentWidth() - InvoiceSpacingConfig.PADDING_H * 2, bodyPaint))
        }

        // ===== PHASE 2: ELEGANT MINIMAL FOOTER (Grid-Based) =====
        // Using GridLayoutManager for footer positioning
        // ✅ BUG FIX #4: Anchor footer to bottom of page instead of floating mid-page
        // Calculate footer Y as a fixed position at the bottom of the page
        val PAGE_HEIGHT_VAL = 842f
        val BOTTOM_MARGIN_VAL = 40f
        val footerHeight = InvoiceSpacingConfig.FOOTER_HEIGHT
        val footerY = PAGE_HEIGHT_VAL - BOTTOM_MARGIN_VAL - footerHeight  // = 762f

        // Only start a new page if content would collide with where footer needs to go
        if (pageManager.currentY > footerY - 20f) {
            canvas = pageManager.startNewPage()
        }

        // ===== CONTENT-CLOSING DIVIDER =====
        // When there's whitespace above the footer, draw a thin branded rule to visually
        // "close" the content area and give the blank space intentional structure.
        val contentCloseY = pageManager.currentY + 12f
        if (contentCloseY < footerY - 30f) {
            val contentClosePaint = Paint().apply {
                color = android.graphics.Color.argb(60,
                    Color.red(colors.primary), Color.green(colors.primary), Color.blue(colors.primary))
                strokeWidth = 0.8f
                style = Paint.Style.STROKE
            }
            canvas.drawLine(layoutManager.getContentLeft(), contentCloseY,
                layoutManager.getContentRight(), contentCloseY, contentClosePaint)
        }

        // Footer background — ✅ FIX #5: reversed gradient (right→left) mirrors the header
        val gradientEnd = try {
            Color.parseColor(snapshot.headerGradientEndColor)
        } catch (e: Exception) { colors.secondary }

        val artFooterBackgroundPaint = Paint().apply {
            style = Paint.Style.FILL
            shader = if (snapshot.enableGradientHeader) {
                // Reversed direction: right (primary) → left (gradientEnd) — mirrors header
                android.graphics.LinearGradient(
                    595f, footerY,
                    0f, footerY,
                    colors.primary,
                    gradientEnd,
                    android.graphics.Shader.TileMode.CLAMP
                )
            } else {
                null.also { color = colors.primary }
            }
        }

        canvas.drawRect(0f, footerY, 595f, footerY + footerHeight, artFooterBackgroundPaint)

        // Diagonal accent on the bottom-left — mirrors the top-right accent in the header
        val footerDiagonalPaint = Paint().apply {
            color = android.graphics.Color.argb(25, 255, 255, 255)
            style = Paint.Style.FILL
        }
        val footerDiagonalPath = android.graphics.Path().apply {
            moveTo(0f, footerY + footerHeight)       // bottom-left
            lineTo(175f, footerY + footerHeight)     // bottom, partway across
            lineTo(125f, footerY)                    // top, inset from left
            lineTo(0f, footerY)                      // top-left
            close()
        }
        canvas.drawPath(footerDiagonalPath, footerDiagonalPaint)

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
        // ✅ ROUND 3: Use companyWebsite from snapshot, fallback to email domain
        val websiteUrl = if (snapshot.companyWebsite.isNotBlank()) {
            snapshot.companyWebsite
        } else {
            val domain = snapshot.businessEmail.substringAfter("@").lowercase()
            "www.$domain"
        }
        val footerContactInfo = "${snapshot.businessEmail} | ${snapshot.businessPhone} | $websiteUrl"
        canvas.drawText(footerContactInfo, 297f, footerY + 25f, artFooterSmallPaint)

        pageManager.advanceY(footerHeight + 3f)

        // ✅ PHASE 3: MOTTO / SLOGAN — rendered below footer message line
        if (snapshot.enableMotto && snapshot.mottoText.isNotBlank()) {
            val mottoY = footerY + 34f
            val mottoPaint = Paint().apply {
                typeface = italicTypeface
                textSize = snapshot.mottoFontSize
                color = try { Color.parseColor(snapshot.mottoColor) } catch (e: Exception) { Color.parseColor("#E8E8E8") }
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("\"${snapshot.mottoText}\"", 297f, mottoY, mottoPaint)
        }

        // ✅ PHASE 3: SIGNATURE AREA — rendered as a line + label above the footer
        if (snapshot.enableSignatureArea) {
            val sigLineLength = snapshot.signatureLineLengthMm * 2.834f  // mm → px
            val sigX = layoutManager.getContentLeft() + 12f
            val sigY = footerY - 22f
            val sigLinePaint = Paint().apply {
                color = Color.parseColor("#333333")
                strokeWidth = 1f
                style = Paint.Style.STROKE
            }
            val sigLabelPaint = Paint().apply {
                typeface = regularTypeface
                textSize = 8f
                color = Color.parseColor("#555555")
                isAntiAlias = true
            }
            canvas.drawLine(sigX, sigY, sigX + sigLineLength, sigY, sigLinePaint)
            canvas.drawText(snapshot.signatureLabel, sigX, sigY + 10f, sigLabelPaint)
        }

        // ✅ PHASE 3 COMPLEX: WATERMARK TEXT — drawn diagonally across page centre
        if (snapshot.enableWatermarkText && snapshot.watermarkText.isNotBlank()) {
            val watermarkAlpha = (snapshot.watermarkOpacity * 255).toInt().coerceIn(10, 200)
            val watermarkPaint = Paint().apply {
                typeface = boldTypeface
                textSize = 60f
                color = android.graphics.Color.argb(watermarkAlpha, 180, 180, 180)
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
            }
            canvas.save()
            canvas.rotate(-45f, 297f, 421f)  // Rotate around page centre
            canvas.drawText(snapshot.watermarkText.uppercase(), 297f, 421f, watermarkPaint)
            canvas.restore()
        }

        // ✅ PHASE 3 COMPLEX: QR CODE — rendered in footer area
        if (snapshot.enableQrCode && snapshot.qrCodeContent.isNotBlank()) {
            val qrRenderer = com.emul8r.bizap.data.service.pdf.PdfQrCodeRenderer(canvas, 595f)
            val qrSizePx = (snapshot.qrCodeSizeMm * 2.834f).toInt()  // mm → px
            val qrX = when (snapshot.qrCodePosition) {
                com.emul8r.bizap.domain.model.QrCodePosition.BOTTOM_RIGHT -> 595f - qrSizePx - 20f
                com.emul8r.bizap.domain.model.QrCodePosition.BOTTOM_LEFT -> 20f
                else -> 595f - qrSizePx - 20f
            }
            val qrY = footerY - qrSizePx - 10f
            try {
                val hints = mapOf(com.google.zxing.EncodeHintType.MARGIN to 1)
                val writer = com.google.zxing.qrcode.QRCodeWriter()
                val bitMatrix = writer.encode(snapshot.qrCodeContent, com.google.zxing.BarcodeFormat.QR_CODE, qrSizePx, qrSizePx, hints)
                val bitmap = android.graphics.Bitmap.createBitmap(qrSizePx, qrSizePx, android.graphics.Bitmap.Config.ARGB_8888)
                for (x in 0 until qrSizePx) {
                    for (y in 0 until qrSizePx) {
                        bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                    }
                }
                canvas.drawBitmap(bitmap, qrX, qrY, null)
                bitmap.recycle()
                Timber.d("✅ QR code rendered at ($qrX, $qrY) size=$qrSizePx")
            } catch (e: Exception) {
                Timber.e(e, "❌ QR code generation failed")
            }
        }

        // ✅ PHASE 3 COMPLEX: PAYMENT ICONS — rendered as emoji row in payment section
        if (snapshot.enablePaymentIcons && snapshot.acceptedPaymentMethodsJson.isNotBlank()) {
            try {
                val methodNames = snapshot.acceptedPaymentMethodsJson
                    .removeSurrounding("[\"", "\"]")
                    .split("\",\"")
                    .filter { it.isNotBlank() }
                var iconX = layoutManager.getContentLeft() + 12f
                val iconPaint = Paint().apply {
                    typeface = boldTypeface
                    textSize = snapshot.paymentIconsSize
                    color = colors.primary
                    isAntiAlias = true
                    textAlign = Paint.Align.LEFT
                }
                methodNames.forEach { methodName ->
                    // Map method name to emoji from PaymentMethod enum
                    val emoji = when {
                        methodName.contains("bank", ignoreCase = true) || methodName.contains("transfer", ignoreCase = true) || methodName.contains("eft", ignoreCase = true) -> "🏦"
                        methodName.contains("credit", ignoreCase = true) || methodName.contains("card", ignoreCase = true) || methodName.contains("visa", ignoreCase = true) || methodName.contains("mastercard", ignoreCase = true) -> "💳"
                        methodName.contains("paypal", ignoreCase = true) -> "💵"
                        methodName.contains("cash", ignoreCase = true) -> "💰"
                        methodName.contains("cheque", ignoreCase = true) || methodName.contains("check", ignoreCase = true) -> "📝"
                        else -> "💳"
                    }
                    canvas.drawText(emoji, iconX, footerY + 12f, iconPaint)
                    iconX += snapshot.paymentIconsSize + 6f
                }
                Timber.d("✅ Payment method icons rendered (${methodNames.size} methods)")
            } catch (e: Exception) {
                Timber.e(e, "⚠️ Payment icons rendering failed (non-blocking)")
            }
        }

        // ✅ PHASE 3 FEATURE #2: Page Numbers - Render on each page
        // Only show page numbers if multi-page document
        if (pageManager.getTotalPages() > 1) {
            // Draw page numbers on each page (need to iterate through pages)
            // This is handled during page rendering - page numbers drawn at footer before finalize
        }

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
     * Draws the brand logo (thswalogo.jpg) as a centred background watermark.
     *
     * - Renders at 75% page size (max ~446×631px on A4) — centred on page
     * - Alpha = 18 (~7% opacity) — subtle, never competes with content
     * - Maintains aspect ratio (contain strategy, not cover)
     * - Soft brand-primary tint for visual cohesion with invoice colours
     * - Silent-fails: watermark is decorative, PDF generation never blocked
     */
    private fun drawBrandWatermark(canvas: Canvas, context: android.content.Context, primaryColor: Int) {
        try {
            val bitmap = android.graphics.BitmapFactory.decodeResource(
                context.resources,
                com.emul8r.bizap.R.drawable.thswalogo
            ) ?: return

            val pageW = 595f
            val pageH = 842f

            // Three-quarter-page max dimensions (contain strategy — preserves aspect, no cropping)
            val maxW = pageW * 0.75f   // ~446px
            val maxH = pageH * 0.75f   // ~631px

            val bitmapAspect = bitmap.width.toFloat() / bitmap.height.toFloat()
            val scaledW: Float
            val scaledH: Float
            if (bitmapAspect > (maxW / maxH)) {
                // Image wider than half-page box — constrain by width
                scaledW = maxW
                scaledH = maxW / bitmapAspect
            } else {
                // Image taller than half-page box — constrain by height
                scaledH = maxH
                scaledW = maxH * bitmapAspect
            }

            // Centre on page
            val left = (pageW - scaledW) / 2f
            val top = (pageH - scaledH) / 2f
            val destRect = android.graphics.RectF(left, top, left + scaledW, top + scaledH)

            val watermarkPaint = Paint().apply {
                alpha = 38  // ~15% opacity — more visible (was 18/~7%)
                isAntiAlias = true
                isFilterBitmap = true
                colorFilter = android.graphics.PorterDuffColorFilter(
                    android.graphics.Color.argb(
                        30,
                        android.graphics.Color.red(primaryColor),
                        android.graphics.Color.green(primaryColor),
                        android.graphics.Color.blue(primaryColor)
                    ),
                    android.graphics.PorterDuff.Mode.SRC_ATOP
                )
            }

            canvas.drawBitmap(bitmap, null, destRect, watermarkPaint)
            bitmap.recycle()

        } catch (e: Exception) {
            timber.log.Timber.w(e, "Brand watermark could not be rendered, continuing without it")
        }
    }

    private fun drawBackgroundPattern(canvas: Canvas, patternType: com.emul8r.bizap.domain.model.BackgroundPattern, opacity: Float) {
        val alpha = (opacity * 255).toInt().coerceIn(0, 255)
        val patternPaint = Paint().apply {
            color = android.graphics.Color.argb(alpha, 200, 200, 200)
            style = Paint.Style.STROKE
            strokeWidth = 1f
            isAntiAlias = true
        }

        val spacing = 40f
        when (patternType) {
            com.emul8r.bizap.domain.model.BackgroundPattern.WAVES -> {
                // Wavy horizontal lines
                var y = 0f
                while (y < 842f) {
                    var x = 0f
                    while (x < 595f) {
                        val nextX = (x + 20f).coerceAtMost(595f)
                        canvas.drawLine(x, y, nextX, y + 10f, patternPaint)
                        x = nextX
                    }
                    y += 30f
                }
            }
            com.emul8r.bizap.domain.model.BackgroundPattern.DOTS -> {
                // Dot grid pattern
                var y = 0f
                while (y < 842f) {
                    var x = 0f
                    while (x < 595f) {
                        canvas.drawCircle(x, y, 1.5f, patternPaint)
                        x += spacing
                    }
                    y += spacing
                }
            }
            com.emul8r.bizap.domain.model.BackgroundPattern.GRID -> {
                // Vertical and horizontal grid lines
                var y = 0f
                while (y < 842f) {
                    canvas.drawLine(0f, y, 595f, y, patternPaint)
                    y += spacing
                }
                var x = 0f
                while (x < 595f) {
                    canvas.drawLine(x, 0f, x, 842f, patternPaint)
                    x += spacing
                }
            }
            com.emul8r.bizap.domain.model.BackgroundPattern.STRIPES -> {
                // Diagonal stripe pattern
                val dashPaint = patternPaint.apply {
                    pathEffect = android.graphics.DashPathEffect(floatArrayOf(10f, 10f), 0f)
                }
                var y = -595f
                while (y < 842f) {
                    canvas.drawLine(0f, y, 595f, y + 595f, dashPaint)
                    y += 30f
                }
            }
            com.emul8r.bizap.domain.model.BackgroundPattern.NONE -> {
                // No pattern
            }
        }
    }

    // ...existing code...
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

    // ✅ PHASE 2 FEATURE #1: LOCALE SUPPORT — Helper data class
    private data class LocaleConfig(
        val locale: Locale,
        val currencySymbol: String,
        val currencyPosition: CurrencyPosition,
        val dateFormat: String,
        val thousandsSeparator: Char,
        val decimalSeparator: Char,
        val decimalPlaces: Int = 2
    )

    // ✅ PHASE 2 FEATURE #1: LOCALE SUPPORT — Get locale configuration
    private fun getLocaleConfig(invoiceLocale: com.emul8r.bizap.domain.model.InvoiceLocale): LocaleConfig {
        return when (invoiceLocale) {
            com.emul8r.bizap.domain.model.InvoiceLocale.AUSTRALIAN -> LocaleConfig(
                locale = Locale("en", "AU"),
                currencySymbol = "$",
                currencyPosition = CurrencyPosition.BEFORE,
                dateFormat = "dd/MM/yyyy",
                thousandsSeparator = ',',
                decimalSeparator = '.'
            )
            com.emul8r.bizap.domain.model.InvoiceLocale.UNITED_STATES -> LocaleConfig(
                locale = Locale.US,
                currencySymbol = "$",
                currencyPosition = CurrencyPosition.BEFORE,
                dateFormat = "MM/dd/yyyy",
                thousandsSeparator = ',',
                decimalSeparator = '.'
            )
            com.emul8r.bizap.domain.model.InvoiceLocale.EUROPEAN -> LocaleConfig(
                locale = Locale("de", "DE"),
                currencySymbol = "€",
                currencyPosition = CurrencyPosition.AFTER,
                dateFormat = "dd/MM/yyyy",
                thousandsSeparator = '.',
                decimalSeparator = ','
            )
            com.emul8r.bizap.domain.model.InvoiceLocale.BRITISH -> LocaleConfig(
                locale = Locale.UK,
                currencySymbol = "£",
                currencyPosition = CurrencyPosition.BEFORE,
                dateFormat = "dd/MM/yyyy",
                thousandsSeparator = ',',
                decimalSeparator = '.'
            )
            com.emul8r.bizap.domain.model.InvoiceLocale.CANADIAN -> LocaleConfig(
                locale = Locale("en", "CA"),
                currencySymbol = "$",
                currencyPosition = CurrencyPosition.BEFORE,
                dateFormat = "yyyy/MM/dd",
                thousandsSeparator = ',',
                decimalSeparator = '.'
            )
            com.emul8r.bizap.domain.model.InvoiceLocale.JAPANESE -> LocaleConfig(
                locale = Locale.JAPAN,
                currencySymbol = "¥",
                currencyPosition = CurrencyPosition.BEFORE,
                dateFormat = "yyyy/MM/dd",
                thousandsSeparator = ',',
                decimalSeparator = '.'
            )
        }
    }

    private fun formatDate(timestamp: Long): String =
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(timestamp))

    // ✅ PHASE 2 FEATURE #4: SPACING PROFILES — Apply spacing multiplier
    private fun getAdjustedSpacing(baseSpacing: Float, spacingProfile: com.emul8r.bizap.domain.model.SpacingProfile): Float {
        val multiplier = InvoiceSpacingConfig.getSpacingMultiplier(spacingProfile)
        return baseSpacing * multiplier
    }

    // ✅ PHASE 2 FEATURE #5: VISUAL ACCENTS — Parse and retrieve visual accent settings
    private fun parseVisualAccents(visualAccentsJson: String): com.emul8r.bizap.domain.model.VisualAccents {
        return try {
            // Parse JSON string to VisualAccents object
            val map = mutableMapOf<String, Boolean>()
            val jsonContent = visualAccentsJson.removeSurrounding("{", "}")
            jsonContent.split(",").forEach { pair ->
                val (key, value) = pair.split(":").let { it[0].trim().removeSurrounding("\"") to it[1].trim().toBoolean() }
                map[key] = value
            }
            com.emul8r.bizap.domain.model.VisualAccents(
                showBorders = map["showBorders"] ?: true,
                showShadows = map["showShadows"] ?: true,
                showDividers = map["showDividers"] ?: true,
                highlightTotals = map["highlightTotals"] ?: true,
                useGradients = map["useGradients"] ?: false
            )
        } catch (e: Exception) {
            Timber.w(e, "Failed to parse visual accents JSON, using defaults")
            com.emul8r.bizap.domain.model.VisualAccents.default()
        }
    }

    private fun generateVersionedFileName(baseFileName: String): String {
        val nameWithoutExt = baseFileName.substringBeforeLast(".pdf")
        var version = 2
        while (true) {
            val name = "${nameWithoutExt}_v${version}.pdf"
            if (!File(context.filesDir, "documents/$name").exists()) return name
            version++
        }
    }

    // ✅ PHASE 3 FEATURE #2: Page Numbers - Helper function
    private fun drawPageNumber(
        canvas: Canvas,
        currentPageNumber: Int,
        totalPages: Int,
        pageWidth: Float = 595f,
        pageHeight: Float = 842f
    ) {
        // Only draw page numbers for multi-page documents
        if (totalPages <= 1) return

        val pageNumberText = "Page $currentPageNumber of $totalPages"
        val pageNumberPaint = Paint().apply {
            textSize = 9f
            color = Color.parseColor("#666666")
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        // Position: 1cm from bottom (28.3px), centered
        val x = pageWidth / 2
        val y = pageHeight - 28.3f
        canvas.drawText(pageNumberText, x, y, pageNumberPaint)
    }

}
