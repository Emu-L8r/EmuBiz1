package com.emul8r.bizap.ui.gui3.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.graphics.vector.ImageVector
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
 * GLOWING BUTTON — Cyberpunk Matrix Button with optional icon capsule.
 *
 * @param icon Optional Material vector icon. Rendered inside a bordered terminal-cell capsule.
 * @param accentColor Per-button accent tint (green, cyan, amber). Drives border, capsule, glow.
 * @param isHighlight Boosts height (56dp), capsule border (2dp), and border alpha for core actions.
 */
@Composable
fun GlowingMatrixButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isHighlight: Boolean = false,
    icon: ImageVector? = null,
    accentColor: Color = MatrixGreen
) {
    var isPressed by remember { mutableStateOf(false) }

    val pulse = LocalMatrixPulse.current
    val glowAlpha = 0.30f + pulse * 0.50f

    val cascadeVisibility by MatrixCascadeState.cascadeVisibility
    val cascadeGlowIntensity = (glowAlpha + cascadeVisibility * 0.2f).coerceIn(0f, 1f)

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(100, easing = EaseInOutQuad),
        label = "buttonScale"
    )

    // Press-flash states — icon capsule brightens on press, returns to pulse-driven idle
    val iconBorderAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else cascadeGlowIntensity * 0.55f,
        animationSpec = tween(if (isPressed) 60 else 200),
        label = "iconBorderAlpha"
    )
    val iconTintAlpha by animateFloatAsState(
        targetValue = if (isPressed) 1.0f else 0.80f,
        animationSpec = tween(if (isPressed) 60 else 200),
        label = "iconTintAlpha"
    )

    val minHeight = if (isHighlight) 56.dp else 48.dp
    val capsuleBorderWidth = if (isHighlight) 2.dp else 1.dp
    val borderAlphaBoost = if (isHighlight) 0.15f else 0f
    val effectiveBorderAlpha = (cascadeGlowIntensity + borderAlphaBoost).coerceIn(0f, 1f)
    val backgroundColor = if (isPressed) accentColor.copy(alpha = 0.15f) else MatrixBlack.copy(alpha = 0.08f)

    val interactionSource = remember { MutableInteractionSource() }
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press   -> isPressed = true
                is PressInteraction.Release -> { isPressed = false; onClick() }
                is PressInteraction.Cancel  -> isPressed = false
                else -> {}
            }
        }
    }

    Box(
        modifier = modifier
            .heightIn(min = minHeight)
            .border(width = 2.dp, color = accentColor.copy(alpha = effectiveBorderAlpha), shape = RoundedCornerShape(4.dp))
            .background(color = backgroundColor, shape = RoundedCornerShape(4.dp))
            .shadow(
                elevation = (2f + cascadeVisibility * 4f).dp,
                shape = RoundedCornerShape(4.dp),
                ambientColor = accentColor.copy(alpha = cascadeGlowIntensity * 0.4f)
            )
            .alpha(0.9f)
            .clickable(interactionSource = interactionSource, indication = null, enabled = enabled, onClick = {})
            .scale(scale),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                // Terminal-cell icon capsule — bordered square housing the vector glyph
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .border(capsuleBorderWidth, accentColor.copy(alpha = iconBorderAlpha), RoundedCornerShape(4.dp))
                        .background(accentColor.copy(alpha = 0.10f), RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor.copy(alpha = iconTintAlpha),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    fontFamily = FontFamily.Monospace,
                    color = accentColor.copy(alpha = if (isHighlight) 1.0f else 0.92f)
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
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
    borderColor: Color = MatrixGreen,
    content: @Composable () -> Unit
) {
    // Use global pulse from MatrixTheme — eliminates per-card InfiniteTransition
    val pulse = LocalMatrixPulse.current
    val borderAlpha = if (isPulsing) 0.30f + pulse * 0.50f else 0.60f
    val bracketAlpha = 0.40f + pulse * 0.42f  // corner brackets pulse with same rhythm
    val bracketColor = if (isPulsing) MatrixGreenBright else borderColor

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .drawWithContent {
                // Inner top glow — lit-from-above black glass effect
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            borderColor.copy(alpha = 0.07f + pulse * 0.05f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = size.height * 0.28f
                    )
                )
                drawContent()
                // Canvas corner brackets — L-shaped accents at each corner, 0 extra composables
                val bLen = 14.dp.toPx()
                val thick = 1.5.dp.toPx()
                val c = bracketColor.copy(alpha = bracketAlpha)
                // top-left
                drawRect(c, Offset(0f, 0f), Size(bLen, thick))
                drawRect(c, Offset(0f, 0f), Size(thick, bLen))
                // top-right
                drawRect(c, Offset(size.width - bLen, 0f), Size(bLen, thick))
                drawRect(c, Offset(size.width - thick, 0f), Size(thick, bLen))
                // bottom-left
                drawRect(c, Offset(0f, size.height - thick), Size(bLen, thick))
                drawRect(c, Offset(0f, size.height - bLen), Size(thick, bLen))
                // bottom-right
                drawRect(c, Offset(size.width - bLen, size.height - thick), Size(bLen, thick))
                drawRect(c, Offset(size.width - thick, size.height - bLen), Size(thick, bLen))
            }
            .border(width = 2.dp, color = borderColor.copy(alpha = borderAlpha), shape = RoundedCornerShape(10.dp))
            .shadow(elevation = if (isPulsing) 12.dp else 4.dp, shape = RoundedCornerShape(10.dp), ambientColor = borderColor.copy(alpha = 0.3f)),
        color = MatrixBlack.copy(alpha = 0.55f),  // increased: better foreground/rain separation
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = FontFamily.Monospace, color = MatrixGreenBright,
                    fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, letterSpacing = 2.sp
                )
            )
            HorizontalDivider(color = MatrixGreen.copy(alpha = 0.4f), thickness = 1.5.dp)
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
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
            .drawWithContent {
                drawContent()
                // Phosphor CRT scanlines — 1px lines every 6dp, very faint
                val scanSpacing = 6.dp.toPx()
                val scanColor = Color(0xFF00DD00).copy(alpha = 0.025f)
                val count = (size.height / scanSpacing).toInt()
                repeat(count) { i ->
                    drawLine(scanColor, Offset(0f, i * scanSpacing), Offset(size.width, i * scanSpacing), 0.5.dp.toPx())
                }
            }
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
                        color = MatrixGreen.copy(alpha = 0.65f),
                        fontWeight = FontWeight.Normal,
                        fontSize = 11.sp
                    )
                )

                Text(
                    text = value,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        color = MatrixGreenBright,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                )
            }
        }
    }
}

/**
 * SCANLINE EFFECT - Canvas-based horizontal lines + moving sweep
 * Replaces previous 50-Box implementation. Single Canvas draw pass, no composable overhead.
 * Moving sweep strip driven by global pulse — no extra InfiniteTransition.
 */
@Composable
fun ScanlineOverlay(modifier: Modifier = Modifier) {
    val pulse = LocalMatrixPulse.current  // 0.0→1.0→0.0, reuse global
    Canvas(modifier = modifier.fillMaxSize()) {
        val lineSpacing = 4.dp.toPx()
        val lineColor = Color(0xFF00DD00).copy(alpha = 0.022f)
        val strokeWidth = 0.7.dp.toPx()
        val lineCount = (size.height / lineSpacing).toInt()
        // All scanlines in one Canvas pass — no composables
        repeat(lineCount) { i ->
            val y = i * lineSpacing
            drawLine(lineColor, Offset(0f, y), Offset(size.width, y), strokeWidth)
        }
        // Moving bright sweep strip driven by global pulse (bidirectional — CRT double-pass)
        val sweepY = pulse * size.height
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.Transparent, Color(0xFF00FF00).copy(alpha = 0.052f), Color.Transparent),
                startY = (sweepY - 55f).coerceAtLeast(0f),
                endY = (sweepY + 55f).coerceAtMost(size.height)
            ),
            topLeft = Offset(0f, sweepY - 55f),
            size = Size(size.width, 110f)
        )
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
 * ENHANCED STATUS BADGE - With glow effect driven by global pulse
 */
@Composable
fun GlowingStatusBadge(
    status: String,
    modifier: Modifier = Modifier,
    style: MatrixStatusStyle = MatrixStatusStyle.NEUTRAL
) {
    // Use global pulse — no local InfiniteTransition needed
    val glowAlpha = 0.40f + LocalMatrixPulse.current * 0.40f

    val (backgroundColor, textColor) = when (style) {
        MatrixStatusStyle.SUCCESS -> Pair(MatrixSuccess.copy(alpha = 0.1f), MatrixSuccess)
        MatrixStatusStyle.ERROR   -> Pair(MatrixError.copy(alpha = 0.1f), MatrixError)
        MatrixStatusStyle.WARNING -> Pair(MatrixWarning.copy(alpha = 0.1f), MatrixWarning)
        MatrixStatusStyle.INFO    -> Pair(CyanAccent.copy(alpha = 0.1f), CyanAccent)
        MatrixStatusStyle.NEUTRAL -> Pair(MatrixGreen.copy(alpha = 0.1f), MatrixGreen)
    }

    Surface(
        modifier = modifier
            .border(width = 1.5.dp, color = textColor.copy(alpha = glowAlpha), shape = RoundedCornerShape(6.dp))
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(6.dp), ambientColor = textColor.copy(alpha = 0.3f)),
        color = backgroundColor,
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = status,
            style = MaterialTheme.typography.labelSmall.copy(
                color = textColor, fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace, fontSize = 11.sp, letterSpacing = 0.5.sp
            ),
            modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm)
        )
    }
}




