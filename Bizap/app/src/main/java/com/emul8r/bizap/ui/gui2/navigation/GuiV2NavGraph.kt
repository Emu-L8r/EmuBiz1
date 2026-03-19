package com.emul8r.bizap.ui.gui2.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.emul8r.bizap.ui.components.SyncStatusIndicator
import com.emul8r.bizap.ui.customers.CustomerDetailScreen
import com.emul8r.bizap.ui.customers.CustomerListScreen
import com.emul8r.bizap.ui.documents.DocumentVaultScreen
import com.emul8r.bizap.ui.gui2.analytics.InvoiceAnalyticsScreenV2
import com.emul8r.bizap.ui.gui2.analytics.PaymentAnalyticsScreenV2
import com.emul8r.bizap.ui.gui2.analytics.RevenueAnalyticsScreenV2
import com.emul8r.bizap.ui.gui2.analytics.RiskAnalyticsScreenV2
import com.emul8r.bizap.ui.gui2.customers.CreateCustomerScreenV2
import com.emul8r.bizap.ui.gui2.customers.EditCustomerScreenV2
import com.emul8r.bizap.ui.gui2.dashboard.DashboardScreenV2
import com.emul8r.bizap.ui.gui2.invoice.InvoiceDetailScreenV2
import com.emul8r.bizap.ui.gui2.invoices.CreateInvoiceScreenV2
import com.emul8r.bizap.ui.gui2.invoices.EditInvoiceScreenV2
import com.emul8r.bizap.ui.gui2.invoices.InvoiceListScreenV2
import com.emul8r.bizap.ui.gui2.settings.SettingsHubScreenV2
import com.emul8r.bizap.ui.gui2.settings.ThemeSettingsScreenV2
import com.emul8r.bizap.ui.landing.GuiMode
import com.emul8r.bizap.ui.settings.BusinessProfileScreen
import com.emul8r.bizap.presentation.ui.screens.SettingsScreen as AppSettingsScreenV2
import com.emul8r.bizap.ui.shared.screens.HelpScreen

/**
 * Navigation graph for GUI2.
 * businessId is passed explicitly as a route parameter to every screen.
 *
 * @param navController  The NavHostController for GUI2.
 * @param startBusinessId  The businessId to use as the root screen argument.
 * @param modifier  Optional modifier for the nav host.
 */
@Composable
fun GuiV2NavGraph(
    navController: NavHostController,
    startBusinessId: Long,
    onSwitchToGui1: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Global Sync and Offline Status Indicator
        SyncStatusIndicator()

        NavHost(
            navController = navController,
            startDestination = ScreenV2.Dashboard(startBusinessId),
            modifier = Modifier.weight(1f)
        ) {
            composable<ScreenV2.Dashboard> { backStackEntry ->
                val route: ScreenV2.Dashboard = backStackEntry.toRoute()
                DashboardScreenV2(
                    businessId = route.businessId,
                    onNavigateToRevenue = { navController.navigateToRevenueAnalyticsV2(route.businessId) },
                    onNavigateToPayment = { navController.navigateToPaymentAnalyticsV2(route.businessId) },
                    onNavigateToRisk = { navController.navigateToRiskAnalyticsV2(route.businessId) },
                    onNavigateToInvoice = { invoiceId ->
                        navController.navigateToInvoiceDetailV2(route.businessId, invoiceId)
                    },
                    onNavigateToCustomers = { navController.navigateToCustomersV2(route.businessId) },
                    onNavigateToInvoices = { navController.navigateToInvoicesV2(route.businessId) },
                    onNavigateToInvoiceAnalytics = { navController.navigateToInvoiceAnalyticsV2(route.businessId) },
                    onNavigateToVault = { navController.navigateToVaultV2(route.businessId) },
                    onCreateCustomer = { navController.navigateToCreateCustomerV2(route.businessId) },
                    onCreateInvoice = { navController.navigateToCreateInvoiceV2(route.businessId) },
                    onNavigateToSettings = { navController.navigateToSettingsV2(route.businessId) },
                    onSwitchToGui1 = onSwitchToGui1
                )
            }

            composable<ScreenV2.RevenueAnalytics> { backStackEntry ->
                val route: ScreenV2.RevenueAnalytics = backStackEntry.toRoute()
                RevenueAnalyticsScreenV2(
                    businessId = route.businessId,
                    onBack = { navController.popBackStack() }
                )
            }

            composable<ScreenV2.PaymentAnalytics> { backStackEntry ->
                val route: ScreenV2.PaymentAnalytics = backStackEntry.toRoute()
                PaymentAnalyticsScreenV2(
                    businessId = route.businessId,
                    onBack = { navController.popBackStack() }
                )
            }

            composable<ScreenV2.RiskAnalytics> { backStackEntry ->
                val route: ScreenV2.RiskAnalytics = backStackEntry.toRoute()
                RiskAnalyticsScreenV2(
                    businessId = route.businessId,
                    onBack = { navController.popBackStack() }
                )
            }

            composable<ScreenV2.InvoiceDetail> { backStackEntry ->
                val route: ScreenV2.InvoiceDetail = backStackEntry.toRoute()
                InvoiceDetailScreenV2(
                    businessId = route.businessId,
                    invoiceId = route.invoiceId,
                    onBack = { navController.popBackStack() }
                )
            }

            // ── Customer routes ──

            composable<ScreenV2.Customers> { backStackEntry ->
                val route: ScreenV2.Customers = backStackEntry.toRoute()
                CustomerListScreen(
                    guiMode = GuiMode.GUI2,
                    businessId = route.businessId,
                    onCustomerClick = { customerId ->
                        navController.navigateToCustomerDetailV2(route.businessId, customerId)
                    },
                    onCreateCustomer = { navController.navigateToCreateCustomerV2(route.businessId) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable<ScreenV2.CustomerDetail> { backStackEntry ->
                val route: ScreenV2.CustomerDetail = backStackEntry.toRoute()
                CustomerDetailScreen(
                    guiMode = GuiMode.GUI2,
                    customerId = route.customerId,
                    businessId = route.businessId,
                    onEdit = {
                        navController.navigateToEditCustomerV2(route.businessId, route.customerId)
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable<ScreenV2.CreateCustomer> { backStackEntry ->
                val route: ScreenV2.CreateCustomer = backStackEntry.toRoute()
                CreateCustomerScreenV2(
                    businessId = route.businessId,
                    onCreate = { navController.popBackStack() },
                    onBack = { navController.popBackStack() }
                )
            }

            composable<ScreenV2.EditCustomer> { backStackEntry ->
                val route: ScreenV2.EditCustomer = backStackEntry.toRoute()
                EditCustomerScreenV2(
                    businessId = route.businessId,
                    customerId = route.customerId,
                    onUpdate = { navController.popBackStack() },
                    onBack = { navController.popBackStack() }
                )
            }

            // ── Invoice routes ──

            composable<ScreenV2.Invoices> { backStackEntry ->
                val route: ScreenV2.Invoices = backStackEntry.toRoute()
                InvoiceListScreenV2(
                    businessId = route.businessId,
                    onInvoiceClick = { invoiceId ->
                        navController.navigateToInvoiceDetailV2(route.businessId, invoiceId)
                    },
                    onCreateInvoice = { navController.navigateToCreateInvoiceV2(route.businessId) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable<ScreenV2.CreateInvoice> { backStackEntry ->
                val route: ScreenV2.CreateInvoice = backStackEntry.toRoute()
                CreateInvoiceScreenV2(
                    businessId = route.businessId,
                    onCreate = { navController.popBackStack() },
                    onBack = { navController.popBackStack() }
                )
            }

            composable<ScreenV2.EditInvoice> { backStackEntry ->
                val route: ScreenV2.EditInvoice = backStackEntry.toRoute()
                EditInvoiceScreenV2(
                    businessId = route.businessId,
                    invoiceId = route.invoiceId,
                    onUpdate = { navController.popBackStack() },
                    onBack = { navController.popBackStack() }
                )
            }

            // ── Settings routes ──

            composable<ScreenV2.Settings> { backStackEntry ->
                val route: ScreenV2.Settings = backStackEntry.toRoute()
                SettingsHubScreenV2(
                    onBusinessProfileClick = {
                        navController.navigateToBusinessProfileV2(route.businessId)
                    },
                    onThemeSettingsClick = {
                        navController.navigateToThemeSettingsV2(route.businessId)
                    },
                    onAppSettingsClick = {
                        navController.navigateToAppSettingsV2(route.businessId)
                    },
                    onHelpClick = {
                        navController.navigateToHelpV2(route.businessId)
                    },
                    onBack = { navController.popBackStack() },
                    onSwitchToGui1 = onSwitchToGui1
                )
            }

            composable<ScreenV2.BusinessProfile> { backStackEntry ->
                BusinessProfileScreen(
                    guiMode = GuiMode.GUI2,
                    onBack = { navController.popBackStack() }
                )
            }

            composable<ScreenV2.ThemeSettings> { backStackEntry ->
                val route: ScreenV2.ThemeSettings = backStackEntry.toRoute()
                ThemeSettingsScreenV2(
                    businessId = route.businessId,
                    onBack = { navController.popBackStack() }
                )
            }

            composable<ScreenV2.InvoiceAnalytics> { backStackEntry ->
                val route: ScreenV2.InvoiceAnalytics = backStackEntry.toRoute()
                InvoiceAnalyticsScreenV2(
                    businessId = route.businessId,
                    onBack = { navController.popBackStack() }
                )
            }

            composable<ScreenV2.Vault> {
                DocumentVaultScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            // SettingsScreen is user-level (not business-scoped) so businessId is not
            // extracted from the route; it is carried only for navigation-graph consistency.
            composable<ScreenV2.AppSettings> {
                AppSettingsScreenV2(
                    onResetConfirmed = { navController.popBackStack() }
                )
            }

            // HelpScreen is shared between GUI1 and GUI2.
            composable<ScreenV2.Help> {
                HelpScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
