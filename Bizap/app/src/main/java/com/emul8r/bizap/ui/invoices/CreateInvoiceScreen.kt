package com.emul8r.bizap.ui.invoices

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import com.emul8r.bizap.ui.common.CurrencySelector
import java.io.File
import java.util.Locale

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
            item { 
                OutlinedTextField(
                    value = uiState.header, 
                    onValueChange = viewModel::onHeaderChange, 
                    label = { Text("Header") }, 
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item { 
                OutlinedTextField(
                    value = uiState.subheader,
                    onValueChange = viewModel::onSubheaderChange,
                    label = { Text("Subheader") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                CurrencySelector(
                    currencies = uiState.currencies,
                    selectedCurrencyCode = uiState.selectedCurrencyCode,
                    onCurrencySelected = viewModel::onCurrencySelected,
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
                        // Update ViewModel with new items
                        updatedItems.forEachIndexed { idx, item ->
                            if (idx < uiState.items.size) {
                                viewModel.updateLineItem(
                                    uiState.items[idx].transientId,
                                    item.description,
                                    item.quantity,
                                    item.unitPrice
                                )
                            }
                        }
                    },
                    isDarkMode = isSystemInDarkTheme()  // ← Add this parameter
                )
            }

            item {
                TextButton(onClick = { viewModel.addLineItem() }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text("Add Line Item")
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
                    }
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
