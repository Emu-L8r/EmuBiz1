package com.emul8r.bizap.ui.gui2.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.emul8r.bizap.domain.analytics.SearchResult
import com.emul8r.bizap.domain.model.gui2.DashboardStateV2
import com.emul8r.bizap.ui.common.GradientBackgrounds.ImagePlaceholderBackground
import com.emul8r.bizap.ui.common.GradientBackgrounds.subtleVerticalGradient
import com.emul8r.bizap.ui.dashboard.components.InvoiceStatusPieChart
import com.emul8r.bizap.ui.dashboard.components.NotesCard
import com.emul8r.bizap.ui.designsystem.BizapColors
import com.emul8r.bizap.ui.designsystem.BizapMetricCard
import com.emul8r.bizap.ui.gui2.common.*
import com.emul8r.bizap.ui.gui2.components.animations.DashboardSkeletonV2
import com.emul8r.bizap.ui.gui2.dashboard.widgets.AnalyticsSearchBar
import com.emul8r.bizap.ui.gui2.dashboard.widgets.DashboardMetricsWidget
import timber.log.Timber

/**
 * GUI2 main dashboard screen.
 * Displays a unified view of revenue, payment, and risk metrics for the given business,
 * with quick-action shortcuts to Customers and Invoices screens.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Suppress("UNUSED_PARAMETER")
@Composable
fun DashboardScreenV2(
    businessId: Long,
    navController: NavController,
    onNavigateToRevenue: () -> Unit,
    onNavigateToPayment: () -> Unit,
    onNavigateToRisk: () -> Unit,
    onNavigateToCustomers: () -> Unit,
    onNavigateToInvoices: () -> Unit,
    onNavigateToInvoice: (Long) -> Unit,
    onNavigateToInvoiceAnalytics: () -> Unit = {},
    onNavigateToDunningNotices: () -> Unit = {},
    onNavigateToVault: () -> Unit = {},
    onNavigateToNotes: () -> Unit = {},
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
                onNavigateToRevenue = onNavigateToRevenue,
                onNavigateToPayment = onNavigateToPayment,
                onNavigateToRisk = onNavigateToRisk,
                onNavigateToCustomers = onNavigateToCustomers,
                onNavigateToInvoices = onNavigateToInvoices,
                onNavigateToInvoice = onNavigateToInvoice,
                onNavigateToInvoiceAnalytics = onNavigateToInvoiceAnalytics,
                onNavigateToDunningNotices = onNavigateToDunningNotices,
                onNavigateToVault = onNavigateToVault,
                onNavigateToNotes = onNavigateToNotes,
                onCreateCustomer = onCreateCustomer,
                onCreateInvoice = onCreateInvoice,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Suppress("DEPRECATION")
@Composable
private fun DashboardContentV2(
    state: DashboardStateV2,
    statusCounts: Map<String, Int>,
    currentNotesCount: Int,
    onNavigateToRevenue: () -> Unit,
    onNavigateToPayment: () -> Unit,
    onNavigateToRisk: () -> Unit,
    onNavigateToCustomers: () -> Unit,
    onNavigateToInvoices: () -> Unit,
    onNavigateToInvoice: (Long) -> Unit,
    onCreateCustomer: () -> Unit,
    onCreateInvoice: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToInvoiceAnalytics: () -> Unit = {},
    onNavigateToDunningNotices: () -> Unit = {},
    onNavigateToVault: () -> Unit = {},
    onNavigateToNotes: () -> Unit = {}
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

            // ── WIN #12: QUICK STATS CARD (HIGH VISIBILITY) ────────────────
            // Shows 4 key metrics at a glance: Revenue, Overdue, Due This Month, Pending
            QuickStatsCard(
                totalRevenue = state.revenueMetrics.ytdRevenue,
                amountOverdue = state.revenueMetrics.overdueAmount,
                dueThisMonth = statusCounts["SENT"]?.let { it + (statusCounts["PARTIALLY_PAID"] ?: 0) } ?: 0,
                pendingPayments = statusCounts["PARTIALLY_PAID"] ?: 0,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            HorizontalDivider()

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
                            onNavigateToInvoice(result.id)
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
                overdueAmount = state.paymentMetrics.overdueCount.toLong(), // Show COUNT of overdue invoices, not amount
                paidThisMonth = state.paymentMetrics.sentCount.toLong(), // Show COUNT of sent invoices, not amount
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
                onClick = onNavigateToNotes
            )

            HorizontalDivider()

            // ── Management Section ────────────────────────────────────────
            SectionHeaderV2(title = "Manage")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateToCustomers,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Customers", fontSize = 12.sp)
                }
                OutlinedButton(
                    onClick = onNavigateToInvoices,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Invoices", fontSize = 12.sp)
                }
                OutlinedButton(
                    onClick = onNavigateToVault,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Storage, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Vault", fontSize = 12.sp)
                }
            }

            HorizontalDivider()

            // ── Invoice Metrics section ──
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

            // ── Risk overview section ──
            SectionHeaderV2(title = "Risk Overview")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BizapMetricCard(
                    title = "High Risk",
                    value = "${state.riskMetrics.highRiskCount}",
                    icon = Icons.Default.Error,
                    backgroundColor = BizapColors.StatusOverdue.copy(alpha = 0.08f),
                    borderColor = BizapColors.StatusOverdue.copy(alpha = 0.3f),
                    accentColor = BizapColors.StatusOverdue,
                    modifier = Modifier.weight(1f)
                )
                BizapMetricCard(
                    title = "At Risk",
                    value = "${state.riskMetrics.atRiskCount}",
                    icon = Icons.Default.Warning,
                    backgroundColor = BizapColors.StatusOutstanding.copy(alpha = 0.08f),
                    borderColor = BizapColors.StatusOutstanding.copy(alpha = 0.3f),
                    accentColor = BizapColors.StatusOutstanding,
                    modifier = Modifier.weight(1f)
                )
                BizapMetricCard(
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

            // ── Payments section ──
            SectionHeaderV2(title = "Payments")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BizapMetricCard(
                    title = "Paid",
                    value = "${state.paymentMetrics.paidCount}",
                    icon = Icons.Default.CheckCircle,
                    backgroundColor = BizapColors.StatusPaid.copy(alpha = 0.08f),
                    borderColor = BizapColors.StatusPaid.copy(alpha = 0.3f),
                    accentColor = BizapColors.StatusPaid,
                    modifier = Modifier.weight(1f)
                )
                BizapMetricCard(
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

            SectionHeaderV2(title = "Revenue")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val expectedRevenue = state.paymentMetrics.outstandingAmount + state.paymentMetrics.collectedAmount
                val actualRevenue = state.paymentMetrics.collectedAmount

                BizapMetricCard(
                    title = "Expected Revenue",
                    value = com.emul8r.bizap.utils.CentsFormatter.formatCents(expectedRevenue),
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    backgroundColor = BizapColors.StatusPaid.copy(alpha = 0.08f),
                    borderColor = BizapColors.StatusPaid.copy(alpha = 0.3f),
                    accentColor = BizapColors.StatusPaid,
                    modifier = Modifier.weight(1f)
                )
                BizapMetricCard(
                    title = "Actual Revenue",
                    value = com.emul8r.bizap.utils.CentsFormatter.formatCents(actualRevenue),
                    icon = Icons.Default.CheckCircle,
                    backgroundColor = BizapColors.StatusSent.copy(alpha = 0.08f),
                    borderColor = BizapColors.StatusSent.copy(alpha = 0.3f),
                    accentColor = BizapColors.StatusSent,
                    modifier = Modifier.weight(1f)
                )
            }

            if (state.paymentMetrics.outstandingAmount > 0L) {
                BizapMetricCard(
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
            // New Customer Button - Outlined style with subtle color
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                color = BizapColors.AnalyticsExcellent.copy(alpha = 0.06f),
                border = BorderStroke(1.5.dp, BizapColors.AnalyticsExcellent.copy(alpha = 0.3f))
            ) {
                Button(
                    onClick = onCreateCustomer,
                    modifier = Modifier.fillMaxSize(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
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
                            tint = BizapColors.AnalyticsExcellent
                        )
                        Text(
                            "New Customer",
                            style = MaterialTheme.typography.labelSmall,
                            color = BizapColors.AnalyticsExcellent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // New Invoice Button - Outlined style
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                color = BizapColors.AnalyticsGood.copy(alpha = 0.06f),
                border = BorderStroke(1.5.dp, BizapColors.AnalyticsGood.copy(alpha = 0.3f))
            ) {
                Button(
                    onClick = onCreateInvoice,
                    modifier = Modifier.fillMaxSize(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
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
                            tint = BizapColors.AnalyticsGood
                        )
                        Text(
                            "New Invoice",
                            style = MaterialTheme.typography.labelSmall,
                            color = BizapColors.AnalyticsGood,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Second row: Vault, Analytics
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Vault Button - Outlined style
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                color = BizapColors.AnalyticsWarning.copy(alpha = 0.06f),
                border = BorderStroke(1.5.dp, BizapColors.AnalyticsWarning.copy(alpha = 0.3f))
            ) {
                Button(
                    onClick = onNavigateToVault,
                    modifier = Modifier.fillMaxSize(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
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
                            tint = BizapColors.AnalyticsWarning
                        )
                        Text(
                            "Vault",
                            style = MaterialTheme.typography.labelSmall,
                            color = BizapColors.AnalyticsWarning,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Visual Data / Analytics Button - Outlined style
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                color = BizapColors.AnalyticsAtRisk.copy(alpha = 0.06f),
                border = BorderStroke(1.5.dp, BizapColors.AnalyticsAtRisk.copy(alpha = 0.3f))
            ) {
                Button(
                    onClick = onNavigateToAnalytics,
                    modifier = Modifier.fillMaxSize(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
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
                            tint = BizapColors.AnalyticsAtRisk
                        )
                        Text(
                            "Analytics",
                            style = MaterialTheme.typography.labelSmall,
                            color = BizapColors.AnalyticsAtRisk,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
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

    val mockInvoices = listOf(
        SearchResult(
            id = 1001L,
            title = "Invoice #2024-001",
            subtitle = "$2,500.00",
            type = com.emul8r.bizap.domain.analytics.SearchType.INVOICE
        ),
        SearchResult(
            id = 1002L,
            title = "Invoice #2024-002",
            subtitle = "$1,850.00",
            type = com.emul8r.bizap.domain.analytics.SearchType.INVOICE
        ),
        SearchResult(
            id = 1003L,
            title = "Invoice #2024-003",
            subtitle = "$3,200.00",
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
