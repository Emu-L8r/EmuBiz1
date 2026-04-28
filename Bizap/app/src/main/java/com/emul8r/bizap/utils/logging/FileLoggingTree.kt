package com.emul8r.bizap.utils.logging

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
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
 * - Writes all log levels to file **off the main thread** via a [Channel] + IO coroutine.
 *   Previously used synchronous [File.appendText] on the calling thread, which caused
 *   50–300 ms StrictMode DiskWrite violations on every log call.
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
 *
 * // In Application.onTerminate() / process exit (optional cleanup)
 * fileTree.close()
 * ```
 *
 * **Viewing Logs:**
 * - Access via `FileLoggingTree.getLogFile(context)`
 * - Read as text: `logFile.readText()`
 *
 * **Overflow policy:**
 * Channel capacity is 512 entries. Under extreme burst load [trySend] silently drops
 * entries rather than blocking the caller. Crashlytics independently captures crashes,
 * so no critical diagnostics are lost.
 */
class FileLoggingTree(private val context: Context) : Timber.Tree() {

    private val logFile = File(context.filesDir, "bizap_logs.txt")
    private val maxFileSizeBytes = 5 * 1024 * 1024  // 5 MB
    private val dateFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)

    // Background IO scope — one consumer drains the channel sequentially.
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /**
     * Bounded channel: [trySend] never blocks the main thread. Lines exceeding
     * capacity are silently dropped (logged to stderr only) rather than stalling UI.
     */
    private val logChannel = Channel<String>(capacity = 512)

    init {
        // Start a single background consumer for the channel.
        scope.launch {
            for (line in logChannel) {
                try {
                    logFile.appendText(line)
                    // Rotation check runs in the background consumer — never on main thread.
                    if (logFile.length() > maxFileSizeBytes) {
                        rotateLogFile()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        // Rotate on startup if a previous run left an oversized file.
        scope.launch {
            if (logFile.exists() && logFile.length() > maxFileSizeBytes) {
                rotateLogFile()
            }
        }
    }

    /**
     * Called by Timber on every log event — **must never block the main thread**.
     * Formats the line and enqueues it via [Channel.trySend]; returns immediately.
     */
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val timestamp = dateFormat.format(Date())
        val level = getPriorityLabel(priority)
        val sb = StringBuilder("[$timestamp] [$level] [$tag] $message\n")
        t?.let { sb.append("${it.stackTraceToString()}\n") }

        // trySend is lock-free and non-blocking; drops silently if channel is full.
        val result = logChannel.trySend(sb.toString())
        if (result.isFailure) {
            // Channel full — log to stderr only (avoid re-entrancy into Timber).
            System.err.println("FileLoggingTree: channel full, dropping log line")
        }
    }

    /**
     * Cancels the background IO scope and closes the channel.
     * Call from Application.onTerminate() or a process-exit hook if desired.
     * Safe to call multiple times.
     */
    fun close() {
        logChannel.close()
        scope.cancel()
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

