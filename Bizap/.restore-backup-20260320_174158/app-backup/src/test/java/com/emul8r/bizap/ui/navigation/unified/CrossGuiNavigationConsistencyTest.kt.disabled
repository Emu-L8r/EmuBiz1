package com.emul8r.bizap.ui.navigation.unified

import com.emul8r.bizap.ui.gui2.navigation.ScreenV2
import com.emul8r.bizap.ui.navigation.Screen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Cross-GUI navigation consistency tests for Phase 3.3 consolidation.
 *
 * Verifies that shared screens (those available in both GUI1 and GUI2) map
 * correctly in both adapters and that GUI-specific screens are correctly
 * blocked (return null) in the non-owning adapter.
 *
 * Also verifies the round-trip property: an [AppScreen] converted to a
 * GUI-specific route and back yields the same logical destination.
 */
class CrossGuiNavigationConsistencyTest {

    private val bizId = 1L

    // ── Shared screens: both adapters must map to a non-null route ─────────────

    @Test
    fun `Dashboard is reachable in GUI1`() {
        assertNotNull(Gui1NavAdapter.toScreen(AppScreen.Dashboard()))
    }

    @Test
    fun `Dashboard is reachable in GUI2`() {
        assertNotNull(Gui2NavAdapter.toScreen(AppScreen.Dashboard(), bizId))
    }

    @Test
    fun `CustomerList is reachable in GUI1`() {
        assertNotNull(Gui1NavAdapter.toScreen(AppScreen.CustomerList()))
    }

    @Test
    fun `CustomerList is reachable in GUI2`() {
        assertNotNull(Gui2NavAdapter.toScreen(AppScreen.CustomerList(), bizId))
    }

    @Test
    fun `InvoiceList is reachable in GUI1`() {
        assertNotNull(Gui1NavAdapter.toScreen(AppScreen.InvoiceList()))
    }

    @Test
    fun `InvoiceList is reachable in GUI2`() {
        assertNotNull(Gui2NavAdapter.toScreen(AppScreen.InvoiceList(), bizId))
    }

    @Test
    fun `SettingsHub is reachable in GUI1`() {
        assertNotNull(Gui1NavAdapter.toScreen(AppScreen.SettingsHub()))
    }

    @Test
    fun `SettingsHub is reachable in GUI2`() {
        assertNotNull(Gui2NavAdapter.toScreen(AppScreen.SettingsHub(), bizId))
    }

    @Test
    fun `AppSettings is reachable in GUI1`() {
        assertNotNull(Gui1NavAdapter.toScreen(AppScreen.AppSettings()))
    }

    @Test
    fun `AppSettings is reachable in GUI2`() {
        assertNotNull(Gui2NavAdapter.toScreen(AppScreen.AppSettings(), bizId))
    }

    @Test
    fun `BusinessProfile is reachable in GUI1`() {
        assertNotNull(Gui1NavAdapter.toScreen(AppScreen.BusinessProfile()))
    }

    @Test
    fun `BusinessProfile is reachable in GUI2`() {
        assertNotNull(Gui2NavAdapter.toScreen(AppScreen.BusinessProfile(), bizId))
    }

    @Test
    fun `Help is reachable in GUI1`() {
        assertEquals(Screen.Help, Gui1NavAdapter.toScreen(AppScreen.Help))
    }

    @Test
    fun `Help is reachable in GUI2`() {
        assertEquals(ScreenV2.Help(bizId), Gui2NavAdapter.toScreen(AppScreen.Help, bizId))
    }

    @Test
    fun `CreateInvoice is reachable in GUI1`() {
        assertNotNull(Gui1NavAdapter.toScreen(AppScreen.CreateInvoice()))
    }

    @Test
    fun `CreateInvoice is reachable in GUI2`() {
        assertNotNull(Gui2NavAdapter.toScreen(AppScreen.CreateInvoice(), bizId))
    }

    @Test
    fun `DocumentVault is reachable in GUI1`() {
        assertNotNull(Gui1NavAdapter.toScreen(AppScreen.DocumentVault()))
    }

    @Test
    fun `DocumentVault is reachable in GUI2`() {
        assertNotNull(Gui2NavAdapter.toScreen(AppScreen.DocumentVault(), bizId))
    }

    // ── GUI1-only screens: must map in GUI1, return null in GUI2 ──────────────

    @Test
    fun `InvoicePdf is GUI1 only - maps in GUI1`() {
        assertNotNull(Gui1NavAdapter.toScreen(AppScreen.InvoicePdf(invoiceId = 1L, isQuote = false)))
    }

    @Test
    fun `InvoicePdf is GUI1 only - returns null in GUI2`() {
        assertNull(Gui2NavAdapter.toScreen(AppScreen.InvoicePdf(invoiceId = 1L, isQuote = false), bizId))
    }

    @Test
    fun `PrefilledItems is GUI1 only - maps in GUI1`() {
        assertNotNull(Gui1NavAdapter.toScreen(AppScreen.PrefilledItems))
    }

    @Test
    fun `PrefilledItems is GUI1 only - returns null in GUI2`() {
        assertNull(Gui2NavAdapter.toScreen(AppScreen.PrefilledItems, bizId))
    }

    @Test
    fun `DunningNotices is GUI1 only - maps in GUI1`() {
        assertNotNull(Gui1NavAdapter.toScreen(AppScreen.DunningNotices))
    }

    @Test
    fun `DunningNotices is GUI1 only - returns null in GUI2`() {
        assertNull(Gui2NavAdapter.toScreen(AppScreen.DunningNotices, bizId))
    }

    @Test
    fun `Notes is GUI1 only - maps in GUI1`() {
        assertNotNull(Gui1NavAdapter.toScreen(AppScreen.Notes))
    }

    @Test
    fun `Notes is GUI1 only - returns null in GUI2`() {
        assertNull(Gui2NavAdapter.toScreen(AppScreen.Notes, bizId))
    }

    // ── GUI2-only screens: must map in GUI2, return null in GUI1 ─────────────

    @Test
    fun `InvoiceAnalytics is GUI2 only - maps in GUI2`() {
        assertNotNull(Gui2NavAdapter.toScreen(AppScreen.InvoiceAnalytics(), bizId))
    }

    @Test
    fun `InvoiceAnalytics is GUI2 only - returns null in GUI1`() {
        assertNull(Gui1NavAdapter.toScreen(AppScreen.InvoiceAnalytics()))
    }

    @Test
    fun `CreateCustomer is GUI2 only - maps in GUI2`() {
        assertNotNull(Gui2NavAdapter.toScreen(AppScreen.CreateCustomer(), bizId))
    }

    @Test
    fun `CreateCustomer is GUI2 only - returns null in GUI1`() {
        assertNull(Gui1NavAdapter.toScreen(AppScreen.CreateCustomer()))
    }

    // ── Round-trip cross-GUI: AppScreen → GUI1 → AppScreen ───────────────────

    @Test
    fun `GUI1 round-trip Dashboard preserves destination`() {
        val appScreen = AppScreen.Dashboard()
        val screen = Gui1NavAdapter.toScreen(appScreen)!!
        assertEquals(appScreen, Gui1NavAdapter.fromScreen(screen))
    }

    @Test
    fun `GUI1 round-trip CustomerList preserves destination`() {
        val appScreen = AppScreen.CustomerList()
        val screen = Gui1NavAdapter.toScreen(appScreen)!!
        assertEquals(appScreen, Gui1NavAdapter.fromScreen(screen))
    }

    @Test
    fun `GUI1 round-trip CustomerDetail preserves customerId`() {
        val appScreen = AppScreen.CustomerDetail(customerId = 77L)
        val screen = Gui1NavAdapter.toScreen(appScreen)!!
        assertEquals(appScreen, Gui1NavAdapter.fromScreen(screen))
    }

    @Test
    fun `GUI1 round-trip InvoiceDetail preserves invoiceId`() {
        val appScreen = AppScreen.InvoiceDetail(invoiceId = 33L)
        val screen = Gui1NavAdapter.toScreen(appScreen)!!
        assertEquals(appScreen, Gui1NavAdapter.fromScreen(screen))
    }

    @Test
    fun `GUI1 round-trip CreateInvoice preserves destination`() {
        val appScreen = AppScreen.CreateInvoice()
        val screen = Gui1NavAdapter.toScreen(appScreen)!!
        assertEquals(appScreen, Gui1NavAdapter.fromScreen(screen))
    }

    @Test
    fun `GUI1 round-trip Help preserves destination`() {
        val screen = Gui1NavAdapter.toScreen(AppScreen.Help)!!
        assertEquals(AppScreen.Help, Gui1NavAdapter.fromScreen(screen))
    }

    // ── Round-trip cross-GUI: AppScreen → GUI2 → AppScreen ───────────────────

    @Test
    fun `GUI2 round-trip Dashboard preserves businessId`() {
        val appScreen = AppScreen.Dashboard(bizId)
        val route = Gui2NavAdapter.toScreen(appScreen, bizId)!!
        assertEquals(appScreen, Gui2NavAdapter.fromScreen(route))
    }

    @Test
    fun `GUI2 round-trip CustomerList preserves businessId`() {
        val appScreen = AppScreen.CustomerList(bizId)
        val route = Gui2NavAdapter.toScreen(appScreen, bizId)!!
        assertEquals(appScreen, Gui2NavAdapter.fromScreen(route))
    }

    @Test
    fun `GUI2 round-trip CustomerDetail preserves both ids`() {
        val appScreen = AppScreen.CustomerDetail(customerId = 88L, businessId = bizId)
        val route = Gui2NavAdapter.toScreen(appScreen, bizId)!!
        assertEquals(appScreen, Gui2NavAdapter.fromScreen(route))
    }

    @Test
    fun `GUI2 round-trip InvoiceDetail preserves both ids`() {
        val appScreen = AppScreen.InvoiceDetail(invoiceId = 44L, businessId = bizId)
        val route = Gui2NavAdapter.toScreen(appScreen, bizId)!!
        assertEquals(appScreen, Gui2NavAdapter.fromScreen(route))
    }

    @Test
    fun `GUI2 round-trip Help maps to AppScreen Help regardless of businessId`() {
        val route = Gui2NavAdapter.toScreen(AppScreen.Help, bizId)!!
        assertEquals(AppScreen.Help, Gui2NavAdapter.fromScreen(route))
    }

    @Test
    fun `GUI2 round-trip CreateInvoice preserves businessId`() {
        val appScreen = AppScreen.CreateInvoice(bizId)
        val route = Gui2NavAdapter.toScreen(appScreen, bizId)!!
        assertEquals(appScreen, Gui2NavAdapter.fromScreen(route))
    }

    @Test
    fun `GUI2 round-trip DocumentVault preserves businessId`() {
        val appScreen = AppScreen.DocumentVault(bizId)
        val route = Gui2NavAdapter.toScreen(appScreen, bizId)!!
        assertEquals(appScreen, Gui2NavAdapter.fromScreen(route))
    }
}
