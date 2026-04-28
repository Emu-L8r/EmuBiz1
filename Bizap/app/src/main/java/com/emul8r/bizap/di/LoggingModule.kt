package com.emul8r.bizap.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.emul8r.bizap.util.logging.DashboardObservabilityManager
import com.emul8r.bizap.util.logging.LogAggregator
import com.emul8r.bizap.util.logging.OperationConfig
import com.emul8r.bizap.util.logging.OperationTracker
import com.emul8r.bizap.util.logging.PerformanceMetricsCalculator
import com.emul8r.bizap.util.logging.VerbosityManager
import javax.inject.Named
import javax.inject.Singleton

/**
 * Hilt DI Module for Phase 4/5 Logging Infrastructure.
 *
 * Provides singleton instances of:
 * - VerbosityManager (Phase 4: Auto-adaptive log level elevation)
 * - OperationTracker (Tracks currently-active operations)
 * - LogAggregator (Phase 5: Circular buffer for operation metrics)
 * - DashboardObservabilityManager (Phase 5: Orchestrator + StateFlow exports)
 * - OperationConfig instances (Per-feature configuration)
 *
 * Architecture:
 * 1. VerbosityManager & OperationTracker are injected into ContextBlockLogger.initialize()
 * 2. LogAggregator is injected into ContextBlockLogger.initialize()
 * 3. DashboardObservabilityManager is started in BizapApplication.onCreate()
 * 4. ViewModels inject specific OperationConfig instances via @Named qualifiers
 *
 * @see ContextBlockLogger.initialize() in BizapApplication.onCreate()
 */
@Module
@InstallIn(SingletonComponent::class)
object LoggingModule {

    /**
     * Provides the singleton VerbosityManager for Phase 4.
     * Manages Timber log level elevation/restoration on a per-tag basis.
     */
    @Provides
    @Singleton
    fun provideVerbosityManager(): VerbosityManager {
        return VerbosityManager()
    }

    /**
     * Provides the singleton OperationTracker for Phase 4/5.
     * Tracks currently-active operations and provides observability.
     */
    @Provides
    @Singleton
    fun provideOperationTracker(): OperationTracker {
        return OperationTracker()
    }

    /**
     * Provides the singleton LogAggregator for Phase 5.
     * Circular buffer (~50 KB for 1000 entries) collecting operation metrics.
     * Thread-safe (ConcurrentLinkedQueue).
     */
    @Provides
    @Singleton
    fun provideLogAggregator(): LogAggregator {
        return LogAggregator(maxSize = 1000)  // ~50 KB
    }

    /**
     * Provides the singleton PerformanceMetricsCalculator for Phase 5.
     * Calculates percentiles and detects anomalies in operation metrics.
     */
    @Provides
    @Singleton
    fun providePerformanceMetricsCalculator(): PerformanceMetricsCalculator {
        return PerformanceMetricsCalculator()
    }

    /**
     * Provides the singleton DashboardObservabilityManager for Phase 5.
     * Orchestrates metric calculation, anomaly detection, and StateFlow emissions.
     * Runs on IO dispatcher to avoid blocking main thread.
     *
     * Started via observabilityManager.start() in BizapApplication.onCreate().
     */
    @Provides
    @Singleton
    fun provideDashboardObservabilityManager(
        aggregator: LogAggregator,
        calculator: PerformanceMetricsCalculator
    ): DashboardObservabilityManager {
        return DashboardObservabilityManager(aggregator, calculator)
    }

    // ============================================================================
    // Per-Feature OperationConfig Instances (Phase 4/5 Configuration)
    // ============================================================================
    // ViewModels inject these via @Named qualifiers (e.g., @Named("invoice_operation_config"))
    // to customize Phase 4/5 behavior per operation type.
    // ============================================================================

    /**
     * Configuration for Invoice Operations (Create, Edit, Export).
     * Slow threshold: 300ms (strict - invoices should be quick)
     * Targets: fast user feedback, performance monitoring
     */
    @Provides
    @Singleton
    @Named("invoice_operation_config")
    fun provideInvoiceOperationConfig(): OperationConfig {
        return OperationConfig.forInvoiceOperations()
    }

    /**
     * Configuration for Analytics Operations (Dashboard metrics, revenue trends).
     * Slow threshold: 1000ms (lenient - analytics is expected to be slower)
     * Targets: background aggregation, trend detection
     */
    @Provides
    @Singleton
    @Named("analytics_operation_config")
    fun provideAnalyticsOperationConfig(): OperationConfig {
        return OperationConfig.forAnalyticsOperations()
    }

    /**
     * Configuration for Payment Operations (Recording, tracking, dunning).
     * Slow threshold: 500ms (moderate)
     * Targets: payment reliability, DSO tracking
     */
    @Provides
    @Singleton
    @Named("payment_operation_config")
    fun providePaymentOperationConfig(): OperationConfig {
        return OperationConfig(
            slowThresholdMs = 500L,
            enableAutoVerbosity = true,
            targetVerbosityLevel = android.util.Log.DEBUG,
            aggregationEnabled = true
        )
    }

    /**
     * Configuration for PDF Export Operations (Export, generation).
     * Slow threshold: 2000ms (very lenient - PDF generation is inherently slow)
     * Targets: user feedback, export performance tracking
     */
    @Provides
    @Singleton
    @Named("pdf_operation_config")
    fun providePdfOperationConfig(): OperationConfig {
        return OperationConfig(
            slowThresholdMs = 2000L,
            enableAutoVerbosity = true,
            targetVerbosityLevel = android.util.Log.INFO,  // Verbose for debugging PDF issues
            aggregationEnabled = true
        )
    }

    /**
     * Configuration for Backup/Restore Operations (Heavy I/O).
     * Slow threshold: 3000ms (very lenient - data I/O is slow)
     * Targets: reliability, restore completion tracking
     */
    @Provides
    @Singleton
    @Named("backup_restore_operation_config")
    fun provideBackupRestoreOperationConfig(): OperationConfig {
        return OperationConfig(
            slowThresholdMs = 3000L,
            enableAutoVerbosity = false,  // Disabled - backups are inherently slow
            targetVerbosityLevel = android.util.Log.DEBUG,
            aggregationEnabled = true
        )
    }

    /**
     * Configuration for CSV Export Operations (Data serialization, file I/O).
     * Slow threshold: 1500ms (moderate-lenient)
     * Targets: export completion, data integrity
     */
    @Provides
    @Singleton
    @Named("csv_export_operation_config")
    fun provideCsvExportOperationConfig(): OperationConfig {
        return OperationConfig(
            slowThresholdMs = 1500L,
            enableAutoVerbosity = true,
            targetVerbosityLevel = android.util.Log.DEBUG,
            aggregationEnabled = true
        )
    }

    /**
     * Configuration for Sync Operations (Network + DB writes).
     * Slow threshold: 2000ms (lenient - network is unpredictable)
     * Targets: sync reliability, network diagnostics
     */
    @Provides
    @Singleton
    @Named("sync_operation_config")
    fun provideSyncOperationConfig(): OperationConfig {
        return OperationConfig(
            slowThresholdMs = 2000L,
            enableAutoVerbosity = true,
            targetVerbosityLevel = android.util.Log.INFO,
            aggregationEnabled = true
        )
    }

    /**
     * Configuration for Low-End Devices (API 21-24, <2GB RAM).
     * Disables Phase 4/5 to avoid overhead.
     * Targets: compatibility, battery preservation
     */
    @Provides
    @Singleton
    @Named("low_end_device_operation_config")
    fun provideLowEndDeviceOperationConfig(): OperationConfig {
        return OperationConfig.forLowEndDevice()
    }

    /**
     * Configuration for Premium Devices (Pixel 7+, >6GB RAM).
     * Stricter thresholds, full Phase 4/5 features enabled.
     * Targets: performance optimization, aggressive monitoring
     */
    @Provides
    @Singleton
    @Named("premium_device_operation_config")
    fun providePremiumDeviceOperationConfig(): OperationConfig {
        return OperationConfig.forPremiumDevice()
    }
}



