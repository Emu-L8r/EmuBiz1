package com.example.databaser

import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val icon: ImageVector? = null,
    @field:StringRes val resourceId: Int? = null
) {
    object Dashboard : Screen("dashboard", Icons.Default.Home, R.string.dashboard)
    object CustomerList : Screen("customerlist", Icons.Default.Person, R.string.customer_list)
    object AddEditCustomer : Screen("addeditcustomer?customerId={customerId}") {
        fun createRoute(customerId: Int?) = "addeditcustomer?customerId=$customerId"
    }
    object AllInvoices : Screen("allinvoices", Icons.Default.DateRange, R.string.all_invoices)
    object AddInvoice : Screen("addinvoice/{customerId}") {
        fun createRoute(customerId: Int) = "addinvoice/$customerId"
    }
    object EditInvoice : Screen("editinvoice/{invoiceId}") {
        fun createRoute(invoiceId: Int) = "editinvoice/$invoiceId"
    }
    object AddLineItem : Screen("addlineitem/{invoiceId}") {
        fun createRoute(invoiceId: Int) = "addlineitem/$invoiceId"
    }
    object EditLineItem : Screen("editlineitem/{lineItemId}") {
        fun createRoute(lineItemId: Int) = "editlineitem/$lineItemId"
    }
    object PrintPreview : Screen("printpreview/{invoiceId}") {
        fun createRoute(invoiceId: Int): String {
            return "printpreview/$invoiceId"
        }
    }
    object Settings : Screen("settings")
    object MySaves : Screen("mysaves")
    object BusinessInfo : Screen("businessinfo")
    object ManagePredefinedLineItems: Screen("managepredefinedlineitems")
    object AddPredefinedLineItem: Screen("addpredefinedlineitem")
    object Trash: Screen("trash")
    object AllQuotes: Screen("allquotes", Icons.Default.Build, R.string.all_quotes)
    object AddEditQuote: Screen("addeditquote?quoteId={quoteId}&customerId={customerId}"){
        fun createRoute(quoteId: Int?, customerId: Int?) = "addeditquote?quoteId=$quoteId&customerId=$customerId"
    }
    object Notes: Screen("notes")
    object AddEditNote: Screen("addeditnote?noteId={noteId}&customerId={customerId}&invoiceId={invoiceId}&quoteId={quoteId}"){
        fun createRoute(noteId: Int?, customerId: Int?, invoiceId: Int?, quoteId: Int?) = "addeditnote?noteId=$noteId&customerId=$customerId&invoiceId=$invoiceId&quoteId=$quoteId"
    }

    companion object {
        const val CUSTOMER_ID = "customerId"
        const val INVOICE_ID = "invoiceId"
        const val LINE_ITEM_ID = "lineItemId"
        const val QUOTE_ID = "quoteId"
        const val NOTE_ID = "noteId"
    }
}
