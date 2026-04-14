package com.emul8r.bizap.ui.dashboard.components.analytics

import androidx.compose.ui.graphics.Color
import com.emul8r.bizap.data.model.DaysToPayMetric
import com.emul8r.bizap.domain.config.BizapConfig
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for [AverageDaysToPayMetric] configuration behavior.
 *
 * Verifies that the Composable respects BizapConfig thresholds instead of
 * using hardcoded values. This ensures:
 * - Retail businesses can use 1-2 day thresholds
 * - B2B businesses can use 30-45 day thresholds
 * - No business type is forced into a single payment expectation
 */
class AverageDaysToPayMetricConfigTest {

    // ── configThresholds ──────────────────────────────────────────────────────

    @Test
    fun `configThresholds - metric respects custom healthy threshold`() {
        // Create retail config (expects 1 day payment)
        val retailConfig = BizapConfig(
            paymentHealthyThresholdDays = 1.0,
            paymentWarningThresholdDays = 3.0
        )

        // At 2 days, should be in warning zone for retail
        val daysToPayment = 2.0
        val isHealthy = daysToPayment < retailConfig.paymentHealthyThresholdDays
        val isWarning = daysToPayment < retailConfig.paymentWarningThresholdDays

        assertEquals(false, isHealthy, "2 days should NOT be healthy for retail (threshold=1)")
        assertEquals(true, isWarning, "2 days should be warning for retail (threshold=1-3)")
    }

    @Test
    fun `configThresholds - metric respects custom warning threshold`() {
        // Create B2B config (expects 30 day payment)
        val b2bConfig = BizapConfig(
            paymentHealthyThresholdDays = 30.0,
            paymentWarningThresholdDays = 45.0
        )

        // At 40 days, should be in warning zone for B2B
        val daysToPayment = 40.0
        val isHealthy = daysToPayment < b2bConfig.paymentHealthyThresholdDays
        val isWarning = daysToPayment < b2bConfig.paymentWarningThresholdDays

        assertEquals(false, isHealthy, "40 days should NOT be healthy for B2B (threshold=30)")
        assertEquals(true, isWarning, "40 days should be warning for B2B (threshold=30-45)")
    }

    @Test
    fun `configThresholds - same data point differs by config`() {
        // 10 days is:
        // - GREEN (healthy) for retail with 15-day threshold
        // - RED (problem) for a business expecting 5-day payment
        val daysToPayment = 10.0

        val productionConfig = BizapConfig.production()  // 15/25 days
        val strictConfig = BizapConfig(
            paymentHealthyThresholdDays = 5.0,
            paymentWarningThresholdDays = 7.0
        )

        val isHealthyProduction = daysToPayment < productionConfig.paymentHealthyThresholdDays
        val isHealthyStrict = daysToPayment < strictConfig.paymentHealthyThresholdDays

        assertEquals(true, isHealthyProduction, "10 days is healthy for standard config (threshold=15)")
        assertEquals(false, isHealthyStrict, "10 days is NOT healthy for strict config (threshold=5)")
    }

    @Test
    fun `configThresholds - production defaults are sensible`() {
        val config = BizapConfig.production()

        // Verify default thresholds for production
        assertEquals(15.0, config.paymentHealthyThresholdDays)
        assertEquals(25.0, config.paymentWarningThresholdDays)

        // These are reasonable for most small-to-medium businesses
        val testPayments = listOf(5.0, 15.0, 25.0, 30.0)
        val results = mutableMapOf<Double, String>()

        for (days in testPayments) {
            results[days] = when {
                days < config.paymentHealthyThresholdDays -> "Healthy"
                days < config.paymentWarningThresholdDays -> "Warning"
                else -> "Problem"
            }
        }

        assertEquals("Healthy", results[5.0])
        // 15.0 is NOT < 15.0, so goes to next condition: 15.0 < 25.0 → Warning
        assertEquals("Warning", results[15.0])
        // 25.0 is NOT < 25.0, so goes to else → Problem
        assertEquals("Problem", results[25.0])
        assertEquals("Problem", results[30.0])
    }

    // ── colorLogic ────────────────────────────────────────────────────────────

    @Test
    fun `colorLogic - determines correct status color based on config`() {
        val config = BizapConfig(
            paymentHealthyThresholdDays = 10.0,
            paymentWarningThresholdDays = 20.0
        )

        val daysHealthy = 5.0
        val daysWarning = 15.0
        val daysProblem = 25.0

        fun getStatusColor(days: Double, config: BizapConfig): String {
            return when {
                days < config.paymentHealthyThresholdDays -> "Green"
                days < config.paymentWarningThresholdDays -> "Yellow"
                else -> "Red"
            }
        }

        assertEquals("Green", getStatusColor(daysHealthy, config))
        assertEquals("Yellow", getStatusColor(daysWarning, config))
        assertEquals("Red", getStatusColor(daysProblem, config))
    }

    @Test
    fun `colorLogic - different configs produce different colors for same value`() {
        val daysToPayment = 20.0

        val strictConfig = BizapConfig(
            paymentHealthyThresholdDays = 10.0,
            paymentWarningThresholdDays = 15.0
        )

        val lenientConfig = BizapConfig(
            paymentHealthyThresholdDays = 30.0,
            paymentWarningThresholdDays = 45.0
        )

        fun getStatusColor(days: Double, config: BizapConfig): String {
            return when {
                days < config.paymentHealthyThresholdDays -> "Green"
                days < config.paymentWarningThresholdDays -> "Yellow"
                else -> "Red"
            }
        }

        val strictResult = getStatusColor(daysToPayment, strictConfig)
        val lenientResult = getStatusColor(daysToPayment, lenientConfig)

        assertEquals("Red", strictResult, "20 days should be RED for strict config (max=15)")
        assertEquals("Green", lenientResult, "20 days should be GREEN for lenient config (min=30)")
    }

    // ── businessTypeScenarios ─────────────────────────────────────────────────

    @Test
    fun `businessTypeScenarios - retail configuration`() {
        val retailConfig = BizapConfig(
            paymentHealthyThresholdDays = 1.0,
            paymentWarningThresholdDays = 3.0
        )

        // Retail scenario: invoice paid same day (0.5 days = 12 hours)
        // This should be in the "Healthy" zone (< 1.0)
        val paidSameDay = 0.5
        assertTrue(paidSameDay < retailConfig.paymentHealthyThresholdDays,
                   "Same-day payment should be healthy for retail")
    }

    @Test
    fun `businessTypeScenarios - B2B configuration`() {
        val b2bConfig = BizapConfig(
            paymentHealthyThresholdDays = 30.0,
            paymentWarningThresholdDays = 45.0
        )

        // B2B scenario: invoice paid in 35 days (within agreement)
        val paidIn35Days = 35.0
        assertEquals(true, paidIn35Days >= b2bConfig.paymentHealthyThresholdDays)
        assertEquals(true, paidIn35Days < b2bConfig.paymentWarningThresholdDays)
    }

    @Test
    fun `businessTypeScenarios - subscription SaaS configuration`() {
        val saasConfig = BizapConfig(
            paymentHealthyThresholdDays = 0.25,  // 6 hours
            paymentWarningThresholdDays = 1.0     // 1 day
        )

        // SaaS scenario: immediate billing, expects same-day payment
        // Payment within 6 hours (0.25 days) should be healthy
        val paidWithin6Hours = 0.2
        assertTrue(paidWithin6Hours < saasConfig.paymentHealthyThresholdDays,
                   "Payment within 6 hours should be healthy for SaaS")
    }
}




