package com.emul8r.bizap.domain.usecase.settings

import com.emul8r.bizap.domain.model.ThemePreference
import com.emul8r.bizap.domain.repository.SettingsRepository
import javax.inject.Inject

/** Persists a new [ThemePreference] (LIGHT / DARK / AUTO). */
class UpdateThemeUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(preference: ThemePreference) =
        repository.updateThemePreference(preference)
}
