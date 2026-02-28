package com.example.databaser.data

import android.content.Context

class MigrationStatusStore(private val context: Context) {
    companion object {
        private const val PREFS_NAME = "migration_prefs"
        private const val KEY_STATUS = "migration_status"
        private const val KEY_BACKUP_DIR = "migration_backup_dir"
    }

    enum class Status { NONE, SUCCESS, FAILURE, NO_DB }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun setStatus(status: Status) {
        prefs.edit().putString(KEY_STATUS, status.name).apply()
    }

    fun getStatus(): Status {
        val name = prefs.getString(KEY_STATUS, Status.NONE.name) ?: Status.NONE.name
        return try { Status.valueOf(name) } catch (_: Exception) { Status.NONE }
    }

    fun setBackupDir(path: String) {
        prefs.edit().putString(KEY_BACKUP_DIR, path).apply()
    }

    fun getBackupDir(): String? = prefs.getString(KEY_BACKUP_DIR, null)
}

