package com.emul8r.bizap.ui.gui2.invoice

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emul8r.bizap.data.local.entities.InvoiceWithItems
import com.emul8r.bizap.ui.gui2.common.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * GUI2 invoice detail screen.
 * Displays a read-only view of a single invoice's details and line items.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceDetailScreenV2(
    businessId: Long,
    invoiceId: Long,
    onBack: () -> Unit,
    viewModel: InvoiceDetailViewModelV2 = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Invoice Detail") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is InvoiceDetailUiStateV2.Loading -> LoadingIndicatorV2(
                modifier = Modifier.padding(paddingValues)
            )
            is InvoiceDetailUiStateV2.NotFound -> ErrorStateV2(
                message = "Invoice #$invoiceId not found.",
                modifier = Modifier.padding(paddingValues)
            )
            is InvoiceDetailUiStateV2.Error -> ErrorStateV2(
                message = state.message,
                modifier = Modifier.padding(paddingValues)
            )
            is InvoiceDetailUiStateV2.Success -> InvoiceDetailContentV2(
                invoice = state.invoice,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
private fun InvoiceDetailContentV2(
    invoice: InvoiceWithItems,
    modifier: Modifier = Modifier
) {
    val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val entity = invoice.invoice

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionHeaderV2("Invoice Info")

        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                DetailRowV2("Customer", entity.customerName)
                DetailRowV2("Status", entity.status)
                DetailRowV2("Date", dateFormatter.format(Date(entity.date)))
                if (entity.dueDate > 0) {
                    DetailRowV2("Due Date", dateFormatter.format(Date(entity.dueDate)))
                }
                DetailRowV2("Total", formatCents(entity.totalAmount))
                DetailRowV2("Amount Paid", formatCents(entity.amountPaid))
                DetailRowV2("Outstanding", formatCents(entity.totalAmount - entity.amountPaid))
                DetailRowV2("Currency", entity.currencyCode)
            }
        }

        if (invoice.items.isNotEmpty()) {
            HorizontalDivider()
            SectionHeaderV2("Line Items")
            invoice.items.forEach { item ->
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.description,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Qty: ${item.quantity}  ×  ${formatCents(item.unitPrice.toLong())}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = formatCents((item.unitPrice * item.quantity).toLong()),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        entity.notes?.let { notes ->
            if (notes.isNotBlank()) {
                HorizontalDivider()
                SectionHeaderV2("Notes")
                Text(notes, style = MaterialTheme.typography.bodyMedium)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun DetailRowV2(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}
