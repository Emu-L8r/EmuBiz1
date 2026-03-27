package com.emul8r.bizap.utils.logging

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import timber.log.Timber
import java.io.File

/**
 * Utility for accessing and sharing app logs.
 *
 * **Purpose:**
 * Provides easy ways to view, export, and share logs for debugging.
 *
 * **Usage:**
 * ```kotlin
 * // Get current logs as text
 * val logsText = LogViewer.getCurrentLogs(context)
 *
 * // Export all logs (current + archived)
 * val exportFile = LogViewer.exportAllLogs(context)
 *
 * // Share logs via email
 * LogViewer.shareLogs(context, activity)
 *
 * // Get log file size
 * val sizeBytes = LogViewer.getLogFileSize(context)
 *
 * // Get summary of recent errors
 * val errorSummary = LogViewer.getErrorSummary(context)
 * ```
 */
object LogViewer {

    /**
     * Get the current log file contents as text.
     */
    fun getCurrentLogs(context: Context): String {
        return try {
            val logFile = FileLoggingTree.getLogFile(context)
            if (logFile.exists()) logFile.readText() else "No logs available yet"
        } catch (e: Exception) {
            "Error reading logs: ${e.message}"
        }
    }

    /**
     * Get all logs (current + archived) combined.
     */
    fun getAllLogs(context: Context): String {
        return try {
            val sb = StringBuilder()

            // Add current log
            val currentLog = FileLoggingTree.getLogFile(context)
            if (currentLog.exists()) {
                sb.append("=== CURRENT LOG ===\n")
                sb.append(currentLog.readText())
                sb.append("\n\n")
            }

            // Add archived logs
            FileLoggingTree.getArchivedLogs(context).forEach { log ->
                sb.append("=== ARCHIVED: ${log.name} ===\n")
                sb.append(log.readText())
                sb.append("\n\n")
            }

            sb.toString()
        } catch (e: Exception) {
            "Error reading logs: ${e.message}"
        }
    }

    /**
     * Export all logs to a file.
     */
    fun exportAllLogs(context: Context): File {
        return FileLoggingTree.exportLogs(context)
    }

    /**
     * Get total size of all logs.
     */
    fun getLogFileSize(context: Context): Long {
        return FileLoggingTree.getTotalLogSize(context)
    }

    /**
     * Get a summary of recent errors from logs.
     */
    fun getErrorSummary(context: Context): String {
        return try {
            val logs = getAllLogs(context)
            val lines = logs.split("\n")
            val errors = lines.filter { it.contains("❌") || it.contains("ERROR") || it.contains("Exception") }

            if (errors.isEmpty()) {
                "✅ No errors found in logs"
            } else {
                "⚠️ Found ${errors.size} error entries:\n\n" + errors.takeLast(10).joinToString("\n")
            }
        } catch (e: Exception) {
            "Error analyzing logs: ${e.message}"
        }
    }

    /**
     * Get a summary of PDF export operations from logs.
     */
    fun getPdfExportSummary(context: Context): String {
        return try {
            val logs = getAllLogs(context)
            val lines = logs.split("\n")
            val pdfLines = lines.filter {
                it.contains("PDF_EXPORT") ||
                it.contains("FILEPROVIDER") ||
                it.contains("SHARE_INTENT")
            }

            if (pdfLines.isEmpty()) {
                "No PDF export operations logged yet"
            } else {
                val attempts = pdfLines.count { it.contains("ATTEMPT") }
                val successes = pdfLines.count { it.contains("SUCCESS") }
                val failures = pdfLines.count { it.contains("FAILURE") }

                val summary = StringBuilder()
                summary.append("📄 PDF Export Summary:\n")
                summary.append("  Attempts: $attempts\n")
                summary.append("  Successes: $successes\n")
                summary.append("  Failures: $failures\n\n")

                summary.append("📋 Recent operations:\n")
                pdfLines.takeLast(5).forEach { line ->
                    summary.append("  $line\n")
                }

                summary.toString()
            }
        } catch (e: Exception) {
            "Error analyzing logs: ${e.message}"
        }
    }

    /**
     * Share logs via email.
     */
    fun shareLogs(context: Context, activity: android.app.Activity) {
        try {
            val logsFile = exportAllLogs(context)
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                logsFile
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_EMAIL, arrayOf("support@bizap.com"))
                putExtra(Intent.EXTRA_SUBJECT, "Bizap Logs - Debug Report")
                putExtra(Intent.EXTRA_TEXT, "Please see attached logs for debugging.")
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            activity.startActivity(Intent.createChooser(intent, "Share Logs"))
            Timber.d("📤 Log sharing intent launched")
        } catch (e: Exception) {
            Timber.e(e, "Failed to share logs")
        }
    }

    /**
     * Clear all logs.
     */
    fun clearLogs(context: Context) {
        try {
            FileLoggingTree.clearLogs(context)
            Timber.d("🗑️ All logs cleared")
        } catch (e: Exception) {
            Timber.e(e, "Failed to clear logs")
        }
    }
}

