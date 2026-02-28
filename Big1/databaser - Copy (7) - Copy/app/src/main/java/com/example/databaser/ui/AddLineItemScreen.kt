package com.example.databaser.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.databaser.R
import com.example.databaser.data.LineItem
import com.example.databaser.viewmodel.LineItemViewModel
import com.example.databaser.viewmodel.PredefinedLineItemViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLineItemScreen(
    invoiceId: Int,
    onAdd: () -> Unit,
    onBackClick: () -> Unit,
) {
    val lineItemViewModel: LineItemViewModel = hiltViewModel()
    val predefinedLineItemViewModel: PredefinedLineItemViewModel = hiltViewModel()
    var job by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var unitPrice by remember { mutableStateOf("") }
    val predefinedItems by predefinedLineItemViewModel.allPredefinedLineItems.collectAsState(initial = emptyList())
    var expanded by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_line_item)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                TextField(
                    value = job,
                    onValueChange = { 
                        job = it 
                        details = ""
                        unitPrice = ""
                    },
                    label = { Text(stringResource(R.string.select_or_enter_item)) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    predefinedItems.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item.job) },
                            onClick = {
                                job = item.job
                                details = item.details
                                unitPrice = item.unitPrice.toString()
                                expanded = false
                            }
                        )
                    }
                }
            }

            TextField(
                value = details,
                onValueChange = { details = it },
                label = { Text(stringResource(R.string.details)) },
                modifier = Modifier.fillMaxWidth()
            )

            TextField(
                value = quantity,
                onValueChange = { quantity = it },
                label = { Text(stringResource(R.string.quantity)) },
                modifier = Modifier.fillMaxWidth()
            )

            TextField(
                value = unitPrice,
                onValueChange = { unitPrice = it },
                label = { Text(stringResource(R.string.unit_price)) },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    if (invoiceId != 0) {
                        coroutineScope.launch {
                            val newItem = LineItem(
                                id = 0, // Room will auto-generate
                                invoiceId = invoiceId, // Use the correct invoiceId
                                job = job,
                                details = details,
                                quantity = quantity.toIntOrNull() ?: 1,
                                unitPrice = unitPrice.toDoubleOrNull() ?: 0.0
                            )
                            lineItemViewModel.addLineItem(newItem)
                            onAdd()
                        }
                    }
                },
                enabled = invoiceId != 0,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(stringResource(R.string.add_line_item))
            }
        }
    }
}
