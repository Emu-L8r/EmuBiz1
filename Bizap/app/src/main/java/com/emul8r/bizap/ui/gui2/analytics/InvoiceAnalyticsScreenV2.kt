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
    invoiceViewModel: InvoiceAnalyticsViewModelV2 = hiltViewModel(),
    paymentViewModel: PaymentAnalyticsViewModelV2 = hiltViewModel(),
    customerViewModel: CustomerAnalyticsViewModelV2 = hiltViewModel(),
    riskViewModel: RiskAnalyticsViewModelV2 = hiltViewModel()
) {
    val invoiceState by invoiceViewModel.uiState.collectAsStateWithLifecycle()
    val paymentState by paymentViewModel.uiState.collectAsStateWithLifecycle()
    val customerState by customerViewModel.uiState.collectAsStateWithLifecycle()
    val riskState by riskViewModel.uiState.collectAsStateWithLifecycle()
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

            // Tab content - render with proper state handling
            when (selectedTabIndex) {
                0 -> {
                    when {
                        invoiceState.isLoading -> LoadingIndicatorV2(modifier = Modifier.padding(paddingValues))
                        invoiceState.error != null -> ErrorStateV2(
                            message = invoiceState.error ?: "Unknown error",
                            modifier = Modifier.padding(paddingValues)
                        )
                        else -> InvoiceAnalyticsContent(
                            state = invoiceState,
                            onSetGranularity = invoiceViewModel::setGranularity,
                            onSetDateRange = invoiceViewModel::setDateRange,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                1 -> {
                    // Payment analytics tab
                    when (paymentState) {
                        PaymentAnalyticsUiStateV2.Loading -> LoadingIndicatorV2(modifier = Modifier.fillMaxSize())
                        is PaymentAnalyticsUiStateV2.Error -> ErrorStateV2(
                            message = (paymentState as PaymentAnalyticsUiStateV2.Error).message,
                            modifier = Modifier.fillMaxSize()
                        )
                        is PaymentAnalyticsUiStateV2.Success -> PaymentAnalyticsContent(
                            metrics = (paymentState as PaymentAnalyticsUiStateV2.Success).metrics,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                2 -> {
                    // Customer analytics tab
                    when (customerState) {
                        CustomerAnalyticsUiStateV2.Loading -> LoadingIndicatorV2(modifier = Modifier.fillMaxSize())
                        is CustomerAnalyticsUiStateV2.Error -> ErrorStateV2(
                            message = (customerState as CustomerAnalyticsUiStateV2.Error).message,
                            modifier = Modifier.fillMaxSize()
                        )
                        is CustomerAnalyticsUiStateV2.Success -> CustomerAnalyticsContent(
                            metrics = (customerState as CustomerAnalyticsUiStateV2.Success).metrics,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                3 -> {
                    // Risk analytics tab
                    when (riskState) {
                        RiskAnalyticsUiStateV2.Loading -> LoadingIndicatorV2(modifier = Modifier.fillMaxSize())
                        is RiskAnalyticsUiStateV2.Error -> ErrorStateV2(
                            message = (riskState as RiskAnalyticsUiStateV2.Error).message,
                            modifier = Modifier.fillMaxSize()
                        )
                        is RiskAnalyticsUiStateV2.Success -> RiskAnalyticsContent(
                            metrics = (riskState as RiskAnalyticsUiStateV2.Success).metrics,
                            modifier = Modifier.fillMaxSize()
                        )
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
    if (data.isEmpty()) {
        return // Empty data, don't render chart
    }

    val maxTotal = data.maxOfOrNull { it.totalCount } ?: 1

    Canvas(modifier = modifier) {
        val chartWidth = size.width
        val chartHeight = size.height - 40f  // Reserve space for labels
        val barWidth = (chartWidth / data.size) * 0.6f
        val gap = (chartWidth / data.size) * 0.4f / 2

        data.forEachIndexed { index, stat ->
            val x = index * (chartWidth / data.size) + gap
            val totalBarHeight = (stat.totalCount.coerceAtLeast(1).toFloat() / maxTotal) * chartHeight
            val paidBarHeight = (stat.paidCount.coerceAtLeast(0).toFloat() / maxTotal) * chartHeight
            val sentBarHeight = (totalBarHeight - paidBarHeight).coerceAtLeast(0f)

            // Draw SENT/unpaid first (bottom)
            if (sentBarHeight > 0) {
                drawRect(
                    color = sentColor,
                    topLeft = Offset(x, chartHeight - totalBarHeight),
                    size = Size(barWidth, sentBarHeight)
                )
            }

            // Draw PAID second (top) - stacked on top of sent
            if (paidBarHeight > 0) {
                drawRect(
                    color = paidColor,
                    topLeft = Offset(x, chartHeight - paidBarHeight),
                    size = Size(barWidth, paidBarHeight)
                )
            }

            // Period label (X axis)
            try {
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
            } catch (e: Exception) {
                // Silently fail on canvas text rendering - not critical
            }
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
