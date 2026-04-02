# 📖 INVOICE SETTINGS SYSTEM - USER & ADMIN GUIDE

---

## 👤 USER GUIDE

### What is the Invoice Settings System?

The Invoice Settings System allows you to:
- ✅ Create and manage company information
- ✅ Configure invoice appearance (themes)
- ✅ Set payment terms and methods
- ✅ Manage tax configuration
- ✅ Create professional invoices

### Getting Started

#### Step 1: Access Settings
1. Open the app menu
2. Navigate to Settings
3. Select "Invoice Settings"

#### Step 2: Configure Your Company

**Required Information:**
- Company Name
- Email Address
- Phone Number
- Business Address

**Optional Information:**
- Website
- Tax ID
- Bank Details
- Payment Methods

#### Step 3: Choose a Theme

**Available Themes:**
- **Canvas Theme:** Professional, traditional invoice design
- **HTML-to-PDF Theme:** Modern, designer-focused layout

Select your preferred theme from the dropdown.

#### Step 4: Set Payment Terms

Configure how customers should pay:
- Payment terms (30, 45, 60 days, etc.)
- Payment methods (EFT, Bank Transfer, etc.)
- Bank account details (if applicable)

#### Step 5: Configure Taxes

Set up tax information:
- Tax name (GST, VAT, etc.)
- Tax rate (percentage)
- Tax ID/ABN

### Creating Invoices

Once settings are configured:

1. Go to "Create Invoice"
2. Select a customer
3. Add line items with quantities and prices
4. Set invoice date and due date
5. Add any notes or special instructions
6. Generate PDF

The invoice will use your configured theme and company information automatically.

### Troubleshooting

**Q: Why isn't my theme showing?**
A: Make sure you've selected a theme in Invoice Settings and saved the changes.

**Q: Can I change themes later?**
A: Yes! Go to Invoice Settings, select a different theme, and all new invoices will use that theme.

**Q: Where are my invoices saved?**
A: Invoices are generated as PDF files and saved to your device's default downloads folder.

**Q: Can I preview my settings?**
A: Yes! In Invoice Settings, there's a preview section showing how your invoices will look.

---

## 🔧 ADMIN GUIDE

### System Architecture

The Invoice Settings System consists of:

```
┌─────────────────────────────────┐
│   Invoice Settings Database      │
│   (Room Database)               │
└──────────────┬──────────────────┘
               │
      ┌────────┴────────┐
      │                 │
┌─────▼────────┐  ┌────▼──────────┐
│  Repository  │  │  DAO Layer    │
│ (Business    │  │ (Database     │
│  Logic)      │  │  Access)      │
└─────┬────────┘  └───────────────┘
      │
┌─────▼──────────────────────┐
│   View Models & UI          │
│   (Settings Screen)         │
└────────────────────────────┘
```

### Database Schema

**InvoiceSettings Table:**
```sql
CREATE TABLE invoice_settings (
  user_id TEXT PRIMARY KEY,
  business_name TEXT,
  business_email TEXT,
  business_phone TEXT,
  business_address TEXT,
  business_website TEXT,
  tax_id TEXT,
  tax_rate REAL,
  tax_name TEXT,
  payment_terms_days INTEGER,
  default_payment_notes TEXT,
  footer_message TEXT,
  invoice_number_prefix TEXT,
  selected_theme TEXT,
  primary_color TEXT,
  secondary_color TEXT,
  bank_name TEXT,
  account_number TEXT,
  routing_code TEXT,
  account_holder TEXT
)
```

### Configuration

#### Environment Variables (if applicable)
- `INVOICE_SETTINGS_DB_NAME`: Database name
- `INVOICE_THEME_DEFAULT`: Default theme (CANVAS or HTML_PDF)

#### Performance Tuning

**Caching:**
- Settings are cached in memory for fast retrieval
- Cache is invalidated on updates
- Clear cache if data becomes stale: `repository.clearCache()`

**Database:**
- Uses Room for efficient local storage
- Indexed queries on user_id for fast lookups
- Connection pooling for concurrent access

### Maintenance

#### Backup & Recovery

**Backup Settings:**
```
Room database is stored at:
/data/data/com.emul8r.bizap/databases/bizap_database.db
```

**Restore:**
1. Close the app
2. Copy backup database file
3. Restart app

#### Monitoring

Key metrics to monitor:
- Settings load time (target: < 500ms)
- Settings save time (target: < 200ms)
- Memory usage (target: < 50MB)
- Cache hit rate (target: > 80%)

#### Troubleshooting

**Settings not saving?**
1. Check database permissions
2. Verify disk space available
3. Check app logs for errors

**Performance degradation?**
1. Clear cache: `repository.clearCache()`
2. Rebuild database indexes
3. Check for memory leaks

### API Reference

#### Repository Methods

```kotlin
// Get settings
suspend fun getSettings(userId: String): InvoiceSettings?

// Save settings
suspend fun saveSettings(settings: InvoiceSettings): Boolean

// Clear cache
fun clearCache()
fun clearCache(userId: String)
```

#### DAO Methods

```kotlin
// Get settings by user ID
suspend fun getSettingsByUserId(userId: String): InvoiceSettings?

// Insert or update settings
suspend fun insertOrUpdateSettings(settings: InvoiceSettings)

// Delete settings
suspend fun deleteSettings(userId: String)
```

### Security Considerations

1. **Data Protection:**
   - Settings stored in app-private database
   - Accessible only within the app
   - No sensitive data transmitted without encryption

2. **Access Control:**
   - Each user has isolated settings
   - No cross-user data leakage
   - User ID validates ownership

3. **Best Practices:**
   - Regularly backup settings
   - Monitor for unauthorized access
   - Keep app updated for security patches

---

## 📋 COMMON TASKS

### Task: Export Settings

Currently not directly supported. Workaround:
1. Take screenshot of settings
2. Document company information
3. Manually recreate in new device

(Feature for future enhancement)

### Task: Import Settings

Currently not supported. Workaround:
1. Manually enter settings
2. Or copy database file directly

(Feature for future enhancement)

### Task: Change Theme

1. Go to Invoice Settings
2. Select different theme
3. Click Save
4. New invoices use new theme

Previous invoices remain with original theme.

---

## 🚀 DEPLOYMENT NOTES

### Prerequisites
- Android 8.0+
- 50MB free storage
- Normal app permissions

### First-Time Setup
1. User opens app
2. Navigates to Settings
3. Configures company info
4. Selects theme
5. System ready to create invoices

### Post-Deployment Checks
- ✅ Settings screen opens
- ✅ All fields editable
- ✅ Theme selection works
- ✅ Save button functions
- ✅ Settings persist after restart

---

## 📞 SUPPORT

For issues or questions:
1. Check this guide first
2. Review troubleshooting section
3. Check app logs (Logcat)
4. Contact support with error details

---

**Document Version:** 1.0  
**Last Updated:** March 30, 2026  
**Status:** Complete


