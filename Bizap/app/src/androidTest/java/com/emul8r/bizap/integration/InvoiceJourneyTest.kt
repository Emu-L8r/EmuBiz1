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
 * Integration tests for complete invoice journeys.
 * 
 * Tests critical user paths from creation to completion:
 * - Create invoice → Send → Payment → Completion
 * - Partial payment flows
 * - Status transitions
 * - Analytics updates
 */
@RunWith(AndroidJUnit4::class)
@MediumTest
class InvoiceJourneyTest : IntegrationTestBase() {
    
    private var testCustomerId: Long = 0L
    
    @Before
    fun setupTestData() = runTest {
        // Create test business profile
        createTestBusinessProfile(id = 1L, name = "Acme Corp").getOrThrow()
        
        // Create test customer
        testCustomerId = createTestCustomer(
            name = "Acme Corp",
            email = "acme@example.com",
            businessId = 1L
        ).getOrThrow()
    }
    
    @Test
    fun testCompleteInvoiceJourney() = runTest {
        // Journey: Create (DRAFT) → Send (SENT) → Payment → Complete (PAID)
        
        // Step 1: Create draft invoice
        val invoiceId = createTestInvoice(
            customerId = testCustomerId,
            amount = 50000L, // $500.00
            status = InvoiceStatus.DRAFT,
            businessId = 1L
        ).getOrThrow()
        
        assertTrue(invoiceId > 0, "Invoice should be created with valid ID")
        
        // Step 2: Verify invoice exists as DRAFT
        var invoice = invoiceRepository.getInvoiceWithItemsById(invoiceId).first()
        assertNotNull(invoice, "Invoice should exist")
        assertEquals(InvoiceStatus.DRAFT, invoice.status, "Invoice should be in DRAFT status")
        assertEquals(50000L, invoice.totalAmount, "Invoice amount should match")
        
        // Step 3: Send invoice (transition DRAFT → SENT)
        invoiceRepository.updateInvoiceStatus(invoiceId, InvoiceStatus.SENT).getOrThrow()
        invoice = invoiceRepository.getInvoiceWithItemsById(invoiceId).first()
        assertEquals(InvoiceStatus.SENT, invoice!!.status, "Invoice should be SENT")
        
        // Step 4: Record full payment (transition SENT → PAID)
        invoiceRepository.updateAmountPaid(invoiceId, 50000L).getOrThrow()
        invoice = invoiceRepository.getInvoiceWithItemsById(invoiceId).first()
        assertEquals(InvoiceStatus.PAID, invoice!!.status, "Invoice should be PAID")
        assertEquals(50000L, invoice.amountPaid, "Full amount should be paid")
    }
    
    @Test
    fun testPartialPaymentJourney() = runTest {
        // Create sent invoice
        val invoiceId = createTestInvoice(
            customerId = testCustomerId,
            amount = 100000L, // $1,000.00
            status = InvoiceStatus.SENT
        ).getOrThrow()
        
        // Make first partial payment (50%)
        invoiceRepository.updateAmountPaid(invoiceId, 50000L).getOrThrow()
        var invoice = invoiceRepository.getInvoiceWithItemsById(invoiceId).first()
        assertEquals(InvoiceStatus.PARTIALLY_PAID, invoice!!.status, 
            "Invoice should be PARTIALLY_PAID after first payment")
        assertEquals(50000L, invoice.amountPaid, "Should have first payment recorded")
        
        // Make second partial payment (remaining 50%)
        invoiceRepository.updateAmountPaid(invoiceId, 100000L).getOrThrow()
        invoice = invoiceRepository.getInvoiceWithItemsById(invoiceId).first()
        assertEquals(InvoiceStatus.PAID, invoice!!.status, 
            "Invoice should be PAID after full payment")
        assertEquals(100000L, invoice.amountPaid, "Full amount should be paid")
    }
    
    @Test
    fun testOverpaymentHandling() = runTest {
        val invoiceId = createTestInvoice(
            customerId = testCustomerId,
            amount = 10000L, // $100.00
            status = InvoiceStatus.SENT
        ).getOrThrow()
        
        // Record overpayment
        invoiceRepository.updateAmountPaid(invoiceId, 15000L).getOrThrow()
        val invoice = invoiceRepository.getInvoiceWithItemsById(invoiceId).first()
        
        assertEquals(InvoiceStatus.PAID, invoice!!.status, "Overpaid invoice should be PAID")
        assertEquals(15000L, invoice.amountPaid, "Overpayment amount should be recorded")
    }
    
    @Test
    fun testInvoiceCancellation() = runTest {
        val invoiceId = createTestInvoice(
            customerId = testCustomerId,
            amount = 25000L,
            status = InvoiceStatus.SENT
        ).getOrThrow()
        
        // Cancel invoice
        invoiceRepository.updateInvoiceStatus(invoiceId, InvoiceStatus.CANCELLED).getOrThrow()
        val invoice = invoiceRepository.getInvoiceWithItemsById(invoiceId).first()
        
        assertEquals(InvoiceStatus.CANCELLED, invoice!!.status, 
            "Invoice should be CANCELLED")
    }
    
    @Test
    fun testMultipleInvoicesForSameCustomer() = runTest {
        // Create multiple invoices for the same customer
        val invoice1 = createTestInvoice(testCustomerId, 10000L, InvoiceStatus.PAID).getOrThrow()
        val invoice2 = createTestInvoice(testCustomerId, 20000L, InvoiceStatus.SENT).getOrThrow()
        val invoice3 = createTestInvoice(testCustomerId, 30000L, InvoiceStatus.DRAFT).getOrThrow()
        
        // Verify all invoices exist
        val allInvoices = invoiceRepository.getAllInvoicesWithItems().first()
        assertTrue(allInvoices.size >= 3, "Should have at least 3 invoices")
        
        val customerInvoices = allInvoices.filter { it.customerId == testCustomerId }
        assertEquals(3, customerInvoices.size, "Customer should have 3 invoices")
        
        // Verify statuses
        assertTrue(customerInvoices.any { it.status == InvoiceStatus.PAID })
        assertTrue(customerInvoices.any { it.status == InvoiceStatus.SENT })
        assertTrue(customerInvoices.any { it.status == InvoiceStatus.DRAFT })
    }
}
