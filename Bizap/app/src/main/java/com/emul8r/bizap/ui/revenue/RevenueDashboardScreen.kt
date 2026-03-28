package com.emul8r.bizap.ui.revenue

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.emul8r.bizap.domain.model.gui2.RevenueMetricsV2
import com.emul8r.bizap.ui.common.GradientBackgrounds.subtleVerticalGradient
import com.emul8r.bizap.ui.common.MetricCard
import com.emul8r.bizap.ui.designsystem.BizapColors
import com.emul8r.bizap.utils.CentsFormatter

/**
 * Revenue dashboard screen Composable for GUI1.
 *
 * **Purpose:**
 * Displays comprehensive revenue metrics and analytics for the active business.
 * Shows real-time financial KPIs: MTD revenue, outstanding, overdue amounts, payment status.
 *
 * **Features:**
 * - MTD (Month-to-Date) Revenue - Total revenue earned this month
 * - Outstanding Balance - Invoices awaiting payment
 * - Overdue Amount - Invoices past due date
 * - Payment Completion Rate - Percentage of invoices paid
 * - Metric cards with color-coded status indicators
 * - Loading spinner while fetching metrics
 * - Error state with retry capability
 * - Empty state if no data
 * - Gradient background for visual polish
 *
 * **Metric Cards:**
 * ```
 * ┌─────────────────────┐
 * │ 💰 MTD Revenue      │ → Green (income)
 * │ $15,234.50         │
 * └─────────────────────┘
 *
 * ┌─────────────────────┐
 * │ ⏰ Outstanding      │ → Orange (action needed)
 * │ $3,456.75          │
 * └─────────────────────┘
 *
 * ┌─────────────────────┐
 * │ ⚠️  Overdue         │ → Red (urgent)
 * │ $1,234.50          │
 * └─────────────────────┘
 *
 * ┌─────────────────────┐
 * │ ✅ Paid Rate        │ → Blue (positive)
 * │ 87%                 │
 * └─────────────────────┘
 * ```
 *
 * **States:**
 * - Loading: Shows spinner with "Calculating..."
 * - Success: Shows all metric cards
 * - Error: Shows error message with retry button
 * - Empty: Shows empty state if no invoices
 *
 * **Data Flow:**
 * ```
 * Screen mounts
 *     ↓
 * ViewModel observes active business
 *     ↓
 * GetRevenueMetricsUseCase calculates metrics
 *     ↓
 * RevenueMetricsV2 emitted with all values
 *     ↓
 * UI renders metric cards
 *     ↓
 * Auto-updates when business context changes
 * ```
 *
 * **Real-time Updates:**
 * When user switches business or invoices are updated:
 * 1. Business context changes
 * 2. ViewModel detects change via flatMapLatest
 * 3. Metrics recalculate automatically
 * 4. UI re-renders with new values
 * 5. No manual refresh needed
 *
 * @param viewModel RevenueDashboardViewModel managing metrics state
 * @param modifier Composable modifier
 * @param headerSlot Optional custom header content
 * @param footerSlot Optional custom footer content
 *
 * @see RevenueDashboardViewModel
 * @see RevenueMetricsV2
 */
@Suppress("DEPRECATION")
@Composable
fun RevenueDashboardScreen(
    modifier: Modifier = Modifier,
    viewModel: RevenueDashboardViewModel = hiltViewModel(),
    headerSlot: (@Composable ColumnScope.() -> Unit)? = null,
    footerSlot: (@Composable ColumnScope.() -> Unit)? = null,
) {
    val state by viewModel.uiState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (val s = state) {
            is RevenueDashboardUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is RevenueDashboardUiState.Success -> {
                RevenueDashboardContent(
                    modifier = modifier,
                    metrics = s.metrics,
                    headerSlot = headerSlot,
                    footerSlot = footerSlot,
                )
            }
            is RevenueDashboardUiState.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Error: ${s.message}", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun RevenueDashboardContent(
    modifier: Modifier = Modifier,
    metrics: RevenueMetricsV2,
    headerSlot: (@Composable ColumnScope.() -> Unit)? = null,
    footerSlot: (@Composable ColumnScope.() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .subtleVerticalGradient()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        headerSlot?.invoke(this) ?: run {
            Text(
                text = "Revenue Dashboard",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
        }

        // Color-coded MTD and YTD revenue cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                modifier = Modifier.weight(1f),
                title = "MTD Collected",
                value = CentsFormatter.formatCents(metrics.mtdRevenue),
                icon = Icons.Default.CheckCircle,
                backgroundColor = BizapColors.StatusPaid.copy(alpha = 0.08f),
                borderColor = BizapColors.StatusPaid.copy(alpha = 0.3f),
                accentColor = BizapColors.StatusPaid,
            )
            MetricCard(
                modifier = Modifier.weight(1f),
                title = "YTD Collected",
                value = CentsFormatter.formatCents(metrics.ytdRevenue),
                icon = Icons.Default.AttachMoney,
                backgroundColor = BizapColors.StatusSent.copy(alpha = 0.08f),
                borderColor = BizapColors.StatusSent.copy(alpha = 0.3f),
                accentColor = BizapColors.StatusSent,
            )
        }

        // Outstanding amount with orange accent
        if (metrics.outstandingAmount > 0L) {
            MetricCard(
                modifier = Modifier.fillMaxWidth(),
                title = "Outstanding (Expected)",
                value = CentsFormatter.formatCents(metrics.outstandingAmount),
                icon = Icons.Default.Schedule,
                backgroundColor = BizapColors.StatusOutstanding.copy(alpha = 0.08f),
                borderColor = BizapColors.StatusOutstanding.copy(alpha = 0.3f),
                accentColor = BizapColors.StatusOutstanding,
            )
        }

        // Overdue amount with warning color accent
        if (metrics.overdueAmount > 0L) {
            MetricCard(
                modifier = Modifier.fillMaxWidth(),
                title = "Overdue",
                value = CentsFormatter.formatCents(metrics.overdueAmount),
                icon = Icons.Default.Warning,
                backgroundColor = BizapColors.StatusOverdue.copy(alpha = 0.08f),
                borderColor = BizapColors.StatusOverdue.copy(alpha = 0.3f),
                accentColor = BizapColors.StatusOverdue,
            )
        }

        footerSlot?.invoke(this) ?: run {
            HorizontalDivider()
            Text(
                text = "Data as of today",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
