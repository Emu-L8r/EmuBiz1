package com.emul8r.bizap.data.service.preview

import com.emul8r.bizap.domain.model.LineItemSnapshot
import com.emul8r.bizap.domain.model.InvoiceSnapshot

/**
 * Placeholder Invoice Generator
 *
 * Generates realistic sample invoice data for preview purposes.
 * Used when:
 * - User enables "Preview with placeholder data" mode
 * - Testing PDF layouts without real invoice data
 * - Demonstrating PDF generation capabilities
 *
 * NOTE: All monetary values are stored as Long (cents) per InvoiceSnapshot spec
 */
object PlaceholderInvoiceGenerator {
    fun generatePreviewInvoice(): InvoiceSnapshot {
        val now = System.currentTimeMillis()
        val thirtyDaysLater = now + (30 * 24 * 60 * 60 * 1000)

        return InvoiceSnapshot(
            invoiceId = System.currentTimeMillis(),
            invoiceNumber = "INV-2026-04-001",
            displayName = "ACME Corporation Invoice",
            customerName = "Smith & Associates, Inc.",
            customerAddress = "456 Customer Street\nNew York, NY 10001\nUSA",
            customerEmail = "billing@smith.com",
            date = now,
            dueDate = thirtyDaysLater,
            items = listOf(
                LineItemSnapshot(
                    description = "Professional Services - April 2026",
                    quantity = 40.0,
                    unitPrice = 15000L,
                    total = 600000L
                ),
                LineItemSnapshot(
                    description = "Software License (Annual)",
                    quantity = 1.0,
                    unitPrice = 50000L,
                    total = 50000L
                ),
                LineItemSnapshot(
                    description = "Support & Maintenance",
                    quantity = 1.0,
                    unitPrice = 30000L,
                    total = 30000L
                )
            ),
            subtotal = 680000L,
            taxRate = 0.10,
            taxAmount = 68000L,
            totalAmount = 748000L,
            businessName = "ACME Corporation",
            businessAbn = "45 832 010 284",
            businessEmail = "contact@acme.com",
            businessPhone = "+1 (555) 123-4567",
            businessAddress = "123 Business Avenue, Suite 100\nSan Francisco, CA 94102\nUSA",
            logoBase64 = null,
            currencyCode = "USD",
            headerText = "Thank you for your business!",
            subheaderText = "Professional Services Invoice",
            footerText = "Payment is due within 30 days of invoice date. Thank you for your business!",
            notes = "Notes: Thank you for choosing ACME Corporation for your professional services needs. If you have any questions about this invoice, please don't hesitate to contact us at contact@acme.com or call +1 (555) 123-4567.",
            bankAccountName = "ACME Operating Account",
            bankAccountNumber = "123456789",
            bankBsb = "06-222-245",
            bankName = "Commonwealth Bank of Australia",
            invoiceStatus = "SENT"
        )
    }

    /**
     * Generate preview invoice with custom business name (useful for testing).
     */
    fun generatePreviewInvoiceWithBusinessName(businessName: String): InvoiceSnapshot {
        return generatePreviewInvoice().copy(
            businessName = businessName,
            invoiceNumber = "INV-2026-04-${System.currentTimeMillis() % 1000}"
        )
    }

    /**
     * Generate minimal preview invoice (tests sparse data).
     */
    fun generateMinimalPreviewInvoice(): InvoiceSnapshot {
        val now = System.currentTimeMillis()
        val thirtyDaysLater = now + (30 * 24 * 60 * 60 * 1000)

        return InvoiceSnapshot(
            invoiceId = System.currentTimeMillis(),
            invoiceNumber = "INV-001",
            displayName = "Minimal Invoice",
            customerName = "Test Customer",
            customerAddress = "Test Address",
            customerEmail = "test@example.com",
            date = now,
            dueDate = thirtyDaysLater,
            items = listOf(
                LineItemSnapshot(
                    description = "Service",
                    quantity = 1.0,
                    unitPrice = 100000L,
                    total = 100000L
                )
            ),
            subtotal = 100000L,
            taxRate = 0.10,
            taxAmount = 10000L,
            totalAmount = 110000L,
            businessName = "Test Business",
            businessAbn = "",
            businessEmail = "test@example.com",
            businessPhone = "",
            businessAddress = "123 Test St",
            logoBase64 = null,
            currencyCode = "USD"
        )
    }
}

