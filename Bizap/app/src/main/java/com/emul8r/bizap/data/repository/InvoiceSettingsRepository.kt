package com.emul8r.bizap.data.repository

import com.emul8r.bizap.data.local.dao.InvoiceSettingsDao
import com.emul8r.bizap.domain.model.InvoiceSettings
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for InvoiceSettings data access.
 *
 * Implements the data access layer for invoice settings persistence.
 * Handles reading/writing settings to the database with proper error handling
 * and initialization of default settings if none exist.
 */
@Singleton
class InvoiceSettingsRepository @Inject constructor(
    private val settingsDao: InvoiceSettingsDao
) {

    /**
     * Get invoice settings for a user.
     * If settings don't exist, creates and returns default settings.
     */
    suspend fun getSettings(userId: String): InvoiceSettings? {
        return settingsDao.getSettings(userId) ?: InvoiceSettings.default(userId).also {
            settingsDao.insertOrUpdate(it)
        }
    }

    /**
     * Get invoice settings as a Flow for reactive updates.
     * Returns null if settings don't exist yet.
     */
    fun getSettingsFlow(userId: String): Flow<InvoiceSettings?> {
        return settingsDao.getSettingsFlow(userId)
    }

    /**
     * Save or update invoice settings.
     * Updates the updatedAt timestamp automatically.
     */
    suspend fun saveSettings(settings: InvoiceSettings) {
        settingsDao.insertOrUpdate(
            settings.copy(updatedAt = System.currentTimeMillis())
        )
    }

    /**
     * Delete invoice settings for a user.
     */
    suspend fun deleteSettings(userId: String) {
        settingsDao.deleteByUserId(userId)
    }

    /**
     * Reset settings to defaults for a user.
     */
    suspend fun resetToDefaults(userId: String) {
        val defaults = InvoiceSettings.default(userId)
        saveSettings(defaults)
    }

    /**
     * Check if settings exist for a user.
     */
    suspend fun exists(userId: String): Boolean {
        return settingsDao.exists(userId)
    }
}
