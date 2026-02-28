package com.example.databaser.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.databaser.R
import com.example.databaser.viewmodel.DashboardViewModel
import com.example.databaser.viewmodel.NoteViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onUnsentInvoicesClick: () -> Unit,
    onSentInvoicesClick: () -> Unit,
    onUnsentQuotesClick: () -> Unit,
    onSentQuotesClick: () -> Unit,
    onNotesClick: () -> Unit,
    onNavigate: (String) -> Unit,
    dashboardViewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory),
    noteViewModel: NoteViewModel = viewModel(factory = NoteViewModel.Factory)
) {
    val dashboardState by dashboardViewModel.uiState.collectAsState()
    val notesCount by noteViewModel.getNotesCount().collectAsState(initial = 0)

    Scaffold(
        topBar = { AppTopAppBar(title = stringResource(id = R.string.dashboard), onNavigate = onNavigate) }
    ) {
        if (dashboardState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(modifier = Modifier.padding(it).padding(16.dp)) {
                item {
                    NotesSummaryCard(notesCount = notesCount, onNotesClick = onNotesClick)
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    QuoteSummaryCards(
                        unsentQuotesCount = dashboardState.unsentQuotes.size,
                        sentQuotesCount = dashboardState.sentQuotes.size,
                        onUnsentQuotesClick = onUnsentQuotesClick,
                        onSentQuotesClick = onSentQuotesClick
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    val paidCount = dashboardState.paidInvoices.size
                    val sentCount = dashboardState.sentInvoices.size
                    val unsentCount = dashboardState.unsentInvoices.size

                    val entries = listOfNotNull(
                        if (paidCount > 0) PieEntry(paidCount.toFloat(), "Paid") else null,
                        if (sentCount > 0) PieEntry(sentCount.toFloat(), "Sent") else null,
                        if (unsentCount > 0) PieEntry(unsentCount.toFloat(), "Unsent") else null
                    )

                    val pieChartColors = listOfNotNull(
                        if (paidCount > 0) MaterialTheme.colorScheme.primary else null,
                        if (sentCount > 0) MaterialTheme.colorScheme.secondary else null,
                        if (unsentCount > 0) MaterialTheme.colorScheme.error else null
                    ).map { color -> color.toArgb() }

                    val legendTextColor = MaterialTheme.colorScheme.onSurface.toArgb()

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "Invoice Status Overview", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            if (entries.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(text = "No data available")
                                }
                            } else {
                                AndroidView(
                                    modifier = Modifier.fillMaxSize(),
                                    factory = { context ->
                                        PieChart(context).apply {
                                            description.isEnabled = false
                                            isDrawHoleEnabled = true
                                            setHoleColor(Color.Transparent.toArgb())
                                            setEntryLabelColor(legendTextColor)
                                            setUsePercentValues(true)
                                        }
                                    },
                                    update = { chart ->
                                        val dataSet = PieDataSet(entries, "").apply {
                                            this.colors = pieChartColors
                                            valueTextColor = legendTextColor
                                            valueTextSize = 12f
                                        }
                                        val pieData = PieData(dataSet).apply {
                                            setValueFormatter(PercentFormatter(chart))
                                            setValueTextColor(legendTextColor)
                                            setValueTextSize(12f)
                                        }
                                        chart.data = pieData
                                        chart.legend.textColor = legendTextColor
                                        chart.animateY(1400)
                                        chart.invalidate()
                                    }
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    InvoiceStatusBarChart(invoiceStatusEntries = dashboardState.invoiceStatusEntries)
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    SummaryCards(
                        unsentInvoicesTotal = dashboardState.unsentInvoicesTotal,
                        sentInvoicesTotal = dashboardState.sentInvoicesTotal,
                        unsentInvoicesCount = dashboardState.unsentInvoices.size,
                        sentInvoicesCount = dashboardState.sentInvoices.size,
                        onUnsentInvoicesClick = onUnsentInvoicesClick,
                        onSentInvoicesClick = onSentInvoicesClick
                    )
                }
            }
        }
    }
}

@Composable
fun SummaryCards(
    unsentInvoicesTotal: Double,
    sentInvoicesTotal: Double,
    unsentInvoicesCount: Int,
    sentInvoicesCount: Int,
    onUnsentInvoicesClick: () -> Unit,
    onSentInvoicesClick: () -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
        SummaryCard(
            title = stringResource(R.string.unsent),
            total = unsentInvoicesTotal.toFloat(),
            count = unsentInvoicesCount,
            onClick = onUnsentInvoicesClick,
            color = MaterialTheme.colorScheme.errorContainer
        )
        SummaryCard(
            title = stringResource(R.string.sent),
            total = sentInvoicesTotal.toFloat(),
            count = sentInvoicesCount,
            onClick = onSentInvoicesClick,
            color = MaterialTheme.colorScheme.tertiaryContainer
        )
    }
}

@Composable
fun QuoteSummaryCards(
    unsentQuotesCount: Int,
    sentQuotesCount: Int,
    onUnsentQuotesClick: () -> Unit,
    onSentQuotesClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = stringResource(id = R.string.quotes), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                QuoteCard(title = stringResource(R.string.unsent_quotes), count = unsentQuotesCount, onClick = onUnsentQuotesClick)
                QuoteCard(title = stringResource(R.string.sent_quotes), count = sentQuotesCount, onClick = onSentQuotesClick)
            }
        }
    }
}

@Composable
fun NotesSummaryCard(notesCount: Int, onNotesClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onNotesClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = stringResource(id = R.string.notes), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = stringResource(R.string.total_notes, notesCount), style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    total: Float,
    count: Int,
    onClick: () -> Unit,
    color: Color
) {
    Card(
        modifier = Modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = stringResource(R.string.unsent_total, total, count), style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun QuoteCard(
    title: String,
    count: Int,
    onClick: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable(onClick = onClick)) {
        Icon(Icons.AutoMirrored.Outlined.Send, contentDescription = null, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.padding(4.dp))
        Text(text = "$title ($count)", style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun InvoiceStatusBarChart(invoiceStatusEntries: Map<String, List<BarEntry>>) {
    val paidColor = MaterialTheme.colorScheme.primary.toArgb()
    val sentColor = MaterialTheme.colorScheme.secondary.toArgb()
    val unsentColor = MaterialTheme.colorScheme.error.toArgb()
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface.toArgb()

    val dates = invoiceStatusEntries["dates"]?.map { 
        val sdf = SimpleDateFormat("dd/MM", Locale.getDefault())
        sdf.format(Date(it.x.toLong()))
    } ?: emptyList()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Invoice Status Over Time", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            if (invoiceStatusEntries.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "No data available")
                }
            } else {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { context ->
                        BarChart(context).apply {
                            description.isEnabled = false
                            legend.isEnabled = true
                            xAxis.apply {
                                position = XAxis.XAxisPosition.BOTTOM
                                setDrawGridLines(false)
                                granularity = 1f
                                isGranularityEnabled = true
                                valueFormatter = IndexAxisValueFormatter(dates)
                            }
                            axisLeft.apply {
                                setDrawGridLines(false)
                            }
                            axisRight.isEnabled = false
                        }
                    },
                    update = { chart ->
                        val paidDataSet = BarDataSet(invoiceStatusEntries["Paid"], "Paid").apply {
                            color = paidColor
                            valueTextColor = onSurfaceColor
                        }
                        val sentDataSet = BarDataSet(invoiceStatusEntries["Sent"], "Sent").apply {
                            color = sentColor
                            valueTextColor = onSurfaceColor
                        }
                        val unsentDataSet = BarDataSet(invoiceStatusEntries["Unsent"], "Unsent").apply {
                            color = unsentColor
                            valueTextColor = onSurfaceColor
                        }

                        chart.data = BarData(paidDataSet, sentDataSet, unsentDataSet).apply {
                            barWidth = 0.2f
                        }
                        chart.xAxis.textColor = onSurfaceColor
                        chart.axisLeft.textColor = onSurfaceColor
                        chart.groupBars(0f, 0.4f, 0.0f)
                        chart.invalidate()
                    }
                )
            }
        }
    }
}