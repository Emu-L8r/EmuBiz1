package com.emul8r.bizap.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.TrendingUp
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
        SettingsItem(
            icon = Icons.AutoMirrored.Filled.ShowChart,
            title = "Risk Dashboard",
            subtitle = "View customer risk and outstanding invoice analytics",
            onClick = { onNavigate(Screen.RiskDashboard) }
        )
        SettingsItem(
            icon = Icons.Default.BarChart,
            title = "Payment Analytics",
            subtitle = "Analyse payment trends and cash flow forecasts",
            onClick = { onNavigate(Screen.PaymentAnalytics) }
        )
        SettingsItem(
            icon = Icons.AutoMirrored.Filled.TrendingUp,
            title = "Revenue Dashboard",
            subtitle = "View revenue metrics and business performance",
            onClick = { onNavigate(Screen.RevenueDashboard) }
        )
        SettingsItem(
            icon = Icons.Default.Notifications,
            title = "Dunning Notices",
            subtitle = "Manage overdue invoice payment reminders",
            onClick = { onNavigate(Screen.DunningNotices) }
        )
        SettingsItem(
            icon = Icons.Default.Description,
            title = "Invoice Templates",
            subtitle = "Create and manage reusable invoice templates",
            onClick = { onNavigate(Screen.InvoiceTemplates()) }
        )
        SettingsItem(
            icon = Icons.Default.Backup,
            title = "Backup & Restore",
            subtitle = "Export or restore your app database",
            onClick = { onNavigate(Screen.BackupRestore) }
        )
        SettingsItem(
            icon = Icons.Default.Groups,
            title = "Customer Segments",
            subtitle = "View customer segmentation and analytics",
            onClick = { onNavigate(Screen.CustomerSegments) }
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
