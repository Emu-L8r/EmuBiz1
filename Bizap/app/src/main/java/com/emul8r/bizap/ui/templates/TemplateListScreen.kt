package com.emul8r.bizap.ui.templates

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.emul8r.bizap.data.local.entities.InvoiceTemplate
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Template List Screen - Shows all invoice templates for a business.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateListScreen(
    businessProfileId: Long,
    onNavigateToCreate: (Long) -> Unit,
    onNavigateToEdit: (String) -> Unit,
    viewModel: InvoiceTemplateViewModel = hiltViewModel()
) {
    LaunchedEffect(businessProfileId) {
        viewModel.loadTemplates(businessProfileId)
    }

    val templates by viewModel.templates.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val navigationEventState by viewModel.navigationEvent.collectAsState()

    LaunchedEffect(navigationEventState) {
        val event = navigationEventState
        when (event) {
            is InvoiceTemplateViewModel.NavigationEvent.NavigateToCreate -> {
                onNavigateToCreate(event.businessProfileId)
                viewModel.clearNavigationEvent()
            }
            is InvoiceTemplateViewModel.NavigationEvent.NavigateToEdit -> {
                onNavigateToEdit(event.templateId)
                viewModel.clearNavigationEvent()
            }
            null -> {}
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Invoice Templates") }) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.navigateToCreate(businessProfileId) }
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Create Template")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                error != null -> {
                    ErrorState(
                        error = error,
                        onRetry = { viewModel.retryLoadTemplates(businessProfileId) },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                templates.isEmpty() -> {
                    EmptyState(modifier = Modifier.align(Alignment.Center))
                }
                else -> {
                    TemplatesList(
                        templates = templates,
                        onEdit = { id -> onNavigateToEdit(id) },
                        onDelete = { id -> viewModel.deleteTemplate(id, businessProfileId) },
                        onSetDefault = { id -> viewModel.setAsDefault(id, businessProfileId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TemplatesList(
    templates: List<InvoiceTemplate>,
    onEdit: (String) -> Unit,
    onDelete: (String) -> Unit,
    onSetDefault: (String) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(templates) { template ->
            TemplateListItem(
                template = template, 
                onEdit = onEdit, 
                onDelete = onDelete, 
                onSetDefault = onSetDefault
            )
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("No templates yet", style = MaterialTheme.typography.headlineSmall)
        Text("Create your first template to get started", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun ErrorState(error: String?, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Failed to load templates", color = MaterialTheme.colorScheme.error)
        Text(error ?: "Unknown error", style = MaterialTheme.typography.bodyMedium)
        Button(onClick = onRetry) { Text("Retry") }
    }
}
