package com.emul8r.bizap.ui.auth

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
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
import kotlin.math.roundToInt

private val AmberBrand = Color(0xFFF59E0B)
private val PrimaryBrand = Color(0xFF5B3BA0)

/**
 * Login screen shown when the app has a saved PIN but no active session.
 *
 * Features:
 * - Business icon box (80dp) with logo or Material icon placeholder
 * - Business name and "Unlock to continue" subtitle
 * - Lock icon (120dp, purple)
 * - Masked PIN input (numeric keyboard)
 * - Amber "Unlock" button
 * - Attempt counter and error messages
 * - Lockout countdown (30 s after 5 failed attempts)
 * - "Forgot PIN?" with warning dialog → wipes all data → PINSetupScreen
 * - Slide-up entrance animation (600ms)
 *
 * @param onAuthenticated Called after a successful PIN entry.
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

    // Slide-up entrance animation
    val density = LocalDensity.current
    var animationStarted by remember { mutableStateOf(false) }
    val slideOffset by animateFloatAsState(
        targetValue = if (animationStarted) 0f else 1f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "slideUp"
    )

    LaunchedEffect(Unit) {
        animationStarted = true
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = dimensionResource(R.dimen.pin_horizontal_padding))
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .offset {
                    val screenHeightPx = with(density) { 800.dp.toPx() }
                    IntOffset(x = 0, y = (screenHeightPx * slideOffset).roundToInt())
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Business icon box
            val displayName = businessProfile.businessName.ifBlank { "My Business" }
            val hasLogo = !businessProfile.logoBase64.isNullOrBlank()

            Card(
                modifier = Modifier.size(dimensionResource(R.dimen.pin_business_icon_size)),
                shape = RoundedCornerShape(dimensionResource(R.dimen.pin_business_icon_radius)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (hasLogo) {
                        AsyncImage(
                            model = businessProfile.logoBase64,
                            contentDescription = "Business Logo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(dimensionResource(R.dimen.pin_business_icon_radius)))
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = "Business",
                            modifier = Modifier.size(40.dp),
                            tint = PrimaryBrand
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Business name
            Text(
                text = displayName,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Subtitle
            Text(
                text = "Unlock to continue",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF6B7280),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.pin_vertical_spacing)))

            // Lock icon
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(dimensionResource(R.dimen.pin_lock_icon_size)),
                tint = PrimaryBrand
            )

            Spacer(modifier = Modifier.height(16.dp))

            // "Enter your PIN" title
            Text(
                text = "Enter your PIN",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.pin_vertical_spacing)))

            // PIN input field
            OutlinedTextField(
                value = uiState.pin,
                onValueChange = { viewModel.onPINChanged(it) },
                label = { Text("PIN") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimensionResource(R.dimen.pin_input_height)),
                enabled = uiState.lockoutSecondsRemaining == 0L && !uiState.isLoading,
                isError = uiState.errorMessage != null,
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center)
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

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.pin_vertical_spacing)))

            // Amber "Unlock" button
            Button(
                onClick = { viewModel.onLoginClicked() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimensionResource(R.dimen.pin_button_height)),
                shape = RoundedCornerShape(dimensionResource(R.dimen.pin_button_radius)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AmberBrand,
                    contentColor = Color.White
                ),
                enabled = uiState.pin.isNotEmpty()
                    && uiState.lockoutSecondsRemaining == 0L
                    && !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Text(
                        text = "Unlock",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.pin_spacing_small)))

            // "Forgot PIN?" link
            TextButton(onClick = { viewModel.onForgotPINClicked() }) {
                Text(
                    text = "Forgot PIN?",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
