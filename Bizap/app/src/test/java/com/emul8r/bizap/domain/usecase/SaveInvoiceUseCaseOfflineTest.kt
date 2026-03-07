package com.emul8r.bizap.domain.usecase

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.emul8r.bizap.data.local.offline.OfflineQueueService
import com.emul8r.bizap.data.repository.SnapshotSyncHelper
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.InvoiceStatus
import com.emul8r.bizap.domain.repository.InvoiceRepository
import com.emul8r.bizap.utils.ConnectivityHelper
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertTrue
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class SaveInvoiceUseCaseOfflineTest {
    
    private lateinit var context: Context
    private lateinit var mockRepository: InvoiceRepository
    private lateinit var mockSnapshotHelper: SnapshotSyncHelper
    private lateinit var mockQueueService: OfflineQueueService
    private lateinit var useCase: SaveInvoiceUseCase
    
    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        mockRepository = mockk()
        mockSnapshotHelper = mockk(relaxed = true)
        mockQueueService = mockk()
        
        useCase = SaveInvoiceUseCase(
            mockRepository,
            mockSnapshotHelper,
            mockQueueService,
            context
        )
    }
    
    @Test
    fun testSaveInvoiceOnline() = runBlocking {
        // GIVEN: Online state
        mockkObject(ConnectivityHelper)
        every { ConnectivityHelper.isNetworkAvailable(any()) } returns true

        coEvery { mockRepository.saveInvoice(any()) } returns Result.success(1L)
        coEvery { mockSnapshotHelper.syncAllSnapshots(any(), any()) } just Runs

        val invoice = createTestInvoice()

        // WHEN
        val result = useCase(invoice)
        
        // THEN
        assertTrue(result.isSuccess)
        assertEquals(1L, result.getOrNull())
        coVerify { mockRepository.saveInvoice(any()) }
        coVerify { mockSnapshotHelper.syncAllSnapshots(any(), any()) }

        unmockkObject(ConnectivityHelper)
    }
    
    @Test
    fun testSaveInvoiceOffline() = runBlocking {
        // GIVEN: Offline state
        mockkObject(ConnectivityHelper)
        every { ConnectivityHelper.isNetworkAvailable(any()) } returns false

        coEvery { mockQueueService.queueCreateInvoice(any()) } returns 100L

        val invoice = createTestInvoice()

        // WHEN
        val result = useCase(invoice)
        
        // THEN
        assertTrue(result.isSuccess)
        assertEquals(100L, result.getOrNull())
        coVerify(exactly = 0) { mockRepository.saveInvoice(any()) }
        coVerify { mockQueueService.queueCreateInvoice(any()) }

        unmockkObject(ConnectivityHelper)
    }

    private fun createTestInvoice(): Invoice {
        val now = System.currentTimeMillis()
        return Invoice(
            id = 0L,
            businessProfileId = 1L,
            customerId = 1L,
            customerName = "Test Customer",
            customerAddress = "123 Test St",
            customerEmail = "test@example.com",
            items = listOf(mockk(relaxed = true)),
            totalAmount = 10000L,
            amountPaid = 0L,
            status = InvoiceStatus.DRAFT,
            date = now,
            dueDate = now + 86400000L,
            isQuote = false,
            currencyCode = "AUD",
            taxRate = 10.0,
            taxAmount = 1000L,
            invoiceYear = 2026,
            invoiceSequence = 1,
            updatedAt = now
        )
    }
}
