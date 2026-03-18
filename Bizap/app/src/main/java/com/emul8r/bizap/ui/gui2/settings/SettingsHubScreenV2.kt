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
import androidx.compose.material.icons.filled.AutoAwesomeMotion
import androidx.compose.material.icons.filled.Tune
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
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsHubScreenV2(
    onBusinessProfileClick: () -> Unit,
    onThemeSettingsClick: () -> Unit,
    onAppSettingsClick: () -> Unit = {},
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
                SettingsContent(
                    businessProfile = state.businessProfile,
                    onBusinessProfileClick = onBusinessProfileClick,
                    onThemeSettingsClick = onThemeSettingsClick,
                    onAppSettingsClick = onAppSettingsClick,
                    onSwitchToGui1 = onSwitchToGui1,
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
    onThemeSettingsClick: () -> Unit,
    onAppSettingsClick: () -> Unit = {},
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
            description = businessProfile.businessName,
            onClick = onBusinessProfileClick
        )

        Divider()

        // Appearance Section
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

        Divider()

        // App Info Section
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

        // GUI Switch Section
        Divider()
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

sealed interface SettingsUiStateV2 {
    object Loading : SettingsUiStateV2
    data class Error(val message: String) : SettingsUiStateV2
    data class Success(val businessProfile: BusinessProfile) : SettingsUiStateV2
}




