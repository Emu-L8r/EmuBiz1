package com.emul8r.bizap.ui.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.time.Instant
import timber.log.Timber

/**
 * Standardized date formatter for the UI
 */
fun formatDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

/**
 * Overload for ISO-8601 date strings (e.g., "2026-04-12T10:30:00Z")
 */
fun formatDate(isoDateString: String?): String {
    if (isoDateString.isNullOrBlank()) return ""
    return try {
        val instant = Instant.parse(isoDateString)
        val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        formatter.format(Date(instant.toEpochMilli()))
    } catch (e: Exception) {
        Timber.w(e, "Failed to parse ISO date: $isoDateString")
        ""
    }
}
