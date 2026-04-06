# Architecture Decisions — Bizap v1.0.0

**Date:** March 2026  
**Status:** Current  
**App Package:** `com.emul8r.bizap`

---

## Table of Contents

1. [Option C: No Snapshot Dependency](#1-option-c-no-snapshot-dependency)
2. [Dual GUI Architecture (GUI1 + GUI2)](#2-dual-gui-architecture-gui1--gui2)
3. [Monetary Values in Cents (Long)](#3-monetary-values-in-cents-long)
4. [Non-Blocking Snapshot Sync](#4-non-blocking-snapshot-sync)
5. [Offline-First with Queue Pattern](#5-offline-first-with-queue-pattern)
6. [Room v32 Migration Strategy](#6-room-v32-migration-strategy)
7. [AnalyticsRepositoryBridge](#7-analyticsrepositorybridge)
8. [AccountingService as Single Source](#8-accountingservice-as-single-source)
9. [Dual-Mode UI (MODERN / COMPACT)](#9-dual-mode-ui-modern--compact) ✅ **Completed**
10. [No Mock Data Fallbacks](#10-no-mock-data-fallbacks) ✅ **Completed**

---

## 1. Option C: No Snapshot Dependency

**Decision:** All dashboard and revenue screens read directly from the `invoices` table via `InvoiceDaoV2`, not from pre-computed snapshot tables.

**Why:**
- Eliminates the possibility of stale snapshots misleading users
- Simplifies the data flow (no cache invalidation logic)
- Room queries with SQLite aggregates are fast enough for the expected data volumes
- Snapshots are still maintained as a write-through cache for other use cases (e.g., SnapshotRepairWorker)

**Trade-offs:**
- Slightly higher DB read load on dashboard refresh (acceptable for this scale)
- Snapshot tables exist but are not the source of truth (`USE_SNAPSHOTS_FOR_DASHBOARDS = false`)

**Evidence:**
```kotlin
// SnapshotCachePolicy.kt
const val USE_SNAPSHOTS_FOR_DASHBOARDS = false

// RevenueRepositoryV2.kt
// Option C — no snapshot dependency
// Data is always fresh and consistent
```

---

## 2. Dual GUI Architecture (GUI1 + GUI2)

**Decision:** Maintain two UI implementations (Classic and Modern) that share the same underlying data layer.

**Why:**
- Allows incremental migration from the original UI to the new Compose-based UI
- Users can switch between GUIs without data loss or inconsistency
- Both GUIs are guaranteed to show the same financial data (same DAO)

**Implementation:**
- `TraditionalGUIMainActivity` (GUI1) → `MainScreen` → reads via `InvoiceDaoV2`
- `ModernGUIMainActivity` (GUI2) → `GuiV2NavGraph` → reads via `InvoiceDaoV2`
- `AnalyticsRepositoryBridge` ensures both GUIs use the same repository implementations

**Evidence:**
```kotlin
// AnalyticsRepositoryBridge.kt
// Unifies GUI1/GUI2 under V2 repositories
```

---

## 3. Monetary Values in Cents (Long)

**Decision:** All monetary amounts are stored and computed as `Long` values representing cents (e.g., $1.23 = 123L).

**Why:**
- Avoids floating-point precision errors in financial calculations
- Consistent with Android Room best practices for currency
- Simple display conversion: `amount / 100.0` for formatting
- Avoids `BigDecimal` overhead for single-currency operations

**Example:**
```kotlin
// InvoiceDaoV2.kt
fun observeMTDRevenue(businessId: Long): Flow<Long>  // returns cents

// Display
val displayAmount = amountInCents / 100.0  // convert to dollars
```

---

## 4. Non-Blocking Snapshot Sync

**Decision:** Snapshot cache updates after payment recording are intentionally non-blocking (exceptions are caught and logged, not re-thrown).

**Why:**
- The primary operation (recording a payment) must always succeed
- Cache updates are best-effort; UI reads from live data anyway
- A cache update failure should never prevent a payment from being recorded
- `SnapshotRepairWorker` runs daily to self-heal any drift

**Critical path vs non-critical:**
```kotlin
// CRITICAL PATH — exceptions ARE re-thrown
try {
    createAnalyticsSnapshots(createdEntity, activeBusinessId)
} catch (e: Exception) {
    throw e  // Invoice creation fails if snapshot fails
}

// NON-CRITICAL PATH — exceptions are logged only
} catch (e: Exception) {
    Timber.w(e, "Snapshot sync failed (non-blocking)")
    // DO NOT re-throw — payment is already recorded
}
```

---

## 5. Offline-First with Queue Pattern

**Decision:** All write operations go through an `OfflineQueueService` that buffers operations when the network is unavailable.

**Why:**
- Business users often work in areas with unreliable connectivity
- Data loss in an invoicing app is unacceptable
- WorkManager provides reliable background sync on reconnect

**Conflict resolution:** "Server wins" strategy on HTTP 409 conflicts. This is appropriate for v1.0 where there is no multi-user scenario requiring merge logic.

**Supported offline operations:**
- Invoice CRUD
- Customer CRUD
- Payment recording

---

## 6. Room v32 Migration Strategy

**Decision:** Incremental migrations (v21 → v32) with each migration in a separate file.

**Why:**
- Each migration is independently testable
- Rollback is possible by reverting migration files
- Transparent audit trail of schema changes

**Migration files in:** `Bizap/app/src/main/java/com/emul8r/bizap/data/local/migrations/`

**Key migrations:**
- `Migration_30_31`: Adds `isActive`, `city`, `postalCode` to customers
- `Migration_31_32`: Adds `invoiceNumber`, `isActive`, `createdAt` to invoices; creates `invoice_items` and `payments` tables for GUI2 Phase 2

---

## 7. AnalyticsRepositoryBridge

**Decision:** A bridge layer that routes analytics queries from both GUIs to the V2 repository implementations.

**Why:**
- Prevents duplicate code between GUI1 and GUI2 analytics
- Single point of change if analytics logic needs updating
- Guarantees both GUIs always show the same numbers

**Evidence:** `data/repository/AnalyticsRepositoryBridge.kt`

---

## 8. AccountingService as Single Source

**Decision:** `AccountingService` in `domain/service/` is the single source of truth for all financial calculations, injecting `InvoiceDaoV2` and `PaymentDaoV2` via `@Singleton`.

**Why:**
- Financial calculations are complex and must be consistent
- A singleton ensures the same calculation logic is used everywhere
- Testable in isolation from UI and database layers

**Evidence:** `domain/service/AccountingService.kt`

---

## Future Decisions (v1.1 Planned)

| Decision | Rationale |
|----------|-----------|
| SQLCipher database encryption | Regulatory compliance and user trust for financial data |
| Cloud backup and sync | Business continuity and multi-device support |
| Biometric authentication | Convenience + security upgrade from PIN |
| Multi-user support | Required for team/enterprise use cases |

---

## 9. Dual-Mode UI (MODERN / COMPACT)

**Decision:** A single `GuiV2NavGraph` renders each screen in either a spacious
**Modern** or dense **Compact** layout based on a `UIMode` enum stored in DataStore.

**Why:**
- Satisfies diverse user preferences (small-screen efficiency vs. large-screen comfort)
- Avoids duplicating two separate navigation graphs
- Screen-level conditional rendering (not component-level) keeps each mode's
  code isolated and easy to maintain independently
- `UIMode` lives in `domain/model/` so the preference is accessible from all
  layers without an upward dependency

**Implementation:**
- `UIMode.MODERN` | `UIMode.COMPACT` enum in `domain/model/UIMode.kt`
- `UIPreferences` interface in `domain/settings/` with `UIPreferencesImpl` in `data/settings/`
- `AppStateViewModel.uiMode: StateFlow<UIMode>` + `setUIMode()` action
- Compact dimension tokens in `ui/theme/CompactDimensions.kt`
- Toggle surfaced in **Settings → Appearance** (`SettingsHubScreenV2`)

**Trade-offs:**
- Slightly more code per screen (each screen has Modern + Compact branch)
- Acceptable because each branch is simple and the total complexity is lower
  than maintaining two full navigation graphs

---

## 10. No Mock Data Fallbacks

**Decision:** `PaymentAnalyticsViewModel` and `RevenueAnalyticsViewModel` must
never fall back to hardcoded sample numbers. On data failure they show an
`Error` state.

**Why:**
- Mock data in production creates a false picture of the business health
- Users have no way to distinguish real metrics from fake ones
- An honest `Error` state allows users to diagnose the problem

**Evidence:** `PaymentAnalyticsViewModel.kt`, `RevenueAnalyticsViewModel.kt`

---
