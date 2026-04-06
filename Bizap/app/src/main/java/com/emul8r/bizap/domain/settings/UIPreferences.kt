package com.emul8r.bizap.domain.settings

import com.emul8r.bizap.domain.model.UIMode
import kotlinx.coroutines.flow.Flow

/**
 * Contract for reading and persisting the user's UI mode preference.
 */
interface UIPreferences {
    /** Emits the current [UIMode] and subsequent changes. */
    val uiMode: Flow<UIMode>

    /** Persists [mode] so it survives app restarts. */
    suspend fun setUIMode(mode: UIMode)
}
