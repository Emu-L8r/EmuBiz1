package com.emul8r.bizap.ui.revenue

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
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
import com.emul8r.bizap.domain.revenue.model.RevenueMetrics
import com.emul8r.bizap.ui.common.GradientBackgrounds.subtleVerticalGradient
import com.emul8r.bizap.ui.common.MetricCard
import com.emul8r.bizap.ui.theme.StatusColors
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
            .subtleVerticalGradient()
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

        // Color-coded MTD and YTD revenue cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "MTD Collected",
                value = CentsFormatter.formatCents(metrics.mtdRevenue),
                icon = Icons.Default.CheckCircle,
                backgroundColor = StatusColors.Paid.copy(alpha = 0.08f),
                borderColor = StatusColors.Paid.copy(alpha = 0.3f),
                accentColor = StatusColors.Paid,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "YTD Collected",
                value = CentsFormatter.formatCents(metrics.ytdRevenue),
                icon = Icons.Default.AttachMoney,
                backgroundColor = StatusColors.Sent.copy(alpha = 0.08f),
                borderColor = StatusColors.Sent.copy(alpha = 0.3f),
                accentColor = StatusColors.Sent,
                modifier = Modifier.weight(1f)
            )
        }

        // Outstanding amount with orange accent
        if (metrics.outstandingAmount > 0L) {
            MetricCard(
                title = "Outstanding (Expected)",
                value = CentsFormatter.formatCents(metrics.outstandingAmount),
                icon = Icons.Default.Schedule,
                backgroundColor = StatusColors.Outstanding.copy(alpha = 0.08f),
                borderColor = StatusColors.Outstanding.copy(alpha = 0.3f),
                accentColor = StatusColors.Outstanding,
                modifier = Modifier.fillMaxWidth()
            )
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
