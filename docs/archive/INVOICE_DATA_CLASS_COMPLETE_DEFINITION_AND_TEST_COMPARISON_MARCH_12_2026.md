# 📊 INVOICE DATA CLASS COMPLETE DEFINITION & TEST COMPARISON (March 12, 2026)

**Source:** `domain/model/Invoice.kt`  
**Test File:** `InvoiceOperationsTest.kt` (lines ~50, ~75, ~110, ~145)  
**Date:** March 12, 2026  

---

## ✅ COMPLETE INVOICE DATA CLASS DEFINITION

### **Full Class Signature (89 lines total)**

```kotlin
@Serializable
data class Invoice(
    // PRIMARY FIELDS (No defaults)
    val customerId: Long?,              // ← NULLABLE
    val customerName: String,           // ← NO DEFAULT
    val date: Long,                     // ← NO DEFAULT (invoice date, millis)
    val totalAmount: Long,              // ← NO DEFAULT (Cents)
    val items: List<LineItem>,          // ← NO DEFAULT
    val isQuote: Boolean,               // ← NO DEFAULT
    val status: InvoiceStatus,          // ← NO DEFAULT
    
    // FIELDS WITH DEFAULTS
    val id: Long = 0,
    val businessProfileId: Long = 0,
    val customerAddress: String = "",
    val customerEmail: String? = null,           // ← NULLABLE with default
    val dueDate: Long = 0,
    val header: String? = null,                  // ← NULLABLE with default
    val subheader: String? = null,               // ← NULLABLE with default
    val notes: String? = null,                   // ← NULLABLE with default
    val footer: String? = null,                  // ← NULLABLE with default
    val photoUris: List<String> = emptyList(),
    val pdfUri: String? = null,                  // ← NULLABLE with default
    val taxRate: Double = 0.1,
    val taxAmount: Long = 0,
    val companyLogoPath: String? = null,         // ← NULLABLE with default
    val updatedAt: Long = 0,
    val amountPaid: Long = 0,
    val parentInvoiceId: Long? = null,           // ← NULLABLE with default
    val version: Int = 1,
    val invoiceYear: Int = 0,
    val invoiceSequence: Int = 0,
    val currencyCode: String = "AUD"
) { ... }
```

---

## 📋 PROPERTY ANALYSIS TABLE

| Property | Type | Default | Nullable | Required? |
|----------|------|---------|----------|-----------|
| **id** | Long | 0 | NO | Optional |
| **businessProfileId** | Long | 0 | NO | Optional |
| **customerId** | Long? | NONE | YES | YES |
| **customerName** | String | NONE | NO | YES |
| **customerAddress** | String | "" | NO | Optional |
| **customerEmail** | String? | null | YES | Optional |
| **date** | Long | NONE | NO | YES |
| **dueDate** | Long | 0 | NO | Optional |
| **totalAmount** | Long | NONE | NO | YES |
| **items** | List<LineItem> | NONE | NO | YES |
| **isQuote** | Boolean | NONE | NO | YES |
| **status** | InvoiceStatus | NONE | NO | YES |
| **header** | String? | null | YES | Optional |
| **subheader** | String? | null | YES | Optional |
| **notes** | String? | null | YES | Optional |
| **footer** | String? | null | YES | Optional |
| **photoUris** | List<String> | emptyList() | NO | Optional |
| **pdfUri** | String? | null | YES | Optional |
| **taxRate** | Double | 0.1 | NO | Optional |
| **taxAmount** | Long | 0 | NO | Optional |
| **companyLogoPath** | String? | null | YES | Optional |
| **updatedAt** | Long | 0 | NO | Optional |
| **amountPaid** | Long | 0 | NO | Optional |
| **parentInvoiceId** | Long? | null | YES | Optional |
| **version** | Int | 1 | NO | Optional |
| **invoiceYear** | Int | 0 | NO | Optional |
| **invoiceSequence** | Int | 0 | NO | Optional |
| **currencyCode** | String | "AUD" | NO | Optional |

**TOTAL: 27 properties**
- **7 with no default (REQUIRED):** customerId, customerName, date, totalAmount, items, isQuote, status
- **20 with defaults (OPTIONAL)**
- **7 nullable properties:** customerId, customerEmail, header, subheader, notes, footer, pdfUri, companyLogoPath, parentInvoiceId

---

## 🔍 TEST CONSTRUCTOR ANALYSIS

### **Test Pattern (appears in InvoiceOperationsTest.kt multiple times)**

```kotlin
val invoice = Invoice(
    id = 1L,
    businessProfileId = 1L,
    customerId = 1L,
    customerName = "Test",
    customerAddress = "",
    customerEmail = "",
    items = emptyList(),
    totalAmount = 1000L,
    amountPaid = 0L,
    status = InvoiceStatus.DRAFT,
    date = System.currentTimeMillis(),
    dueDate = System.currentTimeMillis(),
    isQuote = false,
    currencyCode = "AUD",
    taxRate = 0.0,
    taxAmount = 0L,
    invoiceYear = 2026,
    invoiceSequence = 1,
    notes = ""
)
```

---

## ⚠️ PROPERTIES MISSING FROM TEST CONSTRUCTOR

### **Properties NOT explicitly set in test constructor calls:**

| Property | Default Used? | Issue |
|----------|---|---|
| **header** | ✅ null (default) | Not set, uses default |
| **subheader** | ✅ null (default) | Not set, uses default |
| **footer** | ✅ null (default) | Not set, uses default |
| **photoUris** | ✅ emptyList() (default) | Not set, uses default |
| **pdfUri** | ✅ null (default) | Not set, uses default |
| **companyLogoPath** | ✅ null (default) | Not set, uses default |
| **updatedAt** | ✅ 0 (default) | Not set, uses default |
| **parentInvoiceId** | ✅ null (default) | Not set, uses default |
| **version** | ✅ 1 (default) | Not set, uses default |

### **Total:**
- **9 properties missing** from explicit constructor calls
- **But all have defaults**, so they work fine

---

## ✅ PROPERTIES EXPLICITLY PROVIDED IN TESTS

**14 properties explicitly set:**
1. ✅ id
2. ✅ businessProfileId
3. ✅ customerId
4. ✅ customerName
5. ✅ customerAddress
6. ✅ customerEmail
7. ✅ items
8. ✅ totalAmount
9. ✅ amountPaid
10. ✅ status
11. ✅ date
12. ✅ dueDate
13. ✅ isQuote
14. ✅ currencyCode
15. ✅ taxRate
16. ✅ taxAmount
17. ✅ invoiceYear
18. ✅ invoiceSequence
19. ✅ notes

---

## 🎯 KEY FINDING: THE ACTUAL TEST PROBLEM

Looking at the test constructor calls, **they're actually CORRECT:**

```kotlin
val invoice = Invoice(
    id = 1L,
    businessProfileId = 1L,
    customerId = 1L,           // ← Long? (nullable) - correctly provided
    customerName = "Test",     // ← Required, provided
    items = emptyList(),       // ← Required, provided
    totalAmount = 1000L,       // ← Required, provided
    date = System.currentTimeMillis(),  // ← Required, provided
    isQuote = false,           // ← Required, provided
    status = InvoiceStatus.DRAFT,  // ← Required, provided
    // ... other optional properties ...
)
```

**All 7 required fields are provided.**

---

## 🔴 THE REAL COMPILATION ERROR SOURCE

Going back to the gradle error:

```
e: file:///InvoiceOperationsTest.kt:231:69 Operator call is prohibited on a nullable receiver of type 'kotlin.Long?'. 
Use '?.'-qualified call instead.
```

**Line 231 in the test is:**
```kotlin
val isValid = invoice.totalAmount > 0 && invoice.customerId > 0
                                        ↑ This is Long? 
```

**This is a DIFFERENT issue from the constructor.**

The issue is in a **validation check** where the test tries to use `>` on `invoice.customerId` which is `Long?`.

---

## 📈 SUMMARY

### **Constructor Calls in Tests: ✅ CORRECT**
- All 7 required properties provided
- Missing 9 properties all have defaults
- Invoice objects can be instantiated successfully

### **Compilation Error at Line 231: ❌ REAL ISSUE**
- Using `>` operator on nullable `Long?` type
- Should be: `(invoice.customerId ?: 0L) > 0`
- OR: `invoice.customerId != null && invoice.customerId > 0`

### **Other Compilation Errors: ✅ UNDERSTOOD**
- **PaymentRepositoryTest:** Missing `returns Unit` in MockK setup
- **DataStore tests:** Missing `<Preferences>` type parameter

---

## 🎓 CONCLUSION

The Invoice data class definition is **correct and complete**.
The test constructor calls are **correctly providing all required fields**.
The actual errors are **NOT in the constructor calls**, but in:
1. Using operators on nullable types (Line 231)
2. Incomplete MockK setup for PaymentDaoV2
3. Missing generic type parameters for DataStore

---

**Complete Definition:** 27 properties total  
**Required in tests:** 7 properties  
**Missing (with defaults):** 9 properties  
**Status:** Test constructors are VALID  


