package com.emul8r.bizap.ui.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emul8r.bizap.utils.CentsFormatter
import java.text.SimpleDateFormat
import java.util.*

/**
 * Revenue Analytics Report Screen.
 *
 * Displays revenue trends and metrics in visual format.
 * Shows:
 * - Total revenue summary
 * - Daily/monthly/yearly trends
 * - Growth percentage
 * - Revenue breakdown
 */
@Composable
fun RevenueAnalyticsScreen(
    uiState: RevenueMetricsState,
    modifier: Modifier = Modifier
) {
    when (uiState) {
        is RevenueMetricsState.Loading -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is RevenueMetricsState.Success -> {
            RevenueAnalyticsContent(
                metrics = uiState.metrics,
                modifier = modifier
            )
        }
        is RevenueMetricsState.Error -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Error: ${uiState.message}",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun RevenueAnalyticsContent(
    metrics: RevenueMetrics,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Text(
            text = "Revenue Analytics",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Total Revenue Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Total Revenue",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = CentsFormatter.formatCents(metrics.totalRevenue),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                if (metrics.trend >= 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = "Trending up",
                            tint = Color.Green,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${metrics.trend.toInt()}% vs last period",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Green
                        )
                    }
                }
            }
        }

        // Summary Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryMetricCard(
                label = "This Month",
                value = CentsFormatter.formatCents(metrics.thisMonthRevenue),
                modifier = Modifier.weight(1f)
            )
            SummaryMetricCard(
                label = "This Year",
                value = CentsFormatter.formatCents(metrics.thisYearRevenue),
                modifier = Modifier.weight(1f)
            )
        }

        // Average Daily
        SummaryMetricCard(
            label = "Daily Average",
            value = CentsFormatter.formatCents(metrics.averageDaily),
            modifier = Modifier.fillMaxWidth()
        )

        // Daily Revenue Chart (Placeholder)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Daily Revenue Trend",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // Chart placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "📊 Chart Visualization",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "${metrics.dailyRevenue.size} days of data",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Daily breakdown list
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Daily Breakdown",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    metrics.dailyRevenue.forEach { daily ->
                        DailyRevenueItem(daily = daily)
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryMetricCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun DailyRevenueItem(
    daily: DailyRevenue,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
    val dateStr = dateFormat.format(Date(daily.dateMs))

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = dateStr,
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = CentsFormatter.formatCents(daily.amount),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold
        )
    }
}
