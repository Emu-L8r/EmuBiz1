package com.emul8r.bizap.ui.gui2.invoices

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
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
import coil.compose.AsyncImage
import com.emul8r.bizap.domain.model.InvoiceCustomization
import com.emul8r.bizap.ui.components.InvoiceBottomSummary
import com.emul8r.bizap.ui.components.LineItemsEditor
import com.emul8r.bizap.ui.components.InvoiceCustomizationEditor
import com.emul8r.bizap.ui.components.CurrencySelector
import com.emul8r.bizap.ui.components.PhotoAttachmentPicker
import com.emul8r.bizap.ui.gui2.invoice.AddPhotoDialogV2
import com.emul8r.bizap.ui.invoices.CreateInvoiceViewModel
import com.emul8r.bizap.ui.invoices.CustomerDropdown
import com.emul8r.bizap.ui.invoices.LineItemEditor
import java.io.File

/**
 * GUI2 Create Invoice Screen - uses the shared CreateInvoiceViewModel for feature parity with GUI1.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateInvoiceScreenV2(
    businessId: Long,
    onCreate: () -> Unit,
    onBack: () -> Unit,
    viewModel: CreateInvoiceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    var showPhotoDialog by remember { mutableStateOf(false) }
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraImageUri?.toString()?.let { viewModel.addPhoto(it) }
        }
    }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.toString()?.let { viewModel.addPhoto(it) }
    }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onCreate()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    if (showPhotoDialog) {
        AddPhotoDialogV2(
            onDismiss = { showPhotoDialog = false },
            onTakePhoto = {
                showPhotoDialog = false
                val imageFile = File.createTempFile("photo_", ".jpg", context.cacheDir)
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    imageFile
                )
                cameraImageUri = uri
                cameraLauncher.launch(uri)
            },
            onChooseFromGallery = {
                showPhotoDialog = false
                galleryLauncher.launch("image/*")
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Invoice") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
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
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Guard: Show loading indicator if customers not yet loaded
            if (uiState.customers.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                return@LazyColumn  // Stop rendering form until customers loaded
            }

            item {
                CustomerDropdown(
                    selectedCustomer = uiState.selectedCustomer,
                    customers = uiState.customers,
                    onSelect = viewModel::selectCustomer
                )
            }

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
                        // Handle case where number of items changed
                        if (updatedItems.size == uiState.items.size) {
                            // Standard update - same number of items
                            updatedItems.forEachIndexed { idx, item ->
                                viewModel.updateLineItem(
                                    uiState.items[idx].transientId,
                                    item.description,
                                    item.quantity,
                                    item.unitPrice
                                )
                            }
                        } else if (updatedItems.size > uiState.items.size) {
                            // New items added - add them to the ViewModel
                            val existingCount = uiState.items.size
                            for (i in existingCount until updatedItems.size) {
                                viewModel.addLineItem()
                                // Update the newly added item with values from editor
                                if (i < uiState.items.size) {
                                    val newItem = updatedItems[i]
                                    viewModel.updateLineItem(
                                        uiState.items[i].transientId,
                                        newItem.description,
                                        newItem.quantity,
                                        newItem.unitPrice
                                    )
                                }
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

            // TODO: Move header, footer, and customization to Settings → Invoice Customization
            // For now, these remain on the create invoice page for backwards compatibility
            // User preference: Move invoice customization to settings

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
                        updatedPhotos.filterNot { it in uiState.photoUris }.forEach { viewModel.addPhoto(it) }
                        uiState.photoUris.filterNot { it in updatedPhotos }.forEach { viewModel.removePhoto(it) }
                    }
                )
            }
        }
    }
}

