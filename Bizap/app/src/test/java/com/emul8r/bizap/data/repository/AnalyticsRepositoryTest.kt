package com.emul8r.bizap.data.repository

import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.data.local.dao.AnalyticsEventDao
import com.emul8r.bizap.data.local.entities.AnalyticsEventEntity
import com.emul8r.bizap.domain.analytics.AnalyticsRepository
import com.emul8r.bizap.domain.analytics.InvoiceAnalyticsEvent
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Unit tests for [AnalyticsRepositoryImpl].
 * Verifies event logging, querying, and error handling.
 */
class AnalyticsRepositoryTest : BaseUnitTest() {

    private val dao: AnalyticsEventDao = mockk()
    private lateinit var repository: AnalyticsRepository

    private val businessId = 1L
    private val now = System.currentTimeMillis()

    private fun makeEntity(type: String = "InvoiceCreated"): AnalyticsEventEntity =
        AnalyticsEventEntity(
            id = 0L,
            businessId = businessId,
            eventType = type,
            eventData = """{"type":"$type","businessId":$businessId,"invoiceId":1,"amount":5000,"timestamp":$now}""",
            timestamp = now,
            createdAt = now
        )

    @Before
    fun setUp() {
        repository = AnalyticsRepositoryImpl(dao)
    }

    // ── logEvent ──────────────────────────────────────────────────────────

    @Test
    fun `logEvent returns success when DAO insert succeeds`() = runUnitTest {
        coEvery { dao.insertEvent(any()) } returns 1L
        val event = InvoiceAnalyticsEvent.InvoiceCreated(
            businessId = businessId,
            invoiceId = 10L,
            amount = 5_000L,
            timestamp = now
        )
        val result = repository.logEvent(event)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `logEvent calls DAO insertEvent once`() = runUnitTest {
        coEvery { dao.insertEvent(any()) } returns 1L
        val event = InvoiceAnalyticsEvent.InvoiceCreated(
            businessId = businessId,
            invoiceId = 10L,
            amount = 5_000L,
            timestamp = now
        )
        repository.logEvent(event)
        coVerify(exactly = 1) { dao.insertEvent(any()) }
    }

    @Test
    fun `logEvent returns failure when DAO throws`() = runUnitTest {
        coEvery { dao.insertEvent(any()) } throws RuntimeException("DB error")
        val event = InvoiceAnalyticsEvent.InvoiceCreated(
            businessId = businessId,
            invoiceId = 10L,
            amount = 5_000L,
            timestamp = now
        )
        val result = repository.logEvent(event)
        assertTrue(result.isFailure)
    }

    // ── getEventCount ──────────────────────────────────────────────────────

    @Test
    fun `getEventCount returns success with count from DAO`() = runUnitTest {
        coEvery { dao.getEventCountByType(businessId, "InvoiceCreated", any()) } returns 5
        val result = repository.getEventCount(businessId, "InvoiceCreated", 0L)
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!! == 5)
    }

    @Test
    fun `getEventCount returns failure when DAO throws`() = runUnitTest {
        coEvery { dao.getEventCountByType(any(), any(), any()) } throws RuntimeException("DB error")
        val result = repository.getEventCount(businessId, "InvoiceCreated", 0L)
        assertTrue(result.isFailure)
    }

    // ── getPaymentAmount ───────────────────────────────────────────────────

    @Test
    fun `getPaymentAmount returns success with amount from DAO`() = runUnitTest {
        coEvery { dao.getSumPaymentAmount(businessId, any()) } returns 100_000L
        val result = repository.getPaymentAmount(businessId, 0L)
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!! == 100_000L)
    }

    @Test
    fun `getPaymentAmount returns failure when DAO throws`() = runUnitTest {
        coEvery { dao.getSumPaymentAmount(any(), any()) } throws RuntimeException("DB error")
        val result = repository.getPaymentAmount(businessId, 0L)
        assertTrue(result.isFailure)
    }

    // ── observeRecentEvents ────────────────────────────────────────────────

    @Test
    fun `observeRecentEvents emits mapped events from DAO`() = runUnitTest {
        val entity = makeEntity("InvoiceCreated")
        every { dao.observeRecentEvents(businessId, any()) } returns flowOf(listOf(entity))
        val events = repository.observeRecentEvents(businessId, limitMinutes = 60).first()
        assertTrue(events.isNotEmpty())
    }

    @Test
    fun `observeRecentEvents emits empty list when DAO returns empty`() = runUnitTest {
        every { dao.observeRecentEvents(businessId, any()) } returns flowOf(emptyList())
        val events = repository.observeRecentEvents(businessId, limitMinutes = 60).first()
        assertTrue(events.isEmpty())
    }

    // ── observeEventsByType ────────────────────────────────────────────────

    @Test
    fun `observeEventsByType emits mapped events from DAO`() = runUnitTest {
        val entity = makeEntity("PaymentRecorded")
        every { dao.observeEventsByType(businessId, "PaymentRecorded") } returns flowOf(listOf(entity))
        val events = repository.observeEventsByType(businessId, "PaymentRecorded").first()
        assertTrue(events.isNotEmpty())
    }

    @Test
    fun `observeEventsByType emits empty list when no events of that type`() = runUnitTest {
        every { dao.observeEventsByType(businessId, "UnknownType") } returns flowOf(emptyList())
        val events = repository.observeEventsByType(businessId, "UnknownType").first()
        assertTrue(events.isEmpty())
    }
}



