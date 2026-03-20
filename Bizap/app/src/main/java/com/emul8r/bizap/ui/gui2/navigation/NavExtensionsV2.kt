package com.emul8r.bizap.ui.gui2.navigation

import androidx.navigation.NavHostController

fun NavHostController.navigateToDashboardV2(businessId: Long) {
    navigate(ScreenV2.Dashboard(businessId))
}

fun NavHostController.navigateToCustomerDetailV2(businessId: Long, customerId: Long) {
    navigate(ScreenV2.CustomerDetail(businessId, customerId))
}

fun NavHostController.navigateToInvoiceDetailV2(businessId: Long, invoiceId: Long) {
    navigate(ScreenV2.InvoiceDetail(businessId, invoiceId))
}

fun NavHostController.navigateToSettingsV2(businessId: Long) {
    navigate(ScreenV2.Settings(businessId))
}

fun NavHostController.navigateToBusinessProfileV2(businessId: Long) {
    navigate(ScreenV2.BusinessProfile(businessId))
}

fun NavHostController.navigateToHelpV2(businessId: Long) {
    navigate(ScreenV2.Help(businessId))
}

fun NavHostController.navigateToRiskAnalyticsV2(businessId: Long) {
    navigate(ScreenV2.RiskAnalytics(businessId))
}

fun NavHostController.navigateToPaymentAnalyticsV2(businessId: Long) {
    navigate(ScreenV2.PaymentAnalytics(businessId))
}

fun NavHostController.navigateToRevenueAnalyticsV2(businessId: Long) {
    navigate(ScreenV2.RevenueAnalytics(businessId))
}

fun NavHostController.navigateToDunningNoticesV2(businessId: Long) {
    navigate(ScreenV2.DunningNotices(businessId))
}

fun NavHostController.navigateToPrefilledItemsV2(businessId: Long) {
    navigate(ScreenV2.PrefilledItems(businessId))
}

fun NavHostController.navigateToBackupRestoreV2(businessId: Long) {
    navigate(ScreenV2.BackupRestore(businessId))
}

fun NavHostController.navigateToVaultV2(businessId: Long) {
    navigate(ScreenV2.Vault(businessId))
}