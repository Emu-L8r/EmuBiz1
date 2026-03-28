package com.emul8r.bizap.ui.gui2.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.SignalCellularOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Offline indicator component for bottom of screen.
 *
 * Shows:
 * - Offline status
 * - Pending changes count
 * - Sync button
 * - Auto-hide when online
 */
@Composable
fun OfflineIndicatorV2(
    status: ConnectionStatus = ConnectionStatus.ONLINE,
    pendingChanges: Int = 0,
    onRetrySync: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = when (status) {
            ConnectionStatus.ONLINE -> Color.Transparent
            ConnectionStatus.OFFLINE -> MaterialTheme.colorScheme.errorContainer
            ConnectionStatus.RECONNECTING -> MaterialTheme.colorScheme.tertiaryContainer
        }
    )

    val textColor by animateColorAsState(
        targetValue = when (status) {
            ConnectionStatus.ONLINE -> Color.Transparent
            ConnectionStatus.OFFLINE -> MaterialTheme.colorScheme.error
            ConnectionStatus.RECONNECTING -> MaterialTheme.colorScheme.tertiary
        }
    )

    // Only show if offline or reconnecting
    if (status == ConnectionStatus.ONLINE) {
        return
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status indicator
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (status) {
                        ConnectionStatus.OFFLINE -> Icons.Default.SignalCellularOff
                        ConnectionStatus.RECONNECTING -> Icons.Default.SignalCellularAlt
                        else -> Icons.Default.SignalCellularAlt
                    },
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(18.dp)
                )

                Text(
                    text = when (status) {
                        ConnectionStatus.OFFLINE -> "Offline${if (pendingChanges > 0) " - $pendingChanges pending" else ""}"
                        ConnectionStatus.RECONNECTING -> "Syncing..."
                        else -> "Online"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor
                )
            }

            // Retry button (only show when offline)
            if (status == ConnectionStatus.OFFLINE && pendingChanges > 0) {
                IconButton(
                    onClick = onRetrySync,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Retry sync",
                        tint = textColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

