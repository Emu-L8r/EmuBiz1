package com.emul8r.bizap.ui.navigation.unified

import com.emul8r.bizap.ui.gui2.navigation.ScreenV2
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit tests for [Gui2NavAdapter].
 *
 * Verifies that every [AppScreen] destination maps to the correct [ScreenV2] route
 * (with businessId applied from either the AppScreen or the fallback) and that
 * GUI1-only screens return `null`.
 */
class Gui2NavAdapterTest {

    private val fallback = 10L

    // ── toScreen: core screens ─────────────────────────────────────────────────

    @Test
    fun `Dashboard uses businessId from AppScreen`() {
        assertEquals(
            ScreenV2.Dashboard(5L),
            Gui2NavAdapter.toScreen(AppScreen.Dashboard(5L), fallback)
        )
    }

    @Test
    fun `Dashboard falls back to fallbackBusinessId when AppScreen has no id`() {
        assertEquals(
            ScreenV2.Dashboard(fallback),
            Gui2NavAdapter.toScreen(AppScreen.Dashboard(null), fallback)
        )
    }

    @Test
    fun `CustomerList maps to ScreenV2 Customers`() {
        assertEquals(
            ScreenV2.Customers(fallback),
            Gui2NavAdapter.toScreen(AppScreen.CustomerList(), fallback)
        )
    }

    @Test
    fun `CustomerDetail maps to ScreenV2 CustomerDetail with both ids`() {
        assertEquals(
            ScreenV2.CustomerDetail(businessId = fallback, customerId = 42L),
            Gui2NavAdapter.toScreen(AppScreen.CustomerDetail(customerId = 42L), fallback)
        )
    }

    @Test
    fun `CreateCustomer maps to ScreenV2 CreateCustomer`() {
        assertEquals(
            ScreenV2.CreateCustomer(fallback),
            Gui2NavAdapter.toScreen(AppScreen.CreateCustomer(), fallback)
        )
    }

    @Test
    fun `InvoiceList maps to ScreenV2 Invoices`() {
        assertEquals(
            ScreenV2.Invoices(fallback),
            Gui2NavAdapter.toScreen(AppScreen.InvoiceList(), fallback)
        )
    }

    @Test
    fun `InvoiceDetail maps to ScreenV2 InvoiceDetail with invoiceId`() {
        assertEquals(
            ScreenV2.InvoiceDetail(businessId = fallback, invoiceId = 7L),
            Gui2NavAdapter.toScreen(AppScreen.InvoiceDetail(invoiceId = 7L), fallback)
        )
    }

    @Test
    fun `CreateInvoice maps to ScreenV2 CreateInvoice`() {
        assertEquals(
            ScreenV2.CreateInvoice(fallback),
            Gui2NavAdapter.toScreen(AppScreen.CreateInvoice(), fallback)
        )
    }

    @Test
    fun `InvoicePdf returns null because it is GUI1 only`() {
        assertNull(Gui2NavAdapter.toScreen(AppScreen.InvoicePdf(1L, false), fallback))
    }

    // ── toScreen: settings screens ─────────────────────────────────────────────

    @Test
    fun `SettingsHub maps to ScreenV2 Settings`() {
        assertEquals(
            ScreenV2.Settings(fallback),
            Gui2NavAdapter.toScreen(AppScreen.SettingsHub(), fallback)
        )
    }

    @Test
    fun `AppSettings maps to ScreenV2 AppSettings`() {
        assertEquals(
            ScreenV2.AppSettings(fallback),
            Gui2NavAdapter.toScreen(AppScreen.AppSettings(), fallback)
        )
    }

    @Test
    fun `BusinessProfile maps to ScreenV2 BusinessProfile`() {
        assertEquals(
            ScreenV2.BusinessProfile(fallback),
            Gui2NavAdapter.toScreen(AppScreen.BusinessProfile(), fallback)
        )
    }

    @Test
    fun `PrefilledItems returns null because it is GUI1 only`() {
        assertNull(Gui2NavAdapter.toScreen(AppScreen.PrefilledItems, fallback))
    }

    @Test
    fun `BackupRestore returns null because it is GUI1 only`() {
        assertNull(Gui2NavAdapter.toScreen(AppScreen.BackupRestore, fallback))
    }

    // ── toScreen: analytics screens ────────────────────────────────────────────

    @Test
    fun `RevenueAnalytics maps to ScreenV2 RevenueAnalytics`() {
        assertEquals(
            ScreenV2.RevenueAnalytics(fallback),
            Gui2NavAdapter.toScreen(AppScreen.RevenueAnalytics(), fallback)
        )
    }

    @Test
    fun `PaymentAnalytics maps to ScreenV2 PaymentAnalytics`() {
        assertEquals(
            ScreenV2.PaymentAnalytics(fallback),
            Gui2NavAdapter.toScreen(AppScreen.PaymentAnalytics(), fallback)
        )
    }

    @Test
    fun `RiskAnalytics maps to ScreenV2 RiskAnalytics`() {
        assertEquals(
            ScreenV2.RiskAnalytics(fallback),
            Gui2NavAdapter.toScreen(AppScreen.RiskAnalytics(), fallback)
        )
    }

    @Test
    fun `InvoiceAnalytics maps to ScreenV2 InvoiceAnalytics`() {
        assertEquals(
            ScreenV2.InvoiceAnalytics(fallback),
            Gui2NavAdapter.toScreen(AppScreen.InvoiceAnalytics(), fallback)
        )
    }

    // ── toScreen: misc screens ─────────────────────────────────────────────────

    @Test
    fun `DocumentVault maps to ScreenV2 Vault`() {
        assertEquals(
            ScreenV2.Vault(fallback),
            Gui2NavAdapter.toScreen(AppScreen.DocumentVault(), fallback)
        )
    }

    @Test
    fun `Help maps to ScreenV2 Help`() {
        assertEquals(
            ScreenV2.Help(fallback),
            Gui2NavAdapter.toScreen(AppScreen.Help, fallback)
        )
    }

    @Test
    fun `DunningNotices returns null because it is GUI1 only`() {
        assertNull(Gui2NavAdapter.toScreen(AppScreen.DunningNotices, fallback))
    }

    @Test
    fun `Notes returns null because it is GUI1 only`() {
        assertNull(Gui2NavAdapter.toScreen(AppScreen.Notes, fallback))
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
}
