package com.emul8r.bizap.ui.gui2.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Side-by-side preview comparison component
 * Phase 3.5: Task 3 - Comparison Mode Component
 *
 * Shows "Current" settings preview vs. selected preset preview
 * Helps users decide whether to apply a preset
 */
@Composable
fun PreviewComparison(
    currentPreview: String?,
    presetPreview: String?,
    presetName: String = "Preset",
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ──────────────────────────────────────────────
            // Current Settings Preview
            // ──────────────────────────────────────────────
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Current",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 150.dp, max = 300.dp)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = MaterialTheme.shapes.small
                        ),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        currentPreview?.take(200) ?: "No preview",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(12.dp),
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 10
                    )
                }
            }

            // ──────────────────────────────────────────────
            // Preset Preview
            // ──────────────────────────────────────────────
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    presetName,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 150.dp, max = 300.dp)
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = MaterialTheme.shapes.small
                        ),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        presetPreview?.take(200) ?: "No preview",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(12.dp),
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 10
                    )
                }
            }
        }
    }
}

