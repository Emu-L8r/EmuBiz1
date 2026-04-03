package com.emul8r.bizap.ui.gui2.common

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Reusable empty state screen for when no data is available.
 *
 * Displays a centered icon, title, message, and optional action button.
 * Used across multiple screens (invoices, customers, payments, etc.)
 *
 * Usage:
 * ```kotlin
 * if (invoices.isEmpty()) {
 *     EmptyStateScreen(
 *         icon = Icons.Default.Description,
 *         title = "No invoices yet",
 *         message = "Create your first invoice to get started",
 *         actionLabel = "Create Invoice",
 *         onAction = { navigateToCreate() }
 *     )
 * } else {
 *     InvoiceList(invoices)
 * }
 * ```
 */
@Composable
fun EmptyStateScreen(
    icon: ImageVector = Icons.Default.Description,
    title: String = "No data yet",
    message: String = "Create your first item to get started",
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Icon (large, subtle color)
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.surfaceVariant
        )

        // Title
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        // Message
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        // Action Button (optional)
        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onAction,
                modifier = Modifier.height(40.dp)
            ) {
                Text(actionLabel)
            }
        }
    }
}

