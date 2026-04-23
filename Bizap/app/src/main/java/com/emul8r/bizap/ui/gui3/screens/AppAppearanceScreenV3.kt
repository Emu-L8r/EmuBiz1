package com.emul8r.bizap.ui.gui3.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.emul8r.bizap.MainActivity
import com.emul8r.bizap.domain.model.ThemePreference
import com.emul8r.bizap.ui.gui2.settings.AppAppearanceViewModelV2
import com.emul8r.bizap.ui.gui3.components.GlowingMatrixButton
import com.emul8r.bizap.ui.gui3.components.MatrixBackgroundWrapper
import com.emul8r.bizap.ui.gui3.components.SectionCardMatrix
import com.emul8r.bizap.ui.gui3.theme.*
import com.emul8r.bizap.ui.gui3.util.ScreenType
import com.emul8r.bizap.ui.theme.AppTheme
import com.emul8r.bizap.ui.theme.Spacing
import timber.log.Timber

/**
 * App Appearance Screen V3 (Matrix Edition)
 *
 * GUI3 adaptation of AppAppearanceScreenV2 following Pattern #2C.
 * - Reuses AppAppearanceViewModelV2 (zero new ViewModel code)
 * - Identical UiState (AppSettingsUiState)
 * - Matrix theme: monospace fonts, MatrixGreen, SectionCardMatrix components
 * - Covers: Interface style, light/dark mode, GUI switching, custom colors link
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppAppearanceScreenV3(
    businessId: Long,
    navController: NavHostController,
    onThemeSettingsClick: () -> Unit = {},
    onBusinessProfileClick: () -> Unit = {},
    onBackupRestoreClick: () -> Unit = {},
    onHelpClick: () -> Unit = {},
    viewModel: AppAppearanceViewModelV2 = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    MatrixBackgroundWrapper(screenType = ScreenType.SETTINGS) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            ">> APPEARANCE",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                color = MatrixGreenBright,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                letterSpacing = 1.sp
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MatrixGreen
                            )
                        }
                    },
                    colors = matrixTopAppBarColors()
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MatrixGreen)
                    }
                }

                uiState.error != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            uiState.error ?: "Unknown error",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = MatrixError,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(paddingValues)
                            .padding(Spacing.lg),
                        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
                    ) {

                        // ── INTERFACE STYLE ──────────────────────────────────────────
                        SectionCardMatrix(title = ">> INTERFACE STYLE") {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    "Classic = Material 2  ·  Modern = Material 3",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MatrixGreen.copy(alpha = 0.7f),
                                        fontFamily = FontFamily.Monospace
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                                ) {
                                    AppTheme.entries.forEach { style ->
                                        FilterChip(
                                            selected = uiState.themeStyle == style,
                                            onClick = { viewModel.updateThemeStyle(style) },
                                            label = {
                                                Text(
                                                    when (style) {
                                                        AppTheme.CLASSIC -> "CLASSIC"
                                                        AppTheme.MODERN -> "MODERN"
                                                    },
                                                    fontFamily = FontFamily.Monospace,
                                                    fontSize = 12.sp
                                                )
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MatrixGreen.copy(alpha = 0.25f),
                                                selectedLabelColor = MatrixGreenBright,
                                                labelColor = MatrixGreen.copy(alpha = 0.7f),
                                                containerColor = MatrixSurface
                                            ),
                                            border = FilterChipDefaults.filterChipBorder(
                                                enabled = true,
                                                selected = uiState.themeStyle == style,
                                                borderColor = MatrixGreen.copy(alpha = 0.4f),
                                                selectedBorderColor = MatrixGreen
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        // ── LIGHT / DARK MODE ────────────────────────────────────────
                        SectionCardMatrix(title = ">> LIGHT / DARK MODE") {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    "Controls brightness across the whole app",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MatrixGreen.copy(alpha = 0.7f),
                                        fontFamily = FontFamily.Monospace
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                                ) {
                                    ThemePreference.entries.forEach { pref ->
                                        FilterChip(
                                            selected = uiState.themePreference == pref,
                                            onClick = { viewModel.updateThemePreference(pref) },
                                            label = {
                                                Text(
                                                    pref.name.uppercase(),
                                                    fontFamily = FontFamily.Monospace,
                                                    fontSize = 12.sp
                                                )
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MatrixGreen.copy(alpha = 0.25f),
                                                selectedLabelColor = MatrixGreenBright,
                                                labelColor = MatrixGreen.copy(alpha = 0.7f),
                                                containerColor = MatrixSurface
                                            ),
                                            border = FilterChipDefaults.filterChipBorder(
                                                enabled = true,
                                                selected = uiState.themePreference == pref,
                                                borderColor = MatrixGreen.copy(alpha = 0.4f),
                                                selectedBorderColor = MatrixGreen
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        // ── SWITCH INTERFACE (GUI MODE) ──────────────────────────────
                        SectionCardMatrix(title = ">> SWITCH INTERFACE") {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    "Change to a different UI experience",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MatrixGreen.copy(alpha = 0.7f),
                                        fontFamily = FontFamily.Monospace
                                    )
                                )
                                Spacer(modifier = Modifier.height(Spacing.sm))

                                // GUI1 - Classic
                                MatrixGuiModeRow(
                                    label = "CLASSIC (GUI1)",
                                    description = "Traditional activity-based interface",
                                    isCurrent = false,
                                    onClick = {
                                        Timber.d("GUI3 Appearance: Switching to GUI1")
                                        context.startActivity(
                                            Intent(context, MainActivity::class.java).apply {
                                                putExtra("businessId", businessId)
                                                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                            }
                                        )
                                    }
                                )

                                HorizontalDivider(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(MatrixGreen.copy(alpha = 0.15f))
                                )

                                // GUI2 - Modern
                                MatrixGuiModeRow(
                                    label = "MODERN (GUI2)",
                                    description = "Latest features · Better performance",
                                    isCurrent = false,
                                    onClick = {
                                        Timber.d("GUI3 Appearance: Switching to GUI2")
                                        context.startActivity(
                                            Intent(context, MainActivity::class.java).apply {
                                                putExtra("businessId", businessId)
                                                putExtra(
                                                    "selectedGui",
                                                    com.emul8r.bizap.ui.landing.GuiMode.GUI2.name
                                                )
                                                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                            }
                                        )
                                    }
                                )

                                HorizontalDivider(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(MatrixGreen.copy(alpha = 0.15f))
                                )

                                // GUI3 - Matrix (current)
                                MatrixGuiModeRow(
                                    label = "MATRIX (GUI3) 🟢",
                                    description = "Cyberpunk · Immersive · Premium",
                                    isCurrent = true,
                                    onClick = {}
                                )
                            }
                        }

                        // ── CUSTOM COLORS (link row) ─────────────────────────────────
                        GlowingMatrixButton(
                            text = "⚙  CUSTOM COLORS & THEME SETTINGS  →",
                            onClick = onThemeSettingsClick,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // ── OTHER SETTINGS ───────────────────────────────────────────
                        SectionCardMatrix(title = ">> OTHER SETTINGS") {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                MatrixNavLinkRow(
                                    label = "BUSINESS PROFILE",
                                    onClick = onBusinessProfileClick
                                )
                                HorizontalDivider(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(MatrixGreen.copy(alpha = 0.15f))
                                )
                                MatrixNavLinkRow(
                                    label = "BACKUP & RESTORE",
                                    onClick = onBackupRestoreClick
                                )
                                HorizontalDivider(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(MatrixGreen.copy(alpha = 0.15f))
                                )
                                MatrixNavLinkRow(
                                    label = "HELP",
                                    onClick = onHelpClick
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(Spacing.xxl))
                    }
                }
            }
        }
    }
}

// ── Private helpers ──────────────────────────────────────────────────────────

@Composable
private fun MatrixGuiModeRow(
    label: String,
    description: String,
    isCurrent: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (!isCurrent) Modifier.clickable(onClick = onClick) else Modifier
            )
            .padding(vertical = Spacing.sm, horizontal = Spacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = if (isCurrent) MatrixGreenBright else MatrixGreen,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                )
            )
            Text(
                description,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MatrixGreen.copy(alpha = 0.6f),
                    fontFamily = FontFamily.Monospace
                )
            )
        }
        if (isCurrent) {
            Text(
                "[ACTIVE]",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MatrixGreenBright,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            )
        } else {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MatrixGreen.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun MatrixNavLinkRow(
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = Spacing.sm, horizontal = Spacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium.copy(
                color = MatrixGreen,
                fontFamily = FontFamily.Monospace
            )
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = MatrixGreen.copy(alpha = 0.6f),
            modifier = Modifier.size(16.dp)
        )
    }
}





