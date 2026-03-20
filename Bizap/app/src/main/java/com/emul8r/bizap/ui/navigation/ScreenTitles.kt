package com.emul8r.bizap.ui.navigation

import androidx.annotation.StringRes
import com.emul8r.bizap.R

/**
 * Extension function to get the title string resource ID for a Screen.
 * This centralizes navigation title management and enables easy localization.
 */
@StringRes
fun Screen.getTitleResId(): Int = when (this) {
    is Screen.Dashboard -> R.string.screen_title_dashboard
    is Screen.Customers -> R.string.screen_title_customers
    is Screen.CustomerDetail -> R.string.screen_title_customer_detail
    is Screen.Invoices -> R.string.screen_title_invoices
    is Screen.CreateInvoice -> R.string.screen_title_create_invoice
    is Screen.EditInvoice -> R.string.screen_title_edit_invoice
    is Screen.InvoiceDetail -> R.string.screen_title_invoice_detail
    is Screen.InvoicePdf -> R.string.screen_title_invoice_pdf
    is Screen.DocumentVault -> R.string.screen_title_document_vault
    is Screen.RevenueDashboard -> R.string.screen_title_revenue_dashboard
    is Screen.SettingsHub -> R.string.screen_title_settings
    is Screen.AppSettings -> R.string.screen_title_app_appearance
    is Screen.BusinessProfile -> R.string.screen_title_business_profile
    is Screen.ThemeSettings -> R.string.screen_title_app_appearance
    is Screen.PrefilledItems -> R.string.screen_title_prefilled_items
    is Screen.RiskDashboard -> R.string.screen_title_risk_dashboard
    is Screen.PaymentAnalytics -> R.string.screen_title_payment_analytics
    is Screen.DunningNotices -> R.string.screen_title_dunning_notices
    is Screen.BackupRestore -> R.string.screen_title_backup_restore
    is Screen.CustomerSegments -> R.string.screen_title_customer_segments
    is Screen.CustomerAnalytics -> R.string.screen_title_customer_analytics
    is Screen.Notes -> R.string.screen_title_notes
    is Screen.Help -> R.string.screen_title_help
    // Default fallback for any future Screen types not explicitly handled
    else -> R.string.screen_title_default
}
