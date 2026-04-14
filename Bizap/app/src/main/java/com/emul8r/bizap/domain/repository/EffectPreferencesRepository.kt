package com.emul8r.bizap.domain.repository

import com.emul8r.bizap.domain.model.EffectPreferences
import kotlinx.coroutines.flow.Flow

/**
 * Repository for user preferences controlling GUI3 Matrix immersive effects.
 *
 * Provides read/write access to effect toggles and intensity settings.
 * All operations are GUI-agnostic (thread-safe, observable via Flow).
 *
 * Implementations must:
 * - Persist preferences to local storage (e.g., DataStore)
 * - Return `Flow<EffectPreferences>` to enable reactive UI updates
 * - Handle corruption gracefully (return defaults, not throw)
 *
 * Lifecycle:
 * - On app launch: Load from DataStore or return defaults
 * - On preference change: Emit to Flow immediately
 * - On app pause: Preferences persist across restarts
 */
interface EffectPreferencesRepository {

    /**
     * Observes effect preferences as a [Flow].
     *
     * Emits current values immediately and then each subsequent update
     * (from [savePreferences] calls).
     *
     * On DataStore read error: Falls back to [EffectPreferences.defaults()].
     *
     * @return Flow<EffectPreferences> that never completes during app lifetime
     */
    fun observePreferences(): Flow<EffectPreferences>

    /**
     * Saves effect preferences to persistent storage.
     *
     * Validated before save: [EffectPreferences.isValid()] must return true
     * or values are clamped via [EffectPreferences.normalized()].
     *
     * Emits to [observePreferences] Flow after successful save.
     *
     * @param prefs Preferences to save
     * @throws Exception Only in unrecoverable errors (corrupted DataStore file); caught by implementation
     */
    suspend fun savePreferences(prefs: EffectPreferences)

    /**
     * Convenience method: toggle rain effect on/off, keeping other preferences.
     *
     * Equivalent to:
     * ```kotlin
     * val current = observePreferences().first()
     * savePreferences(current.copy(rainEnabled = enabled))
     * ```
     *
     * @param enabled New rain toggle state
     */
    suspend fun setRainEnabled(enabled: Boolean)

    /**
     * Convenience method: set rain intensity, keeping other preferences.
     *
     * Value is clamped to [0.0f, 1.0f] range before save.
     *
     * @param intensity Rain particle density (0.0–1.0)
     */
    suspend fun setRainIntensity(intensity: Float)

    /**
     * Convenience method: toggle glitch effect on/off, keeping other preferences.
     *
     * @param enabled New glitch toggle state
     */
    suspend fun setGlitchEnabled(enabled: Boolean)

    /**
     * Convenience method: set glitch intensity, keeping other preferences.
     *
     * Value is clamped to [0.0f, 1.0f] range before save.
     *
     * @param intensity Glitch color shift intensity (0.0–1.0)
     */
    suspend fun setGlitchIntensity(intensity: Float)

    /**
     * Convenience method: toggle scanline effect on/off, keeping other preferences.
     *
     * @param enabled New scanline toggle state
     */
    suspend fun setScanlineEnabled(enabled: Boolean)

    /**
     * Convenience method: set scanline intensity, keeping other preferences.
     *
     * Value is clamped to [0.0f, 1.0f] range before save.
     *
     * @param intensity Scanline alpha/flicker rate (0.0–1.0)
     */
    suspend fun setScanlineIntensity(intensity: Float)

    /**
     * Resets all preferences to [EffectPreferences.defaults()].
     */
    suspend fun resetToDefaults()
}

