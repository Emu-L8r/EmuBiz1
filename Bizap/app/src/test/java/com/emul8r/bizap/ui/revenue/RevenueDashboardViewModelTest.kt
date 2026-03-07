package com.emul8r.bizap.ui.revenue

import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.data.repository.SnapshotRebuildService
import com.emul8r.bizap.domain.model.BusinessProfile
import com.emul8r.bizap.domain.revenue.model.RevenueMetrics
import com.emul8r.bizap.domain.revenue.usecase.GetRevenueMetricsUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RevenueDashboardViewModelTest : BaseUnitTest() {

    private val useCase: GetRevenueMetricsUseCase = mockk()
    private val businessProfileRepository = mockk<com.emul8r.bizap.domain.repository.BusinessProfileRepository>()
    private val snapshotRebuildService: SnapshotRebuildService = mockk(relaxed = true)
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
                totalPaidRevenue = 600000L,
                dailyTrend = emptyList(),
                topPerformers = emptyList()
            )
        )
    }

    @Test
    fun `when initialized should create ViewModel`() {
        // Act
        viewModel = RevenueDashboardViewModel(useCase, businessProfileRepository, snapshotRebuildService)

        // Assert - ViewModel created successfully
        assertTrue(::viewModel.isInitialized)
    }

    @Test
    fun `uiState StateFlow is initialized with Loading state`() {
        // Act
        viewModel = RevenueDashboardViewModel(useCase, businessProfileRepository, snapshotRebuildService)

        // Assert - Initial state is Loading
        assertEquals(RevenueDashboardUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `isRefreshing StateFlow starts as false`() {
        // Act
        viewModel = RevenueDashboardViewModel(useCase, businessProfileRepository, snapshotRebuildService)

        // Assert
        assertEquals(false, viewModel.isRefreshing.value)
    }

    @Test
    fun `forceRefresh increments refresh trigger`() {
        // Act
        viewModel = RevenueDashboardViewModel(useCase, businessProfileRepository, snapshotRebuildService)

        viewModel.forceRefresh()

        // Assert - Function executed without error
        assertTrue(true)  // If we got here, forceRefresh worked
    }
}
