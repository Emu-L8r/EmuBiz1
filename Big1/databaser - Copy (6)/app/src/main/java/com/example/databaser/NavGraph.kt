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
import com.example.databaser.ui.AddEditInvoiceScreen
import com.example.databaser.ui.AddEditNoteScreen
import com.example.databaser.ui.AddEditQuoteScreen
import com.example.databaser.ui.AddInvoiceScreen
import com.example.databaser.ui.AddLineItemScreen
import com.example.databaser.ui.AddPredefinedLineItemScreen
import com.example.databaser.ui.AllInvoicesScreen
import com.example.databaser.ui.AllQuotesScreen
import com.example.databaser.ui.CustomerListScreen
import com.example.databaser.ui.DashboardScreen
import com.example.databaser.ui.EditInvoiceScreen
import com.example.databaser.ui.EditLineItemScreen
import com.example.databaser.ui.ManagePredefinedLineItemsScreen
import com.example.databaser.ui.MySavesScreen
import com.example.databaser.ui.NoteListScreen
import com.example.databaser.ui.PrintPreviewScreen
import com.example.databaser.ui.SettingsScreen
import com.example.databaser.ui.TrashScreen

@Composable
fun NavGraph(navController: NavHostController, modifier: Modifier = Modifier, windowSizeClass: WindowSizeClass) {
    NavHost(navController = navController, startDestination = Screen.Dashboard.route, modifier = modifier) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onUnsentInvoicesClick = { navController.navigate(Screen.AllInvoices.route) },
                onSentInvoicesClick = { navController.navigate(Screen.AllInvoices.route) },
                onUnsentQuotesClick = { navController.navigate(Screen.AllQuotes.route) },
                onSentQuotesClick = { navController.navigate(Screen.AllQuotes.route) },
                onNotesClick = { navController.navigate(Screen.Notes.route) },
                onNavigate = { route -> navController.navigate(route) }
            )
        }
        composable(Screen.CustomerList.route) {
            CustomerListScreen(
                onCustomerClick = { customerId ->
                    navController.navigate(Screen.AddInvoice.createRoute(customerId))
                },
                onAddCustomerClick = { navController.navigate(Screen.AddEditCustomer.createRoute(null)) },
                onNavigate = { route -> navController.navigate(route) }
            )
        }
        composable(Screen.AllInvoices.route) {
            AllInvoicesScreen(
                onInvoiceClick = { invoiceId ->
                    navController.navigate(Screen.EditInvoice.createRoute(invoiceId))
                },
                onNavigate = { route -> navController.navigate(route) }
            )
        }
        composable(Screen.AllQuotes.route) {
            AllQuotesScreen(
                onQuoteClick = { quoteId ->
                    navController.navigate(Screen.EditInvoice.createRoute(quoteId))
                },
                onNavigate = { route -> navController.navigate(route) }
            )
        }
        composable(Screen.MySaves.route) {
            MySavesScreen()
        }
        composable(
            Screen.AddInvoice.route,
            arguments = listOf(navArgument(Screen.CUSTOMER_ID) { type = NavType.IntType })
        ) { backStackEntry ->
            AddInvoiceScreen(
                customerId = backStackEntry.arguments?.getInt(Screen.CUSTOMER_ID) ?: 0,
                onInvoiceAdded = { navController.popBackStack() }
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
            AddEditInvoiceScreen(
                invoiceId = backStackEntry.arguments?.getInt("invoiceId"),
                customerId = backStackEntry.arguments?.getInt("customerId"),
                onInvoiceSaved = { navController.popBackStack() },
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
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(
            Screen.AddEditCustomer.route,
            arguments = listOf(navArgument(Screen.CUSTOMER_ID) { 
                type = NavType.IntType
                defaultValue = -1
            })
        ) { backStackEntry ->
            AddEditCustomerScreen(
                customerId = backStackEntry.arguments?.getInt(Screen.CUSTOMER_ID).takeIf { it != -1 },
                onCustomerSaved = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(
            Screen.EditInvoice.route,
            arguments = listOf(navArgument(Screen.INVOICE_ID) { type = NavType.IntType })
        ) { backStackEntry ->
            EditInvoiceScreen(
                invoiceId = backStackEntry.arguments?.getInt(Screen.INVOICE_ID) ?: 0,
                onInvoiceUpdated = { navController.popBackStack() },
                onAddLineItem = { invoiceId ->
                    navController.navigate(Screen.AddLineItem.createRoute(invoiceId))
                },
                onLineEdit = { lineItemId ->
                    navController.navigate(Screen.EditLineItem.createRoute(lineItemId))
                },
                onPrintPreview = { invoiceId ->
                    navController.navigate(Screen.PrintPreview.createRoute(invoiceId))
                }
            )
        }
        composable(
            Screen.EditLineItem.route,
            arguments = listOf(navArgument(Screen.LINE_ITEM_ID) { type = NavType.IntType })
        ) { backStackEntry ->
            EditLineItemScreen(
                lineItemId = backStackEntry.arguments?.getInt(Screen.LINE_ITEM_ID) ?: 0,
                onLineItemUpdated = { navController.popBackStack() }
            )
        }
        composable(
            Screen.AddLineItem.route,
            arguments = listOf(navArgument(Screen.INVOICE_ID) { type = NavType.IntType })
        ) { backStackEntry ->
            AddLineItemScreen(
                onAdd = { navController.popBackStack() }
            )
        }
        composable(
            Screen.PrintPreview.route,
            arguments = listOf(
                navArgument(Screen.INVOICE_ID) { type = NavType.IntType }
            )
        ) { backStackEntry ->
            PrintPreviewScreen(
                invoiceId = backStackEntry.arguments?.getInt(Screen.INVOICE_ID) ?: 0,
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBusinessInfoSaved = { navController.popBackStack() },
                onManagePredefinedLineItems = { navController.navigate(Screen.ManagePredefinedLineItems.route) },
                onTrashClick = { navController.navigate(Screen.Trash.route) }
            )
        }
        composable(Screen.ManagePredefinedLineItems.route) {
            ManagePredefinedLineItemsScreen(onAddClick = { navController.navigate(Screen.AddPredefinedLineItem.route) })
        }
        composable(Screen.AddPredefinedLineItem.route) {
            AddPredefinedLineItemScreen(onItemSaved = { navController.popBackStack() })
        }
        composable(Screen.Trash.route) {
            TrashScreen()
        }
        composable(Screen.Notes.route) {
            NoteListScreen(
                onAddNoteClick = { navController.navigate(Screen.AddEditNote.createRoute(null, null, null)) },
                onNoteClick = { noteId -> navController.navigate(Screen.AddEditNote.createRoute(noteId, null, null)) }
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
                }
            )
        ) { backStackEntry ->
            AddEditNoteScreen(
                noteId = backStackEntry.arguments?.getInt(Screen.NOTE_ID).takeIf { it != -1 },
                customerId = backStackEntry.arguments?.getInt(Screen.CUSTOMER_ID).takeIf { it != -1 },
                invoiceId = backStackEntry.arguments?.getInt(Screen.INVOICE_ID).takeIf { it != -1 },
                onNoteSaved = { navController.popBackStack() }
            )
        }
    }
}
