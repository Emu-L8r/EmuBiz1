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
    val selectedStatuses = remember { mutableStateOf(setOf(InvoiceStatus.SENT, InvoiceStatus.OVERDUE)) }
    val isExporting = remember { mutableStateOf(false) }

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
            // Status filter
            StatusFilterChipsV2(
                selectedStatuses = selectedStatuses.value,
                onStatusesSelected = { selectedStatuses.value = it },
                modifier = Modifier.padding(vertical = 12.dp)
            )

            // Loading indicator if exporting
            if (isExporting.value) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

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
                    selectedStatuses = selectedStatuses.value
                )
            }
        }
    }
}

@Composable
private fun PaymentAnalyticsContentV2(
    metrics: PaymentMetricsV2,
    selectedStatuses: Set<InvoiceStatus> = setOf(InvoiceStatus.SENT, InvoiceStatus.OVERDUE),
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionHeaderV2("Collection Summary")

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCardV2(
                label = "Outstanding",
                value = formatCents(metrics.outstandingAmount),
                modifier = Modifier.weight(1f)
            )
            MetricCardV2(
                label = "Collected",
                value = formatCents(metrics.collectedAmount),
                modifier = Modifier.weight(1f)
            )
        }

        MetricCardV2(
            label = "Collection Rate",
            value = "%.1f%%".format(metrics.collectionRate),
            modifier = Modifier.fillMaxWidth()
        )

        MetricCardV2(
            label = "Avg Days to Payment",
            value = "%.1f days".format(metrics.averageDaysToPayment),
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
                value = "${metrics.paidCount}",
                modifier = Modifier.weight(1f)
            )
            MetricCardV2(
                label = "Sent",
                value = "${metrics.sentCount}",
                modifier = Modifier.weight(1f)
            )
            MetricCardV2(
                label = "Overdue",
                value = "${metrics.overdueCount}",
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCardV2(
                label = "Partial",
                value = "${metrics.partiallyPaidCount}",
                modifier = Modifier.weight(1f)
            )
            MetricCardV2(
                label = "Draft",
                value = "${metrics.draftCount}",
                modifier = Modifier.weight(1f)
            )
            MetricCardV2(
                label = "Total",
                value = "${metrics.totalInvoices}",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
