package com.emul8r.bizap.ui.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emul8r.bizap.ui.analytics.components.AnalyticsFilterChips
import com.emul8r.bizap.ui.analytics.components.MetricBreakdownBottomSheet
import timber.log.Timber

/**
 * Main Focused Insights Analytics Screen.
 *
 * Features a tabbed interface with 4 specialized analytics dashboards:
 * 1. Revenue - MTD, YTD, daily trends
 * 2. Payment - Outstanding, collection rate, aging
 * 3. Customers - Segments, LTV, at-risk
 * 4. Cash Flow - 30-day forecast
 *
 * Each tab can be filtered by date range (7d/30d/90d/custom).
 * Tapping metrics opens ModalBottomSheet drills for detailed breakdowns.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsFocusedInsightsScreen(
    onBack: () -> Unit = {},
    mainViewModel: AnalyticsFocusedInsightsViewModel = hiltViewModel(),
    revenueViewModel: RevenueAnalyticsTabViewModel = hiltViewModel(),
    paymentViewModel: PaymentAnalyticsTabViewModel = hiltViewModel(),
    customerViewModel: CustomerAnalyticsTabViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val mainUiState by mainViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Bottom sheet state for drills
    var showDrillSheet by remember { mutableStateOf(false) }
    var drillData: Pair<String, List<Pair<String, Double>>>? by remember { mutableStateOf(null) }

    val tabTitles = listOf("Revenue", "Payment", "Customers", "Cash Flow")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Analytics Insights",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tab selector
            TabRow(
                selectedTabIndex = mainUiState.selectedTabIndex,
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = mainUiState.selectedTabIndex == index,
                        onClick = { mainViewModel.setTabIndex(index) },
                        text = { Text(title, fontWeight = FontWeight.Medium) },
                        modifier = Modifier.height(48.dp)
                    )
                }
            }

            // Date range filters (shared across all tabs)
            AnalyticsFilterChips(
                selectedRange = mainUiState.selectedDateRange,
                onRangeSelected = { mainViewModel.setDateRange(it) },
                onCustomDateRange = {
                    Timber.d("Analytics: Custom date range picker opened")
                    // TODO: Implement custom date range dialog
                }
            )

            // Tab content
            Box(modifier = Modifier.fillMaxSize()) {
                when (mainUiState.selectedTabIndex) {
                    0 -> RevenueAnalyticsTab(
                        viewModel = revenueViewModel,
                        dateRange = mainUiState.selectedDateRange,
                        onDrillClick = { label, items ->
                            drillData = label to items
                            showDrillSheet = true
                        }
                    )
                    1 -> PaymentAnalyticsTab(
                        viewModel = paymentViewModel,
                        dateRange = mainUiState.selectedDateRange,
                        onDrillClick = { label, items ->
                            drillData = label to items
                            showDrillSheet = true
                        }
                    )
                    2 -> CustomerAnalyticsTab(
                        viewModel = customerViewModel,
                        dateRange = mainUiState.selectedDateRange,
                        onDrillClick = { label, items ->
                            drillData = label to items
                            showDrillSheet = true
                        }
                    )
                    else -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Cash Flow Analytics (Coming Soon)",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        }
    }

    // Drill detail bottom sheet
    if (showDrillSheet && drillData != null) {
        ModalBottomSheet(
            onDismissRequest = { showDrillSheet = false }
        ) {
            MetricBreakdownBottomSheet(
                title = drillData!!.first,
                items = drillData!!.second,
                onDismiss = { showDrillSheet = false },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

