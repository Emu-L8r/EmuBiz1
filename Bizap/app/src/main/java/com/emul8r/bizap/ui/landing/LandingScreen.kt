package com.emul8r.bizap.ui.landing

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Landing screen that lets the user choose between GUI1 (legacy) and GUI2 (new).
 * Shown only when no GUI preference has been saved yet — or when the user explicitly
 * resets their choice from Settings.
 *
 * @param onSelectGui1 Called when the user picks the legacy GUI1 experience.
 * @param onSelectGui2 Called when the user picks the new GUI2 experience.
 */
@Composable
fun LandingScreen(
    onSelectGui1: () -> Unit,
    onSelectGui2: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome to Bizap",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Choose your experience",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            // GUI2 — promoted as the recommended choice
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = onSelectGui2
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "✨  New Experience (GUI2)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Badge { Text("Recommended") }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Context-aware navigation · Direct invoice queries · " +
                                "Unified dashboards · Modern Compose UI",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // GUI1 — legacy option
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = onSelectGui1
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "Classic Experience (GUI1)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "The original app — familiar but with some known limitations.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "You can change this at any time in Settings.",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
