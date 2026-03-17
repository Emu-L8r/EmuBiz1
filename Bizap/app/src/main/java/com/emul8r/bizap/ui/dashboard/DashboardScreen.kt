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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.emul8r.bizap.domain.config.BizapConfig
import com.emul8r.bizap.ui.common.GradientBackgrounds.ImagePlaceholderBackground
import com.emul8r.bizap.ui.common.GradientBackgrounds.subtleVerticalGradient
import com.emul8r.bizap.ui.customers.CustomerViewModel
import com.emul8r.bizap.ui.dashboard.components.InvoiceStatusPieChart
import com.emul8r.bizap.ui.dashboard.components.NotesCard
import com.emul8r.bizap.ui.dashboard.components.analytics.CashFlowTrendChart
import com.emul8r.bizap.ui.dashboard.components.analytics.AverageDaysToPayMetric
import com.emul8r.bizap.ui.dashboard.components.analytics.RevenueConcentrationChart
import com.emul8r.bizap.ui.dashboard.components.analytics.InvoicingVelocityCard
import com.emul8r.bizap.ui.dashboard.components.base.AnalyticsSectionCard
import com.emul8r.bizap.ui.dashboard.components.base.HeaderCardBase
import com.emul8r.bizap.ui.dashboard.components.base.MetricCardBase
import com.emul8r.bizap.presentation.viewmodel.AnalyticsViewModel
import com.emul8r.bizap.presentation.viewmodel.AnalyticsUiState
import com.emul8r.bizap.ui.invoices.InvoiceList
import com.emul8r.bizap.ui.invoices.InvoiceListUiState
import com.emul8r.bizap.ui.invoices.InvoiceListViewModel
import com.emul8r.bizap.ui.navigation.Screen
import com.emul8r.bizap.ui.notes.NotesViewModel
import com.emul8r.bizap.ui.settings.BusinessProfileViewModel
import com.emul8r.bizap.ui.settings.components.BusinessSwitcherDialog
import com.emul8r.bizap.ui.theme.DashboardTheme
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
        ImagePlaceholderBackground(alpha = 0.08f)

        LazyColumn(
            modifier = Modifier
                .padding(DashboardTheme.screenPadding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(DashboardTheme.sectionSpacing)
        ) {
            // ── Business header ────────────────────────────────────────────
            item {
                HeaderCardBase(
                    title = activeBusiness.businessName.ifEmpty { "Default Business" },
                    subtitle = "ABN: ${activeBusiness.abn.ifEmpty { "Not Set" }}",
                    accentColor = MaterialTheme.colorScheme.primary,
                    trailingIcon = Icons.Default.SwapHoriz,
                    onTrailingClick = { showSwitcher = true }
                )
            }

            // ── Invoice status pie chart ───────────────────────────────────
            item {
                InvoiceStatusPieChart(statusCounts = statusCounts)
            }

            // ── Notes card ────────────────────────────────────────────────
            item {
                NotesCard(
                    currentNotesCount = currentNotesCount,
                    onClick = { navController.navigate(Screen.Notes) }
                )
            }

            // ── Row 1: Total Clients | Total Invoices ──────────────────────
            item {
                val totalInvoices = when (val s = invoiceState) {
                    is InvoiceListUiState.Success -> s.invoices.size
                    else -> 0
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(DashboardTheme.cardSpacing)
                ) {
                    MetricCardBase(
                        title = "Total Clients",
                        value = "${customers.size}",
                        icon = Icons.Default.People,
                        accentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCardBase(
                        title = "Total Invoices",
                        value = "$totalInvoices",
                        icon = Icons.Default.Receipt,
                        accentColor = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { navController.navigate(Screen.RevenueDashboard) }
                    )
                }
            }

            // ── Row 2: Invoices Paid | Invoices Pending ────────────────────
            item {
                val paidCount = statusCounts["PAID"] ?: 0
                val pendingCount = (statusCounts["SENT"] ?: 0) + (statusCounts["DRAFT"] ?: 0)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(DashboardTheme.cardSpacing)
                ) {
                    MetricCardBase(
                        title = "Invoices Paid",
                        value = "$paidCount",
                        icon = Icons.Default.CheckCircle,
                        accentColor = StatusColors.Paid,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCardBase(
                        title = "Invoices Pending",
                        value = "$pendingCount",
                        icon = Icons.Default.Schedule,
                        accentColor = StatusColors.Outstanding,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ── Row 3: Expected Revenue | Actual Revenue ───────────────────
            item {
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
                    horizontalArrangement = Arrangement.spacedBy(DashboardTheme.cardSpacing)
                ) {
                    MetricCardBase(
                        title = "Expected Revenue",
                        value = CentsFormatter.formatCents(expectedRevenue),
                        icon = Icons.Default.TrendingUp,
                        accentColor = StatusColors.Paid,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCardBase(
                        title = "Actual Revenue",
                        value = CentsFormatter.formatCents(actualRevenue),
                        icon = Icons.Default.CheckCircle,
                        accentColor = StatusColors.Sent,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ── Row 4: Outstanding | Overdue ───────────────────────────────
            item {
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
                    horizontalArrangement = Arrangement.spacedBy(DashboardTheme.cardSpacing)
                ) {
                    MetricCardBase(
                        title = "Outstanding",
                        value = CentsFormatter.formatCents(outstandingAmount),
                        icon = Icons.Default.Schedule,
                        accentColor = StatusColors.Outstanding,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCardBase(
                        title = "Overdue",
                        value = CentsFormatter.formatCents(overdueAmount),
                        icon = Icons.Default.Error,
                        accentColor = StatusColors.Overdue,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ── Analytics section ──────────────────────────────────────────
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
                        AnalyticsSectionCard(
                            title = "💡 Business Analytics",
                            accentColor = MaterialTheme.colorScheme.primary
                        ) {
                            CashFlowTrendChart(data.cashFlowTrend)
                            AverageDaysToPayMetric(
                                currentDaysToPayment = data.currentAverageDaysToPayment,
                                trendHistory = data.averageDaysToPayTrend,
                                config = BizapConfig()
                            )
                            RevenueConcentrationChart(topCustomers = data.topCustomerMetrics)
                            InvoicingVelocityCard(velocityData = emptyList())
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

            // ── Recent Invoices ────────────────────────────────────────────
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
