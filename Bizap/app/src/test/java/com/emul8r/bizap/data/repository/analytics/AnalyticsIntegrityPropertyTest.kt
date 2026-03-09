package com.emul8r.bizap.data.repository.analytics

import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.data.local.entities.DailyRevenueTrendV2
import com.emul8r.bizap.data.local.entities.InvoiceStatusCountV2
import org.junit.Before
import org.junit.Test
import kotlin.math.absoluteValue
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Property-based integrity tests for analytics calculations.
 *
 * These tests verify that mathematical invariants hold across many randomised input
 * scenarios (100 iterations per property). They are designed to run on every commit
 * to prevent regressions in:
 *
 *   1. outstanding + collected = total_billed (payment invariant)
 *   2. collectionRate ∈ [0, 100] (collection rate bounds)
 *   3. all monetary amounts ≥ 0 (no-negatives invariant)
 *   4. MTD ≤ YTD (revenue period ordering)
 *   5. AnalyticsCalculator produces metrics that satisfy AnalyticsValidator
 */
class AnalyticsIntegrityPropertyTest : BaseUnitTest() {

    private lateinit var validator: AnalyticsValidator
    private lateinit var calculator: AnalyticsCalculator

    private val random = Random(seed = 42)
    private val businessId = 1L

    @Before
    fun setUp() {
        validator = AnalyticsValidator()
        calculator = AnalyticsCalculator()
    }

    // ── Invariant 1: outstanding + collected = totalBilled ─────────────────────

    @Test
    fun `payment invariant - outstanding + collected equals totalBilled across 100 scenarios`() {
        repeat(100) { iteration ->
            val outstanding = random.nextLong(0L, 1_000_000L)
            val collected = random.nextLong(0L, 1_000_000L)
            val totalBilled = outstanding + collected  // correct value

            val result = validator.validatePaymentMetrics(outstanding, collected, totalBilled)

            assertTrue(
                result.isValid,
                "Iteration $iteration: validation failed for outstanding=$outstanding collected=$collected totalBilled=$totalBilled — ${result.error}"
            )
        }
    }

    @Test
    fun `payment invariant - mismatch beyond tolerance is detected across 100 scenarios`() {
        repeat(100) { iteration ->
            val outstanding = random.nextLong(1_000L, 1_000_000L)
            val collected = random.nextLong(1_000L, 1_000_000L)
            // Introduce a large discrepancy (>$1 = 100 cents)
            val badTotal = outstanding + collected + random.nextLong(200L, 10_000L)

            val result = validator.validatePaymentMetrics(outstanding, collected, badTotal)

            assertFalse(
                result.isValid,
                "Iteration $iteration: expected validation failure for outstanding=$outstanding collected=$collected badTotal=$badTotal"
            )
            assertNull(result.error.also { }, "Error message should be present on failure")
            assertTrue(result.error != null, "Error message should be present when invalid")
        }
    }

    // ── Invariant 2: collectionRate ∈ [0, 100] ──────────────────────────────────

    @Test
    fun `collection rate invariant - always bounded in 0 to 100 across 100 scenarios`() {
        repeat(100) { iteration ->
            val outstanding = random.nextLong(0L, 1_000_000L)
            val collected = random.nextLong(0L, 1_000_000L)

            val rate = calculator.computeCollectionRate(outstanding, collected)

            assertTrue(
                rate >= 0.0,
                "Iteration $iteration: collectionRate=$rate is negative for outstanding=$outstanding collected=$collected"
            )
            assertTrue(
                rate <= 100.0,
                "Iteration $iteration: collectionRate=$rate exceeds 100 for outstanding=$outstanding collected=$collected"
            )
        }
    }

    @Test
    fun `collection rate invariant - zero outstanding and collected gives 0 percent`() {
        val rate = calculator.computeCollectionRate(outstanding = 0L, collected = 0L)
        assertEquals(0.0, rate, 0.0001, "Collection rate should be 0 when no invoices exist")
    }

    @Test
    fun `collection rate invariant - fully collected gives 100 percent`() {
        val collected = 50_000L
        val outstanding = 0L
        val rate = calculator.computeCollectionRate(outstanding, collected)
        assertEquals(100.0, rate, 0.0001, "Rate should be 100% when nothing is outstanding")
    }

    @Test
    fun `collection rate invariant - fully outstanding gives 0 percent`() {
        val outstanding = 50_000L
        val collected = 0L
        val rate = calculator.computeCollectionRate(outstanding, collected)
        assertEquals(0.0, rate, 0.0001, "Rate should be 0% when nothing is collected")
    }

    // ── Invariant 3: no negative monetary amounts ────────────────────────────────

    @Test
    fun `no negatives invariant - negative outstanding is detected`() {
        val result = validator.validatePaymentMetrics(
            outstanding = -1L,
            collected = 10_000L,
            totalBilled = 10_000L
        )
        assertFalse(result.isValid, "Negative outstanding should fail validation")
        assertTrue(result.error != null, "Error message expected for negative outstanding")
    }

    @Test
    fun `no negatives invariant - negative collected is detected`() {
        val result = validator.validatePaymentMetrics(
            outstanding = 10_000L,
            collected = -1L,
            totalBilled = 10_000L
        )
        assertFalse(result.isValid, "Negative collected should fail validation")
        assertTrue(result.error != null, "Error message expected for negative collected")
    }

    @Test
    fun `no negatives invariant - zero amounts are valid`() {
        val result = validator.validatePaymentMetrics(
            outstanding = 0L,
            collected = 0L,
            totalBilled = 0L
        )
        assertTrue(result.isValid, "Zero amounts should be valid")
    }

    // ── Invariant 4: MTD ≤ YTD ──────────────────────────────────────────────────

    @Test
    fun `revenue ordering invariant - MTD exceeding YTD emits a warning`() {
        val mtd = 200_000L
        val ytd = 100_000L  // incorrectly less than MTD
        val weekly = 50_000L

        val result = validator.validateRevenueMetrics(mtd, ytd, weekly)

        // Should still return isValid=true (warning, not error) but include a warning
        assertTrue(result.isValid, "MTD > YTD should produce a warning, not an error")
        assertTrue(
            result.warnings.any { it.contains("MTD") },
            "Warning should mention MTD: ${result.warnings}"
        )
    }

    @Test
    fun `revenue ordering invariant - valid MTD and YTD passes across 100 scenarios`() {
        repeat(100) { iteration ->
            val ytd = random.nextLong(0L, 1_000_000L)
            val mtd = random.nextLong(0L, ytd + 1)  // MTD always ≤ YTD
            val weekly = random.nextLong(0L, mtd + 1)

            val result = validator.validateRevenueMetrics(mtd, ytd, weekly)

            assertTrue(
                result.isValid,
                "Iteration $iteration: valid revenue failed — mtd=$mtd ytd=$ytd weekly=$weekly error=${result.error}"
            )
        }
    }

    // ── Invariant 5: AnalyticsCalculator produces valid metrics ──────────────────

    @Test
    fun `calculator and validator agree - combinePaymentMetrics satisfies validatePaymentMetrics across 100 scenarios`() {
        repeat(100) { iteration ->
            val outstanding = random.nextLong(0L, 500_000L)
            val collected = random.nextLong(0L, 500_000L)
            val statusCounts = listOf(
                InvoiceStatusCountV2("PAID", random.nextInt(0, 10)),
                InvoiceStatusCountV2("SENT", random.nextInt(0, 10)),
                InvoiceStatusCountV2("OVERDUE", random.nextInt(0, 5))
            )
            val overdueCount = random.nextInt(0, 10)
            val avgDays = random.nextDouble(0.0, 60.0)

            val metrics = calculator.combinePaymentMetrics(
                businessId = businessId,
                outstanding = outstanding,
                collected = collected,
                statusCounts = statusCounts,
                overdueCount = overdueCount,
                avgDays = avgDays
            )

            // Collection rate should always be in bounds
            val rateValidation = validator.validateCollectionRate(metrics.collectionRate)
            assertTrue(
                rateValidation.isValid,
                "Iteration $iteration: collectionRate=${metrics.collectionRate} failed validation — ${rateValidation.error}"
            )

            // Metrics values should be non-negative
            assertTrue(
                metrics.outstandingAmount >= 0L,
                "Iteration $iteration: outstandingAmount should be non-negative"
            )
            assertTrue(
                metrics.collectedAmount >= 0L,
                "Iteration $iteration: collectedAmount should be non-negative"
            )
        }
    }

    @Test
    fun `calculator and validator agree - combineRevenueMetrics satisfies validateRevenueMetrics`() {
        val mtd = 100_000L
        val ytd = 500_000L
        val weekly = 30_000L
        val trend = listOf(
            DailyRevenueTrendV2(
                dateString = "2025-01-15",
                revenue = 50_000L,
                invoiceCount = 2,
                paidCount = 2
            )
        )

        val metrics = calculator.combineRevenueMetrics(
            businessId = businessId,
            mtd = mtd,
            ytd = ytd,
            weekly = weekly,
            totalPaid = ytd,
            trend = trend
        )

        val validation = validator.validateRevenueMetrics(
            mtdRevenue = metrics.mtdRevenue,
            ytdRevenue = metrics.ytdRevenue,
            weeklyRevenue = metrics.weeklyRevenue
        )

        assertTrue(validation.isValid, "Combined revenue metrics should pass validation: ${validation.error}")
        assertEquals(1, metrics.dailyTrend.size)
        assertEquals("2025-01-15", metrics.dailyTrend[0].date)
        assertEquals(50_000L, metrics.dailyTrend[0].revenueCents)
    }

    // ── Invariant 6: aging bucket sum ───────────────────────────────────────────

    @Test
    fun `aging bucket invariant - matching sums pass across 100 scenarios`() {
        repeat(100) { iteration ->
            val current = random.nextLong(0L, 100_000L)
            val past30 = random.nextLong(0L, 100_000L)
            val past60 = random.nextLong(0L, 100_000L)
            val past90 = random.nextLong(0L, 100_000L)
            val outstanding = current + past30 + past60 + past90

            val buckets = mapOf(
                "current" to current,
                "1-30" to past30,
                "31-60" to past60,
                "61+" to past90
            )

            val result = validator.validateAgingBuckets(buckets, outstanding)

            assertTrue(
                result.isValid,
                "Iteration $iteration: aging buckets should sum to outstanding=$outstanding but got ${buckets.values.sum()}"
            )
        }
    }

    @Test
    fun `aging bucket invariant - mismatch beyond tolerance is detected`() {
        val outstanding = 10_000L
        val buckets = mapOf(
            "current" to 3_000L,
            "1-30" to 2_000L,
            "31-60" to 3_000L,
            "61+" to 1_000L  // intentionally wrong (should be 2_000 to sum to 9_000, but total mismatch needs > 100 cents)
        )
        // 3000 + 2000 + 3000 + 1000 = 9000, but outstanding is 10000 → diff=1000 > 100
        val result = validator.validateAgingBuckets(buckets, outstanding)
        assertFalse(result.isValid, "Mismatch of 1000 cents should fail validation")
    }
}
