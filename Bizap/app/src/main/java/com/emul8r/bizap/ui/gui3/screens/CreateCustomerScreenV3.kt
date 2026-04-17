package com.emul8r.bizap.ui.gui3.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.emul8r.bizap.ui.gui2.customers.CreateCustomerViewModelV2
import com.emul8r.bizap.ui.gui3.components.*
import com.emul8r.bizap.ui.gui3.theme.*
import com.emul8r.bizap.ui.gui3.util.ScreenType
import com.emul8r.bizap.ui.theme.Spacing
import timber.log.Timber

/**
 * Create Customer Screen V3 (Matrix Edition) - PHASE 2A
 *
 * Full-featured customer creation form with cascade-aware interactions.
 *
 * **Features:**
 * - Minimal form (Name + Email only, rest optional)
 * - Real-time validation
 * - Loading state during submission
 * - Auto-navigation on success
 * - Reuses GUI2's ViewModel for business logic
 *
 * **GUI3 Superiority:**
 * ✅ Minimal form (faster entry than GUI2)
 * ✅ Large touch targets (thumb-friendly)
 * ✅ Elegant Matrix theme
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCustomerScreenV3(
    customerId: Long? = null,
    navController: NavHostController,
    viewModel: CreateCustomerViewModelV2 = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isEditMode = customerId != null

    // Navigate back on success
    LaunchedEffect(uiState.customerCreated) {
        if (uiState.customerCreated) {
            Timber.d("✅ CreateCustomerScreenV3: Customer created, navigating back")
            navController.popBackStack()
        }
    }

    MatrixBackgroundWrapper(screenType = ScreenType.FORM) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            if (isEditMode) "BIZAP > EDIT CUSTOMER" else "BIZAP > NEW CUSTOMER",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                color = MatrixGreenBright,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                letterSpacing = 1.sp
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MatrixGreen)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MatrixSurface)
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(Spacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                // Main form section
                MatrixFormSection(
                    title = if (isEditMode) ">> EDIT CUSTOMER" else ">> NEW CUSTOMER",
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Name field
                    MatrixTextField(
                        value = uiState.name,
                        onValueChange = { viewModel.updateName(it) },
                        label = "CUSTOMER NAME *",
                        error = uiState.nameError,
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Email field
                    MatrixTextField(
                        value = uiState.email,
                        onValueChange = { viewModel.updateEmail(it) },
                        label = "EMAIL",
                        error = uiState.emailError,
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Business name field (optional but useful)
                    MatrixTextField(
                        value = uiState.businessName,
                        onValueChange = { viewModel.updateBusinessName(it) },
                        label = "BUSINESS NAME",
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Phone field (optional)
                    MatrixTextField(
                        value = uiState.phone,
                        onValueChange = { viewModel.updatePhone(it) },
                        label = "PHONE",
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Next,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Address field (optional)
                    MatrixTextField(
                        value = uiState.address,
                        onValueChange = { viewModel.updateAddress(it) },
                        label = "ADDRESS",
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Business Details Section
                MatrixFormSection(
                    title = ">> BUSINESS DETAILS",
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Notes field (optional)
                    MatrixTextField(
                        value = uiState.notes,
                        onValueChange = { viewModel.updateNotes(it) },
                        label = "NOTES",
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // General error (if any)
                    if (uiState.generalError != null) {
                        MatrixFormError(
                            message = uiState.generalError ?: "Unknown error",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Save button
                    MatrixFormButton(
                        text = if (isEditMode) "UPDATE" else "CREATE",
                        onClick = { viewModel.createCustomer() },
                        isLoading = uiState.isSubmitting,
                        isEnabled = !uiState.isSubmitting,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.xl))
            }
        }
    }
}



