package com.emul8r.bizap.ui.gui2.dashboard.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Modern (spacious) quick actions row with large buttons.
 */
@Composable
fun ModernQuickActions(
    onCreateInvoice: () -> Unit,
    onCreateCustomer: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onCreateInvoice,
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("New Invoice")
        }
        OutlinedButton(
            onClick = onCreateCustomer,
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Default.People, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("New Customer")
        }
    }
}
