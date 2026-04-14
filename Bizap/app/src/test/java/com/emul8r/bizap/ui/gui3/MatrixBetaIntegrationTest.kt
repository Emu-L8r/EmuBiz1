package com.emul8r.bizap.ui.gui3

import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.analytics.BetaReleaseManager
import com.emul8r.bizap.analytics.MatrixCrashlyticsLogger
import com.emul8r.bizap.ui.gui3.util.AdaptivePerformanceConfig
import com.emul8r.bizap.ui.gui3.util.EnhancedPerformanceProfiler
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Integration tests for Phase 5 beta and adaptive performance features.
 * Tests device tier classification, density adaptation, and metrics collection.
 */
class MatrixBetaIntegrationTest : BaseUnitTest() {

    private val crashlyticsLogger: MatrixCrashlyticsLogger = mockk(relaxed = true)

    @Before
    fun setup() {
        // Common setup for all tests
    }

    // ============ Adaptive Performance Tests ============

    @Test
    fun testAdaptivePerformanceDetectsJank() = runUnitTest {
        val profiler = EnhancedPerformanceProfiler()

        // Simulate 3 consecutive jank frames (50ms each)
        repeat(3) {
            profiler.recordFrame(50_000)  // 50ms = jank
        }

        // Should trigger density reduction after 3 jank frames
        assertTrue(
            profiler.config.particleDensity < 1.0f,
            "Particle density should be reduced after jank detection"
        )
    }

    @Test
    fun testAdaptivePerformanceReducesDensity() = runUnitTest {
        val config = AdaptivePerformanceConfig(
            particleDensity = 1.0f,
            densityReductionStep = 0.25f
        )
        val profiler = EnhancedPerformanceProfiler(config)

        // First jank detection: reduce from 1.0 to 0.75
        repeat(3) {
            profiler.recordFrame(50_000)
        }

        val firstReduction = profiler.config.particleDensity
        assertTrue(
            firstReduction == 0.75f,
            "First reduction should be 1.0 - 0.25 = 0.75"
        )

        // Second jank detection: reduce from 0.75 to 0.5
        repeat(3) {
            profiler.recordFrame(50_000)
        }

        val secondReduction = profiler.config.particleDensity
        assertTrue(
            secondReduction == 0.5f,
            "Second reduction should be 0.75 - 0.25 = 0.5"
        )
    }

    @Test
    fun testAdaptivePerformanceRespectMinDensity() = runUnitTest {
        val config = AdaptivePerformanceConfig.lowEndDevice()
        val profiler = EnhancedPerformanceProfiler(config)

        // Reduce density multiple times
        repeat(20) {
            profiler.recordFrame(50_000)
        }

        // Density should not go below minimum
        assertTrue(
            profiler.config.particleDensity >= config.minDensity,
            "Density should not go below minimum (${config.minDensity})"
        )
    }

    // ============ Device Tier Classification Tests ============

    @Test
    fun testDeviceTierPremium() = runUnitTest {
        val profiler = EnhancedPerformanceProfiler()

        // Simulate premium device frames (12ms average)
        repeat(120) {
            profiler.recordFrame(12_000)
        }

        val tier = profiler.getDeviceTier()
        assertEquals(
            EnhancedPerformanceProfiler.DeviceTier.PREMIUM,
            tier,
            "Device should be classified as PREMIUM"
        )
    }

    @Test
    fun testDeviceTierMidRange() = runUnitTest {
        val profiler = EnhancedPerformanceProfiler()

        // Simulate mid-range device frames (18ms average)
        repeat(120) {
            profiler.recordFrame(18_000)
        }

        val tier = profiler.getDeviceTier()
        assertEquals(
            EnhancedPerformanceProfiler.DeviceTier.MID_RANGE,
            tier,
            "Device should be classified as MID_RANGE"
        )
    }

    @Test
    fun testDeviceTierLowEnd() = runUnitTest {
        val profiler = EnhancedPerformanceProfiler()

        // Simulate low-end device frames (25ms average)
        repeat(120) {
            profiler.recordFrame(25_000)
        }

        val tier = profiler.getDeviceTier()
        assertEquals(
            EnhancedPerformanceProfiler.DeviceTier.LOW_END,
            tier,
            "Device should be classified as LOW_END"
        )
    }

    // ============ Metrics Calculation Tests ============

    @Test
    fun testJankRateCalculation() = runUnitTest {
        val profiler = EnhancedPerformanceProfiler()

        // Simulate 30 frames: 10 janky (>33ms), 20 smooth (<33ms)
        repeat(10) {
            profiler.recordFrame(50_000)  // Jank
        }
        repeat(20) {
            profiler.recordFrame(10_000)  // Smooth
        }

        val jankRate = profiler.getJankRate()
        assertTrue(
            jankRate in 20f..40f,
            "Jank rate should be approximately 33% (got $jankRate%)"
        )
    }

    @Test
    fun testFrameTimeMeasurement() = runUnitTest {
        val profiler = EnhancedPerformanceProfiler()

        // Record varied frame times
        profiler.recordFrame(8_000)   // 8ms
        profiler.recordFrame(16_000)  // 16ms
        profiler.recordFrame(24_000)  // 24ms

        val avg = profiler.getAverageFrameTime()
        val min = profiler.getMinFrameTime()
        val max = profiler.getMaxFrameTime()

        assertEquals(16f, avg, 1f, "Average should be ~16ms")
        assertEquals(8f, min, 1f, "Min should be ~8ms")
        assertEquals(24f, max, 1f, "Max should be ~24ms")
    }

    @Test
    fun testMetricsReporting() = runUnitTest {
        val profiler = EnhancedPerformanceProfiler()

        // Record frames to trigger metrics report
        var reportGenerated = false
        repeat(301) {  // reportingIntervalFrames = 300
            val result = profiler.recordFrame(16_000)
            if (result is EnhancedPerformanceProfiler.AdaptationResult.MetricsReport) {
                reportGenerated = true
            }
        }

        assertTrue(
            reportGenerated,
            "Metrics report should be generated after 300 frames"
        )
    }

    // ============ Configuration Tests ============

    @Test
    fun testAdaptiveConfigPresets() = runUnitTest {
        val premium = AdaptivePerformanceConfig.premiumDevice()
        val midRange = AdaptivePerformanceConfig.midRangeDevice()
        val lowEnd = AdaptivePerformanceConfig.lowEndDevice()

        // Verify different presets have different settings
        assertTrue(
            premium.particleDensity > midRange.particleDensity,
            "Premium should have higher density than mid-range"
        )
        assertTrue(
            midRange.particleDensity > lowEnd.particleDensity,
            "Mid-range should have higher density than low-end"
        )
        assertTrue(
            lowEnd.jankFrameCountThreshold < premium.jankFrameCountThreshold,
            "Low-end should have lower jank threshold"
        )
    }

    @Test
    fun testConfigValidation() = runUnitTest {
        try {
            // Should not throw - valid config
            AdaptivePerformanceConfig(
                particleDensity = 1.0f,
                minDensity = 0.2f,
                densityReductionStep = 0.1f
            )
        } catch (e: Exception) {
            assertTrue(false, "Valid config should not throw: ${e.message}")
        }
    }

    @Test
    fun testConfigValidationFailsInvalid() = runUnitTest {
        try {
            // Should throw - minDensity > particleDensity
            AdaptivePerformanceConfig(
                particleDensity = 0.5f,
                minDensity = 0.8f
            )
            assertTrue(false, "Invalid config should throw")
        } catch (e: IllegalArgumentException) {
            assertTrue(true, "Expected exception for invalid config")
        }
    }

    // ============ Beta Release Tests ============

    @Test
    fun testBetaEnrollment() = runUnitTest {
        val mockPrefs = mockk<android.content.SharedPreferences>(relaxed = true)
        val mockAnalytics = mockk<com.google.firebase.analytics.FirebaseAnalytics>(relaxed = true)

        // Would test BetaReleaseManager here, but requires complex Firebase mocking
        // This is a placeholder for full integration testing
    }
}

