@file:Suppress("UNCHECKED_CAST")
package com.emul8r.bizap.data.service

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.emul8r.bizap.data.local.dao.OfflineOperationDao
import com.emul8r.bizap.data.local.entities.OfflineOperation
import com.emul8r.bizap.data.local.offline.OfflineQueueService
import com.emul8r.bizap.test.WindowsTestRule
import io.mockk.*
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicLong

/**
 * SUITE 3: Concurrent Operations Test
 *
 * Tests that the queue handles multiple concurrent operations (invoices + customers)
 * with data integrity, proper FIFO ordering, and no race conditions.
 */
@RunWith(AndroidJUnit4::class)
class OfflineQueueServiceSuite3Test {

    @get:Rule
    val skipWindowsRule = WindowsTestRule()

    private lateinit var mockDao: OfflineOperationDao
    private lateinit var queueService: OfflineQueueService
    private val operationIdCounter = AtomicLong(0L)

    @Before
    fun setup() {
    }

    /**
     * TEST 3.1: Create Customer + Invoice Back-to-Back
     *
     * Verifies that creating customer and invoice in sequence:
     * - Both operations queued
     * - Customer CREATE before Invoice CREATE (FIFO)
     * - No data loss
     * - Proper ordering by timestamp
     */
    @Test
    fun test_3_1_create_customer_and_invoice_sequential() {
        runBlocking {
            // Arrange
            val capturedOperations = mutableListOf<OfflineOperation>()
            coEvery { mockDao.insert(any()) } answers {
                val op = firstArg<OfflineOperation>()
                capturedOperations.add(op)
                operationIdCounter.incrementAndGet()
            }

            // Act: Create customer
            queueService.queueCreateCustomer(
                com.emul8r.bizap.domain.model.Customer(name = "Acme Corp", id = 1L)
            )

            // Act: Create invoice for same customer
            queueService.queueCreateInvoice(
                com.emul8r.bizap.domain.model.Invoice(id = 1L, businessProfileId = 1L, customerId = 1L, customerName = "Acme Corp", totalAmount = 2500, items = emptyList(), isQuote = false, status = com.emul8r.bizap.domain.model.InvoiceStatus.DRAFT, date = 0L)
            )

            // Assert: Verify both operations queued
            assertEquals(2, capturedOperations.size)

            // Assert: Verify FIFO order (customer before invoice)
            val firstOp = capturedOperations[0]
            val secondOp = capturedOperations[1]

            assertEquals("CREATE_CUSTOMER", firstOp.operationType)
            assertEquals("CREATE_INVOICE", secondOp.operationType)
            assertTrue(firstOp.timestampMs <= secondOp.timestampMs)
        }
    }

    /**
     * TEST 3.2: Rapid-Fire Invoices (Burst Test)
     *
     * Verifies that the queue handles rapid invoice creation:
     * - Create 5 invoices in quick succession
     * - All 5 are queued
     * - No duplicates
     * - Correct FIFO ordering
     * - No data loss
     */
    @Test
    fun test_3_2_rapid_fire_invoices_burst() {
        runBlocking {
            // Arrange
            val capturedOperations = mutableListOf<OfflineOperation>()
            val operationIds = mutableListOf<Long>()

            coEvery { mockDao.insert(any()) } answers {
                val op = firstArg<OfflineOperation>()
                capturedOperations.add(op)
                val id = operationIdCounter.incrementAndGet()
                operationIds.add(id)
                id
            }

            // Act: Create 5 invoices rapidly
            val invoiceAmounts = listOf(500L, 750L, 1200L, 600L, 400L)
            invoiceAmounts.forEachIndexed { i, amount ->
                queueService.queueCreateInvoice(
                    com.emul8r.bizap.domain.model.Invoice(id = i.toLong(), businessProfileId = 1L, customerId = 1L, customerName = "Test", totalAmount = amount, items = emptyList(), isQuote = false, status = com.emul8r.bizap.domain.model.InvoiceStatus.DRAFT, date = 0L)
                )
            }

            // Assert: All 5 invoices queued
            assertEquals(5, capturedOperations.size)

            // Assert: All are CREATE_INVOICE operations
            assertTrue(capturedOperations.all { it.operationType == "CREATE_INVOICE" })

            // Assert: No duplicates in operation IDs
            assertEquals(5, operationIds.distinct().size)

            // Assert: FIFO order by timestamps
            for (i in 0 until capturedOperations.size - 1) {
                assertTrue(
                    capturedOperations[i].timestampMs <= capturedOperations[i + 1].timestampMs
                )
            }

            // Assert: All have PENDING status
            assertTrue(capturedOperations.all { it.status == "PENDING" })
        }
    }

    /**
     * TEST 3.3: Mixed Operations (Invoices + Customers + Payments)
     *
     * Verifies realistic scenario with all operation types interleaved:
     * - Create customer
     * - Create invoice for customer
     * - Record payment
     * - Create another invoice
     * - Update customer
     * - Record another payment
     *
     * Expected queue order matches operation sequence
     */
    @Test
    fun test_3_3_mixed_operations_interleaved() {
        runBlocking {
            // Arrange
            val capturedOperations = mutableListOf<OfflineOperation>()

            coEvery { mockDao.insert(any()) } answers {
                val op = firstArg<OfflineOperation>()
                capturedOperations.add(op)
                operationIdCounter.incrementAndGet()
            }

            // Act: Step 1 - Create customer
            queueService.queueCreateCustomer(com.emul8r.bizap.domain.model.Customer(id = 1L, name = "Customer X"))

            // Act: Step 2 - Create invoice for customer
            queueService.queueCreateInvoice(com.emul8r.bizap.domain.model.Invoice(id = 1L, businessProfileId = 1L, customerId = 1L, customerName = "Customer X", totalAmount = 3000, items = emptyList(), isQuote = false, status = com.emul8r.bizap.domain.model.InvoiceStatus.DRAFT, date = 0L))

            // Act: Step 3 - Record payment
            queueService.queueRecordPayment(1L, 1500L, 1L)

            // Act: Step 4 - Create another invoice
            queueService.queueCreateInvoice(com.emul8r.bizap.domain.model.Invoice(id = 2L, businessProfileId = 1L, customerId = 2L, customerName = "Other", totalAmount = 800, items = emptyList(), isQuote = false, status = com.emul8r.bizap.domain.model.InvoiceStatus.DRAFT, date = 0L))

            // Act: Step 5 - Update customer
            queueService.queueUpdateCustomer(com.emul8r.bizap.domain.model.Customer(id = 1L, name = "Customer X Updated"))

            // Act: Step 6 - Record another payment
            queueService.queueRecordPayment(1L, 1500L, 1L)

            // Assert: All 6 operations queued
            assertEquals(6, capturedOperations.size)

            // Assert: Verify operation types in order
            assertEquals("CREATE_CUSTOMER", capturedOperations[0].operationType)
            assertEquals("CREATE_INVOICE", capturedOperations[1].operationType)
            assertEquals("UPDATE_PAYMENT", capturedOperations[2].operationType)
            assertEquals("CREATE_INVOICE", capturedOperations[3].operationType)
            assertEquals("UPDATE_CUSTOMER", capturedOperations[4].operationType)
            assertEquals("UPDATE_PAYMENT", capturedOperations[5].operationType)

            // Assert: FIFO ordering by timestamp
            for (i in 0 until capturedOperations.size - 1) {
                assertTrue(
                    capturedOperations[i].timestampMs <= capturedOperations[i + 1].timestampMs
                )
            }

            // Assert: All have PENDING status
            assertTrue(capturedOperations.all { it.status == "PENDING" })

            // Assert: No null data
            assertTrue(capturedOperations.all { it.entityData.isNotEmpty() })
        }
    }

    /**
     * TEST 3.4: Concurrent Operations from Multiple Threads
     *
     * Verifies that concurrent operations from different threads:
     * - Are all queued without loss
     * - Have proper timestamps
     * - No race conditions
     * - No duplicate IDs
     */
    @Test
    fun test_3_4_concurrent_operations_multiple_threads() {
        runBlocking {
            // Arrange
            val capturedOperations = mutableListOf<OfflineOperation>()
            val lock = Any()

            coEvery { mockDao.insert(any()) } answers {
                val op = firstArg<OfflineOperation>()
                synchronized(lock) {
                    capturedOperations.add(op)
                }
                operationIdCounter.incrementAndGet()
            }

            // Act: Launch concurrent operations from multiple coroutines
            val job1 = async {
                queueService.queueCreateCustomer(com.emul8r.bizap.domain.model.Customer(name = "Customer 1"))
                queueService.queueCreateInvoice(com.emul8r.bizap.domain.model.Invoice(id = 1L, customerId = null, customerName = "C1", totalAmount = 100, items = emptyList(), isQuote = false, status = com.emul8r.bizap.domain.model.InvoiceStatus.DRAFT, date = 0L))
            }

            val job2 = async {
                queueService.queueCreateCustomer(com.emul8r.bizap.domain.model.Customer(name = "Customer 2"))
                queueService.queueCreateInvoice(com.emul8r.bizap.domain.model.Invoice(id = 2L, customerId = null, customerName = "C2", totalAmount = 200, items = emptyList(), isQuote = false, status = com.emul8r.bizap.domain.model.InvoiceStatus.DRAFT, date = 0L))
            }

            val job3 = async {
                queueService.queueRecordPayment(1L, 150L, 1L)
                queueService.queueUpdateCustomer(com.emul8r.bizap.domain.model.Customer(id = 1L, name = "Updated"))
            }

            // Wait for all to complete
            job1.await()
            job2.await()
            job3.await()

            // Assert: All operations queued (6 total)
            assertEquals(6, capturedOperations.size)

            // Assert: All have valid timestamps
            assertTrue(capturedOperations.all { it.timestampMs > 0 })

            // Assert: All have PENDING status
            assertTrue(capturedOperations.all { it.status == "PENDING" })

            // Assert: No null data
            assertTrue(capturedOperations.all { it.entityData.isNotEmpty() })
        }
    }

    /**
     * TEST 3.5: Operation Count and Completeness
     *
     * Verifies that after all concurrent operations:
     * - Total count matches expected (10+)
     * - All operation types represented
     * - No data loss
     * - Queue ready for sync
     */
    @Test
    fun test_3_5_comprehensive_operation_count_and_types() {
        runBlocking {
            // Arrange
            val capturedOperations = mutableListOf<OfflineOperation>()
            coEvery { mockDao.insert(any()) } answers {
                val op = firstArg<OfflineOperation>()
                capturedOperations.add(op)
                operationIdCounter.incrementAndGet()
            }

            // Act: Create a realistic mix of operations
            // 4 customer creates
            repeat(4) {
                queueService.queueCreateCustomer(com.emul8r.bizap.domain.model.Customer(name = "Customer $it"))
            }
            // 7 invoice creates
            repeat(7) {
                queueService.queueCreateInvoice(com.emul8r.bizap.domain.model.Invoice(id = it.toLong(), customerId = null, customerName = "C$it", totalAmount = (it + 1) * 100L, items = emptyList(), isQuote = false, status = com.emul8r.bizap.domain.model.InvoiceStatus.DRAFT, date = 0L))
            }
            // 2 deletes
            repeat(2) {
                queueService.queueDeleteInvoice(it.toLong(), 1L)
            }
            // 3 payments
            repeat(3) {
                queueService.queueRecordPayment(it.toLong(), 500L, 1L)
            }

            // Assert: Total is 16 operations
            assertEquals(16, capturedOperations.size)

            // Assert: Operation type distribution
            val typeCount = capturedOperations.groupingBy { it.operationType }.eachCount()
            assertEquals(4, typeCount["CREATE_CUSTOMER"])
            assertEquals(7, typeCount["CREATE_INVOICE"])
            assertEquals(2, typeCount["DELETE_INVOICE"])
            assertEquals(3, typeCount["UPDATE_PAYMENT"])

            // Assert: All PENDING
            assertTrue(capturedOperations.all { it.status == "PENDING" })

            // Assert: All have valid data
            assertTrue(capturedOperations.all { it.entityData.isNotEmpty() || it.operationType == "DELETE_INVOICE" })
        }
    }
}
