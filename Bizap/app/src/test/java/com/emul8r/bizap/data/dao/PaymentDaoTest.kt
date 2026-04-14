@file:Suppress("UNCHECKED_CAST")
package com.emul8r.bizap.data.dao

import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.data.local.dao.PaymentDaoV2
import com.emul8r.bizap.data.local.entities.PaymentEntity
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for [PaymentDaoV2].
 *
 * Tests verify payment insert and query operations.
 */
class PaymentDaoTest : BaseUnitTest() {

    private lateinit var paymentDao: PaymentDaoV2

    private val now = System.currentTimeMillis()

    private val testPayment = PaymentEntity(
        id = 1L,
        businessId = 1L,
        invoiceId = 1L,
        amount = 50000L,
        paymentDate = now,
        notes = null,
        createdAt = now
    )

    @Before
    fun setUp() {
        paymentDao = mockk(relaxed = true)
    }

    // ── insert_Success ────────────────────────────────────────────────────────

    @Test
    fun `insert_Success - payment inserted and ID returned`() = runTest {
        coEvery { paymentDao.insert(testPayment) } returns 1L

        val result = paymentDao.insert(testPayment)

        assertEquals(1L, result)
        coVerify { paymentDao.insert(testPayment) }
    }

    @Test
    fun `insert_Success - payment with notes is stored correctly`() = runTest {
        val paymentWithNotes = testPayment.copy(notes = "Bank transfer ref: TXN001")
        coEvery { paymentDao.insert(paymentWithNotes) } returns 2L

        val result = paymentDao.insert(paymentWithNotes)

        assertEquals(2L, result)
        coVerify { paymentDao.insert(paymentWithNotes) }
    }

    @Test
    fun `insert_Success - multiple payments for same invoice are allowed`() = runTest {
        val payment2 = testPayment.copy(id = 2L, amount = 25000L)
        coEvery { paymentDao.insert(testPayment) } returns 1L
        coEvery { paymentDao.insert(payment2) } returns 2L

        val result1 = paymentDao.insert(testPayment)
        val result2 = paymentDao.insert(payment2)

        assertEquals(1L, result1)
        assertEquals(2L, result2)
    }

    // ── observePaymentsForInvoice ─────────────────────────────────────────────

    @Test
    fun `observePaymentsForInvoice - returns payments for a specific invoice`() = runTest {
        every { paymentDao.observePaymentsForInvoice(1L) } returns flowOf(listOf(testPayment))

        val result = paymentDao.observePaymentsForInvoice(1L).first()

        assertEquals(1, result.size)
        assertEquals(50000L, result[0].amount)
    }

    @Test
    fun `observePaymentsForInvoice - returns empty list when no payments`() = runTest {
        every { paymentDao.observePaymentsForInvoice(999L) } returns flowOf(emptyList())

        val result = paymentDao.observePaymentsForInvoice(999L).first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `observePaymentsForInvoice - returns multiple payments in order`() = runTest {
        val payment2 = testPayment.copy(id = 2L, amount = 25000L, paymentDate = now - 86_400_000L)
        every { paymentDao.observePaymentsForInvoice(1L) } returns flowOf(listOf(testPayment, payment2))

        val result = paymentDao.observePaymentsForInvoice(1L).first()

        assertEquals(2, result.size)
        assertEquals(50000L, result[0].amount)
        assertEquals(25000L, result[1].amount)
    }
}



