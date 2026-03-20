package com.emul8r.bizap.ui.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Standardized date formatter for the UI
 */
fun formatDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return formatter.format(Date(timestamp))
}
