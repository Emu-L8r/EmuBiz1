package com.example.databaser.ui

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.databaser.R
import com.example.databaser.data.MigrationStatusStore
import com.example.databaser.viewmodel.SettingsViewModel
import com.example.databaser.viewmodel.ThemeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigate: (String) -> Unit,
    settingsViewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory),
    themeViewModel: ThemeViewModel = viewModel(factory = ThemeViewModel.Factory)
) {
    val useDarkMode by settingsViewModel.useDarkMode.collectAsState()
    val currentTheme by themeViewModel.theme.collectAsState()
    var expanded by remember { mutableStateOf(false) }
    val themes = listOf("Default", "Ocean", "Forest", "Night", "Cosmos", "Fire", "Rainbow", "System")

    // Migration status
    val context = LocalContext.current
    val statusStore = MigrationStatusStore(context)
    val status = statusStore.getStatus()
    val backupDir = statusStore.getBackupDir()

    Scaffold(topBar = { AppTopAppBar(title = stringResource(id = R.string.settings), onNavigate = onNavigate) }) {
        Column(modifier = Modifier.padding(it).padding(16.dp)) {
            // Migration status card
            Card(colors = CardDefaults.cardColors()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Migration status: ${status.name}")
                    if (!backupDir.isNullOrEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                            Text("Backups: ")
                            Spacer(modifier = Modifier.weight(1f))
                            Button(onClick = { onNavigate("manageBackups") }) {
                                Text("Manage Backups")
                            }
                        }
                        Text("Path: $backupDir", modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.padding(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Use Dark Mode")
                Spacer(modifier = Modifier.weight(1f))
                Switch(
                    checked = useDarkMode,
                    onCheckedChange = { settingsViewModel.updateDarkMode(it) },
                    enabled = currentTheme != "System"
                )
            }
            Box {
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { expanded = true },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Theme")
                    Spacer(modifier = Modifier.weight(1f))
                    Text(currentTheme)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    themes.forEach { theme ->
                        DropdownMenuItem(text = { Text(theme) }, onClick = {
                            themeViewModel.updateTheme(theme)
                            expanded = false
                        })
                    }
                }
            }
        }
    }
}
