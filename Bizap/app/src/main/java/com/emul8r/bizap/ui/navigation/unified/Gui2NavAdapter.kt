package com.emul8r.bizap.ui.navigation.unified

import com.emul8r.bizap.ui.gui2.navigation.ScreenV2

/**
 * Translates unified [AppScreen] destinations to GUI2 [ScreenV2] route objects.
 *
 * GUI2 requires an explicit businessId on every route. When [AppScreen] carries a
 * non-null businessId it is used directly; otherwise [fallbackBusinessId] is applied.
 *
 * Screens that are GUI1-only return `null` — the caller should not navigate
 * to those destinations when GUI2 is active.
 *
 * Usage:
 * ```kotlin
 * val route = Gui2NavAdapter.toScreen(AppScreen.Dashboard(businessId), fallbackBusinessId = 1L)
 * if (route != null) navController.navigate(route)
 * ```
 */
object Gui2NavAdapter {

    /**
     * Converts an [AppScreen] to the corresponding GUI2 [ScreenV2] route.
     *
     * @param appScreen           The unified screen destination.
     * @param fallbackBusinessId  Business ID to use when [AppScreen] does not carry one.
     * @return the GUI2 [ScreenV2] route, or `null` if the destination is GUI1-only.
     */
    fun toScreen(appScreen: AppScreen, fallbackBusinessId: Long): ScreenV2? {
        fun biz(id: Long?) = id ?: fallbackBusinessId
        return when (appScreen) {
            // Core
            is AppScreen.Dashboard -> ScreenV2.Dashboard(biz(appScreen.businessId))

            // Customers
            is AppScreen.CustomerList -> ScreenV2.Customers(biz(appScreen.businessId))
            is AppScreen.CustomerDetail ->
                ScreenV2.CustomerDetail(biz(appScreen.businessId), appScreen.customerId)
            is AppScreen.CreateCustomer -> ScreenV2.CreateCustomer(biz(appScreen.businessId))
            is AppScreen.EditCustomer ->
                ScreenV2.EditCustomer(biz(appScreen.businessId), appScreen.customerId)

            // Invoices
            is AppScreen.InvoiceList -> ScreenV2.Invoices(biz(appScreen.businessId))
            is AppScreen.InvoiceDetail ->
                ScreenV2.InvoiceDetail(biz(appScreen.businessId), appScreen.invoiceId)
            is AppScreen.CreateInvoice -> ScreenV2.CreateInvoice(biz(appScreen.businessId))
            is AppScreen.EditInvoice ->
                ScreenV2.EditInvoice(biz(appScreen.businessId), appScreen.invoiceId)
            is AppScreen.InvoicePdf -> null // GUI1-only

            // Settings
            is AppScreen.SettingsHub -> ScreenV2.Settings(biz(appScreen.businessId))
            is AppScreen.AppSettings -> ScreenV2.AppSettings(biz(appScreen.businessId))
            is AppScreen.BusinessProfile -> ScreenV2.BusinessProfile(biz(appScreen.businessId))
            is AppScreen.ThemeSettings -> ScreenV2.ThemeSettings(biz(appScreen.businessId))
            AppScreen.PrefilledItems -> null  // GUI1-only
            AppScreen.BackupRestore -> null   // GUI1-only

            // Analytics
            is AppScreen.RevenueAnalytics -> ScreenV2.RevenueAnalytics(biz(appScreen.businessId))
            is AppScreen.PaymentAnalytics -> ScreenV2.PaymentAnalytics(biz(appScreen.businessId))
            is AppScreen.RiskAnalytics -> ScreenV2.RiskAnalytics(biz(appScreen.businessId))
            is AppScreen.InvoiceAnalytics -> ScreenV2.InvoiceAnalytics(biz(appScreen.businessId))

            // Documents
            is AppScreen.DocumentVault -> ScreenV2.Vault(biz(appScreen.businessId))

            // Misc — GUI2 maps Help to the shared HelpScreen; GUI1-only screens return null
            AppScreen.Help -> ScreenV2.Help(fallbackBusinessId)
            AppScreen.DunningNotices -> null        // GUI1-only
            is AppScreen.InvoiceTemplates -> null   // GUI1-only
            is AppScreen.CreateTemplate -> null     // GUI1-only
            is AppScreen.EditTemplate -> null       // GUI1-only
            AppScreen.CustomerSegments -> null      // GUI1-only
            AppScreen.CustomerAnalytics -> null     // GUI1-only
            AppScreen.Notes -> null                 // GUI1-only
        }
    }

    /**
     * Converts a GUI2 [ScreenV2] route to the equivalent [AppScreen].
     *
     * Useful for recording the current navigation position in a GUI-agnostic way.
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
        is ScreenV2.AppSettings -> AppScreen.AppSettings(screen.businessId)
        is ScreenV2.BusinessProfile -> AppScreen.BusinessProfile(screen.businessId)
        is ScreenV2.ThemeSettings -> AppScreen.ThemeSettings(screen.businessId)
        is ScreenV2.RevenueAnalytics -> AppScreen.RevenueAnalytics(screen.businessId)
        is ScreenV2.PaymentAnalytics -> AppScreen.PaymentAnalytics(screen.businessId)
        is ScreenV2.RiskAnalytics -> AppScreen.RiskAnalytics(screen.businessId)
        is ScreenV2.InvoiceAnalytics -> AppScreen.InvoiceAnalytics(screen.businessId)
        is ScreenV2.Vault -> AppScreen.DocumentVault(screen.businessId)
        is ScreenV2.Help -> AppScreen.Help
    }
}
