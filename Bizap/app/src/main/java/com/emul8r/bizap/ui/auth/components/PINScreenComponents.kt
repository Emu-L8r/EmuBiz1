package com.emul8r.bizap.ui.auth.components
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import java.io.File
import android.net.Uri
import com.emul8r.bizap.R
/**
 * PIN Screen Components Library
 *
 * Unified component system for authentication screens
 * Implements all 7 design improvements:
 * #1: Dual logo header
 * #2: Gradient background
 * #3: Animated lock icon
 * #4: PIN dot indicator
 * #5: Card-based layout
 * #6: Enhanced button
 * #7: Background watermark
 */
// ============================================================================
// IMPROVEMENT #1 & #2: DUAL LOGO HEADER WITH GRADIENT BACKGROUND
// ============================================================================
/**
 * Two-tier logo display (Bizap + Business) with gradient backdrop
 * Combines improvements #1 (logos) + #2 (gradient) into one component
 *
 * @param bizapLogoRes Drawable resource for Bizap logo
 * @param businessLogoUri Optional business logo URI
 * @param businessName Business name to display
 * @param subtitle Subtitle text under business name
 * @param modifier Additional modifiers
 */
@Composable
fun DualLogoHeaderWithGradient(
    bizapLogoRes: Int,
    businessLogoUri: String? = null,
    businessName: String = "My Business",
    subtitle: String = "Unlock to continue",
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        // IMPROVEMENT #2: Gradient background layer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.08f),
                            MaterialTheme.colorScheme.background
                        ),
                        startY = 0f,
                        endY = 350.dp.value
                    )
                )
        )
        // Content overlaid on gradient
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // IMPROVEMENT #1: Bizap app logo (top)
            androidx.compose.foundation.Image(
                painter = painterResource(bizapLogoRes),
                contentDescription = "Bizap Logo",
                modifier = Modifier
                    .size(80.dp)
                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Fit
            )
            // Elegant separator divider
            HorizontalDivider(
                modifier = Modifier
                    .width(100.dp)
                    .height(2.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            )
            // IMPROVEMENT #1: Business logo (below Bizap logo)
            Card(
                modifier = Modifier.size(100.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (!businessLogoUri.isNullOrBlank() && File(businessLogoUri).exists()) {
                        AsyncImage(
                            model = Uri.fromFile(File(businessLogoUri)),
                            contentDescription = "Business Logo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(16.dp))
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = "Business",
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            // Business name and subtitle
            Text(
                text = businessName,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
// ============================================================================
// IMPROVEMENT #3: ANIMATED LOCK ICON
// ============================================================================
/**
 * Animated pulse lock icon with shadow
 * Improvement #3: Makes security icon more prominent and interactive
 *
 * @param modifier Additional modifiers
 * @param size Icon size
 * @param tint Icon color
 * @param pulseOnMount Whether to pulse animation on mount
 * @param numPulses Number of pulses to perform
 */
@Composable
fun AnimatedLockIcon(
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 140.dp,
    tint: Color = MaterialTheme.colorScheme.primary,
    pulseOnMount: Boolean = true,
    numPulses: Int = 3
) {
    var isPulsing by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPulsing) 1.15f else 1.0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 200f),
        label = "lockIconPulse"
    )
    LaunchedEffect(Unit) {
        if (pulseOnMount) {
            repeat(numPulses) {
                delay(500)
                isPulsing = !isPulsing
                delay(500)
                isPulsing = !isPulsing
            }
        }
    }
    Icon(
        imageVector = Icons.Default.Lock,
        contentDescription = "Security Lock",
        modifier = modifier
            .size(size)
            .scale(scale)
            .shadow(elevation = 8.dp),
        tint = tint
    )
}
// ============================================================================
// IMPROVEMENT #4: PIN DOT INDICATOR
// ============================================================================
/**
 * Visual PIN dot indicator (like iOS passcode screen)
 * Improvement #4: Better security indication + visual feedback
 *
 * @param pinLength Current PIN length
 * @param maxLength Maximum PIN length
 * @param modifier Additional modifiers
 */
@Composable
fun PINDotIndicator(
    pinLength: Int,
    maxLength: Int = 6,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(maxLength) { index ->
            val isFilled = index < pinLength
            val scale by animateFloatAsState(
                targetValue = if (isFilled) 1.2f else 0.8f,
                animationSpec = spring(dampingRatio = 0.7f, stiffness = 150f),
                label = "dotScale_"
            )
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .scale(scale)
                    .background(
                        color = if (isFilled)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.outlineVariant,
                        shape = CircleShape
                    )
                    .shadow(elevation = if (isFilled) 4.dp else 0.dp, shape = CircleShape)
            )
        }
    }
}
// ============================================================================
// IMPROVEMENT #5: PIN FORM CARD WRAPPER
// ============================================================================
/**
 * Elevated card container for PIN form
 * Improvement #5: Card-based layout with elevation for visual hierarchy
 *
 * @param modifier Additional modifiers
 * @param content Composable content inside card
 */
@Composable
fun PINFormCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(modifier = modifier.fillMaxWidth(0.9f)) {
        // Logo watermark background
        androidx.compose.foundation.Image(
            painter = painterResource(R.drawable.company_logo),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.Center)
                .size(180.dp)
                .alpha(0.06f),  // Very faint watermark
            contentScale = ContentScale.Fit
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 12.dp,
                pressedElevation = 8.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp),
                content = content
            )
        }
    }
}
// ============================================================================
// IMPROVEMENT #6: ENHANCED UNLOCK BUTTON
// ============================================================================
/**
 * Enhanced unlock button with icon, elevation, and Material 3 styling
 * Improvement #6: Better button styling with icon + Material 3 compliance
 *
 * @param onClick Click callback
 * @param isLoading Loading state
 * @param isEnabled Button enabled state
 * @param modifier Additional modifiers
 * @param label Button label text
 */
@Composable
fun EnhancedUnlockButton(
    onClick: () -> Unit,
    isLoading: Boolean = false,
    isEnabled: Boolean = true,
    modifier: Modifier = Modifier,
    label: String = "Unlock App"
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 8.dp,
            pressedElevation = 4.dp,
            disabledElevation = 0.dp
        ),
        enabled = isEnabled && !isLoading
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            else -> {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier
                        .size(20.dp)
                        .padding(end = 8.dp)
                )
                Text(
                    label,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
// ============================================================================
// IMPROVEMENT #7: OPTIONAL BACKGROUND WATERMARK (Premium)
// ============================================================================
/**
 * Faded background watermark for premium branding
 * Improvement #7: Subtle background image for professional appearance
 *
 * @param backgroundImageRes Optional background image resource
 * @param backgroundAlpha Fade opacity for background
 * @param modifier Additional modifiers
 * @param content Screen content
 */
@Composable
fun BrandedBackgroundWrapper(
    backgroundImageRes: Int? = null,
    backgroundAlpha: Float = 0.05f,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        // Optional faded background
        if (backgroundImageRes != null) {
            androidx.compose.foundation.Image(
                painter = painterResource(backgroundImageRes),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(backgroundAlpha),
                contentScale = ContentScale.Crop
            )
        }
        // Semi-transparent overlay to ensure readability
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background.copy(alpha = 0.95f)
        ) {
            content()
        }
    }
}
// ============================================================================
// BONUS: COMPLETE PIN INPUT SECTION
// ============================================================================
/**
 * Complete PIN input section combining improvements #4, #5, #6
 * Reusable for both login and setup screens
 *
 * @param pinValue Current PIN value
 * @param onPINChanged Callback when PIN changes
 * @param onLoginClicked Callback when login button clicked
 * @param isLoading Loading state
 * @param isEnabled Input enabled state
 * @param errorMessage Error message to display
 * @param attemptCount Number of failed attempts
 * @param lockoutSecondsRemaining Lockout timer seconds
 * @param showForgotPIN Whether to show "Forgot PIN" button
 * @param onForgotPINClick Callback for "Forgot PIN" click
 */
@Composable
fun PINInputSection(
    pinValue: String,
    onPINChanged: (String) -> Unit,
    onLoginClicked: () -> Unit,
    isLoading: Boolean = false,
    isEnabled: Boolean = true,
    errorMessage: String? = null,
    attemptCount: Int = 0,
    lockoutSecondsRemaining: Long = 0L,
    showForgotPIN: Boolean = true,
    onForgotPINClick: () -> Unit = {}
) {
    PINFormCard {
        // Title
        Text(
            text = "Enter your PIN",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        // PIN input field (hidden)
        OutlinedTextField(
            value = pinValue,
            onValueChange = onPINChanged,
            label = { Text("PIN") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            enabled = isEnabled && lockoutSecondsRemaining == 0L && !isLoading,
            isError = errorMessage != null
        )
        // IMPROVEMENT #4: PIN dot indicator
        PINDotIndicator(
            pinLength = pinValue.length,
            modifier = Modifier.padding(vertical = 12.dp)
        )
        // Error message
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall
            )
        }
        // IMPROVEMENT #6: Enhanced unlock button
        EnhancedUnlockButton(
            onClick = onLoginClicked,
            isLoading = isLoading,
            isEnabled = pinValue.isNotEmpty() && lockoutSecondsRemaining == 0L && isEnabled
        )
        // Attempt counter or lockout timer
        when {
            lockoutSecondsRemaining > 0 -> {
                Text(
                    text = "Try again in ${lockoutSecondsRemaining}s",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            attemptCount > 0 -> {
                Text(
                    text = "Attempts: $attemptCount/3",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        // Forgot PIN link
        if (showForgotPIN) {
            TextButton(onClick = onForgotPINClick) {
                Text("Forgot PIN?")
            }
        }
    }
}
