package com.emul8r.bizap.fixtures

import com.emul8r.bizap.domain.model.InvoiceSettings

/**
 * Test configuration for Phase 6 Step 3 testing
 * Centralizes all test constants and configuration
 */
object TestConfig {

    // ============================================================================
    // BASIC TEST CONFIGURATION
    // ============================================================================

    const val TEST_USER_ID = "test_user_123"
    const val TEST_DB_NAME = ":memory:"
    const val TEST_TIMEOUT_MS = 5000L
    const val TEST_RESULTS_DIR = "test_results/"

    // ============================================================================
    // PERFORMANCE TARGETS
    // ============================================================================

    // Settings operations
    const val SETTINGS_LOAD_TARGET_MS = 500L      // Should load in < 500ms
    const val SETTINGS_SAVE_TARGET_MS = 200L      // Should save in < 200ms

    // Invoice operations
    const val PDF_GENERATION_TARGET_MS = 2000L    // Should generate PDF in < 2 seconds
    const val INVOICE_CREATE_TARGET_MS = 500L     // Should create invoice in < 500ms

    // Database operations
    const val DB_QUERY_TARGET_MS = 100L           // Should query in < 100ms
    const val DB_WRITE_TARGET_MS = 100L           // Should write in < 100ms

    // Memory targets
    const val PEAK_MEMORY_TARGET_MB = 50          // Peak memory should be < 50MB
    const val NORMAL_MEMORY_TARGET_MB = 30        // Normal memory usage < 30MB

    // ============================================================================
    // DEFAULT TEST SETTINGS
    // ============================================================================

    val DEFAULT_TEST_SETTINGS = FixtureBuilder.createValidSettings()

    val DEFAULT_TEST_SETTINGS_HTML = FixtureBuilder.createSettings {
        selectedTheme(com.emul8r.bizap.domain.model.InvoiceTheme.HTML_PDF)
    }

    val DEFAULT_TEST_SETTINGS_CANVAS = FixtureBuilder.createSettings {
        selectedTheme(com.emul8r.bizap.domain.model.InvoiceTheme.CANVAS)
    }

    // ============================================================================
    // TEST DATA BATCH SIZES
    // ============================================================================

    const val LOAD_TEST_INVOICE_COUNT = 100       // Load test with 100 invoices
    const val STRESS_TEST_INVOICE_COUNT = 500     // Stress test with 500 invoices
    const val EDGE_CASE_TEST_COUNT = 50           // Test 50 edge cases

    // ============================================================================
    // WORKFLOW DEFINITIONS
    // ============================================================================

    object Workflows {
        const val WORKFLOW_1_NAME = "Settings → Invoice → PDF"
        const val WORKFLOW_2_NAME = "Theme Switching"
        const val WORKFLOW_3_NAME = "Data Persistence"
        const val WORKFLOW_4_NAME = "Settings Updates"

        object Workflow1 {
            val name = WORKFLOW_1_NAME
            val steps = listOf(
                "Create settings with company info",
                "Create invoice with items",
                "Generate PDF with Canvas theme",
                "Verify output",
                "Switch to HTML theme",
                "Generate PDF again",
                "Compare outputs"
            )
        }

        object Workflow2 {
            val name = WORKFLOW_2_NAME
            val steps = listOf(
                "Create settings with Canvas theme",
                "Generate invoice",
                "Switch to HTML theme",
                "Generate invoice again",
                "Verify both outputs differ correctly",
                "Switch back to Canvas",
                "Verify original output"
            )
        }

        object Workflow3 {
            val name = WORKFLOW_3_NAME
            val steps = listOf(
                "Create settings",
                "Save to database",
                "Close app (simulate)",
                "Reopen app",
                "Load settings",
                "Verify all data persists",
                "Repeat with different data"
            )
        }

        object Workflow4 {
            val name = WORKFLOW_4_NAME
            val steps = listOf(
                "Create initial settings",
                "Update business name",
                "Save changes",
                "Reload and verify",
                "Generate invoice",
                "Verify new name in invoice",
                "Test with multiple changes"
            )
        }
    }

    // ============================================================================
    // LOGGING CONFIGURATION
    // ============================================================================

    const val ENABLE_DEBUG_LOGGING = true
    const val ENABLE_PERFORMANCE_LOGGING = true
    const val ENABLE_DETAILED_TIMING = true
    const val ENABLE_MEMORY_TRACKING = true

    // ============================================================================
    // TIMING AND RETRIES
    // ============================================================================

    const val RETRY_COUNT = 3
    const val RETRY_DELAY_MS = 500L
    const val MAX_TEST_DURATION_MS = 60000L    // 1 minute max per test

    // ============================================================================
    // HELPER FUNCTIONS
    // ============================================================================

    /**
     * Check if actual time meets target
     */
    fun checkTiming(actualMs: Long, targetMs: Long): Boolean {
        return actualMs <= targetMs
    }

    /**
     * Check if memory usage is within target
     */
    fun checkMemory(usedMB: Int, targetMB: Int): Boolean {
        return usedMB <= targetMB
    }

    /**
     * Format milliseconds for logging
     */
    fun formatTime(ms: Long): String {
        return when {
            ms < 1000 -> "${ms}ms"
            else -> "%.2fs".format(ms / 1000.0)
        }
    }

    /**
     * Format bytes for logging
     */
    fun formatMemory(bytes: Long): String {
        return when {
            bytes < 1024 -> "${bytes}B"
            bytes < 1024 * 1024 -> "%.2fKB".format(bytes / 1024.0)
            else -> "%.2fMB".format(bytes / (1024.0 * 1024.0))
        }
    }

    /**
     * Get pass/fail status
     */
    fun getStatus(passed: Boolean): String {
        return if (passed) "✅ PASS" else "❌ FAIL"
    }
}

