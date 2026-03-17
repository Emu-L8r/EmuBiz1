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
 * Themed container for a grouped analytics section on the GUI1 dashboard.
 *
 * Wraps an arbitrary number of analytics composables inside a single styled
 * card so that the whole analytics block shares the same border, radius,
 * elevation and padding as all other dashboard cards.
 *
 * @param title     Section heading (e.g. "💡 Business Analytics").
 * @param accentColor Accent color for the card border and title.
 * @param modifier  Optional external modifier.
 * @param content   Analytics composables to render inside the card.
 */
@Composable
fun AnalyticsSectionCard(
    title: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
                .padding(DashboardTheme.cardPadding),
            verticalArrangement = Arrangement.spacedBy(DashboardTheme.sectionSpacing)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
            content()
        }
    }
}
