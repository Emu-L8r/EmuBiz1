package com.emul8r.bizap.ui.gui2.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.emul8r.bizap.domain.analytics.SearchQuery
import com.emul8r.bizap.domain.analytics.SearchResult
import com.emul8r.bizap.domain.model.gui2.DashboardStateV2
import com.emul8r.bizap.ui.common.GradientBackgrounds.subtleVerticalGradient
import com.emul8r.bizap.ui.common.GradientBackgrounds.ImagePlaceholderBackground
import com.emul8r.bizap.ui.common.MetricCard
import com.emul8r.bizap.ui.dashboard.components.InvoiceStatusPieChart
import com.emul8r.bizap.ui.dashboard.components.NotesCard
import com.emul8r.bizap.ui.designsystem.BizapColors
import com.emul8r.bizap.ui.gui2.common.*
import com.emul8r.bizap.ui.gui2.components.animations.DashboardSkeletonV2
import com.emul8r.bizap.ui.gui2.dashboard.widgets.AnalyticsSearchBar
import com.emul8r.bizap.ui.gui2.dashboard.widgets.DashboardMetricsWidget
import com.emul8r.bizap.ui.navigation.Screen
import timber.log.Timber

/**
 * GUI2 main dashboard screen.
 * Displays a unified view of revenue, payment, and risk metrics for the given business,
 * with quick-action shortcuts to Customers and Invoices screens.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreenV2(
    businessId: Long,
    navController: NavController,
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
    val statusCounts by viewModel.statusCounts.collectAsStateWithLifecycle()
    val currentNotesCount by viewModel.currentNotesCount.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                    IconButton(onClick = onSwitchToGui1) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = "Switch to Classic UI")
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
                statusCounts = statusCounts,
                currentNotesCount = currentNotesCount,
                navController = navController,
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
    statusCounts: Map<String, Int>,
    currentNotesCount: Int,
    navController: NavController,
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

            // ── Analytics Search Bar (NEW) ────────────────────────────────
            val searchResults = remember { mutableStateOf<List<SearchResult>>(emptyList()) }

            AnalyticsSearchBar(
                onSearch = { query ->
                    // TODO: Wire to actual search repository in Week 2
                    // For now, using mock data to demonstrate search
                    searchResults.value = getMockSearchResults(query.keyword)
                },
                onResultClick = { result ->
                    // Navigate based on result type
                    when (result.type) {
                        com.emul8r.bizap.domain.analytics.SearchType.INVOICE -> {
                            try {
                                navController.navigate("invoice/${result.id}")
                            } catch (e: Exception) {
                                Timber.e(e, "Failed to navigate to invoice")
                            }
                        }
                        com.emul8r.bizap.domain.analytics.SearchType.CUSTOMER -> {
                            onNavigateToCustomers()
                        }
                        else -> {}
                    }
                },
                searchResults = searchResults.value
            )

            HorizontalDivider()

            // ── Quick Action Buttons (Top Banner) ──────────────────────────
            QuickActionButtonsRow(
                onCreateCustomer = onCreateCustomer,
                onCreateInvoice = onCreateInvoice,
                onNavigateToVault = onNavigateToVault,
                onNavigateToAnalytics = onNavigateToInvoiceAnalytics
            )

            HorizontalDivider()

            // ── Dashboard Metrics Widget ──────────────────────────────────
            // TODO: Wire metrics from ViewModel once repository is wired
            // For now, using mock data to demonstrate UI
            val mockMetrics = com.emul8r.bizap.domain.repository.DashboardMetrics(
                unpaidInvoiceCount = statusCounts["SENT"]?.let { it + (statusCounts["PARTIALLY_PAID"] ?: 0) } ?: 0,
                unpaidAmount = state.paymentMetrics.outstandingAmount,
                overdueAmount = (state.paymentMetrics.overdueCount * 500).toLong(), // Mock calculation
                paidThisMonth = state.paymentMetrics.collectedAmount / 2, // Mock
                totalCustomersOwed = state.paymentMetrics.outstandingAmount,
                lastUpdatedMs = System.currentTimeMillis()
            )

            DashboardMetricsWidget(
                metrics = mockMetrics,
                onUnpaidClick = { onNavigateToPayment() },
                onOverdueClick = { onNavigateToPayment() },
                onPaidClick = { onNavigateToRevenue() }
            )

            HorizontalDivider()
            CategorizedSmartQuickTasks(
                overdueCount = statusCounts["OVERDUE"]?.let { overdue ->
                    statusCounts["PARTIALLY_PAID"]?.let { it + overdue } ?: overdue
                } ?: 0,
                draftCount = statusCounts["DRAFT"] ?: 0,
                totalInvoices = statusCounts.values.sum(),
                onCreateInvoice = onCreateInvoice,
                onViewOverdue = {
                    // Navigate to overdue invoices
                    onNavigateToRevenue()
                },
                onCompleteDrafts = {
                    // Navigate to draft invoices
                    onNavigateToInvoices()
                },
                onSendReminder = {
                    // TODO: Navigate to send reminder screen
                },
                onViewReports = onNavigateToRevenue,
                modifier = Modifier
            )

            HorizontalDivider()

            // ── Invoice Status Pie Chart ───────────────────────────────────
            InvoiceStatusPieChart(statusCounts = statusCounts)

            // ── Notes Card ────────────────────────────────────────────────
            NotesCard(
                currentNotesCount = currentNotesCount,
                onClick = {
                    // Safe navigation with error logging
                    try {
                        navController.navigate(Screen.Notes)
                    } catch (e: IllegalArgumentException) {
                        Timber.e(e, "Navigation to Notes screen failed")
                    }
                }
            )

            HorizontalDivider()
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
                backgroundColor = BizapColors.StatusPaid.copy(alpha = 0.08f),
                borderColor = BizapColors.StatusPaid.copy(alpha = 0.3f),
                accentColor = BizapColors.StatusPaid,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Actual Revenue",
                value = com.emul8r.bizap.utils.CentsFormatter.formatCents(actualRevenue),
                icon = Icons.Default.CheckCircle,
                backgroundColor = BizapColors.StatusSent.copy(alpha = 0.08f),
                borderColor = BizapColors.StatusSent.copy(alpha = 0.3f),
                accentColor = BizapColors.StatusSent,
                modifier = Modifier.weight(1f)
            )
        }
        
        // Outstanding amount card
        if (state.paymentMetrics.outstandingAmount > 0L) {
            MetricCard(
                title = "Outstanding",
                value = com.emul8r.bizap.utils.CentsFormatter.formatCents(state.paymentMetrics.outstandingAmount),
                icon = Icons.Default.Schedule,
                backgroundColor = BizapColors.StatusOutstanding.copy(alpha = 0.08f),
                borderColor = BizapColors.StatusOutstanding.copy(alpha = 0.3f),
                accentColor = BizapColors.StatusOutstanding,
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
                backgroundColor = BizapColors.StatusPaid.copy(alpha = 0.08f),
                borderColor = BizapColors.StatusPaid.copy(alpha = 0.3f),
                accentColor = BizapColors.StatusPaid,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Overdue",
                value = "${state.paymentMetrics.overdueCount}",
                icon = Icons.Default.Error,
                backgroundColor = BizapColors.StatusOverdue.copy(alpha = 0.08f),
                borderColor = BizapColors.StatusOverdue.copy(alpha = 0.3f),
                accentColor = BizapColors.StatusOverdue,
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
                backgroundColor = BizapColors.StatusOverdue.copy(alpha = 0.08f),
                borderColor = BizapColors.StatusOverdue.copy(alpha = 0.3f),
                accentColor = BizapColors.StatusOverdue,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "At Risk",
                value = "${state.riskMetrics.atRiskCount}",
                icon = Icons.Default.Warning,
                backgroundColor = BizapColors.StatusOutstanding.copy(alpha = 0.08f),
                borderColor = BizapColors.StatusOutstanding.copy(alpha = 0.3f),
                accentColor = BizapColors.StatusOutstanding,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Healthy",
                value = "${state.riskMetrics.healthyCount}",
                icon = Icons.Default.CheckCircle,
                backgroundColor = BizapColors.StatusPaid.copy(alpha = 0.08f),
                borderColor = BizapColors.StatusPaid.copy(alpha = 0.3f),
                accentColor = BizapColors.StatusPaid,
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

/**
 * Quick action buttons row at the top of the dashboard.
 * Provides fast access to common actions:
 * - Create New Customer
 * - Create New Invoice
 * - Open Document Vault
 * - View Analytics/Visual Data
 */
@Composable
private fun QuickActionButtonsRow(
    onCreateCustomer: () -> Unit,
    onCreateInvoice: () -> Unit,
    onNavigateToVault: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // First row: New Customer, New Invoice
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // New Customer Button
            Button(
                onClick = onCreateCustomer,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BizapColors.AnalyticsExcellent.copy(alpha = 0.9f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        Icons.Default.PersonAdd,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Color.White
                    )
                    Text(
                        "New Customer",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontSize = 10.sp
                    )
                }
            }

            // New Invoice Button
            Button(
                onClick = onCreateInvoice,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BizapColors.AnalyticsGood.copy(alpha = 0.9f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        Icons.Default.Receipt,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Color.White
                    )
                    Text(
                        "New Invoice",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontSize = 10.sp
                    )
                }
            }
        }

        // Second row: Vault, Analytics
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Vault Button
            Button(
                onClick = onNavigateToVault,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BizapColors.AnalyticsWarning.copy(alpha = 0.9f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        Icons.Default.Inventory,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Color.White
                    )
                    Text(
                        "Vault",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontSize = 10.sp
                    )
                }
            }

            // Visual Data / Analytics Button
            Button(
                onClick = onNavigateToAnalytics,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BizapColors.AnalyticsAtRisk.copy(alpha = 0.9f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        Icons.Default.BarChart,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Color.White
                    )
                    Text(
                        "Analytics",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

/**
 * Generate mock search results for demonstration.
 *
 * TODO: Replace with real search repository in Week 2
 */
private fun getMockSearchResults(keyword: String): List<SearchResult> {
    if (keyword.trim().isEmpty()) return emptyList()

    val keywordLower = keyword.lowercase()

    // Mock invoices
    val mockInvoices = listOf(
        SearchResult(
            id = 1001L,
            title = "Invoice #2024-001",
            subtitle = "\$2,500.00",
            type = com.emul8r.bizap.domain.analytics.SearchType.INVOICE
        ),
        SearchResult(
            id = 1002L,
            title = "Invoice #2024-002",
            subtitle = "\$1,850.00",
            type = com.emul8r.bizap.domain.analytics.SearchType.INVOICE
        ),
        SearchResult(
            id = 1003L,
            title = "Invoice #2024-003",
            subtitle = "\$3,200.00",
            type = com.emul8r.bizap.domain.analytics.SearchType.INVOICE
        ),
    )

    // Mock customers
    val mockCustomers = listOf(
        SearchResult(
            id = 2001L,
            title = "Acme Corporation",
            subtitle = "acme@company.com",
            type = com.emul8r.bizap.domain.analytics.SearchType.CUSTOMER
        ),
        SearchResult(
            id = 2002L,
            title = "Tech Solutions Inc",
            subtitle = "contact@techsolutions.com",
            type = com.emul8r.bizap.domain.analytics.SearchType.CUSTOMER
        ),
        SearchResult(
            id = 2003L,
            title = "Global Enterprises",
            subtitle = "info@globalent.com",
            type = com.emul8r.bizap.domain.analytics.SearchType.CUSTOMER
        ),
    )

    // Filter by keyword
    val results = (mockInvoices + mockCustomers).filter { result ->
        result.title.lowercase().contains(keywordLower) ||
        result.subtitle.lowercase().contains(keywordLower)
    }

    return results.take(10)  // Limit to 10 results
}
