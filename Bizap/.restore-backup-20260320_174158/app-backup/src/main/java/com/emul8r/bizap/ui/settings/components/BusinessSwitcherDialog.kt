package com.emul8r.bizap.ui.settings.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emul8r.bizap.ui.settings.BusinessSwitcherViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessSwitcherDialog(
    onDismiss: () -> Unit,
    viewModel: BusinessSwitcherViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddBusinessField by remember { mutableStateOf(false) }
    var newBusinessName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Switch Business") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.profiles) { profile ->
                        val isActive = profile.id == uiState.activeProfileId
                        ListItem(
                            modifier = Modifier
                                .clickable { 
                                    viewModel.switchBusiness(profile.id)
                                    onDismiss()
                                },
                            headlineContent = { 
                                Text(
                                    text = profile.businessName,
                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                                ) 
                            },
                            supportingContent = { Text(profile.abn) },
                            leadingContent = { 
                                Icon(
                                    imageVector = Icons.Default.Business, 
                                    contentDescription = null,
                                    tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                ) 
                            },
                            trailingContent = {
                                if (isActive) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Active",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            colors = ListItemDefaults.colors(
                                containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
                            )
                        )
                    }
                }

                if (showAddBusinessField) {
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = newBusinessName,
                        onValueChange = { newBusinessName = it },
                        label = { Text("New Business Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            if (showAddBusinessField) {
                Button(
                    onClick = {
                        if (newBusinessName.isNotBlank()) {
                            viewModel.createNewBusiness(newBusinessName)
                            showAddBusinessField = false
                            newBusinessName = ""
                        }
                    },
                    enabled = newBusinessName.isNotBlank()
                ) {
                    Text("Create")
                }
            } else {
                TextButton(onClick = { showAddBusinessField = true }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Add Business")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
