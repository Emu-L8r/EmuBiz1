// This is the REDESIGNED BusinessProfileScreenV1Content function
// It uses the new design system components for consistency and readability
// Replace the old function with this in BusinessProfileScreen.kt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BusinessProfileScreenV1Content_REDESIGNED(viewModel: BusinessProfileViewModel = hiltViewModel()) {
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
                .background(MaterialTheme.colorScheme.background)
                .padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.xl)
        ) {
            // PAGE TITLE
            Text(
                "Business Profile",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = Spacing.md)
            )

            // LOGO SECTION
            SectionHeader(
                icon = Icons.Default.Image,
                title = "📸 Business Logo",
                description = "This logo will appear on invoices"
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = Spacing.sm),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.lg),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    // Logo preview
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .background(MaterialTheme.colorScheme.surface),
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
                                modifier = Modifier.size(56.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Logo action buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = { cameraLauncher.launch(cameraImageUri) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text("Camera", style = MaterialTheme.typography.labelMedium)
                        }

                        OutlinedButton(
                            onClick = { galleryLauncher.launch("image/*") },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text("Gallery", style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    if (profile.logoBase64 != null) {
                        OutlinedButton(
                            onClick = { viewModel.updateProfile(profile.copy(logoBase64 = null)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text("Remove Logo", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }

            // BUSINESS DETAILS SECTION
            SectionHeader(
                icon = Icons.Default.Business,
                title = "📋 Business Details",
                description = "Required for invoices"
            )

            FormSection {
                FormField(
                    value = profile.businessName,
                    onValueChange = { viewModel.updateProfile(profile.copy(businessName = it)) },
                    label = "Trading Name",
                    helperText = "Legal business name for ABN",
                    isRequired = true,
                    isValid = if (profile.businessName.isEmpty()) false else true
                )

                FormField(
                    value = profile.abn,
                    onValueChange = { viewModel.updateProfile(profile.copy(abn = it)) },
                    label = "ABN",
                    helperText = "11-digit Australian Business Number",
                    isRequired = true,
                    keyboardType = KeyboardType.Number,
                    isValid = if (profile.abn.isEmpty()) false else profile.abn.length >= 11
                )

                FormField(
                    value = profile.address,
                    onValueChange = { viewModel.updateProfile(profile.copy(address = it)) },
                    label = "Business Address",
                    helperText = "Full physical address",
                    isRequired = true,
                    maxLines = 2,
                    isValid = if (profile.address.isEmpty()) false else true
                )

                FormField(
                    value = profile.email,
                    onValueChange = { viewModel.updateProfile(profile.copy(email = it)) },
                    label = "Email",
                    helperText = "Contact email address",
                    keyboardType = KeyboardType.Email,
                    isValid = if (profile.email.isEmpty()) false else true
                )

                FormField(
                    value = profile.phone,
                    onValueChange = { viewModel.updateProfile(profile.copy(phone = it)) },
                    label = "Phone",
                    helperText = "Business contact number",
                    keyboardType = KeyboardType.Phone,
                    isValid = if (profile.phone.isEmpty()) false else true
                )

                FormField(
                    value = profile.website,
                    onValueChange = { viewModel.updateProfile(profile.copy(website = it)) },
                    label = "Website",
                    helperText = "Company website (optional)"
                )
            }

            // BANKING DETAILS SECTION
            SectionHeader(
                icon = Icons.Default.Payment,
                title = "💼 Banking Details",
                description = "Optional - for invoice payments"
            )

            FormSection {
                FormField(
                    value = profile.accountName ?: "",
                    onValueChange = { viewModel.updateProfile(profile.copy(accountName = it)) },
                    label = "Account Name",
                    helperText = "Name on bank account"
                )

                FormField(
                    value = profile.bankName ?: "",
                    onValueChange = { viewModel.updateProfile(profile.copy(bankName = it)) },
                    label = "Bank Name",
                    helperText = "Name of your bank"
                )

                FormField(
                    value = profile.bsbNumber ?: "",
                    onValueChange = { viewModel.updateProfile(profile.copy(bsbNumber = it)) },
                    label = "BSB Number",
                    helperText = "6-digit BSB code",
                    keyboardType = KeyboardType.Number
                )

                FormField(
                    value = profile.accountNumber ?: "",
                    onValueChange = { viewModel.updateProfile(profile.copy(accountNumber = it)) },
                    label = "Account Number",
                    helperText = "Bank account number",
                    keyboardType = KeyboardType.Number
                )
            }

            // Debug button
            if (BuildConfig.DEBUG) {
                Button(
                    onClick = { viewModel.onSeedTestDataClicked() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.BugReport, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Seed Test Data", style = MaterialTheme.typography.labelMedium)
                }
            }

            Spacer(modifier = Modifier.height(Spacing.xl))
        }
    }
}

