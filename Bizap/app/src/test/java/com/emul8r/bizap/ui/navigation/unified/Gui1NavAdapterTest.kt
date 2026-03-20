package com.emul8r.bizap.ui.navigation.unified

import com.emul8r.bizap.ui.navigation.Screen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit tests for [Gui1NavAdapter].
 *
 * Verifies that every [AppScreen] destination maps to the correct [Screen] route
 * and that the round-trip conversion (toScreen → fromScreen) preserves the logical
 * destination.
 */
class Gui1NavAdapterTest {

    // ── toScreen: core screens ─────────────────────────────────────────────────

    @Test
    fun `Dashboard maps to Screen Dashboard`() {
        assertEquals(Screen.Dashboard, Gui1NavAdapter.toScreen(AppScreen.Dashboard()))
    }

    @Test
    fun `CustomerList maps to Screen Customers`() {
        assertEquals(Screen.Customers, Gui1NavAdapter.toScreen(AppScreen.CustomerList()))
    }

    @Test
    fun `CustomerDetail maps to Screen CustomerDetail with id`() {
        assertEquals(
            Screen.CustomerDetail(customerId = 42L),
            Gui1NavAdapter.toScreen(AppScreen.CustomerDetail(customerId = 42L))
        )
    }

    @Test
    fun `CreateCustomer returns null because GUI1 uses bottom sheet`() {
        assertNull(Gui1NavAdapter.toScreen(AppScreen.CreateCustomer()))
    }

    @Test
    fun `EditCustomer returns null because GUI1 has no standalone edit screen`() {
        assertNull(Gui1NavAdapter.toScreen(AppScreen.EditCustomer(customerId = 1L)))
    }

    @Test
    fun `InvoiceList maps to Screen Invoices`() {
        assertEquals(Screen.Invoices, Gui1NavAdapter.toScreen(AppScreen.InvoiceList()))
    }

    @Test
    fun `InvoiceDetail maps to Screen InvoiceDetail with id`() {
        assertEquals(
            Screen.InvoiceDetail(invoiceId = 7L),
            Gui1NavAdapter.toScreen(AppScreen.InvoiceDetail(invoiceId = 7L))
        )
    }

    @Test
    fun `CreateInvoice maps to Screen CreateInvoice`() {
        assertEquals(Screen.CreateInvoice, Gui1NavAdapter.toScreen(AppScreen.CreateInvoice()))
    }

    @Test
    fun `EditInvoice maps to Screen EditInvoice with id`() {
        assertEquals(
            Screen.EditInvoice(invoiceId = 5L),
            Gui1NavAdapter.toScreen(AppScreen.EditInvoice(invoiceId = 5L))
        )
    }

    @Test
    fun `InvoicePdf maps to Screen InvoicePdf`() {
        assertEquals(
            Screen.InvoicePdf(invoiceId = 3L, isQuote = true),
            Gui1NavAdapter.toScreen(AppScreen.InvoicePdf(invoiceId = 3L, isQuote = true))
        )
    }

    // ── toScreen: settings screens ─────────────────────────────────────────────

    @Test
    fun `SettingsHub maps to Screen SettingsHub`() {
        assertEquals(Screen.SettingsHub, Gui1NavAdapter.toScreen(AppScreen.SettingsHub()))
    }

    @Test
    fun `AppSettings maps to Screen AppSettings`() {
        assertEquals(Screen.AppSettings, Gui1NavAdapter.toScreen(AppScreen.AppSettings()))
    }

    @Test
    fun `BusinessProfile maps to Screen BusinessProfile`() {
        assertEquals(Screen.BusinessProfile, Gui1NavAdapter.toScreen(AppScreen.BusinessProfile()))
    }

    @Test
    fun `ThemeSettings maps to Screen ThemeSettings`() {
        assertEquals(Screen.ThemeSettings, Gui1NavAdapter.toScreen(AppScreen.ThemeSettings()))
    }

    @Test
    fun `PrefilledItems maps to Screen PrefilledItems`() {
        assertEquals(Screen.PrefilledItems, Gui1NavAdapter.toScreen(AppScreen.PrefilledItems))
    }

    @Test
    fun `BackupRestore maps to Screen BackupRestore`() {
        assertEquals(Screen.BackupRestore, Gui1NavAdapter.toScreen(AppScreen.BackupRestore))
    }

    // ── toScreen: analytics screens ────────────────────────────────────────────

    @Test
    fun `RevenueAnalytics maps to Screen RevenueDashboard`() {
        assertEquals(Screen.RevenueDashboard, Gui1NavAdapter.toScreen(AppScreen.RevenueAnalytics()))
    }

    @Test
    fun `PaymentAnalytics maps to Screen PaymentAnalytics`() {
        assertEquals(
            Screen.PaymentAnalytics(businessId = 1L),
            Gui1NavAdapter.toScreen(AppScreen.PaymentAnalytics(businessId = 1L))
        )
    }

    @Test
    fun `RiskAnalytics maps to Screen RiskDashboard`() {
        assertEquals(Screen.RiskDashboard, Gui1NavAdapter.toScreen(AppScreen.RiskAnalytics()))
    }

    @Test
    fun `InvoiceAnalytics returns null because it is GUI2 only`() {
        assertNull(Gui1NavAdapter.toScreen(AppScreen.InvoiceAnalytics()))
    }

    // ── toScreen: misc screens ─────────────────────────────────────────────────

    @Test
    fun `DocumentVault maps to Screen DocumentVault`() {
        assertEquals(Screen.DocumentVault, Gui1NavAdapter.toScreen(AppScreen.DocumentVault()))
    }

    @Test
    fun `Help maps to Screen Help`() {
        assertEquals(Screen.Help, Gui1NavAdapter.toScreen(AppScreen.Help))
    }

    @Test
    fun `DunningNotices maps to Screen DunningNotices`() {
        assertEquals(Screen.DunningNotices, Gui1NavAdapter.toScreen(AppScreen.DunningNotices))
    }

    @Test
    fun `Notes maps to Screen Notes`() {
        assertEquals(Screen.Notes, Gui1NavAdapter.toScreen(AppScreen.Notes))
    }

    // ── fromScreen: round-trip verification ────────────────────────────────────

    @Test
    fun `fromScreen Dashboard round-trips to AppScreen Dashboard`() {
        assertEquals(AppScreen.Dashboard(), Gui1NavAdapter.fromScreen(Screen.Dashboard))
    }

    @Test
    fun `fromScreen Customers round-trips to AppScreen CustomerList`() {
        assertEquals(AppScreen.CustomerList(), Gui1NavAdapter.fromScreen(Screen.Customers))
    }

    @Test
    fun `fromScreen CustomerDetail preserves customerId`() {
        val result = Gui1NavAdapter.fromScreen(Screen.CustomerDetail(customerId = 99L))
        assertEquals(AppScreen.CustomerDetail(customerId = 99L), result)
    }

    @Test
    fun `fromScreen Help round-trips to AppScreen Help`() {
        assertEquals(AppScreen.Help, Gui1NavAdapter.fromScreen(Screen.Help))
    }

    @Test
    fun `fromScreen InvoiceDetail preserves invoiceId`() {
        val result = Gui1NavAdapter.fromScreen(Screen.InvoiceDetail(invoiceId = 55L))
        assertEquals(AppScreen.InvoiceDetail(invoiceId = 55L), result)
    }

    @Test
    fun `fromScreen Invoices round-trips to AppScreen InvoiceList`() {
        assertEquals(AppScreen.InvoiceList(), Gui1NavAdapter.fromScreen(Screen.Invoices))
    }

    @Test
    fun `fromScreen CreateInvoice round-trips to AppScreen CreateInvoice`() {
        assertEquals(AppScreen.CreateInvoice(), Gui1NavAdapter.fromScreen(Screen.CreateInvoice))
    }

    @Test
    fun `fromScreen EditInvoice preserves invoiceId`() {
        assertEquals(
            AppScreen.EditInvoice(invoiceId = 12L),
            Gui1NavAdapter.fromScreen(Screen.EditInvoice(invoiceId = 12L))
        )
    }

    @Test
    fun `fromScreen InvoicePdf preserves invoiceId and isQuote`() {
        assertEquals(
            AppScreen.InvoicePdf(invoiceId = 8L, isQuote = false),
            Gui1NavAdapter.fromScreen(Screen.InvoicePdf(invoiceId = 8L, isQuote = false))
        )
    }

    @Test
    fun `fromScreen SettingsHub round-trips to AppScreen SettingsHub`() {
        assertEquals(AppScreen.SettingsHub(), Gui1NavAdapter.fromScreen(Screen.SettingsHub))
    }

    @Test
    fun `fromScreen AppSettings round-trips to AppScreen AppSettings`() {
        assertEquals(AppScreen.AppSettings(), Gui1NavAdapter.fromScreen(Screen.AppSettings))
    }

    @Test
    fun `fromScreen BusinessProfile round-trips to AppScreen BusinessProfile`() {
        assertEquals(AppScreen.BusinessProfile(), Gui1NavAdapter.fromScreen(Screen.BusinessProfile))
    }

    @Test
    fun `fromScreen ThemeSettings round-trips to AppScreen ThemeSettings`() {
        assertEquals(AppScreen.ThemeSettings(), Gui1NavAdapter.fromScreen(Screen.ThemeSettings))
    }

    @Test
    fun `fromScreen PrefilledItems round-trips to AppScreen PrefilledItems`() {
        assertEquals(AppScreen.PrefilledItems, Gui1NavAdapter.fromScreen(Screen.PrefilledItems))
    }

    @Test
    fun `fromScreen BackupRestore round-trips to AppScreen BackupRestore`() {
        assertEquals(AppScreen.BackupRestore, Gui1NavAdapter.fromScreen(Screen.BackupRestore))
    }

    @Test
    fun `fromScreen DocumentVault round-trips to AppScreen DocumentVault`() {
        assertEquals(AppScreen.DocumentVault(), Gui1NavAdapter.fromScreen(Screen.DocumentVault))
    }

    @Test
    fun `fromScreen RevenueDashboard round-trips to AppScreen RevenueAnalytics`() {
        assertEquals(AppScreen.RevenueAnalytics(), Gui1NavAdapter.fromScreen(Screen.RevenueDashboard))
    }

    @Test
    fun `fromScreen PaymentAnalytics preserves businessId`() {
        assertEquals(
            AppScreen.PaymentAnalytics(businessId = 3L),
            Gui1NavAdapter.fromScreen(Screen.PaymentAnalytics(businessId = 3L))
        )
    }

    @Test
    fun `fromScreen PaymentAnalytics with null businessId`() {
        assertEquals(
            AppScreen.PaymentAnalytics(businessId = null),
            Gui1NavAdapter.fromScreen(Screen.PaymentAnalytics(businessId = null))
        )
    }

    @Test
    fun `fromScreen RiskDashboard round-trips to AppScreen RiskAnalytics`() {
        assertEquals(AppScreen.RiskAnalytics(), Gui1NavAdapter.fromScreen(Screen.RiskDashboard))
    }

    @Test
    fun `fromScreen DunningNotices round-trips to AppScreen DunningNotices`() {
        assertEquals(AppScreen.DunningNotices, Gui1NavAdapter.fromScreen(Screen.DunningNotices))
    }

    @Test
    fun `fromScreen CustomerSegments round-trips to AppScreen CustomerSegments`() {
        assertEquals(AppScreen.CustomerSegments, Gui1NavAdapter.fromScreen(Screen.CustomerSegments))
    }

    @Test
    fun `fromScreen CustomerAnalytics round-trips to AppScreen CustomerAnalytics`() {
        assertEquals(AppScreen.CustomerAnalytics, Gui1NavAdapter.fromScreen(Screen.CustomerAnalytics))
    }

    @Test
    fun `fromScreen Notes round-trips to AppScreen Notes`() {
        assertEquals(AppScreen.Notes, Gui1NavAdapter.fromScreen(Screen.Notes))
    }

    // ── toScreen: misc screens ────────────────────────────────────────────────

    @Test
    fun `CustomerSegments maps to Screen CustomerSegments`() {
        assertEquals(Screen.CustomerSegments, Gui1NavAdapter.toScreen(AppScreen.CustomerSegments))
    }

    @Test
    fun `CustomerAnalytics maps to Screen CustomerAnalytics`() {
        assertEquals(Screen.CustomerAnalytics, Gui1NavAdapter.toScreen(AppScreen.CustomerAnalytics))
    }
}
