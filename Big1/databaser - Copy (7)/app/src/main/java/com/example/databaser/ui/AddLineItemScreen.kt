package com.example.databaser.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.databaser.R
import com.example.databaser.data.LineItem
import com.example.databaser.viewmodel.PredefinedLineItemViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLineItemScreen(
    onAdd: (LineItem) -> Unit,
    predefinedLineItemViewModel: PredefinedLineItemViewModel = viewModel(factory = PredefinedLineItemViewModel.Factory)
) {
    var job by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var unitPrice by remember { mutableStateOf("") }
    val predefinedItems by predefinedLineItemViewModel.allPredefinedLineItems.collectAsState(initial = emptyList())
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(stringResource(R.string.add_line_item), modifier = Modifier.padding(bottom = 16.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                value = job,
                onValueChange = { job = it },
                label = { Text(stringResource(R.string.select_pre_filled_item)) },
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
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
                val newItem = LineItem(
                    id = 0, // Room will auto-generate
                    invoiceId = 0, // This will be set later
                    job = job,
                    details = details,
                    quantity = quantity.toIntOrNull() ?: 1,
                    unitPrice = unitPrice.toDoubleOrNull() ?: 0.0
                )
                onAdd(newItem)
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(stringResource(R.string.add_line_item))
        }
    }
}
