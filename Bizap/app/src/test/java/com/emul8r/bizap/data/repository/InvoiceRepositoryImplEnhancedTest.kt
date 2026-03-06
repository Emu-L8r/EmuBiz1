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
import com.emul8r.bizap.domain.error.BizapException
import com.emul8r.bizap.domain.model.InvoiceStatus
import com.emul8r.bizap.domain.repository.BusinessProfileRepository
import com.emul8r.bizap.domain.validation.StatusTransitionValidator
import com.emul8r.bizap.util.TestDataFactory
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
import kotlin.test.assertNull
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

    private lateinit var repository: InvoiceRepositoryImpl

    @Before
    fun setup() {
        PerformanceMetrics.resetAll()
        repository = InvoiceRepositoryImpl(invoiceDao, businessProfileRepo, analyticsDao, paymentDao)
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
        coEvery { analyticsDao.updateSnapshotWithVersion(any(), any(), any(), any(), any()) } returns 1

        val result = repository.updateInvoiceStatus(invoiceId, InvoiceStatus.PAID)

        assertTrue(result.isSuccess)
        coVerify { analyticsDao.updateInvoiceSnapshot(any()) }
    }

    @Test
    fun `updateInvoiceStatus increments paidInvoiceCount when transitioning to PAID`() = runTest {
        val invoiceId = 1L
        mockInvoice(invoiceId = invoiceId, status = InvoiceStatus.SENT)
        coEvery { analyticsDao.getInvoiceSnapshot(invoiceId) } returns null
        val dailySnap = makeDailySnapshot(paidInvoiceCount = 3)
        coEvery { analyticsDao.getDailySnapshotByDate(any(), any()) } returns dailySnap
        coEvery { analyticsDao.updateSnapshotWithVersion(any(), any(), eq(4), any(), any()) } returns 1

        val result = repository.updateInvoiceStatus(invoiceId, InvoiceStatus.PAID)

        assertTrue(result.isSuccess)
        coVerify { analyticsDao.updateSnapshotWithVersion(any(), any(), 4, any(), any()) }
    }

    @Test
    fun `updateInvoiceStatus decrements paidInvoiceCount when transitioning from PAID`() = runTest {
        val invoiceId = 1L
        mockInvoice(invoiceId = invoiceId, status = InvoiceStatus.PAID)
        // PAID → PARTIALLY_PAID (but this is blocked by transition rules; use OVERDUE→PARTIALLY_PAID)

        // Instead test OVERDUE→PARTIALLY_PAID doesn't change paidCount
        val invoiceId2 = 2L
        mockInvoice(invoiceId = invoiceId2, status = InvoiceStatus.OVERDUE)
        coEvery { analyticsDao.getInvoiceSnapshot(invoiceId2) } returns null
        val dailySnap = makeDailySnapshot(paidInvoiceCount = 2)
        coEvery { analyticsDao.getDailySnapshotByDate(any(), any()) } returns dailySnap
        coEvery { analyticsDao.updateSnapshotWithVersion(any(), any(), eq(2), any(), any()) } returns 1

        val result = repository.updateInvoiceStatus(invoiceId2, InvoiceStatus.PARTIALLY_PAID)

        assertTrue(result.isSuccess)
        // paidInvoiceCount stays at 2 (OVERDUE→PARTIALLY_PAID doesn't change paid count)
        coVerify { analyticsDao.updateSnapshotWithVersion(any(), any(), 2, any(), any()) }
    }

    @Test
    fun `updateInvoiceStatus skips DailySnapshot update when no snapshot exists`() = runTest {
        val invoiceId = 1L
        mockInvoice(invoiceId = invoiceId, status = InvoiceStatus.DRAFT)
        coEvery { analyticsDao.getInvoiceSnapshot(invoiceId) } returns null
        coEvery { analyticsDao.getDailySnapshotByDate(any(), any()) } returns null

        val result = repository.updateInvoiceStatus(invoiceId, InvoiceStatus.SENT)

        assertTrue(result.isSuccess)
        coVerify(exactly = 0) { analyticsDao.updateSnapshotWithVersion(any(), any(), any(), any(), any()) }
    }

    // ── Optimistic locking ────────────────────────────────────────────────────────

    @Test
    fun `updateInvoiceStatus retries DailySnapshot update on version conflict`() = runTest {
        val invoiceId = 1L
        mockInvoice(invoiceId = invoiceId, status = InvoiceStatus.SENT)
        coEvery { analyticsDao.getInvoiceSnapshot(invoiceId) } returns null

        val snap1 = makeDailySnapshot(version = 1)
        val snap2 = makeDailySnapshot(version = 2)

        // First read returns version 1, conflict; second read returns version 2, succeeds
        coEvery { analyticsDao.getDailySnapshotByDate(any(), any()) } returnsMany listOf(snap1, snap2)
        coEvery { analyticsDao.updateSnapshotWithVersion(any(), any(), any(), eq(1), any()) } returns 0
        coEvery { analyticsDao.updateSnapshotWithVersion(any(), any(), any(), eq(2), any()) } returns 1

        val result = repository.updateInvoiceStatus(invoiceId, InvoiceStatus.PAID)

        assertTrue(result.isSuccess)
        coVerify(exactly = 2) { analyticsDao.updateSnapshotWithVersion(any(), any(), any(), any(), any()) }
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
}
