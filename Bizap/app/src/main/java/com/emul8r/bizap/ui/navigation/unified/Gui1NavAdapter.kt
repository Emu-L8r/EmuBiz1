package com.emul8r.bizap.ui.navigation.unified

import com.emul8r.bizap.ui.navigation.Screen

/**
 * Translates unified [AppScreen] destinations to GUI1 [Screen] route objects.
 *
 * GUI1 uses an implicit business context (no businessId in routes) so
 * businessId is ignored for route translation but preserved in [AppScreen]
 * for cross-GUI consistency.
 *
 * Screens that are GUI2-only return `null` — the caller should not navigate
 * to those destinations when GUI1 is active.
 *
 * Usage:
 * ```kotlin
 * val screen = Gui1NavAdapter.toScreen(AppScreen.Dashboard())
 * if (screen != null) navController.navigate(screen)
 * ```
 */
object Gui1NavAdapter {

    /**
     * Converts an [AppScreen] to the corresponding GUI1 [Screen] route.
     *
     * @return the GUI1 [Screen] route, or `null` if the destination is GUI2-only.
     */
    fun toScreen(appScreen: AppScreen): Screen? = when (appScreen) {
        // Core
        is AppScreen.Dashboard -> Screen.Dashboard

        // Customers
        is AppScreen.CustomerList -> Screen.Customers
        is AppScreen.CustomerDetail -> Screen.CustomerDetail(appScreen.customerId)
        is AppScreen.CreateCustomer -> null // GUI1 uses a bottom sheet from CustomerList
        is AppScreen.EditCustomer -> null   // GUI1 does not have a standalone edit page

        // Invoices
        is AppScreen.InvoiceList -> Screen.Invoices
        is AppScreen.InvoiceDetail -> Screen.InvoiceDetail(appScreen.invoiceId)
        is AppScreen.CreateInvoice -> Screen.CreateInvoice
        is AppScreen.EditInvoice -> Screen.EditInvoice(appScreen.invoiceId)
        is AppScreen.InvoicePdf -> Screen.InvoicePdf(appScreen.invoiceId, appScreen.isQuote)

        // Settings
        is AppScreen.SettingsHub -> Screen.SettingsHub
        is AppScreen.AppSettings -> Screen.AppSettings
        is AppScreen.BusinessProfile -> Screen.BusinessProfile
        is AppScreen.ThemeSettings -> Screen.ThemeSettings
        AppScreen.PrefilledItems -> Screen.PrefilledItems
        AppScreen.BackupRestore -> Screen.BackupRestore

        // Analytics
        is AppScreen.RevenueAnalytics -> Screen.RevenueDashboard
        is AppScreen.PaymentAnalytics -> Screen.PaymentAnalytics(appScreen.businessId)
        is AppScreen.RiskAnalytics -> Screen.RiskDashboard
        is AppScreen.InvoiceAnalytics -> null // GUI2-only

        // Documents
        is AppScreen.DocumentVault -> Screen.DocumentVault

        // Misc
        AppScreen.Help -> Screen.Help
        AppScreen.DunningNotices -> Screen.DunningNotices
        is AppScreen.InvoiceTemplates -> Screen.InvoiceTemplates(appScreen.businessProfileId)
        is AppScreen.CreateTemplate -> Screen.CreateTemplate(appScreen.businessProfileId)
        is AppScreen.EditTemplate -> Screen.EditTemplate(appScreen.templateId)
        AppScreen.CustomerSegments -> Screen.CustomerSegments
        AppScreen.CustomerAnalytics -> Screen.CustomerAnalytics
        AppScreen.Notes -> Screen.Notes
    }

    /**
     * Converts a GUI1 [Screen] route to the equivalent [AppScreen].
     *
     * Useful for recording the current navigation position in a GUI-agnostic way.
     */
    fun fromScreen(screen: Screen): AppScreen = when (screen) {
        Screen.Dashboard -> AppScreen.Dashboard()
        Screen.Customers -> AppScreen.CustomerList()
        is Screen.CustomerDetail -> AppScreen.CustomerDetail(screen.customerId)
        Screen.Invoices -> AppScreen.InvoiceList()
        is Screen.InvoiceDetail -> AppScreen.InvoiceDetail(screen.invoiceId)
        Screen.CreateInvoice -> AppScreen.CreateInvoice()
        is Screen.EditInvoice -> AppScreen.EditInvoice(screen.invoiceId)
        is Screen.InvoicePdf -> AppScreen.InvoicePdf(screen.invoiceId, screen.isQuote)
        Screen.SettingsHub -> AppScreen.SettingsHub()
        Screen.AppSettings -> AppScreen.AppSettings()
        Screen.BusinessProfile -> AppScreen.BusinessProfile()
        Screen.ThemeSettings -> AppScreen.ThemeSettings()
        Screen.PrefilledItems -> AppScreen.PrefilledItems
        Screen.BackupRestore -> AppScreen.BackupRestore
        Screen.DocumentVault -> AppScreen.DocumentVault()
        Screen.RevenueDashboard -> AppScreen.RevenueAnalytics()
        is Screen.PaymentAnalytics -> AppScreen.PaymentAnalytics(screen.businessId)
        Screen.RiskDashboard -> AppScreen.RiskAnalytics()
        Screen.DunningNotices -> AppScreen.DunningNotices
        is Screen.InvoiceTemplates -> AppScreen.InvoiceTemplates(screen.businessProfileId)
        is Screen.CreateTemplate -> AppScreen.CreateTemplate(screen.businessProfileId)
        is Screen.EditTemplate -> AppScreen.EditTemplate(screen.templateId)
        Screen.CustomerSegments -> AppScreen.CustomerSegments
        Screen.CustomerAnalytics -> AppScreen.CustomerAnalytics
        Screen.Notes -> AppScreen.Notes
        Screen.Help -> AppScreen.Help
    }
}
