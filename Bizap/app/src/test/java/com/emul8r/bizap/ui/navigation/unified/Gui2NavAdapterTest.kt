package com.emul8r.bizap.ui.navigation.unified

import com.emul8r.bizap.ui.gui2.navigation.ScreenV2
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit tests for [Gui2NavAdapter].
 *
 * Verifies that every [AppScreen] destination maps to the correct [ScreenV2] route
 * (with businessId from the AppScreen) and that GUI1-only screens return `null`.
 */
class Gui2NavAdapterTest {

    // ── toScreen: core screens ─────────────────────────────────────────────────

    @Test
    fun `Dashboard uses businessId from AppScreen`() {
        assertEquals(
            ScreenV2.Dashboard(5L),
            Gui2NavAdapter.toScreen(AppScreen.Dashboard(5L))
        )
    }

    @Test
    fun `Dashboard defaults to 0L when no businessId provided`() {
        assertEquals(
            ScreenV2.Dashboard(0L),
            Gui2NavAdapter.toScreen(AppScreen.Dashboard(null))
        )
    }

    @Test
    fun `CustomerList maps to ScreenV2 Customers`() {
        assertEquals(
            ScreenV2.Customers(0L),
            Gui2NavAdapter.toScreen(AppScreen.CustomerList())
        )
    }

    @Test
    fun `CustomerDetail maps to ScreenV2 CustomerDetail with both ids`() {
        assertEquals(
            ScreenV2.CustomerDetail(businessId = 0L, customerId = 42L),
            Gui2NavAdapter.toScreen(AppScreen.CustomerDetail(customerId = 42L))
        )
    }

    @Test
    fun `CreateCustomer maps to ScreenV2 CreateCustomer`() {
        assertEquals(
            ScreenV2.CreateCustomer(0L),
            Gui2NavAdapter.toScreen(AppScreen.CreateCustomer())
        )
    }

    @Test
    fun `InvoiceList maps to ScreenV2 Invoices`() {
        assertEquals(
            ScreenV2.Invoices(0L),
            Gui2NavAdapter.toScreen(AppScreen.InvoiceList())
        )
    }

    @Test
    fun `InvoiceDetail maps to ScreenV2 InvoiceDetail with invoiceId`() {
        assertEquals(
            ScreenV2.InvoiceDetail(businessId = 0L, invoiceId = 7L),
            Gui2NavAdapter.toScreen(AppScreen.InvoiceDetail(invoiceId = 7L))
        )
    }

    @Test
    fun `CreateInvoice maps to ScreenV2 CreateInvoice`() {
        assertEquals(
            ScreenV2.CreateInvoice(0L),
            Gui2NavAdapter.toScreen(AppScreen.CreateInvoice())
        )
    }

    @Test
    fun `InvoicePdf returns null because it is GUI1 only`() {
        assertNull(Gui2NavAdapter.toScreen(AppScreen.InvoicePdf(1L, false)))
    }

    // ── toScreen: settings screens ─────────────────────────────────────────────

    @Test
    fun `SettingsHub maps to ScreenV2 Settings`() {
        assertEquals(
            ScreenV2.Settings(3L),
            Gui2NavAdapter.toScreen(AppScreen.SettingsHub(3L))
        )
    }

    @Test
    fun `AppSettings maps to ScreenV2 AppAppearance`() {
        assertEquals(
            ScreenV2.AppAppearance(4L),
            Gui2NavAdapter.toScreen(AppScreen.AppSettings(4L))
        )
    }

    @Test
    fun `ThemeSettings maps to ScreenV2 AppAppearance`() {
        assertEquals(
            ScreenV2.AppAppearance(5L),
            Gui2NavAdapter.toScreen(AppScreen.ThemeSettings(5L))
        )
    }

    @Test
    fun `BusinessProfile maps to ScreenV2 BusinessProfile`() {
        assertEquals(
            ScreenV2.BusinessProfile(6L),
            Gui2NavAdapter.toScreen(AppScreen.BusinessProfile(6L))
        )
    }

    @Test
    fun `DocumentVault maps to ScreenV2 Vault`() {
        assertEquals(
            ScreenV2.Vault(0L),
            Gui2NavAdapter.toScreen(AppScreen.DocumentVault())
        )
    }

    @Test
    fun `Help maps to ScreenV2 Help`() {
        assertEquals(
            ScreenV2.Help(0L),
            Gui2NavAdapter.toScreen(AppScreen.Help)
        )
    }

    @Test
    fun `DunningNotices returns null because it is GUI1 only`() {
        assertNull(Gui2NavAdapter.toScreen(AppScreen.DunningNotices))
    }

    @Test
    fun `Notes returns null because it is GUI1 only`() {
        assertNull(Gui2NavAdapter.toScreen(AppScreen.Notes))
    }

    // ── toScreen: analytics screens ────────────────────────────────────────────

    @Test
    fun `RevenueAnalytics maps to ScreenV2 RevenueAnalytics`() {
        assertEquals(
            ScreenV2.RevenueAnalytics(2L),
            Gui2NavAdapter.toScreen(AppScreen.RevenueAnalytics(2L))
        )
    }

    @Test
    fun `PaymentAnalytics maps to ScreenV2 PaymentAnalytics`() {
        assertEquals(
            ScreenV2.PaymentAnalytics(3L),
            Gui2NavAdapter.toScreen(AppScreen.PaymentAnalytics(3L))
        )
    }

    @Test
    fun `RiskAnalytics maps to ScreenV2 RiskAnalytics`() {
        assertEquals(
            ScreenV2.RiskAnalytics(4L),
            Gui2NavAdapter.toScreen(AppScreen.RiskAnalytics(4L))
        )
    }

    @Test
    fun `InvoiceAnalytics maps to ScreenV2 InvoiceAnalytics`() {
        assertEquals(
            ScreenV2.InvoiceAnalytics(5L),
            Gui2NavAdapter.toScreen(AppScreen.InvoiceAnalytics(5L))
        )
    }

    // ── toScreen: misc screens ─────────────────────────────────────────────────

    @Test
    fun `PrefilledItems returns null because it is GUI1 only`() {
        assertNull(Gui2NavAdapter.toScreen(AppScreen.PrefilledItems))
    }

    @Test
    fun `EditInvoice maps to ScreenV2 EditInvoice with both ids`() {
        assertEquals(
            ScreenV2.EditInvoice(businessId = 2L, invoiceId = 30L),
            Gui2NavAdapter.toScreen(AppScreen.EditInvoice(invoiceId = 30L, businessId = 2L))
        )
    }

    @Test
    fun `EditCustomer maps to ScreenV2 EditCustomer with both ids`() {
        assertEquals(
            ScreenV2.EditCustomer(businessId = 3L, customerId = 50L),
            Gui2NavAdapter.toScreen(AppScreen.EditCustomer(customerId = 50L, businessId = 3L))
        )
    }

    // ── fromScreen: round-trip verification ────────────────────────────────────

    @Test
    fun `fromScreen Dashboard preserves businessId`() {
        assertEquals(
            AppScreen.Dashboard(3L),
            Gui2NavAdapter.fromScreen(ScreenV2.Dashboard(3L))
        )
    }

    @Test
    fun `fromScreen Customers preserves businessId`() {
        assertEquals(
            AppScreen.CustomerList(5L),
            Gui2NavAdapter.fromScreen(ScreenV2.Customers(5L))
        )
    }

    @Test
    fun `fromScreen CustomerDetail preserves both ids`() {
        assertEquals(
            AppScreen.CustomerDetail(customerId = 9L, businessId = 2L),
            Gui2NavAdapter.fromScreen(ScreenV2.CustomerDetail(businessId = 2L, customerId = 9L))
        )
    }

    @Test
    fun `fromScreen Help maps to AppScreen Help`() {
        assertEquals(AppScreen.Help, Gui2NavAdapter.fromScreen(ScreenV2.Help(1L)))
    }

    @Test
    fun `fromScreen Vault maps to AppScreen DocumentVault`() {
        assertEquals(
            AppScreen.DocumentVault(7L),
            Gui2NavAdapter.fromScreen(ScreenV2.Vault(7L))
        )
    }

    @Test
    fun `fromScreen Settings maps to AppScreen SettingsHub`() {
        assertEquals(
            AppScreen.SettingsHub(2L),
            Gui2NavAdapter.fromScreen(ScreenV2.Settings(2L))
        )
    }

    @Test
    fun `fromScreen AppAppearance maps to AppScreen AppSettings`() {
        assertEquals(
            AppScreen.AppSettings(3L),
            Gui2NavAdapter.fromScreen(ScreenV2.AppAppearance(3L))
        )
    }

    @Test
    fun `fromScreen BusinessProfile preserves businessId`() {
        assertEquals(
            AppScreen.BusinessProfile(4L),
            Gui2NavAdapter.fromScreen(ScreenV2.BusinessProfile(4L))
        )
    }

    @Test
    fun `fromScreen AppSettings preserves businessId`() {
        assertEquals(
            AppScreen.AppSettings(5L),
            Gui2NavAdapter.fromScreen(ScreenV2.AppSettings(5L))
        )
    }

    @Test
    fun `fromScreen ThemeSettings preserves businessId`() {
        assertEquals(
            AppScreen.ThemeSettings(6L),
            Gui2NavAdapter.fromScreen(ScreenV2.ThemeSettings(6L))
        )
    }

    @Test
    fun `fromScreen RevenueAnalytics preserves businessId`() {
        assertEquals(
            AppScreen.RevenueAnalytics(7L),
            Gui2NavAdapter.fromScreen(ScreenV2.RevenueAnalytics(7L))
        )
    }

    @Test
    fun `fromScreen PaymentAnalytics preserves businessId`() {
        assertEquals(
            AppScreen.PaymentAnalytics(8L),
            Gui2NavAdapter.fromScreen(ScreenV2.PaymentAnalytics(8L))
        )
    }

    @Test
    fun `fromScreen RiskAnalytics preserves businessId`() {
        assertEquals(
            AppScreen.RiskAnalytics(9L),
            Gui2NavAdapter.fromScreen(ScreenV2.RiskAnalytics(9L))
        )
    }

    @Test
    fun `fromScreen InvoiceAnalytics preserves businessId`() {
        assertEquals(
            AppScreen.InvoiceAnalytics(10L),
            Gui2NavAdapter.fromScreen(ScreenV2.InvoiceAnalytics(10L))
        )
    }

    // ── toScreen: additional mapping tests ────────────────────────────────────

    @Test
    fun `EditInvoice maps to ScreenV2 EditInvoice with both ids`() {
        assertEquals(
            ScreenV2.EditInvoice(businessId = 0L, invoiceId = 30L),
            Gui2NavAdapter.toScreen(AppScreen.EditInvoice(invoiceId = 30L))
        )
    }

    @Test
    fun `CreateCustomer preserves businessId`() {
        assertEquals(
            ScreenV2.CreateCustomer(8L),
            Gui2NavAdapter.toScreen(AppScreen.CreateCustomer(8L))
        )
    }

    @Test
    fun `EditCustomer preserves both ids`() {
        assertEquals(
            ScreenV2.EditCustomer(businessId = 9L, customerId = 11L),
            Gui2NavAdapter.toScreen(AppScreen.EditCustomer(customerId = 11L, businessId = 9L))
        )
    }

    @Test
    fun `fromScreen Invoices preserves businessId`() {
        assertEquals(
            AppScreen.InvoiceList(12L),
            Gui2NavAdapter.fromScreen(ScreenV2.Invoices(12L))
        )
    }

    @Test
    fun `fromScreen InvoiceDetail preserves both ids`() {
        assertEquals(
            AppScreen.InvoiceDetail(invoiceId = 15L, businessId = 6L),
            Gui2NavAdapter.fromScreen(ScreenV2.InvoiceDetail(businessId = 6L, invoiceId = 15L))
        )
    }

    @Test
    fun `fromScreen CreateCustomer preserves businessId`() {
        assertEquals(
            AppScreen.CreateCustomer(8L),
            Gui2NavAdapter.fromScreen(ScreenV2.CreateCustomer(8L))
        )
    }

    @Test
    fun `fromScreen EditCustomer preserves both ids`() {
        assertEquals(
            AppScreen.EditCustomer(customerId = 11L, businessId = 9L),
            Gui2NavAdapter.fromScreen(ScreenV2.EditCustomer(businessId = 9L, customerId = 11L))
        )
    }

    @Test
    fun `fromScreen EditInvoice preserves both ids`() {
        assertEquals(
            AppScreen.EditInvoice(invoiceId = 15L, businessId = 6L),
            Gui2NavAdapter.fromScreen(ScreenV2.EditInvoice(businessId = 6L, invoiceId = 15L))
        )
    }

    @Test
    fun `fromScreen DunningNotices maps to AppScreen DunningNotices`() {
        assertEquals(
            AppScreen.DunningNotices,
            Gui2NavAdapter.fromScreen(ScreenV2.DunningNotices(1L))
        )
    }

    @Test
    fun `fromScreen PrefilledItems maps to AppScreen PrefilledItems`() {
        assertEquals(
            AppScreen.PrefilledItems,
            Gui2NavAdapter.fromScreen(ScreenV2.PrefilledItems(1L))
        )
    }

    @Test
    fun `fromScreen BackupRestore maps to AppScreen BackupRestore`() {
        assertEquals(
            AppScreen.BackupRestore,
            Gui2NavAdapter.fromScreen(ScreenV2.BackupRestore(1L))
        )
    }
}


