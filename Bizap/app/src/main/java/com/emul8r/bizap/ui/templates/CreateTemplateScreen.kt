package com.emul8r.bizap.ui.templates

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import java.util.UUID

/**
 * Create Template Screen - Form for creating new invoice templates.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTemplateScreen(
    businessProfileId: Long,
    onNavigateBack: () -> Unit,
    onTemplateCreated: () -> Unit,
    viewModel: InvoiceTemplateViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val logoHandler = remember { LogoUploadHandler(context) }

    var formState by remember { mutableStateOf(TemplateFormState(id = UUID.randomUUID().toString())) }
    var customFields by remember { mutableStateOf(emptyList<InvoiceCustomField>()) }
    var showLoading by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf<String?>(null) }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            scope.launch {
                showLoading = true
                logoHandler.uploadLogo(uri).onSuccess { filename ->
                    formState = formState.copy(logoFileName = filename)
                }.onFailure { error ->
                    showError = error.message
                }
                showLoading = false
            }
        }
    }

    fun submitForm() {
        val errors = formState.validate()
        if (errors.isNotEmpty()) {
            formState = formState.copy(errors = errors, isValid = false)
            return
        }

        showLoading = true
        val template = InvoiceTemplate(
            id = formState.id,
            businessProfileId = businessProfileId,
            name = formState.name,
            designType = formState.designType,
            logoFileName = formState.logoFileName,
            primaryColor = formState.primaryColor,
            secondaryColor = formState.secondaryColor,
            fontFamily = formState.fontFamily,
            companyName = formState.companyName,
            companyAddress = formState.companyAddress,
            companyPhone = formState.companyPhone,
            companyEmail = formState.companyEmail,
            taxId = formState.taxId,
            bankDetails = formState.bankDetails,
            hideLineItems = formState.hideLineItems,
            hidePaymentTerms = formState.hidePaymentTerms
        )

        viewModel.createTemplate(template)
        customFields.forEach { viewModel.addCustomField(it.copy(templateId = template.id)) }
        showLoading = false
        onTemplateCreated()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Template") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(modifier = Modifier.fillMaxSize().padding(bottom = 80.dp)) {
                TemplateFormContent(
                    formState = formState,
                    onFieldChange = { field, value -> formState = formState.updateField(field, value) },
                    onLogoSelect = { imagePicker.launch("image/*") }
                )
                CustomFieldBuilder(fields = customFields, onFieldsChange = { customFields = it })
            }

            Row(
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(onClick = onNavigateBack, modifier = Modifier.weight(1f)) { Text("Cancel") }
                Button(onClick = { submitForm() }, modifier = Modifier.weight(1f)) {
                    if (showLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp)) else Text("Create")
                }
            }
        }
    }
}

