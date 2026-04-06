package com.emul8r.bizap.ui.gui2.dashboard.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.emul8r.bizap.domain.model.gui2.DashboardStateV2
import com.emul8r.bizap.ui.gui2.common.formatCents
import com.emul8r.bizap.ui.theme.CompactDimensions

/**
 * Compact dashboard metrics section.
 * Displays revenue, payment, and risk data in a dense row layout.
 */
@Composable
fun CompactDashboardMetrics(
    state: DashboardStateV2,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = CompactDimensions.screenPadding),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        CompactMetricChip(
            label = "MTD",
            value = formatCents(state.revenueMetrics.mtdRevenue)
        )
        CompactMetricChip(
            label = "Out",
            value = formatCents(state.paymentMetrics.outstandingAmount)
        )
        CompactMetricChip(
            label = "Paid",
            value = formatCents(state.paymentMetrics.collectedAmount)
        )
    }
}

@Composable
private fun CompactMetricChip(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}
