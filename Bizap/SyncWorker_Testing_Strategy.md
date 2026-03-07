# SyncWorker Testing Strategy - Week 2 (Phase 2)

**Objective:** Verify the reliability and correctness of the background sync process.

## 1. Automated Unit Tests
*   **Worker Initialization:** Test that `SnapshotRepairWorker` can be instantiated and triggered via WorkManager.
*   **Success Flow:** Mock successful repository calls and verify operations move from `SYNCING` to `SYNCED`.
*   **Retry Logic:** Mock transient network failures and verify `retry_count` increments and WorkManager triggers a retry.
*   **Failure Handling:** Mock fatal errors (e.g., 400 Bad Request) and verify operation moves to `FAILED` status with an error message.

## 2. Integration Tests (Simulated Network)
*   **FIFO Verification:** Queue 10 operations and verify they are processed in exact chronological order.
*   **Data Integrity:** Verify that the JSON payload in `entity_data` matches the final state in the "remote" repository mock.
*   **Atomic Sync:** Verify that if an operation fails, the next ones in the queue remain `PENDING`.

## 3. Conflict Resolution Scenarios
*   **Scenario A: Local Wins:** Local `updatedAtMs` is newer than remote. Verify remote is updated.
*   **Scenario B: Remote Wins:** Remote timestamp is newer. Verify local operation is discarded/logged.
*   **Scenario C: Concurrent Deletion:** Entity deleted locally but edited remotely. Verify deletion wins.

## 4. Manual E2E Scenarios (Week 2 Day 10)
*   **Step 1:** Enable Airplane Mode.
*   **Step 2:** Perform 5 distinct operations (Create Invoice, Record Payment, etc.).
*   **Step 3:** Disable Airplane Mode.
*   **Step 4:** Verify "Pending Sync" badges disappear within 30 seconds.
*   **Step 5:** Verify all data appears correctly in both local and remote-simulated views.

## 5. Performance & Resource Testing
*   **Memory:** Check for leaks during large queue processing (100+ items).
*   **Battery:** Verify sync constraints (Network: CONNECTED) are respected by the OS.

---
**Success Criteria:** 100% of queued operations reach a terminal state (SYNCED or FAILED) without data loss or application crashes.
