package com.emul8r.bizap.ui.shared

import com.emul8r.bizap.ui.landing.GuiMode

/**
 * Unified screen routing pattern.
 * Consolidates GUI1 and GUI2 into single implementations.
 *
 * Usage:
 * ```kotlin
 * val router = ScreenRouter.InvoiceDetail(invoiceId = 42L, guiMode = GuiMode.GUI2)
 * navController.navigate(router.toRoute())
 * ```
 */
sealed class ScreenRouter {
    abstract val guiMode: GuiMode

    data class InvoiceDetail(
        val invoiceId: Long,
        override val guiMode: GuiMode = GuiMode.GUI2
    ) : ScreenRouter()

    data class CreateInvoice(
        val businessId: Long,
        override val guiMode: GuiMode = GuiMode.GUI2
    ) : ScreenRouter()

    data class CustomerList(
        val businessId: Long,
        override val guiMode: GuiMode = GuiMode.GUI2
    ) : ScreenRouter()

    data class CustomerDetail(
        val customerId: Long,
        override val guiMode: GuiMode = GuiMode.GUI2
    ) : ScreenRouter()

    data class Dashboard(
        val businessId: Long,
        override val guiMode: GuiMode = GuiMode.GUI2
    ) : ScreenRouter()

    data class Settings(
        override val guiMode: GuiMode = GuiMode.GUI2
    ) : ScreenRouter()
}
