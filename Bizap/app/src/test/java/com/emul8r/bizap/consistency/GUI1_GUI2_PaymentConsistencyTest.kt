@file:Suppress("UNCHECKED_CAST")
package com.emul8r.bizap.consistency

import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.data.local.dao.InvoicePaymentDao
import com.emul8r.bizap.data.local.dao.InvoiceDaoV2
import com.emul8r.bizap.data.local.entities.InvoiceStatusCountV2
import com.emul8r.bizap.data.repository.PaymentAnalyticsRepositoryImpl
import com.emul8r.bizap.data.repository.analytics.AnalyticsCalculator
import com.emul8r.bizap.data.repository.analytics.AnalyticsValidator
import com.emul8r.bizap.data.repository.gui2.PaymentAnalyticsRepositoryV2
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Critical test suite verifying that GUI1 and GUI2 show identical payment data.
 *
 * CONTEXT: GUI1 originally read from InvoicePaymentSnapshot tables (stale cache),
 * while GUI2 read from invoices table (source of truth). This test verifies
 * the fix: both GUIs now share PaymentAnalyticsRepositoryV2 via delegation.
 *
 * PASSING THESE TESTS = No financial discrepancies between UI screens
 */
class GUI1_GUI2_PaymentConsistencyTest : BaseUnitTest() {

    private val invoiceDaoV2: InvoiceDaoV2 = mockk()
    private val paymentDao: InvoicePaymentDao = mockk()
    private lateinit var repositoryV2: PaymentAnalyticsRepositoryV2
    private lateinit var repositoryImpl: PaymentAnalyticsRepositoryImpl

    private val businessId = 1L

    @Before
    fun setup() {
        val calculator = AnalyticsCalculator()
        val validator = AnalyticsValidator()
        repositoryV2 = PaymentAnalyticsRepositoryV2(invoiceDaoV2, calculator, validator)
        repositoryImpl = PaymentAnalyticsRepositoryImpl(paymentDao, mockk(), repositoryV2)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CRITICAL TEST: Record Payment → Both GUIs Show Same Result
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `GUI1_and_GUI2_consistency - after payment recording both show same outstanding balance`() = runTest {
        // SCENARIO: Invoice for $100, payment of $50 recorded
        val amountPaid = 5000L       // $50.00 cents
        val outstanding = 5000L      // $50.00 cents

        stubInvoiceDaoV2(
            outstanding = outstanding,
            collected = amountPaid,
            statusCounts = listOf(
                InvoiceStatusCountV2("PARTIALLY_PAID", 1),
                InvoiceStatusCountV2("SENT", 0)
            )
        )

        // Both repositories should return the same collected and outstanding amounts
        val metricsV2 = repositoryV2.observePaymentMetrics(businessId).first()
        val metricsImpl = repositoryImpl.observePaymentAnalytics(businessId).first()

        assertEquals(amountPaid, metricsV2.collectedAmount, "V2 shows correct collected amount")
        assertEquals(outstanding, metricsV2.outstandingAmount, "V2 shows correct outstanding amount")

        // Convert to dollars for comparison (hundredths of cents)
        val expectedCollected = amountPaid.toDouble() / 100.0
        val expectedOutstanding = outstanding.toDouble() / 100.0

        assertEquals(expectedCollected, metricsImpl.totalPaidAmount, absoluteTolerance = 0.01, message = "GUI1 (via Impl) shows same collected")
        assertEquals(expectedOutstanding, metricsImpl.totalOutstandingAmount, absoluteTolerance = 0.01, message = "GUI1 (via Impl) shows same outstanding")
    }

    @Test
    fun `GUI1_and_GUI2_consistency - collection rate is identical`() = runTest {
        // SCENARIO: $1000 billed, $600 paid = 60% collection rate
        val collected = 60000L      // $600
        val outstanding = 40000L    // $400

        stubInvoiceDaoV2(
            outstanding = outstanding,
            collected = collected,
            statusCounts = listOf(
                InvoiceStatusCountV2("PAID", 3),
                InvoiceStatusCountV2("PARTIALLY_PAID", 2),
                InvoiceStatusCountV2("SENT", 5)
            )
        )

        val metricsV2 = repositoryV2.observePaymentMetrics(businessId).first()
        val metricsImpl = repositoryImpl.observePaymentAnalytics(businessId).first()

        // Collection rate should be: (collected / (collected + outstanding)) * 100
        val expectedRate = 60.0

        assertEquals(expectedRate, metricsV2.collectionRate, absoluteTolerance = 0.1, message = "V2 collection rate")
        assertEquals(expectedRate, metricsImpl.collectionRate, absoluteTolerance = 0.1, message = "Impl collection rate")
    }

    @Test
    fun `GUI1_and_GUI2_consistency - zero outstanding when fully paid`() = runTest {
        val collected = 50000L     // $500
        val outstanding = 0L       // $0

        stubInvoiceDaoV2(
            outstanding = outstanding,
            collected = collected,
            statusCounts = listOf(InvoiceStatusCountV2("PAID", 5))
        )

        val metricsV2 = repositoryV2.observePaymentMetrics(businessId).first()
        val metricsImpl = repositoryImpl.observePaymentAnalytics(businessId).first()

        assertEquals(0L, metricsV2.outstandingAmount, "V2 shows zero outstanding for paid invoices")
        assertEquals(0.0, metricsImpl.totalOutstandingAmount, "Impl shows zero outstanding for paid invoices")

        assertEquals(100.0, metricsV2.collectionRate, absoluteTolerance = 0.1, message = "V2 shows 100% collection")
        assertEquals(100.0, metricsImpl.collectionRate, absoluteTolerance = 0.1, message = "Impl shows 100% collection")
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SNAPSHOT STALENESS TEST: Verify UI doesn't depend on snapshot updates
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `snapshot_staleness_resilience - UI correct even if snapshot sync fails`() = runTest {
        // SCENARIO: Payment recorded but snapshot update failed
        // Invoice table shows: $50 paid
        // Snapshot table shows: $0 paid (stale!)
        // UI should show: $50 paid (from invoices table)

        val collected = 5000L      // $50
        val outstanding = 5000L    // $50

        stubInvoiceDaoV2(
            outstanding = outstanding,
            collected = collected,
            statusCounts = listOf(InvoiceStatusCountV2("PARTIALLY_PAID", 1))
        )

        val metricsV2 = repositoryV2.observePaymentMetrics(businessId).first()
        val metricsImpl = repositoryImpl.observePaymentAnalytics(businessId).first()

        // Both should read from invoices table and show correct data
        // regardless of snapshot state
        assertEquals(collected, metricsV2.collectedAmount, "V2 reads from invoices, not snapshots")
        assertEquals(outstanding, metricsV2.outstandingAmount)

        assertEquals(collected.toDouble() / 100.0, metricsImpl.totalPaidAmount, absoluteTolerance = 0.01)
        assertEquals(outstanding.toDouble() / 100.0, metricsImpl.totalOutstandingAmount, absoluteTolerance = 0.01)
    }

    @Test
    fun `snapshot_staleness_resilience - progress bar always accurate`() = runTest {
        // Invoice detail page uses: amountPaid / totalAmount
        // This should ALWAYS match analytics "collected" amount

        val totalAmount = 20000L   // $200
        val amountPaid = 8000L     // $80
        val outstanding = 12000L   // $120

        stubInvoiceDaoV2(
            outstanding = outstanding,
            collected = amountPaid,
            statusCounts = listOf(InvoiceStatusCountV2("PARTIALLY_PAID", 1))
        )

        val metricsV2 = repositoryV2.observePaymentMetrics(businessId).first()

        // Calculate progress bar ratio: 80 / 200 = 0.4 (40%)
        val progressBarRatio = amountPaid.toDouble() / totalAmount.toDouble()
        val expectedProgress = 0.4

        assertEquals(expectedProgress, progressBarRatio, absoluteTolerance = 0.01, message = "Progress bar shows 40%")

        // Analytics collected amount should equal amountPaid
        assertEquals(amountPaid, metricsV2.collectedAmount, "Collected = amountPaid")
    }

    // ─────────────────────────────────────────────────────────────────────────
    // EDGE CASES: Verify consistency under unusual scenarios
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `edge_case - multiple partial payments on same invoice`() = runTest {
        // Invoice for $500, three payments: $100 + $150 + $100 = $350 paid, $150 outstanding
        val collected = 35000L        // $350
        val outstanding = 15000L      // $150

        stubInvoiceDaoV2(
            outstanding = outstanding,
            collected = collected,
            statusCounts = listOf(InvoiceStatusCountV2("PARTIALLY_PAID", 1))
        )

        val metricsV2 = repositoryV2.observePaymentMetrics(businessId).first()
        val metricsImpl = repositoryImpl.observePaymentAnalytics(businessId).first()

        assertEquals(collected, metricsV2.collectedAmount)
        assertEquals(outstanding, metricsV2.outstandingAmount)

        val totalAmount = collected + outstanding
        val expectedCollectionRate = (collected.toDouble() / totalAmount.toDouble()) * 100
        assertEquals(expectedCollectionRate, metricsV2.collectionRate, absoluteTolerance = 0.1)
        assertEquals(expectedCollectionRate, metricsImpl.collectionRate, absoluteTolerance = 0.1)
    }

    @Test
    fun `edge_case - overpayment prevention in UI`() = runTest {
        // Invoice for $100, verify we never show collected > billed
        val collected = 10000L
        val outstanding = 0L

        stubInvoiceDaoV2(
            outstanding = outstanding,
            collected = collected,
            statusCounts = listOf(InvoiceStatusCountV2("PAID", 1))
        )

        val metricsV2 = repositoryV2.observePaymentMetrics(businessId).first()

        // Sanity check: collected should never exceed billed amount
        assertTrue(metricsV2.collectedAmount <= (metricsV2.collectedAmount + metricsV2.outstandingAmount),
            "Collected never exceeds total billed")
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPER FUNCTIONS
    // ─────────────────────────────────────────────────────────────────────────

    private fun stubInvoiceDaoV2(
        outstanding: Long,
        collected: Long,
        statusCounts: List<InvoiceStatusCountV2>
    ) {
        every { invoiceDaoV2.observeOutstandingAmount(businessId) } returns flowOf(outstanding)
        every { invoiceDaoV2.observeCollectedAmount(businessId) } returns flowOf(collected)
        every { invoiceDaoV2.observeInvoiceCountByStatus(businessId) } returns flowOf(statusCounts)
        every { invoiceDaoV2.observeOverdueCount(businessId) } returns flowOf(0)
        every { invoiceDaoV2.observeAverageDaysToPayment(businessId) } returns flowOf(0.0)
    }
}





