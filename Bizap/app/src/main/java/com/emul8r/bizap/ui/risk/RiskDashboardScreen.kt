package com.emul8r.bizap.ui.risk

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.emul8r.bizap.domain.invoice.model.InvoicePaymentStatus
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiskDashboardScreen(
    viewModel: RiskDashboardViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val uiState = viewModel.uiState.collectAsState().value

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Risk Dashboard") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshRiskInvoices() }) {
                        Icon(Icons.Default.Refresh, "Refresh")
                    }
                }
            )
        }
    ) { paddingValues ->
        when (uiState) {
            is RiskUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is RiskUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Error: ${uiState.message}", color = Color.Red)
                }
            }
            is RiskUiState.Success -> {
                if (uiState.riskInvoices.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No invoices at risk")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            RiskSummaryCard(uiState.riskInvoices)
                        }
                        items(uiState.riskInvoices) { invoice ->
                            RiskInvoiceCard(invoice)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RiskSummaryCard(riskInvoices: List<InvoicePaymentStatus>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Risk Summary", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(12.dp))

            val totalAtRisk = riskInvoices.sumOf { it.outstandingAmount }
            val criticalCount = riskInvoices.count { it.daysOverdue > 60 }
            val mediumCount = riskInvoices.count { it.daysOverdue in 30..60 }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Total at Risk", fontSize = 12.sp, color = Color.Gray)
                    Text("$${String.format(Locale.getDefault(), "%.2f", totalAtRisk)}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Column {
                    Text("Critical", fontSize = 12.sp, color = Color.Gray)
                    Text("$criticalCount", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFE53935))
                }
                Column {
                    Text("Medium", fontSize = 12.sp, color = Color.Gray)
                    Text("$mediumCount", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFFF9800))
                }
            }
        }
    }
}

@Composable
fun RiskInvoiceCard(invoice: InvoicePaymentStatus) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(invoice.invoiceNumber, fontWeight = FontWeight.Bold)
                    Text(invoice.customerName, fontSize = 12.sp, color = Color.Gray)
                }
                Icon(
                    Icons.Default.Warning,
                    "Risk",
                    tint = if (invoice.daysOverdue > 60) Color(0xFFE53935) else Color(0xFFFF9800)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Outstanding", fontSize = 12.sp, color = Color.Gray)
                    Text("$${String.format(Locale.getDefault(), "%.2f", invoice.outstandingAmount)}", fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("Days Overdue", fontSize = 12.sp, color = Color.Gray)
                    Text("${invoice.daysOverdue}", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

