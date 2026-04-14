package com.emul8r.bizap.ui.predictions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emul8r.bizap.domain.model.insights.RiskLevel
import com.emul8r.bizap.domain.prediction.InvoiceRiskScore
import com.emul8r.bizap.domain.prediction.PredictionsViewModel
import com.emul8r.bizap.ui.designsystem.BizapColors
import timber.log.Timber
import java.text.NumberFormat
import java.util.Locale

/**
 * Predictions Screen - AI-powered business insights and forecasts.
 *
 * **Displays:**
 * - 30/60/90 day cash flow forecasts
 * - Risk-scored invoices requiring attention
 * - Critical business alerts
 * - Actionable recommendations
 *
 * **Usage:**
 * ```kotlin
 * PredictionsScreen(
 *     onNavigateToInvoice = { invoiceId -> /* Navigate */ },
 *     onNavigateBack = { /* Back */ }
 * )
 * ```
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PredictionsScreen(
    viewModel: PredictionsViewModel = hiltViewModel(),
    onNavigateToInvoice: (Long) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val forecast30 by viewModel.cashFlowForecast30.collectAsStateWithLifecycle()
    val forecast60 by viewModel.cashFlowForecast60.collectAsStateWithLifecycle()
    val forecast90 by viewModel.cashFlowForecast90.collectAsStateWithLifecycle()
    val riskInvoices by viewModel.riskInvoices.collectAsStateWithLifecycle()
    val criticalAlerts by viewModel.criticalAlerts.collectAsStateWithLifecycle()
    val summary by viewModel.summary.collectAsStateWithLifecycle()

    Timber.d("🎨 Rendering PredictionsScreen")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Business Predictions", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ===== CRITICAL ALERTS SECTION =====
            if (criticalAlerts.isNotEmpty()) {
                item {
                    CriticalAlertsSection(alerts = criticalAlerts)
                }
            }

            // ===== SUMMARY METRICS SECTION =====
            item {
                SummaryMetricsSection(summary)
            }

            // ===== CASH FLOW FORECAST SECTION =====
            item {
                Text(
                    "💰 Cash Flow Forecast",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                CashFlowForecastCard(
                    title = "30-Day Outlook",
                    forecast = forecast30,
                    backgroundColor = BizapColors.AnalyticsExcellent.copy(alpha = 0.1f)
                )
            }

            item {
                CashFlowForecastCard(
                    title = "60-Day Outlook",
                    forecast = forecast60,
                    backgroundColor = BizapColors.AnalyticsWarning.copy(alpha = 0.1f)
                )
            }

            item {
                CashFlowForecastCard(
                    title = "90-Day Outlook",
                    forecast = forecast90,
                    backgroundColor = BizapColors.AnalyticsAtRisk.copy(alpha = 0.1f)
                )
            }

            // ===== RISK ANALYSIS SECTION =====
            if (riskInvoices.isNotEmpty()) {
                item {
                    Text(
                        "⚠️ At-Risk Invoices",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }

                items(riskInvoices.size) { index ->
                    RiskInvoiceCard(
                        risk = riskInvoices[index],
                        onClick = { onNavigateToInvoice(riskInvoices[index].invoiceId) }
                    )
                }
            } else {
                item {
                    EmptyStateCard(
                        title = "No At-Risk Invoices",
                        description = "All invoices are on track for timely payment.",
                        icon = Icons.Default.CheckCircle
                    )
                }
            }

            // ===== RECOMMENDATIONS SECTION =====
            item {
                RecommendationsSection(
                    forecast30 = forecast30,
                    forecast90 = forecast90,
                    riskCount = riskInvoices.size
                )
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

/**
 * Critical Alerts Card - Shows urgent issues requiring attention.
 */
@Composable
private fun CriticalAlertsSection(alerts: List<String>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                BizapColors.AnalyticsAtRisk.copy(alpha = 0.1f),
                RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = BizapColors.AnalyticsAtRisk.copy(alpha = 0.05f)
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "🚨 Critical Alerts",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = BizapColors.AnalyticsAtRisk
            )

            alerts.forEach { alert ->
                Text(
                    alert,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

/**
 * Summary Metrics - High-level overview.
 */
@Composable
private fun SummaryMetricsSection(summary: PredictionsViewModel.PredictionsSummary) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.primaryContainer,
                RoundedCornerShape(12.dp)
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MetricBox(
            label = "At-Risk",
            value = summary.totalRiskInvoices.toString(),
            color = BizapColors.AnalyticsWarning,
            modifier = Modifier.weight(1f)
        )

        MetricBox(
            label = "Critical",
            value = summary.criticalRiskCount.toString(),
            color = BizapColors.AnalyticsAtRisk,
            modifier = Modifier.weight(1f)
        )

        MetricBox(
            label = "Confidence",
            value = "${String.format("%.0f", summary.averageConfidence)}%",
            color = BizapColors.AnalyticsExcellent,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Cash Flow Forecast Card.
 */
@Composable
private fun CashFlowForecastCard(
    title: String,
    forecast: com.emul8r.bizap.domain.model.insights.CashFlowPrediction,
    backgroundColor: Color
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
    val balanceText = currencyFormat.format(forecast.predictedBalance / 100.0)
    val inflowsText = currencyFormat.format(forecast.projectedInflows / 100.0)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Projected Balance",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                    Text(
                        balanceText,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            forecast.predictedBalance < 0 -> BizapColors.AnalyticsAtRisk
                            forecast.predictedBalance < 50000 -> BizapColors.AnalyticsWarning
                            else -> BizapColors.AnalyticsExcellent
                        }
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Risk Level: ${forecast.riskLevel}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = when (forecast.riskLevel) {
                            RiskLevel.RISK -> BizapColors.AnalyticsAtRisk
                            RiskLevel.CAUTION -> BizapColors.AnalyticsWarning
                            RiskLevel.HEALTHY -> BizapColors.AnalyticsExcellent
                        }
                    )
                    Text(
                        "Confidence: ${String.format("%.0f", forecast.confidence)}%",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
            }

            Text(
                "Inflows: $inflowsText",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )

            if (forecast.recommendations.isNotEmpty()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                forecast.recommendations.take(2).forEach { recommendation ->
                    Text(
                        "💡 $recommendation",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

/**
 * Risk Invoice Card - Shows individual invoice risk.
 */
@Composable
private fun RiskInvoiceCard(
    risk: InvoiceRiskScore,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                when (risk.level) {
                    RiskLevel.RISK -> BizapColors.AnalyticsAtRisk.copy(alpha = 0.1f)
                    RiskLevel.CAUTION -> BizapColors.AnalyticsWarning.copy(alpha = 0.1f)
                    RiskLevel.HEALTHY -> BizapColors.AnalyticsExcellent.copy(alpha = 0.1f)
                },
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = when (risk.level) {
                RiskLevel.RISK -> BizapColors.AnalyticsAtRisk.copy(alpha = 0.05f)
                RiskLevel.CAUTION -> BizapColors.AnalyticsWarning.copy(alpha = 0.05f)
                RiskLevel.HEALTHY -> BizapColors.AnalyticsExcellent.copy(alpha = 0.05f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Invoice #${risk.invoiceId}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    risk.factors.joinToString(", "),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${String.format("%.0f", risk.score)}/100",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (risk.level) {
                        RiskLevel.RISK -> BizapColors.AnalyticsAtRisk
                        RiskLevel.CAUTION -> BizapColors.AnalyticsWarning
                        RiskLevel.HEALTHY -> BizapColors.AnalyticsExcellent
                    }
                )
                Text(
                    "${risk.level}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = when (risk.level) {
                        RiskLevel.RISK -> BizapColors.AnalyticsAtRisk
                        RiskLevel.CAUTION -> BizapColors.AnalyticsWarning
                        RiskLevel.HEALTHY -> BizapColors.AnalyticsExcellent
                    }
                )
            }
        }
    }
}

/**
 * Empty State Card.
 */
@Composable
private fun EmptyStateCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "Icon",
                modifier = Modifier.size(48.dp),
                tint = BizapColors.AnalyticsExcellent
            )

            Text(
                title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                description,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

/**
 * Recommendations Section.
 */
@Composable
private fun RecommendationsSection(
    forecast30: com.emul8r.bizap.domain.model.insights.CashFlowPrediction,
    forecast90: com.emul8r.bizap.domain.model.insights.CashFlowPrediction,
    riskCount: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f),
                RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "📋 Recommendations",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Combine all recommendations
            val allRecommendations = (forecast30.recommendations + forecast90.recommendations).distinct()
            allRecommendations.take(4).forEach { recommendation ->
                Text(
                    "• $recommendation",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                )
            }

            if (riskCount == 0) {
                Text(
                    "✅ No immediate risks detected. Continue monitoring for changes.",
                    fontSize = 12.sp,
                    color = BizapColors.AnalyticsExcellent,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * Metric Box for summary display.
 */
@Composable
private fun MetricBox(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(
                color.copy(alpha = 0.2f),
                RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        Text(
            value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}


