package com.emul8r.bizap.ui.gui3.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emul8r.bizap.data.config.FeatureFlag
import com.emul8r.bizap.ui.gui3.components.GlowingMatrixButton
import com.emul8r.bizap.ui.gui3.components.MatrixCardPremium
import com.emul8r.bizap.ui.gui3.components.MatrixBackgroundWrapper
import com.emul8r.bizap.ui.gui3.theme.MatrixGreen
import com.emul8r.bizap.ui.gui3.theme.MatrixError
import com.emul8r.bizap.ui.gui3.util.AdaptivePerformanceManager
import com.emul8r.bizap.ui.gui3.util.Gui3ServiceEntryPoint
import com.emul8r.bizap.ui.gui3.util.MatrixBackgroundConfig
import com.emul8r.bizap.ui.gui3.util.PerformanceProfiler
import com.emul8r.bizap.ui.gui3.util.ScreenType
import com.emul8r.bizap.ui.theme.Spacing
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Matrix Debug Panel — Live effect tuning and profiling UI.
 *
 * Only visible in:
 * - Android Studio Debug builds (always)
 * - Production Beta builds (if MATRIX_DEBUG_PANEL enabled)
 *
 * 🆕 UX Improvements (Phase 3):
 * ✅ Changes persist across navigation (saved to SharedPreferences)
 * ✅ "Saved" feedback indicator with timestamp (top-right of header)
 * ✅ Debounced auto-save (300ms after slider stops moving)
 * ✅ Better labeling: "► LIVE TUNING — persists across screens"
 * ✅ Clear separation of user manual settings vs system auto-adaptation
 * ✅ Range hints under each slider (e.g., "0.1 (subtle) — 1.5 (intense)")
 *
 * Features:
 * - Toggle Canvas renderer on/off
 * - Live-tune rain density, glitch intensity, scanlines
 * - Toggle adaptive performance
 * - View real-time performance metrics
 * - Reset all parameters to defaults
 */
@Suppress("unused")
@Composable
fun MatrixDebugPanelScreenV3(
    businessId: Long = 1L,
    onDismiss: () -> Unit = {}
) {
    val appContext = LocalContext.current.applicationContext
    val entryPoint = remember(appContext) {
        EntryPointAccessors.fromApplication(appContext, Gui3ServiceEntryPoint::class.java)
    }

    val flagManager = remember(entryPoint) { entryPoint.featureFlagManager() }
    val adaptivePerf: AdaptivePerformanceManager = remember(entryPoint) { entryPoint.adaptivePerformanceManager() }
    val profiler: PerformanceProfiler = remember(entryPoint) { entryPoint.performanceProfiler() }

    val canvasEnabled by flagManager
        .observeFlag(FeatureFlag.MATRIX_CANVAS_RENDERER)
        .collectAsStateWithLifecycle(false)

    // ✅ FIX: No initialValue — StateFlow.collectAsStateWithLifecycle() (no arg)
    //    reads StateFlow.value synchronously, so adaptiveConfig is correct on frame 1.
    val adaptiveConfig by adaptivePerf.adaptiveConfig
        .collectAsStateWithLifecycle()

    // ✅ FIX: Initialise from getCurrentConfig() — a synchronous direct read of
    //    StateFlow.value — NOT from adaptiveConfig (which is a Compose State that
    //    was previously seeded from an async initial-value and would capture
    //    MatrixBackgroundConfig() defaults on the first composition frame instead
    //    of the persisted values).
    val initialConfig = remember(entryPoint) { adaptivePerf.getCurrentConfig() }
    var rainDensity by remember { mutableStateOf(initialConfig.rainDensity) }
    var rainSpeed by remember { mutableStateOf(initialConfig.rainSpeed) }
    var glitchIntensity by remember { mutableStateOf(initialConfig.glitchIntensity) }
    var scanlineAlpha by remember { mutableStateOf(initialConfig.scanlineAlpha) }
    var enableAdaptivePerf by remember { mutableStateOf(initialConfig.enableAdaptivePerf) }
    var debugLogging by remember { mutableStateOf(initialConfig.debugLogging) }

    // Derived: true when local slider values differ from last-saved StateFlow config
    val hasUnsavedChanges by remember {
        derivedStateOf {
            rainDensity != adaptiveConfig.rainDensity ||
            rainSpeed != adaptiveConfig.rainSpeed ||
            glitchIntensity != adaptiveConfig.glitchIntensity ||
            scanlineAlpha != adaptiveConfig.scanlineAlpha ||
            enableAdaptivePerf != adaptiveConfig.enableAdaptivePerf ||
            debugLogging != adaptiveConfig.debugLogging
        }
    }

    // ✅ NEW: Save state tracking for UX feedback
    var lastSaveTime by remember { mutableStateOf<Long?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // ✅ NEW: Debounce save operations (300ms after last change)
    var saveDebounceJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

    val performAutoSave = {
        saveDebounceJob?.cancel()
        saveDebounceJob = scope.launch {
            kotlinx.coroutines.delay(300) // Debounce: wait for user to stop changing slider
            isSaving = true
            try {
                // ✅ NEW: Update AdaptivePerformanceManager with new values (persists to prefs)
                val newConfig = adaptiveConfig.copy(
                    rainDensity = rainDensity,
                    rainSpeed = rainSpeed,
                    glitchIntensity = glitchIntensity,
                    scanlineAlpha = scanlineAlpha,
                    enableAdaptivePerf = enableAdaptivePerf,
                    debugLogging = debugLogging
                )
                adaptivePerf.updateConfig(newConfig)
                lastSaveTime = System.currentTimeMillis()
                Timber.i("✅ Effects saved: density=%.2f glitch=%.2f scanlines=%.3f".format(rainDensity, glitchIntensity, scanlineAlpha))
            } catch (e: Exception) {
                Timber.e(e, "❌ Failed to save effects settings")
            } finally {
                isSaving = false
            }
        }
    }

    // Performance metrics
    val metrics = profiler.snapshot()

    MatrixBackgroundWrapper(screenType = ScreenType.DEBUG) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Spacing.lg)
        ) {
            // ✅ NEW: Header with save status indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Spacing.md),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MATRIX DEBUG PANEL",
                    color = MatrixGreen,
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                // ✅ NEW: Save status indicator (Saving... → ✓ HH:MM:SS)
                SaveStatusIndicator(lastSaveTime = lastSaveTime, isSaving = isSaving)
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            // ═══════════════════════════════════════════════════════════════════════════════
            // FEATURE FLAGS
            // ═══════════════════════════════════════════════════════════════════════════════
            MatrixCardPremium(title = "FEATURE FLAGS") {
                // Canvas Renderer Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.md),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Canvas Renderer",
                            color = MatrixGreen,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "GPU-accelerated effects (system-wide)",
                            color = MatrixGreen.copy(alpha = 0.6f),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        )
                    }
                    Switch(
                        checked = canvasEnabled,
                        onCheckedChange = { enabled ->
                            scope.launch {
                                flagManager.setEnabled(FeatureFlag.MATRIX_CANVAS_RENDERER, enabled)
                                Timber.d("Canvas renderer: $enabled")
                            }
                        },
                        modifier = Modifier.graphicsLayer { scaleX = 1.2f; scaleY = 1.2f }
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.sm))

                // Debug Logging Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.md),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Debug Logging",
                            color = MatrixGreen,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Verbose Timber logs (logcat)",
                            color = MatrixGreen.copy(alpha = 0.6f),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        )
                    }
                    Switch(
                        checked = debugLogging,
                        onCheckedChange = {
                            debugLogging = it
                            performAutoSave()
                        },
                        modifier = Modifier.graphicsLayer { scaleX = 1.2f; scaleY = 1.2f }
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            // ═══════════════════════════════════════════════════════════════════════════════
            // ► LIVE TUNING (Manual user settings)
            // ═══════════════════════════════════════════════════════════════════════════════
            MatrixCardPremium(title = "► LIVE TUNING — persists across screens") {
                // Unsaved-changes banner
                if (hasUnsavedChanges) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        Text(
                            text = "⚠",
                            color = MatrixError,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "UNSAVED CHANGES — saving in 300ms",
                            color = MatrixError,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    Text(
                        text = "Adjust effects in real-time. Changes auto-save on release.",
                        color = MatrixGreen.copy(alpha = 0.7f),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(Spacing.md)
                    )
                }

                // Rain Density Slider
                Column(modifier = Modifier.fillMaxWidth().padding(Spacing.md)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = Spacing.sm),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Rain Density:",
                            color = MatrixGreen,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = String.format(Locale.US, "%.2f", rainDensity),
                            color = MatrixGreen,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(end = Spacing.sm)
                        )
                    }
                    Slider(
                        value = rainDensity,
                        onValueChange = {
                            rainDensity = it
                            performAutoSave()
                        },
                        onValueChangeFinished = { performAutoSave() },
                        valueRange = 0.1f..1.5f,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "0.1 (subtle) — 1.5 (intense particle rain)",
                        color = MatrixGreen.copy(alpha = 0.5f),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.md))

                // Rain Speed Slider
                Column(modifier = Modifier.fillMaxWidth().padding(Spacing.md)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = Spacing.sm),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Rain Speed:",
                            color = MatrixGreen,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = String.format(Locale.US, "%.2f", rainSpeed),
                            color = MatrixGreen,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(end = Spacing.sm)
                        )
                    }
                    Slider(
                        value = rainSpeed,
                        onValueChange = {
                            rainSpeed = it
                            performAutoSave()
                        },
                        onValueChangeFinished = { performAutoSave() },
                        valueRange = 0.5f..2.0f,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "0.5 (slow waterfall) — 2.0 (fast cascade)",
                        color = MatrixGreen.copy(alpha = 0.5f),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.md))

                // Glitch Intensity Slider
                Column(modifier = Modifier.fillMaxWidth().padding(Spacing.md)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = Spacing.sm),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Glitch Intensity:",
                            color = MatrixGreen,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = String.format(Locale.US, "%.2f", glitchIntensity),
                            color = MatrixGreen,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(end = Spacing.sm)
                        )
                    }
                    Slider(
                        value = glitchIntensity,
                        onValueChange = {
                            glitchIntensity = it
                            performAutoSave()
                        },
                        onValueChangeFinished = { performAutoSave() },
                        valueRange = 0f..1.0f,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "0.0 (off) — 1.0 (max analog TV artifacts)",
                        color = MatrixGreen.copy(alpha = 0.5f),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.md))

                // Scanline Alpha Slider
                Column(modifier = Modifier.fillMaxWidth().padding(Spacing.md)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = Spacing.sm),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Scanline Alpha:",
                            color = MatrixGreen,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = String.format(Locale.US, "%.3f", scanlineAlpha),
                            color = MatrixGreen,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(end = Spacing.sm)
                        )
                    }
                    Slider(
                        value = scanlineAlpha,
                        onValueChange = {
                            scanlineAlpha = it
                            performAutoSave()
                        },
                        onValueChangeFinished = { performAutoSave() },
                        valueRange = 0f..0.2f,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "0.0 (off) — 0.2 (strong CRT monitor effect)",
                        color = MatrixGreen.copy(alpha = 0.5f),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            // ═══════════════════════════════════════════════════════════════════════════════
            // PERFORMANCE METRICS
            // ═══════════════════════════════════════════════════════════════════════════════
            MatrixCardPremium(title = "PERFORMANCE METRICS (Read-only)") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.md)
                ) {
                    MetricRow(
                        label = "Avg Frame Time",
                        value = String.format(Locale.US, "%.2fms", metrics.avgFrameTimeMs)
                    )
                    MetricRow(
                        label = "Max Frame Time",
                        value = String.format(Locale.US, "%.2fms", metrics.maxFrameTimeMs)
                    )
                    MetricRow(
                        label = "Jank Frames",
                        value = metrics.jankCount.toString()
                    )
                    MetricRow(
                        label = "Jank Rate",
                        value = String.format(Locale.US, "%.1f%%", metrics.jankRate),
                        color = if (metrics.jankRate <= 5.0) MatrixGreen else Color.Red
                    )
                    MetricRow(
                        label = "Total Frames",
                        value = "${metrics.totalFramesRecorded}"
                    )
                    MetricRow(
                        label = "Device Model",
                        value = metrics.deviceModel
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            // ═══════════════════════════════════════════════════════════════════════════════
            // 🔧 ADAPTIVE PERFORMANCE (System auto-reduction)
            // ═══════════════════════════════════════════════════════════════════════════════
            MatrixCardPremium(title = "🔧 ADAPTIVE PERFORMANCE — Auto-Reduction on Jank") {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(Spacing.md)
                ) {
                    Text(
                        text = "When enabled, system automatically reduces effect intensity if 3+ consecutive frame drops detected.",
                        color = MatrixGreen.copy(alpha = 0.7f),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(bottom = Spacing.md)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.sm),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Enable Adaptive",
                                color = MatrixGreen,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "BETA: Currently in testing",
                                color = MatrixGreen.copy(alpha = 0.6f),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp
                            )
                        }
                        Switch(
                            checked = enableAdaptivePerf,
                            onCheckedChange = {
                                enableAdaptivePerf = it
                                performAutoSave()
                            },
                            modifier = Modifier.graphicsLayer { scaleX = 1.2f; scaleY = 1.2f }
                        )
                    }

                    if (enableAdaptivePerf) {
                        Spacer(modifier = Modifier.height(Spacing.md))
                        Text(
                            text = "✅ Adaptive performance: ENABLED\nSystem will monitor frame time and reduce density if jank detected.",
                            color = MatrixGreen.copy(alpha = 0.8f),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(Spacing.md)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            // ═══════════════════════════════════════════════════════════════════════════════
            // ACTIONS
            // ═══════════════════════════════════════════════════════════════════════════════
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                GlowingMatrixButton(
                    text = "RESET TO DEFAULTS",
                    onClick = {
                        val defaultConfig = MatrixBackgroundConfig()
                        rainDensity = defaultConfig.rainDensity
                        rainSpeed = defaultConfig.rainSpeed
                        glitchIntensity = defaultConfig.glitchIntensity
                        scanlineAlpha = defaultConfig.scanlineAlpha
                        enableAdaptivePerf = defaultConfig.enableAdaptivePerf
                        debugLogging = defaultConfig.debugLogging
                        // ✅ NEW: Persist reset to prefs
                        adaptivePerf.reset()
                        profiler.reset()
                    },
                    modifier = Modifier.weight(1f)
                )

                GlowingMatrixButton(
                    text = "CLOSE",
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(Spacing.lg))
        }
    }
}

/**
 * ✅ NEW: Save status indicator showing last save time and status.
 * - Saving: Shows spinner + "Saving..." text
 * - Saved: Shows checkmark + "HH:MM:SS" timestamp
 */
@Composable
private fun SaveStatusIndicator(
    lastSaveTime: Long?,
    isSaving: Boolean
) {
    if (isSaving) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .height(12.sp.value.dp)
                    .graphicsLayer { scaleX = 0.7f; scaleY = 0.7f },
                color = MatrixGreen,
                strokeWidth = 1.5.dp
            )
            Text(
                text = "Saving...",
                color = MatrixGreen.copy(alpha = 0.8f),
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp
            )
        }
    } else if (lastSaveTime != null) {
        val timeText = SimpleDateFormat("HH:mm:ss", Locale.US).format(lastSaveTime)
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "✓",
                color = MatrixGreen,
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = timeText,
                color = MatrixGreen.copy(alpha = 0.7f),
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp
            )
        }
    }
}

@Composable
private fun MetricRow(
    label: String,
    value: String,
    color: Color = MatrixGreen
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = color.copy(alpha = 0.8f),
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp
        )
        Text(
            text = value,
            color = color,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            modifier = Modifier.padding(end = Spacing.md)
        )
    }
}

