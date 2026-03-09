package com.emul8r.bizap.ui.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
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

    Column(modifier = Modifier.padding(16.dp)) {
        // --- PHASE 3B: GLOBAL IDENTITY HEADER ---
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

        Spacer(modifier = Modifier.height(24.dp))

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
                    Icon(Icons.Default.AttachMoney, contentDescription = null)
                    Text("Revenue", style = MaterialTheme.typography.labelMedium)
                    val mtdText = when (val s = revenueState) {
                        is RevenueDashboardUiState.Success -> CentsFormatter.formatCents(s.metrics.totalPaidRevenue)
                        else -> "$0.00"
                    }
                    Text(mtdText, style = MaterialTheme.typography.headlineMedium)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- INVOICE STATUS PIE CHART ---
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

        Spacer(modifier = Modifier.height(24.dp))

        // Notes Card
        NotesCard(
            currentNotesCount = currentNotesCount,
            modifier = Modifier.fillMaxWidth(),
            onClick = { navController.navigate(Screen.Notes) }
        )

        Spacer(modifier = Modifier.height(24.dp))
        
        Text("Recent Invoices", style = MaterialTheme.typography.titleMedium)

        InvoiceList(
            modifier = Modifier.fillMaxWidth(),
            onInvoiceClick = { invoiceId ->
                navController.navigate(Screen.InvoiceDetail(invoiceId))
            }
        )
    }
}
