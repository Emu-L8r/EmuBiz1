package com.example.databaser

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

public sealed class Screen(
    val route: String,
    val resourceId: Int? = null,
    val icon: ImageVector? = null
) {
    object Dashboard : Screen("dashboard", R.string.dashboard, Icons.Filled.Dashboard)
    object CustomerList : Screen("customers", R.string.customers, Icons.Filled.People)
    object AllInvoices : Screen("invoices", R.string.invoices, Icons.Filled.Receipt)
    object AllQuotes : Screen("quotes", R.string.quotes, Icons.Filled.Receipt)
    object MySaves : Screen("mySaves", R.string.my_saves, Icons.Filled.Folder)
    object Settings : Screen("settings", R.string.settings, Icons.Filled.Settings)
    object Notes : Screen("notes", R.string.notes, Icons.Filled.Note)
    object BusinessInfo : Screen("businessInfo", R.string.business_info, Icons.Filled.Business)

    // Other screens without bottom nav presence
    object ManagePredefinedLineItems : Screen("managePredefinedLineItems")
    object AddPredefinedLineItem : Screen("addPredefinedLineItem")
    object Trash : Screen("trash")

    companion object {
        const val CUSTOMER_ID = "customerId"
        const val INVOICE_ID = "invoiceId"
        const val LINE_ITEM_ID = "lineItemId"
        const val NOTE_ID = "noteId"
    }

    object AddInvoice : Screen("addInvoice/{$CUSTOMER_ID}") {
        fun createRoute(customerId: Int) = "addInvoice/$customerId"
    }

    object AddEditCustomer : Screen("addEditCustomer?$CUSTOMER_ID={$CUSTOMER_ID}") {
        fun createRoute(customerId: Int?) = if (customerId != null) "addEditCustomer?$CUSTOMER_ID=$customerId" else "addEditCustomer"
    }

    object EditInvoice : Screen("editInvoice/{$INVOICE_ID}") {
        fun createRoute(invoiceId: Int) = "editInvoice/$invoiceId"
    }

    object EditLineItem : Screen("editLineItem/{$LINE_ITEM_ID}") {
        fun createRoute(lineItemId: Int) = "editLineItem/$lineItemId"
    }

    object AddLineItem : Screen("addLineItem/{$INVOICE_ID}") {
        fun createRoute(invoiceId: Int) = "addLineItem/$invoiceId"
    }

    object PrintPreview : Screen("printPreview/{$INVOICE_ID}") {
        fun createRoute(invoiceId: Int) = "printPreview/$invoiceId"
    }

    object AddEditNote : Screen("addEditNote?$NOTE_ID={$NOTE_ID}&$CUSTOMER_ID={$CUSTOMER_ID}&$INVOICE_ID={$INVOICE_ID}") {
        fun createRoute(noteId: Int?, customerId: Int?, invoiceId: Int?): String {
            val route = "addEditNote"
            val args = mutableListOf<String>()
            noteId?.let { args.add("$NOTE_ID=$it") }
            customerId?.let { args.add("$CUSTOMER_ID=$it") }
            invoiceId?.let { args.add("$INVOICE_ID=$it") }
            return if (args.isNotEmpty()) "$route?${args.joinToString("&")}" else route
        }
    }
}
