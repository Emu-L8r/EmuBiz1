@file:Suppress("UNCHECKED_CAST")
package com.emul8r.bizap.data.repository

import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.data.local.InvoiceDao
import com.emul8r.bizap.data.local.InvoiceDao.DailyRevenueTrend
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import kotlin.test.assertEquals

class RevenueRepositoryImplTest : BaseUnitTest() {

    private val invoiceDao: InvoiceDao = mockk()
    private lateinit var repository: RevenueRepositoryImpl

    @Before
    fun setup() {
        repository = RevenueRepositoryImpl(invoiceDao)
    }

    @Test
    fun `getRevenueMetrics returns correct values from direct invoice queries`() = runTest {
        // Arrange
        val businessId = 1L
        val today = LocalDate.now()
        val trend = listOf(
            DailyRevenueTrend(
                dateString = today.toString(),
                revenue = 100000L,
                invoiceCount = 2,
                paidCount = 1,
                currencyCode = "AUD"
            ),
            DailyRevenueTrend(
                dateString = today.minusDays(5).toString(),
                revenue = 50000L,
                invoiceCount = 1,
                paidCount = 1,
                currencyCode = "AUD"
            )
        )

        every { invoiceDao.observeMTDRevenue(businessId) } returns flowOf(100000L)
        every { invoiceDao.observeYTDRevenue(businessId) } returns flowOf(150000L)
        every { invoiceDao.observeWeeklyRevenue(businessId) } returns flowOf(150000L)
        every { invoiceDao.observeTotalPaidRevenue(businessId) } returns flowOf(150000L)
        every { invoiceDao.observeLast30DaysRevenueTrend(businessId) } returns flowOf(trend)

        // Act
        val metrics = repository.getRevenueMetrics(businessId)

        // Assert
        assertEquals(100000L, metrics.mtdRevenue)
        assertEquals(150000L, metrics.ytdRevenue)
        assertEquals(150000L, metrics.weeklyRevenue)
        assertEquals(150000L, metrics.totalPaidRevenue)
        assertEquals(2, metrics.dailyTrend.size)
    }

    @Test
    fun `getRevenueMetrics returns zeros when no invoices exist`() = runTest {
        // Arrange
        val businessId = 2L

        every { invoiceDao.observeMTDRevenue(businessId) } returns flowOf(0L)
        every { invoiceDao.observeYTDRevenue(businessId) } returns flowOf(0L)
        every { invoiceDao.observeWeeklyRevenue(businessId) } returns flowOf(0L)
        every { invoiceDao.observeTotalPaidRevenue(businessId) } returns flowOf(0L)
        every { invoiceDao.observeLast30DaysRevenueTrend(businessId) } returns flowOf(emptyList())

        // Act
        val metrics = repository.getRevenueMetrics(businessId)

        // Assert
        assertEquals(0L, metrics.mtdRevenue)
        assertEquals(0L, metrics.ytdRevenue)
        assertEquals(0L, metrics.weeklyRevenue)
        assertEquals(0L, metrics.totalPaidRevenue)
        assertEquals(emptyList(), metrics.dailyTrend)
        assertEquals(emptyList(), metrics.topPerformers)
    }

    @Test
    fun `calculateByCurrency groups revenue correctly by currency`() = runTest {
        // Arrange
        val businessId = 3L
        val today = LocalDate.now()
        val trend = listOf(
            DailyRevenueTrend(
                dateString = today.toString(),
                revenue = 60000L,
                invoiceCount = 1,
                paidCount = 1,
                currencyCode = "AUD"
            ),
            DailyRevenueTrend(
                dateString = today.toString(),
                revenue = 40000L,
                invoiceCount = 1,
                paidCount = 1,
                currencyCode = "USD"
            )
        )

        every { invoiceDao.observeMTDRevenue(businessId) } returns flowOf(100000L)
        every { invoiceDao.observeYTDRevenue(businessId) } returns flowOf(100000L)
        every { invoiceDao.observeWeeklyRevenue(businessId) } returns flowOf(100000L)
        every { invoiceDao.observeTotalPaidRevenue(businessId) } returns flowOf(100000L)
        every { invoiceDao.observeLast30DaysRevenueTrend(businessId) } returns flowOf(trend)

        // Act
        val metrics = repository.getRevenueMetrics(businessId)

        // Assert - currencies should be sorted by amount descending
        assertEquals(2, metrics.topPerformers.size)
        assertEquals("AUD", metrics.topPerformers[0].currencyCode)
        assertEquals(60000L, metrics.topPerformers[0].totalAmount)
        assertEquals(60.0, metrics.topPerformers[0].percentageOfTotal, 0.01)
        assertEquals("USD", metrics.topPerformers[1].currencyCode)
        assertEquals(40000L, metrics.topPerformers[1].totalAmount)
        assertEquals(40.0, metrics.topPerformers[1].percentageOfTotal, 0.01)
    }
}
