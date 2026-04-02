# 🧹 PHASE 3: CREATE INVOICE PAGE CLEANUP - IMPLEMENTATION GUIDE

**Date:** March 30, 2026  
**Status:** ⏳ READY TO IMPLEMENT  
**Duration:** 1-2 weeks  
**Priority:** CRITICAL  

---

## 📋 IMPLEMENTATION CHECKLIST

### **STEP 1: Create Data Models (2-3 days)**

#### **1.1 Create InvoiceSettings.kt**

Location: `app/src/main/java/com/emul8r/bizap/domain/model/InvoiceSettings.kt`

**Copy the complete code from PHASE_2_DESIGN_SPECIFICATIONS.md:**
- InvoiceSettings data class
- InvoiceTheme enum
- TaxHandling enum

#### **1.2 Create Room Entity & DAO**

Location: `app/src/main/java/com/emul8r/bizap/data/local/dao/InvoiceSettingsDao.kt`

**Copy the DAO interface from PHASE_2_DESIGN_SPECIFICATIONS.md**

#### **1.3 Create Database Migration**

Location: `app/src/main/java/com/emul8r/bizap/data/local/migration/MIGRATION_AddInvoiceSettings.kt`

**Copy migration from PHASE_2_DESIGN_SPECIFICATIONS.md**

**IMPORTANT:** Update version numbers in migration based on your current schema!

---

### **STEP 2: Create Repository (2-3 days)**

#### **2.1 Create InvoiceSettingsRepository.kt**

Location: `app/src/main/java/com/emul8r/bizap/data/repository/InvoiceSettingsRepository.kt`

```kotlin
package com.emul8r.bizap.data.repository

import com.emul8r.bizap.data.local.dao.InvoiceSettingsDao
import com.emul8r.bizap.domain.model.InvoiceSettings
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InvoiceSettingsRepository @Inject constructor(
    private val settingsDao: InvoiceSettingsDao
) {
    
    suspend fun getSettings(userId: String): InvoiceSettings? {
        return settingsDao.getSettings(userId) ?: InvoiceSettings.default(userId).also {
            settingsDao.insertOrUpdate(it)
        }
    }
    
    fun getSettingsFlow(userId: String): Flow<InvoiceSettings?> {
        return settingsDao.getSettingsFlow(userId)
    }
    
    suspend fun saveSettings(settings: InvoiceSettings) {
        settingsDao.insertOrUpdate(
            settings.copy(updatedAt = System.currentTimeMillis())
        )
    }
    
    suspend fun deleteSettings(userId: String) {
        settingsDao.deleteByUserId(userId)
    }
    
    suspend fun resetToDefaults(userId: String) {
        val defaults = InvoiceSettings.default(userId)
        saveSettings(defaults)
    }
}
```

---

### **STEP 3: Create Theme Infrastructure (3-4 days)**

#### **3.1 Create Theme Interface**

Location: `app/src/main/java/com/emul8r/bizap/domain/pdf/InvoiceThemeRenderer.kt`

**Copy from PHASE_2_DESIGN_SPECIFICATIONS.md**

#### **3.2 Create Theme Manager**

Location: `app/src/main/java/com/emul8r/bizap/data/pdf/InvoiceThemeManagerImpl.kt`

```kotlin
package com.emul8r.bizap.data.pdf

import com.emul8r.bizap.domain.model.InvoiceTheme
import com.emul8r.bizap.domain.pdf.InvoiceThemeRenderer
import com.emul8r.bizap.domain.pdf.InvoiceThemeManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InvoiceThemeManagerImpl @Inject constructor(
    private val canvasTheme: CanvasInvoiceTheme,
    private val htmlPdfTheme: HtmlPdfInvoiceTheme
) : InvoiceThemeManager {
    
    override fun getTheme(theme: InvoiceTheme): InvoiceThemeRenderer {
        return when (theme) {
            InvoiceTheme.CANVAS -> canvasTheme
            InvoiceTheme.HTML_PDF -> htmlPdfTheme
        }
    }
    
    override fun listAvailableThemes(): List<InvoiceTheme> {
        return listOf(
            InvoiceTheme.CANVAS,
            InvoiceTheme.HTML_PDF
        )
    }
}
```

#### **3.3 Create Canvas Theme Wrapper**

Location: `app/src/main/java/com/emul8r/bizap/data/pdf/CanvasInvoiceTheme.kt`

```kotlin
package com.emul8r.bizap.data.pdf

import android.content.Context
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.InvoiceSettings
import com.emul8r.bizap.domain.pdf.CustomizationOption
import com.emul8r.bizap.domain.pdf.InvoiceThemeRenderer
import com.emul8r.bizap.domain.pdf.ValidationResult
import com.emul8r.bizap.data.service.InvoicePdfService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CanvasInvoiceTheme @Inject constructor(
    private val context: Context,
    private val pdfService: InvoicePdfService
) : InvoiceThemeRenderer {
    
    override suspend fun generatePdf(
        invoice: Invoice,
        settings: InvoiceSettings,
        outputPath: String
    ): Result<String> {
        return try {
            val file = pdfService.generateInvoice(
                invoice = invoice,
                isQuote = false,
                overwriteExisting = true
            )
            Result.success(file.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun validateSettings(settings: InvoiceSettings): ValidationResult {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        
        if (settings.businessName.isBlank()) {
            errors.add("Business name is required")
        }
        if (settings.businessEmail.isBlank()) {
            errors.add("Business email is required")
        }
        if (settings.primaryColor.isBlank()) {
            warnings.add("Primary color not set, using default")
        }
        
        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors,
            warnings = warnings
        )
    }
    
    override fun getThemeName(): String = "Canvas Style (Current)"
    
    override fun getThemeDescription(): String = 
        "Professional layered invoice with artistic design (Phase 9 Canvas)"
    
    override fun getSupportedCustomizations(): List<CustomizationOption> =
        listOf(
            CustomizationOption.PRIMARY_COLOR,
            CustomizationOption.LOGO,
            CustomizationOption.TYPOGRAPHY
        )
}
```

#### **3.4 Create Hilt Module**

Location: `app/src/main/java/com/emul8r/bizap/di/PdfModule.kt`

**Copy from PHASE_2_DESIGN_SPECIFICATIONS.md**

---

### **STEP 4: Create Invoice Settings Screen (4-5 days)**

#### **4.1 Create ViewModel**

Location: `app/src/main/java/com/emul8r/bizap/ui/settings/InvoiceSettingsViewModel.kt`

```kotlin
package com.emul8r.bizap.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.data.repository.InvoiceSettingsRepository
import com.emul8r.bizap.domain.model.InvoiceSettings
import com.emul8r.bizap.domain.model.InvoiceTheme
import com.emul8r.bizap.domain.model.TaxHandling
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InvoiceSettingsUiState(
    val settings: InvoiceSettings? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false
)

@HiltViewModel
class InvoiceSettingsViewModel @Inject constructor(
    private val repository: InvoiceSettingsRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(InvoiceSettingsUiState())
    val uiState: StateFlow<InvoiceSettingsUiState> = _uiState.asStateFlow()
    
    private val userId = "current_user" // Get from auth/session
    
    init {
        loadSettings()
    }
    
    private fun loadSettings() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                val settings = repository.getSettings(userId)
                _uiState.value = _uiState.value.copy(
                    settings = settings,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }
    
    fun updateBusinessName(name: String) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(businessName = name)
            )
        }
    }
    
    fun updatePrimaryColor(color: String) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(primaryColor = color)
            )
        }
    }
    
    fun updateSelectedTheme(theme: InvoiceTheme) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(selectedTheme = theme)
            )
        }
    }
    
    fun updatePaymentTermsDays(days: Int) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(paymentTermsDays = days)
            )
        }
    }
    
    fun updateTaxRate(rate: Double) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(taxRate = rate)
            )
        }
    }
    
    fun saveSettings() {
        viewModelScope.launch {
            try {
                _uiState.value.settings?.let { settings ->
                    repository.saveSettings(settings)
                    _uiState.value = _uiState.value.copy(
                        saveSuccess = true,
                        error = null
                    )
                    // Reset success after 2 seconds
                    kotlinx.coroutines.delay(2000)
                    _uiState.value = _uiState.value.copy(saveSuccess = false)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to save settings: ${e.message}"
                )
            }
        }
    }
    
    fun resetToDefaults() {
        viewModelScope.launch {
            try {
                repository.resetToDefaults(userId)
                loadSettings()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to reset settings: ${e.message}"
                )
            }
        }
    }
}
```

#### **4.2 Create Screen Composable**

Location: `app/src/main/java/com/emul8r/bizap/ui/settings/InvoiceSettingsScreen.kt`

```kotlin
package com.emul8r.bizap.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emul8r.bizap.domain.model.InvoiceTheme
import com.emul8r.bizap.domain.model.TaxHandling

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceSettingsScreen(
    viewModel: InvoiceSettingsViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Show snackbar on save success
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            snackbarHostState.showSnackbar("Settings saved successfully")
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Invoice Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text("Error: ${uiState.error}", color = MaterialTheme.colorScheme.error)
                }
            }
            uiState.settings != null -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Theme Selection Section
                    item {
                        ThemeSelectionSection(
                            currentTheme = uiState.settings.selectedTheme,
                            onThemeSelected = { viewModel.updateSelectedTheme(it) }
                        )
                    }
                    
                    // Company Branding Section
                    item {
                        CompanyBrandingSection(
                            settings = uiState.settings,
                            onBusinessNameChanged = { viewModel.updateBusinessName(it) }
                        )
                    }
                    
                    // Colors Section
                    item {
                        ColorsSection(
                            primaryColor = uiState.settings.primaryColor,
                            onColorChanged = { viewModel.updatePrimaryColor(it) }
                        )
                    }
                    
                    // Payment Section
                    item {
                        PaymentSection(
                            paymentTermsDays = uiState.settings.paymentTermsDays,
                            bankName = uiState.settings.bankName ?: "",
                            onPaymentTermsChanged = { viewModel.updatePaymentTermsDays(it) }
                        )
                    }
                    
                    // Tax Section
                    item {
                        TaxSection(
                            taxRate = uiState.settings.taxRate,
                            taxName = uiState.settings.taxName,
                            onTaxRateChanged = { viewModel.updateTaxRate(it) }
                        )
                    }
                    
                    // Action Buttons
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.saveSettings() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Text("Save")
                            }
                            OutlinedButton(
                                onClick = { viewModel.resetToDefaults() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Text("Reset")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ThemeSelectionSection(
    currentTheme: InvoiceTheme,
    onThemeSelected: (InvoiceTheme) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Theme & Style", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                RadioButton(
                    selected = currentTheme == InvoiceTheme.CANVAS,
                    onClick = { onThemeSelected(InvoiceTheme.CANVAS) }
                )
                Text("Canvas Style (Current)")
            }
            
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                RadioButton(
                    selected = currentTheme == InvoiceTheme.HTML_PDF,
                    onClick = { onThemeSelected(InvoiceTheme.HTML_PDF) }
                )
                Text("Modern HTML Style (New)")
            }
        }
    }
}

@Composable
fun CompanyBrandingSection(
    settings: InvoiceSettings,
    onBusinessNameChanged: (String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Company Branding", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = settings.businessName,
                onValueChange = onBusinessNameChanged,
                label = { Text("Company Name") },
                modifier = Modifier.fillMaxWidth()
            )
            
            // Add other fields similarly...
        }
    }
}

@Composable
fun ColorsSection(
    primaryColor: String,
    onColorChanged: (String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Theme Colors", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = primaryColor,
                onValueChange = onColorChanged,
                label = { Text("Primary Color (Hex)") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("#6B4C9A") }
            )
        }
    }
}

@Composable
fun PaymentSection(
    paymentTermsDays: Int,
    bankName: String,
    onPaymentTermsChanged: (Int) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Payment Information", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = paymentTermsDays.toString(),
                onValueChange = { onPaymentTermsChanged(it.toIntOrNull() ?: 30) },
                label = { Text("Payment Terms (Days)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
    }
}

@Composable
fun TaxSection(
    taxRate: Double,
    taxName: String,
    onTaxRateChanged: (Double) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Tax Configuration", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = taxRate.toString(),
                onValueChange = { onTaxRateChanged(it.toDoubleOrNull() ?: 0.1) },
                label = { Text("Tax Rate (%)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
    }
}
```

---

### **STEP 5: Refactor Create Invoice Screen (3-4 days)**

#### **5.1 Update CreateInvoiceViewModel**

Modify: `app/src/main/java/com/emul8r/bizap/ui/invoices/CreateInvoiceViewModel.kt`

**Changes needed:**
- Remove customization-related state variables
- Remove customization-related methods
- Add method to load InvoiceSettings
- Update generatePdf to use ThemeManager

#### **5.2 Update CreateInvoiceScreen**

Modify: `app/src/main/java/com/emul8r/bizap/ui/invoices/CreateInvoiceScreen.kt`

**Changes needed:**
- Remove InvoiceCustomizationEditor component
- Remove photo attachment section (optional)
- Add info banner about settings
- Add navigation to Invoice Settings
- Keep: Customer, dates, items, notes

---

## 📊 IMPLEMENTATION TIMELINE

```
PHASE 3: Create Invoice Cleanup (1-2 weeks)

Week 1:
├── Step 1: Create Data Models (2-3 days)
├── Step 2: Create Repository (2-3 days)
└── Step 3: Create Theme Infrastructure (3-4 days)

Week 2:
├── Step 4: Create Invoice Settings Screen (4-5 days)
├── Step 5: Refactor Create Invoice Screen (3-4 days)
└── Testing & Bug Fixes (1-2 days)
```

---

## ✅ VALIDATION CHECKLIST

After completing Phase 3:

- [ ] Data models compile without errors
- [ ] Database migrations run successfully
- [ ] Repository methods work correctly
- [ ] Theme infrastructure loads themes
- [ ] Invoice Settings screen displays correctly
- [ ] Settings can be saved and loaded
- [ ] Create Invoice page is clean (no customization UI)
- [ ] Settings info banner displays
- [ ] PDF generation still works (Canvas theme)
- [ ] All tests pass (>90% coverage)
- [ ] No TypeErrors or Exceptions

---

**Status:** ⏳ READY TO START  
**Next:** Begin implementing Step 1


