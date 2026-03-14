package com.emul8r.bizap.ui.gui2.invoices

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emul8r.bizap.ui.components.InvoiceBottomSummary
import com.emul8r.bizap.ui.common.CurrencySelector
import com.emul8r.bizap.ui.invoices.CreateInvoiceViewModel
import com.emul8r.bizap.ui.invoices.CustomerDropdown
import com.emul8r.bizap.ui.invoices.LineItemEditor

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
                    currencies = uiState.currencies,
                    selectedCurrencyCode = uiState.selectedCurrencyCode,
                    onCurrencySelected = viewModel::onCurrencySelected,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            items(uiState.items, key = { it.transientId.toString() }) { item ->
                LineItemEditor(
                    description = item.description,
                    quantity = item.quantity,
                    unitPrice = item.unitPrice,
                    onUpdate = { desc, qty, price ->
                        viewModel.updateLineItem(item.transientId, desc, qty, price)
                    },
                    onRemove = { viewModel.removeLineItem(item.transientId) }
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
        }
    }
}

