package com.emul8r.bizap.utils.logging

import android.content.Context
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Custom Timber Tree that writes logs to a file in app's private storage.
 *
 * **Purpose:**
 * Captures all logs to file for debugging and error tracking without external services.
 *
 * **Features:**
 * - Writes all log levels to file
 * - Automatically rotates logs when file exceeds size limit
 * - Includes timestamps with each log entry
 * - Works completely offline
 * - No external dependencies
 *
 * **Usage:**
 * ```kotlin
 * // In Application.onCreate()
 * val fileTree = FileLoggingTree(context)
 * Timber.plant(fileTree)
 * ```
 *
 * **Viewing Logs:**
 * - Access via `FileLoggingTree.getLogFile(context)`
 * - Read as text: `logFile.readText()`
 */
class FileLoggingTree(private val context: Context) : Timber.Tree() {

    private val logFile = File(context.filesDir, "bizap_logs.txt")
    private val maxFileSizeBytes = 5 * 1024 * 1024  // 5 MB
    private val dateFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)

    init {
        // Rotate logs if file is too large
        if (logFile.exists() && logFile.length() > maxFileSizeBytes) {
            rotateLogFile()
        }
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        try {
            val timestamp = dateFormat.format(Date())
            val level = getPriorityLabel(priority)
            val logLine = "[$timestamp] [$level] [$tag] $message\n"

            // Append to file
            logFile.appendText(logLine)

            // Also log exception/stack trace if present
            t?.let {
                logFile.appendText("${it.stackTraceToString()}\n")
            }

            // Rotate if file is getting too large
            if (logFile.length() > maxFileSizeBytes) {
                rotateLogFile()
            }
        } catch (e: Exception) {
            // Silently fail if we can't write logs (don't want file logging to crash the app)
            e.printStackTrace()
        }
    }

    private fun getPriorityLabel(priority: Int): String = when (priority) {
        android.util.Log.VERBOSE -> "V"
        android.util.Log.DEBUG -> "D"
        android.util.Log.INFO -> "I"
        android.util.Log.WARN -> "W"
        android.util.Log.ERROR -> "E"
        android.util.Log.ASSERT -> "A"
        else -> "?"
    }

    private fun rotateLogFile() {
        try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val archivedFile = File(context.filesDir, "bizap_logs_$timestamp.txt")
            logFile.renameTo(archivedFile)

            // Keep only last 3 archived logs to save space
            val archived = context.filesDir.listFiles { f ->
                f.name.startsWith("bizap_logs_") && f.name.endsWith(".txt")
            } ?: emptyArray()

            if (archived.size > 3) {
                archived.sortByDescending { it.lastModified() }
                archived.drop(3).forEach { it.delete() }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        /**
         * Get the current log file for reading.
         */
        fun getLogFile(context: Context): File {
            return File(context.filesDir, "bizap_logs.txt")
        }

        /**
         * Get all archived log files.
         */
        fun getArchivedLogs(context: Context): List<File> {
            return context.filesDir
                .listFiles { f -> f.name.startsWith("bizap_logs_") && f.name.endsWith(".txt") }
                ?.sortedByDescending { it.lastModified() }
                ?: emptyList()
        }

        /**
         * Clear all logs.
         */
        fun clearLogs(context: Context) {
            try {
                getLogFile(context).delete()
                getArchivedLogs(context).forEach { it.delete() }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        /**
         * Get total size of all logs.
         */
        fun getTotalLogSize(context: Context): Long {
            return (listOf(getLogFile(context)) + getArchivedLogs(context))
                .sumOf { if (it.exists()) it.length() else 0L }
        }

        /**
         * Export all logs as a single text file.
         */
        fun exportLogs(context: Context): File {
            val exportFile = File(context.cacheDir, "bizap_logs_export.txt")
            val sb = StringBuilder()

            // Add current log
            val currentLog = getLogFile(context)
            if (currentLog.exists()) {
                sb.append("=== CURRENT LOG (${currentLog.name}) ===\n")
                sb.append(currentLog.readText())
                sb.append("\n\n")
            }

            // Add archived logs
            getArchivedLogs(context).forEach { archived ->
                sb.append("=== ARCHIVED LOG (${archived.name}) ===\n")
                sb.append(archived.readText())
                sb.append("\n\n")
            }

            exportFile.writeText(sb.toString())
            return exportFile
        }
    }
}

