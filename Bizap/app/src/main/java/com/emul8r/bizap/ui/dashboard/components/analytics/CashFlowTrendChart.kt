package com.emul8r.bizap.ui.dashboard.components.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.emul8r.bizap.data.model.CashFlowTrendPoint
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries

/**
 * Cash Flow Trend Chart
 *
 * Shows 30-day invoiced vs. paid trends.
 * Helps users identify seasonal patterns and predict cash needs.
 */
@Composable
fun CashFlowTrendChart(
    dailyTrends: List<CashFlowTrendPoint>,
    modifier: Modifier = Modifier
) {
    val modelProducer = CartesianChartModelProducer()

    // Prepare chart data
    val invoicedAmounts = dailyTrends.map { it.invoicedCents / 100.0 }.toFloatArray()
    val paidAmounts = dailyTrends.map { it.paidCents / 100.0 }.toFloatArray()

    // Update chart with data
    modelProducer.runTransaction {
        lineSeries {
            series(invoicedAmounts.toList())
            series(paidAmounts.toList())
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Text(
            text = "Cash Flow Trend (30 Days)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Legend
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LegendItem("Invoiced", Color(0xFF1976D2))
            LegendItem("Paid", Color(0xFF388E3C))
        }

        // Chart
        if (dailyTrends.isNotEmpty()) {
            CartesianChartHost(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                modelProducer = modelProducer,
                layers = listOf(
                    rememberLineCartesianLayer(
                        lines = listOf(
                            androidx.compose.ui.graphics.Color(0xFF1976D2),
                            androidx.compose.ui.graphics.Color(0xFF388E3C)
                        )
                    )
                ),
                startAxis = rememberStartAxis(),
                bottomAxis = rememberBottomAxis(),
                isZoomEnabled = true
            )
        } else {
            Text(
                text = "No data available",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(32.dp)
            )
        }

        // Info text
        Text(
            text = "💡 Tip: Compare invoiced vs. paid to identify cash flow gaps.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(
        modifier = Modifier.wrapContentSize(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, shape = androidx.compose.foundation.shape.RoundedCornerShape(2.dp))
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

