package com.emul8r.bizap.ui.revenue

import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.domain.model.BusinessProfile
import com.emul8r.bizap.domain.revenue.model.RevenueMetrics
import com.emul8r.bizap.domain.revenue.usecase.GetRevenueMetricsUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Before
import org.junit.Test
import kotlin.test.assertTrue

class RevenueDashboardViewModelTest : BaseUnitTest() {

    private val useCase: GetRevenueMetricsUseCase = mockk()
    private val businessProfileRepository = mockk<com.emul8r.bizap.domain.repository.BusinessProfileRepository>()
    private lateinit var viewModel: RevenueDashboardViewModel

    @Before
    fun setupViewModel() {
        val mockProfile = BusinessProfile(id = 1L)
        every { businessProfileRepository.activeProfile } returns flowOf(mockProfile)
        every { useCase(any()) } returns flowOf(
            RevenueMetrics(
                mtdRevenue = 100000L,
                ytdRevenue = 500000L,
                weeklyRevenue = 50000L,
                dailyTrend = emptyList(),
                topPerformers = emptyList()
            )
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
        every { useCase(any()) } returns flowOf(mockMetrics)

        // Act
        viewModel = RevenueDashboardViewModel(useCase, businessProfileRepository)
        advanceUntilIdle() // Wait for StateFlow to emit
        val state = viewModel.uiState.value

        // Assert
        assertTrue(state is RevenueDashboardUiState.Success)
    }

    @Test
    fun `when use case fails should show error state`() = runTest {
        // Arrange
        every { useCase(any()) } returns flow { throw Exception("Network Error") }

        // Act
        viewModel = RevenueDashboardViewModel(useCase, businessProfileRepository)
        advanceUntilIdle() // Wait for StateFlow to emit
        val state = viewModel.uiState.value

        // Assert
        assertTrue(state is RevenueDashboardUiState.Error)
    }
}
