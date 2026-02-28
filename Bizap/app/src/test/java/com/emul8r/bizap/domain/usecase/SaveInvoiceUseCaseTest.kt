package com.emul8r.bizap.domain.usecase

import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.domain.repository.InvoiceRepository
import com.emul8r.bizap.util.TestDataFactory
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertTrue

/**
 * Unit tests for SaveInvoiceUseCase
 * Verifies business rules for creating invoices
 */
class SaveInvoiceUseCaseTest : BaseUnitTest() {
    
    private val repository: InvoiceRepository = mockk()
    private lateinit var useCase: SaveInvoiceUseCase
    
    @Before
    fun setup() {
        useCase = SaveInvoiceUseCase(repository)
    }
    
    @Test
    fun `test create invoice with empty items fails`() = runTest {
        // Arrange
        val invoice = TestDataFactory.createTestInvoice().copy(items = emptyList())
        
        // Act
        val result = useCase(invoice)
        
        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }
    
    @Test
    fun `test create invoice with valid data succeeds`() = runTest {
        // Arrange
        val invoice = TestDataFactory.createTestInvoice().copy(
            items = listOf(mockk())
        )
        coEvery { repository.saveInvoice(any()) } returns 1L
        
        // Act
        val result = useCase(invoice)
        
        // Assert
        assertTrue(result.isSuccess)
    }
}
