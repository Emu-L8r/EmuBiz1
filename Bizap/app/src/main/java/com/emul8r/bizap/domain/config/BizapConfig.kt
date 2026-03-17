package com.emul8r.bizap.domain.config

/**
 * Centralized configuration for hardcoded business logic.
 * Enables testing and customization without code changes.
 */
data class BizapConfig(
    // Splash Screen
    val splashScreenMinDurationMs: Long = 2500,
    val splashScreenMaxDurationMs: Long = 5000,

    // Analytics
    val analyticsRefreshIntervalMinutes: Int = 5,
    val analyticsDataRetentionDays: Int = 90,

    // Invoice Processing
    val invoiceReminderDaysBeforeDue: Int = 3,
    val invoiceAutoMarkPaidDays: Int = 30,

    // Dashboard
    val dashboardRefreshOnDateChange: Boolean = true,
    val dashboardMaxDisplayedInvoices: Int = 50,

    // Database
    val databaseQueryTimeoutMs: Long = 30_000,
    val databaseBatchSizeLimit: Int = 1000,

    // UI
    val animationDurationMs: Int = 300,
    val snackbarDisplayTimeMs: Int = 4000,

    // Feature Flags
    val enableAnalyticsWidget: Boolean = true,
    val enablePaymentReminders: Boolean = true,
    val enableAutoRefresh: Boolean = true
) {
    companion object {
        /**
         * Default production configuration.
         */
        fun production() = BizapConfig()

        /**
         * Configuration for testing (shorter timeouts, faster animations).
         */
        fun testing() = BizapConfig(
            splashScreenMinDurationMs = 100,
            splashScreenMaxDurationMs = 500,
            animationDurationMs = 50,
            snackbarDisplayTimeMs = 1000
        )

        /**
         * Configuration for development (verbose logging, longer timeouts).
         */
        fun development() = BizapConfig(
            splashScreenMinDurationMs = 1000,
            analyticsRefreshIntervalMinutes = 1,
            animationDurationMs = 500
        )
    }
}
