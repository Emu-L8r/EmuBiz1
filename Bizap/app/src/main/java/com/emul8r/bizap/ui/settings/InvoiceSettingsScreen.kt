package com.emul8r.bizap.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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

/**
 * Invoice Settings Screen
 *
 * Allows users to:
 * - Select invoice theme (Canvas or HTML-to-PDF)
 * - Configure company branding
 * - Set brand colors
 * - Configure payment details
 * - Set tax configuration
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
            snackbarHostState.showSnackbar("Settings saved successfully")
        }
    }

    // Show snackbar on error
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
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
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.settings != null -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Theme Selection Section
                    item {
                        ThemeSelectionSection(
                            currentTheme = uiState.settings?.selectedTheme,
                            onThemeSelected = { viewModel.updateSelectedTheme(it) }
                        )
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
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 24.dp, bottom = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.saveSettings() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Text("Save")
                            }
                            OutlinedButton(
                                onClick = { viewModel.resetToDefaults() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Text("Reset")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ThemeSelectionSection(
    currentTheme: InvoiceTheme?,
    onThemeSelected: (InvoiceTheme) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Theme & Style", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))

            currentTheme?.let { theme ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = theme == InvoiceTheme.CANVAS,
                        onClick = { onThemeSelected(InvoiceTheme.CANVAS) }
                    )
                    Text("Canvas Style (Current)")
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = theme == InvoiceTheme.HTML_PDF,
                        onClick = { onThemeSelected(InvoiceTheme.HTML_PDF) }
                    )
                    Text("Modern HTML Style (Coming Soon)")
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
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Company Branding", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = settings.businessName,
                onValueChange = onBusinessNameChanged,
                label = { Text("Company Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = settings.businessEmail,
                onValueChange = onBusinessEmailChanged,
                label = { Text("Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = settings.businessPhone,
                onValueChange = onBusinessPhoneChanged,
                label = { Text("Phone") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = settings.businessAddress,
                onValueChange = onBusinessAddressChanged,
                label = { Text("Address") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
        }
    }
}

@Composable
fun ColorsSection(
    primaryColor: String,
    onColorChanged: (String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Colors", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))

            // Color preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(
                        color = try {
                            Color(android.graphics.Color.parseColor(primaryColor))
                        } catch (e: Exception) {
                            MaterialTheme.colorScheme.primary
                        }
                    )
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = primaryColor,
                onValueChange = onColorChanged,
                label = { Text("Primary Color (Hex)") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("#6B4C9A") }
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
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Payment", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = paymentTermsDays.toString(),
                onValueChange = { value ->
                    value.toIntOrNull()?.let { onPaymentTermsChanged(it) }
                },
                label = { Text("Payment Terms (Days)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = bankName,
                onValueChange = onBankNameChanged,
                label = { Text("Bank Name") },
                modifier = Modifier.fillMaxWidth()
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
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Tax", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = taxRate.toString(),
                onValueChange = { value ->
                    value.toDoubleOrNull()?.let { onTaxRateChanged(it) }
                },
                label = { Text("Tax Rate (%)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = taxName,
                onValueChange = onTaxNameChanged,
                label = { Text("Tax Name (e.g., GST, VAT)") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}



