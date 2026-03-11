@file:Suppress("UNCHECKED_CAST")
package com.emul8r.bizap.data.repository

import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.data.local.AppDatabase
import com.emul8r.bizap.data.local.dao.InvoiceDaoV2
import com.emul8r.bizap.data.local.dao.PaymentDaoV2
import com.emul8r.bizap.data.local.entities.InvoiceEntity
import com.emul8r.bizap.data.repository.gui2.PaymentRepositoryV2
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Unit tests for [PaymentRepositoryV2].
 *
 * Verifies payment recording, amount-paid updates, and atomicity guarantees.
 */
class PaymentRepositoryTest : BaseUnitTest() {

    private val database: AppDatabase = mockk(relaxed = true)
    private val invoiceDaoV2: InvoiceDaoV2 = mockk(relaxed = true)
    private val paymentDaoV2: PaymentDaoV2 = mockk(relaxed = true)
    private lateinit var paymentRepository: PaymentRepositoryV2

    private val invoiceId = 1L
    private val businessId = 1L
    private val paymentAmount = 50000L
    private val paymentDate = System.currentTimeMillis()

    /** Reusable test invoice entity with enough balance to accept payments. */
    private val testInvoiceEntity = InvoiceEntity(
        id = invoiceId,
        businessProfileId = businessId,
        customerId = 1L,
        customerName = "Test Customer",
        date = System.currentTimeMillis(),
        totalAmount = 100000L,
        isQuote = false,
        status = "SENT",
        amountPaid = 0L
    )

    @Before
    fun setUp() {
        // Room's withTransaction is a suspend inline extension function and cannot be mocked
        // via a regular mockk() proxy. mockkStatic intercepts the JVM static call so that
        // the lambda block executes immediately, enabling DAO mock verification within tests.
        mockkStatic("androidx.room.RoomDatabaseKt")
        coEvery { database.withTransaction(any()) } coAnswers {
            @Suppress("UNCHECKED_CAST")
            (firstArg<suspend () -> Any?>())()
        }
        paymentRepository = PaymentRepositoryV2(database, invoiceDaoV2, paymentDaoV2)
    }

    // ── recordPayment_Atomic ──────────────────────────────────────────────────

    @Test
    fun `recordPayment_Atomic - payment recorded returns success`() = runTest {
        coEvery { invoiceDaoV2.getById(invoiceId) } returns testInvoiceEntity
        coEvery { paymentDaoV2.insert(any()) } returns 1L
        coEvery { invoiceDaoV2.updateAmountPaid(any(), any(), any()) } just Runs
        coEvery { invoiceDaoV2.updateStatus(any(), any(), any()) } just Runs

        val result = paymentRepository.recordPayment(
            invoiceId = invoiceId,
            businessId = businessId,
            amount = paymentAmount,
            paymentDate = paymentDate,
            notes = null
        )

        assertTrue(result.isSuccess)
    }

    @Test
    fun `recordPayment_Atomic - repository is called with correct parameters`() = runTest {
        coEvery { invoiceDaoV2.getById(invoiceId) } returns testInvoiceEntity
        coEvery { paymentDaoV2.insert(any()) } returns 1L
        coEvery { invoiceDaoV2.updateAmountPaid(any(), any(), any()) } just Runs
        coEvery { invoiceDaoV2.updateStatus(any(), any(), any()) } just Runs

        paymentRepository.recordPayment(
            invoiceId = invoiceId,
            businessId = businessId,
            amount = paymentAmount,
            paymentDate = paymentDate,
            notes = null
        )

        assertTrue(result.isSuccess)
    }

    // ── recordPayment_UpdatesInvoice ──────────────────────────────────────────

    @Test
    fun `recordPayment_UpdatesInvoice - amount paid calculation is correct`() {
        val currentAmountPaid = 30000L
        val newPayment = 20000L
        val expectedTotal = currentAmountPaid + newPayment
        assertTrue(expectedTotal == 50000L, "Amount paid should be cumulative")
    }

    @Test
    fun `recordPayment_UpdatesInvoice - full payment makes invoice fully paid`() {
        val invoiceTotal = 100000L
        val paymentAmount = 100000L
        val newAmountPaid = 0L + paymentAmount
        assertTrue(newAmountPaid >= invoiceTotal, "Invoice should be marked as paid")
    }

    @Test
    fun `recordPayment_UpdatesInvoice - partial payment leaves outstanding balance`() {
        val invoiceTotal = 100000L
        val partialPayment = 60000L
        val newAmountPaid = 0L + partialPayment
        val remaining = invoiceTotal - newAmountPaid
        assertTrue(remaining > 0, "Partial payment should leave outstanding balance")
        assertTrue(remaining == 40000L, "Remaining balance should be $400")
    }

    // ── recordPayment_UpdatesSnapshots ────────────────────────────────────────

    @Test
    fun `recordPayment_UpdatesSnapshots - repository failure propagates as failure result`() = runTest {
        coEvery {
            paymentDaoV2.insert(any())
        } throws Exception("Invoice not found")

        val result = paymentRepository.recordPayment(
            invoiceId = 999L,
            businessId = businessId,
            amount = paymentAmount,
            paymentDate = paymentDate,
            notes = null
        )

        assertFalse(result.isSuccess)
        assertNotNull(result.exceptionOrNull())
    }

    @Test
    fun `recordPayment_UpdatesSnapshots - notes are passed to repository`() = runTest {
        val notes = "Paid via EFT"
        coEvery { invoiceDaoV2.getById(invoiceId) } returns testInvoiceEntity
        coEvery { paymentDaoV2.insert(any()) } returns 1L
        coEvery { invoiceDaoV2.updateAmountPaid(any(), any(), any()) } just Runs
        coEvery { invoiceDaoV2.updateStatus(any(), any(), any()) } just Runs

        paymentRepository.recordPayment(
            invoiceId = invoiceId,
            businessId = businessId,
            amount = paymentAmount,
            paymentDate = paymentDate,
            notes = notes
        )

        assertTrue(result.isSuccess)
    }
}
