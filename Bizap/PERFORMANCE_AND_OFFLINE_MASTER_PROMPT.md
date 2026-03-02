# 🚀 MASTER PROMPT: Performance & Offline Pathway for Bizap

**Goal:**  
Make Bizap ultra-fast and robust—responsive UI, smooth scrolling, and reliable data entry in both online and offline scenarios.

**This plan:**  
- Eliminates jank and skipped frames
- Ensures all database/network operations run off the main thread
- Guarantees offline CRUD for invoices/customers and automatic re-sync
- Adds and polishes user feedback for offline/online state
- Leverages WorkManager for smart background sync
- Provides CI-ready steps for code review and long-term code health

---

## 1️⃣ Profile & Eliminate UI Jank

- **Tools:** Android Studio Profiler, `adb shell setprop debug.choreographer.frametime 1`
- **Action:** Instrument and interact with key screens (`InvoiceList`, `Dashboard`).
- **Target:** 
  - No more repeated “Skipped XX frames!” logs.
  - If you see them, use the Profiler to zero in on code:  
    - Large Room or network calls in Compose
    - Expensive recompositions
    - Large unpaginated lists

**Fixes:**
- Extract heavy composables into smaller ones, use `remember`/`derivedStateOf`
- Refactor any ORM or network use to never block the main thread

---

## 2️⃣ Move All DB/Network Off Main Thread

- **Audit:** Find all DAO and network usages (grep for `getAll`, `.execute()`, or synchronous usage).
- **Fix:** Wrap each in proper coroutine logic:
    ```kotlin
    viewModelScope.launch(Dispatchers.IO) { ... }
    ```
    or expose results as `Flow`/`StateFlow` to Compose.
- **Verify:** No direct DB calls in UI code.

---

## 3️⃣ Implement/Upgrade Offline-First Data Layer

**Requirements:**
- Invoice and customer create/edit/delete works offline
- Data is always saved to local Room DB first
- User never loses data if connection is lost

**Sync Logic:**
- Replaying “pending” changes:  
    - Use an operation queue (or `PendingOperation` pattern in Room)
    - On network available, send/replay unsynced changes and clear as acknowledged

---

## 4️⃣ Use WorkManager for Background & Scheduled Sync

- **Daily job:** for tasks like ExchangeRate sync, analytics update, or pending operation replays
- **Ensure all jobs:** Use `ExistingPeriodicWorkPolicy.KEEP`
- **Avoid:** Excessive polling/battery drain—jobs should be scheduled, not polled
- **If jobs already exist:** Review logs, make sure duplicates aren’t spawned

---

## 5️⃣ Gold-standard Offline UX

- **Simulate:** Turn emulator to airplane mode
    - Create/edit invoices/customers, schedule payments, mark status
    - Everything should work seamlessly offline
- **Feedback:** 
    - Show a snackbar or banner when offline:  
      “Working offline, changes will sync automatically.”
    - Hide/disable actions not yet possible offline (if any)

**On reconnect:**  
    - All queued changes sync
    - Success/failure feedback to user (“3 invoices synced!”)

---

## 6️⃣ Optimize Battery, Memory, Scrolling

- **Profile and address:**
    - Large images/bitmaps? Use paging and content scaling
    - Very large data lists? Use `LazyColumn` with paging/snapshots
- **Check:** Average RAM and CPU after creating dozens/hundreds of invoices
- **Remove:** Any direct image decoding in main thread, all memory leaks

---

## 7️⃣ Document & Monitoring

- **Document in README**:
    - “Bizap is fully offline-capable. All features work without a connection and changes will sync transparently.”
- **Set up monitoring**:
    - Enable Firebase Crashlytics and Performance Monitoring
    - Check for ANRs, jank, outliers

---

## 📋 CI READY - COMPLETION CHECKLIST

- [ ] All DB/network ops off main thread (`Dispatchers.IO`)
- [ ] Invoice/customer CRUD is 100% offline-ready
- [ ] WorkManager jobs scheduled smartly (no runaway polling)
- [ ] Offline state produces user feedback; never blocks
- [ ] No “skipped frames” or main-thread warnings under heavy load
- [ ] Large data scenarios scroll smoothly and load within 1 second
- [ ] All background sync tested: both auto and on user reconnection
- [ ] README accurately describes offline behavior and guarantees
