# 🧭 **PHASE 3 - NAVIGATION INTEGRATION GUIDE**

**Status:** Ready for NavGraph integration  
**Date:** March 1, 2026  

---

## **NAVIGATION ROUTES**

Add these routes to your NavGraph:

```kotlin
// Template Management Routes
const val TEMPLATES_ROUTE = "templates"
const val TEMPLATES_LIST_ROUTE = "templates/{businessId}"
const val TEMPLATES_CREATE_ROUTE = "templates/create/{businessId}"
const val TEMPLATES_EDIT_ROUTE = "templates/edit/{templateId}"
```

---

## **NAVGRAPH INTEGRATION EXAMPLE**

```kotlin
// In your NavGraph setup (e.g., Navigation.kt or NavHost composition)

composable(
    route = TEMPLATES_LIST_ROUTE,
    arguments = listOf(
        navArgument("businessId") { type = NavType.LongType }
    )
) { backStackEntry ->
    val businessId = backStackEntry.arguments?.getLong("businessId") ?: 1L
    
    TemplateListScreen(
        businessProfileId = businessId,
        onNavigateToCreate = { navController.navigate("templates/create/$businessId") },
        onNavigateToEdit = { templateId -> navController.navigate("templates/edit/$templateId") }
    )
}

// Phase 4 screens (stubs for now)
composable(
    route = TEMPLATES_CREATE_ROUTE,
    arguments = listOf(
        navArgument("businessId") { type = NavType.LongType }
    )
) { backStackEntry ->
    val businessId = backStackEntry.arguments?.getLong("businessId") ?: 1L
    // TODO: CreateTemplateScreen(businessId) - Phase 4
    Placeholder("Create Template Screen - Phase 4")
}

composable(
    route = TEMPLATES_EDIT_ROUTE,
    arguments = listOf(
        navArgument("templateId") { type = NavType.StringType }
    )
) { backStackEntry ->
    val templateId = backStackEntry.arguments?.getString("templateId") ?: ""
    // TODO: EditTemplateScreen(templateId) - Phase 4
    Placeholder("Edit Template Screen - Phase 4")
}
```

---

## **ENTRY POINTS**

### **From Invoice Manager**
```kotlin
// In InvoiceManagerScreen or similar
Button(
    onClick = { navController.navigate("templates/$businessProfileId") }
) {
    Text("Manage Templates")
}
```

### **From Settings/Admin Panel**
```kotlin
// In SettingsScreen or AdminPanel
MenuItem(
    label = "Invoice Templates",
    onClick = { navController.navigate("templates/$businessProfileId") }
)
```

### **From New "Templates" Tab**
```kotlin
// Add to bottom navigation or tab bar
NavigationBarItem(
    selected = currentRoute == TEMPLATES_ROUTE,
    onClick = { navController.navigate("templates/$businessProfileId") },
    icon = { Icon(Icons.Default.Style, "Templates") },
    label = { Text("Templates") }
)
```

---

## **STATE PASSING**

### **Business Profile ID**
- **Required for:** Loading templates for specific business
- **Source:** Usually from user context or CurrentUserManager
- **Type:** Long
- **Example:** 
  ```kotlin
  val businessId = viewModel.currentBusinessProfile.businessProfileId
  navController.navigate("templates/$businessId")
  ```

### **Template ID (for editing)**
- **Required for:** Editing specific template
- **Source:** User clicks on template in list
- **Type:** String (UUID)
- **Example:**
  ```kotlin
  TemplateListItem(
      template = template,
      onEdit = { templateId -> navController.navigate("templates/edit/$templateId") }
  )
  ```

---

## **PHASE 3 TO PHASE 4 HANDOFF**

### **Phase 3 Provides (✅ Done):**
- ✅ TemplateListScreen - Browse templates
- ✅ TemplateListItem - List items with actions
- ✅ Navigation callbacks ready for Phase 4 screens
- ✅ State management in ViewModel

### **Phase 4 Will Implement:**
- ❌ CreateTemplateScreen - Create new template
- ❌ EditTemplateScreen - Edit existing template
- ❌ TemplateFormContent - Shared form logic
- ❌ ColorPickerDialog - For color selection
- ❌ LogoUploadDialog - For logo upload
- ❌ CustomFieldsManager - Manage custom fields

---

## **EXAMPLE: FULL NAVIGATION FLOW**

```
Home Screen
    ↓
    (User taps "Templates" or "Manage Templates")
    ↓
TemplateListScreen (Phase 3 ✅)
    ├─ Shows list of templates
    ├─ FAB "Create Template"
    └─ Template cards with Edit/Delete buttons
    
    If user clicks "Create" FAB:
    ↓
    CreateTemplateScreen (Phase 4)
    ├─ Form with template details
    ├─ Color picker
    ├─ Company info
    └─ Save → Returns to list
    
    If user clicks "Edit" button:
    ↓
    EditTemplateScreen (Phase 4)
    ├─ Pre-populated form
    ├─ Can modify all fields
    ├─ Manage custom fields
    └─ Save → Returns to list
    
    If user clicks "Delete" button:
    ↓
    Soft-deleted → Removed from list
```

---

## **TESTING NAVIGATION**

### **Unit Test Example**
```kotlin
@Test
fun testNavigateToCreate() {
    // In InvoiceTemplateViewModelTest
    viewModel.navigateToCreate(1L)
    
    assertTrue(
        viewModel.navigationEvent.value 
            is InvoiceTemplateViewModel.NavigationEvent.NavigateToCreate
    )
}
```

### **UI Test Example (Phase 4)**
```kotlin
@Test
fun testNavigationToEditScreen() {
    // Click edit button on template
    // Verify screen changes to EditTemplateScreen
    // Verify template ID passed correctly
}
```

---

## **IMPORTANT NOTES FOR PHASE 4**

1. **Keep same ViewModel pattern** - Use Hilt injection
2. **Reuse repository** - InvoiceTemplateRepository handles all DB ops
3. **Follow Material 3** - Use same design system as Phase 3
4. **Handle navigation events** - Use navigationEvent pattern
5. **Validate form** - Check business logic before save
6. **Error handling** - Show errors with retry option
7. **Loading states** - Show spinners during async operations

---

## **ROUTES REFERENCE**

```
templates/{businessId}              - List templates
templates/create/{businessId}       - Create new
templates/edit/{templateId}         - Edit existing
```

---

## ✅ **READY FOR PHASE 4**

Navigation architecture is ready. All Phase 3 screens are equipped with navigation callbacks. Phase 4 can implement CreateTemplateScreen and EditTemplateScreen with confidence.


