package com.emul8r.bizap.ui.invoice.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.emul8r.bizap.domain.invoice.model.PaymentAnalyticsSummary
import com.emul8r.bizap.ui.designsystem.BizapColors
import java.util.Locale

/**
 * Payment Analytics Dashboard Screen - Professional financial intelligence dashboard.
 * ✅ FIXED: Now refreshes when screen comes into view
 */
@Composable
fun PaymentAnalyticsScreen(
    onBack: () -> Unit = {},
    viewModel: PaymentAnalyticsViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
    topBarSlot: (@Composable ColumnScope.() -> Unit)? = null,
    footerSlot: (@Composable ColumnScope.() -> Unit)? = null,
) {
    val state by viewModel.state.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            when (state) {
                is PaymentAnalyticsUiState.Loading -> {
                    PaymentAnalyticsLoadingScreen()
                }
                is PaymentAnalyticsUiState.Success -> {
                    val analytics = (state as PaymentAnalyticsUiState.Success).analytics
                    Column(modifier = modifier) {
                        topBarSlot?.invoke(this) ?: run {
                            Text(
                                text = "Payment Analytics",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            )
                        }
                        PaymentAnalyticsDashboardActions(
                            isRefreshing = isRefreshing,
                            onRefresh = { viewModel.forceRefresh() },
                            onRebuild = { viewModel.rebuildSnapshots() }
                        )
                        PaymentAnalyticsContent(analytics)
                        footerSlot?.invoke(this) ?: run {
                            Text(
                                text = "Ageing buckets: Current · 30 · 60 · 90+ days",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            )
                        }
                    }
                }
                is PaymentAnalyticsUiState.Error -> {
                    val message = (state as PaymentAnalyticsUiState.Error).message
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        PaymentAnalyticsErrorScreen(message) {}
                        Spacer(Modifier.height(16.dp))
                        PaymentAnalyticsDashboardActions(
                            isRefreshing = isRefreshing,
                            onRefresh = { viewModel.forceRefresh() },
                            onRebuild = { viewModel.rebuildSnapshots() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentAnalyticsDashboardActions(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onRebuild: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick = onRefresh,
            enabled = !isRefreshing,
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
            Spacer(Modifier.width(4.dp))
            Text("Refresh")
        }
        OutlinedButton(
            onClick = onRebuild,
            enabled = !isRefreshing,
            modifier = Modifier.weight(1f)
        ) {
            if (isRefreshing) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
            } else {
                Icon(Icons.Default.Build, contentDescription = "Rebuild Data")
            }
            Spacer(Modifier.width(4.dp))
            Text("Rebuild Data")
        }
    }
}

@Composable
private fun PaymentAnalyticsLoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Loading payment analytics...",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun PaymentAnalyticsErrorScreen(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "⚠️ Error Loading Dashboard",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

@Composable
private fun PaymentAnalyticsContent(analytics: PaymentAnalyticsSummary) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Payment Analytics",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        PaymentKeyMetrics(analytics)
        CollectionRateCard(analytics)
        AgingBreakdownSection(analytics)
        OutstandingByAgingCards(analytics)

        if (analytics.cashFlowForecast.isNotEmpty()) {
            CashFlowForecastSection(analytics)
        }

        if (analytics.riskInvoices.isNotEmpty()) {
            RiskAlertsSection(analytics)
        }

        InvoiceStatusSummary(analytics)

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun PaymentKeyMetrics(analytics: PaymentAnalyticsSummary) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MetricCard(
            icon = Icons.Default.AttachMoney,
            label = "Outstanding",
            value = "$" + String.format(Locale.getDefault(), "%.0f", analytics.totalOutstandingAmount),
            backgroundColor = BizapColors.AnalyticsWarning,
            modifier = Modifier.weight(1f)
        )

        MetricCard(
            icon = Icons.AutoMirrored.Filled.TrendingUp,
            label = "Collection Rate",
            value = String.format(Locale.US, "%.1f", analytics.collectionRate) + "%",
            backgroundColor = BizapColors.AnalyticsExcellent,
            modifier = Modifier.weight(1f)
        )

        MetricCard(
            icon = Icons.Default.Warning,
            label = "Overdue",
            value = analytics.overdueInvoices.toString(),
            backgroundColor = BizapColors.AnalyticsAtRisk,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MetricCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor.copy(alpha = 0.15f)
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = backgroundColor,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = backgroundColor
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CollectionRateCard(analytics: PaymentAnalyticsSummary) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Collection Efficiency",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = String.format(Locale.US, "%.1f", analytics.collectionRate) + "%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = getCollectionRateColor(analytics.collectionRate)
                )
            }

            LinearProgressIndicator(
                progress = { (analytics.collectionRate / 100.0).toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = getCollectionRateColor(analytics.collectionRate)
            )

            Text(
                text = "${analytics.paidInvoices} of ${analytics.totalInvoices} invoices paid",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AgingBreakdownSection(analytics: PaymentAnalyticsSummary) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Outstanding Amount by Aging",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            AgingBracketRow(
                label = "Current (0-30 days)",
                amount = analytics.outstandingByAging.current,
                percentage = if (analytics.outstandingByAging.totalOutstanding > 0) {
                    (analytics.outstandingByAging.current / analytics.outstandingByAging.totalOutstanding * 100).toInt()
                } else 0,
                color = BizapColors.AnalyticsExcellent
            )

            AgingBracketRow(
                label = "Past Due 31-60 days",
                amount = analytics.outstandingByAging.past30,
                percentage = if (analytics.outstandingByAging.totalOutstanding > 0) {
                    (analytics.outstandingByAging.past30 / analytics.outstandingByAging.totalOutstanding * 100).toInt()
                } else 0,
                color = BizapColors.AnalyticsGood
            )

            AgingBracketRow(
                label = "Past Due 61-90 days",
                amount = analytics.outstandingByAging.past60,
                percentage = if (analytics.outstandingByAging.totalOutstanding > 0) {
                    (analytics.outstandingByAging.past60 / analytics.outstandingByAging.totalOutstanding * 100).toInt()
                } else 0,
                color = BizapColors.AnalyticsWarning
            )

            AgingBracketRow(
                label = "Past Due 90+ days",
                amount = analytics.outstandingByAging.past90,
                percentage = if (analytics.outstandingByAging.totalOutstanding > 0) {
                    (analytics.outstandingByAging.past90 / analytics.outstandingByAging.totalOutstanding * 100).toInt()
                } else 0,
                color = BizapColors.AnalyticsAtRisk
            )
        }
    }
}

@Composable
private fun AgingBracketRow(
    label: String,
    amount: Double,
    percentage: Int,
    color: Color
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "$" + String.format(Locale.getDefault(), "%.0f", amount) + " (" + percentage + "%)",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
        LinearProgressIndicator(
            progress = { (percentage.toDouble() / 100.0).toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = color
        )
    }
}

@Composable
private fun OutstandingByAgingCards(analytics: PaymentAnalyticsSummary) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutstandingCard(
            label = "0-30 days",
            amount = analytics.outstandingByAging.current,
            backgroundColor = BizapColors.AnalyticsExcellent,
            modifier = Modifier.weight(1f)
        )

        OutstandingCard(
            label = "31-60 days",
            amount = analytics.outstandingByAging.past30,
            backgroundColor = BizapColors.AnalyticsGood,
            modifier = Modifier.weight(1f)
        )

        OutstandingCard(
            label = "61-90 days",
            amount = analytics.outstandingByAging.past60,
            backgroundColor = BizapColors.AnalyticsWarning,
            modifier = Modifier.weight(1f)
        )

        OutstandingCard(
            label = "90+ days",
            amount = analytics.outstandingByAging.past90,
            backgroundColor = BizapColors.AnalyticsAtRisk,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun OutstandingCard(
    label: String,
    amount: Double,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor.copy(alpha = 0.15f)
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = backgroundColor
            )
            Text(
                text = "$" + String.format(Locale.getDefault(), "%.0f", amount),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CashFlowForecastSection(analytics: PaymentAnalyticsSummary) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "30-Day Cash Flow Forecast",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            val totalForecast = analytics.cashFlowForecast.sumOf { it.netCashFlow }
            val avgConfidence = analytics.cashFlowForecast.map { it.confidence }.average()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Projected Net Cash Flow",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "$" + String.format(Locale.getDefault(), "%.0f", totalForecast),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (totalForecast > 0) BizapColors.AnalyticsExcellent else BizapColors.AnalyticsAtRisk
                    )
                }

                Column {
                    Text(
                        text = "Forecast Confidence",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = String.format(Locale.getDefault(), "%.0f", avgConfidence * 100) + "%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = "${analytics.cashFlowForecast.size} days projected",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RiskAlertsSection(analytics: PaymentAnalyticsSummary) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = BizapColors.AnalyticsWarning.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.elevatedCardColors(
            containerColor = BizapColors.AnalyticsWarning.copy(alpha = 0.15f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Alert",
                    tint = BizapColors.AnalyticsWarning,
                    modifier = Modifier.size(28.dp)
                )
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "⚠️ Payment Risk Alert",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BizapColors.AnalyticsWarning
                    )
                    Text(
                        text = "${analytics.riskInvoices.size} invoice(s) at risk",
                        style = MaterialTheme.typography.bodySmall,
                        color = BizapColors.AnalyticsAtRisk
                    )
                }
            }

            Text(
                text = "Take immediate action to recover outstanding amounts.",
                style = MaterialTheme.typography.bodySmall,
                color = BizapColors.AnalyticsAtRisk
            )
        }
    }
}

@Composable
private fun InvoiceStatusSummary(analytics: PaymentAnalyticsSummary) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Invoice Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            InvoiceStatusRow(
                label = "Total Invoices",
                count = analytics.totalInvoices
            )

            InvoiceStatusRow(
                label = "Paid",
                count = analytics.paidInvoices,
                color = BizapColors.StatusPaid
            )

            InvoiceStatusRow(
                label = "Unpaid",
                count = analytics.unpaidInvoices,
                color = BizapColors.StatusSent
            )

            InvoiceStatusRow(
                label = "Overdue",
                count = analytics.overdueInvoices,
                color = BizapColors.StatusOverdue
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Total Amount",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "$" + String.format(Locale.getDefault(), "%.0f", analytics.totalInvoiceAmount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column {
                    Text(
                        text = "Avg. Payment Time",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = String.format(Locale.US, "%.0f", analytics.averagePaymentTime) + " days",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun InvoiceStatusRow(
    label: String,
    count: Int,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun getCollectionRateColor(rate: Double): Color {
    return when {
        rate >= 90 -> MaterialTheme.colorScheme.primary  // Excellent - primary color
        rate >= 70 -> MaterialTheme.colorScheme.secondary  // Good - secondary color
        rate >= 50 -> MaterialTheme.colorScheme.tertiary  // Fair - tertiary color
        else -> MaterialTheme.colorScheme.error  // Poor - error color
    }
}
