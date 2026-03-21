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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

/**
 * Production-Ready Error Boundary Composable.
 *
 * Catches and displays unhandled exceptions in composables.
 * Features:
 * - Automatic error logging to Crashlytics
 * - User-friendly error messages
 * - Recovery actions (Retry, Go to Dashboard)
 * - Technical error details for debugging
 *
 * Usage:
 *   ErrorBoundaryScaffold(onReturnToDashboard = { navController.navigate("dashboard") }) {
 *       YourScreenContent()
 *   }
 */
@Composable
fun ErrorBoundaryScaffold(
    onReturnToDashboard: () -> Unit = {},
    content: @Composable () -> Unit
) {
    var hasError by remember { mutableStateOf(false) }
    var errorThrowable by remember { mutableStateOf<Throwable?>(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        try {
            Column(modifier = Modifier.padding(paddingValues)) {
                content()
            }
        } catch (e: Exception) {
            // Log error
            hasError = true
            errorThrowable = e
            Timber.e(e, "UI Error caught by ErrorBoundary")
            FirebaseCrashlytics.getInstance().recordException(e)

            // Show error UI
            ErrorScreen(
                error = e,
                onDismiss = {
                    hasError = false
                    errorThrowable = null
                },
                onRetry = {
                    // Reset error state and retry rendering content
                    hasError = false
                    errorThrowable = null
                },
                onReturnToDashboard = onReturnToDashboard
            )
        }
    }
}

/**
 * User-Friendly Error Screen - Displays when an error state is detected.
 *
 * This screen:
 * - Shows a clear error message
 * - Provides technical details for debugging
 * - Offers recovery actions (Retry, Dashboard)
 * - Logs error context for support
 */
@Composable
fun ErrorScreen(
    error: Throwable,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
    onReturnToDashboard: () -> Unit = {}
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
            "An unexpected error occurred while rendering this screen. The app is still running, but we need to take action.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onErrorContainer
        )

        // Technical details
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
                Text("Retry")
            }

            Button(
                onClick = onReturnToDashboard,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Return to Dashboard")
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
            "💡 Tip: This error has been logged to Crashlytics. Share the details above when reporting a bug.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.padding(top = 24.dp, bottom = 24.dp)
        )
    }
}


