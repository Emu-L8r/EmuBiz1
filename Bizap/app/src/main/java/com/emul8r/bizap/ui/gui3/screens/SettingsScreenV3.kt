package com.emul8r.bizap.ui.gui3.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.EntryPointAccessors
import com.emul8r.bizap.data.config.FeatureFlag
import com.emul8r.bizap.ui.gui3.components.MatrixButton
import com.emul8r.bizap.ui.gui3.components.SectionCardMatrix
import com.emul8r.bizap.ui.gui3.components.GlowingMatrixButton
import com.emul8r.bizap.ui.gui3.components.DetailRowMatrix
import com.emul8r.bizap.ui.gui3.components.MatrixBackgroundWrapper
import com.emul8r.bizap.ui.gui3.theme.*
import com.emul8r.bizap.ui.gui3.navigation.ScreenV3
import com.emul8r.bizap.ui.gui3.util.ScreenType
import com.emul8r.bizap.ui.gui3.util.Gui3ServiceEntryPoint
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

    val appContext = LocalContext.current.applicationContext
    val flagManager = remember(appContext) {
        EntryPointAccessors.fromApplication(appContext, Gui3ServiceEntryPoint::class.java)
            .featureFlagManager()
    }
    val scope = rememberCoroutineScope()

    // ✅ NEW: Inject AppStateViewModel for GUI mode persistence
    val appStateViewModel = androidx.hilt.navigation.compose.hiltViewModel<com.emul8r.bizap.ui.state.AppStateViewModel>()

    val canvasEnabled by flagManager
        .observeFlag(FeatureFlag.MATRIX_CANVAS_RENDERER)
        .collectAsStateWithLifecycle(false)

    // ✅ NEW: Observe individual effect flags
    val rainEffectEnabled by flagManager
        .observeFlag(FeatureFlag.EFFECT_RAIN)
        .collectAsStateWithLifecycle(true)

    val glitchEffectEnabled by flagManager
        .observeFlag(FeatureFlag.EFFECT_GLITCH)
        .collectAsStateWithLifecycle(true)

    val scanlinesEffectEnabled by flagManager
        .observeFlag(FeatureFlag.EFFECT_SCANLINES)
        .collectAsStateWithLifecycle(true)

    MatrixBackgroundWrapper(screenType = ScreenType.SETTINGS) {
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
        containerColor = Color.Transparent
     ) { paddingValues ->
         Column(
             modifier = Modifier
                 .fillMaxSize()
                 .verticalScroll(rememberScrollState())
                 .padding(paddingValues)
                 .padding(Spacing.lg),
             verticalArrangement = Arrangement.spacedBy(Spacing.lg)
         ) {
            // ============= APPEARANCE SECTION =============
            SectionCardMatrix(title = ">> APPEARANCE") {
                // Quick link to dedicated AppAppearance screen
                GlowingMatrixButton(
                    text = "⚙  THEME & DISPLAY SETTINGS  →",
                    onClick = { navController.navigate(ScreenV3.AppAppearance(businessId)) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(Spacing.md))
                HorizontalDivider(modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MatrixGreen.copy(alpha = 0.2f))
                )
                Spacer(modifier = Modifier.height(Spacing.lg))

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
                                    // ✅ NEW: Use AppStateViewModel to persist + auto-navigate
                                    appStateViewModel.selectGui(GuiMode.GUI1)
                                    navController.context.startActivity(
                                        android.content.Intent(navController.context, com.emul8r.bizap.MainActivity::class.java).apply {
                                            putExtra("businessId", businessId)
                                            putExtra("selectedGui", GuiMode.GUI1.name)
                                            addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                        }
                                    )
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
                                // ✅ NEW: Use AppStateViewModel to persist + auto-navigate
                                appStateViewModel.selectGui(GuiMode.GUI1)
                                navController.context.startActivity(
                                    android.content.Intent(navController.context, com.emul8r.bizap.MainActivity::class.java).apply {
                                        putExtra("businessId", businessId)
                                        putExtra("selectedGui", GuiMode.GUI1.name)
                                        addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                    }
                                )
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
                                    // ✅ NEW: Use AppStateViewModel to persist + auto-navigate
                                    appStateViewModel.selectGui(GuiMode.GUI2)
                                    navController.context.startActivity(
                                        android.content.Intent(navController.context, com.emul8r.bizap.MainActivity::class.java).apply {
                                            putExtra("businessId", businessId)
                                            putExtra("selectedGui", GuiMode.GUI2.name)
                                            addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                        }
                                    )
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
                                // ✅ NEW: Use AppStateViewModel to persist + auto-navigate
                                appStateViewModel.selectGui(GuiMode.GUI2)
                                navController.context.startActivity(
                                    android.content.Intent(navController.context, com.emul8r.bizap.MainActivity::class.java).apply {
                                        putExtra("businessId", businessId)
                                        putExtra("selectedGui", GuiMode.GUI2.name)
                                        addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                    }
                                )
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
                            "Matrix (GUI3) - Premium Γ£¿",
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

                    // ✅ NEW: Canvas Renderer Toggle
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

                    Spacer(modifier = Modifier.height(Spacing.sm))

                    Text(
                        "Individual Effects",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MatrixGreen.copy(alpha = 0.7f),
                            fontFamily = FontFamily.Monospace
                        )
                    )

                    // ✅ NEW: Digital Rain Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.md),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Digital Rain",
                            color = MatrixGreen,
                            fontFamily = FontFamily.Monospace
                        )
                        Switch(
                            checked = rainEffectEnabled,
                            onCheckedChange = { enabled ->
                                scope.launch {
                                    flagManager.setEnabled(FeatureFlag.EFFECT_RAIN, enabled)
                                }
                            }
                        )
                    }

                    // ✅ NEW: Glitch Effect Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.md),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Glitch Effect",
                            color = MatrixGreen,
                            fontFamily = FontFamily.Monospace
                        )
                        Switch(
                            checked = glitchEffectEnabled,
                            onCheckedChange = { enabled ->
                                scope.launch {
                                    flagManager.setEnabled(FeatureFlag.EFFECT_GLITCH, enabled)
                                }
                            }
                        )
                    }

                    // ✅ NEW: Scanlines Effect Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.md),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Scanline Effect",
                            color = MatrixGreen,
                            fontFamily = FontFamily.Monospace
                        )
                        Switch(
                            checked = scanlinesEffectEnabled,
                            onCheckedChange = { enabled ->
                                scope.launch {
                                    flagManager.setEnabled(FeatureFlag.EFFECT_SCANLINES, enabled)
                                }
                            }
                        )
                    }

                    // Effects Panel Link
                    val context = LocalContext.current

                    GlowingMatrixButton(
                        text = "⚙ EFFECTS PANEL",
                        onClick = {
                            navController.navigate(ScreenV3.MatrixDebugPanel(businessId))
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // ============= VAULT & DOCUMENTS =============
            SectionCardMatrix(title = ">> VAULT & DOCUMENTS") {
                Column(
                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Access your searchable document vault and archive summaries.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MatrixGreen.copy(alpha = 0.7f)
                        )
                    )

                    GlowingMatrixButton(
                        text = "≡ƒôü OPEN VAULT",
                        onClick = { navController.navigate(ScreenV3.Vault(businessId)) },
                        modifier = Modifier.fillMaxWidth(),
                        isHighlight = true,
                        enabled = true
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

}






