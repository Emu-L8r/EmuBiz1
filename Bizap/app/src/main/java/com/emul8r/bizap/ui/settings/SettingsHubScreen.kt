package com.emul8r.bizap.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.emul8r.bizap.ui.navigation.Screen

@Composable
fun SettingsHubScreen(onNavigate: (Screen) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        SettingsItem(
            icon = Icons.Default.Business,
            title = "Business Profile",
            subtitle = "Manage your business details for invoices",
            onClick = { onNavigate(Screen.BusinessProfile) }
        )
        SettingsItem(
            icon = Icons.Default.Description,
            title = "Invoice PDF Settings",
            subtitle = "Customize the layout and appearance of your PDFs",
            onClick = { onNavigate(Screen.InvoiceSettings) }
        )
        SettingsItem(
            icon = Icons.Default.Language,
            title = "Currency & Locale",
            subtitle = "Set your primary currency and regional preferences",
            onClick = { onNavigate(Screen.CurrencySettings) }
        )
        SettingsItem(
            icon = Icons.Default.DashboardCustomize,
            title = "Dashboard Settings",
            subtitle = "Customize your analytics and dashboard cards",
            onClick = { onNavigate(Screen.DashboardSettings) }
        )
        SettingsItem(
            icon = Icons.Default.Storage,
            title = "Data & Backup",
            subtitle = "Export, import, or manage your data",
            onClick = { onNavigate(Screen.DataSettings) }
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
