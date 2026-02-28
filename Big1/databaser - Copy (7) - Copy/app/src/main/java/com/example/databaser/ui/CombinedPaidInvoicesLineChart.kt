package com.example.databaser.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CombinedPaidInvoicesLineChart(
    modifier: Modifier = Modifier, 
    individualEntries: List<Entry>, 
    cumulativeEntries: List<Entry>
) {
    val lineDataSet = LineDataSet(individualEntries, "Paid Invoices").apply {
        color = MaterialTheme.colorScheme.primary.toArgb()
        valueTextColor = MaterialTheme.colorScheme.onSurface.toArgb()
        setCircleColor(MaterialTheme.colorScheme.primary.toArgb())
        circleHoleColor = MaterialTheme.colorScheme.primary.toArgb()
        circleRadius = 4f
        lineWidth = 2f
    }

    val cumulativeLineDataSet = LineDataSet(cumulativeEntries, "Cumulative Total").apply {
        color = MaterialTheme.colorScheme.secondary.toArgb()
        valueTextColor = MaterialTheme.colorScheme.onSurface.toArgb()
        setCircleColor(MaterialTheme.colorScheme.secondary.toArgb())
        circleHoleColor = MaterialTheme.colorScheme.secondary.toArgb()
        circleRadius = 4f
        lineWidth = 2f
    }

    Card(modifier = modifier.height(250.dp).padding(16.dp)) {
        AndroidView(
            factory = { context ->
                LineChart(context).apply {
                    data = LineData(lineDataSet, cumulativeLineDataSet)
                    xAxis.valueFormatter = object : ValueFormatter() {
                        private val sdf = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
                        override fun getFormattedValue(value: Float): String {
                            return sdf.format(Date(value.toLong()))
                        }
                    }
                    xAxis.position = XAxis.XAxisPosition.BOTTOM
                    xAxis.granularity = 1f
                    xAxis.setLabelRotationAngle(-45f)

                    axisRight.isEnabled = false
                    axisLeft.axisMinimum = 0f

                    description.isEnabled = false
                    legend.isEnabled = true
                    animateX(1500)
                    invalidate()
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
