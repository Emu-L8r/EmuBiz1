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
import com.emul8r.bizap.ui.common.GradientBackgrounds.subtleVerticalGradient
import com.emul8r.bizap.ui.common.MetricCard
import com.emul8r.bizap.ui.customers.CustomerViewModel
import com.emul8r.bizap.ui.dashboard.components.InvoiceStatusPieChart
import com.emul8r.bizap.ui.dashboard.components.NotesCard
import com.emul8r.bizap.ui.invoices.InvoiceList
import com.emul8r.bizap.ui.invoices.InvoiceListUiState
import com.emul8r.bizap.ui.invoices.InvoiceListViewModel
import com.emul8r.bizap.ui.navigation.Screen
import com.emul8r.bizap.ui.notes.NotesViewModel
import com.emul8r.bizap.ui.revenue.RevenueDashboardUiState
import com.emul8r.bizap.ui.revenue.RevenueDashboardViewModel
import com.emul8r.bizap.ui.settings.BusinessProfileViewModel
import com.emul8r.bizap.ui.settings.components.BusinessSwitcherDialog
import com.emul8r.bizap.ui.theme.StatusColors
import com.emul8r.bizap.utils.CentsFormatter

@Composable
fun DashboardScreen(
    navController: NavController,
    customerViewModel: CustomerViewModel = hiltViewModel(),
    businessViewModel: BusinessProfileViewModel = hiltViewModel(),
    revenueViewModel: RevenueDashboardViewModel = hiltViewModel(),
    invoiceViewModel: InvoiceListViewModel = hiltViewModel(),
    notesViewModel: NotesViewModel = hiltViewModel()
) {
    val customers by customerViewModel.uiState.collectAsStateWithLifecycle()
    val activeBusiness by businessViewModel.profileState.collectAsStateWithLifecycle()
    val revenueState by revenueViewModel.uiState.collectAsStateWithLifecycle()
    val invoiceState by invoiceViewModel.uiState.collectAsStateWithLifecycle()
    val currentNotesCount by notesViewModel.currentNotesCount.collectAsStateWithLifecycle()
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

    LazyColumn(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
            .subtleVerticalGradient(),
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
            // First row: Total Clients and Total Invoices
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ElevatedCard(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Icon(Icons.Default.People, contentDescription = null)
                        Text("Total Clients", style = MaterialTheme.typography.labelMedium)
                        Text("${customers.size}", style = MaterialTheme.typography.headlineMedium)
                    }
                }

                ElevatedCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { navController.navigate(Screen.RevenueDashboard) },
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Icon(Icons.Default.Receipt, contentDescription = null)
                        Text("Total Invoices", style = MaterialTheme.typography.labelMedium)
                        // Calculate total invoices from invoice state
                        val totalInvoices = when (invoiceState) {
                            is InvoiceListUiState.Success -> (invoiceState as InvoiceListUiState.Success).invoices.size
                            else -> 0
                        }
                        Text("$totalInvoices", style = MaterialTheme.typography.headlineMedium)
                    }
                }
            }
        }

        item {
            // Second row: Paid and Pending Invoices
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ElevatedCard(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Text("Invoices Paid", style = MaterialTheme.typography.labelMedium)
                        val paidCount = statusCounts["PAID"] ?: 0
                        Text("$paidCount", style = MaterialTheme.typography.headlineMedium)
                    }
                }

                ElevatedCard(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Icon(Icons.Default.Schedule, contentDescription = null)
                        Text("Invoices Pending", style = MaterialTheme.typography.labelMedium)
                        val pendingCount = (statusCounts["SENT"] ?: 0) + (statusCounts["DRAFT"] ?: 0)
                        Text("$pendingCount", style = MaterialTheme.typography.headlineMedium)
                    }
                }
            }
        }

        item {
            // Third row: Expected Revenue vs Actual Revenue with color coding
            val expectedRevenue: Long
            val actualRevenue: Long
            val outstandingAmount: Long
            val overdueAmount: Long
            when (val s = revenueState) {
                is RevenueDashboardUiState.Success -> {
                    // Expected Revenue = outstanding + collected (total billed)
                    expectedRevenue = s.metrics.outstandingAmount + s.metrics.totalPaidRevenue
                    // Actual Revenue = amount already collected
                    actualRevenue = s.metrics.totalPaidRevenue
                    outstandingAmount = s.metrics.outstandingAmount
                    // Calculate overdue from invoice status counts
                    val overdueCount = statusCounts["OVERDUE"] ?: 0
                    // Estimate overdue amount (proportional to count)
                    overdueAmount = if (overdueCount > 0 && outstandingAmount > 0) {
                        (outstandingAmount * 0.3).toLong() // Rough estimate
                    } else {
                        0L
                    }
                }
                else -> {
                    expectedRevenue = 0L
                    actualRevenue = 0L
                    outstandingAmount = 0L
                    overdueAmount = 0L
                }
            }
            
            // Color-coded metric cards
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
                is RevenueDashboardUiState.Success -> {
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

        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Invoice Status Overview",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    InvoiceStatusPieChart(
                        statusCounts = statusCounts,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item {
            NotesCard(
                currentNotesCount = currentNotesCount,
                modifier = Modifier.fillMaxWidth(),
                onClick = { navController.navigate(Screen.Notes) }
            )
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
