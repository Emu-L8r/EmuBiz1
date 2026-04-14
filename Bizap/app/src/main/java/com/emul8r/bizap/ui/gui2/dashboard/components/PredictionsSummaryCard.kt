package com.emul8r.bizap.ui.gui2.dashboard.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emul8r.bizap.domain.model.insights.CashFlowPrediction
import com.emul8r.bizap.domain.model.insights.RiskLevel
import com.emul8r.bizap.ui.designsystem.BizapColors
import timber.log.Timber
import java.text.NumberFormat
import java.util.Locale

/**
 * Predictions Summary Card - Dashboard widget showing key predictions at a glance.
 *
 * **Displays:**
 * - Critical alerts (if any)
 * - 30-day cash flow forecast
 * - At-risk invoice count
 * - Navigation link to full predictions screen
 *
 * **Design Features**:
 * - Smooth gradient backgrounds
 * - Animated metrics display
 * - Color-coded risk levels
 * - Professional Material 3 styling
 * - Intuitive layout
 *
 * **Usage:**
 * ```kotlin
 * PredictionsSummaryCard(
 *     forecast = forecast30,
 *     alerts = listOf("Critical: Negative cash flow projected"),
 *     riskCount = 5,
 *     onNavigateToPredictions = { /* navigate */ }
 * )
 * ```
 */
@Composable
fun PredictionsSummaryCard(
    forecast: CashFlowPrediction,
    alerts: List<String>,
    riskCount: Int = 0,
    onNavigateToPredictions: () -> Unit
) {
    Timber.d("🎨 Rendering PredictionsSummaryCard")

    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
    val balanceText = currencyFormat.format(forecast.predictedBalance / 100.0)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigateToPredictions() }
            .animateContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = CardDefaults.outlinedCardBorder(),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp,
            pressedElevation = 12.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header with title and navigate icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "📊 Predictions",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        "30-day business forecast",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Medium
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Navigate to predictions",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )

            // Critical alerts section (if any)
            AnimatedVisibility(
                visible = alerts.isNotEmpty(),
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            BizapColors.AnalyticsAtRisk.copy(alpha = 0.12f),
                            RoundedCornerShape(10.dp)
                        )
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Alert",
                            modifier = Modifier.size(16.dp),
                            tint = BizapColors.AnalyticsAtRisk
                        )
                        Text(
                            "🚨 Critical Alert",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BizapColors.AnalyticsAtRisk
                        )
                    }
                    alerts.take(1).forEach { alert ->
                        Text(
                            alert,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Forecast and risk metrics in elegant grid
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Main metric: 30-Day Balance
                PredictionMetricBox(
                    label = "30-Day Projected Balance",
                    value = balanceText,
                    riskLevel = forecast.riskLevel,
                    confidence = forecast.confidence,
                    isMain = true
                )

                // Secondary metrics in row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // At-Risk Invoices
                    PredictionMetricBox(
                        label = "At-Risk",
                        value = riskCount.toString(),
                        riskLevel = if (riskCount > 0) RiskLevel.RISK else RiskLevel.HEALTHY,
                        modifier = Modifier.weight(1f),
                        isCompact = true
                    )

                    // Trend indicator
                    PredictionMetricBox(
                        label = "Trend",
                        value = if (forecast.predictedBalance >= 0) "↑ Positive" else "↓ Negative",
                        riskLevel = if (forecast.predictedBalance >= 0) RiskLevel.HEALTHY else RiskLevel.RISK,
                        modifier = Modifier.weight(1f),
                        isCompact = true
                    )
                }
            }

            // Footer: CTA
            Text(
                "Tap to view detailed forecasts, risk analysis & recommendations →",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

/**
 * Enhanced metric box with color-coded design.
 */
@Composable
private fun PredictionMetricBox(
    label: String,
    value: String,
    riskLevel: RiskLevel,
    modifier: Modifier = Modifier,
    confidence: Double = 0.0,
    isMain: Boolean = false,
    isCompact: Boolean = false
) {
    val (bgColor, textColor) = when (riskLevel) {
        RiskLevel.HEALTHY -> BizapColors.AnalyticsExcellent to BizapColors.AnalyticsExcellent
        RiskLevel.CAUTION -> BizapColors.AnalyticsWarning to BizapColors.AnalyticsWarning
        RiskLevel.RISK -> BizapColors.AnalyticsAtRisk to BizapColors.AnalyticsAtRisk
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (isMain) Modifier.height(90.dp) else Modifier.height(70.dp)),
        colors = CardDefaults.cardColors(
            containerColor = bgColor.copy(alpha = 0.08f)
        ),
        border = CardDefaults.outlinedCardBorder(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                label,
                fontSize = if (isCompact) 10.sp else 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(if (isCompact) 2.dp else 4.dp))

            Text(
                value,
                fontSize = if (isCompact) 16.sp else 20.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )

            if (isMain && confidence > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Confidence: ${String.format("%.0f", confidence)}%",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
        }
    }
}


