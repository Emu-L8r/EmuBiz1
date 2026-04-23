package com.emul8r.bizap.presentation.viewmodel

import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.data.local.dao.AnalyticsDao
import com.emul8r.bizap.data.model.CustomerRevenue
import com.emul8r.bizap.data.model.DailyRevenue
import com.emul8r.bizap.data.repository.gui2.BusinessContextRepositoryV2
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for [AnalyticsViewModel].
 * Verifies that state flows emit correctly when the business context and DAO data change.
 */
class AnalyticsViewModelTest : BaseUnitTest() {

    private val analyticsDao: AnalyticsDao = mockk(relaxed = true)
    private val businessContextRepository: BusinessContextRepositoryV2 = mockk()

    private val businessId = 1L

    private lateinit var viewModel: AnalyticsViewModel

    @Before
    fun setUp() {
        every { businessContextRepository.observeActiveBusinessId() } returns flowOf(businessId)

        // Default stubs — return empty lists / zero values
        every { analyticsDao.observeDailyRevenue(businessId, any()) } returns flowOf(emptyList())
        every { analyticsDao.observeTopCustomers(businessId, any()) } returns flowOf(emptyList())
        every { analyticsDao.observeAverageDaysToPayment(businessId) } returns flowOf(0.0)
        every { analyticsDao.observeAverageDaysToPayTrend(businessId, any()) } returns flowOf(emptyList())
        every { analyticsDao.observeInvoicingVelocity(businessId, any()) } returns flowOf(emptyList())

        viewModel = AnalyticsViewModel(analyticsDao, businessContextRepository)
    }

    // ── cashFlowTrend ──────────────────────────────────────────────────────

    @Test
    fun `cashFlowTrend emits empty list when no data`() = runUnitTest {
        advanceUntilIdle()
        assertTrue(viewModel.cashFlowTrend.value.isEmpty())
    }

    @Test
    fun `cashFlowTrend maps DailyRevenue to CashFlowTrendPoint`() = runUnitTest {
        val daily = DailyRevenue(
            businessId = businessId,
            date = 1000L,
            invoicedCents = 50_000L,
            paidCents = 30_000L,
            invoiceCount = 2,
            paidCount = 1
        )
        every { analyticsDao.observeDailyRevenue(businessId, any()) } returns flowOf(listOf(daily))

        val vm = AnalyticsViewModel(analyticsDao, businessContextRepository)
        advanceUntilIdle()

        val trend = vm.cashFlowTrend.value
        assertEquals(1, trend.size)
        assertEquals(50_000L, trend[0].invoicedCents)
        assertEquals(30_000L, trend[0].paidCents)
    }

    // ── topCustomers ───────────────────────────────────────────────────────

    @Test
    fun `topCustomers emits empty list when no paid invoices`() = runUnitTest {
        advanceUntilIdle()
        assertTrue(viewModel.topCustomers.value.isEmpty())
    }

    @Test
    fun `topCustomers computes percentage of total correctly`() = runUnitTest {
        val customers = listOf(
            CustomerRevenue(
                customerId = 1L,
                customerName = "Alpha Corp",
                totalRevenueCents = 60_000L,
                invoiceCount = 3,
                lastPaymentDate = 0L
            ),
            CustomerRevenue(
                customerId = 2L,
                customerName = "Beta Ltd",
                totalRevenueCents = 40_000L,
                invoiceCount = 2,
                lastPaymentDate = 0L
            )
        )
        every { analyticsDao.observeTopCustomers(businessId, any()) } returns flowOf(customers)

        val vm = AnalyticsViewModel(analyticsDao, businessContextRepository)
        advanceUntilIdle()

        val result = vm.topCustomers.value
        assertEquals(2, result.size)
        assertEquals(60.0, result[0].percentageOfTotal, 0.01)
        assertEquals(40.0, result[1].percentageOfTotal, 0.01)
    }

    @Test
    fun `topCustomers emits empty when total revenue is zero`() = runUnitTest {
        val customers = listOf(
            CustomerRevenue(
                customerId = 1L,
                customerName = "No Revenue Co",
                totalRevenueCents = 0L,
                invoiceCount = 0,
                lastPaymentDate = 0L
            )
        )
        every { analyticsDao.observeTopCustomers(businessId, any()) } returns flowOf(customers)

        val vm = AnalyticsViewModel(analyticsDao, businessContextRepository)
        advanceUntilIdle()

        assertTrue(vm.topCustomers.value.isEmpty())
    }

    // ── averageDaysToPayment ───────────────────────────────────────────────

    @Test
    fun `averageDaysToPayment emits 0 when no paid invoices`() = runUnitTest {
        advanceUntilIdle()
        assertEquals(0.0, viewModel.averageDaysToPayment.value, 0.01)
    }

    @Test
    fun `averageDaysToPayment emits correct DSO value`() = runUnitTest {
        every { analyticsDao.observeAverageDaysToPayment(businessId) } returns flowOf(14.5)

        val vm = AnalyticsViewModel(analyticsDao, businessContextRepository)
        advanceUntilIdle()

        assertEquals(14.5, vm.averageDaysToPayment.value, 0.01)
    }

    // ── business context switching ─────────────────────────────────────────

    @Test
    fun `switching business context reloads cashFlowTrend for new business`() = runUnitTest {
        val newBusinessId = 99L
        every { businessContextRepository.observeActiveBusinessId() } returns flowOf(newBusinessId)
        every { analyticsDao.observeDailyRevenue(newBusinessId, any()) } returns flowOf(emptyList())
        every { analyticsDao.observeTopCustomers(newBusinessId, any()) } returns flowOf(emptyList())
        every { analyticsDao.observeAverageDaysToPayment(newBusinessId) } returns flowOf(0.0)
        every { analyticsDao.observeAverageDaysToPayTrend(newBusinessId, any()) } returns flowOf(emptyList())
        every { analyticsDao.observeInvoicingVelocity(newBusinessId, any()) } returns flowOf(emptyList())

        val vm = AnalyticsViewModel(analyticsDao, businessContextRepository)
        advanceUntilIdle()

        assertTrue(vm.cashFlowTrend.value.isEmpty())
    }

    // ── error resilience ───────────────────────────────────────────────────

    @Test
    fun `cashFlowTrend emits empty on DAO error`() = runUnitTest {
        every { analyticsDao.observeDailyRevenue(businessId, any()) } returns flow { throw RuntimeException("DB error") }

        val vm = AnalyticsViewModel(analyticsDao, businessContextRepository)
        advanceUntilIdle()

        // Should not throw; emits initial empty value
        assertTrue(vm.cashFlowTrend.value.isEmpty())
    }
}





