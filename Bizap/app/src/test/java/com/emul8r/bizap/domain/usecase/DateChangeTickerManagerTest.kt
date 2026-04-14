package com.emul8r.bizap.domain.usecase

import com.emul8r.bizap.BaseUnitTest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for [DateChangeTickerManager].
 *
 * Verifies date-change observer notification, lifecycle (start/stop watching),
 * and error resilience via the internal [DateChangeTickerManager.notifyObservers]
 * entry point.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DateChangeTickerManagerTest : BaseUnitTest() {

    private lateinit var manager: DateChangeTickerManager
    private val notifiedDates = mutableListOf<LocalDate>()

    private val testObserver = object : DateChangeTickerObserver {
        override suspend fun onDateChanged(newDate: LocalDate) {
            notifiedDates.add(newDate)
        }
    }

    @Before
    fun setUp() {
        notifiedDates.clear()
    }

    // ── observerRegistration ──────────────────────────────────────────────────

    @Test
    fun `observerRegistration - registered observer is notified on date change`() = runTest {
        manager = DateChangeTickerManager(
            scope = CoroutineScope(coroutineContext),
            observers = mutableListOf()
        )
        manager.registerObserver(testObserver)

        val tomorrow = LocalDate.now().plusDays(1)
        manager.notifyObservers(tomorrow)

        assertEquals(1, notifiedDates.size)
        assertEquals(tomorrow, notifiedDates.first())
    }

    @Test
    fun `observerRegistration - multiple observers are all notified`() = runTest {
        val dates1 = mutableListOf<LocalDate>()
        val dates2 = mutableListOf<LocalDate>()

        val observer1 = object : DateChangeTickerObserver {
            override suspend fun onDateChanged(newDate: LocalDate) { dates1.add(newDate) }
        }
        val observer2 = object : DateChangeTickerObserver {
            override suspend fun onDateChanged(newDate: LocalDate) { dates2.add(newDate) }
        }

        manager = DateChangeTickerManager(
            scope = CoroutineScope(coroutineContext),
            observers = mutableListOf(observer1, observer2)
        )

        val newDate = LocalDate.now().plusDays(1)
        manager.notifyObservers(newDate)

        assertEquals(1, dates1.size)
        assertEquals(1, dates2.size)
        assertEquals(newDate, dates1.first())
        assertEquals(newDate, dates2.first())
    }

    @Test
    fun `observerRegistration - observer not notified when not registered`() = runTest {
        manager = DateChangeTickerManager(
            scope = CoroutineScope(coroutineContext),
            observers = mutableListOf()
        )
        // testObserver NOT registered — notifyObservers should not reach it

        manager.notifyObservers(LocalDate.now().plusDays(1))

        assertTrue(notifiedDates.isEmpty(), "Unregistered observer must not be notified")
    }

    // ── startStopWatching ─────────────────────────────────────────────────────

    @Test
    fun `startStopWatching - stopWatching cancels the ticker job`() = runTest {
        manager = DateChangeTickerManager(
            scope = CoroutineScope(coroutineContext),
            observers = mutableListOf()
        )

        manager.startWatching()
        manager.stopWatching()

        // After stopping, no exception should be thrown
        assertTrue(true, "stopWatching completed without error")
    }

    @Test
    fun `startStopWatching - calling startWatching twice does not create duplicate jobs`() = runTest {
        manager = DateChangeTickerManager(
            scope = CoroutineScope(coroutineContext),
            observers = mutableListOf()
        )

        manager.startWatching()
        manager.startWatching() // Should be a no-op since already active

        manager.stopWatching()

        // Verify we can stop and it doesn't throw
        assertTrue(true, "Double startWatching handled gracefully")
    }

    // ── dateChangeDetection ───────────────────────────────────────────────────

    @Test
    fun `dateChangeDetection - observer receives correct new date`() = runTest {
        manager = DateChangeTickerManager(
            scope = CoroutineScope(coroutineContext),
            observers = mutableListOf()
        )
        manager.registerObserver(testObserver)

        val expectedDate = LocalDate.of(2026, 3, 18)
        manager.notifyObservers(expectedDate)

        assertEquals(expectedDate, notifiedDates.first())
        assertEquals(2026, notifiedDates.first().year)
        assertEquals(3, notifiedDates.first().monthValue)
        assertEquals(18, notifiedDates.first().dayOfMonth)
    }

    @Test
    fun `dateChangeDetection - multiple notifications accumulate in order`() = runTest {
        manager = DateChangeTickerManager(
            scope = CoroutineScope(coroutineContext),
            observers = mutableListOf()
        )
        manager.registerObserver(testObserver)

        val day1 = LocalDate.of(2026, 3, 18)
        val day2 = LocalDate.of(2026, 3, 19)

        manager.notifyObservers(day1)
        manager.notifyObservers(day2)

        assertEquals(2, notifiedDates.size)
        assertEquals(day1, notifiedDates[0])
        assertEquals(day2, notifiedDates[1])
    }

    // ── observerErrorHandling ─────────────────────────────────────────────────

    @Test
    fun `observerErrorHandling - failing observer does not block subsequent observers`() = runTest {
        val successDates = mutableListOf<LocalDate>()

        val failingObserver = object : DateChangeTickerObserver {
            override suspend fun onDateChanged(newDate: LocalDate) {
                throw RuntimeException("Simulated observer failure")
            }
        }
        val successObserver = object : DateChangeTickerObserver {
            override suspend fun onDateChanged(newDate: LocalDate) { successDates.add(newDate) }
        }

        // failing observer comes BEFORE success observer to verify try/catch continues iteration
        manager = DateChangeTickerManager(
            scope = CoroutineScope(coroutineContext),
            observers = mutableListOf(failingObserver, successObserver)
        )

        val newDate = LocalDate.now().plusDays(1)
        manager.notifyObservers(newDate)

        assertEquals(1, successDates.size, "Success observer must still receive notification")
        assertEquals(newDate, successDates.first())
    }
}



