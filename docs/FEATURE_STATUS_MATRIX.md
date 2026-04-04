# Feature Status Matrix

Tracks every feature across GUI1 (legacy Activities) and GUI2 (Compose), with current production status, priority, and notes.

> **Last updated:** April 2026  
> **GUI2 target:** 100% feature parity by Q4 2026  
> **GUI1 EOL:** June 2027

---

## Feature Matrix

| Feature | GUI1 | GUI2 | Status | Priority | Notes |
|---|:---:|:---:|---|:---:|---|
| **Invoice CRUD** | ✅ | ✅ | Production | Critical | Shared ViewModel; identical logic |
| **PDF Generation (Canvas)** | ✅ | ✅ | Production | Critical | 4 canvas templates |
| **PDF Generation (HTML)** | ✅ | ✅ | Production | Critical | 7 styles incl. SASS Professional |
| **PDF Live Preview** | ❌ | ✅ | Production | High | Settings screen, WebView with debounce |
| **SASS Style Engine** | ❌ | ✅ | Production | High | `SassStyleEngine.kt` — design-token CSS compiler |
| **Payment Tracking (single)** | ✅ | ✅ | Production | Critical | Full payment recording |
| **Payment Tracking (partial)** | ✅ | ✅ | Production | Critical | Partial amount support |
| **Payment History** | ✅ | ✅ | Production | High | Timeline view per invoice |
| **Multi-Currency** | ✅ | ✅ | Production | High | 30+ currencies, live exchange rates |
| **Tax Integration** | ✅ | ✅ | Production | High | Per-invoice + global tax settings |
| **Analytics Dashboard** | ✅ | ✅ | Production | High | Revenue, payments, customer tabs |
| **Business Profiles** | ✅ | ✅ | Production | High | Multi-business support |
| **Customer Management** | ✅ | ✅ | Production | High | CRUD + analytics per customer |
| **Email Validation** | ✅ | ✅ | Production | Medium | Regex + format check |
| **Dunning Management** | ✅ | ✅ | Production | Medium | Overdue tracking and reminders |
| **Advanced Search/Filter** | ❌ | ✅ | Production | Medium | Date + status + amount filters |
| **QR Code on PDFs** | ❌ | 🔧 | In-Dev | Medium | `PdfQrCodeRenderer` — needs zxing |
| **Backup / Restore** | ❌ | 🔧 | In-Dev | Medium | Infrastructure ready; operations stubbed |
| **Date Range Analytics** | ❌ | 🔧 | In-Dev | Medium | Filter snapshots by date range |
| **Banking Details in PDF** | 🔧 | 🔧 | In-Dev | Medium | Mapper needs BusinessProfile fetch |
| **Analytics Drill-down** | ❌ | 🔧 | In-Dev | Low | Click chart point for details |
| **Mobile Optimisation** | ❌ | ✅ | Production | Low | Responsive Compose layouts |
| **Photo Attachments** | ❌ | ✅ | Production | Low | Camera + gallery integration |
| **Offline Support** | ✅ | ✅ | Production | Low | PendingOperationEntity queue |
| **Notes on Invoices** | ✅ | ✅ | Production | Low | NoteEntity per invoice |
| **Custom Colour Schemes** | ❌ | ✅ | Production | Low | Canvas + HTML palette picker |

---

## Status Definitions

| Status | Meaning |
|---|---|
| **Production** | Fully implemented, tested, and in use |
| **In-Dev** | Implementation started; not production-ready |
| **Experimental** | Proof-of-concept only; may be removed |
| **Deprecated** | Scheduled for removal (see GUI1 EOL) |

## Priority Definitions

| Priority | Meaning |
|---|---|
| **Critical** | App unusable without this |
| **High** | Core value proposition |
| **Medium** | Significant user value |
| **Low** | Nice-to-have / polish |

---

## GUI2 Gaps (Action Items)

The following features are missing from GUI2 or partially implemented:

1. **QR Code PDFs** — Add `zxing` dependency; wire `PdfQrCodeRenderer`
2. **Backup/Restore ops** — Implement `resetAllData()`, `resetCustomerData()`, `resetInvoiceData()` in `BackupRestoreViewModel`
3. **Date Range Analytics** — Add `DateRange` parameter to `PaymentAnalyticsTabViewModel`
4. **Banking Details in PDF** — Fetch `BusinessProfile` in `InvoiceTemplateDataMapper`

See [docs/TODO_IMPLEMENTATION_CHECKLIST.md](TODO_IMPLEMENTATION_CHECKLIST.md) for implementation details.
