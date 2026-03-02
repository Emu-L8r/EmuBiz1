package com.emul8r.bizap.ui.revenue

import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.domain.revenue.model.RevenueMetrics
import com.emul8r.bizap.domain.revenue.usecase.GetRevenueMetricsUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Before
import org.junit.Test
import kotlin.test.assertTrue

class RevenueDashboardViewModelTest : BaseUnitTest() {

    private val useCase: GetRevenueMetricsUseCase = mockk()
    private lateinit var viewModel: RevenueDashboardViewModel

    @Before
    fun setupViewModel() {
        // Setup default mock behavior
        coEvery { useCase(any()) } returns RevenueMetrics(
            mtdRevenue = 100000L,      // $1000 in cents
            ytdRevenue = 500000L,      // $5000 in cents
            weeklyRevenue = 50000L,    // $500 in cents
            dailyTrend = emptyList(),
            topPerformers = emptyList()
        )
    }

    @Test
    fun `when initialized should load success state`() = runTest {
        // Arrange
        val mockMetrics = RevenueMetrics(
            mtdRevenue = 100000L,
            ytdRevenue = 500000L,
            weeklyRevenue = 50000L,
            dailyTrend = emptyList(),
            topPerformers = emptyList()
        )
        coEvery { useCase(any()) } returns mockMetrics

        // Act
        viewModel = RevenueDashboardViewModel(useCase)
        advanceUntilIdle() // Wait for coroutine in init to complete
        val state = viewModel.uiState.value

        // Assert
        assertTrue(state is RevenueDashboardUiState.Success)
    }

    @Test
    fun `when use case fails should show error state`() = runTest {
        // Arrange
        coEvery { useCase(any()) } throws Exception("Network Error")

        // Act
        viewModel = RevenueDashboardViewModel(useCase)
        advanceUntilIdle() // Wait for coroutine in init to complete
        val state = viewModel.uiState.value

        // Assert
        assertTrue(state is RevenueDashboardUiState.Error)
    }
}
