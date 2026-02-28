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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.databaser.R
import com.example.databaser.Screen
import com.example.databaser.data.Note
import com.example.databaser.viewmodel.BusinessInfoViewModel
import com.example.databaser.viewmodel.DashboardViewModel
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
    navController: NavHostController,
    dashboardViewModel: DashboardViewModel = hiltViewModel(),
    businessInfoViewModel: BusinessInfoViewModel = hiltViewModel()
) {
    val dashboardState by dashboardViewModel.uiState.collectAsStateWithLifecycle()
    val filteredNotes by dashboardViewModel.filteredNotes.collectAsStateWithLifecycle()
    val searchQuery by dashboardViewModel.searchQuery.collectAsStateWithLifecycle()
    val businessInfo by businessInfoViewModel.businessInfo.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(businessInfo) {
        if (businessInfo == null || businessInfo?.name?.isBlank() == true) {
            showDialog = true
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { /* Prevent dismissing by clicking outside */ },
            title = { Text("Business Information Required") },
            text = { Text("Please enter your business details in the settings to proceed.") },
            confirmButton = {
                Button(onClick = {
                    navController.navigate(Screen.BusinessInfo.route)
                }) {
                    Text("Go to Settings")
                }
            }
        )
    }

    Scaffold(
        topBar = { 
            AppTopAppBar(
                title = stringResource(id = R.string.dashboard), 
                navController = navController,
                canNavigateBack = false
            ) 
        }
    ) { padding ->
        if (dashboardState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding).padding(16.dp)) {
                item {
                    SearchBar(
                        query = searchQuery,
                        onQueryChange = { dashboardViewModel.onSearchQueryChange(it) },
                        onSearch = { },
                        active = false,
                        onActiveChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(stringResource(id = R.string.search)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search",
                                    modifier = Modifier.clickable { dashboardViewModel.onSearchQueryChange("") }
                                )
                            }
                        }
                    ) { }
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
                item {
                    RecentNotesSummary(notes = filteredNotes, navController = navController)
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
                            containerColor = MaterialTheme.colorScheme.primaryContainer
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
            color = MaterialTheme.colorScheme.secondaryContainer
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
            containerColor = MaterialTheme.colorScheme.primaryContainer
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
fun RecentNotesSummary(notes: List<Note>, navController: NavHostController) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = stringResource(id = R.string.recent_notes), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            if (notes.isEmpty()) {
                Text(text = stringResource(R.string.no_notes_found), style = MaterialTheme.typography.bodyLarge)
            } else {
                LazyColumn(modifier = Modifier.height(200.dp)) {
                    items(notes.take(5)) { note ->
                        NoteListItem(note = note, onClick = { navController.navigate(Screen.AddEditNote.createRoute(note.id, note.customerId, note.invoiceId, note.quoteId)) })
                    }
                }
            }
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

    val dates = invoiceStatusEntries["dates"]?.map { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it.x.toLong())) } ?: emptyList()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Invoice Trends", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    BarChart(context).apply {
                        description.isEnabled = false
                        legend.textColor = onSurfaceColor
                        xAxis.apply {
                            position = XAxis.XAxisPosition.BOTTOM
                            valueFormatter = IndexAxisValueFormatter(dates)
                            textColor = onSurfaceColor
                            setDrawGridLines(false)
                        }
                        axisLeft.apply {
                            textColor = onSurfaceColor
                            setDrawGridLines(false)
                        }
                        axisRight.isEnabled = false
                    }
                },
                update = { chart ->
                    val paidDataSet = BarDataSet(invoiceStatusEntries["Paid"] ?: emptyList(), "Paid").apply { color = paidColor }
                    val sentDataSet = BarDataSet(invoiceStatusEntries["Sent"] ?: emptyList(), "Sent").apply { color = sentColor }
                    val unsentDataSet = BarDataSet(invoiceStatusEntries["Unsent"] ?: emptyList(), "Unsent").apply { color = unsentColor }

                    chart.data = BarData(paidDataSet, sentDataSet, unsentDataSet).apply {
                        barWidth = 0.5f
                    }
                    chart.groupBars(-0.5f, 0.25f, 0.0f)
                    chart.invalidate()
                }
            )
        }
    }
}
