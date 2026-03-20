package com.emul8r.bizap.ui.gui2.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emul8r.bizap.domain.model.gui2.RiskMetricsV2
import com.emul8r.bizap.ui.gui2.common.*

/**
 * GUI2 risk analytics screen.
 * Shows risk tier breakdown: high-risk, at-risk, and healthy invoices.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiskAnalyticsScreenV2(
    businessId: Long,
    onBack: () -> Unit,
    viewModel: RiskAnalyticsViewModelV2 = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Risk Dashboard") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is RiskAnalyticsUiStateV2.Loading -> LoadingIndicatorV2(
                modifier = Modifier.padding(paddingValues)
            )
            is RiskAnalyticsUiStateV2.Error -> ErrorStateV2(
                message = state.message,
                modifier = Modifier.padding(paddingValues)
            )
            is RiskAnalyticsUiStateV2.Success -> RiskAnalyticsContentV2(
                metrics = state.metrics,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
private fun RiskAnalyticsContentV2(
    metrics: RiskMetricsV2,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionHeaderV2("Invoice Health")

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCardV2(
                label = "High Risk",
                value = "${metrics.highRiskCount}",
                supportingText = "60+ days overdue",
                modifier = Modifier.weight(1f)
            )
            MetricCardV2(
                label = "At Risk",
                value = "${metrics.atRiskCount}",
                supportingText = "30–59 days overdue",
                modifier = Modifier.weight(1f)
            )
        }

        MetricCardV2(
            label = "Healthy",
            value = "${metrics.healthyCount}",
            supportingText = "Paid or not yet due",
            modifier = Modifier.fillMaxWidth()
        )

        HorizontalDivider()
        SectionHeaderV2("Exposure")

        MetricCardV2(
            label = "Total Overdue Invoices",
            value = "${metrics.overdueCount}",
            modifier = Modifier.fillMaxWidth()
        )

        MetricCardV2(
            label = "Total Outstanding",
            value = formatCents(metrics.totalOutstandingCents),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}
