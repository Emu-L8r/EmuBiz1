package com.example.databaser.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.databaser.data.PredefinedLineItem
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ServiceItem(service: PredefinedLineItem, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(service.job) },
        supportingContent = { Text(service.details) },
        trailingContent = {
            Text(
                text = "$${String.format(Locale.US, "%.2f", service.unitPrice)}",
                style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
            )
        },
        modifier = Modifier.combinedClickable(onClick = onClick)
    )
}
