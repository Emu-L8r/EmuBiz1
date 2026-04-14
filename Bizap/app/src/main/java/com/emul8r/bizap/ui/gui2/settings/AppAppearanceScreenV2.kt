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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import com.emul8r.bizap.domain.model.DisplayMode
import com.emul8r.bizap.domain.model.ThemePreference
import com.emul8r.bizap.ui.theme.AppTheme
import com.emul8r.bizap.ui.landing.GuiMode
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
                    item {
                        ThemeStyleCard(
                            currentStyle = uiState.themeStyle,
                            onStyleChange = { viewModel.updateThemeStyle(it) }
                        )
                    }

                    item {
                        ThemePreferenceCard(
                            currentPreference = uiState.themePreference,
                            onPreferenceChange = { viewModel.updateThemePreference(it) }
                        )
                    }

                    item {
                        // ── GUI Mode Selection ──
                        GuiModeCard(
                            businessId = businessId,
                            context = context
                        )
                    }

                    item {
                        // ── Theme Customization ──
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onThemeSettingsClick() }
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Advanced Color Themes",
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                    Text(
                                        "Customize colors for your style",
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
private fun ThemeStyleCard(
    currentStyle: AppTheme,
    onStyleChange: (AppTheme) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Theme Style",
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Choose between Classic (Material 2) or Modern (Material 3) design",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AppTheme.entries.forEach { style ->
                    FilterChip(
                        selected = currentStyle == style,
                        onClick = { onStyleChange(style) },
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
                "Theme Mode",
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Control light or dark appearance",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(
                    "Display Mode",
                    style = MaterialTheme.typography.labelMedium
                )
                // Coming Soon Badge
                androidx.compose.material3.Surface(
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                ) {
                    Text(
                        "Coming Soon",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
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
                        onClick = { /* Disabled for now */ },
                        label = { Text(mode.name) },
                        enabled = false,  // Disable clicking
                        modifier = Modifier.alpha(0.6f)  // Show as disabled
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

