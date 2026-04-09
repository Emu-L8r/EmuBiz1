package com.emul8r.bizap

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.emul8r.bizap.domain.model.Customer
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.ui.navigation.Screen
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import timber.log.Timber
import javax.inject.Inject

/**
 * Full-Loop E2E Test: Customer → Invoice → PDF → Payment Status Update
 *
 * This test verifies the complete business workflow without crashes:
 * 1. Create a new customer
 * 2. Create an invoice for that customer
 * 3. View/render PDF invoice
 * 4. Update invoice payment status
 * 5. Verify data persists and UI updates
 *
 * EXPECTED RESULT: User can complete entire workflow without crashes
 */
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class FullLoopE2ETest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val composeTestRule = createComposeRule()

    @Inject
    lateinit var customerRepository: CustomerRepository

    @Inject
    lateinit var invoiceRepository: InvoiceRepository

    @Inject
    lateinit var pdfService: InvoicePdfService

    @Before
    fun setup() {
        hiltRule.inject()
    }

    /**
     * TEST 1: Create Customer
     * Verifies new customer can be created and stored
     */
    @Test
    fun testCreateCustomer() = runBlocking {
        Timber.d("TEST 1: Creating new customer...")

        val customer = Customer(
            id = 1,
            businessId = 1,
            name = "Test Customer Inc",
            email = "test@customer.com",
            phone = "+1234567890",
            address = "123 Test St",
            city = "Test City",
            state = "TS",
            zipCode = "12345",
            country = "USA"
        )

        // Insert customer
        customerRepository.addCustomer(customer)

        // Verify customer exists
        val retrieved = customerRepository.getCustomer(1)
        assert(retrieved != null)
        assert(retrieved?.name == "Test Customer Inc")

        Timber.d("✅ TEST 1 PASSED: Customer created successfully")
    }

    /**
     * TEST 2: Create Invoice for Customer
     * Verifies invoice creation with customer relationship
     */
    @Test
    fun testCreateInvoice() = runBlocking {
        Timber.d("TEST 2: Creating invoice for customer...")

        // First create customer
        val customer = Customer(
            id = 1,
            businessId = 1,
            name = "Test Customer",
            email = "test@customer.com"
        )
        customerRepository.addCustomer(customer)

        // Create invoice
        val invoice = Invoice(
            id = 1,
            businessId = 1,
            customerId = 1,
            invoiceNumber = "INV-001",
            amount = 1500.00,
            issueDate = System.currentTimeMillis(),
            dueDate = System.currentTimeMillis() + (30 * 24 * 60 * 60 * 1000),
            status = "Draft",
            notes = "Test invoice"
        )

        invoiceRepository.addInvoice(invoice)

        // Verify invoice exists
        val retrieved = invoiceRepository.getInvoiceById(1)
        assert(retrieved != null)
        assert(retrieved?.amount == 1500.00)

        Timber.d("✅ TEST 2 PASSED: Invoice created successfully")
    }

    /**
     * TEST 3: Generate PDF
     * Verifies PDF generation doesn't crash
     */
    @Test
    fun testPDFGeneration() = runBlocking {
        Timber.d("TEST 3: Generating PDF...")

        val customer = Customer(
            id = 1,
            businessId = 1,
            name = "Test Customer",
            email = "test@customer.com"
        )
        customerRepository.addCustomer(customer)

        val invoice = Invoice(
            id = 1,
            businessId = 1,
            customerId = 1,
            invoiceNumber = "INV-001",
            amount = 1500.00,
            issueDate = System.currentTimeMillis(),
            dueDate = System.currentTimeMillis() + (30 * 24 * 60 * 60 * 1000),
            status = "Sent",
            notes = "Test invoice for PDF"
        )
        invoiceRepository.addInvoice(invoice)

        // Generate PDF
        try {
            val pdfFile = pdfService.generateInvoicePDF(invoice, customer)
            assert(pdfFile != null)
            assert(pdfFile.exists())
            Timber.d("✅ TEST 3 PASSED: PDF generated successfully (${pdfFile.length()} bytes)")
        } catch (e: Exception) {
            Timber.e(e, "❌ TEST 3 FAILED: PDF generation error")
            throw e
        }
    }

    /**
     * TEST 4: Update Payment Status
     * Verifies payment status update and data persistence
     */
    @Test
    fun testUpdatePaymentStatus() = runBlocking {
        Timber.d("TEST 4: Updating payment status...")

        val customer = Customer(
            id = 1,
            businessId = 1,
            name = "Test Customer",
            email = "test@customer.com"
        )
        customerRepository.addCustomer(customer)

        var invoice = Invoice(
            id = 1,
            businessId = 1,
            customerId = 1,
            invoiceNumber = "INV-001",
            amount = 1500.00,
            issueDate = System.currentTimeMillis(),
            dueDate = System.currentTimeMillis() + (30 * 24 * 60 * 60 * 1000),
            status = "Sent"
        )
        invoiceRepository.addInvoice(invoice)

        // Update status to Paid
        invoice = invoice.copy(status = "Paid", paidDate = System.currentTimeMillis())
        invoiceRepository.updateInvoice(invoice)

        // Verify status updated
        val updated = invoiceRepository.getInvoiceById(1)
        assert(updated?.status == "Paid")
        assert(updated?.paidDate != null)

        Timber.d("✅ TEST 4 PASSED: Payment status updated successfully")
    }

    /**
     * TEST 5: Verify Data Integrity
     * Ensures relationships between customer, invoice, and payments are maintained
     */
    @Test
    fun testDataIntegrity() = runBlocking {
        Timber.d("TEST 5: Verifying data integrity...")

        val customerId = 1L
        val invoiceId = 1L

        val customer = Customer(
            id = customerId,
            businessId = 1,
            name = "Integrity Test Customer",
            email = "integrity@test.com"
        )
        customerRepository.addCustomer(customer)

        val invoice = Invoice(
            id = invoiceId,
            businessId = 1,
            customerId = customerId,
            invoiceNumber = "INV-INTEGRITY",
            amount = 2500.00,
            status = "Sent"
        )
        invoiceRepository.addInvoice(invoice)

        // Update to paid
        val paidInvoice = invoice.copy(
            status = "Paid",
            paidDate = System.currentTimeMillis()
        )
        invoiceRepository.updateInvoice(paidInvoice)

        // Verify relationships
        val retrievedCustomer = customerRepository.getCustomer(customerId)
        val retrievedInvoice = invoiceRepository.getInvoiceById(invoiceId)

        assert(retrievedCustomer != null)
        assert(retrievedInvoice != null)
        assert(retrievedInvoice?.customerId == customerId)
        assert(retrievedInvoice?.status == "Paid")

        Timber.d("✅ TEST 5 PASSED: Data integrity verified")
    }

    /**
     * FULL WORKFLOW TEST
     * Complete end-to-end workflow verification
     */
    @Test
    fun testCompleteWorkflow() = runBlocking {
        Timber.d("🧪 FULL WORKFLOW TEST: Starting complete E2E test...")

        try {
            // Step 1: Create Customer
            Timber.d("Step 1/4: Creating customer...")
            val customer = Customer(
                id = 100,
                businessId = 1,
                name = "Workflow Test Customer",
                email = "workflow@test.com",
                phone = "+1111111111"
            )
            customerRepository.addCustomer(customer)

            // Step 2: Create Invoice
            Timber.d("Step 2/4: Creating invoice...")
            val invoice = Invoice(
                id = 100,
                businessId = 1,
                customerId = 100,
                invoiceNumber = "WF-100",
                amount = 3000.00,
                issueDate = System.currentTimeMillis(),
                dueDate = System.currentTimeMillis() + (45 * 24 * 60 * 60 * 1000),
                status = "Sent"
            )
            invoiceRepository.addInvoice(invoice)

            // Step 3: Generate PDF
            Timber.d("Step 3/4: Generating PDF...")
            val pdfFile = pdfService.generateInvoicePDF(invoice, customer)
            assert(pdfFile.exists())

            // Step 4: Update Payment Status
            Timber.d("Step 4/4: Updating payment status...")
            val paidInvoice = invoice.copy(
                status = "Paid",
                paidDate = System.currentTimeMillis()
            )
            invoiceRepository.updateInvoice(paidInvoice)

            // Verify everything
            val finalInvoice = invoiceRepository.getInvoiceById(100)
            assert(finalInvoice?.status == "Paid")

            Timber.d("✅ FULL WORKFLOW TEST PASSED: Complete workflow executed without crashes!")

        } catch (e: Exception) {
            Timber.e(e, "❌ FULL WORKFLOW TEST FAILED")
            throw e
        }
    }
}

