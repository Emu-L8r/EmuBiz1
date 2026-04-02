package com.emul8r.bizap.data.local.dao

import androidx.room.*
import com.emul8r.bizap.domain.model.InvoiceSettings
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO interface for InvoiceSettings database operations.
 * Handles all database interactions for invoice settings persistence.
 */
@Dao
interface InvoiceSettingsDao {

    /**
     * Insert or update invoice settings.
     * If settings with same userId exists, it will be replaced.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: InvoiceSettings)

    /**
     * Get invoice settings for a specific user.
     * Returns null if settings don't exist.
     */
    @Query("SELECT * FROM invoice_settings WHERE user_id = :userId")
    suspend fun getSettings(userId: String): InvoiceSettings?

    /**
     * Get invoice settings as a Flow for reactive updates.
     * Emits new value whenever settings change.
     */
    @Query("SELECT * FROM invoice_settings WHERE user_id = :userId")
    fun getSettingsFlow(userId: String): Flow<InvoiceSettings?>

    /**
     * Delete invoice settings for a user.
     */
    @Delete
    suspend fun deleteSettings(settings: InvoiceSettings)

    /**
     * Delete invoice settings by user ID.
     */
    @Query("DELETE FROM invoice_settings WHERE user_id = :userId")
    suspend fun deleteByUserId(userId: String)

    /**
     * Check if settings exist for a user.
     */
    @Query("SELECT COUNT(*) FROM invoice_settings WHERE user_id = :userId")
    suspend fun exists(userId: String): Boolean
}

