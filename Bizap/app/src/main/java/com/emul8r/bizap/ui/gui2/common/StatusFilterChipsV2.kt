package com.emul8r.bizap.ui.gui2.common

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.emul8r.bizap.domain.model.InvoiceStatus

/**
 * Status filter chips component for analytics screens.
 *
 * Allows users to select/deselect invoice statuses to filter by.
 * Uses Material 3 FilterChip for selection state visualization.
 *
 * @param selectedStatuses Currently selected statuses
 * @param onStatusesSelected Callback when selection changes
 * @param modifier Optional modifier
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StatusFilterChipsV2(
    selectedStatuses: Set<InvoiceStatus>,
    onStatusesSelected: (Set<InvoiceStatus>) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Filter by Status",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            InvoiceStatus.entries.forEach { status ->
                FilterChip(
                    selected = status in selectedStatuses,
                    onClick = {
                        val updated = selectedStatuses.toMutableSet()
                        if (status in updated) {
                            updated.remove(status)
                        } else {
                            updated.add(status)
                        }
                        onStatusesSelected(updated)
                    },
                    label = {
                        Text(
                            text = status.displayName,
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    modifier = Modifier.wrapContentWidth()
                )
            }
        }
    }
}

/**
 * Extension property for readable status names.
 */
val InvoiceStatus.displayName: String
    get() = when (this) {
        InvoiceStatus.DRAFT -> "Draft"
        InvoiceStatus.SENT -> "Sent"
        InvoiceStatus.PAID -> "Paid"
        InvoiceStatus.PARTIALLY_PAID -> "Partial"
        InvoiceStatus.OVERDUE -> "Overdue"
    }


