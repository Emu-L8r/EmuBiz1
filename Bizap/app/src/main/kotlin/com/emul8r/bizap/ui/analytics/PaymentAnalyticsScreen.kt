package com.emul8r.bizap.ui.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Payment Analytics Report Screen.
 *
 * Displays payment metrics and status:
 * - Collection rate
 * - Days Sales Outstanding (DSO)
 * - Payment status breakdown
 * - Invoice aging
 */
@Composable
fun PaymentAnalyticsScreen(
    uiState: PaymentMetricsState,
    modifier: Modifier = Modifier
) {
    when (uiState) {
        is PaymentMetricsState.Loading -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is PaymentMetricsState.Success -> {
            PaymentAnalyticsContent(
                metrics = uiState.metrics,
                modifier = modifier
            )
        }
        is PaymentMetricsState.Error -> {
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
private fun PaymentAnalyticsContent(
    metrics: PaymentMetrics,
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
            text = "Payment Analytics",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Collection Rate Card (Primary KPI)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = if (metrics.collectionRate >= 80f) {
                            Color(0xFF4CAF50)  // Green
                        } else if (metrics.collectionRate >= 60f) {
                            Color(0xFFFFA726)  // Orange
                        } else {
                            Color(0xFFEF5350)  // Red
                        }
                    )
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Collection Rate",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White
                )
                Text(
                    text = "${metrics.collectionRate.toInt()}%",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "${metrics.paidInvoices} of ${metrics.totalInvoices} invoices paid",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        // DSO Card
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
                    text = "Days Sales Outstanding",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "${metrics.daysOutstanding} days",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Average ${metrics.averagePaymentDays} days to collect",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }

        // Status Breakdown Grid
        Text(
            text = "Invoice Status Breakdown",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PaymentStatusItem(
                label = "Paid",
                count = metrics.paidInvoices,
                total = metrics.totalInvoices,
                icon = Icons.Default.CheckCircle,
                color = Color(0xFF4CAF50)
            )
            PaymentStatusItem(
                label = "Due Soon",
                count = metrics.dueSoonCount,
                total = metrics.totalInvoices,
                icon = Icons.Default.Schedule,
                color = Color(0xFFFFA726)
            )
            PaymentStatusItem(
                label = "Overdue",
                count = metrics.overdueCount,
                total = metrics.totalInvoices,
                icon = Icons.Default.Error,
                color = Color(0xFFEF5350)
            )
            PaymentStatusItem(
                label = "Draft",
                count = metrics.draftCount,
                total = metrics.totalInvoices,
                icon = Icons.Default.Schedule,
                color = Color(0xFFBDBDBD)
            )
        }

        // Status Distribution Pie Chart (Placeholder)
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
                    text = "Status Distribution",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // Chart placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🥧 Status Distribution Chart",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun PaymentStatusItem(
    label: String,
    count: Int,
    total: Int,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    val percentage = if (total > 0) {
        (count.toFloat() / total.toFloat()) * 100f
    } else {
        0f
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(24.dp)
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$count/$total",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }

            // Progress bar
            LinearProgressIndicator(
                progress = { percentage / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = color,
                trackColor = color.copy(alpha = 0.3f)
            )

            Text(
                text = "${percentage.toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}



