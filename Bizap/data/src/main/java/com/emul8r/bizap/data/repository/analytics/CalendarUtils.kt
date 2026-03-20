package com.emul8r.bizap.data.repository.analytics

import java.util.Calendar

/**
 * Shared calendar utility functions for analytics date-window calculations.
 *
 * All calculations use the device's local timezone via [Calendar.getInstance()].
 * Timestamps are in epoch-milliseconds throughout.
 */
object CalendarUtils {

    /** Milliseconds in 7 days — used for the rolling weekly window. */
    const val SEVEN_DAYS_MS = 7L * 24 * 60 * 60 * 1000

    /** Returns epoch-millis for midnight on the 1st of the current local month. */
    fun startOfCurrentMonth(nowMs: Long): Long = Calendar.getInstance().apply {
        timeInMillis = nowMs
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    /** Returns epoch-millis for midnight on January 1st of the current local year. */
    fun startOfCurrentYear(nowMs: Long): Long = Calendar.getInstance().apply {
        timeInMillis = nowMs
        set(Calendar.MONTH, Calendar.JANUARY)
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}
