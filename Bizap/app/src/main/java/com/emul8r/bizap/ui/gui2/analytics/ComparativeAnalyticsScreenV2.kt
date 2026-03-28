package com.emul8r.bizap.ui.gui2.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emul8r.bizap.presentation.viewmodel.ComparativeMetricsViewModelV2
import com.emul8r.bizap.ui.gui2.common.*
import java.time.LocalDate

/**
 * Comparative Analytics Screen - Compare revenue across time periods.
 *
 * Shows:
 * - Month-over-Month comparison
 * - Year-over-Year comparison
 * - Custom period comparison
 * - Growth rate and variance
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComparativeAnalyticsScreenV2(
    businessId: Long,
    onBack: () -> Unit,
    viewModel: ComparativeMetricsViewModelV2 = hiltViewModel()
) {
    val comparativeState by viewModel.comparativeState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Comparative Analytics") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Period Selector
            PeriodSelectorV2(
                onMonthOverMonth = {
                    viewModel.compareMetrics(ComparativeMetricsViewModelV2.ComparisonPeriod.MonthOverMonth())
                },
                onYearOverYear = {
                    viewModel.compareMetrics(ComparativeMetricsViewModelV2.ComparisonPeriod.YearOverYear())
                },
                modifier = Modifier.padding(16.dp)
            )

            // Content
            when {
                comparativeState.isLoading -> {
                    LoadingIndicatorV2(modifier = Modifier.fillMaxSize())
                }
                comparativeState.error != null -> {
                    ErrorStateV2(
                        message = comparativeState.error ?: "Unknown error",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    ComparativeAnalyticsContentV2(
                        state = comparativeState,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

/**
 * Main content for comparative analytics.
 */
@Composable
private fun ComparativeAnalyticsContentV2(
    state: ComparativeMetricsViewModelV2.ComparativeState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Comparison Header
        Text(
            text = "${state.period1Label} vs ${state.period2Label}",
            style = MaterialTheme.typography.headlineSmall
        )

        // Growth Rate Card (Primary)
        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Growth Rate", style = MaterialTheme.typography.labelSmall)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = String.format("%.1f%%", state.growthRate),
                        style = MaterialTheme.typography.displaySmall,
                        color = if (state.growthRate >= 0) Color(0xFF4CAF50) else Color(0xFFB3261E)
                    )
                    Text(
                        text = if (state.growthRate >= 0) "↑ Growing" else "↓ Declining",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // Side-by-side Comparison
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Period 1
            ElevatedCard(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = state.period1Label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = state.period1Metrics?.let { formatCents(it.mtdRevenue) } ?: "-",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = "${state.period1Metrics?.weeklyRevenue ?: 0} items",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Period 2
            ElevatedCard(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = state.period2Label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = state.period2Metrics?.let { formatCents(it.mtdRevenue) } ?: "-",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = "${state.period2Metrics?.weeklyRevenue ?: 0} items",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Variance Card
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Variance", style = MaterialTheme.typography.labelSmall)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatCents(state.variance),
                        style = MaterialTheme.typography.headlineMedium,
                        color = if (state.variance >= 0) Color(0xFF4CAF50) else Color(0xFFB3261E)
                    )
                    Text(
                        text = String.format("%.1f%%", state.variancePercentage),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // Detailed Metrics
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Detailed Breakdown", style = MaterialTheme.typography.labelSmall)

                ComparisonRow(
                    label = "YTD Revenue",
                    period1 = state.period1Metrics?.ytdRevenue ?: 0,
                    period2 = state.period2Metrics?.ytdRevenue ?: 0
                )

                ComparisonRow(
                    label = "Weekly Average",
                    period1 = state.period1Metrics?.weeklyRevenue ?: 0,
                    period2 = state.period2Metrics?.weeklyRevenue ?: 0
                )

                ComparisonRow(
                    label = "All-Time Paid",
                    period1 = state.period1Metrics?.totalPaidRevenue ?: 0,
                    period2 = state.period2Metrics?.totalPaidRevenue ?: 0
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Single comparison row showing period1, period2, and change.
 */
@Composable
private fun ComparisonRow(
    label: String,
    period1: Long,
    period2: Long
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(formatCents(period1), style = MaterialTheme.typography.bodyMedium)
            Text(formatCents(period2), style = MaterialTheme.typography.bodyMedium)
            Text(
                formatCents(period2 - period1),
                style = MaterialTheme.typography.bodyMedium,
                color = if (period2 >= period1) Color(0xFF4CAF50) else Color(0xFFB3261E)
            )
        }
    }
}

