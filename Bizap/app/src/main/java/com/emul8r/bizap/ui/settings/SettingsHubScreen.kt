package com.emul8r.bizap.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AutoAwesomeMotion
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import com.emul8r.bizap.ui.gui2.common.ErrorStateV2
import com.emul8r.bizap.ui.gui2.common.LoadingIndicatorV2
import com.emul8r.bizap.ui.gui2.settings.SettingsHubViewModelV2
import com.emul8r.bizap.ui.gui2.settings.SettingsUiStateV2
import com.emul8r.bizap.ui.landing.GuiMode
import com.emul8r.bizap.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsHubScreen(
    guiMode: GuiMode = GuiMode.GUI1,
    onNavigate: (Screen) -> Unit = {},
    onBusinessProfileClick: () -> Unit = {},
    onThemeSettingsClick: () -> Unit = {},
    onAppSettingsClick: () -> Unit = {},
    onHelpClick: () -> Unit = {},
    onBack: () -> Unit = {},
    onSwitchToGui1: () -> Unit = {},
    onSwitchToGui2: () -> Unit = {},
) {
    when (guiMode) {
        GuiMode.GUI1 -> SettingsHubScreenV1Content(onNavigate = onNavigate, onSwitchToGui2 = onSwitchToGui2)
        GuiMode.GUI2 -> SettingsHubScreenV2Content(
            onBusinessProfileClick = onBusinessProfileClick,
            onThemeSettingsClick = onThemeSettingsClick,
            onAppSettingsClick = onAppSettingsClick,
            onHelpClick = onHelpClick,
            onBack = onBack,
            onSwitchToGui1 = onSwitchToGui1,
        )
    }
}

@Composable
private fun SettingsHubScreenV1Content(
    onNavigate: (Screen) -> Unit,
    onSwitchToGui2: () -> Unit = {}
) {
    Column {
        SettingsItem(
            icon = Icons.Default.Business,
            title = "Business Profile",
            subtitle = "Manage your business details for invoices",
            onClick = { onNavigate(Screen.BusinessProfile) }
        )
        SettingsItem(
            icon = Icons.Default.Palette,
            title = "App Appearance",
            subtitle = "Customize theme, display mode, and appearance",
            onClick = { onNavigate(Screen.AppSettings) }
        )
        SettingsItem(
            icon = Icons.AutoMirrored.Filled.PlaylistAdd,
            title = "Pre-filled Items",
            subtitle = "Manage your saved line items for faster invoicing",
            onClick = { onNavigate(Screen.PrefilledItems) }
        )
        SettingsItem(
            icon = Icons.AutoMirrored.Filled.ShowChart,
            title = "Risk Dashboard",
            subtitle = "View customer risk and outstanding invoice analytics",
            onClick = { onNavigate(Screen.RiskDashboard) }
        )
        SettingsItem(
            icon = Icons.Default.BarChart,
            title = "Payment Analytics",
            subtitle = "Analyse payment trends and cash flow forecasts",
            onClick = { onNavigate(Screen.PaymentAnalytics()) }
        )
        SettingsItem(
            icon = Icons.AutoMirrored.Filled.TrendingUp,
            title = "Revenue Dashboard",
            subtitle = "View revenue metrics and business performance",
            onClick = { onNavigate(Screen.RevenueDashboard) }
        )
        SettingsItem(
            icon = Icons.Default.Notifications,
            title = "Dunning Notices",
            subtitle = "Manage overdue invoice payment reminders",
            onClick = { onNavigate(Screen.DunningNotices) }
        )
        SettingsItem(
            icon = Icons.Default.Backup,
            title = "Backup & Restore",
            subtitle = "Export or restore your app database",
            onClick = { onNavigate(Screen.BackupRestore) }
        )
        SettingsItem(
            icon = Icons.Default.Groups,
            title = "Customer Segments",
            subtitle = "View customer segmentation and analytics",
            onClick = { onNavigate(Screen.CustomerSegments) }
        )
        SettingsItem(
            icon = Icons.AutoMirrored.Filled.HelpOutline,
            title = "Help & About",
            subtitle = "App information, version, and support",
            onClick = { onNavigate(Screen.Help) }
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // ── Interface Section ──
        Text(
            text = "Interface",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
        )

        SettingsItem(
            icon = Icons.Default.AutoAwesomeMotion,
            title = "Switch to GUI2",
            subtitle = "Try the modern interface",
            onClick = onSwitchToGui2
        )
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        leadingContent = { Icon(icon, contentDescription = null) },
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsHubScreenV2Content(
    onBusinessProfileClick: () -> Unit,
    onThemeSettingsClick: () -> Unit,
    onAppSettingsClick: () -> Unit = {},
    onHelpClick: () -> Unit = {},
    onBack: () -> Unit,
    onSwitchToGui1: () -> Unit = {},
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
                SettingsV2Content(
                    businessProfile = state.businessProfile,
                    onBusinessProfileClick = onBusinessProfileClick,
                    onThemeSettingsClick = onThemeSettingsClick,
                    onAppSettingsClick = onAppSettingsClick,
                    onHelpClick = onHelpClick,
                    onSwitchToGui1 = onSwitchToGui1,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun SettingsV2Content(
    businessProfile: BusinessProfile,
    onBusinessProfileClick: () -> Unit,
    onThemeSettingsClick: () -> Unit,
    onAppSettingsClick: () -> Unit = {},
    onHelpClick: () -> Unit = {},
    onSwitchToGui1: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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

        Text(
            text = "Appearance",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 8.dp)
        )

        SettingsCardV2(
            icon = Icons.Default.Palette,
            title = "Theme",
            description = "Customize colors and appearance",
            onClick = onThemeSettingsClick
        )

        SettingsCardV2(
            icon = Icons.Default.Tune,
            title = "App Settings",
            description = "Theme mode, display, notifications and sync",
            onClick = onAppSettingsClick
        )

        SettingsCardV2(
            icon = Icons.AutoMirrored.Filled.HelpOutline,
            title = "Help & About",
            description = "App information, version, and support",
            onClick = onHelpClick
        )

        HorizontalDivider()

        Text(
            text = "About",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 8.dp)
        )

        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("App Name", style = MaterialTheme.typography.bodySmall)
                    Text("Bizap", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Version", style = MaterialTheme.typography.bodySmall)
                    Text("1.0.0", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Build Date", style = MaterialTheme.typography.bodySmall)
                    Text("March 8, 2026", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        HorizontalDivider()
        Text(
            text = "Interface",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 8.dp)
        )

        SettingsCardV2(
            icon = Icons.Default.AutoAwesomeMotion,
            title = "Switch to GUI1",
            description = "Go back to the traditional interface",
            onClick = onSwitchToGui1
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SettingsCardV2(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
