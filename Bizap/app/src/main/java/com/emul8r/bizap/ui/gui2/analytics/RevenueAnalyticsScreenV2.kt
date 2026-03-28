package com.emul8r.bizap.ui.gui2.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emul8r.bizap.domain.model.gui2.RevenueMetricsV2
import com.emul8r.bizap.ui.gui2.common.*

/**
 * GUI2 revenue analytics screen.
 * Shows MTD, YTD, weekly, total paid revenue, and a 30-day daily trend.
 * Includes date range filters and export functionality.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RevenueAnalyticsScreenV2(
    businessId: Long,
    onBack: () -> Unit,
    viewModel: RevenueAnalyticsViewModelV2 = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedDateRange = remember { mutableStateOf(DateRangeV2.THIS_MONTH) }
    val isExporting = remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Revenue Analytics") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    ExportMenuButtonV2(
                        onExportPdf = { isExporting.value = true },
                        onExportCsv = { isExporting.value = true },
                        isExporting = isExporting.value
                    )
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Date range filter
            DateRangeFilterV2(
                selectedRange = selectedDateRange.value,
                onRangeSelected = { selectedDateRange.value = it },
                modifier = Modifier.padding(vertical = 12.dp)
            )

            // Loading indicator if exporting
            if (isExporting.value) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            // Content
            when (val state = uiState) {
                is RevenueAnalyticsUiStateV2.Loading -> LoadingIndicatorV2(
                    modifier = Modifier.fillMaxSize()
                )
                is RevenueAnalyticsUiStateV2.Error -> ErrorStateV2(
                    message = state.message,
                    modifier = Modifier.fillMaxSize()
                )
                is RevenueAnalyticsUiStateV2.Success -> RevenueAnalyticsContentV2(
                    metrics = state.metrics,
                    dateRange = selectedDateRange.value
                )
            }
        }
    }
}

@Composable
private fun RevenueAnalyticsContentV2(
    metrics: RevenueMetricsV2,
    dateRange: DateRangeV2 = DateRangeV2.THIS_MONTH,
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
