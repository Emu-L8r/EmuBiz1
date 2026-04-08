@file:Suppress("UNCHECKED_CAST")
package com.emul8r.bizap.domain.usecase

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.emul8r.bizap.domain.repository.OfflineQueueRepository
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.InvoiceStatus
import com.emul8r.bizap.domain.repository.InvoiceRepository
import com.emul8r.bizap.utils.ConnectivityHelper
import com.emul8r.bizap.test.WindowsTestRule
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertTrue
import kotlin.test.assertEquals

/**
 * SPRINT 3: Simplified to match new SaveInvoiceUseCase architecture
 */
@RunWith(AndroidJUnit4::class)
class SaveInvoiceUseCaseOfflineTest {

    @get:Rule
    val skipWindowsRule = WindowsTestRule()

    private lateinit var context: Context
    private lateinit var mockRepository: InvoiceRepository
    private lateinit var mockOfflineQueue: OfflineQueueRepository
    private lateinit var useCase: SaveInvoiceUseCase

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        mockRepository = mockk()
        mockOfflineQueue = mockk(relaxed = true)

        useCase = SaveInvoiceUseCase(
            mockRepository,
            mockOfflineQueue,
            context
        )
    }

    @Test
    fun testSaveInvoiceOnline() = runBlocking {
        // GIVEN: Online state
        mockkObject(ConnectivityHelper)
        every { ConnectivityHelper.isNetworkAvailable(any()) } returns true

        coEvery { mockRepository.saveInvoice(any()) } returns Result.success(1L)

        val invoice = createTestInvoice()

        // WHEN
        val result = useCase(invoice)

        // THEN
        assertTrue(result.isSuccess)
        assertEquals(1L, result.getOrNull())
        coVerify { mockRepository.saveInvoice(any()) }

        unmockkObject(ConnectivityHelper)
    }

    @Test
    fun testSaveInvoiceOffline() = runBlocking {
        // GIVEN: Offline state
        mockkObject(ConnectivityHelper)
        every { ConnectivityHelper.isNetworkAvailable(any()) } returns false

        coEvery { mockOfflineQueue.enqueue(any()) } just Runs

        val invoice = createTestInvoice()

        // WHEN
        val result = useCase(invoice)

        // THEN
        assertTrue(result.isSuccess)
        assertEquals(-1L, result.getOrNull())  // Placeholder for queued operation
        coVerify(exactly = 0) { mockRepository.saveInvoice(any()) }
        coVerify { mockOfflineQueue.enqueue(any()) }

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
