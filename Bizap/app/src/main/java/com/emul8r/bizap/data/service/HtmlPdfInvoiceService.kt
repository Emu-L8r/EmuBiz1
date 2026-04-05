package com.emul8r.bizap.data.service

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.emul8r.bizap.domain.model.InvoiceSnapshot
import com.emul8r.bizap.domain.model.InvoiceSettings
import com.emul8r.bizap.domain.model.HtmlInvoiceStyle
import com.emul8r.bizap.domain.service.PdfGenerationService
import com.emul8r.bizap.data.service.sass.SassStyleEngine
import com.emul8r.bizap.data.service.sass.SassTokens
import com.emul8r.bizap.data.service.PaymentMethodIconProvider
import com.emul8r.bizap.data.service.svg.SvgElementsProvider
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * HTML-to-PDF implementation of PDF generation service.
 *
 * Generates 4 distinct professional invoice templates using only iText7-compatible CSS.
 * All styles use inline style attributes, HTML tables for layout, and solid colors only —
 * no CSS variables, gradients, border-radius, flexbox, or box-shadows.
 */
@RequiresApi(Build.VERSION_CODES.KITKAT)
class HtmlPdfInvoiceService(
    private val context: Context,
    private val settings: InvoiceSettings? = null
) : PdfGenerationService {

    private val dateFormatter = ThreadLocal.withInitial {
        SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    }

    override suspend fun generatePdf(
        snapshot: InvoiceSnapshot,
        isQuote: Boolean,
        overwriteExisting: Boolean,
        theme: com.emul8r.bizap.domain.model.InvoiceTheme?
    ): File {
        Timber.d("📝 HtmlPdfInvoiceService.generatePdf() START")

        if (settings == null) {
            Timber.e("❌ CRITICAL ERROR: Settings object is NULL")
            throw IllegalStateException("HtmlPdfInvoiceService requires settings")
        }

        try {
            val fileType = if (isQuote) "Quote" else "Invoice"
            val baseFileName = DocumentNamingUtils.generateFileName(
                snapshot.customerName, snapshot.date, snapshot.invoiceId.toInt(), fileType
            )

            val existingFile = File(context.filesDir, "documents/$baseFileName")
            if (!overwriteExisting && existingFile.exists()) {
                return existingFile
            }

            val htmlContent = generateHtmlContent(snapshot, isQuote)
            return convertHtmlToPdf(htmlContent, baseFileName)
        } catch (e: Exception) {
            Timber.e(e, "❌ HtmlPdfInvoiceService.generatePdf() FAILED")
            throw e
        }
    }

    // -------------------------------------------------------------------------
    // Data validation — cleans snapshot before rendering
    // -------------------------------------------------------------------------

    companion object {
        /** Regex matching a well-formed email address. */
        private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        /** Common placeholder / keyboard-mash pattern used in test data. */
        private const val GARBAGE_PATTERN = "asdasd"
    }

    /**
     * Validates and cleans invoice data before template rendering.
     * Removes placeholder/garbage data and ensures professional appearance.
     */
    private fun validateAndCleanInvoiceData(snapshot: InvoiceSnapshot): InvoiceSnapshot {
        return snapshot.copy(
            businessName    = validateBusinessName(snapshot.businessName),
            businessAbn     = validateAndFormatAbn(snapshot.businessAbn),
            businessEmail   = validateEmail(snapshot.businessEmail),
            businessPhone   = validatePhone(snapshot.businessPhone),
            customerName    = validateCustomerName(snapshot.customerName),
            customerAddress = validateAddress(snapshot.customerAddress),
            headerText      = validateHeaderText(snapshot.headerText),
            subheaderText   = validateHeaderText(snapshot.subheaderText),
            notes           = validateNotes(snapshot.notes),
            footerText      = validateFooterText(snapshot.footerText)
        )
    }

    private fun validateBusinessName(name: String): String = when {
        name.isEmpty() -> ""
        name.contains("DEFAULT", ignoreCase = true) -> ""
        name.contains(Regex("\\d{3,}")) -> ""   // reject names with consecutive digit sequences
        name.length < 2 -> ""
        else -> name.trim()
    }

    private fun validateAndFormatAbn(abn: String): String {
        val clean = abn.replace(Regex("[^0-9]"), "")
        return when {
            clean.length != 11 -> ""
            clean == "12345678901" || clean.all { it == clean[0] } -> "" // placeholder
            else -> "${clean.take(2)} ${clean.substring(2, 5)} ${clean.substring(5, 8)} ${clean.substring(8)}"
        }
    }

    private fun validateEmail(email: String): String = when {
        email.isEmpty() -> ""
        email.contains(GARBAGE_PATTERN, ignoreCase = true) -> ""
        !email.matches(EMAIL_REGEX) -> ""
        else -> email.trim()
    }

    private fun validatePhone(phone: String): String {
        val phoneRegex = Regex("[0-9\\s\\-+()]{6,}")
        return when {
            phone.isEmpty() -> ""
            phone.length < 6 -> ""
            !phone.matches(phoneRegex) -> ""
            else -> phone.trim()
        }
    }

    private fun validateCustomerName(name: String): String = when {
        name.isEmpty() -> ""
        name.length <= 2 -> ""
        name.matches(Regex("^[a-zA-Z0-9]{1,3}$")) -> ""
        name.contains(GARBAGE_PATTERN, ignoreCase = true) -> ""
        else -> name.trim()
    }

    private fun validateAddress(address: String): String = when {
        address.isEmpty() -> ""
        address.matches(Regex("^[a-zA-Z0-9]{1,5}$")) -> ""
        address.contains(GARBAGE_PATTERN, ignoreCase = true) -> ""
        else -> address.trim()
    }

    private fun validateHeaderText(text: String): String =
        if (text.isEmpty() || text.contains(GARBAGE_PATTERN, ignoreCase = true)) "" else text.trim()

    private fun validateNotes(text: String): String =
        if (text.isEmpty() || text.contains(GARBAGE_PATTERN, ignoreCase = true)) "" else text.trim()

    private fun validateFooterText(text: String): String =
        if (text.isEmpty()) "Thank you for your business." else text.trim()

    // -------------------------------------------------------------------------
    // HTML generation — selects the correct template based on settings
    // -------------------------------------------------------------------------

    /**
     * Public method to generate HTML content for preview purposes.
     * Does not write to disk — returns HTML string only.
     */
    fun buildPreviewHtml(snapshot: InvoiceSnapshot, isQuote: Boolean = false): String {
        return generateHtmlContent(snapshot, isQuote)
    }

    private fun generateHtmlContent(snapshot: InvoiceSnapshot, isQuote: Boolean): String {
        val clean = validateAndCleanInvoiceData(snapshot)
        val layout = settings?.selectedPageLayout
        val style = settings?.selectedHtmlStyle ?: HtmlInvoiceStyle.MODERN

        Timber.d("📐 Generating HTML with style=$style, layout=$layout")

        // New premium styles always use their own self-contained templates.
        // They define a complete layout, so we skip the PageLayout routing entirely.
        if (style == HtmlInvoiceStyle.PREMIUM_PROFESSIONAL || style == HtmlInvoiceStyle.WARM_APPROACHABLE
            || style == HtmlInvoiceStyle.SASS_PROFESSIONAL || style == HtmlInvoiceStyle.REFINED
            || style == HtmlInvoiceStyle.PROFESSIONAL_PLUS) {
            Timber.d("✅ Using premium style-based generation: ${style.name}")
            return when (style) {
                HtmlInvoiceStyle.PREMIUM_PROFESSIONAL -> generatePremiumProfessionalTemplate(clean, isQuote)
                HtmlInvoiceStyle.WARM_APPROACHABLE    -> generateWarmApproachableTemplate(clean, isQuote)
                HtmlInvoiceStyle.SASS_PROFESSIONAL    -> generateSassProfessionalTemplate(clean, isQuote)
                HtmlInvoiceStyle.REFINED              -> generateRefinedTemplate(clean, isQuote)
                HtmlInvoiceStyle.PROFESSIONAL_PLUS    -> generateProfessionalPlusTemplate(clean, isQuote)
                else -> throw IllegalStateException("Unexpected style in premium branch: $style")
            }
        }

        // PHASE 3: Route based on page layout if set (for legacy styles)
        if (layout != null) {
            Timber.d("✅ Using layout-aware generation: ${layout.name}")
            val layoutFactory = com.emul8r.bizap.data.service.layout.PageLayoutFactory
            val manager = com.emul8r.bizap.data.service.layout.PageLayoutManager()
            val colorScheme = manager.extractColorScheme(settings!!)
            val layoutProvider = layoutFactory.createLayout(layout)
            return layoutProvider.buildInvoiceHtml(clean, isQuote, colorScheme)
        }

        // Fall back to style-based generation (legacy mode)
        Timber.d("⚠️  Using style-based generation (legacy mode)")
        return when (style) {
            HtmlInvoiceStyle.MODERN                -> generateModernTemplate(clean, isQuote)
            HtmlInvoiceStyle.MINIMAL               -> generateMinimalTemplate(clean, isQuote)
            HtmlInvoiceStyle.CORPORATE             -> generateCorporateTemplate(clean, isQuote)
            HtmlInvoiceStyle.CREATIVE              -> generateCreativeTemplate(clean, isQuote)
            HtmlInvoiceStyle.PREMIUM_PROFESSIONAL  -> generatePremiumProfessionalTemplate(clean, isQuote)
            HtmlInvoiceStyle.WARM_APPROACHABLE     -> generateWarmApproachableTemplate(clean, isQuote)
            HtmlInvoiceStyle.SASS_PROFESSIONAL     -> generateSassProfessionalTemplate(clean, isQuote)
            HtmlInvoiceStyle.REFINED               -> generateRefinedTemplate(clean, isQuote)
            HtmlInvoiceStyle.PROFESSIONAL_PLUS     -> generateProfessionalPlusTemplate(clean, isQuote)
        }
    }

    // -------------------------------------------------------------------------
    // Shared helpers
    // -------------------------------------------------------------------------

    private fun formatDate(millis: Long): String =
        if (millis > 0) dateFormatter.get()!!.format(Date(millis)) else ""

    private fun formatMoney(cents: Long, currencyCode: String): String {
        val symbol = when (currencyCode.uppercase()) {
            "AUD", "USD", "CAD", "SGD", "NZD", "HKD" -> "$"
            "GBP" -> "£"
            "EUR" -> "€"
            "JPY" -> "¥"
            else  -> "$"
        }
        return "$symbol${String.format("%.2f", cents / 100.0)} $currencyCode"
    }

    private fun formatQty(qty: Double): String =
        if (qty == qty.toLong().toDouble()) qty.toLong().toString()
        else String.format("%.2f", qty)

    private fun escapeHtml(text: String): String = text
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")

    private fun addressLines(address: String): String =
        escapeHtml(address).replace("\n", "<br/>")

    private fun buildItemsRows(snapshot: InvoiceSnapshot, rowBg1: String, rowBg2: String, amountColor: String): String =
        snapshot.items.mapIndexed { i, item ->
            val bg = if (i % 2 == 0) rowBg1 else rowBg2
            val unitDollars = formatMoney(item.unitPrice, snapshot.currencyCode)
            val totalDollars = formatMoney(item.total, snapshot.currencyCode)
            """<tr style="background-color:$bg;">
                <td style="padding:10px 14px;border-bottom:1px solid #e0e0e0;line-height:1.8;word-wrap:break-word;">${escapeHtml(item.description)}</td>
                <td style="padding:10px 14px;border-bottom:1px solid #e0e0e0;text-align:center;line-height:1.8;">${formatQty(item.quantity)}</td>
                <td style="padding:10px 14px;border-bottom:1px solid #e0e0e0;text-align:right;line-height:1.8;">$unitDollars</td>
                <td style="padding:10px 14px;border-bottom:1px solid #e0e0e0;text-align:right;font-weight:bold;color:$amountColor;line-height:1.8;">$totalDollars</td>
            </tr>"""
        }.joinToString("\n")

    private fun buildTotalsRows(snapshot: InvoiceSnapshot, labelColor: String, totalBg: String, totalFg: String): String {
        val taxPct = snapshot.taxRate * 100
        val taxLabel = if (snapshot.taxRate > 0) {
            val formatted = if (taxPct == taxPct.toLong().toDouble())
                "${taxPct.toLong()}%"
            else
                "${String.format("%.2f", taxPct).trimEnd('0').trimEnd('.')}%"
            "Tax ($formatted)"
        } else "Tax"
        return """
            <tr>
                <td colspan="3" style="padding:9px 14px;text-align:right;color:$labelColor;">Subtotal</td>
                <td style="padding:9px 14px;text-align:right;">${formatMoney(snapshot.subtotal, snapshot.currencyCode)}</td>
            </tr>
            <tr>
                <td colspan="3" style="padding:9px 14px;text-align:right;color:$labelColor;">$taxLabel</td>
                <td style="padding:9px 14px;text-align:right;">${formatMoney(snapshot.taxAmount, snapshot.currencyCode)}</td>
            </tr>
            <tr style="background-color:$totalBg;">
                <td colspan="3" style="padding:12px 14px;text-align:right;font-weight:bold;font-size:13pt;color:$totalFg;">TOTAL DUE</td>
                <td style="padding:12px 14px;text-align:right;font-weight:bold;font-size:13pt;color:$totalFg;">${formatMoney(snapshot.totalAmount, snapshot.currencyCode)}</td>
            </tr>
        """.trimIndent()
    }

    private fun buildPaymentSection(snapshot: InvoiceSnapshot, headingColor: String, borderColor: String): String {
        val hasBank = snapshot.bankName.isNotBlank() || snapshot.bankAccountName.isNotBlank()
            || snapshot.bankAccountNumber.isNotBlank() || snapshot.bankBsb.isNotBlank()
        if (!hasBank) return ""
        return """
            <table width="100%" style="border-collapse:collapse;margin-top:20px;">
                <tr><td colspan="2" style="padding:14px 16px;background-color:#f5f5f5;font-weight:bold;font-size:11pt;color:$headingColor;border-left:4px solid $borderColor;letter-spacing:0.5px;text-transform:uppercase;">PAYMENT DETAILS</td></tr>
                ${if (snapshot.bankName.isNotBlank()) """<tr style="height:16px;"><td style="padding:14px 16px;font-weight:bold;width:40%;line-height:1.8;color:#333333;">Bank</td><td style="padding:14px 16px;line-height:1.8;color:#555555;">${escapeHtml(snapshot.bankName)}</td></tr>""" else ""}
                ${if (snapshot.bankAccountName.isNotBlank()) """<tr style="height:16px;"><td style="padding:14px 16px;font-weight:bold;line-height:1.8;color:#333333;">Account Name</td><td style="padding:14px 16px;line-height:1.8;color:#555555;">${escapeHtml(snapshot.bankAccountName)}</td></tr>""" else ""}
                ${if (snapshot.bankAccountNumber.isNotBlank()) """<tr style="height:16px;"><td style="padding:14px 16px;font-weight:bold;line-height:1.8;color:#333333;">Account Number</td><td style="padding:14px 16px;line-height:1.8;color:#555555;">${escapeHtml(snapshot.bankAccountNumber)}</td></tr>""" else ""}
                ${if (snapshot.bankBsb.isNotBlank()) """<tr style="height:16px;"><td style="padding:14px 16px;font-weight:bold;line-height:1.8;color:#333333;">BSB</td><td style="padding:14px 16px;line-height:1.8;color:#555555;">${escapeHtml(snapshot.bankBsb)}</td></tr>""" else ""}
            </table>
        """.trimIndent()
    }

    private fun buildNotesSection(snapshot: InvoiceSnapshot, headingColor: String, borderColor: String): String {
        if (snapshot.notes.isBlank()) return ""
        return """
            <table width="100%" style="border-collapse:collapse;margin-top:20px;">
                <tr><td style="padding:10px 14px;background-color:#f5f5f5;font-weight:bold;font-size:11pt;color:$headingColor;border-left:4px solid $borderColor;">NOTES</td></tr>
                <tr><td style="padding:10px 14px;font-size:10pt;line-height:1.8;word-wrap:break-word;">${escapeHtml(snapshot.notes)}</td></tr>
            </table>
        """.trimIndent()
    }

    private fun buildStatusWatermark(status: String): String {
        if (status.isBlank() || status == "SENT") return ""
        val color = when (status.uppercase()) {
            "PAID"    -> "#27ae60"
            "DRAFT"   -> "#888888"
            "OVERDUE" -> "#e74c3c"
            "CANCELLED" -> "#999999"
            else      -> "#888888"
        }
        return """<div style="text-align:center;color:$color;font-size:28pt;font-weight:bold;letter-spacing:6px;opacity:0.18;margin:0 0 4px 0;border:3px solid $color;padding:4px 16px;display:inline-block;">${escapeHtml(status.uppercase())}</div>"""
    }

    /**
     * Builds a signature / authorization section for premium templates.
     *
     * Renders two dashed signature lines: "Authorised Signature" on the left
     * and "Date" on the right.  Visibility is controlled by [showSignatureField].
     *
     * @param accentColor  Hex color for the dashed lines and border-top separator
     * @param textMuted    Hex color for the label text below each line
     * @param showSignatureField Whether to render the section at all
     */
    private fun buildSignatureSection(
        accentColor: String,
        textMuted: String,
        showSignatureField: Boolean
    ): String {
        if (!showSignatureField) return ""
        return """
<table width="100%" style="border-collapse:collapse;margin-top:24px;border-top:1px solid $accentColor;">
<tr>
  <td width="45%" style="padding:16px 0 0 0;vertical-align:top;">
    <div style="border-bottom:2px dashed $accentColor;width:85%;height:28px;margin-bottom:4px;"></div>
    <div style="font-size:8.5pt;color:$textMuted;">Authorised Signature</div>
  </td>
  <td width="10%"></td>
  <td width="45%" style="padding:16px 0 0 0;vertical-align:top;text-align:right;">
    <div style="border-bottom:2px dashed $accentColor;width:85%;height:28px;margin-bottom:4px;margin-left:auto;"></div>
    <div style="font-size:8.5pt;color:$textMuted;">Date</div>
  </td>
</tr>
</table>""".trimIndent()
    }

    /**
     * Builds an enhanced payment details section with payment method icon badges.
     *
     * Enhances the standard payment display with a colorized method badge when a
     * payment method name is present in the snapshot, while remaining fully
     * iText7-compatible (table layout, no flexbox).
     */
    private fun buildEnhancedPaymentSection(
        snapshot: InvoiceSnapshot,
        headingColor: String,
        borderColor: String
    ): String {
        val hasBank = snapshot.bankName.isNotBlank() || snapshot.bankAccountName.isNotBlank()
            || snapshot.bankAccountNumber.isNotBlank() || snapshot.bankBsb.isNotBlank()
        if (!hasBank) return ""

        val paymentMethodBadge = PaymentMethodIconProvider.buildPaymentMethodBadge(
            methodName = snapshot.bankName.ifBlank { "Bank Transfer" },
            accentColor = headingColor
        )

        return """
<table width="100%" style="border-collapse:collapse;margin-top:20px;">
  <tr>
    <td colspan="2" style="padding:10px 14px;background-color:#f5f5f5;font-weight:bold;font-size:11pt;color:$headingColor;border-left:4px solid $borderColor;letter-spacing:0.5px;">
      PAYMENT DETAILS&nbsp;&nbsp;$paymentMethodBadge
    </td>
  </tr>
  ${if (snapshot.bankName.isNotBlank()) """<tr><td style="padding:10px 14px;font-weight:bold;width:40%;line-height:1.6;color:#333333;">Bank</td><td style="padding:10px 14px;line-height:1.6;color:#555555;">${escapeHtml(snapshot.bankName)}</td></tr>""" else ""}
  ${if (snapshot.bankAccountName.isNotBlank()) """<tr style="background-color:#f9f9f9;"><td style="padding:10px 14px;font-weight:bold;line-height:1.6;color:#333333;">Account Name</td><td style="padding:10px 14px;line-height:1.6;color:#555555;">${escapeHtml(snapshot.bankAccountName)}</td></tr>""" else ""}
  ${if (snapshot.bankAccountNumber.isNotBlank()) """<tr><td style="padding:10px 14px;font-weight:bold;line-height:1.6;color:#333333;">Account Number</td><td style="padding:10px 14px;line-height:1.6;color:#555555;">${escapeHtml(snapshot.bankAccountNumber)}</td></tr>""" else ""}
  ${if (snapshot.bankBsb.isNotBlank()) """<tr style="background-color:#f9f9f9;"><td style="padding:10px 14px;font-weight:bold;line-height:1.6;color:#333333;">BSB</td><td style="padding:10px 14px;line-height:1.6;color:#555555;">${escapeHtml(snapshot.bankBsb)}</td></tr>""" else ""}
</table>""".trimIndent()
    }

    /**
     * Builds a professional multi-section footer with contact info and trust elements.
     *
     * Renders a 3-column layout (company info | thank-you message | payment info)
     * using iText7-compatible table layout.
     */
    private fun buildProfessionalFooter(
        snapshot: InvoiceSnapshot,
        primaryColor: String,
        accentColor: String,
        bgColor: String
    ): String {
        val footerText = snapshot.footerText.ifBlank { "Thank you for your business!" }
        return """
<table width="100%" style="margin-top:18px;background-color:$bgColor;border-top:3px solid $accentColor;border-collapse:collapse;">
<tr>
  <td width="35%" style="padding:12px 16px;vertical-align:top;">
    ${if (snapshot.businessEmail.isNotBlank()) """<div style="font-size:8.5pt;color:$accentColor;font-weight:700;">${escapeHtml(snapshot.businessEmail)}</div>""" else ""}
    ${if (snapshot.businessPhone.isNotBlank()) """<div style="font-size:8.5pt;color:#6B7280;margin-top:2px;">${escapeHtml(snapshot.businessPhone)}</div>""" else ""}
  </td>
  <td width="30%" style="padding:12px 16px;text-align:center;vertical-align:middle;">
    <div style="font-size:9pt;color:$primaryColor;font-weight:700;line-height:1.5;">${escapeHtml(footerText)}</div>
  </td>
  <td width="35%" style="padding:12px 16px;text-align:right;vertical-align:top;">
    ${if (snapshot.bankName.isNotBlank()) """<div style="font-size:8pt;color:#6B7280;">${PaymentMethodIconProvider.iconFor(snapshot.bankName)} ${escapeHtml(snapshot.bankName)}</div>""" else ""}
    ${if (snapshot.businessAbn.isNotBlank()) """<div style="font-size:7.5pt;color:#9CA3AF;margin-top:2px;">ABN ${escapeHtml(snapshot.businessAbn)}</div>""" else ""}
  </td>
</tr>
</table>""".trimIndent()
    }

    // -------------------------------------------------------------------------
    // Template 1: MODERN (Purple #6B4C9A)
    // -------------------------------------------------------------------------

    private fun generateModernTemplate(snapshot: InvoiceSnapshot, isQuote: Boolean): String {
        val primary = "#6B4C9A"
        val lightPurple = "#ece7f6"
        val docType = if (isQuote) "QUOTE" else "INVOICE"
        val itemRows = buildItemsRows(snapshot, "#ffffff", "#f5f0fc", primary)
        val totalRows = buildTotalsRows(snapshot, "#555555", primary, "#ffffff")
        val paymentSection = buildPaymentSection(snapshot, primary, primary)
        val notesSection = buildNotesSection(snapshot, primary, primary)
        val watermark = buildStatusWatermark(snapshot.invoiceStatus)
        val logoHtml = if (!snapshot.logoBase64.isNullOrBlank())
            """<img src="data:image/png;base64,${snapshot.logoBase64}" style="max-height:60px;max-width:120px;" alt="logo"/>"""
        else ""

        return """<!DOCTYPE html>
<html><head><meta charset="UTF-8"/>
<style>
@page { margin: 15mm 15mm 15mm 15mm; }
body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; font-size: 10pt; color: #333333; margin: 0; padding: 0; line-height: 1.8; }
table { border-collapse: collapse; }
td, th { word-wrap: break-word; }
</style>
</head><body>

<!-- HEADER -->
<table width="100%" style="background-color:$primary;color:#ffffff;margin-bottom:20px;">
<tr>
  <td style="padding:24px 24px;vertical-align:top;">
    $logoHtml
    ${if (snapshot.businessName.isNotBlank()) """<div style="font-size:20pt;font-weight:bold;color:#ffffff;margin-top:6px;line-height:1.3;">${escapeHtml(snapshot.businessName)}</div>""" else ""}
    ${if (snapshot.subheaderText.isNotBlank()) """<div style="font-size:13pt;font-weight:500;color:#d4c5e8;margin-top:6px;line-height:1.4;">${escapeHtml(snapshot.subheaderText)}</div>""" else ""}
    <div style="margin-top:10px;">
    ${if (snapshot.businessAddress.isNotBlank()) """<div style="font-size:9pt;color:#e0d8f0;margin-top:4px;line-height:1.8;">${addressLines(snapshot.businessAddress)}</div>""" else ""}
    ${if (snapshot.businessEmail.isNotBlank()) """<div style="font-size:9pt;color:#e0d8f0;margin-top:4px;">${escapeHtml(snapshot.businessEmail)}</div>""" else ""}
    ${if (snapshot.businessPhone.isNotBlank()) """<div style="font-size:9pt;color:#e0d8f0;margin-top:4px;">${escapeHtml(snapshot.businessPhone)}</div>""" else ""}
    ${if (snapshot.businessAbn.isNotBlank()) """<div style="font-size:9pt;color:#e0d8f0;margin-top:4px;">ABN: ${escapeHtml(snapshot.businessAbn)}</div>""" else ""}
    </div>
  </td>
  <td style="padding:24px 24px;text-align:right;vertical-align:top;">
    <div style="font-size:22pt;font-weight:bold;letter-spacing:3px;color:#ffffff;">$docType</div>
    $watermark
  </td>
</tr>
</table>

<!-- INVOICE META + BILL TO -->
<table width="100%" style="margin-bottom:20px;">
<tr>
  <td width="50%" style="vertical-align:top;padding-right:12px;">
    <table width="100%" style="background-color:$lightPurple;border-left:4px solid $primary;">
      <tr><td colspan="2" style="padding:10px 14px;font-weight:bold;font-size:11pt;color:$primary;">INVOICE DETAILS</td></tr>
      <tr><td style="padding:8px 14px;font-weight:bold;width:45%;line-height:1.8;">Invoice #</td><td style="padding:8px 14px;line-height:1.8;">${escapeHtml(snapshot.invoiceNumber)}</td></tr>
      <tr><td style="padding:8px 14px;font-weight:bold;line-height:1.8;">Date</td><td style="padding:8px 14px;line-height:1.8;">${formatDate(snapshot.date)}</td></tr>
      <tr><td style="padding:8px 14px;font-weight:bold;line-height:1.8;">Due Date</td><td style="padding:8px 14px;line-height:1.8;">${formatDate(snapshot.dueDate)}</td></tr>
      <tr><td style="padding:8px 14px;font-weight:bold;line-height:1.8;">Currency</td><td style="padding:8px 14px;line-height:1.8;">${escapeHtml(snapshot.currencyCode)}</td></tr>
      ${if (snapshot.invoiceStatus.isNotBlank()) """<tr><td style="padding:8px 14px;font-weight:bold;line-height:1.8;">Status</td><td style="padding:8px 14px;line-height:1.8;">${escapeHtml(snapshot.invoiceStatus)}</td></tr>""" else ""}
    </table>
  </td>
  <td width="50%" style="vertical-align:top;padding-left:12px;">
    <table width="100%" style="background-color:#fafafa;border-left:4px solid #aaaaaa;">
      <tr><td style="padding:10px 14px;font-weight:bold;font-size:11pt;color:$primary;">BILL TO</td></tr>
      ${if (snapshot.customerName.isNotBlank()) """<tr><td style="padding:8px 14px;font-weight:bold;font-size:11pt;line-height:1.8;">${escapeHtml(snapshot.customerName)}</td></tr>""" else ""}
      ${if (snapshot.customerAddress.isNotBlank()) """<tr><td style="padding:8px 14px;font-size:9pt;line-height:1.8;">${addressLines(snapshot.customerAddress)}</td></tr>""" else ""}
      ${if (!snapshot.customerEmail.isNullOrBlank()) """<tr><td style="padding:8px 14px;font-size:9pt;line-height:1.8;">${escapeHtml(snapshot.customerEmail)}</td></tr>""" else ""}
    </table>
  </td>
</tr>
</table>

${if (snapshot.headerText.isNotBlank()) """<p style="margin-bottom:16px;font-style:italic;color:#555555;line-height:1.8;">${escapeHtml(snapshot.headerText)}</p>""" else ""}

<!-- LINE ITEMS TABLE -->
<table width="100%" style="border-collapse:collapse;margin-bottom:4px;">
  <tr style="background-color:$primary;color:#ffffff;">
    <th style="padding:12px 14px;text-align:left;font-size:10pt;">Description</th>
    <th style="padding:12px 14px;text-align:center;font-size:10pt;width:10%;">Qty</th>
    <th style="padding:12px 14px;text-align:right;font-size:10pt;width:18%;">Unit Price</th>
    <th style="padding:12px 14px;text-align:right;font-size:10pt;width:18%;">Total</th>
  </tr>
  $itemRows
  <tr><td colspan="4" style="padding:0;"></td></tr>
  $totalRows
</table>

$paymentSection
$notesSection

${if (snapshot.footerText.isNotBlank()) """<p style="margin-top:20px;text-align:center;font-size:9pt;color:#888888;border-top:1px solid #dddddd;padding-top:12px;line-height:1.8;">${escapeHtml(snapshot.footerText)}</p>""" else ""}

</body></html>"""
    }

    // -------------------------------------------------------------------------
    // Template 2: MINIMAL (Near-Black #1a1a1a)
    // -------------------------------------------------------------------------

    private fun generateMinimalTemplate(snapshot: InvoiceSnapshot, isQuote: Boolean): String {
        val primary = "#1a1a1a"
        val docType = if (isQuote) "QUOTE" else "INVOICE"
        val itemRows = buildItemsRows(snapshot, "#ffffff", "#f9f9f9", "#1a1a1a")
        val totalRows = buildTotalsRows(snapshot, "#555555", "#1a1a1a", "#ffffff")
        val paymentSection = buildPaymentSection(snapshot, "#1a1a1a", "#1a1a1a")
        val notesSection = buildNotesSection(snapshot, "#1a1a1a", "#aaaaaa")
        val watermark = buildStatusWatermark(snapshot.invoiceStatus)
        val logoHtml = if (!snapshot.logoBase64.isNullOrBlank())
            """<img src="data:image/png;base64,${snapshot.logoBase64}" style="max-height:55px;max-width:110px;" alt="logo"/>"""
        else ""

        return """<!DOCTYPE html>
<html><head><meta charset="UTF-8"/>
<style>
@page { margin: 15mm 15mm 15mm 15mm; }
body { font-family: Arial, Helvetica, sans-serif; font-size: 10pt; color: #1a1a1a; margin: 0; padding: 0; line-height: 1.8; }
table { border-collapse: collapse; }
td, th { word-wrap: break-word; }
</style>
</head><body>

<!-- HEADER -->
<table width="100%" style="border-bottom:2px solid #1a1a1a;margin-bottom:20px;">
<tr>
  <td style="padding:0 0 14px 0;vertical-align:top;">
    $logoHtml
    ${if (snapshot.businessName.isNotBlank()) """<div style="font-size:19pt;font-weight:bold;color:#1a1a1a;margin-top:6px;line-height:1.3;">${escapeHtml(snapshot.businessName)}</div>""" else ""}
    ${if (snapshot.subheaderText.isNotBlank()) """<div style="font-size:13pt;font-weight:500;color:#666666;margin-top:6px;line-height:1.4;">${escapeHtml(snapshot.subheaderText)}</div>""" else ""}
    <div style="margin-top:8px;">
    ${if (snapshot.businessAddress.isNotBlank()) """<div style="font-size:9pt;color:#555555;margin-top:4px;line-height:1.8;">${addressLines(snapshot.businessAddress)}</div>""" else ""}
    ${if (snapshot.businessEmail.isNotBlank()) """<div style="font-size:9pt;color:#555555;margin-top:4px;">${escapeHtml(snapshot.businessEmail)}</div>""" else ""}
    ${if (snapshot.businessPhone.isNotBlank()) """<div style="font-size:9pt;color:#555555;margin-top:4px;">${escapeHtml(snapshot.businessPhone)}</div>""" else ""}
    ${if (snapshot.businessAbn.isNotBlank()) """<div style="font-size:9pt;color:#555555;margin-top:4px;">ABN: ${escapeHtml(snapshot.businessAbn)}</div>""" else ""}
    </div>
  </td>
  <td style="text-align:right;vertical-align:top;padding-bottom:14px;">
    <div style="font-size:24pt;font-weight:bold;letter-spacing:2px;color:#1a1a1a;">$docType</div>
    $watermark
  </td>
</tr>
</table>

<!-- INVOICE META + BILL TO -->
<table width="100%" style="margin-bottom:20px;">
<tr>
  <td width="50%" style="vertical-align:top;padding-right:16px;">
    <div style="font-weight:bold;font-size:10pt;margin-bottom:10px;text-transform:uppercase;letter-spacing:1px;border-bottom:1px solid #1a1a1a;padding-bottom:5px;">Invoice Details</div>
    <table width="100%">
      <tr><td style="padding:7px 0;font-weight:bold;width:45%;line-height:1.8;">Invoice #</td><td style="padding:7px 0;line-height:1.8;">${escapeHtml(snapshot.invoiceNumber)}</td></tr>
      <tr><td style="padding:7px 0;font-weight:bold;line-height:1.8;">Date</td><td style="padding:7px 0;line-height:1.8;">${formatDate(snapshot.date)}</td></tr>
      <tr><td style="padding:7px 0;font-weight:bold;line-height:1.8;">Due Date</td><td style="padding:7px 0;line-height:1.8;">${formatDate(snapshot.dueDate)}</td></tr>
      <tr><td style="padding:7px 0;font-weight:bold;line-height:1.8;">Currency</td><td style="padding:7px 0;line-height:1.8;">${escapeHtml(snapshot.currencyCode)}</td></tr>
      ${if (snapshot.invoiceStatus.isNotBlank()) """<tr><td style="padding:7px 0;font-weight:bold;line-height:1.8;">Status</td><td style="padding:7px 0;line-height:1.8;">${escapeHtml(snapshot.invoiceStatus)}</td></tr>""" else ""}
    </table>
  </td>
  <td width="50%" style="vertical-align:top;padding-left:16px;">
    <div style="font-weight:bold;font-size:10pt;margin-bottom:10px;text-transform:uppercase;letter-spacing:1px;border-bottom:1px solid #1a1a1a;padding-bottom:5px;">Bill To</div>
    ${if (snapshot.customerName.isNotBlank()) """<div style="font-weight:bold;font-size:11pt;line-height:1.8;">${escapeHtml(snapshot.customerName)}</div>""" else ""}
    ${if (snapshot.customerAddress.isNotBlank()) """<div style="font-size:9pt;color:#555555;margin-top:6px;line-height:1.8;">${addressLines(snapshot.customerAddress)}</div>""" else ""}
    ${if (!snapshot.customerEmail.isNullOrBlank()) """<div style="font-size:9pt;color:#555555;margin-top:4px;line-height:1.8;">${escapeHtml(snapshot.customerEmail)}</div>""" else ""}
  </td>
</tr>
</table>

${if (snapshot.headerText.isNotBlank()) """<p style="margin-bottom:16px;font-size:9pt;color:#555555;font-style:italic;line-height:1.8;">${escapeHtml(snapshot.headerText)}</p>""" else ""}

<!-- LINE ITEMS TABLE -->
<table width="100%" style="margin-bottom:4px;">
  <tr style="background-color:#1a1a1a;color:#ffffff;">
    <th style="padding:12px 14px;text-align:left;">Description</th>
    <th style="padding:12px 14px;text-align:center;width:10%;">Qty</th>
    <th style="padding:12px 14px;text-align:right;width:18%;">Unit Price</th>
    <th style="padding:12px 14px;text-align:right;width:18%;">Total</th>
  </tr>
  $itemRows
  <tr><td colspan="4" style="padding:0;"></td></tr>
  $totalRows
</table>

$paymentSection
$notesSection

${if (snapshot.footerText.isNotBlank()) """<p style="margin-top:20px;text-align:center;font-size:9pt;color:#888888;border-top:1px solid #cccccc;padding-top:12px;line-height:1.8;">${escapeHtml(snapshot.footerText)}</p>""" else ""}

</body></html>"""
    }

    // -------------------------------------------------------------------------
    // Template 3: CORPORATE (Navy #003366)
    // -------------------------------------------------------------------------

    private fun generateCorporateTemplate(snapshot: InvoiceSnapshot, isQuote: Boolean): String {
        val primary = "#003366"
        val offWhite = "#F5F5F5"
        val docType = if (isQuote) "QUOTE" else "INVOICE"
        val itemRows = buildItemsRows(snapshot, "#ffffff", offWhite, primary)
        val totalRows = buildTotalsRows(snapshot, "#003366", primary, "#ffffff")
        val paymentSection = buildPaymentSection(snapshot, primary, primary)
        val notesSection = buildNotesSection(snapshot, primary, "#003366")
        val watermark = buildStatusWatermark(snapshot.invoiceStatus)
        val logoHtml = if (!snapshot.logoBase64.isNullOrBlank())
            """<img src="data:image/png;base64,${snapshot.logoBase64}" style="max-height:60px;max-width:120px;" alt="logo"/>"""
        else ""

        return """<!DOCTYPE html>
<html><head><meta charset="UTF-8"/>
<style>
@page { margin: 15mm 15mm 15mm 15mm; }
body { font-family: Georgia, 'Times New Roman', Times, serif; font-size: 10pt; color: #222222; margin: 0; padding: 0; line-height: 1.8; }
table { border-collapse: collapse; }
td, th { word-wrap: break-word; }
</style>
</head><body>

<!-- HEADER BAR -->
<table width="100%" style="background-color:$primary;margin-bottom:0;">
<tr>
  <td style="padding:22px 24px;vertical-align:middle;">
    $logoHtml
    ${if (snapshot.businessName.isNotBlank()) """<div style="font-size:19pt;font-weight:bold;color:#ffffff;margin-top:4px;line-height:1.3;">${escapeHtml(snapshot.businessName)}</div>""" else ""}
    ${if (snapshot.subheaderText.isNotBlank()) """<div style="font-size:13pt;font-weight:500;color:#c5d9ed;margin-top:6px;line-height:1.4;">${escapeHtml(snapshot.subheaderText)}</div>""" else ""}
  </td>
  <td style="padding:22px 24px;text-align:right;vertical-align:middle;">
    <div style="font-size:20pt;font-weight:bold;letter-spacing:4px;color:#ffffff;">$docType</div>
    $watermark
  </td>
</tr>
</table>

<!-- SUBHEADER BAR -->
<table width="100%" style="background-color:$offWhite;border-bottom:2px solid $primary;margin-bottom:20px;">
<tr>
  <td style="padding:12px 24px;font-size:9pt;color:#555555;vertical-align:top;line-height:1.8;">
    ${if (snapshot.businessAddress.isNotBlank()) """${addressLines(snapshot.businessAddress)}""" else ""}
    ${if (snapshot.businessEmail.isNotBlank()) """${if (snapshot.businessAddress.isNotBlank()) " &nbsp;|&nbsp; " else ""}${escapeHtml(snapshot.businessEmail)}""" else ""}
    ${if (snapshot.businessPhone.isNotBlank()) """ &nbsp;|&nbsp; ${escapeHtml(snapshot.businessPhone)}""" else ""}
    ${if (snapshot.businessAbn.isNotBlank()) """ &nbsp;|&nbsp; ABN: ${escapeHtml(snapshot.businessAbn)}""" else ""}
  </td>
</tr>
</table>

<!-- INVOICE META + BILL TO -->
<table width="100%" style="margin-bottom:20px;border:1px solid #dddddd;">
<tr>
  <td width="50%" style="vertical-align:top;padding:16px 18px;border-right:1px solid #dddddd;">
    <div style="font-weight:bold;font-size:11pt;color:$primary;margin-bottom:12px;text-transform:uppercase;letter-spacing:1px;">Invoice Information</div>
    <table width="100%">
      <tr><td style="padding:8px 0;font-weight:bold;width:45%;color:#444444;line-height:1.8;">Invoice No.</td><td style="padding:8px 0;line-height:1.8;">${escapeHtml(snapshot.invoiceNumber)}</td></tr>
      <tr><td style="padding:8px 0;font-weight:bold;color:#444444;line-height:1.8;">Invoice Date</td><td style="padding:8px 0;line-height:1.8;">${formatDate(snapshot.date)}</td></tr>
      <tr><td style="padding:8px 0;font-weight:bold;color:#444444;line-height:1.8;">Due Date</td><td style="padding:8px 0;line-height:1.8;">${formatDate(snapshot.dueDate)}</td></tr>
      <tr><td style="padding:8px 0;font-weight:bold;color:#444444;line-height:1.8;">Currency</td><td style="padding:8px 0;line-height:1.8;">${escapeHtml(snapshot.currencyCode)}</td></tr>
      ${if (snapshot.invoiceStatus.isNotBlank()) """<tr><td style="padding:8px 0;font-weight:bold;color:#444444;line-height:1.8;">Status</td><td style="padding:8px 0;line-height:1.8;">${escapeHtml(snapshot.invoiceStatus)}</td></tr>""" else ""}
    </table>
  </td>
  <td width="50%" style="vertical-align:top;padding:16px 18px;">
    <div style="font-weight:bold;font-size:11pt;color:$primary;margin-bottom:12px;text-transform:uppercase;letter-spacing:1px;">Billed To</div>
    ${if (snapshot.customerName.isNotBlank()) """<div style="font-weight:bold;font-size:12pt;line-height:1.8;">${escapeHtml(snapshot.customerName)}</div>""" else ""}
    ${if (snapshot.customerAddress.isNotBlank()) """<div style="font-size:9pt;color:#555555;margin-top:6px;line-height:1.8;">${addressLines(snapshot.customerAddress)}</div>""" else ""}
    ${if (!snapshot.customerEmail.isNullOrBlank()) """<div style="font-size:9pt;color:#555555;margin-top:4px;line-height:1.8;">${escapeHtml(snapshot.customerEmail)}</div>""" else ""}
  </td>
</tr>
</table>

${if (snapshot.headerText.isNotBlank()) """<p style="margin-bottom:16px;font-size:9pt;color:#555555;font-style:italic;line-height:1.8;">${escapeHtml(snapshot.headerText)}</p>""" else ""}

<!-- LINE ITEMS TABLE -->
<table width="100%" style="border:1px solid #dddddd;margin-bottom:4px;">
  <tr style="background-color:$primary;color:#ffffff;">
    <th style="padding:12px 14px;text-align:left;">Description</th>
    <th style="padding:12px 14px;text-align:center;width:10%;">Qty</th>
    <th style="padding:12px 14px;text-align:right;width:18%;">Unit Price</th>
    <th style="padding:12px 14px;text-align:right;width:18%;">Amount</th>
  </tr>
  $itemRows
  <tr style="border-top:2px solid $primary;"><td colspan="4" style="padding:0;"></td></tr>
  $totalRows
</table>

$paymentSection
$notesSection

${if (snapshot.footerText.isNotBlank()) """<p style="margin-top:20px;text-align:center;font-size:9pt;color:#888888;border-top:1px solid #cccccc;padding-top:12px;line-height:1.8;">${escapeHtml(snapshot.footerText)}</p>""" else ""}

</body></html>"""
    }

    // -------------------------------------------------------------------------
    // Template 4: CREATIVE (Orange #FF6B35 + Deep Blue #004E89)
    // -------------------------------------------------------------------------

    private fun generateCreativeTemplate(snapshot: InvoiceSnapshot, isQuote: Boolean): String {
        val orange = "#FF6B35"
        val deepBlue = "#004E89"
        val docType = if (isQuote) "QUOTE" else "INVOICE"
        val itemRows = buildItemsRows(snapshot, "#ffffff", "#fff4ee", orange)
        val totalRows = buildTotalsRows(snapshot, deepBlue, deepBlue, "#ffffff")
        val paymentSection = buildPaymentSection(snapshot, deepBlue, orange)
        val notesSection = buildNotesSection(snapshot, deepBlue, orange)
        val watermark = buildStatusWatermark(snapshot.invoiceStatus)
        val logoHtml = if (!snapshot.logoBase64.isNullOrBlank())
            """<img src="data:image/png;base64,${snapshot.logoBase64}" style="max-height:60px;max-width:120px;" alt="logo"/>"""
        else ""

        return """<!DOCTYPE html>
<html><head><meta charset="UTF-8"/>
<style>
@page { margin: 15mm 15mm 15mm 15mm; }
body { font-family: 'Segoe UI', 'Trebuchet MS', Arial, sans-serif; font-size: 10pt; color: #222222; margin: 0; padding: 0; line-height: 1.8; }
table { border-collapse: collapse; }
td, th { word-wrap: break-word; }
</style>
</head><body>

<!-- TWO-TONE HEADER -->
<table width="100%" style="margin-bottom:20px;">
<tr>
  <td width="55%" style="background-color:$deepBlue;padding:24px 24px;vertical-align:middle;">
    $logoHtml
    ${if (snapshot.businessName.isNotBlank()) """<div style="font-size:19pt;font-weight:bold;color:#ffffff;margin-top:6px;line-height:1.3;">${escapeHtml(snapshot.businessName)}</div>""" else ""}
    ${if (snapshot.subheaderText.isNotBlank()) """<div style="font-size:13pt;font-weight:500;color:#a8c9e8;margin-top:6px;line-height:1.4;">${escapeHtml(snapshot.subheaderText)}</div>""" else ""}
    <div style="margin-top:8px;">
    ${if (snapshot.businessAddress.isNotBlank()) """<div style="font-size:9pt;color:#b0c8e0;margin-top:4px;line-height:1.8;">${addressLines(snapshot.businessAddress)}</div>""" else ""}
    ${if (snapshot.businessEmail.isNotBlank()) """<div style="font-size:9pt;color:#b0c8e0;margin-top:4px;">${escapeHtml(snapshot.businessEmail)}</div>""" else ""}
    ${if (snapshot.businessPhone.isNotBlank()) """<div style="font-size:9pt;color:#b0c8e0;margin-top:4px;">${escapeHtml(snapshot.businessPhone)}</div>""" else ""}
    ${if (snapshot.businessAbn.isNotBlank()) """<div style="font-size:9pt;color:#b0c8e0;margin-top:4px;">ABN: ${escapeHtml(snapshot.businessAbn)}</div>""" else ""}
    </div>
  </td>
  <td width="45%" style="background-color:$orange;padding:24px 24px;text-align:right;vertical-align:middle;">
    <div style="font-size:24pt;font-weight:bold;letter-spacing:3px;color:#ffffff;">$docType</div>
    $watermark
    <div style="font-size:10pt;color:#ffffff;margin-top:8px;font-weight:bold;">${escapeHtml(snapshot.invoiceNumber)}</div>
  </td>
</tr>
</table>

<!-- INVOICE META + BILL TO -->
<table width="100%" style="margin-bottom:20px;">
<tr>
  <td width="50%" style="vertical-align:top;padding-right:12px;">
    <table width="100%" style="border-left:4px solid $orange;background-color:#fff4ee;">
      <tr><td colspan="2" style="padding:10px 14px;font-weight:bold;font-size:11pt;color:$deepBlue;">INVOICE DETAILS</td></tr>
      <tr><td style="padding:8px 14px;font-weight:bold;width:45%;color:#444444;line-height:1.8;">Invoice #</td><td style="padding:8px 14px;line-height:1.8;">${escapeHtml(snapshot.invoiceNumber)}</td></tr>
      <tr><td style="padding:8px 14px;font-weight:bold;color:#444444;line-height:1.8;">Date</td><td style="padding:8px 14px;line-height:1.8;">${formatDate(snapshot.date)}</td></tr>
      <tr><td style="padding:8px 14px;font-weight:bold;color:#444444;line-height:1.8;">Due Date</td><td style="padding:8px 14px;line-height:1.8;">${formatDate(snapshot.dueDate)}</td></tr>
      <tr><td style="padding:8px 14px;font-weight:bold;color:#444444;line-height:1.8;">Currency</td><td style="padding:8px 14px;line-height:1.8;">${escapeHtml(snapshot.currencyCode)}</td></tr>
      ${if (snapshot.invoiceStatus.isNotBlank()) """<tr><td style="padding:8px 14px;font-weight:bold;color:#444444;line-height:1.8;">Status</td><td style="padding:8px 14px;line-height:1.8;">${escapeHtml(snapshot.invoiceStatus)}</td></tr>""" else ""}
    </table>
  </td>
  <td width="50%" style="vertical-align:top;padding-left:12px;">
    <table width="100%" style="border-left:4px solid $deepBlue;background-color:#eaf3ff;">
      <tr><td style="padding:10px 14px;font-weight:bold;font-size:11pt;color:$deepBlue;">BILL TO</td></tr>
      ${if (snapshot.customerName.isNotBlank()) """<tr><td style="padding:8px 14px;font-weight:bold;font-size:12pt;line-height:1.8;">${escapeHtml(snapshot.customerName)}</td></tr>""" else ""}
      ${if (snapshot.customerAddress.isNotBlank()) """<tr><td style="padding:8px 14px;font-size:9pt;color:#555555;line-height:1.8;">${addressLines(snapshot.customerAddress)}</td></tr>""" else ""}
      ${if (!snapshot.customerEmail.isNullOrBlank()) """<tr><td style="padding:8px 14px;font-size:9pt;color:#555555;line-height:1.8;">${escapeHtml(snapshot.customerEmail)}</td></tr>""" else ""}
    </table>
  </td>
</tr>
</table>

${if (snapshot.headerText.isNotBlank()) """<p style="margin-bottom:16px;font-size:9pt;color:#555555;font-style:italic;line-height:1.8;">${escapeHtml(snapshot.headerText)}</p>""" else ""}

<!-- LINE ITEMS TABLE -->
<table width="100%" style="margin-bottom:4px;">
  <tr style="background-color:$orange;color:#ffffff;">
    <th style="padding:12px 14px;text-align:left;font-size:10pt;">Description</th>
    <th style="padding:12px 14px;text-align:center;font-size:10pt;width:10%;">Qty</th>
    <th style="padding:12px 14px;text-align:right;font-size:10pt;width:18%;">Unit Price</th>
    <th style="padding:12px 14px;text-align:right;font-size:10pt;width:18%;">Total</th>
  </tr>
  $itemRows
  <tr><td colspan="4" style="padding:0;"></td></tr>
  $totalRows
</table>

$paymentSection
$notesSection

${if (snapshot.footerText.isNotBlank()) """<p style="margin-top:20px;text-align:center;font-size:9pt;color:#888888;border-top:1px solid #dddddd;padding-top:12px;line-height:1.8;">${escapeHtml(snapshot.footerText)}</p>""" else ""}

</body></html>"""
    }

    // -------------------------------------------------------------------------
    // Template 5: PREMIUM PROFESSIONAL (Dark Navy #1C1C2E + Blue #2563EB)
    // -------------------------------------------------------------------------

    private fun generatePremiumProfessionalTemplate(snapshot: InvoiceSnapshot, isQuote: Boolean): String {
        val navy = "#1C1C2E"
        val blue = "#2563EB"
        val lightBlue = "#EFF6FF"
        val docType = if (isQuote) "QUOTE" else "INVOICE"
        val logoHtml = if (!snapshot.logoBase64.isNullOrBlank())
            """<img src="data:image/png;base64,${snapshot.logoBase64}" style="max-height:55px;max-width:110px;margin-bottom:8px;" alt="logo"/>"""
        else ""
        val watermark = buildStatusWatermark(snapshot.invoiceStatus)

        // Tight item rows for premium density (8px vertical padding)
        val itemRows = snapshot.items.mapIndexed { i, item ->
            val bg = if (i % 2 == 0) "#ffffff" else "#F8FAFF"
            val unitDollars = formatMoney(item.unitPrice, snapshot.currencyCode)
            val totalDollars = formatMoney(item.total, snapshot.currencyCode)
            """<tr style="background-color:$bg;">
                <td style="padding:8px 12px;border-bottom:1px solid #E2E8F0;line-height:1.5;word-wrap:break-word;font-size:10pt;">${escapeHtml(item.description)}</td>
                <td style="padding:8px 12px;border-bottom:1px solid #E2E8F0;text-align:center;line-height:1.5;font-size:10pt;">${formatQty(item.quantity)}</td>
                <td style="padding:8px 12px;border-bottom:1px solid #E2E8F0;text-align:right;line-height:1.5;font-size:10pt;">$unitDollars</td>
                <td style="padding:8px 12px;border-bottom:1px solid #E2E8F0;text-align:right;font-weight:bold;color:$blue;line-height:1.5;font-size:10pt;">$totalDollars</td>
            </tr>"""
        }.joinToString("\n")

        // Subtotal + tax rows (compact, right side)
        val taxPct = snapshot.taxRate * 100
        val taxLabel = if (snapshot.taxRate > 0) {
            val formatted = if (taxPct == taxPct.toLong().toDouble())
                "${taxPct.toLong()}%" else "${String.format("%.2f", taxPct).trimEnd('0').trimEnd('.')}%"
            "Tax ($formatted)"
        } else "Tax"
        val subtotalRows = """
            <tr>
                <td colspan="3" style="padding:7px 12px;text-align:right;color:#64748B;font-size:9.5pt;">Subtotal</td>
                <td style="padding:7px 12px;text-align:right;font-size:9.5pt;color:#1E293B;">${formatMoney(snapshot.subtotal, snapshot.currencyCode)}</td>
            </tr>
            <tr>
                <td colspan="3" style="padding:7px 12px;text-align:right;color:#64748B;font-size:9.5pt;">$taxLabel</td>
                <td style="padding:7px 12px;text-align:right;font-size:9.5pt;color:#1E293B;">${formatMoney(snapshot.taxAmount, snapshot.currencyCode)}</td>
            </tr>
        """.trimIndent()

        // Phase 2: Enhanced payment section with method badge
        val paymentSection = buildEnhancedPaymentSection(snapshot, navy, blue)

        val notesSection = if (snapshot.notes.isNotBlank()) """
            <table width="100%" style="border-collapse:collapse;margin-top:14px;">
                <tr><td style="padding:10px 12px;background-color:#F1F5F9;font-weight:bold;font-size:10pt;color:$navy;border-left:4px solid $blue;">NOTES</td></tr>
                <tr><td style="padding:8px 12px;font-size:9.5pt;line-height:1.6;word-wrap:break-word;color:#475569;">${escapeHtml(snapshot.notes)}</td></tr>
            </table>
        """.trimIndent() else ""

        // Phase 2: Signature and professional footer
        val showSignature = settings?.showSignatureField ?: true
        val signatureSection = buildSignatureSection(blue, "#64748B", showSignature)
        val footer = buildProfessionalFooter(snapshot, navy, blue, "#F8FAFC")

        return """<!DOCTYPE html>
<html><head><meta charset="UTF-8"/>
<style>
@page { margin: 12mm 14mm 12mm 14mm; }
body { font-family: 'Segoe UI', Arial, Helvetica, sans-serif; font-size: 10pt; color: #1E293B; margin: 0; padding: 0; line-height: 1.6; }
table { border-collapse: collapse; }
td, th { word-wrap: break-word; }
</style>
</head><body>

<!-- DARK NAVY HEADER -->
<table width="100%" style="background-color:$navy;margin-bottom:0;">
<tr>
  <td style="padding:18px 20px;vertical-align:middle;">
    $logoHtml
    ${if (snapshot.businessName.isNotBlank()) """<div style="font-size:18pt;font-weight:bold;color:#FFFFFF;line-height:1.2;">${escapeHtml(snapshot.businessName)}</div>""" else ""}
    ${if (snapshot.subheaderText.isNotBlank()) """<div style="font-size:11pt;color:#94A3B8;margin-top:4px;line-height:1.4;">${escapeHtml(snapshot.subheaderText)}</div>""" else ""}
    <div style="margin-top:8px;font-size:9pt;color:#64748B;">
    ${if (snapshot.businessAddress.isNotBlank()) """<span>${addressLines(snapshot.businessAddress)}</span>""" else ""}
    ${if (snapshot.businessEmail.isNotBlank()) """<span style="margin-left:6px;">${escapeHtml(snapshot.businessEmail)}</span>""" else ""}
    ${if (snapshot.businessPhone.isNotBlank()) """<span style="margin-left:6px;">${escapeHtml(snapshot.businessPhone)}</span>""" else ""}
    ${if (snapshot.businessAbn.isNotBlank()) """<div style="margin-top:4px;">ABN: ${escapeHtml(snapshot.businessAbn)}</div>""" else ""}
    </div>
  </td>
  <td style="padding:18px 20px;text-align:right;vertical-align:middle;">
    <div style="font-size:26pt;font-weight:bold;color:$blue;letter-spacing:2px;">$docType</div>
    <div style="color:#94A3B8;font-size:9.5pt;margin-top:6px;">${escapeHtml(snapshot.invoiceNumber)}</div>
    $watermark
  </td>
</tr>
</table>

<!-- BLUE ACCENT BAR -->
<table width="100%" style="background-color:$blue;margin-bottom:14px;"><tr><td style="padding:3px 0;"></td></tr></table>

<!-- INVOICE DETAILS + BILL TO (2-column compact) -->
<table width="100%" style="margin-bottom:12px;">
<tr>
  <td width="50%" style="vertical-align:top;padding-right:10px;">
    <table width="100%" style="background-color:$lightBlue;border-left:3px solid $blue;">
      <tr><td colspan="2" style="padding:8px 12px;font-weight:bold;font-size:10pt;color:$navy;text-transform:uppercase;letter-spacing:0.5px;">&#x1F4CB; Invoice Details</td></tr>
      <tr><td style="padding:6px 12px;font-weight:bold;width:44%;font-size:9.5pt;color:#64748B;">Invoice #</td><td style="padding:6px 12px;font-size:9.5pt;color:#1E293B;">${escapeHtml(snapshot.invoiceNumber)}</td></tr>
      <tr style="background-color:#DBEAFE;"><td style="padding:6px 12px;font-weight:bold;font-size:9.5pt;color:#64748B;">Date</td><td style="padding:6px 12px;font-size:9.5pt;color:#1E293B;">${formatDate(snapshot.date)}</td></tr>
      <tr><td style="padding:6px 12px;font-weight:bold;font-size:9.5pt;color:#64748B;">Due Date</td><td style="padding:6px 12px;font-size:9.5pt;color:#1E293B;">${formatDate(snapshot.dueDate)}</td></tr>
      <tr style="background-color:#DBEAFE;"><td style="padding:6px 12px;font-weight:bold;font-size:9.5pt;color:#64748B;">Currency</td><td style="padding:6px 12px;font-size:9.5pt;color:#1E293B;">${escapeHtml(snapshot.currencyCode)}</td></tr>
      ${if (snapshot.invoiceStatus.isNotBlank()) """<tr><td style="padding:6px 12px;font-weight:bold;font-size:9.5pt;color:#64748B;">Status</td><td style="padding:6px 12px;font-size:9.5pt;color:#1E293B;">${escapeHtml(snapshot.invoiceStatus)}</td></tr>""" else ""}
    </table>
  </td>
  <td width="50%" style="vertical-align:top;padding-left:10px;">
    <table width="100%" style="background-color:#F8FAFC;border-left:3px solid #CBD5E1;">
      <tr><td style="padding:8px 12px;font-weight:bold;font-size:10pt;color:$navy;text-transform:uppercase;letter-spacing:0.5px;">&#x1F464; Bill To</td></tr>
      ${if (snapshot.customerName.isNotBlank()) """<tr><td style="padding:6px 12px;font-weight:bold;font-size:11pt;color:#1E293B;">${escapeHtml(snapshot.customerName)}</td></tr>""" else ""}
      ${if (snapshot.customerAddress.isNotBlank()) """<tr><td style="padding:6px 12px;font-size:9.5pt;color:#475569;line-height:1.6;">${addressLines(snapshot.customerAddress)}</td></tr>""" else ""}
      ${if (!snapshot.customerEmail.isNullOrBlank()) """<tr><td style="padding:6px 12px;font-size:9.5pt;color:#475569;">${escapeHtml(snapshot.customerEmail)}</td></tr>""" else ""}
    </table>
  </td>
</tr>
</table>

${if (snapshot.headerText.isNotBlank()) """<p style="margin-bottom:10px;font-style:italic;color:#64748B;font-size:9.5pt;line-height:1.6;">${escapeHtml(snapshot.headerText)}</p>""" else ""}

<!-- LINE ITEMS TABLE (tight rows) -->
<table width="100%" style="border-collapse:collapse;margin-bottom:0;">
  <tr style="background-color:$navy;color:#FFFFFF;">
    <th style="padding:10px 12px;text-align:left;font-size:9.5pt;letter-spacing:0.5px;">DESCRIPTION</th>
    <th style="padding:10px 12px;text-align:center;font-size:9.5pt;width:9%;">QTY</th>
    <th style="padding:10px 12px;text-align:right;font-size:9.5pt;width:17%;">UNIT PRICE</th>
    <th style="padding:10px 12px;text-align:right;font-size:9.5pt;width:17%;">TOTAL</th>
  </tr>
  $itemRows
  $subtotalRows
</table>

<!-- PROMINENT TOTAL DUE SECTION -->
<table width="100%" style="background-color:$blue;margin-top:0;margin-bottom:14px;">
<tr>
  <td style="padding:14px 20px;color:#BFDBFE;font-size:11pt;font-weight:bold;letter-spacing:1px;text-transform:uppercase;">TOTAL DUE</td>
  <td style="padding:14px 20px;text-align:right;color:#FFFFFF;font-size:22pt;font-weight:bold;">${formatMoney(snapshot.totalAmount, snapshot.currencyCode)}</td>
</tr>
</table>

$paymentSection
$notesSection

<!-- Phase 2: Signature section -->
$signatureSection

<!-- Phase 2: Professional multi-column footer -->
$footer

</body></html>"""
    }


    // -------------------------------------------------------------------------
    // Template 6: WARM APPROACHABLE (Amber #F59E0B + Dark #1F2937)
    // -------------------------------------------------------------------------

    private fun generateWarmApproachableTemplate(snapshot: InvoiceSnapshot, isQuote: Boolean): String {
        val amber = "#F59E0B"
        val darkBrown = "#1F2937"
        val warmCream = "#FFFBEB"
        val lightAmber = "#FEF3C7"
        val docType = if (isQuote) "QUOTE" else "INVOICE"
        val logoHtml = if (!snapshot.logoBase64.isNullOrBlank())
            """<img src="data:image/png;base64,${snapshot.logoBase64}" style="max-height:55px;max-width:110px;margin-bottom:8px;" alt="logo"/>"""
        else ""
        val watermark = buildStatusWatermark(snapshot.invoiceStatus)

        // Warm item rows (comfortable 10px padding, warm alternating colors)
        val itemRows = snapshot.items.mapIndexed { i, item ->
            val bg = if (i % 2 == 0) "#FFFFFF" else warmCream
            val unitDollars = formatMoney(item.unitPrice, snapshot.currencyCode)
            val totalDollars = formatMoney(item.total, snapshot.currencyCode)
            """<tr style="background-color:$bg;">
                <td style="padding:9px 12px;border-bottom:1px solid #FDE68A;line-height:1.6;word-wrap:break-word;font-size:10pt;">${escapeHtml(item.description)}</td>
                <td style="padding:9px 12px;border-bottom:1px solid #FDE68A;text-align:center;line-height:1.6;font-size:10pt;">${formatQty(item.quantity)}</td>
                <td style="padding:9px 12px;border-bottom:1px solid #FDE68A;text-align:right;line-height:1.6;font-size:10pt;">$unitDollars</td>
                <td style="padding:9px 12px;border-bottom:1px solid #FDE68A;text-align:right;font-weight:bold;color:$darkBrown;line-height:1.6;font-size:10pt;">$totalDollars</td>
            </tr>"""
        }.joinToString("\n")

        val taxPct = snapshot.taxRate * 100
        val taxLabel = if (snapshot.taxRate > 0) {
            val formatted = if (taxPct == taxPct.toLong().toDouble())
                "${taxPct.toLong()}%" else "${String.format("%.2f", taxPct).trimEnd('0').trimEnd('.')}%"
            "Tax ($formatted)"
        } else "Tax"
        val totalsRows = """
            <tr>
                <td colspan="3" style="padding:8px 12px;text-align:right;color:#6B7280;font-size:9.5pt;">Subtotal</td>
                <td style="padding:8px 12px;text-align:right;font-size:9.5pt;color:#1F2937;">${formatMoney(snapshot.subtotal, snapshot.currencyCode)}</td>
            </tr>
            <tr>
                <td colspan="3" style="padding:8px 12px;text-align:right;color:#6B7280;font-size:9.5pt;">$taxLabel</td>
                <td style="padding:8px 12px;text-align:right;font-size:9.5pt;color:#1F2937;">${formatMoney(snapshot.taxAmount, snapshot.currencyCode)}</td>
            </tr>
            <tr style="background-color:$amber;">
                <td colspan="3" style="padding:12px 16px;text-align:right;font-weight:bold;font-size:11pt;color:#FFFFFF;letter-spacing:0.5px;">TOTAL DUE</td>
                <td style="padding:12px 16px;text-align:right;font-weight:bold;font-size:18pt;color:#FFFFFF;">${formatMoney(snapshot.totalAmount, snapshot.currencyCode)}</td>
            </tr>
        """.trimIndent()

        val hasBank = snapshot.bankName.isNotBlank() || snapshot.bankAccountName.isNotBlank()
            || snapshot.bankAccountNumber.isNotBlank() || snapshot.bankBsb.isNotBlank()
        val paymentSection = if (hasBank) buildEnhancedPaymentSection(snapshot, darkBrown, amber)
        else ""

        val notesSection = if (snapshot.notes.isNotBlank()) """
            <table width="100%" style="border-collapse:collapse;margin-top:14px;">
                <tr><td style="padding:10px 12px;background-color:$lightAmber;font-weight:bold;font-size:10pt;color:$darkBrown;border-left:4px solid $amber;">Notes</td></tr>
                <tr><td style="padding:8px 12px;font-size:9.5pt;line-height:1.6;word-wrap:break-word;color:#6B7280;">${escapeHtml(snapshot.notes)}</td></tr>
            </table>
        """.trimIndent() else ""

        // Phase 2: Signature section (warm amber styling) and professional footer
        val showSignature = settings?.showSignatureField ?: true
        val signatureSection = buildSignatureSection(amber, "#78716C", showSignature)
        val footer = buildProfessionalFooter(snapshot, darkBrown, amber, lightAmber)

        return """<!DOCTYPE html>
<html><head><meta charset="UTF-8"/>
<style>
@page { margin: 12mm 14mm 12mm 14mm; }
body { font-family: 'Segoe UI', Arial, Helvetica, sans-serif; font-size: 10pt; color: #1F2937; margin: 0; padding: 0; line-height: 1.6; }
table { border-collapse: collapse; }
td, th { word-wrap: break-word; }
</style>
</head><body>

<!-- WARM HEADER (cream background with amber accent) -->
<table width="100%" style="background-color:$warmCream;border-bottom:0;margin-bottom:0;">
<tr>
  <td style="padding:20px 20px 16px 20px;vertical-align:top;border-left:6px solid $amber;">
    $logoHtml
    ${if (snapshot.businessName.isNotBlank()) """<div style="font-size:20pt;font-weight:bold;color:$darkBrown;line-height:1.2;">${escapeHtml(snapshot.businessName)}</div>""" else ""}
    ${if (snapshot.subheaderText.isNotBlank()) """<div style="font-size:12pt;color:#92400E;margin-top:4px;line-height:1.4;">${escapeHtml(snapshot.subheaderText)}</div>""" else ""}
    <div style="margin-top:8px;font-size:9pt;color:#78716C;">
    ${if (snapshot.businessAddress.isNotBlank()) """<div style="line-height:1.6;">${addressLines(snapshot.businessAddress)}</div>""" else ""}
    ${if (snapshot.businessEmail.isNotBlank()) """<div style="margin-top:2px;">${escapeHtml(snapshot.businessEmail)}</div>""" else ""}
    ${if (snapshot.businessPhone.isNotBlank()) """<div style="margin-top:2px;">${escapeHtml(snapshot.businessPhone)}</div>""" else ""}
    ${if (snapshot.businessAbn.isNotBlank()) """<div style="margin-top:2px;">ABN: ${escapeHtml(snapshot.businessAbn)}</div>""" else ""}
    </div>
  </td>
  <td style="padding:20px 20px 16px 20px;text-align:right;vertical-align:top;">
    <div style="font-size:24pt;font-weight:bold;color:$amber;letter-spacing:2px;">$docType</div>
    <div style="color:#78716C;font-size:9.5pt;margin-top:6px;">${escapeHtml(snapshot.invoiceNumber)}</div>
    $watermark
  </td>
</tr>
</table>

<!-- AMBER SEPARATOR -->
<table width="100%" style="background-color:$amber;margin-bottom:14px;"><tr><td style="padding:2px 0;"></td></tr></table>

<!-- INVOICE DETAILS + BILL TO (2-column) -->
<table width="100%" style="margin-bottom:12px;">
<tr>
  <td width="50%" style="vertical-align:top;padding-right:10px;">
    <table width="100%" style="background-color:$lightAmber;border-left:3px solid $amber;">
      <tr><td colspan="2" style="padding:8px 12px;font-weight:bold;font-size:10pt;color:$darkBrown;">&#x1F4CB; Invoice Details</td></tr>
      <tr><td style="padding:6px 12px;font-weight:bold;width:44%;font-size:9.5pt;color:#6B7280;">Invoice #</td><td style="padding:6px 12px;font-size:9.5pt;">${escapeHtml(snapshot.invoiceNumber)}</td></tr>
      <tr style="background-color:#FDE68A;"><td style="padding:6px 12px;font-weight:bold;font-size:9.5pt;color:#6B7280;">Date</td><td style="padding:6px 12px;font-size:9.5pt;">${formatDate(snapshot.date)}</td></tr>
      <tr><td style="padding:6px 12px;font-weight:bold;font-size:9.5pt;color:#6B7280;">Due Date</td><td style="padding:6px 12px;font-size:9.5pt;">${formatDate(snapshot.dueDate)}</td></tr>
      <tr style="background-color:#FDE68A;"><td style="padding:6px 12px;font-weight:bold;font-size:9.5pt;color:#6B7280;">Currency</td><td style="padding:6px 12px;font-size:9.5pt;">${escapeHtml(snapshot.currencyCode)}</td></tr>
      ${if (snapshot.invoiceStatus.isNotBlank()) """<tr><td style="padding:6px 12px;font-weight:bold;font-size:9.5pt;color:#6B7280;">Status</td><td style="padding:6px 12px;font-size:9.5pt;">${escapeHtml(snapshot.invoiceStatus)}</td></tr>""" else ""}
    </table>
  </td>
  <td width="50%" style="vertical-align:top;padding-left:10px;">
    <table width="100%" style="background-color:#FAFAF9;border-left:3px solid #D6D3D1;">
      <tr><td style="padding:8px 12px;font-weight:bold;font-size:10pt;color:$darkBrown;">&#x1F464; Bill To</td></tr>
      ${if (snapshot.customerName.isNotBlank()) """<tr><td style="padding:6px 12px;font-weight:bold;font-size:11pt;color:$darkBrown;">${escapeHtml(snapshot.customerName)}</td></tr>""" else ""}
      ${if (snapshot.customerAddress.isNotBlank()) """<tr><td style="padding:6px 12px;font-size:9.5pt;color:#78716C;line-height:1.6;">${addressLines(snapshot.customerAddress)}</td></tr>""" else ""}
      ${if (!snapshot.customerEmail.isNullOrBlank()) """<tr><td style="padding:6px 12px;font-size:9.5pt;color:#78716C;">${escapeHtml(snapshot.customerEmail)}</td></tr>""" else ""}
    </table>
  </td>
</tr>
</table>

${if (snapshot.headerText.isNotBlank()) """<p style="margin-bottom:10px;font-style:italic;color:#78716C;font-size:9.5pt;line-height:1.6;">${escapeHtml(snapshot.headerText)}</p>""" else ""}

<!-- LINE ITEMS TABLE -->
<table width="100%" style="border-collapse:collapse;margin-bottom:0;">
  <tr style="background-color:$amber;color:#FFFFFF;">
    <th style="padding:10px 12px;text-align:left;font-size:9.5pt;">Description</th>
    <th style="padding:10px 12px;text-align:center;font-size:9.5pt;width:9%;">Qty</th>
    <th style="padding:10px 12px;text-align:right;font-size:9.5pt;width:17%;">Unit Price</th>
    <th style="padding:10px 12px;text-align:right;font-size:9.5pt;width:17%;">Total</th>
  </tr>
  $itemRows
  <tr><td colspan="4" style="padding:0;border-top:2px solid $amber;"></td></tr>
  $totalsRows
</table>

$paymentSection
$notesSection

<!-- Phase 2: Signature section (warm amber styling) -->
$signatureSection

<!-- Phase 2: Professional multi-column footer -->
$footer

</body></html>"""
    }


    // -------------------------------------------------------------------------
    // Template 7: SASS PROFESSIONAL (Deep Navy #0A2540 + Electric Blue #0066FF)
    // Generated via SassStyleEngine — the highest-fidelity template.
    // -------------------------------------------------------------------------

    private fun generateSassProfessionalTemplate(snapshot: InvoiceSnapshot, isQuote: Boolean): String {
        val tokens = SassTokens.sassprofessional()
        val engine = SassStyleEngine(tokens)
        val compiledCss = engine.compile()

        val primary    = tokens.colorPrimary        // #0A2540
        val accent     = tokens.colorAccent         // #0066FF
        val surface    = tokens.colorSurface        // #F7F9FC
        val textMuted  = tokens.colorTextMuted      // #6B7280
        val border     = tokens.colorBorder         // #E2E8F0

        val docType = if (isQuote) "QUOTE" else "INVOICE"
        val watermark = buildStatusWatermark(snapshot.invoiceStatus)
        val logoHtml = if (!snapshot.logoBase64.isNullOrBlank())
            """<img src="data:image/png;base64,${snapshot.logoBase64}" style="max-height:60px;max-width:130px;" alt="logo"/>"""
        else ""

        // Build item rows using SASS tokens for row heights and colours
        val itemRows = snapshot.items.mapIndexed { i, item ->
            val bg = if (i % 2 == 0) "#FFFFFF" else surface
            val unitDollars = formatMoney(item.unitPrice, snapshot.currencyCode)
            val totalDollars = formatMoney(item.total, snapshot.currencyCode)
            """<tr style="background-color:$bg;">
                <td style="padding:10px 14px;border-bottom:1px solid $border;line-height:1.8;word-wrap:break-word;">${escapeHtml(item.description)}</td>
                <td style="padding:10px 14px;border-bottom:1px solid $border;text-align:center;line-height:1.8;">${formatQty(item.quantity)}</td>
                <td style="padding:10px 14px;border-bottom:1px solid $border;text-align:right;line-height:1.8;">$unitDollars</td>
                <td style="padding:10px 14px;border-bottom:1px solid $border;text-align:right;font-weight:700;color:$accent;line-height:1.8;">$totalDollars</td>
            </tr>"""
        }.joinToString("\n")

        val taxLabel = if (snapshot.taxRate > 0) "GST (${(snapshot.taxRate * 100).toInt()}%)" else "Tax"
        val totalsRows = """
            <tr>
                <td colspan="3" style="padding:9px 14px;text-align:right;color:$textMuted;">Subtotal</td>
                <td style="padding:9px 14px;text-align:right;">${formatMoney(snapshot.subtotal, snapshot.currencyCode)}</td>
            </tr>
            <tr>
                <td colspan="3" style="padding:9px 14px;text-align:right;color:$textMuted;">$taxLabel</td>
                <td style="padding:9px 14px;text-align:right;">${formatMoney(snapshot.taxAmount, snapshot.currencyCode)}</td>
            </tr>
            <tr>
                <td colspan="3" style="padding:14px;text-align:right;font-weight:700;font-size:11pt;background-color:$primary;color:#FFFFFF;">TOTAL DUE</td>
                <td style="padding:14px;text-align:right;font-weight:700;font-size:14pt;background-color:$primary;color:#FFFFFF;">${formatMoney(snapshot.totalAmount, snapshot.currencyCode)}</td>
            </tr>
        """.trimIndent()

        val hasBank = snapshot.bankName.isNotBlank() || snapshot.bankAccountName.isNotBlank()
            || snapshot.bankAccountNumber.isNotBlank() || snapshot.bankBsb.isNotBlank()
        // Phase 2: Use enhanced payment section with method badge
        val paymentSection = if (hasBank) buildEnhancedPaymentSection(snapshot, primary, accent) else ""

        val notesSection = if (snapshot.notes.isNotBlank()) """
            <table width="100%" style="border-collapse:collapse;margin-top:20px;">
                <tr><td style="padding:10px 14px;background-color:$surface;font-weight:700;font-size:10pt;color:$primary;border-left:4px solid $accent;">NOTES</td></tr>
                <tr><td style="padding:10px 14px;font-size:9.5pt;line-height:1.6;word-wrap:break-word;">${escapeHtml(snapshot.notes)}</td></tr>
            </table>
        """.trimIndent() else ""

        // Phase 2: Signature section and professional footer
        val showSignature = settings?.showSignatureField ?: true
        val signatureSection = buildSignatureSection(accent, textMuted, showSignature)
        val footer = buildProfessionalFooter(snapshot, primary, accent, surface)

        return """<!DOCTYPE html>
<html><head><meta charset="UTF-8"/>
<style>
$compiledCss
</style>
</head><body>

<!-- ═══ HEADER BAND (compiled from SASS tokens) ═══ -->
<table width="100%" style="background-color:$primary;color:#FFFFFF;margin-bottom:0;">
<tr>
  <td style="padding:22px 24px;vertical-align:middle;">
    $logoHtml
    ${if (snapshot.businessName.isNotBlank()) """<div style="font-size:18pt;font-weight:700;color:#FFFFFF;margin-top:${if (logoHtml.isNotBlank()) "8px" else "0"};line-height:1.2;">${escapeHtml(snapshot.businessName)}</div>""" else ""}
    ${if (snapshot.subheaderText.isNotBlank()) """<div style="font-size:10pt;color:#94A3B8;margin-top:4px;">${escapeHtml(snapshot.subheaderText)}</div>""" else ""}
    <div style="margin-top:8px;font-size:8.5pt;color:#CBD5E1;line-height:1.6;">
    ${if (snapshot.businessAddress.isNotBlank()) """<div>${addressLines(snapshot.businessAddress)}</div>""" else ""}
    ${if (snapshot.businessEmail.isNotBlank()) """<div>${escapeHtml(snapshot.businessEmail)}</div>""" else ""}
    ${if (snapshot.businessPhone.isNotBlank()) """<div>${escapeHtml(snapshot.businessPhone)}</div>""" else ""}
    ${if (snapshot.businessAbn.isNotBlank()) """<div>ABN: ${escapeHtml(snapshot.businessAbn)}</div>""" else ""}
    </div>
  </td>
  <td style="padding:22px 24px;text-align:right;vertical-align:middle;">
    <div style="font-size:26pt;font-weight:700;letter-spacing:3px;color:#FFFFFF;">$docType</div>
    <div style="font-size:9pt;color:#64748B;margin-top:6px;">${escapeHtml(snapshot.invoiceNumber)}</div>
    $watermark
  </td>
</tr>
</table>

<!-- ═══ ACCENT BAR (4px electric blue — compiled from sass tokens) ═══ -->
<table width="100%" style="background-color:$accent;margin-bottom:16px;"><tr><td style="padding:2px 0;"></td></tr></table>

<!-- ═══ INVOICE META + BILL TO (2-column grid) ═══ -->
<table width="100%" style="margin-bottom:16px;">
<tr>
  <td width="50%" style="vertical-align:top;padding-right:10px;">
    <table width="100%" style="background-color:$surface;border-left:4px solid $accent;">
      <tr><td colspan="2" style="padding:10px 14px;font-weight:700;font-size:10pt;color:$primary;">&#x1F4CB; INVOICE DETAILS</td></tr>
      <tr><td style="padding:8px 14px;font-weight:700;width:44%;color:$textMuted;font-size:9pt;">Invoice #</td><td style="padding:8px 14px;font-size:9pt;">${escapeHtml(snapshot.invoiceNumber)}</td></tr>
      <tr style="background-color:#EFF6FF;"><td style="padding:8px 14px;font-weight:700;color:$textMuted;font-size:9pt;">Date</td><td style="padding:8px 14px;font-size:9pt;">${formatDate(snapshot.date)}</td></tr>
      <tr><td style="padding:8px 14px;font-weight:700;color:$textMuted;font-size:9pt;">Due Date</td><td style="padding:8px 14px;font-size:9pt;">${formatDate(snapshot.dueDate)}</td></tr>
      <tr style="background-color:#EFF6FF;"><td style="padding:8px 14px;font-weight:700;color:$textMuted;font-size:9pt;">Currency</td><td style="padding:8px 14px;font-size:9pt;">${escapeHtml(snapshot.currencyCode)}</td></tr>
      ${if (snapshot.invoiceStatus.isNotBlank()) """<tr><td style="padding:8px 14px;font-weight:700;color:$textMuted;font-size:9pt;">Status</td><td style="padding:8px 14px;font-size:9pt;">${escapeHtml(snapshot.invoiceStatus)}</td></tr>""" else ""}
    </table>
  </td>
  <td width="50%" style="vertical-align:top;padding-left:10px;">
    <table width="100%" style="background-color:#FAFBFC;border-left:4px solid $border;">
      <tr><td style="padding:10px 14px;font-weight:700;font-size:10pt;color:$primary;">&#x1F464; BILL TO</td></tr>
      ${if (snapshot.customerName.isNotBlank()) """<tr><td style="padding:8px 14px;font-weight:700;font-size:11pt;line-height:1.2;">${escapeHtml(snapshot.customerName)}</td></tr>""" else ""}
      ${if (snapshot.customerAddress.isNotBlank()) """<tr><td style="padding:8px 14px;font-size:9pt;color:$textMuted;line-height:1.6;">${addressLines(snapshot.customerAddress)}</td></tr>""" else ""}
      ${if (!snapshot.customerEmail.isNullOrBlank()) """<tr><td style="padding:8px 14px;font-size:9pt;color:$textMuted;">${escapeHtml(snapshot.customerEmail)}</td></tr>""" else ""}
    </table>
  </td>
</tr>
</table>

${if (snapshot.headerText.isNotBlank()) """<p style="margin:0 0 14px 0;font-style:italic;color:$textMuted;line-height:1.6;font-size:9.5pt;">${escapeHtml(snapshot.headerText)}</p>""" else ""}

<!-- ═══ LINE ITEMS TABLE (row heights enforced — no overlapping text) ═══ -->
<table width="100%" style="border-collapse:collapse;margin-bottom:0;">
  <tr style="background-color:$accent;color:#FFFFFF;">
    <th style="padding:12px 14px;text-align:left;font-size:9.5pt;">Description</th>
    <th style="padding:12px 14px;text-align:center;font-size:9.5pt;width:9%;">Qty</th>
    <th style="padding:12px 14px;text-align:right;font-size:9.5pt;width:17%;">Unit Price</th>
    <th style="padding:12px 14px;text-align:right;font-size:9.5pt;width:17%;">Amount</th>
  </tr>
  $itemRows
  <tr><td colspan="4" style="padding:0;border-top:2px solid $accent;"></td></tr>
  $totalsRows
</table>

$paymentSection
$notesSection

<!-- Phase 2: Signature section -->
$signatureSection

<!-- Phase 2: Professional multi-column footer -->
$footer

</body></html>"""
    }

    // -------------------------------------------------------------------------
    // Template 8: REFINED (Canvas Grid Match) - Purple #6B4C9A + Orange #FF9F43
    // -------------------------------------------------------------------------

    private fun generateRefinedTemplate(snapshot: InvoiceSnapshot, isQuote: Boolean): String {
        val purple = "#6B4C9A"
        val orange = "#FF9F43"
        val docType = if (isQuote) "QUOTE" else "INVOICE"
        val watermark = buildStatusWatermark(snapshot.invoiceStatus)
        val logoHtml = if (!snapshot.logoBase64.isNullOrBlank())
            """<img src="data:image/png;base64,${snapshot.logoBase64}" style="max-height:48px;max-width:130px;" alt="logo"/>"""
        else ""

        // Build item rows matching Canvas striping
        val itemRows = snapshot.items.mapIndexed { i, item ->
            val bg = if (i % 2 == 0) "#FFFFFF" else "#F9F9F9"
            val unitDollars = formatMoney(item.unitPrice, snapshot.currencyCode)
            val totalDollars = formatMoney(item.total, snapshot.currencyCode)
            """<tr style="background-color:$bg;">
                <td style="padding:12px;border-bottom:1px solid #E0E0E0;line-height:1.5;">${escapeHtml(item.description)}</td>
                <td style="padding:12px;border-bottom:1px solid #E0E0E0;text-align:center;line-height:1.5;">${formatQty(item.quantity)}</td>
                <td style="padding:12px;border-bottom:1px solid #E0E0E0;text-align:right;line-height:1.5;">$unitDollars</td>
                <td style="padding:12px;border-bottom:1px solid #E0E0E0;text-align:right;font-weight:bold;color:$purple;line-height:1.5;">$totalDollars</td>
            </tr>"""
        }.joinToString("\n")

        val taxLabel = if (snapshot.taxRate > 0) "Tax (${(snapshot.taxRate * 100).toInt()}%)" else "Tax"

        val hasBank = snapshot.bankName.isNotBlank() || snapshot.bankAccountName.isNotBlank()
            || snapshot.bankAccountNumber.isNotBlank() || snapshot.bankBsb.isNotBlank()
        val paymentSection = if (hasBank) """
            <div class="payment-section">
                <h3>PAYMENT DETAILS</h3>
                <table class="payment-table">
                    ${if (snapshot.bankName.isNotBlank()) """<tr><td class="payment-label">Bank Name:</td><td class="payment-value">${escapeHtml(snapshot.bankName)}</td></tr>""" else ""}
                    ${if (snapshot.bankAccountName.isNotBlank()) """<tr><td class="payment-label">Account Name:</td><td class="payment-value">${escapeHtml(snapshot.bankAccountName)}</td></tr>""" else ""}
                    ${if (snapshot.bankAccountNumber.isNotBlank()) """<tr><td class="payment-label">Account Number:</td><td class="payment-value">${escapeHtml(snapshot.bankAccountNumber)}</td></tr>""" else ""}
                    ${if (snapshot.bankBsb.isNotBlank()) """<tr><td class="payment-label">BSB:</td><td class="payment-value">${escapeHtml(snapshot.bankBsb)}</td></tr>""" else ""}
                </table>
            </div>
        """.trimIndent() else ""

        val notesSection = if (snapshot.notes.isNotBlank()) """
            <div class="payment-section">
                <h3>NOTES</h3>
                <p style="margin:0;padding:8px 0;font-size:10pt;line-height:1.6;">${escapeHtml(snapshot.notes)}</p>
            </div>
        """.trimIndent() else ""

        return """<!DOCTYPE html>
<html><head><meta charset="UTF-8"/>
<link rel="stylesheet" href="invoice-styles-refined.css"/>
<style>
@page { size: A4; margin: 15mm 15mm 15mm 15mm; }
body { font-family: 'Segoe UI', Arial, sans-serif; font-size: 10pt; color: #000000; margin: 0; padding: 0; line-height: 1.6; }
</style>
</head><body>

<div class="invoice-container">

<!-- HEADER SECTION - 60px height with purple background -->
<div class="invoice-header">
    <table style="width:100%;border-collapse:collapse;">
    <tr>
      <td class="company-info">
        $logoHtml
        ${if (snapshot.businessName.isNotBlank()) """<h1>${escapeHtml(snapshot.businessName)}</h1>""" else ""}
        <div class="company-details">
        ${if (snapshot.businessAddress.isNotBlank()) """${addressLines(snapshot.businessAddress)} &nbsp;|&nbsp; """ else ""}
        ${if (snapshot.businessEmail.isNotBlank()) """${escapeHtml(snapshot.businessEmail)} &nbsp;|&nbsp; """ else ""}
        ${if (snapshot.businessPhone.isNotBlank()) """${escapeHtml(snapshot.businessPhone)}""" else ""}
        ${if (snapshot.businessAbn.isNotBlank()) """<br/>ABN: ${escapeHtml(snapshot.businessAbn)}""" else ""}
        </div>
      </td>
      <td class="invoice-title">
        <div class="invoice-label">$docType</div>
        <div class="invoice-number">${escapeHtml(snapshot.invoiceNumber)}</div>
        $watermark
      </td>
    </tr>
    </table>
</div>

<!-- TWO-COLUMN LAYOUT (Bill To & Invoice Details) -->
<div class="details-section">
    <table style="width:100%;border-collapse:separate;border-spacing:8px 0;">
    <tr>
      <td class="card" style="width:50%;">
        <div class="card-header">BILL TO</div>
        ${if (snapshot.customerName.isNotBlank()) """<div class="card-name">${escapeHtml(snapshot.customerName)}</div>""" else ""}
        ${if (snapshot.customerAddress.isNotBlank()) """<div class="card-detail">${addressLines(snapshot.customerAddress)}</div>""" else ""}
        ${if (!snapshot.customerEmail.isNullOrBlank()) """<div class="card-detail">${escapeHtml(snapshot.customerEmail)}</div>""" else ""}
      </td>
      <td class="card" style="width:50%;">
        <div class="card-header">INVOICE DETAILS</div>
        <div class="card-detail"><strong>Invoice #:</strong> ${escapeHtml(snapshot.invoiceNumber)}</div>
        <div class="card-detail"><strong>Date:</strong> ${formatDate(snapshot.date)}</div>
        <div class="card-detail"><strong>Due Date:</strong> ${formatDate(snapshot.dueDate)}</div>
        <div class="card-detail"><strong>Currency:</strong> ${escapeHtml(snapshot.currencyCode)}</div>
        ${if (snapshot.invoiceStatus.isNotBlank()) """<div class="card-detail"><strong>Status:</strong> ${escapeHtml(snapshot.invoiceStatus)}</div>""" else ""}
      </td>
    </tr>
    </table>
</div>

${if (snapshot.headerText.isNotBlank()) """<p style="margin-bottom:12px;font-size:9pt;color:#666666;font-style:italic;">${escapeHtml(snapshot.headerText)}</p>""" else ""}

<!-- ITEMS TABLE - Strict grid matching Canvas -->
<table class="items-table">
  <thead>
    <tr>
      <th class="col-description">DESCRIPTION</th>
      <th class="col-quantity">QTY</th>
      <th class="col-unit-price">UNIT PRICE</th>
      <th class="col-amount">AMOUNT</th>
    </tr>
  </thead>
  <tbody>
    $itemRows
  </tbody>
</table>

<!-- TOTALS SECTION - Typography-driven -->
<div class="totals-section">
    <table class="totals-table">
      <tr>
        <td class="label">Subtotal:</td>
        <td class="amount">${formatMoney(snapshot.subtotal, snapshot.currencyCode)}</td>
      </tr>
      <tr>
        <td class="label">$taxLabel:</td>
        <td class="amount">${formatMoney(snapshot.taxAmount, snapshot.currencyCode)}</td>
      </tr>
      <tr class="totals-divider">
        <td colspan="2" style="padding-top:8px;"></td>
      </tr>
      <tr class="total-due">
        <td class="label">TOTAL DUE:</td>
        <td class="amount">${formatMoney(snapshot.totalAmount, snapshot.currencyCode)}</td>
      </tr>
      <tr class="totals-underline">
        <td colspan="2"></td>
      </tr>
    </table>
</div>

$paymentSection
$notesSection

${if (snapshot.footerText.isNotBlank()) """
<div class="invoice-footer">
    <div class="footer-thank-you">Thank you for your business!</div>
    <div class="footer-contact">${escapeHtml(snapshot.footerText)}</div>
</div>
""" else """
<div class="invoice-footer">
    <div class="footer-thank-you">Thank you for your business!</div>
    ${if (snapshot.businessEmail.isNotBlank() || snapshot.businessPhone.isNotBlank()) """<div class="footer-contact">
        ${if (snapshot.businessEmail.isNotBlank()) """Email: ${escapeHtml(snapshot.businessEmail)}""" else ""}
        ${if (snapshot.businessEmail.isNotBlank() && snapshot.businessPhone.isNotBlank()) """ &nbsp;|&nbsp; """ else ""}
        ${if (snapshot.businessPhone.isNotBlank()) """Phone: ${escapeHtml(snapshot.businessPhone)}""" else ""}
    </div>""" else ""}
</div>
"""}

</div>

</body></html>"""
    }

    // -------------------------------------------------------------------------
    // Template 9: PROFESSIONAL PLUS — Sidebar branding, signature line,
    //             geometric accents. Highest quality Phase 1 template.
    // Colors: Dark Charcoal #1A1A2E + Electric Teal #00C9A7 + Gold #FFD700
    // -------------------------------------------------------------------------

    private fun generateProfessionalPlusTemplate(snapshot: InvoiceSnapshot, isQuote: Boolean): String {
        val charcoal  = "#1A1A2E"    // primary — dark charcoal sidebar
        val teal      = "#00C9A7"    // accent — electric teal
        val gold      = "#E9B84A"    // secondary accent — warm gold
        val lightGray = "#F8F9FA"    // surface background
        val mutedGray = "#F1F3F5"    // muted bg for secondary sections
        val textDark  = "#1A1A2E"    // body text
        val textGray  = "#6C757D"    // muted text
        val border    = "#DEE2E6"    // subtle borders

        val showSignature = settings?.showSignatureField ?: true
        val docType = if (isQuote) "QUOTE" else "INVOICE"
        val watermark = buildStatusWatermark(snapshot.invoiceStatus)
        val logoHtml = if (!snapshot.logoBase64.isNullOrBlank())
            """<img src="data:image/png;base64,${snapshot.logoBase64}" style="max-height:60px;max-width:140px;margin-bottom:12px;" alt="logo"/>"""
        else ""

        // Phase 2: SVG decorative accent line between header and content
        val accentDivider = try {
            val uri = SvgElementsProvider.accentLineDataUri(teal, 600, 4)
            """<img src="$uri" style="width:100%;height:4px;display:block;" alt=""/>"""
        } catch (_: Exception) { "" }

        // Phase 2: Dots accent for sidebar branding
        val dotsAccent = try {
            val uri = SvgElementsProvider.dotsAccentDataUri(teal, 3, 3)
            """<img src="$uri" style="height:10px;margin-top:6px;" alt=""/>"""
        } catch (_: Exception) { "" }

        // Item rows with refined Phase 2 line-heights (1.6) and spacing
        val itemRows = snapshot.items.mapIndexed { i, item ->
            val bg    = if (i % 2 == 0) "#FFFFFF" else lightGray
            val unitD = formatMoney(item.unitPrice, snapshot.currencyCode)
            val totD  = formatMoney(item.total, snapshot.currencyCode)
            """<tr style="background-color:$bg;">
                <td style="padding:11px 14px;border-bottom:1px solid $border;line-height:1.6;word-wrap:break-word;font-size:10pt;">${escapeHtml(item.description)}</td>
                <td style="padding:11px 14px;border-bottom:1px solid $border;text-align:center;line-height:1.6;font-size:10pt;color:$textGray;">${formatQty(item.quantity)}</td>
                <td style="padding:11px 14px;border-bottom:1px solid $border;text-align:right;line-height:1.6;font-size:10pt;color:$textGray;">$unitD</td>
                <td style="padding:11px 14px;border-bottom:1px solid $border;text-align:right;font-weight:700;color:$charcoal;line-height:1.6;font-size:10pt;">$totD</td>
            </tr>"""
        }.joinToString("\n")

        // Totals rows with clear hierarchy
        val taxPct = snapshot.taxRate * 100
        val taxLabel = if (snapshot.taxRate > 0) {
            val fmt = if (taxPct == taxPct.toLong().toDouble()) "${taxPct.toLong()}%"
                      else "${String.format("%.2f", taxPct).trimEnd('0').trimEnd('.')}%"
            "Tax ($fmt)"
        } else "Tax"

        // Phase 2: Enhanced payment section with method badge
        val paymentSection = buildEnhancedPaymentSection(snapshot, charcoal, teal)

        val notesSection = if (snapshot.notes.isNotBlank()) """
<table width="100%" style="border-collapse:collapse;margin-top:18px;">
  <tr><td style="padding:10px 14px;background-color:$mutedGray;font-weight:700;font-size:10pt;color:$charcoal;border-left:4px solid $teal;">Notes</td></tr>
  <tr><td style="padding:10px 14px;font-size:9.5pt;line-height:1.6;word-wrap:break-word;color:$textGray;">${escapeHtml(snapshot.notes)}</td></tr>
</table>""".trimIndent() else ""

        // Phase 2: Signature section using shared helper
        val signatureSection = buildSignatureSection(teal, textGray, showSignature)

        // Phase 2: Professional multi-column footer
        val footer = buildProfessionalFooter(snapshot, charcoal, teal, mutedGray)

        return """<!DOCTYPE html>
<html><head><meta charset="UTF-8"/>
<style>
@page { margin: 12mm 14mm 12mm 14mm; }
body { font-family: Arial, Helvetica, 'Segoe UI', sans-serif; font-size: 10pt; color: $textDark; margin: 0; padding: 0; line-height: 1.6; }
table { border-collapse: collapse; }
td, th { word-wrap: break-word; }
</style>
</head><body>

<!-- ═══ HEADER: two-tone (dark sidebar | light title area) ═══ -->
<table width="100%" style="margin-bottom:0;">
<tr>
  <!-- LEFT: dark sidebar branding -->
  <td width="38%" style="background-color:$charcoal;padding:22px 20px 22px 24px;vertical-align:top;">
    $logoHtml
    ${if (snapshot.businessName.isNotBlank()) """<div style="font-size:16pt;font-weight:700;color:#FFFFFF;line-height:1.2;margin-bottom:6px;">${escapeHtml(snapshot.businessName)}</div>""" else ""}
    ${if (snapshot.subheaderText.isNotBlank()) """<div style="font-size:9.5pt;color:#94A3B8;margin-bottom:8px;line-height:1.4;">${escapeHtml(snapshot.subheaderText)}</div>""" else ""}
    $dotsAccent
    <div style="font-size:8.5pt;color:#94A3B8;line-height:1.6;margin-top:8px;">
    ${if (snapshot.businessAddress.isNotBlank()) """<div>${addressLines(snapshot.businessAddress)}</div>""" else ""}
    ${if (snapshot.businessEmail.isNotBlank()) """<div>${escapeHtml(snapshot.businessEmail)}</div>""" else ""}
    ${if (snapshot.businessPhone.isNotBlank()) """<div>${escapeHtml(snapshot.businessPhone)}</div>""" else ""}
    ${if (snapshot.businessAbn.isNotBlank()) """<div>ABN: ${escapeHtml(snapshot.businessAbn)}</div>""" else ""}
    </div>
  </td>
  <!-- RIGHT: document title and meta -->
  <td width="62%" style="background-color:$lightGray;padding:22px 24px 22px 20px;vertical-align:top;text-align:right;">
    <div style="font-size:28pt;font-weight:700;letter-spacing:3px;color:$charcoal;">$docType</div>
    <div style="font-size:10pt;color:$teal;font-weight:700;margin-top:4px;letter-spacing:1px;">${escapeHtml(snapshot.invoiceNumber)}</div>
    $watermark
    <!-- Invoice meta: date, due, status -->
    <table style="margin-top:14px;margin-left:auto;">
      <tr>
        <td style="padding:4px 10px 4px 0;font-size:9pt;color:$textGray;text-align:right;font-weight:700;">Date:</td>
        <td style="padding:4px 0;font-size:9pt;color:$textDark;text-align:right;">${formatDate(snapshot.date)}</td>
      </tr>
      <tr>
        <td style="padding:4px 10px 4px 0;font-size:9pt;color:$textGray;text-align:right;font-weight:700;">Due Date:</td>
        <td style="padding:4px 0;font-size:9pt;color:$teal;text-align:right;font-weight:700;">${formatDate(snapshot.dueDate)}</td>
      </tr>
      ${if (snapshot.invoiceStatus.isNotBlank()) """<tr><td style="padding:4px 10px 4px 0;font-size:9pt;color:$textGray;text-align:right;font-weight:700;">Status:</td><td style="padding:4px 0;font-size:9pt;color:$textDark;text-align:right;">${escapeHtml(snapshot.invoiceStatus)}</td></tr>""" else ""}
      <tr>
        <td style="padding:4px 10px 4px 0;font-size:9pt;color:$textGray;text-align:right;font-weight:700;">Currency:</td>
        <td style="padding:4px 0;font-size:9pt;color:$textDark;text-align:right;">${escapeHtml(snapshot.currencyCode)}</td>
      </tr>
    </table>
  </td>
</tr>
</table>

<!-- Phase 2: SVG accent divider replacing plain color bar -->
$accentDivider

<!-- BILL TO + INVOICE DETAILS (2-column, Phase 2 refined) -->
<table width="100%" style="margin-top:16px;margin-bottom:14px;">
<tr>
  <td width="50%" style="vertical-align:top;padding-right:10px;">
    <table width="100%" style="background-color:$mutedGray;border-left:4px solid $teal;">
      <tr><td style="padding:9px 14px;font-weight:700;font-size:10pt;color:$charcoal;text-transform:uppercase;letter-spacing:0.5px;">&#x1F4CB; Bill To</td></tr>
      ${if (snapshot.customerName.isNotBlank()) """<tr><td style="padding:6px 14px;font-weight:700;font-size:11pt;color:$charcoal;line-height:1.2;">${escapeHtml(snapshot.customerName)}</td></tr>""" else ""}
      ${if (snapshot.customerAddress.isNotBlank()) """<tr><td style="padding:4px 14px 6px 14px;font-size:9pt;color:$textGray;line-height:1.6;">${addressLines(snapshot.customerAddress)}</td></tr>""" else ""}
      ${if (!snapshot.customerEmail.isNullOrBlank()) """<tr><td style="padding:4px 14px 6px 14px;font-size:9pt;color:$textGray;">${escapeHtml(snapshot.customerEmail)}</td></tr>""" else ""}
    </table>
  </td>
  <td width="50%" style="vertical-align:top;padding-left:10px;">
    ${if (snapshot.headerText.isNotBlank()) """<table width="100%" style="background-color:$lightGray;border-left:3px solid $border;">
      <tr><td style="padding:9px 14px;font-weight:700;font-size:10pt;color:$charcoal;">Message</td></tr>
      <tr><td style="padding:6px 14px;font-size:9.5pt;color:$textGray;font-style:italic;line-height:1.6;">${escapeHtml(snapshot.headerText)}</td></tr>
    </table>""" else ""}
  </td>
</tr>
</table>

<!-- LINE ITEMS TABLE (Phase 2: refined column widths and spacing) -->
<table width="100%" style="border-collapse:collapse;margin-bottom:0;">
  <tr style="background-color:$charcoal;color:#FFFFFF;">
    <th style="padding:12pt 14px;text-align:left;font-size:9.5pt;letter-spacing:0.5px;">DESCRIPTION</th>
    <th style="padding:12pt 14px;text-align:center;font-size:9.5pt;width:9%;letter-spacing:0.5px;">QTY</th>
    <th style="padding:12pt 14px;text-align:right;font-size:9.5pt;width:17%;letter-spacing:0.5px;">UNIT PRICE</th>
    <th style="padding:12pt 14px;text-align:right;font-size:9.5pt;width:17%;letter-spacing:0.5px;">AMOUNT</th>
  </tr>
  $itemRows
  <!-- Subtotal / Tax rows -->
  <tr>
    <td colspan="3" style="padding:8px 14px;text-align:right;font-size:9.5pt;color:$textGray;">Subtotal</td>
    <td style="padding:8px 14px;text-align:right;font-size:9.5pt;color:$textDark;">${formatMoney(snapshot.subtotal, snapshot.currencyCode)}</td>
  </tr>
  <tr style="background-color:$lightGray;">
    <td colspan="3" style="padding:8px 14px;text-align:right;font-size:9.5pt;color:$textGray;">$taxLabel</td>
    <td style="padding:8px 14px;text-align:right;font-size:9.5pt;color:$textDark;">${formatMoney(snapshot.taxAmount, snapshot.currencyCode)}</td>
  </tr>
</table>

<!-- TOTAL DUE BAND — gold accent on dark background -->
<table width="100%" style="background-color:$charcoal;margin-bottom:16px;">
<tr>
  <td width="65%" style="padding:14px 20px;"></td>
  <td style="padding:14px 20px;text-align:right;">
    <div style="font-size:9pt;font-weight:700;color:#94A3B8;text-transform:uppercase;letter-spacing:1px;margin-bottom:4px;">Total Due</div>
    <div style="font-size:22pt;font-weight:700;color:$gold;">${formatMoney(snapshot.totalAmount, snapshot.currencyCode)}</div>
  </td>
</tr>
</table>

$paymentSection
$notesSection

<!-- Phase 2: Signature section (toggleable) -->
$signatureSection

<!-- Phase 2: Professional multi-column footer -->
$footer

</body></html>"""
    }

    // -------------------------------------------------------------------------
    // PDF conversion
    // -------------------------------------------------------------------------

    private fun convertHtmlToPdf(
        htmlContent: String,
        baseFileName: String
    ): File {
        val pdfFileName = baseFileName.replace(".pdf", "_html.pdf")
        val file = File(context.filesDir, "documents/$pdfFileName")
        file.parentFile?.mkdirs()

        try {
            val outputStream = FileOutputStream(file)
            val converterProperties = com.itextpdf.html2pdf.ConverterProperties()

            com.itextpdf.html2pdf.HtmlConverter.convertToPdf(
                htmlContent,
                outputStream,
                converterProperties
            )
            outputStream.flush()
            outputStream.close()

            // ✅ Log success with file size for verification
            Timber.d("✅ PDF created: ${file.name}, size: ${file.length()} bytes")
        } catch (e: Exception) {
            Timber.e(e, "❌ PDF conversion failed")
            throw e
        }

        return file
    }
}
