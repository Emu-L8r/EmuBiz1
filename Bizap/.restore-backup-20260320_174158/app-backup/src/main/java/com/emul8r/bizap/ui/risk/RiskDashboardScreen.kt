package com.emul8r.bizap.ui.risk

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Error
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
import com.emul8r.bizap.ui.common.GradientBackgrounds.subtleVerticalGradient
import com.emul8r.bizap.ui.common.MetricCard
import com.emul8r.bizap.ui.theme.StatusColors
import com.emul8r.bizap.ui.theme.riskHigh
import com.emul8r.bizap.ui.theme.riskMedium
import com.emul8r.bizap.utils.CentsFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiskDashboardScreen(
    viewModel: RiskDashboardViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    headerSlot: (@Composable ColumnScope.() -> Unit)? = null,
) {
    val uiState = viewModel.uiState.collectAsState().value

    Scaffold(
        topBar = {}  // MainActivity provides the header
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
                    Text("Error: ${uiState.message}", color = MaterialTheme.colorScheme.error)
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
                        modifier = modifier
                            .fillMaxSize()
                            .subtleVerticalGradient()
                            .padding(paddingValues)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Column {
                                headerSlot?.invoke(this) ?: run {
                                    Text(
                                        text = "Risk Dashboard",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
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
    val totalAtRisk = riskInvoices.sumOf { it.outstandingAmount }
    val criticalCount = riskInvoices.count { it.daysOverdue > 60 }
    val mediumCount = riskInvoices.count { it.daysOverdue in 30..60 }
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Total at risk card with red/orange accent
        MetricCard(
            title = "Total at Risk",
            value = "$${String.format(Locale.getDefault(), "%.2f", totalAtRisk)}",
            icon = Icons.Default.Error,
            backgroundColor = StatusColors.Overdue.copy(alpha = 0.08f),
            borderColor = StatusColors.Overdue.copy(alpha = 0.3f),
            accentColor = StatusColors.Overdue,
            modifier = Modifier.fillMaxWidth()
        )
        
        // Critical and medium risk counts
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "Critical (60+ days)",
                value = "$criticalCount",
                icon = Icons.Default.Error,
                backgroundColor = StatusColors.Overdue.copy(alpha = 0.12f),
                borderColor = StatusColors.Overdue.copy(alpha = 0.4f),
                accentColor = StatusColors.Overdue,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "At Risk (30-59 days)",
                value = "$mediumCount",
                icon = Icons.Default.Warning,
                backgroundColor = StatusColors.Outstanding.copy(alpha = 0.12f),
                borderColor = StatusColors.Outstanding.copy(alpha = 0.4f),
                accentColor = StatusColors.Outstanding,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun RiskInvoiceCard(invoice: InvoicePaymentStatus) {
    val isHighRisk = invoice.daysOverdue > 60
    val accentColor = if (isHighRisk) StatusColors.Overdue else StatusColors.Outstanding
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = accentColor.copy(alpha = 0.08f)
        ),
        border = BorderStroke(2.dp, accentColor.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(12.dp),
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
                    Text(
                        invoice.invoiceNumber,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        invoice.customerName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    if (isHighRisk) Icons.Default.Error else Icons.Default.Warning,
                    "Risk Level",
                    tint = accentColor,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar showing overdue percentage (max 90 days = 100%)
            val progress = (invoice.daysOverdue.toFloat() / 90f).coerceIn(0f, 1f)
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = accentColor,
                trackColor = accentColor.copy(alpha = 0.2f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Outstanding",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "$${String.format(Locale.getDefault(), "%.2f", invoice.outstandingAmount)}",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = accentColor
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Days Overdue",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "${invoice.daysOverdue}",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = accentColor
                    )
                }
            }
        }
    }
}
