package com.emul8r.bizap.ui.revenue

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.emul8r.bizap.domain.revenue.model.RevenueMetrics
import com.emul8r.bizap.utils.CentsFormatter

@Composable
fun RevenueDashboardScreen(
    viewModel: RevenueDashboardViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
    headerSlot: (@Composable ColumnScope.() -> Unit)? = null,
    footerSlot: (@Composable ColumnScope.() -> Unit)? = null,
) {
    val state by viewModel.uiState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (val s = state) {
            is RevenueDashboardUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is RevenueDashboardUiState.Success -> {
                RevenueDashboardContent(
                    metrics = s.metrics,
                    headerSlot = headerSlot,
                    footerSlot = footerSlot,
                    modifier = modifier,
                )
            }
            is RevenueDashboardUiState.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Error: ${s.message}", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun RevenueDashboardContent(
    metrics: RevenueMetrics,
    headerSlot: (@Composable ColumnScope.() -> Unit)? = null,
    footerSlot: (@Composable ColumnScope.() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        headerSlot?.invoke(this) ?: run {
            Text(
                text = "Revenue Dashboard",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RevenueSummaryCard(label = "MTD Collected", amountCents = metrics.mtdRevenue, modifier = Modifier.weight(1f))
            RevenueSummaryCard(label = "YTD Collected", amountCents = metrics.ytdRevenue, modifier = Modifier.weight(1f))
        }

        if (metrics.outstandingAmount > 0L) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Outstanding (Expected)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = CentsFormatter.formatCents(metrics.outstandingAmount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "Amount billed but not yet collected",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        Text(text = "Revenue by Currency", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        
        metrics.topPerformers.forEach { performer ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = performer.currencyCode, fontWeight = FontWeight.Bold)
                    Text(text = CentsFormatter.formatCents(performer.totalAmount, performer.currencyCode))
                }
            }
        }

        footerSlot?.invoke(this) ?: run {
            HorizontalDivider()
            Text(
                text = "Data as of today",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun RevenueSummaryCard(label: String, amountCents: Long, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = label, style = MaterialTheme.typography.labelSmall)
            Text(
                text = CentsFormatter.formatCents(amountCents),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
