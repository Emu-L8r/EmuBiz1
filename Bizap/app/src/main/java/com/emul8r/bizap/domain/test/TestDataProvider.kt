package com.emul8r.bizap.domain.test

import android.util.Log
import com.emul8r.bizap.ui.invoices.LineItemForm

/**
 * FINAL VERSION v2: TEST DATA PROVIDER
 * Provides high-quality, "Heavy" test data for PDF engine verification.
 */
object TestDataProvider {
    
    fun logInitialization() {
        Log.d("TestData", "✅ TEST DATA PROVIDER INITIALIZED")
        Log.d("TestData", """
            Available Line Items:
            - LONG_DESCRIPTION: "Comprehensive consulting services..."
            - SUPPORT: "Support and maintenance package..."
        """.trimIndent())
    }

    fun getDebugInitialHeader() = "Professional Consulting Services"
    fun getDebugInitialSubheader() = "Strategic Business Review & Audit"
    fun getDebugInitialNotes() = "Development test invoice - auto-populated with defaults. Please pay within 14 days."
    fun getDebugInitialFooter() = "Bizap - Deterministic Invoicing System"
    
    fun getDebugLineItems() = listOf(
        LineItemForm(
            description = "Comprehensive consulting services including business restructuring, strategic planning, and legal compliance audit with full documentation and follow-up support",
            quantity = 1.0,
            unitPrice = 2500.0
        ),
        LineItemForm(
            description = "Support and maintenance package (includes 24/7 monitoring, incident response, and quarterly optimization reviews)",
            quantity = 1.0,
            unitPrice = 500.0
        )
    )
}
