package com.example.databaser.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.databaser.R
import com.example.databaser.data.Note
import com.example.databaser.viewmodel.CustomerViewModel
import com.example.databaser.viewmodel.InvoiceViewModel
import com.example.databaser.viewmodel.NoteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditNoteScreen(
    noteId: Int?,
    customerId: Int?,
    invoiceId: Int?,
    quoteId: Int?,
    onNoteSaved: () -> Unit,
    onBackClick: () -> Unit,
    navController: NavHostController,
    noteViewModel: NoteViewModel = hiltViewModel(),
    customerViewModel: CustomerViewModel = hiltViewModel(),
    invoiceViewModel: InvoiceViewModel = hiltViewModel()
) {
    val note by if (noteId != null) {
        noteViewModel.getNote(noteId).collectAsState(initial = null)
    } else {
        remember { mutableStateOf(null) }
    }

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedCustomerId by remember { mutableStateOf(customerId) }
    var selectedInvoiceId by remember { mutableStateOf(invoiceId) }
    var selectedQuoteId by remember { mutableStateOf(quoteId) }

    if (noteId != null && note != null) {
        title = note!!.title
        content = note!!.content
        selectedCustomerId = note!!.customerId
        selectedInvoiceId = note!!.invoiceId
        selectedQuoteId = note!!.quoteId
    }

    val customers by customerViewModel.allCustomers.collectAsState(initial = emptyList())
    val invoices by invoiceViewModel.uiState.collectAsState()

    var customerExpanded by remember { mutableStateOf(false) }
    var invoiceExpanded by remember { mutableStateOf(false) }
    var quoteExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AppTopAppBar(
                title = if (noteId == null) stringResource(R.string.add_note) else stringResource(R.string.edit_note),
                canNavigateBack = true,
                onNavigateUp = onBackClick,
                navController = navController
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            ThemedTextField(
                value = title,
                onValueChange = { title = it },
                label = stringResource(R.string.title),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            ThemedTextField(
                value = content,
                onValueChange = { content = it },
                label = stringResource(R.string.content),
                modifier = Modifier.fillMaxWidth().weight(1f)
            )
            Spacer(modifier = Modifier.height(16.dp))

            ExposedDropdownMenuBox(expanded = customerExpanded, onExpandedChange = { customerExpanded = !customerExpanded }) {
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = customers.find { it.id == selectedCustomerId }?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Customer") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = customerExpanded) },
                )
                ExposedDropdownMenu(expanded = customerExpanded, onDismissRequest = { customerExpanded = false }) {
                    customers.forEach { customer ->
                        DropdownMenuItem(
                            text = { Text(customer.name) },
                            onClick = {
                                selectedCustomerId = customer.id
                                customerExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            ExposedDropdownMenuBox(expanded = invoiceExpanded, onExpandedChange = { invoiceExpanded = !invoiceExpanded }) {
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = invoices.invoices.find { it.invoice.id == selectedInvoiceId }?.invoice?.invoiceNumber ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Invoice") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = invoiceExpanded) },
                )
                ExposedDropdownMenu(expanded = invoiceExpanded, onDismissRequest = { invoiceExpanded = false }) {
                    invoices.invoices.forEach { invoice ->
                        DropdownMenuItem(
                            text = { Text(invoice.invoice.invoiceNumber) },
                            onClick = {
                                selectedInvoiceId = invoice.invoice.id
                                invoiceExpanded = false
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            ExposedDropdownMenuBox(expanded = quoteExpanded, onExpandedChange = { quoteExpanded = !quoteExpanded }) {
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = invoices.quotes.find { it.invoice.id == selectedQuoteId }?.invoice?.invoiceNumber ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Quote") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = quoteExpanded) },
                )
                ExposedDropdownMenu(expanded = quoteExpanded, onDismissRequest = { quoteExpanded = false }) {
                    invoices.quotes.forEach { quote ->
                        DropdownMenuItem(
                            text = { Text(quote.invoice.invoiceNumber) },
                            onClick = {
                                selectedQuoteId = quote.invoice.id
                                quoteExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                val newNote = Note(
                    id = noteId ?: 0,
                    title = title,
                    content = content,
                    createdAt = System.currentTimeMillis(),
                    customerId = selectedCustomerId,
                    invoiceId = selectedInvoiceId,
                    quoteId = selectedQuoteId
                )
                if (noteId == null) {
                    noteViewModel.addNote(newNote)
                } else {
                    noteViewModel.updateNote(newNote)
                }
                onNoteSaved()
            }) {
                Text(stringResource(R.string.save))
            }
        }
    }
}
