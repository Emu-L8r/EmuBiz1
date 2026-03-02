package com.emul8r.bizap.ui.documents

import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emul8r.bizap.data.local.entities.DocumentStatus
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DocumentVaultScreen(viewModel: DocumentVaultViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchTerm by viewModel.searchTerm.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            SearchBar(
                query = searchTerm,
                onQueryChange = viewModel::onSearchTermChange,
                onSearch = {},
                active = false,
                onActiveChange = {},
                placeholder = { Text("Search by Customer or Invoice ID") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary) },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = SearchBarDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    inputFieldColors = TextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            ) { }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val state = uiState) {
                is DocumentVaultUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.primary)
                }
                is DocumentVaultUiState.Error -> {
                    Text(
                        text = state.message,
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.error
                    )
                }
                is DocumentVaultUiState.Success -> {
                    if (state.documents.isEmpty()) {
                        EmptyState(modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            state.documents.forEach { (month, documents) ->
                                stickyHeader {
                                    Text(
                                        text = month,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary, // Highlight month headers
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                                items(
                                    items = documents,
                                    key = { it.id }
                                ) { item ->
                                    val file = File(item.absolutePath)

                                    ElevatedCard(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.elevatedCardColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        ),
                                        onClick = {
                                            if (file.exists()) {
                                                val uri = FileProvider.getUriForFile(context, "com.emul8r.bizap.fileprovider", file)
                                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                                    setDataAndType(uri, "application/pdf")
                                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                }
                                                context.startActivity(intent)
                                            }
                                        }
                                    ) {
                                        ListItem(
                                            colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                                            headlineContent = { Text("${item.fileType} #${item.invoice.id}") },
                                            supportingContent = {
                                                val statusText = when (item.status) {
                                                    DocumentStatus.DRAFT -> "Draft"
                                                    DocumentStatus.ARCHIVED -> "Archived"
                                                    DocumentStatus.SENT -> "Sent"
                                                    DocumentStatus.PAID -> "Paid"
                                                }
                                                Text("Customer: ${item.invoice.customerName} | ${item.fileType} | Status: $statusText")
                                            },
                                            leadingContent = { Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                            trailingContent = {
                                                IconButton(
                                                    onClick = {
                                                        if (file.exists()) {
                                                            val uri = FileProvider.getUriForFile(context, "com.emul8r.bizap.fileprovider", file)
                                                            val intent = Intent(Intent.ACTION_SEND).apply {
                                                                type = "application/pdf"
                                                                putExtra(Intent.EXTRA_STREAM, uri)
                                                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                            }
                                                            context.startActivity(Intent.createChooser(intent, "Share PDF"))
                                                        }
                                                    },
                                                    enabled = file.exists()
                                                ) {
                                                    Icon(Icons.Default.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.primary)
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            Icons.AutoMirrored.Filled.ReceiptLong,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        Text("No documents found", style = MaterialTheme.typography.bodyLarge)
    }
}

