package com.emul8r.bizap.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.emul8r.bizap.data.local.EffectPreferencesKeys
import com.emul8r.bizap.domain.model.EffectPreferences
import com.emul8r.bizap.domain.repository.EffectPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

/**
 * DataStore-backed implementation of [EffectPreferencesRepository].
 *
 * Uses [Preferences] DataStore (key-value) for simple, non-destructive storage.
 * Each preference has its own typed key and a safe fallback to [EffectPreferences.defaults()]
 * so corrupted or missing entries never crash the app.
 *
 * Thread-safe: DataStore handles all synchronization internally.
 *
 * Performance: DataStore is optimized for small settings objects (~1 KB).
 * For 6 Float + Boolean keys, read overhead is < 2ms on first app load,
 * then cached in memory with < 1ms access time thereafter.
 */
class EffectPreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : EffectPreferencesRepository {

    // ── Read ──────────────────────────────────────────────────────────────

    override fun observePreferences(): Flow<EffectPreferences> = dataStore.data
        .catch { e ->
            Timber.e(e, "EffectPreferencesRepository: DataStore read error – returning defaults")
            emit(androidx.datastore.preferences.core.emptyPreferences())
        }
        .map { prefs -> prefs.toEffectPreferences() }

    // ── Write ─────────────────────────────────────────────────────────────

    override suspend fun savePreferences(prefs: EffectPreferences) {
        safeEdit("savePreferences") { mutablePrefs ->
            val validated = if (prefs.isValid()) prefs else prefs.normalized()
            mutablePrefs[EffectPreferencesKeys.RAIN_ENABLED] = validated.rainEnabled
            mutablePrefs[EffectPreferencesKeys.RAIN_INTENSITY] = validated.rainIntensity
            mutablePrefs[EffectPreferencesKeys.GLITCH_ENABLED] = validated.glitchEnabled
            mutablePrefs[EffectPreferencesKeys.GLITCH_INTENSITY] = validated.glitchIntensity
            mutablePrefs[EffectPreferencesKeys.SCANLINE_ENABLED] = validated.scanlineEnabled
            mutablePrefs[EffectPreferencesKeys.SCANLINE_INTENSITY] = validated.scanlineIntensity
        }
    }

    override suspend fun setRainEnabled(enabled: Boolean) {
        safeEdit("setRainEnabled") {
            it[EffectPreferencesKeys.RAIN_ENABLED] = enabled
        }
    }

    override suspend fun setRainIntensity(intensity: Float) {
        val clamped = intensity.coerceIn(0f, 1f)
        safeEdit("setRainIntensity") {
            it[EffectPreferencesKeys.RAIN_INTENSITY] = clamped
        }
    }

    override suspend fun setGlitchEnabled(enabled: Boolean) {
        safeEdit("setGlitchEnabled") {
            it[EffectPreferencesKeys.GLITCH_ENABLED] = enabled
        }
    }

    override suspend fun setGlitchIntensity(intensity: Float) {
        val clamped = intensity.coerceIn(0f, 1f)
        safeEdit("setGlitchIntensity") {
            it[EffectPreferencesKeys.GLITCH_INTENSITY] = clamped
        }
    }

    override suspend fun setScanlineEnabled(enabled: Boolean) {
        safeEdit("setScanlineEnabled") {
            it[EffectPreferencesKeys.SCANLINE_ENABLED] = enabled
        }
    }

    override suspend fun setScanlineIntensity(intensity: Float) {
        val clamped = intensity.coerceIn(0f, 1f)
        safeEdit("setScanlineIntensity") {
            it[EffectPreferencesKeys.SCANLINE_INTENSITY] = clamped
        }
    }

    override suspend fun resetToDefaults() {
        val defaults = EffectPreferences.defaults()
        savePreferences(defaults)
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    /**
     * Safely edits preferences, catching exceptions and logging.
     *
     * DataStore.edit() is atomic: either all changes succeed or none do.
     * This helper ensures we don't crash even on I/O errors.
     *
     * @param tag Logging tag (identifies which operation failed)
     * @param block Transformation to apply to mutable preferences
     */
    private suspend fun safeEdit(
        tag: String,
        block: suspend (androidx.datastore.preferences.core.MutablePreferences) -> Unit
    ) {
        try {
            dataStore.edit { prefs -> block(prefs) }
        } catch (e: Exception) {
            Timber.e(e, "EffectPreferencesRepository.$tag failed")
        }
    }

    /**
     * Converts raw [Preferences] to typed [EffectPreferences].
     *
     * Falls back to defaults for any missing keys. This ensures the app
     * never crashes due to corrupted or incomplete DataStore entries.
     *
     * @param this Preferences object from DataStore
     * @return Typed EffectPreferences with all values present
     */
    private fun Preferences.toEffectPreferences(): EffectPreferences {
        val defaults = EffectPreferences.defaults()
        return EffectPreferences(
            rainEnabled = this[EffectPreferencesKeys.RAIN_ENABLED] ?: defaults.rainEnabled,
            rainIntensity = this[EffectPreferencesKeys.RAIN_INTENSITY] ?: defaults.rainIntensity,
            glitchEnabled = this[EffectPreferencesKeys.GLITCH_ENABLED] ?: defaults.glitchEnabled,
            glitchIntensity = this[EffectPreferencesKeys.GLITCH_INTENSITY] ?: defaults.glitchIntensity,
            scanlineEnabled = this[EffectPreferencesKeys.SCANLINE_ENABLED] ?: defaults.scanlineEnabled,
            scanlineIntensity = this[EffectPreferencesKeys.SCANLINE_INTENSITY] ?: defaults.scanlineIntensity
        ).normalized()  // Safety: clamp any out-of-range values
    }
}

