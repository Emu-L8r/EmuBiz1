# Known Issues and Monitoring — Bizap v1.0.0

**Date:** March 2026  
**Status:** Living document — updated as issues are discovered and resolved

---

## Philosophy

Transparency builds trust. This document openly lists everything that is known to be imperfect in v1.0.0, along with monitoring strategies and planned resolution timelines. No software is perfect on day one; what matters is knowing what to watch and having a clear plan to improve.

---

## Known Limitations (By Design in v1.0)

### KL-01: No Database Encryption
**Severity:** Medium  
**Status:** Accepted for v1.0, fix planned for v1.1  
**Details:**  
The SQLite database is stored in the app's private directory (`/data/data/com.emul8r.bizap/databases/`). On non-rooted Android devices, this directory is inaccessible to other apps and users. However, on rooted devices or via ADB backup (if backup is not disabled), the database could theoretically be extracted.

**Mitigation in v1.0:**
- App directory is private (Android default security)
- PIN authentication required to use the app
- `android:allowBackup="false"` in manifest prevents ADB backup extraction

**Resolution:** SQLCipher integration planned for v1.1 (1-2 weeks post-launch).

**User-facing impact:** Minimal for typical users on non-rooted devices.

---

### KL-02: No Cloud Backup
**Severity:** Medium  
**Status:** Accepted for v1.0, fix planned for v1.1  
**Details:**  
All data is stored locally on the device. Uninstalling the app, factory resetting the device, or device failure results in permanent data loss.

**Mitigation in v1.0:**
- Export individual invoices as PDFs for record-keeping
- Standard Android backup (if enabled on device) may include app data — but this is not guaranteed

**Resolution:** Cloud backup and sync planned for v1.1 (2-3 weeks post-launch).

**User communication:** Mention this limitation clearly in onboarding/settings.

---

### KL-03: Single-User Only
**Severity:** Low  
**Status:** Accepted for v1.0  
**Details:**  
The app supports one business profile per device. Multiple users sharing a device, or teams needing to collaborate on the same invoice data, are not supported.

**Conflict resolution:** "Server wins" strategy if online conflicts occur.

**Resolution:** Multi-user support planned for v1.2+ (4+ weeks post-launch).

---

### KL-04: Snapshot Tables Not Used for Dashboard
**Severity:** None (it's working as intended)  
**Status:** Intentional architecture decision  
**Details:**  
The app has snapshot tables (`InvoiceAnalyticsSnapshot`, `DailyRevenueSnapshot`, `InvoicePaymentSnapshot`) that were originally designed as a performance cache. By design, these snapshots are NOT used as the data source for dashboard queries (`USE_SNAPSHOTS_FOR_DASHBOARDS = false`). The dashboard reads directly from the `invoices` table.

**Why this is not a bug:** Direct table reads ensure the dashboard always shows fresh, accurate data without risk of snapshot staleness.

**Monitoring:** `SnapshotRepairWorker` runs daily to keep snapshots in sync with live data. Any drift is visible in logs.

---

### KL-05: Dual GUI in Transition
**Severity:** Low  
**Status:** Temporary state  
**Details:**  
Two GUI implementations coexist: GUI1 (Classic) and GUI2 (Modern). Both are functional and show identical data, but the team is gradually migrating to GUI2 as the primary UI.

**User impact:** None — users may switch freely between GUIs. Data is always consistent.

**Resolution:** GUI1 will be deprecated in v1.2 after GUI2 feature parity is confirmed in the wild.

---

## Monitoring Plan

### Crash Rate Monitoring

**Tool:** Google Play Console → Android Vitals → Crashes & ANRs  
**Target:** < 1% crash rate  
**Alert threshold:** > 2% in any 24-hour window  

Key crashes to watch for:
```
com.emul8r.bizap.data.repository.InvoiceRepositoryImpl
com.emul8r.bizap.data.local.AppDatabase
com.emul8r.bizap.ui.gui2.*
```

### ANR (Application Not Responding) Monitoring

**Tool:** Google Play Console → Android Vitals  
**Target:** < 0.5% ANR rate  
**Alert threshold:** > 1% in any 24-hour window  

Common ANR causes to watch:
- Main thread DB access (should be zero — we use coroutines exclusively)
- Sync operations blocking UI

### Revenue Accuracy Spot-Check

Every Monday for the first 4 weeks post-launch, manually verify:
```bash
# Check that InvoiceDaoV2 revenue queries are returning expected values
# Compare against manually calculated expected values for test account
```

If users report incorrect dashboard numbers, immediately run:
```sql
-- Diagnostic query (run via ADB shell sqlite3)
SELECT status, COUNT(*), SUM(amountPaid) 
FROM invoices 
WHERE businessProfileId = 1 
GROUP BY status;
```

### Sync Queue Health

**Check:** Ensure sync queue doesn't grow unbounded  
**Frequency:** Weekly for first month  
**Indicator:** `OfflineOperationDao.getPendingOperations()` count should return to 0 after each sync

### User Reviews

- Monitor Play Store reviews daily for first 2 weeks
- Respond to 1-star reviews within 24 hours
- Key phrases to watch for: "wrong amount", "data lost", "dashboard", "$0"

---

## Active Issues

*(Updated as issues are discovered post-launch)*

| Issue ID | Description | Severity | Status | Target Fix |
|----------|-------------|----------|--------|-----------|
| — | No active issues at launch | — | — | — |

---

## Resolved Issues (Pre-Launch)

| Issue ID | Description | Resolution |
|----------|-------------|------------|
| PRE-01 | Dashboard showed $0 when PARTIALLY_PAID invoices existed | Fixed: Revenue queries now include both `PAID` and `PARTIALLY_PAID` |
| PRE-02 | GUI1 and GUI2 could show different revenue totals | Fixed: Both now read from `InvoiceDaoV2` via `AnalyticsRepositoryBridge` |
| PRE-03 | 31 test files failed to compile after Kotlin/Gradle update | Fixed: Test suite recovered March 10, 2026 (all 936 tests passing) |
| PRE-04 | SnapshotRepairWorker drift detection was blocking operations | Fixed: Made non-blocking, drift detection is advisory only |

---

## Escalation Process

**Minor issue (crash rate 1-2%, UX complaints):**
1. Investigate root cause (< 4 hours)
2. Create hotfix branch
3. Fix, test, submit `v1.0.1` to Play Store

**Major issue (crash rate > 5%, data loss reports):**
1. Immediately halt rollout in Play Console
2. Investigate root cause (< 2 hours)
3. Consider rolling back to previous version (if possible)
4. Fix, test, submit as priority update

**Critical issue (security vulnerability, widespread data corruption):**
1. Immediately request emergency expedited review from Play Store
2. Notify affected users via in-app notification on next launch
3. Provide data recovery assistance if applicable

---

## Contact for Monitoring Alerts

Set up Play Console email alerts for:
- Crash rate exceeds threshold
- ANR rate exceeds threshold
- New 1-star review posted

All alerts → team engineering email list.
