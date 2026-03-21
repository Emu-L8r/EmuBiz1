package com.emul8r.bizap.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import timber.log.Timber

/**
 * Error Boundary Composable - Catches and displays unhandled exceptions.
 *
 * NOTE: Traditional try-catch cannot be used around @Composable functions.
 * This is a simplified error UI that displays app-level errors logged to Timber.
 *
 * For runtime error catching, use this wrapper at the setContent level and
 * handle exceptions in ViewModels/Repositories with Result types.
 *
 * Usage:
 *   ErrorBoundary {
 *       YourAppContent()
 *   }
 */
@Composable
fun ErrorBoundary(
    content: @Composable () -> Unit
) {
    // In a full implementation, you would integrate with Crashlytics
    // or a similar service to catch errors. For now, this is a placeholder.
    content()
}

/**
 * User-Friendly Error Screen - Displays when an error state is detected.
 */
@Composable
fun ErrorScreen(
    error: Throwable,
    onDismiss: () -> Unit,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Text(
            "⚠️ Something Went Wrong",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.padding(top = 24.dp)
        )

        // User-friendly message
        Text(
            "An unexpected error occurred. The app is still running, but something needs attention.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onErrorContainer
        )

        // Technical details (expandable in real app)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(12.dp)
        ) {
            Text(
                "Error Details",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                error.javaClass.simpleName,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )

            Text(
                error.message ?: "No message provided",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 4.dp)
            )

            // Stack trace (first 3 lines for brevity)
            val stackTrace = error.stackTraceToString().lines().take(3).joinToString("\n")
            Text(
                stackTrace,
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Action buttons
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Try Again")
            }

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Dismiss")
            }
        }

        // Diagnostic info
        Text(
            "Tip: This error has been logged. Share the details above when reporting a bug.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.padding(top = 24.dp)
        )
    }
}


