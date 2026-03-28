package com.emul8r.bizap.ui.documents

import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import timber.log.Timber
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DocumentVaultScreen(
    onBack: (() -> Unit)? = null,
    viewModel: DocumentVaultViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchTerm by viewModel.searchTerm.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            if (onBack != null) {
                TopAppBar(
                    title = { Text("Document Vault") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Move SearchBar to content instead of topBar
            SearchBar(
                inputField = {
                    SearchBarDefaults.InputField(
                        query = searchTerm,
                        onQueryChange = viewModel::onSearchTermChange,
                        onSearch = {},
                        expanded = false,
                        onExpandedChange = {},
                        placeholder = { Text("Search by Customer or Invoice ID") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary) }
                    )
                },
                expanded = false,
                onExpandedChange = {},
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = SearchBarDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) { }

            Box(modifier = Modifier.fillMaxSize()) {
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
                                            color = MaterialTheme.colorScheme.primary,
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
                                                try {
                                                    if (item.absolutePath.isBlank()) {
                                                        Timber.e("❌ Document #${item.id} has blank file path")
                                                        android.widget.Toast.makeText(
                                                            context,
                                                            "File path is invalid",
                                                            android.widget.Toast.LENGTH_SHORT
                                                        ).show()
                                                        return@ElevatedCard
                                                    }

                                                    if (!file.exists()) {
                                                        Timber.e("❌ Document #${item.id} file not found: ${item.absolutePath}")
                                                        android.widget.Toast.makeText(
                                                            context,
                                                            "File not found: ${file.name}",
                                                            android.widget.Toast.LENGTH_SHORT
                                                        ).show()
                                                        return@ElevatedCard
                                                    }

                                                    Timber.d("📂 Opening document: ${file.name}")
                                                    val uri = FileProvider.getUriForFile(context, "com.emul8r.bizap.fileprovider", file)
                                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                                        setDataAndType(uri, "application/pdf")
                                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                    }
                                                    context.startActivity(intent)
                                                    Timber.d("✅ PDF opened successfully")
                                                } catch (e: IllegalArgumentException) {
                                                    Timber.e(e, "❌ FileProvider error for document #${item.id}: Invalid path in config?")
                                                    android.widget.Toast.makeText(
                                                        context,
                                                        "Cannot access file - configuration issue",
                                                        android.widget.Toast.LENGTH_SHORT
                                                    ).show()
                                                } catch (e: android.content.ActivityNotFoundException) {
                                                    Timber.e(e, "❌ No PDF viewer installed")
                                                    android.widget.Toast.makeText(
                                                        context,
                                                        "No PDF viewer app installed",
                                                        android.widget.Toast.LENGTH_SHORT
                                                    ).show()
                                                } catch (e: Exception) {
                                                    Timber.e(e, "❌ Unexpected error opening document #${item.id}")
                                                    android.widget.Toast.makeText(
                                                        context,
                                                        "Error opening file: ${e.message}",
                                                        android.widget.Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        ) {
                                            ListItem(
                                                colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                                                headlineContent = { Text(item.invoice.displayName.ifBlank { item.invoice.invoiceNumber }) },
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
                                                            try {
                                                                if (item.absolutePath.isBlank()) {
                                                                    Timber.e("❌ Document #${item.id} has blank path for sharing")
                                                                    android.widget.Toast.makeText(
                                                                        context,
                                                                        "File path is invalid",
                                                                        android.widget.Toast.LENGTH_SHORT
                                                                    ).show()
                                                                    return@IconButton
                                                                }

                                                                if (!file.exists()) {
                                                                    Timber.e("❌ Cannot share: File not found: ${item.absolutePath}")
                                                                    android.widget.Toast.makeText(
                                                                        context,
                                                                        "File no longer exists",
                                                                        android.widget.Toast.LENGTH_SHORT
                                                                    ).show()
                                                                    return@IconButton
                                                                }

                                                                Timber.d("📤 Sharing document: ${file.name}")
                                                                val uri = FileProvider.getUriForFile(context, "com.emul8r.bizap.fileprovider", file)
                                                                val intent = Intent(Intent.ACTION_SEND).apply {
                                                                    type = "application/pdf"
                                                                    putExtra(Intent.EXTRA_STREAM, uri)
                                                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                                }
                                                                context.startActivity(Intent.createChooser(intent, "Share PDF"))
                                                                Timber.d("✅ Share dialog launched")
                                                            } catch (e: IllegalArgumentException) {
                                                                Timber.e(e, "❌ FileProvider error when sharing document #${item.id}")
                                                                android.widget.Toast.makeText(
                                                                    context,
                                                                    "Cannot share file - configuration issue",
                                                                    android.widget.Toast.LENGTH_SHORT
                                                                ).show()
                                                            } catch (e: Exception) {
                                                                Timber.e(e, "❌ Error sharing document #${item.id}")
                                                                android.widget.Toast.makeText(
                                                                    context,
                                                                    "Error sharing file: ${e.message}",
                                                                    android.widget.Toast.LENGTH_SHORT
                                                                ).show()
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
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.AutoMirrored.Filled.ReceiptLong,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "No documents yet",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Generate your first invoice to create a document",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
