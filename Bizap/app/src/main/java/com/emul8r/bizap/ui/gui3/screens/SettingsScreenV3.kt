package com.emul8r.bizap.ui.gui3.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emul8r.bizap.data.config.FeatureFlag
import com.emul8r.bizap.data.config.FeatureFlagManager
import com.emul8r.bizap.ui.gui3.components.*
import com.emul8r.bizap.ui.gui3.theme.*
import com.emul8r.bizap.ui.gui3.navigation.ScreenV3
import com.emul8r.bizap.ui.landing.GuiMode
import com.emul8r.bizap.ui.theme.Spacing
import kotlinx.coroutines.launch

/**
 * Settings Screen V3 (Matrix Edition)
 *
 * Configuration and preferences screen with Matrix styling:
 * - GUI mode selection
 * - Theme settings
 * - Business settings
 * - User preferences
 * - About & Help
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenV3(
    businessId: Long,
    navController: NavHostController,
    onGuiModeChanged: (GuiMode) -> Unit = {}
) {
    var selectedGuiMode by remember { mutableStateOf(GuiMode.GUI3) }
    var selectedFontSize by remember { mutableStateOf("normal") }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var invoiceRemindersEnabled by remember { mutableStateOf(true) }
    var overdueAlertsEnabled by remember { mutableStateOf(true) }

    val flagManager: FeatureFlagManager = hiltViewModel()
    val scope = rememberCoroutineScope()

    val canvasEnabled by flagManager
        .observeFlag(FeatureFlag.MATRIX_CANVAS_RENDERER)
        .collectAsStateWithLifecycle(false)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        ">> SETTINGS",
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
        containerColor = MatrixBlack.copy(alpha = 0.8f)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MatrixBlack)
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            // ============= APPEARANCE SECTION =============
            SectionCardMatrix(title = ">> APPEARANCE") {
                // GUI Mode Selection
                Column(
                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "GUI Mode",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MatrixGreen.copy(alpha = 0.8f),
                            fontFamily = FontFamily.Monospace
                        )
                    )

                    // GUI1 Option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .selectable(
                                selected = selectedGuiMode == GuiMode.GUI1,
                                onClick = {
                                    selectedGuiMode = GuiMode.GUI1
                                    onGuiModeChanged(GuiMode.GUI1)
                                }
                            )
                            .padding(horizontal = Spacing.md),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                    ) {
                        RadioButton(
                            selected = selectedGuiMode == GuiMode.GUI1,
                            onClick = {
                                selectedGuiMode = GuiMode.GUI1
                                onGuiModeChanged(GuiMode.GUI1)
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MatrixGreen,
                                unselectedColor = MatrixGreen.copy(alpha = 0.5f)
                            )
                        )
                        Text(
                            "Classic (GUI1)",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MatrixGreen
                            )
                        )
                    }

                    // GUI2 Option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .selectable(
                                selected = selectedGuiMode == GuiMode.GUI2,
                                onClick = {
                                    selectedGuiMode = GuiMode.GUI2
                                    onGuiModeChanged(GuiMode.GUI2)
                                }
                            )
                            .padding(horizontal = Spacing.md),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                    ) {
                        RadioButton(
                            selected = selectedGuiMode == GuiMode.GUI2,
                            onClick = {
                                selectedGuiMode = GuiMode.GUI2
                                onGuiModeChanged(GuiMode.GUI2)
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MatrixGreen,
                                unselectedColor = MatrixGreen.copy(alpha = 0.5f)
                            )
                        )
                        Text(
                            "Modern (GUI2)",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MatrixGreen
                            )
                        )
                    }

                    // GUI3 Option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .selectable(
                                selected = selectedGuiMode == GuiMode.GUI3,
                                onClick = {
                                    selectedGuiMode = GuiMode.GUI3
                                    onGuiModeChanged(GuiMode.GUI3)
                                }
                            )
                            .padding(horizontal = Spacing.md),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                    ) {
                        RadioButton(
                            selected = selectedGuiMode == GuiMode.GUI3,
                            onClick = {
                                selectedGuiMode = GuiMode.GUI3
                                onGuiModeChanged(GuiMode.GUI3)
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MatrixGreen,
                                unselectedColor = MatrixGreen.copy(alpha = 0.5f)
                            )
                        )
                        Text(
                            "Matrix (GUI3) - Premium ✨",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MatrixGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.lg))
                HorizontalDivider(modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MatrixGreen.copy(alpha = 0.2f))
                )
                Spacer(modifier = Modifier.height(Spacing.lg))

                // Font Size Selection
                Column(
                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Font Size",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MatrixGreen.copy(alpha = 0.8f),
                            fontFamily = FontFamily.Monospace
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        listOf("small" to "Small", "normal" to "Normal", "large" to "Large").forEach { (value, label) ->
                            MatrixButton(
                                text = label,
                                onClick = { selectedFontSize = value },
                                modifier = Modifier.weight(1f),
                                isHighlight = selectedFontSize == value
                            )
                        }
                    }
                }
            }

            // ============= NOTIFICATIONS SECTION =============
            SectionCardMatrix(title = ">> NOTIFICATIONS") {
                Column(
                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Master Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clickable {
                                notificationsEnabled = !notificationsEnabled
                            }
                            .padding(horizontal = Spacing.md),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Enable Notifications",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MatrixGreen
                            )
                        )
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { notificationsEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MatrixGreen,
                                checkedTrackColor = MatrixGreen.copy(alpha = 0.3f)
                            )
                        )
                    }

                    if (notificationsEnabled) {
                        Spacer(modifier = Modifier.height(Spacing.sm))

                        // Invoice Reminders
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .clickable { invoiceRemindersEnabled = !invoiceRemindersEnabled }
                                .padding(start = Spacing.lg, end = Spacing.md),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Invoice Reminders",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MatrixGreen.copy(alpha = 0.9f)
                                )
                            )
                            Switch(
                                checked = invoiceRemindersEnabled,
                                onCheckedChange = { invoiceRemindersEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MatrixGreen,
                                    checkedTrackColor = MatrixGreen.copy(alpha = 0.3f)
                                )
                            )
                        }

                        // Overdue Alerts
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .clickable { overdueAlertsEnabled = !overdueAlertsEnabled }
                                .padding(start = Spacing.lg, end = Spacing.md),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Overdue Alerts",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MatrixGreen.copy(alpha = 0.9f)
                                )
                            )
                            Switch(
                                checked = overdueAlertsEnabled,
                                onCheckedChange = { overdueAlertsEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MatrixGreen,
                                    checkedTrackColor = MatrixGreen.copy(alpha = 0.3f)
                                )
                            )
                        }
                    }
                }
            }

            // ============= BUSINESS SETTINGS SECTION =============
            SectionCardMatrix(title = ">> BUSINESS") {
                Column(
                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    DetailRowMatrix(
                        label = "Business Name",
                        value = "Your Company Name"
                    )

                    DetailRowMatrix(
                        label = "Industry",
                        value = "Software Development"
                    )

                    DetailRowMatrix(
                        label = "Currency",
                        value = "USD ($)"
                    )

                    DetailRowMatrix(
                        label = "Tax ID",
                        value = "12-3456789"
                    )
                }
            }

            // ============= ABOUT SECTION =============
            SectionCardMatrix(title = ">> ABOUT") {
                Column(
                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    DetailRowMatrix(
                        label = "App Version",
                        value = "1.0.0"
                    )

                    DetailRowMatrix(
                        label = "Build",
                        value = "DEBUG (2026.04.13)"
                    )

                    DetailRowMatrix(
                        label = "Last Updated",
                        value = "April 13, 2026"
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.lg))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    MatrixButton(
                        text = "FEEDBACK",
                        onClick = { /* Send feedback */ },
                        modifier = Modifier.weight(1f)
                    )

                    MatrixButton(
                        text = "HELP",
                        onClick = { /* Open help */ },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ============= VISUAL EFFECTS SECTION =============
            SectionCardMatrix(title = ">> VISUAL EFFECTS") {
                Column(
                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Enhanced Background (GPU-Accelerated)",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MatrixGreen.copy(alpha = 0.8f),
                            fontFamily = FontFamily.Monospace
                        )
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.md),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Canvas Renderer",
                            color = MatrixGreen,
                            fontFamily = FontFamily.Monospace
                        )
                        Switch(
                            checked = canvasEnabled,
                            onCheckedChange = { enabled ->
                                scope.launch {
                                    flagManager.setEnabled(FeatureFlag.MATRIX_CANVAS_RENDERER, enabled)
                                }
                            }
                        )
                    }

                    // Debug Panel Link (Debug builds only)
                    val context = LocalContext.current
                    val isDebugBuild = remember {
                        try {
                            context.packageManager.getApplicationInfo(context.packageName, 0).flags and 2 != 0
                        } catch (e: Exception) {
                            false
                        }
                    }

                    if (isDebugBuild) {
                        GlowingMatrixButton(
                            text = "[DEBUG] OPEN EFFECTS PANEL",
                            onClick = {
                                navController.navigate(ScreenV3.MatrixDebugPanel(businessId))
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // ============= VAULT & DOCUMENTS =============
            SectionCardMatrix(title = ">> VAULT & DOCUMENTS") {
                Column(
                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Access your document vault (Coming Soon)",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MatrixGreen.copy(alpha = 0.7f)
                        )
                    )

                    GlowingMatrixButton(
                        text = "📁 OPEN VAULT",
                        onClick = { /* TODO: Implement in next phase */ },
                        modifier = Modifier.fillMaxWidth(),
                        isHighlight = true,
                        enabled = false  // Disabled for now - vault integration coming
                    )
                }
            }

            // ============= DANGER ZONE =============
            SectionCardMatrix(title = ">> DANGER ZONE") {
                Column(
                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Destructive actions. Be careful!",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MatrixError.copy(alpha = 0.8f)
                        )
                    )

                    MatrixButton(
                        text = "CLEAR CACHE",
                        onClick = { /* Clear cache */ },
                        modifier = Modifier.fillMaxWidth(),
                        isHighlight = false
                    )

                    MatrixButton(
                        text = "RESET APP",
                        onClick = { /* Reset app with confirmation */ },
                        modifier = Modifier.fillMaxWidth(),
                        isHighlight = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.xxl))
        }
    }
}


