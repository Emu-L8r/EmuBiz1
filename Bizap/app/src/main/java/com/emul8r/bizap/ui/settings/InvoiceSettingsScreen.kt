package com.emul8r.bizap.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emul8r.bizap.domain.model.InvoiceSettings
import com.emul8r.bizap.domain.model.InvoiceTheme
import timber.log.Timber

/**
 * Invoice Settings Screen - Phase 5 Polish Edition
 *
 * Allows users to:
 * - Select invoice theme (Canvas or HTML-to-PDF)
 * - Configure company branding
 * - Set brand colors
 * - Configure payment details
 * - Set tax configuration
 *
 * Phase 5 Improvements:
 * - Enhanced visual hierarchy
 * - Better spacing and separation
 * - Improved typography
 * - Helper text and descriptions
 * - Polish and refinement throughout
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceSettingsScreen(
    viewModel: InvoiceSettingsViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show snackbar on save success
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            snackbarHostState.showSnackbar("✅ Settings saved successfully")
            Timber.d("Settings saved successfully")
        }
    }

    // Show snackbar on error
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar("⚠️ Error: $error")
            Timber.e("Settings error: $error")
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
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when {
            uiState.isLoading && uiState.settings == null -> {
                // Initial loading state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Loading settings...", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            uiState.error != null && uiState.settings == null -> {
                // Error state with retry option
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Error",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Failed to Load Settings",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            uiState.error ?: "Unknown error",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { viewModel.retryLoadSettings() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Retry")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = onBack,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Go Back")
                        }
                    }
                }
            }
            uiState.settings != null -> {
                // Settings loaded successfully
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    // Welcome/Info Section
                    item {
                        InfoSection()
                    }

                    // Theme Selection Section
                    item {
                        ThemeSelectionSection(
                            currentTheme = uiState.settings?.selectedTheme,
                            onThemeSelected = { viewModel.updateSelectedTheme(it) }
                        )
                    }

                    // Theme Preview Section
                    item {
                        uiState.settings?.let { settings ->
                            InvoiceThemePreview(
                                selectedTheme = settings.selectedTheme,
                                companyName = settings.businessName,
                                primaryColor = settings.primaryColor
                            )
                        }
                    }

                    // Divider
                    item {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    }

                    // Company Branding Section
                    item {
                        uiState.settings?.let { settings ->
                            CompanyBrandingSection(
                                settings = settings,
                                onBusinessNameChanged = { viewModel.updateBusinessName(it) },
                                onBusinessEmailChanged = { viewModel.updateBusinessEmail(it) },
                                onBusinessPhoneChanged = { viewModel.updateBusinessPhone(it) },
                                onBusinessAddressChanged = { viewModel.updateBusinessAddress(it) }
                            )
                        }
                    }

                    // Colors Section
                    item {
                        uiState.settings?.let { settings ->
                            ColorsSection(
                                primaryColor = settings.primaryColor,
                                onColorChanged = { viewModel.updatePrimaryColor(it) }
                            )
                        }
                    }

                    // Divider
                    item {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    }

                    // Payment Section
                    item {
                        uiState.settings?.let { settings ->
                            PaymentSection(
                                paymentTermsDays = settings.paymentTermsDays,
                                bankName = settings.bankName ?: "",
                                onPaymentTermsChanged = { viewModel.updatePaymentTermsDays(it) },
                                onBankNameChanged = { viewModel.updateBankName(it) }
                            )
                        }
                    }

                    // Tax Section
                    item {
                        uiState.settings?.let { settings ->
                            TaxSection(
                                taxRate = settings.taxRate,
                                taxName = settings.taxName,
                                onTaxRateChanged = { viewModel.updateTaxRate(it) },
                                onTaxNameChanged = { viewModel.updateTaxName(it) }
                            )
                        }
                    }

                    // Action Buttons
                    item {
                        ActionButtonsSection(
                            onSave = { viewModel.saveSettings() },
                            onReset = { viewModel.resetToDefaults() }
                        )
                    }

                    // Bottom spacing
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoSection() {
    Column {
        Text(
            text = "Configure Invoice Settings",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Customize how your invoices look and include your business information",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ThemeSelectionSection(
    currentTheme: InvoiceTheme?,
    onThemeSelected: (InvoiceTheme) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Invoice Theme",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "Select how your invoices will be styled",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            currentTheme?.let { theme ->
                // Canvas Style Option
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (theme == InvoiceTheme.CANVAS)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = if (theme == InvoiceTheme.CANVAS)
                        androidx.compose.foundation.BorderStroke(
                            2.dp,
                            MaterialTheme.colorScheme.primary
                        )
                    else
                        null
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        RadioButton(
                            selected = theme == InvoiceTheme.CANVAS,
                            onClick = { onThemeSelected(InvoiceTheme.CANVAS) }
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Canvas Style",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                            )
                            Text(
                                "Clean, traditional invoice design",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (theme == InvoiceTheme.CANVAS) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // HTML-to-PDF Option
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (theme == InvoiceTheme.HTML_PDF)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = if (theme == InvoiceTheme.HTML_PDF)
                        androidx.compose.foundation.BorderStroke(
                            2.dp,
                            MaterialTheme.colorScheme.primary
                        )
                    else
                        null
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        RadioButton(
                            selected = theme == InvoiceTheme.HTML_PDF,
                            onClick = { onThemeSelected(InvoiceTheme.HTML_PDF) }
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Modern HTML Style",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                            )
                            Text(
                                "Professional, modern design (Phase 6)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (theme == InvoiceTheme.HTML_PDF) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CompanyBrandingSection(
    settings: InvoiceSettings,
    onBusinessNameChanged: (String) -> Unit,
    onBusinessEmailChanged: (String) -> Unit,
    onBusinessPhoneChanged: (String) -> Unit,
    onBusinessAddressChanged: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Company Branding",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "Your business information displayed on invoices",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            OutlinedTextField(
                value = settings.businessName,
                onValueChange = onBusinessNameChanged,
                label = { Text("Company Name") },
                placeholder = { Text("e.g., Acme Corporation") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                singleLine = true,
                isError = false
            )

            OutlinedTextField(
                value = settings.businessEmail,
                onValueChange = onBusinessEmailChanged,
                label = { Text("Email Address") },
                placeholder = { Text("e.g., contact@company.com") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                singleLine = true,
                isError = false
            )

            OutlinedTextField(
                value = settings.businessPhone,
                onValueChange = onBusinessPhoneChanged,
                label = { Text("Phone Number") },
                placeholder = { Text("e.g., +1 (555) 123-4567") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                singleLine = true,
                isError = false
            )

            OutlinedTextField(
                value = settings.businessAddress,
                onValueChange = onBusinessAddressChanged,
                label = { Text("Business Address") },
                placeholder = { Text("e.g., 123 Main St, City, State 12345") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 3,
                isError = false
            )
        }
    }
}

@Composable
fun ColorsSection(
    primaryColor: String,
    onColorChanged: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Brand Colors",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "Customize the primary color used in your invoices",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            // Color preview
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = try {
                        Color(android.graphics.Color.parseColor(primaryColor))
                    } catch (e: Exception) {
                        MaterialTheme.colorScheme.primary
                    }
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Preview Color",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White
                    )
                }
            }

            OutlinedTextField(
                value = primaryColor,
                onValueChange = onColorChanged,
                label = { Text("Hex Color Code") },
                placeholder = { Text("#6B4C9A") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                supportingText = { Text("Format: #RRGGBB (e.g., #FF5722)") }
            )
        }
    }
}

@Composable
fun PaymentSection(
    paymentTermsDays: Int,
    bankName: String,
    onPaymentTermsChanged: (Int) -> Unit,
    onBankNameChanged: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Payment Terms",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "Default payment terms and bank information",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            OutlinedTextField(
                value = paymentTermsDays.toString(),
                onValueChange = { value ->
                    value.toIntOrNull()?.let { onPaymentTermsChanged(it) }
                },
                label = { Text("Payment Terms") },
                placeholder = { Text("30") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                singleLine = true,
                supportingText = { Text("Days from invoice date") }
            )

            OutlinedTextField(
                value = bankName,
                onValueChange = onBankNameChanged,
                label = { Text("Bank Name (Optional)") },
                placeholder = { Text("e.g., First National Bank") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}

@Composable
fun TaxSection(
    taxRate: Double,
    taxName: String,
    onTaxRateChanged: (Double) -> Unit,
    onTaxNameChanged: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Tax Configuration",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "Default tax rate applied to invoices",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            OutlinedTextField(
                value = taxRate.toString(),
                onValueChange = { value ->
                    value.toDoubleOrNull()?.let { onTaxRateChanged(it) }
                },
                label = { Text("Tax Rate") },
                placeholder = { Text("10.0") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                singleLine = true,
                supportingText = { Text("Percentage (0-100)") }
            )

            OutlinedTextField(
                value = taxName,
                onValueChange = onTaxNameChanged,
                label = { Text("Tax Name") },
                placeholder = { Text("e.g., GST, VAT, Sales Tax") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}

@Composable
private fun ActionButtonsSection(
    onSave: () -> Unit,
    onReset: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onSave,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Save Settings")
        }

        OutlinedButton(
            onClick = onReset,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Reset to Defaults")
        }
    }
}
