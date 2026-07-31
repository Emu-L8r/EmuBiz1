package com.emul8r.bizap.domain.model

/**
 * Interface for page layout providers.
 *
 * Each layout defines how invoice content is organized on the page:
 * - CLASSIC: Traditional layout (header, bill-to, items, totals, footer)
 * - MODERN: Compact side-by-side layout with grid organization
 */
interface PageLayoutProvider {
    /**
     * Build the complete HTML invoice using this layout.
     *
     * @param snapshot Invoice data to render
     * @param isQuote Whether this is a quote or invoice
     * @param colorScheme Color scheme for styling (primary, accent colors, etc.)
     * @return Complete HTML string for PDF conversion
     */
    fun buildInvoiceHtml(
        snapshot: InvoiceSnapshot,
        isQuote: Boolean,
        colorScheme: InvoiceColorScheme
    ): String

    /**
     * Get the layout name for logging/debugging.
     */
    fun getLayoutName(): String
}

/**
 * Color scheme for invoice styling.
 * Extracted from InvoiceSettings to be layout-agnostic.
 */
data class InvoiceColorScheme(
    val primaryColor: String = "#6B4C9A",
    val accentColor: String = "#FF9F43",
    val lightBackground: String = "#f9f9f9",
    val textDark: String = "#333333",
    val textLight: String = "#666666",
    val borderColor: String = "#e0e0e0"
)

/**
 * CLASSIC LAYOUT
 * Traditional invoice format:
 * - Header (company info, document type)
 * - Bill To + Invoice Details (side-by-side)
 * - Items Table
 * - Totals Section
 * - Payment Details
 * - Notes
 * - Footer
 */
class ClassicPageLayout : PageLayoutProvider {
    override fun getLayoutName(): String = "CLASSIC"

    override fun buildInvoiceHtml(
        snapshot: InvoiceSnapshot,
        isQuote: Boolean,
        colorScheme: InvoiceColorScheme
    ): String {
        val docType = if (isQuote) "QUOTE" else "INVOICE"
        val primary = colorScheme.primaryColor
        val light = colorScheme.lightBackground
        val textDark = colorScheme.textDark

        // Build sections
        val itemRows = buildItemsRows(snapshot, primary, colorScheme)
        val totalRows = buildTotalsRows(snapshot, primary)
        val paymentSection = buildPaymentSection(snapshot, primary)
        val notesSection = buildNotesSection(snapshot, primary)

        val logoHtml = if (!snapshot.logoBase64.isNullOrBlank())
            """<img src="data:image/png;base64,${snapshot.logoBase64}" style="max-height:60px;max-width:120px;" alt="logo"/>"""
        else ""

        return """<!DOCTYPE html>
<html><head><meta charset="UTF-8"/>
<style>
@page { margin: 15mm 15mm 15mm 15mm; }
body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; font-size: 10pt; color: $textDark; margin: 0; padding: 0; line-height: 1.8; }
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
    <div style="margin-top:10px;">
    ${if (snapshot.businessAddress.isNotBlank()) """<div style="font-size:9pt;color:#e0d8f0;margin-top:4px;line-height:1.8;">${addressLines(snapshot.businessAddress)}</div>""" else ""}
    ${if (snapshot.businessEmail.isNotBlank()) """<div style="font-size:9pt;color:#e0d8f0;margin-top:4px;">${escapeHtml(snapshot.businessEmail)}</div>""" else ""}
    ${if (snapshot.businessPhone.isNotBlank()) """<div style="font-size:9pt;color:#e0d8f0;margin-top:4px;">${escapeHtml(snapshot.businessPhone)}</div>""" else ""}
    ${if (snapshot.businessAbn.isNotBlank()) """<div style="font-size:9pt;color:#e0d8f0;margin-top:4px;">ABN: ${escapeHtml(snapshot.businessAbn)}</div>""" else ""}
    </div>
  </td>
  <td style="padding:24px 24px;text-align:right;vertical-align:top;">
    <div style="font-size:22pt;font-weight:bold;letter-spacing:3px;color:#ffffff;">$docType</div>
  </td>
</tr>
</table>

<!-- INVOICE META + BILL TO -->
<table width="100%" style="margin-bottom:20px;">
<tr>
  <td width="50%" style="vertical-align:top;padding-right:12px;">
    <table width="100%" style="background-color:$light;border-left:4px solid $primary;">
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

<!-- ADDITIONAL INFO SECTION (Header/Subheader from form) -->
${if (snapshot.header.isNotBlank() || snapshot.subheader.isNotBlank()) """
<table width="100%" style="border-collapse:collapse;margin:20px 0 20px 0;padding:0;page-break-inside:avoid;">
    <tr>
        <td style="background-color:#f9f9f9;padding:14px 16px;font-weight:bold;font-size:11pt;color:$primary;border-left:5px solid $primary;letter-spacing:0.5px;text-transform:uppercase;width:100%;">📋 Additional Information</td>
    </tr>
    ${if (snapshot.header.isNotBlank()) """<tr><td style="padding:12px 16px;font-size:10pt;line-height:1.8;word-wrap:break-word;color:#444444;background-color:white;border-bottom:1px solid #e8e8e8;">${escapeHtml(snapshot.header)}</td></tr>""" else ""}
    ${if (snapshot.subheader.isNotBlank()) """<tr><td style="padding:12px 16px;font-size:10pt;line-height:1.8;word-wrap:break-word;color:#444444;background-color:white;">${escapeHtml(snapshot.subheader)}</td></tr>""" else ""}
</table>
""" else ""}

<!-- LINE ITEMS TABLE with fixed column widths -->
<table width="100%" style="border-collapse:collapse;table-layout:fixed;margin-bottom:4px;">
  <colgroup>
    <col style="width:54%;"/>
    <col style="width:10%;"/>
    <col style="width:18%;"/>
    <col style="width:18%;"/>
  </colgroup>
  <tr style="background-color:$primary;color:#ffffff;">
    <th style="padding:12px 14px;text-align:left;font-size:10pt;">Description</th>
    <th style="padding:12px 14px;text-align:center;font-size:10pt;">Qty</th>
    <th style="padding:12px 14px;text-align:right;font-size:10pt;">Unit Price</th>
    <th style="padding:12px 14px;text-align:right;font-size:10pt;">Total</th>
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

    // ...existing code...
    private fun escapeHtml(text: String): String = text
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")

    private fun addressLines(address: String): String =
        escapeHtml(address).replace("\n", "<br/>")

    private fun formatDate(millis: Long): String =
        if (millis > 0) java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault()).format(java.util.Date(millis)) else ""

    private fun formatMoney(cents: Long, currencyCode: String): String {
        val symbol = when (currencyCode.uppercase()) {
            "AUD", "USD", "CAD", "SGD", "NZD", "HKD" -> "$"
            "GBP" -> "£"
            else -> "$"
        }
        return "$symbol${String.format("%.2f", cents / 100.0)}"
    }

    private fun formatQty(qty: Double): String =
        if (qty == qty.toLong().toDouble()) qty.toLong().toString() else String.format("%.2f", qty)

    private fun buildItemsRows(snapshot: InvoiceSnapshot, primary: String, colorScheme: InvoiceColorScheme): String =
        snapshot.items.mapIndexed { i, item ->
            val bg = if (i % 2 == 0) "#ffffff" else colorScheme.lightBackground
            val unitDollars = formatMoney(item.unitPrice, snapshot.currencyCode)
            val totalDollars = formatMoney(item.total, snapshot.currencyCode)
            """<tr style="background-color:$bg;">
                <td style="padding:10px 14px;border-bottom:1px solid #e0e0e0;line-height:1.8;word-wrap:break-word;">${escapeHtml(item.description)}</td>
                <td style="padding:10px 14px;border-bottom:1px solid #e0e0e0;text-align:center;line-height:1.8;">${formatQty(item.quantity)}</td>
                <td style="padding:10px 14px;border-bottom:1px solid #e0e0e0;text-align:right;line-height:1.8;">$unitDollars</td>
                <td style="padding:10px 14px;border-bottom:1px solid #e0e0e0;text-align:right;font-weight:bold;color:$primary;line-height:1.8;">$totalDollars</td>
            </tr>"""
        }.joinToString("\n")

    private fun buildTotalsRows(snapshot: InvoiceSnapshot, primary: String): String {
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
                <td colspan="3" style="padding:9px 14px;text-align:right;color:#555555;">Subtotal</td>
                <td style="padding:9px 14px;text-align:right;">${formatMoney(snapshot.subtotal, snapshot.currencyCode)}</td>
            </tr>
            <tr>
                <td colspan="3" style="padding:9px 14px;text-align:right;color:#555555;">$taxLabel</td>
                <td style="padding:9px 14px;text-align:right;">${formatMoney(snapshot.taxAmount, snapshot.currencyCode)}</td>
            </tr>
            <tr style="background-color:#f5f5f5;">
                <td colspan="3" style="padding:12px 14px;text-align:right;font-weight:bold;font-size:13pt;color:$primary;">TOTAL DUE</td>
                <td style="padding:12px 14px;text-align:right;font-weight:bold;font-size:13pt;color:$primary;">${formatMoney(snapshot.totalAmount, snapshot.currencyCode)}</td>
            </tr>
        """.trimIndent()
    }

    private fun buildPaymentSection(snapshot: InvoiceSnapshot, primary: String): String {
        val hasBank = snapshot.bankName.isNotBlank() || snapshot.bankAccountName.isNotBlank()
            || snapshot.bankAccountNumber.isNotBlank() || snapshot.bankBsb.isNotBlank()
        if (!hasBank) return ""
        return """
            <table width="100%" style="border-collapse:collapse;margin-top:20px;">
                <tr><td colspan="2" style="padding:14px 16px;background-color:#f5f5f5;font-weight:bold;font-size:11pt;color:$primary;border-left:4px solid $primary;letter-spacing:0.5px;text-transform:uppercase;">PAYMENT DETAILS</td></tr>
                ${if (snapshot.bankName.isNotBlank()) """<tr style="height:16px;"><td style="padding:14px 16px;font-weight:bold;width:40%;line-height:1.8;color:#333333;">Bank</td><td style="padding:14px 16px;line-height:1.8;color:#555555;">${escapeHtml(snapshot.bankName)}</td></tr>""" else ""}
                ${if (snapshot.bankAccountName.isNotBlank()) """<tr style="height:16px;"><td style="padding:14px 16px;font-weight:bold;line-height:1.8;color:#333333;">Account Name</td><td style="padding:14px 16px;line-height:1.8;color:#555555;">${escapeHtml(snapshot.bankAccountName)}</td></tr>""" else ""}
                ${if (snapshot.bankAccountNumber.isNotBlank()) """<tr style="height:16px;"><td style="padding:14px 16px;font-weight:bold;line-height:1.8;color:#333333;">Account Number</td><td style="padding:14px 16px;line-height:1.8;color:#555555;">${escapeHtml(snapshot.bankAccountNumber)}</td></tr>""" else ""}
                ${if (snapshot.bankBsb.isNotBlank()) """<tr style="height:16px;"><td style="padding:14px 16px;font-weight:bold;line-height:1.8;color:#333333;">BSB</td><td style="padding:14px 16px;line-height:1.8;color:#555555;">${escapeHtml(snapshot.bankBsb)}</td></tr>""" else ""}
            </table>
        """.trimIndent()
    }

    private fun buildNotesSection(snapshot: InvoiceSnapshot, primary: String): String {
        if (snapshot.notes.isBlank()) return ""
        return """
            <table width="100%" style="border-collapse:collapse;margin-top:20px;">
                <tr><td style="padding:10px 14px;background-color:#f5f5f5;font-weight:bold;font-size:11pt;color:$primary;border-left:4px solid $primary;">NOTES</td></tr>
                <tr><td style="padding:10px 14px;font-size:10pt;line-height:1.8;word-wrap:break-word;">${escapeHtml(snapshot.notes)}</td></tr>
            </table>
        """.trimIndent()
    }
}

/**
 * MODERN LAYOUT
 * Compact grid-based layout:
 * - Compact header with logo and company name
 * - Invoice details at top right
 * - Bill To section below
 * - Items Table
 * - Two-column payment section (Payment Details + Bank Transfer)
 * - Compact footer
 *
 * Key differences:
 * - More compact spacing
 * - Better use of whitespace
 * - Improved visual hierarchy
 * - Easier to scan
 */
class ModernPageLayout : PageLayoutProvider {
    override fun getLayoutName(): String = "MODERN"

    override fun buildInvoiceHtml(
        snapshot: InvoiceSnapshot,
        isQuote: Boolean,
        colorScheme: InvoiceColorScheme
    ): String {
        val docType = if (isQuote) "QUOTE" else "INVOICE"
        val primary = colorScheme.primaryColor
        val light = colorScheme.lightBackground
        val textDark = colorScheme.textDark

        val itemRows = buildItemsRows(snapshot, primary, colorScheme)
        val totalRows = buildTotalsRows(snapshot, primary)

        val logoHtml = if (!snapshot.logoBase64.isNullOrBlank())
            """<img src="data:image/png;base64,${snapshot.logoBase64}" style="max-height:50px;max-width:100px;" alt="logo"/>"""
        else ""

        return """<!DOCTYPE html>
<html><head><meta charset="UTF-8"/>
<style>
@page { margin: 12mm 12mm 12mm 12mm; }
body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; font-size: 10pt; color: $textDark; margin: 0; padding: 0; line-height: 1.6; }
table { border-collapse: collapse; }
td, th { word-wrap: break-word; }
</style>
</head><body>

<!-- COMPACT HEADER -->
<table width="100%" style="margin-bottom:16px;">
<tr>
  <td style="vertical-align:middle;padding:12px 0;">
    $logoHtml
    ${if (snapshot.businessName.isNotBlank()) """<div style="font-size:18pt;font-weight:bold;color:$primary;margin-top:4px;">${escapeHtml(snapshot.businessName)}</div>""" else ""}
  </td>
  <td style="text-align:right;vertical-align:top;padding:12px 0;">
    <div style="font-size:20pt;font-weight:bold;color:$primary;letter-spacing:2px;">$docType</div>
    <div style="font-size:11pt;color:#666666;margin-top:4px;">${escapeHtml(snapshot.invoiceNumber)}</div>
  </td>
</tr>
</table>

<!-- INVOICE DETAILS & BILL TO (GRID) -->
<table width="100%" style="margin-bottom:16px;">
<tr>
  <td width="33%" style="vertical-align:top;padding-right:12px;">
    <div style="font-size:9pt;font-weight:bold;color:$primary;margin-bottom:6px;text-transform:uppercase;">INVOICE DATE</div>
    <div style="font-size:10pt;margin-bottom:12px;">${formatDate(snapshot.date)}</div>
    <div style="font-size:9pt;font-weight:bold;color:$primary;margin-bottom:6px;text-transform:uppercase;">DUE DATE</div>
    <div style="font-size:10pt;">${formatDate(snapshot.dueDate)}</div>
  </td>
  <td width="33%" style="vertical-align:top;padding:0 12px;">
    <div style="font-size:9pt;font-weight:bold;color:$primary;margin-bottom:6px;text-transform:uppercase;">FROM</div>
    ${if (snapshot.businessEmail.isNotBlank()) """<div style="font-size:9pt;margin-bottom:2px;">${escapeHtml(snapshot.businessEmail)}</div>""" else ""}
    ${if (snapshot.businessPhone.isNotBlank()) """<div style="font-size:9pt;">${escapeHtml(snapshot.businessPhone)}</div>""" else ""}
  </td>
  <td width="34%" style="vertical-align:top;padding-left:12px;">
    <div style="font-size:9pt;font-weight:bold;color:$primary;margin-bottom:6px;text-transform:uppercase;">BILL TO</div>
    ${if (snapshot.customerName.isNotBlank()) """<div style="font-size:10pt;font-weight:bold;margin-bottom:2px;">${escapeHtml(snapshot.customerName)}</div>""" else ""}
    ${if (snapshot.customerAddress.isNotBlank()) """<div style="font-size:8pt;line-height:1.4;margin-bottom:2px;">${addressLines(snapshot.customerAddress)}</div>""" else ""}
    ${if (!snapshot.customerEmail.isNullOrBlank()) """<div style="font-size:8pt;">${escapeHtml(snapshot.customerEmail)}</div>""" else ""}
  </td>
</tr>
</table>

${if (snapshot.header.isNotBlank()) """<p style="margin-bottom:12px;font-style:italic;color:#555555;line-height:1.6;font-size:9pt;">${escapeHtml(snapshot.header)}</p>""" else ""}

<!-- LINE ITEMS TABLE (COMPACT) with fixed column widths -->
<table width="100%" style="border-collapse:collapse;table-layout:fixed;margin-bottom:4px;">
  <colgroup>
    <col style="width:60%;"/>
    <col style="width:8%;"/>
    <col style="width:16%;"/>
    <col style="width:16%;"/>
  </colgroup>
  <tr style="background-color:$primary;color:#ffffff;">
    <th style="padding:10px 12px;text-align:left;font-size:9pt;">Description</th>
    <th style="padding:10px 12px;text-align:center;font-size:9pt;">Qty</th>
    <th style="padding:10px 12px;text-align:right;font-size:9pt;">Unit Price</th>
    <th style="padding:10px 12px;text-align:right;font-size:9pt;">Total</th>
  </tr>
  $itemRows
  <tr><td colspan="4" style="padding:0;"></td></tr>
  $totalRows
</table>

<!-- COMPACT PAYMENT SECTION -->
${if (snapshot.bankName.isNotBlank() || snapshot.bankAccountName.isNotBlank()) """
<table width="100%" style="margin-top:16px;">
<tr>
  <td width="50%" style="padding-right:12px;">
    <div style="font-size:9pt;font-weight:bold;color:$primary;margin-bottom:6px;text-transform:uppercase;">PAYMENT DETAILS</div>
    ${if (snapshot.bankName.isNotBlank()) """<div style="font-size:8pt;margin-bottom:2px;"><strong>Bank:</strong> ${escapeHtml(snapshot.bankName)}</div>""" else ""}
    ${if (snapshot.bankAccountName.isNotBlank()) """<div style="font-size:8pt;margin-bottom:2px;"><strong>Account:</strong> ${escapeHtml(snapshot.bankAccountName)}</div>""" else ""}
    ${if (snapshot.bankAccountNumber.isNotBlank()) """<div style="font-size:8pt;margin-bottom:2px;"><strong>Number:</strong> ${escapeHtml(snapshot.bankAccountNumber)}</div>""" else ""}
    ${if (snapshot.bankBsb.isNotBlank()) """<div style="font-size:8pt;"><strong>BSB:</strong> ${escapeHtml(snapshot.bankBsb)}</div>""" else ""}
  </td>
  <td width="50%" style="padding-left:12px;">
    ${if (snapshot.notes.isNotBlank()) """<div style="font-size:9pt;font-weight:bold;color:$primary;margin-bottom:6px;text-transform:uppercase;">NOTES</div><div style="font-size:8pt;line-height:1.4;">${escapeHtml(snapshot.notes)}</div>""" else ""}
  </td>
</tr>
</table>
""" else ""}

${if (snapshot.footerText.isNotBlank()) """<p style="margin-top:12px;text-align:center;font-size:8pt;color:#888888;border-top:1px solid #dddddd;padding-top:8px;line-height:1.6;">${escapeHtml(snapshot.footerText)}</p>""" else ""}

</body></html>"""
    }

    // ...existing code...
    private fun escapeHtml(text: String): String = text
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")

    private fun addressLines(address: String): String =
        escapeHtml(address).replace("\n", "<br/>")

    private fun formatDate(millis: Long): String =
        if (millis > 0) java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault()).format(java.util.Date(millis)) else ""

    private fun formatMoney(cents: Long, currencyCode: String): String {
        val symbol = when (currencyCode.uppercase()) {
            "AUD", "USD", "CAD", "SGD", "NZD", "HKD" -> "$"
            "GBP" -> "£"
            else -> "$"
        }
        return "$symbol${String.format("%.2f", cents / 100.0)}"
    }

    private fun formatQty(qty: Double): String =
        if (qty == qty.toLong().toDouble()) qty.toLong().toString() else String.format("%.2f", qty)

    private fun buildItemsRows(snapshot: InvoiceSnapshot, primary: String, colorScheme: InvoiceColorScheme): String =
        snapshot.items.mapIndexed { i, item ->
            val bg = if (i % 2 == 0) "#ffffff" else colorScheme.lightBackground
            val unitDollars = formatMoney(item.unitPrice, snapshot.currencyCode)
            val totalDollars = formatMoney(item.total, snapshot.currencyCode)
            """<tr style="background-color:$bg;">
                <td style="padding:8px 12px;border-bottom:1px solid #e0e0e0;line-height:1.6;word-wrap:break-word;">${escapeHtml(item.description)}</td>
                <td style="padding:8px 12px;border-bottom:1px solid #e0e0e0;text-align:center;line-height:1.6;">${formatQty(item.quantity)}</td>
                <td style="padding:8px 12px;border-bottom:1px solid #e0e0e0;text-align:right;line-height:1.6;">$unitDollars</td>
                <td style="padding:8px 12px;border-bottom:1px solid #e0e0e0;text-align:right;font-weight:bold;color:$primary;line-height:1.6;">$totalDollars</td>
            </tr>"""
        }.joinToString("\n")

    private fun buildTotalsRows(snapshot: InvoiceSnapshot, primary: String): String {
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
                <td colspan="3" style="padding:8px 12px;text-align:right;color:#666666;font-size:9pt;">Subtotal</td>
                <td style="padding:8px 12px;text-align:right;font-size:9pt;">${formatMoney(snapshot.subtotal, snapshot.currencyCode)}</td>
            </tr>
            <tr>
                <td colspan="3" style="padding:8px 12px;text-align:right;color:#666666;font-size:9pt;">$taxLabel</td>
                <td style="padding:8px 12px;text-align:right;font-size:9pt;">${formatMoney(snapshot.taxAmount, snapshot.currencyCode)}</td>
            </tr>
            <tr style="background-color:#f0f0f0;">
                <td colspan="3" style="padding:10px 12px;text-align:right;font-weight:bold;font-size:11pt;color:$primary;">TOTAL</td>
                <td style="padding:10px 12px;text-align:right;font-weight:bold;font-size:11pt;color:$primary;">${formatMoney(snapshot.totalAmount, snapshot.currencyCode)}</td>
            </tr>
        """.trimIndent()
    }
}

/**
 * SPACIOUS layout - Premium spacious design with generous spacing.
 *
 * Uses only table-based layout (no flexbox/CSS grid) for full iText7 compatibility.
 *
 * Key visual characteristics vs Classic and Modern:
 * - Margins: 20mm on all sides (vs 15mm Classic, 12mm Modern)
 * - Font size: 12pt body (vs 10pt Classic, 10pt Modern)
 * - Item row padding: 16px (vs 10px Classic, 8px Modern)
 * - Line height: 2.0 (vs 1.8 Classic, 1.6 Modern)
 * - Section spacing: 30px gaps between sections (vs 20px Classic, 16px Modern)
 * - Header: Spacious two-row layout, logo given generous vertical space
 * - Totals: Presented in their own full-width table with large typography
 * - Payment: Full-width section with generous cell padding
 */
class SpaciousPageLayout : PageLayoutProvider {
    override fun getLayoutName(): String = "SPACIOUS"

    override fun buildInvoiceHtml(
        snapshot: InvoiceSnapshot,
        isQuote: Boolean,
        colorScheme: InvoiceColorScheme
    ): String {
        val docType = if (isQuote) "QUOTE" else "INVOICE"
        val primary = colorScheme.primaryColor
        val textDark = colorScheme.textDark
        val itemRows = buildItemsRows(snapshot, primary, colorScheme)
        val totalRows = buildTotalsRows(snapshot, primary)
        val paymentSection = buildPaymentSection(snapshot, primary)
        val notesSection = buildNotesSection(snapshot, primary)

        val logoHtml = if (!snapshot.logoBase64.isNullOrBlank())
            """<img src="data:image/png;base64,${snapshot.logoBase64}" style="max-height:80px;max-width:160px;" alt="logo"/>"""
        else ""

        return """<!DOCTYPE html>
<html><head><meta charset="UTF-8"/>
<style>
@page { margin: 20mm 20mm 20mm 20mm; }
body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; font-size: 12pt; color: $textDark; margin: 0; padding: 0; line-height: 2.0; }
table { border-collapse: collapse; }
td, th { word-wrap: break-word; }
</style>
</head><body>

<!-- SPACIOUS HEADER: generous padding, logo row + company name row -->
<table width="100%" style="background-color:$primary;color:#ffffff;margin-bottom:30px;">
<tr>
  <td style="padding:30px 30px 10px 30px;vertical-align:top;">
    $logoHtml
    ${if (snapshot.businessName.isNotBlank()) """<div style="font-size:24pt;font-weight:bold;color:#ffffff;margin-top:10px;line-height:1.4;">${escapeHtml(snapshot.businessName)}</div>""" else ""}
    ${if (snapshot.subheader.isNotBlank()) """<div style="font-size:14pt;color:#ddd8f8;margin-top:8px;line-height:1.6;">${escapeHtml(snapshot.subheader)}</div>""" else ""}
    <div style="margin-top:12px;">
    ${if (snapshot.businessAddress.isNotBlank()) """<div style="font-size:10pt;color:#e0d8f0;margin-top:6px;line-height:2.0;">${addressLines(snapshot.businessAddress)}</div>""" else ""}
    ${if (snapshot.businessEmail.isNotBlank()) """<div style="font-size:10pt;color:#e0d8f0;margin-top:6px;">${escapeHtml(snapshot.businessEmail)}</div>""" else ""}
    ${if (snapshot.businessPhone.isNotBlank()) """<div style="font-size:10pt;color:#e0d8f0;margin-top:6px;">${escapeHtml(snapshot.businessPhone)}</div>""" else ""}
    ${if (snapshot.businessAbn.isNotBlank()) """<div style="font-size:10pt;color:#e0d8f0;margin-top:6px;">ABN: ${escapeHtml(snapshot.businessAbn)}</div>""" else ""}
    </div>
  </td>
  <td style="padding:30px 30px 10px 30px;text-align:right;vertical-align:top;">
    <div style="font-size:28pt;font-weight:bold;letter-spacing:4px;color:#ffffff;">$docType</div>
  </td>
</tr>
<tr><td colspan="2" style="padding:0 0 20px 0;"></td></tr>
</table>

<!-- SPACIOUS BILL TO + INVOICE META (generous vertical rhythm) -->
<table width="100%" style="margin-bottom:30px;">
<tr>
  <td width="50%" style="vertical-align:top;padding-right:16px;">
    <table width="100%" style="background-color:#fafafa;border-left:5px solid #aaaaaa;">
      <tr><td style="padding:14px 18px;font-weight:bold;font-size:13pt;color:$primary;line-height:2.0;">BILL TO</td></tr>
      ${if (snapshot.customerName.isNotBlank()) """<tr><td style="padding:10px 18px;font-weight:bold;font-size:13pt;line-height:2.0;">${escapeHtml(snapshot.customerName)}</td></tr>""" else ""}
      ${if (snapshot.customerAddress.isNotBlank()) """<tr><td style="padding:10px 18px;font-size:11pt;line-height:2.0;">${addressLines(snapshot.customerAddress)}</td></tr>""" else ""}
      ${if (!snapshot.customerEmail.isNullOrBlank()) """<tr><td style="padding:10px 18px;font-size:11pt;line-height:2.0;">${escapeHtml(snapshot.customerEmail)}</td></tr>""" else ""}
    </table>
  </td>
  <td width="50%" style="vertical-align:top;padding-left:16px;">
    <table width="100%" style="background-color:#f5f5f5;border-left:5px solid $primary;">
      <tr><td colspan="2" style="padding:14px 18px;font-weight:bold;font-size:13pt;color:$primary;line-height:2.0;">INVOICE DETAILS</td></tr>
      <tr><td style="padding:10px 18px;font-weight:bold;width:45%;line-height:2.0;">Invoice #</td><td style="padding:10px 18px;line-height:2.0;">${escapeHtml(snapshot.invoiceNumber)}</td></tr>
      <tr><td style="padding:10px 18px;font-weight:bold;line-height:2.0;">Date</td><td style="padding:10px 18px;line-height:2.0;">${formatDate(snapshot.date)}</td></tr>
      <tr><td style="padding:10px 18px;font-weight:bold;line-height:2.0;">Due Date</td><td style="padding:10px 18px;line-height:2.0;">${formatDate(snapshot.dueDate)}</td></tr>
      <tr><td style="padding:10px 18px;font-weight:bold;line-height:2.0;">Currency</td><td style="padding:10px 18px;line-height:2.0;">${escapeHtml(snapshot.currencyCode)}</td></tr>
      ${if (snapshot.invoiceStatus.isNotBlank()) """<tr><td style="padding:10px 18px;font-weight:bold;line-height:2.0;">Status</td><td style="padding:10px 18px;line-height:2.0;">${escapeHtml(snapshot.invoiceStatus)}</td></tr>""" else ""}
    </table>
  </td>
</tr>
</table>

${if (snapshot.header.isNotBlank()) """<p style="margin-bottom:24px;font-style:italic;color:#555555;line-height:2.0;">${escapeHtml(snapshot.header)}</p>""" else ""}

<!-- SPACIOUS LINE ITEMS TABLE: 16px row padding, 12pt font with fixed column widths -->
<table width="100%" style="border-collapse:collapse;table-layout:fixed;margin-bottom:4px;">
  <colgroup>
    <col style="width:54%;"/>
    <col style="width:10%;"/>
    <col style="width:18%;"/>
    <col style="width:18%;"/>
  </colgroup>
  <tr style="background-color:$primary;color:#ffffff;">
    <th style="padding:16px 18px;text-align:left;font-size:12pt;">Description</th>
    <th style="padding:16px 18px;text-align:center;font-size:12pt;">Qty</th>
    <th style="padding:16px 18px;text-align:right;font-size:12pt;">Unit Price</th>
    <th style="padding:16px 18px;text-align:right;font-size:12pt;">Total</th>
  </tr>
  $itemRows
  <tr><td colspan="4" style="padding:8px 0;"></td></tr>
  $totalRows
</table>

$paymentSection
$notesSection

${if (snapshot.footerText.isNotBlank()) """<p style="margin-top:30px;text-align:center;font-size:11pt;color:#888888;border-top:1px solid #dddddd;padding-top:16px;line-height:2.0;">${escapeHtml(snapshot.footerText)}</p>""" else ""}

</body></html>"""
    }

    private fun escapeHtml(text: String): String = text
        .replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")

    private fun addressLines(address: String): String =
        escapeHtml(address).replace("\n", "<br/>")

    private fun formatDate(millis: Long): String =
        if (millis > 0) java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault()).format(java.util.Date(millis)) else ""

    private fun formatMoney(cents: Long, currencyCode: String): String {
        val symbol = when (currencyCode.uppercase()) {
            "AUD", "USD", "CAD", "SGD", "NZD", "HKD" -> "$"
            "GBP" -> "£"
            else -> "$"
        }
        return "$symbol${String.format(java.util.Locale.getDefault(), "%.2f", cents / 100.0)}"
    }

    private fun formatQty(qty: Double): String =
        if (qty == qty.toLong().toDouble()) qty.toLong().toString() else String.format(java.util.Locale.getDefault(), "%.2f", qty)

    private fun buildItemsRows(
        snapshot: InvoiceSnapshot,
        primary: String,
        colorScheme: InvoiceColorScheme
    ): String {
        return snapshot.items.mapIndexed { i, item ->
            val bg = if (i % 2 == 0) "#ffffff" else colorScheme.lightBackground
            val unitDollars = formatMoney(item.unitPrice, snapshot.currencyCode)
            val totalDollars = formatMoney(item.total, snapshot.currencyCode)
            """<tr style="background-color:$bg;">
                <td style="padding:16px 18px;border-bottom:1px solid #e0e0e0;line-height:2.0;word-wrap:break-word;">${escapeHtml(item.description)}</td>
                <td style="padding:16px 18px;border-bottom:1px solid #e0e0e0;text-align:center;line-height:2.0;">${formatQty(item.quantity)}</td>
                <td style="padding:16px 18px;border-bottom:1px solid #e0e0e0;text-align:right;line-height:2.0;">$unitDollars</td>
                <td style="padding:16px 18px;border-bottom:1px solid #e0e0e0;text-align:right;font-weight:bold;color:$primary;line-height:2.0;">$totalDollars</td>
            </tr>"""
        }.joinToString("\n")
    }

    private fun buildTotalsRows(snapshot: InvoiceSnapshot, primary: String): String {
        val taxPct = snapshot.taxRate * 100
        val taxLabel = if (snapshot.taxRate > 0) {
            val formatted = if (taxPct == taxPct.toLong().toDouble())
                "${taxPct.toLong()}%"
            else
                "${String.format(java.util.Locale.getDefault(), "%.2f", taxPct).trimEnd('0').trimEnd('.')}%"
            "Tax ($formatted)"
        } else "Tax"
        return """
            <tr>
                <td colspan="3" style="padding:14px 18px;text-align:right;color:#555555;font-size:12pt;">Subtotal</td>
                <td style="padding:14px 18px;text-align:right;font-size:12pt;">${formatMoney(snapshot.subtotal, snapshot.currencyCode)}</td>
            </tr>
            <tr>
                <td colspan="3" style="padding:14px 18px;text-align:right;color:#555555;font-size:12pt;">$taxLabel</td>
                <td style="padding:14px 18px;text-align:right;font-size:12pt;">${formatMoney(snapshot.taxAmount, snapshot.currencyCode)}</td>
            </tr>
            <tr style="background-color:#f5f5f5;">
                <td colspan="3" style="padding:18px 18px;text-align:right;font-weight:bold;font-size:15pt;color:$primary;">TOTAL DUE</td>
                <td style="padding:18px 18px;text-align:right;font-weight:bold;font-size:15pt;color:$primary;">${formatMoney(snapshot.totalAmount, snapshot.currencyCode)}</td>
            </tr>
        """.trimIndent()
    }

    private fun buildPaymentSection(snapshot: InvoiceSnapshot, primary: String): String {
        val hasBank = snapshot.bankName.isNotBlank() || snapshot.bankAccountName.isNotBlank()
            || snapshot.bankAccountNumber.isNotBlank() || snapshot.bankBsb.isNotBlank()
        if (!hasBank) return ""
        return """
            <table width="100%" style="border-collapse:collapse;margin-top:30px;">
                <tr><td colspan="2" style="padding:16px 20px;background-color:#f5f5f5;font-weight:bold;font-size:13pt;color:$primary;border-left:5px solid $primary;letter-spacing:0.5px;text-transform:uppercase;">PAYMENT DETAILS</td></tr>
                ${if (snapshot.bankName.isNotBlank()) """<tr><td style="padding:16px 20px;font-weight:bold;width:40%;line-height:2.0;color:#333333;">Bank</td><td style="padding:16px 20px;line-height:2.0;color:#555555;">${escapeHtml(snapshot.bankName)}</td></tr>""" else ""}
                ${if (snapshot.bankAccountName.isNotBlank()) """<tr><td style="padding:16px 20px;font-weight:bold;line-height:2.0;color:#333333;">Account Name</td><td style="padding:16px 20px;line-height:2.0;color:#555555;">${escapeHtml(snapshot.bankAccountName)}</td></tr>""" else ""}
                ${if (snapshot.bankAccountNumber.isNotBlank()) """<tr><td style="padding:16px 20px;font-weight:bold;line-height:2.0;color:#333333;">Account Number</td><td style="padding:16px 20px;line-height:2.0;color:#555555;">${escapeHtml(snapshot.bankAccountNumber)}</td></tr>""" else ""}
                ${if (snapshot.bankBsb.isNotBlank()) """<tr><td style="padding:16px 20px;font-weight:bold;line-height:2.0;color:#333333;">BSB</td><td style="padding:16px 20px;line-height:2.0;color:#555555;">${escapeHtml(snapshot.bankBsb)}</td></tr>""" else ""}
            </table>
        """.trimIndent()
    }

    private fun buildNotesSection(snapshot: InvoiceSnapshot, primary: String): String {
        if (snapshot.notes.isBlank()) return ""
        return """
            <table width="100%" style="border-collapse:collapse;margin-top:30px;">
                <tr><td style="padding:14px 20px;background-color:#f5f5f5;font-weight:bold;font-size:13pt;color:$primary;border-left:5px solid $primary;">NOTES</td></tr>
                <tr><td style="padding:14px 20px;font-size:12pt;line-height:2.0;word-wrap:break-word;">${escapeHtml(snapshot.notes)}</td></tr>
            </table>
        """.trimIndent()
    }
}

/**
 * COMPACT layout - Executive compact design for many line items
 *
 * Structure:
 * - Minimal margins (20px instead of 40px)
 * - Tight spacing throughout
 * - Small fonts (9pt body text)
 * - Single-line items (no alternating colors)
 * - Designed to fit many items without extra pages
 *
 * Key differences:
 * - Smallest margins of all layouts
 * - Most compact spacing
 * - Smallest fonts
 * - Single-color rows (no alternating)
 * - Ideal for contractors, mechanics with many line items
 */
class CompactPageLayout : PageLayoutProvider {
    override fun getLayoutName(): String = "COMPACT"

    override fun buildInvoiceHtml(
        snapshot: InvoiceSnapshot,
        isQuote: Boolean,
        colorScheme: InvoiceColorScheme
    ): String {
        val docType = if (isQuote) "QUOTE" else "INVOICE"
        val primary = colorScheme.primaryColor

        val itemRows = buildItemsRows(snapshot, primary, colorScheme)
        val totalRows = buildTotalsRows(snapshot, primary)

        val logoHtml = if (!snapshot.logoBase64.isNullOrBlank())
            """<img src="data:image/png;base64,${snapshot.logoBase64}" style="max-height:40px;max-width:80px;" alt="logo"/>"""
        else ""

        return """<!DOCTYPE html>
<html><head><meta charset="UTF-8"/><meta name="viewport" content="width=device-width, initial-scale=1.0"/>
<style>
@page { margin: 8mm 8mm 8mm 8mm; }
body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; font-size: 9pt; color: #333333; margin: 0; padding: 0; line-height: 1.4; }
table { border-collapse: collapse; }
td, th { word-wrap: break-word; }
</style>
</head><body>

<!-- COMPACT HEADER -->
<table width="100%" style="margin-bottom:8px;">
<tr>
  <td style="vertical-align:middle;padding:6px 0;">
    $logoHtml
    ${if (snapshot.businessName.isNotBlank()) """<div style="font-size:14pt;font-weight:bold;color:$primary;margin-top:2px;">${escapeHtml(snapshot.businessName)}</div>""" else ""}
  </td>
  <td style="text-align:right;vertical-align:top;padding:6px 0;">
    <div style="font-size:16pt;font-weight:bold;color:$primary;letter-spacing:1px;">$docType</div>
    <div style="font-size:8pt;color:#666666;margin-top:2px;">${escapeHtml(snapshot.invoiceNumber)}</div>
  </td>
</tr>
</table>

<!-- COMPACT DETAILS GRID -->
<table width="100%" style="margin-bottom:6px;">
<tr>
  <td width="25%" style="vertical-align:top;padding:4px;font-size:8pt;font-weight:bold;color:$primary;">DATE</td>
  <td width="25%" style="vertical-align:top;padding:4px;font-size:8pt;font-weight:bold;color:$primary;">DUE DATE</td>
  <td width="25%" style="vertical-align:top;padding:4px;font-size:8pt;font-weight:bold;color:$primary;">FROM</td>
  <td width="25%" style="vertical-align:top;padding:4px;font-size:8pt;font-weight:bold;color:$primary;">BILL TO</td>
</tr>
<tr>
  <td style="padding:3px;font-size:8pt;">${formatDate(snapshot.date)}</td>
  <td style="padding:3px;font-size:8pt;">${formatDate(snapshot.dueDate)}</td>
  <td style="padding:3px;font-size:8pt;">${escapeHtml(snapshot.businessEmail)}</td>
  <td style="padding:3px;font-size:8pt;"><strong>${escapeHtml(snapshot.customerName)}</strong></td>
</tr>
</table>

<!-- LINE ITEMS TABLE (COMPACT) -->
<table width="100%" style="border-collapse:collapse;margin-bottom:2px;">
  <tr style="background-color:$primary;color:#ffffff;">
    <th style="padding:5px 4px;text-align:left;font-size:8pt;font-weight:bold;">Description</th>
    <th style="padding:5px 4px;text-align:center;font-size:8pt;font-weight:bold;width:8%;">Qty</th>
    <th style="padding:5px 4px;text-align:right;font-size:8pt;font-weight:bold;width:15%;">Unit Price</th>
    <th style="padding:5px 4px;text-align:right;font-size:8pt;font-weight:bold;width:15%;">Total</th>
  </tr>
  $itemRows
  $totalRows
</table>

<!-- FOOTER -->
${if (snapshot.footerText.isNotBlank()) """<p style="margin-top:6px;text-align:center;font-size:7pt;color:#888888;border-top:1px solid #dddddd;padding-top:4px;line-height:1.4;">${escapeHtml(snapshot.footerText)}</p>""" else ""}

</body></html>"""
    }

    private fun escapeHtml(text: String): String = text
        .replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")

    private fun formatDate(millis: Long): String =
        if (millis > 0) java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault()).format(java.util.Date(millis)) else ""

    private fun formatMoney(cents: Long, currencyCode: String): String {
        val symbol = when (currencyCode.uppercase()) {
            "AUD", "USD", "CAD", "SGD", "NZD", "HKD" -> "$"
            "GBP" -> "£"
            else -> "$"
        }
        return "$symbol${String.format(java.util.Locale.getDefault(), "%.2f", cents / 100.0)}"
    }

    private fun formatQty(qty: Double): String =
        if (qty == qty.toLong().toDouble()) qty.toLong().toString() else String.format(java.util.Locale.getDefault(), "%.2f", qty)

    private fun buildItemsRows(
        snapshot: InvoiceSnapshot,
        primary: String,
        colorScheme: InvoiceColorScheme
    ): String {
        return snapshot.items.map { item ->
            """<tr style="background-color:#ffffff;">
                <td style="padding:3px 4px;border-bottom:1px solid #f0f0f0;font-size:8pt;">${escapeHtml(item.description)}</td>
                <td style="padding:3px 4px;border-bottom:1px solid #f0f0f0;text-align:center;font-size:8pt;">${formatQty(item.quantity)}</td>
                <td style="padding:3px 4px;border-bottom:1px solid #f0f0f0;text-align:right;font-size:8pt;">${formatMoney(item.unitPrice, snapshot.currencyCode)}</td>
                <td style="padding:3px 4px;border-bottom:1px solid #f0f0f0;text-align:right;font-weight:bold;color:$primary;font-size:8pt;">${formatMoney(item.total, snapshot.currencyCode)}</td>
            </tr>"""
        }.joinToString("\n")
    }

    private fun buildTotalsRows(snapshot: InvoiceSnapshot, primary: String): String {
        val taxPct = snapshot.taxRate * 100
        val taxLabel = if (snapshot.taxRate > 0) {
            val formatted = if (taxPct == taxPct.toLong().toDouble())
                "${taxPct.toLong()}%"
            else
                "${String.format(java.util.Locale.getDefault(), "%.2f", taxPct).trimEnd('0').trimEnd('.')}%"
            "Tax ($formatted)"
        } else "Tax"
        return """
            <tr>
                <td colspan="3" style="padding:3px 4px;text-align:right;color:#666666;font-size:8pt;">Subtotal</td>
                <td style="padding:3px 4px;text-align:right;font-size:8pt;">${formatMoney(snapshot.subtotal, snapshot.currencyCode)}</td>
            </tr>
            <tr>
                <td colspan="3" style="padding:3px 4px;text-align:right;color:#666666;font-size:8pt;">$taxLabel</td>
                <td style="padding:3px 4px;text-align:right;font-size:8pt;">${formatMoney(snapshot.taxAmount, snapshot.currencyCode)}</td>
            </tr>
            <tr style="background-color:#f5f5f5;">
                <td colspan="3" style="padding:4px 4px;text-align:right;font-weight:bold;font-size:9pt;color:$primary;">TOTAL</td>
                <td style="padding:4px 4px;text-align:right;font-weight:bold;font-size:9pt;color:$primary;">${formatMoney(snapshot.totalAmount, snapshot.currencyCode)}</td>
            </tr>
        """.trimIndent()
    }
}
