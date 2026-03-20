package com.emul8r.bizap.ui.health

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emul8r.bizap.ui.components.SnapshotHealthDetailsCard
import com.emul8r.bizap.ui.components.SnapshotHealthWarningBanner
import com.emul8r.bizap.ui.components.SnapshotHealthWarningInline
import timber.log.Timber

/**
 * Integration examples showing different ways to use the snapshot health warning.
 * Choose the style that fits your screen design.
 */

// ════════════════════════════════════════════════════════════════════════════════
// PATTERN 1: Top Banner (Recommended for Main Screens)
// ════════════════════════════════════════════════════════════════════════════════

/**
 * Example: Dashboard screen with health warning banner at top
 */
@Composable
fun DashboardScreenWithHealthBanner(
    viewModel: SnapshotHealthViewModel,
    content: @Composable () -> Unit
) {
    val healthReport by viewModel.healthReport.collectAsStateWithLifecycle()
    val isBackfilling by viewModel.isBackfillRunning.collectAsStateWithLifecycle()

    // Initial health check
    LaunchedEffect(Unit) {
        Timber.d("Dashboard: Checking snapshot health...")
        viewModel.checkHealth()
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
        ) {
            // WARNING BANNER - Shows at top if issues detected
            SnapshotHealthWarningBanner(
                healthReport = healthReport,
                onDismiss = { viewModel.dismissWarning() },
                onRunBackfill = { viewModel.runBackfill() }
            )

            // Main content below banner
            content()
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════════
// PATTERN 2: Expandable Card (For Detailed View Screens)
// ════════════════════════════════════════════════════════════════════════════════

/**
 * Example: Invoice detail screen with expandable health card
 */
@Composable
fun InvoiceDetailScreenWithHealthCard(
    viewModel: SnapshotHealthViewModel,
    content: @Composable () -> Unit
) {
    val healthReport by viewModel.healthReport.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        if (viewModel.shouldRecheck()) {
            viewModel.checkHealth()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // EXPANDABLE CARD - Click to see full details
        if (healthReport != null && !healthReport!!.isHealthy) {
            SnapshotHealthDetailsCard(
                healthReport = healthReport!!,
                onRunBackfill = { viewModel.runBackfill() },
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // Main content below card
        content()
    }
}

// ════════════════════════════════════════════════════════════════════════════════
// PATTERN 3: Inline Warning (For Compact Screens)
// ════════════════════════════════════════════════════════════════════════════════

/**
 * Example: List screen with inline health warning
 */
@Composable
fun InvoiceListScreenWithInlineWarning(
    viewModel: SnapshotHealthViewModel,
    content: @Composable () -> Unit
) {
    val healthReport by viewModel.healthReport.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        // INLINE WARNING - Compact and dismissible
        SnapshotHealthWarningInline(
            healthReport = healthReport,
            onRunBackfill = { viewModel.runBackfill() },
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Main list content
        content()
    }
}

// ════════════════════════════════════════════════════════════════════════════════
// PATTERN 4: Automatic Periodic Checks
// ════════════════════════════════════════════════════════════════════════════════

/**
 * Example: Screen that automatically re-checks health periodically
 */
@Composable
fun ScreenWithPeriodicHealthCheck(
    viewModel: SnapshotHealthViewModel,
    content: @Composable () -> Unit
) {
    val healthReport by viewModel.healthReport.collectAsStateWithLifecycle()

    // Auto-check health every 5 minutes
    LaunchedEffect(Unit) {
        while (true) {
            if (viewModel.shouldRecheck()) {
                viewModel.checkHealth()
            }
            kotlinx.coroutines.delay(60 * 1000)  // Check every minute if needed
        }
    }

    Column {
        SnapshotHealthWarningBanner(
            healthReport = healthReport,
            onDismiss = { viewModel.dismissWarning() },
            onRunBackfill = { viewModel.runBackfill() }
        )

        content()
    }
}

// ════════════════════════════════════════════════════════════════════════════════
// USAGE IN YOUR EXISTING SCREENS
// ════════════════════════════════════════════════════════════════════════════════

/**
 * HOW TO USE IN RevenueDashboardScreen:
 *
 * @Composable
 * fun RevenueDashboardScreen(
 *     viewModel: RevenueDashboardViewModel = hiltViewModel(),
 *     healthViewModel: SnapshotHealthViewModel = hiltViewModel()  // ← ADD THIS
 * ) {
 *     val state by viewModel.uiState.collectAsState()
 *     val healthReport by healthViewModel.healthReport.collectAsStateWithLifecycle()  // ← ADD THIS
 *
 *     LaunchedEffect(Unit) {
 *         healthViewModel.checkHealth()  // ← ADD THIS
 *     }
 *
 *     Column {
 *         // Show warning if unhealthy
 *         SnapshotHealthWarningBanner(
 *             healthReport = healthReport,
 *             onDismiss = { healthViewModel.dismissWarning() },
 *             onRunBackfill = { healthViewModel.runBackfill() }
 *         )
 *
 *         // Your existing content
 *         when (state) {
 *             is Loading -> { /* ... */ }
 *             is Success -> { /* ... */ }
 *             is Error -> { /* ... */ }
 *         }
 *     }
 * }
 */

/**
 * HOW TO USE IN PaymentAnalyticsScreen:
 *
 * @Composable
 * fun PaymentAnalyticsScreen(
 *     viewModel: PaymentAnalyticsViewModel = hiltViewModel(),
 *     healthViewModel: SnapshotHealthViewModel = hiltViewModel()  // ← ADD THIS
 * ) {
 *     val state by viewModel.uiState.collectAsState()
 *     val healthReport by healthViewModel.healthReport.collectAsStateWithLifecycle()  // ← ADD THIS
 *
 *     Column {
 *         // Show inline warning (compact style)
 *         SnapshotHealthWarningInline(
 *             healthReport = healthReport,
 *             onRunBackfill = { healthViewModel.runBackfill() }
 *         )
 *
 *         // Your existing content
 *         when (state) {
 *             is Loading -> { /* ... */ }
 *             is Success -> { /* ... */ }
 *             is Error -> { /* ... */ }
 *         }
 *     }
 * }
 */

/**
 * HOW TO USE IN CustomerSegmentsScreen:
 *
 * @Composable
 * fun CustomerSegmentsScreen(
 *     viewModel: CustomerSegmentsViewModel = hiltViewModel(),
 *     healthViewModel: SnapshotHealthViewModel = hiltViewModel()  // ← ADD THIS
 * ) {
 *     val state by viewModel.uiState.collectAsState()
 *     val healthReport by healthViewModel.healthReport.collectAsStateWithLifecycle()  // ← ADD THIS
 *
 *     Column {
 *         // Show expandable details card
 *         if (healthReport != null && !healthReport!!.isHealthy) {
 *             SnapshotHealthDetailsCard(
 *                 healthReport = healthReport!!,
 *                 onRunBackfill = { healthViewModel.runBackfill() }
 *             )
 *         }
 *
 *         // Your existing content
 *         LazyColumn {
 *             items(state.segments) { segment ->
 *                 // Segment item
 *             }
 *         }
 *     }
 * }
 */

