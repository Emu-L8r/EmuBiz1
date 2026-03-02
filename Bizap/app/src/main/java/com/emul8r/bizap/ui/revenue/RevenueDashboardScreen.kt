package com.emul8r.bizap.ui.revenue

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
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
import com.emul8r.bizap.domain.revenue.model.RevenueByCurrency
import com.emul8r.bizap.utils.CurrencyFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RevenueDashboardScreen(
    viewModel: RevenueDashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Revenue Report") },
                actions = {
                    IconButton(
                        onClick = { viewModel.refreshAnalytics() },
                        enabled = !isRefreshing
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            AnimatedVisibility(visible = isRefreshing) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                when (val s = state) {
                    is RevenueDashboardUiState.Loading -> {
                        if (!isRefreshing) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                    is RevenueDashboardUiState.Success -> {
                        RevenueDashboardContent(s.metrics)
                    }
                    is RevenueDashboardUiState.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = "Error: ${s.message}", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RevenueDashboardContent(metrics: RevenueMetrics) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. High-Level Metrics
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RevenueSummaryCard(label = "MTD", amountCents = metrics.mtdRevenue, modifier = Modifier.weight(1f))
            RevenueSummaryCard(label = "YTD", amountCents = metrics.ytdRevenue, modifier = Modifier.weight(1f))
        }

        // 2. Visual Revenue Trend
        Text(text = "Revenue Trend", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                if (metrics.dailyTrend.isNotEmpty()) {
                    RevenueTrendChart(dataPoints = metrics.dailyTrend)
                } else {
                    Text(
                        "No trend data available yet",
                        modifier = Modifier.align(Alignment.Center).padding(vertical = 32.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 3. Currency Breakdown
        Text(text = "Revenue by Currency", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        
        metrics.topPerformers.forEach { performer ->
            CurrencyBreakdownCard(performer)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun CurrencyBreakdownCard(performer: RevenueByCurrency) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = performer.currencyCode, fontWeight = FontWeight.Bold)
                Text(
                    text = String.format(Locale.getDefault(), "%.1f%% of total", performer.percentageOfTotal * 100),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = CurrencyFormatter.formatCents(performer.totalAmount, performer.currencyCode),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun RevenueSummaryCard(label: String, amountCents: Long, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = label, 
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
            Text(
                text = CurrencyFormatter.formatCents(amountCents),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
