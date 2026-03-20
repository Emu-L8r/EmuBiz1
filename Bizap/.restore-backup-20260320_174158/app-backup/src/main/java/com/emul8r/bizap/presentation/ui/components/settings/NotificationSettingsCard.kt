package com.emul8r.bizap.presentation.ui.components.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Settings card for toggling in-app and e-mail notifications.
 */
@Composable
fun NotificationSettingsCard(
    notificationsEnabled: Boolean,
    emailNotificationsEnabled: Boolean,
    onNotificationsToggled: (Boolean) -> Unit,
    onEmailNotificationsToggled: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Notifications",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("In-app notifications", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "Show alerts inside the app",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = onNotificationsToggled
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("E-mail reminders", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "Send reminders for overdue invoices",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = emailNotificationsEnabled,
                    onCheckedChange = onEmailNotificationsToggled,
                    enabled = notificationsEnabled
                )
            }
        }
    }
}
