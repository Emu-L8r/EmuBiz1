package com.emul8r.bizap.domain.config

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Unit tests for [BizapConfig].
 *
 * Verifies that production, testing, and development configurations have
 * expected values, and that custom configurations can be created.
 */
class BizapConfigTest {

    // ── productionDefaults ────────────────────────────────────────────────────

    @Test
    fun `productionDefaults - production config has expected splash duration`() {
        val config = BizapConfig.production()
        assertEquals(2500L, config.splashScreenMinDurationMs)
        assertEquals(5000L, config.splashScreenMaxDurationMs)
    }

    @Test
    fun `productionDefaults - production config enables auto refresh`() {
        val config = BizapConfig.production()
        assertTrue(config.enableAutoRefresh)
        assertTrue(config.dashboardRefreshOnDateChange)
    }

    @Test
    fun `productionDefaults - production config enables all feature flags`() {
        val config = BizapConfig.production()
        assertTrue(config.enableAnalyticsWidget)
        assertTrue(config.enablePaymentReminders)
        assertTrue(config.enableAutoRefresh)
    }

    @Test
    fun `productionDefaults - production config has standard UI timings`() {
        val config = BizapConfig.production()
        assertEquals(300, config.animationDurationMs)
        assertEquals(4000, config.snackbarDisplayTimeMs)
    }

    // ── testingConfig ─────────────────────────────────────────────────────────

    @Test
    fun `testingConfig - testing config has shorter splash durations`() {
        val config = BizapConfig.testing()
        assertEquals(100L, config.splashScreenMinDurationMs)
        assertEquals(500L, config.splashScreenMaxDurationMs)
    }

    @Test
    fun `testingConfig - testing config has faster animations`() {
        val config = BizapConfig.testing()
        assertEquals(50, config.animationDurationMs)
    }

    @Test
    fun `testingConfig - testing config has shorter snackbar display`() {
        val config = BizapConfig.testing()
        assertEquals(1000, config.snackbarDisplayTimeMs)
    }

    @Test
    fun `testingConfig - testing splash is faster than production`() {
        val production = BizapConfig.production()
        val testing = BizapConfig.testing()
        assertTrue(testing.splashScreenMinDurationMs < production.splashScreenMinDurationMs)
        assertTrue(testing.animationDurationMs < production.animationDurationMs)
    }

    // ── developmentConfig ─────────────────────────────────────────────────────

    @Test
    fun `developmentConfig - development config has custom splash duration`() {
        val config = BizapConfig.development()
        assertEquals(1000L, config.splashScreenMinDurationMs)
    }

    @Test
    fun `developmentConfig - development config has faster analytics refresh`() {
        val config = BizapConfig.development()
        assertEquals(1, config.analyticsRefreshIntervalMinutes)
    }

    @Test
    fun `developmentConfig - development config has slower animations`() {
        val production = BizapConfig.production()
        val development = BizapConfig.development()
        assertTrue(development.animationDurationMs > production.animationDurationMs)
    }

    // ── customConfig ──────────────────────────────────────────────────────────

    @Test
    fun `customConfig - can disable auto refresh`() {
        val config = BizapConfig(enableAutoRefresh = false)
        assertFalse(config.enableAutoRefresh)
    }

    @Test
    fun `customConfig - can disable dashboard refresh on date change`() {
        val config = BizapConfig(dashboardRefreshOnDateChange = false)
        assertFalse(config.dashboardRefreshOnDateChange)
    }

    @Test
    fun `customConfig - custom values override defaults`() {
        val config = BizapConfig(
            splashScreenMinDurationMs = 999L,
            animationDurationMs = 999,
            databaseBatchSizeLimit = 500
        )
        assertEquals(999L, config.splashScreenMinDurationMs)
        assertEquals(999, config.animationDurationMs)
        assertEquals(500, config.databaseBatchSizeLimit)
        // Non-overridden values remain at default
        assertEquals(BizapConfig().snackbarDisplayTimeMs, config.snackbarDisplayTimeMs)
    }

    @Test
    fun `customConfig - different configs are not equal`() {
        val production = BizapConfig.production()
        val testing = BizapConfig.testing()
        assertNotEquals(production, testing)
    }

    @Test
    fun `customConfig - same config values produce equal configs`() {
        val config1 = BizapConfig.production()
        val config2 = BizapConfig.production()
        assertEquals(config1, config2)
    }

    // ── paymentHealthThresholds ───────────────────────────────────────────────

    @Test
    fun `paymentHealthThresholds - production has default thresholds`() {
        val config = BizapConfig.production()
        assertEquals(15.0, config.paymentHealthyThresholdDays)
        assertEquals(25.0, config.paymentWarningThresholdDays)
    }

    @Test
    fun `paymentHealthThresholds - can configure for retail business (fast payment)`() {
        val retailConfig = BizapConfig(
            paymentHealthyThresholdDays = 1.0,
            paymentWarningThresholdDays = 3.0
        )
        assertEquals(1.0, retailConfig.paymentHealthyThresholdDays)
        assertEquals(3.0, retailConfig.paymentWarningThresholdDays)
    }

    @Test
    fun `paymentHealthThresholds - can configure for B2B business (slow payment)`() {
        val b2bConfig = BizapConfig(
            paymentHealthyThresholdDays = 30.0,
            paymentWarningThresholdDays = 45.0
        )
        assertEquals(30.0, b2bConfig.paymentHealthyThresholdDays)
        assertEquals(45.0, b2bConfig.paymentWarningThresholdDays)
    }

    @Test
    fun `paymentHealthThresholds - different business types have different thresholds`() {
        val retail = BizapConfig(paymentHealthyThresholdDays = 1.0)
        val b2b = BizapConfig(paymentHealthyThresholdDays = 30.0)
        assertTrue(retail.paymentHealthyThresholdDays < b2b.paymentHealthyThresholdDays)
    }

    @Test
    fun `paymentHealthThresholds - healthy threshold is less than warning threshold`() {
        val config = BizapConfig.production()
        assertTrue(
            config.paymentHealthyThresholdDays < config.paymentWarningThresholdDays,
            "Healthy threshold should be stricter than warning threshold"
        )
    }

    @Test
    fun `paymentHealthThresholds - can disable thresholds with zero values`() {
        val noThresholdConfig = BizapConfig(
            paymentHealthyThresholdDays = 0.0,
            paymentWarningThresholdDays = 0.0
        )
        assertEquals(0.0, noThresholdConfig.paymentHealthyThresholdDays)
        assertEquals(0.0, noThresholdConfig.paymentWarningThresholdDays)
    }
}



