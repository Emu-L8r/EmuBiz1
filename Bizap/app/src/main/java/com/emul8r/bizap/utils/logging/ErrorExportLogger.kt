package com.emul8r.bizap.utils.logging

import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

/**
 * Specialized logger for PDF export operations.
 *
 * **Purpose:**
 * Provides structured logging for all PDF export events.
 * Makes it easy to find PDF-related logs by using consistent format.
 *
 * **Features:**
 * - Standardized format for all PDF operations
 * - Easy to search in log files
 * - Tracks: attempts, successes, failures
 * - Includes device context info
 *
 * **Usage:**
 * ```kotlin
 * val logger = ErrorExportLogger()
 * logger.logPdfAttempt(invoiceId = 123)
 * logger.logPdfSuccess(invoiceId = 123, filePath = "/path/to/file.pdf", sizeBytes = 45678)
 * logger.logPdfFailure(invoiceId = 123, error = exception)
 * ```
 */
object ErrorExportLogger {

    private val dateFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)

    /**
     * Log the start of a PDF export attempt.
     */
    fun logPdfAttempt(invoiceId: Long, type: String = "Invoice") {
        val timestamp = dateFormat.format(Date())
        Timber.d("📄 PDF_EXPORT_ATTEMPT [$timestamp] invoiceId=$invoiceId type=$type")
    }

    /**
     * Log successful PDF export.
     */
    fun logPdfSuccess(invoiceId: Long, filePath: String, sizeBytes: Long, type: String = "Invoice") {
        val timestamp = dateFormat.format(Date())
        Timber.d(
            "✅ PDF_EXPORT_SUCCESS [$timestamp] invoiceId=$invoiceId type=$type " +
            "filePath=$filePath sizeBytes=$sizeBytes"
        )
    }

    /**
     * Log PDF export failure.
     */
    fun logPdfFailure(invoiceId: Long, error: Exception, type: String = "Invoice") {
        val timestamp = dateFormat.format(Date())
        Timber.e(error,
            "❌ PDF_EXPORT_FAILURE [$timestamp] invoiceId=$invoiceId type=$type " +
            "errorMsg=${error.message}"
        )
    }

    /**
     * Log FileProvider URI conversion attempt.
     */
    fun logFileProviderAttempt(fileName: String, filePath: String) {
        val timestamp = dateFormat.format(Date())
        Timber.d("🔗 FILEPROVIDER_ATTEMPT [$timestamp] fileName=$fileName")
    }

    /**
     * Log FileProvider URI conversion success.
     */
    fun logFileProviderSuccess(fileName: String, uri: String) {
        val timestamp = dateFormat.format(Date())
        Timber.d("✅ FILEPROVIDER_SUCCESS [$timestamp] fileName=$fileName uri=$uri")
    }

    /**
     * Log FileProvider URI conversion failure.
     */
    fun logFileProviderFailure(fileName: String, error: Exception) {
        val timestamp = dateFormat.format(Date())
        Timber.e(error,
            "❌ FILEPROVIDER_FAILURE [$timestamp] fileName=$fileName " +
            "errorMsg=${error.message}"
        )
    }

    /**
     * Log file validation step.
     */
    fun logFileValidation(
        fileName: String,
        exists: Boolean,
        canRead: Boolean,
        sizeBytes: Long
    ) {
        val timestamp = dateFormat.format(Date())
        Timber.d(
            "🔍 FILE_VALIDATION [$timestamp] fileName=$fileName " +
            "exists=$exists canRead=$canRead sizeBytes=$sizeBytes"
        )
    }

    /**
     * Log share intent launch.
     */
    fun logShareIntent(mimeType: String, fileName: String) {
        val timestamp = dateFormat.format(Date())
        Timber.d("📤 SHARE_INTENT [$timestamp] mimeType=$mimeType fileName=$fileName")
    }

    /**
     * Log download/export to Downloads folder.
     */
    fun logExportToDownloads(fileName: String, destinationPath: String) {
        val timestamp = dateFormat.format(Date())
        Timber.d("💾 EXPORT_TO_DOWNLOADS [$timestamp] fileName=$fileName dest=$destinationPath")
    }

    /**
     * Log print operation.
     */
    fun logPrintOperation(fileName: String) {
        val timestamp = dateFormat.format(Date())
        Timber.d("🖨️ PRINT_OPERATION [$timestamp] fileName=$fileName")
    }

    /**
     * Log CSV export.
     */
    fun logCsvExport(invoiceId: Long, filePath: String, sizeBytes: Long) {
        val timestamp = dateFormat.format(Date())
        Timber.d(
            "📊 CSV_EXPORT_SUCCESS [$timestamp] invoiceId=$invoiceId " +
            "filePath=$filePath sizeBytes=$sizeBytes"
        )
    }

    /**
     * Log CSV export failure.
     */
    fun logCsvExportFailure(invoiceId: Long, error: Exception) {
        val timestamp = dateFormat.format(Date())
        Timber.e(error,
            "❌ CSV_EXPORT_FAILURE [$timestamp] invoiceId=$invoiceId " +
            "errorMsg=${error.message}"
        )
    }
}

