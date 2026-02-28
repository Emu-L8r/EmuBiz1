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
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PaidInvoicesCountBarChart(modifier: Modifier = Modifier, entries: List<BarEntry>) {
    val barDataSet = BarDataSet(entries, "Paid Invoices Count").apply {
        color = MaterialTheme.colorScheme.primary.toArgb()
        valueTextColor = MaterialTheme.colorScheme.onSurface.toArgb()
    }

    Card(modifier = modifier.height(250.dp).padding(16.dp)) {
        AndroidView(
            factory = { context ->
                BarChart(context).apply {
                    data = BarData(barDataSet)
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
                    animateY(1500)
                    invalidate()
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
