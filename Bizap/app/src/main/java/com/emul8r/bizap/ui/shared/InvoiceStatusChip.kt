package com.emul8r.bizap.ui.shared

import androidx.compose.material3.AssistChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun InvoiceStatusChip(status: String) {
    AssistChip(
        onClick = { /*TODO*/ },
        label = { Text(status) }
    )
}
