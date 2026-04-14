package com.emul8r.bizap.util.feature

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber

/**
 * Implementation of FeatureFlags using SharedPreferences for local storage.
 * Provides reactive state flows for all feature flags.
 */
class FeatureFlagsImpl(
    private val prefs: SharedPreferences
) : FeatureFlags {

    // Canvas renderer main gate
    private val _matrixCanvasRenderer = MutableStateFlow(
        prefs.getBoolean(KEY_MATRIX_CANVAS_RENDERER, DEFAULT_CANVAS_RENDERER)
    )
    override val matrixCanvasRendererEnabled: Flow<Boolean> = _matrixCanvasRenderer

    // Effect toggles
    private val _effectRain = MutableStateFlow(
        prefs.getBoolean(KEY_EFFECT_RAIN, DEFAULT_EFFECT_RAIN)
    )
    override val effectRainEnabled: Flow<Boolean> = _effectRain

    private val _effectGlitch = MutableStateFlow(
        prefs.getBoolean(KEY_EFFECT_GLITCH, DEFAULT_EFFECT_GLITCH)
    )
    override val effectGlitchEnabled: Flow<Boolean> = _effectGlitch

    private val _effectScanlines = MutableStateFlow(
        prefs.getBoolean(KEY_EFFECT_SCANLINES, DEFAULT_EFFECT_SCANLINES)
    )
    override val effectScanlinesEnabled: Flow<Boolean> = _effectScanlines

    // Performance optimization
    private val _adaptivePerf = MutableStateFlow(
        prefs.getBoolean(KEY_ADAPTIVE_PERF, DEFAULT_ADAPTIVE_PERF)
    )
    override val adaptivePerfEnabled: Flow<Boolean> = _adaptivePerf

    override suspend fun setMatrixCanvasRendererEnabled(enabled: Boolean) {
        try {
            prefs.edit { putBoolean(KEY_MATRIX_CANVAS_RENDERER, enabled) }
            _matrixCanvasRenderer.emit(enabled)
            Timber.d("Matrix canvas renderer: $enabled")
        } catch (e: Exception) {
            Timber.e(e, "Failed to set matrix canvas renderer flag")
        }
    }

    override suspend fun setEffectRainEnabled(enabled: Boolean) {
        try {
            prefs.edit { putBoolean(KEY_EFFECT_RAIN, enabled) }
            _effectRain.emit(enabled)
            Timber.d("Rain effect: $enabled")
        } catch (e: Exception) {
            Timber.e(e, "Failed to set rain effect flag")
        }
    }

    override suspend fun setEffectGlitchEnabled(enabled: Boolean) {
        try {
            prefs.edit { putBoolean(KEY_EFFECT_GLITCH, enabled) }
            _effectGlitch.emit(enabled)
            Timber.d("Glitch effect: $enabled")
        } catch (e: Exception) {
            Timber.e(e, "Failed to set glitch effect flag")
        }
    }

    override suspend fun setEffectScanlinesEnabled(enabled: Boolean) {
        try {
            prefs.edit { putBoolean(KEY_EFFECT_SCANLINES, enabled) }
            _effectScanlines.emit(enabled)
            Timber.d("Scanline effect: $enabled")
        } catch (e: Exception) {
            Timber.e(e, "Failed to set scanline effect flag")
        }
    }

    override suspend fun setAdaptivePerfEnabled(enabled: Boolean) {
        try {
            prefs.edit { putBoolean(KEY_ADAPTIVE_PERF, enabled) }
            _adaptivePerf.emit(enabled)
            Timber.d("Adaptive perf: $enabled")
        } catch (e: Exception) {
            Timber.e(e, "Failed to set adaptive perf flag")
        }
    }

    override suspend fun resetToDefaults() {
        try {
            prefs.edit { clear() }
            _matrixCanvasRenderer.emit(DEFAULT_CANVAS_RENDERER)
            _effectRain.emit(DEFAULT_EFFECT_RAIN)
            _effectGlitch.emit(DEFAULT_EFFECT_GLITCH)
            _effectScanlines.emit(DEFAULT_EFFECT_SCANLINES)
            _adaptivePerf.emit(DEFAULT_ADAPTIVE_PERF)
            Timber.i("Feature flags reset to defaults")
        } catch (e: Exception) {
            Timber.e(e, "Failed to reset feature flags")
        }
    }

    companion object {
        // Preference keys
        private const val KEY_MATRIX_CANVAS_RENDERER = "matrix_canvas_renderer_enabled"
        private const val KEY_EFFECT_RAIN = "effect_rain_enabled"
        private const val KEY_EFFECT_GLITCH = "effect_glitch_enabled"
        private const val KEY_EFFECT_SCANLINES = "effect_scanlines_enabled"
        private const val KEY_ADAPTIVE_PERF = "adaptive_perf_enabled"

        // Defaults: Canvas off by default (opt-in), all effects on
        private const val DEFAULT_CANVAS_RENDERER = false
        private const val DEFAULT_EFFECT_RAIN = true
        private const val DEFAULT_EFFECT_GLITCH = true
        private const val DEFAULT_EFFECT_SCANLINES = true
        private const val DEFAULT_ADAPTIVE_PERF = false
    }
}

