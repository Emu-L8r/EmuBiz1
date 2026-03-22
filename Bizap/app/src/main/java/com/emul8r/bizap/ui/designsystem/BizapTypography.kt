package com.emul8r.bizap.ui.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Centralized Typography definitions for Bizap app.
 *
 * This object provides all text styles used throughout the app,
 * ensuring consistency and making typography changes easy.
 *
 * Usage:
 * ```
 * Text(text = "Hello", style = BizapTypography.headlineMedium)
 * Text(text = "Subtitle", style = BizapTypography.bodySmall)
 * ```
 */
object BizapTypography {

    // ═══════════════════════════════════════════════════════════════════════════
    // HEADING STYLES
    // ═══════════════════════════════════════════════════════════════════════════

    val headlineExtraLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    )

    val headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    )

    val headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    )

    val headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    )

    // ═══════════════════════════════════════════════════════════════════════════
    // TITLE STYLES
    // ═══════════════════════════════════════════════════════════════════════════

    val titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp
    )

    val titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    )

    val titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    )

    // ═══════════════════════════════════════════════════════════════════════════
    // BODY STYLES
    // ═══════════════════════════════════════════════════════════════════════════

    val bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    )

    val bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    )

    val bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    )

    // ═══════════════════════════════════════════════════════════════════════════
    // LABEL STYLES (Small caps, often for UI labels)
    // ═══════════════════════════════════════════════════════════════════════════

    val labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )

    val labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )

    val labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.sp
    )

    // ═══════════════════════════════════════════════════════════════════════════
    // DISPLAY STYLES (Extra-large headings)
    // ═══════════════════════════════════════════════════════════════════════════

    val displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    )

    val displayMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    )

    val displaySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    )

    // ═══════════════════════════════════════════════════════════════════════════
    // SEMANTIC STYLES (Purpose-specific)
    // ═══════════════════════════════════════════════════════════════════════════

    val metricValue = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    )

    val metricLabel = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    )

    val statusBadge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp
    )

    val cardTitle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.1.sp
    )

    val cardSubtitle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.2.sp
    )

    val button = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    )

    val buttonSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )

    // ═══════════════════════════════════════════════════════════════════════════
    // MONOSPACE / CODE STYLES (For amounts, reference numbers, etc.)
    // ═══════════════════════════════════════════════════════════════════════════

    val monospaceAmount = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )

    val monospaceSmall = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp
    )
}

/**
 * Create a Material3 Typography object using Bizap's custom styles.
 * This can be used to override Material3's default typography.
 */
fun createBizapTypography(): Typography {
    return Typography(
        displayLarge = BizapTypography.displayLarge,
        displayMedium = BizapTypography.displayMedium,
        displaySmall = BizapTypography.displaySmall,
        headlineLarge = BizapTypography.headlineLarge,
        headlineMedium = BizapTypography.headlineMedium,
        headlineSmall = BizapTypography.headlineSmall,
        titleLarge = BizapTypography.titleLarge,
        titleMedium = BizapTypography.titleMedium,
        titleSmall = BizapTypography.titleSmall,
        bodyLarge = BizapTypography.bodyLarge,
        bodyMedium = BizapTypography.bodyMedium,
        bodySmall = BizapTypography.bodySmall,
        labelLarge = BizapTypography.labelLarge,
        labelMedium = BizapTypography.labelMedium,
        labelSmall = BizapTypography.labelSmall
    )
}

