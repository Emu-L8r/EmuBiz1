package com.emul8r.bizap.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.emul8r.bizap.ui.theme.Dimensions
import com.emul8r.bizap.ui.theme.SectionDefaults
import com.emul8r.bizap.ui.theme.Spacing
import com.emul8r.bizap.ui.theme.TextSizes

/**
 * ============================================================================
 * SECTION HEADER COMPONENT - Consistent section organization
 * ============================================================================
 *
 * A reusable section header with:
 * - Icon for visual identification
 * - Title (clear section name)
 * - Optional description
 * - Visual separator (divider line)
 * - Consistent styling and spacing
 *
 * Use this to organize complex screens into clear sections.
 * Example: "📋 Business Details", "💼 Banking Information", etc.
 */

@Composable
fun SectionHeader(
    icon: ImageVector,
    title: String,
    description: String = "",
    showDivider: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.lg)
    ) {
        // Header row with icon and title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(SectionDefaults.headerHeight),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            // Icon
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.width(SectionDefaults.headerIconSize)
            )

            Spacer(modifier = Modifier.width(Spacing.md))

            // Title and optional description
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = TextSizes.h2,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = TextSizes.bodySmall
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Optional divider
        if (showDivider) {
            Spacer(modifier = Modifier.height(Spacing.md))
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = SectionDefaults.dividerThickness,
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }
    }
}

