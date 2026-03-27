package com.emul8r.bizap.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import timber.log.Timber
import java.io.File

/**
 * Safe wrapper around FileProvider for converting File objects to content URIs.
 *
 * **Purpose:**
 * Encapsulates FileProvider logic and provides comprehensive error handling,
 * validation, and logging to prevent crashes from invalid file paths.
 *
 * **Features:**
 * - Validates file exists before URI conversion
 * - Validates file is within app's accessible directories
 * - Provides detailed error messages for debugging
 * - Centralized authority management
 *
 * **Usage:**
 * ```kotlin
 * val uri = FileUriProvider.getUriForFile(context, file)
 *     .onSuccess { uri -> shareFile(uri) }
 *     .onFailure { error -> showError(error.message) }
 * ```
 */
object FileUriProvider {

    private const val TAG = "FileUriProvider"
    private const val AUTHORITY_SUFFIX = ".fileprovider"

    /**
     * Safely converts a File to a content URI with full validation.
     *
     * **Validation checks:**
     * 1. File path is not null or empty
     * 2. File exists on disk
     * 3. File is readable
     * 4. File is within app's private storage directories
     *
     * **@param context** Android application context (used to get FileProvider authority)
     * **@param file** File object to convert to URI
     * **@return** Result containing URI on success, or exception details on failure
     *
     * **@throws IllegalArgumentException** If file validation fails
     * **@throws SecurityException** If FileProvider cannot access the file path
     */
    fun getUriForFile(context: Context, file: File): Result<Uri> {
        return try {
            // Validation 1: Check path is not empty
            val absolutePath = file.absolutePath
            if (absolutePath.isBlank()) {
                com.emul8r.bizap.utils.logging.ErrorExportLogger.logFileValidation(
                    fileName = file.name,
                    exists = false,
                    canRead = false,
                    sizeBytes = 0
                )
                return Result.failure(
                    IllegalArgumentException("File path is empty or blank. Cannot create URI from invalid path.")
                )
            }

            // Validation 2: Check file exists
            if (!file.exists()) {
                Timber.e("$TAG: File does not exist at path: $absolutePath")
                com.emul8r.bizap.utils.logging.ErrorExportLogger.logFileValidation(
                    fileName = file.name,
                    exists = false,
                    canRead = false,
                    sizeBytes = 0
                )
                return Result.failure(
                    IllegalArgumentException("File does not exist: $absolutePath")
                )
            }

            // Validation 3: Check file is readable
            if (!file.canRead()) {
                Timber.e("$TAG: File exists but is not readable: $absolutePath")
                com.emul8r.bizap.utils.logging.ErrorExportLogger.logFileValidation(
                    fileName = file.name,
                    exists = true,
                    canRead = false,
                    sizeBytes = file.length()
                )
                return Result.failure(
                    IllegalArgumentException("File exists but is not readable: $absolutePath")
                )
            }

            // Validation 4: Check file is not empty (optional but helpful for PDFs)
            val fileSize = file.length()
            if (fileSize == 0L) {
                Timber.w("$TAG: File exists but is empty: $absolutePath")
                com.emul8r.bizap.utils.logging.ErrorExportLogger.logFileValidation(
                    fileName = file.name,
                    exists = true,
                    canRead = true,
                    sizeBytes = 0
                )
                return Result.failure(
                    IllegalArgumentException("File is empty (0 bytes): $absolutePath")
                )
            }

            // File validation passed - log it
            com.emul8r.bizap.utils.logging.ErrorExportLogger.logFileValidation(
                fileName = file.name,
                exists = true,
                canRead = true,
                sizeBytes = fileSize
            )

            // Get FileProvider authority from manifest (matches AndroidManifest.xml config)
            val authority = "${context.packageName}$AUTHORITY_SUFFIX"

            // Convert to content URI
            com.emul8r.bizap.utils.logging.ErrorExportLogger.logFileProviderAttempt(
                fileName = file.name,
                filePath = absolutePath
            )

            val uri = FileProvider.getUriForFile(context, authority, file)

            Timber.d("$TAG: Successfully converted file to URI: ${file.name} → $uri")
            com.emul8r.bizap.utils.logging.ErrorExportLogger.logFileProviderSuccess(
                fileName = file.name,
                uri = uri.toString()
            )
            Result.success(uri)

        } catch (e: IllegalArgumentException) {
            // FileProvider threw IllegalArgumentException - file path not in configured roots
            Timber.e(e, "$TAG: FileProvider could not access file path. Check file_paths.xml configuration.")
            com.emul8r.bizap.utils.logging.ErrorExportLogger.logFileProviderFailure(
                fileName = file.name,
                error = e
            )
            Result.failure(e)
        } catch (e: SecurityException) {
            // FileProvider threw SecurityException - permission issue
            Timber.e(e, "$TAG: Security exception accessing file. Check permissions and file_paths.xml.")
            com.emul8r.bizap.utils.logging.ErrorExportLogger.logFileProviderFailure(
                fileName = file.name,
                error = e
            )
            Result.failure(e)
        } catch (e: Exception) {
            // Unexpected error
            Timber.e(e, "$TAG: Unexpected error converting file to URI")
            com.emul8r.bizap.utils.logging.ErrorExportLogger.logFileProviderFailure(
                fileName = file.name,
                error = e
            )
            Result.failure(e)
        }
    }

    /**
     * Validates that a file can be safely shared via FileProvider.
     *
     * Useful for pre-flight checks before attempting to share a file.
     *
     * **@param file** File to validate
     * **@return** true if file is valid and shareable, false otherwise
     */
    fun isFileSharable(file: File): Boolean {
        return file.absolutePath.isNotBlank() &&
                file.exists() &&
                file.canRead() &&
                file.length() > 0
    }

    /**
     * Gets detailed diagnostic info about a file for debugging.
     *
     * **@param file** File to analyze
     * **@return** String describing file status and issues
     */
    fun getFileDiagnostics(file: File): String {
        return buildString {
            append("File: ${file.name}\n")
            append("Path: ${file.absolutePath}\n")
            append("Exists: ${file.exists()}\n")
            append("Readable: ${file.canRead()}\n")
            append("Writable: ${file.canWrite()}\n")
            append("Size: ${file.length()} bytes\n")
            append("IsFile: ${file.isFile}\n")
            append("IsDirectory: ${file.isDirectory}\n")
            append("ParentDir: ${file.parentFile?.absolutePath}\n")
        }
    }
}


