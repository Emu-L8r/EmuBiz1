package com.emul8r.bizap.presentation.viewmodel

import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.data.local.dao.AnalyticsDao
import com.emul8r.bizap.data.model.*
import com.emul8r.bizap.data.repository.gui2.BusinessContextRepositoryV2
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for [AnalyticsViewModel].
 *
 * Verifies that:
 * 1. The active business ID is read dynamically from [BusinessContextRepositoryV2]
 *    rather than using a hardcoded constant (Phase 3.2 – Problem 1 & 3 fix).
 * 2. Days-to-payment and invoicing-velocity flows switch correctly when the
 *    business context changes.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AnalyticsViewModelTest : BaseUnitTest() {

    private val analyticsDao: AnalyticsDao = mockk(relaxed = true)
    private val businessContextRepository: BusinessContextRepositoryV2 = mockk()

    private val activeBusinessIdFlow = MutableStateFlow(1L)

    private lateinit var viewModel: AnalyticsViewModel

    @Before
    fun setup() {
        every { businessContextRepository.observeActiveBusinessId() } returns activeBusinessIdFlow

        // Stub all DAO flows for the default businessId = 1
        stubDaoForBusiness(1L, dso = 15.0, velocity = listOf(InvoiceVelocity(1L, 0L, 2.5, 3, 2, 1, 1)))
        // Stub all DAO flows for businessId = 2
        stubDaoForBusiness(2L, dso = 30.0, velocity = listOf(InvoiceVelocity(2L, 0L, 4.0, 5, 4, 2, 1)))

        viewModel = AnalyticsViewModel(analyticsDao, businessContextRepository)
    }

    // ─── Problem 1 fix: Days-to-payment uses active business ────────────────

    @Test
    fun `averageDaysToPayment reflects active business - business 1`() = runTest {
        advanceUntilIdle()

        val dso = viewModel.averageDaysToPayment.first()
        assertEquals(15.0, dso, absoluteTolerance = 0.001,
            message = "Days-to-payment should match business 1's value (15.0)")
    }

    @Test
    fun `averageDaysToPayment updates when active business switches to business 2`() = runTest {
        advanceUntilIdle()

        // Switch the active business to ID 2
        activeBusinessIdFlow.value = 2L
        advanceUntilIdle()

        val dso = viewModel.averageDaysToPayment.first()
        assertEquals(30.0, dso, absoluteTolerance = 0.001,
            message = "Days-to-payment should switch to business 2's value (30.0)")
    }

    // ─── Problem 3 fix: Invoicing-velocity (bar graph) uses active business ─

    @Test
    fun `invoicingVelocity is non-empty for active business`() = runTest {
        advanceUntilIdle()

        val velocity = viewModel.invoicingVelocity.first()
        assertTrue(velocity.isNotEmpty(),
            "Bar graph data should not be empty when business has invoicing velocity data")
    }

    @Test
    fun `invoicingVelocity updates when active business switches`() = runTest {
        advanceUntilIdle()
        val velocityBusiness1 = viewModel.invoicingVelocity.first()
        assertEquals(2.5, velocityBusiness1.first().avgDaysFromCreationToSent, absoluteTolerance = 0.001)

        // Switch the active business to ID 2
        activeBusinessIdFlow.value = 2L
        advanceUntilIdle()

        val velocityBusiness2 = viewModel.invoicingVelocity.first()
        assertEquals(4.0, velocityBusiness2.first().avgDaysFromCreationToSent, absoluteTolerance = 0.001,
            message = "Velocity should switch to business 2's value")
    }

    // ─── Combined analytics state ────────────────────────────────────────────

    @Test
    fun `analyticsState emits Success when data is available`() = runTest {
        advanceUntilIdle()

        val state = viewModel.analyticsState.first()
        assertTrue(state is AnalyticsUiState.Success,
            "Analytics state should be Success when DAO returns data")
    }

    @Test
    fun `analyticsState currentAverageDaysToPayment matches active business`() = runTest {
        advanceUntilIdle()

        val state = viewModel.analyticsState.first()
        assertTrue(state is AnalyticsUiState.Success)
        assertEquals(15.0, (state as AnalyticsUiState.Success).data.currentAverageDaysToPayment,
            absoluteTolerance = 0.001,
            message = "Combined analytics state should carry the correct DSO for the active business")
    }

    // ─── Helper ──────────────────────────────────────────────────────────────

    private fun stubDaoForBusiness(businessId: Long, dso: Double, velocity: List<InvoiceVelocity>) {
        every { analyticsDao.observeAverageDaysToPayment(businessId) } returns MutableStateFlow(dso)
        every { analyticsDao.observeAverageDaysToPayTrend(businessId) } returns MutableStateFlow(emptyList())
        every { analyticsDao.observeInvoicingVelocity(businessId) } returns MutableStateFlow(velocity)
        every { analyticsDao.observeDailyRevenue(businessId) } returns MutableStateFlow(emptyList())
        every { analyticsDao.observeTopCustomers(businessId, 5) } returns MutableStateFlow(emptyList())
        every { analyticsDao.observeTotalRevenue(businessId) } returns MutableStateFlow(0L)
        every { analyticsDao.observeTotalOutstanding(businessId) } returns MutableStateFlow(0L)
        every { analyticsDao.observeDraftInvoiceCount(businessId) } returns MutableStateFlow(0)
        every { analyticsDao.observeOverdueInvoiceCount(businessId) } returns MutableStateFlow(0)

        // SPRINT 3 FIX: Ensure mocks return proper Flow data to match AnalyticsViewModel expectations
        // The ViewModel uses flatMapLatest to switch data based on active business ID
    }
}



