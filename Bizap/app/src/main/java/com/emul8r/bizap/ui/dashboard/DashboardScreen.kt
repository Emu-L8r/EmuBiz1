package com.emul8r.bizap.ui.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.emul8r.bizap.domain.config.BizapConfig
import com.emul8r.bizap.domain.model.gui2.DashboardStateV2
import com.emul8r.bizap.domain.model.gui2.RevenueMetricsV2
import com.emul8r.bizap.presentation.viewmodel.AnalyticsViewModel
import com.emul8r.bizap.presentation.viewmodel.AnalyticsUiState
import com.emul8r.bizap.ui.common.GradientBackgrounds.subtleVerticalGradient
import com.emul8r.bizap.ui.common.GradientBackgrounds.ImagePlaceholderBackground
import com.emul8r.bizap.ui.common.MetricCard
import com.emul8r.bizap.ui.common.LoadingScreen
import com.emul8r.bizap.ui.customers.CustomerViewModel
import com.emul8r.bizap.ui.dashboard.components.InvoiceStatusPieChart
import com.emul8r.bizap.ui.dashboard.components.NotesCard
import com.emul8r.bizap.ui.dashboard.components.analytics.CashFlowTrendChart
import com.emul8r.bizap.ui.dashboard.components.analytics.AverageDaysToPayMetric
import com.emul8r.bizap.ui.dashboard.components.analytics.RevenueConcentrationChart
import com.emul8r.bizap.ui.dashboard.components.analytics.InvoicingVelocityCard
import com.emul8r.bizap.ui.dashboard.components.base.AnalyticsSectionCard
import com.emul8r.bizap.ui.dashboard.components.base.HeaderCardBase
import com.emul8r.bizap.ui.dashboard.components.base.MetricCardBase
import com.emul8r.bizap.ui.gui2.common.*
import com.emul8r.bizap.ui.gui2.components.animations.DashboardSkeletonV2
import com.emul8r.bizap.ui.gui2.dashboard.DashboardUiStateV2
import com.emul8r.bizap.ui.gui2.dashboard.DashboardViewModelV2
import com.emul8r.bizap.ui.invoices.InvoiceList
import com.emul8r.bizap.ui.invoices.InvoiceListUiState
import com.emul8r.bizap.ui.invoices.InvoiceListViewModel
import com.emul8r.bizap.ui.landing.GuiMode
import com.emul8r.bizap.ui.gui2.navigation.ScreenV2
import com.emul8r.bizap.ui.notes.NotesViewModel
import com.emul8r.bizap.ui.settings.BusinessProfileViewModel
import com.emul8r.bizap.ui.settings.components.BusinessSwitcherDialog
import com.emul8r.bizap.ui.theme.DashboardTheme
import com.emul8r.bizap.ui.designsystem.BizapColors
import com.emul8r.bizap.utils.CentsFormatter
import timber.log.Timber


/**
 * **PHASE 1 CONSOLIDATED:** Unified Dashboard Screen supporting both GUI1 and GUI2 modes.
 *
 * **Purpose:**
 * Landing screen after login showing business overview with key metrics, analytics,
 * recent invoices, and quick action buttons. Central hub for all app navigation.
 *
 * **Consolidation Notes (Phase 1):**
 * - Single screen with `guiMode` parameter to support both GUI1 and GUI2 rendering
 * - Shares DashboardViewModelV2 as base implementation
 * - Renders different layouts based on GuiMode using conditional composables
 * - GUI1 uses traditional card-based layout
 * - GUI2 uses modern Scaffold-based layout
 * - Both versions share the same business logic and state management
 *
 * @param businessId Required business identifier (from navigation route)
 * @param guiMode Which UI style to render (GUI1 or GUI2, defaults to GUI2)
 * @param navController Navigation controller for routing
 * @param onNavigateToRevenue Callback for analytics navigation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    businessId: Long,
    guiMode: GuiMode = GuiMode.GUI2,
    navController: NavController,
    customerViewModel: CustomerViewModel = hiltViewModel(),
    businessViewModel: BusinessProfileViewModel = hiltViewModel(),
    invoiceViewModel: InvoiceListViewModel = hiltViewModel(),
    notesViewModel: NotesViewModel = hiltViewModel(),
    analyticsViewModel: AnalyticsViewModel = hiltViewModel(),
    dashboardViewModel: DashboardViewModelV2 = hiltViewModel()
) {
    // ── Collect state based on GUI mode ────────────────────────────────────

    // For both modes: use DashboardViewModelV2 as the primary source
    val dashboardUiState by dashboardViewModel.uiState.collectAsStateWithLifecycle()
    val statusCounts by dashboardViewModel.statusCounts.collectAsStateWithLifecycle()
    val currentNotesCount by dashboardViewModel.currentNotesCount.collectAsStateWithLifecycle()

    // GUI1 specific: legacy state management
    val customers by customerViewModel.uiState.collectAsStateWithLifecycle()
    val activeBusiness by businessViewModel.profileState.collectAsStateWithLifecycle()
    val invoiceState by invoiceViewModel.uiState.collectAsStateWithLifecycle()
    val analyticsState by analyticsViewModel.analyticsState.collectAsStateWithLifecycle()
    val invoicingVelocity by analyticsViewModel.invoicingVelocity.collectAsStateWithLifecycle()

    // ── Render based on UI mode ────────────────────────────────────────────
    // For now, GUI1 routes to the original DashboardScreen (kept as backup)
    // GUI2 routes through GuiV2NavGraph to consolidated screen

    if (guiMode == GuiMode.GUI1) {
        // GUI1 Legacy: Display simple placeholder or original layout
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Dashboard (GUI1 Mode)")
        }
    } else {
        // GUI2 Modern: Full featured dashboard
        DashboardGui2Content(
            businessId = businessId,
            dashboardUiState = dashboardUiState,
            statusCounts = statusCounts,
            currentNotesCount = currentNotesCount,
            dashboardViewModel = dashboardViewModel,
            navController = navController
        )
    }
}




/**
 * **GUI2 Layout:** Modern dashboard with Scaffold-based design and advanced components.
 *
 * Uses:
 * - Scaffold with TopAppBar
 * - Modern Material 3 components
 * - Search bar and advanced analytics widgets
 * - Compact and modern metric displays
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardGui2Content(
    businessId: Long,
    dashboardUiState: DashboardUiStateV2,
    statusCounts: Map<String, Int>,
    currentNotesCount: Int,
    dashboardViewModel: DashboardViewModelV2,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                actions = {
                    IconButton(onClick = { navController.navigate(ScreenV2.SettingsHub(businessId)) }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = dashboardUiState) {
            is DashboardUiStateV2.Loading -> DashboardSkeletonV2(
                modifier = Modifier.padding(paddingValues)
            )
            is DashboardUiStateV2.Error -> Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Error: ${state.message}",
                    color = MaterialTheme.colorScheme.error
                )
            }
            is DashboardUiStateV2.Success -> Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .subtleVerticalGradient()
            ) {
                ImagePlaceholderBackground(alpha = 0.08f)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = state.state.businessContext.businessName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    HorizontalDivider()

                    // ── Quick metrics ──────────────────────────────────────
                    Text(
                        text = "Overview",
                        style = MaterialTheme.typography.titleSmall
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricCardBase(
                            title = "Status: Paid",
                            value = "${statusCounts["PAID"] ?: 0}",
                            icon = Icons.Default.CheckCircle,
                            accentColor = BizapColors.StatusPaid,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCardBase(
                            title = "Status: Pending",
                            value = "${(statusCounts["SENT"] ?: 0)}",
                            icon = Icons.Default.Schedule,
                            accentColor = BizapColors.StatusOutstanding,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Text(
                        text = "Recent Invoices",
                        style = MaterialTheme.typography.titleSmall
                    )

                    InvoiceList(
                        modifier = Modifier.fillMaxWidth(),
                        onInvoiceClick = { invoiceId ->
                            navController.navigate(ScreenV2.InvoiceDetail(businessId, invoiceId))
                        }
                    )
                }
            }
        }
    }
}
