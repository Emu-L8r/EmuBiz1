package com.emul8r.bizap.ui.shared

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.emul8r.bizap.ui.landing.GuiMode

/**
 * Reusable invoice header component.
 * Works for both GUI1 and GUI2 with styling differences.
 *
 * @param invoiceNumber The invoice number to display
 * @param date The invoice date string
 * @param dueDate The invoice due date string
 * @param guiMode Controls styling — GUI1 uses surfaceVariant, GUI2 uses default card colors
 * @param modifier Optional modifier
 */
@Composable
fun InvoiceHeaderCard(
    invoiceNumber: String,
    date: String,
    dueDate: String,
    guiMode: GuiMode = GuiMode.GUI2,
    modifier: Modifier = Modifier
) {
    val cardColors = when (guiMode) {
        GuiMode.GUI1 -> CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
        GuiMode.GUI2 -> CardDefaults.cardColors()
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = cardColors,
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (guiMode == GuiMode.GUI2) 2.dp else 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = invoiceNumber,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Date",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = date,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Due Date",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = dueDate,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

/**
 * Reusable loading state composable.
 * Consistent loading UI across both GUIs.
 */
@Composable
fun SharedLoadingScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

/**
 * Reusable error state composable.
 * Consistent error UI across both GUIs.
 *
 * @param message The error message to display
 * @param onRetry Optional retry callback — shows a button if provided
 */
@Composable
fun SharedErrorScreen(
    message: String,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
        if (onRetry != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

/**
 * Reusable metric summary row.
 * Used in invoice detail / customer detail screens for both GUIs.
 *
 * @param label The metric label
 * @param value The formatted metric value
 * @param guiMode Controls text weight — GUI2 uses SemiBold
 */
@Composable
fun MetricRow(
    label: String,
    value: String,
    guiMode: GuiMode = GuiMode.GUI2,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (guiMode == GuiMode.GUI2) FontWeight.SemiBold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
