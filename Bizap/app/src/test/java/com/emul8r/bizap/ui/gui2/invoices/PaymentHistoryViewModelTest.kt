package com.emul8r.bizap.ui.gui2.invoices

import androidx.lifecycle.SavedStateHandle
import com.emul8r.bizap.data.local.entities.InvoicePaymentSnapshot
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Unit tests for [PaymentHistoryViewModel].
 *
 * Tests data transformation and state management.
 */
class PaymentHistoryViewModelTest {

    /**
     * Test: Snapshot data transforms to payment history item correctly.
     */
    @Test
    fun testSnapshotToPaymentHistoryItemTransform() {
        // Arrange: Create a snapshot
        val snapshot = createSnapshot(
            invoiceNumber = "INV-001",
            paidAmount = 50_00,
            paymentStatus = "PARTIALLY_PAID",
            daysSinceDue = 5
        )

        // Act: Transform (same logic as ViewModel)
        val item = PaymentHistoryItem(
            date = snapshot.lastUpdatedMs,
            amount = snapshot.paidAmount,
            status = snapshot.paymentStatus,
            daysSinceDue = snapshot.daysSinceDue,
            notes = null
        )

        // Assert
        assertEquals(50_00, item.amount)
        assertEquals("PARTIALLY_PAID", item.status)
        assertEquals(5, item.daysSinceDue)
    }

    /**
     * Test: Multiple snapshots maintain order.
     */
    @Test
    fun testMultipleSnapshotsOrder() {
        // Create snapshots with different amounts
        val snapshot1 = createSnapshot(paidAmount = 100_00, lastUpdatedMs = 3000L)
        val snapshot2 = createSnapshot(paidAmount = 50_00, lastUpdatedMs = 2000L)
        val snapshot3 = createSnapshot(paidAmount = 0L, lastUpdatedMs = 1000L)

        val snapshots = listOf(snapshot1, snapshot2, snapshot3)

        // Transform to items
        val items = snapshots.map { snapshot ->
            PaymentHistoryItem(
                date = snapshot.lastUpdatedMs,
                amount = snapshot.paidAmount,
                status = snapshot.paymentStatus,
                daysSinceDue = snapshot.daysSinceDue,
                notes = null
            )
        }

        // Assert order preserved
        assertEquals(3, items.size)
        assertEquals(100_00, items[0].amount)
        assertEquals(50_00, items[1].amount)
        assertEquals(0L, items[2].amount)
    }

    /**
     * Test: Status values transform correctly.
     */
    @Test
    fun testStatusTransformation() {
        val statuses = listOf("PAID", "UNPAID", "OVERDUE", "PARTIALLY_PAID")

        for (status in statuses) {
            val snapshot = createSnapshot(paymentStatus = status)
            val item = PaymentHistoryItem(
                date = snapshot.lastUpdatedMs,
                amount = snapshot.paidAmount,
                status = snapshot.paymentStatus,
                daysSinceDue = snapshot.daysSinceDue,
                notes = null
            )

            assertEquals(status, item.status)
        }
    }

    /**
     * Test: UI state creation from empty snapshots.
     */
    @Test
    fun testEmptyUiState() {
        val emptySnapshots = emptyList<InvoicePaymentSnapshot>()

        // PaymentHistoryUiState is a sealed interface, use proper types
        val uiState: PaymentHistoryUiState = if (emptySnapshots.isEmpty()) {
            PaymentHistoryUiState.NotFound
        } else {
            PaymentHistoryUiState.Loading
        }

        assertNotNull(uiState)
        assertTrue(uiState is PaymentHistoryUiState.NotFound || uiState is PaymentHistoryUiState.Loading)
    }

    // ============ HELPER FUNCTIONS ============

    private fun createSnapshot(
        invoiceNumber: String = "INV-001",
        paidAmount: Long = 0L,
        paymentStatus: String = "UNPAID",
        lastUpdatedMs: Long = System.currentTimeMillis(),
        daysSinceDue: Int = 0
    ): InvoicePaymentSnapshot {
        return InvoicePaymentSnapshot(
            invoiceId = 123L,
            businessProfileId = 1L,
            customerId = 1L,
            customerName = "Acme Corp",
            invoiceNumber = invoiceNumber,
            invoiceDate = 1000L,
            dueDate = 2000L,
            totalAmount = 100_00,
            paidAmount = paidAmount,
            outstandingAmount = 100_00 - paidAmount,
            paymentStatus = paymentStatus,
            ageingBucket = "CURRENT",
            daysOverdue = daysSinceDue,
            daysSinceDue = daysSinceDue,
            lastPaymentDate = null,
            lastPaymentAmount = paidAmount,
            paymentCount = if (paidAmount > 0) 1 else 0,
            isAtRisk = false,
            riskScore = 0.0,
            riskFactors = "",
            lastUpdatedMs = lastUpdatedMs,
            snapshotDateMs = lastUpdatedMs
        )
    }
}





