package com.emul8r.bizap.ui.gui2.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.emul8r.bizap.ui.gui2.analytics.PaymentAnalyticsScreenV2
import com.emul8r.bizap.ui.gui2.analytics.RevenueAnalyticsScreenV2
import com.emul8r.bizap.ui.gui2.analytics.RiskAnalyticsScreenV2
import com.emul8r.bizap.ui.gui2.dashboard.DashboardScreenV2
import com.emul8r.bizap.ui.gui2.invoice.InvoiceDetailScreenV2

/**
 * Navigation graph for GUI2.
 * businessId is passed explicitly as a route parameter to every screen.
 *
 * @param navController  The NavHostController for GUI2.
 * @param startBusinessId  The businessId to use as the root screen argument.
 * @param onSwitchToGui1  Callback to return to GUI1 (landing screen reset).
 * @param modifier  Optional modifier for the nav host.
 */
@Composable
fun GuiV2NavGraph(
    navController: NavHostController,
    startBusinessId: Long,
    onSwitchToGui1: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = ScreenV2.Dashboard(startBusinessId),
        modifier = modifier
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
    }
}
