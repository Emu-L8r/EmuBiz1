package com.emul8r.bizap.ui.shared

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emul8r.bizap.ui.landing.GuiMode
import com.emul8r.bizap.ui.gui3.theme.MatrixBlack
import com.emul8r.bizap.ui.gui3.theme.MatrixGreen
import com.emul8r.bizap.ui.gui3.theme.MatrixGreenBright
import com.emul8r.bizap.ui.theme.Spacing

/**
 * GUI Mode Selector - Terminal Edition (Phase 1.2)
 *
 * Displays system selector "SYS › 1 ◆ 2 ◆ 3" with cyberpunk aesthetic
 * - Monospace font throughout
 * - Pure Matrix green colors
 * - CRT flicker effect on active mode
 * - Terminal-style label "SYS"
 * - Sharp corners (RoundedCornerShape 2.dp minimal)
 */
@Composable
fun GuiModeSwitcher(
    currentMode: GuiMode,
    onGui1Click: () -> Unit,
    onGui2Click: () -> Unit,
    onGui3Click: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(end = Spacing.md)
    ) {
        Row(
            modifier = Modifier
                .background(
                    color = MatrixBlack.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(2.dp)
                )
                .border(
                    width = 1.dp,
                    color = MatrixGreen.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(2.dp)
                )
                .shadow(
                    elevation = 2.dp,
                    shape = RoundedCornerShape(2.dp),
                    ambientColor = MatrixGreen.copy(alpha = 0.4f)
                )
                .padding(Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            // System label
            Text(
                text = "SYS",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = MatrixGreen.copy(alpha = 0.8f),
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(horizontal = Spacing.sm)
            )

            // Chevron separator
            Text(
                text = "›",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MatrixGreen.copy(alpha = 0.7f)
            )

            // GUI1 Button
            TerminalModeButton(
                label = "1",
                isSelected = currentMode == GuiMode.GUI1,
                onClick = onGui1Click
            )

            // Diamond separator
            Text(
                text = "◆",
                fontFamily = FontFamily.Monospace,
                fontSize = 8.sp,
                color = MatrixGreen.copy(alpha = 0.6f)
            )

            // GUI2 Button
            TerminalModeButton(
                label = "2",
                isSelected = currentMode == GuiMode.GUI2,
                onClick = onGui2Click
            )

            // Diamond separator
            Text(
                text = "◆",
                fontFamily = FontFamily.Monospace,
                fontSize = 8.sp,
                color = MatrixGreen.copy(alpha = 0.6f)
            )

            // GUI3 Button (always bright when selected)
            TerminalModeButton(
                label = "3",
                isSelected = currentMode == GuiMode.GUI3,
                onClick = onGui3Click,
                isPremium = true
            )
        }
    }
}

/**
 * Terminal mode button with CRT flicker for active selection
 */
@Composable
private fun TerminalModeButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    isPremium: Boolean = false,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "crtFlicker")

    // CRT flicker effect - only when selected
    val flickerAlpha by if (isSelected) {
        infiniteTransition.animateFloat(
            initialValue = 0.9f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(100, easing = EaseInOutQuad),
                repeatMode = RepeatMode.Reverse
            ),
            label = "crtFlickerAlpha"
        )
    } else {
        rememberInfiniteTransition(label = "disabled").animateFloat(
            initialValue = 0.6f,
            targetValue = 0.6f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Reverse
            ),
            label = "noFlicker"
        )
    }

    val backgroundColor = when {
        isSelected && isPremium -> MatrixGreenBright.copy(alpha = 0.15f)
        isSelected -> MatrixGreen.copy(alpha = 0.1f)
        else -> MatrixBlack.copy(alpha = 0.05f)
    }

    val textColor = when {
        isSelected && isPremium -> MatrixGreenBright
        isSelected -> MatrixGreen
        else -> MatrixGreen.copy(alpha = 0.5f)
    }

    Button(
        onClick = onClick,
        modifier = modifier
            .height(24.dp)
            .width(24.dp)
            .border(1.dp, textColor.copy(alpha = if (isSelected) 0.8f else 0.4f), RoundedCornerShape(2.dp))
            .background(backgroundColor, RoundedCornerShape(2.dp))
            .alpha(flickerAlpha),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = textColor,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = MatrixGreen.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(2.dp),
        contentPadding = PaddingValues(0.dp),
        enabled = true
    ) {
        Text(
            text = label,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            letterSpacing = 0.5.sp
        )
    }
}

