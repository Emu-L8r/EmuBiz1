# 🚀 PHASE 6 STEP 2: INTEGRATION & DATA MAPPING - IMPLEMENTATION PLAN

**Date:** March 30, 2026  
**Status:** ⏳ IN PROGRESS  
**Duration Estimate:** 1-2 weeks  
**Build Status:** ✅ Phase 6 Step 1 Complete  

---

## 🎯 PHASE 6 STEP 2 OVERVIEW

**Objective:** Integrate the HTML-to-PDF infrastructure into the existing invoice generation pipeline and enable theme selection.

**Key Deliverables:**
1. ✅ InvoiceTemplateDataMapper - Convert Invoice → Template data
2. ✅ HtmlPdfInvoiceTheme - Implement InvoiceThemeRenderer interface
3. ✅ ThemeManager integration with existing systems
4. ✅ End-to-end PDF generation workflow
5. ✅ ViewModels updated for theme selection
6. ✅ Integration tests

---

## 📋 STEP 2 BREAKDOWN

### **Task 2.1: Create InvoiceTemplateDataMapper** (2 days)

**Purpose:** Convert Invoice domain objects into template variable map

**Requirements:**
- Map Invoice fields to template variables
- Convert monetary values (from cents to currency format)
- Format dates properly
- Handle optional fields gracefully
- Support dynamic color injection
- Line item transformation

**Implementation Points:**
```kotlin
// Key mappings needed:
invoice.date (Long timestamp) → formattedInvoiceDate
invoice.dueDate → formattedDueDate
invoice.items (List<LineItem>) → formatted items list
settings properties → template variables
```

### **Task 2.2: Implement HtmlPdfInvoiceTheme** (2 days)

**Purpose:** Implement InvoiceThemeRenderer interface for HTML-to-PDF theme

**Requirements:**
- Extend InvoiceThemeRenderer interface
- Orchestrate data mapping → template processing → PDF conversion
- Settings validation
- Error handling with proper Result<T> returns
- Theme information (name, description, customizations)

**Key Methods:**
- `generatePdf()` - Main PDF generation
- `validateSettings()` - Validate InvoiceSettings
- `getThemeName()` - Return theme display name
- `getThemeDescription()` - Return theme info
- `getSupportedCustomizations()` - List customization options

### **Task 2.3: Wire into Existing PDF Pipeline** (2 days)

**Purpose:** Integrate with existing InvoicePdfService and PDF generation flow

**Integration Points:**
1. Update InvoicePdfService to support theme selection
2. Create ThemeManager for Canvas vs HTML-PDF selection
3. Wire settings theme preference into PDF generation
4. Update database schema if needed
5. Ensure backward compatibility with Canvas theme

**Code Changes:**
- Modify InvoicePdfService to check selected theme
- Create theme factory/manager
- Pass theme to renderer
- Handle theme-specific PDF generation

### **Task 2.4: Update ViewModels & UI** (2 days)

**Purpose:** Allow users to select themes and update invoice generation flow

**Changes Needed:**
1. InvoiceDetailViewModel - Theme selection support
2. CreateInvoiceViewModel - Use selected theme
3. SettingsViewModels - Theme preference persistence
4. Add theme preview/selection UI

**Implementation:**
- Read theme preference from InvoiceSettings
- Pass to PDF generation
- Show theme selection in settings
- Update PDF generation to use selected theme

### **Task 2.5: Integration Testing** (2-3 days)

**Purpose:** Comprehensive testing of end-to-end PDF generation

**Test Coverage:**
- Unit tests for data mapper
- Unit tests for HtmlPdfInvoiceTheme
- Integration tests for full pipeline
- Edge case handling
- Performance testing
- Theme switching tests

---

## 🏗️ ARCHITECTURE CHANGES

### **Current Flow**
```
Invoice Data → InvoicePdfService → PdfGenerationService → PDF File
```

### **New Flow with Themes**
```
Invoice Data + InvoiceSettings (theme)
    ↓
ThemeManager (selects Canvas or HTML-PDF)
    ↓
Selected Theme (InvoiceThemeRenderer implementation)
    ├─ Canvas Theme → Existing Canvas PDF generation
    └─ HTML Theme → InvoiceTemplateDataMapper → HtmlTemplateProcessor → HtmlToPdfConverter → PDF
    ↓
PDF File
```

---

## 📊 TASK BREAKDOWN & TIMELINE

| Task | Description | Days | Priority |
|------|-------------|------|----------|
| 2.1 | InvoiceTemplateDataMapper | 2 | High |
| 2.2 | HtmlPdfInvoiceTheme Implementation | 2 | High |
| 2.3 | Pipeline Integration | 2 | High |
| 2.4 | ViewModel & UI Updates | 2 | Medium |
| 2.5 | Integration Testing | 2-3 | High |

**Total Estimated Time:** 10-11 days (1.5 weeks)

---

## 🚀 IMPLEMENTATION SEQUENCE

### **Day 1-2: Create InvoiceTemplateDataMapper**

1. Analyze Invoice and InvoiceSettings data models
2. Create mapper class with conversion functions
3. Handle date/time conversions (Long → Date strings)
4. Handle currency conversions (cents → formatted currency)
5. Test mapper with sample data

**Files to Create:**
- `app/src/main/java/.../html/InvoiceTemplateDataMapper.kt`

**Files to Review:**
- `domain/model/Invoice.kt`
- `domain/model/InvoiceSettings.kt`
- `domain/model/LineItem.kt`

### **Day 3-4: Implement HtmlPdfInvoiceTheme**

1. Create class implementing InvoiceThemeRenderer
2. Implement all required methods
3. Orchestrate: DataMapper → TemplateProcessor → PdfConverter
4. Add comprehensive error handling
5. Add validation logic

**Files to Create:**
- `app/src/main/java/.../data/pdf/HtmlPdfInvoiceTheme.kt`

**Files to Reference:**
- `data/pdf/CanvasInvoiceTheme.kt` (reference implementation)
- `domain/pdf/InvoiceThemeRenderer.kt` (interface)

### **Day 5-6: Wire into PDF Pipeline**

1. Create ThemeManager/Factory
2. Update InvoicePdfService to use themes
3. Add theme selection logic
4. Ensure backward compatibility
5. Test Canvas theme still works

**Files to Modify:**
- `app/src/main/java/.../data/service/InvoicePdfService.kt`
- `app/src/main/java/.../ui/settings/invoice_theme/InvoiceThemeManager.kt`

**Files to Create:**
- `app/src/main/java/.../data/pdf/InvoiceThemeFactory.kt` (if needed)

### **Day 7-8: Update ViewModels & UI**

1. Update InvoiceDetailViewModel to read theme preference
2. Update CreateInvoiceViewModel to use theme
3. Add theme selection to Settings
4. Update PDF generation calls
5. Test theme switching

**Files to Modify:**
- `ui/invoices/InvoiceDetailViewModel.kt`
- `ui/invoices/CreateInvoiceViewModel.kt`
- `ui/settings/InvoiceSettingsViewModel.kt`
- `ui/gui2/invoices/CreateInvoiceViewModelV2.kt`

### **Day 9-11: Testing & Refinement**

1. Write unit tests for mapper
2. Write tests for HtmlPdfInvoiceTheme
3. Write integration tests for full pipeline
4. Test edge cases
5. Performance testing
6. Fix any issues found

---

## 📊 SUCCESS CRITERIA

✅ InvoiceTemplateDataMapper correctly converts Invoice data  
✅ HtmlPdfInvoiceTheme implements interface correctly  
✅ Theme manager selects correct theme  
✅ Canvas theme still works (backward compatible)  
✅ HTML theme generates valid PDFs  
✅ Theme switching works without errors  
✅ All integration tests pass  
✅ No regressions in existing functionality  

---

## 🔍 IMPLEMENTATION NOTES

### **Data Conversion Challenges**

1. **Timestamps:** Invoice.date is Long (milliseconds), need to format to "MMM dd, yyyy"
2. **Currency:** totalAmount is in cents, need to format as "$X.XX"
3. **LineItems:** Need to transform List<LineItem> → formatted item data
4. **Optional Fields:** Handle nulls gracefully in template

### **Theme Integration**

1. **Factory Pattern:** Consider creating InvoiceThemeFactory to manage theme instances
2. **Dependency Injection:** Both themes should be injectable via Hilt
3. **Settings Persistence:** Theme selection must persist across app restarts
4. **Error Handling:** Each theme must validate settings before PDF generation

### **Backward Compatibility**

1. **Canvas Theme:** Must continue to work exactly as before
2. **Database:** No schema changes if possible
3. **API:** ThemeRenderer interface allows for easy addition of new themes
4. **Migration:** No data migration needed for users

---

## 📝 IMPLEMENTATION CHECKLIST

### **Phase 6 Step 2**

- [ ] InvoiceTemplateDataMapper created and tested
- [ ] HtmlPdfInvoiceTheme implemented
- [ ] ThemeManager created and integrated
- [ ] InvoicePdfService updated for theme support
- [ ] ViewModels updated for theme selection
- [ ] Unit tests written and passing
- [ ] Integration tests written and passing
- [ ] Canvas theme verified working
- [ ] HTML theme generating valid PDFs
- [ ] Theme switching working correctly
- [ ] No regressions in existing features
- [ ] Documentation updated
- [ ] Ready for Phase 6 Step 3 (Testing)

---

## 🎯 NEXT MILESTONE

**Phase 6 Step 3: Comprehensive Testing** (1 week)
- Advanced unit tests
- Edge case testing
- Performance optimization
- Visual regression testing
- Documentation updates

---

**Status:** ⏳ **STEP 2 - IN PROGRESS**  
**Build:** ✅ Step 1 Complete  
**Estimated Completion:** 2 weeks from now  

Ready to begin implementation! Starting with Task 2.1: InvoiceTemplateDataMapper


