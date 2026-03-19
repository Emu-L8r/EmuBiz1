package com.emul8r.bizap.ui.navigation.unified

import com.emul8r.bizap.ui.gui2.navigation.ScreenV2

/**
 * Adapter for translating between GUI2 [ScreenV2] and unified [AppScreen] routes.
 *
 * Provides bidirectional mapping so that GUI2 navigation can be recorded in
 * a GUI-agnostic way via [AppScreen].
 */
object Gui2NavAdapter {

    /**
     * Converts an [AppScreen] route to the equivalent GUI2 [ScreenV2].
     *
     * Used when navigating from unified code to GUI2-specific destinations.
     */
    fun toScreen(appScreen: AppScreen): ScreenV2? = when (appScreen) {
        is AppScreen.Dashboard -> ScreenV2.Dashboard(appScreen.businessId ?: 0L)
        is AppScreen.CustomerList -> ScreenV2.Customers(appScreen.businessId ?: 0L)
        is AppScreen.CustomerDetail ->
            ScreenV2.CustomerDetail(appScreen.businessId ?: 0L, appScreen.customerId)
        is AppScreen.CreateCustomer -> ScreenV2.CreateCustomer(appScreen.businessId ?: 0L)
        is AppScreen.EditCustomer ->
            ScreenV2.EditCustomer(appScreen.businessId ?: 0L, appScreen.customerId)
        is AppScreen.InvoiceList -> ScreenV2.Invoices(appScreen.businessId ?: 0L)
        is AppScreen.InvoiceDetail ->
            ScreenV2.InvoiceDetail(appScreen.businessId ?: 0L, appScreen.invoiceId)
        is AppScreen.CreateInvoice -> ScreenV2.CreateInvoice(appScreen.businessId ?: 0L)
        is AppScreen.EditInvoice ->
            ScreenV2.EditInvoice(appScreen.businessId ?: 0L, appScreen.invoiceId)
        is AppScreen.SettingsHub -> ScreenV2.Settings(appScreen.businessId ?: 0L)
        is AppScreen.AppSettings -> ScreenV2.AppAppearance(appScreen.businessId ?: 0L)
        is AppScreen.BusinessProfile -> ScreenV2.BusinessProfile(appScreen.businessId ?: 0L)
        is AppScreen.ThemeSettings -> ScreenV2.AppAppearance(appScreen.businessId ?: 0L)
        is AppScreen.RevenueAnalytics -> ScreenV2.RevenueAnalytics(appScreen.businessId ?: 0L)
        is AppScreen.PaymentAnalytics -> ScreenV2.PaymentAnalytics(appScreen.businessId ?: 0L)
        is AppScreen.RiskAnalytics -> ScreenV2.RiskAnalytics(appScreen.businessId ?: 0L)
        is AppScreen.InvoiceAnalytics -> ScreenV2.InvoiceAnalytics(appScreen.businessId ?: 0L)
        is AppScreen.DocumentVault -> ScreenV2.Vault(appScreen.businessId ?: 0L)
        is AppScreen.Help -> ScreenV2.Help(0L) // Help doesn't require businessId in ScreenV2
        is AppScreen.DunningNotices -> ScreenV2.DunningNotices(0L)
        is AppScreen.PrefilledItems -> ScreenV2.PrefilledItems(0L)
        is AppScreen.InvoiceTemplates -> ScreenV2.InvoiceTemplates(0L, appScreen.businessProfileId)
        is AppScreen.CreateTemplate -> ScreenV2.CreateTemplate(0L, appScreen.businessProfileId)
        is AppScreen.EditTemplate -> ScreenV2.EditTemplate(0L, appScreen.templateId)
        is AppScreen.BackupRestore -> ScreenV2.BackupRestore(0L)
        // GUI1-only screens return null
        is AppScreen.InvoicePdf -> null
        is AppScreen.CustomerSegments -> null
        is AppScreen.CustomerAnalytics -> null
        is AppScreen.Notes -> null
    }

    /**
     * Converts a GUI2 [ScreenV2] route to the equivalent [AppScreen].
     *
     * Useful for recording the current navigation position in a GUI-agnostic way.
     * 
     * Phase 4 Update: AppAppearance route maps to AppSettings for unified AppScreen compatibility.
     */
    fun fromScreen(screen: ScreenV2): AppScreen = when (screen) {
        is ScreenV2.Dashboard -> AppScreen.Dashboard(screen.businessId)
        is ScreenV2.Customers -> AppScreen.CustomerList(screen.businessId)
        is ScreenV2.CustomerDetail ->
            AppScreen.CustomerDetail(screen.customerId, screen.businessId)
        is ScreenV2.CreateCustomer -> AppScreen.CreateCustomer(screen.businessId)
        is ScreenV2.EditCustomer ->
            AppScreen.EditCustomer(screen.customerId, screen.businessId)
        is ScreenV2.Invoices -> AppScreen.InvoiceList(screen.businessId)
        is ScreenV2.InvoiceDetail ->
            AppScreen.InvoiceDetail(screen.invoiceId, screen.businessId)
        is ScreenV2.CreateInvoice -> AppScreen.CreateInvoice(screen.businessId)
        is ScreenV2.EditInvoice ->
            AppScreen.EditInvoice(screen.invoiceId, screen.businessId)
        is ScreenV2.Settings -> AppScreen.SettingsHub(screen.businessId)
        is ScreenV2.AppAppearance -> AppScreen.AppSettings(screen.businessId)
        is ScreenV2.AppSettings -> AppScreen.AppSettings(screen.businessId)
        is ScreenV2.BusinessProfile -> AppScreen.BusinessProfile(screen.businessId)
        is ScreenV2.ThemeSettings -> AppScreen.ThemeSettings(screen.businessId)
        is ScreenV2.RevenueAnalytics -> AppScreen.RevenueAnalytics(screen.businessId)
        is ScreenV2.PaymentAnalytics -> AppScreen.PaymentAnalytics(screen.businessId)
        is ScreenV2.RiskAnalytics -> AppScreen.RiskAnalytics(screen.businessId)
        is ScreenV2.InvoiceAnalytics -> AppScreen.InvoiceAnalytics(screen.businessId)
        is ScreenV2.Vault -> AppScreen.DocumentVault(screen.businessId)
        is ScreenV2.Help -> AppScreen.Help
        is ScreenV2.DunningNotices -> AppScreen.DunningNotices
        is ScreenV2.PrefilledItems -> AppScreen.PrefilledItems
        is ScreenV2.InvoiceTemplates -> AppScreen.InvoiceTemplates(screen.businessProfileId)
        is ScreenV2.CreateTemplate -> AppScreen.CreateTemplate(screen.businessProfileId)
        is ScreenV2.EditTemplate -> AppScreen.EditTemplate(screen.templateId)
        is ScreenV2.BackupRestore -> AppScreen.BackupRestore
    }
}
