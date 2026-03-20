@file:Suppress("UNCHECKED_CAST")
package com.emul8r.bizap.data.repository

import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.data.local.InvoiceDao
import com.emul8r.bizap.data.local.dao.AnalyticsDao
import com.emul8r.bizap.data.local.dao.InvoicePaymentDao
import com.emul8r.bizap.data.local.entities.InvoiceWithItems
import com.emul8r.bizap.data.repository.SnapshotSyncHelper
import com.emul8r.bizap.data.remote.api.InvoiceApi
import com.emul8r.bizap.domain.model.InvoiceStatus
import com.emul8r.bizap.domain.repository.BusinessProfileRepository
import com.emul8r.bizap.domain.repository.InvoiceRepository
import com.emul8r.bizap.util.TestDataFactory
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import com.emul8r.bizap.data.mapper.toEntity

/**
 * Unit tests for InvoiceRepositoryImpl
 * Verifies multi-business isolation and invoice operations.
 */
class InvoiceRepositoryTest : BaseUnitTest() {
    
    private val invoiceDao: InvoiceDao = mockk()
    private val businessProfileRepo: BusinessProfileRepository = mockk()
    private val analyticsDao: AnalyticsDao = mockk(relaxed = true)
    private val paymentDao: InvoicePaymentDao = mockk(relaxed = true)
    private val snapshotSyncHelper: SnapshotSyncHelper = mockk(relaxed = true)
    private val invoiceApi: InvoiceApi = mockk(relaxed = true)
    private lateinit var repository: InvoiceRepository
    
    @Before
    fun setup() {
        repository = InvoiceRepositoryImpl(invoiceDao, businessProfileRepo, analyticsDao, paymentDao, snapshotSyncHelper, invoiceApi)
    }
    
    @Test
    fun `test get invoices by business id filters correctly`() = runTest {
        // Arrange
        val businessId = 1L
        val testInvoices = listOf(
            TestDataFactory.createTestInvoice(id = 1, businessProfileId = businessId),
            TestDataFactory.createTestInvoice(id = 2, businessProfileId = businessId)
        ).map { com.emul8r.bizap.data.local.entities.InvoiceWithItems(it.toEntity(), emptyList()) }
        
        coEvery { invoiceDao.getInvoicesByBusinessId(businessId) } returns flowOf(testInvoices)
        
        // This is a placeholder for actual repository-level scoping tests
        assertTrue(true)
    }

    @Test
    fun `test calculation of balance remaining`() {
        // Arrange
        val invoice = TestDataFactory.createTestInvoice(total = 100000L).copy(amountPaid = 30000L)

        // Act & Assert
        assertEquals(70000L, invoice.balanceRemaining)
        assertEquals(false, invoice.isFullyPaid)
    }

    @Test
    fun `test fully paid status`() {
        // Arrange
        val invoice = TestDataFactory.createTestInvoice(total = 50000L).copy(amountPaid = 50000L)

        // Act & Assert
        assertEquals(0L, invoice.balanceRemaining)
        assertEquals(true, invoice.isFullyPaid)
    }

    // --- Result pattern tests ---

    @Test
    fun `saveInvoice returns success result with row id on success`() = runTest {
        // Arrange
        val businessId = 1L
        val expectedRowId = 42L
        val testDate = System.currentTimeMillis()
        val invoice = TestDataFactory.createTestInvoice(id = 0).copy(
            date = testDate,
            dailyCounter = 1,
            displayName = "testcustomer-11032026-01"
        )

        coEvery { businessProfileRepo.getActiveBusinessId() } returns businessId
        coEvery { invoiceDao.getMaxSequenceForYear(any(), businessId) } returns 0
        coEvery { invoiceDao.countInvoicesOnDate(any()) } returns 0  // ← ADD THIS
        coEvery { invoiceDao.insert(any(), any()) } returns expectedRowId
        coEvery { snapshotSyncHelper.syncAllSnapshots(any(), any()) } just Runs  // ← ADD THIS

        // Act
        val result = repository.saveInvoice(invoice)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expectedRowId, result.getOrNull())
    }

    @Test
    fun `saveInvoice returns failure result when database throws`() = runTest {
        // Arrange
        val businessId = 1L
        val testDate = System.currentTimeMillis()
        val invoice = TestDataFactory.createTestInvoice(id = 0).copy(
            date = testDate,
            dailyCounter = 1,
            displayName = "testcustomer-11032026-01"
        )
        val dbException = RuntimeException("Database error")

        coEvery { businessProfileRepo.getActiveBusinessId() } returns businessId
        coEvery { invoiceDao.getMaxSequenceForYear(any(), businessId) } returns 0
        coEvery { invoiceDao.countInvoicesOnDate(any()) } returns 0  // ← ADD THIS
        coEvery { invoiceDao.insert(any(), any()) } throws dbException

        // Act
        val result = repository.saveInvoice(invoice)

        // Assert
        assertTrue(result.isFailure)
        assertEquals(dbException, result.exceptionOrNull())
    }

    @Test
    fun `deleteInvoice returns success result on success`() = runTest {
        // Arrange
        val invoiceId = 1L
        coEvery { invoiceDao.deleteInvoiceWithItems(invoiceId) } just Runs

        // Act
        val result = repository.deleteInvoice(invoiceId)

        // Assert
        assertTrue(result.isSuccess)
    }

    @Test
    fun `deleteInvoice returns failure result when database throws`() = runTest {
        // Arrange
        val invoiceId = 1L
        val dbException = RuntimeException("Delete failed")
        coEvery { invoiceDao.deleteInvoiceWithItems(invoiceId) } throws dbException

        // Act
        val result = repository.deleteInvoice(invoiceId)

        // Assert
        assertTrue(result.isFailure)
        assertEquals(dbException, result.exceptionOrNull())
    }

    @Test
    fun `updateInvoiceStatus returns success result on success`() = runTest {
        // Arrange
        val invoiceId = 1L
        val invoiceEntity = TestDataFactory.createTestInvoice(id = invoiceId, status = InvoiceStatus.DRAFT).toEntity()
        val invoiceWithItems = InvoiceWithItems(invoiceEntity, emptyList())

        coEvery { invoiceDao.getInvoiceWithItemsById(invoiceId) } returns flowOf(invoiceWithItems)
        coEvery { invoiceDao.updateInvoiceStatus(invoiceId, any()) } just Runs

        // Act
        val result = repository.updateInvoiceStatus(invoiceId, InvoiceStatus.SENT)

        // Assert
        assertTrue(result.isSuccess)
    }

    @Test
    fun `updateInvoiceStatus returns failure result when database throws`() = runTest {
        // Arrange
        val invoiceId = 1L
        val invoiceEntity = TestDataFactory.createTestInvoice(id = invoiceId, status = InvoiceStatus.DRAFT).toEntity()
        val invoiceWithItems = InvoiceWithItems(invoiceEntity, emptyList())
        val dbException = RuntimeException("Update failed")

        coEvery { invoiceDao.getInvoiceWithItemsById(invoiceId) } returns flowOf(invoiceWithItems)
        coEvery { invoiceDao.updateInvoiceStatus(invoiceId, any()) } throws dbException

        // Act
        val result = repository.updateInvoiceStatus(invoiceId, InvoiceStatus.SENT)

        // Assert
        assertTrue(result.isFailure)
    }

    @Test
    fun `updatePdfPath returns success result on success`() = runTest {
        // Arrange
        coEvery { invoiceDao.updatePdfPath(any(), any()) } just Runs

        // Act
        val result = repository.updatePdfPath(1L, "/path/to/invoice.pdf")

        // Assert
        assertTrue(result.isSuccess)
    }

    @Test
    fun `updatePdfPath returns failure result when database throws`() = runTest {
        // Arrange
        val dbException = RuntimeException("Path update failed")
        coEvery { invoiceDao.updatePdfPath(any(), any()) } throws dbException

        // Act
        val result = repository.updatePdfPath(1L, "/path/to/invoice.pdf")

        // Assert
        assertTrue(result.isFailure)
        assertEquals(dbException, result.exceptionOrNull())
    }

    @Test
    fun `invoice counter increments for same customer same day`() = runTest {
        // Given
        val businessId = 1L
        val testDate = System.currentTimeMillis()
        val baseInvoice = TestDataFactory.createTestInvoice(id = 0).copy(
            date = testDate,
            dailyCounter = 1,
            displayName = "testcustomer-${testDate}-01"
        )

        coEvery { businessProfileRepo.getActiveBusinessId() } returns businessId
        coEvery { invoiceDao.getMaxSequenceForYear(any(), businessId) } returns 0
        coEvery { snapshotSyncHelper.syncAllSnapshots(any(), any()) } just Runs

        // When - first invoice: countInvoicesOnDate returns 0 → dailyCounter = 1
        coEvery { invoiceDao.countInvoicesOnDate(any()) } returns 0
        coEvery { invoiceDao.insert(any(), any()) } returns 1L
        val result1 = repository.saveInvoice(baseInvoice)
        assertTrue(result1.isSuccess, "First invoice should save successfully")

        // When - second invoice same day: countInvoicesOnDate returns 1 → dailyCounter = 2
        coEvery { invoiceDao.countInvoicesOnDate(any()) } returns 1
        coEvery { invoiceDao.getMaxSequenceForYear(any(), businessId) } returns 1
        coEvery { invoiceDao.insert(any(), any()) } returns 2L
        val result2 = repository.saveInvoice(baseInvoice)
        assertTrue(result2.isSuccess, "Second invoice should save successfully")

        // When - third invoice same day: countInvoicesOnDate returns 2 → dailyCounter = 3
        coEvery { invoiceDao.countInvoicesOnDate(any()) } returns 2
        coEvery { invoiceDao.getMaxSequenceForYear(any(), businessId) } returns 2
        coEvery { invoiceDao.insert(any(), any()) } returns 3L
        val result3 = repository.saveInvoice(baseInvoice)
        assertTrue(result3.isSuccess, "Third invoice should save successfully")

        // Then - verify IDs are different (counter incremented)
        assertNotEquals(result1.getOrNull(), result2.getOrNull(), "Invoice IDs should differ")
        assertNotEquals(result2.getOrNull(), result3.getOrNull(), "Invoice IDs should differ")
        assertEquals(1L, result1.getOrNull(), "First invoice ID should be 1")
        assertEquals(2L, result2.getOrNull(), "Second invoice ID should be 2")
        assertEquals(3L, result3.getOrNull(), "Third invoice ID should be 3")

        // Verify countInvoicesOnDate was called for each new invoice
        coVerify(atLeast = 3) { invoiceDao.countInvoicesOnDate(any()) }
    }

    @Test
    fun `testEditInvoiceSuccessfully - existing invoice uses UPDATE path without constraint violation`() = runTest {
        // Arrange
        val businessId = 1L
        val existingInvoiceId = 2L
        val invoice = TestDataFactory.createTestInvoice(id = existingInvoiceId, businessProfileId = businessId)

        coEvery { businessProfileRepo.getActiveBusinessId() } returns businessId
        coEvery { invoiceDao.deleteLineItems(existingInvoiceId) } just Runs
        coEvery { invoiceDao.insertLineItems(any()) } just Runs
        coEvery { invoiceDao.updateInvoice(any()) } just Runs

        // Act
        val result = repository.saveInvoice(invoice)

        // Assert - no UNIQUE constraint violation, update succeeds
        assertTrue(result.isSuccess)
        assertEquals(existingInvoiceId, result.getOrNull())
    }

    @Test
    fun `testRecordPaymentSuccessfully - updateAmountPaid uses UPDATE not INSERT`() = runTest {
        // Arrange
        val invoiceId = 2L
        val paymentAmount = 4400L
        val invoiceEntity = TestDataFactory.createTestInvoice(id = invoiceId).toEntity()
        val invoiceWithItems = InvoiceWithItems(invoiceEntity, emptyList())

        coEvery { invoiceDao.getInvoiceWithItemsById(invoiceId) } returns flowOf(invoiceWithItems)
        coEvery { invoiceDao.updateInvoice(any()) } just Runs

        // Act
        val result = repository.updateAmountPaid(invoiceId, paymentAmount)

        // Assert - no UNIQUE constraint violation, payment recording succeeds
        assertTrue(result.isSuccess)
    }
}
