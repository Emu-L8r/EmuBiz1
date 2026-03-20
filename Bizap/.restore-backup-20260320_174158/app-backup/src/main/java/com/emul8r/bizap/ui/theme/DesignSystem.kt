package com.emul8r.bizap.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ---------------------------------------------------------------------------
// Material 3 Brand Color Palette
// ---------------------------------------------------------------------------

/** Primary brand color — indigo/violet as used by BizapTheme. */
val md_theme_light_primary = Color(0xFF6750A4)
val md_theme_light_onPrimary = Color(0xFFFFFFFF)
val md_theme_light_primaryContainer = Color(0xFFEADDFF)
val md_theme_light_onPrimaryContainer = Color(0xFF21005D)

val md_theme_light_secondary = Color(0xFF625B71)
val md_theme_light_onSecondary = Color(0xFFFFFFFF)
val md_theme_light_secondaryContainer = Color(0xFFE8DEF8)
val md_theme_light_onSecondaryContainer = Color(0xFF1D192B)

val md_theme_light_tertiary = Color(0xFF7D5260)
val md_theme_light_onTertiary = Color(0xFFFFFFFF)
val md_theme_light_tertiaryContainer = Color(0xFFFFD8E4)
val md_theme_light_onTertiaryContainer = Color(0xFF31111D)

val md_theme_light_surface = Color(0xFFFAFAFA)
val md_theme_light_onSurface = Color(0xFF1C1C1C)
val md_theme_light_surfaceVariant = Color(0xFFE7E0EC)
val md_theme_light_onSurfaceVariant = Color(0xFF49454F)

val md_theme_light_background = Color(0xFFFBFDF8)
val md_theme_light_onBackground = Color(0xFF191C19)

val md_theme_light_outline = Color(0xFF79747E)
val md_theme_light_outlineVariant = Color(0xFFCAC4D0)

val md_theme_light_error = Color(0xFFB3261E)
val md_theme_light_onError = Color(0xFFFFFFFF)
val md_theme_light_errorContainer = Color(0xFFF9DEDC)
val md_theme_light_onErrorContainer = Color(0xFF410E0B)

// ---------------------------------------------------------------------------
// Material 3 Semantic / Status Colors
// ---------------------------------------------------------------------------

/** Invoice / financial status — positive values. */
val semanticSuccess = Color(0xFF4CAF50)

/** Caution indicator. */
val semanticWarning = Color(0xFFFFA500)

/** Matches Material 3 error role. */
val semanticError = Color(0xFFB3261E)

/** Informational accent. */
val semanticInfo = Color(0xFF2196F3)

// ---------------------------------------------------------------------------
// Material 3 Typography Scale
// ---------------------------------------------------------------------------

/** Display Large — 57 sp, Bold */
val displayLarge = TextStyle(
    fontSize = 57.sp,
    fontWeight = FontWeight.Bold,
    lineHeight = 64.sp
)

/** Display Medium — 45 sp, Bold */
val displayMedium = TextStyle(
    fontSize = 45.sp,
    fontWeight = FontWeight.Bold,
    lineHeight = 52.sp
)

/** Display Small — 36 sp, Bold */
val displaySmall = TextStyle(
    fontSize = 36.sp,
    fontWeight = FontWeight.Bold,
    lineHeight = 44.sp
)

/** Headline Large — 32 sp, Bold */
val headlineLarge = TextStyle(
    fontSize = 32.sp,
    fontWeight = FontWeight.Bold,
    lineHeight = 40.sp
)

/** Headline Medium — 28 sp, Bold */
val headlineMedium = TextStyle(
    fontSize = 28.sp,
    fontWeight = FontWeight.Bold,
    lineHeight = 36.sp
)

/** Headline Small — 24 sp, Bold */
val headlineSmall = TextStyle(
    fontSize = 24.sp,
    fontWeight = FontWeight.Bold,
    lineHeight = 32.sp
)

/** Title Large — 22 sp, SemiBold */
val titleLarge = TextStyle(
    fontSize = 22.sp,
    fontWeight = FontWeight.SemiBold,
    lineHeight = 28.sp
)

/** Title Medium — 16 sp, SemiBold */
val titleMedium = TextStyle(
    fontSize = 16.sp,
    fontWeight = FontWeight.SemiBold,
    lineHeight = 24.sp
)

/** Title Small — 14 sp, SemiBold */
val titleSmall = TextStyle(
    fontSize = 14.sp,
    fontWeight = FontWeight.SemiBold,
    lineHeight = 20.sp
)

/** Body Large — 16 sp, Normal */
val bodyLarge = TextStyle(
    fontSize = 16.sp,
    fontWeight = FontWeight.Normal,
    lineHeight = 24.sp
)

/** Body Medium — 14 sp, Normal */
val bodyMedium = TextStyle(
    fontSize = 14.sp,
    fontWeight = FontWeight.Normal,
    lineHeight = 20.sp
)

/** Body Small — 12 sp, Normal */
val bodySmall = TextStyle(
    fontSize = 12.sp,
    fontWeight = FontWeight.Normal,
    lineHeight = 16.sp
)

/** Label Large — 14 sp, SemiBold */
val labelLarge = TextStyle(
    fontSize = 14.sp,
    fontWeight = FontWeight.SemiBold,
    lineHeight = 20.sp
)

/** Label Medium — 12 sp, SemiBold */
val labelMedium = TextStyle(
    fontSize = 12.sp,
    fontWeight = FontWeight.SemiBold,
    lineHeight = 16.sp
)

/** Label Small — 11 sp, SemiBold */
val labelSmall = TextStyle(
    fontSize = 11.sp,
    fontWeight = FontWeight.SemiBold,
    lineHeight = 16.sp
)

// ---------------------------------------------------------------------------
// Material 3 Spacing System (8 dp baseline grid)
// ---------------------------------------------------------------------------

/** 2 dp — micro spacing */
val spacing2: Dp = 2.dp

/** 4 dp — extra-small spacing */
val spacing4: Dp = 4.dp

/** 8 dp — small spacing */
val spacing8: Dp = 8.dp

/** 12 dp — medium spacing */
val spacing12: Dp = 12.dp

/** 16 dp — large spacing (standard screen margin) */
val spacing16: Dp = 16.dp

/** 24 dp — extra-large spacing */
val spacing24: Dp = 24.dp

/** 32 dp — double-extra-large spacing */
val spacing32: Dp = 32.dp

/** 48 dp — triple-extra-large spacing */
val spacing48: Dp = 48.dp

// ---------------------------------------------------------------------------
// Material 3 Corner Radius Scale
// ---------------------------------------------------------------------------

/** 8 dp — small components (chips, text fields, small cards) */
val cornerRadiusSmall: Dp = 8.dp

/** 12 dp — medium components (cards, dialogs) */
val cornerRadiusMedium: Dp = 12.dp

/** 16 dp — large components (bottom sheets, large cards) */
val cornerRadiusLarge: Dp = 16.dp

/** 28 dp — extra-large components (FABs, full-width bottom sheets) */
val cornerRadiusExtraLarge: Dp = 28.dp
