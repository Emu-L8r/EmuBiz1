package com.emul8r.bizap.ui.gui2.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation routes for GUI2.
 * Every screen receives a mandatory businessId — no implicit "active business" assumption.
 */
@Serializable
sealed interface ScreenV2 {

    @Serializable
    data class Dashboard(val businessId: Long) : ScreenV2

    @Serializable
    data class RevenueAnalytics(val businessId: Long) : ScreenV2

    @Serializable
    data class PaymentAnalytics(val businessId: Long) : ScreenV2

    @Serializable
    data class RiskAnalytics(val businessId: Long) : ScreenV2

    @Serializable
    data class InvoiceDetail(val businessId: Long, val invoiceId: Long) : ScreenV2
}
