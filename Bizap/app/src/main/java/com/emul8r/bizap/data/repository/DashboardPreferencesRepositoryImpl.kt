package com.emul8r.bizap.data.repository

import com.emul8r.bizap.data.local.dao.DashboardPreferencesDaoV2
import com.emul8r.bizap.data.local.entities.DashboardPreferencesEntity
import com.emul8r.bizap.data.local.entities.DashboardWidget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import timber.log.Timber
import javax.inject.Inject

/**
 * Repository for dashboard preferences.
 *
 * Handles:
 * - Getting user customizations
 * - Updating widget order
 * - Managing hidden/pinned widgets
 */
class DashboardPreferencesRepositoryImpl @Inject constructor(
    private val dashboardPreferencesDao: DashboardPreferencesDaoV2
) : DashboardPreferencesRepository {

    override suspend fun getPreferences(businessId: Long): Result<DashboardPreferencesEntity> =
        withContext(Dispatchers.IO) {
            try {
                val prefs = dashboardPreferencesDao.getPreferences(businessId)
                    ?: DashboardPreferencesEntity(businessProfileId = businessId)
                Result.success(prefs)
            } catch (e: Exception) {
                Timber.e(e, "Failed to get preferences for business: $businessId")
                Result.failure(e)
            }
        }

    override fun observePreferences(businessId: Long): Flow<Result<DashboardPreferencesEntity>> =
        dashboardPreferencesDao.observePreferences(businessId)
            .map { prefs ->
                try {
                    Result.success(prefs ?: DashboardPreferencesEntity(businessProfileId = businessId))
                } catch (e: Exception) {
                    Timber.e(e, "Error observing preferences")
                    Result.failure(e)
                }
            }

    override suspend fun updateWidgetOrder(businessId: Long, widgetOrder: List<DashboardWidget>): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val json = Json.encodeToString(widgetOrder.map { it.name })
                dashboardPreferencesDao.updateWidgetOrder(businessId, json)
                Timber.d("Widget order updated for business: $businessId")
                Result.success(Unit)
            } catch (e: Exception) {
                Timber.e(e, "Failed to update widget order")
                Result.failure(e)
            }
        }

    override suspend fun updateHiddenWidgets(businessId: Long, hiddenWidgets: Set<DashboardWidget>): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val json = Json.encodeToString(hiddenWidgets.map { it.name })
                dashboardPreferencesDao.updateHiddenWidgets(businessId, json)
                Timber.d("Hidden widgets updated for business: $businessId")
                Result.success(Unit)
            } catch (e: Exception) {
                Timber.e(e, "Failed to update hidden widgets")
                Result.failure(e)
            }
        }

    override suspend fun updatePinnedMetrics(businessId: Long, pinnedMetrics: Set<String>): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val json = Json.encodeToString(pinnedMetrics.toList())
                dashboardPreferencesDao.updatePinnedMetrics(businessId, json)
                Timber.d("Pinned metrics updated for business: $businessId")
                Result.success(Unit)
            } catch (e: Exception) {
                Timber.e(e, "Failed to update pinned metrics")
                Result.failure(e)
            }
        }

    override suspend fun toggleWidget(businessId: Long, widget: DashboardWidget, isHidden: Boolean): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val prefs = dashboardPreferencesDao.getPreferences(businessId)
                    ?: return@withContext Result.failure(Exception("Preferences not found"))

                val hiddenList = try {
                    Json.decodeFromString<List<String>>(prefs.hiddenWidgets).toMutableList()
                } catch (e: Exception) {
                    mutableListOf()
                }

                if (isHidden) {
                    if (!hiddenList.contains(widget.name)) {
                        hiddenList.add(widget.name)
                    }
                } else {
                    hiddenList.remove(widget.name)
                }

                val json = Json.encodeToString(hiddenList)
                dashboardPreferencesDao.updateHiddenWidgets(businessId, json)
                Timber.d("Widget ${widget.name} toggled: hidden=$isHidden")
                Result.success(Unit)
            } catch (e: Exception) {
                Timber.e(e, "Failed to toggle widget")
                Result.failure(e)
            }
        }

    override suspend fun resetPreferences(businessId: Long): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                dashboardPreferencesDao.deletePreferences(businessId)
                Timber.d("Preferences reset for business: $businessId")
                Result.success(Unit)
            } catch (e: Exception) {
                Timber.e(e, "Failed to reset preferences")
                Result.failure(e)
            }
        }
}

/**
 * Repository interface for dashboard preferences.
 */
interface DashboardPreferencesRepository {

    /**
     * Get dashboard preferences for a business.
     */
    suspend fun getPreferences(businessId: Long): Result<DashboardPreferencesEntity>

    /**
     * Observe preferences changes.
     */
    fun observePreferences(businessId: Long): Flow<Result<DashboardPreferencesEntity>>

    /**
     * Update widget order.
     */
    suspend fun updateWidgetOrder(businessId: Long, widgetOrder: List<DashboardWidget>): Result<Unit>

    /**
     * Update hidden widgets.
     */
    suspend fun updateHiddenWidgets(businessId: Long, hiddenWidgets: Set<DashboardWidget>): Result<Unit>

    /**
     * Update pinned metrics.
     */
    suspend fun updatePinnedMetrics(businessId: Long, pinnedMetrics: Set<String>): Result<Unit>

    /**
     * Toggle widget visibility.
     */
    suspend fun toggleWidget(businessId: Long, widget: DashboardWidget, isHidden: Boolean): Result<Unit>

    /**
     * Reset all preferences to default.
     */
    suspend fun resetPreferences(businessId: Long): Result<Unit>
}

