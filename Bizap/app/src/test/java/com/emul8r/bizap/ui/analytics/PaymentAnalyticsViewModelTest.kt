package com.emul8r.bizap.ui.analytics

import androidx.lifecycle.SavedStateHandle
import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.InvoiceStatus
import com.emul8r.bizap.domain.repository.InvoiceRepository
import io.mockk.mockk
import io.mockk.every
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Unit tests for PaymentAnalyticsViewModel
 *
 * Verifies payment metrics calculation:
 * - Collection rate (paid/total invoices)
 * - Days Sales Outstanding (DSO)
 * - Payment status breakdown
 */
class PaymentAnalyticsViewModelTest : BaseUnitTest() {

    private val invoiceRepository: InvoiceRepository = mockk()
    private val savedStateHandle: SavedStateHandle = SavedStateHandle().apply {
        set("businessId", 1L)
    }

    private lateinit var viewModel: PaymentAnalyticsViewModel

    @Before
    fun setUp() {
        viewModel = PaymentAnalyticsViewModel(invoiceRepository, savedStateHandle)
    }

    // ── Initial State ──────────────────────────────────────────────────────────

    @Test
    fun `initial state is Loading`() {
        every { invoiceRepository.getAllInvoicesWithItems() } returns flowOf(emptyList())
        viewModel = PaymentAnalyticsViewModel(invoiceRepository, savedStateHandle)

        assertIs<PaymentMetricsState.Loading>(viewModel.paymentMetrics.value)
    }

    // ── Collection Rate Calculation ────────────────────────────────────────────

    @Test
    fun `collection rate is 0 percent for no invoices`() = runUnitTest {
        every { invoiceRepository.getAllInvoicesWithItems() } returns flowOf(emptyList())
        viewModel = PaymentAnalyticsViewModel(invoiceRepository, savedStateHandle)
        advanceUntilIdle()

        val state = viewModel.paymentMetrics.value
        assertIs<PaymentMetricsState.Success>(state)
        assertEquals(0f, state.metrics.collectionRate)
    }

    @Test
    fun `collection rate is 100 percent when all invoices paid`() = runUnitTest {
        val paidInvoices = listOf(
            testInvoice(id = 1L, status = InvoiceStatus.PAID),
            testInvoice(id = 2L, status = InvoiceStatus.PAID),
            testInvoice(id = 3L, status = InvoiceStatus.PAID)
        )
        every { invoiceRepository.getAllInvoicesWithItems() } returns flowOf(paidInvoices)

        viewModel = PaymentAnalyticsViewModel(invoiceRepository, savedStateHandle)
        advanceUntilIdle()

        val state = viewModel.paymentMetrics.value
        assertIs<PaymentMetricsState.Success>(state)
        assertEquals(100f, state.metrics.collectionRate)
    }

    @Test
    fun `collection rate is 50 percent when half paid`() = runUnitTest {
        val invoices = listOf(
            testInvoice(id = 1L, status = InvoiceStatus.PAID),
            testInvoice(id = 2L, status = InvoiceStatus.SENT),
            testInvoice(id = 3L, status = InvoiceStatus.PAID),
            testInvoice(id = 4L, status = InvoiceStatus.OVERDUE)
        )
        every { invoiceRepository.getAllInvoicesWithItems() } returns flowOf(invoices)

        viewModel = PaymentAnalyticsViewModel(invoiceRepository, savedStateHandle)
        advanceUntilIdle()

        val state = viewModel.paymentMetrics.value
        assertIs<PaymentMetricsState.Success>(state)
        assertEquals(50f, state.metrics.collectionRate)
    }

    // ── Invoice Status Breakdown ───────────────────────────────────────────────

    @Test
    fun `payment status breakdown counts all statuses correctly`() = runUnitTest {
        val invoices = listOf(
            testInvoice(id = 1L, status = InvoiceStatus.PAID),
            testInvoice(id = 2L, status = InvoiceStatus.PAID),
            testInvoice(id = 3L, status = InvoiceStatus.SENT),
            testInvoice(id = 4L, status = InvoiceStatus.OVERDUE),
            testInvoice(id = 5L, status = InvoiceStatus.DRAFT)
        )
        every { invoiceRepository.getAllInvoicesWithItems() } returns flowOf(invoices)

        viewModel = PaymentAnalyticsViewModel(invoiceRepository, savedStateHandle)
        advanceUntilIdle()

        val state = viewModel.paymentMetrics.value
        assertIs<PaymentMetricsState.Success>(state)
        assertEquals(2, state.metrics.paymentStatusBreakdown["Paid"])
        assertEquals(1, state.metrics.paymentStatusBreakdown["Pending"])
        assertEquals(1, state.metrics.paymentStatusBreakdown["Overdue"])
        assertEquals(1, state.metrics.paymentStatusBreakdown["Draft"])
    }

    // ── Days Sales Outstanding (DSO) ───────────────────────────────────────────

    @Test
    fun `DSO is 0 when all invoices are paid`() = runUnitTest {
        val paidInvoices = listOf(
            testInvoice(id = 1L, status = InvoiceStatus.PAID),
            testInvoice(id = 2L, status = InvoiceStatus.PAID)
        )
        every { invoiceRepository.getAllInvoicesWithItems() } returns flowOf(paidInvoices)

        viewModel = PaymentAnalyticsViewModel(invoiceRepository, savedStateHandle)
        advanceUntilIdle()

        val state = viewModel.paymentMetrics.value
        assertIs<PaymentMetricsState.Success>(state)
        assertEquals(0, state.metrics.daysOutstanding)
    }

    @Test
    fun `DSO includes outstanding invoices`() = runUnitTest {
        // Create invoices with varied ages
        val now = System.currentTimeMillis()
        val invoices = listOf(
            testInvoice(id = 1L, status = InvoiceStatus.PAID),
            testInvoice(
                id = 2L,
                status = InvoiceStatus.SENT,
                dateCreated = java.time.Instant.ofEpochMilli(now - 10 * 24 * 60 * 60 * 1000).toString()
            ),
            testInvoice(
                id = 3L,
                status = InvoiceStatus.OVERDUE,
                dateCreated = java.time.Instant.ofEpochMilli(now - 20 * 24 * 60 * 60 * 1000).toString()
            )
        )
        every { invoiceRepository.getAllInvoicesWithItems() } returns flowOf(invoices)

        viewModel = PaymentAnalyticsViewModel(invoiceRepository, savedStateHandle)
        advanceUntilIdle()

        val state = viewModel.paymentMetrics.value
        assertIs<PaymentMetricsState.Success>(state)
        // DSO should be average of 10 and 20 = 15 days
        assertEquals(15, state.metrics.daysOutstanding)
    }

    // ── Invoice Counts ────────────────────────────────────────────────────────

    @Test
    fun `total invoices count is accurate`() = runUnitTest {
        val invoices = listOf(
            testInvoice(id = 1L, status = InvoiceStatus.PAID),
            testInvoice(id = 2L, status = InvoiceStatus.SENT),
            testInvoice(id = 3L, status = InvoiceStatus.OVERDUE)
        )
        every { invoiceRepository.getAllInvoicesWithItems() } returns flowOf(invoices)

        viewModel = PaymentAnalyticsViewModel(invoiceRepository, savedStateHandle)
        advanceUntilIdle()

        val state = viewModel.paymentMetrics.value
        assertIs<PaymentMetricsState.Success>(state)
        assertEquals(3, state.metrics.totalInvoices)
    }

    // ── Error Handling ─────────────────────────────────────────────────────────

    @Test
    fun `error state is set on repository failure`() = runUnitTest {
        val exception = Exception("Database error")
        every { invoiceRepository.getAllInvoicesWithItems() } throws exception

        viewModel = PaymentAnalyticsViewModel(invoiceRepository, savedStateHandle)
        advanceUntilIdle()

        val state = viewModel.paymentMetrics.value
        assertIs<PaymentMetricsState.Error>(state)
        assertEquals("Database error", state.message)
    }

    @Test
    fun `error state uses default message if exception message is null`() = runUnitTest {
        val exception = Exception(null as String?)
        every { invoiceRepository.getAllInvoicesWithItems() } throws exception

        viewModel = PaymentAnalyticsViewModel(invoiceRepository, savedStateHandle)
        advanceUntilIdle()

        val state = viewModel.paymentMetrics.value
        assertIs<PaymentMetricsState.Error>(state)
        assertEquals("Unknown error", state.message)
    }

    // ── Helper Functions ───────────────────────────────────────────────────────

    private fun testInvoice(
        id: Long,
        status: InvoiceStatus,
        dateCreated: String = java.time.Instant.now().toString()
    ) = Invoice(
        id = id,
        businessId = 1L,
        customerId = 1L,
        customerName = "Test Customer",
        amount = 1000L,
        status = status.name,
        dateCreated = dateCreated
    )
}

