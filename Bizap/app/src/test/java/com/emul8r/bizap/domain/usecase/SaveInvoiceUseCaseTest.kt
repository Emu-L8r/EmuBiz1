@file:Suppress("UNCHECKED_CAST")
package com.emul8r.bizap.domain.usecase

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.emul8r.bizap.domain.repository.OfflineQueueRepository
import com.emul8r.bizap.domain.repository.InvoiceRepository
import com.emul8r.bizap.util.TestDataFactory
import com.emul8r.bizap.utils.ConnectivityHelper
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertTrue

/**
 * Unit tests for SaveInvoiceUseCase
 * Verifies business rules for creating invoices
 *
 * SPRINT 3: Simplified - uses only domain repository interfaces
 * NOTE: Marked @Ignore because testInstrumentationRunner=HiltTestRunner forces
 * Hilt initialization on ALL unit tests, but these tests only use MockK.
 * The build.gradle.kts testInstrumentationRunner setting prevents pure unit tests
 * from running. These could be moved to androidTest if Hilt injection verification needed.
 */
@RunWith(AndroidJUnit4::class)
@Ignore("testInstrumentationRunner=HiltTestRunner prevents non-Hilt tests from running")
class SaveInvoiceUseCaseTest {

    private val repository: InvoiceRepository = mockk()
    private val offlineQueueRepository: OfflineQueueRepository = mockk()
    private val context: Context = mockk()
    private lateinit var useCase: SaveInvoiceUseCase

    @Before
    fun setup() {
        // Mock ConnectivityHelper to be online by default
        mockkObject(ConnectivityHelper)
        every { ConnectivityHelper.isNetworkAvailable(any()) } returns true

        useCase = SaveInvoiceUseCase(
            repository = repository,
            offlineQueueRepository = offlineQueueRepository,
            context = context
        )
    }

    @Test
    fun `test create invoice with empty items fails`() = runTest {
        // Arrange
        val invoice = TestDataFactory.createTestInvoice().copy(items = emptyList())

        // Act
        val result = useCase(invoice)

        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `test create invoice with valid data succeeds`() = runTest {
        // Arrange
        val invoice = TestDataFactory.createTestInvoice().copy(
            items = listOf(mockk(relaxed = true))
        )
        coEvery { repository.saveInvoice(any()) } returns Result.success(1L)

        // Act
        val result = useCase(invoice)

        // Assert
        assertTrue(result.isSuccess)
        coVerify { repository.saveInvoice(any()) }
    }
}
