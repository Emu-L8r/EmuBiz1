package com.emul8r.bizap.ui.analytics.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.emul8r.bizap.domain.analytics.AnalyticsDateRange

/**
 * Reusable filter chip row for date range selection.
 *
 * Displays preset date range chips (7d, 30d, 90d) plus a custom range button
 * for date range selection. Used across all analytics tabs.
 *
 * @param selectedRange Currently selected date range
 * @param onRangeSelected Callback when a preset range is selected
 * @param onCustomDateRange Callback when custom range button is tapped
 * @param modifier Optional modifier for styling
 */
@Composable
fun AnalyticsFilterChips(
    selectedRange: AnalyticsDateRange,
    onRangeSelected: (AnalyticsDateRange) -> Unit,
    onCustomDateRange: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Preset range chips
        listOf(
            AnalyticsDateRange.SEVEN_DAYS,
            AnalyticsDateRange.THIRTY_DAYS,
            AnalyticsDateRange.NINETY_DAYS
        ).forEach { range ->
            FilterChip(
                selected = selectedRange == range,
                onClick = { onRangeSelected(range) },
                label = { Text(range.label) },
                modifier = Modifier.height(32.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Custom date picker button
        IconButton(
            onClick = onCustomDateRange,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                Icons.Default.Edit,
                contentDescription = "Custom date range",
                modifier = Modifier.size(20.dp)
            )
        }
    }
}


