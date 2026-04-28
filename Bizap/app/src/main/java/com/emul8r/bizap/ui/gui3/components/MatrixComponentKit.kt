package com.emul8r.bizap.ui.gui3.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.emul8r.bizap.ui.gui3.theme.*
import com.emul8r.bizap.ui.theme.Spacing

/**
 * MATRIX COMPONENT KIT (Phase 1.1)
 *
 * Unified component library for GUI3 (Matrix Edition)
 * - Eliminates Material Design color/shape inconsistencies
 * - Single source of truth for all UI components
 * - Pure cyberpunk aesthetic (green on dark, monospace, sharp corners)
 *
 * Components organized by category:
 * 1. Buttons (Primary/Secondary/Danger/Success)
 * 2. Forms (TextField, Toggle)
 * 3. Cards & Containers (Card, Dialog, LoadingScreen, ErrorScreen)
 * 4. Status Displays (Badge, Chip, StatusBar)
 * 5. Utility (SearchBar, Divider)
 */

// ============================================================================
// SECTION 1: BUTTONS
// ============================================================================

/**
 * MatrixPrimaryButton
 * - Green border, transparent background
 * - Default button for primary actions
 */
@Composable
fun MatrixPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isHighlight: Boolean = false
) {
    val borderColor = if (isHighlight) MatrixGreenBright else MatrixGreen
    val textColor = if (enabled) borderColor else MatrixGreen.copy(alpha = 0.5f)

    Button(
        onClick = onClick,
        modifier = modifier
            .height(48.dp)
            .border(2.dp, borderColor, RoundedCornerShape(4.dp)),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = textColor,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = MatrixGreen.copy(alpha = 0.5f)
        ),
        enabled = enabled,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = text,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            letterSpacing = 0.5.sp
        )
    }
}

/**
 * MatrixOutlineButton
 * - Lighter border, less emphasis
 * - For secondary/tertiary actions
 */
@Composable
fun MatrixSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(48.dp)
            .border(1.dp, MatrixGreen.copy(alpha = 0.6f), RoundedCornerShape(4.dp)),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = MatrixGreen.copy(alpha = 0.8f),
            disabledContainerColor = Color.Transparent,
            disabledContentColor = MatrixGreen.copy(alpha = 0.3f)
        ),
        enabled = enabled,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = text,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp
        )
    }
}

/**
 * MatrixDangerButton
 * - Red border for destructive actions
 */
@Composable
fun MatrixDangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(48.dp)
            .border(2.dp, MatrixError, RoundedCornerShape(4.dp)),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = MatrixError,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = MatrixError.copy(alpha = 0.5f)
        ),
        enabled = enabled,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = text,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            letterSpacing = 0.5.sp
        )
    }
}

/**
 * MatrixSuccessButton
 * - Green border with success semantic
 */
@Composable
fun MatrixSuccessButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(48.dp)
            .border(2.dp, MatrixSuccess, RoundedCornerShape(4.dp)),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = MatrixSuccess,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = MatrixSuccess.copy(alpha = 0.5f)
        ),
        enabled = enabled,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = text,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}

// ============================================================================
// SECTION 2: FORMS
// ============================================================================

/**
 * MatrixTextField
 * - Matrix-styled text input
 * - Monospace font, green colors, sharp corners
 */
@Composable
fun MatrixTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
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
            enabled = enabled,
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

        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = MatrixError,
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

/**
 * MatrixToggle
 * - Matrix-styled switch
 */
@Composable
fun MatrixToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.md),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (label != null) {
            Text(
                text = label,
                fontFamily = FontFamily.Monospace,
                color = MatrixGreen,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MatrixGreen,
                checkedTrackColor = MatrixGreen.copy(alpha = 0.3f),
                uncheckedThumbColor = MatrixGreen.copy(alpha = 0.5f),
                uncheckedTrackColor = MatrixGreen.copy(alpha = 0.1f)
            )
        )
    }
}

// ============================================================================
// SECTION 3: CARDS & CONTAINERS
// ============================================================================

/**
 * MatrixCard
 * - Simple container with Matrix border
 */
@Composable
fun MatrixCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MatrixGreen, RoundedCornerShape(4.dp)),
        color = MatrixSurface,
        shape = RoundedCornerShape(4.dp)
    ) {
        Box(modifier = Modifier.padding(Spacing.lg)) {
            content()
        }
    }
}

/**
 * MatrixDialog
 * - Matrix-themed modal dialog
 */
@Composable
fun MatrixDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    confirmButtonText: String = "CONFIRM",
    dismissButtonText: String = "CANCEL"
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .border(2.dp, MatrixGreen, RoundedCornerShape(4.dp)),
            color = MatrixBlack,
            shape = RoundedCornerShape(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                Text(
                    text = title,
                    fontFamily = FontFamily.Monospace,
                    color = MatrixGreenBright,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Text(
                    text = message,
                    fontFamily = FontFamily.Monospace,
                    color = MatrixGreen,
                    fontSize = 13.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    MatrixSecondaryButton(
                        text = dismissButtonText,
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )

                    MatrixPrimaryButton(
                        text = confirmButtonText,
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        isHighlight = true
                    )
                }
            }
        }
    }
}

/**
 * MatrixLoadingScreen
 * - Full-screen loading indicator
 */
@Composable
fun MatrixLoadingScreen(message: String = "Loading...") {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MatrixBlack),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            CircularProgressIndicator(
                color = MatrixGreen,
                modifier = Modifier.size(48.dp),
                strokeWidth = 2.dp
            )

            Text(
                text = message,
                fontFamily = FontFamily.Monospace,
                color = MatrixGreen,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }
    }
}

/**
 * MatrixErrorScreen
 * - Full-screen error display with retry option
 */
@Composable
fun MatrixErrorScreen(
    error: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MatrixBlack),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .border(2.dp, MatrixError, RoundedCornerShape(4.dp))
                .padding(Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Text(
                text = "⚠️ ERROR",
                fontFamily = FontFamily.Monospace,
                color = MatrixError,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Text(
                text = error,
                fontFamily = FontFamily.Monospace,
                color = MatrixGreen,
                fontSize = 12.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                MatrixSecondaryButton(
                    text = "DISMISS",
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                )

                MatrixDangerButton(
                    text = "RETRY",
                    onClick = onRetry,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// ============================================================================
// SECTION 4: STATUS DISPLAYS
// ============================================================================

/**
 * MatrixChip
 * - Small status/tag badge
 */
@Composable
fun MatrixChip(
    label: String,
    modifier: Modifier = Modifier,
    onRemove: (() -> Unit)? = null,
    isSelected: Boolean = false
) {
    val borderColor = if (isSelected) MatrixGreenBright else MatrixGreen

    Surface(
        modifier = modifier
            .border(1.dp, borderColor, RoundedCornerShape(4.dp)),
        color = if (isSelected) MatrixGreen.copy(alpha = 0.15f) else MatrixBlack,
        shape = RoundedCornerShape(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontFamily = FontFamily.Monospace,
                color = borderColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )

            if (onRemove != null) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Remove",
                    tint = borderColor,
                    modifier = Modifier
                        .size(16.dp)
                        .clickable(onClick = onRemove)
                )
            }
        }
    }
}

// ============================================================================
// SECTION 5: UTILITY
// ============================================================================

/**
 * MatrixSearchBar
 * - Search input field
 */
@Composable
fun MatrixSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search..."
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(placeholder) },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MatrixGreen,
            focusedBorderColor = MatrixGreenBright,
            unfocusedTextColor = MatrixGreen,
            focusedTextColor = MatrixGreenBright,
            cursorColor = MatrixGreen
        ),
        shape = RoundedCornerShape(4.dp),
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            fontFamily = FontFamily.Monospace,
            color = MatrixGreen
        )
    )
}

/**
 * MatrixDivider
 * - Semantic divider with Matrix styling
 */
@Composable
fun MatrixDivider(
    modifier: Modifier = Modifier,
    alpha: Float = 0.3f
) {
    HorizontalDivider(
        modifier = modifier.fillMaxWidth(),
        color = MatrixGreen.copy(alpha = alpha),
        thickness = 1.dp
    )
}












