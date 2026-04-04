package com.emul8r.bizap.data.service.pdf_layouts

import com.emul8r.bizap.domain.model.InvoiceSnapshot

/**
 * Abstraction for different page layouts in HTML-to-PDF generation.
 *
 * A layout determines how invoice content is organized (sections, grid, columns, etc.)
 * independently from styling (colors, fonts). Multiple layouts can use the same CSS.
 */
interface PageLayout {
    /**
     * Generate HTML structure for this layout.
     *
     * @param snapshot The invoice data to render
     * @param cssContent The CSS styles to embed (already loaded from assets)
     * @return Complete HTML document ready for PDF conversion
     */
    fun generateHtml(snapshot: InvoiceSnapshot, cssContent: String): String
}

/**
 * CLASSIC layout - Traditional invoice organization
 *
 * Structure:
 * - Header (company info, invoice label)
 * - Bill To & Invoice Details (side by side)
 * - Items Table
 * - Totals Section
 * - Payment Details
 * - Bank Transfer Info
 * - Notes & Footer
 */
class ClassicLayout : PageLayout {
    override fun generateHtml(snapshot: InvoiceSnapshot, cssContent: String): String {
        val itemsHtml = snapshot.items.joinToString("\n") { item ->
            val itemTotal = item.total / 100.0  // Convert cents to dollars for display
            val unitPrice = item.unitPrice / 100.0
            """
            <tr>
                <td>${item.description}</td>
                <td style="text-align: right;">${String.format("%.2f", item.quantity)}</td>
                <td style="text-align: right;">$${String.format("%.2f", unitPrice)}</td>
                <td style="text-align: right;">$${String.format("%.2f", itemTotal)}</td>
            </tr>
            """
        }

        val subtotalAmount = snapshot.subtotal / 100.0
        val taxAmount = snapshot.taxAmount / 100.0
        val totalAmount = snapshot.totalAmount / 100.0
        val invoiceDate = java.text.SimpleDateFormat("dd-MM-yyyy").format(java.util.Date(snapshot.date))
        val dueDate = java.text.SimpleDateFormat("dd-MM-yyyy").format(java.util.Date(snapshot.dueDate))

        return """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Invoice ${snapshot.invoiceNumber}</title>
    <style>
        $cssContent
    </style>
</head>
<body>
    <div class="invoice-container">
        <!-- Header -->
        <div class="invoice-header">
            <div class="company-info">
                <h1>${snapshot.businessName}</h1>
                <p>${snapshot.businessEmail}</p>
                <p>${snapshot.businessPhone}</p>
                <p>${snapshot.businessAddress}</p>
                <p>ABN: ${snapshot.businessAbn}</p>
            </div>
            <div class="invoice-title">
                <h2>INVOICE</h2>
                <p>#${snapshot.invoiceNumber}</p>
            </div>
        </div>

        <!-- Bill To & Invoice Details -->
        <div class="details-section">
            <div class="bill-to">
                <h3>BILL TO</h3>
                <p><strong>${snapshot.customerName}</strong></p>
                <p>${snapshot.customerAddress}</p>
                ${if (snapshot.customerEmail != null) "<p>${snapshot.customerEmail}</p>" else ""}
            </div>
            <div class="invoice-details">
                <h3>INVOICE DETAILS</h3>
                <p><strong>Invoice Date:</strong> $invoiceDate</p>
                <p><strong>Due Date:</strong> $dueDate</p>
                <p><strong>Currency:</strong> ${snapshot.currencyCode}</p>
            </div>
        </div>

        <!-- Items Table -->
        <table class="items-table">
            <thead>
                <tr>
                    <th>Description</th>
                    <th style="text-align: right;">Quantity</th>
                    <th style="text-align: right;">Unit Price</th>
                    <th style="text-align: right;">Total</th>
                </tr>
            </thead>
            <tbody>
                $itemsHtml
            </tbody>
        </table>

        <!-- Totals -->
        <div class="totals-section">
            <div class="totals-box">
                <div class="totals-row">
                    <span>Subtotal:</span>
                    <span>${'$'}${String.format("%.2f", subtotalAmount)}</span>
                </div>
                <div class="totals-row">
                    <span>Tax (10%):</span>
                    <span>${'$'}${String.format("%.2f", taxAmount)}</span>
                </div>
                <div class="totals-row total-amount">
                    <span>TOTAL:</span>
                    <span>${'$'}${String.format("%.2f", totalAmount)}</span>
                </div>
            </div>
        </div>

        <!-- Payment Details -->
        <div class="payment-section">
            <div class="payment-details">
                <h3>PAYMENT DETAILS</h3>
                <p>Please transfer payment to the following account:</p>
                <p><strong>Bank:</strong> ${snapshot.bankName}</p>
                <p><strong>Account Name:</strong> ${snapshot.bankAccountName}</p>
                <p><strong>BSB:</strong> ${snapshot.bankBsb}</p>
                <p><strong>Account Number:</strong> ${snapshot.bankAccountNumber}</p>
            </div>
        </div>

        <!-- Notes -->
        ${if (snapshot.notes.isNotEmpty()) """
        <div class="notes-section">
            <h3>NOTES</h3>
            <p>${snapshot.notes}</p>
        </div>
        """ else ""}

        <!-- Footer -->
        <div class="footer">
            <p>Generated on ${java.text.SimpleDateFormat("dd-MM-yyyy HH:mm").format(java.util.Date())}</p>
        </div>
    </div>
</body>
</html>
        """
    }
}

/**
 * MODERN layout - Compact side-by-side organization
 *
 * Structure:
 * - Compact header with invoice number
 * - Side-by-side Bill To & Invoice Details
 * - Items Table
 * - Payment section (2-column layout)
 * - Compact footer
 */
class ModernLayout : PageLayout {
    override fun generateHtml(snapshot: InvoiceSnapshot, cssContent: String): String {
        val itemsHtml = snapshot.items.joinToString("\n") { item ->
            val itemTotal = item.total / 100.0
            val unitPrice = item.unitPrice / 100.0
            """
            <tr>
                <td>${item.description}</td>
                <td style="text-align: right;">${String.format("%.2f", item.quantity)}</td>
                <td style="text-align: right;">$${String.format("%.2f", unitPrice)}</td>
                <td style="text-align: right;">$${String.format("%.2f", itemTotal)}</td>
            </tr>
            """
        }

        val subtotalAmount = snapshot.subtotal / 100.0
        val taxAmount = snapshot.taxAmount / 100.0
        val totalAmount = snapshot.totalAmount / 100.0
        val invoiceDate = java.text.SimpleDateFormat("dd-MM-yyyy").format(java.util.Date(snapshot.date))
        val dueDate = java.text.SimpleDateFormat("dd-MM-yyyy").format(java.util.Date(snapshot.dueDate))

        return """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Invoice ${snapshot.invoiceNumber}</title>
    <style>
        $cssContent
        .modern-header { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-bottom: 24px; }
        .payment-columns { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
    </style>
</head>
<body>
    <div class="invoice-container">
        <!-- Compact Header -->
        <div class="modern-header">
            <div class="company-info">
                <h1>${snapshot.businessName}</h1>
                <p>${snapshot.businessEmail} | ${snapshot.businessPhone}</p>
            </div>
            <div class="invoice-title" style="text-align: right;">
                <h2>${snapshot.invoiceNumber}</h2>
                <p>Invoice</p>
            </div>
        </div>

        <!-- Side-by-side Details -->
        <div class="details-section">
            <div class="bill-to">
                <h3>BILL TO</h3>
                <p><strong>${snapshot.customerName}</strong></p>
                <p>${snapshot.customerAddress}</p>
            </div>
            <div class="invoice-details">
                <h3>DETAILS</h3>
                <p><strong>Date:</strong> $invoiceDate</p>
                <p><strong>Due:</strong> $dueDate</p>
            </div>
        </div>

        <!-- Items Table -->
        <table class="items-table">
            <thead>
                <tr>
                    <th>Description</th>
                    <th style="text-align: right;">Qty</th>
                    <th style="text-align: right;">Unit Price</th>
                    <th style="text-align: right;">Total</th>
                </tr>
            </thead>
            <tbody>
                $itemsHtml
            </tbody>
        </table>

        <!-- Totals -->
        <div class="totals-box" style="margin: 20px 0;">
            <div class="totals-row">
                <span>Subtotal:</span>
                <span>${'$'}${String.format("%.2f", subtotalAmount)}</span>
            </div>
            <div class="totals-row">
                <span>Tax:</span>
                <span>${'$'}${String.format("%.2f", taxAmount)}</span>
            </div>
            <div class="totals-row total-amount">
                <span>TOTAL:</span>
                <span>${'$'}${String.format("%.2f", totalAmount)}</span>
            </div>
        </div>

        <!-- Two-Column Payment Section -->
        <div class="payment-columns">
            <div class="payment-details">
                <h3>PAYMENT DETAILS</h3>
                <p><strong>${snapshot.bankName}</strong></p>
                <p>${snapshot.bankAccountName}</p>
            </div>
            <div class="bank-transfer">
                <h3>BANK DETAILS</h3>
                <p>BSB: ${snapshot.bankBsb}</p>
                <p>Acc: ${snapshot.bankAccountNumber}</p>
            </div>
        </div>

        <!-- Footer -->
        <div class="footer">
            <p>Thank you for your business</p>
        </div>
    </div>
</body>
</html>
        """
    }
}

