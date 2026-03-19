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

fun NavHostController.navigateToInvoiceAnalyticsV2(businessId: Long) {
    navigate(ScreenV2.InvoiceAnalytics(businessId))
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

fun NavHostController.navigateToRevenueAnalyticsV2(businessId: Long) {
    navigate(ScreenV2.RevenueAnalytics(businessId))
}

fun NavHostController.navigateToPaymentAnalyticsV2(businessId: Long) {
    navigate(ScreenV2.PaymentAnalytics(businessId))
}

