package com.emul8r.bizap.ui.navigation.unified

import kotlinx.serialization.Serializable

/**
 * Unified navigation model for Bizap.
 *
 * AppScreen is a single sealed interface covering all screens available across
 * GUI1 (classic) and GUI2 (modern). Screens that are GUI-specific are annotated
 * in their KDoc. Navigation adapters ([Gui1NavAdapter] / [Gui2NavAdapter]) translate
 * AppScreen destinations to the respective GUI's route types at runtime.
 *
 * ## Consolidation Map
 * | AppScreen                  | GUI1 Screen              | GUI2 ScreenV2              |
 * |----------------------------|--------------------------|----------------------------|
 * | Dashboard                  | Screen.Dashboard         | ScreenV2.Dashboard         |
 * | CustomerList               | Screen.Customers         | ScreenV2.Customers         |
 * | CustomerDetail             | Screen.CustomerDetail    | ScreenV2.CustomerDetail    |
 * | CreateCustomer             | (bottom-sheet)           | ScreenV2.CreateCustomer    |
 * | InvoiceList                | Screen.Invoices          | ScreenV2.Invoices          |
 * | InvoiceDetail              | Screen.InvoiceDetail     | ScreenV2.InvoiceDetail     |
 * | CreateInvoice              | Screen.CreateInvoice     | ScreenV2.CreateInvoice     |
 * | SettingsHub                | Screen.SettingsHub       | ScreenV2.Settings          |
 * | AppSettings                | Screen.AppSettings       | ScreenV2.AppSettings       |
 * | BusinessProfile            | Screen.BusinessProfile   | ScreenV2.BusinessProfile   |
 * | Help                       | (new — shared)           | (new — shared)             |
 * | DocumentVault              | Screen.DocumentVault     | ScreenV2.Vault             |
 * | RevenueAnalytics (GUI1)    | Screen.RevenueDashboard  | —                          |
 * | RevenueAnalytics (GUI2)    | —                        | ScreenV2.RevenueAnalytics  |
 * | PaymentAnalytics           | Screen.PaymentAnalytics  | ScreenV2.PaymentAnalytics  |
 * | RiskDashboard              | Screen.RiskDashboard     | ScreenV2.RiskAnalytics     |
 * | InvoiceAnalytics (GUI2)    | —                        | ScreenV2.InvoiceAnalytics  |
 */
@Serializable
sealed interface AppScreen {

    // ── Core screens ────────────────────────────────────────────────────────────

    /** Main dashboard screen (GUI1 + GUI2). */
    @Serializable
    data class Dashboard(val businessId: Long? = null) : AppScreen

    // ── Customer screens ─────────────────────────────────────────────────────────

    /** Customer list screen (GUI1 + GUI2). */
    @Serializable
    data class CustomerList(val businessId: Long? = null) : AppScreen

    /** Customer detail screen (GUI1 + GUI2). */
    @Serializable
    data class CustomerDetail(
        val customerId: Long,
        val businessId: Long? = null
    ) : AppScreen

    /** Create customer screen (GUI2; GUI1 uses a bottom sheet from CustomerList). */
    @Serializable
    data class CreateCustomer(val businessId: Long? = null) : AppScreen

    /** Edit customer screen (GUI1 + GUI2). */
    @Serializable
    data class EditCustomer(
        val customerId: Long,
        val businessId: Long? = null
    ) : AppScreen

    // ── Invoice screens ───────────────────────────────────────────────────────────

    /** Invoice list screen (GUI1 + GUI2). */
    @Serializable
    data class InvoiceList(val businessId: Long? = null) : AppScreen

    /** Invoice detail screen (GUI1 + GUI2). */
    @Serializable
    data class InvoiceDetail(
        val invoiceId: Long,
        val businessId: Long? = null
    ) : AppScreen

    /** Create invoice screen (GUI1 + GUI2). */
    @Serializable
    data class CreateInvoice(val businessId: Long? = null) : AppScreen

    /** Edit invoice screen (GUI1 + GUI2). */
    @Serializable
    data class EditInvoice(
        val invoiceId: Long,
        val businessId: Long? = null
    ) : AppScreen

    /** Invoice PDF preview screen (GUI1 only). */
    @Serializable
    data class InvoicePdf(val invoiceId: Long, val isQuote: Boolean) : AppScreen

    // ── Settings screens ──────────────────────────────────────────────────────────

    /** Settings hub / entry point screen (GUI1 + GUI2). */
    @Serializable
    data class SettingsHub(val businessId: Long? = null) : AppScreen

    /**
     * App-level settings screen (GUI1 + GUI2 – shared composable).
     *
     * This is the unified `presentation/ui/screens/SettingsScreen.kt` composable.
     * Both GUIs route to the same composable; businessId is carried only for
     * navigation-graph consistency.
     */
    @Serializable
    data class AppSettings(val businessId: Long? = null) : AppScreen

    /**
     * Business profile editor (GUI1 + GUI2).
     *
     * Both GUIs share the same underlying ViewModel ([BusinessProfileViewModel]).
     * The composable is GUI-specific but the data layer is unified.
     */
    @Serializable
    data class BusinessProfile(val businessId: Long? = null) : AppScreen

    /** Theme / appearance settings (GUI1 + GUI2). */
    @Serializable
    data class ThemeSettings(val businessId: Long? = null) : AppScreen

    /** Pre-filled invoice items (GUI1 only). */
    @Serializable
    object PrefilledItems : AppScreen

    /** Backup & Restore (GUI1 only). */
    @Serializable
    object BackupRestore : AppScreen

    // ── Analytics screens ─────────────────────────────────────────────────────────

    /**
     * Revenue / snapshot dashboard (GUI1: RevenueDashboard, GUI2: RevenueAnalytics).
     */
    @Serializable
    data class RevenueAnalytics(val businessId: Long? = null) : AppScreen

    /** Payment analytics (GUI1 + GUI2). */
    @Serializable
    data class PaymentAnalytics(val businessId: Long? = null) : AppScreen

    /**
     * Risk / cash-flow analytics (GUI1: RiskDashboard, GUI2: RiskAnalytics).
     */
    @Serializable
    data class RiskAnalytics(val businessId: Long? = null) : AppScreen

    /** Invoice-level analytics (GUI2 only). */
    @Serializable
    data class InvoiceAnalytics(val businessId: Long? = null) : AppScreen

    // ── Document & Vault ──────────────────────────────────────────────────────────

    /** Document vault (GUI1 + GUI2 – shared composable). */
    @Serializable
    data class DocumentVault(val businessId: Long? = null) : AppScreen

    // ── Misc screens ───────────────────────────────────────────────────────────────

    /** Help / About screen (GUI1 + GUI2 – shared composable). */
    @Serializable
    object Help : AppScreen

    /** Dunning notices (GUI1 only). */
    @Serializable
    object DunningNotices : AppScreen

    /** Invoice template list (GUI1 only). */
    @Serializable
    data class InvoiceTemplates(val businessProfileId: Long = 1L) : AppScreen

    /** Create invoice template (GUI1 only). */
    @Serializable
    data class CreateTemplate(val businessProfileId: Long) : AppScreen

    /** Edit invoice template (GUI1 only). */
    @Serializable
    data class EditTemplate(val templateId: String) : AppScreen

    /** Customer segments view (GUI1 only). */
    @Serializable
    object CustomerSegments : AppScreen

    /** Customer analytics view (GUI1 only). */
    @Serializable
    object CustomerAnalytics : AppScreen

    /** Notes (GUI1 only). */
    @Serializable
    object Notes : AppScreen
}
