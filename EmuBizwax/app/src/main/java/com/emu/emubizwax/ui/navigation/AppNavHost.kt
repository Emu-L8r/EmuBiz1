package com.emu.emubizwax.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.emu.emubizwax.ui.customers.CustomerFormScreen
import com.emu.emubizwax.ui.customers.CustomerListScreen
import com.emu.emubizwax.ui.dashboard.DashboardScreen
import com.emu.emubizwax.ui.invoices.InvoiceDetailsScreen
import com.emu.emubizwax.ui.invoices.InvoiceFormScreen
import com.emu.emubizwax.ui.invoices.InvoiceListScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Dashboard,
        modifier = modifier
    ) {
        composable<Dashboard> {
            DashboardScreen(
                onNavigateToCustomers = { navController.navigate(Customers) },
                onNavigateToInvoices = { navController.navigate(Invoices) }
            )
        }

        composable<Customers> {
            CustomerListScreen(
                onCustomerClick = { id -> navController.navigate(EditCustomer(id)) },
                onAddCustomer = { navController.navigate(AddCustomer) }
            )
        }

        composable<Invoices> {
            InvoiceListScreen(
                onInvoiceClick = { id -> navController.navigate(InvoiceDetails(id)) },
                onAddInvoice = { navController.navigate(AddInvoice) }
            )
        }

        composable<AddCustomer> {
            CustomerFormScreen(
                onSaveComplete = { navController.popBackStack() },
                onNavigateUp = { navController.navigateUp() }
            )
        }

        composable<EditCustomer> {
            CustomerFormScreen(
                onSaveComplete = { navController.popBackStack() },
                onNavigateUp = { navController.navigateUp() }
            )
        }

        composable<InvoiceDetails> { backStackEntry ->
            val details: InvoiceDetails = backStackEntry.toRoute()
            InvoiceDetailsScreen(invoiceId = details.invoiceId)
        }

        composable<AddInvoice> {
            InvoiceFormScreen(onSaveComplete = { navController.popBackStack() })
        }

        composable<EditInvoice> {
            InvoiceFormScreen(onSaveComplete = { navController.popBackStack() })
        }
    }
}
