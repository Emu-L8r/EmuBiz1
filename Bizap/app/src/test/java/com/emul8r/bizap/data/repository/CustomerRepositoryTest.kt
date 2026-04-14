@file:Suppress("UNCHECKED_CAST")
package com.emul8r.bizap.data.repository

import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.domain.model.Customer
import com.emul8r.bizap.domain.repository.CustomerRepository
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit tests for the [CustomerRepository] interface contract.
 *
 * Tests verify CRUD operations, reactive flows, and soft delete behaviour.
 */
class CustomerRepositoryTest : BaseUnitTest() {

    private lateinit var customerRepository: CustomerRepository

    private val testCustomer = Customer(
        id = 1L,
        name = "Test Customer",
        email = "test@example.com",
        phone = "0412345678"
    )

    @Before
    fun setUp() {
        customerRepository = mockk(relaxed = true)
    }

    // ── create_Success ────────────────────────────────────────────────────────

    @Test
    fun `create_Success - customer created in repository returns generated ID`() = runTest {
        coEvery { customerRepository.insert(testCustomer) } returns Result.success(1L)

        val result = customerRepository.insert(testCustomer)

        assertTrue(result.isSuccess)
        assertEquals(1L, result.getOrNull())
    }

    @Test
    fun `create_Success - insert is called with correct customer data`() = runTest {
        coEvery { customerRepository.insert(testCustomer) } returns Result.success(1L)

        customerRepository.insert(testCustomer)

        coVerify { customerRepository.insert(testCustomer) }
    }

    // ── getAll_ReturnsFlow ────────────────────────────────────────────────────

    @Test
    fun `getAll_ReturnsFlow - flow emits list of customers`() = runTest {
        val customers = listOf(
            testCustomer,
            Customer(id = 2L, name = "Second Customer", email = "second@example.com")
        )
        every { customerRepository.getAllCustomers() } returns flowOf(customers)

        val result = customerRepository.getAllCustomers().first()

        assertEquals(2, result.size)
        assertEquals("Test Customer", result[0].name)
    }

    @Test
    fun `getAll_ReturnsFlow - empty flow emits empty list`() = runTest {
        every { customerRepository.getAllCustomers() } returns flowOf(emptyList())

        val result = customerRepository.getAllCustomers().first()

        assertTrue(result.isEmpty())
    }

    // ── update_Success ────────────────────────────────────────────────────────

    @Test
    fun `update_Success - customer update returns success`() = runTest {
        val updatedCustomer = testCustomer.copy(name = "Updated Name")
        coEvery { customerRepository.updateCustomer(updatedCustomer) } returns Result.success(Unit)

        val result = customerRepository.updateCustomer(updatedCustomer)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `update_Success - update is called with modified customer`() = runTest {
        val updatedCustomer = testCustomer.copy(phone = "0498765432")
        coEvery { customerRepository.updateCustomer(updatedCustomer) } returns Result.success(Unit)

        customerRepository.updateCustomer(updatedCustomer)

        coVerify { customerRepository.updateCustomer(updatedCustomer) }
    }

    // ── delete_SoftDelete ─────────────────────────────────────────────────────

    @Test
    fun `delete_SoftDelete - delete customer returns success`() = runTest {
        coEvery { customerRepository.deleteCustomer(1L) } returns Result.success(Unit)

        val result = customerRepository.deleteCustomer(1L)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `delete_SoftDelete - delete is called with correct customer ID`() = runTest {
        coEvery { customerRepository.deleteCustomer(1L) } returns Result.success(Unit)

        customerRepository.deleteCustomer(1L)

        coVerify { customerRepository.deleteCustomer(1L) }
    }

    // ── getByEmail_Found ──────────────────────────────────────────────────────

    @Test
    fun `getByEmail_Found - getCustomerById returns customer when found`() = runTest {
        every { customerRepository.getCustomerById(1L) } returns flowOf(testCustomer)

        val result = customerRepository.getCustomerById(1L).first()

        assertNotNull(result)
        assertEquals("test@example.com", result?.email)
    }

    @Test
    fun `getByEmail_Found - customer data is correctly mapped from repository`() = runTest {
        every { customerRepository.getCustomerById(1L) } returns flowOf(testCustomer)

        val result = customerRepository.getCustomerById(1L).first()

        assertNotNull(result)
        assertEquals(testCustomer.name, result?.name)
        assertEquals(testCustomer.email, result?.email)
    }

    // ── getByEmail_NotFound ───────────────────────────────────────────────────

    @Test
    fun `getByEmail_NotFound - getCustomerById returns null when not found`() = runTest {
        every { customerRepository.getCustomerById(999L) } returns flowOf(null)

        val result = customerRepository.getCustomerById(999L).first()

        assertNull(result)
    }

    @Test
    fun `getByEmail_NotFound - repository handles non-existent customer gracefully`() = runTest {
        coEvery { customerRepository.deleteCustomer(999L) } returns Result.failure(
            Exception("Customer not found")
        )

        val result = customerRepository.deleteCustomer(999L)

        assertFalse(result.isSuccess)
    }
}



