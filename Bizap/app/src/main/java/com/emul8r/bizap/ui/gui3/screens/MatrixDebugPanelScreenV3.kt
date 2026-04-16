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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emul8r.bizap.data.config.FeatureFlag
import com.emul8r.bizap.ui.gui3.components.GlowingMatrixButton
import com.emul8r.bizap.ui.gui3.components.MatrixCardPremium
import com.emul8r.bizap.ui.gui3.components.MatrixBackgroundWrapper
import com.emul8r.bizap.ui.gui3.theme.MatrixGreen
import com.emul8r.bizap.ui.gui3.util.AdaptivePerformanceManager
import com.emul8r.bizap.ui.gui3.util.Gui3ServiceEntryPoint
import com.emul8r.bizap.ui.gui3.util.MatrixBackgroundConfig
import com.emul8r.bizap.ui.gui3.util.PerformanceProfiler
import com.emul8r.bizap.ui.gui3.util.ScreenType
import com.emul8r.bizap.ui.theme.Spacing
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Locale

/**
 * Matrix Debug Panel — Live effect tuning and profiling UI.
 *
 * Only visible in:
 * - Android Studio Debug builds (always)
 * - Production Beta builds (if MATRIX_DEBUG_PANEL enabled)
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

    val adaptiveConfig by adaptivePerf.adaptiveConfig
        .collectAsStateWithLifecycle(MatrixBackgroundConfig())

    // Local UI state for controls
    var rainDensity by remember { mutableStateOf(adaptiveConfig.rainDensity) }
    var rainSpeed by remember { mutableStateOf(adaptiveConfig.rainSpeed) }
    var glitchIntensity by remember { mutableStateOf(adaptiveConfig.glitchIntensity) }
    var scanlineAlpha by remember { mutableStateOf(adaptiveConfig.scanlineAlpha) }
    var enableAdaptivePerf by remember { mutableStateOf(adaptiveConfig.enableAdaptivePerf) }
    var debugLogging by remember { mutableStateOf(adaptiveConfig.debugLogging) }

    // Performance metrics
    val metrics = profiler.snapshot()

    val scope = rememberCoroutineScope()

    MatrixBackgroundWrapper(screenType = ScreenType.DEBUG) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Spacing.lg)
        ) {
            // Header
            Text(
                text = "MATRIX DEBUG PANEL",
                color = MatrixGreen,
                fontSize = 18.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(bottom = Spacing.md)
            )

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
                    Text(
                        text = "Canvas Renderer",
                        color = MatrixGreen,
                        fontFamily = FontFamily.Monospace
                    )
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
                    Text(
                        text = "Debug Logging",
                        color = MatrixGreen,
                        fontFamily = FontFamily.Monospace
                    )
                    Switch(
                        checked = debugLogging,
                        onCheckedChange = { debugLogging = it },
                        modifier = Modifier.graphicsLayer { scaleX = 1.2f; scaleY = 1.2f }
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            // ═══════════════════════════════════════════════════════════════════════════════
            // EFFECT TUNING
            // ═══════════════════════════════════════════════════════════════════════════════
            MatrixCardPremium(title = "EFFECT TUNING") {
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
                            fontSize = 12.sp
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
                        onValueChange = { rainDensity = it },
                        valueRange = 0.1f..1.5f,
                        modifier = Modifier.fillMaxWidth()
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
                            fontSize = 12.sp
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
                        onValueChange = { rainSpeed = it },
                        valueRange = 0.5f..2.0f,
                        modifier = Modifier.fillMaxWidth()
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
                            fontSize = 12.sp
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
                        onValueChange = { glitchIntensity = it },
                        valueRange = 0f..1.0f,
                        modifier = Modifier.fillMaxWidth()
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
                            fontSize = 12.sp
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
                        onValueChange = { scanlineAlpha = it },
                        valueRange = 0f..0.2f,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            // ═══════════════════════════════════════════════════════════════════════════════
            // PERFORMANCE
            // ═══════════════════════════════════════════════════════════════════════════════
            MatrixCardPremium(title = "PERFORMANCE METRICS") {
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
            // ADAPTIVE PERFORMANCE
            // ═══════════════════════════════════════════════════════════════════════════════
            MatrixCardPremium(title = "ADAPTIVE PERFORMANCE") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.md),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Adaptive Perf (Beta)",
                        color = MatrixGreen,
                        fontFamily = FontFamily.Monospace
                    )
                    Switch(
                        checked = enableAdaptivePerf,
                        onCheckedChange = { enableAdaptivePerf = it },
                        modifier = Modifier.graphicsLayer { scaleX = 1.2f; scaleY = 1.2f }
                    )
                }

                if (enableAdaptivePerf) {
                    Text(
                        text = "Auto-reduces effects on jank detection",
                        color = MatrixGreen.copy(alpha = 0.7f),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(Spacing.md)
                    )
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
                    text = "RESET",
                    onClick = {
                        rainDensity = 0.8f
                        rainSpeed = 1.0f
                        glitchIntensity = 0.5f
                        scanlineAlpha = 0.05f
                        enableAdaptivePerf = false
                        debugLogging = false
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









