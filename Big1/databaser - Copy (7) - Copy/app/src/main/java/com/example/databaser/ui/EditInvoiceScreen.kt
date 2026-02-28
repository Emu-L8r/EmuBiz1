package com.example.databaser.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.databaser.R
import com.example.databaser.Screen
import com.example.databaser.data.LineItem
import com.example.databaser.viewmodel.BusinessInfoViewModel
import com.example.databaser.viewmodel.CustomerViewModel
import com.example.databaser.viewmodel.InvoiceViewModel
import com.example.databaser.viewmodel.LineItemViewModel
import com.example.databaser.viewmodel.NoteViewModel
import com.example.databaser.viewmodel.PredefinedLineItemViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditInvoiceScreen(
    invoiceId: Int,
    onInvoiceUpdated: () -> Unit,
    onAddLineItem: (Int) -> Unit,
    onLineEdit: (Int) -> Unit,
    onPrintPreview: (Int) -> Unit,
    onBackClick: () -> Unit,
    invoiceViewModel: InvoiceViewModel,
    customerViewModel: CustomerViewModel = hiltViewModel(),
    businessInfoViewModel: BusinessInfoViewModel = hiltViewModel(),
    lineItemViewModel: LineItemViewModel = hiltViewModel(),
    predefinedLineItemViewModel: PredefinedLineItemViewModel = hiltViewModel(),
    noteViewModel: NoteViewModel = hiltViewModel(),
    navController: NavHostController
) {
    val invoiceWithLineItems by invoiceViewModel.getInvoiceById(invoiceId).collectAsState(initial = null)
    val coroutineScope = rememberCoroutineScope()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val photoUris by invoiceViewModel.photoUris.collectAsState()
    val header by invoiceViewModel.header.collectAsState()
    val subHeader by invoiceViewModel.subHeader.collectAsState()
    val footer by invoiceViewModel.footer.collectAsState()


    LaunchedEffect(backStackEntry) {
        val itemAdded = backStackEntry?.savedStateHandle?.get<Boolean>("item_added")
        if (itemAdded == true) {
            invoiceViewModel.refreshInvoice(invoiceId)
            backStackEntry?.savedStateHandle?.remove<Boolean>("item_added")
        }
    }

    if (invoiceWithLineItems == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        val currentInvoiceWithLineItems = invoiceWithLineItems!!
        val customer by customerViewModel.getCustomerById(currentInvoiceWithLineItems.invoice.customerId)
            .collectAsState(initial = null)
        val businessInfo by businessInfoViewModel.businessInfo.collectAsState(initial = null)
        val notes by noteViewModel.getNotesForInvoice(invoiceId).collectAsState(initial = emptyList())
        var showDeleteDialog by remember { mutableStateOf(false) }
        var showConvertToInvoiceDialog by remember { mutableStateOf(false) }
        val totalAmount = currentInvoiceWithLineItems.lineItems.sumOf { it.quantity * it.unitPrice }
        var showDatePicker by remember { mutableStateOf(false) }
        var selectedDate by remember { mutableStateOf(currentInvoiceWithLineItems.invoice.date) }
        var showDueDatePicker by remember { mutableStateOf(false) }
        var selectedDueDate by remember { mutableStateOf(currentInvoiceWithLineItems.invoice.dueDate) }
        
        val imagePickerLauncher =
            rememberLauncherForActivityResult(contract = ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
                uris.forEach { invoiceViewModel.addPhotoUri(it) }
            }

        LaunchedEffect(invoiceWithLineItems) {
            invoiceViewModel.setPhotoUris(currentInvoiceWithLineItems.invoice.photoUris.map { it.toUri() })
            invoiceViewModel.setHeader(currentInvoiceWithLineItems.invoice.header)
            invoiceViewModel.setSubHeader(currentInvoiceWithLineItems.invoice.subHeader)
            invoiceViewModel.setFooter(currentInvoiceWithLineItems.invoice.footer)
        }

        val currentBusinessInfo = businessInfo
        if (customer == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Scaffold(
                topBar = {
                    AppTopAppBar(
                        title = stringResource(id = R.string.edit_invoice),
                        canNavigateBack = true,
                        onNavigateUp = onBackClick,
                        navController = navController
                    )
                },
                floatingActionButton = {
                    FloatingActionButton(onClick = { onAddLineItem(invoiceId) }) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(id = R.string.add_line_item))
                    }
                }
            ) { padding ->
                Column(modifier = Modifier.padding(padding).padding(16.dp)) {
                    // Top Business and Customer Info
                    Card(elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    stringResource(id = R.string.invoice_from),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                currentBusinessInfo?.logoPath?.let {
                                    Image(
                                        painter = rememberAsyncImagePainter(model = it.toUri()),
                                        contentDescription = stringResource(id = R.string.business_logo),
                                        modifier = Modifier.size(100.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                                currentBusinessInfo?.let {
                                    Text(it.name, style = MaterialTheme.typography.titleLarge)
                                    Text(it.address)
                                    Text(it.contactNumber)
                                    Text(it.email ?: "")
                                }
                            }
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                                Text(
                                    stringResource(id = R.string.invoice_to),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(customer!!.name)
                                Text(customer!!.address)
                                Text(customer!!.contactNumber)
                                Text(customer!!.email)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                stringResource(id = R.string.invoice_details),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        stringResource(
                                            id = R.string.invoice_number,
                                            currentInvoiceWithLineItems.invoice.invoiceNumber
                                        ),
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceAround
                                    ) {
                                        ThemedTextField(
                                            value = dateFormat.format(Date(selectedDate)),
                                            onValueChange = {},
                                            label = "Date",
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { showDatePicker = true }
                                        )
                                        Spacer(modifier = Modifier.padding(8.dp))
                                        ThemedTextField(
                                            value = dateFormat.format(Date(selectedDueDate)),
                                            onValueChange = {},
                                            label = "Due Date",
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { showDueDatePicker = true }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    ThemedTextField(
                        value = header,
                        onValueChange = { invoiceViewModel.setHeader(it) },
                        label = "Header",
                        modifier = Modifier.fillMaxWidth(),
                    )
                    ThemedTextField(
                        value = subHeader,
                        onValueChange = { invoiceViewModel.setSubHeader(it) },
                        label = "Sub-header",
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { imagePickerLauncher.launch("image/*") }) {
                        Text("Upload Photos")
                    }
                    LazyRow {
                        items(photoUris) { uri ->
                            Box(contentAlignment = Alignment.TopEnd) {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = "Selected photo",
                                    modifier = Modifier
                                        .size(100.dp)
                                        .padding(4.dp)
                                )
                                IconButton(
                                    onClick = { invoiceViewModel.removePhotoUri(uri) },
                                    modifier = Modifier
                                        .size(24.dp)
                                        .padding(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove photo",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.background(
                                            MaterialTheme.colorScheme.primary,
                                            CircleShape
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Combined scrollable list
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        item {
                            Row(modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)) {
                                Text(
                                    stringResource(id = R.string.item),
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Text(
                                    stringResource(id = R.string.total),
                                    modifier = Modifier.weight(0.5f),
                                    textAlign = TextAlign.End,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                            HorizontalDivider()
                        }
                        items(currentInvoiceWithLineItems.lineItems) { lineItem ->
                            LineItemRow(lineItem, onLongClick = { onLineEdit(lineItem.id) })
                            HorizontalDivider()
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(id = R.string.notes),
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Button(onClick = { navController.navigate(Screen.AddEditNote.createRoute(null, currentInvoiceWithLineItems.invoice.customerId, invoiceId, null)) }) {
                                Text(stringResource(id = R.string.add_note))
                            }
                        }
                        items(notes) { note ->
                            NoteListItem(
                                note = note,
                                onClick = { navController.navigate(Screen.AddEditNote.createRoute(note.id, note.customerId, note.invoiceId, note.quoteId)) })
                        }
                    }

                    // Bottom section
                    Spacer(modifier = Modifier.height(16.dp))
                    ThemedTextField(
                        value = footer,
                        onValueChange = { invoiceViewModel.setFooter(it) },
                        label = "Footer",
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    currentBusinessInfo?.generalNotes?.let {
                        if (it.isNotBlank()) {
                            Text(text = it, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    currentBusinessInfo?.paymentDetails?.let {
                        if (it.isNotBlank()) {
                            Text(text = it, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(horizontalAlignment = Alignment.End, modifier = Modifier.fillMaxWidth()) {
                        Row {
                            Text(
                                text = stringResource(id = R.string.total),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(end = 16.dp)
                            )
                            Text(
                                text = "$${String.format(Locale.US, "%.2f", totalAmount)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(onClick = {
                            coroutineScope.launch {
                                val photoUriStrings = photoUris.map { it.toString() }
                                invoiceViewModel.updateInvoice(
                                    currentInvoiceWithLineItems.invoice.copy(
                                        header = header,
                                        subHeader = subHeader,
                                        footer = footer,
                                        date = selectedDate,
                                        dueDate = selectedDueDate,
                                        photoUris = photoUriStrings
                                    )
                                )
                                onInvoiceUpdated()
                            }
                        }) {
                            Text(stringResource(id = R.string.save))
                        }
                        if (currentInvoiceWithLineItems.invoice.isQuote) {
                            Button(onClick = { showConvertToInvoiceDialog = true }) {
                                Text(stringResource(id = R.string.convert_to_invoice))
                            }
                        }
                        Button(onClick = { onPrintPreview(currentInvoiceWithLineItems.invoice.id) }) {
                            Text(stringResource(id = R.string.print_preview))
                        }
                        Button(onClick = { showDeleteDialog = true }) {
                            Text(stringResource(id = R.string.delete_invoice))
                        }
                    }
                }
            }

            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text(stringResource(id = R.string.delete_invoice)) },
                    text = { Text(stringResource(id = R.string.confirm_delete_invoice)) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                invoiceViewModel.deleteInvoice(currentInvoiceWithLineItems.invoice)
                                onInvoiceUpdated()
                            }
                        ) {
                            Text(stringResource(id = R.string.delete))
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showDeleteDialog = false }
                        ) {
                            Text(stringResource(id = R.string.cancel))
                        }
                    }
                )
            }
            if (showConvertToInvoiceDialog) {
                AlertDialog(
                    onDismissRequest = { showConvertToInvoiceDialog = false },
                    title = { Text(stringResource(id = R.string.convert_to_invoice)) },
                    text = { Text(stringResource(id = R.string.confirm_convert_to_invoice)) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                invoiceViewModel.convertToInvoice(currentInvoiceWithLineItems.invoice)
                                onInvoiceUpdated()
                            }
                        ) {
                            Text(stringResource(id = R.string.convert))
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showConvertToInvoiceDialog = false }
                        ) {
                            Text(stringResource(id = R.string.cancel))
                        }
                    }
                )
            }
            if (showDatePicker) {
                val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = { 
                            selectedDate = datePickerState.selectedDateMillis!!
                            showDatePicker = false
                        }) {
                            Text(stringResource(id = R.string.ok))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text(stringResource(id = R.string.cancel))
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            if (showDueDatePicker) {
                val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDueDate)
                DatePickerDialog(
                    onDismissRequest = { showDueDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            selectedDueDate = datePickerState.selectedDateMillis!!
                            showDueDatePicker = false
                        }) {
                            Text(stringResource(id = R.string.ok))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDueDatePicker = false }) {
                            Text(stringResource(id = R.string.cancel))
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }
        }
    }
}
