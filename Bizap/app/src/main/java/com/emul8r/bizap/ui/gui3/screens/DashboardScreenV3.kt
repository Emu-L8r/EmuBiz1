@file:Suppress("unused")

package com.emul8r.bizap.ui.gui3.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import com.emul8r.bizap.ui.gui3.components.*
import com.emul8r.bizap.ui.gui3.navigation.ScreenV3
import com.emul8r.bizap.ui.gui3.theme.*
import com.emul8r.bizap.ui.theme.Spacing
import kotlinx.coroutines.delay
import timber.log.Timber
import androidx.compose.foundation.shape.RoundedCornerShape

/**
 * Dashboard Screen V3 (Matrix Edition)
 *
 * Main dashboard with Matrix styling:
 * - Green on dark aesthetic
 * - Bordered cards with glow
 * - Monospace data display
 * - Smooth animations
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreenV3(
    businessId: Long,
    navController: NavHostController,
    onSwitchToGui1: () -> Unit = {},
    onSwitchToGui2: () -> Unit = {}
) {
    println("🟤A DashboardScreenV3 @Composable function body START - businessId=$businessId")
    Timber.d("🟤 DashboardScreenV3 @Composable function body START - businessId=$businessId")

    DashboardScreenV3Content(
        businessId = businessId,
        navController = navController,
        onSwitchToGui1 = onSwitchToGui1,
        onSwitchToGui2 = onSwitchToGui2
    )

    println("🟤 DashboardScreenV3 COMPOSABLE FUNCTION ENDING")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardScreenV3Content(
    businessId: Long,
    navController: NavHostController,
    onSwitchToGui1: () -> Unit,
    onSwitchToGui2: () -> Unit
) {
    println("🟤B DashboardScreenV3Content START")
    var showOverflowMenu by remember { mutableStateOf(false) }

    // Blinking status dot — 800ms toggle, zero animation overhead
    var dotVisible by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(800L)
            dotVisible = !dotVisible
        }
    }
    val dot = if (dotVisible) "●" else "·"

    Box(modifier = Modifier.fillMaxSize()) {
        // Background animation layer - FIRST layer, at z=0
        MatrixBackground(
            intensity = 1.2f,
            modifier = Modifier
                .fillMaxSize()
                .zIndex(0f)
        ) {
            println("🟤C MatrixBackground rendering")
        }

        // CONTENT LAYER: Pure Box-based layout (NO Material3 Scaffold)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(1f)
        ) {
            // ===== TRANSPARENT GUI3 TOP STRIP =====
            // Keep the action controls, but remove the opaque boxed overlay that was
            // producing the harsh square at the top of the dashboard.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "$dot MATRIX DASHBOARD $dot",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            color = MatrixGreenBright,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            letterSpacing = 2.sp
                        )
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                Timber.d("Dashboard: opening settings from top bar")
                                navController.navigate(ScreenV3.Settings(businessId))
                            },
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(MatrixSurface.copy(alpha = 0.45f))
                                .border(1.dp, MatrixGreen.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = MatrixGreen)
                        }

                        Box {
                            IconButton(
                                onClick = { showOverflowMenu = true },
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MatrixSurface.copy(alpha = 0.45f))
                                    .border(1.dp, MatrixGreen.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                            ) {
                                Icon(Icons.Default.MoreVert, contentDescription = "More actions", tint = MatrixGreen)
                            }

                            DropdownMenu(
                                expanded = showOverflowMenu,
                                onDismissRequest = { showOverflowMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(">> SWITCH EXPERIENCE", color = MatrixGreen.copy(alpha = 0.65f)) },
                                    onClick = {},
                                    enabled = false
                                )

                                DropdownMenuItem(
                                    text = { Text("GUI1 · CLASSIC") },
                                    onClick = {
                                        showOverflowMenu = false
                                        Timber.d("Dashboard menu: switch to GUI1")
                                        onSwitchToGui1()
                                    }
                                )

                                DropdownMenuItem(
                                    text = { Text("GUI2 · MODERN") },
                                    onClick = {
                                        showOverflowMenu = false
                                        Timber.d("Dashboard menu: switch to GUI2")
                                        onSwitchToGui2()
                                    }
                                )

                                DropdownMenuItem(
                                    text = { Text("GUI3 · ACTIVE") },
                                    onClick = { showOverflowMenu = false },
                                    enabled = false
                                )

                                HorizontalDivider()

                                DropdownMenuItem(
                                    text = { Text("⚙ OPEN SETTINGS") },
                                    onClick = {
                                        showOverflowMenu = false
                                        Timber.d("Dashboard menu: settings")
                                        navController.navigate(ScreenV3.Settings(businessId))
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Settings, contentDescription = null, tint = MatrixGreen)
                                    }
                                )

                                DropdownMenuItem(
                                    text = { Text("🔐 SECURITY VAULT") },
                                    onClick = {
                                        showOverflowMenu = false
                                        Timber.d("Dashboard menu: security vault")
                                        navController.navigate(ScreenV3.SecurityVault(businessId))
                                    }
                                )

                                DropdownMenuItem(
                                    text = { Text("📁 DOCUMENT VAULT") },
                                    onClick = {
                                        showOverflowMenu = false
                                        Timber.d("Dashboard menu: document vault")
                                        navController.navigate(ScreenV3.Vault(businessId))
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.sm))

                HorizontalDivider(
                    color = MatrixGreen.copy(alpha = 0.25f),
                    thickness = 1.dp
                )

                // Terminal node metadata strip — contextual system info, no extra state
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "NODE_ID:BZ-${businessId.toString().padStart(4, '0')}  SYSLINK:ACTIVE",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            color = MatrixGreen.copy(alpha = 0.42f),
                            fontSize = 9.sp, letterSpacing = 1.1.sp
                        )
                    )
                    Text(
                        text = "SECURE ■  LIVE FEED",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            color = MatrixGreen.copy(alpha = 0.42f),
                            fontSize = 9.sp, letterSpacing = 1.1.sp
                        )
                    )
                }

                // Scrolling status ticker
                SystemStatusTicker(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 3.dp)
                )
            }

            // ===== SCROLLABLE CONTENT AREA (Pure Box/Column, NO Scaffold) =====
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.lg)
            ) {
                // Welcome Section
                WelcomeMatrixBanner()

                // Key Metrics
                KeyMetricsMatrix()

                // Quick Actions
                QuickActionsMatrix(navController, businessId)

                // Recent Activity
                RecentActivityMatrix()

                // GUI Switcher (for testing)
                Spacer(modifier = Modifier.height(Spacing.lg))
                BottomTerminalArtMatrix()

                Spacer(modifier = Modifier.height(Spacing.lg))
                GuiSwitcherMatrix(
                    onSwitchToGui1 = onSwitchToGui1,
                    onSwitchToGui2 = onSwitchToGui2
                )

                Spacer(modifier = Modifier.height(96.dp))
            }
        }

        // DEBUG: Visible text to confirm execution
        Text(
            "🟡 GUI3 ACTIVE 🟡",
            modifier = Modifier
                .zIndex(3f)
                .padding(16.dp),
            color = MatrixGreenBright,
            fontSize = 12.sp
        )
    }
}

// Braille skull/face — rendered alongside money bag in the terminal banner.
private val SKULL_ASCII = """
 ⢠⠊⣉⠒⠤⢀⡀          ⡐⢁⠴⢜⢄
 ⡎⢸  ⠉⠐⠢⢌⠑⢄    ⡸  ⡆    ⠣⠱⡀
 ⡇⢸        ⣀⠗  ⠉⠉⠁  ⠙⠢⠤⡀⢃⢱
 ⡇⠘⣄⢀⠔⠉                    ⠈⠁⠘⡄
 ⢇    ⠁                              ⠘⡄
 ⢸            ⢀⣀⣀⡀        ⢀⣀⣀⡀  ⢣
 ⡸        ⢴⣾⡿⠿⠽⠇        ⠘⠛⠛⠛  ⠈⢄
⠰⡁              ⢠⠒⠢⡀⠈⠒⠊      ⡠⢄  ⡘
 ⠱⣀          ⢀⠜    ⠇        ⢀⠔⠁  ⡏
     ⠑⠤⢄⣀⠔⠁    ⡜        ⠊⠁  ⢀⠜""".trimIndent()

// Mushroom cloud / core meltdown — _ used as space placeholder.
private val MUSHROOM_ASCII = """
+880________________________________
_++88________________________________
_++88________________________________
__+880_________________________++++_
__+888________________________+888+_
__++880______________________+888+__
__++888_____+++88__________+++88__
__++8888__+++8880++88____+++88___
__+++8888+++8880++8888__++888____
___++888++8888+++888888++888_____
___++88++8888++8888888++888______
___++++++888888888888888888______
____++++++88888888888888888______
____++++++++000888888888888______
_____+++++++000088888888888______
______+++++++00088888888888______
_______+++++++088888888888_______
_______+++++++088888888888_______
________+++++++8888888888________
________+++++++0088888888________
________++++++0088888888_________
________+++++0008888888__________""".trimIndent().replace('_', ' ')

// ASCII money-bag art — @ used as $ placeholder to avoid Kotlin string interpolation conflicts.
// replace('@', '$') restores the original characters at runtime.
private val MONEY_BAG_ASCII = """
         @@@@@@@@@@@@@@@@@@@@@@@
       @@@@   @@@@@@@@@@@@@@@@@@@@@
      @@@@      @@@@@@@@@@@@@@@@@@@@@@
    @@@@@        @@@@@@@@@@@@@@@@@@@@@@@
   @@@@@          @@@@@@@@@@@@@@@@@@@@@@@
  @@@@@            @@@@@@@@@@@@@@@@@@@@@@@
 @@@@@@            @@@@@@@@@@@@@@@@@@@@@@@@
 @@@@@@           @@@@@@@@@@@           @@@@@@
 @@...@@@@@       @@@.@@@.@@@         @@@@@
 @@@@@@@@      @@@@   @   @@@@      @@@@@@@@
 @@@@@@@@@@@@@@@@@   @@@   @@@@@@@@@@@@@@@@@
 @@@.@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@.o@@
 @@@  @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@  @@@
  @@@  @'@@@@@@@@@@@@@@@@@@@@@@@@@@@@@  o@@@
  '@o  @@  @@'@@@@@@@@@@@@@'@@  @@       o@@
    @@o@    @@  '@'@@'@@'@@  @@      @   o@@
     @@@o@  @    @@   @@   @@     @@  o@
      '@@@@O@    @@    @@   @@     o@@@
         '@o@@   @@   @@   @@   o@@@
           '@@@@o@o@o@o@o@o@o@o@@@@""".trimIndent().replace('@', '$')

/**
 * Welcome Banner — skull art left, ASCII money-bag right.
 * Scan stripe sweeps across art via global pulse. Tagline types in on first render.
 */
@Composable
fun WelcomeMatrixBanner() {
    val pulse = LocalMatrixPulse.current

    // Typewriter reveal — runs once, terminates (no ongoing animation cost)
    val fullMsg = "INVOICING SYSTEM ONLINE // ALL NODES ACTIVE"
    var revealed by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        for (i in 1..fullMsg.length) {
            delay(38L)
            revealed = i
        }
    }
    val displayMsg = fullMsg.take(revealed) + if (revealed < fullMsg.length) "█" else ""

    MatrixCardPremium(title = ">> BIZAP TERMINAL", isPulsing = true) {
        // ASCII art with scan stripe overlaid
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Left: braille face
                Text(
                    text = SKULL_ASCII,
                    modifier = Modifier.weight(0.45f),
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        color = MatrixGreenBright.copy(alpha = 0.80f),
                        fontSize = 6.sp,
                        lineHeight = 7.5.sp
                    )
                )
                // Right: money bag
                Text(
                    text = MONEY_BAG_ASCII,
                    modifier = Modifier.weight(0.55f),
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        color = MatrixGreen.copy(alpha = 0.88f),
                        fontSize = 6.sp,
                        lineHeight = 7.5.sp
                    )
                )
            }
            // Scan stripe — sweeps bidirectionally over art driven by global pulse (no extra transition)
            Canvas(modifier = Modifier.matchParentSize()) {
                val sweepY = pulse * size.height
                val band = 40f
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0xFF00FF00).copy(alpha = 0.07f), Color.Transparent),
                        startY = (sweepY - band).coerceAtLeast(0f),
                        endY = (sweepY + band).coerceAtMost(size.height)
                    ),
                    topLeft = Offset(0f, (sweepY - band).coerceAtLeast(0f)),
                    size = Size(size.width, (band * 2f).coerceAtMost(size.height))
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = displayMsg,
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                color = MatrixGreenBright.copy(alpha = 0.72f),
                fontSize = 9.sp,
                letterSpacing = 1.5.sp
            )
        )
    }
}

/**
 * Key Metrics Dashboard - NOW WITH PULSING PREMIUM CARD & TERMINAL DISPLAY
 */
@Composable
fun KeyMetricsMatrix() {
    MatrixCardPremium(title = ">> FINANCIAL STATUS", isPulsing = true) {
        TerminalDataDisplay(
            rows = listOf(
                "Total Revenue" to "$245,678.90",
                "Outstanding" to "$28,450.00",
                "Paid This Month" to "$156,234.50",
                "Collection Rate" to "94.2%"
            )
        )
    }
}

/**
 * Quick Actions Section — vector icons, terminal-cell capsules, category accent colors.
 *
 * Color language:
 *   MatrixGreen   — primary create/document actions
 *   CyanAccent    — people/network actions
 *   MatrixWarning — financial/security actions
 *   MatrixDarkGreen — reporting/utility
 */
@Composable
fun QuickActionsMatrix(
    navController: NavHostController,
    businessId: Long
) {
    MatrixCardPremium(title = ">> ACTIONS", isPulsing = false) {

        // ROW 1: CORE CREATE ACTIONS
        Row(
            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlowingMatrixButton(
                text = "NEW INVOICE",
                icon = Icons.Default.Description,
                accentColor = MatrixGreen,
                onClick = { navController.navigate(ScreenV3.CreateInvoice(businessId)) },
                modifier = Modifier.weight(1f).wrapContentHeight(),
                isHighlight = true
            )
            GlowingMatrixButton(
                text = "NEW CUSTOMER",
                icon = Icons.Default.PersonAdd,
                accentColor = CyanAccent,
                onClick = {
                    Timber.d("GUI3: Navigate to create customer from dashboard")
                    navController.navigate(ScreenV3.CreateCustomer(businessId))
                },
                modifier = Modifier.weight(1f).wrapContentHeight(),
                isHighlight = true
            )
        }

        // ROW 2: NAVIGATION
        Row(
            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlowingMatrixButton(
                text = "INVOICES",
                icon = Icons.Default.Folder,
                accentColor = MatrixGreen,
                onClick = { navController.navigate(ScreenV3.Invoices(businessId)) },
                modifier = Modifier.weight(1f).wrapContentHeight()
            )
            GlowingMatrixButton(
                text = "CUSTOMERS",
                icon = Icons.Default.People,
                accentColor = CyanAccent,
                onClick = { navController.navigate(ScreenV3.Customers(businessId)) },
                modifier = Modifier.weight(1f).wrapContentHeight()
            )
        }

        // ROW 3: FINANCIAL OPERATIONS
        Row(
            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlowingMatrixButton(
                text = "PAYMENTS",
                icon = Icons.Default.Payment,
                accentColor = MatrixWarning,
                onClick = { navController.navigate(ScreenV3.PaymentTracking(businessId)) },
                modifier = Modifier.weight(1f).wrapContentHeight()
            )
            GlowingMatrixButton(
                text = "ANALYTICS",
                icon = Icons.Default.BarChart,
                accentColor = CyanAccent,
                onClick = { navController.navigate(ScreenV3.RevenueAnalytics(businessId)) },
                modifier = Modifier.weight(1f).wrapContentHeight()
            )
        }

        // ROW 4: REPORTING & SECURITY
        Row(
            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlowingMatrixButton(
                text = "REPORTS",
                icon = Icons.Default.Assessment,
                accentColor = MatrixDarkGreen,
                onClick = { navController.navigate(ScreenV3.Reports(businessId)) },
                modifier = Modifier.weight(1f).wrapContentHeight()
            )
            GlowingMatrixButton(
                text = "VAULT",
                icon = Icons.Default.Lock,
                accentColor = MatrixWarning,
                onClick = { navController.navigate(ScreenV3.SecurityVault(businessId)) },
                modifier = Modifier.weight(1f).wrapContentHeight()
            )
        }

        // ROW 5: UTILITIES
        Row(
            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlowingMatrixButton(
                text = "TEMPLATES",
                icon = Icons.Default.Dashboard,
                accentColor = MatrixDarkGreen,
                onClick = { navController.navigate(ScreenV3.PrefilledItems(businessId)) },
                modifier = Modifier.weight(1f).wrapContentHeight()
            )
            GlowingMatrixButton(
                text = "HELP",
                icon = Icons.AutoMirrored.Filled.Help,
                accentColor = MatrixGreen.copy(alpha = 0.70f),
                onClick = { navController.navigate(ScreenV3.Help(businessId)) },
                modifier = Modifier.weight(1f).wrapContentHeight()
            )
        }
    }
}

/**
 * Recent Activity Section — live feed aesthetic.
 * Timestamps, alternating row brightness, LIVE indicator.
 */
@Composable
fun RecentActivityMatrix() {
    // Blinking LIVE dot inside card
    var liveDot by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        while (true) { delay(600L); liveDot = !liveDot }
    }
    val liveIndicator = if (liveDot) "● LIVE" else "○ LIVE"

    val activityRows = listOf(
        Triple("[14:22]", "Invoice #INV-001 created", MatrixStatusStyle.SUCCESS to "Created"),
        Triple("[14:18]", "Payment received — Acme Corp", null to "+\$5,000.00"),
        Triple("[13:55]", "Invoice #INV-002 overdue", MatrixStatusStyle.ERROR to "Overdue")
    )

    MatrixCardPremium(title = ">> RECENT ACTIVITY  $liveIndicator", isPulsing = false) {
        activityRows.forEachIndexed { i, (time, label, badgeOrAmount) ->
            val (badgeStyle, badgeText) = badgeOrAmount
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (i % 2 == 0) MatrixGreen.copy(alpha = 0.04f) else Color.Transparent,
                        RoundedCornerShape(4.dp)
                    )
                    .padding(vertical = 4.dp, horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = time,
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            color = MatrixGreen.copy(alpha = 0.45f),
                            fontSize = 9.sp
                        )
                    )
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MatrixGreen.copy(alpha = 0.85f),
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }
                if (badgeStyle != null) {
                    GlowingStatusBadge(status = badgeText, style = badgeStyle)
                } else {
                    Text(
                        text = badgeText,
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            color = MatrixGreenBright,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }
    }
}

/**
 * GUI Switcher for Testing - GLOWING BUTTONS
 */
/**
 * Scrolling terminal-style system status ticker.
 * Uses its own LinearEasing InfiniteTransition (directional scroll cannot use oscillating global pulse).
 * Cost: 1 InfiniteTransition per screen it appears on (not per-component).
 */
@Composable
private fun SystemStatusTicker(modifier: Modifier = Modifier) {
    val scrollOffset by rememberInfiniteTransition(label = "ticker")
        .animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(22000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "tickerScroll"
        )
    val msg = "  ENCRYPT:AES-256  ◈  SYSLINK:ACTIVE  ◈  UPTIME:14D  ◈  NODES:3  ◈  SYNC:OK  ◈  TXNS:1,247  ◈  LATENCY:4ms  ◈  SECURE:YES  ◈  "
    Box(
        modifier = modifier
            .height(13.dp)
            .clipToBounds()
    ) {
        Text(
            text = msg.repeat(3),
            modifier = Modifier.offset { IntOffset(x = (-scrollOffset * 1400f).toInt(), y = 0) },
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                color = MatrixGreen.copy(alpha = 0.36f),
                fontSize = 9.sp, letterSpacing = 0.7.sp
            ),
            maxLines = 1, softWrap = false
        )
    }
}

/**
 * Core Meltdown art — amber/warning treatment, rare glitch jitter.
 */
@Composable
fun BottomTerminalArtMatrix() {
    // Rare glitch jitter: random offset for ~50ms every 2-5 seconds
    var glitchX by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(2000L + (kotlin.random.Random.nextLong(0, 3000)))
            glitchX = kotlin.random.Random.nextInt(-3, 4)
            delay(50L)
            glitchX = 0
        }
    }

    MatrixCardPremium(title = ">> CORE MELTDOWN", isPulsing = false, borderColor = MatrixWarning) {
        // Warning header
        Text(
            text = "⚠  ANOMALY DETECTED — SYSTEM INSTABILITY",
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                color = MatrixError.copy(alpha = 0.85f),
                fontSize = 9.sp,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            text = MUSHROOM_ASCII,
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                color = MatrixWarning.copy(alpha = 0.70f),
                fontSize = 7.sp,
                lineHeight = 8.sp
            ),
            modifier = Modifier
                .fillMaxWidth()
                .offset(x = glitchX.dp)
        )
    }
}

/**
 * Mode selector tiles — GUI1/2/3 as distinct OS-level operating mode entries.
 */
@Composable
fun GuiSwitcherMatrix(
    onSwitchToGui1: () -> Unit,
    onSwitchToGui2: () -> Unit
) {
    MatrixCardPremium(title = ">> SELECT OPERATING MODE", isPulsing = false) {
        ModeTileMatrix(
            modeName = "GUI1  ·  CLASSIC",
            descriptor = "LEGACY ACTIVITIES  //  MATERIAL2",
            status = "STANDBY",
            isActive = false,
            onClick = onSwitchToGui1
        )
        ModeTileMatrix(
            modeName = "GUI2  ·  MODERN",
            descriptor = "COMPOSE  //  MATERIAL3",
            status = "STANDBY",
            isActive = false,
            onClick = onSwitchToGui2
        )
        ModeTileMatrix(
            modeName = "GUI3  ·  MATRIX",
            descriptor = "CYBERPUNK TERMINAL  //  ACTIVE SESSION",
            status = "ACTIVE",
            isActive = true,
            onClick = {}
        )
    }
}

@Composable
private fun ModeTileMatrix(
    modeName: String,
    descriptor: String,
    status: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val pulse = LocalMatrixPulse.current
    val tileColor = if (isActive) MatrixGreenBright else MatrixGreen
    val borderAlpha = if (isActive) 0.45f + pulse * 0.45f else 0.28f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .border(1.dp, tileColor.copy(alpha = borderAlpha), RoundedCornerShape(6.dp))
            .background(
                if (isActive) MatrixGreen.copy(alpha = 0.08f) else MatrixBlack.copy(alpha = 0.30f),
                RoundedCornerShape(6.dp)
            )
            .then(if (!isActive) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = modeName,
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    color = if (isActive) MatrixGreenBright else MatrixGreen.copy(alpha = 0.80f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )
            Text(
                text = descriptor,
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    color = MatrixGreen.copy(alpha = 0.50f),
                    fontSize = 9.sp,
                    letterSpacing = 0.7.sp
                )
            )
        }
        GlowingStatusBadge(
            status = status,
            style = if (isActive) MatrixStatusStyle.SUCCESS else MatrixStatusStyle.NEUTRAL
        )
    }
}

