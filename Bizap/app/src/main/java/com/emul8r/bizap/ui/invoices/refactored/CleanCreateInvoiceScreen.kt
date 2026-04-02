package com.emul8r.bizap.ui.invoices.refactored

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emul8r.bizap.ui.gui2.invoices.CreateInvoiceViewModelV2

/**
 * Clean, Refactored CreateInvoiceScreen
 *
 * Focused purely on invoice data entry:
 * - Customer selection
 * - Invoice dates
 * - Line items
 * - Quick calculations
 *
 * All customization has been moved to InvoiceSettingsScreen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CleanCreateInvoiceScreen(
    viewModel: CreateInvoiceViewModelV2 = hiltViewModel(),
    onBack: () -> Unit = {},
    onInvoiceSaved: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Invoice") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Client Information
            item {
                ClientInfoSection()
            }

            // Section 2: Invoice Details
            item {
                InvoiceDetailsSection()
            }

            // Section 3: Line Items
            item {
                LineItemsSection()
            }

            // Section 4: Summary
            item {
                SummarySection()
            }

            // Section 5: Action Buttons
            item {
                ActionButtonsSection(
                    onSave = { /* Save invoice */ },
                    onCancel = onBack
                )
            }
        }
    }
}

@Composable
fun ClientInfoSection() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Client Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Client selection dropdown
            OutlinedTextField(
                value = "Select Client",
                onValueChange = {},
                label = { Text("Client") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true
            )
        }
    }
}

@Composable
fun InvoiceDetailsSection() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Invoice Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = "2026-03-30",
                    onValueChange = {},
                    label = { Text("Invoice Date") },
                    modifier = Modifier.weight(1f),
                    readOnly = true
                )

                OutlinedTextField(
                    value = "2026-04-30",
                    onValueChange = {},
                    label = { Text("Due Date") },
                    modifier = Modifier.weight(1f),
                    readOnly = true
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = "Additional notes here",
                onValueChange = {},
                label = { Text("Notes (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
        }
    }
}

@Composable
fun LineItemsSection() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Line Items",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = { /* Add item */ }) {
                    Icon(Icons.Default.Add, "Add Item")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sample line item
            LineItemRow(
                description = "Web Design Services",
                quantity = "10",
                price = "$100.00",
                total = "$1,000.00",
                onDelete = {}
            )
        }
    }
}

@Composable
fun LineItemRow(
    description: String,
    quantity: String,
    price: String,
    total: String,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(description, style = MaterialTheme.typography.bodyMedium)
            Text("Qty: $quantity × $price", style = MaterialTheme.typography.labelSmall)
        }

        Text(total, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)

        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Delete, "Delete", modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun SummarySection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Subtotal")
                Text("$1,000.00", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Tax (10%)")
                Text("$100.00", fontWeight = FontWeight.Bold)
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total Due", fontWeight = FontWeight.Bold)
                Text("$1,100.00", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}

@Composable
fun ActionButtonsSection(
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onSave,
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
        ) {
            Text("Create Invoice")
        }

        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
        ) {
            Text("Cancel")
        }
    }
}

