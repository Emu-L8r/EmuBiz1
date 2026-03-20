package com.emul8r.bizap.ui.gui2.navigation

import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
import androidx.navigation.NavHostController

/**
 * Deep link URI scheme and host constants for GUI2.
 *
 * Supported deep links:
 *   bizap://gui2/customer/{customerId}
 *   bizap://gui2/invoice/{invoiceId}
 *   bizap://dashboard
 */
object DeepLinks {
    const val SCHEME = "bizap"
    const val DASHBOARD_HOST = "dashboard"
    const val GUI2_HOST = "gui2"
    const val PATH_CUSTOMER = "customer"
    const val PATH_INVOICE = "invoice"

    /** bizap://dashboard */
    fun dashboardUri(): Uri = "$SCHEME://$DASHBOARD_HOST".toUri()

    /** bizap://gui2/customer/{customerId} */
    fun customerUri(customerId: Long): Uri =
        "$SCHEME://$GUI2_HOST/$PATH_CUSTOMER/$customerId".toUri()

    /** bizap://gui2/invoice/{invoiceId} */
    fun invoiceUri(invoiceId: Long): Uri =
        "$SCHEME://$GUI2_HOST/$PATH_INVOICE/$invoiceId".toUri()
}

/**
 * Result of parsing a deep link intent.
 */
sealed class DeepLinkDestination {
    object Dashboard : DeepLinkDestination()
    data class Customer(val customerId: Long) : DeepLinkDestination()
    data class Invoice(val invoiceId: Long) : DeepLinkDestination()
    object Unknown : DeepLinkDestination()
}

/**
 * Parses an [Intent] and returns the corresponding [DeepLinkDestination].
 * Returns [DeepLinkDestination.Unknown] if the intent does not match any known pattern.
 */
fun parseDeepLinkIntent(intent: Intent?): DeepLinkDestination {
    val uri = intent?.data ?: return DeepLinkDestination.Unknown
    if (uri.scheme != DeepLinks.SCHEME) return DeepLinkDestination.Unknown
    return when (uri.host) {
        DeepLinks.DASHBOARD_HOST -> DeepLinkDestination.Dashboard
        DeepLinks.GUI2_HOST -> {
            val segments = uri.pathSegments
            when {
                segments.size == 2 && segments[0] == DeepLinks.PATH_CUSTOMER ->
                    segments[1].toLongOrNull()
                        ?.let { DeepLinkDestination.Customer(it) }
                        ?: DeepLinkDestination.Unknown

                segments.size == 2 && segments[0] == DeepLinks.PATH_INVOICE ->
                    segments[1].toLongOrNull()
                        ?.let { DeepLinkDestination.Invoice(it) }
                        ?: DeepLinkDestination.Unknown

                else -> DeepLinkDestination.Unknown
            }
        }
        else -> DeepLinkDestination.Unknown
    }
}

/**
 * Handles a [DeepLinkDestination] by navigating the given [NavHostController] to the
 * appropriate screen within GUI2.
 *
 * @param destination   The parsed deep link destination.
 * @param businessId    The active business context (required for all GUI2 routes).
 * @param navController The GUI2 nav host controller.
 */
fun handleDeepLink(
    destination: DeepLinkDestination,
    businessId: Long,
    navController: NavHostController
) {
    when (destination) {
        is DeepLinkDestination.Dashboard ->
            navController.navigateToDashboardV2(businessId)

        is DeepLinkDestination.Customer ->
            navController.navigateToCustomerDetailV2(businessId, destination.customerId)

        is DeepLinkDestination.Invoice ->
            navController.navigateToInvoiceDetailV2(businessId, destination.invoiceId)

        is DeepLinkDestination.Unknown -> Unit
    }
}
