package com.emul8r.bizap.data.service

import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.InvoiceStatus
import org.junit.Test
import java.util.*
import kotlin.test.assertTrue

class InvoiceCsvExporterTest {

    @Test
    fun `generateCsv correctly formats standard data`() {
        val invoices = listOf(
            createTestInvoice("INV-001", "Customer A", 10000L, "AUD")
        )
        val csv = InvoiceCsvExporter.generateCsv(invoices)
        
        assertTrue(csv.contains("Invoice Number,Date,Customer,Total Amount,Currency,Status,Items Count"))
        assertTrue(csv.contains("INV-001"))
        assertTrue(csv.contains("Customer A"))
        assertTrue(csv.contains("100.00"))
        assertTrue(csv.contains("AUD"))
    }

    @Test
    fun `generateCsv escapes special characters`() {
        val invoices = listOf(
            createTestInvoice("INV,002", "Customer \"Quotes\" B", 5000L, "USD")
        )
        val csv = InvoiceCsvExporter.generateCsv(invoices)
        
        // Commas should be wrapped in quotes
        assertTrue(csv.contains("\"INV,002\""))
        // Quotes should be escaped as double-quotes and wrapped
        assertTrue(csv.contains("\"Customer \"\"Quotes\"\" B\""))
    }

    @Test
    fun `generateCsv handles empty list`() {
        val csv = InvoiceCsvExporter.generateCsv(emptyList())
        assertTrue(csv.startsWith("Invoice Number,Date,Customer,Total Amount,Currency,Status,Items Count"))
        assertTrue(csv.lines().size <= 2) // Header + possibly trailing newline
    }

    private fun createTestInvoice(number: String, customer: String, amount: Long, currency: String): Invoice {
        return Invoice(
            id = 1,
            customerName = customer,
            totalAmount = amount,
            currencyCode = currency,
            date = 1709337600000L, // 2024-03-02
            status = InvoiceStatus.PAID,
            items = emptyList(),
            customerId = 1,
            isQuote = false
        ).let { it.copy(invoiceYear = 2024, invoiceSequence = 1) }
    }
}
