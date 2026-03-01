package com.emul8r.bizap.ui.revenue

import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.domain.revenue.model.RevenueMetrics
import com.emul8r.bizap.domain.revenue.usecase.GetRevenueMetricsUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import kotlin.test.assertTrue

// TODO: Refactor to use StandardTestDispatcher instead of delay()
// This test class has timing issues with ViewModel init in runTest
@Ignore("TODO: Refactor to use StandardTestDispatcher instead of delay()")
class RevenueDashboardViewModelTest : BaseUnitTest() {

    private val useCase: GetRevenueMetricsUseCase = mockk()
    private lateinit var viewModel: RevenueDashboardViewModel

    @Before
    fun setupViewModel() {
        // Setup default mock behavior
        coEvery { useCase(any()) } returns RevenueMetrics(
            mtdRevenue = 1000.0,
            ytdRevenue = 5000.0,
            weeklyRevenue = 500.0,
            dailyTrend = emptyList(),
            topPerformers = emptyList()
        )
    }

    @Test
    @Ignore("TODO: Fix async init timing")
    fun `when initialized should load success state`() = runTest {
        // Arrange
        val mockMetrics = RevenueMetrics(
            mtdRevenue = 1000.0,
            ytdRevenue = 5000.0,
            weeklyRevenue = 500.0,
            dailyTrend = emptyList(),
            topPerformers = emptyList()
        )
        coEvery { useCase(any()) } returns mockMetrics

        // Act
        viewModel = RevenueDashboardViewModel(useCase)
        // Allow coroutine in init to complete
        kotlinx.coroutines.delay(100)
        val state = viewModel.uiState.value

        // Assert
        assertTrue(state is RevenueDashboardUiState.Success)
    }

    @Test
    @Ignore("TODO: Fix async init timing")
    fun `when use case fails should show error state`() = runTest {
        // Arrange
        coEvery { useCase(any()) } throws Exception("Network Error")

        // Act
        viewModel = RevenueDashboardViewModel(useCase)
        // Allow coroutine in init to complete
        kotlinx.coroutines.delay(100)
        val state = viewModel.uiState.value

        // Assert
        assertTrue(state is RevenueDashboardUiState.Error)
    }
}
