package com.emul8r.bizap.ui.gui3.util

import android.content.SharedPreferences
import android.os.Build
import androidx.core.content.edit
import com.emul8r.bizap.utils.FirebaseEventTracker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * Monitors frame time and automatically adapts pipeline density
 * to maintain 60 FPS, even on aging devices.
 *
 * ✅ NEW: Also persists manual user adjustments (from Effects Panel sliders)
 * to SharedPreferences so they survive navigation and app restart.
 *
 * ## Adaptive Strategy
 *
 * 1. **Detect:** Consecutive frame drops (> 3 frames exceeding 16.67ms)
 * 2. **Reduce:** Progressively reduce density/effect intensity
 * 3. **Log:** Report adaptation events to Crashlytics/Analytics
 * 4. **Monitor:** Check if adaptation improved frame time
 *
 * ## Example
 *
 * ```
 * Device: Pixel 6a (but aging, degraded over time)
 * Initial state: density=0.8, glitch=0.5
 *
 * ──────────────────────────────────────────────
 * Frame 1: 18ms (jank) ─┐
 * Frame 2: 19ms (jank) ─┤ 3 consecutive
 * Frame 3: 17ms (jank) ─┘    jank frames
 * ──────────────────────────────────────────────
 * Adaptation triggered:
 *   density: 0.8 → 0.64 (×0.8)
 *   glitch: 0.5 → 0.35 (×0.7)
 *
 * Frame 4: 14ms ✅ (< 16.67ms threshold)
 * Frame 5: 15ms ✅
 * ──────────────────────────────────────────────
 * ```
 *
 * @see PerformanceProfiler for frame time tracking
 * @see FirebaseEventTracker for telemetry
 */
@Singleton
class AdaptivePerformanceManager @Inject constructor(
    private val eventTracker: FirebaseEventTracker?,
    @Named("matrix_effects_prefs") private val prefs: SharedPreferences
) {
    // ✅ Initialize with placeholder, then update in init
    private val _adaptiveConfig = MutableStateFlow(MatrixBackgroundConfig())
    val adaptiveConfig: StateFlow<MatrixBackgroundConfig> = _adaptiveConfig

    private var consecutiveJankFrames = 0
    private var totalAdaptations = 0

    init {
        // ✅ NEW: Load persisted config from SharedPreferences on init
        val loadedConfig = loadConfigFromPreferences()
        _adaptiveConfig.value = loadedConfig
        Timber.d("🔄 Loaded effects config from prefs: density=%.2f glitch=%.2f".format(
            loadedConfig.rainDensity, loadedConfig.glitchIntensity
        ))
    }

    companion object {
        private const val JANK_THRESHOLD_MS = 16.67  // 60 FPS target
        private const val CRITICAL_JANK_MS = 33.33  // 30 FPS (double frame drop)
        private const val CONSECUTIVE_JANK_LIMIT = 3  // Trigger adaptation on 3 consecutive
        private const val DENSITY_DECAY = 0.8f  // Reduce density by 20%
        private const val GLITCH_DECAY = 0.7f  // Reduce glitch by 30% (more noticeable)

        // ✅ NEW: SharedPreferences keys with prefix
        private const val PREFS_PREFIX = "matrix_effects_"
        private const val KEY_RAIN_DENSITY = "${PREFS_PREFIX}rain_density"
        private const val KEY_RAIN_SPEED = "${PREFS_PREFIX}rain_speed"
        private const val KEY_GLITCH_INTENSITY = "${PREFS_PREFIX}glitch_intensity"
        private const val KEY_SCANLINE_ALPHA = "${PREFS_PREFIX}scanline_alpha"
        private const val KEY_ENABLE_ADAPTIVE_PERF = "${PREFS_PREFIX}enable_adaptive_perf"
        private const val KEY_DEBUG_LOGGING = "${PREFS_PREFIX}debug_logging"
    }

    /**
     * Called by PerformanceProfiler after each frame is recorded.
     *
     * @param frameTimeMs Frame render time in milliseconds
     */
    fun onFrameTimeRecorded(frameTimeMs: Double) {
        if (frameTimeMs > JANK_THRESHOLD_MS) {
            consecutiveJankFrames++

            if (consecutiveJankFrames >= CONSECUTIVE_JANK_LIMIT) {
                // 3+ consecutive jank frames → trigger adaptation
                adaptPipeline()
                consecutiveJankFrames = 0  // Reset counter after adaptation
            }
        } else {
            // Good frame; reset jank counter
            if (consecutiveJankFrames > 0) {
                Timber.v("Jank streak ended at $consecutiveJankFrames frames; counter reset")
            }
            consecutiveJankFrames = 0
        }

        // Log critical jank (major frame drop)
        if (frameTimeMs > CRITICAL_JANK_MS) {
            Timber.w("🔴 CRITICAL JANK: ${frameTimeMs.format(2)}ms (30 FPS drop) on ${Build.MODEL}")
            eventTracker?.trackMatrixFrameJank(frameTimeMs)
        }
    }

    /**
     * Trigger adaptation: reduce density/effects to improve performance.
     *
     * Called when consecutive jank detected. Uses exponential decay
     * to progressively reduce complexity.
     */
    private fun adaptPipeline() {
        val current = _adaptiveConfig.value
        totalAdaptations++

        // Don't reduce below minimum thresholds
        val newDensity = (current.rainDensity * DENSITY_DECAY).coerceAtLeast(0.3f)
        val newGlitch = (current.glitchIntensity * GLITCH_DECAY).coerceAtLeast(0f)
        val newScanlines = (current.scanlineAlpha * 0.6f).coerceAtLeast(0f)

        val adapted = current.copy(
            rainDensity = newDensity,
            glitchIntensity = newGlitch,
            scanlineAlpha = newScanlines
        )

        _adaptiveConfig.value = adapted

        Timber.w(
            "🔧 ADAPTATION #$totalAdaptations triggered:\n" +
                    "  Density: ${current.rainDensity.format(2)} → ${adapted.rainDensity.format(2)}\n" +
                    "  Glitch: ${current.glitchIntensity.format(2)} → ${adapted.glitchIntensity.format(2)}\n" +
                    "  Scanlines: ${current.scanlineAlpha.format(3)} → ${adapted.scanlineAlpha.format(3)}"
        )

        eventTracker?.trackMatrixAdaptationTriggered(
            oldDensity = current.rainDensity,
            newDensity = adapted.rainDensity,
            oldGlitch = current.glitchIntensity,
            newGlitch = adapted.glitchIntensity
        )

        // ✅ NEW: Persist auto-adapted config to prefs so it survives restart
        persistConfigToPreferences(adapted)
    }

    /**
     * ✅ NEW: Update config with user manual adjustments from Effects Panel.
     * Persists to SharedPreferences and emits to StateFlow.
     *
     * @param config New configuration (from slider adjustments)
     */
    fun updateConfig(config: MatrixBackgroundConfig) {
        _adaptiveConfig.value = config
        persistConfigToPreferences(config)
        Timber.i("✅ Effects config saved: density=%.2f glitch=%.2f scanlines=%.3f".format(
            config.rainDensity, config.glitchIntensity, config.scanlineAlpha
        ))
    }

    /**
     * ✅ NEW: Persist config to SharedPreferences.
     */
    private fun persistConfigToPreferences(config: MatrixBackgroundConfig) {
        try {
            prefs.edit {
                putFloat(KEY_RAIN_DENSITY, config.rainDensity)
                putFloat(KEY_RAIN_SPEED, config.rainSpeed)
                putFloat(KEY_GLITCH_INTENSITY, config.glitchIntensity)
                putFloat(KEY_SCANLINE_ALPHA, config.scanlineAlpha)
                putBoolean(KEY_ENABLE_ADAPTIVE_PERF, config.enableAdaptivePerf)
                putBoolean(KEY_DEBUG_LOGGING, config.debugLogging)
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to persist effects config to prefs")
        }
    }

    /**
     * ✅ NEW: Load config from SharedPreferences (or defaults if not present).
     */
    private fun loadConfigFromPreferences(): MatrixBackgroundConfig {
        return try {
            MatrixBackgroundConfig(
                rainDensity = prefs.getFloat(KEY_RAIN_DENSITY, 0.8f),
                rainSpeed = prefs.getFloat(KEY_RAIN_SPEED, 1.0f),
                glitchIntensity = prefs.getFloat(KEY_GLITCH_INTENSITY, 0.5f),
                scanlineAlpha = prefs.getFloat(KEY_SCANLINE_ALPHA, 0.05f),
                enableAdaptivePerf = prefs.getBoolean(KEY_ENABLE_ADAPTIVE_PERF, false),
                debugLogging = prefs.getBoolean(KEY_DEBUG_LOGGING, false)
            )
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to load effects config from prefs; using defaults")
            MatrixBackgroundConfig()
        }
    }

    /**
     * Get current adapted configuration.
     *
     * @return MatrixBackgroundConfig with current density/effect settings
     */
    fun getCurrentConfig(): MatrixBackgroundConfig = _adaptiveConfig.value

    /**
     * Check if adaptation is enabled (feature flag check).
     *
     * @return true if adaptive performance is active
     */
    fun isAdaptiveEnabled(): Boolean = _adaptiveConfig.value.enableAdaptivePerf

    /**
     * Check if effects have been significantly reduced (adaptation state).
     *
     * @return true if current density < 0.5 (adaptation triggered 2+ times)
     */
    fun isSignificantlyAdapted(): Boolean {
        return _adaptiveConfig.value.rainDensity < 0.5f
    }

    /**
     * Get total number of adaptations triggered (session stat).
     *
     * @return Number of times adaptation occurred
     */
    fun getTotalAdaptations(): Int = totalAdaptations

    /**
     * Get consecutive jank frame count (current state).
     *
     * @return Number of consecutive frames exceeding jank threshold
     */
    fun getConsecutiveJankFrames(): Int = consecutiveJankFrames

    /**
     * Reset adaptation state (for session boundaries or testing).
     */
    fun reset() {
        val defaultConfig = MatrixBackgroundConfig()
        _adaptiveConfig.value = defaultConfig
        persistConfigToPreferences(defaultConfig)
        consecutiveJankFrames = 0
        totalAdaptations = 0
        Timber.d("🔄 Adaptive performance manager + prefs reset to defaults")
    }

    /**
     * Snapshot current adaptation state for diagnostics.
     *
     * @return String describing current configuration and jank state
     */
    fun snapshotState(): String = """
         ╔════ ADAPTIVE PERFORMANCE STATE ════╗
         ║ Device: ${Build.MODEL}
         ║ Total Adaptations: $totalAdaptations
         ║ Consecutive Jank Frames: $consecutiveJankFrames
         ║ Current Density: ${_adaptiveConfig.value.rainDensity.format(2)}
         ║ Current Glitch: ${_adaptiveConfig.value.glitchIntensity.format(2)}
         ║ Current Scanlines: ${_adaptiveConfig.value.scanlineAlpha.format(3)}
         ║ Significantly Adapted: ${isSignificantlyAdapted()}
         ╚════════════════════════════════════╝
     """.trimIndent()
}

/**
 * Extension function for clean float formatting.
 */
internal fun Float.format(digits: Int): String = "%.${digits}f".format(this)
