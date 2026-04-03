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
    // HTML generation — selects the correct template based on settings
    // -------------------------------------------------------------------------

    private fun generateHtmlContent(snapshot: InvoiceSnapshot, isQuote: Boolean): String {
        val style = settings?.selectedHtmlStyle ?: HtmlInvoiceStyle.MODERN
        return when (style) {
            HtmlInvoiceStyle.MODERN    -> generateModernTemplate(snapshot, isQuote)
            HtmlInvoiceStyle.MINIMAL   -> generateMinimalTemplate(snapshot, isQuote)
            HtmlInvoiceStyle.CORPORATE -> generateCorporateTemplate(snapshot, isQuote)
            HtmlInvoiceStyle.CREATIVE  -> generateCreativeTemplate(snapshot, isQuote)
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
                <td style="padding:8px 10px;border-bottom:1px solid #e0e0e0;">${escapeHtml(item.description)}</td>
                <td style="padding:8px 10px;border-bottom:1px solid #e0e0e0;text-align:center;">${formatQty(item.quantity)}</td>
                <td style="padding:8px 10px;border-bottom:1px solid #e0e0e0;text-align:right;">$unitDollars</td>
                <td style="padding:8px 10px;border-bottom:1px solid #e0e0e0;text-align:right;font-weight:bold;color:$amountColor;">$totalDollars</td>
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
                <td colspan="3" style="padding:7px 10px;text-align:right;color:$labelColor;">Subtotal</td>
                <td style="padding:7px 10px;text-align:right;">${formatMoney(snapshot.subtotal, snapshot.currencyCode)}</td>
            </tr>
            <tr>
                <td colspan="3" style="padding:7px 10px;text-align:right;color:$labelColor;">$taxLabel</td>
                <td style="padding:7px 10px;text-align:right;">${formatMoney(snapshot.taxAmount, snapshot.currencyCode)}</td>
            </tr>
            <tr style="background-color:$totalBg;">
                <td colspan="3" style="padding:10px;text-align:right;font-weight:bold;font-size:13pt;color:$totalFg;">TOTAL DUE</td>
                <td style="padding:10px;text-align:right;font-weight:bold;font-size:13pt;color:$totalFg;">${formatMoney(snapshot.totalAmount, snapshot.currencyCode)}</td>
            </tr>
        """.trimIndent()
    }

    private fun buildPaymentSection(snapshot: InvoiceSnapshot, headingColor: String, borderColor: String): String {
        val hasBank = snapshot.bankName.isNotBlank() || snapshot.bankAccountName.isNotBlank()
            || snapshot.bankAccountNumber.isNotBlank() || snapshot.bankBsb.isNotBlank()
        if (!hasBank) return ""
        return """
            <table width="100%" style="border-collapse:collapse;margin-top:14px;">
                <tr><td colspan="2" style="padding:8px 10px;background-color:#f5f5f5;font-weight:bold;font-size:11pt;color:$headingColor;border-left:4px solid $borderColor;">PAYMENT DETAILS</td></tr>
                ${if (snapshot.bankName.isNotBlank()) """<tr><td style="padding:5px 10px;font-weight:bold;width:40%;">Bank</td><td style="padding:5px 10px;">${escapeHtml(snapshot.bankName)}</td></tr>""" else ""}
                ${if (snapshot.bankAccountName.isNotBlank()) """<tr><td style="padding:5px 10px;font-weight:bold;">Account Name</td><td style="padding:5px 10px;">${escapeHtml(snapshot.bankAccountName)}</td></tr>""" else ""}
                ${if (snapshot.bankAccountNumber.isNotBlank()) """<tr><td style="padding:5px 10px;font-weight:bold;">Account Number</td><td style="padding:5px 10px;">${escapeHtml(snapshot.bankAccountNumber)}</td></tr>""" else ""}
                ${if (snapshot.bankBsb.isNotBlank()) """<tr><td style="padding:5px 10px;font-weight:bold;">BSB</td><td style="padding:5px 10px;">${escapeHtml(snapshot.bankBsb)}</td></tr>""" else ""}
            </table>
        """.trimIndent()
    }

    private fun buildNotesSection(snapshot: InvoiceSnapshot, headingColor: String, borderColor: String): String {
        if (snapshot.notes.isBlank()) return ""
        return """
            <table width="100%" style="border-collapse:collapse;margin-top:14px;">
                <tr><td style="padding:8px 10px;background-color:#f5f5f5;font-weight:bold;font-size:11pt;color:$headingColor;border-left:4px solid $borderColor;">NOTES</td></tr>
                <tr><td style="padding:8px 10px;font-size:10pt;">${escapeHtml(snapshot.notes)}</td></tr>
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
body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; font-size: 10pt; color: #333333; margin: 0; padding: 0; }
table { border-collapse: collapse; }
</style>
</head><body>

<!-- HEADER -->
<table width="100%" style="background-color:$primary;color:#ffffff;margin-bottom:16px;">
<tr>
  <td style="padding:18px 20px;vertical-align:top;">
    $logoHtml
    <div style="font-size:18pt;font-weight:bold;color:#ffffff;">${escapeHtml(snapshot.businessName)}</div>
    ${if (snapshot.businessAddress.isNotBlank()) """<div style="font-size:9pt;color:#e0d8f0;margin-top:4px;">${addressLines(snapshot.businessAddress)}</div>""" else ""}
    ${if (snapshot.businessEmail.isNotBlank()) """<div style="font-size:9pt;color:#e0d8f0;">${escapeHtml(snapshot.businessEmail)}</div>""" else ""}
    ${if (snapshot.businessPhone.isNotBlank()) """<div style="font-size:9pt;color:#e0d8f0;">${escapeHtml(snapshot.businessPhone)}</div>""" else ""}
    ${if (snapshot.businessAbn.isNotBlank()) """<div style="font-size:9pt;color:#e0d8f0;">ABN: ${escapeHtml(snapshot.businessAbn)}</div>""" else ""}
  </td>
  <td style="padding:18px 20px;text-align:right;vertical-align:top;">
    <div style="font-size:22pt;font-weight:bold;letter-spacing:3px;color:#ffffff;">$docType</div>
    $watermark
  </td>
</tr>
</table>

<!-- INVOICE META + BILL TO -->
<table width="100%" style="margin-bottom:14px;">
<tr>
  <td width="50%" style="vertical-align:top;padding-right:10px;">
    <table width="100%" style="background-color:$lightPurple;border-left:4px solid $primary;">
      <tr><td colspan="2" style="padding:7px 10px;font-weight:bold;font-size:11pt;color:$primary;">INVOICE DETAILS</td></tr>
      <tr><td style="padding:4px 10px;font-weight:bold;width:45%;">Invoice #</td><td style="padding:4px 10px;">${escapeHtml(snapshot.invoiceNumber)}</td></tr>
      <tr><td style="padding:4px 10px;font-weight:bold;">Date</td><td style="padding:4px 10px;">${formatDate(snapshot.date)}</td></tr>
      <tr><td style="padding:4px 10px;font-weight:bold;">Due Date</td><td style="padding:4px 10px;">${formatDate(snapshot.dueDate)}</td></tr>
      <tr><td style="padding:4px 10px;font-weight:bold;">Currency</td><td style="padding:4px 10px;">${escapeHtml(snapshot.currencyCode)}</td></tr>
      ${if (snapshot.invoiceStatus.isNotBlank()) """<tr><td style="padding:4px 10px;font-weight:bold;">Status</td><td style="padding:4px 10px;">${escapeHtml(snapshot.invoiceStatus)}</td></tr>""" else ""}
    </table>
  </td>
  <td width="50%" style="vertical-align:top;padding-left:10px;">
    <table width="100%" style="background-color:#fafafa;border-left:4px solid #aaaaaa;">
      <tr><td style="padding:7px 10px;font-weight:bold;font-size:11pt;color:$primary;">BILL TO</td></tr>
      <tr><td style="padding:4px 10px;font-weight:bold;font-size:11pt;">${escapeHtml(snapshot.customerName)}</td></tr>
      ${if (snapshot.customerAddress.isNotBlank()) """<tr><td style="padding:4px 10px;font-size:9pt;">${addressLines(snapshot.customerAddress)}</td></tr>""" else ""}
      ${if (!snapshot.customerEmail.isNullOrBlank()) """<tr><td style="padding:4px 10px;font-size:9pt;">${escapeHtml(snapshot.customerEmail)}</td></tr>""" else ""}
    </table>
  </td>
</tr>
</table>

${if (snapshot.headerText.isNotBlank()) """<p style="margin-bottom:10px;font-style:italic;color:#555555;">${escapeHtml(snapshot.headerText)}</p>""" else ""}

<!-- LINE ITEMS TABLE -->
<table width="100%" style="border-collapse:collapse;margin-bottom:4px;">
  <tr style="background-color:$primary;color:#ffffff;">
    <th style="padding:9px 10px;text-align:left;font-size:10pt;">Description</th>
    <th style="padding:9px 10px;text-align:center;font-size:10pt;width:10%;">Qty</th>
    <th style="padding:9px 10px;text-align:right;font-size:10pt;width:18%;">Unit Price</th>
    <th style="padding:9px 10px;text-align:right;font-size:10pt;width:18%;">Total</th>
  </tr>
  $itemRows
  <tr><td colspan="4" style="padding:0;"></td></tr>
  $totalRows
</table>

$paymentSection
$notesSection

${if (snapshot.footerText.isNotBlank()) """<p style="margin-top:16px;text-align:center;font-size:9pt;color:#888888;border-top:1px solid #dddddd;padding-top:8px;">${escapeHtml(snapshot.footerText)}</p>""" else ""}

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
body { font-family: Arial, Helvetica, sans-serif; font-size: 10pt; color: #1a1a1a; margin: 0; padding: 0; }
table { border-collapse: collapse; }
</style>
</head><body>

<!-- HEADER -->
<table width="100%" style="border-bottom:2px solid #1a1a1a;margin-bottom:18px;padding-bottom:12px;">
<tr>
  <td style="padding-bottom:8px;vertical-align:top;">
    $logoHtml
    <div style="font-size:17pt;font-weight:bold;color:#1a1a1a;margin-top:4px;">${escapeHtml(snapshot.businessName)}</div>
    ${if (snapshot.businessAddress.isNotBlank()) """<div style="font-size:9pt;color:#555555;margin-top:3px;">${addressLines(snapshot.businessAddress)}</div>""" else ""}
    ${if (snapshot.businessEmail.isNotBlank()) """<div style="font-size:9pt;color:#555555;">${escapeHtml(snapshot.businessEmail)}</div>""" else ""}
    ${if (snapshot.businessPhone.isNotBlank()) """<div style="font-size:9pt;color:#555555;">${escapeHtml(snapshot.businessPhone)}</div>""" else ""}
    ${if (snapshot.businessAbn.isNotBlank()) """<div style="font-size:9pt;color:#555555;">ABN: ${escapeHtml(snapshot.businessAbn)}</div>""" else ""}
  </td>
  <td style="text-align:right;vertical-align:top;padding-bottom:8px;">
    <div style="font-size:24pt;font-weight:bold;letter-spacing:2px;color:#1a1a1a;">$docType</div>
    $watermark
  </td>
</tr>
</table>

<!-- INVOICE META + BILL TO -->
<table width="100%" style="margin-bottom:16px;">
<tr>
  <td width="50%" style="vertical-align:top;padding-right:14px;">
    <div style="font-weight:bold;font-size:10pt;margin-bottom:6px;text-transform:uppercase;letter-spacing:1px;border-bottom:1px solid #1a1a1a;padding-bottom:3px;">Invoice Details</div>
    <table width="100%">
      <tr><td style="padding:3px 0;font-weight:bold;width:45%;">Invoice #</td><td style="padding:3px 0;">${escapeHtml(snapshot.invoiceNumber)}</td></tr>
      <tr><td style="padding:3px 0;font-weight:bold;">Date</td><td style="padding:3px 0;">${formatDate(snapshot.date)}</td></tr>
      <tr><td style="padding:3px 0;font-weight:bold;">Due Date</td><td style="padding:3px 0;">${formatDate(snapshot.dueDate)}</td></tr>
      <tr><td style="padding:3px 0;font-weight:bold;">Currency</td><td style="padding:3px 0;">${escapeHtml(snapshot.currencyCode)}</td></tr>
      ${if (snapshot.invoiceStatus.isNotBlank()) """<tr><td style="padding:3px 0;font-weight:bold;">Status</td><td style="padding:3px 0;">${escapeHtml(snapshot.invoiceStatus)}</td></tr>""" else ""}
    </table>
  </td>
  <td width="50%" style="vertical-align:top;padding-left:14px;">
    <div style="font-weight:bold;font-size:10pt;margin-bottom:6px;text-transform:uppercase;letter-spacing:1px;border-bottom:1px solid #1a1a1a;padding-bottom:3px;">Bill To</div>
    <div style="font-weight:bold;font-size:11pt;">${escapeHtml(snapshot.customerName)}</div>
    ${if (snapshot.customerAddress.isNotBlank()) """<div style="font-size:9pt;color:#555555;margin-top:3px;">${addressLines(snapshot.customerAddress)}</div>""" else ""}
    ${if (!snapshot.customerEmail.isNullOrBlank()) """<div style="font-size:9pt;color:#555555;">${escapeHtml(snapshot.customerEmail)}</div>""" else ""}
  </td>
</tr>
</table>

${if (snapshot.headerText.isNotBlank()) """<p style="margin-bottom:10px;font-size:9pt;color:#555555;font-style:italic;">${escapeHtml(snapshot.headerText)}</p>""" else ""}

<!-- LINE ITEMS TABLE -->
<table width="100%" style="margin-bottom:4px;">
  <tr style="background-color:#1a1a1a;color:#ffffff;">
    <th style="padding:8px 10px;text-align:left;">Description</th>
    <th style="padding:8px 10px;text-align:center;width:10%;">Qty</th>
    <th style="padding:8px 10px;text-align:right;width:18%;">Unit Price</th>
    <th style="padding:8px 10px;text-align:right;width:18%;">Total</th>
  </tr>
  $itemRows
  <tr><td colspan="4" style="padding:0;"></td></tr>
  $totalRows
</table>

$paymentSection
$notesSection

${if (snapshot.footerText.isNotBlank()) """<p style="margin-top:18px;text-align:center;font-size:9pt;color:#888888;border-top:1px solid #cccccc;padding-top:8px;">${escapeHtml(snapshot.footerText)}</p>""" else ""}

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
body { font-family: Georgia, 'Times New Roman', Times, serif; font-size: 10pt; color: #222222; margin: 0; padding: 0; }
table { border-collapse: collapse; }
</style>
</head><body>

<!-- HEADER BAR -->
<table width="100%" style="background-color:$primary;margin-bottom:0;">
<tr>
  <td style="padding:16px 20px;vertical-align:middle;">
    $logoHtml
    <div style="font-size:17pt;font-weight:bold;color:#ffffff;margin-top:2px;">${escapeHtml(snapshot.businessName)}</div>
  </td>
  <td style="padding:16px 20px;text-align:right;vertical-align:middle;">
    <div style="font-size:20pt;font-weight:bold;letter-spacing:4px;color:#ffffff;">$docType</div>
    $watermark
  </td>
</tr>
</table>

<!-- SUBHEADER BAR -->
<table width="100%" style="background-color:$offWhite;border-bottom:2px solid $primary;margin-bottom:16px;">
<tr>
  <td style="padding:8px 20px;font-size:9pt;color:#555555;vertical-align:top;">
    ${if (snapshot.businessAddress.isNotBlank()) """${addressLines(snapshot.businessAddress)} &nbsp;""" else ""}
    ${if (snapshot.businessEmail.isNotBlank()) """| ${escapeHtml(snapshot.businessEmail)} """ else ""}
    ${if (snapshot.businessPhone.isNotBlank()) """| ${escapeHtml(snapshot.businessPhone)} """ else ""}
    ${if (snapshot.businessAbn.isNotBlank()) """| ABN: ${escapeHtml(snapshot.businessAbn)}""" else ""}
  </td>
</tr>
</table>

<!-- INVOICE META + BILL TO -->
<table width="100%" style="margin-bottom:16px;border:1px solid #dddddd;">
<tr>
  <td width="50%" style="vertical-align:top;padding:12px 14px;border-right:1px solid #dddddd;">
    <div style="font-weight:bold;font-size:11pt;color:$primary;margin-bottom:8px;text-transform:uppercase;letter-spacing:1px;">Invoice Information</div>
    <table width="100%">
      <tr><td style="padding:3px 0;font-weight:bold;width:45%;color:#444444;">Invoice No.</td><td style="padding:3px 0;">${escapeHtml(snapshot.invoiceNumber)}</td></tr>
      <tr><td style="padding:3px 0;font-weight:bold;color:#444444;">Invoice Date</td><td style="padding:3px 0;">${formatDate(snapshot.date)}</td></tr>
      <tr><td style="padding:3px 0;font-weight:bold;color:#444444;">Due Date</td><td style="padding:3px 0;">${formatDate(snapshot.dueDate)}</td></tr>
      <tr><td style="padding:3px 0;font-weight:bold;color:#444444;">Currency</td><td style="padding:3px 0;">${escapeHtml(snapshot.currencyCode)}</td></tr>
      ${if (snapshot.invoiceStatus.isNotBlank()) """<tr><td style="padding:3px 0;font-weight:bold;color:#444444;">Status</td><td style="padding:3px 0;">${escapeHtml(snapshot.invoiceStatus)}</td></tr>""" else ""}
    </table>
  </td>
  <td width="50%" style="vertical-align:top;padding:12px 14px;">
    <div style="font-weight:bold;font-size:11pt;color:$primary;margin-bottom:8px;text-transform:uppercase;letter-spacing:1px;">Billed To</div>
    <div style="font-weight:bold;font-size:12pt;">${escapeHtml(snapshot.customerName)}</div>
    ${if (snapshot.customerAddress.isNotBlank()) """<div style="font-size:9pt;color:#555555;margin-top:3px;">${addressLines(snapshot.customerAddress)}</div>""" else ""}
    ${if (!snapshot.customerEmail.isNullOrBlank()) """<div style="font-size:9pt;color:#555555;">${escapeHtml(snapshot.customerEmail)}</div>""" else ""}
  </td>
</tr>
</table>

${if (snapshot.headerText.isNotBlank()) """<p style="margin-bottom:10px;font-size:9pt;color:#555555;font-style:italic;">${escapeHtml(snapshot.headerText)}</p>""" else ""}

<!-- LINE ITEMS TABLE -->
<table width="100%" style="border:1px solid #dddddd;margin-bottom:4px;">
  <tr style="background-color:$primary;color:#ffffff;">
    <th style="padding:9px 12px;text-align:left;">Description</th>
    <th style="padding:9px 12px;text-align:center;width:10%;">Qty</th>
    <th style="padding:9px 12px;text-align:right;width:18%;">Unit Price</th>
    <th style="padding:9px 12px;text-align:right;width:18%;">Amount</th>
  </tr>
  $itemRows
  <tr style="border-top:2px solid $primary;"><td colspan="4" style="padding:0;"></td></tr>
  $totalRows
</table>

$paymentSection
$notesSection

${if (snapshot.footerText.isNotBlank()) """<p style="margin-top:18px;text-align:center;font-size:9pt;color:#888888;border-top:1px solid #cccccc;padding-top:8px;">${escapeHtml(snapshot.footerText)}</p>""" else ""}

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
body { font-family: 'Segoe UI', 'Trebuchet MS', Arial, sans-serif; font-size: 10pt; color: #222222; margin: 0; padding: 0; }
table { border-collapse: collapse; }
</style>
</head><body>

<!-- TWO-TONE HEADER -->
<table width="100%" style="margin-bottom:16px;">
<tr>
  <td width="55%" style="background-color:$deepBlue;padding:18px 20px;vertical-align:middle;">
    $logoHtml
    <div style="font-size:17pt;font-weight:bold;color:#ffffff;margin-top:3px;">${escapeHtml(snapshot.businessName)}</div>
    ${if (snapshot.businessAddress.isNotBlank()) """<div style="font-size:9pt;color:#b0c8e0;margin-top:3px;">${addressLines(snapshot.businessAddress)}</div>""" else ""}
    ${if (snapshot.businessEmail.isNotBlank()) """<div style="font-size:9pt;color:#b0c8e0;">${escapeHtml(snapshot.businessEmail)}</div>""" else ""}
    ${if (snapshot.businessPhone.isNotBlank()) """<div style="font-size:9pt;color:#b0c8e0;">${escapeHtml(snapshot.businessPhone)}</div>""" else ""}
    ${if (snapshot.businessAbn.isNotBlank()) """<div style="font-size:9pt;color:#b0c8e0;">ABN: ${escapeHtml(snapshot.businessAbn)}</div>""" else ""}
  </td>
  <td width="45%" style="background-color:$orange;padding:18px 20px;text-align:right;vertical-align:middle;">
    <div style="font-size:24pt;font-weight:bold;letter-spacing:3px;color:#ffffff;">$docType</div>
    $watermark
    <div style="font-size:10pt;color:#ffffff;margin-top:4px;font-weight:bold;">${escapeHtml(snapshot.invoiceNumber)}</div>
  </td>
</tr>
</table>

<!-- INVOICE META + BILL TO -->
<table width="100%" style="margin-bottom:14px;">
<tr>
  <td width="50%" style="vertical-align:top;padding-right:10px;">
    <table width="100%" style="border-left:4px solid $orange;background-color:#fff4ee;">
      <tr><td colspan="2" style="padding:7px 10px;font-weight:bold;font-size:11pt;color:$deepBlue;">INVOICE DETAILS</td></tr>
      <tr><td style="padding:4px 10px;font-weight:bold;width:45%;color:#444444;">Invoice #</td><td style="padding:4px 10px;">${escapeHtml(snapshot.invoiceNumber)}</td></tr>
      <tr><td style="padding:4px 10px;font-weight:bold;color:#444444;">Date</td><td style="padding:4px 10px;">${formatDate(snapshot.date)}</td></tr>
      <tr><td style="padding:4px 10px;font-weight:bold;color:#444444;">Due Date</td><td style="padding:4px 10px;">${formatDate(snapshot.dueDate)}</td></tr>
      <tr><td style="padding:4px 10px;font-weight:bold;color:#444444;">Currency</td><td style="padding:4px 10px;">${escapeHtml(snapshot.currencyCode)}</td></tr>
      ${if (snapshot.invoiceStatus.isNotBlank()) """<tr><td style="padding:4px 10px;font-weight:bold;color:#444444;">Status</td><td style="padding:4px 10px;">${escapeHtml(snapshot.invoiceStatus)}</td></tr>""" else ""}
    </table>
  </td>
  <td width="50%" style="vertical-align:top;padding-left:10px;">
    <table width="100%" style="border-left:4px solid $deepBlue;background-color:#eaf3ff;">
      <tr><td style="padding:7px 10px;font-weight:bold;font-size:11pt;color:$deepBlue;">BILL TO</td></tr>
      <tr><td style="padding:4px 10px;font-weight:bold;font-size:12pt;">${escapeHtml(snapshot.customerName)}</td></tr>
      ${if (snapshot.customerAddress.isNotBlank()) """<tr><td style="padding:4px 10px;font-size:9pt;color:#555555;">${addressLines(snapshot.customerAddress)}</td></tr>""" else ""}
      ${if (!snapshot.customerEmail.isNullOrBlank()) """<tr><td style="padding:4px 10px;font-size:9pt;color:#555555;">${escapeHtml(snapshot.customerEmail)}</td></tr>""" else ""}
    </table>
  </td>
</tr>
</table>

${if (snapshot.headerText.isNotBlank()) """<p style="margin-bottom:10px;font-size:9pt;color:#555555;font-style:italic;">${escapeHtml(snapshot.headerText)}</p>""" else ""}

<!-- LINE ITEMS TABLE -->
<table width="100%" style="margin-bottom:4px;">
  <tr style="background-color:$orange;color:#ffffff;">
    <th style="padding:9px 10px;text-align:left;font-size:10pt;">Description</th>
    <th style="padding:9px 10px;text-align:center;font-size:10pt;width:10%;">Qty</th>
    <th style="padding:9px 10px;text-align:right;font-size:10pt;width:18%;">Unit Price</th>
    <th style="padding:9px 10px;text-align:right;font-size:10pt;width:18%;">Total</th>
  </tr>
  $itemRows
  <tr><td colspan="4" style="padding:0;"></td></tr>
  $totalRows
</table>

$paymentSection
$notesSection

${if (snapshot.footerText.isNotBlank()) """<p style="margin-top:16px;text-align:center;font-size:9pt;color:#888888;border-top:1px solid #dddddd;padding-top:8px;">${escapeHtml(snapshot.footerText)}</p>""" else ""}

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

            Timber.d("✅ PDF created: ${file.name}, size: ${file.length()}")
        } catch (e: Exception) {
            Timber.e(e, "❌ PDF conversion failed")
            throw e
        }

        return file
    }
}
