package com.emul8r.bizap

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.emul8r.bizap.data.local.dao.InvoiceDaoV2
import com.emul8r.bizap.data.local.entities.DailyRevenueTrendV2
import com.emul8r.bizap.data.local.entities.InvoiceStatusCountV2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.rules.TestRule
import io.mockk.every

/**
 * Base class for all unit tests.
 * Correctly handles Main Dispatcher overrides for ViewModel testing.
 *
 * SPRINT 3 - TEST OPTIMIZATION: Added common DAO stubbing helpers
 * to eliminate repetitive mocking patterns across 40+ test methods.
 */
@OptIn(ExperimentalCoroutinesApi::class)
abstract class BaseUnitTest {
    
    @get:Rule
    val instantTaskExecutorRule: TestRule = InstantTaskExecutorRule()
    
    protected val testDispatcher = StandardTestDispatcher()
    
    @Before
    fun setupBase() {
        Dispatchers.setMain(testDispatcher)
    }
    
    @After
    fun tearDownBase() {
        Dispatchers.resetMain()
    }

    protected fun runUnitTest(block: suspend TestScope.() -> Unit) = runTest(testDispatcher) {
        block()
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // REVENUE DAO STUBBING HELPERS (Eliminates 40+ repetitions across test files)
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Stub all revenue-related DAO queries for a business.
     * Eliminates the need for 6 separate `every { dao.observe*() }` calls.
     *
     * Usage:
     *   stubRevenueMetrics(dao = mockDao, businessId = 1L, mtd = 200000L, ytd = 800000L)
     *
     * Before (6 lines):
     *   every { dao.observeMTDRevenue(businessId, any(), any()) } returns flowOf(200000L)
     *   every { dao.observeYTDRevenue(businessId, any(), any()) } returns flowOf(800000L)
     *   // ... 4 more lines
     *
     * After (1 line):
     *   stubRevenueMetrics(dao, businessId, mtd = 200000L, ytd = 800000L)
     */
    protected fun stubRevenueMetrics(
        dao: InvoiceDaoV2,
        businessId: Long,
        mtd: Long = 0L,
        ytd: Long = 0L,
        weekly: Long = 0L,
        totalPaid: Long = 0L,
        trend: List<DailyRevenueTrendV2> = emptyList(),
        overdue: Long = 0L
    ) {
        every { dao.observeMTDRevenue(businessId, any(), any()) } returns flowOf(mtd)
        every { dao.observeYTDRevenue(businessId, any(), any()) } returns flowOf(ytd)
        every { dao.observeWeeklyRevenue(businessId, any(), any()) } returns flowOf(weekly)
        every { dao.observeTotalPaidRevenue(businessId) } returns flowOf(totalPaid)
        every { dao.observeLast30DaysRevenueTrend(businessId, any(), any()) } returns flowOf(trend)
        every { dao.observeOverdueAmount(businessId) } returns flowOf(overdue)
    }

    /**
     * Stub all payment-related DAO queries for a business.
     * Eliminates the need for 5 separate `every { dao.observe*() }` calls.
     */
    protected fun stubPaymentMetrics(
        dao: InvoiceDaoV2,
        businessId: Long,
        outstanding: Long = 0L,
        collected: Long = 0L,
        statusCounts: List<InvoiceStatusCountV2> = emptyList(),
        overdueCount: Int = 0,
        averageDaysToPayment: Double = 0.0
    ) {
        every { dao.observeOutstandingAmount(businessId) } returns flowOf(outstanding)
        every { dao.observeCollectedAmount(businessId) } returns flowOf(collected)
        every { dao.observeInvoiceCountByStatus(businessId) } returns flowOf(statusCounts)
        every { dao.observeOverdueCount(businessId) } returns flowOf(overdueCount)
        every { dao.observeAverageDaysToPayment(businessId) } returns flowOf(averageDaysToPayment)
    }

    /**
     * Stub both revenue AND payment metrics in one call.
     * Convenience method for tests that need complete DAO stubs.
     */
    protected fun stubAllMetrics(
        dao: InvoiceDaoV2,
        businessId: Long,
        mtd: Long = 0L,
        ytd: Long = 0L,
        weekly: Long = 0L,
        totalPaid: Long = 0L,
        trend: List<DailyRevenueTrendV2> = emptyList(),
        overdue: Long = 0L,
        outstanding: Long = 0L,
        collected: Long = 0L,
        statusCounts: List<InvoiceStatusCountV2> = emptyList(),
        overdueCount: Int = 0,
        averageDaysToPayment: Double = 0.0
    ) {
        stubRevenueMetrics(dao, businessId, mtd, ytd, weekly, totalPaid, trend, overdue)
        stubPaymentMetrics(dao, businessId, outstanding, collected, statusCounts, overdueCount, averageDaysToPayment)
    }
}



