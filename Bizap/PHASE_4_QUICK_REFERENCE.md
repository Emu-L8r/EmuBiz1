# 📚 **PHASE 4 - QUICK REFERENCE**

---

## **FILES CREATED**

| File | Lines | Purpose |
|------|-------|---------|
| TemplateFormState.kt | 200 | Form state + validation |
| TemplateFormContent.kt | 300 | Reusable form UI |
| CustomFieldBuilder.kt | 250 | Dynamic field management |
| LogoUploadHandler.kt | 200 | File upload + compression |
| CreateTemplateScreen.kt | 250 | New template form |
| EditTemplateScreen.kt | 300 | Edit template form |
| TemplateFormStateTest.kt | 300 | 20+ form tests |
| LogoUploadHandlerTest.kt | 200 | 20+ upload tests |

---

## **KEY CLASSES & METHODS**

### **TemplateFormState**
```kotlin
data class TemplateFormState(
    val name: String,
    val designType: String,  // PROFESSIONAL, MINIMAL, BRANDED
    val primaryColor: String,  // Hex: #RRGGBB
    val secondaryColor: String,
    val fontFamily: String,  // SANS_SERIF, SERIF
    val companyName: String,
    // ...more fields
)

// Methods
fun validate(): Map<String, String>  // Returns validation errors
fun isFormValid(): Boolean
fun updateField(field: String, value: Any?): TemplateFormState
```

### **TemplateFormContent**
```kotlin
@Composable
fun TemplateFormContent(
    formState: TemplateFormState,
    onFieldChange: (String, Any?) -> Unit,
    onLogoSelect: () -> Unit
)
```

### **CustomFieldBuilder**
```kotlin
@Composable
fun CustomFieldBuilder(
    fields: List<InvoiceCustomField>,
    onFieldsChange: (List<InvoiceCustomField>) -> Unit
)
```

### **LogoUploadHandler**
```kotlin
class LogoUploadHandler(context: Context) {
    suspend fun uploadLogo(uri: Uri): Result<String>
    fun getLogoFile(filename: String): File
    fun deleteLogo(filename: String): Boolean
    fun clearAllLogos(): Boolean
}
```

### **CreateTemplateScreen**
```kotlin
@Composable
fun CreateTemplateScreen(
    businessProfileId: Long,
    onNavigateBack: () -> Unit,
    onTemplateCreated: () -> Unit
)
```

### **EditTemplateScreen**
```kotlin
@Composable
fun EditTemplateScreen(
    templateId: String,
    onNavigateBack: () -> Unit,
    onTemplateUpdated: () -> Unit
)
```

---

## **NAVIGATION ROUTES**

```
templates/{businessId}              → TemplateListScreen (Phase 3)
templates/create/{businessId}       → CreateTemplateScreen
templates/edit/{templateId}         → EditTemplateScreen
```

---

## **FORM FIELDS**

| Field | Type | Validation |
|-------|------|-----------|
| name | String | Required, max 100 |
| designType | Enum | PROFESSIONAL, MINIMAL, BRANDED |
| primaryColor | String | Valid hex (#RRGGBB) |
| secondaryColor | String | Valid hex |
| fontFamily | Enum | SANS_SERIF, SERIF |
| companyName | String | Required |
| companyEmail | String | Valid email format |
| hideLineItems | Boolean | - |
| hidePaymentTerms | Boolean | - |

---

## **CUSTOM FIELDS**

| Attribute | Type | Constraint |
|-----------|------|-----------|
| label | String | User-defined |
| fieldType | String | TEXT, NUMBER, DATE |
| isRequired | Boolean | - |
| displayOrder | Int | 1-50 |

---

## **LOGO UPLOAD**

- **Max File Size:** 2MB
- **Max Dimensions:** 1080x720
- **Format:** PNG
- **Storage:** App cache directory
- **Reference:** UUID-based filename

---

## **FORM VALIDATION**

```kotlin
// Name
- Required
- Max 100 characters

// Colors (Primary & Secondary)
- Must be valid hex format (#RRGGBB)

// Email
- Valid email format if provided

// Company Name
- Required

// Design Type
- Must be PROFESSIONAL, MINIMAL, or BRANDED

// Font Family
- Must be SANS_SERIF or SERIF

// Custom Fields
- Maximum 50 per template
```

---

## **ERROR HANDLING**

```kotlin
// File too large
Result.failure(IllegalArgumentException("File size exceeds 2MB"))

// Invalid format
Result.failure(IllegalArgumentException("Could not read image"))

// Validation errors
formState.copy(errors = errors, isValid = false)
```

---

## **UNIT TESTS**

### **TemplateFormStateTest** (20+ tests)
- ✅ Valid form state
- ✅ Empty field validation
- ✅ Color validation
- ✅ Email validation
- ✅ Field updates
- ✅ Enum tests

### **LogoUploadHandlerTest** (20+ tests)
- ✅ Handler initialization
- ✅ File size validation
- ✅ Dimension handling
- ✅ Compression
- ✅ Cache management
- ✅ Error scenarios

---

## **COMPOSABLES**

1. **TemplateFormContent** - Main form
2. **CustomFieldItem** - Single field in builder
3. **ColorPickerField** - Color input
4. **CreateTemplateScreen** - Create form
5. **EditTemplateScreen** - Edit form
6. **CustomFieldBuilder** - Field management

---

## **MATERIAL 3 COMPONENTS**

- TopAppBar (with back button)
- Scaffold (layout)
- OutlinedTextField (inputs)
- Button / OutlinedButton
- Switch (toggles)
- FilterChip (selector)
- Card (containers)
- AlertDialog (delete confirmation)
- CircularProgressIndicator (loading)
- Icon / IconButton
- Row / Column (layouts)

---

## **TESTING EXPECTATIONS**

```
Total Expected Tests: 109/109 ✅

Phase 1-3: 69 tests ✅
Phase 4:   40 tests ✅
```

---

## **NEXT PHASE (Phase 5)**

Apply template to invoice editor:
- Template preview
- Field binding
- Invoice generation
- Template reference in invoice

---

## **QUICK START**

1. **Create Template:**
   ```
   NavController.navigate("templates/create/$businessId")
   ```

2. **Edit Template:**
   ```
   NavController.navigate("templates/edit/$templateId")
   ```

3. **Back to List:**
   ```
   onNavigateBack() or onTemplateCreated() or onTemplateUpdated()
   ```

---

**Status: ✅ COMPLETE & READY**

Phase 4 implementation finished. All forms working. Ready for Phase 5.


