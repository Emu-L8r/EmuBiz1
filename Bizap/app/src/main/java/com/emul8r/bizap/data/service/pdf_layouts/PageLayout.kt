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
        ${getPageBreakCSS()}
        $cssContent
    </style>
</head>
<body>
    <div class="page">
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
        </div>
    </div>

    <!-- Footer -->
    <div class="footer">
        <p>Generated on ${java.text.SimpleDateFormat("dd-MM-yyyy HH:mm").format(java.util.Date())}</p>
    </div>
</body>
</html>
        """
    }

    private fun getPageBreakCSS(): String = """
        /* PAGE LAYOUT & BREAK CONTROL */
        * { box-sizing: border-box; }
        html, body { margin: 0; padding: 0; height: 100%; }

        .page {
            page-break-after: always;
            min-height: 11in;
            display: flex;
            flex-direction: column;
            padding: 0.75in;
            background: white;
        }

        .invoice-container {
            flex: 1;
            display: flex;
            flex-direction: column;
        }

        .section, .invoice-header, .details-section, .items-table,
        .totals-section, .payment-section, .notes-section {
            page-break-inside: avoid;
        }

        .invoice-header { margin-bottom: 25px; }
        .details-section { margin: 20px 0; display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
        .items-table { width: 100%; border-collapse: collapse; margin: 20px 0; }
        .items-table thead { page-break-inside: avoid; background-color: #f5f5f5; }
        .items-table tr { page-break-inside: avoid; }
        .items-table td, .items-table th { padding: 8px 5px; }
        .items-table tbody tr { border-bottom: 1px solid #e0e0e0; }

        .totals-section { margin: 25px 0; page-break-inside: avoid; }
        .totals-box { width: 50%; margin-left: auto; padding: 15px; background: #f9f9f9; border: 1px solid #ddd; }
        .totals-row { display: flex; justify-content: space-between; padding: 5px 0; }

        .payment-section { margin-top: 30px; padding-top: 20px; border-top: 1px solid #ccc; page-break-inside: avoid; }
        .footer { margin-top: auto; padding-top: 15px; border-top: 1px solid #ccc; text-align: center; font-size: 9pt; color: #666; }
    """
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
        ${getPageBreakCSS()}
        $cssContent
        .modern-header { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-bottom: 24px; page-break-inside: avoid; }
        .payment-columns { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; page-break-inside: avoid; }
    </style>
</head>
<body>
    <div class="page">
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
            <div class="totals-section">
                <div class="totals-box">
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

    private fun getPageBreakCSS(): String = """
        /* PAGE LAYOUT & BREAK CONTROL */
        * { box-sizing: border-box; }
        html, body { margin: 0; padding: 0; height: 100%; }

        .page {
            page-break-after: always;
            min-height: 11in;
            display: flex;
            flex-direction: column;
            padding: 0.75in;
            background: white;
        }

        .invoice-container {
            flex: 1;
            display: flex;
            flex-direction: column;
        }

        .section, .invoice-header, .details-section, .items-table,
        .totals-section, .payment-section, .notes-section {
            page-break-inside: avoid;
        }

        .invoice-header { margin-bottom: 25px; }
        .details-section { margin: 20px 0; display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
        .items-table { width: 100%; border-collapse: collapse; margin: 20px 0; }
        .items-table thead { page-break-inside: avoid; background-color: #f5f5f5; }
        .items-table tr { page-break-inside: avoid; }
        .items-table td, .items-table th { padding: 8px 5px; }
        .items-table tbody tr { border-bottom: 1px solid #e0e0e0; }

        .totals-section { margin: 25px 0; page-break-inside: avoid; }
        .totals-box { width: 50%; margin-left: auto; padding: 15px; background: #f9f9f9; border: 1px solid #ddd; }
        .totals-row { display: flex; justify-content: space-between; padding: 5px 0; }

        .payment-section { margin-top: 30px; padding-top: 20px; border-top: 1px solid #ccc; page-break-inside: avoid; }
        .footer { margin-top: auto; padding-top: 15px; border-top: 1px solid #ccc; text-align: center; font-size: 9pt; color: #666; }
    """
}


/**
 * ADVANCED layout - Professional multi-page support
 *
 * Features:
 * - Automatic page breaks for invoices with 12+ items
 * - Page numbering and "continued" indicators
 * - Header repetition on each page
 * - Smart item pagination
 * - Totals on last page only
 *
 * Structure per page:
 * - Header (full on page 1, abbreviated on subsequent pages)
 * - Items table (chunked by ITEMS_PER_PAGE)
 * - Continued indicator (if not last page)
 * - Totals & Payment (last page only)
 * - Page footer with numbering
 */
class AdvancedPageLayout : PageLayout {

    companion object {
        private const val ITEMS_PER_PAGE = 12
    }

    override fun generateHtml(snapshot: InvoiceSnapshot, cssContent: String): String {
        val itemPages = snapshot.items.chunked(ITEMS_PER_PAGE)

        val pages = itemPages.mapIndexed { pageIndex, pageItems ->
            generatePageHtml(
                snapshot = snapshot,
                pageIndex = pageIndex,
                totalPages = itemPages.size,
                pageItems = pageItems
            )
        }

        return wrapHtmlDocument(pages.joinToString("\n"), cssContent)
    }

    private fun generatePageHtml(
        snapshot: InvoiceSnapshot,
        pageIndex: Int,
        totalPages: Int,
        pageItems: List<com.emul8r.bizap.domain.model.LineItemSnapshot>
    ): String {
        val isFirstPage = pageIndex == 0
        val isLastPage = pageIndex == totalPages - 1

        return """
        <div class="page" data-page="${pageIndex + 1}">
            <div class="invoice-container">
                <!-- PAGE HEADER -->
                ${if (isFirstPage) generateFullHeader(snapshot) else generateContinuedHeader(snapshot, pageIndex)}

                <!-- ITEMS TABLE FOR THIS PAGE -->
                ${generateItemsTableForPage(pageItems)}

                <!-- CONTINUED INDICATOR (if not last page) -->
                ${if (!isLastPage) """
                <div class="continued-indicator">
                    <p>... continued on next page ...</p>
                </div>
                """ else ""}

                <!-- TOTALS & PAYMENT (Last page only) -->
                ${if (isLastPage) """
                <div class="totals-section">
                    ${generateTotalsBox(snapshot)}
                </div>

                <div class="payment-section">
                    ${generatePaymentDetails(snapshot)}
                </div>
                """ else ""}
            </div>

            <!-- PAGE FOOTER -->
            ${generatePageFooter(pageIndex + 1, totalPages)}
        </div>
        """
    }

    private fun generateFullHeader(snapshot: InvoiceSnapshot): String {
        val invoiceDate = java.text.SimpleDateFormat("dd-MM-yyyy").format(java.util.Date(snapshot.date))
        val dueDate = java.text.SimpleDateFormat("dd-MM-yyyy").format(java.util.Date(snapshot.dueDate))

        return """
        <div class="invoice-header">
            <div class="company-info">
                <h1>${snapshot.businessName}</h1>
                <p>${snapshot.businessEmail} | ${snapshot.businessPhone}</p>
                <p>${snapshot.businessAddress}</p>
                <p>ABN: ${snapshot.businessAbn}</p>
            </div>
            <div class="invoice-title">
                <h2>INVOICE</h2>
                <p>#${snapshot.invoiceNumber}</p>
            </div>
        </div>

        <div class="details-section">
            <div class="bill-to">
                <h3>BILL TO</h3>
                <p><strong>${snapshot.customerName}</strong></p>
                <p>${snapshot.customerAddress}</p>
            </div>
            <div class="invoice-details">
                <h3>DETAILS</h3>
                <p><strong>Invoice Date:</strong> $invoiceDate</p>
                <p><strong>Due Date:</strong> $dueDate</p>
                <p><strong>Currency:</strong> ${snapshot.currencyCode}</p>
            </div>
        </div>
        """
    }

    private fun generateContinuedHeader(snapshot: InvoiceSnapshot, pageIndex: Int): String = """
        <div class="page-header-continued">
            <p><strong>Invoice #${snapshot.invoiceNumber}</strong> (Page ${pageIndex + 1})</p>
            <p>Bill To: ${snapshot.customerName}</p>
        </div>
    """

    private fun generateItemsTableForPage(items: List<com.emul8r.bizap.domain.model.LineItemSnapshot>): String {
        val itemsHtml = items.joinToString("\n") { item ->
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

        return """
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
        """
    }

    private fun generateTotalsBox(snapshot: InvoiceSnapshot): String {
        val subtotalAmount = snapshot.subtotal / 100.0
        val taxAmount = snapshot.taxAmount / 100.0
        val totalAmount = snapshot.totalAmount / 100.0

        return """
        <div class="totals-box">
            <div class="totals-row">
                <span>Subtotal:</span>
                <span>${'$'}${String.format("%.2f", subtotalAmount)}</span>
            </div>
            <div class="totals-row">
                <span>Tax (${snapshot.taxRate}%):</span>
                <span>${'$'}${String.format("%.2f", taxAmount)}</span>
            </div>
            <div class="totals-row total-amount">
                <span>TOTAL:</span>
                <span>${'$'}${String.format("%.2f", totalAmount)}</span>
            </div>
        </div>
        """
    }

    private fun generatePaymentDetails(snapshot: InvoiceSnapshot): String = """
        <h3>PAYMENT DETAILS</h3>
        <p>Please transfer payment to:</p>
        <table class="payment-table">
            <tr>
                <td><strong>Bank:</strong></td>
                <td>${snapshot.bankName}</td>
            </tr>
            <tr>
                <td><strong>Account Name:</strong></td>
                <td>${snapshot.bankAccountName}</td>
            </tr>
            <tr>
                <td><strong>BSB:</strong></td>
                <td>${snapshot.bankBsb}</td>
            </tr>
            <tr>
                <td><strong>Account Number:</strong></td>
                <td>${snapshot.bankAccountNumber}</td>
            </tr>
        </table>
    """

    private fun generatePageFooter(pageNum: Int, totalPages: Int): String = """
        <div class="page-footer">
            <p>Page $pageNum of $totalPages</p>
            <p>${java.text.SimpleDateFormat("dd-MM-yyyy HH:mm").format(java.util.Date())}</p>
        </div>
    """

    private fun wrapHtmlDocument(pagesHtml: String, cssContent: String): String = """
    <!DOCTYPE html>
    <html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Invoice</title>
        <style>
            ${getAdvancedCSS()}
            $cssContent
        </style>
    </head>
    <body>
        $pagesHtml
    </body>
    </html>
    """

    private fun getAdvancedCSS(): String = """
        /* PAGE LAYOUT & BREAK CONTROL (from Phase 1) */
        * { box-sizing: border-box; }
        html, body { margin: 0; padding: 0; height: 100%; }

        .page {
            page-break-after: always;
            min-height: 11in;
            display: flex;
            flex-direction: column;
            padding: 0.75in;
            background: white;
        }

        .invoice-container {
            flex: 1;
            display: flex;
            flex-direction: column;
        }

        .section, .invoice-header, .details-section, .items-table,
        .totals-section, .payment-section, .notes-section {
            page-break-inside: avoid;
        }

        .invoice-header { margin-bottom: 25px; }
        .details-section { margin: 20px 0; display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
        .items-table { width: 100%; border-collapse: collapse; margin: 20px 0; }
        .items-table thead { page-break-inside: avoid; background-color: #f5f5f5; }
        .items-table tr { page-break-inside: avoid; }
        .items-table td, .items-table th { padding: 8px 5px; }
        .items-table tbody tr { border-bottom: 1px solid #e0e0e0; }

        .totals-section { margin: 25px 0; page-break-inside: avoid; }
        .totals-box { width: 50%; margin-left: auto; padding: 15px; background: #f9f9f9; border: 1px solid #ddd; }
        .totals-row { display: flex; justify-content: space-between; padding: 5px 0; }

        .payment-section { margin-top: 30px; padding-top: 20px; border-top: 1px solid #ccc; page-break-inside: avoid; }
        .footer { margin-top: auto; padding-top: 15px; border-top: 1px solid #ccc; text-align: center; font-size: 9pt; color: #666; }

        /* MULTI-PAGE SPECIFIC */
        .page-header-continued {
            border-bottom: 1px solid #ddd;
            padding-bottom: 10px;
            margin-bottom: 20px;
            page-break-inside: avoid;
        }

        .continued-indicator {
            text-align: center;
            color: #999;
            font-style: italic;
            margin: 20px 0;
            page-break-inside: avoid;
        }

        .page-footer {
            display: flex;
            justify-content: space-between;
            font-size: 9pt;
            color: #999;
            margin-top: 30px;
            border-top: 1px solid #ddd;
            padding-top: 10px;
            page-break-inside: avoid;
        }

        .payment-table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 10px;
        }

        .payment-table td {
            padding: 5px 0;
            border-bottom: 1px solid #eee;
        }

        .payment-table td:first-child {
            width: 30%;
        }
    """
}
