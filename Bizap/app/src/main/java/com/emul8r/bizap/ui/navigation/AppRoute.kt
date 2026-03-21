package com.emul8r.bizap.ui.navigation

import kotlinx.serialization.Serializable

/**
 * Unified navigation routes for the Bizap app.
 * 
 * These routes work with both Classic and Modern themes, replacing the need for
 * separate Screen (GUI1) and ScreenV2 (GUI2) navigation hierarchies.
 * 
 * All routes include an optional businessId parameter for consistency. When not provided,
 * the app will use the active business context from BusinessContextManager.
 */
@Serializable
sealed interface AppRoute {
    
    // Dashboard & Analytics
    @Serializable
    object Dashboard : AppRoute
    
    @Serializable
    object RevenueDashboard : AppRoute
    
    @Serializable
    object RiskDashboard : AppRoute
    
    @Serializable
    data class PaymentAnalytics(val businessId: Long? = null) : AppRoute
    
    // Invoices
    @Serializable
    data class InvoiceList(val businessId: Long? = null) : AppRoute
    
    @Serializable
    data class CreateInvoice(val businessId: Long? = null) : AppRoute
    
    @Serializable
    data class EditInvoice(val invoiceId: Long) : AppRoute
    
    @Serializable
    data class InvoiceDetail(val invoiceId: Long) : AppRoute
    
    @Serializable
    data class InvoicePdf(val invoiceId: Long, val isQuote: Boolean) : AppRoute
    
    // Customers
    @Serializable
    object CustomerList : AppRoute
    
    @Serializable
    data class CustomerDetail(val customerId: Long) : AppRoute
    
    @Serializable
    object CreateCustomer : AppRoute
    
    @Serializable
    data class EditCustomer(val customerId: Long) : AppRoute
    
    @Serializable
    object CustomerSegments : AppRoute
    
    @Serializable
    object CustomerAnalytics : AppRoute
    
    // Documents & Notes
    @Serializable
    object DocumentVault : AppRoute
    
    @Serializable
    object Notes : AppRoute
    
    // Settings
    @Serializable
    object SettingsHub : AppRoute
    
    @Serializable
    object AppSettings : AppRoute
    
    @Serializable
    object ThemeSettings : AppRoute
    
    @Serializable
    object BusinessProfile : AppRoute
    
    @Serializable
    object PrefilledItems : AppRoute
    
    @Serializable
    object BackupRestore : AppRoute
    
    // Other
    @Serializable
    object DunningNotices : AppRoute
    
    @Serializable
    object Help : AppRoute
}
