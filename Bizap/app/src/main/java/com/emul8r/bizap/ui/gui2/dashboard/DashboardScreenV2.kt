package com.emul8r.bizap.ui.gui2.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emul8r.bizap.domain.model.gui2.DashboardStateV2
import com.emul8r.bizap.ui.common.GradientBackgrounds.subtleVerticalGradient
import com.emul8r.bizap.ui.common.GradientBackgrounds.ImagePlaceholderBackground
import com.emul8r.bizap.ui.common.MetricCard
import com.emul8r.bizap.ui.gui2.common.*
import com.emul8r.bizap.ui.gui2.components.animations.DashboardSkeletonV2
import com.emul8r.bizap.ui.theme.StatusColors

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
    onNavigateToInvoiceAnalytics: () -> Unit = {},
    onNavigateToDunningNotices: () -> Unit = {},
    onNavigateToVault: () -> Unit = {},
    onCreateCustomer: () -> Unit,
    onCreateInvoice: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    onSwitchToGui1: () -> Unit = {},
    viewModel: DashboardViewModelV2 = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
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
                onNavigateToInvoiceAnalytics = onNavigateToInvoiceAnalytics,
                onNavigateToDunningNotices = onNavigateToDunningNotices,
                onNavigateToVault = onNavigateToVault,
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
    onNavigateToInvoiceAnalytics: () -> Unit = {},
    onNavigateToDunningNotices: () -> Unit = {},
    onNavigateToVault: () -> Unit = {},
    onCreateCustomer: () -> Unit,
    onCreateInvoice: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Background watermark
        ImagePlaceholderBackground(
            alpha = 0.08f
        )

        Column(
            modifier = modifier
                .fillMaxSize()
                .subtleVerticalGradient()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = state.businessContext.businessName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            // ── Quick Actions ──
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

        // ── Revenue section: Expected vs Actual with color coding ──
        SectionHeaderV2(title = "Revenue")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Expected Revenue = outstanding + collected
            val expectedRevenue = state.paymentMetrics.outstandingAmount + state.paymentMetrics.collectedAmount
            val actualRevenue = state.paymentMetrics.collectedAmount
            MetricCard(
                title = "Expected Revenue",
                value = com.emul8r.bizap.utils.CentsFormatter.formatCents(expectedRevenue),
                icon = Icons.Default.TrendingUp,
                backgroundColor = StatusColors.Paid.copy(alpha = 0.08f),
                borderColor = StatusColors.Paid.copy(alpha = 0.3f),
                accentColor = StatusColors.Paid,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Actual Revenue",
                value = com.emul8r.bizap.utils.CentsFormatter.formatCents(actualRevenue),
                icon = Icons.Default.CheckCircle,
                backgroundColor = StatusColors.Sent.copy(alpha = 0.08f),
                borderColor = StatusColors.Sent.copy(alpha = 0.3f),
                accentColor = StatusColors.Sent,
                modifier = Modifier.weight(1f)
            )
        }
        
        // Outstanding amount card
        if (state.paymentMetrics.outstandingAmount > 0L) {
            MetricCard(
                title = "Outstanding",
                value = com.emul8r.bizap.utils.CentsFormatter.formatCents(state.paymentMetrics.outstandingAmount),
                icon = Icons.Default.Schedule,
                backgroundColor = StatusColors.Outstanding.copy(alpha = 0.08f),
                borderColor = StatusColors.Outstanding.copy(alpha = 0.3f),
                accentColor = StatusColors.Outstanding,
                modifier = Modifier.fillMaxWidth()
            )
        }
        OutlinedButton(
            onClick = onNavigateToRevenue,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("View Revenue Dashboard")
            Spacer(Modifier.width(4.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
        }

        HorizontalDivider()

        // ── Invoice Metrics section (replaces old Revenue section) ──
        SectionHeaderV2(title = "Invoices Sent")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCardV2(
                label = "Total Invoices",
                value = "${state.invoiceMetrics.totalInvoices}",
                modifier = Modifier.weight(1f)
            )
            MetricCardV2(
                label = "Paid",
                value = "${state.invoiceMetrics.paidCount}",
                modifier = Modifier.weight(1f)
            )
            MetricCardV2(
                label = "Pending",
                value = "${state.invoiceMetrics.pendingCount}",
                modifier = Modifier.weight(1f)
            )
        }
        OutlinedButton(
            onClick = onNavigateToInvoiceAnalytics,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("View Invoice Analytics")
            Spacer(Modifier.width(4.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
        }

        HorizontalDivider()

        // ── Payments section with color coding ──
        SectionHeaderV2(title = "Payments")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "Paid",
                value = "${state.paymentMetrics.paidCount}",
                icon = Icons.Default.CheckCircle,
                backgroundColor = StatusColors.Paid.copy(alpha = 0.08f),
                borderColor = StatusColors.Paid.copy(alpha = 0.3f),
                accentColor = StatusColors.Paid,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Overdue",
                value = "${state.paymentMetrics.overdueCount}",
                icon = Icons.Default.Error,
                backgroundColor = StatusColors.Overdue.copy(alpha = 0.08f),
                borderColor = StatusColors.Overdue.copy(alpha = 0.3f),
                accentColor = StatusColors.Overdue,
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

        // ── Risk section with color coding ──
        SectionHeaderV2(title = "Risk Overview")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "High Risk",
                value = "${state.riskMetrics.highRiskCount}",
                icon = Icons.Default.Error,
                backgroundColor = StatusColors.Overdue.copy(alpha = 0.08f),
                borderColor = StatusColors.Overdue.copy(alpha = 0.3f),
                accentColor = StatusColors.Overdue,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "At Risk",
                value = "${state.riskMetrics.atRiskCount}",
                icon = Icons.Default.Warning,
                backgroundColor = StatusColors.Outstanding.copy(alpha = 0.08f),
                borderColor = StatusColors.Outstanding.copy(alpha = 0.3f),
                accentColor = StatusColors.Outstanding,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Healthy",
                value = "${state.riskMetrics.healthyCount}",
                icon = Icons.Default.CheckCircle,
                backgroundColor = StatusColors.Paid.copy(alpha = 0.08f),
                borderColor = StatusColors.Paid.copy(alpha = 0.3f),
                accentColor = StatusColors.Paid,
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

        // ── Dunning Notices quick link ──
        SectionHeaderV2(title = "Dunning Notices")
        OutlinedButton(
            onClick = onNavigateToDunningNotices,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("Manage Overdue Reminders")
            Spacer(Modifier.weight(1f))
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
        OutlinedButton(
            onClick = onNavigateToVault,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Inventory, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("Document Vault")
            Spacer(Modifier.weight(1f))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
