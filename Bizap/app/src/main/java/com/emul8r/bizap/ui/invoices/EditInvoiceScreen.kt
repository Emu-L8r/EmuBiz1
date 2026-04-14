package com.emul8r.bizap.ui.invoices

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.emul8r.bizap.domain.model.Customer
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.calculateTotal
import com.emul8r.bizap.ui.components.InvoiceBottomSummary
import com.emul8r.bizap.ui.common.CurrencySelector
import kotlinx.coroutines.delay
import timber.log.Timber
import java.io.File

/**
 * Invoice editing screen Composable for GUI1.
 *
 * **Purpose:**
 * Allows users to modify existing invoices. Provides same form as CreateInvoiceScreen
 * but with pre-populated data loaded from database.
 *
 * **Features:**
 * - Pre-loaded invoice data (customer, items, customization)
 * - Edit customer selection
 * - Edit/add/remove line items
 * - Edit invoice customization (header, footer, notes)
 * - Manage photo attachments
 * - Save updated invoice
 * - Share updated PDF
 *
 * **Data Flow:**
 * ```
 * Navigate with invoiceId
 *     ↓
 * ViewModel loads invoice from repository
 *     ↓
 * UiState emits Success(invoice, customers)
 *     ↓
 * UI pre-populates form with invoice data
 *     ↓
 * User edits form fields
 *     ↓
 * onInvoiceUpdated callback on save success
 * ```
 *
 * **States:**
 * - Loading: Shows spinner while loading invoice
 * - Success: Shows editable form with invoice data
 * - Error: Shows error message
 *
 * **Immutability Rules:**
 * - Once invoice is sent/paid, cannot be edited
 * - Must create correction instead
 *
 * @param onInvoiceUpdated Callback when invoice is successfully saved
 * @param viewModel EditInvoiceViewModel managing edit state
 *
 * @see EditInvoiceViewModel
 * @see CreateInvoiceScreen
 */
@Composable
fun EditInvoiceScreen(
    onInvoiceUpdated: () -> Unit,
    viewModel: EditInvoiceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is EditInvoiceUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is EditInvoiceUiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = state.message)
            }
        }
        is EditInvoiceUiState.Success -> {
            EditInvoiceContent(
                invoice = state.invoice,
                customers = state.customers,
                onInvoiceUpdated = onInvoiceUpdated,
                viewModel = viewModel
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditInvoiceContent(
    invoice: Invoice,
    customers: List<Customer>,
    onInvoiceUpdated: () -> Unit,
    viewModel: EditInvoiceViewModel
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()

    // FIXED: Proper SharedFlow collection for one-time events
    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            Timber.d("Event received in Screen: $event")
            when (event) {
                is NavigationEvent.BackToInvoiceDetail -> {
                    snackbarHostState.showSnackbar(
                        message = "✅ Invoice saved successfully!",
                        duration = SnackbarDuration.Short
                    )
                    delay(1500) // Give user time to read the success message
                    onInvoiceUpdated() // Trigger navigation back
                }
                is NavigationEvent.ShowError -> {
                    snackbarHostState.showSnackbar(
                        message = "❌ ${event.message}",
                        duration = SnackbarDuration.Long
                    )
                }
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            // tempImageUri?.let { viewModel.addPhoto(it) }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        // uri?.let { viewModel.addPhoto(it) }
    }

    var showAddPhotoDialog by remember { mutableStateOf(false) }

    if (showAddPhotoDialog) {
        AddPhotoDialog(
            onDismiss = { showAddPhotoDialog = false },
            onTakePhoto = {
                showAddPhotoDialog = false
                val file = File(context.cacheDir, "photo_${System.currentTimeMillis()}.jpg")
                tempImageUri = FileProvider.getUriForFile(context, "com.emul8r.bizap.fileprovider", file)
                tempImageUri?.let { cameraLauncher.launch(it) }
            },
            onChooseFromGallery = {
                showAddPhotoDialog = false
                galleryLauncher.launch("image/*")
            }
        )
    }

    Scaffold(
        topBar = {},  // MainActivity provides the header
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.shareInvoice() }) {
                Icon(Icons.Default.Share, contentDescription = "Share Invoice")
            }
        },
        bottomBar = {
            val total = invoice.items.sumOf { it.calculateTotal() }
            InvoiceBottomSummary(
                total = total,
                currencyCode = invoice.currency,
                isSaving = isSaving,
                onSave = { viewModel.saveInvoice() }
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .imePadding() // Ensures the keyboard doesn't hide input fields
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { Text("Customer: ${invoice.customerName}", style = MaterialTheme.typography.titleMedium) }
            item { OutlinedTextField(invoice.header ?: "", { viewModel.onHeaderChange(it) }, label = { Text("Header") }, modifier = Modifier.fillMaxWidth()) }
            item { OutlinedTextField(invoice.subheader ?: "", { viewModel.onSubheaderChange(it) }, label = { Text("Subheader") }, modifier = Modifier.fillMaxWidth()) }

            item {
                val currencies by viewModel.currencies.collectAsStateWithLifecycle()
                CurrencySelector(
                    currencies = currencies,
                    selectedCurrencyCode = invoice.currency,
                    onCurrencySelected = viewModel::onCurrencyChange,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item { Text("Line Items (changes save automatically)", style = MaterialTheme.typography.titleMedium) }

             items(invoice.items, key = { item -> item.id }) { item ->
                LineItemEditor(
                    description = item.description,
                    quantity = item.quantity,
                    unitPrice = item.unitPrice,
                    onUpdate = { desc, qty, price -> viewModel.updateLineItem(item.id.toString(), desc, qty, price) },
                    onRemove = { viewModel.removeLineItem(item.id.toString()) }
                )
            }

            item {
                TextButton(onClick = { viewModel.addLineItem() }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Line Item")
                    Text("Add Line Item")
                }
            }

            item { OutlinedTextField(invoice.notes ?: "", { viewModel.onNotesChange(it) }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth()) }
            item { OutlinedTextField(invoice.footer ?: "", { viewModel.onFooterChange(it) }, label = { Text("Footer") }, modifier = Modifier.fillMaxWidth()) }

            item {
                Text("Photos", style = MaterialTheme.typography.titleMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(invoice.photoUris) { uri ->
                        Image(rememberAsyncImagePainter(uri), null, modifier = Modifier.size(100.dp), contentScale = ContentScale.Crop)
                    }
                }
                Button(onClick = { showAddPhotoDialog = true }) {
                    Icon(Icons.Default.AddAPhoto, contentDescription = "Add Photo")
                    Spacer(Modifier.width(8.dp))
                    Text("Add Photo")
                }
            }
        }
    }
}
