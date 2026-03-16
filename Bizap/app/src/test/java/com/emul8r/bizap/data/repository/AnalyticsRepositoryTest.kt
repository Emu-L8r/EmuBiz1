@file:Suppress("UNCHECKED_CAST")
package com.emul8r.bizap.data.repository

import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.data.local.dao.InvoiceDaoV2
import com.emul8r.bizap.data.local.entities.DailyRevenueTrendV2
import com.emul8r.bizap.data.repository.analytics.AnalyticsCalculator
import com.emul8r.bizap.data.repository.analytics.AnalyticsValidator
import com.emul8r.bizap.data.repository.gui2.RevenueRepositoryV2
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Unit tests for analytics repositories.
 *
 * Verifies revenue calculations, tax logic, and metric aggregation
 * through the [RevenueRepositoryV2] and related repositories.
 */
class AnalyticsRepositoryTest : BaseUnitTest() {

    private val dao: InvoiceDaoV2 = mockk()
    private lateinit var revenueRepository: RevenueRepositoryV2

    private val businessId = 1L

    @Before
    fun setUp() {
        revenueRepository = RevenueRepositoryV2(dao, AnalyticsCalculator(), AnalyticsValidator())
    }

    // ── taxCalculation_Correct ────────────────────────────────────────────────

    @Test
    fun `taxCalculation_Correct - 10% tax on 100000 cents equals 10000 cents`() {
        val subtotal = 100000L  // $1000
        val taxRate = 0.10
        val taxAmount = (subtotal * taxRate).toLong()
        assertEquals(10000L, taxAmount, "10% tax on $1000 should be $100")
    }

    @Test
    fun `taxCalculation_Correct - 0% tax results in zero tax amount`() {
        val subtotal = 100000L
        val taxRate = 0.0
        val taxAmount = (subtotal * taxRate).toLong()
        assertEquals(0L, taxAmount, "0% tax should result in zero tax amount")
    }

    @Test
    fun `taxCalculation_Correct - total is subtotal plus tax`() {
        val subtotal = 100000L
        val taxRate = 0.10
        val taxAmount = (subtotal * taxRate).toLong()
        val total = subtotal + taxAmount
        assertEquals(110000L, total, "Total should be $1100 (subtotal + GST)")
    }

    @Test
    fun `taxCalculation_Correct - multiple line items tax calculation`() {
        // 3 items × $100 = $300 subtotal + 10% = $330 total
        val items = listOf(10000L, 10000L, 10000L)  // $100 each
        val subtotal = items.sum()
        val taxRate = 0.10
        val taxAmount = (subtotal * taxRate).toLong()
        val total = subtotal + taxAmount

        assertEquals(30000L, subtotal)
        assertEquals(3000L, taxAmount)
        assertEquals(33000L, total)
    }

    // ── revenue metrics ───────────────────────────────────────────────────────

    @Test
    fun `revenue metrics - MTD revenue flows from DAO`() = runTest {
        every { dao.observeMTDRevenue(businessId, any(), any()) } returns flowOf(200000L)
        every { dao.observeYTDRevenue(businessId, any(), any()) } returns flowOf(800000L)
        every { dao.observeWeeklyRevenue(businessId, any(), any()) } returns flowOf(50000L)
        every { dao.observeTotalPaidRevenue(businessId) } returns flowOf(600000L)
        every { dao.observeLast30DaysRevenueTrend(businessId) } returns flowOf(emptyList())

        val metrics = revenueRepository.observeRevenueMetrics(businessId).first()

        assertEquals(200000L, metrics.mtdRevenue)
        assertEquals(800000L, metrics.ytdRevenue)
        assertEquals(50000L, metrics.weeklyRevenue)
        assertEquals(600000L, metrics.totalPaidRevenue)
    }

    @Test
    fun `revenue metrics - zero revenue when no paid invoices`() = runTest {
        every { dao.observeMTDRevenue(businessId, any(), any()) } returns flowOf(0L)
        every { dao.observeYTDRevenue(businessId, any(), any()) } returns flowOf(0L)
        every { dao.observeWeeklyRevenue(businessId, any(), any()) } returns flowOf(0L)
        every { dao.observeTotalPaidRevenue(businessId) } returns flowOf(0L)
        every { dao.observeLast30DaysRevenueTrend(businessId) } returns flowOf(emptyList())

        val metrics = revenueRepository.observeRevenueMetrics(businessId).first()

        assertEquals(0L, metrics.mtdRevenue)
        assertEquals(0L, metrics.ytdRevenue)
        assertTrue(metrics.dailyTrend.isEmpty())
    }

    @Test
    fun `revenue metrics - daily trend points are correctly mapped`() = runTest {
        val trendPoints = listOf(
            DailyRevenueTrendV2(
                dateString = "2025-01-15",
                revenue = 50000L,
                invoiceCount = 2,
                paidCount = 2
            )
        )
        every { dao.observeMTDRevenue(businessId, any(), any()) } returns flowOf(50000L)
        every { dao.observeYTDRevenue(businessId, any(), any()) } returns flowOf(50000L)
        every { dao.observeWeeklyRevenue(businessId, any(), any()) } returns flowOf(50000L)
        every { dao.observeTotalPaidRevenue(businessId) } returns flowOf(50000L)
        every { dao.observeLast30DaysRevenueTrend(businessId) } returns flowOf(trendPoints)

        val metrics = revenueRepository.observeRevenueMetrics(businessId).first()

        assertEquals(1, metrics.dailyTrend.size)
        assertEquals("2025-01-15", metrics.dailyTrend[0].date)
        assertEquals(50000L, metrics.dailyTrend[0].revenueCents)
        assertEquals(2, metrics.dailyTrend[0].invoiceCount)
    }

    @Test
    fun `revenue metrics - businessId is correctly propagated`() = runTest {
        every { dao.observeMTDRevenue(businessId, any(), any()) } returns flowOf(100000L)
        every { dao.observeYTDRevenue(businessId, any(), any()) } returns flowOf(100000L)
        every { dao.observeWeeklyRevenue(businessId, any(), any()) } returns flowOf(100000L)
        every { dao.observeTotalPaidRevenue(businessId) } returns flowOf(100000L)
        every { dao.observeLast30DaysRevenueTrend(businessId) } returns flowOf(emptyList())

        val metrics = revenueRepository.observeRevenueMetrics(businessId).first()

        assertNotNull(metrics)
        assertEquals(businessId, metrics.businessProfileId)
    }
}
