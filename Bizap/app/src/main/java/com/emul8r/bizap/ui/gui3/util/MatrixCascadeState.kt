package com.emul8r.bizap.ui.gui3.util

import androidx.compose.runtime.mutableStateOf

/**
 * ✅ PHASE 2 TASK 3 & 4: Cascade Position Tracking
 *
 * Manages global cascade animation state for dynamic button effects:
 * - Buttons react when cascade code passes behind them
 * - Glow intensity increases based on proximity to cascade
 * - Creates "interactive" UI that responds to background animation
 *
 * This singleton is read by GlowingMatrixButton
 */
object MatrixCascadeState {
    // Current Y position of the falling cascade (0 = top, screenHeight = bottom)
    val cascadeYPosition = mutableStateOf(0f)

    // Current cascade visibility (0 = off-screen, 1 = fully visible)
    val cascadeVisibility = mutableStateOf(0f)

    // Optional: Screen height (for normalization)
    val screenHeight = mutableStateOf(0f)

    /**
     * Update cascade position during animation playback
     * Called by MatrixBackground composable on every frame
     */
    fun updateCascadePosition(y: Float) {
        cascadeYPosition.value = y
    }

    /**
     * Update cascade visibility (alpha of the effect)
     * 0 = invisible, 1 = fully opaque
     */
    fun updateCascadeVisibility(alpha: Float) {
        cascadeVisibility.value = alpha.coerceIn(0f, 1f)
    }

    /**
     * Update screen dimensions for normalization
     */
    fun setScreenHeight(height: Float) {
        screenHeight.value = height
    }

    /**
     * Check if cascade is at a given Y position (with tolerance)
     * Used by buttons to determine if they should glow
     *
     * @param buttonY Y position of the button
     * @param tolerance Pixels around button to consider "near"
     * @return true if cascade is within tolerance of buttonY
     */
    fun isCascadeNearby(buttonY: Float, tolerance: Float = 50f): Boolean {
        val cascadeY = cascadeYPosition.value
        return cascadeY in (buttonY - tolerance)..(buttonY + tolerance)
    }

    /**
     * Calculate glow intensity based on cascade proximity
     * Closer = more intense glow
     *
     * @param buttonY Y position of the button
     * @param maxDistance Distance at which glow reaches 0
     * @return Glow intensity (0..1)
     */
    fun calculateGlowIntensity(buttonY: Float, maxDistance: Float = 100f): Float {
        val cascadeY = cascadeYPosition.value
        val distance = kotlin.math.abs(cascadeY - buttonY)
        return (1f - (distance / maxDistance)).coerceIn(0f, 1f)
    }
}


