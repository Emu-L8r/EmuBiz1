package com.emul8r.bizap.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emul8r.bizap.data.local.entities.InvoiceTemplate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceSettingsScreen(
    onBackClick: () -> Unit,
    viewModel: InvoiceSettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Invoice PDF Settings") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val s = state) {
                is InvoiceSettingsUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is InvoiceSettingsUiState.Error -> Text(s.message, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
                is InvoiceSettingsUiState.Success -> {
                    InvoiceSettingsContent(
                        template = s.template,
                        onUpdate = viewModel::updateSettings,
                        onReset = viewModel::resetToDefaults
                    )
                }
            }
        }
    }
}

@Composable
private fun InvoiceSettingsContent(
    template: InvoiceTemplate,
    onUpdate: (InvoiceTemplate) -> Unit,
    onReset: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Section: Layout
        Text("Layout & Style", style = MaterialTheme.typography.titleMedium)
        
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Margin", Modifier.weight(1f))
                    val isCompact = template.marginPreset == "COMPACT"
                    FilterChip(
                        selected = !isCompact,
                        onClick = { onUpdate(template.copy(marginPreset = "NORMAL")) },
                        label = { Text("Normal") }
                    )
                    Spacer(Modifier.width(8.dp))
                    FilterChip(
                        selected = isCompact,
                        onClick = { onUpdate(template.copy(marginPreset = "COMPACT")) },
                        label = { Text("Compact") }
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Font Size", Modifier.weight(1f))
                    val isLarge = template.fontSizePreset == "LARGE"
                    FilterChip(
                        selected = !isLarge,
                        onClick = { onUpdate(template.copy(fontSizePreset = "NORMAL")) },
                        label = { Text("Normal") }
                    )
                    Spacer(Modifier.width(8.dp))
                    FilterChip(
                        selected = isLarge,
                        onClick = { onUpdate(template.copy(fontSizePreset = "LARGE")) },
                        label = { Text("Large") }
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Zebra Stripes", Modifier.weight(1f))
                    Switch(
                        checked = template.showZebraStripes,
                        onCheckedChange = { onUpdate(template.copy(showZebraStripes = it)) }
                    )
                }
            }
        }

        // Section: Visibility
        Text("Company Info Visibility", style = MaterialTheme.typography.titleMedium)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                VisibilityToggle("Show Phone", template.showPhone) { onUpdate(template.copy(showPhone = it)) }
                VisibilityToggle("Show Email", template.showEmail) { onUpdate(template.copy(showEmail = it)) }
                VisibilityToggle("Show Address", template.showAddress) { onUpdate(template.copy(showAddress = it)) }
                VisibilityToggle("Show Tax ID / ABN", template.showTaxId) { onUpdate(template.copy(showTaxId = it)) }
            }
        }

        // Section: Extra Text
        Text("Notes & Footer", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = template.footerMessage,
            onValueChange = { onUpdate(template.copy(footerMessage = it)) },
            label = { Text("Footer Message") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )

        Spacer(Modifier.height(24.dp))

        OutlinedButton(
            onClick = onReset,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Reset to Defaults")
        }
        
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun VisibilityToggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
    }
}
