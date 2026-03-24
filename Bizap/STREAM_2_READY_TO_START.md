# 🎯 STREAM 2: INTEGRATION TESTS — IMPLEMENTATION PLAN

**Status:** Ready to Start  
**Dependency:** Stream 1 (COMPLETE ✅)  
**Duration:** 1 day  
**Effort:** 8-10 person-hours  

---

## OVERVIEW

Stream 2 focuses on integration tests that verify:
1. **GUI Switching** - Invoice/customer data syncs across GUI1↔GUI2
2. **Cross-GUI Data Sync** - Changes in one GUI visible in the other
3. **Navigation** - Deep linking and back button flows work

---

## FILES TO CREATE

### 1. GuiSwitchingTest.kt
**Path:** `app/src/test/java/com/emul8r/bizap/ui/integration/GuiSwitchingTest.kt`

**Tests to implement:**
```
✓ testInvoiceVisibleAcrossGuiSwitch()
  - Create invoice in GUI1
  - Switch to GUI2
  - Verify invoice appears

✓ testPaymentRecordedAcrossGui()
  - Create invoice
  - Record payment in GUI2
  - Verify in GUI1

✓ testStatusChangePropagatesToOtherGui()
  - Create invoice
  - Change status in GUI2
  - Verify in GUI1
```

### 2. CrossGUIDataSyncTest.kt
**Path:** `app/src/test/java/com/emul8r/bizap/ui/integration/CrossGUIDataSyncTest.kt`

**Tests to implement:**
```
✓ testCustomerCreatedInGui1VisibleInGui2()
✓ testCustomerEditPropagatesToSnapshot()
✓ testDeleteCustomerCleansUpSnapshots()
```

### 3. Enhance NavigationIntegrationTest.kt
**Path:** `app/src/test/java/com/emul8r/bizap/ui/gui2/integration/NavigationIntegrationTest.kt`

**Tests to add:**
```
✓ testGuiSwitchingNavigation()
✓ testDeepLinkingPreservesData()
✓ testBackNavigationDoesntLoseData()
```

---

## KEY PATTERNS

### Database Verification
```kotlin
@HiltAndroidTest
class GuiSwitchingTest {
    @Inject lateinit var invoiceRepository: InvoiceRepository
    @Inject lateinit var database: AppDatabase
    
    @Test
    fun testInvoiceVisibleAcrossGui() = runTest {
        // Create via repository (GUI1 path)
        val result = invoiceRepository.saveInvoice(invoice)
        val invoiceId = result.getOrNull()!!
        
        // Verify via database (GUI2 path)
        val retrieved = database.invoiceDao().getInvoiceWithItems(invoiceId)
        assertNotNull(retrieved)
    }
}
```

### Snapshot Verification
```kotlin
@Test
fun testPaymentSnapshot() = runTest {
    // Record payment
    invoiceRepository.updateAmountPaid(invoiceId, 5000)
    
    // Verify snapshot updated
    val snapshot = database.invoicePaymentDao().getSnapshotByInvoiceId(invoiceId)
    assertEquals(5000, snapshot?.paidAmount)
}
```

---

## SETUP REQUIREMENTS

### Annotations & Rules
```kotlin
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class GuiSwitchingTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)
    
    @Before
    fun setup() {
        hiltRule.inject()
    }
}
```

### Dependencies to Inject
```kotlin
@Inject lateinit var invoiceRepository: InvoiceRepository
@Inject lateinit var customerRepository: CustomerRepository
@Inject lateinit var database: AppDatabase
```

---

## TEST DATA HELPERS

Create test helper functions:
```kotlin
private suspend fun createTestInvoice(
    totalAmount: Long,
    invoiceNumber: String = "INV-${System.currentTimeMillis()}"
): Long {
    val invoice = Invoice(
        // ... populate with test data
    )
    return invoiceRepository.saveInvoice(invoice).getOrNull()!!
}

private suspend fun createTestCustomer(
    name: String = "Test Customer"
): Long {
    val customer = Customer(
        // ... populate with test data
    )
    return customerRepository.saveCustomer(customer).getOrNull()!!
}
```

---

## SUCCESS CRITERIA

✅ All 8+ integration tests passing  
✅ GUI switching verified (no data loss)  
✅ Cross-GUI sync confirmed  
✅ Navigation flows tested  
✅ No flaky tests  
✅ <5 minute test execution  

---

## INTEGRATION WITH STREAM 1

Stream 1 data is used:
- Invoice data flows through repositories
- Payment snapshots synced automatically
- UI state properly manages data visibility

---

## NEXT STEPS

1. Create GuiSwitchingTest.kt
2. Create CrossGUIDataSyncTest.kt
3. Enhance NavigationIntegrationTest.kt
4. Run all integration tests
5. Verify no regressions with existing tests
6. Code review
7. Merge to main

---

**Ready to proceed with Stream 2?**

Start with creating the three test files using the patterns documented here.


