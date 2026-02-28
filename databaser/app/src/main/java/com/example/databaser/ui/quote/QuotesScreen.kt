package com.example.databaser.ui.quote

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.databaser.ui.Routes
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuotesScreen(navController: NavController, viewModel: QuotesViewModel = hiltViewModel()) {
    val quotes by viewModel.quotes.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Quotes") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("${Routes.SELECT_CUSTOMER}/quote") }) {
                Icon(Icons.Default.Add, contentDescription = "Add Quote")
            }
        }
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding)) {
            items(quotes) { quote ->
                Card(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                        .clickable { navController.navigate("${Routes.QUOTE_DETAILS}/${quote.customerId}/${quote.id}") }
                ) {
                    Text(
                        text = "Quote #${quote.id} - ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(quote.date))}",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}
