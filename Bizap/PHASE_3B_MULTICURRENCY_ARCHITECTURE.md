# PHASE 3B: SIMPLIFIED CURRENCY ARCHITECTURE

Following the decision to focus on a **Primary Currency** model, this architecture ensures the Bizap engine can support international business identities without the complexity of real-time exchange rates.

## 1. VISION: THE GLOBAL IDENTITY
Phase 3B formalizes how money is represented across the system. Instead of hardcoded "$" symbols, the app will respect a "System Currency" defined in the Business Profile.

## 2. BUSINESS PROFILE ENHANCEMENTS
We add two critical fields to the `BusinessProfile` to drive the formatting engine.

| Field | Type | Description | Example |
|-------|------|-------------|---------|
| `currencyCode` | String | ISO 4217 Code | "USD", "AUD", "EUR" |
| `currencySymbol` | String | Visual character | "$", "€", "£" |

### 2.1 Domain Model Update
```kotlin
data class BusinessProfile(
    // ... existing fields ...
    val currencyCode: String = "AUD",
    val currencySymbol: String = "$"
)
```

---

## 3. DOMAIN LAYER: THE FORMATTING ENGINE

### 3.1 CurrencyFormatter Utility
We will create a pure Kotlin utility in `domain.utils` to ensure deterministic formatting.

```kotlin
object CurrencyFormatter {
    fun format(amount: Double, symbol: String): String {
        return "$symbol${String.format(Locale.getDefault(), "%.2f", amount)}"
    }
}
```

### 3.2 Invoice Model Update
The `Invoice` domain model will now store the symbol active at the time of generation.

```kotlin
data class Invoice(
    // ...
    val currencySymbol: String = "$", // Snapshot of brand currency
    // ...
)
```

---

## 4. DATA LAYER: STORAGE & MIGRATION (V8)

### 4.1 Preferences DataStore
The `BusinessProfileRepository` will be updated to store `currency_code` and `currency_symbol`.

### 4.2 Migration SQL (MIGRATION_7_8)
```sql
-- Add currency snapshot to invoices
ALTER TABLE invoices ADD COLUMN currencySymbol TEXT NOT NULL DEFAULT '$';
```

---

## 5. UI LAYER: DYNAMIC ACCENTS
All currency displays will be updated from:
`Text("$${amount}")` 
to:
`Text(CurrencyFormatter.format(amount, themeConfig.currencySymbol))`

### 5.1 Currency Selector Component
A new dropdown in the **Business Profile Settings** allowing the user to select from common presets (AUD, USD, EUR, GBP, NZD) or enter a custom symbol.

---

## 6. PDF RENDERING INTEGRATION
The `InvoicePdfService` will be updated to pull the `currencySymbol` from the `InvoiceSnapshot`. 
*   The `PdfTableRenderer` will automatically adjust the "Price" and "Amount" columns to accommodate varying symbol widths (e.g., "€" vs "AUD$").

**Next Step:** Proceed to `PHASE_3_INTEGRATION_LAYER.md`.
