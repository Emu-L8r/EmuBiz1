package com.emul8r.bizap.ui.invoices

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emul8r.bizap.domain.model.InvoiceCustomization
import com.emul8r.bizap.ui.components.InvoiceBottomSummary
import com.emul8r.bizap.ui.components.LineItemsEditor
import com.emul8r.bizap.ui.components.CurrencySelector
import com.emul8r.bizap.ui.components.PhotoAttachmentPicker
import timber.log.Timber
import java.io.File

/**
 * Unified Create Invoice Page - Responsive Composable for all screen sizes
 *
 * **Purpose:**
 * Consolidates invoice creation UI for both GUI1 (desktop-like) and GUI2 (modern tablet/phone),
 * providing a single, reusable, and responsive invoice creation experience.
 *
 * **Key Features:**
 * - Responsive layout (phone: stacked, tablet: adaptive)
 * - Customer selection dropdown
 * - Line items editor (add/remove/edit)
 * - Currency selection
 * - Header/Subheader/Notes/Footer customization
 * - Photo attachment (camera or gallery)
 * - Real-time total calculation
 * - Save with validation and PDF generation
 * - Comprehensive error handling
 *
 * **Design Patterns:**
 * - Uses `CreateInvoiceViewModel` for state management
 * - Responsive design with `Scaffold` and `LazyColumn`
 * - Lazy photo gallery using `LazyRow`
 * - Modal dialogs for add-photo and pre-filled items
 *
 * **Data Flow:**
 * ```
 * User input (forms, selections, photos)
 *     ↓
 * ViewModel state update via action methods
 *     ↓
 * UI recomposes with new state
 *     ↓
 * Metrics calculated in real-time
 *     ↓
 * User clicks Save
 *     ↓
 * Validation + Database persistence + PDF generation
 *     ↓
 * Success callback or error snackbar
 * ```
 *
 * @param businessId Current business context (routed from navigation)
 * @param onInvoiceSaved Callback when invoice successfully persisted
 * @param viewModel Injected state manager (default: hiltViewModel)
 *
 *  * @see CreateInvoiceViewModel
 *  * @see CreateInvoiceScreen (GUI1 wrapper)
 *  * @see com.emul8r.bizap.ui.gui2.invoices.CreateInvoiceScreenV2 (GUI2 wrapper)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnifiedCreateInvoicePage(
    businessId: Long = 0L,
    onInvoiceSaved: () -> Unit = {},
    viewModel: CreateInvoiceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var showAddPhotoDialog by remember { mutableStateOf(false) }
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }

    Timber.d("🎨 UnifiedCreateInvoicePage: Rendering - businessId=$businessId")

    // 📸 Camera/Gallery Launchers
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            tempImageUri?.let {
                Timber.d("📷 Photo captured: $it")
                viewModel.addPhoto(it.toString())
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            Timber.d("🖼️ Photo selected from gallery: $it")
            viewModel.addPhoto(it.toString())
        }
    }

    // 🔙 Photo Dialog
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

    // ✅ Handle save success
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            Timber.d("✅ UnifiedCreateInvoicePage: Save successful - calling onInvoiceSaved()")
            onInvoiceSaved()
        }
    }

    // ❌ Handle errors
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Timber.e("❌ UnifiedCreateInvoicePage: Error - $it")
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    // Set business ID on mount
    LaunchedEffect(businessId) {
        if (businessId > 0L) {
            Timber.d("🎯 UnifiedCreateInvoicePage: Setting businessId=$businessId")
            viewModel.setBusinessId(businessId)
        }
    }

    var showAdditionalInfo by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {}, // Parent activity/screen handles top bar
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            val metrics = viewModel.getInvoiceMetrics()
            InvoiceBottomSummary(
                total = metrics.totalAmount,
                currencyCode = uiState.selectedCurrencyCode,
                isSaving = uiState.isSaving,
                onSave = {
                    Timber.d("💾 UnifiedCreateInvoicePage: Save button clicked")
                    viewModel.onSaveClicked()
                }
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
            // Section 1: Customer Selection
            item {
                CustomerDropdown(
                    selectedCustomer = uiState.selectedCustomer,
                    customers = uiState.customers,
                    onSelect = { customer ->
                        Timber.d("👥 Customer selected: ${customer.name}")
                        viewModel.selectCustomer(customer)
                    }
                )
            }

            // Section 2: Additional Info (Optional Collapsible Header/Subheader)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAdditionalInfo = !showAdditionalInfo },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Header with collapse/expand icon
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "📋 Additional Info (Optional)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Icon(
                                imageVector = if (showAdditionalInfo) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Collapsible content
                        if (showAdditionalInfo) {
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = uiState.header,
                                onValueChange = {
                                    viewModel.onHeaderChange(it)
                                    Timber.d("📝 Header changed: $it")
                                },
                                label = { Text("Header (Optional)") },
                                placeholder = { Text("e.g., INVOICE") },
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 1,
                                singleLine = true,
                                supportingText = { Text("Additional clarity for invoice type or context") }
                            )

                            OutlinedTextField(
                                value = uiState.subheader,
                                onValueChange = {
                                    viewModel.onSubheaderChange(it)
                                    Timber.d("📝 Subheader changed: $it")
                                },
                                label = { Text("Subheader (Optional)") },
                                placeholder = { Text("e.g., Tax Invoice") },
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 1,
                                singleLine = true,
                                supportingText = { Text("Secondary info or document type") }
                            )
                        }
                    }
                }
            }

            // Section 3: Currency Selection
            item {
                CurrencySelector(
                    selectedCurrency = uiState.selectedCurrencyCode,
                    onCurrencyChange = { currency ->
                        Timber.d("💱 Currency changed: $currency")
                        viewModel.onCurrencySelected(currency)
                    },
                    isDarkMode = isSystemInDarkTheme(),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Section 4: Line Items Editor
            item {
                val lineItems = uiState.items.map {
                    com.emul8r.bizap.domain.model.InvoiceItem(
                        id = it.transientId.hashCode().toLong(),
                        description = it.description,
                        quantity = it.quantity,
                        unitPrice = it.unitPrice
                    )
                }
                LineItemsEditor(
                    items = lineItems,
                    onItemsChange = { updatedItems ->
                        Timber.d("📦 Line items changed: ${updatedItems.size} items")
                        viewModel.updateLineItemsFromEditor(updatedItems, uiState.items)
                    },
                    isDarkMode = isSystemInDarkTheme()
                )
            }

            // Section 5: Add Item / Load Preset Buttons
            item {
                var showPrefilledDialog by remember { mutableStateOf(false) }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = {
                            Timber.d("➕ Add line item button clicked")
                            viewModel.addLineItem()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text("Add Item")
                    }

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

            // Section 6: Notes
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "📝 Notes (Optional)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = uiState.notes,
                            onValueChange = {
                                viewModel.onNotesChange(it)
                                Timber.d("📝 Notes changed: length=${it.length}")
                            },
                            label = { Text("Add notes for this invoice") },
                            placeholder = { Text("E.g., 'Payment due within 30 days'") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 5,
                            supportingText = { Text("These notes will appear on the invoice") }
                        )
                    }
                }
            }

            // Section 7: Footer
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "🔚 Footer Text (Optional)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = uiState.footer,
                            onValueChange = {
                                viewModel.onFooterChange(it)
                                Timber.d("📝 Footer changed: length=${it.length}")
                            },
                            label = { Text("Footer message") },
                            placeholder = { Text("E.g., 'Thank you for your business!'") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            maxLines = 3,
                            supportingText = { Text("Appears at the bottom of the invoice") }
                        )
                    }
                }
            }

            // Section 9: Photo Attachments (removed Section 8: Customization - moved to Settings)
            item {
                PhotoAttachmentPicker(
                    photos = uiState.photoUris,
                    onPhotosChange = { updatedPhotos ->
                        // Add new photos
                        updatedPhotos.filterNot { it in uiState.photoUris }.forEach {
                            Timber.d("📸 Adding photo: $it")
                            viewModel.addPhoto(it)
                        }
                        // Remove deleted photos
                        uiState.photoUris.filterNot { it in updatedPhotos }.forEach {
                            Timber.d("🗑️ Removing photo: $it")
                            viewModel.removePhoto(it)
                        }
                    },
                    onAddPhotoClicked = { showAddPhotoDialog = true }
                )
            }

            // Bottom spacing
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}


