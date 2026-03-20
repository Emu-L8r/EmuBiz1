package com.emul8r.bizap.ui.gui2.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emul8r.bizap.domain.model.DisplayMode
import com.emul8r.bizap.domain.model.ThemePreference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppAppearanceScreenV2(
    businessId: Long,
    onBack: () -> Unit,
    onBusinessProfileClick: () -> Unit = {},
    onHelpClick: () -> Unit = {},
    onBackupRestoreClick: () -> Unit = {},
    viewModel: AppAppearanceViewModelV2 = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Theme & Display") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
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
            uiState.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        uiState.error ?: "Unknown error",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        ThemePreferenceCard(
                            currentPreference = uiState.themePreference,
                            onPreferenceChange = { viewModel.updateThemePreference(it) }
                        )
                    }

                    item {
                        DisplayModeCard(
                            currentMode = uiState.displayMode,
                            onModeChange = { viewModel.updateDisplayMode(it) }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "App Settings",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    item {
                        SettingsOptionCard(
                            title = "Business Profile",
                            onClick = onBusinessProfileClick
                        )
                    }

                    item {
                        SettingsOptionCard(
                            title = "Backup & Restore",
                            onClick = onBackupRestoreClick
                        )
                    }

                    item {
                        SettingsOptionCard(
                            title = "Help",
                            onClick = onHelpClick
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemePreferenceCard(
    currentPreference: ThemePreference,
    onPreferenceChange: (ThemePreference) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Theme",
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemePreference.entries.forEach { pref ->
                    FilterChip(
                        selected = currentPreference == pref,
                        onClick = { onPreferenceChange(pref) },
                        label = { Text(pref.name) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DisplayModeCard(
    currentMode: DisplayMode,
    onModeChange: (DisplayMode) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Display Mode",
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DisplayMode.entries.forEach { mode ->
                    FilterChip(
                        selected = currentMode == mode,
                        onClick = { onModeChange(mode) },
                        label = { Text(mode.name) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsOptionCard(
    title: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title)
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null
            )
        }
    }
}
