@file:Suppress("unused")

package com.emul8r.bizap.ui.gui3.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import com.emul8r.bizap.ui.gui3.components.*
import com.emul8r.bizap.ui.gui3.navigation.ScreenV3
import com.emul8r.bizap.ui.gui3.theme.*
import com.emul8r.bizap.ui.gui3.util.MatrixCascadeState
import com.emul8r.bizap.ui.theme.Spacing
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
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "▓▓ MATRIX DASHBOARD ▓▓",
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
                GuiSwitcherMatrix(
                    onSwitchToGui1 = onSwitchToGui1,
                    onSwitchToGui2 = onSwitchToGui2
                )

                Spacer(modifier = Modifier.height(Spacing.xxl))
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

/**
 * Welcome Banner with Matrix Aesthetic - PULSING PREMIUM CARD
 * ✅ IMMERSION: Responds to cascading background visibility for reactive feel
 */
@Composable
fun WelcomeMatrixBanner() {
    // ✅ IMMERSION: Read cascade state for reactive opacity animation
    val cascadeVisibility = MatrixCascadeState.cascadeVisibility.value

    val bannerAlpha by animateFloatAsState(
        targetValue = 0.8f + (cascadeVisibility * 0.2f),  // Opacity ranges 0.8-1.0 based on cascade
        animationSpec = tween(300),
        label = "bannerOpacity"
    )

    Box(modifier = Modifier.alpha(bannerAlpha)) {
        MatrixCardPremium(title = ">> WELCOME BACK", isPulsing = true) {
            Text(
                text = "The Matrix awaits your command.",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MatrixGreen.copy(alpha = 0.9f)
                )
            )

            Text(
                text = "Follow the green line to manage your invoices.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MatrixGreen.copy(alpha = 0.7f)
                )
            )
        }
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
 * Quick Actions Section - EXPANDED FOR PHASE 2.3
 * ✅ Row 1: NEW INVOICE | NEW CUSTOMER (core actions, highlighted)
 * ✅ Row 2: VIEW INVOICES | VIEW CUSTOMERS (navigation)
 * ✅ Row 3: PAYMENTS | ANALYTICS (secondary actions)
 * ✅ Row 4: REPORTS | SECURITY VAULT (management & secure access)
 * ✅ Row 5: TEMPLATES | HELP (quick preset management & support)
 */
@Composable
fun QuickActionsMatrix(
    navController: NavHostController,
    businessId: Long
) {
    MatrixCardPremium(title = ">> ACTIONS", isPulsing = false) {
        // ===== ROW 1: CORE ACTIONS (NEW INVOICE | NEW CUSTOMER) =====
        // ✅ Highlighted with isHighlight=true, larger visual weight
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlowingMatrixButton(
                text = "📋 NEW INVOICE",
                onClick = { navController.navigate(ScreenV3.CreateInvoice(businessId)) },
                modifier = Modifier
                    .weight(1f)
                    .wrapContentHeight(),
                isHighlight = true  // ✅ Core action emphasized
            )

            GlowingMatrixButton(
                text = "👤 NEW CUSTOMER",
                onClick = {
                    Timber.d("GUI3: Navigate to create customer from dashboard")
                    navController.navigate(ScreenV3.CreateCustomer(businessId))
                },
                modifier = Modifier
                    .weight(1f)
                    .wrapContentHeight(),
                isHighlight = true  // ✅ Core action emphasized
            )
        }

        // ===== ROW 2: NAVIGATION (VIEW INVOICES | VIEW CUSTOMERS) =====
        // ✅ Standard buttons, equal weight
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlowingMatrixButton(
                text = "📄 INVOICES",
                onClick = { navController.navigate(ScreenV3.Invoices(businessId)) },
                modifier = Modifier
                    .weight(1f)
                    .wrapContentHeight()
            )

            GlowingMatrixButton(
                text = "👥 CUSTOMERS",
                onClick = { navController.navigate(ScreenV3.Customers(businessId)) },
                modifier = Modifier
                    .weight(1f)
                    .wrapContentHeight()
            )
        }

        // ===== ROW 3: SECONDARY ACTIONS (PAYMENTS | ANALYTICS) =====
        // ✅ Standard buttons, supporting features
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlowingMatrixButton(
                text = "💳 PAYMENTS",
                onClick = { navController.navigate(ScreenV3.PaymentTracking(businessId)) },
                modifier = Modifier
                    .weight(1f)
                    .wrapContentHeight()
            )

            GlowingMatrixButton(
                text = "📊 ANALYTICS",
                onClick = { navController.navigate(ScreenV3.RevenueAnalytics(businessId)) },
                modifier = Modifier
                    .weight(1f)
                    .wrapContentHeight()
            )
        }

        // ===== ROW 4: MANAGEMENT (REPORTS | SECURITY VAULT) =====
        // ✅ Advanced features for power users
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlowingMatrixButton(
                text = "📈 REPORTS",
                onClick = { navController.navigate(ScreenV3.Reports(businessId)) },
                modifier = Modifier
                    .weight(1f)
                    .wrapContentHeight()
            )

            GlowingMatrixButton(
                text = "🔐 SECURITY VAULT",
                onClick = { navController.navigate(ScreenV3.SecurityVault(businessId)) },
                modifier = Modifier
                    .weight(1f)
                    .wrapContentHeight(),
                isHighlight = false
            )
        }

        // ===== ROW 5: UTILITIES (TEMPLATES | HELP) =====
        // ✅ Template management for pre-filled invoice items
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlowingMatrixButton(
                text = "🧩 TEMPLATES",
                onClick = { navController.navigate(ScreenV3.PrefilledItems(businessId)) },
                modifier = Modifier
                    .weight(1f)
                    .wrapContentHeight()
            )

            GlowingMatrixButton(
                text = "❓ HELP",
                onClick = { navController.navigate(ScreenV3.Help(businessId)) },
                modifier = Modifier
                    .weight(1f)
                    .wrapContentHeight()
            )
        }
    }
}

/**
 * Recent Activity Section - GLOWING STATUS BADGES
 */
@Composable
fun RecentActivityMatrix() {
    MatrixCardPremium(title = ">> RECENT ACTIVITY", isPulsing = false) {
        // Activity item 1
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Invoice #INV-001 created",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MatrixGreen.copy(alpha = 0.8f)
                )
            )
            GlowingStatusBadge(
                status = "Created",
                style = MatrixStatusStyle.SUCCESS
            )
        }

        // Activity item 2 - spacing handled by parent Column
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Payment received from Acme Corp",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MatrixGreen.copy(alpha = 0.8f)
                )
            )
            FormattedAmountMatrix(
                amount = "+$5,000.00",
                isPositive = true
            )
        }

        // Activity item 3 - spacing handled by parent Column
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Invoice #INV-002 overdue",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MatrixGreen.copy(alpha = 0.8f)
                )
            )
            GlowingStatusBadge(
                status = "Overdue",
                style = MatrixStatusStyle.ERROR
            )
        }
    }
}

/**
 * GUI Switcher for Testing - GLOWING BUTTONS
 */
@Composable
fun GuiSwitcherMatrix(
    onSwitchToGui1: () -> Unit,
    onSwitchToGui2: () -> Unit
) {
    MatrixCardPremium(title = ">> EXPERIENCE SWITCH", isPulsing = false) {
        // Text label with automatic spacing from MatrixCardPremium's Column
        Text(
            "Choose your user experience:",
            style = MaterialTheme.typography.bodySmall.copy(
                color = MatrixGreen.copy(alpha = 0.7f)
            )
        )

        // Buttons automatically spaced by parent Column (Spacing.md)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            GlowingMatrixButton(
                text = "GUI1",
                onClick = onSwitchToGui1,
                modifier = Modifier.weight(1f)
            )

            GlowingMatrixButton(
                text = "GUI2",
                onClick = onSwitchToGui2,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

