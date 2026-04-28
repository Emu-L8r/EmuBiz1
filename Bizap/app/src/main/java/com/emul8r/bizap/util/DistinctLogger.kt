package com.emul8r.bizap.util

import timber.log.Timber

/**
 * Prevents repetitive status logs from cluttering Logcat.
 * Only logs when the message content changes (not on every poll/recomposition).
 *
 * Example:
 *   Before: "[NETWORK] 🔵 WIFI (EXCELLENT)" appears 50x in a row (Compose recomposition)
 *   After:  "[NETWORK] 🔵 WIFI (EXCELLENT)" appears once, then silent until status changes
 *
 * Usage:
 *   distinctLogger.logIfChanged("network_status", "[NETWORK] 🔵 WIFI (EXCELLENT)")
 */
class DistinctLogger {
    private val lastValues = mutableMapOf<String, String>()

    /**
     * Log a message only if it differs from the last logged value for this key.
     *
     * @param key Unique identifier for this log stream (e.g., "network_status", "gui_mode")
     * @param message The log message to conditionally emit
     * @param priority Timber log level (default: DEBUG)
     */
    fun logIfChanged(key: String, message: String, priority: LogPriority = LogPriority.DEBUG) {
        if (lastValues[key] != message) {
            when (priority) {
                LogPriority.DEBUG -> Timber.d(message)
                LogPriority.INFO -> Timber.i(message)
                LogPriority.WARNING -> Timber.w(message)
                LogPriority.ERROR -> Timber.e(message)
            }
            lastValues[key] = message
        }
    }

    /**
     * Clear all cached values (useful for testing or manual reset).
     */
    fun reset() {
        lastValues.clear()
    }

    enum class LogPriority {
        DEBUG, INFO, WARNING, ERROR
    }
}

// Global singleton instance for easy access throughout app
val distinctLogger = DistinctLogger()

