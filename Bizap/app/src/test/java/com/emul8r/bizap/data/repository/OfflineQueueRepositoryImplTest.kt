package com.emul8r.bizap.data.repository

import android.content.Context
import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.data.local.dao.PendingOperationDao
import com.emul8r.bizap.data.local.entities.PendingOperationEntity
import com.emul8r.bizap.domain.model.OperationType
import com.emul8r.bizap.domain.model.PendingOperation
import com.emul8r.bizap.domain.model.PendingOperationStatus
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class OfflineQueueRepositoryImplTest : BaseUnitTest() {

    private val dao: PendingOperationDao = mockk(relaxed = true)
    private val context: Context = mockk(relaxed = true)
    private lateinit var repository: OfflineQueueRepositoryImpl

    @Before
    fun setup() {
        repository = OfflineQueueRepositoryImpl(dao, context)
    }

    @Test
    fun `enqueue inserts operation into dao`() = runTest {
        val operation = buildPendingOperation()
        coEvery { dao.insert(any()) } returns 1L

        repository.enqueue(operation)

        coVerify(exactly = 1) { dao.insert(any()) }
    }

    @Test
    fun `getPendingOperations maps entities to domain models`() = runTest {
        val entity = buildPendingEntity(id = 5)
        every { dao.observePending() } returns flowOf(listOf(entity))

        val result = repository.getPendingOperations().first()

        assertEquals(1, result.size)
        assertEquals(5L, result[0].id)
        assertEquals(OperationType.CREATE, result[0].operationType)
        assertEquals("INVOICE", result[0].entityType)
        assertEquals(PendingOperationStatus.PENDING, result[0].status)
    }

    @Test
    fun `getPendingCount delegates to dao`() = runTest {
        every { dao.observePendingCount() } returns flowOf(3)

        val count = repository.getPendingCount().first()

        assertEquals(3, count)
    }

    @Test
    fun `markCompleted calls dao markCompleted`() = runTest {
        repository.markCompleted(7L)
        coVerify(exactly = 1) { dao.markCompleted(7L) }
    }

    @Test
    fun `markFailed calls dao markFailed with error message`() = runTest {
        repository.markFailed(8L, "Connection timeout")
        coVerify(exactly = 1) { dao.markFailed(8L, any(), "Connection timeout") }
    }

    @Test
    fun `clearCompleted calls dao deleteCompleted`() = runTest {
        repository.clearCompleted()
        coVerify(exactly = 1) { dao.deleteCompleted() }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun buildPendingOperation(id: Long = 1) = PendingOperation(
        id = id,
        operationType = OperationType.CREATE,
        entityType = "INVOICE",
        entityId = 42L,
        payload = "{}",
        status = PendingOperationStatus.PENDING
    )

    private fun buildPendingEntity(id: Long = 1) = PendingOperationEntity(
        id = id,
        operationType = "CREATE",
        entityType = "INVOICE",
        entityId = 42L,
        payload = "{}",
        status = "PENDING"
    )
}
