package com.emul8r.bizap.data.local

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey

/**
 * DataStore preference keys for GUI3 Matrix immersive effects.
 *
 * All keys use the `effect_` namespace to avoid collisions with other settings.
 * Values are stored with typed keys (Boolean, Float) so DataStore handles serialization.
 *
 * Used by [EffectPreferencesRepositoryImpl] to read/write preferences.
 */
object EffectPreferencesKeys {
    // Rain effect (GPU-accelerated particles)
    val RAIN_ENABLED = booleanPreferencesKey("effect_rain_enabled")
    val RAIN_INTENSITY = floatPreferencesKey("effect_rain_intensity")

    // Glitch effect (color shift + chromatic aberration)
    val GLITCH_ENABLED = booleanPreferencesKey("effect_glitch_enabled")
    val GLITCH_INTENSITY = floatPreferencesKey("effect_glitch_intensity")

    // Scanline effect (CRT-style horizontal lines)
    val SCANLINE_ENABLED = booleanPreferencesKey("effect_scanline_enabled")
    val SCANLINE_INTENSITY = floatPreferencesKey("effect_scanline_intensity")

    // Metadata
    val LAST_UPDATED = booleanPreferencesKey("effect_preferences_last_updated")
}

