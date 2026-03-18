package com.emul8r.bizap.domain.usecase.settings

import com.emul8r.bizap.domain.repository.SettingsRepository
import javax.inject.Inject

/** Persists background-sync preference changes. */
class UpdateSyncSettingsUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    suspend fun setAutoSyncEnabled(enabled: Boolean) =
        repository.updateAutoSyncEnabled(enabled)

    suspend fun setSyncFrequencyMinutes(minutes: Int) =
        repository.updateSyncFrequencyMinutes(minutes)
}
