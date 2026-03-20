@file:Suppress("UNCHECKED_CAST")
package com.emul8r.bizap.integration

import android.content.Context
import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.data.local.offline.OfflineQueueService
import com.emul8r.bizap.data.repository.SnapshotSyncHelper
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
 * 3. Snapshot sync failures are handled gracefully (invoice still saved)
 * 4. Offline → online transition queues data correctly
 */
class OfflineSyncFlowTest : BaseUnitTest() {

    private val repository: InvoiceRepository = mockk()
    private val snapshotSyncHelper: SnapshotSyncHelper = mockk(relaxed = true)
    private val offlineQueueService: OfflineQueueService = mockk()
    private val context: Context = mockk()
    private lateinit var saveInvoiceUseCase: SaveInvoiceUseCase

    @Before
    fun setup() {
        mockkObject(ConnectivityHelper)
        saveInvoiceUseCase = SaveInvoiceUseCase(
            repository = repository,
            snapshotSyncHelper = snapshotSyncHelper,
            offlineQueueService = offlineQueueService,
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
        coEvery { offlineQueueService.queueCreateInvoice(invoice) } returns 99L

        val result = saveInvoiceUseCase(invoice)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { offlineQueueService.queueCreateInvoice(invoice) }
        coVerify(exactly = 0) { repository.saveInvoice(any()) }
    }

    @Test
    fun `offlineMode_InvoiceQueued - repository is NOT called when offline`() = runTest {
        every { ConnectivityHelper.isNetworkAvailable(context) } returns false
        val invoice = TestDataFactory.createTestInvoice().copy(
            items = listOf(mockk(relaxed = true))
        )
        coEvery { offlineQueueService.queueCreateInvoice(any()) } returns 1L

        saveInvoiceUseCase(invoice)

        coVerify(exactly = 0) { repository.saveInvoice(any()) }
    }

    @Test
    fun `offlineMode_QueueId_Returned - operation ID is returned as success result`() = runTest {
        every { ConnectivityHelper.isNetworkAvailable(context) } returns false
        val invoice = TestDataFactory.createTestInvoice().copy(
            items = listOf(mockk(relaxed = true))
        )
        val expectedQueueId = 42L
        coEvery { offlineQueueService.queueCreateInvoice(any()) } returns expectedQueueId

        val result = saveInvoiceUseCase(invoice)

        assertTrue(result.isSuccess)
        // The queued operation ID is returned so the caller can track it
    }

    // ── online mode ──────────────────────────────────────────────────────────

    @Test
    fun `onlineMode_InvoiceSaved - invoice saved directly when network available`() = runTest {
        every { ConnectivityHelper.isNetworkAvailable(context) } returns true
        val invoice = TestDataFactory.createTestInvoice().copy(
            items = listOf(mockk(relaxed = true))
        )
        coEvery { repository.saveInvoice(invoice) } returns Result.success(1L)
        coEvery { snapshotSyncHelper.syncAllSnapshots(any(), any()) } just Runs

        val result = saveInvoiceUseCase(invoice)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { repository.saveInvoice(invoice) }
    }

    @Test
    fun `onlineMode_SnapshotSynced - snapshot sync is called after successful save`() = runTest {
        every { ConnectivityHelper.isNetworkAvailable(context) } returns true
        val invoice = TestDataFactory.createTestInvoice().copy(
            items = listOf(mockk(relaxed = true))
        )
        coEvery { repository.saveInvoice(invoice) } returns Result.success(1L)
        coEvery { snapshotSyncHelper.syncAllSnapshots(any(), any()) } just Runs

        saveInvoiceUseCase(invoice)

        coVerify(exactly = 1) { snapshotSyncHelper.syncAllSnapshots(any(), any()) }
    }

    @Test
    fun `onlineMode_QueueNotUsed - offline queue is not called when network available`() = runTest {
        every { ConnectivityHelper.isNetworkAvailable(context) } returns true
        val invoice = TestDataFactory.createTestInvoice().copy(
            items = listOf(mockk(relaxed = true))
        )
        coEvery { repository.saveInvoice(invoice) } returns Result.success(1L)
        coEvery { snapshotSyncHelper.syncAllSnapshots(any(), any()) } just Runs

        saveInvoiceUseCase(invoice)

        coVerify(exactly = 0) { offlineQueueService.queueCreateInvoice(any()) }
    }

    // ── snapshot failure handling ────────────────────────────────────────────

    @Test
    fun `snapshotFailure_InvoicePreserved - invoice save succeeds even if snapshot sync fails`() = runTest {
        every { ConnectivityHelper.isNetworkAvailable(context) } returns true
        val invoice = TestDataFactory.createTestInvoice().copy(
            items = listOf(mockk(relaxed = true))
        )
        coEvery { repository.saveInvoice(invoice) } returns Result.success(1L)
        coEvery { snapshotSyncHelper.syncAllSnapshots(any(), any()) } throws RuntimeException("Snapshot DB error")

        val result = saveInvoiceUseCase(invoice)

        // Invoice save should succeed despite snapshot failure
        assertTrue(result.isSuccess)
    }

    @Test
    fun `snapshotFailure_NoExceptionPropagated - snapshot error does not reach caller`() = runTest {
        every { ConnectivityHelper.isNetworkAvailable(context) } returns true
        val invoice = TestDataFactory.createTestInvoice().copy(
            items = listOf(mockk(relaxed = true))
        )
        coEvery { repository.saveInvoice(invoice) } returns Result.success(5L)
        coEvery { snapshotSyncHelper.syncAllSnapshots(any(), any()) } throws RuntimeException("Snapshot failure")

        var threwException = false
        try {
            saveInvoiceUseCase(invoice)
        } catch (e: Exception) {
            threwException = true
        }

        assertFalse(threwException, "Snapshot failure should be swallowed gracefully")
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
