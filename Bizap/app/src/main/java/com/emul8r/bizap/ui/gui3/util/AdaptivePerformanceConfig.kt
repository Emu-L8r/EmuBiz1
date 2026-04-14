package com.emul8r.bizap.ui.gui3.util

import androidx.compose.runtime.Stable

/**
 * Configuration for adaptive performance tuning.
 * Automatically reduces effect density on low-end devices or during jank.
 *
 * Used by EnhancedPerformanceProfiler to dynamically adjust particle density.
 */
@Stable
data class AdaptivePerformanceConfig(
    // Density multiplier (1.0 = full quality, 0.5 = half density, 0.1 = minimal)
    val particleDensity: Float = 1.0f,

    // Frame time thresholds (in milliseconds)
    val targetFrameTimeMs: Float = 16.67f,    // Target: 60 FPS
    val jankThresholdMs: Float = 33.33f,      // Jank: 30 FPS or worse

    // Adaptation parameters
    val jankFrameCountThreshold: Int = 3,     // Trigger after 3 consecutive jank frames
    val densityReductionStep: Float = 0.25f,  // Reduce by 25% per step
    val minDensity: Float = 0.1f,             // Don't go below 10% (maintain minimum visual quality)

    // Monitoring configuration
    val frameHistorySize: Int = 120,          // Track last 120 frames (~2 sec @ 60 FPS)
    val jankDetectionWindow: Int = 30,        // Analyze last 30 frames for jank rate
    val reportingIntervalFrames: Int = 300,   // Report metrics every 5 sec (~300 frames @ 60 FPS)

    // Device tier classification thresholds
    val midRangeDeviceThreshold: Float = 14.0f,  // Frames > 14ms = mid-range
    val lowEndDeviceThreshold: Float = 20.0f     // Frames > 20ms = low-end
) {

    /**
     * Validate configuration values.
     */
    init {
        require(particleDensity in 0.1f..2.0f) { "Particle density must be 0.1-2.0" }
        require(minDensity > 0f && minDensity <= particleDensity) { "Min density must be positive and <= current" }
        require(densityReductionStep > 0f) { "Reduction step must be positive" }
        require(jankFrameCountThreshold > 0) { "Jank threshold must be positive" }
    }

    companion object {
        /**
         * Preset for premium devices (Pixel 6+, iPhone 13+, Samsung S21+).
         * Maximum quality, aggressive jank detection.
         */
        fun premiumDevice() = AdaptivePerformanceConfig(
            particleDensity = 1.2f,
            densityReductionStep = 0.1f,
            minDensity = 0.5f,
            jankFrameCountThreshold = 5  // More tolerant of occasional jank
        )

        /**
         * Preset for mid-range devices (Pixel 5, OnePlus 8, Moto G series).
         * Balanced quality and performance.
         */
        fun midRangeDevice() = AdaptivePerformanceConfig(
            particleDensity = 0.8f,
            jankFrameCountThreshold = 2,
            minDensity = 0.25f
        )

        /**
         * Preset for low-end devices (5+ year old phones, budget Android).
         * Minimal effects, aggressive density reduction.
         */
        fun lowEndDevice() = AdaptivePerformanceConfig(
            particleDensity = 0.4f,
            jankFrameCountThreshold = 1,  // Single jank frame triggers reduction
            minDensity = 0.1f,
            densityReductionStep = 0.15f   // Larger steps for quick optimization
        )
    }
}

