# 🔧 WEEK 2 SYNCWORKER ARCHITECTURE PREVIEW

**Purpose:** Design document for SyncWorker implementation (Days 6-10)  
**Status:** Ready to implement on Day 6  
**Prerequisite:** Day 5 testing confirms offline system works  

---

## 🎯 SYNCWORKER MISSION

Transform the offline queue into a **fully synchronized system** where:
- Queued operations are processed when online
- Conflicts are detected and resolved
- Failures trigger retries
- Data eventually reaches the "server"
- Users see clear sync status

---

## 🏗️ ARCHITECTURE OVERVIEW

```
┌─────────────────────────────┐
│ Offline Queue System        │
│ (Days 1-5 Complete)         │
├─────────────────────────────┤
│ - OfflineOperation entity    │
│ - OfflineQueueService       │
│ - All operations queued      │
│ - All data persisted        │
└────────────┬────────────────┘
             │
             ▼
┌─────────────────────────────┐
│ SyncWorker (NEW - Days 6-10)│
├─────────────────────────────┤
│ - Process queue operations  │
│ - Detect conflicts          │
│ - Resolve conflicts         │
│ - Retry on failure          │
│ - Update operation status   │
└────────────┬────────────────┘
             │
             ▼
┌─────────────────────────────┐
│ UI Updates                  │
├─────────────────────────────┤
│ - Remove pending badges     │
│ - Show completion           │
│ - Display errors if any     │
│ - Refresh data              │
└─────────────────────────────┘
```

---

## 📋 SYNCWORKER COMPONENTS

### **1. WorkManager Integration**

```kotlin
class SnapshotRepairWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return try {
            // Process operations from queue
            processQueuedOperations()
            Result.success()
        } catch (e: Exception) {
            // Retry with exponential backoff
            Result.retry()
        }
    }
}
```

**Why WorkManager:**
- ✅ Respects battery optimization
- ✅ Handles app termination gracefully
- ✅ Built-in retry logic
- ✅ Persistent across device reboots
- ✅ Queue management

### **2. Operation Processing Loop**

```
Get Next Pending Operation
    ↓
Mark as SYNCING
    ↓
Attempt Network Call
    ├─ Success → Mark as SYNCED
    ├─ Conflict → Resolve conflict
    ├─ Failure → Mark as FAILED (retry count++)
    └─ Network Error → Retry
    ↓
Move to Next Operation
```

### **3. Conflict Detection**

```kotlin
enum class ConflictType {
    INVOICE_MODIFIED_ONLINE_AND_OFFLINE,      // Both changed
    PAYMENT_RECORDED_TWICE,                   // Duplicate
    INVOICE_DELETED_WHILE_OFFLINE,            // Create then delete
    STATUS_CHANGED_BOTH_WAYS,                 // Both changed status
    AMOUNT_CHANGED_BOTH_WAYS                  // Both changed amount
}
```

**Strategy: Last-Write-Wins**
- Online version timestamp vs Offline operation timestamp
- Whoever is newer wins
- User notified of conflict
- Manual override available

### **4. Retry Logic**

```
Attempt 1: Immediate retry
Attempt 2: 30 seconds later
Attempt 3: 2 minutes later
Attempt 4: 10 minutes later
Attempt 5: 1 hour later
Attempt 6+: Mark as FAILED, wait for user

Max attempts: 6
Time window: 24 hours
Then: Alert user of sync failure
```

### **5. Status Updates**

```
PENDING    → In queue, not synced yet
    ↓
SYNCING    → Currently being synced
    ├─ Success → SYNCED ✅
    ├─ Conflict → Show UI overlay
    ├─ Failure → FAILED ❌
    └─ Retry → Back to SYNCING
```

---

## 🔄 SYNC WORKFLOW (High Level)

### **Day 1 (User offline, creates invoice)**
```
User Action: Create invoice
System: Save to local DB + Queue operation
Status: PENDING
UI: Show "⏳ Pending Sync" badge
```

### **Day 2 (User comes online)**
```
System: Detect online status
Worker: Start processing queue
Operation 1: PENDING → SYNCING → SYNCED
```

### **Day 3 (Conflict detected)**
```
System: Detect conflict (same invoice modified both ways)
Worker: Apply last-write-wins
UI: Show conflict dialog (optional)
Resolution: Online version wins (or configurable)
```

### **Day 4 (All synced)**
```
Status: All operations SYNCED
Queue: Empty
UI: All badges removed
Data: Consistent across offline + online
```

---

## 📊 DATABASE CHANGES FOR SYNCWORKER

**New Table: sync_log** (optional, for audit trail)

```sql
CREATE TABLE sync_log (
    id INTEGER PRIMARY KEY,
    operation_id INTEGER,
    sync_timestamp LONG,
    success BOOLEAN,
    error_message TEXT,
    conflict_detected BOOLEAN
)
```

**Update OfflineOperation Table:**
```sql
-- Already has:
-- id, operation_type, entity_id, entity_data,
-- business_profile_id, timestamp_ms, status, retry_count

-- Will use:
-- status = PENDING|SYNCING|SYNCED|FAILED
-- retry_count = for backoff calculation
```

---

## 🧪 SYNCWORKER TEST SCENARIOS

### **Test 1: Simple Sync**
```
Queue: 1 invoice creation
Action: Go online
Expected: Operation synced successfully
Verification: Status = SYNCED, badge removed
```

### **Test 2: Rapid Sync**
```
Queue: 10 operations
Action: Go online
Expected: All synced in order
Verification: All SYNCED, queue empty
```

### **Test 3: Conflict Handling**
```
Setup: 
  - Offline: Change invoice amount to $500
  - Online: Change same invoice amount to $600
Action: Sync
Expected: Last-write-wins applies
Verification: Final amount is whichever is "newer"
```

### **Test 4: Network Failure**
```
Setup: Network fails during sync
Action: Try to sync 3 times
Expected: Retries with backoff
Verification: Retry count increments, eventual success
```

### **Test 5: Deleted While Offline**
```
Setup: Invoice created offline, then deleted offline
Action: Go online
Expected: Delete operation syncs (not create)
Verification: Invoice never created on server
```

---

## 🛠️ IMPLEMENTATION CHECKLIST (Days 6-10)

### **Day 6: Core WorkManager Setup**
- [ ] Create SnapshotRepairWorker class
- [ ] Implement doWork() method
- [ ] Wire to OfflineQueueService
- [ ] Test basic processing
- [ ] Tests passing

### **Day 7: Operation Processing**
- [ ] Create SyncProcessor service
- [ ] Implement operation loop
- [ ] Handle success/failure/retry
- [ ] Update operation status
- [ ] Tests passing

### **Day 8: Conflict Resolution**
- [ ] Detect conflicts
- [ ] Implement last-write-wins
- [ ] User notification system
- [ ] Manual override capability
- [ ] Tests passing

### **Day 9: Retry Logic & Edge Cases**
- [ ] Exponential backoff
- [ ] Max retry attempts
- [ ] Network error handling
- [ ] Edge cases (deleted while offline, etc.)
- [ ] Tests passing

### **Day 10: Integration & Final Testing**
- [ ] End-to-end sync testing
- [ ] Verify consistency
- [ ] Performance optimization
- [ ] Documentation
- [ ] Phase 2 Complete (100%)

---

## 📚 RESOURCES YOU'LL NEED

### **Android Documentation**
- WorkManager guide
- PeriodicWorkRequest
- BackoffPolicy
- Constraints

### **Your Code**
- OfflineQueueService (Day 2)
- OperationSerializer (Day 2)
- All UseCase implementations (Days 3-4)
- Repository interfaces (existing)

### **Design Patterns**
- Observer pattern (for status updates)
- Strategy pattern (for conflict resolution)
- Retry pattern (exponential backoff)

---

## 🚀 WHY THIS DESIGN

**Simple:** Straightforward operation-by-operation processing

**Reliable:** WorkManager handles crashes and reboots

**Recoverable:** Retry mechanism handles transient failures

**Detectable:** Conflicts are caught and resolved

**Testable:** Each component independently testable

**Transparent:** User sees sync progress

---

## 🎯 SUCCESS CRITERIA FOR WEEK 2

By end of Day 10:

```
Functionality:
[✅] SyncWorker processes queue
[✅] Operations sync successfully
[✅] Conflicts detected and resolved
[✅] Retries work correctly
[✅] Status updates properly

Testing:
[✅] All 5+ test scenarios pass
[✅] No data corruption
[✅] Consistent sync results
[✅] Edge cases handled

Code Quality:
[✅] Clean architecture maintained
[✅] Tests passing (300+ expected)
[✅] No regressions
[✅] Documented

Status:
[✅] Phase 2 Complete (100%)
[✅] Ready for Phases 3-12
[✅] Ready for production
```

---

## 🎓 KEY INSIGHTS FOR WEEK 2

**The hard part is done:** Days 1-5 built the complete offline system

**Week 2 is about sync:** Make that offline data reach the "server"

**Pattern is proven:** Same quality as Days 1-5

**Timeline is reasonable:** 5 days for SyncWorker is plenty

**Testing is built in:** E2E scenarios from Day 5 apply to Week 2

---

**Ready to implement Week 2?**

**Day 6 starts fresh with SyncWorker.**

**By March 21, Phase 2 is complete.**


