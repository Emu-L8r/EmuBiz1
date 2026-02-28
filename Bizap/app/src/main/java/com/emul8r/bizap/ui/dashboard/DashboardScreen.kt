package com.emul8r.bizap.ui.dashboard

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
import com.emul8r.bizap.ui.invoices.InvoiceList
import com.emul8r.bizap.ui.navigation.Screen
import com.emul8r.bizap.ui.settings.BusinessProfileViewModel
import com.emul8r.bizap.ui.settings.components.BusinessSwitcherDialog

@Composable
fun DashboardScreen(
    navController: NavController,
    customerViewModel: CustomerViewModel = hiltViewModel(),
    businessViewModel: BusinessProfileViewModel = hiltViewModel()
) {
    val customers by customerViewModel.uiState.collectAsStateWithLifecycle()
    val activeBusiness by businessViewModel.profileState.collectAsStateWithLifecycle()
    var showSwitcher by remember { mutableStateOf(false) }

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
