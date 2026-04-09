package com.emul8r.bizap.ui.analytics

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emul8r.bizap.domain.analytics.AnalyticsDateRange
import com.emul8r.bizap.ui.analytics.components.AnalyticsFilterChips
import com.emul8r.bizap.ui.analytics.components.MetricBreakdownBottomSheet
import timber.log.Timber
import java.util.Calendar

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
    quickReportsViewModel: QuickReportsTabViewModel = hiltViewModel(),
    revenueViewModel: RevenueAnalyticsTabViewModel = hiltViewModel(),
    paymentViewModel: PaymentAnalyticsTabViewModel = hiltViewModel(),
    customerViewModel: CustomerAnalyticsTabViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val mainUiState by mainViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Bottom sheet state for drills
    var showDrillSheet by remember { mutableStateOf(false) }
    var drillData: Pair<String, List<Pair<String, Double>>>? by remember { mutableStateOf(null) }

    /** Shows a two-step date picker: first start date, then end date. */
    fun showCustomDateRangePicker() {
        val today = Calendar.getInstance()
        // Step 1: pick start date
        DatePickerDialog(
            context,
            { _, startYear, startMonth, startDay ->
                val startCal = Calendar.getInstance().apply {
                    set(startYear, startMonth, startDay, 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val startMs = startCal.timeInMillis
                // Step 2: pick end date
                DatePickerDialog(
                    context,
                    { _, endYear, endMonth, endDay ->
                        val endCal = Calendar.getInstance().apply {
                            set(endYear, endMonth, endDay, 23, 59, 59)
                            set(Calendar.MILLISECOND, 999)
                        }
                        Timber.d(
                            "Analytics: Custom date range selected: " +
                            "${startCal.time} → ${endCal.time}"
                        )
                        mainViewModel.setDateRange(AnalyticsDateRange.CUSTOM)
                    },
                    today.get(Calendar.YEAR),
                    today.get(Calendar.MONTH),
                    today.get(Calendar.DAY_OF_MONTH)
                ).also { endPicker ->
                    endPicker.datePicker.minDate = startMs
                    endPicker.datePicker.maxDate = today.timeInMillis
                    endPicker.show()
                }
            },
            today.get(Calendar.YEAR),
            today.get(Calendar.MONTH),
            today.get(Calendar.DAY_OF_MONTH)
        ).also { startPicker ->
            startPicker.datePicker.maxDate = today.timeInMillis
            startPicker.show()
        }
    }

    val tabTitles = listOf("Quick Reports", "Revenue", "Payment", "Customers", "Risk", "Cash Flow")

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
                onCustomDateRange = { showCustomDateRangePicker() }
            )

            // Tab content
            Box(modifier = Modifier.fillMaxSize()) {
                when (mainUiState.selectedTabIndex) {
                    0 -> QuickReportsTab(
                        viewModel = quickReportsViewModel,
                        dateRange = mainUiState.selectedDateRange,
                        onDrillClick = { label, items ->
                            drillData = label to items
                            showDrillSheet = true
                        }
                    )
                    1 -> RevenueAnalyticsTab(
                        viewModel = revenueViewModel,
                        dateRange = mainUiState.selectedDateRange,
                        onDrillClick = { label, items ->
                            drillData = label to items
                            showDrillSheet = true
                        }
                    )
                    2 -> PaymentAnalyticsTab(
                        viewModel = paymentViewModel,
                        dateRange = mainUiState.selectedDateRange,
                        onDrillClick = { label, items ->
                            drillData = label to items
                            showDrillSheet = true
                        }
                    )
                    3 -> CustomerAnalyticsTab(
                        viewModel = customerViewModel,
                        dateRange = mainUiState.selectedDateRange,
                        onDrillClick = { label, items ->
                            drillData = label to items
                            showDrillSheet = true
                        }
                    )
                    4 -> RiskAnalyticsTab(
                        viewModel = paymentViewModel,
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

