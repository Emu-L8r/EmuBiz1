@file:Suppress("UNCHECKED_CAST")
package com.emul8r.bizap.integration

import android.content.Context
import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.domain.repository.OfflineQueueRepository
import com.emul8r.bizap.domain.repository.InvoiceRepository
import com.emul8r.bizap.domain.usecase.SaveInvoiceUseCase
import com.emul8r.bizap.util.TestDataFactory
import com.emul8r.bizap.utils.ConnectivityHelper
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Integration tests for offline sync flow.
 *
 * Verifies that:
 * 1. When offline, invoice creation is queued (not lost)
 * 2. When online, invoice is saved directly to the database
 * 3. Offline → online transition queues data correctly
 *
 * SPRINT 3: Simplified - removed SnapshotSyncRepository dependency
 */
class OfflineSyncFlowTest : BaseUnitTest() {

    private val repository: InvoiceRepository = mockk()
    private val offlineQueueRepository: OfflineQueueRepository = mockk()
    private val context: Context = mockk()
    private lateinit var saveInvoiceUseCase: SaveInvoiceUseCase

    @Before
    fun setup() {
        mockkObject(ConnectivityHelper)
        saveInvoiceUseCase = SaveInvoiceUseCase(
            repository = repository,
            offlineQueueRepository = offlineQueueRepository,
            context = context
        )
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    // ── offline mode ─────────────────────────────────────────────────────────

    @Test
    fun `offlineMode_InvoiceQueued - invoice is queued when network unavailable`() = runTest {
        every { ConnectivityHelper.isNetworkAvailable(context) } returns false
        val invoice = TestDataFactory.createTestInvoice().copy(
            items = listOf(mockk(relaxed = true))
        )
        coEvery { offlineQueueRepository.enqueue(any()) } just Runs

        val result = saveInvoiceUseCase(invoice)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { offlineQueueRepository.enqueue(any()) }
        coVerify(exactly = 0) { repository.saveInvoice(any()) }
    }

    @Test
    fun `offlineMode_InvoiceQueued - repository is NOT called when offline`() = runTest {
        every { ConnectivityHelper.isNetworkAvailable(context) } returns false
        val invoice = TestDataFactory.createTestInvoice().copy(
            items = listOf(mockk(relaxed = true))
        )
        coEvery { offlineQueueRepository.enqueue(any()) } just Runs

        saveInvoiceUseCase(invoice)

        coVerify(exactly = 0) { repository.saveInvoice(any()) }
    }

    // ── online mode ──────────────────────────────────────────────────────────

    @Test
    fun `onlineMode_InvoiceSaved - invoice saved directly when network available`() = runTest {
        every { ConnectivityHelper.isNetworkAvailable(context) } returns true
        val invoice = TestDataFactory.createTestInvoice().copy(
            items = listOf(mockk(relaxed = true))
        )
        coEvery { repository.saveInvoice(invoice) } returns Result.success(1L)

        val result = saveInvoiceUseCase(invoice)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { repository.saveInvoice(invoice) }
    }

    @Test
    fun `onlineMode_QueueNotUsed - offline queue is not called when network available`() = runTest {
        every { ConnectivityHelper.isNetworkAvailable(context) } returns true
        val invoice = TestDataFactory.createTestInvoice().copy(
            items = listOf(mockk(relaxed = true))
        )
        coEvery { repository.saveInvoice(invoice) } returns Result.success(1L)

        saveInvoiceUseCase(invoice)

        coVerify(exactly = 0) { offlineQueueRepository.enqueue(any()) }
    }

    // ── validation ───────────────────────────────────────────────────────────

    @Test
    fun `validation_EmptyItems - invoice with no items is rejected before network check`() = runTest {
        val invoice = TestDataFactory.createTestInvoice().copy(items = emptyList())

        val result = saveInvoiceUseCase(invoice)

        assertFalse(result.isSuccess)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        // Network should not even be checked
        verify(exactly = 0) { ConnectivityHelper.isNetworkAvailable(any()) }
    }

    @Test
    fun `validation_BlankCustomerName - invoice with blank customer name is rejected`() = runTest {
        val invoice = TestDataFactory.createTestInvoice().copy(
            customerName = "   ",
            items = listOf(mockk(relaxed = true))
        )

        val result = saveInvoiceUseCase(invoice)

        assertFalse(result.isSuccess)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }
}



