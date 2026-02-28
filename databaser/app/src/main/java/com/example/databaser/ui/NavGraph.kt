package com.example.databaser.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.databaser.ui.customer.CustomerDetailsScreen
import com.example.databaser.ui.customer.CustomerListScreen
import com.example.databaser.ui.dashboard.DashboardScreen
import com.example.databaser.ui.invoice.InvoiceDetailsScreen
import com.example.databaser.ui.invoice.InvoicesScreen
import com.example.databaser.ui.quote.QuoteDetailsScreen
import com.example.databaser.ui.quote.QuotesScreen

@Composable
fun NavGraph(navController: NavHostController, innerPadding: PaddingValues) {
    NavHost(navController = navController, startDestination = "dashboard", modifier = Modifier.padding(innerPadding)) {
        composable("dashboard") {
            DashboardScreen()
        }
        composable("customers") {
            CustomerListScreen(navController = navController, aFor = "view")
        }
        composable("invoices") {
            InvoicesScreen(navController = navController)
        }
        composable("quotes") {
            QuotesScreen(navController = navController)
        }
        composable(
            route = "customer_details/{customerId}",
            arguments = listOf(navArgument("customerId") { type = NavType.LongType })
        ) {
            backStackEntry ->
            val customerId = backStackEntry.arguments?.getLong("customerId")
            CustomerDetailsScreen(navController = navController, customerId = if(customerId == 0L) null else customerId)
        }
        composable(
            route = "invoice_details/{customerId}/{invoiceId}",
            arguments = listOf(
                navArgument("customerId") { type = NavType.LongType },
                navArgument("invoiceId") { type = NavType.LongType }
            )
        ) {
            backStackEntry ->
            val customerId = backStackEntry.arguments?.getLong("customerId")
            val invoiceId = backStackEntry.arguments?.getLong("invoiceId")
            if (customerId != null) {
                InvoiceDetailsScreen(navController = navController, customerId = customerId, invoiceId = if(invoiceId == 0L) null else invoiceId)
            }
        }
        composable(
            route = "quote_details/{customerId}/{quoteId}",
            arguments = listOf(
                navArgument("customerId") { type = NavType.LongType },
                navArgument("quoteId") { type = NavType.LongType }
            )
        ) {
            backStackEntry ->
            val customerId = backStackEntry.arguments?.getLong("customerId")
            val quoteId = backStackEntry.arguments?.getLong("quoteId")
            if (customerId != null) {
                QuoteDetailsScreen(navController = navController, customerId = customerId, quoteId = if(quoteId == 0L) null else quoteId)
            }
        }
        composable(
            route = "select_customer/{for}",
            arguments = listOf(navArgument("for") { type = NavType.StringType })
        ) {
            backStackEntry ->
            val aFor = backStackEntry.arguments?.getString("for") ?: "view"
            CustomerListScreen(navController = navController, aFor = aFor)
        }
    }
}
