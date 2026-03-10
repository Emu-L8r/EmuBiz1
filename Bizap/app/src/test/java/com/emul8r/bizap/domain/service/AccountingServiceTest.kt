package com.emul8r.bizap.domain.service

import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.data.local.dao.InvoiceDaoV2
import com.emul8r.bizap.data.local.dao.PaymentDaoV2
import com.emul8r.bizap.domain.model.InvoiceStatus
import io.mockk.any
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

/**
 * Unit tests for [AccountingService].
 *
 * Verifies that each method delegates to the correct [InvoiceDaoV2] query with the
 * correct status filters, and that derived calculations (collection rate) are correct.
 */
class AccountingServiceTest : BaseUnitTest() {

    private lateinit var invoiceDaoV2: InvoiceDaoV2
    private lateinit var paymentDaoV2: PaymentDaoV2
    private lateinit var accountingService: AccountingService

    private val businessId = 42L

    @Before
    fun setUp() {
        invoiceDaoV2 = mockk(relaxed = true)
        paymentDaoV2 = mockk(relaxed = true)
        accountingService = AccountingService(invoiceDaoV2, paymentDaoV2)
    }

    // ── observeOutstandingAmount ──────────────────────────────────────────────

    @Test
    fun `observeOutstandingAmount - delegates to dao with correct statuses`() = runTest {
        val expected = 150_00L
        every {
            invoiceDaoV2.observeOutstandingAmountForStatuses(
                businessId = businessId,
                statuses = listOf("SENT", "PARTIALLY_PAID", "OVERDUE")
            )
        } returns flowOf(expected)

        val result = accountingService.observeOutstandingAmount(businessId).first()

        assertEquals(expected, result)
        verify {
            invoiceDaoV2.observeOutstandingAmountForStatuses(
                businessId = businessId,
                statuses = listOf(
                    InvoiceStatus.SENT.name,
                    InvoiceStatus.PARTIALLY_PAID.name,
                    InvoiceStatus.OVERDUE.name
                )
            )
        }
    }

    @Test
    fun `observeOutstandingAmount - returns zero when no outstanding invoices`() = runTest {
        every {
            invoiceDaoV2.observeOutstandingAmountForStatuses(any(), any())
        } returns flowOf(0L)

        val result = accountingService.observeOutstandingAmount(businessId).first()

        assertEquals(0L, result)
    }

    // ── observeCollectedAmount ────────────────────────────────────────────────

    @Test
    fun `observeCollectedAmount - delegates to dao with PAID and PARTIALLY_PAID`() = runTest {
        val expected = 300_00L
        every {
            invoiceDaoV2.observeCollectedAmountForStatuses(
                businessId = businessId,
                statuses = listOf("PAID", "PARTIALLY_PAID")
            )
        } returns flowOf(expected)

        val result = accountingService.observeCollectedAmount(businessId).first()

        assertEquals(expected, result)
    }

    // ── observeBilledAmount ───────────────────────────────────────────────────

    @Test
    fun `observeBilledAmount - excludes DRAFT invoices`() = runTest {
        val expected = 500_00L
        every {
            invoiceDaoV2.observeBilledAmount(
                businessId = businessId,
                excludeStatuses = listOf("DRAFT")
            )
        } returns flowOf(expected)

        val result = accountingService.observeBilledAmount(businessId).first()

        assertEquals(expected, result)
    }

    // ── observeCollectionRate ─────────────────────────────────────────────────

    @Test
    fun `observeCollectionRate - calculates percentage correctly`() = runTest {
        every {
            invoiceDaoV2.observeCollectionMetrics(businessId)
        } returns flowOf(InvoiceDaoV2.CollectionSummary(billedAmount = 1000_00L, collectedAmount = 750_00L))

        val rate = accountingService.observeCollectionRate(businessId).first()

        assertEquals(75.0, rate, 0.001)
    }

    @Test
    fun `observeCollectionRate - returns 0 when billed is zero`() = runTest {
        every {
            invoiceDaoV2.observeCollectionMetrics(businessId)
        } returns flowOf(InvoiceDaoV2.CollectionSummary(billedAmount = 0L, collectedAmount = 0L))

        val rate = accountingService.observeCollectionRate(businessId).first()

        assertEquals(0.0, rate, 0.001)
    }

    @Test
    fun `observeCollectionRate - 100 percent when fully collected`() = runTest {
        every {
            invoiceDaoV2.observeCollectionMetrics(businessId)
        } returns flowOf(InvoiceDaoV2.CollectionSummary(billedAmount = 200_00L, collectedAmount = 200_00L))

        val rate = accountingService.observeCollectionRate(businessId).first()

        assertEquals(100.0, rate, 0.001)
    }

    // ── observeMTDRevenue ─────────────────────────────────────────────────────

    @Test
    fun `observeMTDRevenue - delegates to dao with PAID status`() = runTest {
        val expected = 120_00L
        every {
            invoiceDaoV2.observeRevenueInDateRange(
                businessId = businessId,
                startDateMillis = any(),
                endDateMillis = any(),
                status = "PAID"
            )
        } returns flowOf(expected)

        val result = accountingService.observeMTDRevenue(businessId).first()

        assertEquals(expected, result)
        verify {
            invoiceDaoV2.observeRevenueInDateRange(
                businessId = businessId,
                startDateMillis = any(),
                endDateMillis = any(),
                status = InvoiceStatus.PAID.name
            )
        }
    }

    // ── observeYTDRevenue ─────────────────────────────────────────────────────

    @Test
    fun `observeYTDRevenue - delegates to dao with PAID status`() = runTest {
        val expected = 5000_00L
        every {
            invoiceDaoV2.observeRevenueInDateRange(
                businessId = businessId,
                startDateMillis = any(),
                endDateMillis = any(),
                status = "PAID"
            )
        } returns flowOf(expected)

        val result = accountingService.observeYTDRevenue(businessId).first()

        assertEquals(expected, result)
    }

    // ── observeUnpaidInvoiceCount ─────────────────────────────────────────────

    @Test
    fun `observeUnpaidInvoiceCount - includes SENT PARTIALLY_PAID OVERDUE`() = runTest {
        every {
            invoiceDaoV2.observeInvoiceCountForStatuses(
                businessId = businessId,
                statuses = listOf("SENT", "PARTIALLY_PAID", "OVERDUE")
            )
        } returns flowOf(7)

        val count = accountingService.observeUnpaidInvoiceCount(businessId).first()

        assertEquals(7, count)
    }

    // ── observePaidInvoiceCount ───────────────────────────────────────────────

    @Test
    fun `observePaidInvoiceCount - only includes PAID`() = runTest {
        every {
            invoiceDaoV2.observeInvoiceCountForStatuses(
                businessId = businessId,
                statuses = listOf("PAID")
            )
        } returns flowOf(3)

        val count = accountingService.observePaidInvoiceCount(businessId).first()

        assertEquals(3, count)
    }

    // ── observeTotalInvoiceCount ──────────────────────────────────────────────

    @Test
    fun `observeTotalInvoiceCount - excludes DRAFT`() = runTest {
        every {
            invoiceDaoV2.observeInvoiceCountForStatuses(
                businessId = businessId,
                statuses = listOf("SENT", "PARTIALLY_PAID", "OVERDUE", "PAID")
            )
        } returns flowOf(10)

        val count = accountingService.observeTotalInvoiceCount(businessId).first()

        assertEquals(10, count)
        // DRAFT must not be in the statuses list
        verify {
            invoiceDaoV2.observeInvoiceCountForStatuses(
                businessId = businessId,
                statuses = match { list -> InvoiceStatus.DRAFT.name !in list }
            )
        }
    }

    // ── GUI1 = GUI2 parity ────────────────────────────────────────────────────

    @Test
    fun `same businessId always produces consistent outstanding across multiple calls`() = runTest {
        val expected = 999_99L
        every {
            invoiceDaoV2.observeOutstandingAmountForStatuses(businessId = businessId, statuses = any())
        } returns flowOf(expected)

        val call1 = accountingService.observeOutstandingAmount(businessId).first()
        val call2 = accountingService.observeOutstandingAmount(businessId).first()

        assertEquals(call1, call2, "AccountingService must return identical values for the same businessId")
    }
}
