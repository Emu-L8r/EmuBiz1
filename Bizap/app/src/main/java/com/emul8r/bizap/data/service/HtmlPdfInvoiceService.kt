package com.emul8r.bizap.data.service

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.emul8r.bizap.domain.model.InvoiceSnapshot
import com.emul8r.bizap.domain.model.InvoiceSettings
import com.emul8r.bizap.domain.model.HtmlInvoiceStyle
import com.emul8r.bizap.domain.service.PdfGenerationService
import com.emul8r.bizap.utils.DocumentNamingUtils
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

    private fun generateHtmlContent(snapshot: InvoiceSnapshot, isQuote: Boolean): String {
        val clean = validateAndCleanInvoiceData(snapshot)
        val layout = settings?.selectedPageLayout
        val style = settings?.selectedHtmlStyle ?: HtmlInvoiceStyle.MODERN

        Timber.d("📐 Generating HTML with style=$style, layout=$layout")

        // PHASE 3: Route based on page layout if set
        if (layout != null) {
            Timber.d("✅ Using layout-aware generation: ${layout.name}")
            val layoutFactory = com.emul8r.bizap.data.service.layout.PageLayoutFactory
            val manager = com.emul8r.bizap.data.service.layout.PageLayoutManager()
            val colorScheme = manager.extractColorScheme(settings!!)
            val layoutProvider = layoutFactory.createLayout(layout)
            return layoutProvider.buildInvoiceHtml(clean, isQuote, colorScheme)
        }

        // Fall back to style-based generation (existing)
        Timber.d("⚠️  Using style-based generation (legacy mode)")
        return when (style) {
            HtmlInvoiceStyle.MODERN    -> generateModernTemplate(clean, isQuote)
            HtmlInvoiceStyle.MINIMAL   -> generateMinimalTemplate(clean, isQuote)
            HtmlInvoiceStyle.CORPORATE -> generateCorporateTemplate(clean, isQuote)
            HtmlInvoiceStyle.CREATIVE  -> generateCreativeTemplate(clean, isQuote)
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
