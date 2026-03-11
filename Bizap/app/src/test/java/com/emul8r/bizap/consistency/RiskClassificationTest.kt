@file:Suppress("UNCHECKED_CAST")
package com.emul8r.bizap.consistency

import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.data.local.dao.InvoiceDaoV2
import com.emul8r.bizap.data.repository.analytics.AnalyticsCalculator
import com.emul8r.bizap.data.repository.gui2.RiskAnalyticsRepositoryV2
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests verifying that invoice risk tier classification is computed correctly
 * from the invoices table (not from stale snapshots).
 *
 * Risk tiers:
 * - **High Risk**: OVERDUE > 60 days
 * - **At Risk**: OVERDUE 30–59 days
 * - **Healthy**: Paid or not yet due
 *
 * All calculations use [RiskAnalyticsRepositoryV2] which queries directly
 * from the `invoices` table via [InvoiceDaoV2].
 */
class RiskClassificationTest : BaseUnitTest() {

    private val dao: InvoiceDaoV2 = mockk()
    private lateinit var riskRepo: RiskAnalyticsRepositoryV2

    private val businessId = 1L

    @Before
    fun setup() {
        riskRepo = RiskAnalyticsRepositoryV2(dao, AnalyticsCalculator())
    }

    // ── healthy tier ─────────────────────────────────────────────────────────

    @Test
    fun `healthy_Tier - paid invoices are classified as healthy`() = runTest {
        every { dao.observeHighRiskInvoiceCount(businessId) } returns flowOf(0)
        every { dao.observeAtRiskInvoiceCount(businessId) } returns flowOf(0)
        every { dao.observeHealthyInvoiceCount(businessId) } returns flowOf(10)
        every { dao.observeOverdueCount(businessId) } returns flowOf(0)
        every { dao.observeOutstandingAmount(businessId) } returns flowOf(0L)

        val metrics = riskRepo.observeRiskMetrics(businessId).first()

        assertEquals(10, metrics.healthyCount)
        assertEquals(0, metrics.overdueCount)
        assertEquals(0, metrics.highRiskCount)
        assertEquals(0, metrics.atRiskCount)
    }

    @Test
    fun `healthy_Tier - current invoices (not yet due) are healthy`() = runTest {
        every { dao.observeHighRiskInvoiceCount(businessId) } returns flowOf(0)
        every { dao.observeAtRiskInvoiceCount(businessId) } returns flowOf(0)
        every { dao.observeHealthyInvoiceCount(businessId) } returns flowOf(5)
        every { dao.observeOverdueCount(businessId) } returns flowOf(0)
        every { dao.observeOutstandingAmount(businessId) } returns flowOf(500000L)

        val metrics = riskRepo.observeRiskMetrics(businessId).first()

        assertEquals(5, metrics.healthyCount)
        assertEquals(0, metrics.overdueCount)
    }

    // ── at-risk tier ─────────────────────────────────────────────────────────

    @Test
    fun `atRisk_Tier - invoices overdue 30-59 days are at-risk`() = runTest {
        every { dao.observeHighRiskInvoiceCount(businessId) } returns flowOf(0)
        every { dao.observeAtRiskInvoiceCount(businessId) } returns flowOf(3)
        every { dao.observeHealthyInvoiceCount(businessId) } returns flowOf(7)
        every { dao.observeOverdueCount(businessId) } returns flowOf(3)
        every { dao.observeOutstandingAmount(businessId) } returns flowOf(300000L)

        val metrics = riskRepo.observeRiskMetrics(businessId).first()

        assertEquals(3, metrics.atRiskCount)
        assertEquals(0, metrics.highRiskCount)
        assertEquals(7, metrics.healthyCount)
    }

    // ── high-risk tier ───────────────────────────────────────────────────────

    @Test
    fun `highRisk_Tier - invoices overdue 60+ days are high risk`() = runTest {
        every { dao.observeHighRiskInvoiceCount(businessId) } returns flowOf(2)
        every { dao.observeAtRiskInvoiceCount(businessId) } returns flowOf(1)
        every { dao.observeHealthyInvoiceCount(businessId) } returns flowOf(5)
        every { dao.observeOverdueCount(businessId) } returns flowOf(3)
        every { dao.observeOutstandingAmount(businessId) } returns flowOf(600000L)

        val metrics = riskRepo.observeRiskMetrics(businessId).first()

        assertEquals(2, metrics.highRiskCount)
        assertEquals(1, metrics.atRiskCount)
        assertEquals(3, metrics.overdueCount)
    }

    // ── overdue count consistency ────────────────────────────────────────────

    @Test
    fun `overdueCount_Consistency - high plus at-risk equals total overdue`() = runTest {
        val highRisk = 4
        val atRisk = 6
        val totalOverdue = highRisk + atRisk

        every { dao.observeHighRiskInvoiceCount(businessId) } returns flowOf(highRisk)
        every { dao.observeAtRiskInvoiceCount(businessId) } returns flowOf(atRisk)
        every { dao.observeHealthyInvoiceCount(businessId) } returns flowOf(20)
        every { dao.observeOverdueCount(businessId) } returns flowOf(totalOverdue)
        every { dao.observeOutstandingAmount(businessId) } returns flowOf(1000000L)

        val metrics = riskRepo.observeRiskMetrics(businessId).first()

        assertEquals(totalOverdue, metrics.highRiskCount + metrics.atRiskCount)
        assertEquals(totalOverdue, metrics.overdueCount)
    }

    // ── zero risk ────────────────────────────────────────────────────────────

    @Test
    fun `zeroRisk_NewBusiness - brand new business has no risk`() = runTest {
        every { dao.observeHighRiskInvoiceCount(businessId) } returns flowOf(0)
        every { dao.observeAtRiskInvoiceCount(businessId) } returns flowOf(0)
        every { dao.observeHealthyInvoiceCount(businessId) } returns flowOf(0)
        every { dao.observeOverdueCount(businessId) } returns flowOf(0)
        every { dao.observeOutstandingAmount(businessId) } returns flowOf(0L)

        val metrics = riskRepo.observeRiskMetrics(businessId).first()

        assertEquals(0, metrics.highRiskCount)
        assertEquals(0, metrics.atRiskCount)
        assertEquals(0, metrics.healthyCount)
        assertEquals(0, metrics.overdueCount)
        assertEquals(0L, metrics.totalOutstandingCents)
    }

    // ── outstanding amount ───────────────────────────────────────────────────

    @Test
    fun `outstanding_Tracks_Risk - risk dashboard shows correct outstanding amount`() = runTest {
        val expectedOutstanding = 850000L  // $8,500 at risk

        every { dao.observeHighRiskInvoiceCount(businessId) } returns flowOf(3)
        every { dao.observeAtRiskInvoiceCount(businessId) } returns flowOf(2)
        every { dao.observeHealthyInvoiceCount(businessId) } returns flowOf(10)
        every { dao.observeOverdueCount(businessId) } returns flowOf(5)
        every { dao.observeOutstandingAmount(businessId) } returns flowOf(expectedOutstanding)

        val metrics = riskRepo.observeRiskMetrics(businessId).first()

        assertEquals(expectedOutstanding, metrics.totalOutstandingCents)
        assertTrue(metrics.totalOutstandingCents > 0)
    }

    // ── business isolation ───────────────────────────────────────────────────

    @Test
    fun `businessIsolation - different business IDs return different metrics`() = runTest {
        val businessId2 = 2L

        // Business 1: high risk
        every { dao.observeHighRiskInvoiceCount(businessId) } returns flowOf(5)
        every { dao.observeAtRiskInvoiceCount(businessId) } returns flowOf(3)
        every { dao.observeHealthyInvoiceCount(businessId) } returns flowOf(2)
        every { dao.observeOverdueCount(businessId) } returns flowOf(8)
        every { dao.observeOutstandingAmount(businessId) } returns flowOf(800000L)

        // Business 2: healthy
        every { dao.observeHighRiskInvoiceCount(businessId2) } returns flowOf(0)
        every { dao.observeAtRiskInvoiceCount(businessId2) } returns flowOf(0)
        every { dao.observeHealthyInvoiceCount(businessId2) } returns flowOf(15)
        every { dao.observeOverdueCount(businessId2) } returns flowOf(0)
        every { dao.observeOutstandingAmount(businessId2) } returns flowOf(0L)

        val risk1 = riskRepo.observeRiskMetrics(businessId).first()
        val risk2 = riskRepo.observeRiskMetrics(businessId2).first()

        assertTrue(risk1.highRiskCount > risk2.highRiskCount)
        assertTrue(risk2.healthyCount > risk1.healthyCount)
    }
}
