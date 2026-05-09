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
import com.emul8r.bizap.domain.model.*
import com.emul8r.bizap.domain.model.ColorScheme as InvoiceColorScheme
import com.emul8r.bizap.ui.gui2.components.PresetSelector
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

    // Phase 3B settings - explicit type hints for mutableStateOf
    val pageLayoutState = remember { mutableStateOf<PageLayout>(PageLayout.MODERN) }
    var selectedPageLayout by pageLayoutState

    val spacingState = remember { mutableStateOf<SpacingProfile>(SpacingProfile.NORMAL) }
    var selectedSpacingProfile by spacingState

    val colorState = remember { mutableStateOf<InvoiceColorScheme>(InvoiceColorScheme.PROFESSIONAL) }
    var selectedColorScheme by colorState

    val totalBoxState = remember { mutableStateOf<TotalBoxStyle>(TotalBoxStyle.SUBTLE_BACKGROUND) }
    var selectedTotalBoxStyle by totalBoxState

    var enableAlternatingRows by remember { mutableStateOf(true) }
    var enableDividers by remember { mutableStateOf(true) }

    var isSaving by remember { mutableStateOf(false) }

    // Initialize from loaded settings
    LaunchedEffect(invoiceSettings) {
        invoiceSettings?.let { settings ->
            // Phase 3E: Only load fields that exist in InvoiceSettings
            // Additional fields like invoicePrefix, startingNumber, etc. are planned for Phase 4
            // prefix = settings.invoiceNumberPrefix
            footerText = settings.footerMessage
            // Other Phase 4 fields: invoicePrefix, startingNumber, includeNotes, includeTaxId, showLogo, showCompanyInfo
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
                // ──────────────────────────────────────────────────────
                // PHASE 1: PRESET SELECTOR (WIN #4)
                // ──────────────────────────────────────────────────────
                PresetSelector(
                    onPresetSelected = { preset ->
                        // Apply preset to all settings
                        val presetSettings = preset.applyTo(
                            InvoiceSettings(userId = "current_user")
                        )
                        selectedPageLayout = presetSettings.selectedPageLayout
                        selectedSpacingProfile = presetSettings.selectedSpacingProfile
                        selectedColorScheme = presetSettings.selectedColorScheme
                        selectedTotalBoxStyle = presetSettings.totalBoxStyle
                        enableAlternatingRows = presetSettings.enableAlternatingRowColors
                        enableDividers = presetSettings.enableDividers
                        footerText = presetSettings.footerMessage

                        // Save to ViewModel
                        viewModel.updatePageLayout(selectedPageLayout)
                        viewModel.updateSpacingProfile(selectedSpacingProfile)
                        viewModel.updateColorScheme(selectedColorScheme)
                        viewModel.updateTotalBoxStyle(selectedTotalBoxStyle)
                        viewModel.updateAlternatingRows(enableAlternatingRows)
                        viewModel.updateDividers(enableDividers)
                        viewModel.updateFooterText(footerText)

                        Timber.d("Applied preset: ${preset.name}")
                    },
                    modifier = Modifier.padding(top = 8.dp)
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

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

                // ──────────────────────────────────────────────────────
                // PHASE 3B: New Customization Sections
                // ──────────────────────────────────────────────────────

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Layout & Spacing Section
                PageLayoutSelector(
                    current = selectedPageLayout,
                    onSelect = {
                        selectedPageLayout = it
                        viewModel.updatePageLayout(it)
                    },
                    modifier = Modifier.padding(top = 16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                SpacingProfileSelector(
                    current = selectedSpacingProfile,
                    onSelect = {
                        selectedSpacingProfile = it
                        viewModel.updateSpacingProfile(it)
                    },
                    modifier = Modifier.padding(top = 8.dp)
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                // Colors & Appearance Section
                ColorSchemeSelector(
                    current = selectedColorScheme,
                    onSelect = {
                        selectedColorScheme = it
                        viewModel.updateColorScheme(it)
                    },
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                TotalBoxStyleSelector(
                    current = selectedTotalBoxStyle,
                    onSelect = {
                        selectedTotalBoxStyle = it
                        viewModel.updateTotalBoxStyle(it)
                    },
                    modifier = Modifier.padding(top = 8.dp)
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                // Table Styling Section
                Text(
                    "Table Styling",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )

                SettingToggle(
                    label = "Zebra Striping",
                    checked = enableAlternatingRows,
                    onCheckedChange = {
                        enableAlternatingRows = it
                        viewModel.updateAlternatingRows(it)
                    },
                    description = "Alternate row colors for better readability",
                    modifier = Modifier.padding(top = 12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                SettingToggle(
                    label = "Dividers",
                    checked = enableDividers,
                    onCheckedChange = {
                        enableDividers = it
                        viewModel.updateDividers(it)
                    },
                    description = "Show divider lines between sections",
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Save Button
                Button(
                    onClick = {
                        try {
                            isSaving = true
                            // Phase 3E: Use saveInvoiceSettings() from ViewModel
                            // This updates the loaded settings from the repository
                            viewModel.saveInvoiceSettings()
                            Timber.i("Invoice settings saved")
                            isSaving = false
                        } catch (e: Exception) {
                            Timber.e(e, "Failed to save invoice settings")
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

