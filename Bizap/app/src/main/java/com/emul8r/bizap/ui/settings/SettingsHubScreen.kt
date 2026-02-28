package com.emul8r.bizap.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.emul8r.bizap.ui.navigation.Screen

@Composable
fun SettingsHubScreen(onNavigate: (Screen) -> Unit) {
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
            subtitle = "Customize the look and feel of the app",
            onClick = { onNavigate(Screen.ThemeSettings) }
        )
        SettingsItem(
            icon = Icons.AutoMirrored.Filled.PlaylistAdd,
            title = "Pre-filled Items",
            subtitle = "Manage your saved line items for faster invoicing",
            onClick = { onNavigate(Screen.PrefilledItems) }
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
