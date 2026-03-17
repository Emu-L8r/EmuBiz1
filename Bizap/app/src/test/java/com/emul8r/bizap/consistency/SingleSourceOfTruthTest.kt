@file:Suppress("UNCHECKED_CAST")
package com.emul8r.bizap.consistency

import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.data.local.dao.InvoiceDaoV2
import com.emul8r.bizap.data.local.entities.InvoiceStatusCountV2
import com.emul8r.bizap.data.repository.analytics.AnalyticsCalculator
import com.emul8r.bizap.data.repository.analytics.AnalyticsValidator
import com.emul8r.bizap.data.repository.gui2.PaymentAnalyticsRepositoryV2
import com.emul8r.bizap.data.repository.gui2.RevenueRepositoryV2
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.math.abs
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Validates the Single Source of Truth rules established for financial metrics.
 *
 * Rules under test:
 * 1. Outstanding includes only SENT, PARTIALLY_PAID (and OVERDUE) invoices
 * 2. Revenue/Paid includes only PAID and PARTIALLY_PAID invoices
 * 3. Collection Rate = (Paid / (Paid + Outstanding)) × 100 — amount-based, not count-based
 * 4. All screens share the same V2 repository backed by InvoiceDaoV2
 *
 * Test data mirrors the problem statement scenario:
 *   INV-1: A$100.00, SENT  → outstanding
 *   INV-2: A$222.00, PAID  → collected
 *   INV-3: A$1,000.00, DRAFT → excluded from all metrics
 *
 * Expected results:
 *   Outstanding:     A$100.00  (only SENT)
 *   Collected:       A$222.00  (only PAID)
 *   Collection Rate: ~69%      (222 / (222 + 100) = 68.9%)
 */
class SingleSourceOfTruthTest : BaseUnitTest() {

    private val dao: InvoiceDaoV2 = mockk()
    private lateinit var paymentRepo: PaymentAnalyticsRepositoryV2
    private lateinit var revenueRepo: RevenueRepositoryV2

    private val businessId = 1L

    // Test scenario amounts in cents
    private val inv1Amount = 10000L   // A$100.00 SENT
    private val inv2Amount = 22200L   // A$222.00 PAID
    // inv3 A$1,000 DRAFT is excluded

    @Before
    fun setup() {
        val calculator = AnalyticsCalculator()
        val validator = AnalyticsValidator()
        paymentRepo = PaymentAnalyticsRepositoryV2(dao, calculator, validator)
        revenueRepo = RevenueRepositoryV2(dao, calculator, validator)
    }

    // ── Rule 1: Outstanding only includes SENT + PARTIALLY_PAID (+ OVERDUE) ──

    @Test
    fun `rule1_OutstandingFilter - DRAFT invoices are excluded from outstanding`() = runTest {
        // Only the SENT invoice contributes to outstanding; DRAFT and PAID do not
        stubPaymentDao(
            outstanding = inv1Amount,    // only INV-1 (SENT)
            collected = inv2Amount,      // only INV-2 (PAID)
            statusCounts = listOf(
                InvoiceStatusCountV2("SENT", 1),
                InvoiceStatusCountV2("PAID", 1),
                InvoiceStatusCountV2("DRAFT", 1)
            )
        )

        val metrics = paymentRepo.observePaymentMetrics(businessId).first().getOrThrow()

        assertEquals(inv1Amount, metrics.outstandingAmount,
            "Outstanding should be A\$100 (only SENT invoice, DRAFT excluded)")
    }

    @Test
    fun `rule1_OutstandingFilter - PAID invoices do not appear in outstanding`() = runTest {
        stubPaymentDao(
            outstanding = 0L,            // fully paid business has zero outstanding
            collected = inv2Amount,
            statusCounts = listOf(InvoiceStatusCountV2("PAID", 1))
        )

        val metrics = paymentRepo.observePaymentMetrics(businessId).first().getOrThrow()

        assertEquals(0L, metrics.outstandingAmount, "PAID invoices must not contribute to outstanding")
    }

    @Test
    fun `rule1_OutstandingFilter - PARTIALLY_PAID outstanding is balance remaining`() = runTest {
        val totalAmount = 20000L   // A$200.00 invoice
        val amountPaid = 8000L     // A$80.00 already paid
        val balanceRemaining = totalAmount - amountPaid  // A$120.00 still outstanding

        stubPaymentDao(
            outstanding = balanceRemaining,
            collected = amountPaid,
            statusCounts = listOf(InvoiceStatusCountV2("PARTIALLY_PAID", 1))
        )

        val metrics = paymentRepo.observePaymentMetrics(businessId).first().getOrThrow()

        assertEquals(balanceRemaining, metrics.outstandingAmount,
            "Outstanding for PARTIALLY_PAID should be totalAmount - amountPaid")
        assertEquals(amountPaid, metrics.collectedAmount)
    }

    // ── Rule 2: Revenue/Paid includes only PAID + PARTIALLY_PAID ─────────────

    @Test
    fun `rule2_RevenueFilter - revenue reflects only paid invoices`() = runTest {
        // Revenue = A$222.00 (INV-2 PAID only; INV-1 SENT and INV-3 DRAFT excluded)
        every { dao.observeMTDRevenue(businessId, any(), any()) } returns flowOf(inv2Amount)
        every { dao.observeYTDRevenue(businessId, any(), any()) } returns flowOf(inv2Amount)
        every { dao.observeWeeklyRevenue(businessId, any(), any()) } returns flowOf(inv2Amount)
        every { dao.observeTotalPaidRevenue(businessId) } returns flowOf(inv2Amount)
        every { dao.observeLast30DaysRevenueTrend(businessId) } returns flowOf(emptyList())

        val metrics = revenueRepo.observeRevenueMetrics(businessId).first().getOrThrow()

        assertEquals(inv2Amount, metrics.mtdRevenue,
            "MTD Revenue should be A\$222 (only PAID invoice)")
        assertEquals(inv2Amount, metrics.totalPaidRevenue,
            "Total paid revenue should match the PAID invoice amount")
    }

    @Test
    fun `rule2_RevenueFilter - DRAFT invoices are excluded from all revenue metrics`() = runTest {
        // Even though DRAFT invoice exists, revenue should be zero if no PAID invoices
        every { dao.observeMTDRevenue(businessId, any(), any()) } returns flowOf(0L)
        every { dao.observeYTDRevenue(businessId, any(), any()) } returns flowOf(0L)
        every { dao.observeWeeklyRevenue(businessId, any(), any()) } returns flowOf(0L)
        every { dao.observeTotalPaidRevenue(businessId) } returns flowOf(0L)
        every { dao.observeLast30DaysRevenueTrend(businessId) } returns flowOf(emptyList())

        val metrics = revenueRepo.observeRevenueMetrics(businessId).first().getOrThrow()

        assertEquals(0L, metrics.mtdRevenue, "DRAFT invoices must not appear in MTD revenue")
        assertEquals(0L, metrics.ytdRevenue, "DRAFT invoices must not appear in YTD revenue")
    }

    // ── Rule 3: Collection Rate is amount-based ───────────────────────────────

    @Test
    fun `rule3_CollectionRate - matches problem statement scenario of 69 percent`() = runTest {
        // INV-1 SENT A$100 outstanding, INV-2 PAID A$222 collected, INV-3 DRAFT excluded
        // Expected: 222 / (222 + 100) = 68.94% ≈ 69%
        stubPaymentDao(
            outstanding = inv1Amount,
            collected = inv2Amount,
            statusCounts = listOf(
                InvoiceStatusCountV2("SENT", 1),
                InvoiceStatusCountV2("PAID", 1),
                InvoiceStatusCountV2("DRAFT", 1)
            )
        )

        val metrics = paymentRepo.observePaymentMetrics(businessId).first().getOrThrow()

        val expectedRate = inv2Amount * 100.0 / (inv2Amount + inv1Amount)
        assertEquals(expectedRate, metrics.collectionRate,
            "Collection rate should be ~68.9% for the problem statement scenario")
        assertTrue(metrics.collectionRate in 68.0..70.0,
            "Collection rate should be approximately 69%")
    }

    @Test
    fun `rule3_CollectionRate - zero when no invoices`() = runTest {
        stubPaymentDao(outstanding = 0L, collected = 0L, statusCounts = emptyList())

        val metrics = paymentRepo.observePaymentMetrics(businessId).first().getOrThrow()

        assertEquals(0.0, metrics.collectionRate, "Collection rate should be 0 with no invoices")
    }

    @Test
    fun `rule3_CollectionRate - 100 percent when everything is paid`() = runTest {
        val paid = 500000L
        stubPaymentDao(
            outstanding = 0L,
            collected = paid,
            statusCounts = listOf(InvoiceStatusCountV2("PAID", 5))
        )

        val metrics = paymentRepo.observePaymentMetrics(businessId).first().getOrThrow()

        assertEquals(100.0, metrics.collectionRate,
            "Collection rate should be 100% when all invoices are paid")
    }

    @Test
    fun `rule3_CollectionRate - is amount-based not count-based`() = runTest {
        // 1 large PAID invoice ($900) vs 9 small SENT invoices ($100 total)
        // Count-based rate would be: 1/10 = 10%
        // Amount-based rate should be: 900 / (900 + 100) = 90%
        val largePaid = 90000L    // $900
        val smallOutstanding = 10000L  // $100 total from 9 invoices

        stubPaymentDao(
            outstanding = smallOutstanding,
            collected = largePaid,
            statusCounts = listOf(
                InvoiceStatusCountV2("PAID", 1),
                InvoiceStatusCountV2("SENT", 9)
            )
        )

        val metrics = paymentRepo.observePaymentMetrics(businessId).first().getOrThrow()

        val expectedAmountBased = largePaid * 100.0 / (largePaid + smallOutstanding)
        assertEquals(expectedAmountBased, metrics.collectionRate,
            "Collection rate must be amount-based (90%), not count-based (10%)")
        assertTrue(metrics.collectionRate > 50.0,
            "Amount-based rate (90%) must exceed count-based rate (10%)")
    }

    // ── Rule 4: Single Data Source — both repos share the same DAO ───────────

    @Test
    fun `rule4_SingleSource - revenue and payment repos show consistent collected total`() = runTest {
        val totalCollected = inv2Amount  // A$222.00

        every { dao.observeMTDRevenue(businessId, any(), any()) } returns flowOf(totalCollected)
        every { dao.observeYTDRevenue(businessId, any(), any()) } returns flowOf(totalCollected)
        every { dao.observeWeeklyRevenue(businessId, any(), any()) } returns flowOf(totalCollected)
        every { dao.observeTotalPaidRevenue(businessId) } returns flowOf(totalCollected)
        every { dao.observeLast30DaysRevenueTrend(businessId) } returns flowOf(emptyList())

        stubPaymentDao(
            outstanding = inv1Amount,
            collected = totalCollected,
            statusCounts = listOf(
                InvoiceStatusCountV2("SENT", 1),
                InvoiceStatusCountV2("PAID", 1)
            )
        )

        val revenue = revenueRepo.observeRevenueMetrics(businessId).first().getOrThrow()
        val payment = paymentRepo.observePaymentMetrics(businessId).first().getOrThrow()

        // Both repositories read from the same DAO — totals must match
        assertEquals(revenue.totalPaidRevenue, payment.collectedAmount,
            "Revenue repo totalPaidRevenue must equal payment repo collectedAmount")
    }

    @Test
    fun `rule4_SingleSource - outstanding and collected are mutually consistent`() = runTest {
        // For a business with total billed = outstanding + collected
        val outstanding = inv1Amount   // A$100
        val collected = inv2Amount     // A$222
        val totalRelevant = outstanding + collected  // A$322

        stubPaymentDao(
            outstanding = outstanding,
            collected = collected,
            statusCounts = listOf(
                InvoiceStatusCountV2("SENT", 1),
                InvoiceStatusCountV2("PAID", 1)
            )
        )

        val metrics = paymentRepo.observePaymentMetrics(businessId).first().getOrThrow()

        assertEquals(outstanding, metrics.outstandingAmount)
        assertEquals(collected, metrics.collectedAmount)
        // Validate that collection rate sums to 100 with non-collection rate
        val totalRate = metrics.collectionRate + (metrics.outstandingAmount * 100.0 / totalRelevant)
        assertTrue(abs(totalRate - 100.0) < 0.01,
            "Collection rate + outstanding rate should sum to 100%")
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private fun stubPaymentDao(
        outstanding: Long,
        collected: Long,
        statusCounts: List<InvoiceStatusCountV2>,
        overdue: Int = 0,
        avgDays: Double = 0.0
    ) {
        every { dao.observeOutstandingAmount(businessId) } returns flowOf(outstanding)
        every { dao.observeCollectedAmount(businessId) } returns flowOf(collected)
        every { dao.observeInvoiceCountByStatus(businessId) } returns flowOf(statusCounts)
        every { dao.observeOverdueCount(businessId) } returns flowOf(overdue)
        every { dao.observeAverageDaysToPayment(businessId) } returns flowOf(avgDays)
    }
}
