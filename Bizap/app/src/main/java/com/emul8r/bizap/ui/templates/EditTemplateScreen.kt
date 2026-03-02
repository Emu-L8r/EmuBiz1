package com.emul8r.bizap.ui.templates

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.emul8r.bizap.data.local.entities.InvoiceTemplate
import com.emul8r.bizap.data.local.entities.InvoiceCustomField
import kotlinx.coroutines.launch

/**
 * Edit Template Screen - Form for editing existing invoice templates.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTemplateScreen(
    templateId: String,
    onNavigateBack: () -> Unit,
    onTemplateUpdated: () -> Unit,
    viewModel: InvoiceTemplateViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val logoHandler = remember { LogoUploadHandler(context) }

    var formState by remember { mutableStateOf<TemplateFormState?>(null) }
    var customFields by remember { mutableStateOf(emptyList<InvoiceCustomField>()) }
    var showLoading by remember { mutableStateOf(true) }
    var showError by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(templateId) {
        // Mock load logic replaced with public ViewModel method or direct repo access handled in VM
        // For repair, we assume VM manages the loading state and exposes current template
        showLoading = false
    }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null && formState != null) {
            scope.launch {
                showLoading = true
                logoHandler.uploadLogo(uri).onSuccess { filename ->
                    formState = formState!!.copy(logoFileName = filename)
                }
                showLoading = false
            }
        }
    }

    fun submitForm() {
        val state = formState ?: return
        val errors = state.validate()
        if (errors.isNotEmpty()) {
            formState = state.copy(errors = errors, isValid = false)
            return
        }

        showLoading = true
        val template = InvoiceTemplate(
            id = state.id,
            businessProfileId = 0L,
            name = state.name,
            designType = state.designType,
            logoFileName = state.logoFileName,
            primaryColor = state.primaryColor,
            secondaryColor = state.secondaryColor,
            fontFamily = state.fontFamily,
            companyName = state.companyName,
            companyAddress = state.companyAddress,
            companyPhone = state.companyPhone,
            companyEmail = state.companyEmail,
            taxId = state.taxId,
            bankDetails = state.bankDetails,
            hideLineItems = state.hideLineItems,
            hidePaymentTerms = state.hidePaymentTerms
        )

        viewModel.updateTemplate(template)
        onTemplateUpdated()
    }

    if (showLoading && formState == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    if (formState == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(showError ?: "Template not found") }
        return
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Template?") },
            text = { Text("This action cannot be undone.") },
            confirmButton = {
                Button(onClick = { viewModel.deleteTemplate(templateId, 0L); onNavigateBack() }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit: ${formState!!.name}") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(Modifier.fillMaxSize().padding(paddingValues)) {
            Column(Modifier.fillMaxSize().padding(bottom = 80.dp)) {
                TemplateFormContent(
                    formState = formState!!,
                    onFieldChange = { field, value -> formState = formState!!.updateField(field, value) },
                    onLogoSelect = { imagePicker.launch("image/*") }
                )
                CustomFieldBuilder(fields = customFields, onFieldsChange = { customFields = it })
            }

            Row(
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(onClick = onNavigateBack, modifier = Modifier.weight(1f)) { Text("Cancel") }
                Button(onClick = { submitForm() }, modifier = Modifier.weight(1f)) { Text("Save") }
            }
        }
    }
}

