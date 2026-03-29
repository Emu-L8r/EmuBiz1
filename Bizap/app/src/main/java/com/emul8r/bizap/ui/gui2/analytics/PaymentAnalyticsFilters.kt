package com.emul8r.bizap.ui.gui2.analytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

/**
 * Date range filter chips for analytics screens
 */
@Composable
fun DateRangeFilterChips(
    startDate: Long?,
    endDate: Long?,
    onStartDateClick: () -> Unit,
    onEndDateClick: () -> Unit,
    onClearClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
    val hasFilter = startDate != null || endDate != null

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Start date chip
        FilterChip(
            selected = startDate != null,
            onClick = onStartDateClick,
            label = {
                Text(
                    if (startDate != null) "From: ${dateFormat.format(Date(startDate))}"
                    else "Start Date"
                )
            },
            modifier = Modifier.padding(4.dp)
        )

        // End date chip
        FilterChip(
            selected = endDate != null,
            onClick = onEndDateClick,
            label = {
                Text(
                    if (endDate != null) "To: ${dateFormat.format(Date(endDate))}"
                    else "End Date"
                )
            },
            modifier = Modifier.padding(4.dp)
        )

        // Clear filters button
        if (hasFilter) {
            AssistChip(
                onClick = onClearClick,
                label = { Text("Clear") },
                leadingIcon = {
                    Icon(Icons.Filled.Close, contentDescription = "Clear filters", modifier = Modifier.padding(4.dp))
                },
                modifier = Modifier.padding(4.dp)
            )
        }
    }
}

