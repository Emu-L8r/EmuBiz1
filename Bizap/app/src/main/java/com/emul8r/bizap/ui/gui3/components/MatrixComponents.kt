package com.emul8r.bizap.ui.gui3.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.vector.ImageVector
import com.emul8r.bizap.ui.gui3.theme.*
import com.emul8r.bizap.ui.theme.Spacing

/**
 * Matrix DetailRow Component
 *
 * Displays label-value pair with Matrix styling:
 * - Green monospace text
 * - Bordered card with glow effect
 * - Professional financial data display
 */
@Composable
fun DetailRowMatrix(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    isHighlight: Boolean = false
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (isHighlight) MatrixGreenBright else MatrixGreen,
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.SansSerif,
                color = MatrixGreen.copy(alpha = 0.8f),
                fontWeight = FontWeight.Normal
            )
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace,
                color = if (isHighlight) MatrixGreenBright else MatrixGreen,
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}

/**
 * Matrix SectionCard Component
 *
 * Groups related content with Matrix styling:
 * - Bordered container with glow
 * - Green header
 * - Professional content grouping
 */
@Composable
fun SectionCardMatrix(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MatrixGreen,
                shape = RoundedCornerShape(8.dp)
            ),
        color = MatrixSurface,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg)
        ) {
            // Header
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    color = MatrixGreenBright,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    letterSpacing = 0.5.sp
                ),
                modifier = Modifier.padding(bottom = Spacing.md)
            )

            HorizontalDivider(
                color = MatrixGreen.copy(alpha = 0.3f),
                thickness = 1.dp,
                modifier = Modifier.padding(bottom = Spacing.md)
            )

            // Content
            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                content()
            }
        }
    }
}

/**
 * Matrix Button Component
 *
 * Bordered button with Matrix styling:
 * - Green border
 * - Green text
 * - Transparent background
 * - Glow on hover/focus
 */
@Composable
fun MatrixButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isHighlight: Boolean = false
) {
    val borderColor = if (isHighlight) MatrixGreenBright else MatrixGreen

    Button(
        onClick = onClick,
        modifier = modifier
            .height(48.dp)
            .border(
                width = 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(4.dp)
            ),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = borderColor,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = MatrixGreen.copy(alpha = 0.5f)
        ),
        enabled = enabled,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp
            ),
            maxLines = 2,
            overflow = TextOverflow.Visible
        )
    }
}

/**
 * Matrix FormattedAmount Component
 *
 * Displays monetary values with semantic coloring
 */
@Composable
fun FormattedAmountMatrix(
    amount: String,
    modifier: Modifier = Modifier,
    isPositive: Boolean? = null,
    isHighlight: Boolean = false
) {
    val color = when {
        isHighlight -> MatrixGreenBright
        isPositive == true -> MatrixSuccess
        isPositive == false -> MatrixError
        else -> MatrixGreen
    }

    Text(
        text = amount,
        style = MaterialTheme.typography.bodyMedium.copy(
            fontFamily = FontFamily.Monospace,
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        ),
        modifier = modifier
    )
}

/**
 * Matrix StatusBadge Component
 *
 * Status indicator with Matrix styling:
 * - Semantic colors (green/red/yellow/cyan)
 * - Bordered design
 * - Monospace font
 */
@Composable
fun MatrixStatusBadge(
    status: String,
    modifier: Modifier = Modifier,
    style: MatrixStatusStyle = MatrixStatusStyle.NEUTRAL
) {
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
                width = 1.dp,
                color = textColor,
                shape = RoundedCornerShape(4.dp)
            ),
        color = backgroundColor,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = status,
            style = MaterialTheme.typography.labelSmall.copy(
                color = textColor,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace
            ),
            modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm)
        )
    }
}

/**
 * Matrix OutlinedTextField Component
 *
 * Text input field with Matrix styling:
 * - Green border
 * - Green text and icons
 * - Monospace font option
 * - Error state with red border
 */
@Composable
fun MatrixOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    minLines: Int = 1,
    maxLines: Int = if (minLines > 1) Int.MAX_VALUE else 1
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = if (isError) MatrixError else MatrixGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            isError = isError,
            minLines = minLines,
            maxLines = maxLines,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MatrixGreen,
                focusedBorderColor = MatrixGreenBright,
                errorBorderColor = MatrixError,
                unfocusedLabelColor = MatrixGreen.copy(alpha = 0.7f),
                focusedLabelColor = MatrixGreenBright,
                errorLabelColor = MatrixError,
                cursorColor = MatrixGreen,
                unfocusedTextColor = MatrixGreen,
                focusedTextColor = MatrixGreenBright,
                errorTextColor = MatrixError
            ),
            shape = RoundedCornerShape(4.dp),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace,
                color = MatrixGreen
            )
        )

        // Error message
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = MatrixError,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp),
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

enum class MatrixStatusStyle {
    SUCCESS,
    ERROR,
    WARNING,
    INFO,
    NEUTRAL
}

