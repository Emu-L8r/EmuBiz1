package com.emul8r.bizap.ui.gui2.navigation

import androidx.navigation.NavHostController

/**
 * Navigation helper extensions for GUI2.
 * Every navigation action passes businessId explicitly.
 */

fun NavHostController.navigateToDashboardV2(businessId: Long) {
    navigate(ScreenV2.Dashboard(businessId)) {
        popUpTo(ScreenV2.Dashboard(businessId)) { inclusive = true }
        launchSingleTop = true
    }
}

fun NavHostController.navigateToRevenueAnalyticsV2(businessId: Long) {
    navigate(ScreenV2.RevenueAnalytics(businessId))
}

fun NavHostController.navigateToPaymentAnalyticsV2(businessId: Long) {
    navigate(ScreenV2.PaymentAnalytics(businessId))
}

fun NavHostController.navigateToRiskAnalyticsV2(businessId: Long) {
    navigate(ScreenV2.RiskAnalytics(businessId))
}

fun NavHostController.navigateToInvoiceDetailV2(businessId: Long, invoiceId: Long) {
    navigate(ScreenV2.InvoiceDetail(businessId, invoiceId))
}

// Customer navigation helpers
fun NavHostController.navigateToCustomersV2(businessId: Long) {
    navigate(ScreenV2.Customers(businessId))
}

fun NavHostController.navigateToCustomerDetailV2(businessId: Long, customerId: Long) {
    navigate(ScreenV2.CustomerDetail(businessId, customerId))
}

fun NavHostController.navigateToCreateCustomerV2(businessId: Long) {
    navigate(ScreenV2.CreateCustomer(businessId))
}

fun NavHostController.navigateToEditCustomerV2(businessId: Long, customerId: Long) {
    navigate(ScreenV2.EditCustomer(businessId, customerId))
}

// Invoice navigation helpers
fun NavHostController.navigateToInvoicesV2(businessId: Long) {
    navigate(ScreenV2.Invoices(businessId))
}

fun NavHostController.navigateToCreateInvoiceV2(businessId: Long) {
    navigate(ScreenV2.CreateInvoice(businessId))
}

fun NavHostController.navigateToEditInvoiceV2(businessId: Long, invoiceId: Long) {
    navigate(ScreenV2.EditInvoice(businessId, invoiceId))
}

// Settings navigation helpers
fun NavHostController.navigateToSettingsV2(businessId: Long) {
    navigate(ScreenV2.Settings(businessId))
}

fun NavHostController.navigateToAppAppearanceV2(businessId: Long) {
    navigate(ScreenV2.AppAppearance(businessId))
}

fun NavHostController.navigateToBusinessProfileV2(businessId: Long) {
    navigate(ScreenV2.BusinessProfile(businessId))
}

fun NavHostController.navigateToThemeSettingsV2(businessId: Long) {
    navigate(ScreenV2.ThemeSettings(businessId))
}

fun NavHostController.navigateToInvoiceAnalyticsV2(businessId: Long) {
    navigate(ScreenV2.InvoiceAnalytics(businessId))
}

fun NavHostController.navigateToVaultV2(businessId: Long) {
    navigate(ScreenV2.Vault(businessId))
}

fun NavHostController.navigateToAppSettingsV2(businessId: Long) {
    navigate(ScreenV2.AppSettings(businessId))
}

fun NavHostController.navigateToHelpV2(businessId: Long) {
    navigate(ScreenV2.Help(businessId))
}

fun NavHostController.navigateToDunningNoticesV2(businessId: Long) {
    navigate(ScreenV2.DunningNotices(businessId))
}

fun NavHostController.navigateToPrefilledItemsV2(businessId: Long) {
    navigate(ScreenV2.PrefilledItems(businessId))
}

fun NavHostController.navigateToInvoiceTemplatesV2(businessId: Long) {
    navigate(ScreenV2.InvoiceTemplates(businessId))
}

fun NavHostController.navigateToBackupRestoreV2(businessId: Long) {
    navigate(ScreenV2.BackupRestore(businessId))
}
