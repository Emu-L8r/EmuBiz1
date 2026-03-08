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

class SyncPendingOperationsUseCaseTest : BaseUnitTest() {

    private val offlineQueueRepository: OfflineQueueRepository = mockk(relaxed = true)
    private val dispatcher: SyncOperationDispatcher = mockk(relaxed = true)
    private lateinit var useCase: SyncPendingOperationsUseCase

    @Before
    fun setup() {
        useCase = SyncPendingOperationsUseCase(offlineQueueRepository, dispatcher)
    }

    @Test
    fun `invoke does nothing when queue is empty`() = runTest {
        every { offlineQueueRepository.getPendingOperations() } returns flowOf(emptyList())

        useCase()

        coVerify(exactly = 0) { dispatcher.dispatch(any()) }
        coVerify(exactly = 0) { offlineQueueRepository.markCompleted(any()) }
    }

    @Test
    fun `invoke dispatches each pending operation`() = runTest {
        val operations = listOf(
            buildPendingOperation(id = 1),
            buildPendingOperation(id = 2)
        )
        every { offlineQueueRepository.getPendingOperations() } returns flowOf(operations)

        useCase()

        coVerify(exactly = 1) { dispatcher.dispatch(operations[0]) }
        coVerify(exactly = 1) { dispatcher.dispatch(operations[1]) }
        coVerify(exactly = 1) { offlineQueueRepository.markCompleted(1L) }
        coVerify(exactly = 1) { offlineQueueRepository.markCompleted(2L) }
    }

    @Test
    fun `invoke marks operation as failed when dispatcher throws NonRetryable`() = runTest {
        val operation = buildPendingOperation(id = 10)
        every { offlineQueueRepository.getPendingOperations() } returns flowOf(listOf(operation))
        coEvery { dispatcher.dispatch(any()) } throws SyncOperationDispatcher.SyncException.NonRetryable("Fatal")

        useCase()

        coVerify(exactly = 1) { offlineQueueRepository.markFailed(10L, "Fatal") }
        coVerify(exactly = 0) { offlineQueueRepository.markCompleted(10L) }
    }

    @Test
    fun `invoke throws when dispatcher throws Retryable`() = runTest {
        val operation = buildPendingOperation(id = 10)
        every { offlineQueueRepository.getPendingOperations() } returns flowOf(listOf(operation))
        coEvery { dispatcher.dispatch(any()) } throws SyncOperationDispatcher.SyncException.Retryable("Transient")

        try {
            useCase()
        } catch (e: SyncOperationDispatcher.SyncException.Retryable) {
            // Expected
        }

        coVerify(exactly = 0) { offlineQueueRepository.markCompleted(10L) }
        coVerify(exactly = 0) { offlineQueueRepository.markFailed(10L, any()) }
    }

    @Test
    fun `invoke calls clearCompleted after processing`() = runTest {
        val operations = listOf(buildPendingOperation(id = 1))
        every { offlineQueueRepository.getPendingOperations() } returns flowOf(operations)

        useCase()

        coVerify(exactly = 1) { offlineQueueRepository.clearCompleted() }
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
