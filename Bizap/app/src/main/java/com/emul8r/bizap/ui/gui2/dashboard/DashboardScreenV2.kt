package com.emul8r.bizap.ui.gui2.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emul8r.bizap.domain.model.gui2.DashboardStateV2
import com.emul8r.bizap.ui.gui2.common.*
import com.emul8r.bizap.ui.gui2.components.animations.DashboardSkeletonV2

/**
 * GUI2 main dashboard screen.
 * Displays a unified view of revenue, payment, and risk metrics for the given business,
 * with quick-action shortcuts to Customers and Invoices screens.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreenV2(
    businessId: Long,
    onNavigateToRevenue: () -> Unit,
    onNavigateToPayment: () -> Unit,
    onNavigateToRisk: () -> Unit,
    onNavigateToInvoice: (Long) -> Unit,
    onNavigateToCustomers: () -> Unit,
    onNavigateToInvoices: () -> Unit,
    onCreateCustomer: () -> Unit,
    onCreateInvoice: () -> Unit,
    onSwitchToGui1: () -> Unit,
    viewModel: DashboardViewModelV2 = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                actions = {
                    TextButton(onClick = onSwitchToGui1) { Text("Switch to Classic") }
                }
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is DashboardUiStateV2.Loading -> DashboardSkeletonV2(
                modifier = Modifier.padding(paddingValues)
            )
            is DashboardUiStateV2.Error -> ErrorStateV2(
                message = state.message,
                modifier = Modifier.padding(paddingValues)
            )
            is DashboardUiStateV2.Success -> DashboardContentV2(
                state = state.state,
                onNavigateToRevenue = onNavigateToRevenue,
                onNavigateToPayment = onNavigateToPayment,
                onNavigateToRisk = onNavigateToRisk,
                onNavigateToCustomers = onNavigateToCustomers,
                onNavigateToInvoices = onNavigateToInvoices,
                onCreateCustomer = onCreateCustomer,
                onCreateInvoice = onCreateInvoice,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
private fun DashboardContentV2(
    state: DashboardStateV2,
    onNavigateToRevenue: () -> Unit,
    onNavigateToPayment: () -> Unit,
    onNavigateToRisk: () -> Unit,
    onNavigateToCustomers: () -> Unit,
    onNavigateToInvoices: () -> Unit,
    onCreateCustomer: () -> Unit,
    onCreateInvoice: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = state.businessContext.businessName,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        // ── Quick Actions (Silenced for review) ──
        /*
        SectionHeaderV2(title = "Quick Actions")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onCreateCustomer,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("New Customer")
            }
            Button(
                onClick = onCreateInvoice,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("New Invoice")
            }
        }

        HorizontalDivider()
        */

        // ── Revenue section ──
        SectionHeaderV2(title = "Revenue")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCardV2(
                label = "MTD Revenue",
                value = formatCents(state.revenueMetrics.mtdRevenue),
                modifier = Modifier.weight(1f)
            )
            MetricCardV2(
                label = "YTD Revenue",
                value = formatCents(state.revenueMetrics.ytdRevenue),
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCardV2(
                label = "Outstanding",
                value = formatCents(state.paymentMetrics.outstandingAmount),
                modifier = Modifier.weight(1f)
            )
            MetricCardV2(
                label = "Total Paid",
                value = formatCents(state.revenueMetrics.totalPaidRevenue),
                modifier = Modifier.weight(1f)
            )
        }
        OutlinedButton(
            onClick = onNavigateToRevenue,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("View Revenue Analytics")
            Spacer(Modifier.width(4.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
        }

        HorizontalDivider()

        // ── Payments section ──
        SectionHeaderV2(title = "Payments")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCardV2(
                label = "Paid",
                value = "${state.paymentMetrics.paidCount}",
                modifier = Modifier.weight(1f)
            )
            MetricCardV2(
                label = "Overdue",
                value = "${state.paymentMetrics.overdueCount}",
                modifier = Modifier.weight(1f)
            )
        }
        OutlinedButton(
            onClick = onNavigateToPayment,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("View Payment Analytics")
            Spacer(Modifier.width(4.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
        }

        HorizontalDivider()

        // ── Risk section ──
        SectionHeaderV2(title = "Risk Overview")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCardV2(
                label = "High Risk",
                value = "${state.riskMetrics.highRiskCount}",
                modifier = Modifier.weight(1f)
            )
            MetricCardV2(
                label = "At Risk",
                value = "${state.riskMetrics.atRiskCount}",
                modifier = Modifier.weight(1f)
            )
            MetricCardV2(
                label = "Healthy",
                value = "${state.riskMetrics.healthyCount}",
                modifier = Modifier.weight(1f)
            )
        }
        OutlinedButton(
            onClick = onNavigateToRisk,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("View Risk Dashboard")
            Spacer(Modifier.width(4.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
        }

        HorizontalDivider()

        // ── Navigation links ──
        SectionHeaderV2(title = "Manage")
        OutlinedButton(
            onClick = onNavigateToCustomers,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("View All Customers")
            Spacer(Modifier.weight(1f))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
        }
        OutlinedButton(
            onClick = onNavigateToInvoices,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("View All Invoices")
            Spacer(Modifier.weight(1f))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
