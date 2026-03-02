package com.emul8r.bizap.ui.revenue

import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.emul8r.bizap.domain.revenue.model.DailyRevenuePoint
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.line.lineSpec
import com.patrykandpatrick.vico.compose.component.shape.shader.verticalGradient
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf
import java.time.format.DateTimeFormatter

@Composable
fun RevenueTrendChart(
    dataPoints: List<DailyRevenuePoint>,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    
    val modelProducer = remember(dataPoints) {
        ChartEntryModelProducer(
            dataPoints.mapIndexed { index, point ->
                entryOf(index.toFloat(), (point.amount / 100f))
            }
        )
    }

    val dateFormatter = DateTimeFormatter.ofPattern("dd MMM")
    val bottomAxisFormatter = AxisValueFormatter<com.patrykandpatrick.vico.core.axis.AxisPosition.Horizontal.Bottom> { value, _ ->
        dataPoints.getOrNull(value.toInt())?.date?.format(dateFormatter) ?: ""
    }

    Chart(
        chart = lineChart(
            lines = listOf(
                lineSpec(
                    lineColor = primaryColor,
                    lineBackgroundShader = verticalGradient(
                        arrayOf(primaryColor.copy(alpha = 0.4f), primaryColor.copy(alpha = 0f))
                    )
                )
            )
        ),
        chartModelProducer = modelProducer,
        startAxis = rememberStartAxis(
            valueFormatter = { value, _ -> "$${value.toInt()}" }
        ),
        bottomAxis = rememberBottomAxis(
            valueFormatter = bottomAxisFormatter
        ),
        modifier = modifier.height(200.dp)
    )
}
