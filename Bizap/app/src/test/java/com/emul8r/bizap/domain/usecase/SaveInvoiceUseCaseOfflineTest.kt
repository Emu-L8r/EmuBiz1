package com.emul8r.bizap.domain.usecase

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.test.core.app.ApplicationProvider
import com.emul8r.bizap.data.local.entities.InvoiceEntity
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
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowConnectivityManager
import org.robolectric.shadows.ShadowNetworkCapabilities
import kotlin.test.assertTrue
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
class SaveInvoiceUseCaseOfflineTest {
    
    private lateinit var context: Context
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var shadowConnectivityManager: ShadowConnectivityManager
    private lateinit var mockRepository: InvoiceRepository
    private lateinit var mockSnapshotHelper: SnapshotSyncHelper
    private lateinit var mockQueueService: OfflineQueueService
    private lateinit var useCase: SaveInvoiceUseCase
    
    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        shadowConnectivityManager = shadowOf(connectivityManager)
        
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
        val network = shadowConnectivityManager.activeNetwork
        val capabilities = shadowNetworkCapabilities(network)
        capabilities.addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        
        val invoice = createTestInvoice()
        coEvery { mockRepository.saveInvoice(any()) } returns Result.success(1L)
        
        // WHEN
        val result = useCase(invoice)
        
        // THEN
        assertTrue(result.isSuccess)
        assertEquals(1L, result.getOrNull())
        coVerify { mockRepository.saveInvoice(any()) }
        coVerify { mockSnapshotHelper.syncAllSnapshots(any(), any()) }
    }
    
    @Test
    fun testSaveInvoiceOffline() = runBlocking {
        // GIVEN: Offline state (no active network)
        shadowConnectivityManager.setDefaultNetworkActive(false)
        
        val invoice = createTestInvoice()
        coEvery { mockQueueService.queueCreateInvoice(any()) } returns 100L // Operation ID
        
        // WHEN
        val result = useCase(invoice)
        
        // THEN
        assertTrue(result.isSuccess)
        assertEquals(100L, result.getOrNull())
        coVerify(exactly = 0) { mockRepository.saveInvoice(any()) }
        coVerify { mockQueueService.queueCreateInvoice(any()) }
    }
    
    private fun shadowNetworkCapabilities(network: android.net.Network?): ShadowNetworkCapabilities {
        return shadowOf(connectivityManager.getNetworkCapabilities(network))
    }

    private fun createTestInvoice(): Invoice {
        return Invoice(
            id = 0L,
            businessProfileId = 1L,
            customerId = 1L,
            customerName = "Test Customer",
            totalAmount = 10000,
            amountPaid = 0,
            status = InvoiceStatus.DRAFT,
            items = listOf(mockk(relaxed = true))
        )
    }
}
