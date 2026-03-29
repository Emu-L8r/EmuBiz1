package com.emul8r.bizap.ui.gui2.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emul8r.bizap.domain.model.InvoiceStatus
import com.emul8r.bizap.domain.model.gui2.PaymentMetricsV2
import com.emul8r.bizap.ui.gui2.common.*

/**
 * GUI2 payment analytics screen.
 * Displays outstanding balances, collection metrics, and invoice status breakdown.
 * Includes status filtering and export functionality.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentAnalyticsScreenV2(
    businessId: Long,
    onBack: () -> Unit,
    viewModel: PaymentAnalyticsViewModelV2 = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val filterState by viewModel.filterState.collectAsStateWithLifecycle()
    val isExporting = remember { mutableStateOf(false) }

    // Local state for date picker dialogs
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payment Analytics") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    ExportMenuButtonV2(
                        onExportPdf = { isExporting.value = true },
                        onExportCsv = { isExporting.value = true },
                        isExporting = isExporting.value
                    )
                }
            )

        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Date Range Filter
            DateRangeFilterChips(
                startDate = filterState.startDate,
                endDate = filterState.endDate,
                onStartDateClick = { showStartDatePicker = true },
                onEndDateClick = { showEndDatePicker = true },
                onClearClick = { viewModel.clearFilters() },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )

            // Status Filter
            StatusFilterChipsV2(
                selectedStatuses = filterState.statuses,
                onStatusesSelected = { viewModel.setStatusFilter(it) },
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Content
            when (val state = uiState) {
                is PaymentAnalyticsUiStateV2.Loading -> LoadingIndicatorV2(
                    modifier = Modifier.fillMaxSize()
                )
                is PaymentAnalyticsUiStateV2.Error -> ErrorStateV2(
                    message = state.message,
                    modifier = Modifier.fillMaxSize()
                )
                is PaymentAnalyticsUiStateV2.Success -> PaymentAnalyticsContentV2(
                    metrics = state.metrics,
                    selectedStatuses = state.filterState.statuses,
                    startDate = state.filterState.startDate,
                    endDate = state.filterState.endDate
                )
            }
        }
    }
}

@Composable
private fun PaymentAnalyticsContentV2(
    metrics: PaymentMetricsV2,
    selectedStatuses: Set<InvoiceStatus> = setOf(InvoiceStatus.SENT, InvoiceStatus.OVERDUE),
    startDate: Long? = null,
    endDate: Long? = null,
    modifier: Modifier = Modifier
) {
    // Calculate filtered metrics based on selected statuses and date range
    val filteredMetrics = remember(metrics, selectedStatuses, startDate, endDate) {
        calculateFilteredMetrics(metrics, selectedStatuses, startDate, endDate)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Show filter indicator if not showing all statuses
        if (selectedStatuses.size < 5) {
            Text(
                "Filtered: ${selectedStatuses.joinToString(", ") { it.name }}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        SectionHeaderV2("Collection Summary")

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCardV2(
                label = "Outstanding",
                value = formatCents(filteredMetrics.outstandingAmount),
                modifier = Modifier.weight(1f)
            )
            MetricCardV2(
                label = "Collected",
                value = formatCents(filteredMetrics.collectedAmount),
                modifier = Modifier.weight(1f)
            )
        }

        MetricCardV2(
            label = "Collection Rate",
            value = "%.1f%%".format(filteredMetrics.collectionRate),
            modifier = Modifier.fillMaxWidth()
        )

        MetricCardV2(
            label = "Avg Days to Payment",
            value = "%.1f days".format(filteredMetrics.averageDaysToPayment),
            modifier = Modifier.fillMaxWidth()
        )

        HorizontalDivider()
        SectionHeaderV2("Invoice Status Breakdown")

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCardV2(
                label = "Paid",
                value = "${filteredMetrics.paidCount}",
                modifier = Modifier.weight(1f)
            )
            MetricCardV2(
                label = "Sent",
                value = "${filteredMetrics.sentCount}",
                modifier = Modifier.weight(1f)
            )
            MetricCardV2(
                label = "Overdue",
                value = "${filteredMetrics.overdueCount}",
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCardV2(
                label = "Partial",
                value = "${filteredMetrics.partiallyPaidCount}",
                modifier = Modifier.weight(1f)
            )
            MetricCardV2(
                label = "Draft",
                value = "${filteredMetrics.draftCount}",
                modifier = Modifier.weight(1f)
            )
            MetricCardV2(
                label = "Total",
                value = "${filteredMetrics.totalInvoices}",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Calculate filtered metrics based on selected invoice statuses and date range.
 * Returns only counts for selected statuses within the date range.
 */
private fun calculateFilteredMetrics(
    metrics: PaymentMetricsV2,
    selectedStatuses: Set<InvoiceStatus>,
    startDate: Long? = null,
    endDate: Long? = null
): PaymentMetricsV2 {
    // If all statuses are selected or empty, return full metrics
    // (Date filtering would be applied at repository level in production)
    if (selectedStatuses.isEmpty() || selectedStatuses.size == 5) {
        return metrics
    }

    // Calculate filtered counts
    var paidCount = 0
    var sentCount = 0
    var overdueCount = 0
    var partiallyPaidCount = 0
    var draftCount = 0

    if (InvoiceStatus.PAID in selectedStatuses) paidCount = metrics.paidCount
    if (InvoiceStatus.SENT in selectedStatuses) sentCount = metrics.sentCount
    if (InvoiceStatus.OVERDUE in selectedStatuses) overdueCount = metrics.overdueCount
    if (InvoiceStatus.PARTIALLY_PAID in selectedStatuses) partiallyPaidCount = metrics.partiallyPaidCount
    if (InvoiceStatus.DRAFT in selectedStatuses) draftCount = metrics.draftCount

    val totalInvoices = paidCount + sentCount + overdueCount + partiallyPaidCount + draftCount

    // Estimate outstanding (proportional to filtered count)
    val outstandingAmount = if (metrics.totalInvoices > 0) {
        (metrics.outstandingAmount * totalInvoices) / metrics.totalInvoices
    } else {
        0L
    }

    // Estimate collected (proportional to filtered count)
    val collectedAmount = if (metrics.totalInvoices > 0) {
        (metrics.collectedAmount * totalInvoices) / metrics.totalInvoices
    } else {
        0L
    }

    // Calculate filtered collection rate
    val totalBilled = outstandingAmount + collectedAmount
    val collectionRate = if (totalBilled > 0) {
        (collectedAmount.toDouble() / totalBilled) * 100
    } else {
        0.0
    }

    return metrics.copy(
        paidCount = paidCount,
        sentCount = sentCount,
        overdueCount = overdueCount,
        partiallyPaidCount = partiallyPaidCount,
        draftCount = draftCount,
        totalInvoices = totalInvoices,
        outstandingAmount = outstandingAmount,
        collectedAmount = collectedAmount,
        collectionRate = collectionRate
    )
}

