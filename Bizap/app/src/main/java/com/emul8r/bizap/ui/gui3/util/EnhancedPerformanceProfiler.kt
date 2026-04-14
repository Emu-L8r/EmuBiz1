package com.emul8r.bizap.ui.gui3.util

import androidx.compose.runtime.Stable
import kotlin.math.roundToInt

/**
 * Enhanced performance profiler with adaptive density adjustment.
 *
 * Monitors frame times and automatically reduces particle density when jank is detected.
 * Implements device tier classification based on performance characteristics.
 *
 * Usage:
 * ```
 * val profiler = EnhancedPerformanceProfiler()
 *
 * // In draw loop:
 * val result = profiler.recordFrame(frameTimeUs)
 * when (result) {
 *   is AdaptationResult.DensityReduced -> {
 *     // Update UI with new density
 *     updateEffectDensity(result.newDensity)
 *   }
 *   is AdaptationResult.MetricsReport -> {
 *     // Send metrics to analytics
 *     logMetrics(result)
 *   }
 *   null -> {} // No action needed
 * }
 * ```
 */
@Stable
class EnhancedPerformanceProfiler(
    initialConfig: AdaptivePerformanceConfig = AdaptivePerformanceConfig()
) {
    private var _config = initialConfig
    private val frameHistory = FloatArray(_config.frameHistorySize)
    private var frameIndex = 0
    private var totalFrames = 0L
    private var jankFrameCount = 0

    /**
     * Current performance configuration.
     * Read-only property; use adaptation methods to modify.
     */
    val config: AdaptivePerformanceConfig
        get() = _config

    /**
     * Record a frame's timing and check for adaptation needs.
     *
     * @param frameTimeUs Frame time in microseconds
     * @return AdaptationResult if action was taken, null otherwise
     */
    fun recordFrame(frameTimeUs: Long): AdaptationResult? {
        val frameTimeMs = frameTimeUs / 1000f
        frameHistory[frameIndex % _config.frameHistorySize] = frameTimeMs
        frameIndex = (frameIndex + 1) % _config.frameHistorySize
        totalFrames++

        // Check if frame was janky
        if (frameTimeMs > _config.jankThresholdMs) {
            jankFrameCount++
        } else {
            jankFrameCount = 0
        }

        // Check if we should reduce density
        if (jankFrameCount >= _config.jankFrameCountThreshold) {
            val avgFrameTime = getAverageFrameTime()
            if (avgFrameTime > _config.jankThresholdMs) {
                jankFrameCount = 0  // Reset counter for next cycle
                return reduceParticleDensity(avgFrameTime)
            }
        }

        // Periodically report metrics
        if (totalFrames % _config.reportingIntervalFrames == 0L) {
            return createMetricsReport()
        }

        return null
    }

    /**
     * Manually reduce particle density (e.g., on app backgrounding).
     */
    private fun reduceParticleDensity(avgFrameTimeMs: Float): AdaptationResult.DensityReduced {
        val oldDensity = _config.particleDensity
        val newDensity = (oldDensity - _config.densityReductionStep)
            .coerceAtLeast(_config.minDensity)

        _config = _config.copy(particleDensity = newDensity)

        return AdaptationResult.DensityReduced(oldDensity, newDensity, avgFrameTimeMs)
    }

    /**
     * Create a metrics report for analytics.
     */
    private fun createMetricsReport(): AdaptationResult.MetricsReport {
        return AdaptationResult.MetricsReport(
            avgFrameTimeMs = getAverageFrameTime(),
            maxFrameTimeMs = getMaxFrameTime(),
            minFrameTimeMs = getMinFrameTime(),
            jankRatePercent = getJankRate(),
            currentDensity = _config.particleDensity,
            totalFrames = totalFrames
        )
    }

    /**
     * Get average frame time from history (in ms).
     */
    fun getAverageFrameTime(): Float {
        val sum = frameHistory.sum()
        val count = if (totalFrames < frameHistory.size) totalFrames else frameHistory.size.toLong()
        return if (count > 0) sum / count else 0f
    }

    /**
     * Get maximum frame time from history (in ms).
     */
    fun getMaxFrameTime(): Float {
        return frameHistory.maxOrNull() ?: 0f
    }

    /**
     * Get minimum frame time from history (in ms).
     */
    fun getMinFrameTime(): Float {
        return frameHistory.minOrNull() ?: 0f
    }

    /**
     * Calculate jank rate (% of frames exceeding jank threshold).
     */
    fun getJankRate(): Float {
        val windowSize = _config.jankDetectionWindow.coerceAtMost(frameHistory.size)
        if (totalFrames < windowSize) return 0f

        val jankCount = (0 until windowSize).count { i ->
            frameHistory[(frameIndex - windowSize + i + frameHistory.size) % frameHistory.size] > _config.jankThresholdMs
        }

        return (jankCount * 100f) / windowSize
    }

    /**
     * Classify device performance tier based on average frame time.
     */
    fun getDeviceTier(): DeviceTier {
        val avgTime = getAverageFrameTime()
        return when {
            avgTime < _config.midRangeDeviceThreshold -> DeviceTier.PREMIUM
            avgTime < _config.lowEndDeviceThreshold -> DeviceTier.MID_RANGE
            else -> DeviceTier.LOW_END
        }
    }

    /**
     * Reset profiler state.
     */
    fun reset() {
        frameIndex = 0
        totalFrames = 0L
        jankFrameCount = 0
        frameHistory.fill(0f)
    }

    /**
     * Result of performance analysis.
     */
    sealed interface AdaptationResult {
        /**
         * Particle density was reduced due to jank.
         */
        data class DensityReduced(
            val oldDensity: Float,
            val newDensity: Float,
            val avgFrameTimeMs: Float
        ) : AdaptationResult

        /**
         * Periodic metrics report for analytics.
         */
        data class MetricsReport(
            val avgFrameTimeMs: Float,
            val maxFrameTimeMs: Float,
            val minFrameTimeMs: Float,
            val jankRatePercent: Float,
            val currentDensity: Float,
            val totalFrames: Long
        ) : AdaptationResult
    }

    /**
     * Device performance tier classification.
     */
    enum class DeviceTier {
        PREMIUM,    // High-end: Pixel 6+, iPhone 13+
        MID_RANGE,  // Mid-range: Pixel 5, OnePlus 8
        LOW_END     // Budget: 5+ year old devices
    }
}

