package com.emul8r.bizap.data.repository

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.emul8r.bizap.data.local.AppDatabase
import com.emul8r.bizap.data.local.entities.InvoiceEntity
import com.emul8r.bizap.data.local.entities.PaymentEntity
import com.emul8r.bizap.data.repository.gui2.PaymentRepositoryV2
import com.emul8r.bizap.domain.model.InvoiceStatus
import com.emul8r.bizap.data.repository.SnapshotSyncHelper
import com.emul8r.bizap.test.WindowsTestRule
import io.mockk.just
import io.mockk.mockk
import io.mockk.Runs
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Comprehensive in-memory Room database tests for [PaymentRepositoryV2].
 *
 * Uses a real SQLite in-memory database via Room to verify:
 * - Atomic transaction semantics (all-or-nothing writes)
 * - Invoice amountPaid updates after each payment
 * - Status transitions: SENT → PARTIALLY_PAID → PAID
 * - Transaction rollback when invoice is not found
 * - Correct outstanding-balance arithmetic across multiple payments
 *
 * Note: SnapshotSyncHelper is mocked to isolate payment logic testing
 * from snapshot sync logic (which has known issues and should be tested separately).
 *
 * PR C Verification: validates that PR A's atomic payment recording is correct.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [28])
class PaymentRepositoryTest {

    @get:Rule
    val skipWindowsRule = WindowsTestRule()

    private lateinit var database: AppDatabase
    private lateinit var paymentRepository: PaymentRepositoryV2
    private lateinit var mockSnapshotSyncHelper: SnapshotSyncHelper

    private val businessId = 1L
    private val paymentDate = System.currentTimeMillis()

    // ── Test lifecycle ────────────────────────────────────────────────────────

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        // Mock SnapshotSyncHelper to prevent snapshot sync failures from blocking payment tests
        mockSnapshotSyncHelper = mockk(relaxed = true)

        paymentRepository = PaymentRepositoryV2(
            database = database,
            invoiceDaoV2 = database.invoiceDaoV2(),
            paymentDaoV2 = database.paymentDaoV2(),
            snapshotSyncHelper = mockSnapshotSyncHelper
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    // ── Helper functions ──────────────────────────────────────────────────────

    /**
     * Inserts a test invoice into the in-memory database and returns its auto-generated ID.
     *
     * @param totalAmount Invoice total in cents (e.g. 10000 = $100.00).
     * @param status      Initial status string (e.g. "SENT").
     * @param amountPaid  Already-paid amount in cents (default 0).
     */
    private suspend fun createTestInvoice(
        totalAmount: Long = 10000L,
        status: String = "SENT",
        amountPaid: Long = 0L
    ): Long = database.invoiceDaoV2().insert(
        InvoiceEntity(
            businessProfileId = businessId,
            customerId = null,
            customerName = "Test Customer",
            date = System.currentTimeMillis(),
            totalAmount = totalAmount,
            isQuote = false,
            status = status,
            amountPaid = amountPaid,
            isActive = true,
            createdAt = System.currentTimeMillis()
        )
    )

    /**
     * Builds a [PaymentEntity] for the given invoice (not persisted — for assertion purposes).
     */
    private fun createTestPayment(invoiceId: Long, amount: Long): PaymentEntity =
        PaymentEntity(
            businessId = businessId,
            invoiceId = invoiceId,
            amount = amount,
            paymentDate = paymentDate,
            notes = null,
            createdAt = System.currentTimeMillis()
        )

    // ── Test 1: Payment recorded returns success ──────────────────────────────

    @Test
    fun `recordPayment_Atomic - payment recorded returns success`() = runTest {
        val invoiceId = createTestInvoice(totalAmount = 10000L)

        val result = paymentRepository.recordPayment(
            invoiceId = invoiceId,
            businessId = businessId,
            amount = 5000L,
            paymentDate = paymentDate,
            notes = null
        )

        assertTrue(result.isSuccess, "recordPayment should return success for a valid invoice")
    }

    // ── Test 2: Payment updates invoice amountPaid correctly ──────────────────

    @Test
    fun `recordPayment_UpdatesInvoice - payment updates invoice amountPaid correctly`() = runTest {
        val invoiceId = createTestInvoice(totalAmount = 10000L)
        val paymentAmount = 5000L

        paymentRepository.recordPayment(
            invoiceId = invoiceId,
            businessId = businessId,
            amount = paymentAmount,
            paymentDate = paymentDate,
            notes = null
        )

        val updatedInvoice = database.invoiceDaoV2().getById(invoiceId)
        assertNotNull(updatedInvoice, "Invoice should still exist after payment")
        assertEquals(paymentAmount, updatedInvoice.amountPaid,
            "Invoice amountPaid should equal the recorded payment amount")
    }

    // ── Test 3: Full payment marks invoice as PAID ────────────────────────────

    @Test
    fun `recordPayment_StatusTransition - full payment marks invoice as PAID`() = runTest {
        val totalAmount = 10000L
        val invoiceId = createTestInvoice(totalAmount = totalAmount)

        paymentRepository.recordPayment(
            invoiceId = invoiceId,
            businessId = businessId,
            amount = totalAmount,
            paymentDate = paymentDate,
            notes = null
        )

        val updatedInvoice = database.invoiceDaoV2().getById(invoiceId)
        assertNotNull(updatedInvoice)
        assertEquals(InvoiceStatus.PAID.name, updatedInvoice.status,
            "Invoice should be marked as PAID after full payment")
        assertEquals(totalAmount, updatedInvoice.amountPaid,
            "Invoice amountPaid should equal totalAmount after full payment")
    }

    // ── Test 4: Partial payment marks invoice as PARTIALLY_PAID ──────────────

    @Test
    fun `recordPayment_StatusTransition - partial payment marks invoice as PARTIALLY_PAID`() = runTest {
        val totalAmount = 10000L
        val partialPayment = 6000L
        val invoiceId = createTestInvoice(totalAmount = totalAmount)

        paymentRepository.recordPayment(
            invoiceId = invoiceId,
            businessId = businessId,
            amount = partialPayment,
            paymentDate = paymentDate,
            notes = null
        )

        val updatedInvoice = database.invoiceDaoV2().getById(invoiceId)
        assertNotNull(updatedInvoice)
        assertEquals(InvoiceStatus.PARTIALLY_PAID.name, updatedInvoice.status,
            "Invoice should be marked as PARTIALLY_PAID after partial payment")
        assertEquals(partialPayment, updatedInvoice.amountPaid,
            "Invoice amountPaid should equal the partial payment amount")
    }

    // ── Test 5: Multiple payments accumulate correctly ────────────────────────

    @Test
    fun `recordPayment_Accumulation - multiple payments accumulate correctly`() = runTest {
        val totalAmount = 10000L
        val invoiceId = createTestInvoice(totalAmount = totalAmount)

        // First payment: $30
        paymentRepository.recordPayment(
            invoiceId = invoiceId,
            businessId = businessId,
            amount = 3000L,
            paymentDate = paymentDate,
            notes = "First payment"
        )

        // Second payment: $20
        paymentRepository.recordPayment(
            invoiceId = invoiceId,
            businessId = businessId,
            amount = 2000L,
            paymentDate = paymentDate,
            notes = "Second payment"
        )

        // Third payment: $50 (clears the balance)
        paymentRepository.recordPayment(
            invoiceId = invoiceId,
            businessId = businessId,
            amount = 5000L,
            paymentDate = paymentDate,
            notes = "Final payment"
        )

        val updatedInvoice = database.invoiceDaoV2().getById(invoiceId)
        assertNotNull(updatedInvoice)
        assertEquals(totalAmount, updatedInvoice.amountPaid,
            "amountPaid should equal the sum of all payments (3000+2000+5000=10000)")
        assertEquals(InvoiceStatus.PAID.name, updatedInvoice.status,
            "Invoice should be PAID after all payments accumulate to totalAmount")

        val payments = database.paymentDaoV2()
            .observePaymentsForInvoice(invoiceId)
            .first()
        assertEquals(3, payments.size, "Three payment records should exist in the database")
    }

    // ── Test 6: Transaction rollback on failure (non-existent invoice) ────────

    @Test
    fun `recordPayment_Atomicity - transaction rolls back when invoice not found`() = runTest {
        val nonExistentInvoiceId = 999L

        val result = paymentRepository.recordPayment(
            invoiceId = nonExistentInvoiceId,
            businessId = businessId,
            amount = 5000L,
            paymentDate = paymentDate,
            notes = null
        )

        assertFalse(result.isSuccess,
            "recordPayment should fail when invoice does not exist")
        assertNotNull(result.exceptionOrNull(),
            "Failure result should contain the exception")

        // Verify the transaction rolled back — no orphan payment rows
        val payments = database.paymentDaoV2()
            .observePaymentsForInvoice(nonExistentInvoiceId)
            .first()
        assertTrue(payments.isEmpty(),
            "No payment rows should be persisted when the transaction rolls back")
    }

    // ── Test 7: Outstanding balance validation ────────────────────────────────

    @Test
    fun `recordPayment_OutstandingBalance - outstanding balance decreases with each payment`() = runTest {
        val totalAmount = 10000L
        val invoiceId = createTestInvoice(totalAmount = totalAmount)

        // Record first payment of $30
        paymentRepository.recordPayment(
            invoiceId = invoiceId,
            businessId = businessId,
            amount = 3000L,
            paymentDate = paymentDate,
            notes = null
        )
        val afterFirstPayment = database.invoiceDaoV2().getById(invoiceId)
        assertNotNull(afterFirstPayment)
        val outstandingAfterFirst = afterFirstPayment.totalAmount - afterFirstPayment.amountPaid
        assertEquals(7000L, outstandingAfterFirst,
            "Outstanding should be $70 after first $30 payment on a $100 invoice")

        // Record second payment of $20
        paymentRepository.recordPayment(
            invoiceId = invoiceId,
            businessId = businessId,
            amount = 2000L,
            paymentDate = paymentDate,
            notes = null
        )
        val afterSecondPayment = database.invoiceDaoV2().getById(invoiceId)
        assertNotNull(afterSecondPayment)
        val outstandingAfterSecond = afterSecondPayment.totalAmount - afterSecondPayment.amountPaid
        assertEquals(5000L, outstandingAfterSecond,
            "Outstanding should be $50 after second $20 payment")

        // Record final payment of $50
        paymentRepository.recordPayment(
            invoiceId = invoiceId,
            businessId = businessId,
            amount = 5000L,
            paymentDate = paymentDate,
            notes = null
        )
        val afterFinalPayment = database.invoiceDaoV2().getById(invoiceId)
        assertNotNull(afterFinalPayment)
        val outstandingAfterFinal = afterFinalPayment.totalAmount - afterFinalPayment.amountPaid
        assertEquals(0L, outstandingAfterFinal,
            "Outstanding should be $0 after all payments clear the invoice")
        assertEquals(InvoiceStatus.PAID.name, afterFinalPayment.status,
            "Invoice should be PAID when outstanding reaches zero")
    }
}
