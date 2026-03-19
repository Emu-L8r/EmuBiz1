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
import com.emul8r.bizap.ui.gui2.analytics.InvoiceAnalyticsScreenV2
import com.emul8r.bizap.ui.gui2.analytics.PaymentAnalyticsScreenV2
import com.emul8r.bizap.ui.gui2.analytics.RevenueAnalyticsScreenV2
import com.emul8r.bizap.ui.gui2.analytics.RiskAnalyticsScreenV2
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
import com.emul8r.bizap.ui.settings.backup.BackupRestoreScreen
import com.emul8r.bizap.ui.settings.BusinessProfileScreen
import com.emul8r.bizap.ui.settings.PrefilledItemsScreen
import com.emul8r.bizap.ui.settings.SettingsScreen as AppSettingsScreenV2
import com.emul8r.bizap.ui.shared.screens.HelpScreen
import com.emul8r.bizap.ui.templates.CreateTemplateScreen
import com.emul8r.bizap.ui.templates.EditTemplateScreen
import com.emul8r.bizap.ui.templates.TemplateListScreen

@Composable
fun GuiV2NavGraph(
    navController: NavHostController,
    onSwitchToGui1: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = ScreenV2.Dashboard(businessId = 1L),
        modifier = Modifier
    ) {
        composable<ScreenV2.Dashboard> { backStackEntry ->
            val route: ScreenV2.Dashboard = backStackEntry.toRoute()
            Column {
                SyncStatusIndicator()
                DashboardScreenV2(
                    businessId = route.businessId,
                    onNavigateToInvoices = { navController.navigate(ScreenV2.Invoices(route.businessId)) },
                    onNavigateToCustomers = { navController.navigate(ScreenV2.Customers(route.businessId)) },
                    onNavigateToSettings = { navController.navigateToSettingsV2(route.businessId) },
                    onNavigateToAnalytics = { navController.navigate(ScreenV2.RiskAnalytics(route.businessId)) }
                )
            }
        }

        composable<ScreenV2.Invoices> { backStackEntry ->
            val route: ScreenV2.Invoices = backStackEntry.toRoute()
            InvoiceListScreenV2(
                businessId = route.businessId,
                onNavigateToDetail = { invoiceId ->
                    navController.navigate(ScreenV2.InvoiceDetail(route.businessId, invoiceId))
                },
                onNavigateToCreate = {
                    navController.navigate(ScreenV2.CreateInvoice(route.businessId))
                },
                onNavigateToEdit = { invoiceId ->
                    navController.navigate(ScreenV2.EditInvoice(route.businessId, invoiceId))
                }
            )
        }

        composable<ScreenV2.InvoiceDetail> { backStackEntry ->
            val route: ScreenV2.InvoiceDetail = backStackEntry.toRoute()
            InvoiceDetailScreenV2(
                businessId = route.businessId,
                invoiceId = route.invoiceId,
                onBack = { navController.popBackStack() },
                onNavigateToEdit = {
                    navController.navigate(ScreenV2.EditInvoice(route.businessId, route.invoiceId))
                }
            )
        }

        composable<ScreenV2.CreateInvoice> { backStackEntry ->
            val route: ScreenV2.CreateInvoice = backStackEntry.toRoute()
            CreateInvoiceScreenV2(
                businessId = route.businessId,
                onBack = { navController.popBackStack() },
                onInvoiceCreated = { navController.popBackStack() }
            )
        }

        composable<ScreenV2.EditInvoice> { backStackEntry ->
            val route: ScreenV2.EditInvoice = backStackEntry.toRoute()
            EditInvoiceScreenV2(
                businessId = route.businessId,
                invoiceId = route.invoiceId,
                onBack = { navController.popBackStack() },
                onInvoiceUpdated = { navController.popBackStack() }
            )
        }

        composable<ScreenV2.Customers> { backStackEntry ->
            val route: ScreenV2.Customers = backStackEntry.toRoute()
            CustomerListScreen(
                guiMode = GuiMode.GUI2,
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
                guiMode = GuiMode.GUI2,
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
                onBack = { navController.popBackStack() },
                onCustomerCreated = { navController.popBackStack() }
            )
        }

        composable<ScreenV2.EditCustomer> { backStackEntry ->
            val route: ScreenV2.EditCustomer = backStackEntry.toRoute()
            EditCustomerScreenV2(
                businessId = route.businessId,
                customerId = route.customerId,
                onBack = { navController.popBackStack() },
                onCustomerUpdated = { navController.popBackStack() }
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

        composable<ScreenV2.Settings> { backStackEntry ->
            val route: ScreenV2.Settings = backStackEntry.toRoute()
            SettingsHubScreenV2(
                onBusinessProfileClick = {
                    navController.navigateToBusinessProfileV2(route.businessId)
                },
                onAppAppearanceClick = {
                    navController.navigateToAppAppearanceV2(route.businessId)
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
                onInvoiceTemplatesClick = {
                    navController.navigateToInvoiceTemplatesV2(route.businessId)
                },
                onBackupRestoreClick = {
                    navController.navigateToBackupRestoreV2(route.businessId)
                },
                onBack = { navController.popBackStack() },
                onSwitchToGui1 = onSwitchToGui1
            )
        }

        composable<ScreenV2.AppAppearance> { backStackEntry ->
            val route: ScreenV2.AppAppearance = backStackEntry.toRoute()
            AppAppearanceScreenV2(
                businessId = route.businessId,
                onBack = { navController.popBackStack() }
            )
        }

        composable<ScreenV2.BusinessProfile> {
            BusinessProfileScreen(
                guiMode = GuiMode.GUI2,
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

        // HelpScreen is shared between GUI1 and GUI2.
        composable<ScreenV2.Help> {
            HelpScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable<ScreenV2.DunningNotices> {
            DunningNoticesScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<ScreenV2.PrefilledItems> {
            PrefilledItemsScreen()
        }

        composable<ScreenV2.InvoiceTemplates> { backStackEntry ->
            val route: ScreenV2.InvoiceTemplates = backStackEntry.toRoute()
            TemplateListScreen(
                businessProfileId = route.businessProfileId,
                onNavigateToCreate = { bpId ->
                    navController.navigate(ScreenV2.CreateTemplate(route.businessId, bpId))
                },
                onNavigateToEdit = { templateId ->
                    navController.navigate(ScreenV2.EditTemplate(route.businessId, templateId))
                }
            )
        }

        composable<ScreenV2.CreateTemplate> { backStackEntry ->
            val route: ScreenV2.CreateTemplate = backStackEntry.toRoute()
            CreateTemplateScreen(
                businessProfileId = route.businessProfileId,
                onNavigateBack = { navController.popBackStack() },
                onTemplateCreated = { navController.popBackStack() }
            )
        }

        composable<ScreenV2.EditTemplate> { backStackEntry ->
            val route: ScreenV2.EditTemplate = backStackEntry.toRoute()
            EditTemplateScreen(
                templateId = route.templateId,
                onNavigateBack = { navController.popBackStack() },
                onTemplateUpdated = { navController.popBackStack() }
            )
        }

        composable<ScreenV2.BackupRestore> {
            BackupRestoreScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}

// ── Navigation Extension Functions ──────────────────────────────────────

fun NavHostController.navigateToSettingsV2(businessId: Long) {
    navigate(ScreenV2.Settings(businessId))
}

fun NavHostController.navigateToAppAppearanceV2(businessId: Long) {
    navigate(ScreenV2.AppAppearance(businessId))
}

fun NavHostController.navigateToBusinessProfileV2(businessId: Long) {
    navigate(ScreenV2.BusinessProfile(businessId))
}

fun NavHostController.navigateToHelpV2(businessId: Long) {
    navigate(ScreenV2.Help(businessId))
}

fun NavHostController.navigateToRiskAnalyticsV2(businessId: Long) {
    navigate(ScreenV2.RiskAnalytics(businessId))
}

fun NavHostController.navigateToPaymentAnalyticsV2(businessId: Long) {
    navigate(ScreenV2.PaymentAnalytics(businessId))
}

fun NavHostController.navigateToRevenueAnalyticsV2(businessId: Long) {
    navigate(ScreenV2.RevenueAnalytics(businessId))
}

fun NavHostController.navigateToDunningNoticesV2(businessId: Long) {
    navigate(ScreenV2.DunningNotices(businessId))
}

fun NavHostController.navigateToPrefilledItemsV2(businessId: Long) {
    navigate(ScreenV2.PrefilledItems(businessId))
}

fun NavHostController.navigateToInvoiceTemplatesV2(businessId: Long) {
    navigate(ScreenV2.InvoiceTemplates(businessId))
}

fun NavHostController.navigateToBackupRestoreV2(businessId: Long) {
    navigate(ScreenV2.BackupRestore(businessId))
}
