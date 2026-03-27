package com.emul8r.bizap.ui.gui2.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emul8r.bizap.data.local.dao.InvoicePeriodStat
import com.emul8r.bizap.ui.gui2.common.ErrorStateV2
import com.emul8r.bizap.ui.gui2.common.LoadingIndicatorV2
import com.emul8r.bizap.ui.gui2.common.MetricCardV2
import com.emul8r.bizap.ui.gui2.common.SectionHeaderV2

/**
 * GUI2 Analytics Hub - Displays multiple analytics views.
 * Features tabs for: Invoices, Payments, Customers, Risk
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceAnalyticsScreenV2(
    businessId: Long,
    onBack: () -> Unit,
    viewModel: InvoiceAnalyticsViewModelV2 = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTabIndex by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analytics Hub") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            // Tab row
            TabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Invoices") }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Payments") }
                )
                Tab(
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 },
                    text = { Text("Customers") }
                )
                Tab(
                    selected = selectedTabIndex == 3,
                    onClick = { selectedTabIndex = 3 },
                    text = { Text("Risk") }
                )
            }

            // Tab content
            when (selectedTabIndex) {
                0 -> {
                    when {
                        state.isLoading -> LoadingIndicatorV2(modifier = Modifier.padding(paddingValues))
                        state.error != null -> ErrorStateV2(
                            message = state.error ?: "Unknown error",
                            modifier = Modifier.padding(paddingValues)
                        )
                        else -> InvoiceAnalyticsContent(
                            state = state,
                            onSetGranularity = viewModel::setGranularity,
                            onSetDateRange = viewModel::setDateRange,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                1 -> {
                    // Payment analytics placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Payment,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text("Payment Analytics", style = MaterialTheme.typography.titleMedium)
                            Text("Collection rates, payment trends, and outstanding balances",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                2 -> {
                    // Customer analytics placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.People,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text("Customer Analytics", style = MaterialTheme.typography.titleMedium)
                            Text("Customer segments, lifetime value, and engagement metrics",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                3 -> {
                    // Risk analytics placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text("Risk Analytics", style = MaterialTheme.typography.titleMedium)
                            Text("At-risk customers, overdue invoices, and collection status",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InvoiceAnalyticsContent(
    state: InvoiceAnalyticsState,
    onSetGranularity: (AnalyticsGranularity) -> Unit,
    onSetDateRange: (AnalyticsDateRange) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── Granularity toggle ───────────────────────────────────────────────
        SectionHeaderV2("View by")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AnalyticsGranularity.entries.forEach { gran ->
                FilterChip(
                    selected = state.granularity == gran,
                    onClick = { onSetGranularity(gran) },
                    label = { Text(gran.name.lowercase().replaceFirstChar { it.uppercase() }) }
                )
            }
        }

        // ── Date range selector ──────────────────────────────────────────────
        SectionHeaderV2("Date range")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AnalyticsDateRange.entries.forEach { range ->
                FilterChip(
                    selected = state.dateRange == range,
                    onClick = { onSetDateRange(range) },
                    label = { Text(range.label) }
                )
            }
        }

        // ── Summary metrics ──────────────────────────────────────────────────
        if (state.data.isNotEmpty()) {
            val totalSent = state.data.sumOf { it.totalCount }
            val totalPaid = state.data.sumOf { it.paidCount }
            SectionHeaderV2("Summary")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCardV2(
                    label = "Total Sent",
                    value = "$totalSent",
                    modifier = Modifier.weight(1f)
                )
                MetricCardV2(
                    label = "Completed",
                    value = "$totalPaid",
                    modifier = Modifier.weight(1f)
                )
                MetricCardV2(
                    label = "Completion Rate",
                    value = if (totalSent > 0) "${(totalPaid * 100 / totalSent)}%" else "—",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // ── Bar chart ────────────────────────────────────────────────────────
        SectionHeaderV2("Invoices over time")
        if (state.data.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No invoice data for this period", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            InvoiceStackedBarChart(
                data = state.data,
                paidColor = MaterialTheme.colorScheme.primary,
                sentColor = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            )

            // ── Legend ──────────────────────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                LegendItem(color = MaterialTheme.colorScheme.primary, label = "Paid")
                LegendItem(color = MaterialTheme.colorScheme.secondary, label = "Sent (unpaid)")
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Canvas(modifier = Modifier.size(12.dp)) {
            drawRect(color = color, size = Size(size.width, size.height))
        }
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}

/**
 * Stacked bar chart that shows PAID (bottom, primary) and SENT/unpaid (top, secondary) per period.
 */
@Composable
private fun InvoiceStackedBarChart(
    data: List<InvoicePeriodStat>,
    paidColor: Color,
    sentColor: Color,
    modifier: Modifier = Modifier
) {
    val maxTotal = data.maxOfOrNull { it.totalCount } ?: 1

    Canvas(modifier = modifier) {
        val chartWidth = size.width
        val chartHeight = size.height - 40f  // Reserve space for labels
        val barWidth = (chartWidth / data.size) * 0.6f
        val gap = (chartWidth / data.size) * 0.4f / 2

        data.forEachIndexed { index, stat ->
            val x = index * (chartWidth / data.size) + gap
            val totalBarHeight = (stat.totalCount.toFloat() / maxTotal) * chartHeight
            val paidBarHeight = (stat.paidCount.toFloat() / maxTotal) * chartHeight
            val sentBarHeight = totalBarHeight - paidBarHeight

            // Paid (bottom)
            if (paidBarHeight > 0) {
                drawRect(
                    color = paidColor,
                    topLeft = Offset(x, chartHeight - totalBarHeight),
                    size = Size(barWidth, paidBarHeight)
                )
            }
            // Sent/unpaid (top)
            if (sentBarHeight > 0) {
                drawRect(
                    color = sentColor,
                    topLeft = Offset(x, chartHeight - totalBarHeight),
                    size = Size(barWidth, sentBarHeight)
                )
                // Paid sits on top of sent in this stacked view
                if (paidBarHeight > 0) {
                    drawRect(
                        color = paidColor,
                        topLeft = Offset(x, chartHeight - paidBarHeight),
                        size = Size(barWidth, paidBarHeight)
                    )
                }
            }

            // Period label (X axis)
            drawContext.canvas.nativeCanvas.drawText(
                stat.periodLabel.takeLast(5),  // e.g. "03-25" from "2026-03-25"
                x + barWidth / 2,
                size.height - 4f,
                android.graphics.Paint().apply {
                    textAlign = android.graphics.Paint.Align.CENTER
                    textSize = 28f
                    color = android.graphics.Color.GRAY
                }
            )
        }

        // Baseline
        drawLine(
            color = Color.LightGray,
            start = Offset(0f, chartHeight),
            end = Offset(chartWidth, chartHeight),
            strokeWidth = 1.5f
        )
    }
}
