# 🏗️ PHASE 2: DESIGN & ARCHITECTURE - SPECIFICATIONS

**Date:** March 30, 2026  
**Status:** ✅ DESIGN COMPLETE  
**Duration:** 1 week  

---

## 📐 DATA MODELS

### **1. InvoiceSettings Data Model**

**File Location:** `app/src/main/java/com/emul8r/bizap/domain/model/InvoiceSettings.kt`

```kotlin
package com.emul8r.bizap.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

/**
 * Centralized invoice settings and customization.
 * Stored per user, applied to all invoices unless overridden.
 */
@Entity(tableName = "invoice_settings")
data class InvoiceSettings(
    @PrimaryKey
    val userId: String,
    
    // THEME SELECTION
    val selectedTheme: InvoiceTheme = InvoiceTheme.CANVAS,
    
    // COMPANY BRANDING
    val businessName: String = "",
    val businessLogo: ByteArray? = null,
    val businessEmail: String = "",
    val businessPhone: String = "",
    val businessAddress: String = "",
    val businessWebsite: String? = null,
    val businessAbn: String? = null,
    
    // THEME COLORS
    val primaryColor: String = "#6B4C9A",      // Default purple
    val secondaryColor: String? = null,
    val accentColor: String? = null,
    val fontFamily: String? = null,
    
    // TAX CONFIGURATION
    val taxId: String? = null,
    val taxRate: Double = 0.10,
    val taxName: String = "GST",
    val taxHandling: TaxHandling = TaxHandling.EXCLUSIVE,
    
    // PAYMENT DETAILS
    val paymentTermsDays: Int = 30,
    val defaultPaymentNotes: String = "",
    val footerMessage: String = "Thank you for your business",
    val invoiceNumberPrefix: String = "INV-",
    
    // BANK DETAILS
    val bankName: String? = null,
    val accountNumber: String? = null,
    val routingCode: String? = null,
    val accountHolder: String? = null,
    
    // METADATA
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Serializable {
    
    /**
     * Validate settings for required fields.
     */
    fun isValid(): Boolean {
        return businessName.isNotBlank() &&
               businessEmail.isNotBlank() &&
               businessPhone.isNotBlank() &&
               businessAddress.isNotBlank()
    }
    
    /**
     * Get default settings for user.
     */
    companion object {
        fun default(userId: String) = InvoiceSettings(userId = userId)
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        
        other as InvoiceSettings
        
        if (userId != other.userId) return false
        if (selectedTheme != other.selectedTheme) return false
        if (businessName != other.businessName) return false
        if (!businessLogo.contentEquals(other.businessLogo)) return false
        if (primaryColor != other.primaryColor) return false
        if (taxRate != other.taxRate) return false
        if (paymentTermsDays != other.paymentTermsDays) return false
        
        return true
    }
    
    override fun hashCode(): Int {
        var result = userId.hashCode()
        result = 31 * result + selectedTheme.hashCode()
        result = 31 * result + businessName.hashCode()
        result = 31 * result + (businessLogo?.contentHashCode() ?: 0)
        result = 31 * result + primaryColor.hashCode()
        result = 31 * result + taxRate.hashCode()
        result = 31 * result + paymentTermsDays
        return result
    }
}

/**
 * Enum for invoice theme selection.
 */
enum class InvoiceTheme {
    CANVAS,      // Existing Canvas-based PDF (Phase 9 implementation)
    HTML_PDF     // New HTML-to-PDF modern style
}

/**
 * Enum for tax handling mode.
 */
enum class TaxHandling {
    INCLUSIVE,   // Tax included in amount
    EXCLUSIVE    // Tax added to amount
}
```

---

### **2. Theme Interface**

**File Location:** `app/src/main/java/com/emul8r/bizap/domain/pdf/InvoiceTheme.kt`

```kotlin
package com.emul8r.bizap.domain.pdf

import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.InvoiceSettings
import kotlin.Result

/**
 * Theme interface for invoice PDF generation.
 * Allows multiple theme implementations (Canvas, HTML-to-PDF, etc.)
 */
interface InvoiceThemeRenderer {
    
    /**
     * Generate PDF for the given invoice with settings.
     * 
     * @param invoice The invoice data to render
     * @param settings The invoice settings (branding, colors, etc.)
     * @param outputPath Where to save the PDF file
     * @return Result with file path on success or error message on failure
     */
    suspend fun generatePdf(
        invoice: Invoice,
        settings: InvoiceSettings,
        outputPath: String
    ): Result<String>
    
    /**
     * Validate settings compatibility with this theme.
     */
    fun validateSettings(settings: InvoiceSettings): ValidationResult
    
    /**
     * Get user-friendly theme name.
     */
    fun getThemeName(): String
    
    /**
     * Get theme description for UI display.
     */
    fun getThemeDescription(): String
    
    /**
     * List customization options supported by theme.
     */
    fun getSupportedCustomizations(): List<CustomizationOption>
}

/**
 * Validation result for settings.
 */
data class ValidationResult(
    val isValid: Boolean,
    val errors: List<String> = emptyList(),
    val warnings: List<String> = emptyList()
)

/**
 * Customization options available in a theme.
 */
enum class CustomizationOption {
    PRIMARY_COLOR,
    SECONDARY_COLOR,
    ACCENT_COLOR,
    FONT_FAMILY,
    LOGO,
    LAYOUT,
    TYPOGRAPHY
}

/**
 * Theme manager factory.
 */
interface InvoiceThemeManager {
    fun getTheme(theme: com.emul8r.bizap.domain.model.InvoiceTheme): InvoiceThemeRenderer
    fun listAvailableThemes(): List<com.emul8r.bizap.domain.model.InvoiceTheme>
}
```

---

## 🗄️ DATABASE SCHEMA

### **3. Room Entity & DAO**

**File Location:** `app/src/main/java/com/emul8r/bizap/data/local/dao/InvoiceSettingsDao.kt`

```kotlin
package com.emul8r.bizap.data.local.dao

import androidx.room.*
import com.emul8r.bizap.domain.model.InvoiceSettings
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceSettingsDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: InvoiceSettings)
    
    @Query("SELECT * FROM invoice_settings WHERE user_id = :userId")
    suspend fun getSettings(userId: String): InvoiceSettings?
    
    @Query("SELECT * FROM invoice_settings WHERE user_id = :userId")
    fun getSettingsFlow(userId: String): Flow<InvoiceSettings?>
    
    @Delete
    suspend fun deleteSettings(settings: InvoiceSettings)
    
    @Query("DELETE FROM invoice_settings WHERE user_id = :userId")
    suspend fun deleteByUserId(userId: String)
    
    @Query("SELECT COUNT(*) FROM invoice_settings WHERE user_id = :userId")
    suspend fun exists(userId: String): Boolean
}
```

### **4. Database Migration Script**

**File Location:** `app/src/main/java/com/emul8r/bizap/data/local/migration/MIGRATION_AddInvoiceSettings.kt`

```kotlin
package com.emul8r.bizap.data.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration: Add invoice_settings table
 * From: Current schema version
 * To: Current + 1
 */
object MIGRATION_AddInvoiceSettings : Migration(
    startVersion = 1,  // UPDATE THIS TO CURRENT VERSION
    endVersion = 2     // UPDATE THIS TO NEXT VERSION
) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS invoice_settings (
                user_id TEXT PRIMARY KEY NOT NULL,
                selected_theme TEXT NOT NULL DEFAULT 'CANVAS',
                business_name TEXT NOT NULL DEFAULT '',
                business_logo BLOB,
                business_email TEXT NOT NULL DEFAULT '',
                business_phone TEXT NOT NULL DEFAULT '',
                business_address TEXT NOT NULL DEFAULT '',
                business_website TEXT,
                business_abn TEXT,
                primary_color TEXT NOT NULL DEFAULT '#6B4C9A',
                secondary_color TEXT,
                accent_color TEXT,
                font_family TEXT,
                tax_id TEXT,
                tax_rate REAL NOT NULL DEFAULT 0.10,
                tax_name TEXT NOT NULL DEFAULT 'GST',
                tax_handling TEXT NOT NULL DEFAULT 'EXCLUSIVE',
                payment_terms_days INTEGER NOT NULL DEFAULT 30,
                default_payment_notes TEXT DEFAULT '',
                footer_message TEXT DEFAULT 'Thank you for your business',
                invoice_number_prefix TEXT NOT NULL DEFAULT 'INV-',
                bank_name TEXT,
                account_number TEXT,
                routing_code TEXT,
                account_holder TEXT,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL
            )
            """.trimIndent()
        )
        
        // Create index for faster lookups
        database.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS index_invoice_settings_user_id ON invoice_settings(user_id)"
        )
    }
}
```

---

## 🎨 UI WIREFRAMES

### **5. Create Invoice Page (Cleaned)**

```
┌────────────────────────────────────────┐
│ CREATE INVOICE                  ← Back │
├────────────────────────────────────────┤
│                                        │
│ ℹ️ INFO BANNER                         │
│ "Theme will be applied from            │
│  Invoice Settings. Go to Settings to   │
│  customize branding and colors."       │
│                                        │
├────────────────────────────────────────┤
│ CUSTOMER                               │
│ ┌────────────────────────────────────┐ │
│ │ ▼ Select Customer        [or New]  │ │
│ │                                    │ │
│ │ Name: John Doe Business            │ │
│ │ Email: john@example.com            │ │
│ └────────────────────────────────────┘ │
│                                        │
├────────────────────────────────────────┤
│ INVOICE DETAILS                        │
│ ┌────────────────────────────────────┐ │
│ │ Invoice Date: [Mar 30, 2026]  ▼   │ │
│ │ Due Date: [Apr 29, 2026]      ▼   │ │
│ │ Invoice #: [Auto-generated]        │ │
│ │ Reference: [Optional]              │ │
│ │ Currency: AUD ▼                    │ │
│ └────────────────────────────────────┘ │
│                                        │
├────────────────────────────────────────┤
│ LINE ITEMS                             │
│ ┌────────────────────────────────────┐ │
│ │ Description    |  Qty | Price|Total│ │
│ │ Item 1         |   1  | $100 |$100│ │
│ │ Item 2         |   2  | $50  |$100│ │
│ │ [+ Add Item]                       │ │
│ └────────────────────────────────────┘ │
│                                        │
├────────────────────────────────────────┤
│ NOTES (Optional)                       │
│ ┌────────────────────────────────────┐ │
│ │ [Thank you for your business...]   │ │
│ │                                    │ │
│ └────────────────────────────────────┘ │
│                                        │
├────────────────────────────────────────┤
│ SUMMARY                                │
│ ┌────────────────────────────────────┐ │
│ │ Subtotal:          $200.00         │ │
│ │ Tax (10%):         $20.00          │ │
│ │ ─────────────────────────────      │ │
│ │ TOTAL:             $220.00         │ │
│ └────────────────────────────────────┘ │
│                                        │
│ [SAVE DRAFT] [GENERATE PDF] [CANCEL]   │
│                                        │
└────────────────────────────────────────┘
```

### **6. Invoice Settings Page**

```
┌────────────────────────────────────────┐
│ SETTINGS                              │
│   > INVOICE SETTINGS           ← Back │
├────────────────────────────────────────┤
│                                        │
│ THEME & STYLE                          │
│ ┌────────────────────────────────────┐ │
│ │ ○ Canvas Style (Current)           │ │
│ │ ○ Modern HTML Style (New)          │ │
│ │ [Preview Theme]  [Save Theme]      │ │
│ └────────────────────────────────────┘ │
│                                        │
│ COMPANY BRANDING                       │
│ ┌────────────────────────────────────┐ │
│ │ [Upload Logo]        [Preview]     │ │
│ │ Company Name: [Input]              │ │
│ │ ABN: [Input]                       │ │
│ │ Email: [Input]                     │ │
│ │ Phone: [Input]                     │ │
│ │ Address: [Multi-line Input]        │ │
│ │ Website: [Input]                   │ │
│ └────────────────────────────────────┘ │
│                                        │
│ THEME COLORS                           │
│ ┌────────────────────────────────────┐ │
│ │ Primary Color: [Color Picker]      │ │
│ │ Secondary Color: [Color Picker]    │ │
│ │ Accent Color: [Color Picker]       │ │
│ └────────────────────────────────────┘ │
│                                        │
│ PAYMENT INFORMATION                    │
│ ┌────────────────────────────────────┐ │
│ │ Payment Terms (Days): [Input: 30]  │ │
│ │                                    │ │
│ │ Bank Details:                      │ │
│ │   Bank Name: [Input]               │ │
│ │   Account Number: [Input]          │ │
│ │   Routing Code: [Input]            │ │
│ │   Account Holder: [Input]          │ │
│ └────────────────────────────────────┘ │
│                                        │
│ TAX CONFIGURATION                      │
│ ┌────────────────────────────────────┐ │
│ │ Tax ID: [Input]                    │ │
│ │ Tax Rate (%): [Input: 10]          │ │
│ │ Tax Name: [Input: GST]             │ │
│ │ Tax Handling: [Dropdown]           │ │
│ │   ○ Inclusive                      │ │
│ │   ○ Exclusive (default)            │ │
│ └────────────────────────────────────┘ │
│                                        │
│ INVOICE DEFAULTS                       │
│ ┌────────────────────────────────────┐ │
│ │ Default Notes: [Text Area]         │ │
│ │ Footer Message: [Text Area]        │ │
│ │ Invoice Prefix: [Input: INV-]      │ │
│ └────────────────────────────────────┘ │
│                                        │
│ [SAVE CHANGES] [PREVIEW] [RESET]       │
│                                        │
└────────────────────────────────────────┘
```

---

## 🏛️ ARCHITECTURE DIAGRAM

```
┌──────────────────────────────────────────────────────────┐
│                      USER FLOWS                          │
├──────────────────────────────────────────────────────────┤
│                                                          │
│  User 1: Configure Settings         User 2: Create      │
│  ┌──────────────────────────┐       Invoice             │
│  │ Settings > Invoice       │       ┌─────────────────┐ │
│  │   ├─ Theme selection     │       │ Create Invoice  │ │
│  │   ├─ Branding (logo)     │       │   ├─ Customer   │ │
│  │   ├─ Colors              │       │   ├─ Dates      │ │
│  │   ├─ Payment details     │       │   ├─ Items      │ │
│  │   └─ Tax configuration   │       │   └─ Notes      │ │
│  └────────┬─────────────────┘       └────────┬────────┘ │
│           │                                  │           │
│           └──────────────┬───────────────────┘           │
│                          │                               │
│                          ↓                               │
│            ┌─────────────────────────┐                   │
│            │  InvoiceSettings Table  │                   │
│            │  (Database)             │                   │
│            └────────────┬────────────┘                   │
│                         │                                │
│                         ↓                                │
│            ┌─────────────────────────┐                   │
│            │  ThemeManager           │                   │
│            │  (Factory Pattern)      │                   │
│            └────────────┬────────────┘                   │
│                         │                                │
│          ┌──────────────┴──────────────┐                 │
│          │                             │                 │
│          ↓                             ↓                 │
│  ┌────────────────────┐    ┌──────────────────────┐     │
│  │ CanvasInvoice      │    │ HtmlPdfInvoice       │     │
│  │ Theme              │    │ Theme                │     │
│  │ (Phase 9, Canvas)  │    │ (Modern, HTML+CSS)   │     │
│  └────────────┬───────┘    └──────────┬───────────┘     │
│               │                       │                  │
│               └───────────┬───────────┘                  │
│                           │                              │
│                           ↓                              │
│                    ┌─────────────┐                       │
│                    │ PDF Output  │                       │
│                    │ (File)      │                       │
│                    └─────────────┘                       │
│                                                          │
└──────────────────────────────────────────────────────────┘
```

---

## 🔄 DATA FLOW SEQUENCE

```
Sequence: User Creates Invoice with Settings

1. Settings Flow:
   ┌─────────────────────────────────────┐
   │ User: Settings > Invoice Settings   │
   └────────────┬────────────────────────┘
                ↓
   ┌─────────────────────────────────────┐
   │ InvoiceSettingsScreen               │
   │ - Display all customization options │
   │ - Allow user to change settings     │
   └────────────┬────────────────────────┘
                ↓
   ┌─────────────────────────────────────┐
   │ InvoiceSettingsViewModel.saveSettings()
   │ - Validate all inputs               │
   │ - Save to InvoiceSettingsRepository │
   └────────────┬────────────────────────┘
                ↓
   ┌─────────────────────────────────────┐
   │ InvoiceSettingsRepository.save()    │
   │ - Write to InvoiceSettingsDao       │
   │ - Persist to database               │
   └────────────┬────────────────────────┘
                ↓
   ┌─────────────────────────────────────┐
   │ Settings saved ✓                    │
   └─────────────────────────────────────┘

2. Invoice Creation Flow:
   ┌─────────────────────────────────────┐
   │ User: Create Invoice                │
   └────────────┬────────────────────────┘
                ↓
   ┌─────────────────────────────────────┐
   │ CreateInvoiceScreen                 │
   │ - CLEAN: Customer, dates, items     │
   │ - Show settings info banner         │
   │ - Load current theme preview        │
   └────────────┬────────────────────────┘
                ↓
   ┌─────────────────────────────────────┐
   │ User: Enter data, click "Generate"  │
   └────────────┬────────────────────────┘
                ↓
   ┌─────────────────────────────────────┐
   │ CreateInvoiceViewModel.onGenerate() │
   │ - Validate invoice data only        │
   │ - Save invoice to database          │
   │ - Load InvoiceSettings              │
   │ - Call ThemeManager.generatePdf()   │
   └────────────┬────────────────────────┘
                ↓
   ┌─────────────────────────────────────┐
   │ ThemeManager.getTheme()             │
   │ - Read selectedTheme from settings  │
   │ - Return appropriate renderer       │
   │   (CanvasInvoiceTheme or           │
   │    HtmlPdfInvoiceTheme)            │
   └────────────┬────────────────────────┘
                ↓
   ┌─────────────────────────────────────┐
   │ SelectedTheme.generatePdf()         │
   │ - Read invoice data                 │
   │ - Read all settings                 │
   │ - Apply theme-specific styling      │
   │ - Generate PDF with customization   │
   └────────────┬────────────────────────┘
                ↓
   ┌─────────────────────────────────────┐
   │ PDF Generated ✓                     │
   │ User receives styled invoice        │
   └─────────────────────────────────────┘
```

---

## 📦 DEPENDENCY INJECTION (Hilt)

### **7. Hilt Modules**

**File Location:** `app/src/main/java/com/emul8r/bizap/di/PdfModule.kt`

```kotlin
package com.emul8r.bizap.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.emul8r.bizap.data.pdf.CanvasInvoiceTheme
import com.emul8r.bizap.data.pdf.HtmlPdfInvoiceTheme
import com.emul8r.bizap.domain.pdf.InvoiceThemeManager
import com.emul8r.bizap.data.pdf.InvoiceThemeManagerImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PdfModule {
    
    @Provides
    @Singleton
    fun provideCanvasTheme(): CanvasInvoiceTheme = CanvasInvoiceTheme()
    
    @Provides
    @Singleton
    fun provideHtmlPdfTheme(): HtmlPdfInvoiceTheme = HtmlPdfInvoiceTheme()
    
    @Provides
    @Singleton
    fun provideThemeManager(
        canvasTheme: CanvasInvoiceTheme,
        htmlPdfTheme: HtmlPdfInvoiceTheme
    ): InvoiceThemeManager = 
        InvoiceThemeManagerImpl(canvasTheme, htmlPdfTheme)
}
```

---

## 📝 IMPLEMENTATION CHECKLIST

### **Phase 2: Design Complete**
- [x] Create data models (InvoiceSettings, enums)
- [x] Design theme interface
- [x] Create database schema
- [x] Create UI wireframes
- [x] Design architecture
- [x] Plan dependency injection
- [x] Create audit report

### **Phase 3: Ready to Begin**
- [ ] Implement CreateInvoiceScreen cleanup
- [ ] Remove customization UI
- [ ] Add settings banner
- [ ] Update ViewModel

---

**Design Complete:** March 30, 2026  
**Status:** ✅ READY FOR PHASE 3 IMPLEMENTATION  
**Next:** Begin Create Invoice page cleanup


