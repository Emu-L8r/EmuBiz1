package com.emul8r.bizap.domain.usecase.settings

import com.emul8r.bizap.domain.model.Settings
import com.emul8r.bizap.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Returns a [Flow] that emits the latest [Settings] snapshot whenever any preference changes.
 *
 * Usage:
 * ```kotlin
 * val settings by getSettingsUseCase().collectAsStateWithLifecycle(Settings())
 * ```
 */
class GetSettingsUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    operator fun invoke(): Flow<Settings> = repository.settings
}
