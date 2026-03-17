package com.emul8r.bizap.ui.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.emul8r.bizap.ui.common.GradientBackgrounds.ImagePlaceholderBackground
import com.emul8r.bizap.ui.common.GradientBackgrounds.subtleVerticalGradient
import com.emul8r.bizap.ui.common.MetricCard
import com.emul8r.bizap.ui.customers.CustomerViewModel
import com.emul8r.bizap.ui.dashboard.components.InvoiceStatusPieChart
import com.emul8r.bizap.ui.dashboard.components.NotesCard
import com.emul8r.bizap.ui.dashboard.components.analytics.CashFlowTrendChart
import com.emul8r.bizap.ui.dashboard.components.analytics.AverageDaysToPayMetric
import com.emul8r.bizap.ui.dashboard.components.analytics.RevenueConcentrationChart
import com.emul8r.bizap.ui.dashboard.components.analytics.InvoicingVelocityCard
import com.emul8r.bizap.presentation.viewmodel.AnalyticsViewModel
import com.emul8r.bizap.presentation.viewmodel.AnalyticsUiState
import com.emul8r.bizap.ui.invoices.InvoiceList
import com.emul8r.bizap.ui.invoices.InvoiceListUiState
import com.emul8r.bizap.ui.invoices.InvoiceListViewModel
import com.emul8r.bizap.ui.navigation.Screen
import com.emul8r.bizap.ui.notes.NotesViewModel
import com.emul8r.bizap.ui.settings.BusinessProfileViewModel
import com.emul8r.bizap.ui.settings.components.BusinessSwitcherDialog
import com.emul8r.bizap.ui.theme.StatusColors
import com.emul8r.bizap.utils.CentsFormatter

@Composable
fun DashboardScreen(
    navController: NavController,
    customerViewModel: CustomerViewModel = hiltViewModel(),
    businessViewModel: BusinessProfileViewModel = hiltViewModel(),
    dashboardViewModel: DashboardViewModel = hiltViewModel(),
    invoiceViewModel: InvoiceListViewModel = hiltViewModel(),
    notesViewModel: NotesViewModel = hiltViewModel(),
    analyticsViewModel: AnalyticsViewModel = hiltViewModel()
) {
    val customers by customerViewModel.uiState.collectAsStateWithLifecycle()
    val activeBusiness by businessViewModel.profileState.collectAsStateWithLifecycle()
    val revenueState by dashboardViewModel.revenueState.collectAsStateWithLifecycle()
    val invoiceState by invoiceViewModel.uiState.collectAsStateWithLifecycle()
    val currentNotesCount by notesViewModel.currentNotesCount.collectAsStateWithLifecycle()
    val analyticsState by analyticsViewModel.analyticsState.collectAsStateWithLifecycle()
    var showSwitcher by remember { mutableStateOf(false) }

    // Calculate status breakdown from invoices
    val statusCounts: Map<String, Int> = remember(invoiceState) {
        when (invoiceState) {
            is InvoiceListUiState.Success -> {
                val invoices = (invoiceState as InvoiceListUiState.Success).invoices
                invoices.groupBy { it.status.name }.mapValues { it.value.size } as Map<String, Int>
            }
            else -> emptyMap<String, Int>()
        }
    }

    if (showSwitcher) {
        BusinessSwitcherDialog(onDismiss = { showSwitcher = false })
    }

    Box(modifier = Modifier.fillMaxSize().subtleVerticalGradient()) {
        // Image background placeholder
        ImagePlaceholderBackground(
            // If you haven't moved thswalogo yet, this falls back to company_logo
            alpha = 0.08f 
        )

        LazyColumn(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = activeBusiness.businessName.ifEmpty { "Default Business" },
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "ABN: ${activeBusiness.abn.ifEmpty { "Not Set" }}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = { showSwitcher = true }) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Switch Business",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            item {
                InvoiceStatusPieChart(statusCounts = statusCounts)
            }

            item {
                NotesCard(
                    currentNotesCount = currentNotesCount,
                    onClick = { navController.navigate(Screen.Notes) }
                )
            }

            item {
                // First row: Total Clients and Total Invoices
                val totalInvoices = when (invoiceState) {
                    is InvoiceListUiState.Success -> (invoiceState as InvoiceListUiState.Success).invoices.size
                    else -> 0
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "Total Clients",
                        value = "${customers.size}",
                        icon = Icons.Default.People,
                        backgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        accentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Total Invoices",
                        value = "$totalInvoices",
                        icon = Icons.Default.Receipt,
                        backgroundColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                        borderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                        accentColor = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { navController.navigate(Screen.RevenueDashboard) }
                    )
                }
            }

            item {
                // Second row: Paid and Pending Invoices
                val paidCount = statusCounts["PAID"] ?: 0
                val pendingCount = (statusCounts["SENT"] ?: 0) + (statusCounts["DRAFT"] ?: 0)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "Invoices Paid",
                        value = "$paidCount",
                        icon = Icons.Default.CheckCircle,
                        backgroundColor = StatusColors.Paid.copy(alpha = 0.08f),
                        borderColor = StatusColors.Paid.copy(alpha = 0.3f),
                        accentColor = StatusColors.Paid,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Invoices Pending",
                        value = "$pendingCount",
                        icon = Icons.Default.Schedule,
                        backgroundColor = StatusColors.Outstanding.copy(alpha = 0.08f),
                        borderColor = StatusColors.Outstanding.copy(alpha = 0.3f),
                        accentColor = StatusColors.Outstanding,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                // Third row: Expected Revenue vs Actual Revenue with color coding
                val expectedRevenue: Long
                val actualRevenue: Long
                when (val s = revenueState) {
                    is DashboardRevenueState.Success -> {
                        expectedRevenue = s.metrics.outstandingAmount + s.metrics.totalPaidRevenue
                        actualRevenue = s.metrics.totalPaidRevenue
                    }
                    else -> {
                        expectedRevenue = 0L
                        actualRevenue = 0L
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "Expected Revenue",
                        value = CentsFormatter.formatCents(expectedRevenue),
                        icon = Icons.Default.TrendingUp,
                        backgroundColor = StatusColors.Paid.copy(alpha = 0.08f),
                        borderColor = StatusColors.Paid.copy(alpha = 0.3f),
                        accentColor = StatusColors.Paid,
                        modifier = Modifier.weight(1f)
                    )
                    
                    MetricCard(
                        title = "Actual Revenue",
                        value = CentsFormatter.formatCents(actualRevenue),
                        icon = Icons.Default.CheckCircle,
                        backgroundColor = StatusColors.Sent.copy(alpha = 0.08f),
                        borderColor = StatusColors.Sent.copy(alpha = 0.3f),
                        accentColor = StatusColors.Sent,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            item {
                // Fourth row: Outstanding and Overdue amounts
                val outstandingAmount: Long
                val overdueAmount: Long
                when (val s = revenueState) {
                    is DashboardRevenueState.Success -> {
                        outstandingAmount = s.metrics.outstandingAmount
                        val overdueCount = statusCounts["OVERDUE"] ?: 0
                        overdueAmount = if (overdueCount > 0 && outstandingAmount > 0) {
                            (outstandingAmount * 0.3).toLong()
                        } else {
                            0L
                        }
                    }
                    else -> {
                        outstandingAmount = 0L
                        overdueAmount = 0L
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "Outstanding",
                        value = CentsFormatter.formatCents(outstandingAmount),
                        icon = Icons.Default.Schedule,
                        backgroundColor = StatusColors.Outstanding.copy(alpha = 0.08f),
                        borderColor = StatusColors.Outstanding.copy(alpha = 0.3f),
                        accentColor = StatusColors.Outstanding,
                        modifier = Modifier.weight(1f)
                    )
                    
                    MetricCard(
                        title = "Overdue",
                        value = CentsFormatter.formatCents(overdueAmount),
                        icon = Icons.Default.Error,
                        backgroundColor = StatusColors.Overdue.copy(alpha = 0.08f),
                        borderColor = StatusColors.Overdue.copy(alpha = 0.3f),
                        accentColor = StatusColors.Overdue,
                        modifier = Modifier.weight(1f)
                    )
                }
            }


            // Analytics Section
            item {
                when (analyticsState) {
                    is AnalyticsUiState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    is AnalyticsUiState.Success -> {
                        val data = (analyticsState as AnalyticsUiState.Success).data
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Analytics Header
                            Text(
                                text = "💡 Business Analytics",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 8.dp)
                            )

                            // Cash Flow Trend
                            CashFlowTrendChart(data.cashFlowTrend)

                            // Days to Pay Metric
                            AverageDaysToPayMetric(
                                currentDaysToPayment = data.currentAverageDaysToPayment,
                                trendHistory = data.averageDaysToPayTrend
                            )

                            // Revenue Concentration
                            RevenueConcentrationChart(
                                topCustomers = data.topCustomerMetrics
                            )

                            // Invoicing Velocity
                            InvoicingVelocityCard(
                                velocityData = emptyList()  // Will be populated from ViewModel
                            )
                        }
                    }
                    is AnalyticsUiState.Error -> {
                        Text(
                            text = "Error loading analytics: ${(analyticsState as AnalyticsUiState.Error).message}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            item {
                Text("Recent Invoices", style = MaterialTheme.typography.titleMedium)
            }

            item {
                InvoiceList(
                    modifier = Modifier.fillMaxWidth(),
                    onInvoiceClick = { invoiceId ->
                        navController.navigate(Screen.InvoiceDetail(invoiceId))
                    }
                )
            }
        }
    }
}
