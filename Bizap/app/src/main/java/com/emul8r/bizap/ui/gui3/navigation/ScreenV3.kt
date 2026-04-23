package com.emul8r.bizap.ui.gui3.navigation

import kotlinx.serialization.Serializable

/**
 * GUI3 Screen Routes
 *
 * Type-safe navigation routes for Matrix UI screens.
 * **CRITICAL DISCIPLINE:** All parameters are MANDATORY (no defaults).
 * This matches GUI2's ScreenV2 pattern to prevent data-bleed bugs.
 *
 * Rule: If businessId is omitted at compile-time, it MUST fail.
 * No lazy defaults that could cause silent context collapse.
 */
sealed class ScreenV3 {
    @Serializable
    data class Dashboard(val businessId: Long) : ScreenV3()

    @Serializable
    data class Invoices(val businessId: Long) : ScreenV3()

    @Serializable
    data class InvoiceDetail(val businessId: Long, val invoiceId: Long) : ScreenV3()

    @Serializable
    data class CreateInvoice(val businessId: Long) : ScreenV3()

    @Serializable
    data class EditInvoice(val businessId: Long, val invoiceId: Long) : ScreenV3()

    @Serializable
    data class Customers(val businessId: Long) : ScreenV3()

    @Serializable
    data class CustomerDetail(val businessId: Long, val customerId: Long) : ScreenV3()

    @Serializable
    data class CreateCustomer(val businessId: Long) : ScreenV3()

    @Serializable
    data class EditCustomer(val businessId: Long, val customerId: Long) : ScreenV3()

    @Serializable
    data class PaymentTracking(val businessId: Long) : ScreenV3()

    @Serializable
    data class Settings(val businessId: Long) : ScreenV3()

    @Serializable
    data class Reports(val businessId: Long) : ScreenV3()

    @Serializable
    data class Help(val businessId: Long) : ScreenV3()

    /**
     * Template items screen for quickly managing invoice presets.
     */
    @Serializable
    data class PrefilledItems(val businessId: Long) : ScreenV3()

    @Serializable
    data class PaymentAnalytics(val businessId: Long) : ScreenV3()

    @Serializable
    data class RevenueAnalytics(val businessId: Long) : ScreenV3()

    /**
     * PARITY FIX (Ultrathink Diagnosis - Missing in GUI3):
     * Focused analytics insights screen (analytics deep-dive).
     * Reuses AnalyticsViewModel from GUI2 (Pattern 2C).
     */
    @Serializable
    data class AnalyticsFocusedInsights(val businessId: Long) : ScreenV3()

    /**
     * PARITY FIX (Ultrathink Diagnosis - Missing in GUI3):
     * Invoice-specific analytics and trends.
     * Reuses AnalyticsViewModel from GUI2 (Pattern 2C).
     */
    @Serializable
    data class InvoiceAnalytics(val businessId: Long) : ScreenV3()

    /**
     * PARITY FIX (Ultrathink Diagnosis - Missing in GUI3):
     * Advanced reporting and export functionality.
     * Reuses AdvancedReportingViewModel from GUI2 (Pattern 2C).
     */
    @Serializable
    data class AdvancedReporting(val businessId: Long) : ScreenV3()

    /**
     * PARITY FIX (Ultrathink Diagnosis - Missing in GUI3):
     * Business-level insights and performance metrics.
     * Reuses BusinessInsightsViewModel from GUI2 (Pattern 2C).
     */
    @Serializable
    data class BusinessInsights(val businessId: Long) : ScreenV3()

    /**
     * PARITY FIX (Ultrathink Diagnosis - Missing in GUI3):
     * Predictive analytics and forecasting.
     * Reuses PredictionsViewModel from GUI2 (Pattern 2C).
     */
    @Serializable
    data class Predictions(val businessId: Long) : ScreenV3()

    /**
     * PARITY FIX (Ultrathink Diagnosis - Missing in GUI3):
     * Notes and memo management for invoices.
     * Reuses NotesViewModel from GUI2 (Pattern 2C).
     */
    @Serializable
    data class Notes(val businessId: Long) : ScreenV3()

    /**
     * Paywall/Subscription screen (premium upgrade).
     * Shows pricing and allows user to purchase premium tier.
     */
    @Serializable
    data class Paywall(val businessId: Long) : ScreenV3()

    /**
     * Matrix Debug Panel — Live effect tuning and profiling UI.
     * Only visible in:
     * - Debug builds (always)
     * - Production Beta builds (if MATRIX_DEBUG_PANEL enabled)
     */
    @Serializable
    data class MatrixDebugPanel(val businessId: Long) : ScreenV3()

    /**
     * PDF Viewer Screen (Phase 4.1)
     * Displays and manages PDF documents with download/share functionality.
     */
    @Serializable
    data class ViewPdf(val businessId: Long, val invoiceId: Long, val pdfPath: String) : ScreenV3()

    /**
     * Vault Screen (Phase 4.2)
     * Secure storage for documents, backups, and credentials.
     */
    @Serializable
    data class Vault(val businessId: Long) : ScreenV3()

    /**
     * Security Vault Screen — info-rich Matrix vault for docs, backups, and security status.
     */
    @Serializable
    data class SecurityVault(val businessId: Long) : ScreenV3()

    /**
     * App Appearance Screen — Theme, display mode, GUI mode switching, and custom colors.
     * Mirrors GUI2's AppAppearance screen with Matrix cyberpunk styling.
     */
    @Serializable
    data class AppAppearance(val businessId: Long) : ScreenV3()
}
