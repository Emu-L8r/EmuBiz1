package com.emul8r.bizap.ui.gui3.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.emul8r.bizap.ui.gui3.components.*
import com.emul8r.bizap.ui.gui3.navigation.ScreenV3
import com.emul8r.bizap.ui.gui3.theme.*
import com.emul8r.bizap.ui.landing.GuiMode
import com.emul8r.bizap.ui.shared.GuiModeSwitcher
import com.emul8r.bizap.ui.theme.Spacing
import timber.log.Timber

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
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "BIZAP > MATRIX",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            color = MatrixGreenBright,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            letterSpacing = 1.sp
                        )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MatrixSurface,
                    navigationIconContentColor = MatrixGreen,
                    titleContentColor = MatrixGreen,
                    actionIconContentColor = MatrixGreen
                ),
                actions = {
                    // GUI Mode Switcher (1, 2, 3 buttons)
                    GuiModeSwitcher(
                        currentMode = GuiMode.GUI3,
                        onGui1Click = onSwitchToGui1,
                        onGui2Click = onSwitchToGui2,
                        onGui3Click = {} // Already on GUI3
                    )

                    // Settings icon
                    IconButton(
                        onClick = { navController.navigate(ScreenV3.Settings(businessId)) }
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }

                    IconButton(onClick = {}) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MatrixBlack)
                .padding(paddingValues)
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
}

/**
 * Welcome Banner with Matrix Aesthetic - PULSING PREMIUM CARD
 */
@Composable
fun WelcomeMatrixBanner() {
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
 * Quick Actions Section - NOW WITH VAULT ACCESS
 */
@Composable
fun QuickActionsMatrix(
    navController: NavHostController,
    businessId: Long
) {
    val context = LocalContext.current

    MatrixCardPremium(title = ">> ACTIONS", isPulsing = false) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlowingMatrixButton(
                text = "NEW INVOICE",
                onClick = { navController.navigate(ScreenV3.CreateInvoice(businessId)) },
                modifier = Modifier
                    .weight(1f),
                isHighlight = true
            )

            GlowingMatrixButton(
                text = "CUSTOMERS",
                onClick = { navController.navigate(ScreenV3.Customers(businessId)) },
                modifier = Modifier
                    .weight(1f)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlowingMatrixButton(
                text = "NEW CUSTOMER",
                onClick = {
                    Timber.d("GUI3: Navigate to create customer from dashboard")
                    navController.navigate(ScreenV3.CreateCustomer(businessId))
                },
                modifier = Modifier
                    .weight(1f),
                isHighlight = true
            )

            GlowingMatrixButton(
                text = "INVOICES",
                onClick = { navController.navigate(ScreenV3.Invoices(businessId)) },
                modifier = Modifier
                    .weight(1f)
            )

            GlowingMatrixButton(
                text = "PAYMENTS",
                onClick = { navController.navigate(ScreenV3.PaymentTracking(businessId)) },
                modifier = Modifier
                    .weight(1f)
            )
        }

        // New Row: Vault Access (Coming Soon)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlowingMatrixButton(
                text = "📁 VAULT",
                onClick = {
                    Timber.d("GUI3: Vault feature coming soon")
                    // TODO: Integrate vault access in next phase
                },
                modifier = Modifier
                    .weight(1f),
                isHighlight = true,
                enabled = false  // Disabled for now - vault integration coming
            )

            Spacer(modifier = Modifier.weight(2f))  // Spacing for visual balance
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

        Spacer(modifier = Modifier.height(Spacing.md))

        // Activity item 2
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

        Spacer(modifier = Modifier.height(Spacing.md))

        // Activity item 3
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
        Text(
            "Choose your user experience:",
            style = MaterialTheme.typography.bodySmall.copy(
                color = MatrixGreen.copy(alpha = 0.7f)
            ),
            modifier = Modifier.padding(bottom = Spacing.md)
        )

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

/**
 * Coming Soon Placeholder Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComingSoonScreen(
    title: String,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "BIZAP > $title",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            color = MatrixGreenBright
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MatrixGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MatrixSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MatrixBlack)
                .padding(paddingValues)
                .padding(Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                ">> MATRIX",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    color = MatrixGreenBright,
                    letterSpacing = 2.sp
                )
            )

            Spacer(modifier = Modifier.height(Spacing.lg))

            Text(
                "$title coming soon...",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = MatrixGreen,
                    fontFamily = FontFamily.Monospace
                )
            )

            Spacer(modifier = Modifier.height(Spacing.xxl))

            Text(
                "Screen under construction",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MatrixGreen.copy(alpha = 0.6f)
                )
            )
        }
    }
}

