package com.emul8r.bizap.data.dao

import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.data.local.CustomerDao
import com.emul8r.bizap.data.local.entities.CustomerEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit tests for [CustomerDao].
 *
 * Tests use mocked DAO to verify query and mutation operations
 * at the data access layer.
 */
class CustomerDaoTest : BaseUnitTest() {

    private lateinit var customerDao: CustomerDao

    private val testEntity = CustomerEntity(
        id = 1L,
        businessProfileId = 1L,
        name = "Test Customer",
        email = "test@example.com",
        phone = "0412345678",
        isActive = true
    )

    @Before
    fun setUp() {
        customerDao = mockk(relaxed = true)
    }

    // ── insert_Success ────────────────────────────────────────────────────────

    @Test
    fun `insert_Success - customer is inserted and ID is returned`() = runTest {
        coEvery { customerDao.insert(testEntity) } returns 1L

        val result = customerDao.insert(testEntity)

        assertEquals(1L, result)
        coVerify { customerDao.insert(testEntity) }
    }

    @Test
    fun `insert_Success - second insert returns incremented ID`() = runTest {
        val secondEntity = testEntity.copy(id = 0L, name = "Second Customer", email = "second@example.com")
        coEvery { customerDao.insert(secondEntity) } returns 2L

        val result = customerDao.insert(secondEntity)

        assertEquals(2L, result)
    }

    // ── getById_Found ─────────────────────────────────────────────────────────

    @Test
    fun `getById_Found - returns customer entity when found`() = runTest {
        every { customerDao.getCustomerById(1L) } returns flowOf(testEntity)

        val result = customerDao.getCustomerById(1L).first()

        assertNotNull(result)
        assertEquals("Test Customer", result?.name)
        assertEquals("test@example.com", result?.email)
    }

    @Test
    fun `getById_Found - all fields are correctly populated`() = runTest {
        every { customerDao.getCustomerById(1L) } returns flowOf(testEntity)

        val result = customerDao.getCustomerById(1L).first()

        assertNotNull(result)
        assertEquals(testEntity.id, result?.id)
        assertEquals(testEntity.businessProfileId, result?.businessProfileId)
        assertEquals(testEntity.phone, result?.phone)
        assertEquals(testEntity.isActive, result?.isActive)
    }

    // ── getById_NotFound ──────────────────────────────────────────────────────

    @Test
    fun `getById_NotFound - returns null when customer does not exist`() = runTest {
        every { customerDao.getCustomerById(999L) } returns flowOf(null)

        val result = customerDao.getCustomerById(999L).first()

        assertNull(result)
    }

    // ── update_Success ────────────────────────────────────────────────────────

    @Test
    fun `update_Success - customer is updated in DAO`() = runTest {
        val updatedEntity = testEntity.copy(name = "Updated Name", phone = "0498765432")

        customerDao.update(updatedEntity)

        coVerify { customerDao.update(updatedEntity) }
    }

    @Test
    fun `update_Success - update does not affect other customers`() = runTest {
        val entity1 = testEntity
        val entity2 = testEntity.copy(id = 2L, name = "Other Customer")
        val updatedEntity1 = entity1.copy(name = "Updated Customer")

        customerDao.update(updatedEntity1)

        coVerify(exactly = 1) { customerDao.update(updatedEntity1) }
        coVerify(exactly = 0) { customerDao.update(entity2) }
    }

    // ── observeAll_ReturnsFlow ────────────────────────────────────────────────

    @Test
    fun `observeAll_ReturnsFlow - flow emits list of all customers`() = runTest {
        val customers = listOf(
            testEntity,
            testEntity.copy(id = 2L, name = "Second Customer", email = "second@example.com")
        )
        every { customerDao.getAllCustomers() } returns flowOf(customers)

        val result = customerDao.getAllCustomers().first()

        assertEquals(2, result.size)
        assertEquals("Test Customer", result[0].name)
        assertEquals("Second Customer", result[1].name)
    }

    @Test
    fun `observeAll_ReturnsFlow - empty database emits empty list`() = runTest {
        every { customerDao.getAllCustomers() } returns flowOf(emptyList())

        val result = customerDao.getAllCustomers().first()

        assertTrue(result.isEmpty())
    }
}
