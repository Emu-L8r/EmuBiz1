@file:Suppress("UNCHECKED_CAST")
package com.emul8r.bizap.data.dao

import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.data.local.InvoiceDao
import com.emul8r.bizap.data.local.entities.InvoiceEntity
import com.emul8r.bizap.data.local.entities.InvoiceWithItems
import com.emul8r.bizap.data.local.entities.LineItemEntity
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Unit tests for [InvoiceDao].
 *
 * Tests use mocked DAO to verify invoice and line item query operations.
 */
class InvoiceDaoTest : BaseUnitTest() {

    private lateinit var invoiceDao: InvoiceDao

    private val now = System.currentTimeMillis()

    private val testInvoiceEntity = InvoiceEntity(
        id = 1L,
        businessProfileId = 1L,
        customerId = 1L,
        customerName = "Test Customer",
        date = now,
        dueDate = now + 86_400_000L,
        totalAmount = 50000L,
        currencyCode = "AUD",
        status = "DRAFT",
        isQuote = false,
        version = 1
    )

    private val testLineItem = LineItemEntity(
        id = 1L,
        invoiceId = 1L,
        description = "Service",
        quantity = 1.0,
        unitPrice = 50000L
    )

    private val testInvoiceWithItems = InvoiceWithItems(
        invoice = testInvoiceEntity,
        items = listOf(testLineItem)
    )

    @Before
    fun setUp() {
        invoiceDao = mockk(relaxed = true)
    }

    // ── insert_Success ────────────────────────────────────────────────────────

    @Test
    fun `insert_Success - invoice inserted returns generated ID`() = runTest {
        coEvery { invoiceDao.insertInvoice(testInvoiceEntity) } returns 1L

        val result = invoiceDao.insertInvoice(testInvoiceEntity)

        assertEquals(1L, result)
        coVerify { invoiceDao.insertInvoice(testInvoiceEntity) }
    }

    @Test
    fun `insert_Success - line items are associated with the invoice`() = runTest {
        val lineItems = listOf(testLineItem)
        coEvery { invoiceDao.insertLineItems(lineItems) } returns Unit

        invoiceDao.insertLineItems(lineItems)

        coVerify { invoiceDao.insertLineItems(lineItems) }
    }

    // ── getWithItems_LoadsRelationship ─────────────────────────────────────────

    @Test
    fun `getWithItems_LoadsRelationship - invoice loaded with line items`() = runTest {
        every { invoiceDao.getInvoiceWithItemsById(1L) } returns flowOf(testInvoiceWithItems)

        val result = invoiceDao.getInvoiceWithItemsById(1L).first()

        assertNotNull(result)
        assertEquals(1, result?.items?.size)
        assertEquals("Service", result?.items?.get(0)?.description)
    }

    @Test
    fun `getWithItems_LoadsRelationship - subtotal is calculated from line items`() = runTest {
        every { invoiceDao.getInvoiceWithItemsById(1L) } returns flowOf(testInvoiceWithItems)

        val result = invoiceDao.getInvoiceWithItemsById(1L).first()

        assertNotNull(result)
        val subtotal = result!!.subtotal
        assertEquals(50000L, subtotal, "Subtotal should match unitPrice × quantity")
    }

    @Test
    fun `getWithItems_LoadsRelationship - invoice without items has zero subtotal`() = runTest {
        val emptyInvoice = testInvoiceWithItems.copy(items = emptyList())
        every { invoiceDao.getInvoiceWithItemsById(2L) } returns flowOf(emptyInvoice)

        val result = invoiceDao.getInvoiceWithItemsById(2L).first()

        assertNotNull(result)
        assertEquals(0L, result!!.subtotal, "Empty invoice should have zero subtotal")
    }

    // ── observeByCustomer_ReturnsFlow ─────────────────────────────────────────

    @Test
    fun `observeByCustomer_ReturnsFlow - flow emits invoices for customer`() = runTest {
        val customerId = 1L
        val businessId = 1L
        every { invoiceDao.getInvoicesForCustomer(customerId, businessId) } returns flowOf(
            listOf(testInvoiceWithItems)
        )

        val result = invoiceDao.getInvoicesForCustomer(customerId, businessId).first()

        assertEquals(1, result.size)
        assertEquals(customerId, result[0].invoice.customerId)
    }

    @Test
    fun `observeByCustomer_ReturnsFlow - empty list when no invoices for customer`() = runTest {
        val customerId = 999L
        val businessId = 1L
        every { invoiceDao.getInvoicesForCustomer(customerId, businessId) } returns flowOf(emptyList())

        val result = invoiceDao.getInvoicesForCustomer(customerId, businessId).first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `observeByCustomer_ReturnsFlow - multiple invoices returned correctly`() = runTest {
        val customerId = 1L
        val businessId = 1L
        val invoice2 = testInvoiceWithItems.copy(
            invoice = testInvoiceEntity.copy(id = 2L, totalAmount = 25000L)
        )
        every { invoiceDao.getInvoicesForCustomer(customerId, businessId) } returns flowOf(
            listOf(testInvoiceWithItems, invoice2)
        )

        val result = invoiceDao.getInvoicesForCustomer(customerId, businessId).first()

        assertEquals(2, result.size)
    }
}
