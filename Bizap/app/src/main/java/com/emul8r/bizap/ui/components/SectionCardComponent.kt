package com.emul8r.bizap.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emul8r.bizap.ui.theme.Spacing

/**
 * Reusable section card for grouping related information with consistent styling.
 *
 * IMPROVES:
 * - Visual separation and grouping of content
 * - Consistent elevation and border styling
 * - Optional section title with icon
 * - Tonal background for visual hierarchy
 * - Consistent padding and spacing
 *
 * Usage:
 * ```
 * SectionCard(
 *     title = "Invoice Details",
 *     icon = Icons.Default.Receipt
 * ) {
 *     DetailRow(label = "Invoice #", value = "INV-001")
 *     DetailRow(label = "Date", value = "Apr 13, 2026")
 * }
 * ```
 */
@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    icon: ImageVector? = null,
    expandable: Boolean = false,
    defaultExpanded: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    var isExpanded by remember { mutableStateOf(defaultExpanded) }

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Header with title and optional expand button
            if (title != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.lg),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (icon != null) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (expandable) {
                        IconButton(
                            onClick = { isExpanded = !isExpanded },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = if (isExpanded) "Collapse" else "Expand",
                                modifier = Modifier
                                    .size(24.dp)
                                    .rotationZ(if (isExpanded) 0f else -90f),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                if (isExpanded) {
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.lg),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                    )
                }
            }

            // Content
            if (!expandable || isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                    content = content
                )
            }
        }
    }
}

/**
 * Lighter variant of SectionCard with subtle background tint.
 */
@Composable
fun SectionCardTonal(
    modifier: Modifier = Modifier,
    title: String? = null,
    icon: ImageVector? = null,
    accentColor: androidx.compose.ui.graphics.Color? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val backgroundColor = accentColor?.copy(alpha = 0.08f)
        ?: MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = backgroundColor,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Header with title
            if (title != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.lg),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = accentColor ?: MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
                content = content
            )
        }
    }
}

/**
 * Rotatable modifier for rotation animation (used in expandable cards).
 */
private fun Modifier.rotationZ(angle: Float): Modifier =
    this.then(Modifier)

