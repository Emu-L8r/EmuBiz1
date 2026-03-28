package com.emul8r.bizap.data.local.dao

import androidx.room.*
import com.emul8r.bizap.data.local.entities.DashboardPreferencesEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for dashboard preferences operations.
 *
 * Handles:
 * - Getting user preferences
 * - Updating preferences
 * - Observing changes
 */
@Dao
interface DashboardPreferencesDaoV2 {

    /**
     * Get preferences for a business.
     */
    @Query("SELECT * FROM dashboard_preferences WHERE businessProfileId = :businessId")
    suspend fun getPreferences(businessId: Long): DashboardPreferencesEntity?

    /**
     * Observe preferences changes.
     */
    @Query("SELECT * FROM dashboard_preferences WHERE businessProfileId = :businessId")
    fun observePreferences(businessId: Long): Flow<DashboardPreferencesEntity?>

    /**
     * Insert or update preferences.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePreferences(preferences: DashboardPreferencesEntity)

    /**
     * Update widget order.
     */
    @Query("UPDATE dashboard_preferences SET widgetOrder = :widgetOrder, updatedAt = :now WHERE businessProfileId = :businessId")
    suspend fun updateWidgetOrder(businessId: Long, widgetOrder: String, now: Long = System.currentTimeMillis())

    /**
     * Update hidden widgets.
     */
    @Query("UPDATE dashboard_preferences SET hiddenWidgets = :hiddenWidgets, updatedAt = :now WHERE businessProfileId = :businessId")
    suspend fun updateHiddenWidgets(businessId: Long, hiddenWidgets: String, now: Long = System.currentTimeMillis())

    /**
     * Update pinned metrics.
     */
    @Query("UPDATE dashboard_preferences SET pinnedMetrics = :pinnedMetrics, updatedAt = :now WHERE businessProfileId = :businessId")
    suspend fun updatePinnedMetrics(businessId: Long, pinnedMetrics: String, now: Long = System.currentTimeMillis())

    /**
     * Delete preferences.
     */
    @Query("DELETE FROM dashboard_preferences WHERE businessProfileId = :businessId")
    suspend fun deletePreferences(businessId: Long)
}

