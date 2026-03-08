package com.emul8r.bizap.consistency

import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.data.local.InvoiceDao
import com.emul8r.bizap.data.local.dao.AnalyticsDao
import com.emul8r.bizap.data.consistency.DataConsistencyValidator
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertEquals

/**
 * Tests verifying that daily revenue snapshot totals match the sum from the invoices table.
 *
 * These tests verify the [DataConsistencyValidator] correctly identifies:
 * 1. Consistent data (snapshot matches calculated)
 * 2. Inconsistent data above the tolerance threshold
 * 3. Edge cases (empty data, exact tolerance boundary)
 */
class DailyRevenueTotalTest : BaseUnitTest() {

    private val invoiceDao: InvoiceDao = mockk()
    private val analyticsDao: AnalyticsDao = mockk()
    private lateinit var validator: DataConsistencyValidator

    private val businessId = 1L

    @Before
    fun setup() {
        validator = DataConsistencyValidator(invoiceDao, analyticsDao)
    }

    // ── consistent data ──────────────────────────────────────────────────────

    @Test
    fun `revenueConsistency_Passing - snapshot matches calculated revenue`() = runTest {
        val calculatedRevenue = 500000L  // $5,000.00
        every { invoiceDao.observeTotalPaidRevenue(businessId) } returns flowOf(calculatedRevenue)
        coEvery { analyticsDao.getTotalPaidRevenueLong(businessId) } returns calculatedRevenue

        val report = validator.runDailyHealthCheck(businessId)

        assertTrue(report.isConsistent)
        assertEquals(0L, report.revenueDiffCents)
    }

    @Test
    fun `revenueConsistency_WithinTolerance - 1 cent difference is considered consistent`() = runTest {
        val calculatedRevenue = 500000L
        val snapshotRevenue = 499999L  // 1¢ difference — within tolerance
        every { invoiceDao.observeTotalPaidRevenue(businessId) } returns flowOf(calculatedRevenue)
        coEvery { analyticsDao.getTotalPaidRevenueLong(businessId) } returns snapshotRevenue

        val report = validator.runDailyHealthCheck(businessId)

        assertTrue(report.isConsistent)
        assertEquals(1L, report.revenueDiffCents)
    }

    @Test
    fun `revenueConsistency_ExactMatch - identical values produce zero diff`() = runTest {
        val revenue = 123456L
        every { invoiceDao.observeTotalPaidRevenue(businessId) } returns flowOf(revenue)
        coEvery { analyticsDao.getTotalPaidRevenueLong(businessId) } returns revenue

        val report = validator.runDailyHealthCheck(businessId)

        assertTrue(report.isConsistent)
        assertEquals(0L, report.revenueDiffCents)
        assertEquals(revenue, report.calculatedRevenueCents)
        assertEquals(revenue, report.snapshotRevenueCents)
    }

    // ── inconsistent data ────────────────────────────────────────────────────

    @Test
    fun `revenueInconsistency_Detected - 2 cent difference exceeds tolerance`() = runTest {
        val calculatedRevenue = 500000L
        val snapshotRevenue = 499998L  // 2¢ difference — above tolerance
        every { invoiceDao.observeTotalPaidRevenue(businessId) } returns flowOf(calculatedRevenue)
        coEvery { analyticsDao.getTotalPaidRevenueLong(businessId) } returns snapshotRevenue

        val report = validator.runDailyHealthCheck(businessId)

        assertFalse(report.isConsistent)
        assertEquals(2L, report.revenueDiffCents)
    }

    @Test
    fun `revenueInconsistency_LargeGap - stale snapshot showing zero when revenue exists`() = runTest {
        val calculatedRevenue = 1000000L  // $10,000
        val snapshotRevenue = 0L          // Stale/empty snapshot
        every { invoiceDao.observeTotalPaidRevenue(businessId) } returns flowOf(calculatedRevenue)
        coEvery { analyticsDao.getTotalPaidRevenueLong(businessId) } returns snapshotRevenue

        val report = validator.runDailyHealthCheck(businessId)

        assertFalse(report.isConsistent)
        assertEquals(1000000L, report.revenueDiffCents)
        assertEquals(calculatedRevenue, report.calculatedRevenueCents)
        assertEquals(snapshotRevenue, report.snapshotRevenueCents)
    }

    // ── empty data ───────────────────────────────────────────────────────────

    @Test
    fun `emptyData_Consistent - both zero is consistent`() = runTest {
        every { invoiceDao.observeTotalPaidRevenue(businessId) } returns flowOf(0L)
        coEvery { analyticsDao.getTotalPaidRevenueLong(businessId) } returns 0L

        val report = validator.runDailyHealthCheck(businessId)

        assertTrue(report.isConsistent)
        assertEquals(0L, report.revenueDiffCents)
    }

    // ── error handling ───────────────────────────────────────────────────────

    @Test
    fun `errorHandling_DAOException - DAO failure produces error report without crashing`() = runTest {
        every { invoiceDao.observeTotalPaidRevenue(businessId) } returns flowOf(500000L)
        coEvery { analyticsDao.getTotalPaidRevenueLong(businessId) } throws RuntimeException("DB error")

        val report = validator.runDailyHealthCheck(businessId)

        // Should not throw, just report inconsistency with error
        assertFalse(report.isConsistent)
    }

    @Test
    fun `errorHandling_ReportContainsTimestamp - checkedAtMs is populated`() = runTest {
        every { invoiceDao.observeTotalPaidRevenue(businessId) } returns flowOf(100L)
        coEvery { analyticsDao.getTotalPaidRevenueLong(businessId) } returns 100L

        val before = System.currentTimeMillis()
        val report = validator.runDailyHealthCheck(businessId)
        val after = System.currentTimeMillis()

        assertTrue(report.checkedAtMs in before..after)
    }
}
