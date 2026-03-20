package com.emul8r.bizap.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.emul8r.bizap.domain.model.DisplayMode
import com.emul8r.bizap.domain.model.Settings
import com.emul8r.bizap.domain.model.ThemePreference
import com.emul8r.bizap.domain.model.UiDensity
import com.emul8r.bizap.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

/**
 * DataStore-backed implementation of [SettingsRepository].
 *
 * Uses [Preferences] DataStore (key-value) so no custom serializer is required.
 * Each preference has its own typed key and a safe fallback to the [Settings] default
 * so corrupted or missing entries never crash the app.
 */
class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    // ── Preference keys ───────────────────────────────────────────────────

    private object Keys {
        val THEME_PREFERENCE = stringPreferencesKey("settings_theme_preference")
        val DISPLAY_MODE = stringPreferencesKey("settings_display_mode")
        val DEFAULT_INVOICE_STATUS_FILTER = stringPreferencesKey("settings_default_invoice_status_filter")
        val DEFAULT_DAYS_LOOKBACK = intPreferencesKey("settings_default_days_lookback")
        val UI_DENSITY = stringPreferencesKey("settings_ui_density")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("settings_notifications_enabled")
        val EMAIL_NOTIFICATIONS_ENABLED = booleanPreferencesKey("settings_email_notifications_enabled")
        val CURRENCY_CODE = stringPreferencesKey("settings_currency_code")
        val LOCALE_LANGUAGE = stringPreferencesKey("settings_locale_language")
        val AUTO_SYNC_ENABLED = booleanPreferencesKey("settings_auto_sync_enabled")
        val SYNC_FREQUENCY_MINUTES = intPreferencesKey("settings_sync_frequency_minutes")
        val LAST_UPDATED = stringPreferencesKey("settings_last_updated")
    }

    // ── Read ──────────────────────────────────────────────────────────────

    override val settings: Flow<Settings> = dataStore.data
        .catch { e ->
            Timber.e(e, "SettingsRepository: DataStore read error – returning defaults")
            emit(androidx.datastore.preferences.core.emptyPreferences())
        }
        .map { prefs -> prefs.toSettings() }

    // ── Write ─────────────────────────────────────────────────────────────

    override suspend fun updateThemePreference(preference: ThemePreference) {
        safeEdit("updateThemePreference") { it[Keys.THEME_PREFERENCE] = preference.name }
    }

    override suspend fun updateDisplayMode(mode: DisplayMode) {
        safeEdit("updateDisplayMode") { it[Keys.DISPLAY_MODE] = mode.name }
    }

    override suspend fun updateUiDensity(density: UiDensity) {
        safeEdit("updateUiDensity") { it[Keys.UI_DENSITY] = density.name }
    }

    override suspend fun updateDefaultInvoiceStatusFilter(status: String) {
        safeEdit("updateDefaultInvoiceStatusFilter") {
            it[Keys.DEFAULT_INVOICE_STATUS_FILTER] = status
        }
    }

    override suspend fun updateDefaultDaysLookback(days: Int) {
        safeEdit("updateDefaultDaysLookback") { it[Keys.DEFAULT_DAYS_LOOKBACK] = days }
    }

    override suspend fun updateNotificationsEnabled(enabled: Boolean) {
        safeEdit("updateNotificationsEnabled") { it[Keys.NOTIFICATIONS_ENABLED] = enabled }
    }

    override suspend fun updateEmailNotificationsEnabled(enabled: Boolean) {
        safeEdit("updateEmailNotificationsEnabled") {
            it[Keys.EMAIL_NOTIFICATIONS_ENABLED] = enabled
        }
    }

    override suspend fun updateCurrencyCode(code: String) {
        safeEdit("updateCurrencyCode") { it[Keys.CURRENCY_CODE] = code }
    }

    override suspend fun updateLocaleLanguage(language: String) {
        safeEdit("updateLocaleLanguage") { it[Keys.LOCALE_LANGUAGE] = language }
    }

    override suspend fun updateAutoSyncEnabled(enabled: Boolean) {
        safeEdit("updateAutoSyncEnabled") { it[Keys.AUTO_SYNC_ENABLED] = enabled }
    }

    override suspend fun updateSyncFrequencyMinutes(minutes: Int) {
        safeEdit("updateSyncFrequencyMinutes") { it[Keys.SYNC_FREQUENCY_MINUTES] = minutes }
    }

    override suspend fun resetToDefaults() {
        safeEdit("resetToDefaults") { it.clear() }
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private suspend fun safeEdit(
        tag: String,
        block: (MutablePreferences) -> Unit
    ) {
        try {
            dataStore.edit { prefs ->
                block(prefs)
                prefs[Keys.LAST_UPDATED] = System.currentTimeMillis().toString()
            }
        } catch (e: Exception) {
            Timber.e(e, "SettingsRepository.$tag failed")
        }
    }

    private fun Preferences.toSettings(): Settings {
        val defaults = Settings()
        return Settings(
            themePreference = this[Keys.THEME_PREFERENCE]
                ?.let { safeEnumValueOf<ThemePreference>(it) }
                ?: defaults.themePreference,
            displayMode = this[Keys.DISPLAY_MODE]
                ?.let { safeEnumValueOf<DisplayMode>(it) }
                ?: defaults.displayMode,
            defaultInvoiceStatusFilter = this[Keys.DEFAULT_INVOICE_STATUS_FILTER]
                ?: defaults.defaultInvoiceStatusFilter,
            defaultDaysLookback = this[Keys.DEFAULT_DAYS_LOOKBACK]
                ?: defaults.defaultDaysLookback,
            uiDensity = this[Keys.UI_DENSITY]
                ?.let { safeEnumValueOf<UiDensity>(it) }
                ?: defaults.uiDensity,
            notificationsEnabled = this[Keys.NOTIFICATIONS_ENABLED]
                ?: defaults.notificationsEnabled,
            emailNotificationsEnabled = this[Keys.EMAIL_NOTIFICATIONS_ENABLED]
                ?: defaults.emailNotificationsEnabled,
            currencyCode = this[Keys.CURRENCY_CODE] ?: defaults.currencyCode,
            localeLanguage = this[Keys.LOCALE_LANGUAGE] ?: defaults.localeLanguage,
            autoSyncEnabled = this[Keys.AUTO_SYNC_ENABLED] ?: defaults.autoSyncEnabled,
            syncFrequencyMinutes = this[Keys.SYNC_FREQUENCY_MINUTES]
                ?: defaults.syncFrequencyMinutes,
            lastUpdated = this[Keys.LAST_UPDATED]?.toLongOrNull() ?: defaults.lastUpdated
        )
    }

    private inline fun <reified T : Enum<T>> safeEnumValueOf(name: String): T? = try {
        enumValueOf<T>(name)
    } catch (e: IllegalArgumentException) {
        Timber.w("SettingsRepository: unknown enum value '$name' for ${T::class.simpleName}")
        null
    }
}
