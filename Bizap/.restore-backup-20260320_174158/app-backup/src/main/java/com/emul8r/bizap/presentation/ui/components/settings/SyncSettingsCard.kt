package com.emul8r.bizap.presentation.ui.components.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Settings card for controlling background sync behaviour.
 *
 * @param autoSyncEnabled        Whether automatic background sync is on.
 * @param syncFrequencyMinutes   How often sync runs when [autoSyncEnabled] is true.
 * @param onAutoSyncToggled      Callback when the master sync switch changes.
 * @param onFrequencySelected    Callback when the user picks a different frequency.
 */
@Composable
fun SyncSettingsCard(
    autoSyncEnabled: Boolean,
    syncFrequencyMinutes: Int,
    onAutoSyncToggled: (Boolean) -> Unit,
    onFrequencySelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Sync & Storage",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Auto sync", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "Sync data automatically in the background",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = autoSyncEnabled,
                    onCheckedChange = onAutoSyncToggled
                )
            }

            if (autoSyncEnabled) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Sync frequency",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(8.dp))

                val frequencyOptions = listOf(5, 15, 30, 60)
                frequencyOptions.forEach { minutes ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (minutes < 60) "Every $minutes minutes"
                                   else "Every hour",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        RadioButton(
                            selected = syncFrequencyMinutes == minutes,
                            onClick = { onFrequencySelected(minutes) }
                        )
                    }
                }
            }
        }
    }
}
