package com.emul8r.bizap.domain.usecase

import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.domain.model.OperationType
import com.emul8r.bizap.domain.model.PendingOperation
import com.emul8r.bizap.domain.model.PendingOperationStatus
import com.emul8r.bizap.domain.repository.OfflineQueueRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertTrue

class SyncPendingOperationsUseCaseTest : BaseUnitTest() {

    private val offlineQueueRepository: OfflineQueueRepository = mockk(relaxed = true)
    private lateinit var useCase: SyncPendingOperationsUseCase

    @Before
    fun setup() {
        useCase = SyncPendingOperationsUseCase(offlineQueueRepository)
    }

    @Test
    fun `invoke does nothing when queue is empty`() = runTest {
        every { offlineQueueRepository.getPendingOperations() } returns flowOf(emptyList())

        useCase()

        coVerify(exactly = 0) { offlineQueueRepository.markCompleted(any()) }
        coVerify(exactly = 0) { offlineQueueRepository.markFailed(any(), any()) }
    }

    @Test
    fun `invoke marks each pending operation as completed`() = runTest {
        val operations = listOf(
            buildPendingOperation(id = 1),
            buildPendingOperation(id = 2),
            buildPendingOperation(id = 3)
        )
        every { offlineQueueRepository.getPendingOperations() } returns flowOf(operations)
        coEvery { offlineQueueRepository.markCompleted(any()) } returns Unit

        useCase()

        coVerify(exactly = 1) { offlineQueueRepository.markCompleted(1L) }
        coVerify(exactly = 1) { offlineQueueRepository.markCompleted(2L) }
        coVerify(exactly = 1) { offlineQueueRepository.markCompleted(3L) }
    }

    @Test
    fun `invoke calls clearCompleted after processing`() = runTest {
        val operations = listOf(buildPendingOperation(id = 1))
        every { offlineQueueRepository.getPendingOperations() } returns flowOf(operations)

        useCase()

        coVerify(exactly = 1) { offlineQueueRepository.clearCompleted() }
    }

    @Test
    fun `invoke marks operation as failed when exception is thrown`() = runTest {
        val operations = listOf(buildPendingOperation(id = 10))
        every { offlineQueueRepository.getPendingOperations() } returns flowOf(operations)
        coEvery { offlineQueueRepository.markCompleted(10L) } throws RuntimeException("Boom")

        useCase()

        coVerify(exactly = 1) { offlineQueueRepository.markFailed(10L, "Boom") }
    }

    @Test
    fun `clearCompleted is not called when queue is empty`() = runTest {
        every { offlineQueueRepository.getPendingOperations() } returns flowOf(emptyList())

        useCase()

        coVerify(exactly = 0) { offlineQueueRepository.clearCompleted() }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun buildPendingOperation(
        id: Long = 1,
        operationType: OperationType = OperationType.CREATE,
        entityType: String = "INVOICE",
        entityId: Long = 42L
    ) = PendingOperation(
        id = id,
        operationType = operationType,
        entityType = entityType,
        entityId = entityId,
        payload = "{}",
        status = PendingOperationStatus.PENDING
    )
}
