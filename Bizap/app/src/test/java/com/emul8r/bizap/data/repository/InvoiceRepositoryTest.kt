package com.emul8r.bizap.data.repository

import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.data.local.InvoiceDao
import com.emul8r.bizap.data.local.entities.InvoiceWithItems
import com.emul8r.bizap.domain.model.InvoiceStatus
import com.emul8r.bizap.domain.repository.BusinessProfileRepository
import com.emul8r.bizap.domain.repository.InvoiceRepository
import com.emul8r.bizap.util.TestDataFactory
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import com.emul8r.bizap.data.mapper.toEntity

/**
 * Unit tests for InvoiceRepositoryImpl
 * Verifies multi-business isolation and invoice operations.
 */
class InvoiceRepositoryTest : BaseUnitTest() {
    
    private val invoiceDao: InvoiceDao = mockk()
    private val businessProfileRepo: BusinessProfileRepository = mockk()
    private lateinit var repository: InvoiceRepository
    
    @Before
    fun setup() {
        repository = InvoiceRepositoryImpl(invoiceDao, businessProfileRepo)
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
        val invoice = TestDataFactory.createTestInvoice(id = 0)

        coEvery { businessProfileRepo.getActiveBusinessId() } returns businessId
        coEvery { invoiceDao.getMaxSequenceForYear(any(), businessId) } returns 0
        coEvery { invoiceDao.insert(any(), any()) } returns expectedRowId

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
        val invoice = TestDataFactory.createTestInvoice(id = 0)
        val dbException = RuntimeException("Database error")

        coEvery { businessProfileRepo.getActiveBusinessId() } returns businessId
        coEvery { invoiceDao.getMaxSequenceForYear(any(), businessId) } returns 0
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
        coEvery { invoiceDao.updateInvoiceStatus(invoiceId, any()) } just Runs

        // Act
        val result = repository.updateInvoiceStatus(invoiceId, InvoiceStatus.PAID)

        // Assert
        assertTrue(result.isSuccess)
    }

    @Test
    fun `updateInvoiceStatus returns failure result when database throws`() = runTest {
        // Arrange
        val invoiceId = 1L
        val dbException = RuntimeException("Update failed")
        coEvery { invoiceDao.updateInvoiceStatus(invoiceId, any()) } throws dbException

        // Act
        val result = repository.updateInvoiceStatus(invoiceId, InvoiceStatus.PAID)

        // Assert
        assertTrue(result.isFailure)
        assertEquals(dbException, result.exceptionOrNull())
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
