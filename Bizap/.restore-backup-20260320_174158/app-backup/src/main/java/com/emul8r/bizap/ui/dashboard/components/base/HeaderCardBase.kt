package com.emul8r.bizap.ui.dashboard.components.base

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import com.emul8r.bizap.ui.theme.DashboardTheme
import com.emul8r.bizap.ui.theme.dashboardCardElevation

/**
 * Consistent section-header card for the GUI1 dashboard.
 *
 * Used to render a titled, optionally-actionable header block (e.g. the
 * business name/ABN row or the "Business Analytics" section banner).
 * Styling tokens come from [DashboardTheme].
 *
 * @param title         Primary heading text.
 * @param subtitle      Optional secondary text shown below [title].
 * @param accentColor   Accent color for title and border.
 * @param trailingIcon  Optional icon shown at the trailing edge of the header.
 * @param onTrailingClick Callback invoked when [trailingIcon] is tapped.
 * @param modifier      Optional external modifier.
 */
@Composable
fun HeaderCardBase(
    title: String,
    subtitle: String? = null,
    accentColor: Color = Color.Unspecified,
    trailingIcon: ImageVector? = null,
    onTrailingClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val useAccent = accentColor != Color.Unspecified

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (useAccent) {
                DashboardTheme.containerColor(accentColor)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (useAccent) DashboardTheme.borderStroke(accentColor) else null,
        shape = DashboardTheme.cardShape,
        elevation = dashboardCardElevation()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DashboardTheme.cardPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (useAccent) accentColor else MaterialTheme.colorScheme.primary
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(DashboardTheme.cardSpacing / 4))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (trailingIcon != null) {
                IconButton(onClick = { onTrailingClick?.invoke() }) {
                    Icon(
                        imageVector = trailingIcon,
                        contentDescription = null,
                        tint = if (useAccent) accentColor else MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
