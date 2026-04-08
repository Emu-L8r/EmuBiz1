package com.emul8r.bizap.ui.gui2.invoices

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emul8r.bizap.ui.components.LineItemsEditor
import com.emul8r.bizap.ui.components.CurrencySelector
import com.emul8r.bizap.ui.invoices.CreateInvoiceViewModel
import com.emul8r.bizap.ui.invoices.CustomerDropdown
import timber.log.Timber

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
    var showPrefilledItemsDialog by remember { mutableStateOf(false) }

    Timber.d("🔷 CreateInvoiceScreenV2: Composing - businessId=$businessId, saveSuccess=${uiState.saveSuccess}")

    // 🔥 CRITICAL FIX: Set the businessId from navigation route so invoices save to correct business
    LaunchedEffect(businessId) {
        Timber.d("🎯 CreateInvoiceScreenV2: LaunchedEffect(businessId) - calling viewModel.setBusinessId($businessId)")
        viewModel.setBusinessId(businessId)
    }

    LaunchedEffect(uiState.saveSuccess) {
        Timber.d("🔍 CreateInvoiceScreenV2: LaunchedEffect triggered - saveSuccess=${uiState.saveSuccess}")
        if (uiState.saveSuccess) {
            Timber.d("✅ CreateInvoiceScreenV2: saveSuccess is TRUE - calling onCreate() navigation callback")
            Timber.d("   onCreate = $onCreate")
            try {
                onCreate()
                Timber.d("✅ CreateInvoiceScreenV2: onCreate() called successfully - should navigate back to list")
            } catch (e: Exception) {
                Timber.e(e, "❌ CreateInvoiceScreenV2: onCreate() threw exception!")
            }
        } else {
            Timber.d("⏳ CreateInvoiceScreenV2: saveSuccess is FALSE - waiting for save to complete")
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }


    Scaffold(
        topBar = {
            val metrics = viewModel.getInvoiceMetrics()
            TopAppBar(
                title = { Text("Create Invoice") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Save button moved to top bar for better tablet accessibility
                    Button(
                        onClick = {
                            Timber.d("🎬 CreateInvoiceScreenV2: SAVE BUTTON CLICKED")
                            Timber.d("   Calling viewModel.onSaveClicked()...")
                            viewModel.onSaveClicked()
                            Timber.d("   onSaveClicked() call completed - waiting for saveSuccess state change")
                        },
                        enabled = !uiState.isSaving,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp).padding(end = 4.dp),
                                strokeWidth = 2.dp
                            )
                            Text("Saving...")
                        } else {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp).padding(end = 4.dp))
                            Text("Save")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
                        // ✅ FIX FOR ISSUE #2: Use UUID-aware batch update
                        // This prevents index-based mismatch when items are deleted or reordered
                        viewModel.updateLineItemsFromEditor(updatedItems, uiState.items)
                    },
                    isDarkMode = isSystemInDarkTheme(),
                    onAddPrefilledClick = { showPrefilledItemsDialog = true }
                )
            }

            // ✅ REMOVED DUPLICATE: Only one add button in LineItemsEditor component
            // Duplicate TextButton removed - caused UX confusion

            item {
                OutlinedTextField(
                    value = uiState.notes,
                    onValueChange = viewModel::onNotesChange,
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }

            // ✅ Footer & Company Name: Auto-loaded from Invoice Settings
            // Configure these in Settings > Invoice Settings
            // They're pre-populated when creating invoice from saved defaults
        }
    }

    // Pre-filled Items Dialog
    if (showPrefilledItemsDialog) {
        PrefilledItemsSelectionDialog(
            onItemSelected = { prefilledItem ->
                // Convert PrefilledItem to LineItem and add to invoice
                viewModel.addLineItemFromPrefilledItem(prefilledItem)
                showPrefilledItemsDialog = false
                // Show success message
                Timber.d("✅ Added pre-filled item: ${prefilledItem.description}")
            },
            onDismiss = { showPrefilledItemsDialog = false }
        )
    }
}

@Composable
private fun PrefilledItemsSelectionDialog(
    onItemSelected: (com.emul8r.bizap.domain.model.PrefilledItem) -> Unit,
    onDismiss: () -> Unit,
    viewModel: com.emul8r.bizap.ui.settings.PrefilledItemsViewModel = hiltViewModel()
) {
    val prefilledItems by viewModel.items.collectAsStateWithLifecycle()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add Item from Templates")
        },
        text = {
            if (prefilledItems.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "No pre-filled items yet.\nCreate them in Settings > Pre-filled Items",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(prefilledItems, key = { it.id }) { item ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onItemSelected(item)
                                },
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        item.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "$${item.unitPrice / 100.0}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Add",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        },
        modifier = Modifier.fillMaxWidth(0.9f)
    )
}
