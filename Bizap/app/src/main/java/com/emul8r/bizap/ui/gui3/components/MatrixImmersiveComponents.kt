package com.emul8r.bizap.ui.gui3.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emul8r.bizap.ui.gui3.theme.*
import com.emul8r.bizap.ui.gui3.util.MatrixCascadeState
import com.emul8r.bizap.ui.theme.Spacing
import kotlinx.coroutines.flow.collect

/**
 * PHASE 4: IMMERSIVE MATRIX COMPONENTS
 *
 * Enhanced components for epic cyberpunk aesthetic
 */

/**
 * GLOWING BUTTON - Pure Cyberpunk Matrix Button (No Material3 dependency)
 * ✅ PHASE 2 TASK 2: Glasmorphic style - cascading background visible through buttons!
 * ✅ PHASE 2 TASK 3 & 4: Cascade glow reaction - button glows when code passes behind
 * Interactive with pulsing glow and full click handling
 */
@Composable
fun GlowingMatrixButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isHighlight: Boolean = false
) {
    var isPressed by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "glowPulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    // ✅ PHASE 2 TASK 3 & 4: Read cascade state - simple immersive effect
    val cascadeVisibility by MatrixCascadeState.cascadeVisibility
    val cascadeGlowIntensity by animateFloatAsState(
        targetValue = glowAlpha + (cascadeVisibility * 0.2f),  // Glow intensifies when cascade is visible
        animationSpec = tween(200),
        label = "cascadeGlow"
    )

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(100, easing = EaseInOutQuad),
        label = "buttonScale"
    )

    val borderColor = if (isHighlight) MatrixGreenBright else MatrixGreen
    val backgroundColor = if (isPressed) MatrixGreen.copy(alpha = 0.15f) else MatrixBlack.copy(alpha = 0.08f)  // ✅ Nearly transparent

    val interactionSource = remember { MutableInteractionSource() }

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> isPressed = true
                is PressInteraction.Release -> {
                    isPressed = false
                    onClick()
                }
                is PressInteraction.Cancel -> isPressed = false
                else -> {}
            }
        }
    }

    // ✅ PHASE 2 TASK 2: Glasmorphic box - cascade visible through!
    Box(
        modifier = modifier
            .heightIn(min = 48.dp)
            .border(
                width = 2.dp,
                color = borderColor.copy(alpha = cascadeGlowIntensity),  // ✅ TASK 4: Glow intensity changes
                shape = RoundedCornerShape(4.dp)
            )
            .background(
                color = backgroundColor,  // ✅ 0.08f = nearly transparent
                shape = RoundedCornerShape(4.dp)
            )
            .shadow(
                elevation = (2f + (cascadeVisibility * 4f)).dp,  // ✅ TASK 4: Shadow increases when cascade active
                shape = RoundedCornerShape(4.dp),
                ambientColor = MatrixGreen.copy(alpha = cascadeGlowIntensity * 0.4f)  // ✅ Glow shadow
            )
            .alpha(0.9f)  // ✅ Button slightly transparent overall
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = {}
            )
            .scale(scale),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                fontFamily = FontFamily.Monospace,
                color = borderColor
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
    }
}

/**
 * MATRIX CARD - Premium bordered container with glow
 * ✅ PHASE 1: Glassmorphic style with reduced opacity to reveal background
 * Use for important data sections
 */
@Composable
fun MatrixCardPremium(
    title: String,
    modifier: Modifier = Modifier,
    isPulsing: Boolean = false,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cardGlow")
    val borderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = if (isPulsing) 0.8f else 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isPulsing) 2000 else 1, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "borderAlpha"
    )

    // ✅ GLASSMORPHIC: Semi-transparent background (0.15f opacity) reveals cascading effect
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                color = MatrixGreen.copy(alpha = if (isPulsing) borderAlpha else 0.6f),
                shape = RoundedCornerShape(10.dp)
            )
            .shadow(
                elevation = if (isPulsing) 12.dp else 4.dp,
                shape = RoundedCornerShape(10.dp),
                ambientColor = MatrixGreen.copy(alpha = 0.3f)
            ),
        color = MatrixBlack.copy(alpha = 0.15f),  // ✅ CHANGED: 0.15f glassmorphic instead of MatrixSurface
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)  // CONSISTENT SPACING between elements
        ) {
            // Header with Matrix styling
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    color = MatrixGreenBright,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    letterSpacing = 2.sp
                )
            )

            HorizontalDivider(
                color = MatrixGreen.copy(alpha = 0.4f),
                thickness = 1.5.dp
            )

            // Content - wrapped in a Column for automatic spacing
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)  // Elements inside content also spaced
            ) {
                content()
            }
        }
    }
}

/**
 * TERMINAL DISPLAY - Monospace data table
 * Perfect for financial/analytical displays
 */
@Composable
fun TerminalDataDisplay(
    rows: List<Pair<String, String>>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MatrixBlack.copy(alpha = 0.5f),
                shape = RoundedCornerShape(4.dp)
            )
            .border(
                width = 1.dp,
                color = MatrixGreen.copy(alpha = 0.3f),
                shape = RoundedCornerShape(4.dp)
            )
            .padding(Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        rows.forEach { (label, value) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        color = MatrixGreen.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp
                    )
                )

                Text(
                    text = value,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        color = MatrixGreenBright,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                )
            }
        }
    }
}

/**
 * SCANLINE EFFECT - Horizontal lines for authenticity
 */
@Composable
fun ScanlineOverlay(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        repeat(50) { index ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .offset(y = (index * 4).dp)
                    .background(MatrixGreen.copy(alpha = 0.02f))
            )
        }
    }
}

/**
 * GLITCH EFFECT TEXT - Screen transition effect
 */
@Composable
fun GlitchText(
    text: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "glitch")
    val offsetX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(100),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glitchX"
    )

    Text(
        text = text,
        modifier = modifier.alpha((1f - (offsetX * 0.3f))),
        style = MaterialTheme.typography.headlineMedium.copy(
            fontFamily = FontFamily.Monospace,
            color = MatrixGreen,
            letterSpacing = 2.sp
        )
    )
}

/**
 * ENHANCED STATUS BADGE - With glow effect
 */
@Composable
fun GlowingStatusBadge(
    status: String,
    modifier: Modifier = Modifier,
    style: MatrixStatusStyle = MatrixStatusStyle.NEUTRAL
) {
    val infiniteTransition = rememberInfiniteTransition(label = "badgeGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "badgeGlowAlpha"
    )

    val (backgroundColor, textColor) = when (style) {
        MatrixStatusStyle.SUCCESS -> Pair(
            MatrixSuccess.copy(alpha = 0.1f),
            MatrixSuccess
        )
        MatrixStatusStyle.ERROR -> Pair(
            MatrixError.copy(alpha = 0.1f),
            MatrixError
        )
        MatrixStatusStyle.WARNING -> Pair(
            MatrixWarning.copy(alpha = 0.1f),
            MatrixWarning
        )
        MatrixStatusStyle.INFO -> Pair(
            CyanAccent.copy(alpha = 0.1f),
            CyanAccent
        )
        MatrixStatusStyle.NEUTRAL -> Pair(
            MatrixGreen.copy(alpha = 0.1f),
            MatrixGreen
        )
    }

    Surface(
        modifier = modifier
            .border(
                width = 1.5.dp,
                color = textColor.copy(alpha = glowAlpha),
                shape = RoundedCornerShape(6.dp)
            )
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(6.dp),
                ambientColor = textColor.copy(alpha = 0.3f)
            ),
        color = backgroundColor,
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = status,
            style = MaterialTheme.typography.labelSmall.copy(
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                letterSpacing = 0.5.sp
            ),
            modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm)
        )
    }
}







