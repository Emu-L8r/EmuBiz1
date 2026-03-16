package com.emul8r.bizap.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * One-time dialog shown on first app launch warning users that all data is stored
 * locally on their device with no cloud backup.
 *
 * This dialog only appears once; after the user taps "I Understand" it is never
 * shown again (the preference is persisted in DataStore via [LandingViewModel]).
 */
@Composable
fun FirstLaunchWarningDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},  // Not dismissible by back-press; must tap button
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(40.dp)
            )
        },
        title = {
            Text(
                text = "Important: Data Storage Warning",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "⚠️ All your data is stored locally on this device only.",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "There is NO cloud backup. If you lose your phone, delete the app, or factory-reset your device, your data will be gone forever and cannot be recovered.",
                    style = MaterialTheme.typography.bodyMedium
                )
                HorizontalDivider()
                Text(
                    text = "📤 We strongly recommend exporting your data as CSV regularly via the invoice detail screen.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "You are responsible for maintaining your own backups.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("I Understand", modifier = Modifier.padding(vertical = 4.dp))
            }
        }
    )
}
