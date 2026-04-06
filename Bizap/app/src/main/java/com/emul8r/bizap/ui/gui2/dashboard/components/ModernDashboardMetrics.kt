package com.emul8r.bizap.ui.gui2.dashboard.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.emul8r.bizap.domain.model.gui2.DashboardStateV2
import com.emul8r.bizap.ui.gui2.common.formatCents

/**
 * Modern (spacious) dashboard metrics section.
 * Displays revenue, payment, and risk data in large cards.
 */
@Composable
fun ModernDashboardMetrics(
    state: DashboardStateV2,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MetricBox(
            label = "MTD Revenue",
            value = formatCents(state.revenueMetrics.mtdRevenue),
            modifier = Modifier.fillMaxWidth()
        )
        MetricBox(
            label = "Outstanding",
            value = formatCents(state.paymentMetrics.outstandingAmount),
            modifier = Modifier.fillMaxWidth()
        )
        MetricBox(
            label = "Collected",
            value = formatCents(state.paymentMetrics.collectedAmount),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
