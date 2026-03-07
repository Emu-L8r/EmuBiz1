package com.emul8r.bizap.ui.revenue

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
            when (val s = state) {
                is RevenueDashboardUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is RevenueDashboardUiState.Success -> {
                    RevenueDashboardContent(
                        metrics = s.metrics,
                        isRefreshing = isRefreshing,
                        onRefresh = { viewModel.forceRefresh() },
                        onRebuild = { viewModel.rebuildSnapshots() },
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
                        Spacer(Modifier.height(16.dp))
                        DashboardActionButtons(
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
private fun RevenueDashboardContent(
    metrics: RevenueMetrics,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onRebuild: () -> Unit,
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

        DashboardActionButtons(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            onRebuild = onRebuild
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RevenueSummaryCard(label = "MTD", amountCents = metrics.mtdRevenue, modifier = Modifier.weight(1f))
            RevenueSummaryCard(label = "YTD", amountCents = metrics.ytdRevenue, modifier = Modifier.weight(1f))
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
private fun DashboardActionButtons(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onRebuild: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
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
