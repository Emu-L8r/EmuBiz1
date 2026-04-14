package com.emul8r.bizap.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Specialized component for displaying formatted monetary amounts with consistent styling.
 *
 * IMPROVES:
 * - Consistent monetary value presentation across all screens
 * - Proper emphasis (bold weight for amounts)
 * - Semantic coloring for positive/negative/neutral amounts
 * - Optional currency symbol styling
 *
 * Usage:
 * ```
 * FormattedAmountText(amount = "1,234.56")  // Standard amount
 * FormattedAmountText(amount = "1,234.56", isPositive = true)  // Green
 * FormattedAmountText(amount = "500.00", isPositive = false)  // Red (overdue)
 * FormattedAmountText(amount = "0.00")  // Gray (zero amount)
 * ```
 */
@Composable
fun FormattedAmountText(
    amount: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 14.sp,
    isPositive: Boolean? = null,  // null = neutral/default, true = positive/success, false = negative/error
    emphasize: Boolean = true
) {
    val color = when {
        isPositive == true -> Color(0xFF2E7D32)  // Green
        isPositive == false -> Color(0xFFD32F2F)  // Red
        else -> MaterialTheme.colorScheme.onSurface  // Default
    }

    Text(
        text = amount,
        style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = fontSize,
            fontWeight = if (emphasize) FontWeight.Bold else FontWeight.Normal
        ),
        color = color,
        modifier = modifier
    )
}

/**
 * Displays a monetary value with a prefix (like $ or €) in a lighter color.
 *
 * Usage:
 * ```
 * AmountWithCurrency(currency = "$", amount = "1,234.56")
 * AmountWithCurrency(currency = "€", amount = "5,000.00", isPositive = true)
 * ```
 */
@Composable
fun AmountWithCurrency(
    currency: String,
    amount: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 14.sp,
    isPositive: Boolean? = null,
    emphasize: Boolean = true
) {
    Text(
        text = "$currency$amount",
        style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = fontSize,
            fontWeight = if (emphasize) FontWeight.Bold else FontWeight.Normal
        ),
        color = when {
            isPositive == true -> Color(0xFF2E7D32)
            isPositive == false -> Color(0xFFD32F2F)
            else -> MaterialTheme.colorScheme.onSurface
        },
        modifier = modifier
    )
}

/**
 * Displays amount with badge-like styling for highlighting important values.
 *
 * Usage:
 * ```
 * AmountBadge(amount = "$ 5,000.00", label = "Total Invoice")
 * ```
 */
@Composable
fun AmountBadge(
    amount: String,
    modifier: Modifier = Modifier,
    label: String? = null,
    isPositive: Boolean? = null
) {
    Surface(
        modifier = modifier,
        color = when {
            isPositive == true -> Color(0xFF2E7D32).copy(alpha = 0.1f)
            isPositive == false -> Color(0xFFD32F2F).copy(alpha = 0.1f)
            else -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        },
        shape = MaterialTheme.shapes.small
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            if (label != null) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = amount,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                color = when {
                    isPositive == true -> Color(0xFF2E7D32)
                    isPositive == false -> Color(0xFFD32F2F)
                    else -> MaterialTheme.colorScheme.primary
                }
            )
        }
    }
}


