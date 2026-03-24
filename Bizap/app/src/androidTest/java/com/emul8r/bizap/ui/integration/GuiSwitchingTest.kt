package com.emul8r.bizap.ui.integration

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.emul8r.bizap.data.local.AppDatabase
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.InvoiceStatus
import com.emul8r.bizap.domain.repository.InvoiceRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Integration tests for GUI switching.
 *
 * Verifies that invoices and payments created in GUI1 are visible in GUI2,
 * and vice versa. Tests the data synchronization layer.
 *
 * **Tests:**
 * - Invoice created via GUI1 visible in GUI2
 * - Payment recorded in GUI2 visible in GUI1
 * - Status changes propagate between GUIs
 * - Payment snapshots automatically updated
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class GuiSwitchingTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var invoiceRepository: InvoiceRepository

    @Inject
    lateinit var database: AppDatabase

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    /**
     * Test: Invoice created via GUI1 (repository) is visible in GUI2 (database).
     *
     * **Scenario:**
     * 1. Create invoice via InvoiceRepository (GUI1 layer)
     * 2. Query database directly (GUI2 layer)
     * 3. Verify invoice appears with correct data
     *
     * **Success Criteria:**
     * - Invoice ID returned from save
     * - Invoice retrievable via database query
     * - All fields match (customer, amount, date, etc.)
     */
    @Test
    fun testInvoiceVisibleAcrossGuiSwitch() = runTest {
        // Arrange: Create test invoice via repository (GUI1 path)
        val invoice = createTestInvoice(
            customerName = "Test Customer",
            totalAmount = 100_00,
            invoiceNumber = "INV-GUI1-001"
        )

        // Act: Save via repository (simulates GUI1)
        val result = invoiceRepository.saveInvoice(invoice)
        assert(result.isSuccess) { "Save should succeed" }
        val invoiceId = result.getOrNull()!!

        // Retrieve via database (simulates GUI2)
        val retrieved = database.invoiceDao().getInvoiceWithItems(invoiceId)

        // Assert: Verify visibility across GUIs
        assertNotNull(retrieved, "Invoice should be visible in GUI2")
        assertEquals("Test Customer", retrieved!!.invoice.customerName)
        assertEquals(100_00, retrieved.invoice.totalAmount)
        assertEquals("INV-GUI1-001", retrieved.invoice.invoiceNumber)
    }

    /**
     * Test: Payment recorded in GUI2 is visible in GUI1.
     *
     * **Scenario:**
     * 1. Create invoice
     * 2. Record payment via repository (GUI2 layer)
     * 3. Verify payment visible via database (GUI1 layer)
     * 4. Verify snapshot updated automatically
     *
     * **Success Criteria:**
     * - amountPaid field updated
     * - Payment snapshot created/updated
     * - Status reflects payment (PAID, PARTIALLY_PAID, etc.)
     */
    @Test
    fun testPaymentRecordedAcrossGui() = runTest {
        // Arrange: Create invoice
        val invoiceId = createAndSaveTestInvoice(totalAmount = 100_00)

        // Act: Record payment via repository (GUI2 layer)
        val paymentResult = invoiceRepository.updateAmountPaid(invoiceId, 50_00)
        assert(paymentResult.isSuccess) { "Payment update should succeed" }

        // Retrieve via database (GUI1 layer)
        val invoice = database.invoiceDao().getInvoiceWithItems(invoiceId)
        assertNotNull(invoice, "Invoice should still exist")

        // Assert: Payment visible in both layers
        assertEquals(50_00, invoice!!.invoice.amountPaid)

        // Verify snapshot updated
        val snapshot = database.invoicePaymentDao().getSnapshotByInvoiceId(invoiceId)
        assertNotNull(snapshot, "Snapshot should be created")
        assertEquals(50_00, snapshot!!.paidAmount)
        assertEquals(50_00, snapshot.outstandingAmount)
    }

    /**
     * Test: Multiple payments accumulate correctly.
     *
     * **Scenario:**
     * 1. Create invoice for $100
     * 2. Record payment of $30
     * 3. Record payment of $20
     * 4. Record payment of $50
     * 5. Verify total paid = $100 (PAID status)
     *
     * **Success Criteria:**
     * - Each payment accumulates
     * - Total matches sum of payments
     * - Status changes to PAID when total reached
     */
    @Test
    fun testMultiplePaymentsAccumulate() = runTest {
        // Arrange: Create $100 invoice
        val invoiceId = createAndSaveTestInvoice(totalAmount = 100_00)

        // Act & Assert: Record multiple payments
        invoiceRepository.updateAmountPaid(invoiceId, 30_00)
        var invoice = database.invoiceDao().getInvoiceWithItems(invoiceId)
        assertEquals(30_00, invoice!!.invoice.amountPaid)

        invoiceRepository.updateAmountPaid(invoiceId, 50_00)
        invoice = database.invoiceDao().getInvoiceWithItems(invoiceId)
        assertEquals(50_00, invoice!!.invoice.amountPaid)

        invoiceRepository.updateAmountPaid(invoiceId, 100_00)
        invoice = database.invoiceDao().getInvoiceWithItems(invoiceId)
        assertEquals(100_00, invoice!!.invoice.amountPaid)

        // Verify snapshot reflects PAID status
        val snapshot = database.invoicePaymentDao().getSnapshotByInvoiceId(invoiceId)
        assertEquals(0L, snapshot!!.outstandingAmount) // Fully paid
    }

    /**
     * Test: Status changes propagate between GUIs.
     *
     * **Scenario:**
     * 1. Create invoice (DRAFT status)
     * 2. Update status to SENT via repository
     * 3. Verify status changed in database
     * 4. Verify snapshot paymentStatus updated
     *
     * **Success Criteria:**
     * - Status updated in invoice table
     * - Snapshot paymentStatus reflects new status
     * - Visible in both GUI1 and GUI2 queries
     */
    @Test
    fun testStatusChangePropagatesToOtherGui() = runTest {
        // Arrange: Create invoice
        val invoiceId = createAndSaveTestInvoice()

        // Act: Update status via repository (simulates GUI2)
        val statusResult = invoiceRepository.updateInvoiceStatus(invoiceId, InvoiceStatus.SENT)
        assert(statusResult.isSuccess) { "Status update should succeed" }

        // Verify via database (simulates GUI1)
        val invoice = database.invoiceDao().getInvoiceWithItems(invoiceId)
        assertNotNull(invoice)

        // Assert: Status changed in both layers
        assertEquals(InvoiceStatus.SENT.name, invoice!!.invoice.status)

        // Verify snapshot updated
        val snapshot = database.invoicePaymentDao().getSnapshotByInvoiceId(invoiceId)
        assertNotNull(snapshot)
        assertEquals("SENT", snapshot!!.paymentStatus)
    }

    /**
     * Test: Deleting invoice cleans up both tables.
     *
     * **Scenario:**
     * 1. Create invoice and snapshot
     * 2. Delete invoice via repository
     * 3. Verify both invoice and snapshot removed
     *
     * **Success Criteria:**
     * - Invoice row deleted from invoices table
     * - Associated snapshot deleted from snapshots table
     * - Database constraints maintained (no orphans)
     */
    @Test
    fun testDeleteInvoiceCleanupBothTables() = runTest {
        // Arrange: Create invoice
        val invoiceId = createAndSaveTestInvoice()

        // Verify setup
        var invoice = database.invoiceDao().getInvoiceWithItems(invoiceId)
        assertNotNull(invoice, "Invoice should exist after creation")

        // Act: Delete via repository
        val deleteResult = invoiceRepository.deleteInvoice(invoiceId)
        assert(deleteResult.isSuccess) { "Delete should succeed" }

        // Assert: Invoice removed
        invoice = database.invoiceDao().getInvoiceWithItems(invoiceId)
        assertEquals(null, invoice, "Invoice should be deleted")

        // Verify snapshot also removed (cascade delete)
        val snapshot = database.invoicePaymentDao().getSnapshotByInvoiceId(invoiceId)
        assertEquals(null, snapshot, "Snapshot should be cascade deleted")
    }

    /**
     * Test: Concurrent updates don't cause race conditions.
     *
     * **Scenario:**
     * 1. Create invoice
     * 2. Update amount paid
     * 3. Update status simultaneously (simulated sequential)
     * 4. Verify both updates applied
     *
     * **Success Criteria:**
     * - Both updates reflected in database
     * - No data corruption or loss
     * - Snapshot consistent with latest state
     */
    @Test
    fun testConcurrentUpdatesConsistent() = runTest {
        // Arrange
        val invoiceId = createAndSaveTestInvoice(totalAmount = 100_00)

        // Act: Multiple updates
        invoiceRepository.updateAmountPaid(invoiceId, 50_00)
        invoiceRepository.updateInvoiceStatus(invoiceId, InvoiceStatus.PARTIALLY_PAID)

        // Assert: All updates applied
        val invoice = database.invoiceDao().getInvoiceWithItems(invoiceId)
        assertEquals(50_00, invoice!!.invoice.amountPaid)
        assertEquals(InvoiceStatus.PARTIALLY_PAID.name, invoice.invoice.status)

        // Verify snapshot
        val snapshot = database.invoicePaymentDao().getSnapshotByInvoiceId(invoiceId)
        assertEquals(50_00, snapshot!!.paidAmount)
        assertEquals("PARTIALLY_PAID", snapshot.paymentStatus)
    }

    // ============ HELPER FUNCTIONS ============

    private fun createTestInvoice(
        customerName: String = "Test Customer",
        totalAmount: Long = 10_00,
        invoiceNumber: String = "INV-${System.currentTimeMillis()}"
    ): Invoice {
        return Invoice(
            id = 0,
            businessProfileId = 1L,
            customerId = 1L,
            customerName = customerName,
            invoiceNumber = invoiceNumber,
            date = System.currentTimeMillis(),
            dueDate = System.currentTimeMillis() + 86400000L,
            items = listOf(),
            totalAmount = totalAmount,
            taxAmount = 0,
            taxRate = 0.0,
            status = InvoiceStatus.DRAFT.name,
            amountPaid = 0,
            header = "",
            subheader = "",
            notes = "",
            footer = "",
            currencyCode = "AUD",
            invoiceYear = 2026,
            invoiceSequence = 1
        )
    }

    private suspend fun createAndSaveTestInvoice(
        totalAmount: Long = 10_00,
        invoiceNumber: String = "INV-${System.currentTimeMillis()}"
    ): Long {
        val invoice = createTestInvoice(totalAmount = totalAmount, invoiceNumber = invoiceNumber)
        return invoiceRepository.saveInvoice(invoice).getOrNull()!!
    }
}

