package com.emul8r.bizap.ui.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emul8r.bizap.BuildConfig
import com.emul8r.bizap.domain.model.BusinessProfile
import com.emul8r.bizap.ui.gui2.common.ErrorStateV2
import com.emul8r.bizap.ui.gui2.common.LoadingIndicatorV2
import com.emul8r.bizap.ui.gui2.settings.BusinessProfileUiStateV2
import com.emul8r.bizap.ui.gui2.settings.BusinessProfileViewModelV2
import com.emul8r.bizap.ui.landing.GuiMode
import com.emul8r.bizap.utils.ImageCompressor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessProfileScreen(
    guiMode: GuiMode = GuiMode.GUI1,
    onBack: () -> Unit = {},
    viewModel: BusinessProfileViewModel = hiltViewModel(),
) {
    when (guiMode) {
        GuiMode.GUI1 -> BusinessProfileScreenV1Content(viewModel = viewModel)
        GuiMode.GUI2 -> BusinessProfileScreenV2Content(onBack = onBack)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BusinessProfileScreenV1Content(viewModel: BusinessProfileViewModel = hiltViewModel()) {
    val profile by viewModel.profileState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                val base64 = withContext(Dispatchers.IO) {
                    ImageCompressor.uriToBase64(context, it)
                }
                base64?.let { encoded ->
                    viewModel.updateProfile(profile.copy(logoBase64 = encoded))
                }
            }
        }
    }

    val cameraImageUri = remember {
        val photoFile = File(context.cacheDir, "logo_${System.currentTimeMillis()}.jpg")
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", photoFile)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            scope.launch {
                val base64 = withContext(Dispatchers.IO) {
                    ImageCompressor.uriToBase64(context, cameraImageUri)
                }
                base64?.let { encoded ->
                    viewModel.updateProfile(profile.copy(logoBase64 = encoded))
                }
            }
        }
    }

    Scaffold(
        topBar = {}
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Business Logo", style = MaterialTheme.typography.titleMedium)
            Text(
                "This logo will appear on your invoices and in the app header",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (profile.logoBase64 != null) {
                            val bitmap = remember(profile.logoBase64) {
                                ImageCompressor.base64ToBitmap(profile.logoBase64!!)
                            }
                            bitmap?.let {
                                Image(
                                    bitmap = it.asImageBitmap(),
                                    contentDescription = "Business Logo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = "No logo",
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = { cameraLauncher.launch(cameraImageUri) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = null)
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text("Camera")
                        }

                        OutlinedButton(
                            onClick = { galleryLauncher.launch("image/*") },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null)
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text("Gallery")
                        }
                    }

                    if (profile.logoBase64 != null) {
                        OutlinedButton(
                            onClick = { viewModel.updateProfile(profile.copy(logoBase64 = null)) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text("Remove Logo")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            Text("Business Details", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = profile.businessName,
                onValueChange = { viewModel.updateProfile(profile.copy(businessName = it)) },
                label = { Text("Trading Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = profile.abn,
                onValueChange = { viewModel.updateProfile(profile.copy(abn = it)) },
                label = { Text("ABN") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = profile.address,
                onValueChange = { viewModel.updateProfile(profile.copy(address = it)) },
                label = { Text("Business Address") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = profile.email,
                onValueChange = { viewModel.updateProfile(profile.copy(email = it)) },
                label = { Text("Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = profile.phone,
                onValueChange = { viewModel.updateProfile(profile.copy(phone = it)) },
                label = { Text("Phone") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            Text("Billing Info", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = profile.bankName ?: "",
                onValueChange = { viewModel.updateProfile(profile.copy(bankName = it)) },
                label = { Text("Bank Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = profile.accountName ?: "",
                onValueChange = { viewModel.updateProfile(profile.copy(accountName = it)) },
                label = { Text("Account Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = profile.bsbNumber ?: "",
                onValueChange = { viewModel.updateProfile(profile.copy(bsbNumber = it)) },
                label = { Text("BSB Number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = profile.accountNumber ?: "",
                onValueChange = { viewModel.updateProfile(profile.copy(accountNumber = it)) },
                label = { Text("Account Number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            Text("Tax Settings", style = MaterialTheme.typography.titleMedium)
            Text(
                text = "Configure tax collection for invoices",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Business is tax registered", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = "Enable if your business collects VAT/GST/Sales Tax",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = profile.isTaxRegistered,
                    onCheckedChange = { viewModel.updateProfile(profile.copy(isTaxRegistered = it)) }
                )
            }

            if (profile.isTaxRegistered) {
                OutlinedTextField(
                    value = (profile.defaultTaxRate * 100).toString(),
                    onValueChange = { value ->
                        val rate = value.toFloatOrNull()
                        if (rate != null && rate >= 0 && rate <= 100) {
                            viewModel.updateProfile(profile.copy(defaultTaxRate = rate / 100f))
                        }
                    },
                    label = { Text("Tax Rate (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    supportingText = { Text("Enter the tax rate as a percentage (e.g., 10 for 10%)") },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(
                    text = "When disabled, invoices will show subtotal only (no tax)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BusinessProfileScreenV2Content(
    onBack: () -> Unit,
    viewModel: BusinessProfileViewModelV2 = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Business Profile") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is BusinessProfileUiStateV2.Loading -> {
                LoadingIndicatorV2(modifier = Modifier.padding(paddingValues))
            }
            is BusinessProfileUiStateV2.Error -> {
                ErrorStateV2(
                    message = state.message,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            is BusinessProfileUiStateV2.Success -> {
                BusinessProfileV2Form(
                    initialProfile = state.businessProfile,
                    onSave = { profile ->
                        viewModel.updateBusinessProfile(profile)
                        onBack()
                    },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun BusinessProfileV2Form(
    initialProfile: BusinessProfile,
    onSave: (BusinessProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    var businessName by remember(initialProfile) { mutableStateOf(initialProfile.businessName) }
    var businessAbn by remember(initialProfile) { mutableStateOf(initialProfile.abn) }
    var businessAddress by remember(initialProfile) { mutableStateOf(initialProfile.address) }
    var businessPhone by remember(initialProfile) { mutableStateOf(initialProfile.phone) }
    var businessEmail by remember(initialProfile) { mutableStateOf(initialProfile.email) }
    var isSaving by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = businessName,
            onValueChange = {
                businessName = it
                nameError = null
            },
            label = { Text("Business Name *") },
            modifier = Modifier.fillMaxWidth(),
            isError = nameError != null,
            supportingText = nameError?.let { { Text(it) } }
        )

        OutlinedTextField(
            value = businessAbn,
            onValueChange = { businessAbn = it },
            label = { Text("ABN/Tax ID") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = businessAddress,
            onValueChange = { businessAddress = it },
            label = { Text("Address") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )

        OutlinedTextField(
            value = businessPhone,
            onValueChange = { businessPhone = it },
            label = { Text("Phone") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = businessEmail,
            onValueChange = { businessEmail = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (businessName.isBlank()) {
                    nameError = "Business name is required"
                    return@Button
                }
                isSaving = true
                onSave(
                    initialProfile.copy(
                        businessName = businessName,
                        abn = businessAbn,
                        address = businessAddress,
                        phone = businessPhone,
                        email = businessEmail
                    )
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaving
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Save Profile")
            }
        }
    }
}
