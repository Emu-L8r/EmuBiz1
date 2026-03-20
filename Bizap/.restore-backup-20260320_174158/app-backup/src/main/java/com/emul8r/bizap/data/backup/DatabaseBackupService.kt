package com.emul8r.bizap.data.backup

import android.content.Context
import android.net.Uri
import com.emul8r.bizap.data.local.AppDatabase
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * Service for backing up the Room database to user-selected storage.
 * Handles closing the database, copying all database files, and managing backup metadata.
 */
class DatabaseBackupService @Inject constructor(
    private val context: Context,
    private val appDatabase: AppDatabase
) {

    /**
     * Creates a backup of the Room database.
     * Closes the database before copying to ensure consistency.
     * Copies all three SQLite files: main DB, shared memory, and write-ahead log.
     *
     * @param backupUri The Uri of the directory where the backup should be saved
     * @return Result<File> containing the backup file path or error message
     */
    suspend fun createBackup(backupUri: Uri): Result<File> {
        return try {
            Timber.d("Starting database backup to $backupUri")

            // Close the database to ensure all writes are flushed
            appDatabase.close()
            Timber.d("Database closed for backup")

            val databaseFile = context.getDatabasePath("bizap-db")
            val shmFile = context.getDatabasePath("bizap-db-shm")
            val walFile = context.getDatabasePath("bizap-db-wal")

            if (!databaseFile.exists()) {
                Timber.e("Database file not found at ${databaseFile.absolutePath}")
                return Result.failure(Exception("Database file not found"))
            }

            // Create output directory if it doesn't exist
            val outputDir = if (backupUri.toString().startsWith("file://")) {
                File(backupUri.path!!)
            } else {
                // For content:// URIs, create in app's cache directory
                File(context.cacheDir, "backups").apply { mkdirs() }
            }

            val timestamp = SimpleDateFormat("yyyy-MM-dd-HHmmss", Locale.US).format(Date())
            val backupFile = File(outputDir, "bizap-backup-$timestamp.db")

            // Copy main database file
            databaseFile.copyTo(backupFile, overwrite = true)
            Timber.d("Backed up main database to ${backupFile.absolutePath}")

            // Copy shared memory file if it exists (helps with recovery)
            if (shmFile.exists()) {
                val backupShmFile = File(outputDir, "bizap-backup-$timestamp.db-shm")
                shmFile.copyTo(backupShmFile, overwrite = true)
                Timber.d("Backed up shared memory file")
            }

            // Copy write-ahead log file if it exists
            if (walFile.exists()) {
                val backupWalFile = File(outputDir, "bizap-backup-$timestamp.db-wal")
                walFile.copyTo(backupWalFile, overwrite = true)
                Timber.d("Backed up write-ahead log file")
            }

            Timber.d("Backup completed successfully: ${backupFile.absolutePath} (${backupFile.length()} bytes)")
            Result.success(backupFile)

        } catch (e: Exception) {
            Timber.e(e, "Database backup failed")
            Result.failure(e)
        }
    }

    /**
     * Gets the size of a backup file in bytes.
     *
     * @param backupFile The backup file to measure
     * @return Size in bytes
     */
    fun getBackupSize(backupFile: File): Long {
        var size = backupFile.length()

        // Include related files if they exist
        val shmFile = File(backupFile.parentFile, backupFile.name.replace(".db", ".db-shm"))
        val walFile = File(backupFile.parentFile, backupFile.name.replace(".db", ".db-wal"))

        if (shmFile.exists()) size += shmFile.length()
        if (walFile.exists()) size += walFile.length()

        return size
    }

    /**
     * Gets the last modification time of a backup file.
     *
     * @param backupFile The backup file
     * @return Formatted date string, or empty if file doesn't exist
     */
    fun getBackupDateTime(backupFile: File): String {
        if (!backupFile.exists()) return ""
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(backupFile.lastModified()))
    }
}


