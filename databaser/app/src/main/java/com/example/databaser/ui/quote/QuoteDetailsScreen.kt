package com.example.databaser.ui.quote

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.databaser.data.model.QuoteLineItem
import com.example.databaser.util.PdfGenerator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuoteDetailsScreen(
    navController: NavController,
    customerId: Long,
    quoteId: Long?,
    viewModel: QuoteDetailsViewModel = hiltViewModel()
) {
    val quote by viewModel.quote.collectAsState()
    val lineItems by viewModel.lineItems.collectAsState()
    val context = LocalContext.current

    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val currentDate = sdf.format(Date())

    var date by remember { mutableStateOf(currentDate) }
    var header by remember { mutableStateOf("") }
    var subHeader by remember { mutableStateOf("") }
    var footer by remember { mutableStateOf("") }

    LaunchedEffect(quote) {
        quote?.let {
            date = sdf.format(Date(it.date))
            header = it.header
            subHeader = it.subHeader
            footer = it.footer
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (quoteId == 0L) "Add Quote" else "Edit Quote") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("Date") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = header,
                onValueChange = { header = it },
                label = { Text("Header") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = subHeader,
                onValueChange = { subHeader = it },
                label = { Text("Sub-Header") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = footer,
                onValueChange = { footer = it },
                label = { Text("Footer") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            )

            Row {
                Text("Line Items", modifier = Modifier.weight(1f))
                IconButton(onClick = { viewModel.addLineItem() }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Line Item")
                }
            }

            LazyColumn {
                items(lineItems) { lineItem ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = lineItem.description,
                            onValueChange = { viewModel.updateLineItem(lineItem.copy(description = it)) },
                            label = { Text("Description") },
                            modifier = Modifier.weight(1f).padding(end = 8.dp)
                        )
                        OutlinedTextField(
                            value = lineItem.quantity.toString(),
                            onValueChange = { viewModel.updateLineItem(lineItem.copy(quantity = it.toIntOrNull() ?: 0)) },
                            label = { Text("Qty") },
                            modifier = Modifier.weight(0.5f).padding(end = 8.dp)
                        )
                        OutlinedTextField(
                            value = lineItem.price.toString(),
                            onValueChange = { viewModel.updateLineItem(lineItem.copy(price = it.toDoubleOrNull() ?: 0.0)) },
                            label = { Text("Price") },
                            modifier = Modifier.weight(0.5f)
                        )
                        IconButton(onClick = { viewModel.removeLineItem(lineItem) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove Line Item")
                        }
                    }
                }
            }

            Text("Total: ${lineItems.sumOf { it.quantity * it.price }}")

            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        if (date.isNotEmpty()) {
                            val dateInMillis = sdf.parse(date)?.time ?: System.currentTimeMillis()
                            viewModel.saveQuote(customerId, dateInMillis, header, subHeader, footer, lineItems)
                            navController.popBackStack()
                        } else {
                            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                ) {
                    Text("Save")
                }
                OutlinedButton(
                    onClick = {
                        val text = "Header: $header\nSub-Header: $subHeader\n${lineItems.joinToString("\n") { "${it.description} - ${it.quantity} - ${it.price}" }}\nFooter: $footer"
                        val file = PdfGenerator.generatePdf(context, text)
                        file?.let {
                            val uri = FileProvider.getUriForFile(context, context.applicationContext.packageName + ".provider", it)
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.data = uri
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            context.startActivity(intent)
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Print Preview")
                }
            }
        }
    }
}
