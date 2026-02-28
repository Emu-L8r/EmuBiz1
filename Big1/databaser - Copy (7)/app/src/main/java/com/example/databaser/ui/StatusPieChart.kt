package com.example.databaser.ui

import android.graphics.Color
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter

@Composable
fun StatusPieChart(unsent: Float, sent: Float, paid: Float) {
    val allZero = unsent == 0f && sent == 0f && paid == 0f

    val entries = if (allZero) {
        listOf(PieEntry(1f, ""))
    } else {
        if (paid > 0f) {
            listOf(
                PieEntry(unsent, "Unsent"),
                PieEntry(sent, "Sent"),
                PieEntry(paid, "Paid")
            )
        } else {
            listOf(
                PieEntry(unsent, "Unsent"),
                PieEntry(sent, "Sent")
            )
        }
    }

    val colors = if (allZero) {
        listOf(Color.LTGRAY)
    } else {
        if (paid > 0f) {
            listOf(
                Color.parseColor("#9C27B0"), // purple
                Color.parseColor("#2196F3"), // blue
                Color.parseColor("#4CAF50") // green
            )
        } else {
            listOf(
                Color.parseColor("#9C27B0"), // purple
                Color.parseColor("#2196F3") // blue
            )
        }
    }

    Card(modifier = Modifier.fillMaxWidth().height(200.dp).padding(16.dp)) {
        AndroidView(
            factory = { context ->
                PieChart(context).apply pieChart@{
                    this.data = PieData(PieDataSet(entries, "").apply {
                        this.colors = colors
                        this.valueTextColor = Color.BLACK
                        this.valueTextSize = 14f
                        this.valueFormatter = if (allZero) null else PercentFormatter(this@pieChart)
                        setDrawValues(!allZero)
                    })
                    this.description.isEnabled = false
                    this.isDrawHoleEnabled = true
                    this.setUsePercentValues(!allZero)
                    this.setDrawEntryLabels(!allZero)
                    this.legend.isEnabled = false
                    if (!allZero) {
                        this.animateY(1400)
                    }
                    this.invalidate()
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
