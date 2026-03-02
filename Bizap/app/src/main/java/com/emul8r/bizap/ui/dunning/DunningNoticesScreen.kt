package com.emul8r.bizap.ui.dunning

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.emul8r.bizap.domain.invoice.model.DunningNotice
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DunningNoticesScreen(
    viewModel: DunningNoticesViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val uiState = viewModel.uiState.collectAsState().value

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dunning Notices", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshDunningNotices() }) {
                        Icon(Icons.Default.Refresh, "Refresh")
                    }
                }
            )
        }
    ) { paddingValues ->
        when (uiState) {
            is DunningUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is DunningUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Error: ${uiState.message}",
                        color = Color.Red,
                        fontSize = 16.sp
                    )
                }
            }
            is DunningUiState.Success -> {
                DunningNoticesContent(
                    notices = uiState.notices,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
fun DunningNoticesContent(
    notices: List<DunningNotice>,
    modifier: Modifier = Modifier
) {
    if (notices.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No pending dunning notices", fontSize = 16.sp)
        }
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(notices) { notice ->
                DunningNoticeCard(notice)
            }
        }
    }
}

@Composable
fun DunningNoticeCard(notice: DunningNotice) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = notice.customerName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Customer ID: ${notice.customerId}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                Surface(
                    color = when (notice.noticeLevel) {
                        1 -> Color(0xFFFFE082)
                        2 -> Color(0xFFFF9800)
                        else -> Color(0xFFE53935)
                    },
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "Notice ${notice.noticeLevel}",
                        modifier = Modifier.padding(6.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Amount Due",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "$${String.format("%.2f", notice.totalAmountDue)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE53935)
                    )
                }
                Column {
                    Text(
                        text = "Days Overdue",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "${notice.daysSinceDue}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column {
                    Text(
                        text = "Invoices",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "${notice.overdueInvoices.size}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Sent: ${notice.sentDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = "Next Action: ${notice.nextActionDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { /* Send notice action */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1976D2)
                )
            ) {
                Text("Send Notice", color = Color.White)
            }
        }
    }
}

