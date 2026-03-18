package com.emul8r.bizap.domain.usecase.settings

import com.emul8r.bizap.domain.repository.SettingsRepository
import javax.inject.Inject

/** Resets every user preference to its factory default value. */
class ResetSettingsToDefaultUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke() = repository.resetToDefaults()
}
