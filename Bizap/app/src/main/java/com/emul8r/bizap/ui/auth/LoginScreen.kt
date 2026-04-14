package com.emul8r.bizap.ui.auth

import android.net.Uri
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.emul8r.bizap.R
import com.emul8r.bizap.ui.designsystem.BizapColors
import com.emul8r.bizap.ui.auth.components.BrandedBackgroundWrapper
import com.emul8r.bizap.ui.auth.components.DualLogoHeaderWithGradient
import com.emul8r.bizap.ui.auth.components.AnimatedLockIcon
import com.emul8r.bizap.ui.auth.components.PINInputSection
import java.io.File
import kotlin.math.roundToInt

private val AmberBrand = BizapColors.AnalyticsWarning  // Material Amber
private val PrimaryBrand = BizapColors.Presets.Purple  // Material Purple

/**
 * Login screen Composable for PIN authentication.
 *
 * **Purpose:**
 * Displayed when user has set up a PIN but doesn't have an active session.
 * Allows user to enter PIN to unlock app and access business data.
 *
 * **Features:**
 * - Business branding (logo + name)
 * - Large lock icon (visual security indicator)
 * - PIN input field (masked, numeric keyboard)
 * - Amber unlock button
 * - Error message display
 * - Attempt counter
 * - Lockout countdown (after 3 failed attempts = 60s lockout)
 * - "Forgot PIN?" flow (warning dialog → reset → PINSetupScreen)
 *
 * **Layout:**
 * ```
 * ┌─────────────────────┐
 * │  Business Logo      │  (80dp box)
 * │  "Business Name"    │
 * │  "Unlock to continue" │
 * │                     │
 * │  [Lock Icon]        │  (120dp)
 * │                     │
 * │  [PIN Input]        │  (masked dots)
 * │                     │
 * │  [UNLOCK Button]    │  (amber)
 * │                     │
 * │  Attempts: 1/3      │
 * │  "Forgot PIN?"      │
 * └─────────────────────┘
 * ```
 *
 * **State Management:**
 * - Observes [LoginViewModel.uiState]
 * - Updates on PIN entry, lockout, authentication
 * - Auto-refreshes on date change (midnight)
 *
 * **Navigation:**
 * - On success: Navigate to main app
 * - On "Forgot PIN?": Show warning dialog → reset app data → PINSetupScreen
 *
 * **Animations:**
 * - Lock icon scales on error (visual feedback)
 * - Pin entry feedback (haptic + visual)
 *
 * **Usage:**
 * ```kotlin
 * @Composable
 * fun AuthNavigation() {
 *     val authState by viewModel.authState.collectAsStateWithLifecycle()
 *
 *     when (authState) {
 *         AuthState.RequiresLogin -> LoginScreen()
 *         // ...
 *     }
 * }
 * ```
 *
 * @see LoginViewModel
 * @see PINSetupScreen
 */
@Composable
fun LoginScreen(
    onAuthenticated: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val businessProfile by viewModel.businessProfile.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) onAuthenticated()
    }

    // Forgot PIN confirmation dialog
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

    BrandedBackgroundWrapper(
        backgroundImageRes = null,  // Optional: R.drawable.thswalogo
        backgroundAlpha = 0.05f
    ) {
        // Slide-up entrance animation
        var animationStarted by remember { mutableStateOf(false) }
        val slideOffset by animateFloatAsState(
            targetValue = if (animationStarted) 0f else 1f,
            animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
            label = "slideUp"
        )

        LaunchedEffect(Unit) {
            animationStarted = true
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = dimensionResource(R.dimen.pin_horizontal_padding))
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .offset {
                    IntOffset(x = 0, y = (300f * slideOffset).roundToInt())
                }
                .verticalScroll(rememberScrollState()), // Enable vertical scrolling
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top) // Changed from Center to spacedBy with top alignment
        ) {
            val displayName = businessProfile.businessName.ifBlank { "My Business" }
            val logoPath = businessProfile.logoBase64

            // NEW: Dual logo header with gradient (Improvements #1 & #2)
            DualLogoHeaderWithGradient(
                bizapLogoRes = R.drawable.company_logo,
                businessLogoUri = logoPath,
                businessName = displayName,
                subtitle = stringResource(R.string.label_unlock_to_continue)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // NEW: Animated lock icon (Improvement #3)
            AnimatedLockIcon(
                size = 140.dp,
                tint = MaterialTheme.colorScheme.primary,
                numPulses = 3
            )

            Spacer(modifier = Modifier.height(32.dp))

            // NEW: PIN input section with all improvements (Improvements #4, #5, #6)
            PINInputSection(
                pinValue = uiState.pin,
                onPINChanged = { viewModel.onPINChanged(it) },
                onLoginClicked = { viewModel.onLoginClicked() },
                isLoading = uiState.isLoading,
                isEnabled = uiState.lockoutSecondsRemaining == 0L,
                errorMessage = uiState.errorMessage,
                attemptCount = uiState.attemptCount,
                lockoutSecondsRemaining = uiState.lockoutSecondsRemaining,
                showForgotPIN = true,
                onForgotPINClick = { viewModel.onForgotPINDismissed() }
            )
        }
    }
}
