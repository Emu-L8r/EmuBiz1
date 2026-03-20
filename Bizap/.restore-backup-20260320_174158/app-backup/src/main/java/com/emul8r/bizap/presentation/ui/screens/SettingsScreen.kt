package com.emul8r.bizap.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emul8r.bizap.presentation.ui.components.settings.AboutSettingsCard
import com.emul8r.bizap.presentation.ui.components.settings.DisplayModeSettingsCard
import com.emul8r.bizap.presentation.ui.components.settings.NotificationSettingsCard
import com.emul8r.bizap.presentation.ui.components.settings.SyncSettingsCard
import com.emul8r.bizap.presentation.ui.components.settings.ThemeSettingsCard
import com.emul8r.bizap.presentation.viewmodel.SettingsViewModel

private val tabs = listOf("Theme & Display", "Notifications", "Sync & Storage", "About")

/**
 * Top-level Settings screen that consolidates all user preferences into four tabs:
 *
 * 1. **Theme & Display** – colour-scheme, display mode, UI density
 * 2. **Notifications** – in-app and e-mail notification toggles
 * 3. **Sync & Storage** – auto-sync and frequency controls
 * 4. **About** – app version and build information
 *
 * The screen is self-contained: it obtains its own [SettingsViewModel] via Hilt so it can
 * be dropped into any navigation graph without passing a ViewModel reference manually.
 */
@Composable
fun SettingsScreen(
    onResetConfirmed: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showResetDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // ── Tab row ────────────────────────────────────────────────────────
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex])
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) }
                )
            }
        }

        // ── Tab content ────────────────────────────────────────────────────
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (selectedTabIndex) {
                0 -> { // Theme & Display
                    item {
                        ThemeSettingsCard(
                            currentPreference = settings.themePreference,
                            onPreferenceSelected = { viewModel.setThemePreference(it) }
                        )
                    }
                    item {
                        DisplayModeSettingsCard(
                            currentMode = settings.displayMode,
                            onModeSelected = { viewModel.setDisplayMode(it) }
                        )
                    }
                }

                1 -> { // Notifications
                    item {
                        NotificationSettingsCard(
                            notificationsEnabled = settings.notificationsEnabled,
                            emailNotificationsEnabled = settings.emailNotificationsEnabled,
                            onNotificationsToggled = { viewModel.setNotificationsEnabled(it) },
                            onEmailNotificationsToggled = {
                                viewModel.setEmailNotificationsEnabled(it)
                            }
                        )
                    }
                }

                2 -> { // Sync & Storage
                    item {
                        SyncSettingsCard(
                            autoSyncEnabled = settings.autoSyncEnabled,
                            syncFrequencyMinutes = settings.syncFrequencyMinutes,
                            onAutoSyncToggled = { viewModel.setAutoSyncEnabled(it) },
                            onFrequencySelected = { viewModel.setSyncFrequencyMinutes(it) }
                        )
                    }
                }

                3 -> { // About & Advanced
                    item {
                        AboutSettingsCard()
                    }
                    item {
                        OutlinedButton(
                            onClick = { showResetDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Reset all settings to default")
                        }
                    }
                }
            }
        }
    }

    // ── Reset confirmation dialog ──────────────────────────────────────────
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset settings?") },
            text = {
                Text(
                    "This will reset all preferences to their factory defaults. " +
                    "Your data will not be affected."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetToDefaults()
                        showResetDialog = false
                        onResetConfirmed()
                    }
                ) { Text("Reset") }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("Cancel") }
            }
        )
    }
}
