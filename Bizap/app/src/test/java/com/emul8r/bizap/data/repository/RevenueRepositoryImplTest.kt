@file:Suppress("UNCHECKED_CAST")
package com.emul8r.bizap.data.repository

import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.data.local.dao.InvoiceDaoV2
import com.emul8r.bizap.data.local.entities.DailyRevenueTrendV2
import com.emul8r.bizap.data.repository.analytics.AnalyticsCalculator
import com.emul8r.bizap.data.repository.analytics.AnalyticsValidator
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RevenueRepositoryImplTest : BaseUnitTest() {

    private val invoiceDaoV2: InvoiceDaoV2 = mockk()
    private lateinit var repository: RevenueRepositoryImpl

    @Before
    fun setup() {
        val calculator = AnalyticsCalculator()
        val validator = AnalyticsValidator()
        repository = RevenueRepositoryImpl(invoiceDaoV2, calculator, validator)
    }

    @Test
    fun `observeRevenueMetrics returns correct values from V2 invoice queries`() = runTest {
        // Arrange
        val businessId = 1L
        val trend = listOf(
            DailyRevenueTrendV2(
                dateString = "2026-03-01",
                revenueCents = 100000L,
                invoiceCount = 2,
                businessProfileId = businessId.toInt()
            ),
            DailyRevenueTrendV2(
                dateString = "2026-03-02",
                revenueCents = 50000L,
                invoiceCount = 1,
                businessProfileId = businessId.toInt()
            )
        )

        every { invoiceDaoV2.observeMTDRevenue(businessId, any(), any()) } returns flowOf(100000L)
        every { invoiceDaoV2.observeYTDRevenue(businessId, any(), any()) } returns flowOf(150000L)
        every { invoiceDaoV2.observeWeeklyRevenue(businessId, any(), any()) } returns flowOf(150000L)
        every { invoiceDaoV2.observeTotalPaidRevenue(businessId) } returns flowOf(150000L)
        every { invoiceDaoV2.observeOutstandingAmount(businessId) } returns flowOf(0L)
        every { invoiceDaoV2.observeLast30DaysRevenueTrend(businessId) } returns flowOf(trend)
        every { invoiceDaoV2.observeOverdueAmount(businessId) } returns flowOf(0L)

        // Act
        val result = repository.observeRevenueMetrics(businessId).first()
        val metrics = result.getOrThrow()

        // Assert
        assertEquals(100000L, metrics.mtdRevenue)
        assertEquals(150000L, metrics.ytdRevenue)
        assertEquals(150000L, metrics.weeklyRevenue)
        assertEquals(150000L, metrics.totalPaidRevenue)
        assertEquals(2, metrics.dailyTrend.size)
    }

    @Test
    fun `observeRevenueMetrics returns zeros when no invoices exist`() = runTest {
        // Arrange
        val businessId = 2L

        every { invoiceDaoV2.observeMTDRevenue(businessId, any(), any()) } returns flowOf(0L)
        every { invoiceDaoV2.observeYTDRevenue(businessId, any(), any()) } returns flowOf(0L)
        every { invoiceDaoV2.observeWeeklyRevenue(businessId, any(), any()) } returns flowOf(0L)
        every { invoiceDaoV2.observeTotalPaidRevenue(businessId) } returns flowOf(0L)
        every { invoiceDaoV2.observeLast30DaysRevenueTrend(businessId) } returns flowOf(emptyList())
        every { invoiceDaoV2.observeOverdueAmount(businessId) } returns flowOf(0L)

        // Act
        val result = repository.observeRevenueMetrics(businessId).first()
        val metrics = result.getOrThrow()

        // Assert
        assertEquals(0L, metrics.mtdRevenue)
        assertEquals(0L, metrics.ytdRevenue)
        assertEquals(0L, metrics.weeklyRevenue)
        assertEquals(0L, metrics.totalPaidRevenue)
        assertTrue(metrics.dailyTrend.isEmpty())
    }

    @Test
    fun `observeRevenueMetrics emits failure result when DAO throws`() = runTest {
        // Arrange
        val businessId = 3L
        val exception = RuntimeException("DB error")

        every { invoiceDaoV2.observeMTDRevenue(businessId, any(), any()) } throws exception
        every { invoiceDaoV2.observeYTDRevenue(businessId, any(), any()) } returns flowOf(0L)
        every { invoiceDaoV2.observeWeeklyRevenue(businessId, any(), any()) } returns flowOf(0L)
        every { invoiceDaoV2.observeTotalPaidRevenue(businessId) } returns flowOf(0L)
        every { invoiceDaoV2.observeLast30DaysRevenueTrend(businessId) } returns flowOf(emptyList())
        every { invoiceDaoV2.observeOverdueAmount(businessId) } returns flowOf(0L)

        // Act
        val result = repository.observeRevenueMetrics(businessId).first()

        // Assert - flow catches exceptions and wraps them as failure Results
        assertTrue(result.isFailure)
    }
}
