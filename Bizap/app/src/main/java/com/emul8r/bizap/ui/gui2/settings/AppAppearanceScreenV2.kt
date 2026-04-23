package com.emul8r.bizap.ui.gui2.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import com.emul8r.bizap.domain.model.ThemePreference
import com.emul8r.bizap.ui.theme.AppTheme
import com.emul8r.bizap.ui.activities.MatrixGUIMainActivity
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppAppearanceScreenV2(
    businessId: Long,
    onBack: () -> Unit,
    onBusinessProfileClick: () -> Unit = {},
    onThemeSettingsClick: () -> Unit = {},
    onHelpClick: () -> Unit = {},
    onBackupRestoreClick: () -> Unit = {},
    viewModel: AppAppearanceViewModelV2 = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

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
                    // ── SINGLE APPEARANCE CARD ──────────────────────────────────────
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    "Appearance",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                // Interface Style (Classic vs Modern)
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("Interface Style", style = MaterialTheme.typography.labelMedium)
                                    Text(
                                        "Classic = Material 2  ·  Modern = Material 3",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        AppTheme.entries.forEach { style ->
                                            FilterChip(
                                                selected = uiState.themeStyle == style,
                                                onClick = { viewModel.updateThemeStyle(style) },
                                                label = {
                                                    Text(
                                                        when (style) {
                                                            AppTheme.CLASSIC -> "Classic"
                                                            AppTheme.MODERN -> "Modern"
                                                        }
                                                    )
                                                }
                                            )
                                        }
                                    }
                                }

                                HorizontalDivider()

                                // Light / Dark / Auto
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("Light / Dark Mode", style = MaterialTheme.typography.labelMedium)
                                    Text(
                                        "Control brightness across the whole app",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        ThemePreference.entries.forEach { pref ->
                                            FilterChip(
                                                selected = uiState.themePreference == pref,
                                                onClick = { viewModel.updateThemePreference(pref) },
                                                label = {
                                                    Text(
                                                        pref.name.lowercase()
                                                            .replaceFirstChar { it.uppercase() }
                                                    )
                                                }
                                            )
                                        }
                                    }
                                }

                                HorizontalDivider()

                                // Switch Interface (GUI Mode)
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("Switch Interface", style = MaterialTheme.typography.labelMedium)
                                    Text(
                                        "Change to a different UI experience",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    GuiModeCard(businessId = businessId, context = context)
                                }
                            }
                        }
                    }

                    // ── CUSTOM COLORS (link row) ────────────────────────────────────
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onThemeSettingsClick() }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Custom Colors",
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                    Text(
                                        "Preset themes and color picker",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // ── OTHER SETTINGS ──────────────────────────────────────────────
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Other Settings",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    item { SettingsOptionCard(title = "Business Profile", onClick = onBusinessProfileClick) }
                    item { SettingsOptionCard(title = "Backup & Restore", onClick = onBackupRestoreClick) }
                    item { SettingsOptionCard(title = "Help", onClick = onHelpClick) }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
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

@Composable
private fun GuiModeCard(
    businessId: Long,
    context: android.content.Context
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "GUI Mode",
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Choose your interface experience",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))

            // GUI1 - Classic
            GuiModeOption(
                label = "Classic (GUI1)",
                description = "Traditional activity-based interface",
                onClick = {
                    Timber.d("Settings: Switching to GUI1")
                    context.startActivity(
                        Intent(context, com.emul8r.bizap.MainActivity::class.java).apply {
                            putExtra("businessId", businessId)
                        }
                    )
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // GUI2 - Modern (Current)
            GuiModeOption(
                label = "Modern (GUI2)",
                description = "Latest features · Better performance",
                isCurrent = true,
                onClick = {} // Already on GUI2
            )

            Spacer(modifier = Modifier.height(8.dp))

            // GUI3 - Matrix
            GuiModeOption(
                label = "Matrix (GUI3) 🟢",
                description = "Cyberpunk · Immersive · Premium",
                onClick = {
                    Timber.d("Settings: Switching to GUI3 (Matrix)")
                    context.startActivity(
                        Intent(context, MatrixGUIMainActivity::class.java).apply {
                            putExtra("businessId", businessId)
                        }
                    )
                }
            )
        }
    }
}

@Composable
private fun GuiModeOption(
    label: String,
    description: String,
    isCurrent: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isCurrent) { onClick() },
        color = if (isCurrent)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surfaceContainerLow,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isCurrent)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurface
                )
                Text(
                    description,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isCurrent)
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isCurrent) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = androidx.compose.foundation.shape.CircleShape,
                    modifier = Modifier.size(24.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "✓",
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}

