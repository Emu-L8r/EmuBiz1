package com.emul8r.bizap.data.repository

import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.data.local.CustomerDao
import com.emul8r.bizap.util.TestDataFactory
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Unit tests for CustomerRepositoryImpl.
 * Verifies the Result<T> pattern for customer data operations.
 */
class CustomerRepositoryTest : BaseUnitTest() {
    
    private val customerDao: CustomerDao = mockk()
    private lateinit var repository: CustomerRepositoryImpl
    
    @Before
    fun setup() {
        repository = CustomerRepositoryImpl(customerDao)
    }

    // --- insert() Result<Long> tests ---

    @Test
    fun `insert returns success result with row id on success`() = runTest {
        val customer = TestDataFactory.createTestCustomer()
        coEvery { customerDao.insert(any()) } returns 42L

        val result = repository.insert(customer)

        assertTrue(result.isSuccess)
        assertEquals(42L, result.getOrNull())
    }

    @Test
    fun `insert returns failure result on database error`() = runTest {
        val customer = TestDataFactory.createTestCustomer()
        coEvery { customerDao.insert(any()) } throws RuntimeException("DB error")

        val result = repository.insert(customer)

        assertTrue(result.isFailure)
        assertNotNull(result.exceptionOrNull())
    }

    // --- updateCustomer() Result<Unit> tests ---

    @Test
    fun `updateCustomer returns success result on success`() = runTest {
        val customer = TestDataFactory.createTestCustomer()
        coEvery { customerDao.update(any()) } just Runs

        val result = repository.updateCustomer(customer)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `updateCustomer returns failure result on database error`() = runTest {
        val customer = TestDataFactory.createTestCustomer()
        coEvery { customerDao.update(any()) } throws RuntimeException("DB error")

        val result = repository.updateCustomer(customer)

        assertTrue(result.isFailure)
        assertNotNull(result.exceptionOrNull())
    }

    // --- deleteCustomer() Result<Unit> tests ---

    @Test
    fun `deleteCustomer returns success result on success`() = runTest {
        coEvery { customerDao.deleteCustomer(1L) } just Runs

        val result = repository.deleteCustomer(1L)

        assertTrue(result.isSuccess)
        coVerify { customerDao.deleteCustomer(1L) }
    }

    @Test
    fun `deleteCustomer returns failure result on database error`() = runTest {
        coEvery { customerDao.deleteCustomer(any()) } throws RuntimeException("DB error")

        val result = repository.deleteCustomer(999L)

        assertTrue(result.isFailure)
        assertNotNull(result.exceptionOrNull())
    }
}
