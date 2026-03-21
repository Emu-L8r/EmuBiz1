package com.emul8r.bizap.presentation.ui.components.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.emul8r.bizap.domain.model.DisplayMode

/**
 * Settings card for choosing how list data (invoices, customers) is presented.
 */
@Composable
fun DisplayModeSettingsCard(
    currentMode: DisplayMode,
    onModeSelected: (DisplayMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Display Mode",
                    style = MaterialTheme.typography.titleMedium
                )
                // Coming Soon Badge
                Surface(
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
            Spacer(Modifier.height(12.dp))

            DisplayMode.entries.forEach { mode ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(0.6f),  // Show as disabled
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = mode.label(),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    RadioButton(
                        selected = mode == currentMode,
                        onClick = { /* Disabled for now */ },
                        enabled = false  // Disable clicking
                    )
                }
            }
        }
    }
}

private fun DisplayMode.label() = when (this) {
    DisplayMode.LIST_VIEW -> "List view"
    DisplayMode.GRID_VIEW -> "Grid view"
    DisplayMode.CARD_VIEW -> "Card view"
}
