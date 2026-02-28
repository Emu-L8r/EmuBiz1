package com.example.databaser.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.databaser.R
import com.example.databaser.data.PredefinedLineItem
import com.example.databaser.viewmodel.PredefinedLineItemViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPredefinedLineItemScreen(
    onItemSaved: () -> Unit,
    predefinedLineItemViewModel: PredefinedLineItemViewModel = viewModel(factory = PredefinedLineItemViewModel.Factory)
) {
    var job by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }
    var unitPrice by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_pre_filled_item)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            ThemedTextField(
                value = job,
                onValueChange = { job = it },
                label = stringResource(R.string.job),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            ThemedTextField(
                value = details,
                onValueChange = { details = it },
                label = stringResource(R.string.details),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            ThemedTextField(
                value = unitPrice,
                onValueChange = { unitPrice = it },
                label = stringResource(R.string.unit_price),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                val newItem = PredefinedLineItem(
                    job = job,
                    details = details,
                    unitPrice = unitPrice.toDoubleOrNull() ?: 0.0
                )
                predefinedLineItemViewModel.addPredefinedLineItem(newItem)
                onItemSaved()
            }) {
                Text(stringResource(R.string.save))
            }
        }
    }
}
