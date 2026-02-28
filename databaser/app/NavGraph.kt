package com.example.databaser

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.databaser.ui.AddEditCustomerScreen
import com.example.databaser.ui.AddEditNoteScreen
import com.example.databaser.ui.AddEditQuoteScreen
import com.example.databaser.ui.AddInvoiceScreen
import com.example.databaser.ui.AddLineItemScreen
import com.example.databaser.ui.AddPredefinedLineItemScreen
import com.example.databaser.ui.AllQuotesScreen
import com.example.databaser.ui.BusinessInfoScreen
import com.example.databaser.ui.CustomerListScreen
import com.example.databaser.ui.DashboardScreen
import com.example.databaser.ui.EditLineItemScreen
import com.example.databaser.ui.ManagePredefinedLineItemsScreen
import com.example.databaser.ui.MySavesScreen
import com.example.databaser.ui.NoteListScreen
import com.example.databaser.ui.SettingsScreen
import com.example.databaser.ui.TrashScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    windowSizeClass: WindowSizeClass
) {
    NavHost(navController = navController, startDestination = Screen.Dashboard.route, modifier = modifier) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onUnsentInvoicesClick = { navController.navigate("invoices") },
                onSentInvoicesClick = { navController.navigate("invoices") },
                onUnsentQuotesClick = { navController.navigate(Screen.AllQuotes.route) },
                onSentQuotesClick = { navController.navigate(Screen.AllQuotes.route) },
                onNotesClick = { navController.navigate(Screen.Notes.route) },
                navController = navController
            )
        }
        composable(Screen.CustomerList.route) {
            CustomerListScreen(
                onCustomerClick = { customerId ->
                    navController.navigate(Screen.AddEditCustomer.createRoute(customerId))
                },
                onAddCustomerClick = { navController.navigate(Screen.AddEditCustomer.createRoute(null)) },
                navController = navController,
                onBackClick = { navController.popBackStack() }
            )
        }
        invoiceNavGraph(navController)
        composable(Screen.AllQuotes.route) {
            AllQuotesScreen(
                onQuoteClick = { quoteId, customerId ->
                    navController.navigate("addeditquote?quoteId=$quoteId&customerId=$customerId")
                },
                onAddQuoteClick = { customerId -> navController.navigate("addeditquote?customerId=$customerId") },
                navController = navController,
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(Screen.MySaves.route) {
            MySavesScreen(navController = navController)
        }
        composable(Screen.BusinessInfo.route) {
            BusinessInfoScreen(
                navController = navController,
                onManagePredefinedLineItems = { navController.navigate(Screen.ManagePredefinedLineItems.route) },
                onTrashClick = { navController.navigate(Screen.Trash.route) },
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(
            Screen.AddInvoice.route,
            arguments = listOf(navArgument(Screen.CUSTOMER_ID) { type = NavType.IntType })
        ) { backStackEntry ->
            AddInvoiceScreen(
                customerId = backStackEntry.arguments?.getInt(Screen.CUSTOMER_ID) ?: 0,
                onInvoiceAdded = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(
            "addeditquote?quoteId={quoteId}&customerId={customerId}",
            arguments = listOf(
                navArgument("quoteId") { 
                    type = NavType.IntType
                    defaultValue = -1
                },
                navArgument("customerId") { 
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStackEntry ->
            AddEditQuoteScreen(
                quoteId = backStackEntry.arguments?.getInt("quoteId"),
                customerId = backStackEntry.arguments?.getInt("customerId"),
                onQuoteSaved = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() },
                navController = navController
            )
        }
        composable(
            Screen.AddEditCustomer.route,
            arguments = listOf(
                navArgument(Screen.CUSTOMER_ID) {
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) { backStackEntry ->
            AddEditCustomerScreen(
                customerId = backStackEntry.arguments?.getString(Screen.CUSTOMER_ID)?.toIntOrNull(),
                onCustomerSaved = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() },
                navController = navController
            )
        }

        composable(
            Screen.EditLineItem.route,
            arguments = listOf(navArgument(Screen.LINE_ITEM_ID) { type = NavType.IntType })
        ) { backStackEntry ->
            EditLineItemScreen(
                lineItemId = backStackEntry.arguments?.getInt(Screen.LINE_ITEM_ID) ?: 0,
                onLineItemUpdated = { navController.popBackStack() },
                navController = navController
            )
        }
        composable(
            Screen.AddLineItem.route,
            arguments = listOf(navArgument(Screen.INVOICE_ID) { type = NavType.IntType })
        ) { backStackEntry ->
            AddLineItemScreen(
                invoiceId = backStackEntry.arguments?.getInt(Screen.INVOICE_ID) ?: 0,
                onAdd = {
                    navController.previousBackStackEntry?.savedStateHandle?.set("item_added", true)
                    navController.popBackStack()
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                navController = navController,
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(Screen.ManagePredefinedLineItems.route) {
            ManagePredefinedLineItemsScreen(
                onAddClick = { navController.navigate(Screen.AddPredefinedLineItem.route) },
                onBackClick = { navController.popBackStack() },
                navController = navController
            )
        }
        composable(Screen.AddPredefinedLineItem.route) {
            AddPredefinedLineItemScreen(
                onItemSaved = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(Screen.Trash.route) {
            TrashScreen()
        }
        composable(Screen.Notes.route) {
            NoteListScreen(
                onAddNoteClick = { navController.navigate(Screen.AddEditNote.createRoute(null, null, null, null)) },
                onNoteClick = { noteId -> navController.navigate(Screen.AddEditNote.createRoute(noteId, null, null, null)) },
                onBackClick = { navController.popBackStack() },
                navController = navController
            )
        }
        composable(
            Screen.AddEditNote.route,
            arguments = listOf(
                navArgument(Screen.NOTE_ID) { 
                    type = NavType.IntType
                    defaultValue = -1
                },
                navArgument(Screen.CUSTOMER_ID) {
                    type = NavType.IntType
                    defaultValue = -1
                },
                navArgument(Screen.INVOICE_ID) {
                    type = NavType.IntType
                    defaultValue = -1
                },
                navArgument(Screen.QUOTE_ID) {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStackEntry ->
            AddEditNoteScreen(
                noteId = backStackEntry.arguments?.getInt(Screen.NOTE_ID)?.takeIf { it != -1 },
                customerId = backStackEntry.arguments?.getInt(Screen.CUSTOMER_ID)?.takeIf { it != -1 },
                invoiceId = backStackEntry.arguments?.getInt(Screen.INVOICE_ID)?.takeIf { it != -1 },
                quoteId = backStackEntry.arguments?.getInt(Screen.QUOTE_ID)?.takeIf { it != -1 },
                onNoteSaved = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() },
                navController = navController
            )
        }
    }
}