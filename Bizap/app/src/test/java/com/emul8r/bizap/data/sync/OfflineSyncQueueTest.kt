package com.emul8r.bizap.data.sync

import com.emul8r.bizap.data.local.PendingOperationDao
import com.emul8r.bizap.data.repository.OfflineSyncQueue
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

/**
 * Unit tests for OfflineSyncQueue.
 * Verifies that operations are correctly queued and pending count is observed.
 */
class OfflineSyncQueueTest {

    private val pendingOperationDao: PendingOperationDao = mockk()
    private val json = Json { ignoreUnknownKeys = true }
    private lateinit var syncQueue: OfflineSyncQueue

    @Before
    fun setup() {
        syncQueue = OfflineSyncQueue(pendingOperationDao, json)
    }

    @Test
    fun `test queueOperation inserts operation into DAO`() = runTest {
        // Arrange
        coEvery { pendingOperationDao.insertOperation(any()) } returns 1L
        
        // Act
        val id = syncQueue.queueOperation(
            operationType = "CREATE",
            entityType = "INVOICE",
            businessProfileId = 1L,
            payload = "{}",
            entityId = 100L
        )

        // Assert
        coVerify { pendingOperationDao.insertOperation(match { 
            it.operationType == "CREATE" && 
            it.entityType == "INVOICE" && 
            it.businessProfileId == 1L &&
            it.entityId == 100L
        }) }
        assert(id.isNotEmpty())
    }

    @Test
    fun `test pendingCount observes DAO flow`() = runTest {
        // Arrange
        coEvery { pendingOperationDao.observePendingCount() } returns flowOf(5)

        // Act & Assert
        syncQueue.pendingCount.collect { count ->
            assertEquals(5, count)
        }
    }

    @Test
    fun `test markSynced updates status in DAO`() = runTest {
        // Arrange
        coEvery { pendingOperationDao.updateStatus(any(), any()) } returns Unit

        // Act
        syncQueue.markSynced("test-id")

        // Assert
        coVerify { pendingOperationDao.updateStatus("test-id", "SYNCED") }
    }

    @Test
    fun `test markFailed updates status in DAO`() = runTest {
        // Arrange
        coEvery { pendingOperationDao.updateStatus(any(), any()) } returns Unit

        // Act
        syncQueue.markFailed("test-id", "Network Error")

        // Assert
        coVerify { pendingOperationDao.updateStatus("test-id", "FAILED") }
    }
}
