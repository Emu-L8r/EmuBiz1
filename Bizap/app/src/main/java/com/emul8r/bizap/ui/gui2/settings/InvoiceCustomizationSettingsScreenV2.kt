package com.emul8r.bizap.ui.gui2.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import timber.log.Timber

/**
 * Invoice Customization Settings Screen (GUI2)
 * Allows users to customize invoice numbering, layout, and appearance
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceCustomizationSettingsScreenV2(
    onBack: () -> Unit,
    viewModel: InvoiceCustomizationViewModel = hiltViewModel()
) {
    val invoiceSettings by viewModel.invoiceSettings.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    var prefix by remember { mutableStateOf("") }
    var startingNumber by remember { mutableStateOf("") }
    var footerText by remember { mutableStateOf("") }
    var includeNotes by remember { mutableStateOf(true) }
    var includeTaxId by remember { mutableStateOf(true) }
    var showLogo by remember { mutableStateOf(true) }
    var showCompanyInfo by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    // Initialize from loaded settings
    LaunchedEffect(invoiceSettings) {
        invoiceSettings?.let { settings ->
            prefix = settings.invoicePrefix
            startingNumber = settings.startingNumber.toString()
            footerText = settings.footerText
            includeNotes = settings.includeNotes
            includeTaxId = settings.includeTaxId
            showLogo = settings.showLogo
            showCompanyInfo = settings.showCompanyInfo
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    // Show error snackbar
    LaunchedEffect(errorMessage) {
        errorMessage?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Long
            )
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Invoice Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Invoice Numbering Section
                Text(
                    "Invoice Numbering",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )

                // Invoice Prefix
                OutlinedTextField(
                    value = prefix,
                    onValueChange = { prefix = it.take(10) },
                    label = { Text("Invoice Prefix") },
                    placeholder = { Text("e.g., INV") },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("Prefix for invoice numbers (e.g., INV-2026-001)") },
                    maxLines = 1
                )

                // Starting Number
                OutlinedTextField(
                    value = startingNumber,
                    onValueChange = { startingNumber = it.filter { c -> c.isDigit() } },
                    label = { Text("Starting Invoice Number") },
                    placeholder = { Text("e.g., 1001") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    supportingText = { Text("Next invoice will use this number") },
                    maxLines = 1
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Invoice Layout Section
                Text(
                    "Invoice Layout",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )

                // Toggle: Show Logo
                SettingToggleRow(
                    title = "Show Logo",
                    description = "Display business logo on invoices",
                    checked = showLogo,
                    onCheckedChange = { showLogo = it }
                )

                // Toggle: Show Company Info
                SettingToggleRow(
                    title = "Show Company Info",
                    description = "Display business information header",
                    checked = showCompanyInfo,
                    onCheckedChange = { showCompanyInfo = it }
                )

                // Toggle: Include Notes
                SettingToggleRow(
                    title = "Include Notes",
                    description = "Show notes section on invoices",
                    checked = includeNotes,
                    onCheckedChange = { includeNotes = it }
                )

                // Toggle: Include Tax ID
                SettingToggleRow(
                    title = "Include Tax ID",
                    description = "Show tax ID field on invoices",
                    checked = includeTaxId,
                    onCheckedChange = { includeTaxId = it }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Footer Section
                Text(
                    "Invoice Footer",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )

                OutlinedTextField(
                    value = footerText,
                    onValueChange = { footerText = it.take(200) },
                    label = { Text("Footer Text") },
                    placeholder = { Text("e.g., Thank you for your business!") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 80.dp),
                    supportingText = { Text("${footerText.length}/200 characters") },
                    minLines = 3
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Save Button
                Button(
                    onClick = {
                        try {
                            isSaving = true
                            val number = startingNumber.toIntOrNull() ?: 1001
                            val newSettings = InvoiceSettings(
                                invoicePrefix = prefix.ifBlank { "INV" },
                                startingNumber = number,
                                includeNotes = includeNotes,
                                includeTaxId = includeTaxId,
                                footerText = footerText,
                                showLogo = showLogo,
                                showCompanyInfo = showCompanyInfo
                            )
                            viewModel.updateInvoiceSettings(newSettings)
                            Timber.i("Invoice customization settings updated")
                            isSaving = false
                        } catch (e: Exception) {
                            Timber.e(e, "Failed to update invoice settings")
                            isSaving = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .height(48.dp),
                    enabled = !isLoading && !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Save Settings")
                }
            }
        }
    }
}

/**
 * Reusable row for settings with toggle
 */
@Composable
private fun SettingToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp)
        ) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

