package com.emul8r.bizap.util

import com.emul8r.bizap.domain.model.gui2.RevenueMetricsV2
import com.emul8r.bizap.domain.model.gui2.PaymentMetricsV2
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Common test assertions for Bizap tests.
 *
 * SPRINT 3 - TEST OPTIMIZATION: Eliminates 12+ repetitions of identical
 * assertion sequences across test files.
 *
 * Before (12+ files):
 *   assertEquals(0L, metrics.mtdRevenue)
 *   assertEquals(0L, metrics.ytdRevenue)
 *   assertEquals(0L, metrics.weeklyRevenue)
 *   assertEquals(0L, metrics.totalPaidRevenue)
 *   assertTrue(metrics.dailyTrend.isEmpty())
 *
 * After (1 call):
 *   assertRevenueMetricsAllZero(metrics)
 */

/**
 * Assert all revenue metrics are zero (no data scenario).
 * Common assertion used in 12+ test methods.
 */
fun assertRevenueMetricsAllZero(metrics: RevenueMetricsV2) {
    assertEquals(0L, metrics.mtdRevenue, "MTD revenue should be 0")
    assertEquals(0L, metrics.ytdRevenue, "YTD revenue should be 0")
    assertEquals(0L, metrics.weeklyRevenue, "Weekly revenue should be 0")
    assertEquals(0L, metrics.totalPaidRevenue, "Total paid revenue should be 0")
    assertTrue(metrics.dailyTrend.isEmpty(), "Daily trend should be empty")
}

/**
 * Assert revenue metrics match expected values.
 * Replaces 5+ individual assertEquals calls.
 */
fun assertRevenueMetricsEqual(
    actual: RevenueMetricsV2,
    expectedMtd: Long,
    expectedYtd: Long,
    expectedWeekly: Long,
    expectedTotalPaid: Long,
    expectedTrendSize: Int = 0
) {
    assertEquals(expectedMtd, actual.mtdRevenue, "MTD revenue mismatch")
    assertEquals(expectedYtd, actual.ytdRevenue, "YTD revenue mismatch")
    assertEquals(expectedWeekly, actual.weeklyRevenue, "Weekly revenue mismatch")
    assertEquals(expectedTotalPaid, actual.totalPaidRevenue, "Total paid revenue mismatch")
    assertEquals(expectedTrendSize, actual.dailyTrend.size, "Daily trend size mismatch")
}

/**
 * Assert all payment metrics are zero (no data scenario).
 * Common assertion used in 8+ test methods.
 */
fun assertPaymentMetricsAllZero(metrics: PaymentMetricsV2) {
    assertEquals(0L, metrics.outstandingAmount, "Outstanding should be 0")
    assertEquals(0L, metrics.collectedAmount, "Collected should be 0")
    assertEquals(0, metrics.sentCount, "Sent count should be 0")
    assertEquals(0, metrics.paidCount, "Paid count should be 0")
    assertEquals(0, metrics.overdueCount, "Overdue count should be 0")
}

/**
 * Assert payment metrics match expected values.
 * Replaces 5+ individual assertEquals calls.
 */
fun assertPaymentMetricsEqual(
    actual: PaymentMetricsV2,
    expectedOutstanding: Long,
    expectedCollected: Long,
    expectedSentCount: Int = 0,
    expectedPaidCount: Int = 0,
    expectedOverdueCount: Int = 0
) {
    assertEquals(expectedOutstanding, actual.outstandingAmount, "Outstanding mismatch")
    assertEquals(expectedCollected, actual.collectedAmount, "Collected mismatch")
    assertEquals(expectedSentCount, actual.sentCount, "Sent count mismatch")
    assertEquals(expectedPaidCount, actual.paidCount, "Paid count mismatch")
    assertEquals(expectedOverdueCount, actual.overdueCount, "Overdue count mismatch")
}





