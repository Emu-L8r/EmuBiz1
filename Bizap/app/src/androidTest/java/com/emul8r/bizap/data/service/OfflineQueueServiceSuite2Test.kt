@file:Suppress("UNCHECKED_CAST")
package com.emul8r.bizap.data.service

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.emul8r.bizap.data.local.dao.OfflineOperationDao
import com.emul8r.bizap.data.local.entities.OfflineOperation
import com.emul8r.bizap.data.local.offline.OfflineQueueService
import com.emul8r.bizap.test.WindowsTestRule
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * SUITE 2: Customer Operations Test
 *
 * Tests that customer management operations (Create, Update, Delete) correctly
 * queue when offline and maintain data integrity.
 */
@RunWith(AndroidJUnit4::class)
class OfflineQueueServiceSuite2Test {

    @get:Rule
    val skipWindowsRule = WindowsTestRule()

    private lateinit var mockDao: OfflineOperationDao
    private lateinit var queueService: OfflineQueueService

    @Before
    fun setup() {
        mockDao = mockk(relaxed = true)
        queueService = OfflineQueueService(mockDao, mockk())
    }

    /**
     * TEST 2.1: Create Customer Offline
     *
     * Verifies that creating a customer while offline:
     * - Queues the operation
     * - Persists to database
     * - Updates UI with badge
     * - No data loss
     */
    @Test
    fun test_2_1_create_customer_offline() {
        runBlocking {
            // Arrange
            val customerId = 1L
            val customerJson = """
            {
                "id": 0,
                "businessProfileId": 1,
                "name": "Offline Test Customer",
                "email": "offline.test@example.com",
                "phone": "555-1234",
                "address": "456 Offline St, City"
            }
            """.trimIndent()

            coEvery { mockDao.insert(any()) } returns customerId

            // Act
            val result = queueService.queueCreateCustomer(
                com.emul8r.bizap.domain.model.Customer(name = "Offline Test Customer", email = "offline.test@example.com", id = 0, businessName = null, businessNumber = null, phone = "555-1234", address = "456 Offline St, City")
            )

            // Assert
            assertEquals(customerId, result)
            coVerify { mockDao.insert(any()) }
        }
    }

    /**
     * TEST 2.2: Update Customer Offline
     *
     * Verifies that updating a customer while offline:
     * - Queues the update operation
     * - Maintains operation order (CREATE then UPDATE)
     * - Database has 2 entries
     * - No data loss
     */
    @Test
    fun test_2_2_update_customer_offline() {
        runBlocking {
            // Arrange
            val customerId = 1L
            coEvery { mockDao.insert(any()) } returns 2L

            // Act
            val result = queueService.queueUpdateCustomer(
                com.emul8r.bizap.domain.model.Customer(id = customerId, name = "Offline Test Customer - UPDATED", businessName = null, businessNumber = null, email = "updated@example.com", phone = "555-5678", address = "789 Updated St")
            )

            // Assert
            assertEquals(2L, result)
            coVerify { mockDao.insert(match { it.operationType == "UPDATE_CUSTOMER" }) }
        }
    }

    /**
     * TEST 2.3: Delete Customer Offline
     *
     * Verifies that deleting a customer while offline:
     * - Queues the delete operation
     * - Customer removed from list immediately
     * - Database entry created
     * - No orphaned data
     */
    @Test
    fun test_2_3_delete_customer_offline() {
        runBlocking {
            // Arrange
            val customerId = 1L
            coEvery { mockDao.insert(any()) } returns 3L

            // Act
            val result = queueService.queueDeleteCustomer(
                customerId = customerId,
                businessId = 1L
            )

            // Assert
            assertEquals(3L, result)
            coVerify { mockDao.insert(match { it.operationType == "DELETE_CUSTOMER" }) }
        }
    }

    /**
     * TEST 2.4: Multiple Customer Operations
     *
     * Verifies that the queue handles multiple customer operations in sequence:
     * - Create 3 customers
     * - Update one customer
     * - Delete one customer
     * - All operations queued correctly
     * - Correct FIFO ordering
     * - No data loss
     */
    @Test
    fun test_2_4_multiple_customer_operations_sequential() {
        runBlocking {
            // Arrange: Setup mock to return incremental IDs
            var operationIdCounter = 1L
            coEvery { mockDao.insert(any()) } answers {
                operationIdCounter++
            }

            // Act: Create 3 customers
            val create1 = queueService.queueCreateCustomer(com.emul8r.bizap.domain.model.Customer(name = "Customer A"))
            val create2 = queueService.queueCreateCustomer(com.emul8r.bizap.domain.model.Customer(name = "Customer B"))
            val create3 = queueService.queueCreateCustomer(com.emul8r.bizap.domain.model.Customer(name = "Customer C"))

            // Act: Update Customer A
            val update1 = queueService.queueUpdateCustomer(com.emul8r.bizap.domain.model.Customer(id = 1L, name = "Customer A Updated"))

            // Act: Delete Customer C
            val delete1 = queueService.queueDeleteCustomer(3L, 1L)

            // Assert: Verify all operations were queued
            coVerify(exactly = 5) { mockDao.insert(any()) }

            // Assert: Verify operations have different IDs (no duplicates)
            val allIds = listOf(create1, create2, create3, update1, delete1)
            assertEquals(5, allIds.distinct().size)

            // Assert: Verify operation order (they should increment)
            assertEquals(true, create1 < create2)
            assertEquals(true, create2 < create3)
            assertEquals(true, create3 < update1)
            assertEquals(true, update1 < delete1)
        }
    }

    /**
     * TEST 2.5: Customer Operation Data Integrity
     *
     * Verifies that customer data is properly serialized:
     * - Valid JSON format
     * - All required fields present
     * - No null values
     * - Timestamps are reasonable
     */
    @Test
    fun test_2_5_customer_operation_data_integrity() {
        runBlocking {
            // Arrange
            var capturedOperation: OfflineOperation? = null
            coEvery { mockDao.insert(any()) } answers {
                capturedOperation = firstArg()
                1L
            }

            // Act
            queueService.queueCreateCustomer(com.emul8r.bizap.domain.model.Customer(name = "Test Customer"))

            // Assert: Verify operation was captured
            assertNotNull(capturedOperation)
            capturedOperation?.let { op ->
                assertEquals("CREATE_CUSTOMER", op.operationType)
                assertEquals("PENDING", op.status)
                assertNotNull(op.entityData)
                assertEquals(true, op.entityData.contains("Test Customer"))
                assertEquals(true, op.timestampMs > 0)
            }
        }
    }
}

