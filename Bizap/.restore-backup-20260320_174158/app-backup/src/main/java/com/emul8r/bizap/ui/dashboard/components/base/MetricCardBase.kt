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
 * Base metric card for the GUI1 dashboard.
 *
 * All styling (border width, border alpha, background alpha, corner radius,
 * icon size, internal padding) is driven by [DashboardTheme] so that every
 * metric card on the screen looks identical without any inline magic numbers.
 *
 * @param title     Short label displayed above the value.
 * @param value     Formatted metric value (e.g. "42" or "A$1,234.00").
 * @param icon      Icon shown to the right of the value.
 * @param accentColor Primary color used for the icon, value text, background tint and border.
 * @param modifier  Optional external modifier (e.g. `Modifier.weight(1f)`).
 */
@Composable
fun MetricCardBase(
    title: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = DashboardTheme.containerColor(accentColor)
        ),
        border = DashboardTheme.borderStroke(accentColor),
        shape = DashboardTheme.cardShape,
        elevation = dashboardCardElevation()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DashboardTheme.cardPadding)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(DashboardTheme.cardSpacing / 3))
                    Text(
                        text = value,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                }
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(DashboardTheme.metricIconSize)
                )
            }
        }
    }
}
