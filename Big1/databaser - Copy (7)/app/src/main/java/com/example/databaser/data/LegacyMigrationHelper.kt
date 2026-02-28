package com.example.databaser.data

import android.content.Context
import android.util.Log
import net.sqlcipher.database.SQLiteDatabase
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object LegacyMigrationHelper {
    private const val TAG = "LegacyMigration"
    private const val LEGACY_PASSPHRASE = "test"

    // Convert bytes to hex string (lowercase)
    private fun bytesToHex(bytes: ByteArray): String {
        val hexChars = CharArray(bytes.size * 2)
        val hexArray = "0123456789abcdef".toCharArray()
        for (j in bytes.indices) {
            val v = bytes[j].toInt() and 0xFF
            hexChars[j * 2] = hexArray[v ushr 4]
            hexChars[j * 2 + 1] = hexArray[v and 0x0F]
        }
        return String(hexChars)
    }

    private fun createBackup(dbFile: File, context: Context): File? {
        return try {
            val backupsDir = File(context.filesDir, "db_backups")
            if (!backupsDir.exists()) backupsDir.mkdirs()
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val backupFile = File(backupsDir, "${dbFile.name}_backup_$timestamp")
            dbFile.copyTo(backupFile)
            Log.i(TAG, "Created DB backup at ${backupFile.absolutePath}")
            backupFile
        } catch (t: Throwable) {
            Log.w(TAG, "Failed to create DB backup: ${t.message}")
            null
        }
    }

    /**
     * Attempts to migrate a legacy SQLCipher database protected with the ASCII password "test"
     * to the new keystore-wrapped passphrase provided by PassphraseProvider.
     * Returns migration status and records backup path in MigrationStatusStore.
     */
    fun tryMigrateLegacyDb(context: Context, passphraseProvider: PassphraseProvider, statusStore: MigrationStatusStore): MigrationStatusStore.Status {
        try {
            val dbFile: File = context.getDatabasePath(DATABASE_NAME)
            if (!dbFile.exists()) {
                Log.i(TAG, "No database file found at ${dbFile.path}")
                statusStore.setStatus(MigrationStatusStore.Status.NO_DB)
                return MigrationStatusStore.Status.NO_DB
            }

            // Create a backup before attempting migration
            val backup = createBackup(dbFile, context)
            backup?.let { statusStore.setBackupDir(it.parentFile.absolutePath) }

            // Try opening DB with legacy passphrase
            val legacyBytes = SQLiteDatabase.getBytes(LEGACY_PASSPHRASE.toCharArray())
            var db: SQLiteDatabase? = null
            try {
                db = SQLiteDatabase.openDatabase(dbFile.path, legacyBytes, null, SQLiteDatabase.OPEN_READWRITE)
            } catch (t: Throwable) {
                Log.i(TAG, "Failed to open DB with legacy passphrase: ${t.message}")
                statusStore.setStatus(MigrationStatusStore.Status.FAILURE)
                return MigrationStatusStore.Status.FAILURE
            }

            // Obtain new passphrase
            val newPass = passphraseProvider.getRawPassphrase()
            try {
                val hex = bytesToHex(newPass)
                // Use hex literal to set binary key
                val sql = "PRAGMA rekey = \"x'$hex'\";"
                db.execSQL(sql)
                Log.i(TAG, "Rekey succeeded")
                statusStore.setStatus(MigrationStatusStore.Status.SUCCESS)
                return MigrationStatusStore.Status.SUCCESS
            } catch (t: Throwable) {
                Log.e(TAG, "Rekey failed: ${t.message}", t)
                // Keep backup for recovery
                statusStore.setStatus(MigrationStatusStore.Status.FAILURE)
                return MigrationStatusStore.Status.FAILURE
            } finally {
                try { db.close() } catch (_: Throwable) {}
                // wipe sensitive bytes
                passphraseProvider.wipe(newPass)
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Unexpected migration failure: ${t.message}", t)
            statusStore.setStatus(MigrationStatusStore.Status.FAILURE)
            return MigrationStatusStore.Status.FAILURE
        }
    }
}
