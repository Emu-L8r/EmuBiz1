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
