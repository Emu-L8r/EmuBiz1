package com.emul8r.bizap.data.backup

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.emul8r.bizap.data.local.AppDatabase
import timber.log.Timber
import java.io.File
import javax.inject.Inject

/**
 * Service for restoring the Room database from a backup file.
 * Validates backup integrity before restoring and closes the database during the process.
 */
class DatabaseRestoreService @Inject constructor(
    private val context: Context,
    private val appDatabase: AppDatabase
) {

    /**
     * Validates that a file is a valid SQLite database.
     * Checks the file header and attempts to open it.
     *
     * @param backupFile The file to validate
     * @return true if the file is a valid SQLite database, false otherwise
     */
    private fun isValidSQLiteDatabase(backupFile: File): Boolean {
        return try {
            if (!backupFile.exists() || backupFile.length() == 0L) {
                Timber.w("Backup file does not exist or is empty")
                return false
            }

            // Check SQLite file header (first 16 bytes should be "SQLite format 3")
            val header = ByteArray(16)
            backupFile.inputStream().use { it.read(header) }
            val headerString = String(header, Charsets.UTF_8)

            if (!headerString.startsWith("SQLite format 3")) {
                Timber.w("Invalid SQLite header in backup file")
                return false
            }

            // Try to open the database to verify it's not corrupted
            val testDb = SQLiteDatabase.openDatabase(
                backupFile.absolutePath,
                null,
                SQLiteDatabase.OPEN_READONLY
            )
            val version = testDb.version
            testDb.close()

            Timber.d("Backup validation successful. DB version: $version")
            true

        } catch (e: Exception) {
            Timber.e(e, "Backup validation failed")
            false
        }
    }

    /**
     * Restores the database from a backup file.
     * Closes the current database, validates the backup, copies it over, and cleans up temporary files.
     * The app process should be restarted after calling this for Room to pick up the new database.
     *
     * @param backupFile The backup file to restore from
     * @return Result<Unit> with success or error message
     */
    suspend fun restoreFromBackup(backupFile: File): Result<Unit> {
        return try {
            Timber.d("Starting database restore from ${backupFile.absolutePath}")

            // Validate the backup file before proceeding
            if (!isValidSQLiteDatabase(backupFile)) {
                return Result.failure(Exception("Backup file is not a valid SQLite database"))
            }

            // Close the current database
            appDatabase.close()
            Timber.d("Current database closed for restore")

            val databaseFile = context.getDatabasePath("bizap-db")
            val shmFile = context.getDatabasePath("bizap-db-shm")
            val walFile = context.getDatabasePath("bizap-db-wal")

            // Remove old shared memory and WAL files
            // (these will be regenerated from the main database file)
            if (shmFile.exists()) {
                shmFile.delete()
                Timber.d("Deleted old shared memory file")
            }
            if (walFile.exists()) {
                walFile.delete()
                Timber.d("Deleted old write-ahead log file")
            }

            // Copy the backup file over the existing database
            backupFile.copyTo(databaseFile, overwrite = true)
            Timber.d("Restored database from backup")

            // Copy related files from backup if they exist
            val backupShmFile = File(
                backupFile.parentFile,
                backupFile.name.replace(".db", ".db-shm")
            )
            if (backupShmFile.exists()) {
                backupShmFile.copyTo(shmFile, overwrite = true)
                Timber.d("Restored shared memory file")
            }

            val backupWalFile = File(
                backupFile.parentFile,
                backupFile.name.replace(".db", ".db-wal")
            )
            if (backupWalFile.exists()) {
                backupWalFile.copyTo(walFile, overwrite = true)
                Timber.d("Restored write-ahead log file")
            }

            Timber.d("Database restore completed successfully")
            Timber.w("APP RESTART REQUIRED: The app process must restart for Room to recognize the new database")

            Result.success(Unit)

        } catch (e: Exception) {
            Timber.e(e, "Database restore failed")
            Result.failure(e)
        }
    }

    /**
     * Triggers a restart of the app process.
     * This must be called after a successful restore so Room picks up the new database.
     *
     * @param context Android context
     */
    fun restartApp(context: Context) {
        try {
            val packageManager = context.packageManager
            val intent = packageManager.getLaunchIntentForPackage(context.packageName)
            if (intent != null) {
                Timber.d("Restarting app process")
                context.startActivity(intent)
                Runtime.getRuntime().exit(0)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to restart app")
        }
    }
}



