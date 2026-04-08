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
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.AutoAwesomeMotion
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Tune
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emul8r.bizap.domain.model.BusinessProfile
import com.emul8r.bizap.R
import com.emul8r.bizap.ui.gui2.common.ErrorStateV2
import com.emul8r.bizap.ui.gui2.common.LoadingIndicatorV2
import com.emul8r.bizap.ui.gui2.settings.SettingsHubViewModelV2
import com.emul8r.bizap.ui.gui2.settings.SettingsUiStateV2
import com.emul8r.bizap.ui.landing.GuiMode
import com.emul8r.bizap.ui.navigation.Screen

/**
 * Settings hub screen Composable for GUI2.
 *
 * **Purpose:**
 * Central settings management screen showing all app configuration options organized by category.
 * Allows users to manage business profile, themes, notifications, backup/restore, and more.
 *
 * **Features:**
 * - Business Profile Management
 *   - Edit business name, ABN, address, contact info
 *   - Upload/change business logo
 *   - Bank account details
 * - Theme Customization
 *   - Color picker for primary/secondary/tertiary colors
 *   - Dark mode toggle
 *   - Font size adjustment
 * - Prefilled Items
 *   - Manage predefined line item descriptions
 *   - Quick-add common items to invoices
 * - Backup & Restore
 *   - Cloud backup configuration
 *   - Local backup/restore
 *   - Data export options
 * - Notifications
 *   - Payment reminders
 *   - Overdue alerts
 *   - App notifications toggle
 * - Help & Support
 *   - In-app help
 *   - Contact support
 *   - FAQ/Knowledge base
 *
 * **Organization:**
 * ```
 * ┌─────────────────────────────┐
 * │ Settings                    │ (header with back button)
 * │                             │
 * │ 💼 Business Profile         │ → Edit business details
 * │ 🎨 Themes                   │ → Customize colors/fonts
 * │ 📝 Prefilled Items          │ → Manage quick items
 * │ 💾 Backup & Restore         │ → Data management
 * │ 🔔 Notifications            │ → Alert preferences
 * │ ❓ Help                      │ → Support options
 * └─────────────────────────────┘
 * ```
 *
 * **Navigation:**
 * - Tap item → Navigate to specific settings screen
 * - Back button → Return to main app
 *
 * **Data Flow:**
 * ```
 * Screen mounts
 *     ↓
 * Load all settings from ViewModels
 *     ↓
 * Display organized menu items
 *     ↓
 * User taps category
 *     ↓
 * Navigate to specific settings screen
 *     ↓
 * User makes changes
 *     ↓
 * Changes saved automatically
 * ```
 *
 * @param onBack Callback for back button navigation
 * @param onNavigateTo Callback for navigating to specific settings section
 * @param modifier Composable modifier
 *
 * @see BusinessProfileViewModel
 * @see ThemeSettingsViewModel
 * @see PrefilledItemsViewModel
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsHubScreen(
    onBack: () -> Unit = {},
    onNavigateTo: (Screen) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // GUI2 only - GUI1 legacy code removed
    SettingsHubScreenV2Content(
        onBusinessProfileClick = { onNavigateTo(Screen.BusinessProfile) },
        onThemeSettingsClick = { onNavigateTo(Screen.ThemeSettings) },
        onAppSettingsClick = { onNavigateTo(Screen.AppSettings) },
        onHelpClick = { onNavigateTo(Screen.Help) },
        onBack = onBack,
    )
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

        @Suppress("DEPRECATION")
        SettingsCardV2(
            icon = Icons.Filled.Help,
            title = "Help & About",
            description = "App information, version, and support",
            onClick = onHelpClick
        )

        HorizontalDivider()

        Text(
            text = stringResource(R.string.label_about),
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
                    Text(stringResource(R.string.label_app_name_label), style = MaterialTheme.typography.bodySmall)
                    Text(stringResource(R.string.label_app_name_value), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
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
