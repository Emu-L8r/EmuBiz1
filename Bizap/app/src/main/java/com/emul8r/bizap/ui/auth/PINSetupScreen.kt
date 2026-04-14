package com.emul8r.bizap.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emul8r.bizap.R
import com.emul8r.bizap.ui.auth.components.AnimatedLockIcon
import com.emul8r.bizap.ui.auth.components.BrandedBackgroundWrapper
import com.emul8r.bizap.ui.auth.components.EnhancedUnlockButton
import com.emul8r.bizap.ui.auth.components.PINDotIndicator
import com.emul8r.bizap.ui.auth.components.PINFormCard

/**
 * First-launch screen where the user creates their PIN.
 * Includes enhanced design with logo, animations, and visual indicators.
 *
 * @param onSetupComplete Called after the PIN is saved and the session starts.
 */
@Composable
fun PINSetupScreen(
    onSetupComplete: () -> Unit,
    viewModel: PINSetupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isComplete) {
        if (uiState.isComplete) onSetupComplete()
    }

    BrandedBackgroundWrapper {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .windowInsetsPadding(WindowInsets.safeDrawing),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Logo with gradient background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                                MaterialTheme.colorScheme.background
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.company_logo),
                    contentDescription = "Bizap Logo",
                    modifier = Modifier
                        .size(80.dp)
                        .shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Fit
                )
            }

            // Animated lock icon
            AnimatedLockIcon(
                size = 60.dp,
                numPulses = 2
            )

            // PIN form card
            PINFormCard {
                // Title
                Text(
                    text = "Create your PIN",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )

                // Subtitle
                Text(
                    text = "Choose a PIN (4+ digits) to protect your business data.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                // First PIN input
                OutlinedTextField(
                    value = uiState.pin,
                    onValueChange = { viewModel.onPINChanged(it) },
                    label = { Text("PIN") },
                    placeholder = { Text("Enter 4+ digits") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = uiState.errorMessage != null
                )

                // PIN dot indicator for first PIN
                PINDotIndicator(
                    pinLength = uiState.pin.length,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Confirm PIN input
                OutlinedTextField(
                    value = uiState.confirmPin,
                    onValueChange = { viewModel.onConfirmPINChanged(it) },
                    label = { Text("Confirm PIN") },
                    placeholder = { Text("Re-enter your PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = uiState.errorMessage != null
                )

                // PIN dot indicator for confirm PIN
                PINDotIndicator(
                    pinLength = uiState.confirmPin.length,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Error message
                if (uiState.errorMessage != null) {
                    Text(
                        text = uiState.errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }

                // Set PIN button - ALWAYS VISIBLE
                EnhancedUnlockButton(
                    onClick = { viewModel.onSetupClicked() },
                    isLoading = uiState.isLoading,
                    isEnabled = uiState.pin.length >= 4 && uiState.pin == uiState.confirmPin,
                    label = "Set PIN"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
