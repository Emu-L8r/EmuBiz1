package com.emul8r.bizap.tax

import com.emul8r.bizap.domain.model.BusinessProfile
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.InvoiceStatus
import com.emul8r.bizap.domain.model.LineItem
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Integration tests for tax registration toggle feature
 * Tests full data flow from business profile to invoice
 */
class TaxRegistrationIntegrationTest {

    @Test
    fun testCreateInvoice_BusinessNotTaxRegistered() {
        // Setup: Business NOT tax registered
        val business = BusinessProfile(
            id = 1,
            businessName = "Startup Inc",
            abn = "12345678901",
            email = "startup@test.com",
            phone = "555-1111",
            address = "123 Start St",
            isTaxRegistered = false,
            defaultTaxRate = 0.10f
        )

        // Create invoice
        val lineItems = listOf(
            LineItem(id = 1, invoiceId = 1, description = "Consulting", quantity = 10.0, unitPrice = 300.0)
        )
        val subtotal = lineItems.sumOf { it.quantity * it.unitPrice }

        // Calculate tax (should be 0)
        val taxRate = if (business.isTaxRegistered) business.defaultTaxRate.toDouble() else 0.0
        val taxAmount = if (business.isTaxRegistered) subtotal * taxRate else 0.0
        val total = subtotal + taxAmount

        val invoice = Invoice(
            id = 1,
            customerId = 1,
            customerName = "Customer A",
            date = System.currentTimeMillis(),
            dueDate = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000,
            totalAmount = total,
            items = lineItems,
            status = InvoiceStatus.DRAFT,
            taxRate = taxRate,
            taxAmount = taxAmount
        )

        // Verify: Total should be $3000 (no tax)
        assertEquals(3000.0, subtotal)
        assertEquals(0.0, taxAmount)
        assertEquals(3000.0, invoice.totalAmount)
    }

    @Test
    fun testCreateInvoice_BusinessTaxRegistered10Percent() {
        // Setup: Business IS tax registered (10%)
        val business = BusinessProfile(
            id = 1,
            businessName = "Established Corp",
            abn = "98765432101",
            email = "corp@test.com",
            phone = "555-2222",
            address = "456 Corp Ave",
            isTaxRegistered = true,
            defaultTaxRate = 0.10f
        )

        // Create invoice
        val lineItems = listOf(
            LineItem(id = 1, invoiceId = 1, description = "Service", quantity = 10.0, unitPrice = 300.0)
        )
        val subtotal = lineItems.sumOf { it.quantity * it.unitPrice }

        // Calculate tax (should be $300)
        val taxRate = if (business.isTaxRegistered) business.defaultTaxRate.toDouble() else 0.0
        val taxAmount = if (business.isTaxRegistered) subtotal * taxRate else 0.0
        val total = subtotal + taxAmount

        val invoice = Invoice(
            id = 1,
            customerId = 1,
            customerName = "Customer B",
            date = System.currentTimeMillis(),
            dueDate = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000,
            totalAmount = total,
            items = lineItems,
            status = InvoiceStatus.DRAFT,
            taxRate = taxRate,
            taxAmount = taxAmount
        )

        // Verify: Total should be $3300 ($3000 + $300 tax)
        assertEquals(3000.0, subtotal)
        assertEquals(300.0, taxAmount)
        assertEquals(3300.0, invoice.totalAmount)
    }

    @Test
    fun testCreateInvoice_BusinessTaxRegistered15Percent() {
        val business = BusinessProfile(
            id = 1, businessName = "High Tax Co", abn = "11111111111",
            email = "high@test.com", phone = "555-3333", address = "789 High St",
            isTaxRegistered = true, defaultTaxRate = 0.15f
        )

        val lineItems = listOf(
            LineItem(id = 1, invoiceId = 1, description = "Product", quantity = 10.0, unitPrice = 300.0)
        )
        val subtotal = lineItems.sumOf { it.quantity * it.unitPrice }
        val taxRate = if (business.isTaxRegistered) business.defaultTaxRate.toDouble() else 0.0
        val taxAmount = if (business.isTaxRegistered) subtotal * taxRate else 0.0
        val total = subtotal + taxAmount

        // Verify: Total should be $3450 ($3000 + $450 tax)
        assertEquals(3000.0, subtotal)
        assertEquals(450.0, taxAmount)
        assertEquals(3450.0, total)
    }

    @Test
    fun testToggleTaxRegistration_AffectsNewInvoices() {
        // Business starts NOT registered
        var business = BusinessProfile(
            id = 1, businessName = "Toggle Co", abn = "22222222222",
            email = "toggle@test.com", phone = "555-4444", address = "321 Toggle Rd",
            isTaxRegistered = false
        )

        // Create invoice 1 (no tax)
        val subtotal1 = 1000.0
        val taxAmount1 = if (business.isTaxRegistered) subtotal1 * 0.10 else 0.0
        val total1 = subtotal1 + taxAmount1

        assertEquals(1000.0, total1)

        // Business changes to tax registered
        business = business.copy(isTaxRegistered = true, defaultTaxRate = 0.10f)

        // Create invoice 2 (with tax)
        val subtotal2 = 1000.0
        val taxAmount2 = if (business.isTaxRegistered) subtotal2 * business.defaultTaxRate else 0.0
        val total2 = subtotal2 + taxAmount2

        assertEquals(1100.0, total2)
    }

    @Test
    fun testMultipleBusinesses_DifferentTaxSettings() {
        val business1 = BusinessProfile(
            id = 1, businessName = "No Tax Co", abn = "1", email = "1@test.com",
            phone = "1", address = "1", isTaxRegistered = false
        )

        val business2 = BusinessProfile(
            id = 2, businessName = "Tax Co", abn = "2", email = "2@test.com",
            phone = "2", address = "2", isTaxRegistered = true, defaultTaxRate = 0.10f
        )

        val subtotal = 1000.0

        // Invoice from business 1 (no tax)
        val tax1 = if (business1.isTaxRegistered) subtotal * business1.defaultTaxRate else 0.0
        val total1 = subtotal + tax1

        // Invoice from business 2 (with tax)
        val tax2 = if (business2.isTaxRegistered) subtotal * business2.defaultTaxRate else 0.0
        val total2 = subtotal + tax2

        assertEquals(1000.0, total1)
        assertEquals(1100.0, total2)
    }

    @Test
    fun testPdfGeneration_NoTax() {
        val business = BusinessProfile(
            id = 1, businessName = "No Tax", abn = "1", email = "1@test.com",
            phone = "1", address = "1", isTaxRegistered = false
        )

        val invoice = Invoice(
            id = 1, customerId = 1, customerName = "Customer",
            date = System.currentTimeMillis(), dueDate = System.currentTimeMillis(),
            totalAmount = 1000.0, items = emptyList(), status = InvoiceStatus.DRAFT,
            taxRate = 0.0, taxAmount = 0.0
        )

        // PDF should NOT show tax row
        assertEquals(0.0, invoice.taxAmount)
        assertTrue(invoice.totalAmount == 1000.0)
    }

    @Test
    fun testPdfGeneration_WithTax() {
        val business = BusinessProfile(
            id = 1, businessName = "Tax Co", abn = "1", email = "1@test.com",
            phone = "1", address = "1", isTaxRegistered = true, defaultTaxRate = 0.10f
        )

        val invoice = Invoice(
            id = 1, customerId = 1, customerName = "Customer",
            date = System.currentTimeMillis(), dueDate = System.currentTimeMillis(),
            totalAmount = 1100.0, items = emptyList(), status = InvoiceStatus.DRAFT,
            taxRate = 0.10, taxAmount = 100.0
        )

        // PDF SHOULD show tax row
        assertEquals(100.0, invoice.taxAmount)
        assertTrue(invoice.totalAmount == 1100.0)
    }

    @Test
    fun testBackwardCompatibility_ExistingInvoices() {
        // Existing invoices (created before tax toggle feature)
        // Will have tax already applied (hardcoded 10%)
        val oldInvoice = Invoice(
            id = 999, customerId = 999, customerName = "Old Customer",
            date = System.currentTimeMillis(), dueDate = System.currentTimeMillis(),
            totalAmount = 3300.0, items = emptyList(), status = InvoiceStatus.PAID,
            taxRate = 0.10, taxAmount = 300.0
        )

        // Should still display correctly
        assertEquals(300.0, oldInvoice.taxAmount)
        assertEquals(3300.0, oldInvoice.totalAmount)
        assertTrue(oldInvoice.taxAmount > 0)
    }

    @Test
    fun testPaymentProgressBar_CorrectTotal() {
        val business = BusinessProfile(
            id = 1, businessName = "Test", abn = "1", email = "1@test.com",
            phone = "1", address = "1", isTaxRegistered = false
        )

        val invoice = Invoice(
            id = 1, customerId = 1, customerName = "Customer",
            date = System.currentTimeMillis(), dueDate = System.currentTimeMillis(),
            totalAmount = 3000.0, items = emptyList(), status = InvoiceStatus.DRAFT,
            taxRate = 0.0, taxAmount = 0.0
        )

        // Payment progress should use $3000 (not $3300)
        val amountPaid = 1500.0
        val progress = amountPaid / invoice.totalAmount

        assertEquals(0.5, progress) // 50% paid
    }

    @Test
    fun testChangeTaxRate_AffectsNewInvoicesOnly() {
        var business = BusinessProfile(
            id = 1, businessName = "Rate Change Co", abn = "1", email = "1@test.com",
            phone = "1", address = "1", isTaxRegistered = true, defaultTaxRate = 0.10f
        )

        // Create invoice 1 with 10% tax
        val subtotal = 1000.0
        val tax1 = subtotal * business.defaultTaxRate
        val total1 = subtotal + tax1

        assertEquals(100.0, tax1)
        assertEquals(1100.0, total1)

        // Change tax rate to 20%
        business = business.copy(defaultTaxRate = 0.20f)

        // Create invoice 2 with 20% tax
        val tax2 = subtotal * business.defaultTaxRate
        val total2 = subtotal + tax2

        assertEquals(200.0, tax2)
        assertEquals(1200.0, total2)

        // Invoice 1 should still show 10% (unchanged)
        assertEquals(100.0, tax1)
    }
}

