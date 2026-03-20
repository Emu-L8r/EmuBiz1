package com.emul8r.bizap.performance

import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.InvoiceStatus
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Performance baseline tests
 * Verifies that operations complete within acceptable timeframes
 */
class PerformanceBaselineTest {

    // ========================================
    // Invoice Creation Performance Tests
    // ========================================

    @Test
    fun `performance - invoice creation completes quickly`() = runTest {
        // Given
        val startTime = System.currentTimeMillis()

        // When - Create invoice (simulated)
        val invoice = Invoice(
            id = 0,
            businessProfileId = 1L,
            customerId = 1L,
            customerName = "Test",
            customerAddress = "123 St",
            customerEmail = "test@example.com",
            items = emptyList(),
            totalAmount = 1000L,
            amountPaid = 0L,
            status = InvoiceStatus.DRAFT,
            date = System.currentTimeMillis(),
            dueDate = System.currentTimeMillis() + 86400000,
            isQuote = false,
            currencyCode = "AUD",
            taxRate = 0.0,
            taxAmount = 0L,
            invoiceYear = 2026,
            invoiceSequence = 1,
            notes = ""
        )

        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime

        // Then - Should complete within reasonable time
        assertTrue(
            duration < 100,
            "Invoice creation should complete within 100ms, took ${duration}ms"
        )
    }

    @Test
    fun `performance - bulk invoice creation`() = runTest {
        // Given
        val invoiceCount = 1000
        val startTime = System.currentTimeMillis()

        // When - Create multiple invoices
        val invoices = (1..invoiceCount).map { i ->
            Invoice(
                id = i.toLong(),
                businessProfileId = 1L,
                customerId = (i % 10).toLong(),
                customerName = "Customer $i",
                customerAddress = "Address $i",
                customerEmail = "email$i@example.com",
                items = emptyList(),
                totalAmount = (i * 100).toLong(),
                amountPaid = 0L,
                status = InvoiceStatus.DRAFT,
                date = System.currentTimeMillis(),
                dueDate = System.currentTimeMillis(),
                isQuote = false,
                currencyCode = "AUD",
                taxRate = 0.0,
                taxAmount = 0L,
                invoiceYear = 2026,
                invoiceSequence = i,
                notes = ""
            )
        }

        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime

        // Then
        assertTrue(
            duration < 1000,
            "Creating $invoiceCount invoices should complete within 1s, took ${duration}ms"
        )
        assertTrue(invoices.size == invoiceCount)
    }

    // ========================================
    // Payment Recording Performance Tests
    // ========================================

    @Test
    fun `performance - payment recording completes quickly`() = runTest {
        // Given
        val invoice = Invoice(
            id = 1L,
            businessProfileId = 1L,
            customerId = 1L,
            customerName = "Test",
            customerAddress = "",
            customerEmail = "",
            items = emptyList(),
            totalAmount = 1000L,
            amountPaid = 0L,
            status = InvoiceStatus.SENT,
            date = System.currentTimeMillis(),
            dueDate = System.currentTimeMillis(),
            isQuote = false,
            currencyCode = "AUD",
            taxRate = 0.0,
            taxAmount = 0L,
            invoiceYear = 2026,
            invoiceSequence = 1,
            notes = ""
        )

        val startTime = System.currentTimeMillis()

        // When - Record payment
        val updated = invoice.copy(amountPaid = 500L)

        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime

        // Then
        assertTrue(
            duration < 50,
            "Payment recording should complete within 50ms, took ${duration}ms"
        )
        assertTrue(updated.amountPaid == 500L)
    }

    // ========================================
    // List Processing Performance Tests
    // ========================================

    @Test
    fun `performance - processing 100 invoices`() = runTest {
        // Given
        val invoices = (1..100).map { i ->
            Invoice(
                id = i.toLong(),
                businessProfileId = 1L,
                customerId = 1L,
                customerName = "Customer",
                customerAddress = "",
                customerEmail = "",
                items = emptyList(),
                totalAmount = 1000L,
                amountPaid = if (i % 2 == 0) 1000L else 0L,
                status = if (i % 2 == 0) InvoiceStatus.PAID else InvoiceStatus.SENT,
                date = System.currentTimeMillis(),
                dueDate = System.currentTimeMillis(),
                isQuote = false,
                currencyCode = "AUD",
                taxRate = 0.0,
                taxAmount = 0L,
                invoiceYear = 2026,
                invoiceSequence = i,
                notes = ""
            )
        }

        val startTime = System.currentTimeMillis()

        // When - Process invoices
        val paidCount = invoices.count { it.status == InvoiceStatus.PAID }
        val totalOutstanding = invoices.sumOf { it.totalAmount - it.amountPaid }

        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime

        // Then
        assertTrue(
            duration < 100,
            "Processing 100 invoices should complete within 100ms, took ${duration}ms"
        )
        assertTrue(paidCount == 50)
    }

    // ========================================
    // String Processing Performance Tests
    // ========================================

    @Test
    fun `performance - string formatting`() = runTest {
        val startTime = System.currentTimeMillis()

        // When - Format strings
        val formatted = (1..1000).map { i ->
            "Invoice #INV-2026-%06d".format(i)
        }

        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime

        // Then
        assertTrue(
            duration < 100,
            "Formatting 1000 strings should complete within 100ms, took ${duration}ms"
        )
        assertTrue(formatted.size == 1000)
    }

    // ========================================
    // Calculation Performance Tests
    // ========================================

    @Test
    fun `performance - tax calculation`() = runTest {
        val amounts = (1..1000).map { it * 100L }
        val taxRate = 10.0

        val startTime = System.currentTimeMillis()

        // When - Calculate tax for multiple invoices
        val results = amounts.map { amount ->
            val taxAmount = (amount * taxRate / 100).toLong()
            amount + taxAmount
        }

        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime

        // Then
        assertTrue(
            duration < 50,
            "Calculating tax for 1000 amounts should complete within 50ms, took ${duration}ms"
        )
        assertTrue(results.size == 1000)
    }

    // ========================================
    // Memory Efficiency Tests
    // ========================================

    @Test
    fun `memory - creating large invoice list should be efficient`() = runTest {
        val runtime = Runtime.getRuntime()
        val memBefore = runtime.totalMemory() - runtime.freeMemory()

        // When - Create large list
        @Suppress("UNUSED_VARIABLE")
        val invoices = (1..10000).map { i ->
            Invoice(
                id = i.toLong(),
                businessProfileId = 1L,
                customerId = (i % 100).toLong(),
                customerName = "Customer $i",
                customerAddress = "Address",
                customerEmail = "email@example.com",
                items = emptyList(),
                totalAmount = 1000L,
                amountPaid = 0L,
                status = InvoiceStatus.DRAFT,
                date = System.currentTimeMillis(),
                dueDate = System.currentTimeMillis(),
                isQuote = false,
                currencyCode = "AUD",
                taxRate = 0.0,
                taxAmount = 0L,
                invoiceYear = 2026,
                invoiceSequence = i,
                notes = ""
            )
        }

        val memAfter = runtime.totalMemory() - runtime.freeMemory()
        val memUsed = memAfter - memBefore

        // Then - Memory usage should be reasonable
        assertTrue(
            memUsed < 50_000_000, // Less than 50MB
            "Creating 10000 invoices should use less than 50MB, used ${memUsed / 1_000_000}MB"
        )
    }
}

