package com.emul8r.bizap.data.config

import kotlinx.coroutines.flow.Flow

/**
 * Enumeration of all feature flags tracked via Firebase Remote Config.
 *
 * Each flag carries a [key] (the Remote Config parameter name) and a [defaultValue]
 * that is used when Remote Config is unavailable or the flag has not been configured.
 *
 * Naming convention: lowercase_underscore keys matching the Remote Config console.
 */
enum class FeatureFlag(val key: String, val defaultValue: Boolean) {
    // Existing feature flags
    RECURRING_INVOICES("recurring_invoices", false),
    EMAIL_INTEGRATION("email_integration", false),
    ADVANCED_SEARCH("advanced_search", false),
    CUSTOMER_PORTAL("customer_portal", false),
    PUSH_NOTIFICATIONS("push_notifications", false),
    MULTI_BUSINESS("multi_business", false),
    DARK_MODE("dark_mode", false),
    LOCALIZATION("localization", false),

    // ═══════════════════════════════════════════════════════════════════════════════
    // MATRIX CANVAS RENDERER FEATURE FLAGS (v1.0 — April 2026)
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Main feature gate for Canvas-based Matrix background engine.
     * When enabled: Uses GPU-accelerated particle renderer (60 FPS target on Pixel 6a)
     * When disabled: Falls back to text-based background (legacy)
     *
     * Rollout: Gradual (5% → 25% → 50% → 100% over 4 weeks)
     */
    MATRIX_CANVAS_RENDERER("matrix_canvas_renderer", false),

    /**
     * Enables in-app debug panel for Matrix effects tuning.
     * Only visible in:
     * - Android Studio Debug builds (always)
     * - Production Beta builds (if enabled)
     *
     * Allows live adjustment: rain density, glitch intensity, scanline alpha, etc.
     */
    MATRIX_DEBUG_PANEL("matrix_debug_panel", false),

    /**
     * Enables adaptive performance mode: auto-reduces density/effects on jank detection.
     * When enabled: If consecutive frame drops detected → reduce particle count + effect intensity
     * Purpose: Maintains 60 FPS on aging devices; auto-healing UX
     *
     * Beta feature; not enabled by default.
     */
    MATRIX_ADAPTIVE_PERF("matrix_adaptive_perf", false),

    // ═══════════════════════════════════════════════════════════════════════════════
    // IMMERSIVE EFFECTS FEATURE FLAGS (Phase 1 — April 2026)
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Enables GPU-accelerated rain particle effect in GUI3.
     * When enabled: Matrix rain cascades down screen (3-layer effect with varying speeds)
     * Performance: 2–4ms on Pixel 6a; configurable density via EffectPreferences
     * Default: true (effect enabled by default; users can disable in Settings)
     */
    EFFECT_RAIN("effect_rain", true),

    /**
     * Enables color shift + chromatic aberration glitch effect in GUI3.
     * When enabled: Occasional color shifts and RGB channel separation
     * Performance: 1–2ms on Pixel 6a; intensity controlled via EffectPreferences
     * Default: true (effect enabled by default; users can disable in Settings)
     */
    EFFECT_GLITCH("effect_glitch", true),

    /**
     * Enables CRT-style horizontal scanline effect in GUI3.
     * When enabled: Subtle animated scanlines with flicker; creates vintage aesthetic
     * Performance: 1–2ms on Pixel 6a; alpha/flicker rate controlled via EffectPreferences
     * Default: true (effect enabled by default; users can disable in Settings)
     */
    EFFECT_SCANLINES("effect_scanlines", true)
}

/**
 * FeatureFlagManager — controls feature availability at runtime.
 *
 * Implementations may use Firebase Remote Config for remote control or fall back
 * to local defaults, making the interface suitable for both production and testing.
 */
interface FeatureFlagManager {
    /**
     * Returns whether [flag] is enabled globally.
     *
     * Falls back to [FeatureFlag.defaultValue] on any error.
     */
    suspend fun isEnabled(flag: FeatureFlag): Boolean

    /**
     * Returns whether [flag] is enabled for a specific user as part of a gradual rollout.
     *
     * The rollout percentage is read from a companion Remote Config key
     * `<flag.key>_rollout_percentage`. A [userId] is placed into a stable bucket
     * (0–99) so the same user consistently sees (or does not see) the feature.
     *
     * @param flag   The feature to check
     * @param userId Stable identifier for bucketing (e.g. business / user ID)
     */
    suspend fun isEnabledForUser(flag: FeatureFlag, userId: Long): Boolean

    /**
     * Override a flag value locally (useful for QA / debug builds).
     *
     * This value persists only for the current process lifetime unless the
     * implementation stores it persistently.
     */
    suspend fun setEnabled(flag: FeatureFlag, enabled: Boolean)

    /**
     * Observe flag changes as a [Flow].
     *
     * Emits the current value immediately and then emits each subsequent update.
     */
    fun observeFlag(flag: FeatureFlag): Flow<Boolean>
}
