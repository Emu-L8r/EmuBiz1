package com.example.databaser.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.databaser.R
import com.example.databaser.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavHostController,
    onBackClick: () -> Unit,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
) {
    val useDarkMode by settingsViewModel.useDarkMode.collectAsState()
    val useCompactMode by settingsViewModel.useCompactMode.collectAsState()
    val currentTheme by settingsViewModel.theme.collectAsState()
    val currentDateFormat by settingsViewModel.dateFormat.collectAsState()

    var themeExpanded by remember { mutableStateOf(false) }
    val themes = listOf("Default", "Indigo", "Emerald", "Amber", "Crimson", "Forest", "Ocean", "System")

    var dateFormatExpanded by remember { mutableStateOf(false) }
    val dateFormats = listOf("dd/MM/yyyy", "MM/dd/yyyy", "yyyy/MM/dd")

    Scaffold(
        topBar = { 
            AppTopAppBar(
                title = stringResource(id = R.string.settings), 
                navController = navController,
                canNavigateBack = true,
                onNavigateUp = onBackClick
            ) 
        }
    ) {
        Column(modifier = Modifier.padding(it).padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Use Dark Mode")
                Spacer(modifier = Modifier.weight(1f))
                Switch(
                    checked = useDarkMode,
                    onCheckedChange = { settingsViewModel.updateDarkMode(it) },
                    enabled = currentTheme != "System"
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Use Compact Mode")
                Spacer(modifier = Modifier.weight(1f))
                Switch(
                    checked = useCompactMode,
                    onCheckedChange = { settingsViewModel.updateCompactMode(it) }
                )
            }
            Box {
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { themeExpanded = true },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Theme")
                    Spacer(modifier = Modifier.weight(1f))
                    Text(currentTheme)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
                DropdownMenu(
                    expanded = themeExpanded,
                    onDismissRequest = { themeExpanded = false }
                ) {
                    themes.forEach { theme ->
                        DropdownMenuItem(text = { Text(theme) }, onClick = { 
                            settingsViewModel.updateTheme(theme)
                            themeExpanded = false
                        })
                    }
                }
            }
            Box {
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { dateFormatExpanded = true },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Date Format")
                    Spacer(modifier = Modifier.weight(1f))
                    Text(currentDateFormat)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
                DropdownMenu(
                    expanded = dateFormatExpanded,
                    onDismissRequest = { dateFormatExpanded = false }
                ) {
                    dateFormats.forEach { dateFormat ->
                        DropdownMenuItem(text = { Text(dateFormat) }, onClick = { 
                            settingsViewModel.updateDateFormat(dateFormat)
                            dateFormatExpanded = false
                        })
                    }
                }
            }
        }
    }
}
