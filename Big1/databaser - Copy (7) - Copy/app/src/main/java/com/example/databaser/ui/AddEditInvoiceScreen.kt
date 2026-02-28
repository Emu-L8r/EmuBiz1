package com.example.databaser.ui

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.databaser.R
import com.example.databaser.Screen
import com.example.databaser.data.Invoice
import com.example.databaser.data.LineItem
import com.example.databaser.viewmodel.BusinessInfoViewModel
import com.example.databaser.viewmodel.CustomerViewModel
import com.example.databaser.viewmodel.InvoiceViewModel
import com.example.databaser.viewmodel.PredefinedLineItemViewModel
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditInvoiceScreen(
    invoiceId: Int?,
    customerId: Int?,
    onInvoiceSaved: () -> Unit,
    onBackClick: () -> Unit,
    invoiceViewModel: InvoiceViewModel = hiltViewModel(),
    customerViewModel: CustomerViewModel = hiltViewModel(),
    predefinedLineItemViewModel: PredefinedLineItemViewModel = hiltViewModel(),
    businessInfoViewModel: BusinessInfoViewModel = hiltViewModel(),
    navController: NavHostController
) {
    var currentCustomerId by rememberSaveable { mutableIntStateOf(customerId ?: -1) }
    val context = LocalContext.current

    if (currentCustomerId == -1) {
        // Show customer list if no customer is selected
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("No customer selected. Please select a customer.")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { navController.navigate(Screen.CustomerList.route) }) {
                Text("Select Customer")
            }
        }
        return
    }

    var currentInvoiceId by rememberSaveable { mutableIntStateOf(invoiceId ?: -1) }
    val isEditing = currentInvoiceId != -1
    var photoUris by rememberSaveable { mutableStateOf<List<Uri>>(emptyList()) }
    val customer by customerViewModel.getCustomerById(currentCustomerId).collectAsStateWithLifecycle(initialValue = null)
    val businessInfo by businessInfoViewModel.businessInfo.collectAsStateWithLifecycle(initialValue = null)
    val invoice by if (isEditing) {
        invoiceViewModel.getInvoiceById(currentInvoiceId).collectAsStateWithLifecycle(initialValue = null)
    } else {
        remember { mutableStateOf(null) }
    }
    val predefinedLineItems by predefinedLineItemViewModel.allPredefinedLineItems.collectAsStateWithLifecycle()
    val lineItems = remember { mutableStateListOf<LineItem>() }
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    var showCustomItemDialog by remember { mutableStateOf(false) }
    var showEditItemDialog by remember { mutableStateOf<LineItem?>(null) }
    var selectedDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var selectedDueDate by remember { mutableLongStateOf(0L) }
    var header by remember { mutableStateOf("") }
    var subHeader by remember { mutableStateOf("") }
    var footer by remember { mutableStateOf("") }

    val imagePickerLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
        photoUris = photoUris + uris
    }


    LaunchedEffect(key1 = invoiceId) {
        currentInvoiceId = invoiceId ?: -1
    }
    
    LaunchedEffect(navController.currentBackStackEntry) {
        navController.currentBackStackEntry?.savedStateHandle?.get<Int>("newCustomerId")?.let {
            currentCustomerId = it
            navController.currentBackStackEntry?.savedStateHandle?.remove<Int>("newCustomerId")
        }
    }

    LaunchedEffect(key1 = invoice) {
        if (isEditing) {
            invoice?.let {
                selectedDate = it.invoice.date
                selectedDueDate = it.invoice.dueDate
                header = it.invoice.header
                subHeader = it.invoice.subHeader
                footer = it.invoice.footer
                if (lineItems.isEmpty()) { // To avoid overwriting on recomposition
                    lineItems.addAll(it.lineItems)
                }
                photoUris = it.invoice.photoUris.map { Uri.parse(it) }
            }
        }
    }

    LaunchedEffect(key1 = businessInfo) {
        if (!isEditing) {
            businessInfo?.let {
                selectedDate = System.currentTimeMillis()
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = selectedDate
                calendar.add(Calendar.DAY_OF_YEAR, it.defaultDueDateDays)
                selectedDueDate = calendar.timeInMillis
            }
        }
    }

    LaunchedEffect(navController.currentBackStackEntry) {
        navController.currentBackStackEntry?.savedStateHandle?.get<LineItem>("newLineItem")?.let {
            lineItems.add(it)
            navController.currentBackStackEntry?.savedStateHandle?.remove<LineItem>("newLineItem")
        }
    }

    if ((isEditing && customer == null) || businessInfo == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(if (isEditing) stringResource(id = R.string.edit_invoice) else stringResource(id = R.string.add_invoice)) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.back))
                        }
                    },
                    actions = {
                        if (isEditing) {
                            IconButton(onClick = {
                                val invoiceIdForPreview = if (currentInvoiceId != -1) currentInvoiceId else null
                                if (invoiceIdForPreview != null) {
                                    navController.navigate(Screen.PrintPreview.createRoute(invoiceIdForPreview))
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Print,
                                    contentDescription = stringResource(id = R.string.print_preview)
                                )
                            }
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = {
                    showBottomSheet = true
                }) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(id = R.string.add_line_item))
                }
            },
            bottomBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            coroutineScope.launch {
                                if (customer?.name?.isNotBlank() == true) {
                                    if (currentInvoiceId != -1) {
                                        invoice?.invoice?.let { existingInvoice ->
                                            val invoiceToUpdate = existingInvoice.copy(
                                                date = selectedDate,
                                                dueDate = selectedDueDate,
                                                header = header,
                                                subHeader = subHeader,
                                                footer = footer,
                                                photoUris = photoUris.map { it.toString() }
                                            )
                                            invoiceViewModel.updateInvoiceWithLineItems(invoiceToUpdate, lineItems)
                                            onInvoiceSaved()
                                        }
                                    } else {
                                        val invoiceNumber = invoiceViewModel.generateInvoiceNumber(customer!!.name)
                                        val invoiceToCreate = Invoice(
                                            customerId = currentCustomerId,
                                            date = selectedDate,
                                            invoiceNumber = invoiceNumber,
                                            dueDate = selectedDueDate,
                                            isQuote = false,
                                            header = header,
                                            subHeader = subHeader,
                                            footer = footer,
                                            photoUris = photoUris.map { uri -> uri.toString() }
                                        )
                                        invoiceViewModel.addInvoiceWithLineItems(invoiceToCreate, lineItems)
                                        onInvoiceSaved()
                                    }
                                } else {
                                    Toast.makeText(context, "Customer name cannot be empty", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }) {
                        Text(stringResource(id = R.string.save))
                    }
                }
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                item {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        customer?.let {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(stringResource(id = R.string.invoice_to), style = MaterialTheme.typography.titleMedium)
                                Text(it.name)
                                Text(it.address)
                                Text(it.contactNumber)
                            }
                        }
                        OutlinedButton(onClick = { navController.navigate(Screen.AddEditCustomer.createRoute(null)) }) {
                            Text("New Customer")
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    OutlinedTextField(value = header, onValueChange = {header = it}, label = { Text("Header")}, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = subHeader, onValueChange = {subHeader = it}, label = { Text("Sub-header")}, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = footer, onValueChange = {footer = it}, label = { Text("Footer")}, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    Button(onClick = { imagePickerLauncher.launch("image/*") }) {
                        Text("Upload Photos")
                    }
                    AsyncImage(model = photoUris.firstOrNull(), contentDescription = null)
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                        Text(stringResource(id = R.string.item), modifier = Modifier.weight(1f), style = MaterialTheme. typography.labelSmall)
                        Text(stringResource(id = R.string.total), modifier = Modifier.weight(0.5f), textAlign = TextAlign.End, style = MaterialTheme.typography.labelSmall)
                    }
                    HorizontalDivider()
                }
                items(lineItems) { lineItem ->
                    LineItemRow(lineItem, onLongClick = { showEditItemDialog = lineItem })
                    HorizontalDivider()
                }
            }
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(stringResource(id = R.string.services), style = MaterialTheme.typography.titleMedium)
                    Button(onClick = { showCustomItemDialog = true }) {
                        Text(stringResource(id = R.string.add_custom_item))
                    }
                }
                HorizontalDivider()
                LazyColumn {
                    items(predefinedLineItems) { service ->
                        ServiceItem(
                            service = service,
                            onClick = { 
                                lineItems.add(LineItem(invoiceId = 0, job = service.job, details = service.details, quantity = 1, unitPrice = service.unitPrice))
                                coroutineScope.launch {
                                    sheetState.hide()
                                }.invokeOnCompletion { 
                                    if (!sheetState.isVisible) {
                                        showBottomSheet = false
                                    }
                                }
                             }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }

    if (showCustomItemDialog) {
        AddCustomItemDialog(
            onDismiss = { showCustomItemDialog = false },
            onSave = {newItem ->
                lineItems.add(newItem)
                showCustomItemDialog = false
            }
        )
    }

    showEditItemDialog?.let {
        EditLineItemDialog(
            lineItem = it,
            onDismiss = { showEditItemDialog = null },
            onSave = { updatedLineItem ->
                val index = lineItems.indexOf(it)
                if (index != -1) {
                    lineItems[index] = updatedLineItem
                }
                showEditItemDialog = null
            }
        )
    }
}
