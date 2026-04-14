package com.emul8r.bizap.domain.model

import java.util.UUID

/**
 * Generates realistic placeholder invoice data for preview/testing purposes.
 * Used in settings screen to show how invoices will look without real business data.
 *
 * All monetary amounts are stored as Long (cents) per InvoiceSnapshot convention.
 */
object PlaceholderInvoiceGenerator {

    /**
     * Generate a complete placeholder invoice snapshot for preview purposes.
     * All data is realistic but fictional.
     *
     * Amounts are in cents (e.g., $1500.00 = 150000L)
     */
    fun generatePreviewInvoice(): InvoiceSnapshot {
        val now = System.currentTimeMillis()
        val dueDateMs = now + (30 * 24 * 60 * 60 * 1000) // 30 days from now

        // Create sample items with prices in cents
        val items = listOf(
            LineItemSnapshot(
                description = "Professional Services - Consulting",
                quantity = 1.0,
                unitPrice = 150000L,  // $1500.00
                total = 150000L
            ),
            LineItemSnapshot(
                description = "Software Development (40 hours @ $125/hr)",
                quantity = 40.0,
                unitPrice = 12500L,   // $125.00
                total = 500000L       // 40 × $125 = $5000
            ),
            LineItemSnapshot(
                description = "Design Work - UI/UX",
                quantity = 1.0,
                unitPrice = 80000L,   // $800.00
                total = 80000L
            )
        )

        // Calculate totals in cents
        val subtotal = 150000L + 500000L + 80000L  // $7300.00 = 730000 cents
        val taxRate = 0.10  // 10% GST
        val tax = (subtotal * taxRate).toLong()    // $730.00 = 73000 cents
        val total = subtotal + tax                 // $8030.00 = 803000 cents

        return InvoiceSnapshot(
            invoiceId = 12345L,
            invoiceNumber = "INV-2026-04-001",
            displayName = "Invoice 2026-04-001",
            customerName = "John Smith",
            customerEmail = "john.smith@example.com",
            customerAddress = "456 Customer Avenue, Melbourne VIC 3000, Australia",
            date = now,
            dueDate = dueDateMs,
            items = items,
            subtotal = subtotal,
            taxRate = taxRate,
            taxAmount = tax,
            totalAmount = total,
            businessName = "ACME Corporation Pty Ltd",
            businessAbn = "45 832 010 284",
            businessEmail = "contact@acmecorp.com.au",
            businessPhone = "+61 (2) 5555 1234",
            businessAddress = "123 Business Street, Suite 500, Sydney NSW 2000, Australia",
            logoBase64 = null,
            currencyCode = "AUD",
            header = "",
            subheader = "",
            footerText = "",
            notes = "Thank you for your business. Please remit payment by the due date to maintain account status.",
            bankAccountName = "ACME Corporation Pty Ltd Operating Account",
            bankAccountNumber = "123456789",
            bankBsb = "06-222-245",
            bankName = "Commonwealth Bank of Australia",
            invoiceStatus = "DRAFT"
        )
    }

    /**
     * Generate placeholder data for a quote (same structure as invoice).
     * Used to preview what quotes will look like.
     */
    fun generatePreviewQuote(): InvoiceSnapshot {
        return generatePreviewInvoice().copy(
            invoiceNumber = "QTE-2026-04-001",
            displayName = "Quote 2026-04-001",
            invoiceStatus = "DRAFT"
        )
    }
}



