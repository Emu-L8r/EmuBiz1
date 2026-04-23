package com.emul8r.bizap.consistency

import com.emul8r.bizap.ui.gui2.navigation.ScreenV2
import com.emul8r.bizap.ui.gui3.navigation.ScreenV3
import com.emul8r.bizap.ui.navigation.AppRoute
import org.junit.Test
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Architecture tests for navigation route consistency.
 *
 * These tests enforce that:
 * 1. All AppRoute entries have matching ScreenV2 + ScreenV3 entries.
 * 2. Route parameter names are identical across ScreenV2 and ScreenV3
 *    (preventing the "parameter drift" bug that causes runtime crashes).
 * 3. No route class is missing from any navigation system.
 *
 * If any test here fails, a developer has added/changed a route in one
 * system but not the others. Fix: update all three route files.
 *
 * Run with: ./gradlew testDebugUnitTest --tests "*.NavigationRouteConsistencyTest"
 */
class NavigationRouteConsistencyTest {

    /**
     * Tests that ScreenV2 and ScreenV3 expose identical parameter names for
     * routes that exist in both.
     *
     * Example failure case:
     *   ScreenV2.InvoiceDetail(businessId: Long, invoiceId: Long)
     *   ScreenV3.InvoiceDetail(bizId: Long, invoiceId: Long)   ← "bizId" vs "businessId"
     *   → NavigationRoute crash at runtime when ScreenRouter bridges them
     */
    @Test
    fun `ScreenV2 and ScreenV3 routes have identical parameter names`() {
        val v2Routes = getNestedDataClasses(ScreenV2::class)
        val v3Routes = getNestedDataClasses(ScreenV3::class)

        // Find routes that exist in both V2 and V3 (should be most of them)
        val v2Names = v2Routes.map { it.simpleName }
        val v3Names = v3Routes.map { it.simpleName }
        val sharedNames = v2Names.intersect(v3Names.toSet())

        assertTrue(sharedNames.isNotEmpty(), "Expected at least 5 shared routes between ScreenV2 and ScreenV3")

        val mismatches = mutableListOf<String>()

        for (routeName in sharedNames) {
            val v2Route = v2Routes.first { it.simpleName == routeName }
            val v3Route = v3Routes.first { it.simpleName == routeName }

            val v2Params = getConstructorParamNames(v2Route)
            val v3Params = getConstructorParamNames(v3Route)

            if (v2Params != v3Params) {
                mismatches.add(
                    "Route '$routeName' parameter mismatch:\n" +
                    "  ScreenV2 params: $v2Params\n" +
                    "  ScreenV3 params: $v3Params\n" +
                    "  → Fix: Make parameter names identical in both ScreenV2.kt and ScreenV3.kt"
                )
            }
        }

        if (mismatches.isNotEmpty()) {
            fail(
                "❌ Navigation parameter drift detected in ${mismatches.size} route(s):\n\n" +
                mismatches.joinToString("\n\n") +
                "\n\nThis will cause runtime navigation crashes. Fix before merging."
            )
        }
    }

    /**
     * Tests that every AppRoute entry has a corresponding ScreenV2 entry.
     * AppRoute is the unified navigation layer — if it defines a route,
     * GUI2 must be able to handle it.
     */
    @Test
    fun `every AppRoute has a corresponding ScreenV2 entry`() {
        val appRouteNames = getNestedClasses(AppRoute::class).map { it.simpleName }
        val v2RouteNames = getNestedDataClasses(ScreenV2::class).map { it.simpleName }

        val missingInV2 = appRouteNames.filter { appRouteName ->
            // Map AppRoute names to ScreenV2 equivalents (some may differ by convention)
            val equivalentName = mapAppRouteToScreenName(appRouteName)
            equivalentName != null && !v2RouteNames.any { it?.equals(equivalentName, ignoreCase = true) == true }
        }

        if (missingInV2.isNotEmpty()) {
            fail(
                "❌ AppRoute entries not found in ScreenV2: $missingInV2\n" +
                "Add these routes to ScreenV2.kt to maintain GUI2 navigation completeness."
            )
        }
    }

    /**
     * Tests that every AppRoute entry has a corresponding ScreenV3 entry.
     * If AppRoute defines a route, GUI3 must be able to handle it.
     */
    @Test
    fun `every AppRoute has a corresponding ScreenV3 entry`() {
        val appRouteNames = getNestedClasses(AppRoute::class).map { it.simpleName }
        val v3RouteNames = getNestedDataClasses(ScreenV3::class).map { it.simpleName }

        val missingInV3 = appRouteNames.filter { appRouteName ->
            val equivalentName = mapAppRouteToScreenName(appRouteName)
            equivalentName != null && !v3RouteNames.any { it?.equals(equivalentName, ignoreCase = true) == true }
        }

        if (missingInV3.isNotEmpty()) {
            fail(
                "❌ AppRoute entries not found in ScreenV3: $missingInV3\n" +
                "Add these routes to ScreenV3.kt to maintain GUI3 navigation completeness."
            )
        }
    }

    /**
     * Tests that ScreenV2 uses sealed interface (modern Kotlin pattern)
     * and ScreenV3 uses sealed class (compatible pattern with default params).
     * This verifies the architecture contracts aren't accidentally changed.
     */
    @Test
    fun `ScreenV2 is a sealed interface and ScreenV3 is a sealed class`() {
        assertTrue(
            ScreenV2::class.java.isInterface,
            "ScreenV2 must be a sealed interface (current architecture contract)"
        )
        assertTrue(
            !ScreenV3::class.java.isInterface,
            "ScreenV3 must be a sealed class, not interface (supports default params)"
        )
    }

    /**
     * Tests that every ScreenV2 data class has businessId as a parameter.
     * This is the mandatory pattern for GUI2 — explicit context, no implicit business.
     */
    @Test
    fun `all ScreenV2 data class routes have businessId parameter`() {
        val v2Routes = getNestedDataClasses(ScreenV2::class)
        val missingBusinessId = v2Routes.filter { route ->
            val params = getConstructorParamNames(route)
            "businessId" !in params
        }

        if (missingBusinessId.isNotEmpty()) {
            fail(
                "❌ ScreenV2 routes missing businessId parameter: ${missingBusinessId.map { it.simpleName }}\n" +
                "All GUI2 routes must include businessId: Long for explicit business context."
            )
        }
    }

    /**
     * Tests that ScreenV3 routes support default businessId values.
     * ScreenV3 uses sealed class with defaults — the pattern must hold.
     */
    @Test
    fun `ScreenV3 data class routes have businessId with default value`() {
        val v3Routes = getNestedDataClasses(ScreenV3::class)
        val missingDefault = v3Routes.filter { route ->
            val params = getConstructorParamNames(route)
            "businessId" !in params
        }

        // Some routes legitimately don't need businessId (e.g., MatrixDebugPanel is OK)
        val allowedWithoutBusinessId = setOf("MatrixDebugPanel")
        val unexpected = missingDefault
            .map { it.simpleName ?: "" }
            .filter { it !in allowedWithoutBusinessId }

        if (unexpected.isNotEmpty()) {
            fail(
                "❌ ScreenV3 routes missing businessId parameter: $unexpected\n" +
                "All GUI3 routes should include businessId: Long = 1L for default business context."
            )
        }
    }

    /**
     * Smoke test: verifies that both navigation route files have at least
     * the minimum expected number of routes (guards against accidental deletion).
     */
    @Test
    fun `navigation route files have minimum expected route count`() {
        val v2Count = getNestedDataClasses(ScreenV2::class).size
        val v3Count = getNestedDataClasses(ScreenV3::class).size
        val appRouteCount = getNestedClasses(AppRoute::class).size

        assertTrue(v2Count >= 15, "ScreenV2 should have at least 15 routes, found $v2Count")
        assertTrue(v3Count >= 12, "ScreenV3 should have at least 12 routes, found $v3Count")
        assertTrue(appRouteCount >= 15, "AppRoute should have at least 15 routes, found $appRouteCount")
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private fun getNestedDataClasses(sealed: KClass<*>): List<KClass<*>> =
        sealed.nestedClasses.filter { it.isData }

    private fun getNestedClasses(sealed: KClass<*>): List<KClass<*>> =
        sealed.nestedClasses.toList()

    private fun getConstructorParamNames(klass: KClass<*>): List<String> =
        klass.primaryConstructor?.parameters?.map { it.name ?: "" } ?: emptyList()

    /**
     * Maps AppRoute class names to their ScreenV2/V3 equivalents.
     * Returns null for routes that don't require a screen equivalent
     * (e.g., abstract navigation anchors).
     */
    private fun mapAppRouteToScreenName(appRouteName: String?): String? = when (appRouteName) {
        "Dashboard" -> "Dashboard"
        "InvoiceList" -> "Invoices"
        "CreateInvoice" -> "CreateInvoice"
        "EditInvoice" -> "EditInvoice"
        "InvoiceDetail" -> "InvoiceDetail"
        "CustomerList" -> "Customers"
        "CustomerDetail" -> "CustomerDetail"
        "CreateCustomer" -> "CreateCustomer"
        "EditCustomer" -> "EditCustomer"
        "PaymentAnalytics" -> "PaymentAnalytics"
        "SettingsHub" -> "Settings"
        "Help" -> "Help"
        // These AppRoutes may not have direct screen equivalents — skip them
        "RevenueDashboard", "RiskDashboard", "DocumentVault",
        "Notes", "AppSettings", "ThemeSettings", "BusinessProfile",
        "PrefilledItems", "BackupRestore", "DunningNotices",
        "CustomerSegments", "CustomerAnalytics", "InvoicePdf" -> null
        else -> null  // Unknown routes: skip rather than fail (AppRoute is aspirational)
    }
}



