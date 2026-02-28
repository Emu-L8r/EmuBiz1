package com.example.databaser

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.databaser.ui.AddEditInvoiceScreen
import com.example.databaser.ui.AllInvoicesScreen
import com.example.databaser.ui.PrintPreviewScreen
import com.example.databaser.viewmodel.InvoiceViewModel

fun NavGraphBuilder.invoiceNavGraph(navController: NavHostController) {
    navigation(startDestination = Screen.AllInvoices.route, route = "invoices") {
        composable(Screen.AllInvoices.route) {
            AllInvoicesScreen(
                onInvoiceClick = { invoiceId, customerId ->
                    navController.navigate("addeditinvoice?invoiceId=$invoiceId&customerId=$customerId")
                },
                onAddInvoiceClick = { navController.navigate("addeditinvoice") },
                navController = navController,
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(
            "addeditinvoice?invoiceId={invoiceId}&customerId={customerId}",
            arguments = listOf(
                navArgument("invoiceId") {
                    type = NavType.IntType
                    defaultValue = -1
                },
                navArgument("customerId") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStackEntry ->
            val customerIdFromState by backStackEntry.savedStateHandle.getLiveData<Int>("customerId").observeAsState()
            val customerIdFromArgs = backStackEntry.arguments?.getInt("customerId")
            AddEditInvoiceScreen(
                invoiceId = backStackEntry.arguments?.getInt("invoiceId"),
                customerId = customerIdFromState ?: customerIdFromArgs,
                onInvoiceSaved = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() },
                navController = navController
            )
        }
        composable(
            Screen.PrintPreview.route,
            arguments = listOf(navArgument(Screen.INVOICE_ID) { type = NavType.IntType })
        ) { backStackEntry ->
            val parentEntry = remember(backStackEntry) { navController.getBackStackEntry("invoices") }
            val invoiceViewModel: InvoiceViewModel = hiltViewModel(parentEntry)
            PrintPreviewScreen(
                invoiceId = backStackEntry.arguments?.getInt(Screen.INVOICE_ID) ?: 0,
                onBackClick = { navController.popBackStack() },
                invoiceViewModel = invoiceViewModel
            )
        }
    }
}