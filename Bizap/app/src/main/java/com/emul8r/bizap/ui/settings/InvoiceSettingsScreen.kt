package com.emul8r.bizap.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emul8r.bizap.domain.model.InvoiceTheme
import com.emul8r.bizap.domain.model.HtmlInvoiceStyle
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceSettingsScreen(
    viewModel: InvoiceSettingsViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            snackbarHostState.showSnackbar("✅ Settings saved successfully")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PDF Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (uiState.isLoading && uiState.settings == null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                item {
                    InfoSection()
                }

                item {
                    ThemeSelectionSection(
                        currentTheme = uiState.settings?.selectedTheme,
                        onThemeSelected = { viewModel.updateSelectedTheme(it) }
                    )
                }

                item {
                    HtmlStyleSelectionSection(
                        selectedStyle = uiState.settings?.selectedHtmlStyle ?: HtmlInvoiceStyle.MODERN,
                        onStyleSelected = { viewModel.updateSelectedHtmlStyle(it) },
                        isActive = uiState.settings?.selectedTheme == InvoiceTheme.HTML_PDF
                    )
                }

                item {
                    uiState.settings?.let { settings ->
                        ColorsSection(
                            primaryColor = settings.primaryColor,
                            onColorChanged = { viewModel.updatePrimaryColor(it) }
                        )
                    }
                }

                item {
                    uiState.settings?.let { settings ->
                        PaymentSection(
                            paymentTermsDays = settings.paymentTermsDays,
                            onPaymentTermsChanged = { viewModel.updatePaymentTermsDays(it) }
                        )
                    }
                }

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

                item {
                    ActionButtonsSection(
                        onSave = { viewModel.saveSettings() },
                        onReset = { viewModel.resetToDefaults() }
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoSection() {
    Column {
        Text("PDF Invoice Settings", style = MaterialTheme.typography.headlineSmall)
        Text("Select your template style and customize appearance", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun ThemeSelectionSection(currentTheme: InvoiceTheme?, onThemeSelected: (InvoiceTheme) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Invoice Theme", style = MaterialTheme.typography.titleMedium)
            val theme = currentTheme ?: InvoiceTheme.CANVAS
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = theme == InvoiceTheme.CANVAS, onClick = { onThemeSelected(InvoiceTheme.CANVAS) })
                Text("Canvas Style")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = theme == InvoiceTheme.HTML_PDF, onClick = { onThemeSelected(InvoiceTheme.HTML_PDF) })
                Text("Modern HTML Style")
            }
        }
    }
}

@Composable
fun HtmlStyleSelectionSection(
    selectedStyle: HtmlInvoiceStyle,
    onStyleSelected: (HtmlInvoiceStyle) -> Unit,
    isActive: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        border = if (isActive) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("HTML Invoice Styles", style = MaterialTheme.typography.titleMedium)
            
            HtmlInvoiceStyle.values().forEach { style ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = selectedStyle == style, onClick = { onStyleSelected(style) })
                    Column {
                        Text(style.displayName, fontWeight = FontWeight.Bold)
                        Text(style.description, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
fun ColorsSection(primaryColor: String, onColorChanged: (String) -> Unit) {
    val colors = listOf("#6B4C9A", "#2E5090", "#27AE60", "#E67E22")
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Brand Color", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                colors.forEach { hex ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(android.graphics.Color.parseColor(hex)))
                            .padding(4.dp)
                    ) {
                        RadioButton(
                            selected = primaryColor == hex,
                            onClick = { onColorChanged(hex) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentSection(paymentTermsDays: Int, onPaymentTermsChanged: (Int) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Payment Terms (Days)", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = paymentTermsDays.toString(),
                onValueChange = { it.toIntOrNull()?.let(onPaymentTermsChanged) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
    }
}

@Composable
fun TaxSection(taxRate: Double, taxName: String, onTaxRateChanged: (Double) -> Unit, onTaxNameChanged: (String) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Tax Configuration", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(value = taxName, onValueChange = onTaxNameChanged, label = { Text("Tax Name") })
            OutlinedTextField(
                value = taxRate.toString(),
                onValueChange = { it.toDoubleOrNull()?.let(onTaxRateChanged) },
                label = { Text("Tax Rate") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
        }
    }
}

@Composable
private fun ActionButtonsSection(onSave: () -> Unit, onReset: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) { Text("Save Settings") }
        OutlinedButton(onClick = onReset, modifier = Modifier.fillMaxWidth()) { Text("Reset to Defaults") }
    }
}
