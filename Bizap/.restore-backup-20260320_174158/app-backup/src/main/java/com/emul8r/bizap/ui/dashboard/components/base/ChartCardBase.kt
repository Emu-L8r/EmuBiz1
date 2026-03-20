package com.emul8r.bizap.ui.dashboard.components.base

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.emul8r.bizap.ui.theme.DashboardTheme
import com.emul8r.bizap.ui.theme.dashboardCardElevation

/**
 * Consistent wrapper for chart and analytics cards on the GUI1 dashboard.
 *
 * Provides a themed card shell (border, radius, elevation, padding) around any
 * chart content, keeping all visual tokens in [DashboardTheme].
 *
 * @param title       Optional section title shown at the top of the card.
 * @param accentColor Accent used for the border and title text.
 *                    Defaults to a neutral surface-based tint when not set.
 * @param modifier    Optional external modifier.
 * @param content     Composable chart/analytics content.
 */
@Composable
fun ChartCardBase(
    title: String? = null,
    accentColor: Color = Color.Unspecified,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val resolvedBorder = if (accentColor != Color.Unspecified) {
        DashboardTheme.borderStroke(accentColor)
    } else {
        null
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (accentColor != Color.Unspecified) {
                DashboardTheme.containerColor(accentColor)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            }
        ),
        border = resolvedBorder,
        shape = DashboardTheme.cardShape,
        elevation = dashboardCardElevation()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DashboardTheme.cardPadding),
            verticalArrangement = Arrangement.spacedBy(DashboardTheme.cardSpacing)
        ) {
            if (title != null) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (accentColor != Color.Unspecified) {
                        accentColor
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            }
            content()
        }
    }
}
