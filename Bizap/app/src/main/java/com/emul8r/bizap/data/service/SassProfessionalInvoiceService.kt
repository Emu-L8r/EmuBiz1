package com.emul8r.bizap.data.service

import android.content.Context
import com.emul8r.bizap.domain.model.InvoiceSettings
import com.emul8r.bizap.domain.model.InvoiceSnapshot
import com.emul8r.bizap.utils.CentsFormatter
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

/**
 * SASS Professional Invoice Service
 *
 * Generates beautiful, professional-looking invoices using a modern two-column layout with:
 * - Professional branding header (gradient blue)
 * - Left column: Customer & business details, contact info, payment details
 * - Right column: Invoice summary, line items table, totals
 * - Modern footer with thank you message, payment instructions, signature area
 * - Responsive design optimized for PDF rendering
 *
 * **Design Features:**
 * - Professional color scheme (navy blue gradients)
 * - Two-column layout for visual balance
 * - Icon integration (emoji for accessibility)
 * - Zebra-striped table rows for readability
 * - Modern typography hierarchy
 * - Print-optimized CSS with page breaks
 *
 * **Usage:**
 * 1. Load invoice data from database
 * 2. Create SassProfessionalInvoiceService
 * 3. Call `buildInvoiceHtml()` to get HTML template
 * 4. Pass HTML to iText 7 PDF generator
 * 5. Profit! 💰
 */
class SassProfessionalInvoiceService(
    private val context: Context,
    private val settings: InvoiceSettings?
) {

    /**
     * Build complete invoice HTML using SASS Professional template.
     * Replaces all placeholder variables with actual invoice data.
     */
    fun buildInvoiceHtml(invoice: InvoiceSnapshot, isQuote: Boolean = false): String {
        Timber.d("🎨 Building SASS Professional invoice: ${invoice.invoiceNumber}")

        try {
            // Load template from resources
            val templateHtml = loadSassTemplate()

            // Format dates
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val invoiceDate = dateFormat.format(Date(invoice.date))
            val dueDate = dateFormat.format(Date(invoice.dueDate))

            // Build line items HTML
            val itemsHtml = buildItemsHtml(invoice)

            // Use invoice snapshot totals (already calculated)
            val subtotal = invoice.subtotal
            val taxAmount = invoice.taxAmount
            val totalAmount = invoice.totalAmount

            // Build replacements map
            val replacements = mapOf(
                "\${companyName}" to invoice.businessName,
                "\${invoiceNumber}" to invoice.invoiceNumber,
                "\${invoiceDate}" to invoiceDate,
                "\${dueDate}" to dueDate,
                "\${customerName}" to invoice.customerName,
                "\${customerAddress}" to invoice.customerAddress,
                "\${customerEmail}" to (invoice.customerEmail ?: ""),
                "\${customerPhone}" to invoice.businessPhone,
                "\${businessName}" to invoice.businessName,
                "\${businessAbn}" to invoice.businessAbn,
                "\${bankName}" to invoice.bankName,
                "\${accountNumber}" to invoice.bankAccountNumber,
                "\${bsbNumber}" to invoice.bankBsb,
                "\${paymentTerms}" to (settings?.paymentTermsDays ?: 30).toString(),
                "\${invoiceStatus}" to invoice.invoiceStatus,
                "\${itemsHtml}" to itemsHtml,
                "\${subtotal}" to CentsFormatter.formatCents(subtotal),
                "\${taxRate}" to "${(settings?.taxRate?.times(100) ?: 10).toInt()}",
                "\${taxAmount}" to CentsFormatter.formatCents(taxAmount),
                "\${totalAmount}" to CentsFormatter.formatCents(totalAmount),
                "\${authorizedBy}" to (settings?.invoiceNumberPrefix?.removeSuffix("-") ?: "Authorized Signatory"),
                "\${notes}" to (invoice.notes ?: "")
            )

            // Replace all placeholders
            var html = templateHtml
            replacements.forEach { (placeholder, value) ->
                html = html.replace(placeholder, value)
            }

            Timber.d("✅ SASS Professional invoice HTML generated: ${html.length} bytes")
            return html

        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to build SASS Professional invoice HTML")
            throw e
        }
    }

    /**
     * Build HTML table rows for line items with zebra striping.
     */
    private fun buildItemsHtml(invoice: InvoiceSnapshot): String {
        return invoice.items.mapIndexed { index, item ->
            val total = item.quantity * item.unitPrice
            """
            <div class="item-row">
                <div class="item-description">${item.description}</div>
                <div class="item-qty">${String.format("%.2f", item.quantity)}</div>
                <div class="item-price">${CentsFormatter.formatCents(item.unitPrice.toLong())}</div>
                <div class="item-total">${CentsFormatter.formatCents(total.toLong())}</div>
            </div>
            """.trimIndent()
        }.joinToString("\n")
    }

    /**
     * Load SASS Professional template from resources.
     * Falls back to default HTML if file not found.
     */
    private fun loadSassTemplate(): String {
        return try {
            context.assets.open("templates/invoice-template-sass-professional.html")
                .bufferedReader()
                .use { it.readText() }
        } catch (e: Exception) {
            Timber.w(e, "Failed to load SASS template from assets, using fallback")
            getDefaultSassTemplate()
        }
    }

    /**
     * Default SASS template fallback (inline).
     * Used if template file is not available in assets.
     */
    private fun getDefaultSassTemplate(): String {
        return """
            <!DOCTYPE html>
            <html>
            <head><title>Invoice</title>
            <style>
                body { font-family: Arial; background: #f5f5f5; }
                .invoice { background: white; padding: 20px; }
                .header { background: #1c3f6b; color: white; padding: 20px; }
                .content { display: flex; min-height: 600px; }
                .left-column { flex: 0 0 40%; padding: 20px; background: #f9f9f9; }
                .right-column { flex: 1; padding: 20px; }
                .items-header { font-weight: bold; border-bottom: 2px solid #1c3f6b; }
                .item-row { display: flex; justify-content: space-between; padding: 8px 0; }
                .footer { background: #1c3f6b; color: white; padding: 20px; }
            </style></head>
            <body>
            <div class="invoice">
                <div class="header"><h2>${"$"}{invoiceNumber}</h2></div>
                <div class="content">
                    <div class="left-column">
                        <h3>Bill To</h3>
                        <p>${"$"}{customerName}</p>
                        <p>${"$"}{customerEmail}</p>
                    </div>
                    <div class="right-column">
                        <div class="items-header">Items</div>
                        ${"$"}{itemsHtml}
                        <div><strong>Total: ${"$"}{totalAmount}</strong></div>
                    </div>
                </div>
                <div class="footer">Thank you for your business!</div>
            </div>
            </body></html>
        """.trimIndent()
    }

    companion object {
        const val TEMPLATE_FILE = "templates/invoice-template-sass-professional.html"
    }
}

