# Performance Baseline

Performance targets and known bottlenecks for BizAP, established April 2026.

---

## Targets

| Operation | Target | Acceptable | Unacceptable |
|---|---|---|---|
| Invoice creation (save) | < 500 ms | < 1 s | > 2 s |
| PDF generation — Canvas | < 2 s | < 3 s | > 5 s |
| PDF generation — HTML (SASS Pro) | < 3 s | < 4 s | > 6 s |
| Dashboard initial load | < 1 s | < 2 s | > 3 s |
| Invoice list load (1 000 rows) | < 500 ms | < 1 s | > 2 s |
| Customer search (live) | < 150 ms | < 300 ms | > 500 ms |
| Exchange rate fetch | < 2 s | < 4 s | N/A (network) |
| Database migration | < 3 s | < 8 s | > 15 s |

---

## Expected Dataset Sizes

| Entity | Typical | Peak |
|---|---|---|
| Invoices | 500 – 1 000 | 20 000 |
| Line items | 2 000 – 5 000 | 100 000 |
| Customers | 100 – 500 | 5 000 |
| Payments | 500 – 2 000 | 40 000 |
| Analytics snapshots | 365 / year | 3 000 |

---

## Known Bottlenecks

### #1 — PDF Generation (HTML templates)

**Cause:** iText7 HTML-to-PDF conversion is CPU-bound and runs on the calling coroutine.  
**Impact:** 2–4 s for a 10-item invoice.  
**Mitigation options:**
- Move to `Dispatchers.IO` (already done)
- Pre-warm iText7 instance (not yet done)
- Cache compiled CSS from `SassStyleEngine` across calls (not yet done)

### #2 — Analytics Snapshot Queries

**Cause:** `InvoiceAnalyticsSnapshot` aggregation queries scan all rows without date filtering.  
**Impact:** 300–800 ms on 3 000+ snapshots.  
**Mitigation:** Implement date-range index on `snapshot_date`; wire `DateRange` filter in `PaymentAnalyticsTabViewModel`.

### #3 — Dashboard Cold Start

**Cause:** Multiple parallel Room queries on first load; no caching layer.  
**Impact:** 800 ms – 1.5 s on typical dataset.  
**Mitigation:** Load dashboard metrics lazily; show skeleton screens while loading (already partially done).

### #4 — PDF Live Preview (WebView)

**Cause:** Full HTML re-render on every settings change.  
**Impact:** 100–300 ms per re-render, perceived as sluggish.  
**Mitigation:** Debounce already implemented (300 ms); consider partial HTML diffing.

---

## Profiling Commands

```bash
# CPU profile — attach Android Studio Profiler during a specific operation
# View → Tool Windows → Profiler

# Memory profile — check for leaks after PDF generation
# Profiler → Memory → Record native allocations

# Database query trace
adb shell "setprop log.tag.SQLiteStatements VERBOSE"
adb logcat -s SQLiteStatements

# Gradle build time
./gradlew assembleDebug --profile
# Open build/reports/profile/profile-*.html in browser
```

---

## Optimisation Opportunities

| Area | Opportunity | Impact | Effort |
|---|---|---|---|
| SASS CSS | Cache `SassStyleEngine.compile()` result | Medium | Low |
| PDF generation | Pre-warm iText7 on app start | High | Medium |
| Analytics | Add `snapshot_date` index | High | Low |
| Invoice list | Add pagination to Room query | Medium | Low |
| Preview | Throttle WebView reloads to 500 ms | Low | Low |
