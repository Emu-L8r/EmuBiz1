package com.emul8r.bizap.util.logging

import android.util.Log

/**
 * Configuration for operation logging behavior.
 *
 * Allows fine-grained control over:
 * - When operations are considered "slow"
 * - Whether automatic verbosity elevation is enabled
 * - Target verbosity level during slow operations
 * - Whether metrics aggregation is enabled
 *
 * Can be injected per-ViewModel or globally, and overridden via Firebase Remote Config.
 *
 * @param slowThresholdMs Operation duration threshold (ms) above which operation is considered "slow"
 * @param enableAutoVerbosity If true, automatically elevate Timber log level for slow operations
 * @param targetVerbosityLevel Target Timber log level during slow operations (e.g., Log.DEBUG, Log.VERBOSE)
 * @param aggregationEnabled If true, collect operation metrics for dashboard observability
 * @param maxAggregationBufferSize Maximum number of operation entries to keep in rolling buffer
 */
data class OperationConfig(
    val slowThresholdMs: Long = 500L,
    val enableAutoVerbosity: Boolean = true,
    val targetVerbosityLevel: Int = Log.DEBUG,
    val aggregationEnabled: Boolean = true,
    val maxAggregationBufferSize: Int = 1000
) {
    companion object {
        /**
         * Default configuration for all operations.
         * Balanced for most devices/use cases.
         */
        fun default() = OperationConfig(
            slowThresholdMs = 500L,
            enableAutoVerbosity = true,
            targetVerbosityLevel = Log.DEBUG,
            aggregationEnabled = true,
            maxAggregationBufferSize = 1000
        )

        /**
         * Configuration for low-end devices (avoid overhead).
         */
        fun forLowEndDevice() = OperationConfig(
            slowThresholdMs = 1000L,  // More lenient
            enableAutoVerbosity = false,  // Skip verbosity elevation (save CPU)
            targetVerbosityLevel = Log.INFO,
            aggregationEnabled = false,  // Disable aggregation (save memory)
            maxAggregationBufferSize = 100
        )

        /**
         * Configuration for mid-range devices (balanced).
         */
        fun forMidRangeDevice() = OperationConfig(
            slowThresholdMs = 500L,
            enableAutoVerbosity = true,
            targetVerbosityLevel = Log.DEBUG,
            aggregationEnabled = true,
            maxAggregationBufferSize = 500
        )

        /**
         * Configuration for premium devices (aggressive monitoring).
         */
        fun forPremiumDevice() = OperationConfig(
            slowThresholdMs = 300L,  // Stricter threshold
            enableAutoVerbosity = true,
            targetVerbosityLevel = Log.VERBOSE,
            aggregationEnabled = true,
            maxAggregationBufferSize = 1000
        )

        /**
         * Configuration optimized for invoice operations (fast expected, low tolerance).
         */
        fun forInvoiceOperations() = OperationConfig(
            slowThresholdMs = 300L,
            enableAutoVerbosity = true,
            targetVerbosityLevel = Log.DEBUG,
            aggregationEnabled = true,
            maxAggregationBufferSize = 1000
        )

        /**
         * Configuration optimized for analytics operations (slower expected).
         */
        fun forAnalyticsOperations() = OperationConfig(
            slowThresholdMs = 1000L,
            enableAutoVerbosity = true,
            targetVerbosityLevel = Log.DEBUG,
            aggregationEnabled = true,
            maxAggregationBufferSize = 1000
        )

        /**
         * Configuration with aggregation disabled (for performance testing).
         */
        fun withoutAggregation() = OperationConfig(
            slowThresholdMs = 500L,
            enableAutoVerbosity = true,
            targetVerbosityLevel = Log.DEBUG,
            aggregationEnabled = false,
            maxAggregationBufferSize = 0
        )
    }
}

