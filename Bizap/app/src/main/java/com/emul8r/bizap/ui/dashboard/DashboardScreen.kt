package com.emul8r.bizap.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.emul8r.bizap.ui.invoices.InvoiceList
import com.emul8r.bizap.ui.navigation.Screen
import com.emul8r.bizap.ui.revenue.RevenueDashboardViewModel
import com.emul8r.bizap.ui.revenue.RevenueDashboardUiState
import com.emul8r.bizap.ui.revenue.RevenueTrendChart
import com.emul8r.bizap.ui.revenue.RevenueSummaryCard
import com.emul8r.bizap.ui.revenue.CurrencyBreakdownCard
import com.emul8r.bizap.ui.settings.BusinessProfileViewModel
import com.emul8r.bizap.ui.settings.components.BusinessSwitcherDialog

@Composable
fun DashboardScreen(
    navController: NavController,
    customerViewModel: CustomerViewModel = hiltViewModel(),
    businessViewModel: BusinessProfileViewModel = hiltViewModel(),
    revenueViewModel: RevenueDashboardViewModel = hiltViewModel()
) {
    val customers by customerViewModel.uiState.collectAsStateWithLifecycle()
    val activeBusiness by businessViewModel.profileState.collectAsStateWithLifecycle()
    val revenueState by revenueViewModel.uiState.collectAsStateWithLifecycle()
    var showSwitcher by remember { mutableStateOf(false) }

    if (showSwitcher) {
        BusinessSwitcherDialog(onDismiss = { showSwitcher = false })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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

        // --- REVENUE ANALYTICS SECTION ---
        when (val state = revenueState) {
            is RevenueDashboardUiState.Success -> {
                val metrics = state.metrics
                
                // 1. High-Level Metrics
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ElevatedCard(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Icon(Icons.Default.People, contentDescription = null)
                            Text("Total Clients", style = MaterialTheme.typography.labelMedium)
                            Text("${customers.size}", style = MaterialTheme.typography.headlineMedium)
                        }
                    }
                    RevenueSummaryCard(label = "MTD", amountCents = metrics.mtdRevenue, modifier = Modifier.weight(1f))
                }

                // 2. Revenue Trend Chart
                Text(text = "Revenue Trend", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        if (metrics.dailyTrend.isNotEmpty()) {
                            RevenueTrendChart(dataPoints = metrics.dailyTrend)
                        } else {
                            Text(
                                "No trend data available",
                                modifier = Modifier.align(Alignment.Center).padding(vertical = 32.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // 3. Currency Breakdown (if multiple)
                if (metrics.topPerformers.isNotEmpty()) {
                    Text(text = "Revenue by Currency", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    metrics.topPerformers.forEach { performer ->
                        CurrencyBreakdownCard(performer)
                    }
                }
            }
            is RevenueDashboardUiState.Loading -> {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            else -> {
                // Fallback to basic cards if error or empty
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
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Icon(Icons.Default.AttachMoney, contentDescription = null)
                            Text("Revenue", style = MaterialTheme.typography.labelMedium)
                            Text("$0.00", style = MaterialTheme.typography.headlineMedium)
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Recent Invoices", style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = { navController.navigate(Screen.RevenueDashboard) }) {
                Text("View Detailed Report")
            }
        }

        // We wrap InvoiceList in a Box with a fixed height or similar if it's not scrollable itself
        // But InvoiceList usually uses a LazyColumn. Inside a verticalScroll Column, this is a problem.
        // We should either use a non-lazy version or just take a few items.
        // For now, let's keep it as is, but be aware of the "LazyColumn inside Scrollable Column" issue.
        // Actually, InvoiceList currently uses a LazyColumn internally.
        
        Box(modifier = Modifier.heightIn(max = 400.dp)) {
            InvoiceList(
                modifier = Modifier.fillMaxWidth(),
                onInvoiceClick = { invoiceId ->
                    navController.navigate(Screen.InvoiceDetail(invoiceId))
                }
            )
        }
    }
}
