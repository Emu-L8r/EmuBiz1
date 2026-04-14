package com.emul8r.bizap.ui.gui3.navigation

import kotlinx.serialization.Serializable

/**
 * GUI3 Screen Routes
 *
 * Type-safe navigation routes for Matrix UI screens.
 * Follows Material 3 navigation patterns.
 */
sealed class ScreenV3 {
    @Serializable
    data class Dashboard(val businessId: Long = 1L) : ScreenV3()

    @Serializable
    data class Invoices(val businessId: Long = 1L) : ScreenV3()

    @Serializable
    data class InvoiceDetail(val businessId: Long = 1L, val invoiceId: Long) : ScreenV3()

    @Serializable
    data class CreateInvoice(val businessId: Long = 1L) : ScreenV3()

    @Serializable
    data class Customers(val businessId: Long = 1L) : ScreenV3()

    @Serializable
    data class CustomerDetail(val businessId: Long = 1L, val customerId: Long) : ScreenV3()

    @Serializable
    data class CreateCustomer(val businessId: Long = 1L) : ScreenV3()

    @Serializable
    data class PaymentTracking(val businessId: Long = 1L) : ScreenV3()

    @Serializable
    data class Settings(val businessId: Long = 1L) : ScreenV3()

    @Serializable
    data class Reports(val businessId: Long = 1L) : ScreenV3()

    @Serializable
    data class Help(val businessId: Long = 1L) : ScreenV3()

    @Serializable
    data class PaymentAnalytics(val businessId: Long = 1L) : ScreenV3()

    @Serializable
    data class RevenueAnalytics(val businessId: Long = 1L) : ScreenV3()

    /**
     * Paywall/Subscription screen (premium upgrade).
     * Shows pricing and allows user to purchase premium tier.
     */
    @Serializable
    data class Paywall(val businessId: Long = 1L) : ScreenV3()
}
