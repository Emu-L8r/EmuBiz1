package com.emul8r.bizap.ui.gui2.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emul8r.bizap.domain.model.gui2.RevenueMetricsV2
import com.emul8r.bizap.ui.gui2.common.*

/**
 * GUI2 revenue analytics screen.
 * Shows MTD, YTD, weekly, total paid revenue, and a 30-day daily trend.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RevenueAnalyticsScreenV2(
    businessId: Long,
    onBack: () -> Unit,
    viewModel: RevenueAnalyticsViewModelV2 = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Revenue Analytics") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is RevenueAnalyticsUiStateV2.Loading -> LoadingIndicatorV2(
                modifier = Modifier.padding(paddingValues)
            )
            is RevenueAnalyticsUiStateV2.Error -> ErrorStateV2(
                message = state.message,
                modifier = Modifier.padding(paddingValues)
            )
            is RevenueAnalyticsUiStateV2.Success -> RevenueAnalyticsContentV2(
                metrics = state.metrics,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
private fun RevenueAnalyticsContentV2(
    metrics: RevenueMetricsV2,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionHeaderV2("Revenue Summary")

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCardV2(
                label = "Month-to-Date",
                value = formatCents(metrics.mtdRevenue),
                modifier = Modifier.weight(1f)
            )
            MetricCardV2(
                label = "Year-to-Date",
                value = formatCents(metrics.ytdRevenue),
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCardV2(
                label = "Last 7 Days",
                value = formatCents(metrics.weeklyRevenue),
                modifier = Modifier.weight(1f)
            )
            MetricCardV2(
                label = "All-Time Paid",
                value = formatCents(metrics.totalPaidRevenue),
                modifier = Modifier.weight(1f)
            )
        }

        HorizontalDivider()
        SectionHeaderV2("30-Day Daily Trend")

        if (metrics.dailyTrend.isEmpty()) {
            Text(
                text = "No revenue data in the last 30 days.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            metrics.dailyTrend.forEach { point ->
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(point.date, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            formatCents(point.revenueCents),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
