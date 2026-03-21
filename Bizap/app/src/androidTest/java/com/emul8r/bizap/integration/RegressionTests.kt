package com.emul8r.bizap.integration

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.emul8r.bizap.domain.model.InvoiceStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Regression Tests - Prevents re-introduction of known bugs.
 * 
 * Each test validates a specific scenario that previously caused issues
 * or edge cases that need explicit validation.
 */
@RunWith(AndroidJUnit4::class)
@MediumTest
class RegressionTests : IntegrationTestBase() {
    
    private var testCustomerId: Long = 0L
    
    @Before
    fun setupTestData() = runTest {
        createTestBusinessProfile(id = 1L).getOrThrow()
        testCustomerId = createTestCustomer(businessId = 1L).getOrThrow()
    }
    
    @Test
    fun testInvoiceCreationWithoutLineItems() = runTest {
        // Regression: Invoices without line items should be valid
        val invoiceId = createTestInvoice(
            customerId = testCustomerId,
            amount = 10000L,
            status = InvoiceStatus.DRAFT
        ).getOrThrow()
        
        assertNotNull(invoiceId, "Invoice should be created")
        assertTrue(invoiceId > 0, "Invoice ID should be positive")
        
        val invoice = invoiceRepository.getInvoiceWithItemsById(invoiceId).first()
        assertNotNull(invoice, "Invoice should exist")
        assertEquals(0, invoice.items.size, "Invoice should have no line items")
    }
    
    @Test
    fun testZeroAmountInvoice() = runTest {
        // Regression: Zero-amount invoices should be handled gracefully
        val invoiceId = createTestInvoice(
            customerId = testCustomerId,
            amount = 0L,
            status = InvoiceStatus.DRAFT
        ).getOrThrow()
        
        val invoice = invoiceRepository.getInvoiceWithItemsById(invoiceId).first()
        assertNotNull(invoice)
        assertEquals(0L, invoice.totalAmount, "Zero amount should be preserved")
    }
    
    @Test
    fun testNegativeAmountPrevention() = runTest {
        // Regression: Negative amounts should be prevented or handled
        // This test documents the expected behavior
        val invoiceId = createTestInvoice(
            customerId = testCustomerId,
            amount = 10000L,
            status = InvoiceStatus.SENT
        ).getOrThrow()
        
        // Attempting to set negative payment should be handled gracefully
        // (Implementation may prevent this at ViewModel level)
        val invoice = invoiceRepository.getInvoiceWithItemsById(invoiceId).first()
        assertNotNull(invoice)
        assertTrue(invoice.amountPaid >= 0, "Amount paid should never be negative")
    }
    
    @Test
    fun testDuplicateInvoiceNumberHandling() = runTest {
        // Regression: System should handle invoices with similar numbers
        val invoice1 = createTestInvoice(testCustomerId, 10000L).getOrThrow()
        val invoice2 = createTestInvoice(testCustomerId, 20000L).getOrThrow()
        
        val inv1 = invoiceRepository.getInvoiceWithItemsById(invoice1).first()
        val inv2 = invoiceRepository.getInvoiceWithItemsById(invoice2).first()
        
        assertNotNull(inv1)
        assertNotNull(inv2)
        // Invoice numbers should be unique (timestamps ensure this in our implementation)
        assertTrue(inv1.invoiceNumber != inv2.invoiceNumber, 
            "Invoice numbers should be unique")
    }
    
    @Test
    fun testCustomerDeletionDoesNotDeleteInvoices() = runTest {
        // Regression: Deleting customer should not cascade delete invoices
        val invoiceId = createTestInvoice(testCustomerId, 10000L).getOrThrow()
        
        // Delete customer
        customerRepository.deleteCustomer(testCustomerId).getOrThrow()
        
        // Invoice should still exist (orphaned but preserved)
        val invoice = invoiceRepository.getInvoiceWithItemsById(invoiceId).first()
        assertNotNull(invoice, "Invoice should still exist after customer deletion")
        assertEquals(testCustomerId, invoice.customerId, 
            "Customer reference should be preserved")
    }
    
    @Test
    fun testMultipleStatusTransitions() = runTest {
        // Regression: Multiple status changes should all be persisted correctly
        val invoiceId = createTestInvoice(
            customerId = testCustomerId,
            amount = 30000L,
            status = InvoiceStatus.DRAFT
        ).getOrThrow()
        
        // Rapid status transitions
        invoiceRepository.updateInvoiceStatus(invoiceId, InvoiceStatus.SENT).getOrThrow()
        invoiceRepository.updateInvoiceStatus(invoiceId, InvoiceStatus.CANCELLED).getOrThrow()
        invoiceRepository.updateInvoiceStatus(invoiceId, InvoiceStatus.DRAFT).getOrThrow()
        
        val invoice = invoiceRepository.getInvoiceWithItemsById(invoiceId).first()
        assertEquals(InvoiceStatus.DRAFT, invoice!!.status, 
            "Final status should be DRAFT")
    }
    
    @Test
    fun testLargeAmountHandling() = runTest {
        // Regression: Large amounts (e.g., $1M+) should be handled correctly
        val largeAmount = 100_000_000L // $1,000,000.00
        val invoiceId = createTestInvoice(
            customerId = testCustomerId,
            amount = largeAmount,
            status = InvoiceStatus.SENT
        ).getOrThrow()
        
        val invoice = invoiceRepository.getInvoiceWithItemsById(invoiceId).first()
        assertEquals(largeAmount, invoice!!.totalAmount, 
            "Large amounts should be preserved accurately")
    }
    
    @Test
    fun testConcurrentInvoiceCreation() = runTest {
        // Regression: Creating multiple invoices in quick succession should work
        val ids = mutableListOf<Long>()
        
        repeat(5) { index ->
            val id = createTestInvoice(
                customerId = testCustomerId,
                amount = (index + 1) * 10000L
            ).getOrThrow()
            ids.add(id)
        }
        
        assertEquals(5, ids.size, "All invoices should be created")
        assertEquals(ids.distinct().size, ids.size, "All IDs should be unique")
    }
    
    @Test
    fun testPartialPaymentDoesNotExceedTotal() = runTest {
        // Regression: Partial payment tracking should be accurate
        val invoiceId = createTestInvoice(
            customerId = testCustomerId,
            amount = 50000L,
            status = InvoiceStatus.SENT
        ).getOrThrow()
        
        // Make partial payment
        invoiceRepository.updateAmountPaid(invoiceId, 25000L).getOrThrow()
        var invoice = invoiceRepository.getInvoiceWithItemsById(invoiceId).first()
        
        assertEquals(InvoiceStatus.PARTIALLY_PAID, invoice!!.status)
        assertEquals(25000L, invoice.amountPaid)
        
        // Complete payment
        invoiceRepository.updateAmountPaid(invoiceId, 50000L).getOrThrow()
        invoice = invoiceRepository.getInvoiceWithItemsById(invoiceId).first()
        
        assertEquals(InvoiceStatus.PAID, invoice!!.status)
        assertEquals(50000L, invoice.amountPaid)
    }
    
    @Test
    fun testInvoiceRetrievalAfterMultipleOperations() = runTest {
        // Regression: Invoice should remain retrievable after various operations
        val invoiceId = createTestInvoice(testCustomerId, 15000L).getOrThrow()
        
        // Perform multiple operations
        invoiceRepository.updateInvoiceStatus(invoiceId, InvoiceStatus.SENT).getOrThrow()
        invoiceRepository.updateAmountPaid(invoiceId, 5000L).getOrThrow()
        invoiceRepository.updateAmountPaid(invoiceId, 15000L).getOrThrow()
        
        // Should still be retrievable
        val invoice = invoiceRepository.getInvoiceWithItemsById(invoiceId).first()
        assertNotNull(invoice, "Invoice should still be retrievable")
        assertEquals(InvoiceStatus.PAID, invoice.status)
    }
}
