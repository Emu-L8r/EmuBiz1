# 6-Week Technical Debt Elimination Plan
## EmuBiz1 - Exit "Migration Purgatory" Strategy

**Status**: Ready for Implementation  
**Created**: March 20, 2026  
**Branch**: `copilot/fix-resource-shrinking`

---

## Executive Summary

This plan addresses the three remaining technical debt hurdles blocking full GUI2 adoption and the deletion of GUI1, which currently creates a 30% velocity tax on all feature development.

### The Three Core Hurdles

1. **Resource Shrinking Disabled** - 3-5 MB APK bloat, workaround hiding Proto conflict
2. **GUI2 Feature Gaps** - Photo attachments missing, incomplete state management
3. **Clean Architecture** - Already compliant! Domain layer has no Android dependencies

### Timeline Overview

| Phase | Focus | Duration | Impact |
|-------|-------|----------|--------|
| 2A | Fix resource shrinking | 1 week | -3 MB APK, clean build |
| 2B | GUI2 feature parity | 2-3 weeks | 100% feature complete |
| 2C | Architecture validation | 1 week | Prevent regression |
| 2D | Delete GUI1 (conditional) | 1 week | 30% codebase reduction |
| **Total** | **Complete debt elimination** | **3-6 weeks** | **+30-40% velocity** |

---

## Phase 2A: Fix Resource Shrinking (Week 1)

### Current State
- **File**: `Bizap/app/build.gradle.kts`
- **Line 95**: `isShrinkResources = false  // Disabled: causes FileSystemAlreadyExistsException`
- **APK Size**: 12-15 MB (should be 9-12 MB)

### Root Cause Analysis
The `FileSystemAlreadyExistsException` typically occurs when:
1. Multiple resource files have the same name but different paths
2. Proto/AIDL generated resources conflict
3. Duplicate resources from dependencies

### Implementation Steps

#### Step 1: Enable and Identify (1-2 hours)
```kotlin
// Bizap/app/build.gradle.kts, line 95
release {
    signingConfig = signingConfigs.getByName("release")
    isMinifyEnabled = true
    isShrinkResources = true  // Enable to capture error
    proguardFiles(...)
}
```

Build command:
```bash
cd Bizap && ./gradlew clean assembleRelease 2>&1 | tee resource-shrink-error.log
```

Expected error format:
```
Caused by: java.nio.file.FileSystemAlreadyExistsException: /path/to/resource.xml
    at com.android.build.gradle.internal.tasks.shrinkResources...
```

#### Step 2: Fix Common Causes (2-3 hours)

**Option A: Proto Resource Conflicts**
```kotlin
// Bizap/app/build.gradle.kts - Add to android {} block
androidResources {
    noCompress += listOf("proto", "pb")
}

// OR rename conflicting proto resources
protobuf {
    generateProtoTasks {
        all().forEach { task ->
            task.plugins {
                create("java") {
                    option("lite")
                }
            }
        }
    }
}
```

**Option B: Dependency Resource Conflicts**
```kotlin
// Bizap/app/build.gradle.kts - Exclude problematic resources
dependencies {
    implementation("com.some.library:name:version") {
        exclude(group = "resource-conflicting-group")
    }
}
```

**Option C: Duplicate Resources in Assets**
- Check `app/src/main/res/` for duplicate resource names
- Check `app/src/main/assets/` for conflicts
- Rename or remove duplicates

#### Step 3: Verify (30 minutes)
```bash
# Build release APK
./gradlew assembleRelease

# Check APK size
ls -lh app/build/outputs/apk/release/app-release.apk

# Expected: 9-12 MB (down from 12-15 MB)
```

#### Step 4: Test (30 minutes)
- Install on real device
- Test all major features
- Verify no runtime crashes
- Check resources load correctly

### Success Criteria
- ✅ `isShrinkResources = true` in release build
- ✅ Build succeeds without FileSystemAlreadyExistsException
- ✅ APK size reduced by 3-5 MB
- ✅ No runtime crashes on real device

---

## Phase 2B: GUI2 Feature Parity (Weeks 2-4)

### Current State Analysis

**Feature Comparison Matrix**

| Feature | GUI1 | GUI2 | Gap |
|---------|------|------|-----|
| Line Items Editor | ✅ | ✅ | None - Shared component |
| Currency Selection | ✅ | ✅ | None - Shared component |
| Customer Dropdown | ✅ | ✅ | None - Shared component |
| Payment Dialogs | ✅ | ✅ | None - RecordPaymentDialogV2 exists |
| Status Management | ✅ | ✅ | None - StatusUpdateMenuV2 exists |
| **Photo Attachments** | ✅ | ❌ | **CRITICAL MISSING** |
| **Complete State Management** | ✅ | ❌ | **CRITICAL MISSING** |
| **Full Edit Capabilities** | ✅ | ⚠️ | **PARTIAL MISSING** |
| PDF Generation | ✅ | ❌ | Nice to have |
| CSV Export | ✅ | ❌ | Nice to have |
| Invoice Versioning | ✅ | ❌ | Nice to have |

### Sprint 1: Photo Attachments System (2-3 days)

#### Files to Create/Modify

**1. Create AddPhotoDialogV2.kt**
```kotlin
// File: Bizap/app/src/main/java/com/emul8r/bizap/ui/gui2/invoice/AddPhotoDialogV2.kt
@Composable
fun AddPhotoDialogV2(
    onDismiss: () -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Photo") },
        text = {
            Column {
                Button(
                    onClick = {
                        onCameraClick()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Take Photo")
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        onGalleryClick()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Choose from Gallery")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
```

**2. Update CreateInvoiceScreenV2.kt**
```kotlin
// Add photo state
var showAddPhotoDialog by remember { mutableStateOf(false) }
var tempImageUri by remember { mutableStateOf<Uri?>(null) }

// Camera launcher
val cameraLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.TakePicture()
) { success ->
    if (success) {
        tempImageUri?.let { uri ->
            viewModel.addPhoto(uri.toString())
        }
    }
}

// Gallery launcher
val galleryLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
) { uri ->
    uri?.let { viewModel.addPhoto(it.toString()) }
}

// Photo display section
if (uiState.photoUris.isNotEmpty()) {
    item {
        Text("Photos", style = MaterialTheme.typography.titleMedium)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            items(uiState.photoUris) { uri ->
                Box {
                    AsyncImage(
                        model = uri,
                        contentDescription = "Invoice photo",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    IconButton(
                        onClick = { viewModel.removePhoto(uri) },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(24.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Remove photo",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// Add photo button
item {
    OutlinedButton(
        onClick = { showAddPhotoDialog = true },
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(Icons.Default.AddAPhoto, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text("Add Photo")
    }
}

// Photo dialog
if (showAddPhotoDialog) {
    AddPhotoDialogV2(
        onDismiss = { showAddPhotoDialog = false },
        onCameraClick = {
            val uri = createImageUri(context)
            tempImageUri = uri
            cameraLauncher.launch(uri)
        },
        onGalleryClick = {
            galleryLauncher.launch("image/*")
        }
    )
}
```

**3. Update CreateInvoiceViewModelV2.kt**
```kotlin
// Add to state
data class CreateInvoiceUiState(
    val customers: List<Customer> = emptyList(),
    val selectedCustomer: Customer? = null,
    val items: List<LineItemForm> = listOf(LineItemForm()),
    val header: String = "",
    val subheader: String = "",
    val notes: String = "",
    val footer: String = "",
    val photoUris: List<String> = emptyList(),  // ADD THIS
    val currencies: List<Currency> = emptyList(),
    val selectedCurrencyCode: String = "AUD",
    val taxRate: Double = 0.0,
    val isTaxRegistered: Boolean = false,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null
)

// Add methods
fun addPhoto(uri: String) {
    _uiState.update { it.copy(photoUris = it.photoUris + uri) }
}

fun removePhoto(uri: String) {
    _uiState.update { it.copy(photoUris = it.photoUris - uri) }
}
```

**4. Update AndroidManifest.xml** (if needed)
```xml
<!-- Add camera permission -->
<uses-permission android:name="android.permission.CAMERA" />
<uses-feature android:name="android.camera.any" android:required="false" />

<!-- Add file provider for camera -->
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

**5. Add Coil dependency** (if not present)
```kotlin
// Bizap/app/build.gradle.kts
implementation("io.coil-kt:coil-compose:2.7.0")
```

#### Testing Checklist
- [ ] Camera permission requested correctly
- [ ] Camera captures photo and displays it
- [ ] Gallery picker works
- [ ] Photo thumbnails display correctly
- [ ] Photo removal works
- [ ] Multiple photos supported
- [ ] Photos saved with invoice

---

### Sprint 2: Complete CreateInvoiceUiState (1-2 days)

Currently, `CreateInvoiceViewModelV2` only has 2 StateFlows. It needs the full state structure from GUI1.

#### File to Modify
**CreateInvoiceViewModelV2.kt**

```kotlin
@HiltViewModel
class CreateInvoiceViewModelV2 @Inject constructor(
    private val invoiceRepository: InvoiceRepository,
    private val customerRepository: CustomerRepository,
    private val businessContextManager: BusinessContextManager,
    private val calculateInvoiceMetricsUseCase: CalculateInvoiceMetricsUseCase,
    private val generateAndSaveInvoiceUseCase: GenerateAndSaveInvoiceUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateInvoiceUiState())
    val uiState: StateFlow<CreateInvoiceUiState> = _uiState.asStateFlow()

    init {
        loadCustomers()
        loadBusinessProfile()
    }

    private fun loadCustomers() {
        viewModelScope.launch {
            customerRepository.getAllCustomersFlow(businessContextManager.requireActiveBusinessId())
                .catch { error ->
                    _uiState.update { it.copy(error = error.message) }
                }
                .collect { customers ->
                    _uiState.update { it.copy(customers = customers) }
                }
        }
    }

    private fun loadBusinessProfile() {
        viewModelScope.launch {
            businessProfileRepository.getBusinessProfile(businessContextManager.requireActiveBusinessId())
                .collect { profile ->
                    _uiState.update {
                        it.copy(
                            taxRate = profile.taxRate,
                            isTaxRegistered = profile.isTaxRegistered
                        )
                    }
                }
        }
    }

    fun selectCustomer(customer: Customer?) {
        _uiState.update { it.copy(selectedCustomer = customer) }
    }

    fun updateHeader(header: String) {
        _uiState.update { it.copy(header = header) }
    }

    fun updateSubheader(subheader: String) {
        _uiState.update { it.copy(subheader = subheader) }
    }

    fun updateNotes(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    fun updateFooter(footer: String) {
        _uiState.update { it.copy(footer = footer) }
    }

    fun updateLineItem(index: Int, item: LineItemForm) {
        val newItems = _uiState.value.items.toMutableList()
        newItems[index] = item
        _uiState.update { it.copy(items = newItems) }
    }

    fun addLineItem() {
        _uiState.update {
            it.copy(items = it.items + LineItemForm())
        }
    }

    fun removeLineItem(index: Int) {
        if (_uiState.value.items.size > 1) {
            _uiState.update {
                it.copy(items = it.items.filterIndexed { i, _ -> i != index })
            }
        }
    }

    fun selectCurrency(currencyCode: String) {
        _uiState.update { it.copy(selectedCurrencyCode = currencyCode) }
    }

    fun addPhoto(uri: String) {
        _uiState.update { it.copy(photoUris = it.photoUris + uri) }
    }

    fun removePhoto(uri: String) {
        _uiState.update { it.copy(photoUris = it.photoUris - uri) }
    }

    fun saveInvoice(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }

            try {
                // Validate
                val validationResult = ValidationRules.validateInvoice(
                    customerId = _uiState.value.selectedCustomer?.id,
                    lineItems = _uiState.value.items,
                    dueDate = calculateDueDate()
                )

                if (!validationResult.isValid) {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            error = validationResult.errors.joinToString("\n")
                        )
                    }
                    return@launch
                }

                // Calculate metrics
                val metrics = calculateInvoiceMetricsUseCase(
                    items = _uiState.value.items.map { it.toLineItem() },
                    taxRate = _uiState.value.taxRate
                )

                // Create invoice
                val invoice = Invoice(
                    id = 0,
                    businessId = businessContextManager.requireActiveBusinessId(),
                    customerId = _uiState.value.selectedCustomer!!.id,
                    invoiceNumber = generateInvoiceNumber(),
                    date = LocalDate.now(),
                    dueDate = calculateDueDate(),
                    subtotalAmount = metrics.subtotal,
                    taxAmount = metrics.tax,
                    totalAmount = metrics.total,
                    status = "DRAFT",
                    currencyCode = _uiState.value.selectedCurrencyCode,
                    header = _uiState.value.header,
                    subheader = _uiState.value.subheader,
                    notes = _uiState.value.notes,
                    footer = _uiState.value.footer,
                    photoUris = _uiState.value.photoUris,
                    items = _uiState.value.items.map { it.toLineItem() }
                )

                // Save to database
                val invoiceId = invoiceRepository.createInvoice(invoice)

                // Generate PDF
                generateAndSaveInvoiceUseCase(invoiceId)

                _uiState.update {
                    it.copy(isSaving = false, saveSuccess = true)
                }

                onSuccess()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isSaving = false, error = e.message ?: "Failed to save invoice")
                }
            }
        }
    }

    private fun calculateDueDate(): LocalDate {
        return LocalDate.now().plusDays(30)
    }

    private suspend fun generateInvoiceNumber(): String {
        val count = invoiceRepository.getInvoiceCount(businessContextManager.requireActiveBusinessId())
        return "INV-${String.format("%05d", count + 1)}"
    }
}
```

---

### Sprint 3: Edit Screen Enhancement (2-3 days)

Currently, `EditInvoiceScreenV2` only allows editing `totalAmount` and `notes`. Need to add full editing capabilities.

#### File to Modify
**EditInvoiceScreenV2.kt**

Key changes:
1. Add line items editor
2. Add customer selector
3. Add currency selector
4. Add photo management
5. Remove read-only restrictions
6. Add proper validation

See GUI1's `EditInvoiceScreen.kt` as reference.

---

### Sprint 4: Advanced Features (1-2 days)

Optional nice-to-have features:
- PDF export/share dialog
- CSV export
- Invoice deletion
- Invoice versioning/corrections

---

## Phase 2C: Clean Architecture Validation (Week 5)

### Good News: Already Compliant! ✅

The exploration revealed that the domain layer is already clean:
- No Room dependencies in domain
- No Paging dependencies in domain
- Domain models are pure Kotlin data classes
- Room entities are properly in the data layer
- Mappers exist in repositories

### Tasks for This Phase

#### 1. Document Architecture (1-2 days)

Create **ARCHITECTURE.md**:
```markdown
# Clean Architecture Pattern

## Layer Structure

### Domain Layer (`/app/src/main/java/com/emul8r/bizap/domain/`)
- Pure Kotlin, no Android dependencies
- Business logic and rules
- Interfaces for repositories
- Use cases

### Data Layer (`/app/src/main/java/com/emul8r/bizap/data/`)
- Room entities with `@Entity` annotations
- DAOs for database access
- Repository implementations
- Mappers: Entity ↔ Domain Model

### Presentation Layer (`/app/src/main/java/com/emul8r/bizap/ui/`)
- ViewModels
- Compose UI screens
- Navigation

## Dependency Rules
- Domain: No dependencies on data or presentation
- Data: Depends on domain (implements interfaces)
- Presentation: Depends on domain (calls use cases)
```

#### 2. Add Architecture Tests (2-3 days)

**ArchitectureTest.kt**
```kotlin
@Test
fun `domain layer should not depend on Room`() {
    val domainPackage = "com.emul8r.bizap.domain"
    val roomPackage = "androidx.room"

    // Scan domain classes
    val domainClasses = getAllClasses(domainPackage)

    domainClasses.forEach { clazz ->
        val imports = getImports(clazz)
        assertFalse(
            imports.any { it.startsWith(roomPackage) },
            "Domain class $clazz should not import Room"
        )
    }
}

@Test
fun `domain layer should not depend on Android framework`() {
    val domainPackage = "com.emul8r.bizap.domain"
    val androidPackages = listOf("android.", "androidx.")

    val domainClasses = getAllClasses(domainPackage)

    domainClasses.forEach { clazz ->
        val imports = getImports(clazz)
        assertFalse(
            imports.any { import ->
                androidPackages.any { import.startsWith(it) }
            },
            "Domain class $clazz should not import Android framework"
        )
    }
}
```

#### 3. Review Data Layer Mappers (1 day)

Ensure all mappers are consistent:
```kotlin
fun InvoiceEntity.toDomain(): Invoice { ... }
fun Invoice.toEntity(): InvoiceEntity { ... }

fun CustomerEntity.toDomain(): Customer { ... }
fun Customer.toEntity(): CustomerEntity { ... }
```

---

## Phase 2D: GUI1 Deletion (Week 6)

### Prerequisites
- ✅ GUI2 has 100% feature parity
- ✅ All tests passing
- ✅ User acceptance testing complete

### Impact Analysis

**Files to Delete** (~200 files, ~15,000 lines of code)
- `/ui/invoices/CreateInvoiceScreen.kt`
- `/ui/invoices/InvoiceListScreen.kt`
- `/ui/invoices/InvoiceDetailScreen.kt`
- `/ui/invoices/EditInvoiceScreen.kt`
- `/ui/customers/CustomerListScreen.kt`
- `/ui/customers/CustomerDetailScreen.kt`
- `/ui/navigation/Screen.kt`
- `/ui/navigation/NavGraph.kt`
- And ~190 more GUI1 files...

**Files to Keep**
- All ViewModels (already consolidated)
- Shared components
- Domain/Data layers
- GUI2 screens

**Files to Update**
- `MainActivity.kt` - Remove GUI switcher
- `SettingsScreen.kt` - Remove GUI toggle
- All navigation references

### Implementation Steps

#### 1. Create Backup Branch
```bash
git checkout -b archive/gui1-final
git push origin archive/gui1-final
```

#### 2. Delete GUI1 Files
```bash
# List all GUI1 files
find . -name "*Screen.kt" ! -name "*ScreenV2.kt" -path "*/ui/invoices/*"
find . -name "*Screen.kt" ! -name "*ScreenV2.kt" -path "*/ui/customers/*"

# Delete (verify list first!)
# git rm ...
```

#### 3. Update MainActivity
Remove GUI switcher logic, always use GUI2.

#### 4. Update Navigation
Remove all `Screen.kt` references, use only `ScreenV2.kt`.

#### 5. Remove Settings Toggle
Delete UI toggle and DataStore key for GUI selection.

#### 6. Run Full Test Suite
```bash
./gradlew test
./gradlew connectedAndroidTest
```

#### 7. Update Documentation
- README
- CONTRIBUTING
- API docs

---

## Success Metrics

### Phase 2A Success
- APK size: 9-12 MB (down from 12-15 MB)
- Build time: Same or faster
- No runtime crashes

### Phase 2B Success
- GUI2 feature count = GUI1 feature count
- All user acceptance tests pass
- Photo attachments working
- Complete state management

### Phase 2C Success
- Architecture tests passing
- Documentation complete
- No architecture violations

### Phase 2D Success
- Codebase: -30% lines of code
- Single GUI: No more dual maintenance
- Velocity: +30-40% on future features

---

## Risk Mitigation

### What Could Go Wrong?

**Phase 2A Risks**
- Resource conflict is in third-party library → Can't fix directly
  - Mitigation: Exclude problematic library resources, use different library version
- APK size doesn't decrease → Resource shrinking not the issue
  - Mitigation: Use APK Analyzer to identify actual bloat sources

**Phase 2B Risks**
- Photo attachments break on certain devices → Permission/storage issues
  - Mitigation: Extensive device testing, use Android CameraX library
- State management breaks existing flows → Regression bugs
  - Mitigation: Comprehensive testing, keep GUI1 until 100% confidence

**Phase 2C Risks**
- Architecture tests are too strict → False positives
  - Mitigation: Refine test rules, allow necessary exceptions

**Phase 2D Risks**
- GUI1 deletion breaks something subtle → Runtime crashes
  - Mitigation: Don't delete until 100% confidence, keep backup branch

---

## Decision Framework

### When to START each phase?

**Phase 2A**: START IMMEDIATELY
- Low risk, high ROI
- Quick psychological win
- Clears workaround

**Phase 2B**: START after 2A complete OR in parallel
- Medium risk, highest ROI
- Required for GUI1 deletion
- 2-3 weeks of focused work

**Phase 2C**: START in parallel with 2B
- Low risk, documentation-heavy
- Prevents future violations
- Can be done anytime

**Phase 2D**: START ONLY after 2B + 2C complete
- High risk, highest impact
- Requires 100% confidence
- User migration plan essential

### When to SKIP a phase?

**Skip 2A if**:
- APK size is not a concern
- Users have unlimited bandwidth/storage
- Resource conflict is too complex

**Skip 2B if**:
- Users don't need photo attachments
- GUI1 support can continue indefinitely
- Team has capacity for dual maintenance

**Never skip 2C**:
- Architecture debt compounds over time
- Prevention is easier than cleanup

**Skip 2D if**:
- Users still heavily use GUI1
- Business can afford velocity tax
- Risk tolerance is low

---

## Recommended Approach

### Scenario 1: "Full Commitment" (6 weeks)
Do all phases in sequence. Exit purgatory completely.

**Timeline**:
- Week 1: Phase 2A
- Weeks 2-4: Phase 2B
- Week 5: Phase 2C
- Week 6: Phase 2D

**Outcome**: Single GUI, clean architecture, 30-40% velocity increase

---

### Scenario 2: "Pragmatic" (3 weeks)
Do 2A + 2B, defer 2D.

**Timeline**:
- Week 1: Phase 2A
- Weeks 2-3: Phase 2B (prioritize critical features)

**Outcome**: GUI2 feature-complete, GUI1 can be deleted when ready

---

### Scenario 3: "Quick Win" (1 week)
Do only 2A.

**Timeline**:
- Week 1: Phase 2A

**Outcome**: Smaller APK, clean build, confidence boost

---

## Next Steps

1. **Review this plan** with your team
2. **Decide on scenario** (Full Commitment / Pragmatic / Quick Win)
3. **Check constraints** (deadlines, capacity, user migration)
4. **Start Phase 2A** (resource shrinking fix)
5. **Iterate and adjust** as you learn

---

## Questions to Answer

- [ ] Timeline: Can you commit 3-6 weeks to this work?
- [ ] Users: What % still actively use GUI1?
- [ ] Priority: Which hurdle bothers your team most?
- [ ] Constraints: Any conflicting deadlines or commitments?
- [ ] Risk: What's your tolerance for potential breaking changes?

---

**Ready to start? Begin with Phase 2A this week!**

