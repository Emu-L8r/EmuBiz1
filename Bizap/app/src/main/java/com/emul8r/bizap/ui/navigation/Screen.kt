package com.emul8r.bizap.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Screen {
    @Serializable
    object Dashboard : Screen

    @Serializable
    object Customers : Screen

    @Serializable
    data class CustomerDetail(val customerId: Long) : Screen


    @Serializable
    object Invoices : Screen

    @Serializable
    object CreateInvoice : Screen

    @Serializable
    data class EditInvoice(val invoiceId: Long) : Screen

    @Serializable
    data class InvoiceDetail(val invoiceId: Long) : Screen

    @Serializable
    data class InvoicePdf(val invoiceId: Long, val isQuote: Boolean) : Screen

    @Serializable
    object DocumentVault : Screen

    @Serializable
    object RevenueDashboard : Screen


    // Settings Routes
    @Serializable
    object SettingsHub : Screen

    @Serializable
    object BusinessProfile : Screen

    @Serializable
    object ThemeSettings : Screen

    @Serializable
    object PrefilledItems : Screen

    @Serializable
    object RiskDashboard : Screen

    @Serializable
    object PaymentAnalytics : Screen

    @Serializable
    object DunningNotices : Screen

    @Serializable
    data class InvoiceTemplates(val businessProfileId: Long = 1L) : Screen

    @Serializable
    data class CreateTemplate(val businessProfileId: Long) : Screen

    @Serializable
    data class EditTemplate(val templateId: String) : Screen

    @Serializable
    object BackupRestore : Screen
}
