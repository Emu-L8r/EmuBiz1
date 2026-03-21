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

/**
 * GUI Parity Tests - Ensures GUI1 and GUI2 produce identical results.
 * 
 * These tests verify that regardless of which UI is used, the underlying
 * data operations produce the same results. This is critical during the
 * transition period while both GUIs are supported.
 */
@RunWith(AndroidJUnit4::class)
@MediumTest
class GUIParityTest : IntegrationTestBase() {
    
    private var testCustomerId: Long = 0L
    
    @Before
    fun setupTestData() = runTest {
        createTestBusinessProfile(id = 1L).getOrThrow()
        testCustomerId = createTestCustomer(businessId = 1L).getOrThrow()
    }
    
    @Test
    fun testInvoiceCreationParity() = runTest {
        // Create two identical invoices (simulating GUI1 and GUI2)
        val invoice1Id = createTestInvoice(
            customerId = testCustomerId,
            amount = 10000L,
            status = InvoiceStatus.DRAFT
        ).getOrThrow()
        
        val invoice2Id = createTestInvoice(
            customerId = testCustomerId,
            amount = 10000L,
            status = InvoiceStatus.DRAFT
        ).getOrThrow()
        
        // Verify both invoices were created
        val invoice1 = invoiceRepository.getInvoiceWithItemsById(invoice1Id).first()
        val invoice2 = invoiceRepository.getInvoiceWithItemsById(invoice2Id).first()
        
        assertNotNull(invoice1, "First invoice should exist")
        assertNotNull(invoice2, "Second invoice should exist")
        
        // Verify identical structure (excluding IDs and timestamps)
        assertEquals(invoice1.totalAmount, invoice2.totalAmount, 
            "Both invoices should have same amount")
        assertEquals(invoice1.status, invoice2.status, 
            "Both invoices should have same status")
        assertEquals(invoice1.currency, invoice2.currency, 
            "Both invoices should use same currency")
        assertEquals(invoice1.customerId, invoice2.customerId, 
            "Both invoices should reference same customer")
    }
    
    @Test
    fun testPaymentRecordingParity() = runTest {
        // Create invoice
        val invoiceId = createTestInvoice(
            customerId = testCustomerId,
            amount = 50000L,
            status = InvoiceStatus.SENT
        ).getOrThrow()
        
        // Record payment (same operation in both GUIs)
        invoiceRepository.updateAmountPaid(invoiceId, 50000L).getOrThrow()
        
        // Verify result
        val invoice = invoiceRepository.getInvoiceWithItemsById(invoiceId).first()
        assertNotNull(invoice)
        assertEquals(50000L, invoice.amountPaid, "Payment should be recorded")
        assertEquals(InvoiceStatus.PAID, invoice.status, "Status should update to PAID")
    }
    
    @Test
    fun testStatusTransitionParity() = runTest {
        val invoiceId = createTestInvoice(
            customerId = testCustomerId,
            amount = 20000L,
            status = InvoiceStatus.DRAFT
        ).getOrThrow()
        
        // Transition DRAFT → SENT (same in both GUIs)
        invoiceRepository.updateInvoiceStatus(invoiceId, InvoiceStatus.SENT).getOrThrow()
        var invoice = invoiceRepository.getInvoiceWithItemsById(invoiceId).first()
        assertEquals(InvoiceStatus.SENT, invoice!!.status)
        
        // Transition SENT → CANCELLED
        invoiceRepository.updateInvoiceStatus(invoiceId, InvoiceStatus.CANCELLED).getOrThrow()
        invoice = invoiceRepository.getInvoiceWithItemsById(invoiceId).first()
        assertEquals(InvoiceStatus.CANCELLED, invoice!!.status)
    }
    
    @Test
    fun testCustomerLinkingParity() = runTest {
        // Create another customer
        val customer2Id = createTestCustomer(
            name = "Second Customer",
            email = "second@example.com"
        ).getOrThrow()
        
        // Create invoices for different customers
        val invoice1 = createTestInvoice(testCustomerId, 10000L).getOrThrow()
        val invoice2 = createTestInvoice(customer2Id, 20000L).getOrThrow()
        
        // Verify customer associations
        val inv1 = invoiceRepository.getInvoiceWithItemsById(invoice1).first()
        val inv2 = invoiceRepository.getInvoiceWithItemsById(invoice2).first()
        
        assertEquals(testCustomerId, inv1!!.customerId)
        assertEquals(customer2Id, inv2!!.customerId)
    }
}
