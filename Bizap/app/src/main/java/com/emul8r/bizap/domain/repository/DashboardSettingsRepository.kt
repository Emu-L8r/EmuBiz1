package com.emul8r.bizap.domain.repository

import com.emul8r.bizap.domain.model.DashboardSettings
import kotlinx.coroutines.flow.Flow

interface DashboardSettingsRepository {
    val settings: Flow<DashboardSettings>
    suspend fun updateSettings(settings: DashboardSettings)
}
