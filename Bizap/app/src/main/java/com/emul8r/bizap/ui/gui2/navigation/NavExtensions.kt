package com.emul8r.bizap.ui.gui2.navigation

import androidx.navigation.NavController
import com.emul8r.bizap.ui.gui2.navigation.ScreenV2

/**
 * Navigation extension functions for GUI2 routes.
 *
 * Provides type-safe navigation shortcuts to avoid boilerplate.
 */

fun NavController.navigateToAnalyticsFocusedInsights(businessId: Long) {
    this.navigate(ScreenV2.AnalyticsFocusedInsights(businessId))
}

fun NavController.navigateToInvoiceAnalytics(businessId: Long) {
    this.navigate(ScreenV2.InvoiceAnalytics(businessId))
}

fun NavController.navigateToPaymentAnalytics(businessId: Long) {
    this.navigate(ScreenV2.PaymentAnalytics(businessId))
}

fun NavController.navigateToRevenueAnalytics(businessId: Long) {
    this.navigate(ScreenV2.RevenueAnalytics(businessId))
}

fun NavController.navigateToRiskAnalytics(businessId: Long) {
    this.navigate(ScreenV2.RiskAnalytics(businessId))
}

fun NavController.navigateToBusinessProfileV2(businessId: Long) {
    this.navigate(ScreenV2.BusinessProfile(businessId))
}

fun NavController.navigateToDunningNoticesV2(businessId: Long) {
    this.navigate(ScreenV2.DunningNotices(businessId))
}

fun NavController.navigateToPrefilledItemsV2(businessId: Long) {
    this.navigate(ScreenV2.PrefilledItems(businessId))
}

fun NavController.navigateToAdvancedReporting(businessId: Long) {
    this.navigate(ScreenV2.AdvancedReporting(businessId))
}

fun NavController.navigateToBusinessInsights(businessId: Long) {
    this.navigate(ScreenV2.BusinessInsights(businessId))
}
