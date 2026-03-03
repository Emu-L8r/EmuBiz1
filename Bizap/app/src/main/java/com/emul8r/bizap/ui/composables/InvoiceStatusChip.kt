package com.emul8r.bizap.ui.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.emul8r.bizap.ui.theme.invoiceStatusDraft
import com.emul8r.bizap.ui.theme.invoiceStatusOverdue
import com.emul8r.bizap.ui.theme.invoiceStatusPaid
import com.emul8r.bizap.ui.theme.invoiceStatusSent

@Composable
fun InvoiceStatusChip(status: String) {
    val colorScheme = MaterialTheme.colorScheme
    val color = when (status) {
        "PAID" -> colorScheme.invoiceStatusPaid
        "SENT" -> colorScheme.invoiceStatusSent
        "DRAFT" -> colorScheme.invoiceStatusDraft
        else -> colorScheme.invoiceStatusOverdue
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, color)
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}
