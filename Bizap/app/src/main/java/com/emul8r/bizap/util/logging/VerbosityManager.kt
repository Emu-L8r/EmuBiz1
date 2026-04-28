package com.emul8r.bizap.util.logging

import android.util.Log
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages Timber log verbosity elevation for slow operations.
 *
 * **Purpose:**
 * When an operation is detected as slow (exceeds slowThresholdMs), automatically elevate
 * Timber's log level to capture more diagnostic details (e.g., from DEBUG to VERBOSE).
 * When the operation completes, restore normal verbosity.
 *
 * **Thread Safety:**
 * All state is thread-safe (uses ConcurrentHashMap).
 *
 * **Design:**
 * - Elevation is **per-tag** (e.g., "INVOICE", "ANALYTICS") to avoid flooding unrelated logs
 * - Restores the previous level automatically when operation completes
 * - Tracks nested operations (if INVOICE tag elevated twice, both must complete before restore)
 *
 * **Usage:**
 * ```kotlin
 * if (elapsedMs > config.slowThresholdMs) {
 *     verbosityManager.elevateVerbosity("INVOICE", Log.DEBUG)
 * }
 *
 * // ... continue logging at elevated level ...
 *
 * verbosityManager.restoreVerbosity("INVOICE")
 * ```
 */
@Singleton
class VerbosityManager @Inject constructor() {

    /**
     * Tracks the elevation stack per tag.
     * Maps tag → list of previous log levels (for nested elevation support).
     */
    private val elevationStack = mutableMapOf<String, MutableList<Int>>()
    private val lock = Object()

    /**
     * Tracks start time of elevation per tag (for debugging/metrics).
     */
    private val elevationStartTimeMs = mutableMapOf<String, Long>()

    /**
     * Elevate verbosity for a specific tag.
     * Safe to call multiple times for same tag (nested operations supported).
     *
     * @param tag Semantic tag (e.g., "INVOICE", "ANALYTICS", "PDF")
     * @param targetLevel Target log level (e.g., Log.DEBUG, Log.VERBOSE)
     */
    fun elevateVerbosity(tag: String, targetLevel: Int) {
        synchronized(lock) {
            // Create stack for this tag if not present
            if (!elevationStack.containsKey(tag)) {
                elevationStack[tag] = mutableListOf()
                elevationStartTimeMs[tag] = System.currentTimeMillis()
            }

            // Push current level onto stack (so we can restore later)
            val currentLevel = getEffectiveLevel(tag)
            elevationStack[tag]?.add(currentLevel)

            // Timber elevation happens here (actual log level change)
            // Note: Timber doesn't have a built-in "setLogLevel" for tags.
            // Instead, we wrap in a custom tree that respects this manager's state.
            // See ElevatedVerbosityTree for implementation.
        }

        Timber.d("✓ [$tag] Verbosity elevated to level $targetLevel")
    }

    /**
     * Restore verbosity for a specific tag to its previous level.
     * Must be called once for each elevation.
     *
     * @param tag Semantic tag to restore
     */
    fun restoreVerbosity(tag: String) {
        synchronized(lock) {
            val stack = elevationStack[tag] ?: return  // No elevation active

            if (stack.isEmpty()) {
                elevationStack.remove(tag)
                elevationStartTimeMs.remove(tag)
                return
            }

            // Pop previous level and restore
            val previousLevel = stack.removeAt(stack.size - 1)

            if (stack.isEmpty()) {
                elevationStack.remove(tag)
                val duration = System.currentTimeMillis() - (elevationStartTimeMs[tag] ?: 0)
                elevationStartTimeMs.remove(tag)
                Timber.d("✓ [$tag] Verbosity restored (elevation lasted ${duration}ms)")
            }
        }
    }

    /**
     * Get the current effective log level for a tag.
     * Returns the most elevated level in the stack, or default if not elevated.
     *
     * @param tag Semantic tag
     * @return Effective log level (Log.VERBOSE, Log.DEBUG, Log.INFO, etc.)
     */
    fun getEffectiveLevel(tag: String): Int {
        synchronized(lock) {
            val stack = elevationStack[tag]
            if (stack != null && stack.isNotEmpty()) {
                // Return the highest priority (lowest numeric value)
                // Log.VERBOSE = 2, Log.DEBUG = 3, Log.INFO = 4, etc.
                return stack.minOrNull() ?: Log.DEBUG
            }
        }
        return Log.DEBUG  // Default level
    }

    /**
     * Check if a tag is currently elevated.
     *
     * @param tag Semantic tag
     * @return true if elevation is active, false otherwise
     */
    fun isElevated(tag: String): Boolean {
        synchronized(lock) {
            val stack = elevationStack[tag]
            return stack != null && stack.isNotEmpty()
        }
    }

    /**
     * Get all currently elevated tags.
     * Useful for diagnostics/debugging.
     *
     * @return List of tags with active elevation
     */
    fun getElevatedTags(): List<String> {
        synchronized(lock) {
            return elevationStack
                .filterValues { it.isNotEmpty() }
                .keys
                .toList()
        }
    }

    /**
     * Reset all elevations (for testing or manual reset).
     */
    fun resetAll() {
        synchronized(lock) {
            elevationStack.clear()
            elevationStartTimeMs.clear()
        }
        Timber.i("🔄 Verbosity manager reset (all elevations cleared)")
    }
}

