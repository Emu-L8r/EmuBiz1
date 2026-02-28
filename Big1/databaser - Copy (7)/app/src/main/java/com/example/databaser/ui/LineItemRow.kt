package com.example.databaser.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.example.databaser.data.LineItem
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LineItemRow(lineItem: LineItem, onLongClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(lineItem.job, fontWeight = FontWeight.Bold) },
        supportingContent = { Text(lineItem.details) },
        trailingContent = {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$${String.format(Locale.US, "%.2f", lineItem.quantity * lineItem.unitPrice)}",
                    style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${lineItem.quantity} x $${String.format(Locale.US, "%.2f", lineItem.unitPrice)}",
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                )
            }
        },
        modifier = Modifier.combinedClickable(onClick = {}, onLongClick = onLongClick)
    )
}
