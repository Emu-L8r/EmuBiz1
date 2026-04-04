package com.emul8r.bizap.ui.landing

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Dialog shown to Classic (GUI1) users encouraging them to migrate to the Modern (GUI2) interface.
 *
 * GUI1 is scheduled for retirement in **June 2027**. This dialog informs users and
 * gives them a one-tap option to switch immediately.
 *
 * Display conditions (caller's responsibility):
 * - Show at most once per session
 * - Persist a "has seen migration dialog" flag in DataStore to avoid repeating
 *
 * @param onSwitchNow Called when the user taps "Switch Now" — caller should persist
 *                    [AppTheme.MODERN] and navigate accordingly.
 * @param onLater     Called when the user taps "Later" — dismiss the dialog.
 */
@Composable
fun GuiMigrationDialog(
    onSwitchNow: () -> Unit,
    onLater: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onLater,
        title = {
            Text(text = "Switch to Modern Interface")
        },
        text = {
            Column {
                Text(
                    text = "The Classic interface will be retired in June 2027. " +
                           "Ready to switch to the modern interface?"
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "✨ Benefits:\n" +
                           "• Faster performance\n" +
                           "• Better Material Design 3\n" +
                           "• More features & analytics\n" +
                           "• All your data is preserved"
                )
            }
        },
        confirmButton = {
            Button(onClick = onSwitchNow) {
                Text("Switch Now")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onLater) {
                Text("Later")
            }
        }
    )
}
