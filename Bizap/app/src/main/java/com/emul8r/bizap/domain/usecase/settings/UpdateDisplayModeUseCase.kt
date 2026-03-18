package com.emul8r.bizap.domain.usecase.settings

import com.emul8r.bizap.domain.model.DisplayMode
import com.emul8r.bizap.domain.repository.SettingsRepository
import javax.inject.Inject

/** Persists a new [DisplayMode] (LIST_VIEW / GRID_VIEW / CARD_VIEW). */
class UpdateDisplayModeUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(mode: DisplayMode) =
        repository.updateDisplayMode(mode)
}
