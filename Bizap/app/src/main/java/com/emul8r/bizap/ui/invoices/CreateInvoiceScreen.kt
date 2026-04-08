package com.emul8r.bizap.ui.invoices

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.emul8r.bizap.BuildConfig
import com.emul8r.bizap.domain.model.Customer
import com.emul8r.bizap.domain.model.InvoiceCustomization
import com.emul8r.bizap.ui.components.InvoiceBottomSummary
import com.emul8r.bizap.ui.components.LineItemsEditor
import com.emul8r.bizap.ui.components.InvoiceCustomizationEditor
import com.emul8r.bizap.ui.components.CurrencySelector
import com.emul8r.bizap.ui.components.PhotoAttachmentPicker
import java.io.File
import java.util.Locale

/**
 * Invoice creation screen Composable for GUI1.
 *
 * **Purpose:**
 * Full-featured invoice creation form allowing users to:
 * - Select customer from dropdown
 * - Add multiple line items with descriptions, quantities, prices
 * - Customize invoice headers, footers, notes
 * - Attach photos to invoice
 * - See real-time totals (subtotal, tax, final total)
 * - Save invoice and generate PDF
 *
 * **Features:**
 * - Customer selector dropdown
 * - Line items editor (add/remove/edit items)
 * - Currency selector (AUD, USD, etc.)
 * - Real-time metric calculation (subtotal, tax, total)
 * - Invoice customization (header, subheader, footer, notes)
 * - Photo attachment (camera or gallery)
 * - Save button (with validation)
 * - Error handling with snackbars
 *
 * **Data Flow:**
 * ```
 * Screen collects user input
 *     ↓
 * ViewModel updates state via methods:
 * - selectCustomer(), addLineItem(), updateLineItem()
 * - onCurrencySelected(), onHeaderChange(), etc.
 *     ↓
 * getInvoiceMetrics() calculates totals
 *     ↓
 * onSaveClicked() validates & saves to database
 *     ↓
 * PDF generation triggered
 *     ↓
 * Success callback: onInvoiceSaved()
 * ```
 *
 * **Layout Structure:**
 * ```
 * Top: Customer selector + currency picker
 * Middle: Line items editor (scrollable)
 * Bottom: Invoice totals (subtotal, tax, total)
 * Floating: Bottom sheet for customization
 * Dialogs: Photo picker, error messages
 * ```
 *
 * **Validation:**
 * - Customer must be selected
 * - At least one line item required
 * - All items must have valid amounts
 * - Proper currency selected
 *
 * **Error Handling:**
 * - Validation errors shown in snackbar
 * - Network errors shown in snackbar
 * - Loading state shows spinner
 * - Success navigates back
 *
 * @param viewModel CreateInvoiceViewModel managing form state
 * @param onInvoiceSaved Callback when invoice successfully saved (navigate back)
 *
 * @see CreateInvoiceViewModel
 * @see InvoiceBottomSummary
 * @see LineItemsEditor
 * @see InvoiceCustomizationEditor
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateInvoiceScreen(
    viewModel: CreateInvoiceViewModel = hiltViewModel(),
    onInvoiceSaved: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var showAddPhotoDialog by remember { mutableStateOf(false) }
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            tempImageUri?.let { viewModel.addPhoto(it.toString()) }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.addPhoto(it.toString()) }
    }

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

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onInvoiceSaved()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {},  // MainActivity provides the header
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            val metrics = viewModel.getInvoiceMetrics()
            InvoiceBottomSummary(
                total = metrics.totalAmount,
                currencyCode = uiState.selectedCurrencyCode,
                isSaving = uiState.isSaving,
                onSave = { viewModel.onSaveClicked() }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { CustomerDropdown(uiState.selectedCustomer, uiState.customers, viewModel::selectCustomer) }

            // ✅ IMPROVED: Header/Subheader section with visual hierarchy and professional styling
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "📄 Invoice Header & Title",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = uiState.header,
                            onValueChange = viewModel::onHeaderChange,
                            label = { Text("Invoice Title") },
                            placeholder = { Text("e.g., INVOICE") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 1,
                            singleLine = true,
                            supportingText = { Text("Main document title - appears at top of PDF") }
                        )

                        OutlinedTextField(
                            value = uiState.subheader,
                            onValueChange = viewModel::onSubheaderChange,
                            label = { Text("Subtitle (Optional)") },
                            placeholder = { Text("e.g., Tax Invoice") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 1,
                            singleLine = true,
                            supportingText = { Text("Secondary title or document type") }
                        )
                    }
                }
            }

            item {
                CurrencySelector(
                    selectedCurrency = uiState.selectedCurrencyCode,
                    onCurrencyChange = viewModel::onCurrencySelected,
                    isDarkMode = isSystemInDarkTheme(),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Phase 2: Line Items Component
            item {
                val lineItems = uiState.items.map {
                    com.emul8r.bizap.domain.model.LineItem(
                        id = it.transientId.hashCode().toLong(),
                        description = it.description,
                        quantity = it.quantity,
                        unitPrice = it.unitPrice
                    )
                }
                LineItemsEditor(
                    items = lineItems,
                    onItemsChange = { updatedItems ->
                        // ✅ FIX FOR ISSUE #2: Use UUID-aware batch update
                        // This prevents index-based mismatch when items are deleted or reordered
                        viewModel.updateLineItemsFromEditor(updatedItems, uiState.items)
                    },
                    isDarkMode = isSystemInDarkTheme()
                )
            }

            // ✅ FIXED: Both buttons in one row for better visibility
            item {
                var showPrefilledDialog by remember { mutableStateOf(false) }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Left button: Add Item
                    TextButton(
                        onClick = { viewModel.addLineItem() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text("Add Item")
                    }

                    // Right button: Load Pre-filled
                    OutlinedButton(
                        onClick = { showPrefilledDialog = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text("Load Preset")
                    }
                }

                if (showPrefilledDialog) {
                    AlertDialog(
                        onDismissRequest = { showPrefilledDialog = false },
                        title = { Text("Pre-filled Items") },
                        text = { Text("Load pre-configured line items from your settings to speed up invoice creation.") },
                        dismissButton = {
                            TextButton(onClick = { showPrefilledDialog = false }) {
                                Text("Cancel")
                            }
                        },
                        confirmButton = {
                            Button(onClick = { showPrefilledDialog = false }) {
                                Text("Done")
                            }
                        }
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = uiState.notes,
                    onValueChange = viewModel::onNotesChange,
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }

            item {
                OutlinedTextField(
                    value = uiState.footer,
                    onValueChange = viewModel::onFooterChange,
                    label = { Text("Footer") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Phase 2: Customization Component
            item {
                val customization = InvoiceCustomization(
                    companyName = uiState.companyName,
                    headerText = uiState.header,
                    footerText = uiState.footer,
                    templateType = uiState.templateType
                )
                InvoiceCustomizationEditor(
                    customization = customization,
                    onCustomizationChange = { updated ->
                        viewModel.updateCompanyName(updated.companyName)
                        viewModel.updateTemplateType(updated.templateType)
                    }
                )
            }

            // Phase 2: Photo Attachments Component
            item {
                PhotoAttachmentPicker(
                    photos = uiState.photoUris,
                    onPhotosChange = { updatedPhotos ->
                        // Add new photos
                        updatedPhotos.filterNot { it in uiState.photoUris }.forEach { viewModel.addPhoto(it) }
                        // Remove deleted photos
                        uiState.photoUris.filterNot { it in updatedPhotos }.forEach { viewModel.removePhoto(it) }
                    },
                    onAddPhotoClicked = { showAddPhotoDialog = true }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDropdown(
    selectedCustomer: Customer?,
    customers: List<Customer>,
    onSelect: (Customer) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedCustomer?.name ?: "Select Customer",
            onValueChange = {},
            readOnly = true,
            label = { Text("Customer") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            customers.forEach { customer ->
                DropdownMenuItem(
                    text = { Text(customer.name) },
                    onClick = {
                        onSelect(customer)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun LineItemEditor(
    description: String,
    quantity: Double,
    unitPrice: Long,
    onUpdate: (String, Double, Long) -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = description,
            onValueChange = { onUpdate(it, quantity, unitPrice) },
            label = { Text("Service/Item") },
            modifier = Modifier.weight(1.5f)
        )
        OutlinedTextField(
            value = if (quantity == 0.0) "" else quantity.toString(),
            onValueChange = { it.toDoubleOrNull()?.let { valQty -> onUpdate(description, valQty, unitPrice) } },
            label = { Text("Qty") },
            modifier = Modifier.weight(0.5f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )
        OutlinedTextField(
            value = if (unitPrice == 0L) "" else (unitPrice.toDouble() / 100.0).toString(),
            onValueChange = { it.toDoubleOrNull()?.let { valPrice -> onUpdate(description, quantity, (valPrice * 100).toLong()) } },
            label = { Text("$") },
            modifier = Modifier.weight(0.7f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )
        IconButton(onClick = onRemove) {
            Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
        }
    }
}
