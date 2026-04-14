package com.emul8r.bizap.ui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.emul8r.bizap.ui.landing.GuiMode
import com.emul8r.bizap.ui.theme.Spacing

/**
 * GUI Mode Selector for TopAppBar
 * Displays 3 small buttons (1, 2, 3) to switch between GUIs
 * Placed in top-right corner for easy access from any screen
 */
@Composable
fun GuiModeSwitcher(
    currentMode: GuiMode,
    onGui1Click: () -> Unit,
    onGui2Click: () -> Unit,
    onGui3Click: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(end = Spacing.md)
    ) {
        Row(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // GUI1 Button
            SwitchButton(
                label = "1",
                isSelected = currentMode == GuiMode.GUI1,
                onClick = onGui1Click,
                isMatrix = false
            )

            // GUI2 Button
            SwitchButton(
                label = "2",
                isSelected = currentMode == GuiMode.GUI2,
                onClick = onGui2Click,
                isMatrix = false
            )

            // GUI3 Button
            SwitchButton(
                label = "3",
                isSelected = currentMode == GuiMode.GUI3,
                onClick = onGui3Click,
                isMatrix = true
            )
        }
    }
}

@Composable
private fun SwitchButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    isMatrix: Boolean = false,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = when {
                        isSelected && isMatrix -> androidx.compose.ui.graphics.Color(0xFF00DD00).copy(alpha = 0.9f)
                        isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                        else -> MaterialTheme.colorScheme.surfaceContainerLow
                    },
                    shape = RoundedCornerShape(6.dp)
                )
                .padding(6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected && isMatrix) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

