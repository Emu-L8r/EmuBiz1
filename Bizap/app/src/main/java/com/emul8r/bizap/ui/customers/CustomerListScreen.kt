package com.emul8r.bizap.ui.customers

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun CustomerListScreen(
    onCustomerClick: (Long) -> Unit,
    onViewSegments: (() -> Unit)? = null,
    onViewAnalytics: (() -> Unit)? = null,
    viewModel: CustomerViewModel = hiltViewModel()
) {
    val customers by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        if (onViewAnalytics != null) {
            OutlinedButton(
                onClick = onViewAnalytics,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.Insights, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("View Customer Analytics")
            }
        }
        if (onViewSegments != null) {
            OutlinedButton(
                onClick = onViewSegments,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.PieChart, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("View Customer Segments")
            }
        }
        // MainActivity's Scaffold provides the TopAppBar, so just show content
        CustomerList(customers = customers, onCustomerClick = onCustomerClick)
    }
}
