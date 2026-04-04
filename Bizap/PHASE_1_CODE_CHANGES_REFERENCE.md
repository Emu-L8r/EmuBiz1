# 📝 PHASE 1 - EXACT CODE CHANGES REFERENCE

Quick reference for all code changes made in Phase 1.

---

## 1. InvoiceSettings.kt - New Enums & Fields

### Added Enums (lines 88-120)

```kotlin
/**
 * Enum for PDF rendering engine selection.
 *
 * CANVAS: Android's PdfDocument API - direct coordinate control, artistic designs
 * HTML_CSS: HTML-to-PDF conversion - professional layouts, CSS styling
 */
enum class PdfEngine {
    CANVAS,      // Canvas-based rendering with coordinate control
    HTML_CSS     // HTML-to-PDF rendering with CSS styling
}

/**
 * Enum for page layout organization.
 *
 * CLASSIC: Original layout - Header | Bill To + Invoice Details | Items | Totals | Footer
 * MODERN: Compact side-by-side layout with grid organization
 */
enum class PageLayout {
    CLASSIC,     // Traditional invoice layout
    MODERN       // Compact modern grid-based layout
}
```

### Added Fields to InvoiceSettings data class (after line 24)

```kotlin
// PDF ENGINE SELECTION (new three-tier architecture)
@ColumnInfo(name = "selected_pdf_engine")
val selectedPdfEngine: PdfEngine = PdfEngine.HTML_CSS,

// PAGE LAYOUT SELECTION
@ColumnInfo(name = "selected_page_layout")
val selectedPageLayout: PageLayout = PageLayout.MODERN,

// PREVIEW MODE
@ColumnInfo(name = "preview_with_placeholder")
val previewWithPlaceholder: Boolean = false,
```

---

## 2. PlaceholderInvoiceGenerator.kt - New File

Located: `app/src/main/java/com/emul8r/bizap/domain/model/PlaceholderInvoiceGenerator.kt`

```kotlin
object PlaceholderInvoiceGenerator {
    /**
     * Generate a complete placeholder invoice snapshot for preview purposes.
     * All data is realistic but fictional.
     * Amounts are in cents (e.g., $1500.00 = 150000L)
     */
    fun generatePreviewInvoice(): InvoiceSnapshot {
        val now = System.currentTimeMillis()
        val dueDateMs = now + (30 * 24 * 60 * 60 * 1000)

        val items = listOf(
            LineItemSnapshot(
                description = "Professional Services - Consulting",
                quantity = 1.0,
                unitPrice = 150000L,  // $1500.00
                total = 150000L
            ),
            LineItemSnapshot(
                description = "Software Development (40 hours @ $125/hr)",
                quantity = 40.0,
                unitPrice = 12500L,   // $125.00
                total = 500000L       // 40 × $125 = $5000
            ),
            LineItemSnapshot(
                description = "Design Work - UI/UX",
                quantity = 1.0,
                unitPrice = 80000L,   // $800.00
                total = 80000L
            )
        )

        val subtotal = 150000L + 500000L + 80000L
        val taxRate = 0.10
        val tax = (subtotal * taxRate).toLong()
        val total = subtotal + tax

        return InvoiceSnapshot(
            invoiceId = 12345L,
            invoiceNumber = "INV-2026-04-001",
            displayName = "Invoice 2026-04-001",
            customerName = "John Smith",
            customerEmail = "john.smith@example.com",
            customerAddress = "456 Customer Avenue, Melbourne VIC 3000, Australia",
            date = now,
            dueDate = dueDateMs,
            items = items,
            subtotal = subtotal,
            taxRate = taxRate,
            taxAmount = tax,
            totalAmount = total,
            businessName = "ACME Corporation Pty Ltd",
            businessAbn = "45 832 010 284",
            businessEmail = "contact@acmecorp.com.au",
            businessPhone = "+61 (2) 5555 1234",
            businessAddress = "123 Business Street, Suite 500, Sydney NSW 2000, Australia",
            logoBase64 = null,
            currencyCode = "AUD",
            headerText = "",
            subheaderText = "",
            footerText = "",
            notes = "Thank you for your business. Please remit payment by the due date to maintain account status.",
            bankAccountName = "ACME Corporation Pty Ltd Operating Account",
            bankAccountNumber = "123456789",
            bankBsb = "06-222-245",
            bankName = "Commonwealth Bank of Australia",
            invoiceStatus = "DRAFT"
        )
    }

    fun generatePreviewQuote(): InvoiceSnapshot {
        return generatePreviewInvoice().copy(
            invoiceNumber = "QTE-2026-04-001",
            displayName = "Quote 2026-04-001",
            invoiceStatus = "DRAFT"
        )
    }
}
```

---

## 3. PageLayout.kt - New File

Located: `app/src/main/java/com/emul8r/bizap/data/service/pdf_layouts/PageLayout.kt`

```kotlin
interface PageLayout {
    fun generateHtml(snapshot: InvoiceSnapshot, cssContent: String): String
}

class ClassicLayout : PageLayout {
    override fun generateHtml(snapshot: InvoiceSnapshot, cssContent: String): String {
        // ... generates traditional invoice HTML (see file for full implementation)
    }
}

class ModernLayout : PageLayout {
    override fun generateHtml(snapshot: InvoiceSnapshot, cssContent: String): String {
        // ... generates compact modern invoice HTML (see file for full implementation)
    }
}
```

---

## 4. InvoiceSettingsViewModel.kt - New Methods

### Import additions (lines 7-8)

```kotlin
import com.emul8r.bizap.domain.model.PdfEngine
import com.emul8r.bizap.domain.model.PageLayout
```

### New methods (after updateSelectedTheme)

```kotlin
fun updateSelectedPdfEngine(engine: PdfEngine) {
    _uiState.value.settings?.let { current ->
        _uiState.value = _uiState.value.copy(
            settings = current.copy(selectedPdfEngine = engine)
        )
    }
}

fun updateSelectedPageLayout(layout: PageLayout) {
    _uiState.value.settings?.let { current ->
        _uiState.value = _uiState.value.copy(
            settings = current.copy(selectedPageLayout = layout)
        )
    }
}

fun updatePreviewWithPlaceholder(enabled: Boolean) {
    _uiState.value.settings?.let { current ->
        _uiState.value = _uiState.value.copy(
            settings = current.copy(previewWithPlaceholder = enabled)
        )
    }
}
```

---

## 5. MIGRATION_AddPdfEngineAndLayout.kt - New File

Located: `app/src/main/java/com/emul8r/bizap/data/local/migration/MIGRATION_AddPdfEngineAndLayout.kt`

```kotlin
object MIGRATION_AddPdfEngineAndLayout : Migration(
    startVersion = 39,
    endVersion = 40
) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add selected_pdf_engine column
        database.execSQL(
            """
            ALTER TABLE invoice_settings 
            ADD COLUMN selected_pdf_engine TEXT NOT NULL DEFAULT 'HTML_CSS'
            """.trimIndent()
        )

        // Add selected_page_layout column
        database.execSQL(
            """
            ALTER TABLE invoice_settings 
            ADD COLUMN selected_page_layout TEXT NOT NULL DEFAULT 'MODERN'
            """.trimIndent()
        )

        // Add preview_with_placeholder column
        database.execSQL(
            """
            ALTER TABLE invoice_settings 
            ADD COLUMN preview_with_placeholder INTEGER NOT NULL DEFAULT 0
            """.trimIndent()
        )

        // Create indexes
        database.execSQL(
            """
            CREATE INDEX IF NOT EXISTS idx_invoice_settings_pdf_engine 
            ON invoice_settings(selected_pdf_engine)
            """.trimIndent()
        )

        database.execSQL(
            """
            CREATE INDEX IF NOT EXISTS idx_invoice_settings_page_layout 
            ON invoice_settings(selected_page_layout)
            """.trimIndent()
        )
    }
}
```

---

## 6. DatabaseModule.kt - Import & Registration

### Import addition (line 16)

```kotlin
import com.emul8r.bizap.data.local.migration.MIGRATION_AddPdfEngineAndLayout
```

### Migration registration (in addMigrations list)

```kotlin
.addMigrations(
    // ... existing migrations ...
    MIGRATION_AddInvoiceSettings,  // Add invoice_settings table for Phase 4
    MIGRATION_38_39,               // Add selected_html_style and selected_canvas_template columns
    MIGRATION_AddPdfEngineAndLayout  // Add PDF engine and page layout columns for three-tier architecture
)
```

---

## 7. AppDatabase.kt - Version Update

### Version change (line 47)

```kotlin
version = 40,  // v39→40: Add PDF engine, page layout, and preview with placeholder columns
```

---

## Summary of Changes

| File | Type | Changes |
|------|------|---------|
| InvoiceSettings.kt | Modified | +2 enums, +3 fields |
| PlaceholderInvoiceGenerator.kt | Created | 78 lines |
| PageLayout.kt | Created | 280 lines (interface + 2 layouts) |
| InvoiceSettingsViewModel.kt | Modified | +3 methods, +2 imports |
| MIGRATION_AddPdfEngineAndLayout.kt | Created | 63 lines |
| DatabaseModule.kt | Modified | +1 import, +1 migration |
| AppDatabase.kt | Modified | version 39 → 40 |

**Total new code: ~430 lines**  
**Total modifications: ~35 lines**  
**Build status: ✅ SUCCESS**  

---

## Verification Commands

To verify Phase 1 implementation:

```bash
# Compile Kotlin code
./gradlew :app:compileDebugKotlin

# Expected output: BUILD SUCCESSFUL in ~19 seconds

# Check new files exist
ls -la app/src/main/java/com/emul8r/bizap/domain/model/PlaceholderInvoiceGenerator.kt
ls -la app/src/main/java/com/emul8r/bizap/data/service/pdf_layouts/PageLayout.kt
ls -la app/src/main/java/com/emul8r/bizap/data/local/migration/MIGRATION_AddPdfEngineAndLayout.kt

# Build APK
./gradlew assembleDebug
```

---

*End of Phase 1 Code Reference*

