package com.emul8r.bizap.domain.repository

import com.emul8r.bizap.domain.model.DisplayMode
import com.emul8r.bizap.domain.model.Settings
import com.emul8r.bizap.domain.model.ThemePreference
import com.emul8r.bizap.domain.model.UiDensity
import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth for all user preferences.
 *
 * All reads are exposed as [Flow]s so the UI can react to changes without polling.
 * All writes are suspending so they are safe to call from any coroutine.
 */
interface SettingsRepository {

    /** Emits the full [Settings] snapshot whenever any preference changes. */
    val settings: Flow<Settings>

    // ── Theme ──────────────────────────────────────────────────────────────

    suspend fun updateThemePreference(preference: ThemePreference)

    // ── Display ────────────────────────────────────────────────────────────

    suspend fun updateDisplayMode(mode: DisplayMode)

    suspend fun updateUiDensity(density: UiDensity)

    // ── Invoice defaults ───────────────────────────────────────────────────

    suspend fun updateDefaultInvoiceStatusFilter(status: String)

    suspend fun updateDefaultDaysLookback(days: Int)

    // ── Notifications ──────────────────────────────────────────────────────

    suspend fun updateNotificationsEnabled(enabled: Boolean)

    suspend fun updateEmailNotificationsEnabled(enabled: Boolean)

    // ── Locale / currency ──────────────────────────────────────────────────

    suspend fun updateCurrencyCode(code: String)

    suspend fun updateLocaleLanguage(language: String)

    // ── Sync ───────────────────────────────────────────────────────────────

    suspend fun updateAutoSyncEnabled(enabled: Boolean)

    suspend fun updateSyncFrequencyMinutes(minutes: Int)

    // ── Reset ──────────────────────────────────────────────────────────────

    /** Resets every preference to its factory default. */
    suspend fun resetToDefaults()
}
