# 🚀 PHASE 2: OFFLINE SYNC QUEUE - IMPLEMENTATION GUIDE

**Start Date:** March 8, 2026  
**Duration:** 2 weeks  
**Priority:** 🔴 Critical  
**Status:** ⏳ READY TO START  

---

## 📋 PHASE 2 OBJECTIVE

Enable users to create, edit, and delete invoices **without internet**, with automatic synchronization when connection is restored.

---

## 🎯 USER STORIES

### **Story 1: Create Invoice Offline**
```
As a user with no internet
I want to create an invoice
So that I don't lose work when offline

Acceptance Criteria:
✅ Can create invoice without internet
✅ Invoice appears in list immediately
✅ Has "pending sync" indicator
✅ Syncs automatically when online
```

### **Story 2: Edit Invoice Offline**
```
As a user working offline
I want to edit an existing invoice
So that I can update details without waiting for internet

Acceptance Criteria:
✅ Can edit any invoice offline
✅ Changes saved locally
✅ Has "pending sync" indicator
✅ Syncs when internet returns
```

### **Story 3: Record Payment Offline**
```
As a user without internet
I want to record a payment
So that I don't lose payment information

Acceptance Criteria:
✅ Can record payment offline
✅ Shows as pending until synced
✅ Auto-syncs when online
✅ Reflects in analytics after sync
```

### **Story 4: Delete Invoice Offline**
```
As a user working offline
I want to delete an invoice
So that I can keep my data clean

Acceptance Criteria:
✅ Can delete invoice offline
✅ Shows as pending deletion
✅ Actually deletes when online
✅ No conflicts if re-created
```

### **Story 5: Conflict Resolution**
```
As a user who modified an invoice offline and online
I need conflicts handled gracefully
So that I don't lose data

Acceptance Criteria:
✅ Last-write-wins strategy applied
✅ User notified of conflict
✅ Both versions available for review
✅ Can manually select which to keep
```

---

## 🏗️ ARCHITECTURE DESIGN

### **Database Schema**

```
OfflineOperationQueue table:
├── id (PrimaryKey, Long)
├── operationType (String) - CREATE_INVOICE, UPDATE_INVOICE, RECORD_PAYMENT, DELETE_INVOICE
├── entityId (Long) - invoice ID
├── entityData (String/Blob) - serialized Invoice/Payment data
├── timestamp (Long) - when operation was queued
├── status (String) - PENDING, SYNCING, SYNCED, FAILED
├── retryCount (Int) - number of sync attempts
├── errorMessage (String) - last error if any
└── businessProfileId (Long) - which business this belongs to
```

### **Operation Types**

```kotlin
enum class OfflineOperationType {
    CREATE_INVOICE,      // Create new invoice
    UPDATE_INVOICE,      // Update existing invoice
    UPDATE_PAYMENT,      // Record payment
    DELETE_INVOICE,      // Delete invoice
    UPDATE_STATUS        // Change invoice status
}
```

### **Data Flow**

```
User Action (Create/Edit/Delete)
    ↓
Check Internet Connection
    ├─ Online: Save directly to server + local DB
    └─ Offline: Save to local DB + queue operation
    ↓
Operation Queued
├─ OfflineOperationQueue table
├─ Show "pending sync" badge in UI
└─ Keep in local list for immediate display
    ↓
Connection Restored
    ↓
SyncWorker Triggered
├─ Process queue in order
├─ Handle conflicts
├─ Update local DB on success
└─ Retry on failure
    ↓
UI Updates
├─ Remove "pending sync" badge
├─ Refresh analytics
└─ Show success message
```

---

## 📦 IMPLEMENTATION COMPONENTS

### **1. Entity & DAO Layer**

Files to create:
- `OfflineOperation.kt` - Entity class
- `OfflineOperationDao.kt` - Room DAO
- `OfflineOperationRepository.kt` - Data access interface

### **2. Business Logic Layer**

Files to create:
- `OfflineQueueService.kt` - Queue management
- `ConflictResolver.kt` - Handle sync conflicts
- `SyncWorker.kt` - Background sync worker

### **3. UI Layer Updates**

Files to modify:
- `InvoiceDetailScreen.kt` - Add "pending sync" indicator
- `InvoiceListScreen.kt` - Show sync status badges
- `DashboardScreen.kt` - Show offline badge

### **4. Integration**

Files to modify:
- `SaveInvoiceUseCase.kt` - Queue instead of sync if offline
- `RecordPaymentUseCase.kt` - Queue payment if offline
- `DeleteInvoiceUseCase.kt` - Queue deletion if offline

---

## 📅 WEEK-BY-WEEK BREAKDOWN

### **Week 1: Foundation**

#### **Day 1: Database & Entity**
- [ ] Create `OfflineOperation` entity
- [ ] Create `OfflineOperationDao` with queries
- [ ] Add migration to `AppDatabase.kt`
- [ ] Create `OfflineOperationRepository`
- **Deliverable:** Can query and insert operations

#### **Day 2: Queue Service**
- [ ] Create `OfflineQueueService`
- [ ] Implement `queueOperation()` method
- [ ] Add operation serialization/deserialization
- [ ] Create in-memory cache of pending operations
- **Deliverable:** Operations can be queued and retrieved

#### **Day 3-4: UseCase Integration**
- [ ] Update `SaveInvoiceUseCase` to check connectivity
- [ ] If offline, queue instead of sync
- [ ] Update `RecordPaymentUseCase` similarly
- [ ] Update `DeleteInvoiceUseCase` similarly
- **Deliverable:** Offline operations are queued

#### **Day 5: Testing & Verification**
- [ ] Unit tests for `OfflineQueueService`
- [ ] Test offline operation queueing
- [ ] Test operation retrieval
- [ ] Verify database persistence
- **Deliverable:** 20+ unit tests passing

---

### **Week 2: Sync & UI**

#### **Day 6: Sync Worker**
- [ ] Create `SyncWorker` (WorkManager)
- [ ] Implement operation processing loop
- [ ] Add retry logic (exponential backoff)
- [ ] Handle network failures gracefully
- **Deliverable:** Worker can process queued operations

#### **Day 7: Conflict Resolution**
- [ ] Create `ConflictResolver` service
- [ ] Implement last-write-wins strategy
- [ ] Add conflict detection
- [ ] Log conflicts for user review
- **Deliverable:** Conflicts detected and logged

#### **Day 8: UI Indicators**
- [ ] Add "pending sync" badge to invoice items
- [ ] Add "offline mode" indicator to dashboard
- [ ] Show sync status in details screen
- [ ] Add sync progress indicator
- **Deliverable:** Users see sync status visually

#### **Day 9: Status Updates**
- [ ] Update UI when sync completes
- [ ] Refresh analytics after sync
- [ ] Remove pending badges on success
- [ ] Show errors clearly
- **Deliverable:** UI updates reflect sync state

#### **Day 10: End-to-End Testing**
- [ ] Manual test: offline create → sync
- [ ] Manual test: offline edit → sync
- [ ] Manual test: offline delete → sync
- [ ] Test conflict scenarios
- [ ] Verify no data loss
- **Deliverable:** 5+ E2E test scenarios passing

---

## ✅ SUCCESS CRITERIA

By end of Phase 2:

```
Functionality:
[✅] User can create invoice offline
[✅] User can edit invoice offline
[✅] User can record payment offline
[✅] User can delete invoice offline
[✅] Automatic sync when online
[✅] Conflicts handled gracefully
[✅] No data loss

UI/UX:
[✅] "Offline" badge visible
[✅] "Pending sync" indicators on items
[✅] Clear status messages
[✅] Progress shown during sync

Code Quality:
[✅] 80%+ test coverage
[✅] No regressions in existing tests
[✅] Clean architecture maintained
[✅] Documented implementation

Performance:
[✅] Offline operations instant
[✅] Sync completes in <10 seconds
[✅] No main thread blocking
[✅] Memory usage reasonable
```

---

## 🛠️ TECHNICAL DECISIONS

### **Conflict Resolution Strategy: Last-Write-Wins**

Why this approach:
- Simple to implement
- Predictable behavior
- Fast sync process
- Works for most use cases (invoices rarely edited simultaneously)

Alternative: Operational Transform (more complex, skip for Phase 2)

### **Serialization: JSON (Gson/Moshi)**

Why JSON:
- Easy to serialize Invoice objects
- Human-readable for debugging
- Smaller than Parcelable
- Firebase-friendly

### **Sync Trigger: WorkManager**

Why WorkManager:
- Built-in retry logic
- Battery efficient
- Respects Doze mode
- Works across Android versions

### **Offline Detection: ConnectivityManager**

Why ConnectivityManager:
- Standard Android approach
- Works reliably
- Can detect WiFi vs mobile
- Allows for smart sync timing

---

## 📚 DEPENDENCIES & LIBRARIES

No new external dependencies needed! Use existing:
- ✅ Room (database)
- ✅ WorkManager (background sync)
- ✅ Hilt (dependency injection)
- ✅ Kotlin serialization (JSON)
- ✅ Timber (logging)

---

## 🔗 REFERENCE FILES

From Phase 1 that you'll build on:
- `SaveInvoiceUseCase.kt` - Already has snapshot creation
- `InvoiceEntity.kt` - Entity model
- `InvoiceDao.kt` - DAO pattern to follow
- `AppDatabase.kt` - Where to register new DAO

---

## 🎯 STARTING TOMORROW (March 8)

### **Day 1 Tasks:**

1. Create `OfflineOperation.kt` entity file
2. Create `OfflineOperationDao.kt` with CRUD operations
3. Add database migration
4. Verify compilation and tests pass
5. Commit to GitHub

**Estimated Time:** 2-3 hours

---

## 📞 NEED HELP?

Reference these docs as you build:
- `PHASE_2_TO_12_DEVELOPMENT_ROADMAP.md` - High-level overview
- `PHASE_1_COMPLETION_REPORT.md` - How Phase 1 was done
- Android WorkManager docs - For SyncWorker
- Room database docs - For new DAO

---

## 🚀 LET'S BUILD PHASE 2!

You've got:
✅ Solid foundation from Phase 1
✅ Clear requirements above
✅ Proven build/test process
✅ 2 weeks to implement

**Time to make offline work flawlessly!** 💪

---

**Phase 2 Status:** Ready to begin  
**Start Date:** March 8, 2026  
**Expected Completion:** March 21, 2026  
**Difficulty:** Medium (new concepts but straightforward)  
**Confidence:** High (all patterns established in Phase 1)


