package com.emul8r.bizap.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emul8r.bizap.ui.theme.Spacing

/**
 * Reusable detail row component for consistent display of label-value pairs.
 *
 * IMPROVES:
 * - Consistent spacing and alignment across all detail screens
 * - Semantic color contrast (onSurfaceVariant for labels, onSurface for values)
 * - Proper typography hierarchy (labelSmall for labels, bodyMedium for values)
 * - Optional divider for visual separation between rows
 * - Flexible value content (can include amounts, badges, icons, etc.)
 *
 * Usage:
 * ```
 * DetailRow(label = "Invoice #", value = "INV-001")
 * DetailRow(label = "Status", value = "Paid", showDivider = true)
 * DetailRow(
 *     label = "Total",
 *     value = "$ 1,234.56",
 *     valueColor = MaterialTheme.colorScheme.primary,
 *     valueWeight = FontWeight.Bold
 * )
 * ```
 */
@Composable
fun DetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    showDivider: Boolean = false,
    valueWeight: FontWeight = FontWeight.Normal,
    valueColor: androidx.compose.ui.graphics.Color? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(Spacing.lg + Spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    fontWeight = valueWeight
                ),
                color = valueColor ?: MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End
            )
        }

        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.xs),
                thickness = 1.dp
            )
        }
    }
}

/**
 * Alternative layout for detail rows where value takes a full line (e.g., for longer text).
 */
@Composable
fun DetailRowStacked(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    showDivider: Boolean = false,
    valueWeight: FontWeight = FontWeight.Normal,
    valueColor: androidx.compose.ui.graphics.Color? = null
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                fontWeight = valueWeight
            ),
            color = valueColor ?: MaterialTheme.colorScheme.onSurface
        )

        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.xs),
                thickness = 1.dp
            )
        }
    }
}

/**
 * Composable version of DetailRow that accepts composable content instead of strings.
 * Allows for custom badges, icons, amounts with formatting, etc.
 */
@Composable
fun DetailRowComposable(
    label: String,
    modifier: Modifier = Modifier,
    showDivider: Boolean = false,
    content: @Composable RowScope.() -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(Spacing.lg + Spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )

            content()
        }

        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.xs),
                thickness = 1.dp
            )
        }
    }
}


