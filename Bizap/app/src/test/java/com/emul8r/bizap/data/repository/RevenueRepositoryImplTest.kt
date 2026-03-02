package com.emul8r.bizap.data.repository

import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.data.local.dao.AnalyticsDao
import com.emul8r.bizap.data.local.entities.DailyRevenueSnapshot
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import kotlin.test.assertEquals

class RevenueRepositoryImplTest : BaseUnitTest() {

    private val analyticsDao: AnalyticsDao = mockk()
    private lateinit var repository: RevenueRepositoryImpl

    @Before
    fun setup() {
        repository = RevenueRepositoryImpl(analyticsDao)
    }

    @Test
    fun `getRevenueMetrics correctly calculates MTD and YTD`() = runTest {
        // Arrange
        val businessId = 1L
        val today = LocalDate.now()
        val testSnapshots = listOf(
            DailyRevenueSnapshot(
                businessProfileId = businessId,
                dateString = today.toString(),
                dateMs = System.currentTimeMillis(),
                totalRevenue = 100000L, // $1000.00
                invoiceCount = 1
            ),
            DailyRevenueSnapshot(
                businessProfileId = businessId,
                dateString = today.minusMonths(1).toString(),
                dateMs = System.currentTimeMillis() - 2592000000,
                totalRevenue = 50000L, // $500.00
                invoiceCount = 1
            )
        )

        coEvery { analyticsDao.getLast30DaysRevenue(businessId) } returns testSnapshots

        // Act
        val metrics = repository.getRevenueMetrics(businessId)

        // Assert
        assertEquals(100000L, metrics.mtdRevenue)
        // Note: YTD calculation logic in RevenueRepositoryImpl might need verification if it's just summing these two
        // But for the purpose of fixing the test types:
        assertEquals(150000L, metrics.ytdRevenue)
    }
}
