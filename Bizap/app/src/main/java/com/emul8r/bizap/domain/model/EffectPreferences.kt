package com.emul8r.bizap.domain.model

/**
 * User preferences for GUI3 Matrix immersive effects.
 *
 * Encapsulates toggles and intensity settings for the three Phase 1 effects:
 * - Rain particles (GPU-accelerated cascading code)
 * - Glitch effect (color shift + chromatic aberration)
 * - Scanlines (CRT-style horizontal lines with flicker)
 *
 * All intensity values range from 0.0f (off) to 1.0f (maximum).
 * Stored in DataStore for persistence across app restarts.
 *
 * @param rainEnabled Whether rain particle effect is active
 * @param rainIntensity Rain particle density (0.0–1.0), default 0.7f
 * @param glitchEnabled Whether glitch effect is active
 * @param glitchIntensity Glitch color shift intensity (0.0–1.0), default 0.5f
 * @param scanlineEnabled Whether CRT scanline effect is active
 * @param scanlineIntensity Scanline alpha/flicker rate (0.0–1.0), default 0.6f
 */
data class EffectPreferences(
    val rainEnabled: Boolean = true,
    val rainIntensity: Float = 0.7f,
    val glitchEnabled: Boolean = true,
    val glitchIntensity: Float = 0.5f,
    val scanlineEnabled: Boolean = true,
    val scanlineIntensity: Float = 0.6f
) {
    /**
     * Validates all intensity values are in [0.0f, 1.0f] range.
     * Returns `true` if all values are valid, `false` otherwise.
     *
     * Called by repository before persisting to DataStore.
     */
    fun isValid(): Boolean =
        rainIntensity in 0f..1f &&
        glitchIntensity in 0f..1f &&
        scanlineIntensity in 0f..1f

    /**
     * Clamps all intensity values to [0.0f, 1.0f] range.
     * Used as a safety measure when loading from corrupted DataStore.
     */
    fun normalized(): EffectPreferences = copy(
        rainIntensity = rainIntensity.coerceIn(0f, 1f),
        glitchIntensity = glitchIntensity.coerceIn(0f, 1f),
        scanlineIntensity = scanlineIntensity.coerceIn(0f, 1f)
    )

    companion object {
        /**
         * Default factory: all effects enabled at moderate-to-high intensity.
         */
        fun defaults(): EffectPreferences = EffectPreferences()

        /**
         * Performance-optimized preset: reduced intensity for low-end devices.
         * Used when AdaptivePerformanceManager detects jank.
         */
        fun performanceOptimized(): EffectPreferences = EffectPreferences(
            rainEnabled = true,
            rainIntensity = 0.4f,
            glitchEnabled = true,
            glitchIntensity = 0.3f,
            scanlineEnabled = true,
            scanlineIntensity = 0.3f
        )

        /**
         * Immersive preset: maximum effect intensity for premium experience.
         * Used when device has headroom (< 8ms per frame).
         */
        fun immersive(): EffectPreferences = EffectPreferences(
            rainEnabled = true,
            rainIntensity = 1.0f,
            glitchEnabled = true,
            glitchIntensity = 0.8f,
            scanlineEnabled = true,
            scanlineIntensity = 0.8f
        )
    }
}

