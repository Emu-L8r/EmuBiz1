package com.emul8r.bizap.ui.gui2.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation routes for GUI2.
 * Every screen receives a mandatory businessId — no implicit "active business" assumption.
 */
@Serializable
sealed interface ScreenV2 {

    @Serializable
    data class Dashboard(val businessId: Long) : ScreenV2

    @Serializable
    data class RevenueAnalytics(val businessId: Long) : ScreenV2

    @Serializable
    data class PaymentAnalytics(val businessId: Long) : ScreenV2

    @Serializable
    data class RiskAnalytics(val businessId: Long) : ScreenV2

    @Serializable
    data class InvoiceDetail(val businessId: Long, val invoiceId: Long) : ScreenV2

    @Serializable
    data class Customers(val businessId: Long) : ScreenV2

    @Serializable
    data class CustomerDetail(val businessId: Long, val customerId: Long) : ScreenV2

    @Serializable
    data class CreateCustomer(val businessId: Long) : ScreenV2

    @Serializable
    data class EditCustomer(val businessId: Long, val customerId: Long) : ScreenV2

    @Serializable
    data class Invoices(val businessId: Long) : ScreenV2

    @Serializable
    data class CreateInvoice(val businessId: Long) : ScreenV2

    @Serializable
    data class EditInvoice(val businessId: Long, val invoiceId: Long) : ScreenV2

    @Serializable
    data class Settings(val businessId: Long) : ScreenV2

    @Serializable
    data class BusinessProfile(val businessId: Long) : ScreenV2

    /**
     * Unified App Appearance screen (Phase 4 consolidation).
     * Combines theme, display mode, and appearance settings.
     */
    @Serializable
    data class AppAppearance(val businessId: Long) : ScreenV2

    @Serializable
    data class InvoiceAnalytics(val businessId: Long) : ScreenV2

    @Serializable
    data class AnalyticsFocusedInsights(val businessId: Long) : ScreenV2

    @Serializable
    data class Vault(val businessId: Long) : ScreenV2

    /**
     * Help / About screen (shared HelpScreen composable).
     * businessId is carried for navigation-graph consistency only.
     */
    @Serializable
    data class Help(val businessId: Long) : ScreenV2

    @Serializable
    data class DunningNotices(val businessId: Long) : ScreenV2

    @Serializable
    data class PrefilledItems(val businessId: Long) : ScreenV2

    @Serializable
    data class BackupRestore(val businessId: Long) : ScreenV2

    @Serializable
    data class ThemeSettings(val businessId: Long) : ScreenV2

    @Serializable
    data class InvoiceCustomization(val businessId: Long) : ScreenV2

    @Serializable
    data class AdvancedReporting(val businessId: Long) : ScreenV2

    @Serializable
    data class BusinessInsights(val businessId: Long) : ScreenV2
}
