package com.example.databaser.ui.invoice

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.databaser.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceDetailsScreen(
    navController: NavController,
    customerId: Long,
    invoiceId: Long?,
    viewModel: InvoiceDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.pdfFile.collect { file ->
            val uri = FileProvider.getUriForFile(context, context.applicationContext.packageName + ".provider", file)
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = uri
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            context.startActivity(intent)
        }

        viewModel.event.collect { event ->
            when (event) {
                is InvoiceDetailsViewModel.Event.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }

                is InvoiceDetailsViewModel.Event.InvoiceSaved -> {
                    navController.popBackStack()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (invoiceId == 0L) stringResource(R.string.add_invoice) else stringResource(R.string.edit_invoice)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.errorMessage != null) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = uiState.errorMessage!!, style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            Column(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
                OutlinedTextField(
                    value = uiState.date,
                    onValueChange = { viewModel.onDateChange(it) },
                    label = { Text(stringResource(R.string.date)) },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = uiState.header,
                    onValueChange = { viewModel.onHeaderChange(it) },
                    label = { Text(stringResource(R.string.header)) },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = uiState.subHeader,
                    onValueChange = { viewModel.onSubHeaderChange(it) },
                    label = { Text(stringResource(R.string.sub_header)) },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = uiState.footer,
                    onValueChange = { viewModel.onFooterChange(it) },
                    label = { Text(stringResource(R.string.footer)) },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                )

                Row {
                    Text(stringResource(R.string.line_items), modifier = Modifier.weight(1f))
                    IconButton(onClick = { viewModel.addLineItem() }) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_line_item))
                    }
                }

                LazyColumn {
                    items(uiState.lineItems) { lineItem ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = lineItem.description,
                                onValueChange = { viewModel.onLineItemDescriptionChange(lineItem.id, it) },
                                label = { Text(stringResource(R.string.description)) },
                                modifier = Modifier.weight(1f).padding(end = 8.dp)
                            )
                            OutlinedTextField(
                                value = lineItem.quantity.toString(),
                                onValueChange = { viewModel.onLineItemQuantityChange(lineItem.id, it) },
                                label = { Text(stringResource(R.string.qty)) },
                                modifier = Modifier.weight(0.5f).padding(end = 8.dp)
                            )
                            OutlinedTextField(
                                value = lineItem.price.toString(),
                                onValueChange = { viewModel.onLineItemPriceChange(lineItem.id, it) },
                                label = { Text(stringResource(R.string.price)) },
                                modifier = Modifier.weight(0.5f)
                            )
                            IconButton(onClick = { viewModel.removeLineItem(lineItem) }) {
                                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.remove_line_item))
                            }
                        }
                    }
                }

                Text("${stringResource(R.string.total)}: ${uiState.total}")

                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { viewModel.saveInvoice(customerId) },
                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                    ) {
                        Text(stringResource(R.string.save))
                    }
                    OutlinedButton(
                        onClick = { viewModel.generatePdf() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.print_preview))
                    }
                }
            }
        }
    }
}
