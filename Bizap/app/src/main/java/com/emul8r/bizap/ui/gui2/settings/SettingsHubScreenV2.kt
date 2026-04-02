package com.emul8r.bizap.ui.gui2.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emul8r.bizap.domain.model.BusinessProfile
import com.emul8r.bizap.ui.gui2.common.LoadingIndicatorV2
import com.emul8r.bizap.ui.gui2.common.ErrorStateV2

/**
 * GUI2 Settings Hub Screen
 * Central location for app settings and preferences.
 * 
 * Phase 4 Update: Consolidated "Theme" and "App Settings" into single "App Appearance" card.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsHubScreenV2(
    onBusinessProfileClick: () -> Unit,
    onAppAppearanceClick: () -> Unit,
    onInvoiceSettingsClick: () -> Unit = {},
    onHelpClick: () -> Unit = {},
    onRiskDashboardClick: () -> Unit = {},
    onPaymentAnalyticsClick: () -> Unit = {},
    onRevenueDashboardClick: () -> Unit = {},
    onDunningNoticesClick: () -> Unit = {},
    onPrefilledItemsClick: () -> Unit = {},
    onBackupRestoreClick: () -> Unit = {},
    onBack: () -> Unit,
    viewModel: SettingsHubViewModelV2 = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is SettingsUiStateV2.Loading -> {
                LoadingIndicatorV2(modifier = Modifier.padding(paddingValues))
            }
            is SettingsUiStateV2.Error -> {
                ErrorStateV2(
                    message = state.message,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            is SettingsUiStateV2.Success -> {
                SettingsContent(
                    businessProfile = state.businessProfile,
                    onBusinessProfileClick = onBusinessProfileClick,
                    onAppAppearanceClick = onAppAppearanceClick,
                    onInvoiceSettingsClick = onInvoiceSettingsClick,
                    onHelpClick = onHelpClick,
                    onRiskDashboardClick = onRiskDashboardClick,
                    onPaymentAnalyticsClick = onPaymentAnalyticsClick,
                    onRevenueDashboardClick = onRevenueDashboardClick,
                    onDunningNoticesClick = onDunningNoticesClick,
                    onPrefilledItemsClick = onPrefilledItemsClick,
                    onBackupRestoreClick = onBackupRestoreClick,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun SettingsContent(
    businessProfile: BusinessProfile,
    onBusinessProfileClick: () -> Unit,
    onAppAppearanceClick: () -> Unit,
    onInvoiceSettingsClick: () -> Unit = {},
    onHelpClick: () -> Unit = {},
    onRiskDashboardClick: () -> Unit = {},
    onPaymentAnalyticsClick: () -> Unit = {},
    onRevenueDashboardClick: () -> Unit = {},
    onDunningNoticesClick: () -> Unit = {},
    onPrefilledItemsClick: () -> Unit = {},
    onBackupRestoreClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Business Profile Section
        Text(
            text = "Business",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 8.dp)
        )

        SettingsCardV2(
            icon = Icons.Default.Business,
            title = "Business Profile",
            description = businessProfile.businessName ?: "Not Set",
            onClick = onBusinessProfileClick
        )

        HorizontalDivider()

        // Appearance Section
        Text(
            text = "Appearance",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 8.dp)
        )

        SettingsCardV2(
            icon = Icons.Default.Palette,
            title = "App Appearance",
            description = "Theme, display mode, colors, and presets",
            onClick = onAppAppearanceClick
        )

        SettingsCardV2(
            icon = Icons.Default.Description,
            title = "PDF Settings",
            description = "PDF template, styling, payment terms, and tax configuration",
            onClick = onInvoiceSettingsClick
        )

        SettingsCardV2(
            icon = Icons.AutoMirrored.Filled.HelpOutline,
            title = "Help & About",
            description = "App information, version, and support",
            onClick = onHelpClick
        )

        HorizontalDivider()

        // Analytics Section
        Text(
            text = "Analytics",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 8.dp)
        )

        SettingsCardV2(
            icon = Icons.AutoMirrored.Filled.ShowChart,
            title = "Risk Dashboard",
            description = "View customer risk and outstanding invoice analytics",
            onClick = onRiskDashboardClick
        )

        SettingsCardV2(
            icon = Icons.Default.BarChart,
            title = "Payment Analytics",
            description = "Analyse payment trends and cash flow forecasts",
            onClick = onPaymentAnalyticsClick
        )

        SettingsCardV2(
            icon = Icons.AutoMirrored.Filled.TrendingUp,
            title = "Revenue Dashboard",
            description = "View revenue metrics and business performance",
            onClick = onRevenueDashboardClick
        )

        SettingsCardV2(
            icon = Icons.Default.Notifications,
            title = "Dunning Notices",
            description = "Manage overdue invoice payment reminders",
            onClick = onDunningNoticesClick
        )

        HorizontalDivider()

        // Management Section
        Text(
            text = "Management",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 8.dp)
        )

        SettingsCardV2(
            icon = Icons.AutoMirrored.Filled.PlaylistAdd,
            title = "Pre-filled Items",
            description = "Manage your saved line items for faster invoicing",
            onClick = onPrefilledItemsClick
        )

        SettingsCardV2(
            icon = Icons.Default.Backup,
            title = "Backup & Restore",
            description = "Export or restore your app database",
            onClick = onBackupRestoreClick
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SettingsCardV2(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
