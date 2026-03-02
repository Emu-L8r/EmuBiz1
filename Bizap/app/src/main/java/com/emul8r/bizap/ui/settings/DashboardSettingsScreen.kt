package com.emul8r.bizap.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emul8r.bizap.domain.model.DashboardSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardSettingsScreen(
    onBackClick: () -> Unit,
    viewModel: DashboardSettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard Settings") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Analytics Visibility",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                "Choose which cards and charts are displayed on your main dashboard.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DashboardToggle(
                        label = "Show Revenue Trend Graph",
                        checked = settings.showRevenueTrend,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(showRevenueTrend = it)) }
                    )
                    DashboardToggle(
                        label = "Show MTD Revenue Card",
                        checked = settings.showMtdCard,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(showMtdCard = it)) }
                    )
                    DashboardToggle(
                        label = "Show YTD Revenue Card",
                        checked = settings.showYtdCard,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(showYtdCard = it)) }
                    )
                    DashboardToggle(
                        label = "Show Total Clients Card",
                        checked = settings.showTotalClientsCard,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(showTotalClientsCard = it)) }
                    )
                    DashboardToggle(
                        label = "Show Currency Breakdown",
                        checked = settings.showCurrencyBreakdown,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(showCurrencyBreakdown = it)) }
                    )
                    DashboardToggle(
                        label = "Show Recent Invoices",
                        checked = settings.showRecentInvoices,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(showRecentInvoices = it)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = { viewModel.resetToDefaults() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Reset to Defaults")
            }
        }
    }
}

@Composable
private fun DashboardToggle(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
