package com.emul8r.bizap.ui.invoice.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.emul8r.bizap.domain.invoice.model.PaymentAnalyticsSummary
import timber.log.Timber

/**
 * Payment Analytics Dashboard Screen - Professional financial intelligence dashboard.
 */
@Composable
fun PaymentAnalyticsScreen(
    viewModel: PaymentAnalyticsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (state) {
            is PaymentAnalyticsUiState.Loading -> {
                PaymentAnalyticsLoadingScreen()
            }
            is PaymentAnalyticsUiState.Success -> {
                val analytics = (state as PaymentAnalyticsUiState.Success).analytics
                PaymentAnalyticsContent(analytics)
            }
            is PaymentAnalyticsUiState.Error -> {
                val message = (state as PaymentAnalyticsUiState.Error).message
                PaymentAnalyticsErrorScreen(message) {
                    viewModel.retryLoadAnalytics()
                }
            }
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
            icon = Icons.Filled.AttachMoney,
            label = "Outstanding",
            value = "$" + String.format("%.0f", analytics.totalOutstandingAmount),
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.weight(1f)
        )

        MetricCard(
            icon = Icons.Filled.TrendingUp,
            label = "Collection Rate",
            value = String.format("%.1f", analytics.collectionRate) + "%",
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )

        MetricCard(
            icon = Icons.Filled.Warning,
            label = "Overdue",
            value = analytics.overdueInvoices.toString(),
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MetricCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))
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
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = color
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
                    text = String.format("%.1f", analytics.collectionRate) + "%",
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
                color = getCollectionRateColor(analytics.collectionRate),
                trackColor = getCollectionRateColor(analytics.collectionRate).copy(alpha = 0.2f)
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
                color = MaterialTheme.colorScheme.primary
            )

            AgingBracketRow(
                label = "Past Due 31-60 days",
                amount = analytics.outstandingByAging.past30,
                percentage = if (analytics.outstandingByAging.totalOutstanding > 0) {
                    (analytics.outstandingByAging.past30 / analytics.outstandingByAging.totalOutstanding * 100).toInt()
                } else 0,
                color = MaterialTheme.colorScheme.secondary
            )

            AgingBracketRow(
                label = "Past Due 61-90 days",
                amount = analytics.outstandingByAging.past60,
                percentage = if (analytics.outstandingByAging.totalOutstanding > 0) {
                    (analytics.outstandingByAging.past60 / analytics.outstandingByAging.totalOutstanding * 100).toInt()
                } else 0,
                color = MaterialTheme.colorScheme.tertiary
            )

            AgingBracketRow(
                label = "Past Due 90+ days",
                amount = analytics.outstandingByAging.past90,
                percentage = if (analytics.outstandingByAging.totalOutstanding > 0) {
                    (analytics.outstandingByAging.past90 / analytics.outstandingByAging.totalOutstanding * 100).toInt()
                } else 0,
                color = MaterialTheme.colorScheme.error
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
                text = "$" + String.format("%.0f", amount) + " (" + percentage + "%)",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
        LinearProgressIndicator(
            progress = { (percentage.toDouble() / 100.0).toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = color,
            trackColor = color.copy(alpha = 0.2f)
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
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )

        OutstandingCard(
            label = "31-60 days",
            amount = analytics.outstandingByAging.past30,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.weight(1f)
        )

        OutstandingCard(
            label = "61-90 days",
            amount = analytics.outstandingByAging.past60,
            color = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.weight(1f)
        )

        OutstandingCard(
            label = "90+ days",
            amount = analytics.outstandingByAging.past90,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun OutstandingCard(
    label: String,
    amount: Double,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))
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
                color = color
            )
            Text(
                text = "$" + String.format("%.0f", amount),
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
                        text = "$" + String.format("%.0f", totalForecast),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (totalForecast > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }

                Column {
                    Text(
                        text = "Forecast Confidence",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = String.format("%.0f", avgConfidence * 100) + "%",
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
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
                    imageVector = Icons.Filled.Warning,
                    contentDescription = "Alert",
                    modifier = Modifier.size(28.dp)
                )
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "⚠️ Payment Risk Alert",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${analytics.riskInvoices.size} invoice(s) at risk",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Text(
                text = "Take immediate action to recover outstanding amounts.",
                style = MaterialTheme.typography.bodySmall
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
                color = MaterialTheme.colorScheme.primary
            )

            InvoiceStatusRow(
                label = "Unpaid",
                count = analytics.unpaidInvoices,
                color = MaterialTheme.colorScheme.secondary
            )

            InvoiceStatusRow(
                label = "Overdue",
                count = analytics.overdueInvoices,
                color = MaterialTheme.colorScheme.error
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
                        text = "$" + String.format("%.0f", analytics.totalInvoiceAmount),
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
                        text = String.format("%.0f", analytics.averagePaymentTime) + " days",
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
        rate >= 90 -> MaterialTheme.colorScheme.primary
        rate >= 70 -> MaterialTheme.colorScheme.secondary
        rate >= 50 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.error
    }
}
