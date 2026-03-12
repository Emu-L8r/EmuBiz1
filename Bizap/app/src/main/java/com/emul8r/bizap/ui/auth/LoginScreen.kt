package com.emul8r.bizap.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Login screen shown when the app has a saved PIN but no active session.
 *
 * Features:
 * - Masked PIN input (numeric keyboard)
 * - Attempt counter and error messages
 * - Lockout countdown (30 s after 5 failed attempts)
 * - "Forgot PIN?" with warning dialog → wipes all data → PINSetupScreen
 *
 * @param onAuthenticated Called after a successful PIN entry.
 */
@Composable
fun LoginScreen(
    onAuthenticated: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) onAuthenticated()
    }

    if (uiState.showForgotPINDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onForgotPINDismissed() },
            title = { Text("Clear all data?") },
            text = {
                Text(
                    "This will permanently delete ALL app data including invoices, customers, " +
                        "and payments. This cannot be undone."
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.onForgotPINConfirmed() }) {
                    Text("Clear All", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onForgotPINDismissed() }) {
                    Text("Cancel")
                }
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
                .windowInsetsPadding(WindowInsets.safeDrawing),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Enter your PIN",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

            OutlinedTextField(
                value = uiState.pin,
                onValueChange = { viewModel.onPINChanged(it) },
                label = { Text("PIN") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.lockoutSecondsRemaining == 0L && !uiState.isLoading,
                isError = uiState.errorMessage != null
            )

            if (uiState.errorMessage != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = uiState.errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { viewModel.onLoginClicked() },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.pin.isNotEmpty()
                    && uiState.lockoutSecondsRemaining == 0L
                    && !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Unlock")
                }
            }

            Spacer(Modifier.height(16.dp))

            TextButton(onClick = { viewModel.onForgotPINClicked() }) {
                Text("Forgot PIN?")
            }
        }
    }
}
