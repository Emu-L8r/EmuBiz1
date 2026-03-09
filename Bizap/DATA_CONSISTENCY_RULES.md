# Data Consistency Rules (Enforced)

## Rule 1: Multi-Tenant Safety
Every invoice query MUST include:
```sql
WHERE businessProfileId = :businessId
```

## Rule 2: Soft-Delete Safety
Every invoice query MUST include:
```sql
WHERE isActive = 1
```

## Rule 3: Status Filtering
Financial metrics MUST exclude DRAFT:
```sql
WHERE status IN ('SENT', 'PARTIALLY_PAID', 'PAID', 'OVERDUE')
```

## Rule 4: Money Unit
- Database: ALWAYS cents (Long)
- UI layer: Convert to dollars only in formatting
- Never mix units

## Rule 5: Date Boundaries
- MTD: First day of current month 00:00 to today 23:59
- YTD: Jan 1 of current year 00:00 to today 23:59
- Use `invoice.date` (creation date), not `updatedAt`

## Rule 6: Payment→Status Sync
When a payment is recorded:
1. Calculate `newAmountPaid = invoice.amountPaid + paymentAmount`
2. Determine `newStatus` based on `newAmountPaid` vs `totalAmount`
3. Update both `amountPaid` AND `status` in a **single atomic transaction**
4. Never leave `invoice.status` stale

Implementation: `PaymentRepositoryV2.recordPayment()` handles this atomically via
`database.withTransaction {}`.

## Rule 7: Query All Through AccountingService
For financial metrics, prefer `AccountingService` over direct DAO queries.
`AccountingService` is the single source of truth; both GUI1 and GUI2 should
obtain numbers through it (or the underlying V2 repositories it delegates to).

---

## Architecture: Single Source of Truth

```
GUI1 screens ─┐
              ├─▶ AccountingService ─▶ InvoiceDaoV2 ─▶ invoices table
GUI2 screens ─┘
```

All calculations use `InvoiceDaoV2`, which queries the `invoices` table directly.
Snapshots are never read for financial calculations (write-through cache only;
see `SnapshotCachePolicy`).

---

## Verification Checklist
- [ ] All DAO queries have `businessProfileId` filter
- [ ] All DAO queries have `isActive = 1` filter
- [ ] Snapshot tables are not used for financial metric reads
- [ ] Payment logic calls `updateStatus()` and `updateAmountPaid()` in one transaction
- [ ] All tests pass
- [ ] GUI1 = GUI2 (numbers identical via AccountingService)
