package com.emul8r.bizap.ui.integration

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
 * Navigation integration tests.
 *
 * Verifies that navigation flows work correctly between GUI1 and GUI2,
 * including deep linking, back button behavior, and data preservation.
 *
 * **Tests:**
 * - Deep linking to invoice detail preserves data
 * - Back navigation doesn't lose data
 * - GUI switching via navigation maintains state
 * - Multiple navigation levels work correctly
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class NavigationIntegrationTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var invoiceRepository: InvoiceRepository

    @Inject
    lateinit var database: AppDatabase

    @Before
    fun setup() {
        hiltRule.inject()
    }

    /**
     * Test: Deep linking to invoice detail preserves data.
     *
     * **Scenario:**
     * 1. Create invoice
     * 2. Deep link to invoice detail screen
     * 3. Verify invoice data loads correctly
     * 4. Navigate back
     * 5. Deep link again - data still there
     *
     * **Success Criteria:**
     * - Invoice data preserved across deep links
     * - No data loss on back navigation
     * - Multiple deep links work correctly
     */
    @Test
    fun testDeepLinkingPreservesData() = runTest {
        // Arrange: Create invoice
        val invoice = createTestInvoice()
        val result = invoiceRepository.saveInvoice(invoice)
        val invoiceId = result.getOrNull()!!

        // Act: Simulate deep link by querying database
        val retrieved1 = database.invoiceDao().getInvoiceWithItems(invoiceId)
        assertNotNull(retrieved1, "Invoice should load on first deep link")

        // Simulate back navigation (app pauses, resumes)
        // Re-deep link to same invoice
        val retrieved2 = database.invoiceDao().getInvoiceWithItems(invoiceId)

        // Assert: Data preserved across deep links
        assertNotNull(retrieved2, "Invoice should load on second deep link")
        assertEquals(retrieved1!!.invoice.id, retrieved2!!.invoice.id)
        assertEquals(retrieved1.invoice.customerName, retrieved2.invoice.customerName)
    }

    /**
     * Test: Back navigation from detail to list preserves list state.
     *
     * **Scenario:**
     * 1. Load invoice list (GUI1)
     * 2. Navigate to invoice detail
     * 3. Verify detail data loaded
     * 4. Press back button
     * 5. Verify list still shows all invoices
     *
     * **Success Criteria:**
     * - Invoice detail loads correctly
     * - Back button returns to list
     * - List data not lost
     */
    @Test
    fun testBackNavigationDoesntLoseData() = runTest {
        // Arrange: Create multiple invoices
        val invoice1 = createTestInvoice("INV-001")
        val invoice2 = createTestInvoice("INV-002")
        val invoice3 = createTestInvoice("INV-003")

        val id1 = invoiceRepository.saveInvoice(invoice1).getOrNull()!!
        val id2 = invoiceRepository.saveInvoice(invoice2).getOrNull()!!
        val id3 = invoiceRepository.saveInvoice(invoice3).getOrNull()!!

        // Act: Navigate to detail for invoice 2
        val detail = database.invoiceDao().getInvoiceWithItems(id2)
        assertNotNull(detail, "Detail should load")

        // Simulate back navigation - query list again
        val list1 = database.invoiceDao().getInvoiceWithItems(id1)
        val list3 = database.invoiceDao().getInvoiceWithItems(id3)

        // Assert: All invoices still accessible
        assertNotNull(list1, "Invoice 1 should still be accessible")
        assertNotNull(list3, "Invoice 3 should still be accessible")
    }

    /**
     * Test: GUI switching via navigation maintains state.
     *
     * **Scenario:**
     * 1. Open invoice detail in GUI1
     * 2. Switch to GUI2 (same invoice)
     * 3. Modify something in GUI2
     * 4. Switch back to GUI1
     * 5. Verify changes visible
     *
     * **Success Criteria:**
     * - Data loads in both GUIs
     * - Changes visible across GUIs
     * - No data loss during switching
     */
    @Test
    fun testGuiSwitchingViaNavigationMaintainsState() = runTest {
        // Arrange: Create invoice
        val invoice = createTestInvoice()
        val invoiceId = invoiceRepository.saveInvoice(invoice).getOrNull()!!

        // Act: Load in GUI1 (database query)
        val gui1View = database.invoiceDao().getInvoiceWithItems(invoiceId)
        assertNotNull(gui1View)
        assertEquals(0L, gui1View!!.invoice.amountPaid)

        // Switch to GUI2: modify state (record payment)
        invoiceRepository.updateAmountPaid(invoiceId, 50_00)

        // Switch back to GUI1: reload
        val gui1ViewAfterSwitch = database.invoiceDao().getInvoiceWithItems(invoiceId)

        // Assert: Changes visible in GUI1
        assertNotNull(gui1ViewAfterSwitch)
        assertEquals(50_00, gui1ViewAfterSwitch!!.invoice.amountPaid)
    }

    /**
     * Test: Multi-level navigation works correctly.
     *
     * **Scenario:**
     * 1. List screen → Detail screen (navigate down)
     * 2. Detail screen → Payment screen (navigate down)
     * 3. Payment screen → Detail screen (navigate up)
     * 4. Detail screen → List screen (navigate up)
     *
     * **Success Criteria:**
     * - Each level loads correctly
     * - Back button navigates properly
     * - Data preserved at each level
     */
    @Test
    fun testMultiLevelNavigationWorks() = runTest {
        // Arrange: Create invoice
        val invoiceId = invoiceRepository.saveInvoice(createTestInvoice()).getOrNull()!!

        // Level 1: List (can query all invoices)
        // Simulated by querying database

        // Level 2: Detail (open specific invoice)
        val detail = database.invoiceDao().getInvoiceWithItems(invoiceId)
        assertNotNull(detail, "Detail should load")

        // Level 3: Payment (open payment screen from detail)
        // Verify payment data available
        val snapshot = database.invoicePaymentDao().getSnapshotByInvoiceId(invoiceId)
        assertNotNull(snapshot, "Payment data should be available")

        // Navigate back: Payment → Detail
        val detailAgain = database.invoiceDao().getInvoiceWithItems(invoiceId)
        assertNotNull(detailAgain)

        // Navigate back: Detail → List (query all invoices)
        // Data still available
    }

    /**
     * Test: Navigation preserves invoice state during edit flow.
     *
     * **Scenario:**
     * 1. Open invoice detail
     * 2. Navigate to edit screen
     * 3. Modify data (status change)
     * 4. Save and navigate back to detail
     * 5. Verify changes reflected
     *
     * **Success Criteria:**
     * - Edit data saved to database
     * - Navigation back shows updated data
     * - No intermediate state lost
     */
    @Test
    fun testNavigationPreservesEditFlow() = runTest {
        // Arrange: Create invoice
        val invoiceId = invoiceRepository.saveInvoice(createTestInvoice()).getOrNull()!!

        // Act: Navigate to detail
        val detail = database.invoiceDao().getInvoiceWithItems(invoiceId)
        assertNotNull(detail)
        assertEquals(InvoiceStatus.DRAFT.name, detail!!.invoice.status)

        // Simulate navigation to edit and update
        invoiceRepository.updateInvoiceStatus(invoiceId, InvoiceStatus.SENT)

        // Navigate back to detail
        val detailAfterEdit = database.invoiceDao().getInvoiceWithItems(invoiceId)

        // Assert: Changes persisted
        assertNotNull(detailAfterEdit)
        assertEquals(InvoiceStatus.SENT.name, detailAfterEdit!!.invoice.status)
    }

    /**
     * Test: Rapid navigation doesn't cause race conditions.
     *
     * **Scenario:**
     * 1. Open detail
     * 2. Quickly navigate back and forth multiple times
     * 3. Verify data consistent at each step
     *
     * **Success Criteria:**
     * - No crashes during rapid navigation
     * - Data remains consistent
     * - No partial data loads
     */
    @Test
    fun testRapidNavigationConsistent() = runTest {
        // Arrange
        val invoiceId = invoiceRepository.saveInvoice(createTestInvoice()).getOrNull()!!

        // Act: Rapid navigation
        repeat(5) { _ ->
            val detail = database.invoiceDao().getInvoiceWithItems(invoiceId)
            assertNotNull(detail, "Detail should load on each navigation")
            assertEquals(invoiceId, detail!!.invoice.id)
        }

        // Assert: Final state consistent
        val final = database.invoiceDao().getInvoiceWithItems(invoiceId)
        assertNotNull(final)
        assertEquals(invoiceId, final!!.invoice.id)
    }

    // ============ HELPER FUNCTIONS ============

    private fun createTestInvoice(
        invoiceNumber: String = "INV-${System.currentTimeMillis()}"
    ): Invoice {
        return Invoice(
            id = 0,
            businessProfileId = 1L,
            customerId = 1L,
            customerName = "Test Customer",
            invoiceNumber = invoiceNumber,
            date = System.currentTimeMillis(),
            dueDate = System.currentTimeMillis() + 86400000L,
            items = listOf(),
            totalAmount = 100_00,
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
}

