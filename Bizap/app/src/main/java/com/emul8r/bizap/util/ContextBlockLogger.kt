package com.emul8r.bizap.util

import com.emul8r.bizap.util.logging.LogAggregator
import com.emul8r.bizap.util.logging.OperationConfig
import com.emul8r.bizap.util.logging.OperationEntry
import com.emul8r.bizap.util.logging.OperationTracker
import com.emul8r.bizap.util.logging.VerbosityManager
import timber.log.Timber

/**
 * ContextBlockLogger — Logs operation start/completion with automatic timing.
 *
 * **Phase 1 (Baseline):**
 * Wraps long-running operations (>100ms) with >> START and << COMPLETED markers
 * for easy scanning and performance measurement in Logcat.
 *
 * **Phase 4 (Auto-Adaptive Verbosity):**
 * Automatically elevates Timber log level for operations exceeding slowThresholdMs.
 * Restores normal verbosity when operation completes.
 * Controlled via OperationConfig per-ViewModel or globally.
 *
 * **Phase 5 (Dashboard Observability):**
 * Collects operation metrics (duration, success/failure) into LogAggregator.
 * Enables dashboard observability, performance trend detection, and alerting.
 *
 * **Usage:**
 * ```
 * // Phase 1 (existing API, still works)
 * ContextBlockLogger.logOperation("INVOICE", "Creating invoice") {
 *     invoiceRepository.createInvoice(data)
 * }
 *
 * // Phase 4 & 5 (with config)
 * val config = OperationConfig.forInvoiceOperations()
 * ContextBlockLogger.logOperation("INVOICE", "Creating invoice", config = config) {
 *     invoiceRepository.createInvoice(data)
 * }
 *
 * // Logcat output:
 * // >> [INVOICE] Creating invoice...
 * // ... internal logs ...
 * // << [INVOICE] COMPLETED: 234ms 📊
 * ```
 *
 * @param tag Semantic tag (e.g., "INVOICE", "PDF", "ANALYTICS")
 * @param operationName Human-readable operation name
 * @param block Suspend lambda to execute and time
 * @param warnThresholdMs Optional threshold (log 🟡 if exceeds)
 * @param config Optional OperationConfig for Phase 4/5 features (auto-verbosity, aggregation)
 */
object ContextBlockLogger {

    // Phase 4/5 dependencies (injected via DI when available)
    // Note: Must be public and @PublishedApi for inline functions to access
    @PublishedApi
    internal var verbosityManager: VerbosityManager? = null
    @PublishedApi
    internal var operationTracker: OperationTracker? = null
    @PublishedApi
    internal var logAggregator: LogAggregator? = null

    /**
     * Initialize Phase 4/5 dependencies.
     * Called during app initialization (ideally in Application.onCreate()).
     * If not called, Phase 4/5 features are silently skipped (graceful degradation).
     */
    fun initialize(
        verbosityMgr: VerbosityManager,
        opTracker: OperationTracker,
        aggregator: LogAggregator
    ) {
        verbosityManager = verbosityMgr
        operationTracker = opTracker
        logAggregator = aggregator
        Timber.d("✓ ContextBlockLogger initialized (Phase 4/5 enabled)")
    }

    /**
     * Logs a suspend operation with start/completion markers and auto-timing.
     *
     * **Phase 4 Behavior:**
     * If config.enableAutoVerbosity == true and elapsed > config.slowThresholdMs,
     * elevates Timber log level to config.targetVerbosityLevel for remaining duration.
     *
     * **Phase 5 Behavior:**
     * If config.aggregationEnabled == true, records operation metrics to LogAggregator.
     */
    suspend inline fun <T> logOperation(
        tag: String,
        operationName: String,
        warnThresholdMs: Long = Long.MAX_VALUE,
        config: OperationConfig? = null,
        block: suspend () -> T
    ): T {
        val startMs = System.currentTimeMillis()
        val actualConfig = config ?: OperationConfig.default()

        Timber.d(">> [$tag] $operationName...")

        return try {
            val result = block()
            val elapsedMs = System.currentTimeMillis() - startMs

            // Phase 4: Auto-elevate verbosity if operation is slow
            if (actualConfig.enableAutoVerbosity && elapsedMs > actualConfig.slowThresholdMs) {
                verbosityManager?.elevateVerbosity(tag, actualConfig.targetVerbosityLevel)
            }

            // Log completion with emoji and time
            if (elapsedMs > warnThresholdMs) {
                Timber.w("🟡 [$tag] COMPLETED (slow): $elapsedMs ms 📊")
            } else {
                Timber.d("🟢 [$tag] COMPLETED: $elapsedMs ms 📊")
            }

            // Phase 5: Record to aggregator
            if (actualConfig.aggregationEnabled && logAggregator != null) {
                logAggregator!!.append(
                    OperationEntry.success(
                        tag = tag,
                        operationName = operationName,
                        durationMs = elapsedMs
                    )
                )
            }

            // Phase 4: Restore verbosity
            if (actualConfig.enableAutoVerbosity && elapsedMs > actualConfig.slowThresholdMs) {
                verbosityManager?.restoreVerbosity(tag)
            }

            result
        } catch (e: Exception) {
            val elapsedMs = System.currentTimeMillis() - startMs
            Timber.e("🔴 [$tag] FAILED after $elapsedMs ms: ${e.message}")

            // Phase 5: Record failure
            if (actualConfig.aggregationEnabled && logAggregator != null) {
                logAggregator!!.append(
                    OperationEntry.failure(
                        tag = tag,
                        operationName = operationName,
                        durationMs = elapsedMs,
                        errorMessage = e.message ?: "Unknown error"
                    )
                )
            }

            // Phase 4: Restore verbosity
            if (actualConfig.enableAutoVerbosity) {
                verbosityManager?.restoreVerbosity(tag)
            }

            throw e
        }
    }

    /**
     * Logs a synchronous operation with start/completion markers and auto-timing.
     *
     * **Phase 4 Behavior:**
     * If config.enableAutoVerbosity == true and elapsed > config.slowThresholdMs,
     * elevates Timber log level to config.targetVerbosityLevel for remaining duration.
     *
     * **Phase 5 Behavior:**
     * If config.aggregationEnabled == true, records operation metrics to LogAggregator.
     */
    inline fun <T> logOperationSync(
        tag: String,
        operationName: String,
        warnThresholdMs: Long = Long.MAX_VALUE,
        config: OperationConfig? = null,
        block: () -> T
    ): T {
        val startMs = System.currentTimeMillis()
        val actualConfig = config ?: OperationConfig.default()

        Timber.d(">> [$tag] $operationName...")

        return try {
            val result = block()
            val elapsedMs = System.currentTimeMillis() - startMs

            // Phase 4: Auto-elevate verbosity if operation is slow
            if (actualConfig.enableAutoVerbosity && elapsedMs > actualConfig.slowThresholdMs) {
                verbosityManager?.elevateVerbosity(tag, actualConfig.targetVerbosityLevel)
            }

            // Log completion with emoji and time
            if (elapsedMs > warnThresholdMs) {
                Timber.w("🟡 [$tag] COMPLETED (slow): $elapsedMs ms 📊")
            } else {
                Timber.d("🟢 [$tag] COMPLETED: $elapsedMs ms 📊")
            }

            // Phase 5: Record to aggregator
            if (actualConfig.aggregationEnabled && logAggregator != null) {
                logAggregator!!.append(
                    OperationEntry.success(
                        tag = tag,
                        operationName = operationName,
                        durationMs = elapsedMs
                    )
                )
            }

            // Phase 4: Restore verbosity
            if (actualConfig.enableAutoVerbosity && elapsedMs > actualConfig.slowThresholdMs) {
                verbosityManager?.restoreVerbosity(tag)
            }

            result
        } catch (e: Exception) {
            val elapsedMs = System.currentTimeMillis() - startMs
            Timber.e("🔴 [$tag] FAILED after $elapsedMs ms: ${e.message}")

            // Phase 5: Record failure
            if (actualConfig.aggregationEnabled && logAggregator != null) {
                logAggregator!!.append(
                    OperationEntry.failure(
                        tag = tag,
                        operationName = operationName,
                        durationMs = elapsedMs,
                        errorMessage = e.message ?: "Unknown error"
                    )
                )
            }

            // Phase 4: Restore verbosity
            if (actualConfig.enableAutoVerbosity) {
                verbosityManager?.restoreVerbosity(tag)
            }

            throw e
        }
    }

    /**
     * Logs start of an operation without automatic completion.
     * Use when operation spans multiple async boundaries.
     *
     * **Phase 4 & 5:** No automatic behavior here; called methods are used at completion.
     *
     * Usage:
     * ```
     * contextBlockLogger.logStart("SYNC", "Processing offline queue")
     * viewModel.uiState.collect { state ->
     *     if (state is SyncUiState.Complete) {
     *         contextBlockLogger.logComplete("SYNC", startMs, "Offline queue processed")
     *     }
     * }
     * ```
     */
    fun logStart(tag: String, operationName: String): Long {
        Timber.d(">> [$tag] $operationName...")
        return System.currentTimeMillis()
    }

    /**
     * Logs completion of a manually-timed operation.
     * Pair with [logStart] for operations spanning async boundaries.
     *
     * **Phase 4:** No automatic behavior (elevated if started at this call).
     * **Phase 5:** Automatically records to aggregator.
     */
    fun logComplete(
        tag: String,
        startMs: Long,
        operationName: String,
        warnThresholdMs: Long = Long.MAX_VALUE,
        config: OperationConfig? = null
    ) {
        val elapsedMs = System.currentTimeMillis() - startMs
        val actualConfig = config ?: OperationConfig.default()

        if (elapsedMs > warnThresholdMs) {
            Timber.w("🟡 [$tag] COMPLETED (slow): $elapsedMs ms 📊 — $operationName")
        } else {
            Timber.d("🟢 [$tag] COMPLETED: $elapsedMs ms 📊 — $operationName")
        }

        // Phase 5: Record to aggregator
        if (actualConfig.aggregationEnabled && logAggregator != null) {
            logAggregator!!.append(
                OperationEntry.success(
                    tag = tag,
                    operationName = operationName,
                    durationMs = elapsedMs
                )
            )
        }
    }

    /**
     * Logs a failure for a manually-timed operation.
     *
     * **Phase 5:** Automatically records to aggregator as failure.
     */
    fun logFailure(
        tag: String,
        startMs: Long,
        operationName: String,
        exception: Throwable,
        config: OperationConfig? = null
    ) {
        val elapsedMs = System.currentTimeMillis() - startMs
        val actualConfig = config ?: OperationConfig.default()

        Timber.e("🔴 [$tag] FAILED after $elapsedMs ms — $operationName: ${exception.message}")

        // Phase 5: Record failure
        if (actualConfig.aggregationEnabled && logAggregator != null) {
            logAggregator!!.append(
                OperationEntry.failure(
                    tag = tag,
                    operationName = operationName,
                    durationMs = elapsedMs,
                    errorMessage = exception.message ?: "Unknown error"
                )
            )
        }
    }
}

/**
 * Convenience extension for ViewModels and suspend functions.
 * Usage: logOperation("INVOICE", "Saving invoice") { ... }
 *
 * **Phase 4 & 5:** Works with default OperationConfig.
 * To customize, pass explicit config: logOperation("INVOICE", ..., config = OperationConfig.forInvoiceOperations())
 */
suspend inline fun <T> logOperation(
    tag: String,
    operationName: String,
    warnThresholdMs: Long = Long.MAX_VALUE,
    config: OperationConfig? = null,
    block: suspend () -> T
): T = ContextBlockLogger.logOperation(tag, operationName, warnThresholdMs, config, block)

/**
 * Convenience extension for synchronous operations.
 */
inline fun <T> logOperationSync(
    tag: String,
    operationName: String,
    warnThresholdMs: Long = Long.MAX_VALUE,
    config: OperationConfig? = null,
    block: () -> T
): T = ContextBlockLogger.logOperationSync(tag, operationName, warnThresholdMs, config, block)









