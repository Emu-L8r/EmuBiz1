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
 * Compact quick actions row with small icon buttons.
 */
@Composable
fun CompactQuickActions(
    onCreateInvoice: () -> Unit,
    onCreateCustomer: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CompactActionButton(
            icon = Icons.Default.Add,
            label = "Invoice",
            onClick = onCreateInvoice,
            modifier = Modifier.weight(1f)
        )
        CompactActionButton(
            icon = Icons.Default.People,
            label = "Customer",
            onClick = onCreateCustomer,
            modifier = Modifier.weight(1f)
        )
    }
}
