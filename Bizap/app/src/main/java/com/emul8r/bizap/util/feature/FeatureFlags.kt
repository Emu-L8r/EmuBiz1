package com.emul8r.bizap.util.feature

import kotlinx.coroutines.flow.Flow

/**
 * Feature flags interface for runtime effect control.
 * Enables toggling Matrix Canvas effects without code changes.
 */
interface FeatureFlags {
    // Canvas rendering main gate
    val matrixCanvasRendererEnabled: Flow<Boolean>

    // Individual effect toggles
    val effectRainEnabled: Flow<Boolean>
    val effectGlitchEnabled: Flow<Boolean>
    val effectScanlinesEnabled: Flow<Boolean>

    // Performance optimization
    val adaptivePerfEnabled: Flow<Boolean>

    // Setter methods
    suspend fun setMatrixCanvasRendererEnabled(enabled: Boolean)
    suspend fun setEffectRainEnabled(enabled: Boolean)
    suspend fun setEffectGlitchEnabled(enabled: Boolean)
    suspend fun setEffectScanlinesEnabled(enabled: Boolean)
    suspend fun setAdaptivePerfEnabled(enabled: Boolean)

    // Reset to defaults
    suspend fun resetToDefaults()
}

