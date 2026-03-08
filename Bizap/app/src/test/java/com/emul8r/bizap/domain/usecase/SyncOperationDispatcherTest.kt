package com.emul8r.bizap.domain.usecase

import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.domain.model.Customer
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.OperationType
import com.emul8r.bizap.domain.model.PendingOperation
import com.emul8r.bizap.domain.model.PendingOperationStatus
import com.emul8r.bizap.domain.repository.CustomerRepository
import com.emul8r.bizap.domain.repository.InvoiceRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Before
import org.junit.Test
import kotlin.test.assertIs

class SyncOperationDispatcherTest : BaseUnitTest() {

    private val invoiceRepository: InvoiceRepository = mockk(relaxed = true)
    private val customerRepository: CustomerRepository = mockk(relaxed = true)
    private val json = Json { ignoreUnknownKeys = true }
    private lateinit var dispatcher: SyncOperationDispatcher

    @Before
    fun setup() {
        dispatcher = SyncOperationDispatcher(invoiceRepository, customerRepository, json)
    }

    @Test
    fun `dispatch CREATE INVOICE calls createInvoiceRemote and saveInvoice`() = runTest {
        val invoice = mockk<Invoice>(relaxed = true)
        val operation = buildOperation(
            entityType = "INVOICE",
            operationType = OperationType.CREATE,
            payload = json.encodeToString(invoice)
        )
        coEvery { invoiceRepository.createInvoiceRemote(any()) } returns Result.success(invoice)

        dispatcher.dispatch(operation)

        coVerify { invoiceRepository.createInvoiceRemote(any()) }
        coVerify { invoiceRepository.saveInvoice(any()) }
    }

    @Test
    fun `dispatch UPDATE INVOICE with conflict performs Server Wins resolution`() = runTest {
        val invoice = mockk<Invoice>(relaxed = true) { every { id } returns 101L }
        val operation = buildOperation(
            entityType = "INVOICE",
            operationType = OperationType.UPDATE,
            payload = json.encodeToString(invoice)
        )
        coEvery { invoiceRepository.updateInvoiceRemote(any()) } returns Result.failure(Exception("409 Conflict"))
        coEvery { invoiceRepository.getInvoiceRemote(101L) } returns Result.success(invoice)

        dispatcher.dispatch(operation)

        coVerify { invoiceRepository.updateInvoiceRemote(any()) }
        coVerify { invoiceRepository.getInvoiceRemote(101L) }
        coVerify { invoiceRepository.saveInvoice(any()) }
    }

    @Test
    fun `dispatch unknown entity type throws NonRetryable`() = runTest {
        val operation = buildOperation(entityType = "UNKNOWN")

        val result = runCatching { dispatcher.dispatch(operation) }

        assertIs<SyncOperationDispatcher.SyncException.NonRetryable>(result.exceptionOrNull())
    }

    @Test
    fun `dispatch 500 error throws Retryable`() = runTest {
        val invoice = mockk<Invoice>(relaxed = true)
        val operation = buildOperation(
            entityType = "INVOICE",
            operationType = OperationType.CREATE,
            payload = json.encodeToString(invoice)
        )
        coEvery { invoiceRepository.createInvoiceRemote(any()) } returns Result.failure(Exception("500 Internal Server Error"))

        val result = runCatching { dispatcher.dispatch(operation) }

        assertIs<SyncOperationDispatcher.SyncException.Retryable>(result.exceptionOrNull())
    }

    // ── Stress Test Simulation ────────────────────────────────────────────────

    @Test
    fun `dispatch large volume of operations sequentially`() = runTest {
        val operations = List(100) { i ->
            buildOperation(id = i.toLong(), entityId = i.toLong())
        }
        
        operations.forEach { 
            val invoice = mockk<Invoice>(relaxed = true)
            coEvery { invoiceRepository.createInvoiceRemote(any()) } returns Result.success(invoice)
            dispatcher.dispatch(it) 
        }

        coVerify(exactly = 100) { invoiceRepository.createInvoiceRemote(any()) }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun buildOperation(
        id: Long = 1,
        entityType: String = "INVOICE",
        operationType: OperationType = OperationType.CREATE,
        entityId: Long = 42L,
        payload: String = "{}"
    ) = PendingOperation(
        id = id,
        operationType = operationType,
        entityType = entityType,
        entityId = entityId,
        payload = payload,
        status = PendingOperationStatus.PENDING
    )
}
