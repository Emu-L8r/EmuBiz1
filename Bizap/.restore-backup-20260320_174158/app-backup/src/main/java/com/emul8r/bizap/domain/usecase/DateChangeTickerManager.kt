package com.emul8r.bizap.domain.usecase

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject

/**
 * Detects when the date changes (at midnight) and emits events.
 * Used to trigger automatic data refresh for date-dependent calculations.
 */
interface DateChangeTickerObserver {
    suspend fun onDateChanged(newDate: LocalDate)
}

/**
 * Watches for midnight transitions and notifies registered observers.
 * Checks every minute whether the current date differs from the last observed date.
 */
class DateChangeTickerManager @Inject constructor(
    private val scope: CoroutineScope,
    private val observers: MutableList<DateChangeTickerObserver> = mutableListOf()
) {

    private var currentDate = LocalDate.now()

    @Volatile
    private var tickerJob: Job? = null

    /**
     * Start watching for midnight transitions.
     * Checks every minute if the date has changed.
     * Safe to call multiple times — only one watcher runs at a time.
     */
    fun startWatching() {
        if (tickerJob?.isActive == true) return
        tickerJob = scope.launch {
            while (isActive) {
                delay(60_000L) // Check every minute

                val now = LocalDate.now()
                if (now != currentDate) {
                    currentDate = now
                    notifyObservers(now)
                }
            }
        }
    }

    /**
     * Stop watching for date changes.
     */
    fun stopWatching() {
        tickerJob?.cancel()
        tickerJob = null
    }

    /**
     * Register an observer to be notified of date changes.
     */
    fun registerObserver(observer: DateChangeTickerObserver) {
        observers.add(observer)
    }

    /**
     * Notify all registered observers of a date change.
     * Internal visibility allows unit tests to trigger notifications directly.
     */
    internal suspend fun notifyObservers(newDate: LocalDate) {
        observers.forEach { observer ->
            try {
                observer.onDateChanged(newDate)
            } catch (e: Exception) {
                Timber.e(e, "Error notifying observer of date change")
            }
        }
    }
}
