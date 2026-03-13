# SyncWorker Implementation Plan - Week 2 (Phase 2)

**Status:** Draft - Ready for Day 6 implementation
**Objective:** Implement background synchronization for the offline operation queue.

## 1. WorkManager Setup
*   **Worker Class:** `SnapshotRepairWorker` (inherited from `CoroutineWorker`).
*   **Trigger Conditions:** 
    *   Network state: `CONNECTED`
    *   Battery: `NOT_LOW`
    *   Device: `IDLE` (preferred for large syncs)
*   **Scheduling:** 
    *   Immediate: Triggered via `OneTimeWorkRequest` after connection restoration.
    *   Periodic: Runs every 24 hours via `PeriodicWorkRequest` for "Self-Healing".

## 2. Operation Processing Strategy
*   **Order:** strictly First-In-First-Out (FIFO) based on `timestamp_ms`.
*   **Batching:** Process operations one by one to ensure atomic success/failure tracking.
*   **Step-by-Step Flow:**
    1.  Fetch next `PENDING` operation from `OfflineOperationDao`.
    2.  Mark as `SYNCING`.
    3.  Deserialize `entity_data` using `OperationSerializer`.
    4.  Call the corresponding Repository method (e.g., `invoiceRepo.saveInvoice`).
    5.  On Success: Mark as `SYNCED` and cleanup (soft delete or move to log).
    6.  On Failure: Increment `retry_count` and move back to `FAILED` or `PENDING` depending on error type.

## 3. Conflict Resolution Approach
*   **Strategy:** **Last-Write-Wins (LWW)**.
*   **Logic:** 
    *   Compare the `updatedAtMs` of the local operation against the remote entity timestamp.
    *   If local is newer, overwrite remote.
    *   If remote is newer, discard local update and notify user (optional logging).
*   **Conflict Types:**
    *   Invoice Edited Online & Offline: LWW based on timestamp.
    *   Deleted Locally but Edited Remotely: Deletion wins.

## 4. Retry Logic & Error Handling
*   **Backoff Policy:** `Exponential Backoff` (Initial delay: 30 seconds).
*   **Max Retries:** 6 attempts.
*   **Error Categorization:**
    *   `Transient` (Network Timeout): Trigger retry.
    *   `Fatal` (Auth Error, Validation Error): Mark as `FAILED`, log to Crashlytics, notify user.

## 5. UI & State Flow
*   **Observation:** ViewModels observe `OfflineQueueService.queueState`.
*   **Feedback:** 
    *   `isSyncing == true`: Show small progress spinner in the header.
    *   `totalPending == 0`: Remove all pending badges.
    *   `failedCount > 0`: Show "Sync Issues" warning in Settings.

---
**Next Action:** Day 6 - Implement `SnapshotRepairWorker` core loop.
