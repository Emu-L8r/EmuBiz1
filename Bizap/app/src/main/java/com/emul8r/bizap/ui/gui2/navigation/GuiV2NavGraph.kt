package com.emul8r.bizap.ui.gui2.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.emul8r.bizap.ui.components.SyncStatusIndicator
import com.emul8r.bizap.ui.documents.DocumentVaultScreen
import com.emul8r.bizap.ui.dunning.DunningNoticesScreen
import com.emul8r.bizap.ui.notes.NotesScreen
import com.emul8r.bizap.ui.gui2.analytics.InvoiceAnalyticsScreenV2
import com.emul8r.bizap.ui.gui2.analytics.PaymentAnalyticsScreenV2
import com.emul8r.bizap.ui.gui2.analytics.RevenueAnalyticsScreenV2
import com.emul8r.bizap.ui.gui2.analytics.RiskAnalyticsScreenV2
import com.emul8r.bizap.ui.analytics.AnalyticsFocusedInsightsScreen
import com.emul8r.bizap.ui.customers.CustomerDetailScreen
import com.emul8r.bizap.ui.customers.CustomerListScreen
import com.emul8r.bizap.ui.gui2.customers.CreateCustomerScreenV2
import com.emul8r.bizap.ui.gui2.customers.EditCustomerScreenV2
import com.emul8r.bizap.ui.landing.GuiMode
import com.emul8r.bizap.ui.gui2.dashboard.DashboardScreenV2
import com.emul8r.bizap.ui.gui2.invoice.InvoiceDetailScreenV2
import com.emul8r.bizap.ui.gui2.invoices.CreateInvoiceScreenV2
import com.emul8r.bizap.ui.gui2.invoices.EditInvoiceScreenV2
import com.emul8r.bizap.ui.gui2.invoices.InvoiceListScreenV2
import com.emul8r.bizap.ui.gui2.settings.SettingsHubScreenV2
import com.emul8r.bizap.ui.gui2.settings.AppAppearanceScreenV2
import com.emul8r.bizap.ui.gui2.settings.InvoiceCustomizationSettingsScreenV2
import com.emul8r.bizap.ui.settings.backup.BackupRestoreScreen
import com.emul8r.bizap.ui.settings.BusinessProfileScreen
import com.emul8r.bizap.ui.settings.InvoiceSettingsScreen
import com.emul8r.bizap.ui.settings.PrefilledItemsScreen
import com.emul8r.bizap.ui.shared.screens.HelpScreen
import com.emul8r.bizap.ui.navigation.Screen
import com.emul8r.bizap.domain.model.UIMode
import timber.log.Timber

@Composable
fun GuiV2NavGraph(
    navController: NavHostController,
    startBusinessId: Long = 1L,
    onSwitchToGui1: () -> Unit = {},
    uiMode: UIMode = UIMode.MODERN,
    onUIModeChange: (UIMode) -> Unit = {}
) {
    NavHost(
        navController = navController,
        startDestination = ScreenV2.Dashboard(businessId = startBusinessId),
        modifier = Modifier
    ) {
        composable<ScreenV2.Dashboard> { backStackEntry ->
            val route: ScreenV2.Dashboard = backStackEntry.toRoute()
            Column {
                SyncStatusIndicator()
                DashboardScreenV2(
                    businessId = route.businessId,
                    navController = navController,
                    onNavigateToRevenue = { navController.navigate(ScreenV2.RevenueAnalytics(route.businessId)) },
                    onNavigateToPayment = { navController.navigate(ScreenV2.PaymentAnalytics(route.businessId)) },
                    onNavigateToRisk = { navController.navigate(ScreenV2.RiskAnalytics(route.businessId)) },
                    onNavigateToCustomers = { navController.navigate(ScreenV2.Customers(route.businessId)) },
                    onNavigateToInvoices = { navController.navigate(ScreenV2.Invoices(route.businessId)) },
                    onNavigateToInvoice = { invoiceId -> navController.navigate(ScreenV2.InvoiceDetail(route.businessId, invoiceId)) },
                    onNavigateToInvoiceAnalytics = { navController.navigate(ScreenV2.InvoiceAnalytics(route.businessId)) },
                    onNavigateToDunningNotices = { navController.navigate(ScreenV2.DunningNotices(route.businessId)) },
                    onNavigateToVault = { navController.navigate(ScreenV2.Vault(route.businessId)) },
                    onNavigateToNotes = {
                        // ✅ FIX #1: Notes navigation - bridge to GUI1 Notes screen
                        // Notes is currently GUI1-only, so we navigate using Screen.Notes
                        // This allows GUI2 users to access notes functionality
                        try {
                            navController.navigate(Screen.Notes)
                            Timber.d("Navigating to Notes screen from GUI2")
                        } catch (e: IllegalArgumentException) {
                            Timber.e(e, "Failed to navigate to Notes screen")
                        }
                    },
                    onCreateCustomer = { navController.navigate(ScreenV2.CreateCustomer(route.businessId)) },
                    onCreateInvoice = { navController.navigate(ScreenV2.CreateInvoice(route.businessId)) },
                    onNavigateToSettings = { navController.navigateToSettingsV2(route.businessId) },
                    onSwitchToGui1 = onSwitchToGui1,
                    uiMode = uiMode
                )
            }
        }

        composable<ScreenV2.Invoices> { backStackEntry ->
            val route: ScreenV2.Invoices = backStackEntry.toRoute()
            InvoiceListScreenV2(
                businessId = route.businessId,
                navController = navController,
                uiMode = uiMode
            )
        }

        composable<ScreenV2.InvoiceDetail> { backStackEntry ->
            val route: ScreenV2.InvoiceDetail = backStackEntry.toRoute()
            InvoiceDetailScreenV2(
                businessId = route.businessId,
                invoiceId = route.invoiceId,
                onBack = { navController.popBackStack() },
                onNavigateToVault = { navController.navigate(ScreenV2.Vault(route.businessId)) }
            )
        }

        composable<ScreenV2.CreateInvoice> { backStackEntry ->
            val route: ScreenV2.CreateInvoice = backStackEntry.toRoute()
            CreateInvoiceScreenV2(
                businessId = route.businessId,
                onBack = { navController.popBackStack() },
                onCreate = { navController.popBackStack() }
            )
        }

        composable<ScreenV2.EditInvoice> { backStackEntry ->
            val route: ScreenV2.EditInvoice = backStackEntry.toRoute()
            EditInvoiceScreenV2(
                businessId = route.businessId,
                invoiceId = route.invoiceId,
                onBack = { navController.popBackStack() },
                onUpdate = { navController.popBackStack() }
            )
        }

        composable<ScreenV2.Customers> { backStackEntry ->
            val route: ScreenV2.Customers = backStackEntry.toRoute()
            CustomerListScreen(
                businessId = route.businessId,
                onCustomerClick = { customerId ->
                    navController.navigate(ScreenV2.CustomerDetail(route.businessId, customerId))
                },
                onCreateCustomer = {
                    navController.navigate(ScreenV2.CreateCustomer(route.businessId))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable<ScreenV2.CustomerDetail> { backStackEntry ->
            val route: ScreenV2.CustomerDetail = backStackEntry.toRoute()
            CustomerDetailScreen(
                customerId = route.customerId,
                businessId = route.businessId,
                onEdit = { navController.popBackStack() },
                onBack = { navController.popBackStack() },
                onNavigateToEdit = { customerId ->
                    navController.navigate(ScreenV2.EditCustomer(route.businessId, customerId))
                }
            )
        }

        composable<ScreenV2.CreateCustomer> { backStackEntry ->
            val route: ScreenV2.CreateCustomer = backStackEntry.toRoute()
            CreateCustomerScreenV2(
                businessId = route.businessId,
                navController = navController
            )
        }

        composable<ScreenV2.EditCustomer> { backStackEntry ->
            val route: ScreenV2.EditCustomer = backStackEntry.toRoute()
            EditCustomerScreenV2(
                businessId = route.businessId,
                customerId = route.customerId,
                onBack = { navController.popBackStack() },
                onUpdate = { navController.popBackStack() }
            )
        }

        composable<ScreenV2.RevenueAnalytics> { backStackEntry ->
            val route: ScreenV2.RevenueAnalytics = backStackEntry.toRoute()
            RevenueAnalyticsScreenV2(
                businessId = route.businessId,
                navController = navController
            )
        }

        composable<ScreenV2.PaymentAnalytics> { backStackEntry ->
            val route: ScreenV2.PaymentAnalytics = backStackEntry.toRoute()
            PaymentAnalyticsScreenV2(
                businessId = route.businessId,
                navController = navController
            )
        }

        composable<ScreenV2.RiskAnalytics> { backStackEntry ->
            val route: ScreenV2.RiskAnalytics = backStackEntry.toRoute()
            RiskAnalyticsScreenV2(
                businessId = route.businessId,
                navController = navController
            )
        }

        composable<ScreenV2.Settings> { backStackEntry ->
            val route: ScreenV2.Settings = backStackEntry.toRoute()
            SettingsHubScreenV2(
                onBusinessProfileClick = {
                    navController.navigateToBusinessProfileV2(route.businessId)
                },
                onAppAppearanceClick = {
                    navController.navigate(ScreenV2.AppAppearance(route.businessId))
                },
                onInvoiceSettingsClick = {
                    navController.navigateToInvoiceSettingsV2(route.businessId)
                },
                onHelpClick = {
                    navController.navigateToHelpV2(route.businessId)
                },
                onRiskDashboardClick = {
                    navController.navigateToRiskAnalyticsV2(route.businessId)
                },
                onPaymentAnalyticsClick = {
                    navController.navigateToPaymentAnalyticsV2(route.businessId)
                },
                onRevenueDashboardClick = {
                    navController.navigateToRevenueAnalyticsV2(route.businessId)
                },
                onDunningNoticesClick = {
                    navController.navigateToDunningNoticesV2(route.businessId)
                },
                onPrefilledItemsClick = {
                    navController.navigateToPrefilledItemsV2(route.businessId)
                },
                onBackupRestoreClick = {
                    navController.navigate(ScreenV2.BackupRestore(route.businessId))
                },
                currentUIMode = uiMode,
                onUIModeChange = onUIModeChange,
                onBack = { navController.popBackStack() }
            )
        }

        composable<ScreenV2.AppAppearance> { backStackEntry ->
            val route: ScreenV2.AppAppearance = backStackEntry.toRoute()
            AppAppearanceScreenV2(
                businessId = route.businessId,
                onBack = { navController.popBackStack() },
                onBusinessProfileClick = { navController.navigateToBusinessProfileV2(route.businessId) },
                onThemeSettingsClick = { navController.navigate(ScreenV2.ThemeSettings(route.businessId)) },
                onHelpClick = { navController.navigate(ScreenV2.Help(route.businessId)) },
                onBackupRestoreClick = { navController.navigate(ScreenV2.BackupRestore(route.businessId)) }
            )
        }

        composable<ScreenV2.ThemeSettings> { backStackEntry ->
            val route: ScreenV2.ThemeSettings = backStackEntry.toRoute()
            com.emul8r.bizap.ui.theme.UnifiedThemeSettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable<ScreenV2.InvoiceCustomization> { backStackEntry ->
            val route: ScreenV2.InvoiceCustomization = backStackEntry.toRoute()
            InvoiceCustomizationSettingsScreenV2(
                onBack = { navController.popBackStack() }
            )
        }

        composable<ScreenV2.InvoiceSettings> { backStackEntry ->
            val route: ScreenV2.InvoiceSettings = backStackEntry.toRoute()
            InvoiceSettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable<ScreenV2.BusinessProfile> {
            BusinessProfileScreen(
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

        composable<ScreenV2.AnalyticsFocusedInsights> { backStackEntry ->
            val route: ScreenV2.AnalyticsFocusedInsights = backStackEntry.toRoute()
            AnalyticsFocusedInsightsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable<ScreenV2.Vault> { backStackEntry ->
            val route: ScreenV2.Vault = backStackEntry.toRoute()
            DocumentVaultScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // HelpScreen is shared between GUI1 and GUI2.
        composable<ScreenV2.Help> { backStackEntry ->
            val route: ScreenV2.Help = backStackEntry.toRoute()
            HelpScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable<ScreenV2.DunningNotices> { backStackEntry ->
            val route: ScreenV2.DunningNotices = backStackEntry.toRoute()
            DunningNoticesScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<ScreenV2.PrefilledItems> { backStackEntry ->
            val route: ScreenV2.PrefilledItems = backStackEntry.toRoute()
            PrefilledItemsScreen()
        }

        composable<ScreenV2.BackupRestore> { backStackEntry ->
            val route: ScreenV2.BackupRestore = backStackEntry.toRoute()
            BackupRestoreScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable<ScreenV2.Notes> { backStackEntry ->
            val route: ScreenV2.Notes = backStackEntry.toRoute()
            NotesScreen(
                navController = navController
            )
        }
    }
}
