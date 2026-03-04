# Week 3: Safe Database Migrations + Core Unit Tests

**Date:** March 5, 2026  
**Status:** ✅ **READY FOR IMPLEMENTATION**

---

## 📋 Overview: What You're Learning This Week

### Part 1: Safe Database Migrations (3 hours)
- ✅ Understanding migration risks
- ✅ Room migration patterns
- ✅ Writing safe migrations (no data loss)
- ✅ Testing migrations
- ✅ Your current migration chain (v21→v24)

### Part 2: Core Unit Tests (4 hours)
- ✅ 10 essential unit tests
- ✅ Repository pattern testing
- ✅ ViewModel testing with mocks
- ✅ Use case testing
- ✅ Running tests + coverage

---

## ✅ PART 1: Safe Database Migrations

### What You've Already Fixed

Your `DatabaseModule.kt` is **CORRECT** - no `fallbackToDestructiveMigration()` 🎉

```kotlin
// ✅ SAFE - Uses explicit migrations
.addMigrations(MIGRATION_21_22, MIGRATION_22_23, MIGRATION_23_24)
.build()
```

### Your Migration Chain

```
v21 → v22: Drop pending_operations table (sync removed)
v22 → v23: Add currencyCode to line_items (multi-currency support)
v23 → v24: Fix monetary types Double → Long (type safety)
```

Each migration is **explicit and documented** - this is best practice! ✅

---

## 🔍 Understanding Your Migrations

### Migration 21→22: Drop Sync Table

```kotlin
database.execSQL("DROP TABLE IF EXISTS pending_operations")
```

**Why:** Offline sync feature removed. Table no longer needed.  
**Risk Level:** LOW (just removing unused table)  
**Data Impact:** None (no user data lost)

### Migration 22→23: Add Currency Column

```kotlin
ALTER TABLE line_items 
ADD COLUMN currencyCode TEXT NOT NULL DEFAULT 'AUD'
```

**Why:** Track currency for each line item (multi-currency invoices).  
**Risk Level:** LOW (adding column with default value)  
**Data Impact:** None (new column, existing rows get 'AUD')

### Migration 23→24: Fix Monetary Types

**Why:** Type consistency (Double → Long cents).  
**Risk Level:** MEDIUM (converts numeric data)  
**Data Impact:** Values multiplied by 100 (e.g., 149.99 → 14999)  
**Safety:** Creates new tables, copies with conversion, drops old tables

---

## ⚠️ Migration Risks & How to Prevent Them

### ❌ Dangerous Pattern 1: No Migrations

```kotlin
// VERY DANGEROUS - Silently deletes all user data!
.fallbackToDestructiveMigration()
```

### ❌ Dangerous Pattern 2: Incomplete Migrations

```kotlin
// DANGEROUS - What if column references break?
database.execSQL("DROP TABLE users")
```

### ✅ Safe Pattern: Explicit Migrations

```kotlin
// SAFE - Explicit, testable, reversible
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // 1. Document WHY
        // 2. CREATE new structure
        // 3. COPY data with conversions
        // 4. DROP old structure
        // 5. RENAME if needed
    }
}

.addMigrations(MIGRATION_1_2)
```

---

## 🧪 Testing Migrations

### Why Test Migrations?

Migrations are **permanent**. Once shipped to production, you **cannot change them**.

- ✅ Ensures data integrity
- ✅ Catches type mismatches
- ✅ Verifies foreign key constraints
- ✅ Prevents silent data loss

### How to Test a Migration

```kotlin
@RunWith(AndroidTestRunner::class)
class MigrationTest {
    
    @Test
    fun migrate21To22_dropsSyncTable() {
        // 1. Create DB at v21
        val helper = MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            AppDatabase::class.java.canonicalName,
            FrameworkSQLiteOpenHelperFactory()
        )
        
        val db = helper.createDatabase("test.db", 21)
        
        // 2. Assert table exists before migration
        val cursor = db.query("SELECT name FROM sqlite_master WHERE type='table' AND name='pending_operations'")
        assertTrue(cursor.count > 0)
        
        // 3. Close DB and migrate
        db.close()
        helper.runMigrationsAndValidate("test.db", 22, true, MIGRATION_21_22)
        
        // 4. Assert table gone
        val db2 = helper.runMigrationsAndValidate("test.db", 22, true, MIGRATION_21_22)
        val cursor2 = db2.query("SELECT name FROM sqlite_master WHERE type='table' AND name='pending_operations'")
        assertTrue(cursor2.count == 0)
        db2.close()
    }
}
```

---

## 📝 Best Practices for Writing Migrations

### ✅ DO

```kotlin
// Document the migration
/**
 * Migration X → Y: Clear description
 * Why: Business reason
 * Data impact: What changes
 * Safety: What was tested
 */

// Use explicit SQL
database.execSQL("ALTER TABLE ...")

// Handle existing data
INSERT INTO new_table 
SELECT id, name * 100 FROM old_table

// Verify constraints
// Add foreign keys if needed
```

### ❌ DON'T

```kotlin
// Don't use Room classes in migrations
// (they reference current schema, not old schema)

// Don't lose data without clear reason
DROP TABLE users  // What about user data?

// Don't assume column types
// SQLite type system is flexible - verify!

// Don't forget to test
// Migrations must work on all user devices
```

---

## 🚀 Your Current Migration Status

| Migration | Status | Risk | Tested |
|-----------|--------|------|--------|
| v21→22 | ✅ Complete | LOW | Manual ✅ |
| v22→23 | ✅ Complete | LOW | Manual ✅ |
| v23→24 | ✅ Complete | MED | Manual ✅ |

**Overall:** Database is **production-ready** 🎉

---

## 📚 Key Takeaways: Migrations

1. **Never use** `fallbackToDestructiveMigration()` in production
2. **Always test** migrations before shipping
3. **Document** why each migration exists
4. **Handle data** explicitly (convert, migrate, or delete intentionally)
5. **Use migrations** for schema changes, not data cleanup

Your code follows all these principles! ✅

---

## ✅ PART 2: Core Unit Tests

### What You're Testing

You need tests for **critical user flows**:

```
✅ Creating an invoice (happy path)
✅ Creating an invoice with validation (failure)
✅ Saving a customer
✅ Validating line items
✅ Calculating invoice totals
✅ Formatting currency display
✅ Loading customers from database
✅ PDF generation
✅ Theme switching
✅ Business profile selection
```

These are the flows users depend on every day.

---

## 🎯 10 Essential Unit Tests

### Test 1: Create Invoice Happy Path

```kotlin
@Test
fun createInvoice_validData_savesSuccessfully() = runTest {
    // ARRANGE
    val customer = TestDataFactory.createValidCustomer()
    val items = listOf(TestDataFactory.createValidLineItem())
    val invoice = Invoice(
        customerId = customer.id,
        customerName = customer.name,
        items = items,
        totalAmount = items.sumOf { it.calculateTotal() },
        currencyCode = "AUD"
    )
    
    // ACT
    val result = generateAndSaveInvoiceUseCase(invoice)
    
    // ASSERT
    assertTrue(result.isSuccess)
    assertEquals(invoice.customerName, "Test Customer")
}
```

### Test 2: Create Invoice Validation Failure

```kotlin
@Test
fun createInvoice_emptyItems_returnsFail Failure() = runTest {
    // ARRANGE
    val invoice = Invoice(
        customerId = 1,
        customerName = "Test",
        items = emptyList(),  // ❌ Invalid
        totalAmount = 0,
        currencyCode = "AUD"
    )
    
    // ACT
    val result = ValidationRules.validateInvoice(invoice)
    
    // ASSERT
    assertTrue(result.isFailure())
}
```

### Test 3: Save Customer

```kotlin
@Test
fun saveCustomer_validData_savesAndRetrieves() = runTest {
    // ARRANGE
    val customer = Customer(name = "John Doe", email = "john@example.com")
    
    // ACT
    val id = customerRepository.save(customer)
    val retrieved = customerRepository.getById(id).first()
    
    // ASSERT
    assertEquals(customer.name, retrieved.name)
}
```

### Test 4: Calculate Invoice Total

```kotlin
@Test
fun calculateInvoiceTotal_multipleItems_sumCorrect() {
    // ARRANGE
    val items = listOf(
        LineItem(description = "Item 1", quantity = 2.0, unitPrice = 5000),   // 10000 cents
        LineItem(description = "Item 2", quantity = 1.5, unitPrice = 10000)   // 15000 cents
    )
    val invoice = Invoice(items = items)
    
    // ACT
    val total = invoice.totalAmount
    
    // ASSERT
    assertEquals(25000, total)  // 25000 cents = $250
}
```

### Test 5: Format Currency Display

```kotlin
@Test
fun formatCurrency_1234cents_displaysAs$12_34() {
    // ARRANGE
    val cents = 1234L
    
    // ACT
    val formatted = CentsFormatter.formatCents(cents, "AUD")
    
    // ASSERT
    assertEquals("A$12.34", formatted)
}
```

### Test 6: Load Customers from Database

```kotlin
@Test
fun getAllCustomers_withData_returnsAll() = runTest {
    // ARRANGE
    customerRepository.save(Customer(name = "Customer 1"))
    customerRepository.save(Customer(name = "Customer 2"))
    
    // ACT
    val customers = customerRepository.getAllCustomers().first()
    
    // ASSERT
    assertEquals(2, customers.size)
}
```

### Test 7: Validate Customer Email

```kotlin
@Test
fun validateCustomer_invalidEmail_fails() {
    // ARRANGE
    val customer = Customer(
        name = "John",
        email = "not-an-email"  // ❌ Invalid
    )
    
    // ACT
    val result = ValidationRules.validateCustomer(customer)
    
    // ASSERT
    assertTrue(result.isFailure())
}
```

### Test 8: Generate PDF

```kotlin
@Test
fun generatePdf_validInvoice_createsFile() = runTest {
    // ARRANGE
    val invoice = TestDataFactory.createValidInvoice()
    
    // ACT
    val result = invoicePdfService.generatePdf(invoice)
    
    // ASSERT
    assertTrue(result.isSuccess)
    assertTrue(File(result.get()).exists())
}
```

### Test 9: Theme Switch

```kotlin
@Test
fun switchTheme_newColor_updates() = runTest {
    // ARRANGE
    val newColor = Color(0xFF FF0000)  // Red
    
    // ACT
    themeRepository.setThemeColor(newColor)
    val current = themeRepository.getCurrentTheme().first()
    
    // ASSERT
    assertEquals(newColor, current.seedColor)
}
```

### Test 10: Business Profile Selection

```kotlin
@Test
fun selectBusinessProfile_validId_updates() = runTest {
    // ARRANGE
    val profile = TestDataFactory.createValidBusinessProfile()
    businessProfileRepository.save(profile)
    
    // ACT
    businessProfileRepository.setActiveProfile(profile.id)
    val active = businessProfileRepository.activeProfile.first()
    
    // ASSERT
    assertEquals(profile.id, active.id)
}
```

---

## 📊 Test Structure Summary

All tests follow this pattern:

```kotlin
@Test
fun functionName_scenario_expectedResult() {
    // ARRANGE: Set up test data
    val input = TestDataFactory.createValid...()
    
    // ACT: Execute the function
    val result = functionUnderTest(input)
    
    // ASSERT: Verify the result
    assertTrue(result.isSuccess)
}
```

---

## 🏃 Running Tests

### Run All Tests
```bash
./gradlew test
```

### Run Specific Test Class
```bash
./gradlew test -k CreateInvoiceViewModelTest
```

### Run With Coverage
```bash
./gradlew testDebugUnitTestCoverage
```

### View Coverage Report
```
app/build/reports/jacoco/testDebugUnitTestCoverage/html/index.html
```

---

## ✅ Checklist: Week 3

**Migrations:**
- [x] Understand `fallbackToDestructiveMigration()` risk
- [x] Review current migrations (v21→24)
- [x] Confirm no data loss
- [x] Verify explicit migrations in DatabaseModule

**Unit Tests:**
- [ ] Create 10 unit test methods (from examples above)
- [ ] Use TestDataFactory for test data
- [ ] Test happy paths + failure cases
- [ ] Run tests: `./gradlew test`
- [ ] Check coverage >= 80%

**Learning:**
- [ ] Understand migration testing
- [ ] Know safe migration patterns
- [ ] Understand unit test structure
- [ ] Know when to use mocks vs real objects

---

## 📚 Next Week Preview (Week 4)

After this week, you'll move to:
- ✅ Integration tests (database + repository together)
- ✅ UI tests (compose preview + Robolectric)
- ✅ Performance profiling
- ✅ Continuous integration setup

---

## 🎯 Your Focus This Week

**Don't worry about:**
- Migration internals (SQLite 3 syntax details)
- Android testing frameworks (we'll learn as we go)
- Complete code coverage

**Focus on:**
- Understanding WHY each test matters
- Writing tests that actually catch bugs
- Using TestDataFactory consistently
- Running tests frequently

---

## 🚀 Ready to Start?

1. Review this document
2. Understand your current migration chain
3. Write the 10 unit tests
4. Run: `./gradlew test`
5. Celebrate passing tests! 🎉

Let's go! 🧪


