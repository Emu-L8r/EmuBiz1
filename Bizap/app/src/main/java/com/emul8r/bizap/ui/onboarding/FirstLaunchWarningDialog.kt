package com.emul8r.bizap.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emul8r.bizap.ui.theme.Spacing

/**
 * One-time dialog shown on first app launch warning users that all data is stored
 * locally on their device with no cloud backup.
 *
 * REDESIGNED FOR READABILITY with:
 * - Larger, clear text (16-24sp, NO MORE bodySmall)
 * - Better color contrast (onSurface instead of onSurfaceVariant)
 * - Proper spacing and breathing room (using Spacing tokens)
 * - Clear visual hierarchy (different sizes and weights)
 * - Scrollable content (for small screens)
 *
 * This dialog only appears once; after the user taps "Got It" it is never
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
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                text = "Welcome to Bizap! ",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Spacing.lg)
            ) {
                // Intro text - Large and clear
                Text(
                    text = "✅ You're in control. Here's what that means:",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )

                HorizontalDivider(modifier = Modifier.fillMaxWidth())

                // Data Privacy Section
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    Text(
                        "",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(top = Spacing.xs)
                    )
                    Column {
                        Text(
                            text = "Complete Privacy",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(Spacing.xs))
                        Text(
                            text = "All invoices stay on YOUR device—never uploaded anywhere. Only you see your data.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                            lineHeight = 20.sp
                        )
                    }
                }

                // Your Responsibility Section
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    Text(
                        "",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(top = Spacing.xs)
                    )
                    Column {
                        Text(
                            text = "You're Responsible",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(Spacing.xs))
                        Text(
                            text = "If you lose your phone or delete the app, your data is gone. No cloud backup = no recovery.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                            lineHeight = 20.sp
                        )
                    }
                }

                // Keep Data Safe Section
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    Text(
                        "",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(top = Spacing.xs)
                    )
                    Column {
                        Text(
                            text = "Keep Your Data Safe",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(Spacing.xs))
                        Text(
                            text = "Regularly export invoices to CSV from the invoice detail screen. Store backups in a safe place.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                            lineHeight = 20.sp
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.fillMaxWidth())

                // Acceptance statement - Clear and readable
                Text(
                    text = "I accept these terms and will manage my data responsibly.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                    lineHeight = 20.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    "Got It! Let's Go ",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        },
        modifier = Modifier.fillMaxWidth(0.95f)
    )
}

