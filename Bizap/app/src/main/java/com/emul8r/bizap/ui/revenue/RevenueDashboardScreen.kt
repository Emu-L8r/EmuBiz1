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
import java.util.Locale

@Composable
fun RevenueDashboardScreen(
    viewModel: RevenueDashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
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

@Composable
private fun RevenueDashboardContent(metrics: RevenueMetrics) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RevenueSummaryCard(label = "MTD", amount = metrics.mtdRevenue.toDouble() / 100.0, modifier = Modifier.weight(1f))
            RevenueSummaryCard(label = "YTD", amount = metrics.ytdRevenue.toDouble() / 100.0, modifier = Modifier.weight(1f))
        }

        Text(text = "Revenue by Currency", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        
        metrics.topPerformers.forEach { performer ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = performer.currencyCode, fontWeight = FontWeight.Bold)
                    Text(text = "$${String.format(Locale.getDefault(), "%.2f", performer.totalAmount)}")
                }
            }
        }
    }
}

@Composable
private fun RevenueSummaryCard(label: String, amount: Double, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = label, style = MaterialTheme.typography.labelSmall)
            Text(
                text = "$${String.format(Locale.getDefault(), "%.2f", amount)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

