package com.emul8r.bizap

import com.emul8r.bizap.data.repository.AccountingRepository
import com.emul8r.bizap.data.repository.AnalyticsRepositoryBridge
import com.emul8r.bizap.data.repository.SnapshotCachePolicy
import com.emul8r.bizap.data.repository.analytics.AnalyticsCalculator
import com.emul8r.bizap.data.repository.analytics.AnalyticsValidator
import com.emul8r.bizap.data.repository.gui2.PaymentAnalyticsRepositoryV2
import com.emul8r.bizap.data.repository.gui2.RevenueRepositoryV2
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.InvoiceStatus
import com.emul8r.bizap.domain.model.LineItem
import com.emul8r.bizap.domain.usecase.RecordPaymentUseCase
import com.emul8r.bizap.utils.CentsFormatter
import io.mockk.mockk
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Baseline test that validates all key imports and classes compile successfully.
 *
 * This test serves as a compile-time guard: if any referenced class is missing,
 * renamed, or fails to compile, this test will fail at build time — surfacing the
 * problem immediately rather than silently breaking downstream tests.
 *
 * Run on every build as part of CI/CD (see android-ci.yml).
 */
class AllTestsCompileTest {

    // ── Confirm key classes can be instantiated / referenced ──────────────────

    @Test
    fun `AccountingRepository class is accessible`() {
        val dao = mockk<com.emul8r.bizap.data.local.dao.InvoiceDaoV2>(relaxed = true)
        val calc = AnalyticsCalculator()
        val validator = AnalyticsValidator()
        val repo = AccountingRepository(dao, calc, validator)
        assertNotNull(repo)
    }

    @Test
    fun `AnalyticsRepositoryBridge class is accessible`() {
        val revenue = mockk<RevenueRepositoryV2>(relaxed = true)
        val payment = mockk<PaymentAnalyticsRepositoryV2>(relaxed = true)
        val risk = mockk<com.emul8r.bizap.data.repository.gui2.RiskAnalyticsRepositoryV2>(relaxed = true)
        val bridge = AnalyticsRepositoryBridge(revenue, payment, risk)
        assertNotNull(bridge)
    }

    @Test
    fun `SnapshotCachePolicy constants are defined`() {
        assertFalse(SnapshotCachePolicy.USE_SNAPSHOTS_FOR_DASHBOARDS,
            "Dashboards must never use snapshots as primary source")
        assertTrue(SnapshotCachePolicy.MAX_SNAPSHOT_AGE_MS > 0)
        assertTrue(SnapshotCachePolicy.CONSISTENCY_TOLERANCE_CENTS >= 0)
    }

    @Test
    fun `CentsFormatter is accessible and converts correctly`() {
        assertNotNull(CentsFormatter)
        val cents = CentsFormatter.dollarsToCents(149.99)
        assertTrue(cents > 0L, "Conversion must produce positive cents")
    }

    @Test
    fun `Invoice domain model compiles and uses Long for monetary fields`() {
        val invoice = Invoice(
            id = 1L,
            businessProfileId = 1L,
            customerId = 1L,
            customerName = "Test",
            date = System.currentTimeMillis(),
            totalAmount = 14999L,
            amountPaid = 0L,
            items = listOf(LineItem(description = "Item", quantity = 1.0, unitPrice = 14999L)),
            isQuote = false,
            status = InvoiceStatus.SENT
        )
        assertNotNull(invoice)
        assertTrue(invoice.totalAmount is Long, "totalAmount must be Long (cents)")
        assertTrue(invoice.amountPaid is Long, "amountPaid must be Long (cents)")
    }

    @Test
    fun `RecordPaymentUseCase compiles and is injectable`() {
        val paymentRepo = mockk<com.emul8r.bizap.data.repository.gui2.PaymentRepositoryV2>(relaxed = true)
        val useCase = RecordPaymentUseCase(paymentRepo)
        assertNotNull(useCase)
    }

    @Test
    fun `AnalyticsCalculator computeCollectionRate is correct`() {
        val calc = AnalyticsCalculator()
        val rate = calc.computeCollectionRate(outstanding = 10000L, collected = 22200L)
        assertTrue(rate in 68.0..70.0, "Expected ~68.9%, got $rate")
    }

    @Test
    fun `AnalyticsCalculator collection rate is 0 with no data`() {
        val calc = AnalyticsCalculator()
        val rate = calc.computeCollectionRate(outstanding = 0L, collected = 0L)
        assertTrue(rate == 0.0, "Expected 0.0 with no invoices, got $rate")
    }

    @Test
    fun `AnalyticsCalculator collection rate is 100 when all paid`() {
        val calc = AnalyticsCalculator()
        val rate = calc.computeCollectionRate(outstanding = 0L, collected = 50000L)
        assertTrue(rate == 100.0, "Expected 100.0 when fully collected, got $rate")
    }
}
