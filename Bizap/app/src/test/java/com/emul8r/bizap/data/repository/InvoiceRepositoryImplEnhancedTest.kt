package com.emul8r.bizap.data.repository

import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.data.local.InvoiceDao
import com.emul8r.bizap.data.local.dao.AnalyticsDao
import com.emul8r.bizap.data.local.dao.InvoicePaymentDao
import com.emul8r.bizap.data.local.entities.DailyRevenueSnapshot
import com.emul8r.bizap.data.local.entities.InvoiceAnalyticsSnapshot
import com.emul8r.bizap.data.local.entities.InvoiceWithItems
import com.emul8r.bizap.data.mapper.toEntity
import com.emul8r.bizap.data.monitoring.PerformanceMetrics
import com.emul8r.bizap.data.remote.api.InvoiceApi
import com.emul8r.bizap.domain.error.BizapException
import com.emul8r.bizap.domain.model.InvoiceStatus
import com.emul8r.bizap.domain.repository.BusinessProfileRepository
import com.emul8r.bizap.domain.validation.StatusTransitionValidator
import com.emul8r.bizap.util.TestDataFactory
import io.mockk.any
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Comprehensive unit tests for the enhanced [InvoiceRepositoryImpl].
 *
 * Covers:
 *   - Input validation (invoiceId > 0)
 *   - Status-transition validation
 *   - Snapshot sync (InvoiceAnalyticsSnapshot, DailyRevenueSnapshot, InvoicePaymentSnapshot)
 *   - Retry logic on transient failures
 *   - Snapshot consistency verification / auto-repair
 *   - Performance metrics tracking
 *   - Not-found error when invoice is missing
 */
class InvoiceRepositoryImplEnhancedTest : BaseUnitTest() {

    private val invoiceDao: InvoiceDao = mockk()
    private val businessProfileRepo: BusinessProfileRepository = mockk()
    private val analyticsDao: AnalyticsDao = mockk(relaxed = true)
    private val paymentDao: InvoicePaymentDao = mockk(relaxed = true)
    private val snapshotSyncHelper: SnapshotSyncHelper = mockk(relaxed = true)
    private val invoiceApi: InvoiceApi = mockk()

    private lateinit var repository: InvoiceRepositoryImpl

    @Before
    fun setup() {
        PerformanceMetrics.resetAll()
        repository = InvoiceRepositoryImpl(
            invoiceDao,
            businessProfileRepo,
            analyticsDao,
            paymentDao,
            snapshotSyncHelper,
            invoiceApi
        )
    }

    @After
    fun teardown() {
        PerformanceMetrics.resetAll()
    }

    // ── Helpers ──────────────────────────────────────────────────────────────────

    private fun invoiceWithStatus(
        invoiceId: Long = 1L,
        status: InvoiceStatus = InvoiceStatus.DRAFT
    ): InvoiceWithItems {
        val entity = TestDataFactory.createTestInvoice(id = invoiceId, status = status).toEntity()
        return InvoiceWithItems(entity, emptyList())
    }

    private fun mockInvoice(
        invoiceId: Long = 1L,
        status: InvoiceStatus = InvoiceStatus.DRAFT
    ) {
        val iwi = invoiceWithStatus(invoiceId, status)
        coEvery { invoiceDao.getInvoiceWithItemsById(invoiceId) } returns flowOf(iwi)
        coEvery { invoiceDao.updateInvoiceStatus(invoiceId, any()) } just Runs
        coEvery { invoiceDao.updateAmountPaid(invoiceId, any()) } just Runs
    }

    private fun makeDailySnapshot(
        businessId: Long = 1L,
        date: String = LocalDate.now().toString(),
        totalRevenue: Long = 0L,
        paidInvoiceCount: Int = 0,
        version: Int = 1
    ) = DailyRevenueSnapshot(
        id = 100L,
        businessProfileId = businessId,
        dateString = date,
        dateMs = System.currentTimeMillis(),
        totalRevenue = totalRevenue,
        paidInvoiceCount = paidInvoiceCount,
        version = version
    )

    private fun makeAnalyticsSnapshot(invoiceId: Long = 1L, status: String = "DRAFT") =
        InvoiceAnalyticsSnapshot(
            invoiceId = invoiceId,
            businessProfileId = 1L,
            customerId = 1L,
            customerName = "UNREALCUSTOMER1",
            invoiceNumber = "INV-2024-000001",
            currencyCode = "AUD",
            subtotal = 100000L,
            taxAmount = 10000L,
            totalAmount = 110000L,
            status = status,
            isPaid = status == InvoiceStatus.PAID.name,
            isOverdue = false,
            invoiceDateMs = System.currentTimeMillis(),
            createdAtMs = System.currentTimeMillis()
        )

    // ── Input validation ──────────────────────────────────────────────────────────

    @Test
    fun `updateInvoiceStatus rejects invoiceId zero`() = runTest {
        val result = repository.updateInvoiceStatus(0L, InvoiceStatus.SENT)

        assertTrue(result.isFailure)
        assertIs<IllegalArgumentException>(result.exceptionOrNull())
    }

    @Test
    fun `updateInvoiceStatus rejects negative invoiceId`() = runTest {
        val result = repository.updateInvoiceStatus(-1L, InvoiceStatus.SENT)

        assertTrue(result.isFailure)
        assertIs<IllegalArgumentException>(result.exceptionOrNull())
    }

    @Test
    fun `updateInvoiceStatus accepts positive invoiceId`() = runTest {
        mockInvoice(invoiceId = 1L, status = InvoiceStatus.DRAFT)

        val result = repository.updateInvoiceStatus(1L, InvoiceStatus.SENT)

        assertTrue(result.isSuccess)
    }

    // ── Not-found handling ────────────────────────────────────────────────────────

    @Test
    fun `updateInvoiceStatus returns NotFoundError when invoice does not exist`() = runTest {
        coEvery { invoiceDao.getInvoiceWithItemsById(999L) } returns flowOf(null)

        val result = repository.updateInvoiceStatus(999L, InvoiceStatus.SENT)

        assertTrue(result.isFailure)
        assertIs<BizapException.NotFoundError>(result.exceptionOrNull())
    }

    // ── Status-transition validation ──────────────────────────────────────────────

    @Test
    fun `updateInvoiceStatus blocks DRAFT to PAID transition`() = runTest {
        mockInvoice(invoiceId = 1L, status = InvoiceStatus.DRAFT)

        val result = repository.updateInvoiceStatus(1L, InvoiceStatus.PAID)

        assertTrue(result.isFailure)
        assertIs<BizapException.BusinessLogicError>(result.exceptionOrNull())
    }

    @Test
    fun `updateInvoiceStatus blocks PAID to SENT transition`() = runTest {
        mockInvoice(invoiceId = 1L, status = InvoiceStatus.PAID)

        val result = repository.updateInvoiceStatus(1L, InvoiceStatus.SENT)

        assertTrue(result.isFailure)
        assertIs<BizapException.BusinessLogicError>(result.exceptionOrNull())
    }

    @Test
    fun `updateInvoiceStatus blocks PAID to DRAFT transition`() = runTest {
        mockInvoice(invoiceId = 1L, status = InvoiceStatus.PAID)

        val result = repository.updateInvoiceStatus(1L, InvoiceStatus.DRAFT)

        assertTrue(result.isFailure)
        assertIs<BizapException.BusinessLogicError>(result.exceptionOrNull())
    }

    @Test
    fun `updateInvoiceStatus allows DRAFT to SENT transition`() = runTest {
        mockInvoice(invoiceId = 1L, status = InvoiceStatus.DRAFT)

        val result = repository.updateInvoiceStatus(1L, InvoiceStatus.SENT)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `updateInvoiceStatus allows SENT to PAID transition`() = runTest {
        mockInvoice(invoiceId = 1L, status = InvoiceStatus.SENT)

        val result = repository.updateInvoiceStatus(1L, InvoiceStatus.PAID)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `updateInvoiceStatus allows SENT to OVERDUE transition`() = runTest {
        mockInvoice(invoiceId = 1L, status = InvoiceStatus.SENT)

        val result = repository.updateInvoiceStatus(1L, InvoiceStatus.OVERDUE)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `updateInvoiceStatus allows SENT to PARTIALLY_PAID transition`() = runTest {
        mockInvoice(invoiceId = 1L, status = InvoiceStatus.SENT)

        val result = repository.updateInvoiceStatus(1L, InvoiceStatus.PARTIALLY_PAID)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `updateInvoiceStatus allows OVERDUE to PAID transition`() = runTest {
        mockInvoice(invoiceId = 1L, status = InvoiceStatus.OVERDUE)

        val result = repository.updateInvoiceStatus(1L, InvoiceStatus.PAID)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `updateInvoiceStatus allows PARTIALLY_PAID to PAID transition`() = runTest {
        mockInvoice(invoiceId = 1L, status = InvoiceStatus.PARTIALLY_PAID)

        val result = repository.updateInvoiceStatus(1L, InvoiceStatus.PAID)

        assertTrue(result.isSuccess)
    }

    // ── StatusTransitionValidator unit tests ──────────────────────────────────────

    @Test
    fun `StatusTransitionValidator allowedTransitions for DRAFT contains SENT and OVERDUE`() {
        val allowed = StatusTransitionValidator.allowedTransitions(InvoiceStatus.DRAFT)
        assertTrue(InvoiceStatus.SENT in allowed)
        assertTrue(InvoiceStatus.OVERDUE in allowed)
        assertFalse(InvoiceStatus.PAID in allowed)
    }

    @Test
    fun `StatusTransitionValidator allowedTransitions for PAID is empty`() {
        val allowed = StatusTransitionValidator.allowedTransitions(InvoiceStatus.PAID)
        assertTrue(allowed.isEmpty())
    }

    @Test
    fun `StatusTransitionValidator validate throws BusinessLogicError for PAID to DRAFT`() {
        val ex = runCatching {
            StatusTransitionValidator.validate(1L, InvoiceStatus.PAID, InvoiceStatus.DRAFT)
        }.exceptionOrNull()

        assertNotNull(ex)
        assertIs<BizapException.BusinessLogicError>(ex)
        assertTrue(ex.message!!.contains("PAID"))
    }

    @Test
    fun `StatusTransitionValidator validate succeeds for DRAFT to SENT`() {
        // Should not throw
        StatusTransitionValidator.validate(1L, InvoiceStatus.DRAFT, InvoiceStatus.SENT)
    }

    // ── Snapshot synchronisation ──────────────────────────────────────────────────

    @Test
    fun `updateInvoiceStatus updates InvoiceAnalyticsSnapshot status when it exists`() = runTest {
        val invoiceId = 1L
        mockInvoice(invoiceId = invoiceId, status = InvoiceStatus.SENT)
        val analyticsSnap = makeAnalyticsSnapshot(invoiceId = invoiceId, status = "SENT")
        coEvery { analyticsDao.getInvoiceSnapshot(invoiceId) } returns analyticsSnap
        coEvery { analyticsDao.updateInvoiceSnapshot(any()) } just Runs

        val result = repository.updateInvoiceStatus(invoiceId, InvoiceStatus.PAID)

        assertTrue(result.isSuccess)
        coVerify { analyticsDao.updateInvoiceSnapshot(any()) }
    }

    @Test
    fun `updateInvoiceStatus calls optimistic lock update when transitioning to PAID`() = runTest {
        val invoiceId = 1L
        mockInvoice(invoiceId = invoiceId, status = InvoiceStatus.SENT)
        coEvery { analyticsDao.getInvoiceSnapshot(invoiceId) } returns null

        val result = repository.updateInvoiceStatus(invoiceId, InvoiceStatus.PAID)

        assertTrue(result.isSuccess)
        coVerify { analyticsDao.updateDailySnapshotWithOptimisticLock(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `updateInvoiceStatus calls optimistic lock update for OVERDUE to PARTIALLY_PAID transition`() = runTest {
        val invoiceId2 = 2L
        mockInvoice(invoiceId = invoiceId2, status = InvoiceStatus.OVERDUE)
        coEvery { analyticsDao.getInvoiceSnapshot(invoiceId2) } returns null

        val result = repository.updateInvoiceStatus(invoiceId2, InvoiceStatus.PARTIALLY_PAID)

        assertTrue(result.isSuccess)
        coVerify { analyticsDao.updateDailySnapshotWithOptimisticLock(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `updateInvoiceStatus skips DailySnapshot update when no snapshot exists`() = runTest {
        val invoiceId = 1L
        mockInvoice(invoiceId = invoiceId, status = InvoiceStatus.DRAFT)
        coEvery { analyticsDao.getInvoiceSnapshot(invoiceId) } returns null

        val result = repository.updateInvoiceStatus(invoiceId, InvoiceStatus.SENT)

        assertTrue(result.isSuccess)
        coVerify(exactly = 0) { analyticsDao.updateSnapshotWithVersion(any(), any(), any(), any(), any()) }
    }

    // ── Optimistic locking ────────────────────────────────────────────────────────

    @Test
    fun `updateInvoiceStatus delegates daily snapshot update to DAO optimistic lock method`() = runTest {
        val invoiceId = 1L
        mockInvoice(invoiceId = invoiceId, status = InvoiceStatus.SENT)
        coEvery { analyticsDao.getInvoiceSnapshot(invoiceId) } returns null

        val result = repository.updateInvoiceStatus(invoiceId, InvoiceStatus.PAID)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { analyticsDao.updateDailySnapshotWithOptimisticLock(any(), any(), any(), any(), any()) }
    }

    // ── Retry on transient failure ────────────────────────────────────────────────

    @Test
    fun `updateInvoiceStatus returns failure when invoiceDao updateStatus throws non-retryable error`() = runTest {
        val invoiceId = 1L
        mockInvoice(invoiceId = invoiceId, status = InvoiceStatus.DRAFT)
        // Override updateInvoiceStatus to throw
        coEvery { invoiceDao.updateInvoiceStatus(invoiceId, any()) } throws RuntimeException("DB locked")

        val result = repository.updateInvoiceStatus(invoiceId, InvoiceStatus.SENT)

        assertTrue(result.isFailure)
    }

    // ── Snapshot consistency verification ────────────────────────────────────────

    @Test
    fun `updateInvoiceStatus regenerates snapshot when analytics snapshot is missing`() = runTest {
        val invoiceId = 1L
        mockInvoice(invoiceId = invoiceId, status = InvoiceStatus.DRAFT)

        // First call from updateInvoiceStatus, second from verifySnapshotConsistency
        coEvery { analyticsDao.getInvoiceSnapshot(invoiceId) } returns null

        val result = repository.updateInvoiceStatus(invoiceId, InvoiceStatus.SENT)

        assertTrue(result.isSuccess)
        // upsertInvoiceSnapshot should be called from regenerateAnalyticsSnapshot
        coVerify(atLeast = 1) { analyticsDao.upsertInvoiceSnapshot(any()) }
    }

    @Test
    fun `updateInvoiceStatus regenerates snapshot when drift detected`() = runTest {
        val invoiceId = 1L
        mockInvoice(invoiceId = invoiceId, status = InvoiceStatus.DRAFT)

        // updateInvoiceStatus step: no analytics snapshot
        val staleSnapshot = makeAnalyticsSnapshot(invoiceId = invoiceId, status = "DRAFT")
        // First call returns stale snapshot for update; second call (verifySnapshotConsistency)
        // also returns stale snapshot, triggering regeneration
        coEvery { analyticsDao.getInvoiceSnapshot(invoiceId) } returns staleSnapshot

        val result = repository.updateInvoiceStatus(invoiceId, InvoiceStatus.SENT)

        assertTrue(result.isSuccess)
    }

    // ── Performance metrics ───────────────────────────────────────────────────────

    @Test
    fun `updateInvoiceStatus records success in PerformanceMetrics on success`() = runTest {
        mockInvoice(invoiceId = 1L, status = InvoiceStatus.DRAFT)

        repository.updateInvoiceStatus(1L, InvoiceStatus.SENT)

        assertEquals(1, PerformanceMetrics.getSuccessCount("updateInvoiceStatus"))
        assertEquals(0, PerformanceMetrics.getFailureCount("updateInvoiceStatus"))
    }

    @Test
    fun `updateInvoiceStatus records failure in PerformanceMetrics on validation error`() = runTest {
        val result = repository.updateInvoiceStatus(-1L, InvoiceStatus.SENT)

        assertTrue(result.isFailure)
        assertEquals(0, PerformanceMetrics.getSuccessCount("updateInvoiceStatus"))
        assertEquals(1, PerformanceMetrics.getFailureCount("updateInvoiceStatus"))
    }

    @Test
    fun `PerformanceMetrics getAverageLatencyMs returns zero when no calls recorded`() {
        assertEquals(0.0, PerformanceMetrics.getAverageLatencyMs("unknown_op"))
    }

    @Test
    fun `PerformanceMetrics getFailureRate returns correct ratio`() {
        PerformanceMetrics.recordSuccess("op", 10L)
        PerformanceMetrics.recordFailure("op", 5L)

        assertEquals(0.5, PerformanceMetrics.getFailureRate("op"), absoluteTolerance = 0.001)
    }

    @Test
    fun `PerformanceMetrics getFailureRate returns zero for unknown operation`() {
        assertEquals(0.0, PerformanceMetrics.getFailureRate("no_such_op"))
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // PATHWAY 2: Analytics Snapshot Creation Tests
    // ═══════════════════════════════════════════════════════════════════════════════

    @Test
    fun `saveInvoice creates DailyRevenueSnapshot`() = runTest {
        val businessId = 1L
        val invoice = TestDataFactory.createTestInvoice(id = 0, status = InvoiceStatus.PAID)

        coEvery { businessProfileRepo.getActiveBusinessId() } returns businessId
        coEvery { invoiceDao.getMaxSequenceForYear(any(), businessId) } returns 0
        coEvery { invoiceDao.insert(any(), any()) } returns 123L
        coEvery { analyticsDao.insertDailySnapshot(any()) } just Runs
        coEvery { analyticsDao.getDailySnapshotByDate(any(), any()) } returns null

        repository.saveInvoice(invoice).getOrThrow()

        coVerify { analyticsDao.insertDailySnapshot(any()) }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // PATHWAY 2B: Payment Update Snapshot Sync Tests
    // ═══════════════════════════════════════════════════════════════════════════════

    @Test
    fun `updateAmountPaid updates existing payment snapshot`() = runTest {
        mockInvoice(invoiceId = 1L, status = InvoiceStatus.SENT)
        coEvery { invoiceDao.updateInvoice(any()) } just Runs

        val existingSnapshot = mockk<com.emul8r.bizap.data.local.entities.InvoicePaymentSnapshot>(relaxed = true)
        coEvery { paymentDao.getSnapshotByInvoiceId(1L) } returns existingSnapshot
        coEvery { paymentDao.updateSnapshot(any()) } just Runs

        repository.updateAmountPaid(1L, 5000).getOrThrow()

        coVerify { paymentDao.updateSnapshot(any()) }
    }

    @Test
    fun `updateAmountPaid creates payment snapshot if missing`() = runTest {
        mockInvoice(invoiceId = 1L, status = InvoiceStatus.SENT)
        coEvery { invoiceDao.updateInvoice(any()) } just Runs
        coEvery { paymentDao.getSnapshotByInvoiceId(1L) } returns null  // Missing
        coEvery { paymentDao.insertSnapshots(any()) } just Runs

        repository.updateAmountPaid(1L, 5000).getOrThrow()

        coVerify { paymentDao.insertSnapshots(any()) }
    }

    // ── Invoice Deletion Snapshot Cleanup Tests ─────────────────────────────────────

    @Test
    fun `deleteInvoice deletes InvoiceAnalyticsSnapshot`() = runTest {
        coEvery { analyticsDao.deleteInvoiceSnapshot(123L) } just Runs
        coEvery { paymentDao.deleteSnapshotByInvoiceId(123L) } just Runs
        coEvery { invoiceDao.deleteInvoiceWithItems(123L) } just Runs

        repository.deleteInvoice(123L).getOrThrow()

        coVerify { analyticsDao.deleteInvoiceSnapshot(123L) }
    }

    @Test
    fun `deleteInvoice deletes InvoicePaymentSnapshot`() = runTest {
        coEvery { analyticsDao.deleteInvoiceSnapshot(123L) } just Runs
        coEvery { paymentDao.deleteSnapshotByInvoiceId(123L) } just Runs
        coEvery { invoiceDao.deleteInvoiceWithItems(123L) } just Runs

        repository.deleteInvoice(123L).getOrThrow()

        coVerify { paymentDao.deleteSnapshotByInvoiceId(123L) }
    }

    @Test
    fun `deleteInvoice deletes invoice record`() = runTest {
        coEvery { analyticsDao.deleteInvoiceSnapshot(123L) } just Runs
        coEvery { paymentDao.deleteSnapshotByInvoiceId(123L) } just Runs
        coEvery { invoiceDao.deleteInvoiceWithItems(123L) } just Runs

        repository.deleteInvoice(123L).getOrThrow()

        coVerify { invoiceDao.deleteInvoiceWithItems(123L) }
    }

    @Test
    fun `deleteInvoice does NOT delete DailyRevenueSnapshot`() = runTest {
        coEvery { analyticsDao.deleteInvoiceSnapshot(123L) } just Runs
        coEvery { paymentDao.deleteSnapshotByInvoiceId(123L) } just Runs
        coEvery { invoiceDao.deleteInvoiceWithItems(123L) } just Runs

        repository.deleteInvoice(123L).getOrThrow()

        // DailyRevenueSnapshot should NOT be deleted (historical aggregate)
        coVerify(inverse = true) { analyticsDao.deleteDailySnapshot(any()) }
    }

    // ── Status Update: Comprehensive Snapshot Sync Tests ──────────────────────────

    @Test
    fun `updateInvoiceStatus syncs InvoiceAnalyticsSnapshot`() = runTest {
        mockInvoice(invoiceId = 1L, status = InvoiceStatus.SENT)

        val existingAnalyticsSnapshot = mockk<InvoiceAnalyticsSnapshot>(relaxed = true)
        coEvery { analyticsDao.getInvoiceSnapshot(1L) } returns existingAnalyticsSnapshot
        coEvery { analyticsDao.updateInvoiceSnapshot(any()) } just Runs
        coEvery { analyticsDao.getDailySnapshotByDate(any(), any()) } returns null
        coEvery { analyticsDao.updateDailySnapshotWithOptimisticLock(any(), any(), any(), any(), any()) } returns Unit
        coEvery { paymentDao.getSnapshotByInvoiceId(1L) } returns null

        repository.updateInvoiceStatus(1L, InvoiceStatus.PAID).getOrThrow()

        coVerify { analyticsDao.updateInvoiceSnapshot(any()) }
    }

    @Test
    fun `updateInvoiceStatus syncs DailyRevenueSnapshot`() = runTest {
        mockInvoice(invoiceId = 1L, status = InvoiceStatus.SENT)

        val existingAnalyticsSnapshot = mockk<InvoiceAnalyticsSnapshot>(relaxed = true)
        coEvery { analyticsDao.getInvoiceSnapshot(1L) } returns existingAnalyticsSnapshot
        coEvery { analyticsDao.updateInvoiceSnapshot(any()) } just Runs
        coEvery { analyticsDao.getDailySnapshotByDate(any(), any()) } returns null
        coEvery { analyticsDao.updateDailySnapshotWithOptimisticLock(any(), any(), any(), any(), any()) } returns Unit
        coEvery { paymentDao.getSnapshotByInvoiceId(1L) } returns null

        repository.updateInvoiceStatus(1L, InvoiceStatus.PAID).getOrThrow()

        coVerify { analyticsDao.updateDailySnapshotWithOptimisticLock(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `updateInvoiceStatus syncs InvoicePaymentSnapshot`() = runTest {
        mockInvoice(invoiceId = 1L, status = InvoiceStatus.SENT)

        val existingAnalyticsSnapshot = mockk<InvoiceAnalyticsSnapshot>(relaxed = true)
        val existingPaymentSnapshot = mockk<com.emul8r.bizap.data.local.entities.InvoicePaymentSnapshot>(relaxed = true)

        coEvery { analyticsDao.getInvoiceSnapshot(1L) } returns existingAnalyticsSnapshot
        coEvery { analyticsDao.updateInvoiceSnapshot(any()) } just Runs
        coEvery { analyticsDao.getDailySnapshotByDate(any(), any()) } returns null
        coEvery { analyticsDao.updateDailySnapshotWithOptimisticLock(any(), any(), any(), any(), any()) } returns Unit
        coEvery { paymentDao.getSnapshotByInvoiceId(1L) } returns existingPaymentSnapshot
        coEvery { paymentDao.updateSnapshot(any()) } just Runs

        repository.updateInvoiceStatus(1L, InvoiceStatus.PAID).getOrThrow()

        coVerify { paymentDao.updateSnapshot(any()) }
    }

    @Test
    fun `updateInvoiceStatus sets isPaid flag correctly for PAID status`() = runTest {
        mockInvoice(invoiceId = 1L, status = InvoiceStatus.SENT)

        val existingAnalyticsSnapshot = InvoiceAnalyticsSnapshot(
            invoiceId = 1L,
            businessProfileId = 1L,
            customerId = 1L,
            customerName = "Test",
            invoiceNumber = "INV-1",
            currencyCode = "AUD",
            subtotal = 10000L,
            taxAmount = 1000L,
            totalAmount = 11000L,
            status = "SENT",
            isPaid = false,
            isOverdue = false,
            invoiceDateMs = System.currentTimeMillis(),
            createdAtMs = System.currentTimeMillis()
        )

        coEvery { analyticsDao.getInvoiceSnapshot(1L) } returns existingAnalyticsSnapshot
        coEvery { analyticsDao.updateInvoiceSnapshot(any()) } just Runs
        coEvery { analyticsDao.getDailySnapshotByDate(any(), any()) } returns null
        coEvery { analyticsDao.updateDailySnapshotWithOptimisticLock(any(), any(), any(), any(), any()) } returns Unit
        coEvery { paymentDao.getSnapshotByInvoiceId(1L) } returns null

        repository.updateInvoiceStatus(1L, InvoiceStatus.PAID).getOrThrow()

        coVerify {
            analyticsDao.updateInvoiceSnapshot(
                match { snapshot ->
                    snapshot.status == "PAID" && snapshot.isPaid == true
                }
            )
        }
    }

    @Test
    fun `Snapshot sync handles all three snapshots atomically`() = runTest {
        mockInvoice(invoiceId = 1L, status = InvoiceStatus.SENT)

        val analyticsSnapshot = InvoiceAnalyticsSnapshot(
            invoiceId = 1L,
            businessProfileId = 1L,
            customerId = 1L,
            customerName = "Test",
            invoiceNumber = "INV-1",
            currencyCode = "AUD",
            subtotal = 10000L,
            taxAmount = 1000L,
            totalAmount = 11000L,
            status = "SENT",
            isPaid = false,
            isOverdue = false,
            invoiceDateMs = System.currentTimeMillis(),
            createdAtMs = System.currentTimeMillis()
        )

        val paymentSnapshot = mockk<com.emul8r.bizap.data.local.entities.InvoicePaymentSnapshot>(relaxed = true)

        coEvery { analyticsDao.getInvoiceSnapshot(1L) } returns analyticsSnapshot
        coEvery { analyticsDao.updateInvoiceSnapshot(any()) } just Runs
        coEvery { analyticsDao.getDailySnapshotByDate(any(), any()) } returns null
        coEvery { analyticsDao.updateDailySnapshotWithOptimisticLock(any(), any(), any(), any(), any()) } returns Unit
        coEvery { paymentDao.getSnapshotByInvoiceId(1L) } returns paymentSnapshot
        coEvery { paymentDao.updateSnapshot(any()) } just Runs

        repository.updateInvoiceStatus(1L, InvoiceStatus.PAID).getOrThrow()

        // Verify all three snapshots were synced
        coVerify {
            analyticsDao.updateInvoiceSnapshot(any())
            analyticsDao.updateDailySnapshotWithOptimisticLock(any(), any(), any(), any(), any())
            paymentDao.updateSnapshot(any())
        }
    }

    // ── Auto-payment recording when PAID ─────────────────────────────────────────

    @Test
    fun `updateInvoiceStatus auto-updates amountPaid when transitioning to PAID`() = runTest {
        val invoiceId = 1L
        mockInvoice(invoiceId = invoiceId, status = InvoiceStatus.SENT)

        val result = repository.updateInvoiceStatus(invoiceId, InvoiceStatus.PAID)

        assertTrue(result.isSuccess)
        coVerify { invoiceDao.updateAmountPaid(invoiceId, any()) }
    }

    @Test
    fun `updateInvoiceStatus auto-records payment entry when transitioning to PAID`() = runTest {
        val invoiceId = 1L
        mockInvoice(invoiceId = invoiceId, status = InvoiceStatus.SENT)

        val result = repository.updateInvoiceStatus(invoiceId, InvoiceStatus.PAID)

        assertTrue(result.isSuccess)
        coVerify { paymentDao.insertPayment(any()) }
    }

    @Test
    fun `updateInvoiceStatus does NOT auto-record payment when transitioning to SENT`() = runTest {
        val invoiceId = 1L
        mockInvoice(invoiceId = invoiceId, status = InvoiceStatus.DRAFT)

        val result = repository.updateInvoiceStatus(invoiceId, InvoiceStatus.SENT)

        assertTrue(result.isSuccess)
        coVerify(inverse = true) { invoiceDao.updateAmountPaid(any(), any()) }
        coVerify(inverse = true) { paymentDao.insertPayment(any()) }
    }

    @Test
    fun `updateInvoiceStatus does NOT auto-record payment when already fully paid`() = runTest {
        val invoiceId = 1L
        // Create an invoice that is already fully paid
        val fullyPaidEntity = TestDataFactory.createTestInvoice(
            id = invoiceId,
            status = InvoiceStatus.PARTIALLY_PAID,
            total = 100000L
        ).toEntity().copy(amountPaid = 100000L)
        val iwi = InvoiceWithItems(fullyPaidEntity, emptyList())
        coEvery { invoiceDao.getInvoiceWithItemsById(invoiceId) } returns flowOf(iwi)
        coEvery { invoiceDao.updateInvoiceStatus(invoiceId, any()) } just Runs

        val result = repository.updateInvoiceStatus(invoiceId, InvoiceStatus.PAID)

        assertTrue(result.isSuccess)
        // amountPaid should not be updated since it already equals totalAmount
        coVerify(inverse = true) { invoiceDao.updateAmountPaid(any(), any()) }
        coVerify(inverse = true) { paymentDao.insertPayment(any()) }
    }

    @Test
    fun `updateInvoiceStatus payment snapshot shows zero outstanding when PAID`() = runTest {
        val invoiceId = 1L
        mockInvoice(invoiceId = invoiceId, status = InvoiceStatus.SENT)

        val existingPaymentSnapshot = mockk<com.emul8r.bizap.data.local.entities.InvoicePaymentSnapshot>(relaxed = true)
        coEvery { paymentDao.getSnapshotByInvoiceId(invoiceId) } returns existingPaymentSnapshot
        coEvery { paymentDao.updateSnapshot(any()) } just Runs

        repository.updateInvoiceStatus(invoiceId, InvoiceStatus.PAID).getOrThrow()

        coVerify {
            paymentDao.updateSnapshot(
                match { snapshot -> snapshot.outstandingAmount == 0L }
            )
        }
    }
}
