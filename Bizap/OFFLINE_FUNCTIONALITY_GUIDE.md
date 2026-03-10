# 📱 Bizap Offline Functionality Guide

## Overview

Bizap is designed as an **offline-first application**, meaning it works seamlessly whether you have an internet connection or not. All your data is stored locally on your device, and changes automatically sync to the cloud when you're back online.

---

## ✨ Key Features

### 1. **Works Completely Offline**
- Create, edit, and delete invoices without internet
- Manage customers (create, update, delete)
- Record payments against invoices
- View all existing data (invoices, customers, analytics)
- All operations saved locally and queued for sync

### 2. **Automatic Sync**
- When internet is restored, changes sync automatically
- Smart retry mechanism (up to 5 attempts with exponential backoff)
- Background sync using WorkManager (battery-efficient)
- No manual intervention required

### 3. **Visual Status Indicators**
- **Offline Banner**: Shows "You are currently offline" (red banner)
- **Syncing Banner**: Shows "X changes syncing..." (yellow/tertiary banner)
- **Synced Banner**: Shows "All changes synced" (green banner with checkmark)
- Appears at top of screens automatically

### 4. **Conflict Resolution**
- If data changed on another device: **Server version wins**
- Your local changes preserved in offline queue
- Automatic retry after conflict resolution
- No data loss - conflicts logged for review

---

## 🏗️ Architecture Components

### Core Services

#### **OfflineQueueService**
**Location:** `app/src/main/java/com/emul8r/bizap/data/local/offline/OfflineQueueService.kt`

Central service managing all offline operations:
- **In-memory cache** for fast access
- **Thread-safe** with Mutex locks
- **StateFlow** for reactive UI updates
- **Persistent storage** in Room database

**Supported Operations:**
```kotlin
queueCreateInvoice(invoice)      // Queue new invoice creation
queueUpdateInvoice(invoice)      // Queue invoice updates
queueDeleteInvoice(invoiceId)    // Queue invoice deletion
queueCreateCustomer(customer)    // Queue customer creation
queueUpdateCustomer(customer)    // Queue customer updates
queueDeleteCustomer(customerId)  // Queue customer deletion
queueRecordPayment(...)          // Queue payment recording
queueStatusUpdate(...)           // Queue status changes
```

#### **NetworkMonitor**
**Location:** `app/src/main/java/com/emul8r/bizap/data/network/NetworkMonitor.kt`

Real-time network connectivity detection:
- **Flow-based reactive API**: `isOnline: Flow<Boolean>`
- Monitors WiFi, Cellular, and Ethernet
- Detects network availability changes instantly
- Triggers sync automatically when online

#### **SyncWorker**
**Location:** `app/src/main/java/com/emul8r/bizap/data/worker/SyncWorker.kt`

Background worker that processes the offline queue:
- **Triggered by**: Network connectivity changes, manual sync requests
- **Network constraint**: Only runs when connected
- **Retry policy**: Exponential backoff (5 attempts max)
- **Battery efficient**: Uses WorkManager best practices

#### **SnapshotCachePolicy**
**Location:** `app/src/main/java/com/emul8r/bizap/data/repository/SnapshotCachePolicy.kt`

Defines caching strategy for analytics:
- **Write-through cache**: Updates on every invoice operation
- **24-hour staleness tolerance**: Repairs automatically
- **NOT source of truth**: All calculations use Invoice table directly
- **Dashboard queries**: Use V2 repositories (not snapshots)

---

## 🔄 How It Works

### When You Go Offline

```
1. User performs action (create invoice, record payment, etc.)
   ↓
2. App detects no network via NetworkMonitor
   ↓
3. Operation saved to local Room database
   ↓
4. Operation added to offline queue (pending status)
   ↓
5. UI updated immediately (local data)
   ↓
6. Sync banner shows "X changes pending"
```

### When Network Returns

```
1. NetworkMonitor detects connectivity restored
   ↓
2. SyncWorker triggered automatically
   ↓
3. Retrieves pending operations (FIFO order)
   ↓
4. For each operation:
   - Mark as "SYNCING"
   - Send to server API
   - Handle response (success/retry/fail)
   ↓
5. On success:
   - Mark as "SYNCED"
   - Remove from queue
   - Update UI
   ↓
6. On failure:
   - Retry with exponential backoff
   - After 5 attempts: mark as "FAILED"
   - Show error in UI
```

### Conflict Resolution

```
1. Operation sent to server
   ↓
2. Server returns 409 Conflict (data changed elsewhere)
   ↓
3. App fetches latest data from server
   ↓
4. Local data overwritten with server version
   ↓
5. Conflict logged for review
   ↓
6. Retry original operation (may succeed or fail)
```

**Strategy**: "Server Wins" - Ensures data consistency across devices

---

## 📊 Data Flow Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                    USER ACTION (Offline)                     │
└───────────────────┬─────────────────────────────────────────┘
                    │
        ┌───────────▼──────────────┐
        │  OfflineQueueService     │
        │  - Save to Room DB       │
        │  - Add to pending queue  │
        │  - Update QueueState     │
        └───────────┬──────────────┘
                    │
        ┌───────────▼──────────────┐
        │  UI Updates              │
        │  - Show sync banner      │
        │  - Display pending count │
        └───────────┬──────────────┘
                    │
        ┌───────────▼──────────────┐
        │  Network Restored        │
        │  (NetworkMonitor)        │
        └───────────┬──────────────┘
                    │
        ┌───────────▼──────────────────────┐
        │  SyncWorker (WorkManager)        │
        │  - Constraint: NetworkType.CONNECTED │
        │  - Backoff: Exponential          │
        │  - Max retries: 5                │
        └───────────┬──────────────────────┘
                    │
        ┌───────────▼──────────────────────┐
        │  SyncPendingOperationsUseCase    │
        │  - Get pending (FIFO)            │
        │  - Dispatch via SyncOperationDispatcher │
        └───────────┬──────────────────────┘
                    │
        ┌───────────▼──────────────────────┐
        │  Repository Layer                │
        │  - invoiceRepository.createInvoiceRemote() │
        │  - customerRepository.createCustomerRemote() │
        │  - etc.                          │
        └───────────┬──────────────────────┘
                    │
        ┌───────────▼──────────────────────┐
        │  Server API                      │
        │  - Success: mark SYNCED          │
        │  - 409 Conflict: resolve & retry │
        │  - Error: retry or mark FAILED   │
        └──────────────────────────────────┘
```

---

## 🎯 Cache Architecture

### Three Snapshot Tables

1. **InvoiceAnalyticsSnapshot**
   - Financial data (totalAmount, amountPaid, outstanding)
   - Invoice status and dates
   - Updated on every invoice operation

2. **DailyRevenueSnapshot**
   - Daily revenue aggregates
   - Grouped by date and currency
   - Used for revenue dashboard

3. **InvoicePaymentSnapshot**
   - Payment status and aging
   - Risk scores and buckets
   - Used for payment analytics

### Cache Policy Rules

✅ **DO:**
- Use snapshots for dashboard performance (read-optimized)
- Update snapshots on every invoice write (write-through)
- Tolerate up to 24 hours staleness
- Use V2 repositories for all dashboard queries

❌ **DON'T:**
- Use snapshots as source of truth for financial calculations
- Block main operations if snapshot update fails
- Query snapshots directly (use repositories)

### Snapshot Maintenance

**SnapshotRepairWorker** (Background maintenance):
- Runs every 24 hours
- Verifies all invoices have snapshots
- Rebuilds missing/inconsistent snapshots
- Self-healing for edge cases

**SnapshotSyncRetryWorker** (Retry failed syncs):
- Runs every 15 minutes
- Retries failed snapshot sync operations
- Uses SnapshotRetryQueue

---

## 🔧 Technical Details

### Database Schema

#### `offline_operations` Table
```sql
CREATE TABLE offline_operations (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    operationType TEXT NOT NULL,        -- CREATE_INVOICE, UPDATE_INVOICE, etc.
    entityId INTEGER NOT NULL,
    entityData TEXT NOT NULL,           -- JSON serialized entity
    businessProfileId INTEGER NOT NULL,
    timestampMs INTEGER NOT NULL,
    status TEXT NOT NULL,               -- PENDING, SYNCING, SYNCED, FAILED
    retryCount INTEGER DEFAULT 0,
    errorMessage TEXT
);

CREATE INDEX idx_offline_operations_status ON offline_operations(status);
CREATE INDEX idx_offline_operations_business ON offline_operations(businessProfileId);
```

### Retry Strategy

**Exponential Backoff with Jitter:**
```kotlin
waitTime = baseDelay * (multiplier ^ attemptNumber)
jitteredWait = waitTime * (0.5 to 1.5)
```

**Example Retry Timeline:**
- Attempt 1: Fail → Wait 1-1.5 seconds
- Attempt 2: Fail → Wait 2-3 seconds
- Attempt 3: Fail → Wait 4-6 seconds
- Attempt 4: Fail → Wait 8-12 seconds
- Attempt 5: Fail → Mark as FAILED

**Why Jitter?** Prevents "thundering herd" problem where all clients retry simultaneously.

### Error Classification

**Retryable Errors** (will retry):
- Network timeout
- 500 Internal Server Error
- 502 Bad Gateway
- 503 Service Unavailable
- Connection refused
- Unknown errors (conservative approach)

**Non-Retryable Errors** (marked as FAILED):
- 401 Unauthorized (auth issue)
- 403 Forbidden (permission issue)
- 404 Not Found (entity doesn't exist)
- 400 Bad Request (validation error)

---

## 👨‍💻 Developer Guide

### Using OfflineQueueService

```kotlin
@Inject lateinit var offlineQueueService: OfflineQueueService

// Initialize on app start or business selection
offlineQueueService.initialize(businessId)

// Queue an invoice creation
val invoice = Invoice(...)
val operationId = offlineQueueService.queueCreateInvoice(invoice)

// Observe queue state
offlineQueueService.queueState.collect { state ->
    println("Pending: ${state.totalPending}")
    println("Failed: ${state.failedCount}")
    println("Syncing: ${state.isSyncing}")
}
```

### Using NetworkMonitor

```kotlin
@Inject lateinit var networkMonitor: NetworkMonitor

// Observe network status
networkMonitor.isOnline.collect { online ->
    if (online) {
        println("✅ Network available")
    } else {
        println("❌ Network unavailable")
    }
}
```

### Triggering Manual Sync

```kotlin
// From anywhere with Context
SyncWorker.enqueueOneShot(context)
```

### Adding Sync Status UI

```kotlin
@Composable
fun MyScreen() {
    Column {
        // Add sync status indicator at top
        SyncStatusIndicator()
        
        // Rest of your screen content
        // ...
    }
}
```

---

## 🧪 Testing Offline Functionality

### Manual Testing Scenarios

#### Scenario 1: Create Invoice Offline
1. Turn off WiFi/Cellular on device
2. Create a new invoice in the app
3. Verify invoice appears in local list
4. Verify sync banner shows "1 change syncing..."
5. Turn on WiFi/Cellular
6. Wait for automatic sync
7. Verify sync banner shows "All changes synced"
8. Check server to confirm invoice created

#### Scenario 2: Edit Invoice While Offline
1. Turn off network
2. Edit an existing invoice (change amount, status, etc.)
3. Save changes
4. Verify changes appear locally
5. Restore network
6. Verify automatic sync occurs
7. Confirm changes on server

#### Scenario 3: Multiple Operations Offline
1. Turn off network
2. Create 2 invoices
3. Edit 1 invoice
4. Record 1 payment
5. Verify sync banner shows "4 changes syncing..."
6. Restore network
7. Verify all 4 operations sync successfully

#### Scenario 4: Conflict Resolution
1. Edit invoice on Device A (offline)
2. Edit same invoice on Device B (online)
3. Device B's change syncs to server
4. Restore network on Device A
5. Device A detects conflict (409)
6. Device A fetches latest from server
7. Device A's local data updated with server version
8. Verify no data corruption

#### Scenario 5: Failed Sync Retry
1. Turn off network
2. Create invoice
3. Turn on network but block app's internet access (firewall)
4. Verify retry attempts (5 times with backoff)
5. Verify operation marked as FAILED after 5 attempts
6. Unblock internet access
7. Manually trigger sync (or wait for next automatic attempt)
8. Verify failed operation retries and succeeds

### Automated Testing

**Unit Tests:**
- `OfflineQueueServiceTest.kt` - Queue operations
- `SyncPendingOperationsUseCaseTest.kt` - Sync logic
- `NetworkMonitorTest.kt` - Connectivity detection

**Integration Tests:**
- `OfflineFlowIntegrationTest.kt` - End-to-end offline flow

**UI Tests:**
- `SyncStatusIndicatorTest.kt` - UI component behavior

---

## 📱 User Experience

### What Users See

#### Online with No Pending Changes
- ✅ No banner displayed (or brief "All changes synced" message)
- App works normally

#### Offline
- 🔴 Red banner: "You are currently offline"
- All operations still work (saved locally)
- Changes queued for sync

#### Syncing Changes
- 🟡 Yellow banner: "3 changes syncing..."
- Progress indicator spinning
- Operations being sent to server

#### Sync Complete
- ✅ Green banner (brief): "All changes synced"
- Then banner disappears

#### Sync Failed
- 🔴 Red banner: "Failed to sync. Will retry automatically"
- User can continue working
- App retries in background

---

## 🔒 Data Integrity Guarantees

### ACID Properties

✅ **Atomicity**: Each operation is atomic (Room transactions)
✅ **Consistency**: Validation before queue insertion
✅ **Isolation**: Mutex locks prevent race conditions
✅ **Durability**: Room database persists to disk

### Consistency Checks

**DataConsistencyValidator** verifies:
- Snapshot totals match calculated totals (within 1¢)
- All invoices have corresponding snapshots
- No orphaned snapshot records

**Tolerance**: 1¢ difference allowed (floating-point rounding)

**Repair**: SnapshotRepairWorker runs daily to fix inconsistencies

---

## 🎛️ Configuration

### WorkManager Policies

**SyncWorker:**
- **Constraint**: NetworkType.CONNECTED
- **Backoff**: Exponential
- **Max Attempts**: 5
- **Tag**: "offline_sync"

**SnapshotRepairWorker:**
- **Period**: 24 hours
- **Constraint**: Device idle + charging (optional)
- **Tag**: "snapshot_repair"

**SnapshotSyncRetryWorker:**
- **Period**: 15 minutes
- **Constraint**: NetworkType.CONNECTED
- **Tag**: "snapshot_retry"

### Snapshot Cache Policy

```kotlin
object SnapshotCachePolicy {
    const val MAX_SNAPSHOT_AGE_MS = 24 * 60 * 60 * 1000L  // 24 hours
    const val CONSISTENCY_TOLERANCE_CENTS = 1L             // 1¢
    const val USE_SNAPSHOTS_FOR_DASHBOARDS = false         // ALWAYS false
}
```

---

## 🐛 Troubleshooting

### Problem: Changes Not Syncing

**Possible Causes:**
1. Network not actually available (check device settings)
2. WorkManager constraints not met
3. All 5 retry attempts failed
4. App killed before sync completed

**Solutions:**
1. Verify network connectivity
2. Check WorkManager logs: `adb shell dumpsys jobscheduler`
3. Manually trigger sync: `SyncWorker.enqueueOneShot(context)`
4. Check failed operations count in database

### Problem: Sync Banner Stuck on "Syncing..."

**Possible Causes:**
1. Network extremely slow
2. Server not responding
3. Operation failed but status not updated

**Solutions:**
1. Wait for timeout (30 seconds per operation)
2. Check server health
3. Force sync restart: kill app and reopen
4. Check logs for error messages

### Problem: Data Mismatch After Sync

**Possible Causes:**
1. Conflict resolution occurred (server won)
2. Snapshot stale (hasn't repaired yet)
3. Timing issue (dashboard cached old data)

**Solutions:**
1. Review conflict logs (Timber logs)
2. Trigger snapshot repair: `SnapshotRepairWorker.triggerImmediateRepair()`
3. Pull to refresh dashboard
4. Verify source of truth (Invoice table) matches server

---

## 📚 Related Documentation

- **[SnapshotCachePolicy.kt](app/src/main/java/com/emul8r/bizap/data/repository/SnapshotCachePolicy.kt)** - Cache policy rules
- **[OfflineQueueService.kt](app/src/main/java/com/emul8r/bizap/data/local/offline/OfflineQueueService.kt)** - Queue service implementation
- **[SyncWorker.kt](app/src/main/java/com/emul8r/bizap/data/worker/SyncWorker.kt)** - Background sync worker
- **[NetworkMonitor.kt](app/src/main/java/com/emul8r/bizap/data/network/NetworkMonitor.kt)** - Connectivity monitoring

---

## 🎓 Best Practices

### For Developers

1. **Always check network before API calls**
   ```kotlin
   val isOnline = ConnectivityHelper.isNetworkAvailable(context)
   if (!isOnline) {
       // Queue operation instead
       offlineQueueService.queueCreateInvoice(invoice)
   } else {
       // Call API directly
       invoiceRepository.createInvoiceRemote(invoice)
   }
   ```

2. **Use NetworkMonitor for reactive UI**
   ```kotlin
   val isOnline by networkMonitor.isOnline.collectAsStateWithLifecycle()
   
   Button(
       onClick = { /* ... */ },
       enabled = isOnline || allowOffline
   ) {
       Text(if (isOnline) "Save to Cloud" else "Save Locally")
   }
   ```

3. **Add SyncStatusIndicator to all major screens**
   ```kotlin
   @Composable
   fun InvoiceListScreen() {
       Column {
           SyncStatusIndicator()  // Always at top
           // ... rest of screen
       }
   }
   ```

4. **Handle offline gracefully in ViewModels**
   ```kotlin
   viewModelScope.launch {
       if (networkMonitor.isOnline.first()) {
           // Online: sync immediately
           invoiceRepository.createInvoiceRemote(invoice)
       } else {
           // Offline: queue for later
           offlineQueueService.queueCreateInvoice(invoice)
       }
       _uiState.update { it.copy(saveSuccess = true) }
   }
   ```

### For Users

1. **Trust the offline mode** - All your data is safe locally
2. **Don't worry about sync** - It happens automatically
3. **Check sync banner** - Shows real-time sync status
4. **Failed operations are retried** - No manual intervention needed
5. **Server version wins conflicts** - Ensures consistency across devices

---

## 📊 Metrics & Monitoring

### Key Metrics to Track

1. **Pending Operations Count**: `offlineQueueService.queueState.totalPending`
2. **Failed Operations Count**: `offlineQueueService.queueState.failedCount`
3. **Last Sync Time**: `offlineQueueService.queueState.lastSyncTime`
4. **Network Status**: `networkMonitor.isOnline`
5. **Snapshot Health**: DataConsistencyValidator results

### Logging

All offline components use **Timber** for structured logging:
- 📝 Queue operations: "Queued CREATE_INVOICE: 123"
- 🔄 Sync attempts: "SyncWorker: Processing queue..."
- ✅ Success: "Operation 123 synced successfully"
- ❌ Failures: "Failed to sync operation 123: timeout"

**Log Tags:**
- `OfflineQueueService`
- `SyncWorker`
- `NetworkMonitor`
- `SyncPendingOperationsUseCase`

---

## 🚀 Performance Considerations

### Memory

- **In-memory cache**: Limited to current business's pending operations
- **Cleared on logout**: `offlineQueueService.initialize(newBusinessId)`
- **Typical size**: 10-100 operations = ~10-100 KB

### Storage

- **Room database**: Local SQLite (efficient, indexed)
- **Typical usage**: 1 MB per 1,000 operations
- **Cleanup**: Successfully synced operations deleted after 7 days

### Battery

- **WorkManager**: Battery-efficient background work
- **Network constraint**: Only syncs when connected (no polling)
- **Exponential backoff**: Reduces retry frequency over time
- **Batch operations**: Syncs multiple operations together

### Network

- **Batch API calls**: Groups operations when possible
- **Compression**: JSON payloads compressed if large
- **Retry policy**: Smart exponential backoff (not hammering server)
- **Conflict detection**: Uses ETags/version numbers

---

## 🎯 Summary

Bizap's offline functionality is **production-ready** with:

✅ **Comprehensive offline support** - All CRUD operations work offline
✅ **Automatic sync** - No user intervention required
✅ **Smart retry** - Exponential backoff with conflict resolution
✅ **Visual feedback** - Sync status banner on all screens
✅ **Data integrity** - ACID guarantees + consistency validation
✅ **Battery efficient** - WorkManager + network constraints
✅ **Self-healing** - Automatic snapshot repair
✅ **Conflict resolution** - Server-wins strategy
✅ **Developer-friendly** - Clean APIs + comprehensive logging

**Bottom Line:** Users can confidently work offline knowing their data is safe and will sync automatically when back online.
